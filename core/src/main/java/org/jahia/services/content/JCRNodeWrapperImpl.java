/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.*;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.jcr.version.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.AccessControlException;
import java.util.*;

/**
 * Wrappers around <code>javax.jcr.Node</code> to be able to inject
 * Jahia specific actions.
 *
 * @author toto
 */
public class JCRNodeWrapperImpl extends JCRItemWrapperImpl implements JCRNodeWrapper {
    protected static final Logger logger = Logger.getLogger(JCRNodeWrapper.class);

    protected Node objectNode = null;
    protected JCRFileContent fileContent = null;
    protected JCRSiteNode site = null;
    protected Map<Locale, Node> i18NobjectNodes = null;

    protected static String[] defaultPerms = {Constants.JCR_READ_RIGHTS_LIVE, Constants.JCR_READ_RIGHTS, Constants.JCR_WRITE_RIGHTS, Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, Constants.JCR_WRITE_RIGHTS_LIVE};
    protected static Map<String,List<String>> defaultDependencies;
    
    static {
        defaultDependencies = new HashMap<String, List<String>>();
        defaultDependencies.put(Constants.JCR_READ_RIGHTS, Arrays.asList(Constants.JCR_READ_RIGHTS_LIVE));
        defaultDependencies.put(Constants.JCR_WRITE_RIGHTS, Arrays.asList(Constants.JCR_READ_RIGHTS));
        defaultDependencies.put(Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, Arrays.asList(Constants.JCR_WRITE_RIGHTS));
        defaultDependencies.put(Constants.JCR_WRITE_RIGHTS_LIVE, Arrays.asList(Constants.JCR_READ_RIGHTS_LIVE));
    }

    private static final String J_PRIVILEGES = "j:privileges";

    private transient Map<String, String> propertiesAsString;

