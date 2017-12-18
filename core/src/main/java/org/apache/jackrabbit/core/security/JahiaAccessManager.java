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
package org.apache.jackrabbit.core.security;

import net.sf.ehcache.Element;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.PrivilegeRegistry;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.PathFactoryImpl;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.jackrabbit.spi.commons.namespace.SessionNamespaceResolver;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.services.render.filter.cache.CacheClusterEvent;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.security.AccessControlException;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.AccessControlPolicy;
import javax.jcr.security.Privilege;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Current ACL policy :
 * <p/>
 * - If there is a grant ACE defined for the user matching the permission, grant access
 * - If there is a deny ACE defined for the user matching the permission, deny access
 * - Go to parent node, repeat
 * - Then, start again from the leaf
 * - If there are at least one grant ACEs defined for groups the user belongs to, grant access
 * - Go to the parent node, repeat
 * - Deny access
 * <p/>
 *
 * @author toto
 */
public class JahiaAccessManager extends AbstractAccessControlManager implements AccessManager, AccessControlManager {
    private static final Logger logger = LoggerFactory.getLogger(JahiaAccessManager.class);

    private static final Map<String, Map<String, String>> PRIVILEGE_NAMES = new ConcurrentHashMap<String, Map<String, String>>(2);

    private static final Subject SYSTEM_SUBJECT = new Subject(true, new HashSet<SystemPrincipal>(
            Arrays.asList(new SystemPrincipal())), Collections.EMPTY_SET, Collections.EMPTY_SET);

    /**
     * Subject whose access rights this AccessManager should reflect
     */
    protected Subject subject;

    /**
     * hierarchy manager used for ACL-based access control model
     */
    protected HierarchyManager hierMgr;
    protected NamePathResolver resolver;
    private JahiaPrivilegeRegistry privilegeRegistry;
    private boolean initialized;
    protected String workspaceName;

    private JahiaGroupManagerService groupService;

    protected JahiaPrincipal jahiaPrincipal;

    private JahiaSystemSession securitySession;
    private Session defaultWorkspaceSecuritySession;

    private RepositoryContext repositoryContext;
    private WorkspaceConfig workspaceConfig;

    private static volatile Cache<String, Set<Privilege>> privilegesInRole = null;
    private static volatile Cache<String, Boolean> matchingPermissions = null;
    private Map<String, Boolean> pathPermissionCache = null;
    private Map<Path, CompiledAcl> compiledAcls = new HashMap<Path, CompiledAcl>();
    private Boolean isAdmin = null;

    private static ThreadLocal<Collection<String>> deniedPathes = new ThreadLocal<Collection<String>>();

    private boolean isAliased = false;
    private DefaultNamePathResolver pr;
    private static final Pattern REFERENCE_FIELD_LANGUAGE_PATTERN = Pattern.compile("(.*)j:referenceInField_.*_([a-z]{2}(_[A-Z]{2})?)_[0-9]+([/].*)?$");
    private static final Pattern TRANSLATION_LANGUAGE_PATTERN = Pattern.compile("(.*)j:translation_([a-z]{2}(_[A-Z]{2})?)([/].*)?$");

    public static String getPrivilegeName(String privilegeName, String workspace) {
        if (workspace == null) {
            return privilegeName;
        }

        Map<String, String> wsp = PRIVILEGE_NAMES.get(workspace);
        if (wsp == null) {
            wsp = new ConcurrentHashMap<String, String>();
            PRIVILEGE_NAMES.put(workspace, wsp);
        }
        String name = wsp.get(privilegeName);
        if (name == null) {
            name = privilegeName + "_" + workspace;
            wsp.put(privilegeName, name);
        }

        return name;
    }

    public static void setDeniedPaths(Collection<String> denied) {
        JahiaAccessManager.deniedPathes.set(denied);
    }

