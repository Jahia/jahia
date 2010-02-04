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

package org.jahia.services.rbac.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;

/**
 * Service for managing roles and permissions.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleManager {

    private static final List<PermissionImpl> EMPTY_PERMISSION_LIST = Collections.emptyList();

    private static final List<RoleImpl> EMPTY_ROLE_LIST = Collections.emptyList();

    private static RoleManager instance;

    public static final String JAHIANT_PERMISSION = "jnt:permission";

    public static final String JAHIANT_PERMISSION_GROUP = "jnt:permissionGroup";

    public static final String JAHIANT_PERMISSIONS = "jnt:permissions";

    public static final String JAHIANT_ROLE = "jnt:role";

    public static final String JAHIANT_ROLES = "jnt:roles";

    private static Logger logger = Logger.getLogger(RoleManager.class);

    /**
     * Returns an instance of this service.
     * 
     * @return an instance of this service
     */
    public static RoleManager getInstance() {
        if (instance == null) {
            instance = new RoleManager();
        }
        return instance;
    }

    protected String defaultPermissionGroup = "global";

    protected String permissionsNodeName = "permissions";

    protected String rolesNodeName = "roles";

    /**
     * Looks up the permission with the requested name for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     * 
     * @param name the name of the permission to look up
     * @param group the permission group name
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    private PermissionImpl getPermission(final String name, final String group, final String site)
            throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                PermissionImpl perm = null;
                JCRNodeWrapper permissionsHome = getPermissionsHome(site, group, session);
                try {
                    perm = toPermission(permissionsHome.getNode(name));
                } catch (PathNotFoundException e) {
                    // the role does not exist
                }
                return perm;
            }
        });
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
    public List<PermissionImpl> getPermissions(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<PermissionImpl> permissions = new LinkedList<PermissionImpl>();
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
            permissionsNode = session.getNode(permissionGroup);
        } catch (PathNotFoundException ex) {
            // create it
            session.checkout(permissionsNode);
            permissionsNode = permissionsNode.addNode(permissionGroup, JAHIANT_PERMISSION_GROUP);
        }

        return permissionsNode;
    }

    /**
     * Looks up the role with the requested name for the specified site. If site
     * is not specified considers it as a global role. Returns ${@code null} if
     * the requested role is not found.
     * 
     * @param name the name of the role to look up
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @return the role with the requested name for the specified site. If site
     *         is not specified considers it as a global role. Returns ${@code
     *         null} if the requested role is not found.
     * @throws RepositoryException in case of an error
     */
    private RoleImpl getRole(final String name, final String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                RoleImpl role = null;
                JCRNodeWrapper rolesHome = getRolesHome(site, session);
                try {
                    role = toRole(rolesHome.getNode(name), site);
                } catch (PathNotFoundException e) {
                    // the role does not exist
                }
                return role;
            }
        });
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
    public List<RoleImpl> getRoles(final String site, JCRSessionWrapper session) throws RepositoryException {
        List<RoleImpl> roles = new LinkedList<RoleImpl>();
        JCRNodeWrapper rolesHome = getRolesHome(site, session);
        for (NodeIterator iterator = rolesHome.getNodes(); iterator.hasNext();) {
            JCRNodeWrapper roleNode = (JCRNodeWrapper) iterator.nextNode();
            if (roleNode.isNodeType(JAHIANT_ROLE)) {
                roles.add(toRole(roleNode, site));
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
        }

        return rolesNode;
    }

    /**
     * Revokes a permission from the specified role.
     * 
     * @param role the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permission the permission to be removed, defined as a JCR path of
     *            the corresponding node
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role is not found
     */
    public void revokePermission(final String role, String permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<String> toRevoke = new LinkedList<String>();
        toRevoke.add(permission);
        revokePermissions(role, toRevoke, session);
    }

    /**
     * Revokes permissions from the specified role.
     * 
     * @param role the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissions permissions to be removed, defined as a JCR path of
     *            the corresponding node
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role is not found
     */
    public void revokePermissions(final String role, List<String> permissions, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(role);
        Set<String> toRevoke = new HashSet<String>(permissions);
        if (roleNode.hasProperty("j:permissions")) {
            Value[] values = roleNode.getProperty("j:permissions").getValues();
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
                    roleNode.setProperty("j:permissions", newValues.toArray(new Value[] {}));
                }
            }
        }
    }

    /**
     * Grant permissions to the specified role.
     * 
     * @param role the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permissions permissions to be granted, defined as a JCR path of
     *            the corresponding node
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role is not found
     */
    public void grantPermissions(final String role, List<String> permissions, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        JCRNodeWrapper roleNode = session.getNode(role);
        Set<String> toBeGranted = new LinkedHashSet<String>(permissions.size());
        for (String permission : toBeGranted) {
            try {
                JCRNodeWrapper permissionNode = session.getNode(permission);
                toBeGranted.add(permissionNode.getIdentifier());
            } catch (PathNotFoundException ex) {
                logger.warn("Unable to find a node that corresponds to a permission '" + permission);
            }
        }

        List<Value> newValues = new LinkedList<Value>();
        if (roleNode.hasProperty("j:permissions")) {
            Value[] oldValues = roleNode.getProperty("j:permissions").getValues();
            for (Value oldOne : oldValues) {
                newValues.add(oldOne);
                toBeGranted.remove(oldOne.getString());

            }
        }
        for (String granted : toBeGranted) {
            newValues.add(new ValueImpl(granted, PropertyType.WEAKREFERENCE));
        }
        session.checkout(roleNode);
        roleNode.setProperty("j:permissions", newValues.toArray(new Value[] {}));
    }

    /**
     * Grant a permission to the specified role.
     * 
     * @param role the role to be modified, defined as a JCR path of the
     *            corresponding node
     * @param permission permission to be granted, defined as a JCR path of the
     *            corresponding node
     * @throws RepositoryException in case of an error
     * @throws PathNotFoundException in case the specified role is not found
     */
    public void grantPermission(final String role, String permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<String> granted = new LinkedList<String>();
        granted.add(permission);
        grantPermissions(role, granted, session);
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     * 
     * @param permission the permission to be stored
     * @throws RepositoryException in case of an error
     */
    private void savePermission(final PermissionImpl permission) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                savePermission(permission, session);
                return true;
            }
        });
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     * 
     * @param permission the permission to be stored
     * @param session current JCR session
     * @return the corresponding permission node
     * @throws RepositoryException in case of an error
     */
    private JCRNodeWrapper savePermission(PermissionImpl permission, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(
                permission instanceof SitePermissionImpl ? ((SitePermissionImpl) permission).getSite() : null, session);
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
            target = permissionsNode.getNode(permission.getName());
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(permissionsNode);
            target = permissionsNode.addNode(permission.getName(), JAHIANT_PERMISSION);
        }

        if (permission.getDescription() != null) {
            target.setProperty("jcr:description", permission.getDescription());
        }

        return target;
    }

    /**
     * Creates or updates the specified {@link RoleImpl}.
     * 
     * @param role the role to be stored
     * @throws RepositoryException in case of an error
     */
    private void saveRole(final RoleImpl role) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                saveRole(role, session);
                return true;
            }
        });
    }

    /**
     * Creates or updates the specified {@link RoleImpl}.
     * 
     * @param role the role to be stored
     * @param session current JCR session
     * @return the corresponding role node
     * @throws RepositoryException in case of an error
     */
    private JCRNodeWrapper saveRole(RoleImpl role, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper rolesHome = getRolesHome(role instanceof SiteRoleImpl ? ((SiteRoleImpl) role).getSite() : null,
                session);
        JCRNodeWrapper roleNode = null;
        try {
            roleNode = rolesHome.getNode(role.getName());
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(rolesHome);
            roleNode = rolesHome.addNode(role.getName(), JAHIANT_ROLE);
        }
        if (role.getDescription() != null) {
            roleNode.setProperty("jcr:description", role.getDescription());
        }

        List<Value> values = new LinkedList<Value>();
        for (PermissionImpl permission : role.getPermissions()) {
            values.add(new ValueImpl(savePermission(permission, session).getIdentifier(), PropertyType.WEAKREFERENCE));
        }
        roleNode.setProperty("j:permissions", values.toArray(new Value[] {}));

        return roleNode;
    }

    /**
     * @param defaultPermissionGroup the defaultPermissionGroup to set
     */
    public void setDefaultPermissionGroup(String defaultPermissionGroup) {
        this.defaultPermissionGroup = defaultPermissionGroup;
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
     * Converts the provided JCR node to a {@link PermissionImpl} object
     * populating corresponding fields.
     * 
     * @param permissionNode the underlying JCR node
     * @return {@link PermissionImpl} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected PermissionImpl toPermission(JCRNodeWrapper permissionNode) throws RepositoryException {
        String site = permissionNode.getPath().startsWith("/sites/") ? StringUtils.substringBetween(permissionNode
                .getPath(), "/sites/", "/" + permissionsNodeName + "/") : null;
        String group = StringUtils.substringBetween(permissionNode.getPath(), "/" + permissionsNodeName + "/", "/");

        PermissionImpl perm = site != null ? new SitePermissionImpl(permissionNode.getName(), group, site)
                : new PermissionImpl(permissionNode.getName(), group);
        perm.setPath(permissionNode.getPath());
        if (permissionNode.hasProperty("jcr:title")) {
            perm.setTitle(permissionNode.getProperty("jcr:title").getString());
        }
        if (permissionNode.hasProperty("jcr:description")) {
            perm.setDescription(permissionNode.getProperty("jcr:description").getString());
        }

        return perm;
    }

    /**
     * Converts the provided JCR node to a {@link RoleImpl} object populating
     * corresponding fields.
     * 
     * @param roleNode the underlying JCR node
     * @param target site key
     * @return {@link RoleImpl} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    protected RoleImpl toRole(JCRNodeWrapper roleNode, String site) throws RepositoryException {
        RoleImpl role = site != null ? new SiteRoleImpl(roleNode.getName(), site) : new RoleImpl(roleNode.getName());
        role.setPath(roleNode.getPath());
        if (roleNode.hasProperty("jcr:title")) {
            role.setTitle(roleNode.getProperty("jcr:title").getString());
        }
        if (roleNode.hasProperty("jcr:description")) {
            role.setDescription(roleNode.getProperty("jcr:description").getString());
        }
        if (roleNode.hasProperty("j:permissions")) {
            Value[] values = roleNode.getProperty("j:permissions").getValues();
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