    protected JCRNodeWrapperImpl(Node objectNode, String path, JCRSessionWrapper session, JCRStoreProvider provider) {
        super(session, provider);
        this.objectNode = objectNode;
        setItem(objectNode);
        if (path != null) {
            this.localPath = path;
        } else {
            try {
                this.localPath = objectNode.getPath();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Node getRealNode() {
        return objectNode;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        if (localPath.equals("/") || localPath.equals(provider.getRelativeRoot())) {
            if (provider.getMountPoint().equals("/")) {
                throw new ItemNotFoundException();
            }
            return (JCRNodeWrapper) session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(), "/"));
        } else {
            return (JCRNodeWrapper) session.getItem(StringUtils.substringBeforeLast(getPath(), "/"));
        }
    }

    /**
     * {@inheritDoc}
     */
    public JahiaUser getUser() {
        return session.getUser();
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String[]>> getAclEntries() {
        try {
            Map<String, List<String[]>> permissions = new HashMap<String, List<String[]>>();
            Map<String, List<String[]>> inheritedPerms = new HashMap<String, List<String[]>>();

            recurseonACPs(permissions, inheritedPerms, objectNode);
            for (Map.Entry<String,List<String[]>> s : inheritedPerms.entrySet()) {
                if (permissions.containsKey(s.getKey())) {
                    List<String[]> l = permissions.get(s.getKey());
                    l.addAll(s.getValue());
                } else {
                    permissions.put(s.getKey(), s.getValue());
                }
            }
            return permissions;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    public Map<String, Map<String, String>> getActualAclEntries() {
        Map<String, Map<String, String>> actualACLs = new HashMap<String, Map<String, String>>();
        Map<String, List<String[]>> allACLs = getAclEntries();
        if (allACLs != null) {
            for (Map.Entry<String, List<String[]>> entry : allACLs.entrySet()) {
                Map<String, String> permissionsForUser = new HashMap<String, String>();
                // filtering stuff (path, GRANT/DENY, jcr:perm)
                for (String[] perms : entry.getValue()) {
                    if (permissionsForUser.containsKey(perms[2])) {
                        if (perms[0].equals(getPath())) {
                            permissionsForUser.put(perms[2], perms[1]);
                        }
                    } else {
                        permissionsForUser.put(perms[2], perms[1]);
                    }
                }
                actualACLs.put(entry.getKey(), permissionsForUser);
            }
        }
        return actualACLs;
    }

    private void recurseonACPs(Map<String, List<String[]>> results, Map<String, List<String[]>> inherited, Node n) throws RepositoryException {
        try {
            Map<String, List<String[]>> current = results;
            while (true) {
                if (n.hasNode("j:acl")) {
                    Node acl = n.getNode("j:acl");
                    NodeIterator aces = acl.getNodes();
                    Map<String, List<String[]>> localResults = new HashMap<String, List<String[]>>();
                    while (aces.hasNext()) {
                        Node ace = aces.nextNode();
                        if (ace.isNodeType("jnt:ace")) {
                            String principal = ace.getProperty("j:principal").getString();
                            String type = ace.getProperty("j:aceType").getString();
                            Value[] privileges = ace.getProperty("j:privileges").getValues();

                            if (!current.containsKey(principal)) {
                                List<String[]> p = localResults.get(principal);
                                if (p == null) {
                                    p = new ArrayList<String[]>();
                                    localResults.put(principal, p);
                                }
                                for (Value privilege : privileges) {
                                    p.add(new String[]{n.getPath(), type, privilege.getString()});
                                }
                            }
                        }
                    }
                    current.putAll(localResults);
                    if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                        return;
                    }
                }
                n = n.getParent();
                current = inherited;
            }
        } catch (ItemNotFoundException e) {
            logger.debug(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getAvailablePermissions() {
        return Collections.singletonMap("default", Arrays.asList(defaultPerms));
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getPermissionsDependencies() {
        return defaultDependencies;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWriteable() {
        //        if (isNodeType("jnt:mountPoint")) {
//            return getUser().isAdminMember(0);
//        }
        JCRStoreProvider jcrStoreProvider = getProvider();
        if (jcrStoreProvider.isDynamicallyMounted()) {
            try {
                return ((JCRNodeWrapper) session.getItem(jcrStoreProvider.getMountPoint())).hasPermission(WRITE);
            } catch (RepositoryException e) {
                logger.error("Cannot get node", e);
                return false;
            }
        }
        return hasPermission(WRITE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String perm) {
        String ws = null;
        int permissions = 0;
        try {
            if (READ.equals(perm)) {
                permissions = Permission.READ;
                ws = "default";
            } else if (WRITE.equals(perm)) {
                permissions = Permission.ADD_NODE;
                ws = "default";
            } else if (MODIFY_ACL.equals(perm)) {
                permissions = Permission.MODIFY_AC;
                ws = "default";
            } else if (READ_LIVE.equals(perm)) {
                permissions = Permission.READ;
                ws = Constants.LIVE_WORKSPACE;
            } else if (WRITE_LIVE.equals(perm)) {
                permissions = Permission.ADD_NODE;
                ws = Constants.LIVE_WORKSPACE;
            }
            if (ws == null) {
                return false;
            }
            // todo all the code below should only use the JCR 2.0 API !
            if (ws.equals(workspace.getName())) {
                Session providerSession = session.getProviderSession(provider);
                if (providerSession instanceof SessionImpl) {
                SessionImpl jrSession = (SessionImpl) session.getProviderSession(provider);
                Path path = jrSession.getQPath(localPath).getNormalizedPath();
                return jrSession.getAccessManager().isGranted(path, permissions);
            } else {
                    // this is not a Jackrabbit implementation, we will use the new JCR 2.0 API instead.
                    AccessControlManager accessControlManager = providerSession.getAccessControlManager();
                    if (accessControlManager != null) {
                        List<Privilege> privileges = convertPermToPrivileges(perm, accessControlManager);
                        return accessControlManager.hasPrivileges(localPath, privileges.toArray(new Privilege[privileges.size()]));
                    }
                    return true;
                }
            } else {
                Session providerSession = provider.getCurrentUserSession(Constants.LIVE_WORKSPACE);
                if (providerSession instanceof SessionImpl) {
                SessionImpl jrSession = (SessionImpl) provider.getCurrentUserSession(Constants.LIVE_WORKSPACE);
                Node current = this;
                while (true) {
                    try {
                        Path path = jrSession.getQPath(current.getCorrespondingNodePath(ws)).getNormalizedPath();
                        return jrSession.getAccessManager().isGranted(path, permissions);
                    } catch (ItemNotFoundException nfe) {
                        // corresponding node not found
                        try {
                            current = current.getParent();
                        } catch (AccessDeniedException e) {
                            return false;
                        } catch (ItemNotFoundException e) {
                            return false;
                        }
                    }
                }
                } else {
                    // we are not dealing with a Jackrabbit session, we will use the JCR 2.0 API instead.
                    AccessControlManager accessControlManager = providerSession.getAccessControlManager();
                    if (accessControlManager != null) {
                        Node current = this;
                        List<Privilege> privileges = convertPermToPrivileges(perm, accessControlManager);
                        while (true) {
                            try {
                                return accessControlManager.hasPrivileges(current.getCorrespondingNodePath(ws), privileges.toArray(new Privilege[privileges.size()]));
                            } catch (ItemNotFoundException infe) {
                                try {
                                    current = current.getParent();
                                } catch (AccessDeniedException ade) {
                                    return false;
                                } catch (ItemNotFoundException infe2) {
                                    return false;
            }
                            }
                        }
                    }
                    return true;
                }
            }
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
            return false;
        }
    }

    private List<Privilege> convertPermToPrivileges(String perm, AccessControlManager accessControlManager) throws RepositoryException {
        List<Privilege> privileges = new ArrayList<Privilege>();
        if (READ.equals(perm) || READ_LIVE.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_READ));
        } else if (WRITE.equals(perm) || WRITE_LIVE.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_ADD_CHILD_NODES));
        } else if (MODIFY_ACL.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_MODIFY_ACCESS_CONTROL));
        }
        return privileges;
    }

    /**
     * {@inheritDoc}
     */
    public boolean changePermissions(String user, String perm) {
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    public boolean changePermissions(String user, Map<String, String> perm) {
        try {
            changePermissions(objectNode, user, perm);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokePermissions(String user) {
        try {
            revokePermission(objectNode, user);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokeAllPermissions() {
        try {
            if (objectNode.hasNode("j:acl")) {
                objectNode.getNode("j:acl").remove();
                objectNode.removeMixin("jmix:accessControlled");
                return true;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    public boolean getAclInheritanceBreak() {
        try {
            return getAclInheritanceBreak(objectNode);
        } catch (RepositoryException e) {
            logger.error("Cannot get acl", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean setAclInheritanceBreak(boolean inheritance) {
        try {
            setAclInheritanceBreak(objectNode, inheritance);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return addNode(name, JNT_FOLDER);
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException {
        if (!isCheckedOut()) {
            checkout();
        }
        JCRNodeWrapper file = null;
        try {
            file = getNode(name);
        } catch (PathNotFoundException e) {
            logger.debug("file " + name + " does not exist, creating...");
            file = addNode(name, JNT_FILE);
        }
        if (file != null) {
            file.getFileContent().uploadFile(is, contentType);
        } else {
            logger.error("can't write to file " + name + " because it doesn't exist");
        }
        return file;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper addNode(String name) throws RepositoryException {
        Node n = objectNode.addNode(name);
        return provider.getNodeWrapper(n, buildSubnodePath(name), session);
    }

    private String buildSubnodePath(String name) {
        if (localPath.equals("/")) {
            return localPath + name;
        } else {
            return localPath + "/" + name;
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        Node n = objectNode.addNode(name, type);
        return provider.getNodeWrapper(n, buildSubnodePath(name), session);
    }

    public JCRNodeWrapper addNode(String name, String type, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        if (objectNode instanceof NodeImpl) {
            JahiaSessionImpl jrSession = (JahiaSessionImpl) objectNode.getSession();

            jrSession.getNodeTypeInstanceHandler().setCreated(created);
            jrSession.getNodeTypeInstanceHandler().setCreatedBy(createdBy);
            jrSession.getNodeTypeInstanceHandler().setLastModified(lastModified);
            jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(lastModifiedBy);
            try {
                if (identifier != null) {
                    org.jahia.services.content.nodetypes.Name jahiaName = new org.jahia.services.content.nodetypes.Name(name, NodeTypeRegistry.getInstance().getNamespaces());
                    Name qname = NameFactoryImpl.getInstance().create(jahiaName.getUri() == null ? "" : jahiaName.getUri(), jahiaName.getLocalName());
                    Name typeName = null;
                    if (type != null) {
                        org.jahia.services.content.nodetypes.Name jahiaTypeName = NodeTypeRegistry.getInstance()
                                .getNodeType(type).getNameObject();
                        typeName = NameFactoryImpl.getInstance().create(jahiaTypeName.getUri(),
                                jahiaTypeName.getLocalName());
                    }
                    Node child = ((NodeImpl) objectNode).addNode(qname, typeName, org.apache.jackrabbit.core.id.NodeId.valueOf(identifier));
                    return provider.getNodeWrapper(child, buildSubnodePath(name), session);
                } else {
                    return addNode(name, type);
                }
            } finally {
                jrSession.getNodeTypeInstanceHandler().setCreated(null);
                jrSession.getNodeTypeInstanceHandler().setCreatedBy(null);
                jrSession.getNodeTypeInstanceHandler().setLastModified(null);
                jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(null);
            }
        } else {
            return addNode(name, type);
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRPlaceholderNode getPlaceholder() throws RepositoryException {
        return new JCRPlaceholderNode(this);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated As of JCR 2.0, {@link #getIdentifier()} should be used instead.
     */
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return objectNode.getUUID();
    }

    /**
     * {@inheritDoc}
     */
    public String getStorageName() {
        String uuid;
        try {
            uuid = objectNode.getIdentifier();
        } catch (RepositoryException e) {
            uuid = localPath;
        }
        return provider.getKey() + ":" + uuid;
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsoluteUrl(ServletRequest request) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(request) + getUrl();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrl() {
        if (objectNode != null) {
            return provider.getHttpPath() + "/" + getSession().getWorkspace().getName() + getPath();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsoluteWebdavUrl(final HttpServletRequest request) {
        if (objectNode != null) {
            return provider.getAbsoluteContextPath(request) + getWebdavUrl();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getWebdavUrl() {
        if (objectNode != null) {
            try {
                return provider.getWebdavPath() + objectNode.getPath();
            } catch (RepositoryException e) {
                logger.error("Cannot get file path", e);
            }
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getThumbnails() {
        List<String> names = new ArrayList<String>();
        try {
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node.isNodeType("jnt:resource")) {
                    if (!node.getName().equals("jcr:content")) {
                        names.add(node.getName());
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    public String getThumbnailUrl(String name) {
        return getWebdavUrl() + "/" + name;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getThumbnailUrls() {
        List<String> list = getThumbnails();
        Map<String, String> map = new HashMap<String, String>(list.size());
        for (String thumbnailName : list) {
            map.put(thumbnailName, getThumbnailUrl(thumbnailName));
        }
        return map;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    public List<JCRNodeWrapper> getChildren() {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        try {
            NodeIterator nodes = getNodes();
            while (nodes.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                list.add(node);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public List<JCRNodeWrapper> getEditableChildren() {
        List<JCRNodeWrapper> list = getChildren();
        list.add(new JCRPlaceholderNode(this));
        return list;
    }

    /**
     * @see #getNodes(String)
     * @deprecated
     */
    public List<JCRNodeWrapper> getChildren(String name) {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        try {
            NodeIterator nodes = getNodes(name);
            while (nodes.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) nodes.next();
                list.add(node);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes() throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getSessionFactory().getMountPoints();
            for (Map.Entry<String, JCRStoreProvider> entry : mountPoints.entrySet()) {
                if (!entry.getKey().equals("/")) {
                    String mpp = entry.getKey().substring(0, entry.getKey().lastIndexOf('/'));
                    if (mpp.equals("")) mpp = "/";
                    if (mpp.equals(getPath())) {
                        JCRStoreProvider storeProvider = entry.getValue();
                        String root = storeProvider.getRelativeRoot();
                        final Node node = session.getProviderSession(storeProvider).getNode(root.length() == 0 ? "/" : root);
                        list.add(storeProvider.getNodeWrapper(node, "/", session));
                    }
                }
            }
        }

        NodeIterator ni = objectNode.getNodes();

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            if (session.getLocale() == null || !node.getName().startsWith("j:translation_")) {
                try {
                    JCRNodeWrapper child = provider.getNodeWrapper(node, buildSubnodePath(node.getName()), session);
                    list.add(child);
                } catch (PathNotFoundException e) {
                    if (logger.isDebugEnabled())
                        logger.debug(e.getMessage(), e);
                }
            }
        }
        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes(String name) throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> mountPoints = provider.getSessionFactory().getMountPoints();

            if (mountPoints.containsKey(getPath() + "/" + name)) {
                JCRStoreProvider storeProvider = mountPoints.get(getPath() + "/" + name);
                final Node node = session.getProviderSession(storeProvider).getNode(storeProvider.getRelativeRoot());
                list.add(storeProvider.getNodeWrapper(node, "/", session));
            }
        }

        NodeIterator ni = objectNode.getNodes(name);

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            JCRNodeWrapper child = provider.getNodeWrapper(node, buildSubnodePath(node.getName()), session);
            list.add(child);
        }

        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        if (objectNode.hasNode(s)) {
            return provider.getNodeWrapper(objectNode.getNode(s), buildSubnodePath(s), session);
        }
        List<JCRNodeWrapper> c = getChildren();
        for (JCRNodeWrapper jcrNodeWrapper : c) {
            if (jcrNodeWrapper.getName().equals(s)) {
                return jcrNodeWrapper;
            }
        }
        throw new PathNotFoundException(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible() {
        try {
            Property hidden = objectNode.getProperty("j:hidden");
            return hidden == null || !hidden.getBoolean();
        } catch (RepositoryException e) {
            return true;
        }
    }

    /**
     * Returns a lazy map for accessing node properties with string values.
     * 
     * @return a lazy map for accessing node properties with string values
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getPropertiesAsString() {
        if (propertiesAsString == null) {
            Map<String, String> res = Collections.emptyMap();
            res = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {
                public Object transform(Object input) {
                    String name = (String) input;
                    try {
                        if (hasProperty(name)) {
                            Property p = getProperty(name);
                            return p.getString();
                        }
                    } catch (RepositoryException e) {
                        logger.error("Repository error while retrieving property " + name, e);
                    }
                    return null;
                }
            });
            propertiesAsString = res;
        }

        return propertiesAsString;
    }

    public Map<String, String> getAllPropertiesAsString() throws RepositoryException {
        if (propertiesAsString == null) {
            Map<String, String> res = new HashMap<String, String>();
            PropertyIterator pi = getProperties();
            if (pi != null) {
                while (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    if (p.getType() == PropertyType.BINARY) {
                        continue;
                    }
                    if (!p.isMultiple()) {
                        res.put(p.getName(), p.getString());
                    } else {
                        Value[] vs = p.getValues();
                        StringBuffer b = new StringBuffer();
                        for (int i = 0; i < vs.length; i++) {
                            Value v = vs[i];
                            b.append(v.getString());
                            if (i + 1 < vs.length) {
                                b.append(" ");
                            }
                        }
                        res.put(p.getName(), b.toString());
                    }
                }
            }
            propertiesAsString = res;
        }

        return propertiesAsString;
    }    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        try {
            if ((objectNode.getPath().equals("/") || objectNode.getPath().equals(provider.getRelativeRoot())) && provider.getMountPoint().length() > 1) {
                String mp = provider.getMountPoint();
                return mp.substring(mp.lastIndexOf('/') + 1);
            } else {
                return objectNode.getName(); //JCRContentUtils.decodeInternalName(name);
            }
        } catch (RepositoryException e) {
            logger.error("Repository error", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeType getPrimaryNodeType() throws RepositoryException {
        try {
            return NodeTypeRegistry.getInstance().getNodeType(objectNode.getPrimaryNodeType().getName());
        } catch (NoSuchNodeTypeException e) {
            return NodeTypeRegistry.getInstance().getNodeType("nt:base");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getPrimaryNodeTypeName() throws RepositoryException {
        return objectNode.getPrimaryNodeType().getName();
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> l = new ArrayList<NodeType>();
        for (NodeType nodeType : objectNode.getMixinNodeTypes()) {
            l.add(NodeTypeRegistry.getInstance().getNodeType(nodeType.getName()));
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.addMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        objectNode.removeMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        return objectNode.canAddMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public ExtendedNodeDefinition getDefinition() throws RepositoryException {
        NodeDefinition definition = objectNode.getDefinition();
        if (definition == null) {
            return null;
        }
        ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(definition.getDeclaringNodeType().getName());
        if (definition.getName().equals("*")) {
            for (ExtendedNodeDefinition d : nt.getUnstructuredChildNodeDefinitions().values()) {
                return d;
            }

        } else {
            return nt.getNodeDefinition(definition.getName());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getNodeTypes() throws RepositoryException {
        List<String> results = new ArrayList<String>();
        results.add(objectNode.getPrimaryNodeType().getName());
        NodeType[] mixin = objectNode.getMixinNodeTypes();
        for (int i = 0; i < mixin.length; i++) {
            NodeType mixinType = mixin[i];
            results.add(mixinType.getName());
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNodeType(String type) {
        try {
            return objectNode.isNodeType(type);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCollection() {
        return true;
//        try {
//            return objectNode.isNodeType("jmix:collection") || objectNode.isNodeType("nt:folder") || objectNode.getPath().equals("/");
//        } catch (RepositoryException e) {
//            return false;
//        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFile() {
        try {
            return objectNode.isNodeType(Constants.NT_FILE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPortlet() {
        try {
            return objectNode.isNodeType(Constants.JAHIANT_PORTLET);
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Date getLastModifiedAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getContentLastModifiedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (PathNotFoundException pnfe) {
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getLastPublishedAsDate() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getContentLastPublishedAsDate() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.LASTPUBLISHED).getDate().getTime();
        } catch (PathNotFoundException pnfe) {
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Date getCreationDateAsDate() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATED).getDate().getTime();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getCreationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_CREATEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getModificationUser() {
        try {
            return objectNode.getProperty(Constants.JCR_LASTMODIFIEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getPublicationUser() {
        try {
            return objectNode.getProperty(Constants.LASTPUBLISHEDBY).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getLanguage() {
        String language = null;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            try {
                language = getI18N(locale).getProperty("jcr:language").getString();
            } catch (Exception e) {
                language = getSession().getLocale().toString();
            }
        }
        return language;
    }

    /**
     * Return the internationalization node, containing localized properties
     *
     * @param locale
     * @return
     */
    public Node getI18N(Locale locale) throws RepositoryException {
        return getI18N(locale, true);
    }

    private boolean hasI18N(Locale locale) throws RepositoryException {
        return ((i18NobjectNodes != null && i18NobjectNodes.containsKey(locale)) || objectNode.hasNode("j:translation_"+locale));
    }

    private Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
        //getSession().getLocale()
        if (i18NobjectNodes == null) {
            i18NobjectNodes = new HashMap<Locale, Node>();
        }
        Node node;
        if (i18NobjectNodes.containsKey(locale)) {
            node = i18NobjectNodes.get(locale);
            if (node != null) {
                return node;
            }
        } else if (objectNode.hasNode("j:translation_"+locale)) {
            node = objectNode.getNode("j:translation_"+locale);
            i18NobjectNodes.put(locale, node);
            return node;
        }

        if (fallback) {
            final Locale fallbackLocale = getSession().getFallbackLocale();
            if (fallbackLocale != null && fallbackLocale != locale) {
                return getI18N(fallbackLocale);
            }
        }
        throw new ItemNotFoundException(locale.toString());
    }

    public Node getOrCreateI18N(Locale locale) throws RepositoryException {
        try {
            return getI18N(locale, false);
        } catch (RepositoryException e) {
            Node t = objectNode.addNode("j:translation_"+locale, Constants.JAHIANT_TRANSLATION);
            t.setProperty("jcr:language", locale.toString());

            i18NobjectNodes.put(locale, t);
            return t;
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                try {
                    final Node localizedNode = getI18N(locale);
                    return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                            session, provider, getApplicablePropertyDefinition(name),
                            name);
                } catch (ItemNotFoundException e) {
                    return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
                }
            }
        }
        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getProperties() throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale);
        }
        return new LazyPropertyIterator(this);
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getProperties(String s) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, s);
        }
        return new LazyPropertyIterator(this, null, s);
    }


    /**
     * {@inheritDoc}
     */
    public String getPropertyAsString(String name) {
        return getPropertiesAsString().get(name);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (value != null && PropertyType.UNDEFINED != epd.getRequiredType() && value.getType() != epd.getRequiredType()) {
            value = getSession().getValueFactory().createValue(value.getString(), epd.getRequiredType());
        }
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        if (value == null) {
            objectNode.setProperty(name, value);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value, type), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        if (value == null) {
            objectNode.setProperty(name, value);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null && PropertyType.UNDEFINED != epd.getRequiredType() && values[i].getType() != epd.getRequiredType()) {
                    values[i] = getSession().getValueFactory()
                            .createValue(values[i].getString(), epd.getRequiredType());
                }
            }
        }

        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);

        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values, type), session, provider, getApplicablePropertyDefinition(name), name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] v = null;
        if (values != null) {
            v = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    v[i] = getSession().getValueFactory().createValue(values[i]);
                } else {
                    v[i] = null;
                }
            }
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value[] v = null;
        if (values != null) {
            v = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    v[i] = getSession().getValueFactory().createValue(values[i], type);
                } else {
                    v[i] = null;
                }
            }
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value, type);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            if (value instanceof JCRNodeWrapper) {
                value = ((JCRNodeWrapper) value).getRealNode();
            }
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);

            v = getSession().getValueFactory().createValue(value, epd.getRequiredType() == PropertyType.WEAKREFERENCE);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Binary value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value);
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, BigDecimal value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = null;
        if (value != null) {
            v = getSession().getValueFactory().createValue(value);
        }
        return setProperty(name, v);
    }


    /**
     * {@inheritDoc}
     */
    public boolean hasProperty(String propertyName) throws RepositoryException {
        boolean result = objectNode.hasProperty(propertyName);
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null && !propertyName.equals("jcr:language")) {
            try {
                ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(propertyName);
                if (epd != null && epd.isInternationalized()) {
                    if (hasI18N(locale)) {
                        final Node localizedNode = getI18N(locale);
                        return localizedNode.hasProperty(propertyName);
                    }
                }
            } catch (ConstraintViolationException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProperties() throws RepositoryException {
        boolean result = objectNode.hasProperties();
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            if (hasI18N(locale)) {
                return getI18N(locale).hasProperties();
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException {
        String pref = objectNode.getSession().getNamespacePrefix(namespace);
        String key = pref + ":" + name;
        return setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    public List<JCRItemWrapper> getAncestors() throws RepositoryException {
        List<JCRItemWrapper> ancestors = new ArrayList<JCRItemWrapper>();
        for (int i = 0; i < getDepth(); i++) {
            try {
                ancestors.add(getAncestor(i));
            } catch (AccessDeniedException ade) {
                return ancestors;
            }
        }
        return ancestors;
    }

    /**
     * {@inheritDoc}
     */
    public boolean rename(String newName) throws RepositoryException {
        if (!isCheckedOut()) {
            checkout();
        }
        JCRNodeWrapper parent = getParent();
        if (!parent.isCheckedOut()) {
            parent.checkout();
        }

        // the following code is use to conserve the ordering when renaming a node, we do this only if the parent
        // node is orderable.
        String nextNodeName = null;
        boolean nodePositionFound = false;
        if (parent.getPrimaryNodeType().hasOrderableChildNodes()) {
            NodeIterator nodeIterator = parent.getNodes();
            while (nodeIterator.hasNext()) {
                Node currentNode = nodeIterator.nextNode();
                if (currentNode.getIdentifier().equals(getIdentifier())) {
                    nodePositionFound = true;
                    if (nodeIterator.hasNext()) {
                        nextNodeName = nodeIterator.nextNode().getName();
                    } else {
                        // do nothing, we will keep null as the nextNode value
                    }
                    break;
                }
            }
        }

        getSession().move(getPath(), parent.getPath() + "/" + newName);
        this.localPath = parent.getPath() + "/" + newName;
        this.objectNode = getSession().getNode(localPath);
        if ((nodePositionFound) && (parent.getPrimaryNodeType().hasOrderableChildNodes())) {
            parent.orderBefore(newName, nextNodeName);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(String dest) throws RepositoryException {
        return copy(dest, getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(String dest, String name) throws RepositoryException {
        JCRNodeWrapper node = (JCRNodeWrapper) session.getItem(dest);
        boolean sameProvider = (provider.getKey().equals(node.getProvider().getKey()));
        if (!sameProvider) {
            copy(node, name, true);
            node.save();
        } else {
            copy(node, name, true);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes) throws RepositoryException {
        Map<String, List<String>> references = new HashMap<String, List<String>>();
        boolean copy = copy(dest, name, allowsExternalSharedNodes, references);
        ReferencesHelper.resolveCrossReferences(getSession(), references);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references) throws RepositoryException {
        JCRNodeWrapper copy = null;
        try {
            copy = (JCRNodeWrapper) session
                    .getItem(dest.getPath() + "/" + name);
            if (!copy.isCheckedOut()) {
                copy.checkout();
            }
        } catch (PathNotFoundException ex) {
            // node does not exist
        }

        final Map<String, String> uuidMapping = getSession().getUuidMapping();

        if (copy == null || copy.getDefinition().allowsSameNameSiblings()) {
            if (!dest.isCheckedOut() && dest.isVersioned()) {
                session.checkout(dest);
            }
            String typeName = getPrimaryNodeTypeName();
            copy = dest.addNode(name, typeName);
        }

        try {
            NodeType[] mixin = objectNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                copy.addMixin(aMixin.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Error adding mixin types to copy", e);
        }

        if (copy != null) {
            uuidMapping.put(getIdentifier(), copy.getIdentifier());
            if (hasProperty("jcr:language")) {
                copy.setProperty("jcr:language", getProperty("jcr:language").getString());
            }
            copyProperties(copy, references);
        }

        NodeIterator ni = getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper source = (JCRNodeWrapper) ni.next();
            if (source.isNodeType("mix:shareable")) {
                if (uuidMapping.containsKey(source.getIdentifier())) {
                    // ugly save because to make node really shareable
                    session.save();
                    copy.clone(session.getNodeByUUID(uuidMapping.get(source.getIdentifier())), source.getName());
                } else if (allowsExternalSharedNodes) {
                    copy.clone(source, source.getName());
                } else {
                    source.copy(copy, source.getName(), allowsExternalSharedNodes, references);
                }
            } else {
                source.copy(copy, source.getName(), allowsExternalSharedNodes, references);
            }
        }

        return true;
    }

    public void copyProperties(JCRNodeWrapper destinationNode, Map<String, List<String>> references) throws RepositoryException {
        PropertyIterator props = getProperties();

        while (props.hasNext()) {
            Property property = props.nextProperty();
            try {
                if (!property.getDefinition().isProtected() && !Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                    if (property.getType() == PropertyType.REFERENCE || property.getType() == PropertyType.WEAKREFERENCE) {
                        if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            Value[] values = property.getValues();
                            for (Value value : values) {
                                keepReference(destinationNode, references, property, value.getString());
                            }
                        } else {
                            keepReference(destinationNode, references, property, property.getValue().getString());
                        }
                    }
                    if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                        destinationNode.setProperty(property.getName(), property.getValues());
                    } else {
                        destinationNode.setProperty(property.getName(), property.getValue());
                    }
                }
            } catch (Exception e) {
                logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
            }
        }
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property, String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }


    /**
     * {@inheritDoc}
     */
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        item.remove();
    }

    /**
     * {@inheritDoc}
     */
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        return objectNode.lock(isDeep, isSessionScoped);
    }

    /**
     * {@inheritDoc}
     * @param type
     */
    public boolean lockAndStoreToken(String type) throws RepositoryException {
        String l = (getSession().isSystem()?" system ":getSession().getUserID())+":"+type;

        if (!isNodeType("jmix:lockable")) {
            return false;
        }
        if (!objectNode.isLocked()) {
            lockNode(objectNode);
        }

        addLockTypeValue(objectNode, l);

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION)) {
            Node trans = getOrCreateI18N(session.getLocale());
            if (!trans.isLocked()) {
                lockNode(trans);
            }
            addLockTypeValue(trans, l);
        }
        objectNode.getSession().save();
        return true;
    }

    private void lockNode(final Node objectNode) throws RepositoryException {
        Lock lock = objectNode.lock(false, false);
        if (lock.getLockToken() != null) {
            try {
                if (!objectNode.isCheckedOut()) {
                    objectNode.checkout();
                }
                objectNode.setProperty("j:locktoken", lock.getLockToken());
//                objectNode.getSession().removeLockToken(lock.getLockToken());
            } catch (RepositoryException e) {
                logger.error("Cannot store token for "+getPath(),e);
                objectNode.unlock();
            }
        } else {
            logger.error("Lost lock ! "+ objectNode.getPath());
        }
    }

    private void addLockTypeValue(final Node objectNode, String l) throws RepositoryException {
        if (!objectNode.isCheckedOut()) {
            objectNode.checkout();
        }
        if (objectNode.hasProperty("j:lockTypes")) {
            Property property = objectNode.getProperty("j:lockTypes");
            Value[] oldValues = property.getValues();
            for (Value value : oldValues) {
                if (value.getString().equals(l)) {
                    return;
                }
            }
            Value[] newValues = new Value[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = getSession().getValueFactory().createValue(l);
            property.setValue(newValues);
        } else {
            objectNode.setProperty("j:lockTypes", new String[]{l});
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean isLocked() {
        try {
            return objectNode.isLocked();
        } catch (RepositoryException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLockable() {
        try {
            return objectNode.isNodeType(Constants.MIX_LOCKABLE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public List<Locale> getLockedLocales() throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = objectNode.getNodes("j:translation*");
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (n.isLocked()) {
                r.add(new Locale(n.getProperty("jcr:language").getString()));
            }
        }
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public javax.jcr.lock.Lock getLock() {
        try {
            final javax.jcr.lock.Lock lock = objectNode.getLock();
            return new javax.jcr.lock.Lock() {
                public String getLockOwner() {
                    return lock.getLockOwner();
                }

                public boolean isDeep() {
                    return lock.isDeep();
                }

                public long getSecondsRemaining() throws RepositoryException {
                    return lock.getSecondsRemaining();
                }

                public boolean isLockOwningSession() {
                    return lock.isLockOwningSession();
                }

                public Node getNode() {
                    return JCRNodeWrapperImpl.this;
                }

                public String getLockToken() {
                    return lock.getLockToken();
                }

                public boolean isLive() throws RepositoryException {
                    return lock.isLive();
                }

                public boolean isSessionScoped() {
                    return lock.isSessionScoped();
                }

                public void refresh() throws LockException, RepositoryException {
                    lock.isSessionScoped();
                }
            };
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        unlock("user");
    }

    public void unlock(String type)
            throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
            InvalidItemStateException, RepositoryException {
        if (!isLocked()) {
            throw new LockException("Node not locked");
        }

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION) && hasI18N(session.getLocale())) {
            Node trans = getI18N(session.getLocale(), false);
            unlock(trans, type);
        }

        if (!isNodeType(Constants.JAHIANT_TRANSLATION) && !getLockedLocales().isEmpty()) {
            throw new LockException("Translations still locked");
        }

        unlock(objectNode, type);

    }

    private void unlock(final Node objectNode, String type) throws RepositoryException {
        if (hasProperty("j:locktoken")) {
            Property property = objectNode.getProperty("j:locktoken");
            String token = property.getString();
            Value[] types = objectNode.getProperty("j:lockTypes").getValues();
            for (Value value : types) {
                String owner = StringUtils.substringBefore(value.getString(),":");
                String currentType = StringUtils.substringAfter(value.getString(),":");
                if (currentType.equals(type)) {
                    if (getSession().isSystem() || getSession().getUserID().equals(owner)) {
                        final Map<String,Value> valueList = new HashMap<String, Value>();
                        for (Value v : types) {
                            valueList.put(v.getString(),v);
                        }
                        valueList.remove(value.getString());
                        if (!objectNode.isCheckedOut()) {
                            objectNode.checkout();
                        }
                        if (valueList.isEmpty()) {
                            objectNode.getSession().addLockToken(token);
                            objectNode.unlock();
                            property.remove();
                            objectNode.getProperty("j:lockTypes").remove();
                        } else {
                            objectNode.setProperty("j:lockTypes", valueList.values().toArray(new Value[valueList.size()]));
                        }
                        getSession().save();

                    } else {
                        throw new LockException("Not owner of lock");
                    }
                }
            }
        } else {
            objectNode.unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean holdsLock() throws RepositoryException {
        return objectNode.holdsLock();
    }

    /**
     * {@inheritDoc}
     */
    public String getLockOwner() {
        if (getLock() == null) {
            return null;
        }
        if ("shared".equals(provider.getAuthenticationType())) {
            return getLock().getLockOwner();
        } else {
            return getSession().getUserID();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void versionFile() {
        try {
            objectNode.addMixin(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error("Error while adding versionable mixin type", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVersioned() {
        try {
            return objectNode.isNodeType(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error("Error while checking if object node is versioned", e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void checkpoint() {
        try {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKPOINT, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    objectNode.checkin();
                    objectNode.checkout();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error setting checkpoint", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getVersions() {
        List<String> results = new ArrayList<String>();
        try {
            VersionHistory vh = objectNode.getVersionHistory();
            VersionIterator vi = vh.getAllVersions();

            // forget root version
            vi.nextVersion();

            while (vi.hasNext()) {
                Version version = vi.nextVersion();
                results.add(version.getName());
            }
        } catch (RepositoryException e) {
            logger.error("Error while retrieving versions", e);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public List<Version> getVersionsAsVersion() {
        List<Version> results = new ArrayList<Version>();
        try {
            VersionHistory vh = objectNode.getVersionHistory();
            VersionIterator vi = vh.getAllVersions();

            // forget root version
            vi.nextVersion();

            while (vi.hasNext()) {
                Version version = vi.nextVersion();
                results.add(version);
            }
            Collections.sort(results, new Comparator<Version>() {
                public int compare(Version o1, Version o2) {
                    try {
                        return o1.getCreated().compareTo(o2.getCreated());
                    } catch (RepositoryException e) {
                        return -1;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving versions", e);
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    public List<VersionInfo> getVersionInfos() throws RepositoryException {
        return ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(session, this);
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getFrozenVersion(String name) {
        try {
            Version v = objectNode.getVersionHistory().getVersion(name);
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            return provider.getNodeWrapper(frozen, session);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving frozen version", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getFrozenVersionAsRegular(Date versionDate) {
        try {
            VersionHistory vh = objectNode.getSession().getWorkspace().getVersionManager().getVersionHistory(objectNode.getPath());
            Version v = JCRVersionService.findClosestVersion(vh, versionDate);
            if (v == null) {
                return null;
            }
            Node frozen = v.getNode(Constants.JCR_FROZENNODE);
            return new JCRFrozenNodeAsRegular(provider.getNodeWrapper(frozen, session), versionDate);
        } catch (UnsupportedRepositoryOperationException e) {
            if(getSession().getVersionDate()==null) {
                logger.error("Error while retrieving frozen version", e);
            }
        } catch (RepositoryException e) {
            logger.error("Error while retrieving frozen version", e);
        }
        return null;
    }

    /**
     * Change the permissions of a user on the given node.
     *
     * @param objectNode The node on which to change permission
     * @param user       The user to update
     * @param perm       the permission to update for the user
     * @throws RepositoryException
     */
    public static void changePermissions(Node objectNode, String user, String perm) throws RepositoryException {
        Map<String, String> permsAsMap = new HashMap<String, String>();
        perm = perm.toLowerCase();
        if (perm.length() == 2) {
            if (perm.charAt(0) == 'r') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "DENY");
            }
            if (perm.charAt(1) == 'w') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "GRANT");
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "DENY");
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "DENY");
            }
        } else if (perm.length() == 5) {
            if (perm.charAt(0) == 'r') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS_LIVE, "DENY");
            }
            if (perm.charAt(1) == 'e') {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_READ_RIGHTS, "DENY");
            }
            if (perm.charAt(2) == 'w') {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS, "DENY");
            }
            if (perm.charAt(3) == 'a') {
                permsAsMap.put(Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_MODIFYACCESSCONTROL_RIGHTS, "DENY");
            }
            if (perm.charAt(4) == 'p') {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS_LIVE, "GRANT");
            } else {
                permsAsMap.put(Constants.JCR_WRITE_RIGHTS_LIVE, "DENY");
            }
        }
        changePermissions(objectNode, user, permsAsMap);
    }

    /**
     * Change the permissions of a user on the given node.
     *
     * @param objectNode The node on which to change permissions
     * @param user       The user to update
     * @param perms      A map with the name of the permission, and "GRANT" or "DENY" as a value
     * @throws RepositoryException
     */
    public static void changePermissions(Node objectNode, String user, Map<String, String> perms) throws RepositoryException {
        List<String> gr = new ArrayList<String>();
        List<String> den = new ArrayList<String>();

        for (Map.Entry<String, String> entry : perms.entrySet()) {
            if ("GRANT".equals(entry.getValue())) {
                gr.add(entry.getKey());
            } else if ("DENY".equals(entry.getValue())) {
                den.add(entry.getKey());
            }
        }

        Node acl = getAcl(objectNode);
        NodeIterator ni = acl.getNodes();
        Node aceg = null;
        Node aced = null;
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.getProperty("j:principal").getString().equals(user)) {
                if (ace.getProperty("j:aceType").getString().equals("GRANT")) {
                    aceg = ace;
                } else {
                    aced = ace;
                }
            }
        }
        if (aceg == null) {
            aceg = acl.addNode("GRANT_" + user.replace(':', '_'), "jnt:ace");
            aceg.setProperty("j:principal", user);
            aceg.setProperty("j:protected", false);
            aceg.setProperty("j:aceType", "GRANT");
        }
        if (aced == null) {
            aced = acl.addNode("DENY_" + user.replace(':', '_'), "jnt:ace");
            aced.setProperty("j:principal", user);
            aced.setProperty("j:protected", false);
            aced.setProperty("j:aceType", "DENY");
        }

        List<String> grClone = new ArrayList<String>(gr);
        List<String> denClone = new ArrayList<String>(den);
        if (aceg.hasProperty(J_PRIVILEGES)) {
            final Value[] values = aceg.getProperty(J_PRIVILEGES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    grClone.add(s);
                }
            }
        }
        if (aced.hasProperty(J_PRIVILEGES)) {
            final Value[] values = aced.getProperty(J_PRIVILEGES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    denClone.add(s);
                }
            }
        }
        String[] grs = new String[grClone.size()];
        grClone.toArray(grs);
        aceg.setProperty(J_PRIVILEGES, grs);
        String[] dens = new String[denClone.size()];
        denClone.toArray(dens);
        aced.setProperty(J_PRIVILEGES, dens);
    }

    /**
     * Revoke all permissions for the specified user
     *
     * @param objectNode The node on which to revoke permissions
     * @param user
     * @throws RepositoryException
     */
    public static void revokePermission(Node objectNode, String user) throws RepositoryException {
        Node acl = getAcl(objectNode);

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.getProperty("j:principal").getString().equals(user)) {
                ace.remove();
            }
        }
    }

    /**
     * Revoke all permissions for all users on given node
     *
     * @param objectNode The node on which to revoke all permission
     * @throws RepositoryException
     */
    public static void revokeAllPermissions(Node objectNode) throws RepositoryException {
        Node acl = getAcl(objectNode);

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            ace.remove();
        }
    }

    /**
     * Check if acl inheritance is broken on the given node or not
     *
     * @param objectNode The node on which to check ACL inheritance break
     * @return true if ACL inheritance is broken
     * @throws RepositoryException
     */
    public static boolean getAclInheritanceBreak(Node objectNode) throws RepositoryException {
        if (!getAcl(objectNode).hasProperty("j:inherit")) {
            return false;
        }
        return !getAcl(objectNode).getProperty("j:inherit").getBoolean();
    }

    /**
     * Breaks the ACL inheritance on a given object node
     *
     * @param objectNode  The node on which to set ACL inheritance
     * @param inheritance true if ACL inheritance should be broken and false to inherit ACL from parent
     * @throws RepositoryException
     */
    public static void setAclInheritanceBreak(Node objectNode, boolean inheritance) throws RepositoryException {
        getAcl(objectNode).setProperty("j:inherit", !inheritance);
    }

    /**
     * Returns the ACL node of the given node or creates one
     *
     * @param objectNode The node to get the ACL node from
     * @return the ACL <code>Node</code> for the given node
     * @throws RepositoryException
     */
    public static Node getAcl(Node objectNode) throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            return objectNode.getNode("j:acl");
        } else {
            objectNode.addMixin("jmix:accessControlled");
            return objectNode.addNode("j:acl", "jnt:acl");
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRStoreProvider getJCRProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    public JCRStoreProvider getProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        objectNode.orderBefore(s, s1);
    }

    /**
     * {@inheritDoc}
     */
    public JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return provider.getItemWrapper(objectNode.getPrimaryItem(), session);
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() throws RepositoryException {
        return objectNode.getIndex();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getReferences() throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(), this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNode(String s) throws RepositoryException {
        // add mountpoints here
        final boolean b = objectNode.hasNode(s);
        if (b && Constants.LIVE_WORKSPACE.equals(getSession().getWorkspace().getName()) && !s.startsWith("j:translation")) {
            final JCRNodeWrapper wrapper;
            try {
                wrapper = (JCRNodeWrapper) getNode(s);
            } catch (RepositoryException e) {
                return false;
            }
            return wrapper.checkValidity();
        }
        return b;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNodes() throws RepositoryException {
        return objectNode.hasNodes();
    }

    /**
     * {@inheritDoc}
     */
    public JCRVersion checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return (JCRVersion) session.getWorkspace().getVersionManager().checkin(getPath());
    }

    /**
     * {@inheritDoc}
     */
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        session.checkout(this);
    }

    /**
     * {@inheritDoc}
     */
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.doneMerge(objectNode.getPath(), ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.cancelMerge(objectNode.getPath(), ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    public void update(final String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_UPDATE, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                objectNode.update(srcWorkspace);
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        if (provider.getMountPoint().equals("/")) {
            return objectNode.getCorrespondingNodePath(s);
        } else {
            return provider.getMountPoint() + objectNode.getCorrespondingNodePath(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCheckedOut() throws RepositoryException {
        VersionManager versionManager = session.getProviderSession(provider).getWorkspace().getVersionManager();
        boolean co = versionManager.isCheckedOut(objectNode.getPath());
        if (co && session.getLocale() != null) {
            try {
                co &= versionManager.isCheckedOut(getI18N(session.getLocale()).getPath());
            } catch (ItemNotFoundException e) {
                // no i18n node
            }
        }

        return co;
    }

    /**
     * {@inheritDoc}
     */
    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(s, b);
    }

    /**
     * {@inheritDoc}
     */
    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        getRealNode().restore(version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, b);
    }

    /**
     * {@inheritDoc}
     */
    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, s, b);
    }

    /**
     * {@inheritDoc}
     */
    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restoreByLabel(s, b);
    }

    /**
     * {@inheritDoc}
     */
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (VersionHistory) getProvider().getNodeWrapper((Node) getRealNode().getVersionHistory(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getBaseVersion(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    public JCRFileContent getFileContent() {
        if (fileContent == null) {
            fileContent = new JCRFileContent(this, objectNode);
        }
        return fileContent;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName)
            throws ConstraintViolationException, RepositoryException {
        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !propertyName.equals("jcr:language")) {
            return getParent().getApplicablePropertyDefinition(propertyName);
        }

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        Iterator<ExtendedNodeType> iterator = getNodeTypesIterator();
        while (iterator.hasNext()) {
            ExtendedNodeType type = iterator.next();
            final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
            if (definitionMap.containsKey(propertyName)) {
                return definitionMap.get(propertyName);
            }
            types.add(type);
        }
        for (ExtendedNodeType type : types) {
            for (ExtendedPropertyDefinition epd : type.getUnstructuredPropertyDefinitions().values()) {
                // check type .. ?
                return epd;
            }
        }
        throw new ConstraintViolationException("Cannot find definition for " + propertyName + " on node " + getName() + " (" + getPrimaryNodeTypeName() + ")");
    }

    private Iterator<ExtendedNodeType> getNodeTypesIterator() {
        return new Iterator<ExtendedNodeType>() {
            int i = 0;
            ExtendedNodeType next;
            boolean fetched = false;
            Iterator<ExtendedNodeType> mix = null;

            public boolean hasNext() {
                if (!fetched) {
                    try {
                        if (i == 0) {
                            next = getPrimaryNodeType();
                        } else if (i == 1 && isNodeType("nt:frozenNode")) {
                            next = NodeTypeRegistry.getInstance().getNodeType(objectNode.getProperty("jcr:frozenPrimaryType").getString());
                        } else {
                            if (mix == null) {
                                mix = Arrays.asList(getMixinNodeTypes()).iterator();
                            }
                            if (mix.hasNext()) {
                                next = mix.next();
                            } else {
                                next = null;
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.warn(e.getMessage(), e);
                    } finally {
                        i++;
                    }
                    fetched = true;
                }
                return (next != null);
            }
            private boolean isNodeType(String nodeType) {
                boolean isNodeType = false;
                try {
                    isNodeType = objectNode.isNodeType(nodeType);
                } catch (RepositoryException e) {
                    logger.warn(e.getMessage(), e);
                }
                return isNodeType;
            }
            public ExtendedNodeType next() {
                if (!fetched) {
                    hasNext();
                }
                if (next != null) {
                    fetched = false;
                    return next;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final JCRNodeWrapper fileNodeWrapper = (JCRNodeWrapper) o;

        return !(getPath() != null ? !getPath().equals(fileNodeWrapper.getPath()) : fileNodeWrapper.getPath() != null);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (getPath() != null ? getPath().hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public NodeIterator getNodes(String[] nameGlobs) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() throws RepositoryException {
        return objectNode.getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(name), this);
    }

    /**
     * {@inheritDoc}
     */
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getWeakReferences(), this);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getWeakReferences(name), this);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getSharedSet() throws RepositoryException {
        List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();

        NodeIterator ni = objectNode.getSharedSet();
        while (ni.hasNext()) {
            Node node = ni.nextNode();
            JCRNodeWrapper child = provider.getNodeWrapper(node, session);
            list.add(child);
        }

        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeSharedSet();
    }

    /**
     * {@inheritDoc}
     */
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeShare();
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException
     *          as long as Jahia doesn't support it
     */
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];
    }

    public JCRNodeWrapper clone(JCRNodeWrapper sharedNode, String name) throws ItemExistsException, VersionException,
            ConstraintViolationException, LockException,
            RepositoryException {
        if (!sharedNode.isNodeType("jmix:shareable")) {
            if (!sharedNode.isCheckedOut()) {
                sharedNode.checkout();
            }
            sharedNode.addMixin("jmix:shareable");
            sharedNode.getRealNode().getSession().save();

            try {
                final String path = sharedNode.getCorrespondingNodePath(Constants.LIVE_WORKSPACE);
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper n = session.getNode(path);
                        n.checkout();
                        n.addMixin("jmix:shareable");
                        n.getRealNode().getSession().save();
                        return null;
                    }
                });
            } catch (ItemNotFoundException e) {
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        if (getRealNode() instanceof NodeImpl && sharedNode.getRealNode() instanceof NodeImpl) {
            String uri = "";
            if (name.contains(":")) {
                uri = session.getNamespaceURI(StringUtils.substringBefore(name, ":"));
                name = StringUtils.substringAfter(name, ":");
            }
            org.apache.jackrabbit.spi.Name jrname = NameFactoryImpl.getInstance().create(uri, name);

            NodeImpl node = (NodeImpl) getRealNode();

            try {
                return provider.getNodeWrapper(node.clone((NodeImpl) sharedNode.getRealNode(), jrname), buildSubnodePath(name), session);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean checkValidity() {
        final JCRSessionWrapper jcrSessionWrapper = getSession();
        try {
            if (Constants.LIVE_WORKSPACE.equals(jcrSessionWrapper.getWorkspace().getName())) {
                if (jcrSessionWrapper.getLocale() != null) {
                    if (objectNode.hasProperty("j:published") && !objectNode.getProperty("j:published").getBoolean()) {
                        return false;
                    }
                }
                return checkLanguageValidity(null);
            }
        } catch (RepositoryException e) {
            return false;
        }
        return true;
    }

    public boolean checkLanguageValidity(Set<String> languages) {
        final JCRSessionWrapper jcrSessionWrapper = getSession();
        try {
            Locale locale = jcrSessionWrapper.getLocale();
            if (locale != null) {
                JCRSiteNode siteNode = getResolveSite();
                if(siteNode==null) {
                    return checkI18nAndMandatoryPropertiesForLocale(locale);
                } else {
                    Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                    if(!siteNode.getLanguagesAsLocales().contains(locale)){
                        return false;
                    }
                    for (String mandatoryLanguage : mandatoryLanguages) {
                        locale = LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage);
                        if(!checkI18nAndMandatoryPropertiesForLocale(locale)){
                            return false;
                        }
                    }
                }
            } else if(languages!=null) {
                for (String language : languages) {
                    locale = LanguageCodeConverters.getLocaleFromCode(language);
                    if(checkI18nAndMandatoryPropertiesForLocale(locale)) {
                        JCRSiteNode siteNode = getResolveSite();
                        if(siteNode!=null) {
                            Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                            if(mandatoryLanguages==null || mandatoryLanguages.isEmpty()) {
                                return true;
                            }
                            for (String mandatoryLanguage : mandatoryLanguages) {
                                locale = LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage);
                                if(!checkI18nAndMandatoryPropertiesForLocale(locale)) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        } catch (RepositoryException e) {
            return false;
        }
        return true;
    }

    private boolean hasTranslations() throws RepositoryException {
        NodeIterator ni = objectNode.getNodes();
        boolean translated = false;
        while (!translated && ni.hasNext()) {
            Node n = (Node) ni.next();
            if (n.getName().startsWith("j:translation_")) {
                translated = true;
            }
        }
        return translated;
    }

    public boolean checkI18nAndMandatoryPropertiesForLocale(Locale locale)
            throws RepositoryException {
        Node i18n = null;
        if (hasI18N(locale)) {
            i18n = getI18N(locale, false);
        }
        for (ExtendedPropertyDefinition def : getPrimaryNodeType().getPropertyDefinitionsAsMap().values()) {
            if (def.isInternationalized() && def.isMandatory()) {
                if (i18n == null || !i18n.hasProperty(def.getName())) {
                    return false;
                }
            }
        }
        return true;
    }


    public JCRSiteNode getResolveSite() throws RepositoryException {
        if (site != null) {
            return site;
        }

        try {
            if (isNodeType("jnt:virtualsite")) {
                return (site = (JCRSiteNode) provider.getService().decorate(this));
            }
            String path = getPath();
            if (path.startsWith("/sites/")) {
                return (site = new JCRSiteNode(getSession().getNode(path.substring(0, path.indexOf('/',7)))));
            }

            if (path.startsWith("/templateSets/")) {
                return (site = new JCRSiteNode(getSession().getNode(path.substring(0, path.indexOf('/',14)))));
            }
        } catch (ItemNotFoundException e) {
        }            
        return null;
//        return ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
    }



}