    /**
     * Empty constructor
     */
    public JahiaAccessManager() {
        initialized = false;
        jahiaPrincipal = null;
        if (privilegesInRole == null) {
            CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
            if (cacheService != null) {
                // Jahia is initialized
                try {
                    privilegesInRole = cacheService.getCache("org.jahia.security.privilegesInRolesCache", true);
                } catch (JahiaInitializationException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if (matchingPermissions == null) {
            CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
            if (cacheService != null) {
                // Jahia is initialized
                try {
                    matchingPermissions = cacheService.getCache("org.jahia.security.matchingPermissions", true);
                } catch (JahiaInitializationException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void init(AMContext amContext) throws AccessDeniedException, Exception {
        init(amContext, null, null, null, null);
    }

    public void init(AMContext amContext, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager) throws AccessDeniedException, Exception {
        init(amContext, null, null, null, null);
    }

    public JahiaSystemSession getSecuritySession() throws RepositoryException {
        if (securitySession != null) {
            return securitySession;
        }

        securitySession = new JahiaSystemSession(repositoryContext, SYSTEM_SUBJECT, workspaceConfig);

        return securitySession;
    }

    public Session getDefaultWorkspaceSecuritySession() throws RepositoryException {
        if (workspaceConfig.getName().equals("default")) {
            return getSecuritySession();
        }

        if (defaultWorkspaceSecuritySession != null) {
            return defaultWorkspaceSecuritySession;
        }

        defaultWorkspaceSecuritySession = new JahiaSystemSession(repositoryContext, SYSTEM_SUBJECT,
                repositoryContext.getRepository().getConfig().getWorkspaceConfig("default"));
        return defaultWorkspaceSecuritySession;
    }

    public boolean isSystemPrincipal() {
        return jahiaPrincipal != null && jahiaPrincipal.isSystem();
    }

    /**
     * {@inheritDoc}
     */
    public void init(AMContext context, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager, RepositoryContext repositoryContext, WorkspaceConfig workspaceConfig) throws AccessDeniedException, Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        pathPermissionCache = Collections.synchronizedMap(new LRUMap(SettingsBean.getInstance().getAccessManagerPathPermissionCacheMaxSize()));
        subject = context.getSubject();
        resolver = context.getNamePathResolver();
        hierMgr = context.getHierarchyManager();
        workspaceName = context.getWorkspaceName();
        this.repositoryContext = repositoryContext;
        this.workspaceConfig = workspaceConfig;
        privilegeRegistry = new JahiaPrivilegeRegistry(context.getSession().getWorkspace().getNamespaceRegistry());

        Set<JahiaPrincipal> principals = subject.getPrincipals(JahiaPrincipal.class);
        if (!principals.isEmpty()) {
            jahiaPrincipal = principals.iterator().next();
        }

        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();

        NamespaceResolver nr = new SessionNamespaceResolver(getSecuritySession());

        pr = new DefaultNamePathResolver(nr,true);
        initialized = true;
    }

    public void close() throws Exception {
        if (securitySession != null) {
            securitySession.logout();
        }
        if (defaultWorkspaceSecuritySession != null) {
            defaultWorkspaceSecuritySession.logout();
        }
    }

    /**
     * @deprecated
     */
    public void checkPermission(ItemId id, int actions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
        if (!isGranted(id, actions)) {
            throw new AccessDeniedException("Not sufficient privileges for permissions : " + actions + " on " + id);
        }
    }

    public void checkPermission(Path path, int permissions) throws AccessDeniedException, RepositoryException {
        if (!isGranted(path, permissions)) {
            throw new AccessDeniedException("Not sufficient privileges for permissions : " + permissions + " on " + path + " [" + deniedPathes.get() + "]");
        }
    }

    protected void checkPermission(String absPath, int permission)
            throws AccessDeniedException, PathNotFoundException, RepositoryException {
        checkValidNodePath(absPath);
        checkPermission(resolver.getQPath(absPath), permission);
    }


    public boolean hasPrivileges(String absPath, Set<Principal> principals, Privilege[] privileges)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        checkInitialized();
        checkValidNodePath(absPath);
        checkPermission(absPath, Permission.READ_AC);

        if (privileges == null || privileges.length == 0) {
            // null or empty privilege array -> return true
            if (logger.isDebugEnabled()) {
                logger.debug("No privileges passed -> allowed.");
            }
            return true;
        } else {
            int privs = PrivilegeRegistry.getBits(privileges);
            Path p = resolver.getQPath(absPath);
            return isGranted(p, privs);
        }
    }

    @Override
    protected void checkInitialized() throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
    }


    @Override
    protected PrivilegeManager getPrivilegeManager() throws RepositoryException {
        return new PrivilegeManager() {
            public Privilege[] getRegisteredPrivileges() throws RepositoryException {
                return JahiaPrivilegeRegistry.getRegisteredPrivileges();
            }

            public Privilege getPrivilege(String privilegeName) throws AccessControlException, RepositoryException {
                return privilegeRegistry.getPrivilege(privilegeName, workspaceName);
            }

            public Privilege registerPrivilege(String privilegeName, boolean isAbstract, String[] declaredAggregateNames) throws AccessDeniedException, NamespaceException, RepositoryException {
                return null;
            }
        };
    }

    public void checkRepositoryPermission(int permissions) throws AccessDeniedException, RepositoryException {
        if (!isGranted(PathFactoryImpl.getInstance().getRootPath(), permissions)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    /**
     * @see AbstractAccessControlManager#checkValidNodePath(String)
     */
    @Override
    protected void checkValidNodePath(String absPath) throws PathNotFoundException, RepositoryException {
        checkValidNodePath(resolver.getQPath(absPath));
    }

    protected void checkValidNodePath(Path p) throws RepositoryException {
        if (!p.isAbsolute()) {
            throw new RepositoryException("Absolute path expected.");
        }
        if (hierMgr.resolveNodePath(p) == null) {
            throw new PathNotFoundException("No such node " + p);
        }
    }

    public AccessControlPolicy[] getEffectivePolicies(Set<Principal> principals)
            throws AccessDeniedException, AccessControlException, UnsupportedRepositoryOperationException,
            RepositoryException {
        return new AccessControlPolicy[0];
    }

    public Privilege[] getPrivileges(String absPath, Set<Principal> principals)
            throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new Privilege[0];
    }

    /*
    * @deprecated
    */
    public boolean isGranted(ItemId id, int actions) throws ItemNotFoundException, RepositoryException {
        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
        }
        Set<String> perm = new HashSet<String>();
        if ((actions & READ) == READ) {
            perm.add(getPrivilegeName(Privilege.JCR_READ, workspaceName));
        }
        if ((actions & WRITE) == WRITE) {
            if (id.denotesNode()) {
                perm.add(getPrivilegeName(Privilege.JCR_ADD_CHILD_NODES, workspaceName));
            } else {
                perm.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
            }
        }
        if ((actions & REMOVE) == REMOVE) {
            perm.add((id.denotesNode()) ? getPrivilegeName(Privilege.JCR_REMOVE_CHILD_NODES, workspaceName) : getPrivilegeName(Privilege.JCR_REMOVE_NODE, workspaceName));
        }
        Path path = hierMgr.getPath(id);
        return isGranted(path, perm);
    }

    public boolean isGranted(Path absPath, int permissions) throws RepositoryException {
        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
        }

        Set<String> privs = new HashSet<String>();

        if (permissions == Permission.ADD_NODE || permissions == Permission.SET_PROPERTY || permissions == Permission.REMOVE_PROPERTY) {
            String fullPath=pr.getJCRPath(absPath);
            if (permissions == Permission.ADD_NODE && (fullPath.contains("j:translation_") || fullPath.contains("j:referenceInField_"))) {
                permissions = Permission.SET_PROPERTY;
            } else {
                absPath = absPath.getAncestor(1);
            }
        }

        for (Privilege privilege : privilegeRegistry.getPrivileges(permissions, workspaceName)) {
            privs.add(privilege.getName());
        }

        return isGranted(absPath, privs);
    }

    public boolean isGranted(Path absPath, Set<String> permissions) throws RepositoryException {
        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
        }

        absPath = absPath.getCanonicalPath();
        if (permissions.size() == 1 && absPath.denotesRoot() && permissions.contains(getPrivilegeName(Privilege.JCR_READ, workspaceName))) {
            return true;
        }

        boolean res = false;

        String jcrPath = pr.getJCRPath(absPath);

        String cacheKey = jcrPath + " : " + permissions;

        Boolean result = pathPermissionCacheGet(cacheKey);
        if (result != null) {
            return result;
        }

        try {
            if (deniedPathes.get() != null && deniedPathes.get().contains(jcrPath)) {
                pathPermissionCachePut(cacheKey, false);
                return false;
            }

            if (isSystemPrincipal()) {
                pathPermissionCachePut(cacheKey, true);
                return true;
            }

            Item i = null;
            Boolean itemExists = null;

            // Always deny write access on system folders
            if (permissions.contains(getPrivilegeName(Privilege.JCR_WRITE, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_REMOVE_NODE, workspaceName))) {
                itemExists = getSecuritySession().itemExists(absPath);
                if (itemExists) {
                    i = getSecuritySession().getItem(absPath);
                    if (i.isNode()) {
                        if (((Node) i).isNodeType(Constants.JAHIAMIX_SYSTEMNODE)) {
                            pathPermissionCachePut(cacheKey, false);
                            return false;
                        }
                    }
                }
            }

            // Administrators are always granted
            if (jahiaPrincipal != null) {
                if (isAdmin(null)) {
                    pathPermissionCachePut(cacheKey, true);
                    return true;
                }
            }

            int depth = 1;
            if (itemExists == null) {
                itemExists = getSecuritySession().itemExists(absPath);
            }

            if (!itemExists) {
                pathPermissionCachePut(cacheKey, true);
                return true;
            }

            Path nodePath = absPath;

            while (!itemExists) {
                nodePath = absPath.getAncestor(depth++);
                itemExists = getSecuritySession().itemExists(nodePath);
            }

            if (i == null) {
                i = getSecuritySession().getItem(nodePath);
            }

            if (i instanceof Version) {
                i = ((Version) i).getContainingHistory();
            }
            if (i instanceof VersionHistory) {
                PropertyIterator pi = ((VersionHistory) i).getReferences();
                if (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    i = p.getParent();
                    nodePath = pr.getQPath(i.getPath());
                }
            }

            Node n;

            if (i.isNode()) {
                n = (Node) i;
            } else {
                n = i.getParent();
                nodePath = nodePath.getAncestor(1);
            }

            jcrPath = nodePath != absPath ? pr.getJCRPath(nodePath) : jcrPath;

            // Translation permissions
            if (permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName))) {
                if (jcrPath.contains("j:translation_")) {
                    Matcher matcher = TRANSLATION_LANGUAGE_PATTERN.matcher(jcrPath);
                    if(matcher.matches()) {
                        String language = matcher.group(2);
                        permissions.remove(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
                        permissions.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName) + "_" + language);
                    }
                } else if (jcrPath.contains("j:referenceInField_")) {
                    Matcher matcher = REFERENCE_FIELD_LANGUAGE_PATTERN.matcher(jcrPath);
                    if(matcher.matches()) {
                        String language = matcher.group(2);
                        permissions.remove(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
                        permissions.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName) + "_" + language);
                    }
                }
            }

