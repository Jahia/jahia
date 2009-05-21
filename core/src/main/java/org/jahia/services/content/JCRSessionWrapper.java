/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.jahia.services.usermanager.JahiaUser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.security.AccessControlException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 6, 2009
 * Time: 12:11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRSessionWrapper implements Session {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JCRSessionWrapper.class);

    private JCRStoreService service;
    private JahiaUser user;
    private boolean isSystem = true;
    private Credentials credentials;
    private JCRWorkspaceWrapper workspace;
    private boolean isLive = true;

    private Map<JCRStoreProvider, Session> sessions = new HashMap<JCRStoreProvider, Session>();

    public JCRSessionWrapper(JahiaUser user, Credentials credentials, boolean isSystem, String workspace, JCRStoreService service) {
        this.user = user;
        this.isSystem = isSystem;
        this.credentials = credentials;
        this.workspace = new JCRWorkspaceWrapper(workspace, this, service);
        this.service = service;
    }



    public Node getRootNode() throws RepositoryException {
        JCRStoreProvider provider = service.getProvider("/");
        return provider.getNodeWrapper("/", this);
    }

    public Repository getRepository() {
        return service;
    }

    public String getUserID() {
        return ((SimpleCredentials)credentials).getUserID();
    }

    public Object getAttribute(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAttributeNames() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JCRWorkspaceWrapper getWorkspace() {
        return workspace;
    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        Map<String,JCRStoreProvider> providers = service.getProviders();

        for (JCRStoreProvider provider : providers.values()) {
            try {
                Session session = getProviderSession(provider);
                Node n = session.getNodeByUUID(uuid);
                return provider.getNodeWrapper(n, this);
            } catch (ItemNotFoundException ee) {
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug("getNodeByUUID unsupported by : "+provider.getKey() + " / " + provider.getClass().getName());
            }
        }
        throw new ItemNotFoundException(uuid);
    }

    public Item getItem(String path) throws PathNotFoundException, RepositoryException {
        Map<String, JCRStoreProvider> dynamicMountPoints = service.getDynamicMountPoints();
        for (Iterator<String> iterator = dynamicMountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (path.startsWith(mp+"/")) {
                String localPath = path.substring(mp.length());
                JCRStoreProvider provider = dynamicMountPoints.get(mp);
//                Item item = getProviderSession(provider).getItem(localPath);
                Item item = getProviderSession(provider).getItem(provider.encodeInternalName(localPath));
                if (item.isNode()) {
                    return provider.getNodeWrapper((Node) item, this);
                } else {
                    return provider.getPropertyWrapper((Property) item, this);
                }
            }
        }
        Map<String, JCRStoreProvider> mountPoints = service.getMountPoints();
        for (Iterator<String> iterator = mountPoints.keySet().iterator(); iterator.hasNext();) {
            String mp = iterator.next();
            if (mp.equals("/") || path.equals(mp) || path.startsWith(mp+"/")) {
                String localPath = path;
                if (!mp.equals("/")) {
                    localPath = path.substring(mp.length());
                }
                JCRStoreProvider provider = mountPoints.get(mp);
                if (localPath.equals("")) {
                    localPath = "/";
                }
//                Item item = getProviderSession(provider).getItem(localPath);
                Item item = getProviderSession(provider).getItem(provider.encodeInternalName(localPath));
                if (item.isNode()) {
                    return provider.getNodeWrapper((Node) item, this);
                } else {
                    return provider.getPropertyWrapper((Property) item, this);
                }
            }
        }
        throw new PathNotFoundException(path);
    }

    public JCRNodeWrapper getNode(String path) throws PathNotFoundException, RepositoryException {
        return (JCRNodeWrapper) getItem(path);
    }

    public boolean itemExists(String path) throws RepositoryException {
        try {
            getItem(path);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public void move(String source, String dest) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        ((JCRNodeWrapper)getItem(source)).moveFile(dest);
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        for (Session session : sessions.values()) {
            session.save();
        }
    }

    public void refresh(boolean b) throws RepositoryException {
        for (Session session : sessions.values()) {
            session.refresh(b);
        }
    }

    public boolean hasPendingChanges() throws RepositoryException {
        for (Session session : sessions.values()) {
            if (session.hasPendingChanges()) {
                return true;
            }
        }
        return false;
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkPermission(String s, String s1) throws AccessControlException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void exportSystemView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException, SAXException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void exportSystemView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException, PathNotFoundException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void exportDocumentView(String s, ContentHandler contentHandler, boolean b, boolean b1) throws PathNotFoundException, SAXException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void exportDocumentView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException, PathNotFoundException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setNamespacePrefix(String s, String s1) throws NamespaceException, RepositoryException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespaceURI(String s) throws NamespaceException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespacePrefix(String s) throws NamespaceException, RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void logout() {
        for (Session session : sessions.values()) {
            session.logout();
        }
        isLive = false;
    }

    public boolean isLive() {
        return isLive;
    }

    public void addLockToken(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getLockTokens() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeLockToken(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Session getProviderSession(JCRStoreProvider provider) throws RepositoryException {
        if (sessions.get(provider) == null) {
            Session s = provider.getSession(credentials, workspace.getName());
            sessions.put(provider, s);
        }
        return sessions.get(provider);
    }

    public JahiaUser getUser() {
        return user;
    }
}
