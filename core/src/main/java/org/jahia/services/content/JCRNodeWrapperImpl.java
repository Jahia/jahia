/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.apache.jackrabbit.core.JahiaSessionImpl;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.apache.jackrabbit.util.Text;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.*;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.jcr.version.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static org.jahia.api.Constants.*;

/**
 * Wrappers around <code>javax.jcr.Node</code> to be able to inject
 * Jahia specific actions.
 *
 * @author toto
 */
public class JCRNodeWrapperImpl extends JCRItemWrapperImpl implements JCRNodeWrapper {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeWrapperImpl.class);

    private static final String[] TRANSLATION_NODES_PATTERN = new String[] {"j:translation_*"};
    private static final String TRANSLATION_PREFIX = "j:translation_";

    protected Node objectNode = null;
    protected JCRFileContent fileContent = null;
    protected JCRSiteNode site = null;
    protected boolean parentAlreadyResolved = false;
    protected JCRNodeWrapper resolvedParentNode = null;

    protected Map<Locale, Node> i18NobjectNodes = null;

    protected Map<String, List<String[]>> aclEntries = null;
    protected Boolean breakAcl = null;

    public static final String EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR = "___";
    public static final Pattern EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR_PATTERN = Pattern.compile(EXTERNAL_IDENTIFIER_PROP_NAME_SEPARATOR);

    private static final ExtendedNodeType[] EMPTY_EXTENDED_NODE_TYPE_ARRAY = new ExtendedNodeType[0];

    private Map<String, ExtendedPropertyDefinition> applicablePropertyDefinition = new HashMap<String, ExtendedPropertyDefinition>();
    private Map<String, Boolean> hasPropertyCache = new HashMap<String, Boolean>();
    private ExtendedNodeType[] originalMixins = null;

    private static boolean doCopy(JCRNodeWrapper source, JCRNodeWrapper dest, String name,
                                  boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes,
                                  int maxBatch, MutableInt batchCount, boolean isTopObject) throws RepositoryException {
        if (source instanceof JCRNodeWrapperImpl) {
            return ((JCRNodeWrapperImpl) source).internalCopy(dest, name, allowsExternalSharedNodes, references,
                    ignoreNodeTypes, maxBatch, batchCount, isTopObject);
        } else if (source instanceof JCRNodeDecorator) {
            return ((JCRNodeDecorator) source).internalCopy(dest, name, allowsExternalSharedNodes, references,
                    ignoreNodeTypes, maxBatch, batchCount, isTopObject);
        } else {
            return source
                    .copy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount);
        }
    }

    protected static void unlock(final Node objectNode, String type, String userID, JCRSessionWrapper session) throws RepositoryException {
        if (objectNode.hasProperty("j:locktoken")) {
            Property property = objectNode.getProperty("j:locktoken");
            String token = property.getString();
            Value[] types = objectNode.getProperty("j:lockTypes").getValues();
            for (Value value : types) {
                String owner = StringUtils.substringBefore(value.getString(), ":");
                String currentType = StringUtils.substringAfter(value.getString(), ":");
                if (currentType.equals(type)) {
                    if (userID.equals(owner)) {
                        objectNode.getSession().addLockToken(token);
                        session.checkout(objectNode);
                        List<Value> valueList = new ArrayList<Value>(Arrays.asList(types));
                        valueList.remove(value);
                        if (valueList.isEmpty()) {
                            session.save();
                            objectNode.unlock();
                            property.remove();
                            objectNode.getProperty("j:lockTypes").remove();
                        } else {
                            objectNode.setProperty("j:lockTypes", valueList.toArray(new Value[valueList.size()]));
                        }
                        session.save();

                        return;
                    }
                }
            }
        } else {
            objectNode.unlock();
        }
    }

    protected JCRNodeWrapperImpl(Node objectNode, String path, JCRNodeWrapper parent, JCRSessionWrapper session, JCRStoreProvider provider) throws RepositoryException {
        super(session, provider);
        this.objectNode = objectNode;
        setItem(objectNode);
        if (path != null) {
            if (path.endsWith("/") && !path.equals("/")) {
                path = StringUtils.substringBeforeLast(path, "/");
            }
            try {
                this.localPath = objectNode.getPath();
                this.localPathInProvider = objectNode.getPath();
                if (path.contains(JCRSessionWrapper.DEREF_SEPARATOR)) {
                    this.localPath = path;
                }
                // In case we are accessing versionned node, this ensure that localPath contain the expected localPath
                if (this.localPath.startsWith("/jcr:system")) {
                    this.localPath = path;
                    this.localPathInProvider = path;
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            this.localPath = objectNode.getPath();
            this.localPathInProvider = objectNode.getPath();
        }
        if (parent != null) {
            parentAlreadyResolved = true;
            resolvedParentNode = parent;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getRealNode() {
        return objectNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public JCRUserNode getUser() {
        try {
            return (JCRUserNode) session.getUserNode();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<String[]>> getAclEntries() {
        try {
            String path = getPath();
            if (aclEntries == null) {
                Map<String, List<String[]>> entries = new LinkedHashMap<String, List<String[]>>();
                breakAcl = false;
                try {
                    if (hasNode("j:acl")) {
                        Node acl = getNode("j:acl");
                        NodeIterator aces = acl.getNodes();
                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            if (ace.isNodeType("jnt:ace")) {
                                if (!ace.hasProperty(Constants.J_ROLES)) {
                                    continue;
                                }
                                String principal = ace.getProperty("j:principal").getString();

                                if (!entries.containsKey(principal)) {
                                    entries.put(principal, new ArrayList<String[]>());
                                }
                                Value[] roles = ace.getProperty(Constants.J_ROLES).getValues();
                                if (!ace.isNodeType("jnt:externalAce")) {
                                    String type = ace.getProperty("j:aceType").getString();
                                    for (Value role : roles) {
                                        entries.get(principal).add(new String[] {path, type, role.getString()});
                                    }
                                } else {
                                    for (Value role : roles) {
                                        entries.get(principal).add(new String[] {path, "EXTERNAL", role.getString() + "/" + ace.getProperty("j:externalPermissionsName").getString()});
                                    }
                                }
                            }
                        }
                        breakAcl = (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean());
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug(e.getMessage(), e);
                }

                Map<String, List<String[]>> result = entries;

                if (!breakAcl && !path.equals("/")) {
                    result = new LinkedHashMap<>();
                    for (Map.Entry<String, List<String[]>> entry : entries.entrySet()) {
                        result.put(entry.getKey(), new ArrayList<String[]>(entry.getValue()));
                    }
                    try {
                        for (Map.Entry<String, List<String[]>> entry : getParent().getAclEntries().entrySet()) {
                            String key = entry.getKey();
                            List<String[]> value = entry.getValue();
                            List<String[]> aclsForKey = result.get(key);
                            if (aclsForKey != null) {
                                aclsForKey.addAll(value);
                            } else {
                                result.put(key, value);
                            }
                        }
                    } catch (ItemNotFoundException e) {
                        logger.debug(e.getMessage(), e);
                    }
                }
                if (session.isReadOnlyCacheEnabled()) {
                    aclEntries = result;
                }
                return result;
            }

            return aclEntries;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Map<String, List<JCRNodeWrapper>> getAvailableRoles() throws RepositoryException {
        Map<String, List<JCRNodeWrapper>> res = new HashMap<String, List<JCRNodeWrapper>>();
        NodeIterator ni = session.getWorkspace().getQueryManager().createQuery(
                "select * from [" + Constants.JAHIANT_ROLE + "] as r where isdescendantnode(r,['/roles'])",
                Query.JCR_SQL2).execute().getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper role = (JCRNodeWrapper) ni.nextNode();
            boolean add = false;
            if (role.hasProperty("j:hidden") && role.getProperty("j:hidden").getBoolean()) {
                // skip
            } else if (role.hasProperty("j:nodeTypes")) {
                Value[] values = role.getProperty("j:nodeTypes").getValues();
                if (values.length > 0) {
                    for (Value value : values) {
                        if (isNodeType(value.getString())) {
                            add = true;
                            break;
                        }
                    }
                } else {
                    add = true;
                }
            } else {
                add = true;
            }
            if (add) {
                String roleGroup;
                if (role.hasProperty("j:roleGroup")) {
                    roleGroup = role.getProperty("j:roleGroup").getString();
                } else {
                    roleGroup = "default";
                }
                if (!res.containsKey(roleGroup)) {
                    res.put(roleGroup, new ArrayList<JCRNodeWrapper>());
                }
                res.get(roleGroup).add(role);
            }
        }
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(String perm) {
        try {
            if (session.isSystem()) {
                return true;
            }

            AccessControlManager accessControlManager = getAccessControlManager();
            return accessControlManager == null || accessControlManager.hasPrivileges(localPathInProvider, new Privilege[] {accessControlManager.privilegeFromName(perm)});
        } catch (RepositoryException re) {
            logger.error("Cannot check permission " + perm, re);
            return false;
        }
    }

    @Override
    public AccessControlManager getAccessControlManager() throws RepositoryException {
        Session providerSession = session.getProviderSession(provider);
        // this is not a Jackrabbit implementation, we will use the new JCR 2.0 API instead.
        AccessControlManager accessControlManager = null;
        try {
            accessControlManager = providerSession.getAccessControlManager();
        } catch (UnsupportedRepositoryOperationException uroe) {
            logger.warn("Access control manager is not supported for node " + getPath() + ": " + uroe.getMessage());
        }
        return accessControlManager;
    }

    @Override
    public Set<String> getPermissions() {
        Set<String> result = new HashSet<String>();
        try {
            AccessControlManager accessControlManager = getAccessControlManager();
            if (accessControlManager != null) {
                Privilege[] p = accessControlManager.getPrivileges(localPathInProvider);
                for (Privilege privilege : p) {
                    result.add(privilege.getName());
                    if (privilege.isAggregate()) {
                        for (Privilege privilege1 : privilege.getAggregatePrivileges()) {
                            result.add(privilege1.getName());
                        }
                    }
                }
            }
        } catch (RepositoryException re) {
            logger.error("Cannot check perm ", re);
        }
        return result;
    }

    @Override
    public BitSet getPermissionsAsBitSet() {
        BitSet b = null;
        try {
            AccessControlManager accessControlManager = getAccessControlManager();
            if (accessControlManager == null) {
                return b;
            }
            Privilege[] app = accessControlManager.getPrivileges(localPathInProvider);
            Privilege[] pr = accessControlManager.getSupportedPrivileges(localPathInProvider);
            b = new BitSet(pr.length);
            if (app.length == pr.length) {
                // in case of admin user all supported permissions are present
                b.set(0, pr.length);
            } else {
                Set<Privilege> effective = new HashSet<Privilege>();
                for (Privilege privilege : app) {
                    effective.add(privilege);
                    if (privilege.isAggregate()) {
                        for (Privilege p : privilege.getAggregatePrivileges()) {
                            effective.add(p);
                        }
                    }
                }
                int position = 0;
                for (Privilege privilege : pr) {
                    if (effective.contains(privilege)) {
                        b.set(position);
                    }
                    position++;
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot check perm ", e);
        }
        return b;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean grantRoles(String principalKey, Set<String> roles) throws RepositoryException {
        Map<String, String> m = new HashMap<String, String>();
        for (String role : roles) {
            m.put(role, "GRANT");
        }
        return changeRoles(principalKey, m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean denyRoles(String principalKey, Set<String> roles) throws RepositoryException {
        Map<String, String> m = new HashMap<String, String>();
        for (String role : roles) {
            m.put(role, "DENY");
        }
        return changeRoles(principalKey, m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean changeRoles(String principalKey, Map<String, String> roles) throws RepositoryException {
        if (!isCheckedOut() && isNodeType(Constants.MIX_VERSIONABLE)) {
            getSession().checkout(this);
        }

        List<String> gr = new ArrayList<String>();
        List<String> den = new ArrayList<String>();
        List<String> rem = new ArrayList<String>();

        for (Map.Entry<String, String> entry : roles.entrySet()) {
            if ("GRANT".equals(entry.getValue())) {
                gr.add(entry.getKey());
            } else if ("DENY".equals(entry.getValue())) {
                den.add(entry.getKey());
            } else if ("REMOVE".equals(entry.getValue())) {
                rem.add(entry.getKey());
            }
        }

        Node acl = getOrCreateAcl();
        NodeIterator ni = acl.getNodes();
        Node aceg = null;
        Node aced = null;
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.isNodeType("jnt:ace") && !ace.isNodeType("jnt:externalAce")) {
                if (ace.getProperty("j:principal").getString().equals(principalKey)) {
                    if (ace.getProperty("j:aceType").getString().equals("GRANT")) {
                        aceg = ace;
                    } else {
                        aced = ace;
                    }
                }
            }
        }
        if (aceg == null) {
            aceg = acl.addNode("GRANT_" + JCRContentUtils.replaceColon(principalKey).replaceAll("/", "_"), "jnt:ace");
            aceg.setProperty("j:principal", principalKey);
            aceg.setProperty("j:protected", false);
            aceg.setProperty("j:aceType", "GRANT");
        }
        if (aced == null) {
            aced = acl.addNode("DENY_" + JCRContentUtils.replaceColon(principalKey).replaceAll("/", "_"), "jnt:ace");
            aced.setProperty("j:principal", principalKey);
            aced.setProperty("j:protected", false);
            aced.setProperty("j:aceType", "DENY");
        }

        List<String> grClone = new ArrayList<String>(gr);
        List<String> denClone = new ArrayList<String>(den);
        if (aceg.hasProperty(Constants.J_ROLES)) {
            final Value[] values = aceg.getProperty(Constants.J_ROLES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s) && !rem.contains(s)) {
                    grClone.add(s);
                }
            }
        }
        if (aced.hasProperty(Constants.J_ROLES)) {
            final Value[] values = aced.getProperty(Constants.J_ROLES).getValues();
            for (Value value : values) {
                final String s = value.getString();
                if (!gr.contains(s) && !den.contains(s) && !rem.contains(s)) {
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

        this.aclEntries = null;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean revokeRolesForPrincipal(String principalKey) throws RepositoryException {
        boolean modified = false;
        Node acl = getOrCreateAcl();

        NodeIterator ni = acl.getNodes();
        while (ni.hasNext()) {
            Node ace = ni.nextNode();
            if (ace.isNodeType("jnt:ace") && !ace.isNodeType("jnt:externalAce")) {
                if (ace.getProperty("j:principal").getString().equals(principalKey)) {
                    ace.remove();
                    modified = true;
                }
            }
        }

        if (modified) {
            aclEntries = null;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean revokeAllRoles() throws RepositoryException {
        boolean modified = false;
        if (hasNode("j:acl")) {
            JCRNodeWrapper acl = getNode("j:acl");
            NodeIterator ni = acl.getNodes();
            while (ni.hasNext()) {
                Node ace = ni.nextNode();
                if (ace.isNodeType("jnt:ace") && !ace.isNodeType("jnt:externalAce")) {
                    ace.remove();
                    modified = true;
                }
            }
            if (!acl.hasNodes()) {
                acl.remove();
                modified = true;
                if (isNodeType("jmix:accessControlled")) {
                    removeMixin("jmix:accessControlled");
                }
            }
            if (modified) {
                aclEntries = null;
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setAclInheritanceBreak(boolean breakAclInheritance) throws RepositoryException {
        try {
            boolean inheritAcl = !breakAclInheritance;
            Node aclNode = getOrCreateAcl();
            if (!aclNode.hasProperty("j:inherit") || aclNode.getProperty("j:inherit").getBoolean() != inheritAcl) {
                aclNode.setProperty("j:inherit", inheritAcl);
                aclEntries = null;
            }
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
     * @throws RepositoryException in case of JCR-related errors
     */
    @Override
    public boolean getAclInheritanceBreak() throws RepositoryException {
        if (hasNode("j:acl")) {
            Node acl = getNode("j:acl");
            return acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean();
        }
        return false;
    }

    /**
     * Returns the ACL node of the given node or creates one
     *
     * @return the ACL <code>Node</code> for the given node
     * @throws RepositoryException in case of JCR-related errors
     */
    public Node getOrCreateAcl() throws RepositoryException {
        if (hasNode("j:acl")) {
            return getNode("j:acl");
        } else {
            if (!isCheckedOut()) {
                checkout();
            }
            addMixin("jmix:accessControlled");
            return addNode("j:acl", "jnt:acl");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeWrapper createCollection(String name) throws RepositoryException {
        return addNode(name, Constants.JAHIANT_FOLDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeWrapper uploadFile(String name, final InputStream is, final String contentType) throws RepositoryException {
        JCRLockUtils.checkLock(this, false, true);

        name = JCRContentUtils.escapeLocalNodeName(FilenameUtils.getName(name));

        JCRNodeWrapper file = null;
        try {
            file = getNode(name);
            if (!file.isCheckedOut()) {
                file.getSession().checkout(file);
            }
        } catch (PathNotFoundException e) {
            logger.debug("file {} does not exist, creating...", name);
            if (!isCheckedOut()) {
                getSession().checkout(this);
            }
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
    @Override
    public JCRNodeWrapper addNode(String name) throws RepositoryException {
        JCRLockUtils.checkLock(this, false, true);

        Node n = objectNode.addNode(name);
        return provider.getNodeWrapper(n, buildSubnodePath(name), this, session);
    }

    protected String buildSubnodePath(String name) {
        if (localPath.equals("/")) {
            return localPath + name;
        } else {
            return localPath + "/" + name;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeWrapper addNode(String name, String type) throws RepositoryException {
        JCRLockUtils.checkLock(this, false, true);
        Node n = objectNode.addNode(name, type);
        JCRNodeWrapper newNode = provider.getNodeWrapper(n, buildSubnodePath(name), this, session);
        session.registerNewNode(newNode);
        return newNode;
    }

    @Override
    public JCRNodeWrapper addNode(String name, String type, String identifier, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException {
        JCRLockUtils.checkLock(this, false, true);

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
    @Override
    public JCRPlaceholderNode getPlaceholder() throws RepositoryException {
        return new JCRPlaceholderNode(this);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated As of JCR 2.0, {@link #getIdentifier()} should be used instead.
     */
    @Override
    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {
        return objectNode.getUUID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAbsoluteUrl(ServletRequest request) {
        return provider.getAbsoluteContextPath(request) + getUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        try {
            if (isNodeType(Constants.NT_FILE)) {
                return provider.getHttpPath() + "/" + getSession().getWorkspace().getName() + Text.escapePath(getCanonicalPath());
            } else {
                String path = JCRSessionFactory.getInstance().getCurrentServletPath();
                if (path == null) {
                    path = "/cms/render";
                }
                return Jahia.getContextPath() + path + "/" + getSession().getWorkspace().getName() + "/" + getSession().getLocale() + Text.escapePath(getPath()) + ".html";
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get type", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAbsoluteWebdavUrl(final HttpServletRequest request) {
        return provider.getAbsoluteContextPath(request) + getWebdavUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWebdavUrl() {
        return Jahia.getContextPath() + provider.getWebdavPath() + "/" + session.getWorkspace().getName() + localPathInProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getThumbnails() {
        List<String> names = new ArrayList<String>();
        try {
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                String name = node.getName();
                if (!name.equals("jcr:content") && (node.isNodeType("jnt:resource")
                        || (node.isNodeType("nt:frozenNode") && "jnt:resource".equals(node.getProperty("jcr:frozenPrimaryType").getString())))) {
                    names.add(name);
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
    @Override
    public String getThumbnailUrl(String name) {
        String url = getUrl();
        return url + (url.indexOf('?') != -1 ? "&" : "?") + "t=" + name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public JCRNodeWrapper getNode(String s) throws PathNotFoundException, RepositoryException {
        if (objectNode.hasNode(s)) {
            // we have that node in our default JR repository
            final Node node = objectNode.getNode(s);
            // check if this node is not a dynamic mount point to an external repository
            if (!isExternalRoot(node)) {
                // the node is local -> get it
                return s.indexOf('/') == -1 ? provider.getNodeWrapper(node, buildSubnodePath(s), this, session)
                        : provider.getNodeWrapper(node, session);
            }
        }
        // no local node found -> check mounted providers
        if (provider.getService() != null && provider.getSessionFactory().areMultipleMountPointsRegistered()) {
            String targetPath = getPath() + '/' + s;
            JCRStoreProvider targetProvider = provider.getSessionFactory().getProvider(targetPath, false);
            if (targetProvider != null && targetProvider != provider) {
                // we found a provider which can handle the specified path
                JCRNodeWrapper providerRoot = targetProvider.getNodeWrapper(session.getProviderSession(targetProvider)
                        .getNode(targetProvider.getRelativeRoot().isEmpty() ? "/" : targetProvider.getRelativeRoot()),
                        "/", this, session);
                if (!targetProvider.getMountPoint().equals(targetPath)) {
                    String childPath = StringUtils.substringAfter(targetPath, targetProvider.getMountPoint() + "/");
                    if (childPath.length() > 0) {
                        return providerRoot.getNode(childPath);
                    }
                }

                return providerRoot;
            }
        }

        throw new PathNotFoundException(s);
    }

    private boolean isExternalRoot(final Node node) throws RepositoryException, ValueFormatException,
            PathNotFoundException {
        return node.hasProperty("j:isExternalProviderRoot")
                && node.getProperty("j:isExternalProviderRoot").getBoolean()
                || provider.getSessionFactory().getMountPoints().containsKey(node.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeIteratorWrapper getNodes() throws RepositoryException {
        return getNodes(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeIteratorWrapper getNodes(String namePattern) throws RepositoryException {
        return getNodes(namePattern, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeIteratorWrapper getNodes(String[] nameGlobs) throws RepositoryException {
        return getNodes(null, nameGlobs);
    }

    protected JCRNodeIteratorWrapper getNodes(String namePattern, String[] nameGlobs) throws RepositoryException {
        List<JCRNodeWrapper> list = null;
        List<String> mounts = null;
        if (provider.getService() != null && provider.getSessionFactory().areMultipleMountPointsRegistered()) {
            Map<String, JCRStoreProvider> mountPoints = provider.getSessionFactory().getMountPoints();
                // if we have any registered mount points (except the default one, which is "/")
                String path = getPath();
                for (Map.Entry<String, JCRStoreProvider> entry : mountPoints.entrySet()) {
                    String key = entry.getKey();
                    // skip default provider and those, whose mount point path does not start with the path of this node
                    if (!entry.getValue().isDefault() && key.startsWith(path)) {
                        int pos = key.lastIndexOf('/');
                        String mpp = pos > 0 ? key.substring(0, pos) : "/";
                        if (mpp.equals(path)) {
                            // mount point matches the path; check name patterns if they were specified
                            final String name = key.substring(pos + 1);
                            if (namePattern == null
                                    && nameGlobs == null
                                    || key.length() > pos + 1
                                    && (namePattern != null
                                    && ChildrenCollectorFilter.matches(name, namePattern) || nameGlobs != null
                                    && ChildrenCollectorFilter.matches(name, nameGlobs))) {
                                JCRStoreProvider storeProvider = entry.getValue();
                                String root = storeProvider.getRelativeRoot();

                                try {
                                    final Node node = session.getProviderSession(storeProvider).getNode(
                                            root.length() == 0 ? "/" : root);
                                    if (list == null) {
                                        list = new LinkedList<JCRNodeWrapper>();
                                        mounts = new LinkedList<String>();
                                    }
                                    list.add(storeProvider.getNodeWrapper(node, "/", this, session));
                                    mounts.add(name);
                                } catch (PathNotFoundException e) {
                                    // current session doesn't have the right to read the mounted node
                                }
                            }
                        }
                    }
                }
        }

        NodeIterator ni = namePattern != null ? objectNode.getNodes(namePattern) : (nameGlobs != null ? objectNode
                .getNodes(nameGlobs) : objectNode.getNodes());

        return new ChildNodesIterator(ni, mounts, list, this, session, provider);
    }

    @Override
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
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
                    StringBuilder b = new StringBuilder();
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
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        try {
            if ((localPathInProvider.equals("/") || localPathInProvider.equals(provider.getRelativeRoot())) && provider.getMountPoint().length() > 1) {
                String mp = provider.getMountPoint();
                return mp.substring(mp.lastIndexOf('/') + 1);
            } else {
                return objectNode.getName();
            }
        } catch (RepositoryException e) {
            logger.error("Repository error unable to read node {}", localPath, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getPrimaryNodeTypeName() throws RepositoryException {
        String name = objectNode.getPrimaryNodeType().getName();
        if (NodeTypeRegistry.getInstance().hasNodeType(name)) {
            return name;
        } else {
            return "nt:base";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedNodeType[] getMixinNodeTypes() throws RepositoryException {
        List<NodeType> l = null;
        NodeType[] mixinNodeTypes = objectNode.getMixinNodeTypes();
        for (NodeType nodeType : mixinNodeTypes) {
            try {
                if (l == null) {
                    l = new ArrayList<NodeType>(mixinNodeTypes.length);
                }
                l.add(NodeTypeRegistry.getInstance().getNodeType(nodeType.getName()));
            } catch (NoSuchNodeTypeException e) {
                logger.debug("Skipping missing mixin {}", nodeType.getName());
            }
        }
        return l != null ? l.toArray(new ExtendedNodeType[l.size()]) : EMPTY_EXTENDED_NODE_TYPE_ARRAY;
    }

    public ExtendedNodeType[] getOriginalMixinNodeTypes() throws RepositoryException {
        if (originalMixins == null) {
            originalMixins = getMixinNodeTypes();
        }
        return originalMixins;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMixin(String s) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        checkLock();
        try {
            getOriginalMixinNodeTypes();
            objectNode.addMixin(s);
        } finally {
            flushLocalCaches();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {

        checkLock();

        NodeTypeRegistry nodeTypeRegistry = NodeTypeRegistry.getInstance();
        ExtendedNodeType mixin = nodeTypeRegistry.getNodeType(mixinName);

        try {
            getOriginalMixinNodeTypes();
            objectNode.removeMixin(mixinName);

            // Removing a mixin also causes removal of any child nodes defined by the mixin.
            // In case the mixin defines any child nodes, flush the cache to ensure it does not contain any nodes
            // that has been just removed along with the mixin.
            if (!mixin.getChildNodeDefinitionsAsMap().isEmpty()) {
                getSession().flushCaches();
            }

        } finally {
            flushLocalCaches();
        }

        // Remove i18n properties defined by the mixin, from translation nodes, if any.
        for (NodeIterator translationNodes = objectNode.getNodes(TRANSLATION_NODES_PATTERN); translationNodes.hasNext(); ) {

            Node translationNode = translationNodes.nextNode();

            for (PropertyIterator properties = translationNode.getProperties(); properties.hasNext(); ) {

                Property property = properties.nextProperty();

                if (!property.getDefinition().getName().equals("*")) {
                    // The property matches translation node's own named property definition rather than a property definition
                    // provided by the parent node - must be preserved.
                    logger.debug("removeMixin - preserving property '{}'", property.getPath());
                    continue;
                }

                ExtendedPropertyDefinition propertyDefinition = getApplicablePropertyDefinition(property.getName(), property.getType(), false);
                if (propertyDefinition == null) {
                    propertyDefinition = getApplicablePropertyDefinition(property.getName(), property.getType(), true);
                }
                if (propertyDefinition != null && propertyDefinition.isInternationalized()) {
                    // After removing the mixin, the parent node still has an i18n property definition that matches this property,
                    // so the property is defined by another (still existing) type of the node and must be preserved therefore.
                    logger.debug("removeMixin - preserving property '{}'", property.getPath());
                    continue;
                }

                logger.debug("removeMixin - removing property '{}'", property.getPath());
                property.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAddMixin(String s) throws NoSuchNodeTypeException, RepositoryException {
        try {
            checkLock();
        } catch (LockException e) {
            return false;
        }

        return objectNode.canAddMixin(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public List<String> getNodeTypes() throws RepositoryException {
        List<String> results = new ArrayList<String>();
        if (NodeTypeRegistry.getInstance().hasNodeType(objectNode.getPrimaryNodeType().getName())) {
            results.add(objectNode.getPrimaryNodeType().getName());
        } else {
            results.add("nt:base");
        }
        NodeType[] mixin = objectNode.getMixinNodeTypes();
        for (int i = 0; i < mixin.length; i++) {
            NodeType mixinType = mixin[i];
            if (NodeTypeRegistry.getInstance().hasNodeType(mixinType.getName())) {
                results.add(mixinType.getName());
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNodeType(String type) throws RepositoryException {
        return "nt:base".equals(type) || objectNode.isNodeType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public boolean isFile() {
        try {
            return isNodeType(Constants.NT_FILE);
        } catch (RepositoryException e) {
            logger.error("Cannot get type", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPortlet() {
        try {
            return isNodeType(Constants.JAHIANT_PORTLET);
        } catch (RepositoryException e) {
            logger.error("Cannot get type", e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public String getLanguage() {
        String language = null;
        try {
            if (Constants.JAHIANT_TRANSLATION.equals(getPrimaryNodeTypeName())) {
                language = getProperty("jcr:language").getString();
            }
        } catch (RepositoryException e1) {
        }
        if (language == null && getSession().getLocale() != null) {
            try {
                language = getI18N(getSession().getLocale()).getProperty("jcr:language")
                        .getString();
            } catch (Exception e) {
                language = getSession().getLocale().toString();
            }
        }
        return language;
    }

    @Override
    public List<Locale> getExistingLocales() throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = getI18Ns();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            r.add(LanguageCodeConverters.languageCodeToLocale(n.getProperty("jcr:language").getString()));
        }
        return r;
    }

    /**
     * Return the internationalization node, containing localized properties
     *
     * @param locale
     * @return
     */
    @Override
    public Node getI18N(Locale locale) throws RepositoryException {
        return getI18N(locale, true);
    }

    @Override
    public boolean hasI18N(Locale locale) throws RepositoryException {
        return hasI18N(locale, true);
    }

    @Override
    public boolean hasI18N(Locale locale, boolean fallback) throws RepositoryException {
        return hasI18N(locale, fallback, true);
    }

    private boolean hasI18N(Locale locale, boolean fallback, boolean checkPublication) throws RepositoryException {
        boolean b = checkI18NNode(locale, checkPublication);
        if (!b && fallback) {
            final Locale fallbackLocale = getSession().getFallbackLocale();
            if (fallbackLocale != null && fallbackLocale != locale) {
                b = checkI18NNode(fallbackLocale, checkPublication);
            }
        }
        return b;
    }

    private boolean checkI18NNode(Locale locale, boolean checkPublication) throws RepositoryException {
        boolean b = false;
        final String transName = getTranslationNodeName(locale);
        if ((i18NobjectNodes != null && i18NobjectNodes.containsKey(locale)) || objectNode.hasNode(transName)) {
            if (checkPublication && Constants.LIVE_WORKSPACE.equals(session.getWorkspace().getName())) {
                final Node node = objectNode.getNode(transName);
                b = !node.hasProperty(Constants.PUBLISHED) || node.getProperty(Constants.PUBLISHED).getBoolean();
            } else {
                b = true;
            }
        }
        return b;
    }

    static String getTranslationNodeName(Locale locale) {
        return TRANSLATION_PREFIX + locale;
    }

    @Override
    public Node getI18N(Locale locale, boolean fallback) throws RepositoryException {
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
        } else {
            final String translationNodeName = getTranslationNodeName(locale);
            if (objectNode.hasNode(translationNodeName)) {
                node = objectNode.getNode(translationNodeName);
                if (!Constants.LIVE_WORKSPACE.equals(session.getWorkspace().getName()) || !node.hasProperty(Constants.PUBLISHED) || node.getProperty(Constants.PUBLISHED).getBoolean()) {
                    i18NobjectNodes.put(locale, node);
                    return node;
                }
            }
        }

        if (fallback) {
            final Locale fallbackLocale = getSession().getFallbackLocale();
            if (fallbackLocale != null && fallbackLocale != locale) {
                return getI18N(fallbackLocale);
            }
        }
        throw new ItemNotFoundException(locale.toString());
    }

    @Override
    public NodeIterator getI18Ns() throws RepositoryException {
        return objectNode.getNodes("j:translation*");
    }

    @Override
    public Node getOrCreateI18N(final Locale locale) throws RepositoryException {
        try {
            return getI18N(locale, false);
        } catch (RepositoryException e) {
            Node t = objectNode.addNode(getTranslationNodeName(locale), Constants.JAHIANT_TRANSLATION);
            t.setProperty("jcr:language", locale.toString());
            i18NobjectNodes.put(locale, t);
            return t;
        }
    }

    @Override
    public Node getOrCreateI18N(Locale locale, Calendar created, String createdBy, Calendar lastModified, String lastModifiedBy) throws RepositoryException {
        JahiaSessionImpl jrSession = (JahiaSessionImpl) objectNode.getSession();

        try {
            return getI18N(locale, false);
        } catch (RepositoryException e) {
            try {
                jrSession.getNodeTypeInstanceHandler().setCreated(created);
                jrSession.getNodeTypeInstanceHandler().setCreatedBy(createdBy);
                jrSession.getNodeTypeInstanceHandler().setLastModified(lastModified);
                jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(lastModifiedBy);

                Node t = objectNode.addNode(getTranslationNodeName(locale), Constants.JAHIANT_TRANSLATION);
                t.setProperty("jcr:language", locale.toString());

                i18NobjectNodes.put(locale, t);
                return t;
            } finally {
                jrSession.getNodeTypeInstanceHandler().setCreated(null);
                jrSession.getNodeTypeInstanceHandler().setCreatedBy(null);
                jrSession.getNodeTypeInstanceHandler().setLastModified(null);
                jrSession.getNodeTypeInstanceHandler().setLastModifiedBy(null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper getProperty(String name) throws javax.jcr.PathNotFoundException, javax.jcr.RepositoryException {
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd != null) {
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
        if (epd == null) {
            throw new PathNotFoundException(name);
        }
        if (locale != null) {
            if (epd.isInternationalized()) {
                Pattern pathPattern = JCRContentUtils.getInstance().getHandleFallbackLocaleForPathPattern();
                if (pathPattern == null || locale.equals(SettingsBean.getInstance().getDefaultLocale())) {
                    try {
                        final Node localizedNode = getI18N(locale);
                        return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                                session, provider, epd,
                                name);
                    } catch (ItemNotFoundException e) {
                        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
                    }
                } else {
                    return internalGetPropertyI18nWithDefFallback(name, epd, locale, pathPattern);
                }
            }
        }
        return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
    }

    private JCRPropertyWrapper internalGetPropertyI18nWithDefFallback(String name,
                                                                      ExtendedPropertyDefinition epd, Locale locale, Pattern pathPattern) throws RepositoryException {
        Node localizedNode = null;
        try {
            localizedNode = getI18N(locale);
            return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                    session, provider, epd,
                    name);
        } catch (ItemNotFoundException e) {
            try {
                return new JCRPropertyWrapperImpl(this, objectNode.getProperty(name), session, provider, epd);
            } catch (PathNotFoundException pnte) {
                if (pathPattern.matcher(getPath()).matches()) {
                    try {
                        localizedNode = getI18N(SettingsBean.getInstance().getDefaultLocale());
                        return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                                session, provider, epd,
                                name);
                    } catch (ItemNotFoundException e2) {
                        throw pnte;
                    }
                }
                throw pnte;
            }
        } catch (PathNotFoundException e) {
            if (pathPattern.matcher(getPath()).matches()) {
                try {
                    localizedNode = getI18N(SettingsBean.getInstance().getDefaultLocale());
                    return new JCRPropertyWrapperImpl(this, localizedNode.getProperty(name),
                            session, provider, epd,
                            name);
                } catch (ItemNotFoundException e2) {
                    throw e;
                }
            }
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale);
        }
        return new LazyPropertyIterator(this, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getPropertyAsString(String name) {
        try {
            Property property = getProperty(name);
            if (property == null) {
                return null;
            }
            if (property.getType() == PropertyType.BINARY) {
                return null;
            }
            if (!property.isMultiple()) {
                return property.getString();
            } else {
                Value[] vs = property.getValues();
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < vs.length; i++) {
                    Value v = vs[i];
                    b.append(v.getString());
                    if (i + 1 < vs.length) {
                        b.append(" ");
                    }
                }
                return b.toString();
            }
        } catch (RepositoryException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public JCRPropertyWrapper setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        hasPropertyCache.remove(name);

        name = ensurePrefixedName(name);
        final Locale locale = getSession().getLocale();
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

        if (value != null
                && PropertyType.UNDEFINED != epd.getRequiredType()
                && value.getType() != epd.getRequiredType()
                && !(value.getType() == PropertyType.WEAKREFERENCE
                && epd.getRequiredType() == PropertyType.REFERENCE)) {
            // if the type doesn't match the required type, we attempt a conversion.
            value = getSession().getValueFactory().createValue(value.getString(), epd.getRequiredType());
        }

        if (!session.isSystem() && !epd.isProtected() && !epd.getDeclaringNodeType().canSetProperty(epd.getName(), value)) {
            throw new ConstraintViolationException("Invalid value for : " + epd.getName());
        }

        JCRLockUtils.checkLock(this, locale != null && epd.isInternationalized(), false);
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);

        if (locale != null) {
            if (epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value), session, provider, epd, name);
            }
        }

        if (value == null) {
            objectNode.setProperty(name, (Value) null);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        hasPropertyCache.remove(name);

        final Locale locale = getSession().getLocale();
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

        if (!session.isSystem() && !epd.isProtected() && !epd.getDeclaringNodeType().canSetProperty(epd.getName(), value)) {
            throw new ConstraintViolationException("Invalid value for : " + epd.getName());
        }

        JCRLockUtils.checkLock(this, locale != null && epd.isInternationalized(), false);
        value = JCRStoreService.getInstance().getInterceptorChain().beforeSetValue(this, name, epd, value);

        if (locale != null) {
            if (epd.isInternationalized()) {
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, value, type), session, provider, epd, name);
            }
        }

        if (value == null) {
            objectNode.setProperty(name, (Value) null);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, value, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        hasPropertyCache.remove(name);
        final Locale locale = getSession().getLocale();
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }
        if (values != null) {
            Value[] valuesCopy = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null
                        && PropertyType.UNDEFINED != epd.getRequiredType()
                        && values[i].getType() != epd.getRequiredType()
                        && !(values[i].getType() == PropertyType.WEAKREFERENCE
                        && epd.getRequiredType() == PropertyType.REFERENCE)) {
                    valuesCopy[i] = getSession().getValueFactory()
                            .createValue(values[i].getString(), epd.getRequiredType());
                } else {
                    valuesCopy[i] = values[i];
                }
            }

            values = valuesCopy;
        }

        if (!session.isSystem() && !epd.isProtected() && !epd.getDeclaringNodeType().canSetProperty(epd.getName(), values)) {
            throw new ConstraintViolationException("Invalid value for : " + epd.getName());
        }

        JCRLockUtils.checkLock(this, locale != null && epd.isInternationalized(), false);
        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd.isInternationalized()) {
                if (values == null) {
                    getOrCreateI18N(locale).setProperty(name, (Value[]) null);
                    return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
                }
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values), session, provider, epd, name);
            }
        }

        if (values == null) {
            objectNode.setProperty(name, (Value[]) null);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }

        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String namespace, String name, String value) throws RepositoryException {
        String pref = objectNode.getSession().getNamespacePrefix(namespace);
        String key = pref + ":" + name;
        return setProperty(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        final Locale locale = getSession().getLocale();
        hasPropertyCache.remove(name);
        name = ensurePrefixedName(name);
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
        if (epd == null) {
            throw new ConstraintViolationException("Couldn't find definition for property " + name);
        }

        if (!session.isSystem() && !epd.isProtected() && !epd.getDeclaringNodeType().canSetProperty(epd.getName(), values)) {
            throw new ConstraintViolationException("Invalid value for : " + epd.getName());
        }

        JCRLockUtils.checkLock(this, locale != null && epd.isInternationalized(), false);
        values = JCRStoreService.getInstance().getInterceptorChain().beforeSetValues(this, name, epd, values);

        if (locale != null) {
            if (epd.isInternationalized()) {
                if (values == null) {
                    getOrCreateI18N(locale).setProperty(name, (Value[]) null);
                    return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
                }
                return new JCRPropertyWrapperImpl(this, getOrCreateI18N(locale).setProperty(name, values, type), session, provider, epd, name);
            }
        }

        if (values == null) {
            objectNode.setProperty(name, (Value[]) null);
            return new JCRPropertyWrapperImpl(this, null, session, provider, epd);
        }
        return new JCRPropertyWrapperImpl(this, objectNode.setProperty(name, values, type), session, provider, epd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public JCRPropertyWrapper setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRPropertyWrapper setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        Value v = getSession().getValueFactory().createValue(value);
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public JCRPropertyWrapper setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, RepositoryException {
        Value v = null;
        if (value != null) {
            if (value instanceof JCRNodeWrapper) {
                value = ((JCRNodeWrapper) value).getRealNode();
            }
            ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(name);
            if (epd != null) {
                v = getSession().getValueFactory().createValue(value, true); // in Jahia we always create weak-references
            }
        }
        return setProperty(name, v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
        ExtendedPropertyDefinition epd = getApplicablePropertyDefinition(propertyName);
        if (epd == null) {
            return false;
        }
        if (locale != null && !propertyName.equals("jcr:language")) {
            try {
                if (epd.isInternationalized()) {
                    if (hasI18N(locale, true)) {
                        final Node localizedNode = getI18N(locale);
                        return localizedNode.hasProperty(propertyName);
                    }
                }
            } catch (ConstraintViolationException e) {
                return false;
            }
        }
        if (objectNode instanceof NodeImpl) {
            return ((NodeImpl) objectNode).hasProperty(((SessionImpl) objectNode.getSession()).getQName(propertyName));
        }
        return objectNode.hasProperty(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
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
        if (i18NobjectNodes != null) {
            i18NobjectNodes.clear();
        }
        this.localPathInProvider = parent.getPath() + "/" + newName;
        String mountPoint = getProvider().getMountPoint();
        if (mountPoint.length() > 1 && localPathInProvider.startsWith(mountPoint)) {
            localPathInProvider = StringUtils.substringAfter(localPathInProvider, mountPoint);
        }
        this.localPath = localPathInProvider;

        this.objectNode = getSession().getProviderSession(getProvider()).getNode(localPathInProvider);
        if ((nodePositionFound) && (parent.getPrimaryNodeType().hasOrderableChildNodes())) {
            JCRLockUtils.checkLock(parent, false, true);
            parent.getRealNode().orderBefore(newName, nextNodeName);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(String dest) throws RepositoryException {
        return copy(dest, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(String dest, String name) throws RepositoryException {
        return copy(dest, name, NodeNamingConflictResolutionStrategy.MERGE);
    }

    @Override
    public boolean copy(String dest, String name, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException {
        JCRNodeWrapper node = (JCRNodeWrapper) session.getItem(dest);
        boolean sameProvider = (provider.getKey().equals(node.getProvider().getKey()));
        if (!sameProvider) {
            copy(node, name, true, namingConflictResolutionStrategy);
            node.save();
        } else {
            copy(node, name, true, namingConflictResolutionStrategy);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes) throws RepositoryException {
        return copy(dest, name, allowsExternalSharedNodes, NodeNamingConflictResolutionStrategy.MERGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy) throws RepositoryException {
        Map<String, List<String>> references = new HashMap<String, List<String>>();
        boolean copy = copy(dest, name, allowsExternalSharedNodes, references, null, 0, new MutableInt(0), namingConflictResolutionStrategy);
        ReferencesHelper.resolveCrossReferences(getSession(), references, false);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references) throws RepositoryException {
        return copy(dest, name, allowsExternalSharedNodes, references, null, 0, new MutableInt(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, List<String> ignoreNodeTypes, int maxBatch) throws RepositoryException {
        Map<String, List<String>> references = new HashMap<String, List<String>>();
        boolean copy = copy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, new MutableInt(0));
        ReferencesHelper.resolveCrossReferences(getSession(), references, false);
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batchCount) throws RepositoryException {
        return copy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount, NodeNamingConflictResolutionStrategy.MERGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(
        JCRNodeWrapper dest,
        String name,
        boolean allowsExternalSharedNodes,
        Map<String, List<String>> references,
        List<String> ignoreNodeTypes,
        int maxBatch,
        MutableInt batchCount,
        NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy
    ) throws RepositoryException {
        return internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount, true, namingConflictResolutionStrategy);
    }

    public boolean internalCopy(JCRNodeWrapper dest, String name, boolean allowsExternalSharedNodes, Map<String, List<String>> references, List<String> ignoreNodeTypes, int maxBatch, MutableInt batchCount, boolean isTopObject) throws RepositoryException {
        return internalCopy(dest, name, allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount, isTopObject, NodeNamingConflictResolutionStrategy.MERGE);
    }

    public boolean internalCopy(
        JCRNodeWrapper dest,
        String name,
        boolean allowsExternalSharedNodes,
        Map<String, List<String>> references,
        List<String> ignoreNodeTypes,
        int maxBatch,
        MutableInt batchCount,
        boolean isTopObject,
        NodeNamingConflictResolutionStrategy namingConflictResolutionStrategy
    ) throws RepositoryException {

        if (isTopObject) {
            getSession().getUuidMapping().put("top-" + getIdentifier(), StringUtils.EMPTY);
        }

        JCRNodeWrapper copy = null;
        try {
            copy = (JCRNodeWrapper) session.getItem(dest.getPath() + "/" + name);
            getSession().checkout(copy);
        } catch (PathNotFoundException ex) {
            // node does not exist
        }

        if (copy != null && !copy.getParent().getDefinition().allowsSameNameSiblings()) {
            if (namingConflictResolutionStrategy == NodeNamingConflictResolutionStrategy.FAIL) {
                throw new ItemExistsException("Same name siblings are not allowed: node " + copy.getPath());
            }
        }

        if (ignoreNodeTypes != null) {
            for (String nodeType : ignoreNodeTypes) {
                if (isNodeType(nodeType)) {
                    return false;
                }
            }
        }

        batchCount.increment();
        if (maxBatch > 0 && batchCount.intValue() > maxBatch) {
            try {
                session.save();
                batchCount.setValue(0);
            } catch (ConstraintViolationException e) {
                // save on the next node when next node is needed (like content node for files)
                batchCount.setValue(maxBatch - 1);
            }
        }

        final Map<String, String> uuidMapping = getSession().getUuidMapping();

        if (copy == null || copy.getDefinition().allowsSameNameSiblings()) {
            if (dest.isVersioned()) {
                session.checkout(dest);
            }
            String typeName = getPrimaryNodeTypeName();
            try {
                copy = dest.addNode(name, typeName);
            } catch (ItemExistsException e) {
                copy = dest.getNode(name);
            } catch (ConstraintViolationException e) {
                logger.error("Cannot copy node", e);
                return false;
            }
        }

        try {
            if (copy.getProvider().isUpdateMixinAvailable()) {
                NodeType[] mixin = objectNode.getMixinNodeTypes();
                for (NodeType aMixin : mixin) {
                    if (!Constants.forbiddenMixinToCopy.contains(aMixin.getName())) {
                        copy.addMixin(aMixin.getName());
                    }
                }
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
                    doCopy(source, copy, source.getName(), allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount, false);
                }
            } else if (!source.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT) && !source.isNodeType(Constants.JAHIANT_REFERENCEINFIELD)) {
                doCopy(source, copy, source.getName(), allowsExternalSharedNodes, references, ignoreNodeTypes, maxBatch, batchCount, false);
            }
        }

        return true;
    }

    @Override
    public void copyProperties(JCRNodeWrapper destinationNode, Map<String, List<String>> references) throws RepositoryException {
        PropertyIterator props = getProperties();

        while (props.hasNext()) {
            Property property = props.nextProperty();
            boolean b = !property.getDefinition().getDeclaringNodeType().isMixin() || destinationNode.getProvider().isUpdateMixinAvailable();
            try {
                if (!Constants.forbiddenPropertiesToCopy.contains(property.getName()) && b) {
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
                logger.debug("Unable to copy property '" + property.getName() + "'. Skipping.", e);
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
    @Override
    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        JCRNodeWrapper parent = null;
        try {
            parent = getParent();
        } catch (ItemNotFoundException e) {
            // do nothing, in some cases parent is not readable because of validity checks (published, languages, workspace, etc .)
        }
        if (parent != null) {
            JCRLockUtils.checkLock(parent, false, true);
        }

        getSession().unregisterNewNode(this);
        if (!this.hasNodes()) {
            getSession().removeFromCache(this);
        } else {
            getSession().flushCaches();
        }
        item.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        session.checkReadOnly("Node lock operation is not permitted for the current session as it is in read-only mode");
        return objectNode.lock(isDeep, isSessionScoped);
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     */
    @Override
    public boolean lockAndStoreToken(String type) throws RepositoryException {
        String l = getSession().isSystem() ? " system " : getSession().getUserID();

        return lockAndStoreToken(type, l);
    }

    /**
     * {@inheritDoc}
     *
     * @param type
     */
    @Override
    public boolean lockAndStoreToken(String type, String userID) throws RepositoryException {
        session.checkReadOnly("Node lock operation is not permitted for the current session as it is in read-only mode");

        if (!isNodeType("jmix:lockable")) {
            return false;
        }
        String token = null;
        String i18nToken = null;

        Node locked = null;
        Node i18nLocked = null;

        if (!objectNode.isLocked()) {
            lockNode(objectNode);
            locked = objectNode;
        } else {
            Property property = objectNode.getProperty("j:locktoken");
            token = property.getString();

            objectNode.getSession().getWorkspace().getLockManager().addLockToken(token);
        }
        try {
            addLockTypeValue(objectNode, userID + ":" + type);

            if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION)) {
                Node trans = null;
                try {
                    trans = getI18N(session.getLocale());
                    if (!trans.isLocked()) {
                        lockNode(trans);
                        i18nLocked = trans;
                    } else {
                        Property property = trans.getProperty("j:locktoken");
                        i18nToken = property.getString();

                        trans.getSession().getWorkspace().getLockManager().addLockToken(i18nToken);
                    }
                    addLockTypeValue(trans, userID + ":" + type);
                } catch (ItemNotFoundException e) {
                }
            }
            objectNode.getSession().save();
        } catch (RepositoryException e) {
            // Clean locks before leaving
            try {
                if (locked != null) {
                    locked.getSession().getWorkspace().getLockManager().unlock(locked.getPath());
                }
            } catch (RepositoryException unlockEx) {
                logger.warn("Error when unlocking unsuccessful lock on node: " + getPath(), unlockEx);
            }
            try {
                if (i18nLocked != null) {
                    i18nLocked.getSession().getWorkspace().getLockManager().unlock(i18nLocked.getPath());
                }
            } catch (RepositoryException unlockEx) {
                logger.warn("Error when unlocking unsuccessful lock on node: " + getPath(), unlockEx);
            }
            if (token != null) {
                objectNode.getSession().getWorkspace().getLockManager().removeLockToken(token);
            }
            if (i18nToken != null) {
                objectNode.getSession().getWorkspace().getLockManager().removeLockToken(i18nToken);
            }
            throw e;
        }
        return true;
    }

    private void lockNode(final Node objectNode) throws RepositoryException {
        getSession().checkout(objectNode);
        Lock lock = objectNode.lock(false, false);
        if (lock.getLockToken() != null) {
            try {
                objectNode.setProperty("j:locktoken", lock.getLockToken());
//                objectNode.getSession().removeLockToken(lock.getLockToken());
            } catch (RepositoryException e) {
                logger.error("Cannot store token for " + getPath(), e);
                objectNode.unlock();
            }
        } else {
            logger.error("Lost lock ! " + localPathInProvider);
        }
    }

    private void addLockTypeValue(final Node objectNode, String l) throws RepositoryException {
        getSession().checkout(objectNode);

        if (objectNode.hasProperty("j:lockTypes")) {
            Property property = objectNode.getProperty("j:lockTypes");
            Value[] oldValues = property.getValues();
            boolean addValue = true;
            for (Value oldValue : oldValues) {
                if (l.equals(oldValue.getString())) {
                    addValue = false;
                    break;
                }
            }
            //Avoid having twice the same lock
            if (addValue) {
                Value[] newValues = new Value[oldValues.length + 1];
                System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                newValues[oldValues.length] = getSession().getValueFactory().createValue(l);
                property.setValue(newValues);
            }
        } else {
            objectNode.setProperty("j:lockTypes", new String[] {l});
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public boolean isLockable() {
        try {
            return objectNode.isNodeType(Constants.MIX_LOCKABLE);
        } catch (RepositoryException e) {
            return false;
        }
    }

    @Override
    public List<Locale> getLockedLocales() throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = objectNode.getNodes(TRANSLATION_NODES_PATTERN);
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (n.isLocked()) {
                r.add(LanguageCodeConverters.languageCodeToLocale(n.getProperty("jcr:language").getString()));
            }
        }
        return r;
    }

    public List<Locale> getLockedLocalesForUserAndType(String type) throws RepositoryException {
        List<Locale> r = new ArrayList<Locale>();
        NodeIterator ni = getI18Ns();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            if (n.isLocked() && n.hasProperty("j:lockTypes")) {
                String l = (getSession().isSystem() ? " system " : getSession().getUserID()) + ":" + type;
                Value[] v = n.getProperty("j:lockTypes").getValues();
                for (Value value : v) {
                    if (value.getString().equals(l)) {
                        r.add(LanguageCodeConverters.getLocaleFromCode(n.getProperty("jcr:language").getString()));
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
    @Override
    public javax.jcr.lock.Lock getLock() {
        try {
            final javax.jcr.lock.Lock lock = objectNode.getLock();
            return new javax.jcr.lock.Lock() {
                @Override
                public String getLockOwner() {
                    return lock.getLockOwner();
                }

                @Override
                public boolean isDeep() {
                    return lock.isDeep();
                }

                @Override
                public long getSecondsRemaining() throws RepositoryException {
                    return lock.getSecondsRemaining();
                }

                @Override
                public boolean isLockOwningSession() {
                    return lock.isLockOwningSession();
                }

                @Override
                public Node getNode() {
                    try {
                        return getProvider().getNodeWrapper(lock.getNode(), getSession());
                    } catch (RepositoryException e) {
                        logger.warn("Can't get wrapper for node holding lock", e);
                        return JCRNodeWrapperImpl.this;
                    }
                }

                @Override
                public String getLockToken() {
                    return lock.getLockToken();
                }

                @Override
                public boolean isLive() throws RepositoryException {
                    return lock.isLive();
                }

                @Override
                public boolean isSessionScoped() {
                    return lock.isSessionScoped();
                }

                @Override
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
    @Override
    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
        session.checkReadOnly("Node unlock operation is not permitted for the current session as it is in read-only mode");
        objectNode.unlock();
    }

    @Override
    public void unlock(String type)
            throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
            InvalidItemStateException, RepositoryException {
        if (getSession().getUser() != null) {
            unlock(type, getSession().getUser().getName());
        } else {
            unlock(type, getSession().getUserID());
        }
    }

    @Override
    public void unlock(String type, String userID)
            throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException,
            InvalidItemStateException, RepositoryException {

        if (!isLocked()) {
            throw new LockException("Node not locked");
        }

        session.checkReadOnly("Node unlock operation is not permitted for the current session as it is in read-only mode");

        if (session.getLocale() != null && !isNodeType(Constants.JAHIANT_TRANSLATION) && hasI18N(session.getLocale(),
                false)) {
            Node trans = getI18N(session.getLocale(), false);
            if (trans.isLocked()) {
                unlock(trans, type, userID);
            }
        }

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !getLockedLocalesForUserAndType(type).isEmpty()) {
            return;
        }

        unlock(objectNode, type, userID);
    }

    private void unlock(final Node objectNode, String type, String userID) throws RepositoryException {
        unlock(objectNode, type, userID, getSession());
    }

    @Override
    public void clearAllLocks() throws RepositoryException {
        session.checkReadOnly("Clear all locks on node operation is not permitted for the current session as it is in read-only mode");

        if (!isNodeType(Constants.JAHIANT_TRANSLATION)) {
            NodeIterator ni = objectNode.getNodes(TRANSLATION_NODES_PATTERN);
            while (ni.hasNext()) {
                Node trans = (Node) ni.next();
                clearAllLocks(trans);
            }
        }

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !getLockedLocales().isEmpty()) {
            return;
        }

        clearAllLocks(objectNode);
    }

    private void clearAllLocks(final Node objectNode) throws RepositoryException {
        getSession().checkout(objectNode);
        if (objectNode.isLocked()) {
            objectNode.unlock();
        }
        if (objectNode.hasProperty("j:locktoken")) {
            objectNode.getProperty("j:locktoken").remove();
            getSession().save();
        }
        if (objectNode.hasProperty("j:lockTypes")) {
            objectNode.getProperty("j:lockTypes").remove();
            getSession().save();
        }
    }

    @Override
    public void checkLock() throws RepositoryException {
        JCRLockUtils.checkLock(this, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean holdsLock() throws RepositoryException {
        return objectNode.holdsLock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLockOwner() throws RepositoryException {
        if (getLock() == null) {
            return null;
        }
        if (!"shared".equals(provider.getAuthenticationType())) {
            List<String> lockOwners = JCRLockUtils.getLockOwners(objectNode);
            if (lockOwners.isEmpty()) {
                return null;
            }
            if (lockOwners.isEmpty()) {
                return StringUtils.EMPTY;
            }
            if (lockOwners.size() == 1) {
                return String.valueOf(lockOwners.get(0)).trim();
            } else {
                StringBuilder owners = new StringBuilder();
                for (String s : lockOwners) {
                    owners.append(s).append(" ");
                }
                return owners.toString().trim();
            }
        } else {
            return getSession().getUserID();
        }
    }

    @Override
    public Map<String, List<String>> getLockInfos() throws RepositoryException {
        Map<String, List<String>> locks = new HashMap<String, List<String>>();
        List<String> lockInfos = JCRLockUtils.getLockInfos(objectNode);
        if (!lockInfos.isEmpty()) {
            locks.put(null, lockInfos);
        }
        NodeIterator ni = getI18Ns();
        while (ni.hasNext()) {
            Node n = ni.nextNode();
            lockInfos = JCRLockUtils.getLockInfos(n);
            if (!lockInfos.isEmpty()) {
                locks.put(n.getProperty("jcr:language").getString(), lockInfos);
            }
        }
        return locks;
    }



    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public boolean isVersioned() {
        try {
            return getProvider().isVersioningAvailable() && objectNode.isNodeType(Constants.MIX_VERSIONABLE);
        } catch (RepositoryException e) {
            logger.error("Error while checking if object node is versioned", e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkpoint() {
        try {
            JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_CHECKPOINT, new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getWorkspace().getVersionManager().checkpoint(localPathInProvider);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error setting checkpoint for node " + getPath(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
                @Override
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
    @Override
    public List<VersionInfo> getVersionInfos() throws RepositoryException {
        return ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(session, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VersionInfo> getLinearVersionInfos() throws RepositoryException {
        return ServicesRegistry.getInstance().getJCRVersionService().getLinearVersionInfos(session, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRStoreProvider getJCRProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRStoreProvider getProvider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void orderBefore(String s, String s1) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException {
        checkLock();
        objectNode.orderBefore(s, s1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRItemWrapper getPrimaryItem() throws ItemNotFoundException, RepositoryException {
        return provider.getItemWrapper(objectNode.getPrimaryItem(), session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() throws RepositoryException {
        return objectNode.getIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getReferences() throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(), getSession(), getProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNode(String s) throws RepositoryException {
        if (!objectNode.hasNode(s)) {
            // no local node found -> check mounted providers
            if (provider.getService() != null && provider.getSessionFactory().areMultipleMountPointsRegistered()) {
                String targetPath = getPath() + '/' + s;
                JCRStoreProvider targetProvider = provider.getSessionFactory().getProvider(targetPath, false);
                if (targetProvider != null && targetProvider != provider) {
                    // we found a provider which can handle the specified path
                    if (targetProvider.getMountPoint().equals(targetPath)) {
                        // it is the provider mount point itself
                        return true;
                    } else {
                        // it is a child node -> check its existence
                        JCRNodeWrapper providerRoot = targetProvider.getNodeWrapper(
                                session.getProviderSession(targetProvider).getNode(
                                        targetProvider.getRelativeRoot().isEmpty() ? "/" : targetProvider
                                                .getRelativeRoot()), "/", this, session);
                        String childPath = StringUtils.substringAfter(targetPath, targetProvider.getMountPoint() + "/");
                        if (childPath.length() > 0) {
                            return providerRoot.hasNode(childPath);
                        }
                    }
                }
            }

            return false;
        }

        if (Constants.LIVE_WORKSPACE.equals(getSession().getWorkspace().getName()) && !s.startsWith("j:translation")) {
            // in live workspace we also check the validity of the node
            final JCRNodeWrapper wrapper;
            try {
                wrapper = (JCRNodeWrapper) getNode(s);
            } catch (RepositoryException e) {
                return false;
            }
            return wrapper.checkValidity();
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNodes() throws RepositoryException {
        if (provider.getService() != null) {
            Map<String, JCRStoreProvider> allMountPoints = provider.getSessionFactory().getMountPoints();
            if (allMountPoints.size() > 1) {
                String pathPrefix = getPath() + "/";
                for (String entry : allMountPoints.keySet()) {
                    if (!entry.equals("/") && entry.startsWith(pathPrefix)
                            && entry.indexOf('/', pathPrefix.length() - 1) == -1) {
                        return true;
                    }
                }
            }
        }

        if (!objectNode.hasNodes()) {
            // underlying node has no children
            return false;
        }

        // if we are in live or session is localized, we need additional checks
        boolean inLive = Constants.LIVE_WORKSPACE.equals(getSession().getWorkspace().getName());
        if (inLive || session.getLocale() != null) {
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                try {
                    Node child = ni.nextNode();
                    String childName = child.getName();
                    if (session.getLocale() != null && childName.startsWith(TRANSLATION_PREFIX)) {
                        // skip j:translation_* nodes in localized session
                        continue;
                    }
                    if (getNode(childName).checkValidity()) {
                        return true;
                    }
                } catch (PathNotFoundException e) {
                    // ignore
                }
            }
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRVersion checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException {
        return (JCRVersion) session.getWorkspace().getVersionManager().checkin(getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        session.checkout(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.doneMerge(localPathInProvider, ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException {
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        versionManager.cancelMerge(localPathInProvider, ((JCRVersion) version).getRealNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final String srcWorkspace) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException {
        JCRObservationManager.doWorkspaceWriteCall(getSession(), JCRObservationManager.NODE_UPDATE, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                objectNode.update(srcWorkspace);
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeIteratorWrapper merge(String s, boolean b) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCorrespondingNodePath(String s) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException {
        String nodePath = objectNode.getCorrespondingNodePath(s);
        if (provider.getMountPoint().equals("/")) {
            return nodePath;
        } else {
            return nodePath.equals("/")?provider.getMountPoint():provider.getMountPoint() + nodePath;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCheckedOut() throws RepositoryException {
        VersionManager versionManager = session.getProviderSession(provider).getWorkspace().getVersionManager();
        boolean co = versionManager.isCheckedOut(localPathInProvider);
        if (co && session.getLocale() != null) {
            try {
                co &= versionManager.isCheckedOut(getI18N(session.getLocale()).getPath());
            } catch (ItemNotFoundException e) {
                logger.debug("isCheckedOut : no i18n node for node {}", localPathInProvider);
            }
        }

        return co;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(s, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(Version version, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException {
        getRealNode().restore(version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restore(Version version, String s, boolean b) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restore(version instanceof JCRVersion ? ((JCRVersion) version).getRealNode() : version, s, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreByLabel(String s, boolean b) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException {
        getRealNode().restoreByLabel(s, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (VersionHistory) getProvider().getNodeWrapper((Node) getRealNode().getVersionHistory(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return (Version) getProvider().getNodeWrapper((Node) getRealNode().getBaseVersion(), (JCRSessionWrapper) getSession());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRFileContent getFileContent() {
        if (fileContent == null) {
            fileContent = new JCRFileContent(this, objectNode);
        }
        return fileContent;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName)  throws RepositoryException {
        return getApplicablePropertyDefinition(propertyName, 0, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedPropertyDefinition getApplicablePropertyDefinition(String propertyName, int requiredPropertyType, boolean isMultiple)
            throws RepositoryException {
        ExtendedPropertyDefinition result = null;
        if (applicablePropertyDefinition.containsKey(propertyName)) {
            result = applicablePropertyDefinition.get(propertyName);
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

        if (isNodeType(Constants.JAHIANT_TRANSLATION) && !propertyName.equals("jcr:language")) {
            result = getParent().getApplicablePropertyDefinition(propertyName);
            if (result != null) {
                applicablePropertyDefinition.put(propertyName, result);
                return result;
            }
        }

        for (ExtendedNodeType type : types) {
            for (ExtendedPropertyDefinition epd : type.getUnstructuredPropertyDefinitions().values()) {
                // check type .. ?
                if ((requiredPropertyType == 0 || epd.getRequiredType() == 0 || epd.getRequiredType() == requiredPropertyType) && isMultiple == epd.isMultiple()) {
                    result = epd;
                    applicablePropertyDefinition.put(propertyName, result);
                    return result;
                }
            }
        }
        applicablePropertyDefinition.put(propertyName, result);
        return result;
    }

    @Override
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

    @Override
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

            @Override
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

            @Override
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

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final JCRNodeWrapper fileNodeWrapper = (JCRNodeWrapper) o;

        return !(getPath() != null ? !getPath().equals(fileNodeWrapper.getPath()) : fileNodeWrapper.getPath() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (getPath() != null ? getPath().hashCode() : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        final Locale locale = getSession().getLocale();
        if (locale != null) {
            return new LazyPropertyIterator(this, locale, strings);
        }
        return new LazyPropertyIterator(this, null, strings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() throws RepositoryException {
        return objectNode.getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getReferences(String name) throws RepositoryException {
        return new PropertyIteratorImpl(objectNode.getReferences(name), getSession(), getProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyIterator getWeakReferences() throws RepositoryException {
        return getWeakReferences(null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    @Override
    public PropertyIterator getWeakReferences(String name) throws RepositoryException {
        // shortcut if node isn't referenceable
        if (!isNodeType(Constants.MIX_REFERENCEABLE)) {
            return new PropertyIteratorImpl(PropertyIteratorAdapter.EMPTY, getSession(), getProvider());
        }

        return getSession().getWeakReferences(this, name);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    @Override
    public void setPrimaryType(String nodeTypeName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCRNodeIteratorWrapper getSharedSet() throws RepositoryException {
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
    @Override
    public void removeSharedSet() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeSharedSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeShare() throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        objectNode.removeShare();
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    @Override
    public void followLifecycleTransition(String transition) throws UnsupportedRepositoryOperationException, InvalidLifecycleTransitionException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAllowedLifecycleTransistions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new String[0];
    }

    @Override
    public JCRNodeWrapper clone(JCRNodeWrapper sharedNode, String name) throws ItemExistsException, VersionException,
            ConstraintViolationException, LockException,
            RepositoryException {
        if (!sharedNode.isNodeType("jmix:shareable")) {
            getSession().checkout(sharedNode);
            sharedNode.addMixin("jmix:shareable");
            sharedNode.getRealNode().getSession().save();

            try {
                final String path = sharedNode.getCorrespondingNodePath(Constants.LIVE_WORKSPACE);
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, new JCRCallback<Object>() {

                    @Override
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

    @Override
    public boolean checkValidity() {
        try {
            if (getPath().startsWith("/sites")) {
                final JCRSessionWrapper jcrSessionWrapper = getSession();
                if (Constants.LIVE_WORKSPACE.equals(jcrSessionWrapper.getWorkspace().getName()) &&
                        !JCRStoreService.getInstance().getNoValidityCheckTypes().contains(getPrimaryNodeTypeName())) {
                    boolean isLocaleDefined = jcrSessionWrapper.getLocale() != null;
                    if (isLocaleDefined) {
                        if (objectNode.hasProperty("j:published") && !objectNode.getProperty("j:published").getBoolean()) {
                            // Node is completely unpublished
                            return false;
                        } else {
                            // if (language is invalid OR (the node does'nt have an i18n subnode published AND the node have an i18n subnode))
                            if (JCRContentUtils.isLanguageInvalid(objectNode, jcrSessionWrapper.getLocale().toString()) ||
                                    (!hasI18N(jcrSessionWrapper.getLocale(), true) && hasI18N(jcrSessionWrapper.getLocale(), true, false))) {
                                return false;
                            }
                        }
                    }
                    boolean result = checkLanguageValidity(null);
                    if (result && isLocaleDefined) {
                        result = VisibilityService.getInstance().matchesConditions(this);
                    }
                    return result;
                }
            }
            if (getProvider().isDefault()) {
                return !objectNode.hasProperty("j:isExternalProviderRoot");
            }
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Node " + getPath() + " is not valid due to an Exception during validity check", e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean checkLanguageValidity(Set<String> languages) {
        final JCRSessionWrapper jcrSessionWrapper = getSession();
        try {
            Locale locale = jcrSessionWrapper.getLocale();
            if (locale != null) {
                JCRSiteNode siteNode = getResolveSite();
                if (siteNode != null) {
                    Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                    List<Locale> locales = jcrSessionWrapper.isLive() ? siteNode.getActiveLiveLanguagesAsLocales() : siteNode.getLanguagesAsLocales();
                    if (locales.size() == 0) {
                        return true;
                    }
                    // BACKLOG-3125 - perhaps getNoLanguageValidityCheckTypes().contains needs to be applied more generally in this method
                    if (!JCRStoreService.getInstance()
                            .getNoLanguageValidityCheckTypes()
                            .contains(getPrimaryNodeTypeName())
                            && !locales.contains(locale) && !site.isAllowsUnlistedLanguages() && hasI18N(locale)) {
                        return false;
                    }
                    for (String mandatoryLanguage : mandatoryLanguages) {
                        if (!checkI18nAndMandatoryPropertiesForLocale(LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage))) {
                            return false;
                        }
                    }
                }
                boolean b = checkI18nAndMandatoryPropertiesForLocale(locale);
                if (!b && siteNode != null && siteNode.isMixLanguagesActive()) {
                    b = checkI18nAndMandatoryPropertiesForLocale(LanguageCodeConverters.getLocaleFromCode(siteNode.getDefaultLanguage()));
                }
                return b;
            } else if (languages != null) {
                for (String language : languages) {
                    if (checkI18nAndMandatoryPropertiesForLocale(LanguageCodeConverters.getLocaleFromCode(language))) {
                        JCRSiteNode siteNode = getResolveSite();
                        if (siteNode != null) {
                            Set<String> mandatoryLanguages = siteNode.getMandatoryLanguages();
                            if (mandatoryLanguages == null || mandatoryLanguages.isEmpty()) {
                                return true;
                            }
                            for (String mandatoryLanguage : mandatoryLanguages) {
                                if (!checkI18nAndMandatoryPropertiesForLocale(LanguageCodeConverters.getLocaleFromCode(mandatoryLanguage))) {
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

    @Override
    public boolean hasTranslations() throws RepositoryException {
        return objectNode.getNodes(TRANSLATION_NODES_PATTERN).hasNext();
    }

    @Override
    public boolean checkI18nAndMandatoryPropertiesForLocale(Locale locale)
            throws RepositoryException {
        Node i18n = null;
        if (hasI18N(locale, false)) {
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


    @Override
    public JCRSiteNode getResolveSite() throws RepositoryException {
        if (site != null) {
            return site;
        }

        try {
            String path = getCanonicalPath();
            path = path.startsWith("/modulesFileSystem/") ? path.replace("/modulesFileSystem/", "/modules/") : path;

            if (path.startsWith("/sites/") || path.startsWith("/modules/")) {
                int index = path.indexOf('/', path.indexOf('/', 1) + 1);
                if (index == -1) {
                    JCRNodeWrapper node = provider.getNodeWrapper(objectNode, session);
                    if (node instanceof JCRSiteNode) {
                        return (site = (JCRSiteNode) node);
                    }
                }
                try {
                    return (site = (JCRSiteNode) (getSession().getNode(index == -1 ? path : path.substring(0, index))));
                } catch (ClassCastException e) {
                    logger.debug("Cannot resolve site for node " + this.getPath(), e);
                    // if node is not a site ( eg ACL / workflow )
                }
            }

            return (site = (JCRSiteNode) (getSession().getNode(JCRContentUtils.getSystemSitePath())));
        } catch (PathNotFoundException e) {
            logger.debug("Cannot resolve site for node " + this.getPath(), e);
        }
        logger.debug("Cannot resolve site for node " + this.getPath());
        return null;
//        return ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
    }

    @Override
    public String getDisplayableName() {
        try {
            if (isNodeType(Constants.JAHIAMIX_RB_TITLE)) {
                String rb = getProperty(Constants.JAHIA_TITLE_KEY).getValue().getString();
                if (rb != null) {
                    return getResourceBundle(rb);
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Failed to get resourceBundled title", e);
        }

        String title = null;
        try {
            title = getProperty(Constants.JCR_TITLE).getValue().getString();
        } catch (RepositoryException e) {
            //Search for primary field if present
            try {
                String itemName = getPrimaryNodeType().getPrimaryItemName();
                if (itemName != null) {
                    String s = getProperty(itemName).getValue().getString();
                    if (s != null && s.length() > 0) {
                        title = s.contains("<") ? new TextExtractor(new Source(s)).toString() : s;
                    }
                }
            } catch (RepositoryException e1) {
                title = null;
            }
        }

        // also return unescaped name if title is empty
        if (title != null && !title.isEmpty()) {
            return (session.getWorkspace().getName().equals(Constants.EDIT_WORKSPACE) && title.contains("##resourceBundle(")) ?
                    interpolateResourceBundle(title) :
                    title;
        } else {
            return getUnescapedName();
        }
    }

    @Override
    public String getUnescapedName() {
        String name = getName();
        return name != null ? JCRContentUtils.unescapeLocalNodeName(name) : null;
    }

    private String interpolateResourceBundle(String title) {
        Locale locale = getSession().getLocale();
        try {
            JCRSiteNode site = getResolveSite();

            for (String module : site.getInstalledModules()) {
                try {
                    return Messages.interpolateResourceBundleMacro(title,
                            locale != null ? locale : session.getFallbackLocale(),
                            ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(
                                    module)
                    );
                } catch (Exception e) {
                    // ignore
                    return title;
                }
            }
        } catch (RepositoryException e) {
            logger.warn("Unable to resolve the site for node {}. Cause: {}", getPath(),
                    e.getMessage());
        }
        return title;
    }

    private String getResourceBundle(String title) {
        Locale locale = getSession().getLocale();
        try {
            JCRSiteNode site = getResolveSite();
            for (String module : site.getInstalledModules()) {
                try {
                    String r = Messages.get(null,
                            ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(module),
                            title, locale != null ? locale : session.getFallbackLocale(), null);
                    if (r != null) {
                        return r;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            return title;
        } catch (RepositoryException e) {
            logger.warn("Unable to resolve the site for node {}. Cause: {}", getPath(),
                    e.getMessage());
        }
        return title;
    }

    public void flushLocalCaches() {
        applicablePropertyDefinition.clear();
        hasPropertyCache.clear();
    }

    @Override
    public boolean canMarkForDeletion() throws RepositoryException {
        JCRStoreProvider provider = getProvider();
        if (!provider.isLockingAvailable() || !provider.isUpdateMixinAvailable()) {
            return false;
        }

        for (String skipType : JCRContentUtils.getInstance().getUnsupportedMarkForDeletionNodeTypes()) {
            if (isNodeType(skipType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isMarkedForDeletion() throws RepositoryException {
        return objectNode.isNodeType(JAHIAMIX_MARKED_FOR_DELETION);
    }

    @Override
    public void markForDeletion(String comment) throws RepositoryException {
        long timer = System.currentTimeMillis();
        if (!canMarkForDeletion()) {
            throw new UnsupportedRepositoryOperationException("Mark for deletion is not supported on this node !");
        }
        checkout();
        if (!objectNode.isNodeType(JAHIAMIX_MARKED_FOR_DELETION)) {
            // no mixin yet, add it
            addMixin(JAHIAMIX_MARKED_FOR_DELETION);
        }
        if (!objectNode.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT)) {
            // no mixin for the root node of the deletion yet, add it
            addMixin(JAHIAMIX_MARKED_FOR_DELETION_ROOT);
        }

        // store deletion info: user, date, comment
        objectNode.setProperty(MARKED_FOR_DELETION_USER, session.getUserID());
        objectNode.setProperty(MARKED_FOR_DELETION_DATE, Calendar.getInstance());
        if (comment != null && comment.length() > 0) {
            objectNode.setProperty(MARKED_FOR_DELETION_MESSAGE, comment);
        }

        // mark all child nodes as deleted
        markNodesForDeletion(this);

        if (session.hasPendingChanges()) {
            objectNode.getSession().save();
        }

        if (hasPermission(Privilege.JCR_LOCK_MANAGEMENT)) {
            lockAndStoreToken(MARKED_FOR_DELETION_LOCK_TYPE, MARKED_FOR_DELETION_LOCK_USER);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("markForDeletion for node {} took {} ms", getPath(),
                    (System.currentTimeMillis() - timer));
        }
    }

    private static void markNodesForDeletion(JCRNodeWrapper node) throws RepositoryException {
        for (NodeIterator iterator = node.getNodes(); iterator.hasNext(); ) {

            JCRNodeWrapper child = (JCRNodeWrapper) iterator.nextNode();

            if (child.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                continue;
            }

            child.getSession().checkout(child);

            if (child.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT)) {
                // if by any chance the child node was already marked for deletion (root), remove the mixin
                try {
                    child.unlock(MARKED_FOR_DELETION_LOCK_TYPE, MARKED_FOR_DELETION_LOCK_USER);
                } catch (LockException ignored) {
                    // ignore
                }
                child.removeMixin(JAHIAMIX_MARKED_FOR_DELETION_ROOT);
            }
            // set mixin
            if (!child.isNodeType(JAHIAMIX_MARKED_FOR_DELETION)) {
                child.addMixin(JAHIAMIX_MARKED_FOR_DELETION);
            }

            if (child.getSession().hasPendingChanges()) {
                child.getSession().save();
            }

            // set lock
            if (child.hasPermission(Privilege.JCR_LOCK_MANAGEMENT)) {
                child.lockAndStoreToken(MARKED_FOR_DELETION_LOCK_TYPE, MARKED_FOR_DELETION_LOCK_USER);
            }

            // recurse into children
            markNodesForDeletion(child);
        }
    }

    @Override
    public void unmarkForDeletion() throws RepositoryException {
        long timer = System.currentTimeMillis();
        if (!canMarkForDeletion()) {
            throw new UnsupportedRepositoryOperationException("Mark for deletion is not supported on this node !");
        }

        checkout();

        // remove lock
        if (isNodeType("jmix:lockable")) {
            try {
                unlock(MARKED_FOR_DELETION_LOCK_TYPE, MARKED_FOR_DELETION_LOCK_USER);
            } catch (LockException ex) {
                logger.warn("Node {} is not locked. Skipping during undelete operation.", getPath());
            }
        }

        if (objectNode.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT)) {
            removeMixin(JAHIAMIX_MARKED_FOR_DELETION_ROOT);
            if (objectNode.isNodeType(JAHIAMIX_MARKED_FOR_DELETION)) {
                removeMixin(JAHIAMIX_MARKED_FOR_DELETION);
            }

            // unmark all child nodes
            unmarkNodesForDeletion(this);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("unmarkForDeletion for node {} took {} ms", getPath(),
                    (System.currentTimeMillis() - timer));
        }
    }

    private static void unmarkNodesForDeletion(JCRNodeWrapper node) throws RepositoryException {
        for (NodeIterator iterator = node.getNodes(); iterator.hasNext(); ) {
            JCRNodeWrapper child = (JCRNodeWrapper) iterator.nextNode();
            if (child.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                continue;
            }
            child.getSession().checkout(child);

            // do unlock
            if (child.isNodeType("jmix:lockable")) {
                try {
                    child.unlock(MARKED_FOR_DELETION_LOCK_TYPE, MARKED_FOR_DELETION_LOCK_USER);
                } catch (LockException ex) {
                    logger.warn("Node {} is not locked. Skipping during undelete operation.",
                            child.getPath());
                }
            }

            // remove mixin
            if (child.isNodeType(JAHIAMIX_MARKED_FOR_DELETION)) {
                child.removeMixin(JAHIAMIX_MARKED_FOR_DELETION);
            }

            // if the child node was before deleted, remove its root mixin
            if (child.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT)) {
                child.removeMixin(JAHIAMIX_MARKED_FOR_DELETION_ROOT);
            }


            // recurse into children
            unmarkNodesForDeletion(child);
        }
    }
}
