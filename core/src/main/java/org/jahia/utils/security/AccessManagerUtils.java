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
package org.jahia.utils.security;

import net.sf.ehcache.Element;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.jaas.JahiaPrincipal;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
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
import javax.jcr.security.Privilege;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AccessManagerUtils provide functions to work with acls/ace and roles
 * - test if a jahiaPrincipal match permissions for a path
 * - get roles necessary for a given path and a given jahiaPrincipal
 * - get permissions for a given role
 * @author kevan
 */
public class AccessManagerUtils {
    private static final Logger logger = LoggerFactory.getLogger(AccessManagerUtils.class);

    private static final Map<String, Map<String, String>> PRIVILEGE_NAMES = new ConcurrentHashMap<String, Map<String, String>>(2);

    private static volatile Cache<String, Set<Privilege>> privilegesInRole = null;
    private static volatile Cache<String, Boolean> matchingPermissions = null;
    public static ThreadLocal<Collection<String>> deniedPathes = new ThreadLocal<Collection<String>>();

    private static final Pattern REFERENCE_FIELD_LANGUAGE_PATTERN = Pattern.compile("(.*)j:referenceInField_.*_([a-z]{2}(_[A-Z]{2})?)_[0-9]+([/].*)?$");
    private static final Pattern TRANSLATION_LANGUAGE_PATTERN = Pattern.compile("(.*)j:translation_([a-z]{2}(_[A-Z]{2})?)([/].*)?$");

    /**
     * Acl node representation to be store in cache
     */
    public static class CompiledAcl {
        boolean broken = false;
        Set<CompiledAce> aces = new HashSet<CompiledAce>();
    }

    /**
     * Ace node representation to be store in cache
     */
    public static class CompiledAce {
        String principal;
        Set<String> roles = new HashSet<String>();
        boolean granted;
    }

    /**
     * Init privilegesInRole and matchingPermissions static cache
     */
    public static void initCaches() {
        if(privilegesInRole == null) {
            privilegesInRole = initCache("org.jahia.security.privilegesInRolesCache");
        }
        if(matchingPermissions == null) {
            matchingPermissions = initCache("org.jahia.security.matchingPermissions");
        }
    }

    /**
     * Flush privilegesInRole cache
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

    /**
     * Flush matchingPermissions cache
     */
    public static void flushMatchingPermissions() {
        if (matchingPermissions != null) {
            matchingPermissions.flush();
        }
    }

    /**
     * Get the full privilege name combine privilege name with workspace to generate the permission name used in jahia
     * exemple: privilege: Privilege.JCR_REMOVE_NODE and workspace: Constants.EDIT_WORKSPACE
     * result: "{http://www.jcp.org/jcr/1.0}removeNode_default"
     * @param privilegeName privilege name, like Privilege.JCR_REMOVE_NODE
     * @param workspace workspace name, like Constants.EDIT_WORKSPACE
     * @return the privilege name
     */
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

    /**
     * add pathes to list of always denied pathes
     * @param denied list of path
     */
    public static void setDeniedPaths(Collection<String> denied) {
        AccessManagerUtils.deniedPathes.set(denied);
    }

    /**
     * check if the principal is system
     * @param jahiaPrincipal jahiaPrincipal to test
     * @return true if system
     */
    public static boolean isSystemPrincipal(JahiaPrincipal jahiaPrincipal) {
        return jahiaPrincipal != null && jahiaPrincipal.isSystem();
    }

