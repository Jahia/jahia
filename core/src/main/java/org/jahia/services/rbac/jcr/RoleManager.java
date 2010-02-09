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

package org.jahia.services.rbac.jcr;

import static org.jahia.api.Constants.JCR_DESCRIPTION;
import static org.jahia.api.Constants.JCR_TITLE;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
import org.jahia.services.usermanager.jcr.JCRPrincipal;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;

/**
 * Service for managing roles and permissions.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleManager {

    private static final List<JCRPermission> EMPTY_PERMISSION_LIST = Collections.emptyList();

    private static final List<JahiaPrincipal> EMPTY_PRINCIPAL_LIST = Collections.emptyList();

    private static final List<JCRRole> EMPTY_ROLE_LIST = Collections.emptyList();

    public static final String JAHIANT_PERMISSION = "jnt:permission";

    public static final String JAHIANT_PERMISSION_GROUP = "jnt:permissionGroup";

    public static final String JAHIANT_PERMISSIONS = "jnt:permissions";

    public static final String JAHIANT_ROLE = "jnt:role";

    public static final String JAHIANT_ROLES = "jnt:roles";

    public static final String JMIX_ROLE_BASED_ACCESS_CONTROLLED = "jmix:roleBasedAccessControlled";

    private static Logger logger = Logger.getLogger(RoleManager.class);

    public static final String PROPERTY_PERMISSSIONS = "j:permissions";

    public static final String PROPERTY_ROLES = "j:roles";

    protected String defaultPermissionGroup = "global";

    protected JCRGroupManagerProvider jcrGroupManagerProvider;

    protected JCRUserManagerProvider jcrUserManagerProvider;

    protected String permissionsNodeName = "permissions";

    protected String rolesNodeName = "roles";

    protected JahiaSitesService sitesService;

    /**
     * Looks up the permission by its corresponding JCR node path. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param jcrPath the JCR path of the corresponding JCR node
     * @param session current JCR session
     * @return the permission by its corresponding JCR node path. Returns $
     *         {@code null} if the requested permission is not found
     * @throws RepositoryException in case of an error
     */
    public JCRPermission getPermission(String jcrPath, JCRSessionWrapper session) throws RepositoryException {
        JCRPermission perm = null;
        try {
            perm = toPermission(session.getNode(jcrPath));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return perm;
    }

    /**
     * Looks up the permission with the requested name for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param name the name of the permission to look up
     * @param group the permission group name
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    public JCRPermission getPermission(String name, String group, String site, JCRSessionWrapper session)
            throws RepositoryException {
        JCRPermission perm = null;
        JCRNodeWrapper permissionsHome = getPermissionsHome(site, group, session);
        try {
            perm = toPermission(permissionsHome.getNode(name));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return perm;
    }

    /**
     * Returns a list of permissions, defined for the specified site. If the
     * specified site is ${@code null} returns global permissions for the
     * server.
     * 
     * @param site the site key to retrieve permissions for
     * @param session current JCR session
     * @return a list of permissions, defined for the specified site. If the
     *         specified site is ${@code null} returns global permissions for
     *         the server
     * @throws RepositoryException in case of an error
     */
    public List<JCRPermission> getPermissions(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<JCRPermission> permissions = new LinkedList<JCRPermission>();
        JCRNodeWrapper permissionsHome = getPermissionsHome(site, session);
        for (NodeIterator groupIterator = permissionsHome.getNodes(); groupIterator.hasNext();) {
            Node groupNode = groupIterator.nextNode();
            if (groupNode.isNodeType(JAHIANT_PERMISSION_GROUP)) {
                for (NodeIterator permIterator = groupNode.getNodes(); permIterator.hasNext();) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) permIterator.nextNode();
                    if (permissionNode.isNodeType(JAHIANT_PERMISSION)) {
                        permissions.add(toPermission(permissionNode));
                    }
                }
            }
        }
        return permissions.isEmpty() ? EMPTY_PERMISSION_LIST : permissions;
    }

    /**
     * Looks up the permissions with the requested group for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param group the permission group name
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    public List<JCRPermission> getPermissions(String group, String site, JCRSessionWrapper session)
            throws RepositoryException {
        List<JCRPermission> permissions = new LinkedList<JCRPermission>();
        JCRNodeWrapper permissionsHome = getPermissionsHome(site, session);
        for (NodeIterator groupIterator = permissionsHome.getNodes(); groupIterator.hasNext();) {
            Node groupNode = groupIterator.nextNode();
            if (groupNode.isNodeType(JAHIANT_PERMISSION_GROUP)) {
                for (NodeIterator permIterator = groupNode.getNodes(); permIterator.hasNext();) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) permIterator.nextNode();
                    if (permissionNode.isNodeType(JAHIANT_PERMISSION)) {
                        JCRPermission p = toPermission(permissionNode);
                        if (p.getGroup().equalsIgnoreCase(group)) {
                            permissions.add(p);
                        }
                    }
                }
            }
        }
        return permissions.isEmpty() ? EMPTY_PERMISSION_LIST : permissions;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified. This method
     * creates the requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified.
     *         This method creates the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPermissionsHome(String site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper permissionsNode = null;
        try {
            permissionsNode = session.getNode(site == null ? "/" + permissionsNodeName : "/sites/" + site + "/"
                    + permissionsNodeName);
        } catch (PathNotFoundException ex) {
            // create it
            JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
            session.checkout(parentNode);
            permissionsNode = parentNode.addNode(permissionsNodeName, JAHIANT_PERMISSIONS);
            session.save();
        }

        return permissionsNode;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified. The
     * permission group is considered in both cases. This method creates the
     * requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param group the permission group
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified. The
     *         permission group is considered in both cases. This method creates
     *         the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPermissionsHome(String site, String group, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(site, session);
        String permissionGroup = StringUtils.defaultIfEmpty(group, defaultPermissionGroup);
        try {
            permissionsNode = permissionsNode.getNode(permissionGroup);
        } catch (PathNotFoundException ex) {
            // create it
            session.checkout(permissionsNode);
            permissionsNode = permissionsNode.addNode(permissionGroup, JAHIANT_PERMISSION_GROUP);
            session.save();
        }

        return permissionsNode;
    }

    /**
     * Retrieved a JCR node that corresponds to the specified principal.
     * 
     * @param principal the principal to look up
     * @param session current JCR session
     * @return a JCR node that corresponds to the specified principal or null if
     *         the node cannot be retrieved
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPrincipalNode(JahiaPrincipal principal, JCRSessionWrapper session)
            throws RepositoryException {
        return getPrincipalNode(principal, session, false);
    }

    /**
     * Retrieved a JCR node that corresponds to the specified principal.
     * 
     * @param principal the principal to look up
     * @param session current JCR session
     * @param createExternalIfNeeded should we create a a node in the JCR for an
     *            external principal if needed?
     * @return a JCR node that corresponds to the specified principal or null if
     *         the node cannot be retrieved
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getPrincipalNode(JahiaPrincipal principal, JCRSessionWrapper session,
            boolean createExternalIfNeeded) throws RepositoryException {
        JCRNodeWrapper principalNode = null;
        if (principal instanceof JCRPrincipal) {
            try {
                principalNode = ((JCRPrincipal) principal).getNode(session);
            } catch (ItemNotFoundException e) {
                logger.warn("Unable to find principal node with identifier "
                        + ((JCRPrincipal) principal).getIdentifier());
            }
        } else if (principal instanceof JahiaUser) {
            JCRUser externalUser = jcrUserManagerProvider.lookupExternalUser(principal.getName());
            if (externalUser == null && createExternalIfNeeded) {
                JCRStoreService.getInstance().deployExternalUser(principal.getName(),
                        ((JahiaUser) principal).getProviderName());
                externalUser = jcrUserManagerProvider.lookupExternalUser(principal.getName());
            }
            if (externalUser != null) {
                try {
                    principalNode = externalUser.getNode(session);
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find user node with identifier " + externalUser.getIdentifier());
                }
            }
        } else if (principal instanceof JahiaGroup) {
            JahiaGroup grp = (JahiaGroup) principal;
            JCRGroup externalGroup = jcrGroupManagerProvider.lookupExternalGroup(grp.getGroupname());
            if (externalGroup == null && createExternalIfNeeded) {
                externalGroup = jcrGroupManagerProvider.createExternalGroup(grp.getGroupKey(), grp.getProviderName());
            }
            if (externalGroup != null) {
                try {
                    principalNode = externalGroup.getNode(session);
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find group node with identifier " + externalGroup.getIdentifier());
                }
            }
        }
        return principalNode;
    }

    /**
     * Returns a list of principals having the specified permission or an empty
     * list if the permission is not granted to anyone.
     * 
     * @param jcrPermissionPath the JCR path of the corresponding permission
     *            node
     * @param session current JCR session
     * @return a list of principals having the specified permission or an empty
     *         list if the permission is not granted to anyone
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role cannot be found
     */
    public List<JahiaPrincipal> getPrincipalsInPermission(final String jcrPermissionPath, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        Set<JahiaPrincipal> principals = new LinkedHashSet<JahiaPrincipal>();
        for (JCRRole role : getRolesInPermission(jcrPermissionPath, session)) {
            principals.addAll(getPrincipalsInRole(role.getPath(), session));
        }

        return principals.isEmpty() ? EMPTY_PRINCIPAL_LIST : new LinkedList<JahiaPrincipal>(principals);
    }

    /**
     * Returns a list of principals having the specified role or an empty list
     * if the role is not granted to anyone.
     * 
     * @param jcrRolePath the JCR path of the corresponding role node
     * @param session current JCR session
     * @return a list of principals having the specified role or an empty list
     *         if the role is not granted to anyone
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role cannot be found
     */
    public List<JahiaPrincipal> getPrincipalsInRole(final String jcrRolePath, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<JahiaPrincipal> principals = new LinkedList<JahiaPrincipal>();
        JCRNodeWrapper role = session.getNode(jcrRolePath);
        for (PropertyIterator iterator = role.getWeakReferences(PROPERTY_ROLES); iterator.hasNext();) {
            Property prop = iterator.nextProperty();
            Node principalNode = prop.getParent();
            if (principalNode != null) {
                JahiaPrincipal principal = toPrincipal(principalNode);
                if (principal != null) {
                    principals.add(principal);
                }
            } else {
                logger.warn("Principal node, referencing role '" + jcrRolePath + "' in property '" + prop.getPath()
                        + "' cannot be found");
            }

        }
        return principals.isEmpty() ? EMPTY_PRINCIPAL_LIST : principals;
    }

    /**
     * Looks up the role by its JCR path. Returns ${@code null} if the requested
     * role is not found.
     * 
     * @param jcrPath the JCR path of the corresponding node
     * @param session current JCR session
     * @return the role with the requested path. Returns ${@code null} if the
     *         requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public JCRRole getRole(final String jcrPath, JCRSessionWrapper session) throws RepositoryException {
        JCRRole role = null;
        try {
            role = toRole(session.getNode(jcrPath));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return role;
    }

    /**
     * Looks up the role with the requested name for the specified site. If site
     * is not specified considers it as a global role. Returns ${@code null} if
     * the requested role is not found.
     * 
     * @param name the name of the role to look up
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param session current JCR session
     * @return the role with the requested name for the specified site. If site
     *         is not specified considers it as a global role. Returns ${@code
     *         null} if the requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public JCRRole getRole(final String name, final String site, JCRSessionWrapper session) throws RepositoryException {
        JCRRole role = null;
        JCRNodeWrapper rolesHome = getRolesHome(site, session);
        try {
            role = toRole(rolesHome.getNode(name));
        } catch (PathNotFoundException e) {
            // the role does not exist
        }
        return role;
    }

    /**
     * Returns a list of roles, defined for the specified site. If the specified
     * site is ${@code null} returns global permissions for the server.
     * 
     * @param site the site key to retrieve roles for
     * @param session current JCR session
     * @return a list of roles, defined for the specified site. If the specified
     *         site is ${@code null} returns global permissions for the server.
     * @throws RepositoryException in case of an error
     */
    public List<JCRRole> getRoles(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<JCRRole> roles = new LinkedList<JCRRole>();
        JCRNodeWrapper rolesHome = getRolesHome(site, session);
        for (NodeIterator iterator = rolesHome.getNodes(); iterator.hasNext();) {
            JCRNodeWrapper roleNode = (JCRNodeWrapper) iterator.nextNode();
            if (roleNode.isNodeType(JAHIANT_ROLE)) {
                roles.add(toRole(roleNode));
            }
        }
        return roles.isEmpty() ? EMPTY_ROLE_LIST : roles;
    }

    /**
     * Returns the node that corresponds to the roles of the specified site or
     * the global roles, if the site is not specified. This method creates the
     * requested node, if it cannot be found.
     * 
     * @param site the site key or ${@code null} if the global roles node is
     *            requested
     * @param session current JCR session
     * @return the node that corresponds to the roles of the specified site or
     *         the global roles, if the site is not specified. This method
     *         creates the requested node, if it cannot be found
     * @throws RepositoryException in case of an error
     */
    protected JCRNodeWrapper getRolesHome(String site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rolesNode = null;
        try {
            rolesNode = session.getNode(site == null ? "/" + rolesNodeName : "/sites/" + site + "/" + rolesNodeName);
        } catch (PathNotFoundException ex) {
            // create it
            JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
            session.checkout(parentNode);
            rolesNode = parentNode.addNode(rolesNodeName, JAHIANT_ROLES);
            session.save();
        }

        return rolesNode;
    }

    /**
     * Returns a list of roles, having the specified permission or an empty list
     * if the permission is not granted to any role.
     * 
     * @param jcrPermissionPath the JCR path of the corresponding permission
     *            node
     * @param session current JCR session
     * @return a list of roles, having the specified permission or an empty list
     *         if the permission is not granted to any role
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role cannot be found
     */
    public List<JCRRole> getRolesInPermission(final String jcrPermissionPath, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<JCRRole> roles = new LinkedList<JCRRole>();
        JCRNodeWrapper permission = session.getNode(jcrPermissionPath);
        for (PropertyIterator iterator = permission.getWeakReferences(PROPERTY_PERMISSSIONS); iterator.hasNext();) {
            Property prop = iterator.nextProperty();
            Node roleNode = prop.getParent();
            if (roleNode != null) {
                JCRRole role = toRole(roleNode);
                if (role != null) {
                    roles.add(role);
                }
            } else {
                logger.warn("Role node, referencing permission '" + jcrPermissionPath + "' in property '"
                        + prop.getPath() + "' cannot be found or is not of type " + JAHIANT_ROLE);
            }

        }
        return roles;
    }

    /**
     * Grants a permission to the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPath permission to be granted, defined as a JCR path
     *            of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantPermission(final String roleJcrPath, String permissionJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> granted = new LinkedList<String>();
        granted.add(permissionJcrPath);
        grantPermissions(roleJcrPath, granted, session);
    }

    /**
     * Grants permissions to the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPaths permissions to be granted, defined as a JCR
     *            path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantPermissions(final String roleJcrPath, List<String> permissionJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (permissionJcrPaths == null || permissionJcrPaths.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(roleJcrPath);
        Set<String> toBeGranted = new LinkedHashSet<String>(permissionJcrPaths.size());
        for (String permission : permissionJcrPaths) {
            try {
                JCRNodeWrapper permissionNode = session.getNode(permission);
                toBeGranted.add(permissionNode.getIdentifier());
            } catch (PathNotFoundException ex) {
                logger.warn("Unable to find a node that corresponds to a permission '" + permission);
            }
        }

        List<Value> newValues = new LinkedList<Value>();
        if (roleNode.hasProperty(PROPERTY_PERMISSSIONS)) {
            Value[] oldValues = roleNode.getProperty(PROPERTY_PERMISSSIONS).getValues();
            for (Value oldOne : oldValues) {
                newValues.add(oldOne);
                toBeGranted.remove(oldOne.getString());

            }
        }
        for (String granted : toBeGranted) {
            newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
        }
        session.checkout(roleNode);
        roleNode.setProperty(PROPERTY_PERMISSSIONS, newValues.toArray(new Value[] {}));
        session.save();
    }

    /**
     * Grants a role to the specified principal.
     * 
     * @param principal principal to grant the role to
     * @param roleJcrPath the role to be granted, defined as a JCR path of the
     *            corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantRole(final JahiaPrincipal principal, String roleJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> granted = new LinkedList<String>();
        granted.add(roleJcrPath);
        grantRoles(principal, granted, session);
    }

    /**
     * Grants roles to the specified principal.
     * 
     * @param principal principal to grant roles to
     * @param roleJcrPaths the list of roles to be granted, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void grantRoles(final JahiaPrincipal principal, List<String> roleJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (roleJcrPaths == null || roleJcrPaths.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session, true);
        if (principalNode != null) {
            session.checkout(principalNode);

            if (!principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)) {
                principalNode.addMixin(JMIX_ROLE_BASED_ACCESS_CONTROLLED);
            }

            Set<String> toBeGranted = new LinkedHashSet<String>(roleJcrPaths.size());
            for (String role : roleJcrPaths) {
                try {
                    toBeGranted.add(session.getNode(role).getIdentifier());
                } catch (PathNotFoundException ex) {
                    logger.warn("Unable to find a node that corresponds to a role '" + role);
                }
            }

            List<Value> newValues = new LinkedList<Value>();
            if (principalNode.hasProperty(PROPERTY_ROLES)) {
                Value[] oldValues = principalNode.getProperty(PROPERTY_ROLES).getValues();
                for (Value oldOne : oldValues) {
                    newValues.add(oldOne);
                    toBeGranted.remove(oldOne.getString());
                }
            }
            for (String granted : toBeGranted) {
                newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
            }
            principalNode.setProperty(PROPERTY_ROLES, newValues.toArray(new Value[] {}));
            session.save();

            invalidateCache(principal);
        } else {
            logger.warn("Unable to find corresponding JCR node for principal " + principal + ". Skip granting roles.");
        }
    }

    protected void invalidateCache(JahiaPrincipal principal) {
        // TODO implement cache invalidation
    }

    /**
     * Revokes a permission from the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPath the permission to be removed, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokePermission(final String roleJcrPath, String permissionJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> toRevoke = new LinkedList<String>();
        toRevoke.add(permissionJcrPath);
        revokePermissions(roleJcrPath, toRevoke, session);
    }

    /**
     * Revokes permissions from the specified role.
     * 
     * @param roleJcrPath the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissionJcrPaths permissions to be removed, defined as a JCR
     *            path of the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokePermissions(final String roleJcrPath, List<String> permissionJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (permissionJcrPaths == null || permissionJcrPaths.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(roleJcrPath);
        Set<String> toRevoke = new HashSet<String>(permissionJcrPaths);
        if (roleNode.hasProperty(PROPERTY_PERMISSSIONS)) {
            Value[] values = roleNode.getProperty(PROPERTY_PERMISSSIONS).getValues();
            if (values != null) {
                List<Value> newValues = new LinkedList<Value>();
                for (Value value : values) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (permissionNode != null && !toRevoke.contains(permissionNode.getPath())) {
                        newValues.add(value);
                    }
                }
                if (values.length != newValues.size()) {
                    session.checkout(roleNode);
                    roleNode.setProperty(PROPERTY_PERMISSSIONS, newValues.toArray(new Value[] {}));
                    session.save();
                }
            }
        }
    }

    /**
     * Revokes a role from the specified principal.
     * 
     * @param principal principal to revoke the role from
     * @param roleJcrPath the role to be revoked, defined as a JCR path of the
     *            corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeRole(final JahiaPrincipal principal, String roleJcrPath, JCRSessionWrapper session)
            throws RepositoryException {
        List<String> revoked = new LinkedList<String>();
        revoked.add(roleJcrPath);
        revokeRoles(principal, revoked, session);
    }

    /**
     * Revokes roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @param roleJcrPaths the list of roles to revoke, defined as a JCR path of
     *            the corresponding node
     * @param session current JCR session
     * @throws RepositoryException in case of an error
     */
    public void revokeRoles(final JahiaPrincipal principal, List<String> roleJcrPaths, JCRSessionWrapper session)
            throws RepositoryException {
        if (roleJcrPaths == null || roleJcrPaths.isEmpty()) {
            return;
        }

        JCRNodeWrapper principalNode = getPrincipalNode(principal, session);
        if (principalNode != null && principalNode.isNodeType(JMIX_ROLE_BASED_ACCESS_CONTROLLED)
                && principalNode.hasProperty(PROPERTY_ROLES)) {
            Value[] values = principalNode.getProperty(PROPERTY_ROLES).getValues();
            if (values != null) {
                Set<String> toRevoke = new HashSet<String>(roleJcrPaths);
                List<Value> newValues = new LinkedList<Value>();
                for (Value value : values) {
                    JCRNodeWrapper roleNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (roleNode != null && !toRevoke.contains(roleNode.getPath())) {
                        newValues.add(value);
                    }
                }
                if (values.length != newValues.size()) {
                    session.checkout(principalNode);
                    principalNode.setProperty(PROPERTY_ROLES, newValues.toArray(new Value[] {}));
                    session.save();

                    invalidateCache(principal);
                }
            }
        } else {
            if (principalNode == null) {
                logger.warn("Unable to find corresponding JCR node for principal " + principal
                        + ". Skip revoking roles.");
            } else {
                logger.warn("Principal '" + principal.getName()
                        + "' does not have any roles assigned. Skip revoking roles.");
            }
        }
    }

    /**
     * Creates or updates the specified {@link JCRPermission}.
     * 
     * @param permission the permission to be stored
     * @param session current JCR session
     * @return the corresponding permission node
     * @throws RepositoryException in case of an error
     */
    public JCRNodeWrapper savePermission(JCRPermission permission, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(
                permission instanceof JCRSitePermission ? ((JCRSitePermission) permission).getSite() : null, session);
        JCRNodeWrapper target = null;
        try {
            String group = StringUtils.defaultIfEmpty(permission.getGroup(), defaultPermissionGroup);
            try {
                permissionsNode = permissionsNode.getNode(group);
            } catch (PathNotFoundException e) {
                // does not exist yet
                session.checkout(permissionsNode);
                permissionsNode = permissionsNode.addNode(group, JAHIANT_PERMISSION_GROUP);
            }
            permission.setGroup(group);
            target = permissionsNode.getNode(permission.getName());
            if (target.hasProperty(JCR_TITLE)) {
                permission.setTitle(target.getProperty(JCR_TITLE).getString());
            }
            if (target.hasProperty(JCR_DESCRIPTION)) {
                permission.setDescription(target.getProperty(JCR_DESCRIPTION).getString());
            }
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(permissionsNode);
            target = permissionsNode.addNode(permission.getName(), JAHIANT_PERMISSION);
        }

        permission.setPath(target.getPath());

        session.save();

        return target;
    }

    /**
     * Creates or updates the specified {@link JCRRole}.
     * 
     * @param role the role to be stored
     * @param session current JCR session
     * @return the corresponding role node
     * @throws RepositoryException in case of an error
     */
    public JCRNodeWrapper saveRole(JCRRole role, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rolesHome = getRolesHome(role instanceof JCRSiteRole ? ((JCRSiteRole) role).getSite() : null,
                session);
        JCRNodeWrapper roleNode = null;
        try {
            roleNode = rolesHome.getNode(role.getName());
            if (roleNode.hasProperty(JCR_TITLE)) {
                role.setTitle(roleNode.getProperty(JCR_TITLE).getString());
            }
            if (roleNode.hasProperty(JCR_DESCRIPTION)) {
                role.setDescription(roleNode.getProperty(JCR_DESCRIPTION).getString());
            }
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(rolesHome);
            roleNode = rolesHome.addNode(role.getName(), JAHIANT_ROLE);
        }

        role.setPath(roleNode.getPath());

        List<Value> values = new LinkedList<Value>();
        for (JCRPermission permission : role.getPermissions()) {
            values.add(new ValueImpl(savePermission(permission, session).getIdentifier(), PropertyType.WEAKREFERENCE));
        }
        roleNode.setProperty(PROPERTY_PERMISSSIONS, values.toArray(new Value[] {}));

        session.save();

        return roleNode;
    }

    /**
     * @param defaultPermissionGroup the defaultPermissionGroup to set
     */
    public void setDefaultPermissionGroup(String defaultPermissionGroup) {
        this.defaultPermissionGroup = defaultPermissionGroup;
    }

    /**
     * @param jcrGroupManagerProvider the jcrGroupManagerProvider to set
     */
    public void setJCRGroupManagerProvider(JCRGroupManagerProvider jcrGroupManagerProvider) {
        this.jcrGroupManagerProvider = jcrGroupManagerProvider;
    }

    /**
     * @param jcrUserManagerProvider the jcrUserManagerProvider to set
     */
    public void setJCRUserManagerProvider(JCRUserManagerProvider jcrUserManagerProvider) {
        this.jcrUserManagerProvider = jcrUserManagerProvider;
    }

    /**
     * @param permissionsNodeName the permissionsNodeName to set
     */
    public void setPermissionsNodeName(String permissionsNodeName) {
        this.permissionsNodeName = permissionsNodeName;
    }

    /**
     * @param rolesNodeName the rolesNodeName to set
     */
    public void setRolesNodeName(String rolesNodeName) {
        this.rolesNodeName = rolesNodeName;
    }

    /**
     * @param sitesService the sitesService to set
     */
    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    /**
     * Converts the provided JCR node to a {@link JCRPermission} object
     * populating corresponding fields.
     * 
     * @param permissionNode the underlying JCR node
     * @return {@link JCRPermission} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected JCRPermission toPermission(Node permissionNode) throws RepositoryException {
        String site = permissionNode.getPath().startsWith("/sites/") ? StringUtils.substringBetween(permissionNode
                .getPath(), "/sites/", "/" + permissionsNodeName + "/") : null;
        String group = StringUtils.substringBetween(permissionNode.getPath(), "/" + permissionsNodeName + "/", "/");

        JCRPermission perm = site != null ? new JCRSitePermission(permissionNode.getName(), group, site)
                : new JCRPermission(permissionNode.getName(), group);
        perm.setPath(permissionNode.getPath());
        if (permissionNode.hasProperty(JCR_TITLE)) {
            perm.setTitle(permissionNode.getProperty(JCR_TITLE).getString());
        }
        if (permissionNode.hasProperty(JCR_DESCRIPTION)) {
            perm.setDescription(permissionNode.getProperty(JCR_DESCRIPTION).getString());
        }

        return perm;
    }

    protected JahiaPrincipal toPrincipal(Node principalNode) throws RepositoryException {
        JahiaPrincipal principal = null;
        if (principalNode.isNodeType(Constants.JAHIANT_USER)) {
            // principal is a user
            JahiaUser user = null;
            if (principalNode.hasProperty(JCRUser.J_EXTERNAL)
                    && principalNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                // lookup external user node
                user = jcrUserManagerProvider.lookupExternalUser(principalNode.getName());
            } else {
                // lookup internal user node
                user = jcrUserManagerProvider.lookupUser(principalNode.getName());
            }
            if (user != null) {
                principal = user;
            } else {
                logger.warn("User cannot be found by name '" + principalNode.getName() + "'");
            }
        } else if (principalNode.isNodeType(Constants.JAHIANT_GROUP)) {
            // principal is a group
            if (principalNode.hasProperty(JCRUser.J_EXTERNAL)
                    && principalNode.getProperty(JCRUser.J_EXTERNAL).getBoolean()) {
                // lookup external group node
                principal = jcrGroupManagerProvider.lookupExternalGroup(principalNode.getName());
            } else {
                int siteId = 0;
                String siteKey = StringUtils.substringBetween(principalNode.getPath(), "/sites/", "/");
                if (siteKey != null) {
                    JahiaSite site = null;
                    try {
                        site = sitesService.getSiteByKey(siteKey);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (site != null) {
                        siteId = site.getID();
                    } else {
                        logger.warn("Unable to lookup site for key '" + siteKey + "'. Skipping group '"
                                + principalNode.getPath() + "'");
                        siteId = -1;
                    }
                }
                if (siteId != -1) {
                    JahiaGroup group = jcrGroupManagerProvider.lookupGroup(siteId, principalNode.getName());
                    if (group != null) {
                        principal = group;
                    } else {
                        logger.warn("Group cannot be found for name '" + principalNode.getName() + "' and site ID "
                                + siteId);
                    }
                }
            }            

        }
        return principal;
    }

    /**
     * Converts the provided JCR node to a {@link JCRRole} object populating
     * corresponding fields.
     * 
     * @param roleNode the underlying JCR node
     * @return {@link JCRRole} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected JCRRole toRole(Node roleNode) throws RepositoryException {
        String site = roleNode.getPath().startsWith("/sites/") ? StringUtils.substringBetween(roleNode.getPath(),
                "/sites/", "/" + rolesNodeName + "/") : null;
        JCRRole role = site != null ? new JCRSiteRole(roleNode.getName(), site) : new JCRRole(roleNode.getName());
        role.setPath(roleNode.getPath());
        if (roleNode.hasProperty(JCR_TITLE)) {
            role.setTitle(roleNode.getProperty(JCR_TITLE).getString());
        }
        if (roleNode.hasProperty(JCR_DESCRIPTION)) {
            role.setDescription(roleNode.getProperty(JCR_DESCRIPTION).getString());
        }
        if (roleNode.hasProperty(PROPERTY_PERMISSSIONS)) {
            Value[] values = roleNode.getProperty(PROPERTY_PERMISSSIONS).getValues();
            if (values != null) {
                for (Value value : values) {
                    JCRNodeWrapper permissionNode = (JCRNodeWrapper) ((JCRValueWrapper) value).getNode();
                    if (permissionNode != null) {
                        role.getPermissions().add(toPermission(permissionNode));
                    }
                }
            }
        }
        return role;
    }

}