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

import java.util.LinkedList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.EnforcementPolicy;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.Role;
import org.jahia.services.rbac.EnforcementPolicy.EnforcementPolicyResult;
import org.jahia.services.rbac.EnforcementPolicy.GrantAllEnforcementPolicy;
import org.jahia.services.usermanager.JahiaPrincipal;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlService {

    private static Logger logger = Logger.getLogger(RoleBasedAccessControlService.class);

    private EnforcementPolicy policy = new GrantAllEnforcementPolicy();

    private RoleBasedAccessControlManager rbacManager;

    /**
     * Returns a list of principals having the specified permission or an empty
     * list if the permission is not granted to anyone.
     * 
     * @param permission the permission to check for
     * @return a list of principals having the specified permission or an empty
     *         list if the permission is not granted to anyone
     * @throws RepositoryException in case of an error
     */
    public List<JahiaPrincipal> getPrincipalsInPermission(final Permission permission) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<JahiaPrincipal>>() {
            public List<JahiaPrincipal> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return rbacManager.getPrincipalsInPermission(permission, session);
            }
        });
    }

    /**
     * Returns a list of principals having the specified role or an empty list
     * if the role is not granted to anyone.
     * 
     * @param role the role
     * @return a list of principals having the specified role or an empty list
     *         if the role is not granted to anyone
     * @throws RepositoryException in case of an error
     */
    public List<JahiaPrincipal> getPrincipalsInRole(final Role role) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<JahiaPrincipal>>() {
            public List<JahiaPrincipal> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return rbacManager.getPrincipalsInRole(role, session);
            }
        });
    }

    /**
     * Grants a role to the specified principal.
     * 
     * @param principal principal to grant the role to
     * @param role the role to be granted
     * @throws RepositoryException in case of an error
     */
    public void grantRole(final JahiaPrincipal principal, final Role role) throws RepositoryException {
        List<Role> granted = new LinkedList<Role>();
        granted.add(role);
        grantRoles(principal, granted);
    }

    /**
     * Grants roles to the specified principal.
     * 
     * @param principal principal to grant roles to
     * @param roles the list of roles to be granted
     * @throws RepositoryException in case of an error
     */
    public void grantRoles(final JahiaPrincipal principal, final List<Role> roles) throws RepositoryException {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                rbacManager.grantRoles(principal, roles, session);
                return true;
            }
        });
    }

    /**
     * Returns {@code true} if this principal has the specified role, {@code
     * false} otherwise.
     * 
     * @param principal the principal to check for role
     * @param role the application-specific role identifier
     * @return {@code true} if this principal has the specified role, {@code
     *         false} otherwise
     * @throws RepositoryException in case of an error
     */
    public boolean hasRole(final JahiaPrincipal principal, final Role role) {
        EnforcementPolicyResult result = policy.enforce(principal);
        if (result.isApplied()) {
            return result.getResult();
        }

        boolean hasIt = false;

        try {
            hasIt = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws PathNotFoundException, RepositoryException {
                    return rbacManager.hasRole(principal, role, session);
                }
            });
        } catch (PathNotFoundException e) {
            logger.debug("Corresponding node cannot be found for role: " + role);
            hasIt = policy.isPermitNonExistingRoles();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasIt;
    }

    /**
     * Returns {@code true} if this principal is permitted to perform an action
     * or access a resource summarized by the specified permission string.
     * 
     * @param principal the principal to check for permission
     * @param permission the identifier of a permission that is being checked
     * @return {@code true} if this principal is permitted to perform an action
     *         or access a resource summarized by the specified permission
     */
    public boolean isPermitted(final JahiaPrincipal principal, final Permission permission) {
        EnforcementPolicyResult result = policy.enforce(principal);
        if (result.isApplied()) {
            return result.getResult();
        }

        boolean hasIt = false;
        try {
            hasIt = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return rbacManager.isPermitted(principal, permission, session);
                }
            });
        } catch (PathNotFoundException e) {
            logger.debug("Corresponding node cannot be found for permission: " + permission);
            hasIt = policy.isPermitNonExistingPermissions();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasIt;
    }

    /**
     * Revokes all roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @throws RepositoryException in case of an error
     */
    public void revokeAllRoles(final JahiaPrincipal principal) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                rbacManager.revokeAllRoles(principal, session);
                return true;
            }
        });
    }

    /**
     * Revokes a role from the specified principal.
     * 
     * @param principal principal to revoke the role from
     * @param role the role to be revoked
     * @throws RepositoryException in case of an error
     */
    public void revokeRole(final JahiaPrincipal principal, final Role role) throws RepositoryException {
        List<Role> revoked = new LinkedList<Role>();
        revoked.add(role);
        revokeRoles(principal, revoked);
    }

    /**
     * Revokes roles from the specified principal.
     * 
     * @param principal principal to revoke roles from
     * @param roles the list of roles to be revoked
     * @throws RepositoryException in case of an error
     */
    public void revokeRoles(final JahiaPrincipal principal, final List<Role> roles) throws RepositoryException {
        if (roles == null || roles.isEmpty()) {
            return;
        }

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                rbacManager.revokeRoles(principal, roles, session);
                return true;
            }
        });
    }

    /**
     * Injects the enforcement policy to use.
     * 
     * @param policy the policy to set
     */
    protected void setEnformcementPolicy(EnforcementPolicy policy) {
        this.policy = policy;
    }

    /**
     * Injects the dependency to {@link RoleBasedAccessControlManager}.
     * 
     * @param roleBasedAccessControlManager the dependency to
     *            {@link RoleBasedAccessControlManager}
     */
    public void setRoleBasedAccessControlManager(RoleBasedAccessControlManager roleBasedAccessControlManager) {
        this.rbacManager = roleBasedAccessControlManager;
    }

}
