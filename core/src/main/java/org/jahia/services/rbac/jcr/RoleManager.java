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
import java.util.LinkedList;
import java.util.List;

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
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.Role;

/**
 * Service for managing roles and permissions.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleManager {

    private static final List<PermissionImpl> EMPTY_PERMISSION_LIST = Collections.emptyList();

    private static final List<RoleImpl> EMPTY_ROLE_LIST = Collections.emptyList();

    public static final String JAHIANT_PERMISSION = "jnt:permission";

    public static final String JAHIANT_PERMISSION_GROUP = "jnt:permissionGroup";

    public static final String JAHIANT_PERMISSIONS = "jnt:permissions";

    public static final String JAHIANT_ROLE = "jnt:role";

    public static final String JAHIANT_ROLES = "jnt:roles";

    private static Logger logger = Logger.getLogger(RoleManager.class);

    public static final String PROPERTY_PERMISSSIONS = "j:permissions";

    private String defaultPermissionGroup = "global";

    private String permissionsNodeName = "permissions";

    private String rolesNodeName = "roles";

    /**
     * Deletes the specified permission if it exists.
     * 
     * @param permission the permission to be deleted
     * @param session current JCR session
     * @throws PathNotFoundException in case the corresponding permission cannot
     *             be found
     * @throws RepositoryException in case of an error
     */
    public void deletePermission(final Permission permission, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper permissionNode = loadPermissionNode(permission, session);
        session.checkout(permissionNode.getParent());
        session.checkout(permissionNode);
        permissionNode.remove();
        session.save();
    }

    /**
     * Deletes the specified role if it exists.
     * 
     * @param role the role to be deleted
     * @param session current JCR session
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public void deleteRole(final Role role, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper roleNode = loadRoleNode(role, session);
        session.checkout(roleNode.getParent());
        session.checkout(roleNode);
        roleNode.remove();
        session.save();
    }

    private String getPermissionPath(Permission permission) {
        return (permission.getSite() == null ? "" : "/sites/" + permission.getSite()) + "/" + permissionsNodeName + "/"
                + StringUtils.defaultIfEmpty(permission.getGroup(), "global") + "/" + permission.getName();
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
        try {
            for (NodeIterator groupIterator = getPermissionsHome(site, false, session).getNodes(); groupIterator
                    .hasNext();) {
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
        } catch (PathNotFoundException ex) {
            // no permissions node found
        }
        return permissions.isEmpty() ? EMPTY_PERMISSION_LIST : permissions;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified. The
     * permission group is considered in both cases.<br>
     * If the node is not found and the <code>createIfNotPresent</code> is set
     * to true this method creates the requested nodes; otherwise it returns
     * null.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param group the permission group
     * @param createIfNotPresent do we need to create corresponding nodes for
     *            permissions and group if not yet present?
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified. The
     *         permission group is considered in both cases. If the node is not
     *         found and the <code>createIfNotPresent</code> is set to true this
     *         method creates the requested nodes; otherwise it returns null.
     * @throws RepositoryException in case of an error
     */
    private JCRNodeWrapper getPermissionsGroupHome(String site, String group, boolean createIfNotPresent,
            JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper permissionsNode = getPermissionsHome(site, createIfNotPresent, session);

        String permissionGroup = StringUtils.defaultIfEmpty(group, defaultPermissionGroup);
        try {
            permissionsNode = permissionsNode.getNode(permissionGroup);
        } catch (PathNotFoundException ex) {
            if (createIfNotPresent) {
                // create it
                session.checkout(permissionsNode);
                permissionsNode = permissionsNode.addNode(permissionGroup, JAHIANT_PERMISSION_GROUP);
                session.save();
            } else {
                throw ex;
            }
        }

        return permissionsNode;
    }

    /**
     * Returns the node that corresponds to the permissions of the specified
     * site or the global permissions, if the site is not specified.<br>
     * If the node is not found and the <code>createIfNotPresent</code> is set
     * to true this method creates the requested nodes; otherwise it throws
     * {@link PathNotFoundException}.
     * 
     * @param site the site key or ${@code null} if the global permissions node
     *            is requested
     * @param createIfNotPresent do we need to create corresponding nodes for
     *            permissions and group if not yet present?
     * @param session current JCR session
     * @return the node that corresponds to the permissions of the specified
     *         site or the global permissions, if the site is not specified. If
     *         the node is not found and the <code>createIfNotPresent</code> is
     *         set to true this method creates the requested nodes; otherwise it
     *         throws {@link PathNotFoundException}.
     * @throws PathNotFoundException in case the node cannot be found and
     *             <code>createIfNotPresent</code> is set to false
     * @throws RepositoryException in case of an error
     */
    private JCRNodeWrapper getPermissionsHome(String site, boolean createIfNotPresent, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        JCRNodeWrapper permissionsNode = null;
        try {
            permissionsNode = session.getNode(site == null ? "/" + permissionsNodeName : "/sites/" + site + "/"
                    + permissionsNodeName);
        } catch (PathNotFoundException ex) {
            if (createIfNotPresent) {
                // create it
                JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
                session.checkout(parentNode);
                permissionsNode = parentNode.addNode(permissionsNodeName, JAHIANT_PERMISSIONS);
                session.save();
            } else {
                throw ex;
            }
        }

        return permissionsNode;
    }

    private String getRolePath(Role role) {
        return (role.getSite() == null ? "" : "/sites/" + role.getSite()) + "/" + rolesNodeName + "/" + role.getName();
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
        try {
            JCRNodeWrapper rolesHome = getRolesHome(site, false, session);
            for (NodeIterator iterator = rolesHome.getNodes(); iterator.hasNext();) {
                JCRNodeWrapper roleNode = (JCRNodeWrapper) iterator.nextNode();
                if (roleNode.isNodeType(JAHIANT_ROLE)) {
                    roles.add(toRole(roleNode));
                }
            }
        } catch (PathNotFoundException ex) {
            // roles node is not found
        }
        return roles.isEmpty() ? EMPTY_ROLE_LIST : roles;
    }

    /**
     * Returns the node that corresponds to the roles of the specified site or
     * the global roles, if the site is not specified. If the node is not found
     * and the <code>createIfNotPresent</code> is set to true this method
     * creates the requested nodes; otherwise it throws
     * {@link PathNotFoundException}.
     * 
     * @param site the site key or ${@code null} if the global roles node is
     *            requested
     * @param createIfNotPresent do we need to create corresponding node if not
     *            yet present?
     * @param session current JCR session
     * @return the node that corresponds to the roles of the specified site or
     *         the global roles, if the site is not specified. If the node is
     *         not found and the <code>createIfNotPresent</code> is set to true
     *         this method creates the requested nodes; otherwise it throws
     *         {@link PathNotFoundException}.
     * @throws PathNotFoundException in case the node cannot be found and
     *             <code>createIfNotPresent</code> is set to false
     * @throws RepositoryException in case of an error
     */
    private JCRNodeWrapper getRolesHome(String site, boolean createIfNotPresent, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        JCRNodeWrapper rolesNode = null;
        try {
            rolesNode = session.getNode(site == null ? "/" + rolesNodeName : "/sites/" + site + "/" + rolesNodeName);
        } catch (PathNotFoundException ex) {
            if (createIfNotPresent) {
                // create it
                JCRNodeWrapper parentNode = session.getNode(site != null ? "/sites/" + site : "/");
                session.checkout(parentNode);
                rolesNode = parentNode.addNode(rolesNodeName, JAHIANT_ROLES);
                session.save();
            } else {
                throw ex;
            }
        }

        return rolesNode;
    }

    /**
     * Returns a list of roles, having the specified permission or an empty list
     * if the permission is not granted to any role.
     * 
     * @param permission the permission to check
     * @param session current JCR session
     * @return a list of roles, having the specified permission or an empty list
     *         if the permission is not granted to any role
     * @throws RepositoryException in case of an error
     */
    public List<RoleImpl> getRolesInPermission(final Permission permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        List<RoleImpl> roles = new LinkedList<RoleImpl>();
        JCRNodeWrapper permissionNode = null;
        try {
            permissionNode = loadPermissionNode(permission, session);
        } catch (PathNotFoundException e) {
            logger.warn("Unable to find the node for the permission " + permission);
        }
        if (permissionNode != null) {
            for (PropertyIterator iterator = permissionNode.getWeakReferences(PROPERTY_PERMISSSIONS); iterator
                    .hasNext();) {
                Property prop = iterator.nextProperty();
                Node roleNode = prop.getParent();
                if (roleNode != null) {
                    RoleImpl role = toRole(roleNode);
                    if (role != null) {
                        roles.add(role);
                    }
                } else {
                    logger.warn("Role node, referencing permission '" + permissionNode.getPath() + "' in property '"
                            + prop.getPath() + "' cannot be found or is not of type " + JAHIANT_ROLE);
                }
            }
        }
        return roles;
    }

    /**
     * Looks up the permission with the requested name/group for the specified
     * site. If site is not specified considers it as a global permission.
     * Throws {@link PathNotFoundException} if the requested permission is not
     * found.
     * 
     * @param permission the permission to search for
     * @param session current JCR session
     * @return the permission with the requested name/group for the specified
     *         site. If site is not specified considers it as a global
     *         permission. Throws {@link PathNotFoundException} if the requested
     *         permission is not found
     * @throws PathNotFoundException in case the corresponding permission cannot
     *             be found
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl loadPermission(Permission permission, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        return toPermission(loadPermissionNode(permission, session));
    }

    JCRNodeWrapper loadPermissionNode(Permission permission, JCRSessionWrapper session) throws PathNotFoundException,
            RepositoryException {
        return session.getNode(getPermissionPath(permission));
    }

    /**
     * Looks up the role with the requested name for the specified site. If site
     * is not specified considers it as a global role. Throws
     * {@link PathNotFoundException} if the requested role is not found.
     * 
     * @param role the role to search for
     * @param session current JCR session
     * @return the role with the requested name for the specified site. If site
     *         is not specified considers it as a global role. Throws
     *         {@link PathNotFoundException} if the requested role is not found.
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl loadRole(Role role, JCRSessionWrapper session) throws PathNotFoundException, RepositoryException {
        return toRole(loadRoleNode(role, session));
    }

    JCRNodeWrapper loadRoleNode(Role role, JCRSessionWrapper session) throws PathNotFoundException, RepositoryException {
        return session.getNode(getRolePath(role));
    }

    private void populateJCRData(Node node, BaseImpl itemToBePopuilated) throws RepositoryException {
        itemToBePopuilated.setIdentifier(node.getIdentifier());
        itemToBePopuilated.setPath(node.getPath());
        if (node.hasProperty(JCR_TITLE)) {
            itemToBePopuilated.setTitle(node.getProperty(JCR_TITLE).getString());
        }
        if (node.hasProperty(JCR_DESCRIPTION)) {
            itemToBePopuilated.setDescription(node.getProperty(JCR_DESCRIPTION).getString());
        }
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     * 
     * @param permissionId the permission to be stored
     * @param session current JCR session
     * @param createIfNotPresent do we need to create corresponding nodes for if
     *            not yet present?
     * @return the corresponding permission node
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl savePermission(Permission permissionId, JCRSessionWrapper session)
            throws PathNotFoundException, RepositoryException {
        String group = StringUtils.defaultIfEmpty(permissionId.getGroup(), defaultPermissionGroup);
        JCRNodeWrapper permissionsNode = getPermissionsGroupHome(permissionId.getSite(), group, true, session);
        JCRNodeWrapper permissionNode = null;
        try {
            permissionNode = permissionsNode.getNode(permissionId.getName());
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(permissionsNode);
            permissionNode = permissionsNode.addNode(permissionId.getName(), JAHIANT_PERMISSION);
        }

        session.save();

        PermissionImpl permission = permissionId instanceof PermissionImpl ? (PermissionImpl) permissionId
                : new PermissionImpl(permissionId.getName(), group, permissionId.getSite());
        permission.setGroup(group);

        populateJCRData(permissionNode, permission);

        return permission;
    }

    /**
     * Creates or updates the specified {@link RoleImpl}, including the
     * associated permissions.
     * 
     * @param role the role to be stored
     * @param session current JCR session
     * @return the corresponding role node
     * @throws RepositoryException in case of an error
     */
    public RoleImpl saveRole(RoleImpl role, boolean updatePermisisons, JCRSessionWrapper session)
            throws RepositoryException {
        JCRNodeWrapper rolesHome = getRolesHome(role.getSite(), true, session);
        JCRNodeWrapper roleNode = null;
        try {
            roleNode = rolesHome.getNode(role.getName());
        } catch (PathNotFoundException e) {
            // does not exist yet
            session.checkout(rolesHome);
            roleNode = rolesHome.addNode(role.getName(), JAHIANT_ROLE);
        }

        populateJCRData(roleNode, role);

        if (updatePermisisons) {
            updatePermissions(role, true, session);
        }

        session.save();

        return role;
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
    private PermissionImpl toPermission(Node permissionNode) throws RepositoryException {
        PermissionImpl perm = new PermissionImpl(permissionNode.getName(), StringUtils.substringBetween(permissionNode
                .getPath(), "/" + permissionsNodeName + "/", "/"), JCRContentUtils.getSiteKey(permissionNode.getPath()));

        populateJCRData(permissionNode, perm);

        return perm;
    }

    /**
     * Converts the provided JCR node to a {@link RoleImpl} object populating
     * corresponding fields.
     * 
     * @param roleNode the underlying JCR node
     * @return {@link RoleImpl} object populated with corresponding data
     * @throws RepositoryException in case of an error
     */
    private RoleImpl toRole(Node roleNode) throws RepositoryException {
        RoleImpl role = new RoleImpl(roleNode.getName(), JCRContentUtils.getSiteKey(roleNode.getPath()));

        populateJCRData(roleNode, role);

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

    /**
     * Updates the assigned permissions of the specified role.
     * 
     * @param role the role to be modified
     * @param createPermissionIfNotPresent if set to <code>true</code> the
     *            missing permission nodes will be automatically created
     * @param session current JCR session
     * @return the resulting role instance
     * @throws RepositoryException in case of an error
     */
    public RoleImpl updatePermissions(final RoleImpl role, boolean createPermissionIfNotPresent,
            JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper roleNode = loadRoleNode(role, session);
        List<Value> values = new LinkedList<Value>();
        for (PermissionImpl permission : role.getPermissions()) {
            try {
                values.add(new ValueImpl(createPermissionIfNotPresent ? savePermission(permission, session)
                        .getIdentifier() : loadPermissionNode(permission, session).getIdentifier(),
                        PropertyType.WEAKREFERENCE));
            } catch (PathNotFoundException ex) {
                logger.warn("Permission to be granted for role '" + role.getName() + "' cannot be found " + permission
                        + ". It will be skipped.");
            }
        }
        session.checkout(roleNode);
        roleNode.setProperty(PROPERTY_PERMISSSIONS, values.toArray(new Value[] {}));
        session.save();

        return role;
    }

}