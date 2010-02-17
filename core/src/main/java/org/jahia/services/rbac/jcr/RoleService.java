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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.Role;

/**
 * Service for managing roles and permissions.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleService {

    private RoleManager roleManager;

    /**
     * Deletes the specified permission if it exists.
     * 
     * @param permission the permission to be deleted
     * @throws PathNotFoundException in case the corresponding permission cannot
     *             be found
     * @throws RepositoryException in case of an error
     */
    public void deletePermission(final Permission permission, JCRSessionWrapper session) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                roleManager.deletePermission(permission, session);
                return true;
            }
        });
    }

    /**
     * Deletes the specified role if it exists.
     * 
     * @param role the role to be deleted
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public void deleteRole(final Role role) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                roleManager.deleteRole(role, session);
                return true;
            }
        });
    }

    /**
     * Returns the requested permission or <code>null</code> if the requested
     * permission cannot be found.
     * 
     * @param permissionId the permission to look up for
     * @return the requested permission or <code>null</code> if the requested
     *         permission cannot be found
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl getPermission(final Permission permissionId) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    return roleManager.loadPermission(permissionId, session);
                } catch (PathNotFoundException ex) {
                    return null;
                }
            }
        });
    }

    /**
     * Looks up list of permissions with for the specified site. If site is not
     * specified considers server-level permissions.
     * 
     * @param site the site key or ${@code null} if the global permissions are
     *            requested
     * @return the list of permissions with for the specified site. If site is
     *         not specified considers server-level permissions
     * @throws RepositoryException in case of an error
     */
    public List<PermissionImpl> getPermissions(final String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<PermissionImpl>>() {
            public List<PermissionImpl> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return roleManager.getPermissions(site, session);
            }
        });
    }

    /**
     * Returns the requested role or <code>null</code> if the requested role
     * cannot be found.
     * 
     * @param roleId the role to look up for
     * @return the requested role or <code>null</code> if the requested role
     *         cannot be found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl getRole(final Role roleId) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    return roleManager.loadRole(roleId, session);
                } catch (PathNotFoundException ex) {
                    return null;
                }
            }
        });
    }

    /**
     * Returns a list of roles, defined for the specified site. If the specified
     * site is ${@code null} returns global permissions for the server.
     * 
     * @param site the site key to retrieve roles for
     * @return a list of roles, defined for the specified site. If the specified
     *         site is ${@code null} returns global permissions for the server.
     * @throws RepositoryException in case of an error
     */
    public List<RoleImpl> getRoles(final String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<RoleImpl>>() {
            public List<RoleImpl> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return roleManager.getRoles(site, session);
            }
        });
    }

    /**
     * Grants the specified permission to the provided role. This operation does
     * not revoke currently assigned permissions, but rather adds the specified
     * one.
     * 
     * @param roleId the role to be modified
     * @param permission the permission to be granted
     * @return updated role object
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl grantPermission(final Role roleId, final Permission permission) throws RepositoryException {
        List<Permission> perms = new LinkedList<Permission>();
        perms.add(permission);
        return grantPermissions(roleId, perms);
    }

    /**
     * Grants specified permissions to the provided role. This operation does
     * not revoke currently assigned permissions, but rather adds the specified
     * ones.
     * 
     * @param roleId the role to be modified
     * @param permissions permissions to be granted
     * @return updated role object
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl grantPermissions(final Role roleId, final List<? extends Permission> permissions)
            throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                RoleImpl role = roleManager.loadRole(roleId, session);
                Set<PermissionImpl> finalPermissions = new LinkedHashSet<PermissionImpl>(role.getPermissions());
                for (Permission permission : permissions) {
                    finalPermissions.add(new PermissionImpl(permission.getName(), permission.getGroup(), permission
                            .getSite()));
                }
                if (finalPermissions.size() != role.getPermissions().size()) {
                    role.setPermissions(finalPermissions);
                    roleManager.updatePermissions(role, false, session);
                }
                return role;
            }
        });
    }

    /**
     * Revokes all permissions from the provided role.
     * 
     * @param roleId the role to be modified
     * @return updated role object
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl revokeAllPermissions(final Role roleId) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                RoleImpl role = roleManager.loadRole(roleId, session);
                if (role.getPermissions().size() > 0) {
                    role.getPermissions().clear();
                    roleManager.updatePermissions(role, false, session);
                }
                return role;
            }
        });
    }

    /**
     * Revokes the specified permission from the provided role.
     * 
     * @param roleId the role to be modified
     * @param permission permission to be revoked
     * @return updated role object
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl revokePermission(final Role roleId, final Permission permission) throws RepositoryException {
        List<Permission> perms = new LinkedList<Permission>();
        perms.add(permission);
        return revokePermissions(roleId, perms);
    }

    /**
     * Revokes specified permissions from the provided role.
     * 
     * @param roleId the role to be modified
     * @param permissions permissions to be revoked
     * @return updated role object
     * @throws PathNotFoundException in case the corresponding role cannot be
     *             found
     * @throws RepositoryException in case of an error
     */
    public RoleImpl revokePermissions(final Role roleId, final List<? extends Permission> permissions)
            throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                RoleImpl role = roleManager.loadRole(roleId, session);
                Set<PermissionImpl> finalPermissions = new LinkedHashSet<PermissionImpl>(role.getPermissions());
                for (Permission permission : permissions) {
                    finalPermissions.remove(new PermissionImpl(permission.getName(), permission.getGroup(), permission
                            .getSite()));
                }
                if (finalPermissions.size() != role.getPermissions().size()) {
                    role.setPermissions(finalPermissions);
                    roleManager.updatePermissions(role, false, session);
                }
                return role;
            }
        });
    }

    /**
     * Creates a new permission with the specified data if it does not exist yet
     * or returns the existing one.
     * 
     * @param permissionIdentity the permission data
     * @return newly created or existing instance of the permission
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl savePermission(final Permission permissionIdentity) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return roleManager.savePermission(permissionIdentity, session);
            }
        });
    }

    /**
     * Creates or updates the specified role without touching permissions.
     * 
     * @param role the role to be modified
     * @return updated role object
     * @throws RepositoryException in case of an error
     */
    public RoleImpl saveRole(final Role role) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return roleManager.saveRole(new RoleImpl(role.getName(), role.getSite()), false, session);
            }
        });
    }

    /**
     * Creates or updates the specified role, recursively creating or updating
     * the assigned permissions.
     * 
     * @param role the role to be modified
     * @return updated role object
     * @throws RepositoryException in case of an error
     */
    public RoleImpl saveRole(final RoleImpl role) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return roleManager.saveRole(role, true, session);
            }
        });
    }

    /**
     * Injects the dependency to {@link RoleManager}.
     * 
     * @param roleManager the dependency to {@link RoleManager}
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

}