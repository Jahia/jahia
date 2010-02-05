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

import java.util.List;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaPrincipal;

/**
 * Service for managing roles and permissions.
 *
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class SystemRoleManager extends RoleManager {

    /**
     * Looks up the permission by its corresponding JCR node path. Returns $
     * {@code null} if the requested permission is not found.
     *
     * @param jcrPath the JCR path of the corresponding JCR node
     * @return the permission by its corresponding JCR node path. Returns $
     *         {@code null} if the requested permission is not found
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl getPermission(final String jcrPath) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getPermission(jcrPath, session);
            }
        });
    }

    /**
     * Looks up the permission with the requested name for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     *
     * @param name  the name of the permission to look up
     * @param group the permission group name
     * @param site  the site key or ${@code null} if the global permissions node
     *              is requested
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl getPermission(final String name, final String group, final String site)
            throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getPermission(name, group, site, session);
            }
        });
    }


    /**
     * Looks up list of permissions with the requested name for the specified site.
     * If site is not specified considers it as a global permission. Returns $
     * {@code null} if the requested permission is not found.
     *
     * @param group the permission group name
     * @param site  the site key or ${@code null} if the global permissions node
     *              is requested
     * @return the permission with the requested name for the specified site. If
     *         site is not specified considers it as a global permission.
     *         Returns ${@code null} if the requested permission is not found.
     * @throws RepositoryException in case of an error
     */
    public List<PermissionImpl> getPermissions(final String group,final  String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<PermissionImpl>>() {
            public List<PermissionImpl> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getPermissions(group, site, session);
            }
        });
    }



    /**
     * Returns a list of permissions, defined for the specified site. If the
     * specified site is ${@code null} returns global permissions for the
     * server.
     *
     * @param site the site key to retrieve permissions for
     * @return a list of permissions, defined for the specified site. If the
     *         specified site is ${@code null} returns global permissions for
     *         the server
     * @throws RepositoryException in case of an error
     */
    public List<PermissionImpl> getPermissions(final String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<PermissionImpl>>() {
            public List<PermissionImpl> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getPermissions(site, session);
            }
        });
    }

    /**
     * Looks up the role by its JCR path. Returns ${@code null} if the requested
     * role is not found.
     *
     * @param jcrPath the JCR path of the corresponding node
     * @return the role with the requested path. Returns ${@code null} if the
     *         requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public RoleImpl getRole(final String jcrPath) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getRole(jcrPath, session);
            }
        });
    }

    /**
     * Looks up the role with the requested name for the specified site. If site
     * is not specified considers it as a global role. Returns ${@code null} if
     * the requested role is not found.
     *
     * @param name the name of the role to look up
     * @param site the site key or ${@code null} if the global permissions node
     *             is requested
     * @return the role with the requested name for the specified site. If site
     *         is not specified considers it as a global role. Returns ${@code
     *         null} if the requested role is not found.
     * @throws RepositoryException in case of an error
     */
    public RoleImpl getRole(final String name, final String site) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return getRole(name, site, session);
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
                return getRoles(site, session);
            }
        });
    }

    /**
     * Grants a permission to the specified role.
     *
     * @param roleJcrPath       the role to be modified, defined as a JCR path of the
     *                          corresponding node
     * @param permissionJcrPath permission to be granted, defined as a JCR path
     *                          of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantPermission(final String roleJcrPath, final String permissionJcrPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                grantPermission(roleJcrPath, permissionJcrPath, session);
                return true;
            }
        });
    }

    /**
     * Grants permissions to the specified role.
     *
     * @param roleJcrPath        the role to be modified, defined as a JCR path of the
     *                           corresponding node
     * @param permissionJcrPaths permissions to be granted, defined as a JCR
     *                           path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantPermissions(final String roleJcrPath, final List<String> permissionJcrPaths)
            throws RepositoryException {

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                grantPermissions(roleJcrPath, permissionJcrPaths, session);
                return true;
            }
        });
    }

    /**
     * Grants a role to the specified principal.
     *
     * @param principal   principal to grant roles to
     * @param roleJcrPath the roles to be granted, defined as a JCR path of the
     *                    corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantRole(final JahiaPrincipal principal, final String roleJcrPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                grantRole(principal, roleJcrPath, session);
                return true;
            }
        });
    }

    /**
     * Grants roles to the specified principal.
     *
     * @param principal    principal to grant roles to
     * @param roleJcrPaths the list of roles to be granted, defined as a JCR
     *                     path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void grantRoles(final JahiaPrincipal principal, final List<String> roleJcrPaths) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                grantRoles(principal, roleJcrPaths, session);
                return true;
            }
        });
    }

    /**
     * Revokes a permission from the specified role.
     *
     * @param roleJcrPath       the role to be modified, defined as a JCR path of the
     *                          corresponding node
     * @param permissionJcrPath the permission to be removed, defined as a JCR
     *                          path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void revokePermission(final String roleJcrPath, final String permissionJcrPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                revokePermission(roleJcrPath, permissionJcrPath);
                return true;
            }
        });
    }

    /**
     * Revokes permissions from the specified role.
     *
     * @param roleJcrPath        the role to be modified, defined as a JCR path of the
     *                           corresponding node
     * @param permissionJcrPaths permissions to be removed, defined as a JCR
     *                           path of the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void revokePermissions(final String roleJcrPath, final List<String> permissionJcrPaths)
            throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                revokePermissions(roleJcrPath, permissionJcrPaths);
                return true;
            }
        });
    }

    /**
     * Revokes a role from the specified principal.
     *
     * @param principal   principal to revoke the role from
     * @param roleJcrPath the role to be revoked, defined as a JCR path of the
     *                    corresponding node
     * @throws RepositoryException in case of an error
     */
    public void revokeRole(final JahiaPrincipal principal, final String roleJcrPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                revokeRole(principal, roleJcrPath, session);
                return true;
            }
        });
    }

    /**
     * Revokes roles from the specified principal.
     *
     * @param principal    principal to revoke roles from
     * @param roleJcrPaths the list of roles to revoke, defined as a JCR path of
     *                     the corresponding node
     * @throws RepositoryException in case of an error
     */
    public void revokeRoles(final JahiaPrincipal principal, final List<String> roleJcrPaths) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                revokeRoles(principal, roleJcrPaths, session);
                return true;
            }
        });
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     *
     * @param permission the permission to be stored
     * @return an updated {@link PermissionImpl} instance
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl savePermission(final PermissionImpl permission) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<PermissionImpl>() {
            public PermissionImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                savePermission(permission, session);
                return permission;
            }
        });
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     *
     * @return an updated {@link PermissionImpl} instance
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl savePermission(final String name, final String group) throws RepositoryException {
        return savePermission(new PermissionImpl(name, group));
    }

    /**
     * Creates or updates the specified {@link PermissionImpl}.
     *
     * @return an updated {@link PermissionImpl} instance
     * @throws RepositoryException in case of an error
     */
    public PermissionImpl savePermission(final String name, final String group,final String site) throws RepositoryException {
        return savePermission(new SitePermissionImpl(name, group,site));
    }

    

    /**
     * Creates or updates the specified {@link RoleImpl}.
     *
     * @param role the role to be stored
     * @return an updated {@link RoleImpl} instance
     * @throws RepositoryException in case of an error
     */
    public RoleImpl saveRole(final RoleImpl role) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<RoleImpl>() {
            public RoleImpl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                saveRole(role, session);
                return role;
            }
        });
    }
}