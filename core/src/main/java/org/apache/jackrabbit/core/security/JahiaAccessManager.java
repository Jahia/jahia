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

package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.RepositoryContext;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.security.authorization.*;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.conversion.DefaultNamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.conversion.PathResolver;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.jackrabbit.spi.commons.namespace.SessionNamespaceResolver;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import sun.print.PeekGraphics;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.security.*;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.naming.NamingException;
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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 28 févr. 2006
 * Time: 17:58:41
 * To change this template use File | Settings | File Templates.
 */
public class JahiaAccessManager extends AbstractAccessControlManager implements AccessManager, AccessControlManager {
    public static final Logger logger = Logger.getLogger(JahiaAccessManager.class);

    /**
     * Subject whose access rights this AccessManager should reflect
     */
    protected Subject subject;

    private static Repository repository;

    /**
     * hierarchy manager used for ACL-based access control model
     */
    protected HierarchyManager hierMgr;
    protected NamePathResolver resolver;
    private WorkspaceAccessManager wspAccessMgr;
    private AccessControlProvider acProviderMgr;
    private PrivilegeRegistry privilegeRegistry;
    private boolean initialized;
    protected String workspaceName;

    private JahiaUserManagerService userService;
    private JahiaGroupManagerService groupService;
    private JahiaSitesService sitesService;

    protected JahiaPrincipal jahiaPrincipal;

    private static Map<String, Integer> permissions;

    private Session session;
    private Session securitySession;
    private RepositoryContext repositoryContext;
    private WorkspaceConfig workspaceConfig;

    private Map<String, Boolean> cache = new HashMap<String, Boolean>();

    private static ThreadLocal<Collection<String>> deniedPathes = new ThreadLocal<Collection<String>>();

    private boolean isAliased = false;

    public static void setDeniedPaths(Collection<String> denied) {
        JahiaAccessManager.deniedPathes.set(denied);
    }

