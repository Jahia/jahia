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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QueryManagerImpl;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Jahia specific wrapper around <code>javax.jcr.Workspace</code> to be able to inject
 * Jahia specific actions and to manage workspaces on multiple repository providers in
 * the backend. 
 * 
 * Jahia services should use this wrapper rather than the original workspace interface to 
 * ensure that we manipulate wrapped nodes and not the ones from the underlying 
 * implementation.
 *
 * @author toto
 */
public class JCRWorkspaceWrapper implements Workspace {
    private JCRSessionFactory service;
    private String name;
    private JCRSessionWrapper session;
    private JCRObservationManager observationManager;

    public JCRWorkspaceWrapper(String name, JCRSessionWrapper session, JCRSessionFactory service) {
        this.name = name;
        this.service = service;
        this.session = session;;
    }

    public JCRSessionWrapper getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void copy(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().copy(source, dest);
        }
    }

    public void copy(String srcWs, String source, String dest) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().copy(srcWs, source, dest);
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public void clone(String srcWs, String source, String dest, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            throw new UnsupportedRepositoryOperationException();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            session.getProviderSession(provider).getWorkspace().clone(srcWs,source,dest,removeExisting);
        }
    }

    public void move(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        move(source, dest, false);
    }

    void move(String source, String dest, boolean sessionMove) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        JCRStoreProvider provider = service.getProvider(source);
        JCRStoreProvider destProvider = service.getProvider(dest);
        if (destProvider != provider) {
            try {
                session.getItem(dest);
                throw new ItemExistsException(dest);
            } catch (RepositoryException e) {
            }

            copy(source,dest);
            session.getItem(source).remove();
        } else {
            if (provider.getMountPoint().length()>1) {
                dest = dest.substring(provider.getMountPoint().length());
                source = source.substring(provider.getMountPoint().length());
            }
            JCRNodeWrapper sourceNode = session.getNode(source);
            if (sourceNode.isNodeType("jmix:shareable")) {
                JCRNodeWrapper parentNode = session.getNode(StringUtils.substringBeforeLast(dest,"/"));
                parentNode.clone(sourceNode, StringUtils.substringAfterLast(dest,"/"));
                List<Value> values = new ArrayList<Value>();
                String v = sourceNode.getPath() + ":::" + dest;
                if (sourceNode.hasProperty("j:movedFrom")) {
                    values.addAll(Arrays.asList(sourceNode.getProperty("j:movedFrom").getValues()));
                    for (Value value : values) {
                        String s = value.getString();
                        if (s.endsWith(":::"+sourceNode.getPath())) {
                            v = StringUtils.substringBefore(s,":::") + ":::" + dest;
                            values.remove(value);
                            break;
                        }
                    }
                }
                values.add(getSession().getValueFactory().createValue(v));
                sourceNode.setProperty("j:movedFrom", values.toArray(new Value[values.size()]));
                sourceNode.remove();
            } else {
                if (sessionMove) {
                    session.getProviderSession(provider).move(source, dest);
                } else {
                    session.getProviderSession(provider).getWorkspace().move(source, dest);
                }
            }
        }
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public QueryManager getQueryManager() {
        return new QueryManagerImpl(session, service);
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return service.getNamespaceRegistry();
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return NodeTypeRegistry.getInstance();
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        if (observationManager == null) {
            this.observationManager = new JCRObservationManager(this);
        }
        return observationManager;
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {      
        return getSession().getProviderSession(service.getProvider("/")).getWorkspace().getVersionManager();
    }

    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

}
