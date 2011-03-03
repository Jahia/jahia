/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.decorator.JCRPlaceholderNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRVersion;
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
import javax.jcr.query.*;
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
    protected static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeWrapper.class);

    protected Node objectNode = null;
    protected JCRFileContent fileContent = null;
    protected JCRSiteNode site = null;
    protected boolean parentAlreadyResolved = false;
    protected JCRNodeWrapper resolvedParentNode = null;

    protected Map<Locale, Node> i18NobjectNodes = null;

    private transient Map<String, String> propertiesAsString;
    private static final String REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME = "j:referenceNodeIdentifiers";
    private static final String REFERENCE_PROPERTY_NAMES_PROPERTYNAME = "j:referencePropertyNames";
    public static final String EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR = "___";

    private Map<String, ExtendedPropertyDefinition> applicablePropertyDefinition = new HashMap<String, ExtendedPropertyDefinition>();
    private Map<String, Boolean> hasPropertyCache = new HashMap<String, Boolean>();

    protected JCRNodeWrapperImpl(Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session, JCRStoreProvider provider) {
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
        if (parent != null) {
            parentAlreadyResolved = true;
            resolvedParentNode = parent;
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
        if (parentAlreadyResolved) {
            return resolvedParentNode;
        }

        try {
            if (localPath.equals("/") || localPath.equals(provider.getRelativeRoot())) {
                if (provider.getMountPoint().equals("/")) {
                    throw new ItemNotFoundException();
                }
                return (JCRNodeWrapper) session.getItem(StringUtils.substringBeforeLast(provider.getMountPoint(), "/"));
            } else {
                return (JCRNodeWrapper) session.getItem(StringUtils.substringBeforeLast(getPath(), "/"));
            }
        } catch (PathNotFoundException e) {
            throw new ItemNotFoundException(e);
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
            Map<String, List<String[]>> results = new HashMap<String, List<String[]>>();

            Node n = objectNode;
            try {
                while (true) {
                    if (n.hasNode("j:acl")) {
                        Node acl = n.getNode("j:acl");
                        NodeIterator aces = acl.getNodes();
                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            if (ace.isNodeType("jnt:ace")) {
                                String principal = ace.getProperty("j:principal").getString();
                                String type = ace.getProperty("j:aceType").getString();
                                if (!ace.hasProperty(Constants.J_ROLES)) {
                                    continue;
                                }
                                Value[] roles = ace.getProperty(Constants.J_ROLES).getValues();

                                if (!results.containsKey(principal)) {
                                    results.put(principal, new ArrayList<String[]>());
                                }

                                for (Value role : roles) {
                                    results.get(principal).add(new String[]{n.getPath(), type, role.getString()});
                                }
                            }
                        }
                        if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                            return results;
                        }
                    }
                    n = n.getParent();
                }
            } catch (ItemNotFoundException e) {
                logger.debug(e.getMessage(), e);
            }


            return results;
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

    /**
     * {@inheritDoc}
     */
    public Map<String, List<JCRNodeWrapper>> getAvailableRoles() throws RepositoryException {
        JCRNodeWrapper roles = session.getNode("/roles");
        List<JCRNodeWrapper> res = new ArrayList<JCRNodeWrapper>();
        NodeIterator ni = roles.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper role = (JCRNodeWrapper) ni.nextNode();
            if (role.hasProperty("j:nodeTypes")) {
                Value[] values = role.getProperty("j:nodeTypes").getValues();
                for (Value value : values) {
                    if (isNodeType(value.getString())) {
                        res.add(role);
                        break;
                    }
                }
            } else {
                res.add(role);
            }
        }
        return Collections.singletonMap("default", res);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasPermission(String perm) {
        try {
            if (session.isSystem()) {
                return true;
            }

            AccessControlManager accessControlManager = getAccessControlManager();
            if (accessControlManager != null) {
//                        List<Privilege> privileges = convertPermToPrivileges(perm, accessControlManager);

                return accessControlManager.hasPrivileges(localPath, new Privilege[]{accessControlManager.privilegeFromName(perm)});
            }
            return true;
        } catch (AccessControlException e) {
            return false;
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
            return false;
        }
    }

    public AccessControlManager getAccessControlManager() throws RepositoryException {
        Session providerSession = session.getProviderSession(provider);
        // this is not a Jackrabbit implementation, we will use the new JCR 2.0 API instead.
        return providerSession.getAccessControlManager();
    }

    public Set<String> getPermissions() {
        Set<String> result = new HashSet<String>();
        try {
            AccessControlManager accessControlManager = getAccessControlManager();
            if (accessControlManager != null) {
                Privilege[] p = accessControlManager.getPrivileges(localPath);
                for (Privilege privilege : p) {
                    result.add(privilege.getName());
                    for (Privilege privilege1 : privilege.getAggregatePrivileges()) {
                        result.add(privilege1.getName());
                    }
                }
            }
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
        }
        return result;
    }

    public BitSet getPermissionsAsBitSet() {
        BitSet b = null;
        try {
            AccessControlManager accessControlManager = getAccessControlManager();
            List<Privilege> pr = Arrays.asList(accessControlManager.getSupportedPrivileges(localPath));
            List<Privilege> app = Arrays.asList(accessControlManager.getPrivileges(localPath));
            b = new BitSet(pr.size());
            for (Privilege privilege : app) {
                b.set(pr.indexOf(privilege));
                for (Privilege privilege1 : privilege.getAggregatePrivileges()) {
                    b.set(pr.indexOf(privilege1));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot check perm ", e);
        }
        return b;
    }


    private List<Privilege> convertPermToPrivileges(String perm, AccessControlManager accessControlManager) throws RepositoryException {
        List<Privilege> privileges = new ArrayList<Privilege>();
        if (Constants.JCR_READ_RIGHTS.equals(perm) || Constants.JCR_READ_RIGHTS_LIVE.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_READ));
        } else if (Constants.JCR_WRITE_RIGHTS.equals(perm) || Constants.JCR_WRITE_RIGHTS_LIVE.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_ADD_CHILD_NODES));
        } else if (Constants.JCR_MODIFYACCESSCONTROL_RIGHTS.equals(perm)) {
            privileges.add(accessControlManager.privilegeFromName(Privilege.JCR_MODIFY_ACCESS_CONTROL));
        }
        return privileges;
    }

    public boolean grantRoles(String user, Set<String> roles) throws RepositoryException {
        Map m = new HashMap<String, String>();
        for (String role : roles) {
            m.put(role, "GRANT");
        }
        return changeRoles(user, m);
    }

    public boolean denyRoles(String user, Set<String> roles) throws RepositoryException {
        Map m = new HashMap<String, String>();
        for (String role : roles) {
            m.put(role, "DENY");
        }
        return changeRoles(user, m);
    }

    /**
     * {@inheritDoc}
     */
    public boolean changeRoles(String user, Map<String, String> roles) throws RepositoryException {
        if (!objectNode.isCheckedOut() && objectNode.isNodeType(Constants.MIX_VERSIONABLE)) {
            objectNode.getSession().getWorkspace().getVersionManager().checkout(objectNode.getPath());
        }

        List<String> gr = new ArrayList<String>();
        List<String> den = new ArrayList<String>();

        for (Map.Entry<String, String> entry : roles.entrySet()) {
            if ("GRANT".equals(entry.getValue())) {
                gr.add(entry.getKey());
            } else if ("DENY".equals(entry.getValue())) {
                den.add(entry.getKey());
            }
        }

        Node acl = getOrCreateAcl();
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
        if (aceg.hasProperty(Constants.J_ROLES)) {
            final Value[] values = aceg.getProperty(Constants.J_ROLES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    grClone.add(s);
                }
            }
        }
        if (aced.hasProperty(Constants.J_ROLES)) {
            final Value[] values = aced.getProperty(Constants.J_ROLES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s)) {
                    denClone.add(s);
                }
            }
        }
        String[] grs = new String[grClone.size()];
        grClone.toArray(grs);
        if (grs.length == 0) {
            aceg.remove();
        } else {
            aceg.setProperty(Constants.J_ROLES, grs);
        }
        String[] dens = new String[denClone.size()];
        denClone.toArray(dens);
        if (dens.length == 0) {
            aced.remove();
        } else {
            aced.setProperty(Constants.J_ROLES, dens);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokeRolesForUser(String user) throws RepositoryException {
        Node acl = getOrCreateAcl();

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.getProperty("j:principal").getString().equals(user)) {
                ace.remove();
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean revokeAllRoles() throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            objectNode.getNode("j:acl").remove();
            objectNode.removeMixin("jmix:accessControlled");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean setAclInheritanceBreak(boolean inheritance) throws RepositoryException {
        try {
            getOrCreateAcl().setProperty("j:inherit", !inheritance);
        } catch (RepositoryException e) {
            logger.error("Cannot change acl", e);
            return false;
        }

        return true;
    }

    /**
     * Check if acl inheritance is broken on the given node or not
     *
     * @return true if ACL inheritance is broken
     * @throws RepositoryException
     */
    public boolean getAclInheritanceBreak() throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            Node acl = objectNode.getNode("j:acl");
            return acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean();
        }
        return false;
    }

    /**
     * Returns the ACL node of the given node or creates one
     *
     * @return the ACL <code>Node</code> for the given node
     * @throws RepositoryException
     */
    public Node getOrCreateAcl() throws RepositoryException {
        if (objectNode.hasNode("j:acl")) {
            return objectNode.getNode("j:acl");
        } else {
            if (!objectNode.isCheckedOut()) {
                objectNode.checkout();
            }
            objectNode.addMixin("jmix:accessControlled");
            return objectNode.addNode("j:acl", "jnt:acl");
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return addNode(name, Constants.JAHIANT_FOLDER);
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException {
        checkLock();

        JCRNodeWrapper file = null;
        try {
            file = getNode(name);
            if (!file.isCheckedOut()) {
                file.getSession().checkout(file);
            }
        } catch (PathNotFoundException e) {
            logger.debug("file " + name + " does not exist, creating...");
            file = addNode(name, Constants.JAHIANT_FILE);
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
        checkLock();

        Node n = objectNode.addNode(name);
        return provider.getNodeWrapper(n, buildSubnodePath(name), this, session);
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
        checkLock();

        Node n = objectNode.addNode(name, type);
        return provider.getNodeWrapper(n, buildSubnodePath(name), this, session);
    }

    public JCRNodeWrapper addNode(String name, String type, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        checkLock();

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
                    return provider.getNodeWrapper(child, buildSubnodePath(name), this, session);
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
        return provider.getAbsoluteContextPath(request) + getUrl();
    }

    /**
     * {@inheritDoc}
     */
    public String getUrl() {
        if (isNodeType(Constants.JAHIANT_FILE)) {
            return provider.getHttpPath() + "/" + getSession().getWorkspace().getName() + getPath();
        } else {
            String path = JCRSessionFactory.getInstance().getCurrentServletPath();
            if (path == null) {
                path = "/cms/render";
            }
            if ("/".equals(Jahia.getContextPath())) {
                return path + "/" + getSession().getWorkspace().getName() + "/" + getSession().getLocale() + getPath() + ".html";
            }
            return Jahia.getContextPath() + path + "/" + getSession().getWorkspace().getName() + "/" + getSession().getLocale() + getPath() + ".html";
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getAbsoluteWebdavUrl(final HttpServletRequest request) {
        return provider.getAbsoluteContextPath(request) + getWebdavUrl();
    }

    /**
     * {@inheritDoc}
     */
    public String getWebdavUrl() {
        try {
            return provider.getWebdavPath() + "/" + session.getWorkspace().getName() + objectNode.getPath();
        } catch (RepositoryException e) {
            logger.error("Cannot get file path", e);
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
                        list.add(storeProvider.getNodeWrapper(node, "/", this, session));
                    }
                }
            }
        }

        NodeIterator ni = objectNode.getNodes();

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            if (session.getLocale() == null || !node.getName().startsWith("j:translation_")) {
                try {
                    JCRNodeWrapper child = provider.getNodeWrapper(node, buildSubnodePath(node.getName()), this, session);
                    list.add(child);
                } catch (ItemNotFoundException e) {
                    if (logger.isDebugEnabled())
                        logger.debug(e.getMessage(), e);
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
                list.add(storeProvider.getNodeWrapper(node, "/", this, session));
            }
        }

        NodeIterator ni = objectNode.getNodes(name);

        while (ni.hasNext()) {
            Node node = ni.nextNode();
            JCRNodeWrapper child = provider.getNodeWrapper(node, buildSubnodePath(node.getName()), this, session);
            list.add(child);
        }

        return new NodeIteratorImpl(list.iterator(), list.size());
    }

    /**
     * {@inheritDoc}
     */
    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        if (objectNode.hasNode(s)) {
            return provider.getNodeWrapper(objectNode.getNode(s), buildSubnodePath(s), this, session);
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
        checkLock();
        applicablePropertyDefinition.clear();
        objectNode.addMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public void removeMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        checkLock();
        applicablePropertyDefinition.clear();
        objectNode.removeMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        checkLock();

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
                ExtendedNodeType[] requiredPrimaryTypes = d.getRequiredPrimaryTypes();
                NodeType[] a2 = definition.getRequiredPrimaryTypes();
                boolean valid = true;
                for (ExtendedNodeType extendedNodeType : requiredPrimaryTypes) {
                    boolean found = false;
                    for (NodeType nodeType : a2) {
                        if (nodeType.getName().equals(extendedNodeType.getName())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        valid = false;
                        break;
                    }
                }
                if (valid)
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
        return isNodeType(Constants.NT_FILE);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPortlet() {
        return isNodeType(Constants.JAHIANT_PORTLET);
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

    public List<Locale> getExistingLocales() throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = objectNode.getNodes("j:translation*");
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            r.add(new Locale(n.getProperty("jcr:language").getString()));
        }
        return r;
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

    private boolean hasI18N(Locale locale, boolean fallback) throws RepositoryException {
        boolean b = (i18NobjectNodes != null && i18NobjectNodes.containsKey(locale)) || objectNode.hasNode(
                "j:translation_" + locale);
        if(!b && fallback) {
            final Locale fallbackLocale = getSession().getFallbackLocale();
            if (fallbackLocale != null && fallbackLocale != locale) {
                b = (i18NobjectNodes != null && i18NobjectNodes.containsKey(fallbackLocale)) || objectNode.hasNode(
                "j:translation_" + fallbackLocale);
            }
        }
        return b;
    }

    protected Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
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
        } else if (objectNode.hasNode("j:translation_" + locale)) {
            node = objectNode.getNode("j:translation_" + locale);
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
            Node t = objectNode.addNode("j:translation_" + locale, Constants.JAHIANT_TRANSLATION);
            t.setProperty("jcr:language", locale.toString());

            i18NobjectNodes.put(locale, t);
            return t;
        }
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd != null) {
            if ((epd.getRequiredType() == PropertyType.WEAKREFERENCE) ||
                    (epd.getRequiredType() == PropertyType.REFERENCE)) {
                if (isNodeType(Constants.JAHIAMIX_EXTERNALREFERENCE)) {
                    return retrieveExternalReferenceProperty(name, epd);
                }
            }
            return internalGetProperty(name, epd);
        } else {
            throw new PathNotFoundException(name);
        }
    }

    private JCRPropertyWrapper internalGetProperty(String name, ExtendedPropertyDefinition epd) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (epd == null) {
            epd = getApplicablePropertyDefinition(name);
        }
        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                try {
                    final Node localizedNode = getI18N(locale);
                    return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                            session, provider, epd,
                            name);
                } catch (ItemNotFoundException e) {
                    return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
                }
            }
        }
        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
    }

    // TODO does not yet support i18n reference nodes

    private JCRPropertyWrapper retrieveExternalReferenceProperty(String name, ExtendedPropertyDefinition epd) throws RepositoryException {
        Property referenceProperty = getProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME);
        Value[] propertyReferences = referenceProperty.getValues();
        String foundNodeIdentifier = null;
        for (Value propertyReference : propertyReferences) {
            String curPropertyReference = propertyReference.getString();
            String[] refParts = curPropertyReference.split(EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR);
            String curNodeIdentifier = refParts[0];
            String curPropertyName = refParts[1];
            if (curPropertyName.equals(name)) {
                foundNodeIdentifier = curNodeIdentifier;
                break;
            }
        }
        if (foundNodeIdentifier != null) {
            Node referencedNode = getSession().getNodeByIdentifier(foundNodeIdentifier);
            Property nodeProperty = new ExternalReferencePropertyImpl(name, this, session, foundNodeIdentifier, referencedNode);
            return new JCRPropertyWrapperImpl(this, nodeProperty, session, provider, epd);
        } else {
            throw new PathNotFoundException("Couldn't find matching external property reference for name " + name);
        }
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

    private String ensurePrefixedName(String name) {
        if (!name.startsWith("{")) {
            return name;
        }
        org.jahia.services.content.nodetypes.Name nameObj = new org.jahia.services.content.nodetypes.Name(
                name, NodeTypeRegistry.getInstance().getNamespaces());
        return StringUtils.isEmpty(nameObj.getPrefix()) ? nameObj.getLocalName() : nameObj
                .getPrefix() + ":" + nameObj.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        checkLock();
        hasPropertyCache.remove(name);

        name = ensurePrefixedName(name);
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

        if (value != null && PropertyType.UNDEFINED != epd.getRequiredType() && value.getType() != epd.getRequiredType()) {
            // if the type doesn't match the required type, we attempt a conversion.
            value = getSession().getValueFactory().createValue(value.getString(), epd.getRequiredType());
        }
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);

        if (value instanceof ExternalReferenceValue) {
            String nodeIdentifier = value.getString();
            JCRNodeWrapper node = getSession().getNodeByIdentifier(nodeIdentifier);
            return setExternalReferenceProperty(name, node, epd);
        }

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value), session, provider, epd, name);
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
        checkLock();
        hasPropertyCache.remove(name);

        final Locale locale = getSession().getLocale();
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);

        if (value instanceof ExternalReferenceValue) {
            String nodeIdentifier = value.getString();
            JCRNodeWrapper node = getSession().getNodeByIdentifier(nodeIdentifier);
            return setExternalReferenceProperty(name, node, epd);
        }

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value, type), session, provider, epd, name);
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
        checkLock();
        hasPropertyCache.remove(name);
        final Locale locale = getSession().getLocale();
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

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
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values), session, provider, epd, name);
            }
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    public JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        hasPropertyCache.remove(name);
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd != null && epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values, type), session, provider, epd, name);
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
    public JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Value v = null;
        if (value != null) {
            if (value instanceof JCRNodeWrapper) {
                value = ((JCRNodeWrapper) value).getRealNode();
            }
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
            if (epd != null) {
                if (value.getClass().getName().equals(getRealNode().getClass().getName())) {
                    v = getSession().getValueFactory().createValue(value, epd.getRequiredType() == PropertyType.WEAKREFERENCE);
                } else {
                    return setExternalReferenceProperty(name, value, epd);

                }
            }
        }
        return setProperty(name, v);
    }

    private JCRPropertyWrapper setExternalReferenceProperty(String name, Node node, ExtendedPropertyDefinition epd) throws RepositoryException {
        internalSetExternalReferenceProperty(name, node.getIdentifier());

        Property nodeProperty = new ExternalReferencePropertyImpl(name, this, session, node.getIdentifier(), node);
        return new JCRPropertyWrapperImpl(this, nodeProperty, session, provider, epd);
    }

    private void internalSetExternalReferenceProperty(String name, String nodeIdentifier) throws RepositoryException {
        // we are creating a reference to a node that is in another repository, we will use a special mixin for that.
        List<Value> nodeIdentifiers = null;
        List<Value> referenceProperties = null;
        if (!isNodeType(Constants.JAHIAMIX_EXTERNALREFERENCE)) {
            addMixin(Constants.JAHIAMIX_EXTERNALREFERENCE);
            nodeIdentifiers = new ArrayList<Value>();
            referenceProperties = new ArrayList<Value>();
        } else {
            Property nodeIdentifierProperty = getProperty(REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME);
            if (nodeIdentifierProperty != null) {
                // We create an ArrayList because Arrays.asList returns a non-mutable list
                nodeIdentifiers = new ArrayList<Value>(Arrays.asList(nodeIdentifierProperty.getValues()));
            } else {
                nodeIdentifiers = new ArrayList<Value>();
            }
            Property referenceProperty = getProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME);
            if (referenceProperty != null) {
                // We create an ArrayList because Arrays.asList returns a non-mutable list
                referenceProperties = new ArrayList<Value>(Arrays.asList(referenceProperty.getValues()));
            } else {
                referenceProperties = new ArrayList<Value>();
            }

        }
        Value newNodeIdentifierValue = getSession().getValueFactory().createValue(nodeIdentifier);
        if (!nodeIdentifiers.contains(newNodeIdentifierValue)) {
            nodeIdentifiers.add(newNodeIdentifierValue);
        }
        setProperty(REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME, nodeIdentifiers.toArray(new Value[nodeIdentifiers.size()]));

        Value newPropertyReferenceValue = getSession().getValueFactory().createValue(nodeIdentifier + EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR + name);
        if (!referenceProperties.contains(newPropertyReferenceValue)) {
            referenceProperties.add(newPropertyReferenceValue);
        }
        setProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME, referenceProperties.toArray(new Value[referenceProperties.size()]));
    }

    protected void removeExternalReferenceProperty(String name) throws ItemNotFoundException, RepositoryException {
        if (!isNodeType(Constants.JAHIAMIX_EXTERNALREFERENCE)) {
            // quick sanity check, but usually we will do this in this method's caller
            return;
        }
        boolean mustRemoveMixin = false;
        String externalReferenceId = findExternalReferenceIdFromPropertyName(name);
        if (externalReferenceId == null) {
            return;
        }
        Property nodeIdentifierProperty = getProperty(REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME);
        List<Value> identifiers = new ArrayList<Value>(Arrays.asList(nodeIdentifierProperty.getValues()));
        if (identifiers.size() == 0) {
            nodeIdentifierProperty.remove();
            return;
        }
        Value externalReferenceIdValue = getSession().getValueFactory().createValue(externalReferenceId);
        if (identifiers.contains(externalReferenceIdValue)) {
            identifiers.remove(externalReferenceIdValue);
        }
        if (identifiers.size() > 0) {
            setProperty(REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME, identifiers.toArray(new Value[identifiers.size()]));
        } else {
            nodeIdentifierProperty.remove();
            mustRemoveMixin = true;
        }

        Property referencePropertyNames = getProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME);
        List<Value> propertyNames = new ArrayList<Value>(Arrays.asList(referencePropertyNames.getValues()));
        if (propertyNames.size() == 0) {
            referencePropertyNames.remove();
        }
        Value externalReferencePropertyValue = getSession().getValueFactory().createValue(externalReferenceId + EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR + name);
        if (propertyNames.contains(externalReferencePropertyValue)) {
            propertyNames.remove(externalReferencePropertyValue);
        }
        if (propertyNames.size() > 0) {
            setProperty(REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME, propertyNames.toArray(new Value[propertyNames.size()]));
        } else {
            referencePropertyNames.remove();
            mustRemoveMixin = true;
        }

        if (internalHasProperty(name)) {
            Property property = internalGetProperty(name, null);
            property.remove();
        }

        if (mustRemoveMixin) {
            // as the node can have multiple reference properties, we only remove the mixin if all properties have
            // been removed.
            removeMixin(Constants.JAHIAMIX_EXTERNALREFERENCE);
        }

    }

    private String findExternalReferenceIdFromPropertyName(String name) throws ItemNotFoundException, RepositoryException {
        Property referenceProperty = getProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME);
        if (referenceProperty == null) {
            throw new ItemNotFoundException("Couldn't find " + REFERENCE_PROPERTY_NAMES_PROPERTYNAME + " property on node " + getPath());
        }
        Value[] referencePropNames = referenceProperty.getValues();
        for (Value referencePropName : referencePropNames) {
            String[] referencePropNameParts = referencePropName.getString().split(EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR);
            if (referencePropNameParts[1].equals(name)) {
                return referencePropNameParts[0];
            }
        }
        return null;
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
        if (hasPropertyCache.containsKey(propertyName)) {
            return hasPropertyCache.get(propertyName);
        }
        boolean result = internalHasProperty(propertyName);
        hasPropertyCache.put(propertyName, result);
        return result;
    }

    private boolean internalHasProperty(String propertyName) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null && !propertyName.equals("jcr:language")) {
            try {
                ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(propertyName);
                if (epd != null && epd.isInternationalized()) {
                    if (hasI18N(locale, true)) {
                        final Node localizedNode = getI18N(locale);
                        return localizedNode.hasProperty(propertyName);
                    }
                }
            } catch (ConstraintViolationException e) {
                return false;
            }
        }
        return objectNode.hasProperty(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasProperties() throws RepositoryException {
        boolean result = objectNode.hasProperties();
        if (result) return true;
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            if (hasI18N(locale, true)) {
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
            } catch (PathNotFoundException nfe) {
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
        checkLock();

        getSession().checkout(this);
        JCRNodeWrapper parent = getParent();
        getSession().checkout(parent);


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
            getSession().checkout(copy);
        } catch (PathNotFoundException ex) {
            // node does not exist
        }

        final Map<String, String> uuidMapping = getSession().getUuidMapping();

        if (copy == null || copy.getDefinition().allowsSameNameSiblings()) {
            if (dest.isVersioned()) {
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
                if (!Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
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
     *
     * @param type
     */
    public boolean lockAndStoreToken(String type) throws RepositoryException {
        String l = (getSession().isSystem() ? " system " : getSession().getUserID()) + ":" + type;

        if (!isNodeType("jmix:lockable")) {
            return false;
        }
        if (!objectNode.isLocked()) {
            lockNode(objectNode);
        }

        addLockTypeValue(objectNode, l);

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION)) {
            Node trans = null;
            try {
                trans = getI18N(session.getLocale());
                if (!trans.isLocked()) {
                    lockNode(trans);
                }
                addLockTypeValue(trans, l);
            } catch (ItemNotFoundException e) {
            }
        }
        objectNode.getSession().save();
        return true;
    }

    private void lockNode(final Node objectNode) throws RepositoryException {
        Lock lock = objectNode.lock(false, false);
        if (lock.getLockToken() != null) {
            try {
                getSession().checkout(objectNode);
                objectNode.setProperty("j:locktoken", lock.getLockToken());
//                objectNode.getSession().removeLockToken(lock.getLockToken());
            } catch (RepositoryException e) {
                logger.error("Cannot store token for " + getPath(), e);
                objectNode.unlock();
            }
        } else {
            logger.error("Lost lock ! " + objectNode.getPath());
        }
    }

    private void addLockTypeValue(final Node objectNode, String l) throws RepositoryException {
        getSession().checkout(objectNode);

        if (objectNode.hasProperty(Constants.JAHIA_LOCKTYPES)) {
            Property property = objectNode.getProperty(Constants.JAHIA_LOCKTYPES);
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
            objectNode.setProperty(Constants.JAHIA_LOCKTYPES, new String[]{l});
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

    public List<Locale> getLockedLocales(String type) throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = objectNode.getNodes("j:translation*");
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (n.isLocked() && n.hasProperty(Constants.JAHIA_LOCKTYPES)) {
                String l = (getSession().isSystem() ? " system " : getSession().getUserID()) + ":" + type;
                Value[] v = n.getProperty(Constants.JAHIA_LOCKTYPES).getValues();
                for (Value value : v) {
                    if (value.getString().equals(l)) {
                        r.add(new Locale(n.getProperty("jcr:language").getString()));
                        break;
                    }
                }

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
                    try {
                        return getProvider().getNodeWrapper(lock.getNode(), getSession());
                    } catch (RepositoryException e) {
                        logger.warn("Can't get wrapper for node holding lock", e);
                        return JCRNodeWrapperImpl.this;
                    }
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

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION) && hasI18N(session.getLocale(),
                false)) {
            Node trans = getI18N(session.getLocale(), false);
            if (trans.isLocked()) {
                unlock(trans, type);
            }
        }

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !getLockedLocales(type).isEmpty()) {
            return;
        }

        unlock(objectNode, type);
    }

    private void unlock(final Node objectNode, String type) throws RepositoryException {
        if (objectNode.hasProperty("j:locktoken")) {
            Property property = objectNode.getProperty("j:locktoken");
            String token = property.getString();
            Value[] types = objectNode.getProperty(Constants.JAHIA_LOCKTYPES).getValues();
            for (Value value : types) {
                String owner = StringUtils.substringBefore(value.getString(), ":");
                String currentType = StringUtils.substringAfter(value.getString(), ":");
                if (currentType.equals(type)) {
                    if (getSession().isSystem() || getSession().getUserID().equals(owner)) {
                        objectNode.getSession().addLockToken(token);
                        final Map<String, Value> valueList = new HashMap<String, Value>();
                        for (Value v : types) {
                            valueList.put(v.getString(), v);
                        }
                        valueList.remove(value.getString());
                        getSession().checkout(objectNode);

                        if (valueList.isEmpty()) {
                            objectNode.unlock();
                            property.remove();
                            objectNode.getProperty(Constants.JAHIA_LOCKTYPES).remove();
                        } else {
                            objectNode.setProperty(Constants.JAHIA_LOCKTYPES, valueList.values().toArray(new Value[valueList.size()]));
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

    public void clearAllLocks() throws RepositoryException {
        if (!isLocked()) {
            throw new LockException("Node not locked");
        }

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION) && hasI18N(session.getLocale(),
                false)) {
            Node trans = getI18N(session.getLocale(), false);
            if (trans.isLocked()) {
                clearAllLocks(trans);
            }
        }

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !getLockedLocales().isEmpty()) {
            return;
        }

        clearAllLocks(objectNode);
    }

    private void clearAllLocks(final Node objectNode) throws RepositoryException {
        if (objectNode.hasProperty("j:locktoken")) {
            Property property = objectNode.getProperty("j:locktoken");
            String token = property.getString();

            objectNode.getSession().addLockToken(token);

            objectNode.checkout();
            objectNode.unlock();
            property.remove();
            objectNode.getProperty(Constants.JAHIA_LOCKTYPES).remove();

            getSession().save();
        }
    }

    protected void checkLock() throws RepositoryException {
        if (isLocked() && !session.isSystem()) {
            List<String> owners = getLockOwners();
            if (owners.size() == 1 && owners.contains(session.getUserID())) {
                session.addLockToken(objectNode.getProperty("j:locktoken").getString());
            } else {
                throw new LockException("Node locked.");
            }
            if (session.getLocale() != null) {
                Node i18n = getI18N(session.getLocale());
                if (i18n.isLocked()) {
                    owners = getLockOwners(i18n);
                    if (owners.size() == 1 && owners.contains(session.getUserID())) {
                        session.addLockToken(i18n.getProperty("j:locktoken").getString());
                    } else {
                        throw new LockException("Node locked.");
                    }
                }
            }
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
        if (!"shared".equals(provider.getAuthenticationType())) {
            return getLock().getLockOwner();
        } else {
            return getSession().getUserID();
        }
    }

    public List<String> getLockOwners() throws RepositoryException {
        return getLockOwners(objectNode);
    }

    private List<String> getLockOwners(Node node) throws RepositoryException {
        List<String> r = new ArrayList<String>();
        if (node.hasProperty(Constants.JAHIA_LOCKTYPES)) {
            Value[] values = node.getProperty(Constants.JAHIA_LOCKTYPES).getValues();
            for (Value value : values) {
                String owner = StringUtils.substringBefore(value.getString(), ":");
                if (!r.contains(owner)) {
                    r.add(owner);
                }
            }
        }
        return r;
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
        checkLock();
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
            throws RepositoryException {
        ExtendedPropertyDefinition result = null;
        if (applicablePropertyDefinition.containsKey(propertyName)) {
            result = applicablePropertyDefinition.get(propertyName);
            return result;
        }

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !propertyName.equals("jcr:language")) {
            result = getParent().getApplicablePropertyDefinition(propertyName);
            applicablePropertyDefinition.put(propertyName, result);
            return result;
        }

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        Iterator<ExtendedNodeType> iterator = getNodeTypesIterator();
        while (iterator.hasNext()) {
            ExtendedNodeType type = iterator.next();
            final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
            applicablePropertyDefinition.putAll(definitionMap);
            if (definitionMap.containsKey(propertyName)) {
                result = definitionMap.get(propertyName);
                return result;
            }
            types.add(type);
        }
        for (ExtendedNodeType type : types) {
            for (ExtendedPropertyDefinition epd : type.getUnstructuredPropertyDefinitions().values()) {
                // check type .. ?
                result = epd;
                applicablePropertyDefinition.put(propertyName, result);
                return result;
            }
        }
        applicablePropertyDefinition.put(propertyName, result);
        return result;
    }

    public List<ExtendedPropertyDefinition> getReferenceProperties() throws RepositoryException {

        List<ExtendedPropertyDefinition> defs = new ArrayList<ExtendedPropertyDefinition>();
        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        Iterator<ExtendedNodeType> iterator = getNodeTypesIterator();

        if (isNodeType(Constants.JAHIANT_TRANSLATION)) {
            return getParent().getReferenceProperties();
        }

        while (iterator.hasNext()) {
            ExtendedNodeType type = iterator.next();
            final Map<String, ExtendedPropertyDefinition> definitionMap = type.getPropertyDefinitionsAsMap();
            for (ExtendedPropertyDefinition definition : definitionMap.values()) {
                if (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == PropertyType.WEAKREFERENCE) {
                    defs.add(definition);
                }
            }

            types.add(type);
        }
        for (ExtendedNodeType type : types) {
            for (ExtendedPropertyDefinition definition : type.getUnstructuredPropertyDefinitions().values()) {
                if (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == PropertyType.WEAKREFERENCE) {
                    defs.add(definition);
                }
            }
        }

        return defs;
    }

    public ExtendedNodeDefinition getApplicableChildNodeDefinition(String childName, String nodeType)
            throws ConstraintViolationException, RepositoryException {
        ExtendedNodeType requiredType = NodeTypeRegistry.getInstance().getNodeType(nodeType);

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        Iterator<ExtendedNodeType> iterator = getNodeTypesIterator();
        while (iterator.hasNext()) {
            ExtendedNodeType type = iterator.next();
            final Map<String, ExtendedNodeDefinition> definitionMap = type.getChildNodeDefinitionsAsMap();
            if (definitionMap.containsKey(childName)) {
                ExtendedNodeDefinition epd = definitionMap.get(childName);
                for (String req : epd.getRequiredPrimaryTypeNames()) {
                    if (requiredType.isNodeType(req)) {
                        return epd;
                    }
                }
                throw new ConstraintViolationException("Definition type for " + childName + " on node " + getName() + " (" + getPrimaryNodeTypeName() + ") does not match " + nodeType);
            }
            types.add(type);
        }
        for (ExtendedNodeType type : types) {
            for (ExtendedNodeDefinition epd : type.getUnstructuredChildNodeDefinitions().values()) {
                for (String req : epd.getRequiredPrimaryTypeNames()) {
                    if (requiredType.isNodeType(req)) {
                        return epd;
                    }
                }
            }
        }
        throw new ConstraintViolationException("Cannot find definition for " + childName + " on node " + getName() + " (" + getPrimaryNodeTypeName() + ")");
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
        if (provider.getKey().equals("default")) {
            return new PropertyIteratorImpl(objectNode.getWeakReferences(), this);
        } else {
            return getExternalWeakReferences();
        }
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
            getSession().checkout(sharedNode);
            sharedNode.addMixin("jmix:shareable");
            sharedNode.getRealNode().getSession().save();

            try {
                final String path = sharedNode.getCorrespondingNodePath(Constants.LIVE_WORKSPACE);
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper n = session.getNode(path);
                        getSession().checkout(n);
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
                return provider.getNodeWrapper(node.clone((NodeImpl) sharedNode.getRealNode(), jrname), buildSubnodePath(name),
                        this, session);
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
                if (siteNode != null) {
                    Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                    List<Locale> locales = siteNode.getLanguagesAsLocales();
                    if (locales.size() == 0) {
                        return true;
                    }
                    if (!locales.contains(locale)) {
                        return false;
                    }
                    for (String mandatoryLanguage : mandatoryLanguages) {
                        locale = LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage);
                        if (!checkI18nAndMandatoryPropertiesForLocale(locale)) {
                            return false;
                        }
                    }
                }
                return checkI18nAndMandatoryPropertiesForLocale(locale);
            } else if (languages != null) {
                for (String language : languages) {
                    locale = LanguageCodeConverters.getLocaleFromCode(language);
                    if (checkI18nAndMandatoryPropertiesForLocale(locale)) {
                        JCRSiteNode siteNode = getResolveSite();
                        if (siteNode != null) {
                            Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                            if (mandatoryLanguages == null || mandatoryLanguages.isEmpty()) {
                                return true;
                            }
                            for (String mandatoryLanguage : mandatoryLanguages) {
                                locale = LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage);
                                if (!checkI18nAndMandatoryPropertiesForLocale(locale)) {
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
        if (hasI18N(locale,false)) {
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
                int index = path.indexOf('/', 7);
                try {
                    return (site = (JCRSiteNode) (getSession().getNode(index == -1 ? path : path.substring(0, index))));
                } catch (ClassCastException e) {
                    // if node is not a site ( eg ACL / workflow )
                    return null;
                }
            }

            if (path.startsWith("/templateSets/")) {
                int index = path.indexOf('/', 14);
                return (site = (JCRSiteNode) (getSession().getNode(index == -1 ? path : path.substring(0, index))));
            }

            return (site = (JCRSiteNode) (getSession().getNode(JCRContentUtils.getSystemSitePath())));
        } catch (ItemNotFoundException e) {
        }
        return null;
//        return ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
    }

    public PropertyIterator getExternalWeakReferences() throws RepositoryException {
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query query = queryManager.createQuery("SELECT * FROM [jmix:externalReference] as n WHERE n.[" + REFERENCE_NODE_IDENTIFIERS_PROPERTYNAME + "]='" + getIdentifier() + "'", Query.JCR_SQL2);
        QueryResult queryResult = query.execute();
        RowIterator rowIterator = queryResult.getRows();
        List<Property> matchingProperties = new ArrayList<Property>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            JCRNodeWrapper node = (JCRNodeWrapper) row.getNode();
            Property referenceProperty = node.getProperty(REFERENCE_PROPERTY_NAMES_PROPERTYNAME);
            Value[] propertyReferences = referenceProperty.getValues();
            String foundPropertyName = null;
            for (Value propertyReference : propertyReferences) {
                String curPropertyReference = propertyReference.getString();
                String[] refParts = curPropertyReference.split(EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR);
                String curNodeIdentifier = refParts[0];
                String curPropertyName = refParts[1];
                if (curNodeIdentifier.equals(getIdentifier())) {
                    foundPropertyName = curPropertyName;
                    break;
                }
            }
            if (foundPropertyName != null) {
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(foundPropertyName);
                if (epd != null)  {
                    Property nodeProperty = new ExternalReferencePropertyImpl(foundPropertyName, node, session, getIdentifier(), this);
                    matchingProperties.add(new JCRPropertyWrapperImpl(this, nodeProperty, session, provider, epd));
                }
            } else {
                throw new PathNotFoundException("Couldn't find matching external property reference for name " + foundPropertyName);
            }

        }
        return new PropertyIteratorAdapter(matchingProperties.iterator());
    }


    public String getDisplayableName() {
        String title = getPropertyAsString(Constants.JCR_TITLE);
        //Search for primary field if present
        if (title == null) {
            try {
                String itemName = getPrimaryNodeType().getPrimaryItemName();
                if (itemName != null) {
                    String s = getPropertyAsString(itemName);
                    title = new TextExtractor(new Source(s != null ? s : itemName)).toString();
                }
            } catch (RepositoryException e) {
                title = null;
            }
        }
        return title != null ? title : getName();
    }
}