    static {
        permissions = new HashMap<String, Integer>();
        permissions.put(Constants.JCR_READ_RIGHTS, Permission.READ);
        permissions.put("jcr:setProperties", Permission.SET_PROPERTY);
        permissions.put("jcr:addChildNodes", Permission.ADD_NODE);
        permissions.put("jcr:removeChildNodes", Permission.REMOVE_NODE);
        permissions.put(Constants.JCR_WRITE_RIGHTS, Permission.SET_PROPERTY + Permission.REMOVE_PROPERTY + Permission.ADD_NODE + Permission.REMOVE_NODE + Permission.NODE_TYPE_MNGMT + Permission.LOCK_MNGMT + Permission.VERSION_MNGMT);
        permissions.put("jcr:getAccessControlPolicy", Permission.READ);
        permissions.put("jcr:setAccessControlPolicy", Permission.SET_PROPERTY + Permission.REMOVE_PROPERTY);
        permissions.put("jcr:all", Permission.ALL);
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
        if (jahiaPrincipal != null) {
            return jahiaPrincipal.isSystem();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void init(AMContext context, AccessControlProvider acProvider, WorkspaceAccessManager wspAccessManager, RepositoryContext repositoryContext, WorkspaceConfig workspaceConfig) throws AccessDeniedException, Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }
//        super.init(context, acProvider, wspAccessManager);
        session = context.getSession();
        subject = context.getSubject();
        resolver = context.getNamePathResolver();
        hierMgr = context.getHierarchyManager();
        workspaceName = context.getWorkspaceName();
        this.repositoryContext = repositoryContext;
        this.workspaceConfig = workspaceConfig;
        privilegeRegistry = new PrivilegeRegistry(resolver);

        Set principals = subject.getPrincipals(JahiaPrincipal.class);
        if (!principals.isEmpty()) {
            jahiaPrincipal = (JahiaPrincipal) principals.iterator().next();
        }

        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        sitesService = ServicesRegistry.getInstance().getJahiaSitesService();

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
            throw new AccessDeniedException("Not sufficient privileges for permissions : " + permissions + " on " + id);
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
            logger.debug("No privileges passed -> allowed.");
            return true;
        } else {
            int privs = PrivilegeRegistry.getBits(privileges);
            Path p = resolver.getQPath(absPath);
            return isGranted(p, privs);
        }
    }

    @Override protected void checkInitialized() throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
    }

    /**
     * @see AbstractAccessControlManager#getPrivilegeRegistry()
     */
    @Override
    protected PrivilegeRegistry getPrivilegeRegistry() throws RepositoryException {
        checkInitialized();
        return privilegeRegistry;
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
        int perm = 0;
        if ((actions & READ) == READ) {
            perm |= Permission.READ;
        }
        if ((actions & WRITE) == WRITE) {
            if (id.denotesNode()) {
                // TODO: check again if correct
                perm |= Permission.SET_PROPERTY;
                perm |= Permission.ADD_NODE;
            } else {
                perm |= Permission.SET_PROPERTY;
            }
        }
        if ((actions & REMOVE) == REMOVE) {
            perm |= (id.denotesNode()) ? Permission.REMOVE_NODE : Permission.REMOVE_PROPERTY;
        }
        Path path = hierMgr.getPath(id);
        return isGranted(path, perm);
    }

    public boolean isGranted(Path absPath, int permissions) throws RepositoryException {

        String absPathStr = absPath.toString();
        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
        }

        if (permissions == Permission.READ && absPathStr.equals("{}")) {
            return true;
        }

        if (cache.containsKey(absPathStr + " : " + permissions)) {
            return cache.get(absPathStr + " : " + permissions);
        }

        try {
            NamespaceResolver nr = new SessionNamespaceResolver(getSecuritySession());

            PathResolver pr = new DefaultNamePathResolver(nr);
            String jcrPath = pr.getJCRPath(absPath);

            if (deniedPathes.get() != null && deniedPathes.get().contains(jcrPath)) {
                cache.put(absPathStr + " : " + permissions, false);
                return false;
            }

            if (isSystemPrincipal()) {
                cache.put(absPathStr + " : " + permissions, true);
                return true;
            }

            // Always deny write access on system folders
            if (getSecuritySession().itemExists(jcrPath)) {
                Item i = getSecuritySession().getItem(jcrPath);
                if (i.isNode() && permissions != Permission.READ) {
                    String ntName = ((Node) i).getPrimaryNodeType().getName();
                    if (ntName.equals(Constants.JAHIANT_SYSTEMFOLDER) || ntName.equals("rep:root")) {
                        cache.put(absPathStr + " : " + permissions, false);
                        return false;
                    }
                }
            }
            
            boolean newItem = !getSecuritySession().itemExists(jcrPath); // Jackrabbit checks the ADD_NODE permission on non-existing nodes

            if ((Permission.SET_PROPERTY | permissions) == Permission.SET_PROPERTY ||
                    ((Permission.ADD_NODE | permissions) == Permission.ADD_NODE && newItem) ||
                    (Permission.REMOVE_NODE | permissions) == Permission.REMOVE_NODE ||
                    (Permission.REMOVE_PROPERTY | permissions) == Permission.REMOVE_PROPERTY) {
                Item i;
                if (getSecuritySession().itemExists(jcrPath)) {
                    i = getSecuritySession().getItem(jcrPath);
                } else {
                    i = session.getItem(jcrPath);
                }

                if (!i.isNode()) {
                    i = i.getParent();
                }

                final NodeDefinition definition = ((Node) i).getDefinition();

                try {
                    final String primaryTypeName = ((Node) i).getPrimaryNodeType().getName();
                    if (!primaryTypeName.equals("jnt:translation")) {
                        boolean live = NodeTypeRegistry.getInstance().getNodeType(primaryTypeName).isLiveContent();
                        ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(definition.getDeclaringNodeType().getName());
                        ExtendedNodeDefinition extendedDefinition;
                        if (definition.getName().equals("*")) {
                            StringBuffer s = new StringBuffer("");
                            for (String s1 : definition.getRequiredPrimaryTypeNames()) {
                                s.append(s1).append(" ");
                            }
                            extendedDefinition = t.getUnstructuredChildNodeDefinitions().get(s.toString().trim());
                        } else {
                            extendedDefinition = t.getChildNodeDefinitionsAsMap().get(definition.getName());
                        }
                        if (extendedDefinition != null) {
//                            System.out.println("---> "+jcrPath+ " - "+permissions + "  - isLiveContent1 = "+extendedDefinition.isLiveContent()+ "  - isLiveContent2 = "+ t.isLiveContent());
                            live |= extendedDefinition.isLiveContent();
                        }
                        if (live != Constants.LIVE_WORKSPACE.equals(workspaceName)) {
                            return false;
                        }
                    }
                } catch (NoSuchNodeTypeException e) {
                    // ..
                }
            }
           
            // Administrators are always granted
            if (jahiaPrincipal != null) {
                if (isAdmin(jahiaPrincipal.getName(),0)) {
                    cache.put(absPathStr + " : " + permissions, true);
                    return true;
                }
            }

            int depth = 1;
            while (!getSecuritySession().itemExists(jcrPath)) {
                jcrPath = pr.getJCRPath(absPath.getAncestor(depth++));
            }

            Item i = getSecuritySession().getItem(jcrPath);

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

            int siteId = 0;

            String site = null;

            try {
                while (!i.isNode() || !((Node) i).isNodeType("jnt:virtualsite")) {
                    i = i.getParent();
                }
                site = i.getName();
                siteId = (int) ((Node)i).getProperty("j:siteId").getLong();
            } catch (ItemNotFoundException e) {
            }

            if (jahiaPrincipal != null) {
                if (isAdmin(jahiaPrincipal.getName(), siteId)) {
                    cache.put(absPathStr + " : " + permissions, true);
                    return true;
                }
            }

            return recurseonACPs(jcrPath, getSecuritySession(), permissions, site);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cache.put(absPathStr + " : " + permissions, false);
        return false;
    }

    public boolean isGranted(Path parentPath, Name childName, int permissions) throws RepositoryException {
//        Path p = PathFactoryImpl.getInstance().create(parentPath, childName, true);
        // check on parent
        return isGranted(parentPath, permissions);
    }

    public boolean canRead(Path path, ItemId itemId) throws RepositoryException {
        if (itemId != null) {
            return isGranted(itemId, JahiaAccessManager.READ);
        } else if (path != null) {
            return isGranted(path, Permission.READ);
        }
        return false;
    }

    public boolean canRead(Path path) throws RepositoryException {
        return isGranted(path, Permission.READ);
    }

    /**
     * @see AccessManager#canAccess(String)
     */
    public boolean canAccess(String workspaceName) throws RepositoryException {
        return true;
    }


    private Path getPath(ItemId id) throws RepositoryException {
        Path path = null;
        try {
            // Get the path of the node
            path = hierMgr.getPath(id);
        } catch (ItemNotFoundException e) {
            // This might be a property, get the path of the parent node
            if (!id.denotesNode()) {
                id = ((PropertyId) id).getParentId();
                try {
                    path = hierMgr.getPath(id);
                } catch (ItemNotFoundException e1) {
                }
            }
        }
        return path;
    }

    private boolean recurseonACPs(String jcrPath, Session s, int permissions, String site) throws RepositoryException {
        boolean result = false;
        Set<String> groups = new HashSet<String>();
        while (jcrPath.length() > 0) {
            if (s.itemExists(jcrPath)) {
                Item i = s.getItem(jcrPath);
                if (i.isNode()) {
                    Node node = (Node) i;
                    if (node.isNodeType("mix:accessControlled")) {
                        // Old JCR-2 specifications
                        Node acp = node.getProperty("jcr:accessControlPolicy").getNode();
                        NodeIterator aces = acp.getNode("jcr:acl").getNodes("jcr:ace");

                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            String principal = ace.getProperty("jcr:principal").getString();
                            String type = ace.getProperty("jcr:aceType").getString();
                            Value[] privileges = ace.getProperty("jcr:privileges").getValues();
                            for (int j = 0; j < privileges.length; j++) {
                                Value privilege = privileges[j];
                                if (match(permissions, privilege.getString())) {
                                    String principalName = principal.substring(2);
                                    if (principal.charAt(0) == 'u') {
                                        if (principalName.equals(jahiaPrincipal.getName())) {
                                            return type.equals("GRANT");
                                        }
                                    } else if (principal.charAt(0) == 'g') {
                                        if (principalName.equals("guest") || !jahiaPrincipal.isGuest() && isUserMemberOf(jahiaPrincipal.getName(), principalName, site)) {
                                            if (!groups.contains(principalName)) {
                                                result |= type.equals("GRANT");
                                                groups.add(principalName);
                                            }
                                        }
                                    } else if (principal.charAt(0) == 'r') {
                                        boolean b = hasRole(principalName, jahiaPrincipal, site);
                                        if (b) {
                                            if (!groups.contains(principalName)) {
                                                result |= type.equals("GRANT");
                                                groups.add(principalName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (node.hasNode("j:acl")) {
                        // Jahia specific ACL
                        Node acl = node.getNode("j:acl");
                        NodeIterator aces = acl.getNodes();

                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            String principal = ace.getProperty("j:principal").getString();
                            String type = ace.getProperty("j:aceType").getString();
                            Value[] privileges = ace.getProperty("j:privileges").getValues();
                            for (int j = 0; j < privileges.length; j++) {
                                Value privilege = privileges[j];
                                if (match(permissions, privilege.getString())) {
                                    final String principalName = principal.substring(2);
                                    if (principal.charAt(0) == 'u') {
                                        if (principalName.equals(jahiaPrincipal.getName())) {
                                            return type.equals("GRANT");
                                        }
                                    } else if (principal.charAt(0) == 'g') {
                                        if (principalName.equals("guest") || !jahiaPrincipal.isGuest() && isUserMemberOf(jahiaPrincipal.getName(), principalName, site)) {
                                            if (!groups.contains(principalName)) {
                                                result |= type.equals("GRANT");
                                                groups.add(principalName);
                                            }
                                        }
                                    } else if (principal.charAt(0) == 'r') {
                                        boolean b = hasRole(principalName, jahiaPrincipal, site);
                                        if (b) {
                                            if (!groups.contains(principalName)) {
                                                result |= type.equals("GRANT");
                                                groups.add(principalName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                            return result;
                        }
                    }
                }
                if ("/".equals(jcrPath)) {
                    return result;
                } else if (jcrPath.lastIndexOf('/') > 0) {
                    jcrPath = jcrPath.substring(0, jcrPath.lastIndexOf('/'));
                } else {
                    jcrPath = "/";
                }
            }
        }
        return result;
    }


    public boolean match(int permission, String privilege) {
        String workspace = "default";
        if (privilege.contains("_")) {
            workspace = privilege.substring(privilege.lastIndexOf('_') + 1);
            privilege = privilege.substring(0, privilege.lastIndexOf('_'));
        }
        if (!isAliased && !workspace.equals(workspaceName)) {
            return false;
        }
        Integer foundPermission = permissions.get(privilege);
        return foundPermission != null && (foundPermission & permission) != 0;
    }

    // todo : implements methods ..

    public boolean hasPrivileges(String absPath, Privilege[] privileges) throws PathNotFoundException, RepositoryException {
        checkInitialized();
        checkValidNodePath(absPath);
        if (privileges == null || privileges.length == 0) {
            // null or empty privilege array -> return true
            logger.debug("No privileges passed -> allowed.");
            return true;
        } else {
            int privs = PrivilegeRegistry.getBits(privileges);
            Path p = resolver.getQPath(absPath);
            return isGranted(p, privs);
        }
    }

    public Privilege[] getPrivileges(String absPath) throws PathNotFoundException, RepositoryException {
        return new Privilege[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new AccessControlPolicy[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static synchronized Repository getRepository() throws NamingException {
        return repository;
    }

    public static void setRepository(Repository arepository) {
        repository = arepository;
    }

    public void setAliased(boolean aliased) {
        isAliased = aliased;
    }

    public boolean isAdmin(String username, int siteId) {
        JahiaUser user = userService.lookupUser(username);
        if (user != null) {
            return user.isAdminMember(siteId);
        }
        return false;
    }


    private boolean isUserMemberOf(String username, String groupname, String site) {
        JahiaSite s = null;
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupname)) {
            return true;
        }
        if (JahiaGroupManagerService.USERS_GROUPNAME.equals(groupname) && site == null && !JahiaUserManagerService.GUEST_USERNAME.equals(username)) {
            return true;
        }
        int siteId = 0;
        try {
            s = sitesService.getSiteByKey(site);
            if (s != null) {
                siteId = s.getID();
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        JahiaUser user = userService.lookupUser(username);
        JahiaGroup group = groupService.lookupGroup(siteId, groupname);
        return (user != null) && (group != null) && group.isMember(user);
    }

    private boolean hasRole(String roleName, JahiaPrincipal principal, String site) {
        long timer = System.currentTimeMillis();
        Boolean cachedResult = null;
        String siteCachKey = site != null ? site : "_server_";
        Map<String, Boolean> roles = principal.getRoleCache().get(siteCachKey);
        if (roles != null) {
            cachedResult = roles.get(roleName);
        }
        if (cachedResult != null) {
            return cachedResult;
        }

        org.jahia.services.usermanager.JahiaPrincipal user = userService.lookupUser(principal.getName());
        JahiaSite s = null;
        if (user == null) {
            int siteId = 0;
            try {
                s = sitesService.getSiteByKey(site);
                if (s != null) {
                    siteId = s.getID();
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            user = groupService.lookupGroup(siteId, principal.getName());
        }
        boolean hasRole = user != null && user.hasRole(new RoleIdentity(roleName, site));
        if (roles == null) {
            roles = new HashMap<String, Boolean>();
            principal.getRoleCache().put(siteCachKey, roles);
        }
        roles.put(roleName, Boolean.valueOf(hasRole));

        logger.debug("Checking role " + roleName + " for " + principal.getName() + " in site " + site + " took " + (System.currentTimeMillis() - timer) + " ms");
        return hasRole;
    }


}
