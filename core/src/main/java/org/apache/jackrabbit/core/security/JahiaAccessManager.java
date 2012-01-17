/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.security;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.authorization.*;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.PathResolver;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.jackrabbit.spi.commons.namespace.SessionNamespaceResolver;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.usermanager.JahiaGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.jcr.*;
import javax.jcr.security.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.*;

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
    
    private static final Map<String, Map<String, String>> PRIVILEGE_NAMES = new HashMap<String, Map<String,String>>(2);
    
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

    private JahiaUserManagerService userService;
    private JahiaGroupManagerService groupService;
    private JahiaSitesService sitesService;

    protected JahiaPrincipal jahiaPrincipal;

    private Session securitySession;
    private RepositoryContext repositoryContext;
    private WorkspaceConfig workspaceConfig;

    private Map<String, Set<Privilege>> privilegesInRole = new HashMap<String, Set<Privilege>>();
    private Map<String, Boolean> cache = new HashMap<String, Boolean>();
    private Map<String, CompiledAcl> compiledAcls = new HashMap<String, CompiledAcl>();
    private Boolean isAdmin = null;

    private static ThreadLocal<Collection<String>> deniedPathes = new ThreadLocal<Collection<String>>();

    private boolean isAliased = false;
    private Set<String> userMembership;
    private JahiaUser jahiaUser;

    public static String getPrivilegeName(String privilegeName, String workspace) {
        if (workspace ==  null) {
            return privilegeName;
        }
        
        Map<String, String> wsp = PRIVILEGE_NAMES.get(workspace);
        if (wsp == null) {
            wsp = new HashMap<String, String>();
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
    }

    public void init(AMContext amContext) throws AccessDeniedException, Exception {
        init(amContext, null, null, null, null);
    }

    public void init(AMContext amContext, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager) throws AccessDeniedException, Exception {
        init(amContext, null, null, null, null);
    }

    public Session getSecuritySession() throws RepositoryException {
        if (securitySession != null) {
            return securitySession;
        }

        // create subject with SystemPrincipal
        Set<SystemPrincipal> principals = new HashSet<SystemPrincipal>();
        principals.add(new SystemPrincipal());
        Subject systemSubject = new Subject(true, principals, Collections.EMPTY_SET, Collections.EMPTY_SET);

        securitySession = new JahiaSystemSession(repositoryContext, systemSubject,
                workspaceConfig);
        return securitySession;
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
//        super.init(context, acProvider, wspAccessManager);
        subject = context.getSubject();
        resolver = context.getNamePathResolver();
        hierMgr = context.getHierarchyManager();
        workspaceName = context.getWorkspaceName();
        this.repositoryContext = repositoryContext;
        this.workspaceConfig = workspaceConfig;
        privilegeRegistry = new JahiaPrivilegeRegistry(context.getSession().getWorkspace().getNamespaceRegistry());

        Set<JahiaPrincipal> principals = subject.getPrincipals(JahiaPrincipal.class);
        if (!principals.isEmpty()) {
            jahiaPrincipal = (JahiaPrincipal) principals.iterator().next();
        }

        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

        if (!jahiaPrincipal.isSystem()) {
            if (!JahiaLoginModule.GUEST.equals(jahiaPrincipal.getName())) {
                jahiaUser = userService.lookupUser(jahiaPrincipal.getName());
                if (jahiaUser != null) {
                    userMembership = new HashSet<String>(groupService.getUserMembership(jahiaUser));
                }
            }
        } else {
            userMembership = new HashSet<String>();
        }

        initialized = true;
    }

    public void close() throws Exception {
        if (securitySession != null) {
            securitySession.logout();
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
    public Privilege privilegeFromName(String privilegeName) throws AccessControlException, RepositoryException {
        checkInitialized();

        return privilegeRegistry.getPrivilege(privilegeName, workspaceName);
    }

    @Override
    public Privilege[] getSupportedPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        checkInitialized();
        checkValidNodePath(absPath);

        // return all known privileges everywhere.
        return privilegeRegistry.getRegisteredPrivileges();
    }


    /**
     * @see AbstractAccessControlManager#getPrivilegeRegistry()
     */
    @Override
    protected PrivilegeRegistry getPrivilegeRegistry() throws RepositoryException {
        // Do not use jackrabbit privileges registry
        return null;
    }


    /**
     * @see AbstractAccessControlManager#checkValidNodePath(String)
     */
    @Override
    protected void checkValidNodePath(String absPath) throws PathNotFoundException, RepositoryException {
        Path p = resolver.getQPath(absPath);
        if (!p.isAbsolute()) {
            throw new RepositoryException("Absolute path expected.");
        }
        if (hierMgr.resolveNodePath(p) == null) {
            throw new PathNotFoundException("No such node " + absPath);
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
        Set<String> perm = new HashSet<String>();
        if ((actions & READ) == READ) {
            perm.add(getPrivilegeName(Privilege.JCR_READ, workspaceName));
        }
        if ((actions & WRITE) == WRITE) {
            if (id.denotesNode()) {
                // TODO: check again if correct
                perm.add(getPrivilegeName(Privilege.JCR_ADD_CHILD_NODES, workspaceName));
                perm.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
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
        Set<String> privs = new HashSet<String>();

        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
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

        String absPathStr = absPath.toString();
        
        if (permissions.size() == 1 && absPathStr.equals("{}") && permissions.contains(getPrivilegeName(Privilege.JCR_READ,  workspaceName))) {
            return true;
        }

        boolean res = false;

        String cacheKey = absPathStr + " : " + permissions;
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }

        try {
            NamespaceResolver nr = new SessionNamespaceResolver(getSecuritySession());

            PathResolver pr = new DefaultNamePathResolver(nr);
            String jcrPath = pr.getJCRPath(absPath);

            if (deniedPathes.get() != null && deniedPathes.get().contains(jcrPath)) {
                cache.put(cacheKey, false);
                return false;
            }

            if (isSystemPrincipal()) {
                cache.put(cacheKey, true);
                return true;
            }

            Item i = null;
            Boolean itemExists = null;

            // Always deny write access on system folders
            if (permissions.contains(getPrivilegeName(Privilege.JCR_WRITE, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_REMOVE_NODE, workspaceName))) {
                itemExists = getSecuritySession().itemExists(jcrPath);
                if (itemExists.booleanValue()) {
                    i = getSecuritySession().getItem(jcrPath);
                    if (i.isNode()) {
                        if (((Node) i).isNodeType(Constants.JAHIAMIX_SYSTEMNODE)) {
                            cache.put(cacheKey, false);
                            return false;
                        }
                    }
                }
            }

            if (permissions.size() != 1 || !permissions.contains(getPrivilegeName(Privilege.JCR_ADD_CHILD_NODES, workspaceName))) {
                if (itemExists == null) {
                    itemExists = getSecuritySession().itemExists(jcrPath);
                }
                boolean newItem = !itemExists.booleanValue(); // Jackrabbit checks the ADD_NODE permission on non-existing nodes
                if (newItem) {
                    // If node is new (local to the session), always grant permission
                    cache.put(cacheKey, true);
                    return true;
                }
            }

            // Administrators are always granted
            if (jahiaPrincipal != null) {
                if (isAdmin(jahiaPrincipal.getName(), 0)) {
                    cache.put(cacheKey, true);
                    return true;
                }
            }

            int depth = 1;
            if (itemExists == null) {
                itemExists = getSecuritySession().itemExists(jcrPath);
            }
            while (!itemExists.booleanValue()) {
                jcrPath = pr.getJCRPath(absPath.getAncestor(depth++));
                itemExists = getSecuritySession().itemExists(jcrPath);
            }

            if (i == null) {
                i = getSecuritySession().getItem(jcrPath);
            }

            if (i instanceof Version) {
                i = ((Version) i).getContainingHistory();
            }
            if (i instanceof VersionHistory) {
                PropertyIterator pi = ((VersionHistory) i).getReferences();
                if (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    i = p.getParent();
                    jcrPath = i.getPath();
                }
            }

            Node n;

            if (i.isNode()) {
                n = (Node) i;
            } else {
                n = i.getParent();
                jcrPath = StringUtils.substringBeforeLast(jcrPath, "/");
            }

            // Translation permissions
            String name = StringUtils.substringAfterLast(jcrPath, "/");
            if (name.startsWith("j:translation_")) {
                String language = StringUtils.substringAfter(name, "j:translation_");
                if (permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName))) {
                    permissions.remove(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName));
                    permissions.add(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName) + "_" + language);
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

            // Todo : optimize site resolution
//            int siteId = 0;
            String site = null;
            if (jcrPath.startsWith(JahiaSitesBaseService.SITES_JCR_PATH)) {
                if (jcrPath.length() > JahiaSitesBaseService.SITES_JCR_PATH.length() + 1) {
                    site = StringUtils.substringBefore(jcrPath.substring(JahiaSitesBaseService.SITES_JCR_PATH.length() + 1), "/");
                }
            } else {
                Node s = n;
                try {
                    while (!s.isNodeType("jnt:virtualsite")) {
                        s = s.getParent();
                    }
                    site = s.getName();
                    //                siteId = (int) s.getProperty("j:siteId").getLong();
                } catch (ItemNotFoundException e) {
                } catch (PathNotFoundException e) {
                }
            }

//            if (jahiaPrincipal != null) {
//                if (isAdmin(jahiaPrincipal.getName(), siteId)) {
//                    cache.put(absPathStr + " : " + permissions, true);
//                    return true;
//                }
//            }


            res = recurseOnACPs(jcrPath, getSecuritySession(), permissions, site);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        cache.put(absPathStr + " : " + permissions, res);
        return res;
    }

    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
//        Path p = PathFactoryImpl.getInstance().create(parentPath, childName, true);
        // check on parent
        return isGranted(parentPath, permissions);
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


    private boolean recurseOnACPs(String jcrPath, Session s, Set<String> permissions, String site) throws RepositoryException {
        Set<String> foundRoles = new HashSet<String>();
        permissions = new HashSet<String>(permissions);
        while (jcrPath.length() > 0) {
            Map<String, Boolean> roles;

            CompiledAcl acl = compiledAcls.get(jcrPath);

            if (acl == null) {
                acl = new CompiledAcl();
                compiledAcls.put(jcrPath, acl);

                Item i = s.getItem(jcrPath);
                if (i.isNode()) {
                    Node node = (Node) i;
                    if (node.hasNode("j:acl")) {
                        // Jahia specific ACL
                        Node aclNode = node.getNode("j:acl");
                        NodeIterator aces = aclNode.getNodes();

                        while (aces.hasNext()) {
                            Node aceNode = aces.nextNode();
                            String principal = aceNode.getProperty("j:principal").getString();

                            if (matchUser(principal, site)) {
                                CompiledAce ace = new CompiledAce();
                                acl.aces.add(ace);
                                ace.principal = principal;
                                ace.granted = !aceNode.getProperty("j:aceType").getString().equals("DENY");
                                Value[] roleValues = aceNode.getProperty("j:roles").getValues();
                                for (Value role1 : roleValues) {
                                    String role = role1.getString();
                                    ace.roles.add(role);
                                }
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
                    if (matchPermission(permissions, role, s)) {
                        return true;
                    }
                }
            }
            if (acl.broken) {
                return false;
            }

            if ("/".equals(jcrPath)) {
                return false;
            } else if (jcrPath.lastIndexOf('/') > 0) {
                jcrPath = jcrPath.substring(0, jcrPath.lastIndexOf('/'));
            } else {
                jcrPath = "/";
            }
        }
        return false;
    }


    public Set<Privilege> getPermissionsInRole(String role, Session s) throws RepositoryException {
        if (privilegesInRole.containsKey(role)) {
            return privilegesInRole.get(role);
        } else {
            Set<Privilege> list = new HashSet<Privilege>();
            try {
                Node roleNode = securitySession.getNode("/roles/" + role);
                if (roleNode.hasProperty("j:permissions")) {
                    Value[] perms = roleNode.getProperty("j:permissions").getValues();
                    for (Value value : perms) {
                        Node p = s.getNodeByIdentifier(value.getString());
                        Privilege privilege = privilegeRegistry.getPrivilege(p);
                        list.add(privilege);
                    }
                }
                privilegesInRole.put(roleNode.getName(), list);
            } catch (PathNotFoundException e) {
            }
            return list;
        }
    }

    public boolean matchPermission(Set<String> permissions, String role, Session s) throws RepositoryException {
        Set<Privilege> permsInRole = getPermissionsInRole(role, s);
        logger.debug("Checking role " +role);

        for (Privilege privilege : permsInRole) {
            String privilegeName = privilege.getName();
            if (checkPrivilege(permissions, privilegeName)) {
                return true;
            }
            if (isAliased && privilegeName.contains("_" + Constants.LIVE_WORKSPACE)) {
                if (checkPrivilege(permissions, privilegeName.replaceAll("_" + Constants.LIVE_WORKSPACE, "_" + workspaceName))) {
                    return true;
                }
            }

            for (Privilege sub : privilege.getAggregatePrivileges()) {
                if (checkPrivilege(permissions, sub.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPrivilege(Set<String> permissions, String privilegeName) {
        if (permissions.contains(privilegeName)) {
            logger.debug("Found privilege " +privilegeName);
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
            if ((jahiaPrincipal.isGuest() && principalName.equals("guest")) || (principalName.equals(jahiaPrincipal.getName()))) {
                return true;
            }
        } else if (principal.charAt(0) == 'g') {
            if (principalName.equals("guest") || (!jahiaPrincipal.isGuest() &&
                    (isUserMemberOf(principalName, site) || isUserMemberOf(principalName, null)))) {
                return true;
            }
        }
        return false;
    }


    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
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

            Path p = resolver.getQPath(absPath);

            return isGranted(p, privs);
        }
    }

    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        Set<Privilege> results = new HashSet<Privilege>();
        if (isAdmin(jahiaPrincipal.getName(), 0)) {
            return getSupportedPrivileges(absPath);
        }
        Session s = getSecuritySession();
        Set<String> grantedRoles = getRoles(absPath);

        for (String role : grantedRoles) {
            Node node = null;
            try {
                node = securitySession.getNode("/roles/" + role);
            } catch (PathNotFoundException pnfe) {
                logger.warn("Role " + role + " is missing despite still being in use in path "+absPath+ " (or parent). Please re-create it in the administration, remove all uses and then you can delete it !");
                continue;
            }
            if (node.hasProperty("j:permissions")) {
                Value[] perms = node.getProperty("j:permissions").getValues();

                for (Value value : perms) {
                    Node p = s.getNodeByIdentifier(value.getString());
                    try {
                        Privilege privilege = privilegeRegistry.getPrivilege(p);
                        results.add(privilege);
                    } catch (AccessControlException e) {
                        logger.debug("Permission not available : " + p, e);
                    }
                }
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

    public boolean isAdmin(String username, int siteId) {
        if (isAdmin == null) {
            // optimize away guest, we assume he can never be site administrator.
            if (JahiaLoginModule.GUEST.equals(username)) {
                return false;
            }
            JahiaUser user = userService.lookupUser(username);
            if (user != null) {
                return isAdmin = user.isAdminMember(siteId);
            }
            return isAdmin = false;
        }
        return isAdmin;
    }


    private boolean isUserMemberOf(String groupname, String site) {
        JahiaSite s = null;
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupname)) {
            return true;
        }
        if (JahiaGroupManagerService.USERS_GROUPNAME.equals(groupname) && site == null && !JahiaUserManagerService.GUEST_USERNAME.equals(jahiaPrincipal.getName())) {
            return true;
        }
        int siteId = 0;
        if (site != null) {
            try {
                s = sitesService.getSiteByKey(site);
                if (s != null) {
                    siteId = s.getID();
                }
            } catch (JahiaException e) {
                logger.error("Error while retrieving site key" + site, e);
            }
        }

        JahiaGroup group = groupService.lookupGroup(siteId, groupname);
        if (group == null) {
            group = groupService.lookupGroup(0, groupname);
        }
        return (jahiaUser != null) && (group != null) && group.isMember(jahiaUser);
    }

    public Set<String> getRoles(String absPath) throws PathNotFoundException, RepositoryException {
        Set<String> grantedRoles = new HashSet<String>();
        Set<String> foundRoles = new HashSet<String>();
        Session s = getSecuritySession();
        Node n = s.getNode(absPath);

        String site = null;
        Node c = n;
        try {
            while (!c.isNodeType("jnt:virtualsite")) {
                c = c.getParent();
            }
            site = c.getName();
        } catch (ItemNotFoundException e) {
        } catch (PathNotFoundException e) {
        }

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

                                Value[] roles = ace.getProperty(Constants.J_ROLES).getValues();
                                for (Value r : roles) {
                                    String role = r.getString();
                                    if (!foundRoles.contains(principal + ":" + role)) {
                                        if (granted) {
                                            grantedRoles.add(role);
                                        }
                                        foundRoles.add(principal + ":" + role);
                                    }
                                }
                            }
                        }
                    }
                    if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                        return grantedRoles;
                    }
                }
                n = n.getParent();
            }
        } catch (ItemNotFoundException e) {
            logger.debug(e.getMessage(), e);
        }
        return grantedRoles;
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
}
