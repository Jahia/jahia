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

import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 28 févr. 2006
 * Time: 17:58:41
 * 
 */
public class JahiaAccessManager extends AbstractAccessControlManager implements AccessManager, AccessControlManager {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaAccessManager.class);

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

    private Map<String, Boolean> cache = new HashMap<String, Boolean>();

    private static ThreadLocal<Collection<String>> deniedPathes = new ThreadLocal<Collection<String>>();

    private boolean isAliased = false;

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
        subject = context.getSubject();
        resolver = context.getNamePathResolver();
        hierMgr = context.getHierarchyManager();
        workspaceName = context.getWorkspaceName();
        this.repositoryContext = repositoryContext;
        this.workspaceConfig = workspaceConfig;
        privilegeRegistry = new JahiaPrivilegeRegistry(context.getSession().getWorkspace().getNamespaceRegistry());

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
            perm.add(Privilege.JCR_READ + "_" + workspaceName);
        }
        if ((actions & WRITE) == WRITE) {
            if (id.denotesNode()) {
                // TODO: check again if correct
                perm.add(Privilege.JCR_ADD_CHILD_NODES+"_"+workspaceName);
                perm.add(Privilege.JCR_MODIFY_PROPERTIES+"_"+workspaceName);
            } else {
                perm.add(Privilege.JCR_MODIFY_PROPERTIES+"_"+workspaceName);
            }
        }
        if ((actions & REMOVE) == REMOVE) {
            perm.add( (id.denotesNode()) ? Privilege.JCR_REMOVE_CHILD_NODES+"_"+workspaceName : Privilege.JCR_REMOVE_NODE+"_"+workspaceName);
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
        String absPathStr = absPath.toString();
        if (isSystemPrincipal() && deniedPathes.get() == null) {
            return true;
        }

        if (permissions.equals(Collections.singleton(Privilege.JCR_READ + "_" +workspaceName)) && absPathStr.equals("{}")) {
            return true;
        }

        boolean res = false;

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
                if (i.isNode() && permissions.contains(Collections.singleton(Privilege.JCR_WRITE + "_" + workspaceName))) {
                    String ntName = ((Node) i).getPrimaryNodeType().getName();
                    if (ntName.equals(Constants.JAHIANT_SYSTEMFOLDER) || ntName.equals("rep:root")) {
                        cache.put(absPathStr + " : " + permissions, false);
                        return false;
                    }
                }
            }

            boolean newItem = !getSecuritySession().itemExists(jcrPath); // Jackrabbit checks the ADD_NODE permission on non-existing nodes
            if (newItem && !permissions.equals(Collections.singleton(Privilege.JCR_ADD_CHILD_NODES + "_" + workspaceName))) {
                // If node is new (local to the session), always grant permission
                cache.put(absPathStr + " : " + permissions, true);
                return true;
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

            Node n = i.isNode() ? (Node) i : i.getParent();

            // Translation permissions
            if (n.getName().startsWith("j:translation_")) {
                String language = StringUtils.substringAfter(n.getName(), "j:translation_");
                if (permissions.contains(Privilege.JCR_MODIFY_PROPERTIES + "_" + workspaceName)) {
                    permissions.remove(Privilege.JCR_MODIFY_PROPERTIES + "_" + workspaceName);
                    permissions.remove(Privilege.JCR_MODIFY_PROPERTIES + "_" + workspaceName + "_" + language);
                }
            }

            int siteId = 0;
            String site = null; boolean isTemplate = false;
            Node s = n;
            try {
                while (!s.isNodeType("jnt:virtualsite")) {
                    if (s.isNodeType("jnt:templatesFolder")) {
                        isTemplate = true;
                    }
                    s = s.getParent();
                }
                site = s.getName();
                siteId = (int) s.getProperty("j:siteId").getLong();
            } catch (ItemNotFoundException e) {
            } catch (PathNotFoundException e) {
            }

            if (jahiaPrincipal != null) {
                if (isAdmin(jahiaPrincipal.getName(), siteId)) {
                    cache.put(absPathStr + " : " + permissions, true);
                    return true;
                }
            }


            res = recurseonACPs(jcrPath, getSecuritySession(), permissions, site);

//            // Staging read access on virtualsite node is granted if live read access is also granted
//            if (!res && (n.isNodeType("jnt:virtualsite") || isTemplate) && permissions.equals(Collections.singleton(Privilege.JCR_READ + "_" + workspaceName))) {
//                permissions.clear();
//                permissions.add(Privilege.JCR_READ + "_live");
//                res = recurseonACPs(jcrPath, getSecuritySession(), permissions, site);
//            }

        } catch (Exception e) {
            e.printStackTrace();
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

    private boolean recurseonACPs(String jcrPath, Session s, Set<String> permissions, String site) throws RepositoryException {
        Set<String> foundRoles = new HashSet<String>();
        while (jcrPath.length() > 0) {
            if (s.itemExists(jcrPath)) {
                Item i = s.getItem(jcrPath);
                if (i.isNode()) {
                    Node node = (Node) i;
                    if (node.hasNode("j:acl")) {
                        // Jahia specific ACL
                        Node acl = node.getNode("j:acl");
                        NodeIterator aces = acl.getNodes();

                        while (aces.hasNext()) {
                            Node ace = aces.nextNode();
                            String principal = ace.getProperty("j:principal").getString();
                            String type = ace.getProperty("j:aceType").getString();
                            Value[] roles = ace.getProperty("j:roles").getValues();
                            for (int j = 0; j < roles.length; j++) {
                                String role = roles[j].getString();
                                if (matchUser(principal, site)) {
                                    if (foundRoles.contains(role)) {
                                        continue;
                                    }

                                    foundRoles.add(role);

                                    if (type.equals("DENY")) {
                                        continue;
                                    }
                                    if (matchPermission(permissions, role, s)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                            return false;
                        }
                    }
                }
                if ("/".equals(jcrPath)) {
                    return false;
                } else if (jcrPath.lastIndexOf('/') > 0) {
                    jcrPath = jcrPath.substring(0, jcrPath.lastIndexOf('/'));
                } else {
                    jcrPath = "/";
                }
            }
        }
        return false;
    }


    public boolean matchPermission(Set<String> permissions, String role, Session s) throws RepositoryException {
        Node node = s.getNode("/roles/" + role);
        if (node.hasProperty("j:permissions")) {
            Value[] perms = node.getProperty("j:permissions").getValues();

            permissions = new HashSet<String>(permissions);

            for (Value value : perms) {
                Node p = s.getNodeByIdentifier(value.getString());
                Privilege privilege = privilegeRegistry.getPrivilege(p);

                if (permissions.contains(privilege.getName())) {
                    permissions.remove(privilege.getName());
                    if (permissions.isEmpty()) {
                        return true;
                    }
                }

                for (Privilege sub : privilege.getAggregatePrivileges()) {
                    if (permissions.contains(sub.getName())) {
                        permissions.remove(sub.getName());
                        if (permissions.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean matchUser(String principal, String site) {
        final String principalName = principal.substring(2);
        if (principal.charAt(0) == 'u') {
            if ((jahiaPrincipal.isGuest() || (!principalName.equals("guest")) && principalName.equals(jahiaPrincipal.getName()))) {
                return true;
            }
        } else if (principal.charAt(0) == 'g') {
            if (principalName.equals("guest") || (!jahiaPrincipal.isGuest() &&
                    (isUserMemberOf(jahiaPrincipal.getName(), principalName, site) || isUserMemberOf(jahiaPrincipal.getName(), principalName, null)))) {
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
            logger.debug("No privileges passed -> allowed.");
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
        try {
            if (isAdmin(jahiaPrincipal.getName(),0)) {
                return getSupportedPrivileges(absPath);
            }

            Set<String> grantedRoles = new HashSet<String>();
            Set<String> deniedRoles = new HashSet<String>();
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
                                        if (!grantedRoles.contains(role) && !deniedRoles.contains(role)) {
                                            if (granted) {
                                                grantedRoles.add(role);
                                            } else {
                                                deniedRoles.add(role);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                            return results.toArray(new Privilege[results.size()]);
                        }
                    }
                    n = n.getParent();
                }
            } catch (ItemNotFoundException e) {
                logger.debug(e.getMessage(), e);
            }

            for (String role : grantedRoles) {
                Node node = s.getNode("/roles/" + role);
                if (node.hasProperty("j:permissions")) {
                    Value[] perms = node.getProperty("j:permissions").getValues();

                    for (Value value : perms) {
                        Node p = s.getNodeByIdentifier(value.getString());
                        Privilege privilege = privilegeRegistry.getPrivilege(p);
                        results.add(privilege);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return results.toArray(new Privilege[results.size()]);
    }

    public AccessControlPolicy[] getEffectivePolicies(String absPath) throws PathNotFoundException, AccessDeniedException, RepositoryException {
        return new AccessControlPolicy[0];
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


}
