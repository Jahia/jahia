/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.jahia;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.content.ContentObject;
import org.jahia.bin.Jahia;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.*;

/**
 * Implementation of the JCR Session. 
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:04:39
 */
public class SessionImpl implements Session {

    private static final transient Logger logger = Logger.getLogger(SessionImpl.class);
    
    private JahiaUser jahiaUser;
    private String userId;
    private RepositoryImpl repository;
    private WorkspaceImpl workspace;
    private Map<String, NodeImpl> nodesByPath = new HashMap();
    private Map<String, NodeImpl> nodesByUUID = new HashMap();
    private Set<ItemImpl> modifiedItems = new HashSet<ItemImpl>();

    public SessionImpl(RepositoryImpl repository, JahiaUser jahiaUser, String userId) throws RepositoryException {
        this.repository = repository;
        this.jahiaUser = jahiaUser;
        this.userId = userId;
    }

    public ProcessingContext getProcessingContext(JahiaSite jahiaSite) throws RepositoryException {
        return Jahia.getThreadParamBean();
    }

    public RepositoryImpl getRepository() {
        return repository;
    }

    public String getUserID() {
        return userId;
    }

    public Object getAttribute(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getAttributeNames() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = (WorkspaceImpl) workspace;
    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public Node getRootNode() throws RepositoryException {
        if (nodesByPath.get("/") == null) {
            return new JahiaRootNodeImpl(this);
        }
        return nodesByPath.get("/");
    }

    public Node getNodeByUUID(String uuid) throws ItemNotFoundException,
            RepositoryException {
        NodeImpl node = nodesByUUID.get(uuid);
        if (node == null) {
            try {
                List l = ServicesRegistry.getInstance().getJahiaPageService()
                        .findPagesByPropertyNameAndValue("uuid", uuid);
                if (!l.isEmpty()) {
                    node = getJahiaPageNode((ContentPage) l.iterator().next());
                }
                if (node == null) {
                    l = ServicesRegistry.getInstance()
                            .getJahiaContainersService()
                            .findContainersByPropertyNameAndValue("uuid", uuid);
                    if (!l.isEmpty()) {
                        node = getJahiaContainerNode((ContentContainer) l
                                .iterator().next());
                    }
                }
                if (node == null) {
                    l = ServicesRegistry.getInstance()
                            .getJahiaContainersService()
                            .findContainerListsByPropertyNameAndValue("uuid",
                                    uuid);
                    if (!l.isEmpty()) {
                        node = getJahiaContainerListNode((ContentContainerList) l
                                .iterator().next());
                    }
                }
                if (node == null) {
                    l = getRepository().getSitesService()
                            .findSiteByPropertyNameAndValue("uuid", uuid);
                    if (!l.isEmpty()) {
                        node = getJahiaSiteNode((JahiaSite) l.iterator().next());
                        nodesByUUID.put(uuid, node);
                    }
                }
                if (node != null) {
                    nodesByUUID.put(uuid, node);
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (node == null) {
            throw new ItemNotFoundException(uuid);
        }

        return node;
    }

    public Item getItem(String s) throws PathNotFoundException, RepositoryException {
        Node n = getRootNode();
        StringTokenizer st = new StringTokenizer(s,"/");
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            try {
                n = n.getNode(name);
            } catch (PathNotFoundException e) {
                return n.getProperty(name);
            }
        }
        return n;
    }

    public boolean itemExists(String s) throws javax.jcr.RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void move(String s, String s1) throws ItemExistsException, PathNotFoundException, VersionException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void save() throws AccessDeniedException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, RepositoryException {
        for (ItemImpl item : modifiedItems) {
            item.save();
        }
    }

    public void refresh(boolean b) throws RepositoryException {
    }

    public boolean hasPendingChanges() throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkPermission(String path, String actions) throws AccessControlException, RepositoryException {
        Item i = getItem(path);
        int perm = JahiaBaseACL.READ_RIGHTS;
        if (actions.contains("add_node") || actions.contains("set_property") || actions.contains("remove")) {
            perm = JahiaBaseACL.WRITE_RIGHTS;
        }
        if (i instanceof JahiaContentNodeImpl) {
            if (!((JahiaContentNodeImpl)i).getContentObject().checkAccess(jahiaUser, perm, false)) {
                throw new AccessControlException(path);
            }
        }
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
        Map params = new HashMap();
        params.put(ImportExportService.EXPORT_FORMAT, ImportExportService.DOCUMENT_EXPORTER);

        ContentObject object = null;
        Item i = getItem(s);
        if (i instanceof JahiaPageNodeImpl) {
            object = ((JahiaPageNodeImpl)i).getContentObject();
        } else if (i instanceof JahiaContainerNodeImpl) {
            object =  ((JahiaContainerNodeImpl)i).getContentObject();
        }
        if (object != null ) {
            try {
                ProcessingContext processingContext = getProcessingContext(((ItemImpl) i).getSite());
                ServicesRegistry.getInstance().getImportExportService().export(object, processingContext.getLocale().toString(), contentHandler, new HashSet(), processingContext, params);
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void exportDocumentView(String s, OutputStream outputStream, boolean b, boolean b1) throws IOException, PathNotFoundException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void setNamespacePrefix(String s, String s1) throws NamespaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getNamespaceURI(String s) throws NamespaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getNamespacePrefix(String s) throws NamespaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void logout() {
        workspace.close();
    }

    public void addLockToken(String s) {
    }

    public String[] getLockTokens() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeLockToken(String s) {
    }

    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ValueFactory() {
            public Value createValue(String s) {
                return new ValueImpl(s, PropertyType.STRING);
            }

            public Value createValue(String s, int i) throws ValueFormatException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Value createValue(long l) {
                return new ValueImpl(l);
            }

            public Value createValue(double v) {
                return new ValueImpl(v);
            }

            public Value createValue(boolean b) {
                return new ValueImpl(b);
            }

            public Value createValue(Calendar calendar) {
                return new ValueImpl(calendar);
            }

            public Value createValue(InputStream inputStream) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Value createValue(Node node) throws RepositoryException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    public boolean isLive() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    void addModifiedItem(ItemImpl i) {
        modifiedItems.add(i);
    }

    NodeImpl getJahiaRootNode() throws RepositoryException {
        if (nodesByPath.get("/") == null) {
            NodeImpl res = new JahiaRootNodeImpl(this);
            nodesByPath.put("/", res);
        }
        return nodesByPath.get("/");
    }

    NodeImpl getJahiaSiteNode(JahiaSite site)throws RepositoryException {
        String path = "/" + site.getSiteKey();
        NodeImpl node = nodesByPath.get(path);
        if (node == null || ((JahiaSiteNodeImpl)node).getSite() != site) {
            node = new JahiaSiteNodeImpl(this, site);
            nodesByPath.put(path, node);
        }
        return node;
    }

    NodeImpl getJahiaPageNode(ContentPage page)throws RepositoryException {
        return getContentNode(page);
    }

    NodeImpl getJahiaContainerListNode(ContentContainerList list) throws RepositoryException{
        return getContentNode(list);
    }

    NodeImpl getJahiaContainerNode(ContentContainer cont)throws RepositoryException {
        return getContentNode(cont);
    }

    private JahiaContentNodeImpl getContentNode(ContentObject obj)
            throws RepositoryException {
        String uuid = JahiaContentNodeImpl.getUUID(obj);
        JahiaContentNodeImpl node = (JahiaContentNodeImpl) nodesByUUID
                .get(uuid);
        if (node == null || node.getContentObject() != obj) {
            if (obj instanceof ContentPage) {
                node = new JahiaPageNodeImpl(this, (ContentPage) obj);
            } else if (obj instanceof ContentContainerList) {
                node = new JahiaContainerListNodeImpl(this,
                        (ContentContainerList) obj);
            } else if (obj instanceof ContentContainer) {
                node = new JahiaContainerNodeImpl(this, (ContentContainer) obj);
            } else {
                throw new IllegalArgumentException(
                        "Unknown content object type for object: " + obj);
            }
            nodesByPath.put(node.getPath(), node);
            nodesByUUID.put(uuid, node);
        }

        return node;
    }

}