            // Always allow to add child nodes when it's a translation node and the user have the translate permission

            if(permissions.contains(getPrivilegeName(Privilege.JCR_ADD_CHILD_NODES, workspaceName))) {
                String childNodeName = pr.getJCRName(absPath.getName());
                if((childNodeName.startsWith("j:translation_") || childNodeName.startsWith("j:referenceInField_")) &&
                        hasPrivileges(nodePath, new Privilege[] {privilegeFromName(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName))})){
                    return true;
                }
            }

            String ntName = n.getPrimaryNodeType().getName();
            if (ntName.equals("jnt:acl") || ntName.equals("jnt:ace")) {
                if (permissions.contains(getPrivilegeName(Privilege.JCR_READ, workspaceName))) {
                    permissions.add(getPrivilegeName(Privilege.JCR_READ_ACCESS_CONTROL, workspaceName));
                }
                if (permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName))) {
                    permissions.add(getPrivilegeName(Privilege.JCR_MODIFY_ACCESS_CONTROL, workspaceName));
                }
            }

            String site = resolveSite(jcrPath);

//            if (jahiaPrincipal != null) {
//                if (isAdmin(jahiaPrincipal.getName(), siteId)) {
//                    cache.put(absPathStr + " : " + permissions, true);
//                    return true;
//                }
//            }


            res = recurseOnACPs(nodePath, getSecuritySession(), permissions, site);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        pathPermissionCachePut(cacheKey, res);
        return res;
    }

    private String resolveSite(String jcrPath) {
        String site;
        if (jcrPath.startsWith(JahiaSitesService.SITES_JCR_PATH + "/")) {
            site = StringUtils.substringBefore(jcrPath.substring(JahiaSitesService.SITES_JCR_PATH.length() + 1), "/");
        } else {
            site = JahiaSitesService.SYSTEM_SITE_KEY;
        }
        return site;
    }

    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
        Path p = PathFactoryImpl.getInstance().create(parentPath, childName, true);
        return isGranted(p, permissions);
    }

    public boolean canRead(Path path, ItemId itemId) throws RepositoryException {
        if (path != null) {
            return isGranted(path, Permission.READ);
        } else if (itemId != null) {
            return isGranted(itemId, JahiaAccessManager.READ);
        }
        return false;
    }

    /**
     * @see AccessManager#canAccess(String)
     */
    public boolean canAccess(String workspaceName) throws RepositoryException {
        return true;
    }


    private boolean recurseOnACPs(Path nodePath, JahiaSystemSession s, Set<String> permissions, String site) throws RepositoryException {
        Set<String> foundRoles = new HashSet<String>();
        permissions = new HashSet<String>(permissions);
        while (nodePath.getLength() > 0) {
            CompiledAcl acl = compiledAcls.get(nodePath);

            if (acl == null) {
                acl = new CompiledAcl();
                compiledAcls.put(nodePath, acl);

                Item i = s.getItem(nodePath);
                if (i.isNode()) {
                    Node node = (Node) i;
                    if (node.hasNode("j:acl")) {
                        // Jahia specific ACL
                        Node aclNode = node.getNode("j:acl");
                        NodeIterator aces = aclNode.getNodes();

                        while (aces.hasNext()) {
                            Node aceNode = aces.nextNode();
                            try {
                                String principal = aceNode.getProperty("j:principal").getString();

                                if (matchUser(principal, site)) {
                                    CompiledAce ace = new CompiledAce();
                                    acl.aces.add(ace);
                                    ace.principal = principal;
                                    ace.granted = !aceNode.getProperty("j:aceType").getString().equals("DENY");
                                    if (aceNode.isNodeType("jnt:externalAce")) {
                                        Value[] roleValues = aceNode.getProperty("j:roles").getValues();
                                        for (Value role1 : roleValues) {
                                            String role = role1.getString();
                                            ace.roles.add(role + "/" + aceNode.getProperty("j:externalPermissionsName").getString());
                                        }
                                    } else {
                                        Value[] roleValues = aceNode.getProperty("j:roles").getValues();
                                        for (Value role1 : roleValues) {
                                            String role = role1.getString();
                                            ace.roles.add(role);
                                        }
                                    }
                                }
                            } catch (RepositoryException e) {
                                logger.error("Can't read ACE "+aceNode.getPath(),e);
                            }
                        }
                        acl.broken = aclNode.hasProperty("j:inherit") && !aclNode.getProperty("j:inherit").getBoolean();
                    }
                }
            }

            for (CompiledAce perm : acl.aces) {
                for (String role : perm.roles) {
                    String key = perm.principal + ":" + role;
                    if (foundRoles.contains(key)) {
                        continue;
                    }
                    foundRoles.add(key);

                    if (!perm.granted) {
                        continue;
                    }
                    if (matchPermission(permissions, role)) {
                        return true;
                    }
                }
            }
            if (acl.broken) {
                return false;
            }

            if (nodePath.denotesRoot()) {
                return false;
            } else {
                nodePath = nodePath.getAncestor(1);
            }
        }
        return false;
    }

    public Set<Privilege> getPermissionsInRole(String role) throws RepositoryException {
        if (privilegesInRole != null) {
            Set<Privilege> permsInRole = privilegesInRole.get(role);
            if (permsInRole == null) {
                permsInRole = internalGetPermissionsInRole(role);
                privilegesInRole.put(role, permsInRole);
            }
            return permsInRole;
        } else {
            return internalGetPermissionsInRole(role);
        }
    }

    private Set<Privilege> internalGetPermissionsInRole(String role) throws RepositoryException {
        Set<Privilege> privileges;
        String externalPermission = null;

        String roleName = role;
        if (roleName.contains("/")) {
            externalPermission = StringUtils.substringAfter(role, "/");
            roleName = StringUtils.substringBefore(role, "/");
        }

        Node roleNode = findRoleNode(roleName);
        if (roleNode != null) {
            privileges = getPrivileges(roleNode, externalPermission);
        } else {
            privileges = Collections.emptySet();
        }
        return privileges;
    }

    private Node findRoleNode(String role) throws RepositoryException {
        try {
            NodeIterator nodes = getDefaultWorkspaceSecuritySession().getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(role) + "' and isdescendantnode(r,['/roles'])",
                            Query.JCR_SQL2).execute().getNodes();
            if (nodes.hasNext()) {
                return nodes.nextNode();
            }
        } catch (PathNotFoundException e) {
        }
        return null;
    }

    private Set<Privilege> getPrivileges(Node roleNode, String externalPermission) throws RepositoryException {
        Set<Privilege> privileges = new HashSet<Privilege>();

        Node roleParent = roleNode.getParent();
        if (roleParent.isNodeType(Constants.JAHIANT_ROLE)) {
            privileges = getPrivileges(roleParent, externalPermission);
        }
        if (externalPermission != null) {
            if (roleNode.hasNode(externalPermission)) {
                roleNode = roleNode.getNode(externalPermission);
            } else {
                return privileges;
            }
        }
        Session s = roleNode.getSession();
        if (roleNode.hasProperty("j:permissionNames")) {
            Value[] perms = roleNode.getProperty("j:permissionNames").getValues();
            for (Value value : perms) {
                try {
                    try {
                        Privilege privilege = privilegeRegistry.getPrivilege(value.getString(), null);
                        privileges.add(privilege);
                    } catch (AccessControlException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Permission not available : " + value.getString(), e);
                        }
                    }
                } catch (RepositoryException e) {

                } catch (IllegalStateException e) {

                }
            }
        } else if (roleNode.hasProperty("j:permissions")) {
            Value[] perms = roleNode.getProperty("j:permissions").getValues();
            for (Value value : perms) {
                try {
                    Node p = s.getNodeByIdentifier(value.getString());
                    try {
                        Privilege privilege = privilegeRegistry.getPrivilege(p);
                        privileges.add(privilege);
                    } catch (AccessControlException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Permission not available : " + p, e);
                        }
                    }
                } catch (RepositoryException e) {

                } catch (IllegalStateException e) {

                }
            }
        }
        return privileges;
    }


    public boolean matchPermission(Set<String> permissions, String role) throws RepositoryException {
        int permissionsSize = permissions.size();
        StringBuilder stringBuilder = new StringBuilder(role);
        for (String permission : permissions) {
            stringBuilder.append(permission);
        }
        String entryKey = stringBuilder.toString();
        Boolean cachedValue = matchingPermissionsGet(entryKey);
        if (cachedValue == null) {
            Set<Privilege> permsInRole = getPermissionsInRole(role);
            if (logger.isDebugEnabled()) {
                logger.debug("Checking role {}", role);
            }

            for (Privilege privilege : permsInRole) {
                String privilegeName = privilege.getName();
                if (checkPrivilege(permissions, privilegeName, entryKey)) {
                    return true;
                }

                for (Privilege sub : privilege.getAggregatePrivileges()) {
                    if (checkPrivilege(permissions, sub.getName(), entryKey)) {
                        return true;
                    }
                }
            }
            if (permissionsSize == permissions.size()) {
                // Do not cache if permissions set is modified
                matchingPermissionsPut(entryKey, Boolean.FALSE);
            }
            return false;
        } else {
            if (cachedValue) {
                permissions.clear();
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean checkPrivilege(Set<String> permissions, String name, String cacheEntryKey) {
        if (!isAliased || !name.contains("_" + Constants.EDIT_WORKSPACE)) {
            if (checkPrivilege(permissions, name)) {
                matchingPermissionsPut(cacheEntryKey, Boolean.TRUE);
                return true;
            }
        }
        if (isAliased && name.contains("_" + Constants.LIVE_WORKSPACE)) {
            if (checkPrivilege(permissions, name.replaceAll("_" + Constants.LIVE_WORKSPACE,
                    "_" + workspaceName))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPrivilege(Set<String> permissions, String privilegeName) {
        if (permissions.contains(privilegeName)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Found privilege {}", privilegeName);
            }
            permissions.remove(privilegeName);
            if (permissions.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean matchUser(String principal, String site) {
        final String principalName = principal.substring(2);
        if (principal.charAt(0) == 'u') {
            if ((jahiaPrincipal.isGuest() && principalName.equals("guest")) ||
                    (principalName.equals(jahiaPrincipal.getName()) && (jahiaPrincipal.getRealm() == null || jahiaPrincipal.getRealm().equals(site)))) {
                return true;
            }
        } else if (principal.charAt(0) == 'g') {
            if (isUserMemberOf(principalName, site)) {
                return true;
            }
        }
        return false;
    }


    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        return hasPrivileges(resolver.getQPath(absPath), privileges);
    }

    public boolean hasPrivileges(Path absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        checkInitialized();
        checkValidNodePath(absPath);
        if (privileges == null || privileges.length == 0) {
            // null or empty privilege array -> return true
            if (logger.isDebugEnabled()) {
                logger.debug("No privileges passed -> allowed.");
            }
            return true;
        } else {
            Set<String> privs = new HashSet<String>();

            for (Privilege privilege : privileges) {
                privs.add(privilege.getName());
            }

            return isGranted(absPath, privs);
        }
    }

    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        if (isAdmin(null)) {
            return getSupportedPrivileges(absPath);
        }

        Set<String> grantedRoles = getRoles(absPath);

        Set<Privilege> results = new HashSet<Privilege>();

        for (String role : grantedRoles) {
            Set<Privilege> permissionsInRole = getPermissionsInRole(role);
            if (!permissionsInRole.isEmpty()) {
                results.addAll(permissionsInRole);
            } else {
                logger.debug("No permissions found for role '{}' on path '{}' (or parent)", role, absPath);
            }
        }

        return results.toArray(new Privilege[results.size()]);
    }

    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new AccessControlPolicy[0];
    }

    public void setAliased(boolean aliased) {
        isAliased = aliased;
    }

    public boolean isAdmin(String siteKey) {
        if (isAdmin == null) {
            // optimize away guest, we assume he can never be site administrator.
            if (JahiaLoginModule.GUEST.equals(jahiaPrincipal.getName())) {
                return false;
            }
            return isAdmin = groupService.isAdminMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), siteKey);
        }
        return isAdmin;
    }


    private boolean isUserMemberOf(String groupname, String site) {
        return  (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupname)) ||
                (!jahiaPrincipal.isGuest() && JahiaGroupManagerService.USERS_GROUPNAME.equals(groupname)) ||
                (!jahiaPrincipal.isGuest() && JahiaGroupManagerService.SITE_USERS_GROUPNAME.equals(groupname) && (jahiaPrincipal.getRealm() == null || jahiaPrincipal.getRealm().equals(site))) ||
                (!jahiaPrincipal.isGuest() && (groupService.isMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), groupname, site) || groupService.isMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), groupname, null)));
    }

    public Set<String> getRoles(String absPath) throws PathNotFoundException, RepositoryException {
        Set<String> grantedRoles = new HashSet<String>();
        Set<String> foundRoles = new HashSet<String>();
        Session s = getDefaultWorkspaceSecuritySession();
        Node n = s.getNode(absPath);

        String site = resolveSite(n.getPath());

        try {
            while (true) {
                if (n.hasNode("j:acl")) {
                    Node acl = n.getNode("j:acl");
                    NodeIterator aces = acl.getNodes();
                    while (aces.hasNext()) {
                        Node ace = aces.nextNode();
                        if (ace.isNodeType("jnt:ace")) {
                            String principal = ace.getProperty("j:principal").getString();

                            if (matchUser(principal, site)) {
                                boolean granted = ace.getProperty("j:aceType").getString().equals("GRANT");

                                String roleSuffix = "";
                                if (ace.isNodeType("jnt:externalAce")) {
                                    roleSuffix = "/" + ace.getProperty("j:externalPermissionsName").getString();
                                }

                                Value[] roles = ace.getProperty(Constants.J_ROLES).getValues();
                                for (Value r : roles) {
                                    String role = r.getString();
                                    if (!foundRoles.contains(principal + ":" + role + roleSuffix)) {
                                        if (granted) {
                                            grantedRoles.add(role + roleSuffix);
                                        }
                                        foundRoles.add(principal + ":" + role + roleSuffix);
                                    }
                                }
                            }
                        }
                    }
                    if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                        return grantedRoles;
                    }
                }
                if (n.getPath().equals("/")) {
                    break;
                }
                n = n.getParent();
            }
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }
        return grantedRoles;
    }
    

    private Boolean matchingPermissionsGet(String key) {
        return isAliased ? null : matchingPermissions.get(key);
    }

    private void matchingPermissionsPut(String key, Boolean value) {
        if (!isAliased) {
            matchingPermissions.put(key, value);
        }
    }
    
    private Boolean pathPermissionCacheGet(String key) {
        return isAliased ? null : pathPermissionCache.get(key);
    }

    private void pathPermissionCachePut(String key, Boolean value) {
        if (!isAliased) {
            pathPermissionCache.put(key, value);
        }
        if (logger.isDebugEnabled() && pathPermissionCache.size() == SettingsBean.getInstance().getAccessManagerPathPermissionCacheMaxSize()) {
            logger.debug("ACL cache size exceeded, putting new key : "+key);
        }
    }

    class CompiledAcl {
        boolean broken = false;
        Set<CompiledAce> aces = new HashSet<CompiledAce>();
    }

    class CompiledAce {
        String principal;
        Set<String> roles = new HashSet<String>();
        boolean granted;
    }

    /**
     * Flush the cache of privileges set by role
     */
    public static void flushPrivilegesInRoles() {
        if (privilegesInRole != null) {
            privilegesInRole.flush();
        }
        if (matchingPermissions != null) {
            matchingPermissions.flush();
            if (SettingsBean.getInstance().isClusterActivated()) {
                // Matching Permissions cache is not a selfPopulating Replicated cache so we need to send a command
                // to flush it across the cluster
                net.sf.ehcache.Cache htmlCacheEventSync = getHtmlCacheEventSync();
                if (htmlCacheEventSync != null) {
                    htmlCacheEventSync.put(new Element("FLUSH_MATCHINGPERMISSIONS-" + UUID.randomUUID(),
                            // Create an empty CacheClusterEvent to be executed after next Journal sync
                            new CacheClusterEvent("", getClusterRevision())));
                }
            }
        }
    }

    private static net.sf.ehcache.Cache getHtmlCacheEventSync() {
        net.sf.ehcache.Cache htmlCacheEventSync = null;
        try {
            htmlCacheEventSync = ModuleCacheProvider.getInstance().getSyncCache();
        } catch (Exception e) {
            // not initialized yet
        }

        return htmlCacheEventSync;
    }

    private static long getClusterRevision() {
        return SpringJackrabbitRepository.getInstance().getClusterRevision();
    }

    public static void flushMatchingPermissions() {
        if (matchingPermissions != null) {
            matchingPermissions.flush();
        }
    }
}