    /**
     * Entry point to test if the given jahiaPrincipal match the given permissions on a node
     * @param pathWrapper the path to the node
     * @param permissions the permissions ask for check
     * @param securitySession the session used to read the j:acl nodes, it should be a system session to be sure that nodes are readable in any case,
     *                        the workspace of the session is important because acls under nodes can be different depending on the workspace.
     *                        Normally the workspace of this session should be the same ot the workspace where you want to do the check of permissions.
     * @param jahiaPrincipal the jahiaPrincipal to test
     * @param workspaceName the workspace to check (used to construct the privilege names)
     * @param isAliased if the current user is aliased
     * @param pathPermissionCache Map used as a cache in memory to store the result of this function, to avoid recalculate everything if check is ask with similar parameters after
     * @param compiledAcls Map used as a cache in memory to store the j:acl result for a given node, to avoid read jcr again to retrieve the acls in next calls
     * @param privilegeRegistry Jahia Privilege registry, used to read Privilege or retrieve them using names.
     * @return true if the jahiaPrincipal match the permissions for the given path, if not return false
     * @throws RepositoryException
     */
    public static boolean isGranted(PathWrapper pathWrapper, Set<String> permissions, Session securitySession,
                                    JahiaPrincipal jahiaPrincipal, String workspaceName,
                                    boolean isAliased, Map<String, Boolean> pathPermissionCache,
                                    Map<Object, CompiledAcl> compiledAcls, JahiaPrivilegeRegistry privilegeRegistry) throws RepositoryException {

        if (isSystemPrincipal(jahiaPrincipal) && deniedPathes.get() == null) {
            return true;
        }

        if (permissions.size() == 1 && pathWrapper.isRoot() && permissions.contains(getPrivilegeName(Privilege.JCR_READ, workspaceName))) {
            return true;
        }

        boolean res = false;

        String jcrPath = pathWrapper.getPathStr();

        String cacheKey = jcrPath + " : " + permissions;

        Boolean result = pathPermissionCacheGet(cacheKey, isAliased, pathPermissionCache);
        if (result != null) {
            return result;
        }

        try {
            if (deniedPathes.get() != null && deniedPathes.get().contains(jcrPath)) {
                pathPermissionCachePut(cacheKey, false, isAliased, pathPermissionCache);
                return false;
            }

            if (isSystemPrincipal(jahiaPrincipal)) {
                pathPermissionCachePut(cacheKey, true, isAliased, pathPermissionCache);
                return true;
            }

            Item i = null;
            Boolean itemExists = null;

            // Always deny write access on system folders
            if (permissions.contains(getPrivilegeName(Privilege.JCR_WRITE, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName)) ||
                    permissions.contains(getPrivilegeName(Privilege.JCR_REMOVE_NODE, workspaceName))) {
                itemExists = pathWrapper.itemExist();
                if (itemExists) {
                    i = pathWrapper.getItem();
                    if (i.isNode()) {
                        if (((Node) i).isNodeType(Constants.JAHIAMIX_SYSTEMNODE)) {
                            pathPermissionCachePut(cacheKey, false, isAliased, pathPermissionCache);
                            return false;
                        }
                    }
                }
            }

            // Administrators are always granted
            if (jahiaPrincipal != null) {
                if (isAdmin(null, jahiaPrincipal)) {
                    pathPermissionCachePut(cacheKey, true, isAliased, pathPermissionCache);
                    return true;
                }
            }

            if (itemExists == null) {
                itemExists = pathWrapper.itemExist();
            }

            if (!itemExists) {
                pathPermissionCachePut(cacheKey, true, isAliased, pathPermissionCache);
                return true;
            }

            if (i == null) {
                i = pathWrapper.getItem();
            }

            if (i instanceof Version) {
                i = ((Version) i).getContainingHistory();
            }

            PathWrapper nodePathWrapper = pathWrapper;
            if (i instanceof VersionHistory) {
                PropertyIterator pi = ((VersionHistory) i).getReferences();
                if (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    i = p.getParent();
                    nodePathWrapper = pathWrapper.getNewPathWrapper(i.getPath());
                }
            }

            Node n;

            if (i.isNode()) {
                n = (Node) i;
            } else {
                n = i.getParent();
                nodePathWrapper = nodePathWrapper.getAncestor();
            }

            jcrPath = !nodePathWrapper.equals(pathWrapper) ? nodePathWrapper.getPathStr() : jcrPath;

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
                String nodeName = pathWrapper.getNodeName();
                if((nodeName.startsWith("j:translation_") || nodeName.startsWith("j:referenceInField_")) &&
                        isGranted(nodePathWrapper, Collections.singleton(getPrivilegeName(Privilege.JCR_MODIFY_PROPERTIES, workspaceName)),
                                securitySession, jahiaPrincipal, workspaceName, isAliased,
                                pathPermissionCache, compiledAcls, privilegeRegistry)){
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


            res = recurseOnACPs(nodePathWrapper, securitySession, permissions, site, compiledAcls, jahiaPrincipal, isAliased, privilegeRegistry, workspaceName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        pathPermissionCachePut(cacheKey, res, isAliased, pathPermissionCache);
        return res;
    }

    /**
     * Retrieve permissions of a given role
     * The privilegesInRole cache will be used to read the permissions, if not found we used a system session in default workspace to read the role node
     * @param role Role name
     * @param privilegeRegistry JahiaPrivilegeRegistry
     * @return return Set of Privilege objects
     * @throws RepositoryException
     */
    public static Set<Privilege> getPermissionsInRole(String role, JahiaPrivilegeRegistry privilegeRegistry) throws RepositoryException {
        Set<Privilege> permsInRole = null;
        if(privilegesInRole == null) {
            privilegesInRole = initCache("org.jahia.security.privilegesInRolesCache");
        } else {
            permsInRole = privilegesInRole.get(role);
        }

        if (permsInRole == null) {
            permsInRole = internalGetPermissionsInRole(role, privilegeRegistry);
            privilegesInRole.put(role, permsInRole);
        }
        return permsInRole;
    }

    /**
     * Test if a given role contains the list of permissions
     * @param permissions list of permissions to test
     * @param role the role
     * @param isAliased if the current user is aliased
     * @param privilegeRegistry the JahiaPrivilegedRegistry
     * @param workspaceName the workspace
     * @return true if the role contain all the permissions
     * @throws RepositoryException
     */
    public static boolean matchPermission(Set<String> permissions, String role, boolean isAliased, JahiaPrivilegeRegistry privilegeRegistry,
                                          String workspaceName) throws RepositoryException {
        int permissionsSize = permissions.size();
        StringBuilder stringBuilder = new StringBuilder(role);
        for (String permission : permissions) {
            stringBuilder.append(permission);
        }
        String entryKey = stringBuilder.toString();
        Boolean cachedValue = matchingPermissionsGet(entryKey, isAliased);
        if (cachedValue == null) {
            Set<Privilege> permsInRole = getPermissionsInRole(role, privilegeRegistry);
            if (logger.isDebugEnabled()) {
                logger.debug("Checking role {}", role);
            }

            for (Privilege privilege : permsInRole) {
                String privilegeName = privilege.getName();
                if (checkPrivilege(permissions, privilegeName, entryKey, isAliased, workspaceName)) {
                    return true;
                }

                for (Privilege sub : privilege.getAggregatePrivileges()) {
                    if (checkPrivilege(permissions, sub.getName(), entryKey, isAliased, workspaceName)) {
                        return true;
                    }
                }
            }
            if (permissionsSize == permissions.size()) {
                // Do not cache if permissions set is modified
                matchingPermissionsPut(entryKey, Boolean.FALSE, isAliased);
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

    /**
     * Get the list Privilege from granted roles for a given principal on a node, recursive check on parents nodes
     * when the acl node have the "inherit" flag, the getRoles(...) function is used to retrieve the roles.
     * @param absPath the path to the node
     * @param workspace the workspace
     * @param jahiaPrincipal the principal
     * @param privilegeRegistry the JahiaPrivilegeRegistry
     * @return the list of privileges from the granted roles for the user on the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static Privilege[] getPrivileges(String absPath, String workspace, JahiaPrincipal jahiaPrincipal, JahiaPrivilegeRegistry privilegeRegistry) throws PathNotFoundException, RepositoryException {
        Node node = JCRSessionFactory.getInstance().getCurrentSystemSession(workspace, null, null).getNode(absPath);
        return getPrivileges(node, jahiaPrincipal, privilegeRegistry);
    }

    /**
     * Get the list Privilege from granted roles for a given principal on a node, recursive check on parents nodes
     * when the acl node have the "inherit" flag, the getRoles(...) function is used to retrieve the roles.
     * @param node the node
     * @param jahiaPrincipal the principal
     * @param privilegeRegistry the JahiaPrivilegeRegistry
     * @return the list of privileges from the granted roles for the user on the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static Privilege[] getPrivileges(Node node, JahiaPrincipal jahiaPrincipal, JahiaPrivilegeRegistry privilegeRegistry) throws PathNotFoundException, RepositoryException {

        Set<String> grantedRoles = getRoles(node, jahiaPrincipal);
        Set<Privilege> results = new HashSet<Privilege>();

        for (String role : grantedRoles) {
            Set<Privilege> permissionsInRole = getPermissionsInRole(role, privilegeRegistry);
            if (!permissionsInRole.isEmpty()) {
                results.addAll(permissionsInRole);
            } else {
                logger.debug("No permissions found for role '{}' on path '{}' (or parent)", role, node.getPath());
            }
        }

        return results.toArray(new Privilege[results.size()]);
    }

    /**
     * Test if the given JahiaPrincipal is administrator, store the result in the jahiaPrincipal so we don't query the groupService in next calls
     * @param siteKey if set, the test will check if the user is site administrator
     * @param jahiaPrincipal the principal
     * @return true if the user is administrator
     */
    public static boolean isAdmin(String siteKey, JahiaPrincipal jahiaPrincipal) {
        if(jahiaPrincipal.getAdmin() == null) {
            // optimize away guest, we assume he can never be site administrator.
            jahiaPrincipal.setAdmin(!JahiaLoginModule.GUEST.equals(jahiaPrincipal.getName()) &&
                    ServicesRegistry.getInstance().getJahiaGroupManagerService().isAdminMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), siteKey));
        }

        return jahiaPrincipal.getAdmin();
    }

    /**
     * Get the list of granted role for a given principal on a node, recursive check on parents when the acl node have the "inherit" flag
     * @param absPath the path of the node
     * @param workspace the workspace
     * @param jahiaPrincipal the jahiaPrincipal
     * @return the list of granted roles for the user on the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static Set<String> getRoles(String absPath, String workspace, JahiaPrincipal jahiaPrincipal) throws PathNotFoundException, RepositoryException {
        Node node = JCRSessionFactory.getInstance().getCurrentSystemSession(workspace, null, null).getNode(absPath);
        return getRoles(node, jahiaPrincipal);
    }

    /**
     * Get the list of granted role for a given principal on a node, recursive check on parents when the acl node have the "inherit" flag
     * @param node the node, the session of the should allow to read under j:acl and ace nodes
     * @param jahiaPrincipal the jahiaPrincipal
     * @return the list of granted roles for the user on the node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public static Set<String> getRoles(Node node, JahiaPrincipal jahiaPrincipal) throws PathNotFoundException, RepositoryException {
        String site = resolveSite(node.getPath());

        if (node instanceof JCRNodeWrapper) {
             return getOptimizedRoles((JCRNodeWrapper) node, jahiaPrincipal, site);
        }

        Set<String> grantedRoles = new HashSet<String>();
        Set<String> foundRoles = new HashSet<String>();

        try {
            while (true) {
                if (node.hasNode("j:acl")) {
                    Node acl = node.getNode("j:acl");
                    NodeIterator aces = acl.getNodes();
                    while (aces.hasNext()) {
                        Node ace = aces.nextNode();
                        if (ace.isNodeType("jnt:ace")) {
                            String principal = ace.getProperty("j:principal").getString();

                            if (matchUser(principal, site, jahiaPrincipal)) {
                                boolean granted = ace.getProperty("j:aceType").getString().equals("GRANT");

                                String roleSuffix = "";
                                if (ace.isNodeType("jnt:externalAce")) {
                                    roleSuffix = "/" + ace.getProperty("j:externalPermissionsName").getString();
                                }

                                Value[] roles = ace.getProperty(Constants.J_ROLES).getValues();
                                for (Value r : roles) {
                                    String role = r.getString();
                                    String key = principal + ":" + role + roleSuffix;
                                    if (!foundRoles.contains(key)) {
                                        if (granted) {
                                            grantedRoles.add(role + roleSuffix);
                                        }
                                        foundRoles.add(key);
                                    }
                                }
                            }
                        }
                    }
                    if (acl.hasProperty("j:inherit") && !acl.getProperty("j:inherit").getBoolean()) {
                        return grantedRoles;
                    }
                }
                if (node.getPath().equals("/")) {
                    break;
                }
                node = node.getParent();
            }
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }

        return grantedRoles;
    }

    private static Set<String> getOptimizedRoles(JCRNodeWrapper node, JahiaPrincipal jahiaPrincipal, String site) {
        Set<String> grantedRoles = new HashSet<String>();
        Set<String> foundRoles = new HashSet<String>();

        Map<String, List<String[]>> acl = node.getAclEntries();

        for (Map.Entry<String, List<String[]>> entry : acl.entrySet()) {
            String principal = entry.getKey();
            if (matchUser(principal, site, jahiaPrincipal)) {
                for (String[] ace : entry.getValue()) {
                    boolean granted = !ace[1].equals("DENY");
                    String role = ace[2];
                    String key = principal + ":" + role;
                    if (!foundRoles.contains(key)) {
                        if (granted) {
                            grantedRoles.add(role);
                        }
                        foundRoles.add(key);
                    }
                }
            }
        }
        return grantedRoles;
    }

    private static String resolveSite(String jcrPath) {
        String site;
        if (jcrPath.startsWith(JahiaSitesService.SITES_JCR_PATH + "/")) {
            site = StringUtils.substringBefore(jcrPath.substring(JahiaSitesService.SITES_JCR_PATH.length() + 1), "/");
        } else {
            site = JahiaSitesService.SYSTEM_SITE_KEY;
        }
        return site;
    }

    private static boolean recurseOnACPs(PathWrapper pathWrapper, Session s, Set<String> permissions, String site, Map<Object, CompiledAcl> compiledAcls, JahiaPrincipal jahiaPrincipal,
                                         boolean isAliased, JahiaPrivilegeRegistry privilegeRegistry, String workspaceName) throws RepositoryException {

        Set<String> foundRoles = new HashSet<String>();
        permissions = new HashSet<String>(permissions);
        while (pathWrapper.getLength() > 0) {

            CompiledAcl acl = compiledAcls.get(pathWrapper.getInnerObject());

            if (acl == null) {
                acl = new CompiledAcl();
                compiledAcls.put(pathWrapper.getInnerObject(), acl);

                Item i = pathWrapper.getItem();
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

                                if (matchUser(principal, site, jahiaPrincipal)) {
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
                    if (matchPermission(permissions, role, isAliased, privilegeRegistry, workspaceName)) {
                        return true;
                    }
                }
            }
            if (acl.broken) {
                return false;
            }

            if (pathWrapper.isRoot()) {
                return false;
            } else {
                pathWrapper = pathWrapper.getAncestor();
            }
        }
        return false;
    }

    private static Set<Privilege> internalGetPermissionsInRole(String role, JahiaPrivilegeRegistry privilegeRegistry) throws RepositoryException {
        Set<Privilege> privileges;
        String externalPermission = null;

        String roleName = role;
        if (roleName.contains("/")) {
            externalPermission = StringUtils.substringAfter(role, "/");
            roleName = StringUtils.substringBefore(role, "/");
        }

        Node roleNode = findRoleNode(roleName);
        if (roleNode != null) {
            privileges = getPrivileges(roleNode, externalPermission, privilegeRegistry);
        } else {
            privileges = Collections.emptySet();
        }
        return privileges;
    }

    private static Node findRoleNode(String role) throws RepositoryException {
        try {
            // we need a default system session to read roles from default workspace
            NodeIterator nodes = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, null, null).getWorkspace().getQueryManager().createQuery(
                    "select * from [" + Constants.JAHIANT_ROLE + "] as r where localname()='" + JCRContentUtils.sqlEncode(role) + "' and isdescendantnode(r,['/roles'])",
                    Query.JCR_SQL2).execute().getNodes();
            if (nodes.hasNext()) {
                return nodes.nextNode();
            }
        } catch (PathNotFoundException e) {
        }
        return null;
    }

    private static Set<Privilege> getPrivileges(Node roleNode, String externalPermission, JahiaPrivilegeRegistry privilegeRegistry) throws RepositoryException {
        Set<Privilege> privileges = new HashSet<Privilege>();

        Node roleParent = roleNode.getParent();
        if (roleParent.isNodeType(Constants.JAHIANT_ROLE)) {
            privileges = getPrivileges(roleParent, externalPermission, privilegeRegistry);
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

    private static boolean checkPrivilege(Set<String> permissions, String name, String cacheEntryKey, boolean isAliased, String workspaceName) {
        if (!isAliased || !name.contains("_" + Constants.EDIT_WORKSPACE)) {
            if (checkPrivilege(permissions, name)) {
                matchingPermissionsPut(cacheEntryKey, Boolean.TRUE, isAliased);
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

    private static boolean checkPrivilege(Set<String> permissions, String privilegeName) {
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

    private static boolean matchUser(String principal, String site, JahiaPrincipal jahiaPrincipal) {
        final String principalName = principal.substring(2);
        if (principal.charAt(0) == 'u') {
            if ((jahiaPrincipal.isGuest() && principalName.equals("guest")) ||
                    (principalName.equals(jahiaPrincipal.getName()) && (jahiaPrincipal.getRealm() == null || jahiaPrincipal.getRealm().equals(site)))) {
                return true;
            }
        } else if (principal.charAt(0) == 'g') {
            if (isUserMemberOf(principalName, site, jahiaPrincipal)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUserMemberOf(String groupname, String site, JahiaPrincipal jahiaPrincipal) {
        return  (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupname)) ||
                (!jahiaPrincipal.isGuest() && JahiaGroupManagerService.USERS_GROUPNAME.equals(groupname)) ||
                (!jahiaPrincipal.isGuest() && JahiaGroupManagerService.SITE_USERS_GROUPNAME.equals(groupname) && (jahiaPrincipal.getRealm() == null || jahiaPrincipal.getRealm().equals(site))) ||
                (!jahiaPrincipal.isGuest() && (ServicesRegistry.getInstance().getJahiaGroupManagerService().isMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), groupname, site) || ServicesRegistry.getInstance().getJahiaGroupManagerService().isMember(jahiaPrincipal.getName(), jahiaPrincipal.getRealm(), groupname, null)));
    }

    private static Boolean matchingPermissionsGet(String key, boolean isAliased) {
        return isAliased ? null : matchingPermissions.get(key);
    }

    private static void matchingPermissionsPut(String key, Boolean value, boolean isAliased) {
        if (!isAliased) {
            matchingPermissions.put(key, value);
        }
    }

    private static Boolean pathPermissionCacheGet(String key, boolean isAliased, Map<String, Boolean> pathPermissionCache) {
        return isAliased ? null : pathPermissionCache.get(key);
    }

    private static void pathPermissionCachePut(String key, Boolean value, boolean isAliased, Map<String, Boolean> pathPermissionCache) {
        if (!isAliased) {
            pathPermissionCache.put(key, value);
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

    private static  <K,V> Cache<K, V> initCache(String name) {
        CacheService cacheService = ServicesRegistry.getInstance().getCacheService();
        if (cacheService != null) {
            // Jahia is initialized
            try {
                return cacheService.getCache(name, true);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
