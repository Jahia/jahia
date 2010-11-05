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

package org.jahia.ajax.gwt.helper;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.rbac.Role;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.rbac.jcr.PermissionImpl;
import org.jahia.services.rbac.jcr.RoleBasedAccessControlService;
import org.jahia.services.rbac.jcr.RoleImpl;
import org.jahia.services.rbac.jcr.RoleService;
import org.jahia.services.usermanager.*;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Roles and permission GWT helper class.
 *
 * @author ktlili
 * Date: Feb 3, 2010
 * Time: 4:15:49 PM
 */
public class RolesPermissionsHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RolesPermissionsHelper.class);

    private JahiaGroupManagerService groupManagerService;
    private RoleBasedAccessControlService rbacService;
    private RoleService roleService;
    private JahiaUserManagerService userManagerService;

    /**
     * Grants the specified permissions to a role.
     * 
     * @param role the role to grant permissions
     * @param gwtPermissions permissions to be granted
     * @throws GWTJahiaServiceException in case of an error
     */
    public void addRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> gwtPermissions)
            throws GWTJahiaServiceException {
        List<Permission> permissions = new LinkedList<Permission>();
        for (GWTJahiaPermission perm : gwtPermissions) {
            permissions.add(new PermissionIdentity(perm.getName(), perm.getGroup(), perm.getSite()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("addRolePermissions() ," + role.getName() + "," + permissions);
        }
        try {
            roleService.grantPermissions(new RoleIdentity(role.getName(), role.getSite()), permissions);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaPermission createPermission(String name, String group, String siteKey)
            throws GWTJahiaServiceException {
        try {
            return toPermission(roleService.savePermission(new PermissionIdentity(name, group, siteKey)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Get list of granted permissions
     *
     * @return
     */
    public List<GWTJahiaPermission> getGrantedPermissions(final JCRSiteNode site, JahiaUser user)
            throws GWTJahiaServiceException {
        final List<GWTJahiaPermission> permissions = new LinkedList<GWTJahiaPermission>();
        try {
            long timer = System.currentTimeMillis();
            if (!user.isRoot() && !JahiaUserManagerService.isGuest(user)) {
                Set<PermissionImpl> foundPermissions = new HashSet<PermissionImpl>();
                timer = System.currentTimeMillis();
                Set<Role> roles = rbacService.getRoles(user);
                for (Role roleId : roles) {
                    Set<PermissionImpl> rolePermissions = roleService.getRole(roleId).getPermissions();
                    for (PermissionImpl perm : rolePermissions) {
                        if (!foundPermissions.contains(perm)) {
                            foundPermissions.add(perm);
                            permissions.add(toPermission(perm));
                        }
                    }
                }
            } else {
                // site permission
                if (site != null) {
                    for (PermissionImpl permission : roleService.getPermissions(site.getSiteKey())) {
                        if (user.isPermitted(permission)) {
                            permissions.add(toPermission(permission));
                        }
                    }
                }
    
                // server permission
                for (PermissionImpl permission : roleService.getPermissions(null)) {
                    if (user.isPermitted(permission)) {
                        permissions.add(toPermission(permission));
                    }
                }
            }
            if (logger.isDebugEnabled()) {
            	logger.debug("Checking user granted permissions took " + (System.currentTimeMillis() - timer) + " ms");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return permissions;
    }

    /**
     * Get all permissions for the specified site or for the server if the site
     * is not specified.
     * 
     * @param site target site key or {@code null} if the server level
     *            permissions are requested
     * @return all permissions for the specified site or for the server if the
     *         site is not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    private List<GWTJahiaPermission> getPermissions(String site)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving permissions for " + (site != null ? "site " + site : "server"));
        }

        List<GWTJahiaPermission> permissions = new LinkedList<GWTJahiaPermission>();
        try {
            for (PermissionImpl permission : roleService.getPermissions(site)) {
                permissions.add(toPermission(permission));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return permissions;
    }

    public List<GWTJahiaPrincipal> getPrincipalsInRole(GWTJahiaRole role) throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("getPrincipalsInRole() ," + role.getName());
        }

        List<GWTJahiaPrincipal> p = new LinkedList<GWTJahiaPrincipal>();
        try {
            for (JahiaPrincipal jahiaPrincipal : rbacService.getPrincipalsInRole(new RoleIdentity(role.getName(), role.getSite()))) {
                GWTJahiaPrincipal gwtPrincipal = null;
                if (jahiaPrincipal instanceof JahiaUser) {
                    JahiaUser user = (JahiaUser) jahiaPrincipal;
                    GWTJahiaUser gwtUser = new GWTJahiaUser(user.getUsername(), user.getUserKey());
                    gwtUser.setProvider(user.getProviderName());
                    gwtPrincipal = gwtUser;
                } else if (jahiaPrincipal instanceof JahiaGroup) {
                    JahiaGroup group = (JahiaGroup) jahiaPrincipal;
                    GWTJahiaGroup gwtGroup = new GWTJahiaGroup(group.getGroupname(), group.getGroupKey());
                    gwtGroup.setProvider(group.getProviderName());
                    gwtPrincipal = gwtGroup;
                } else {
                    logger.warn("Unknown principal type for principal " + jahiaPrincipal);
                }
                if (gwtPrincipal != null) {
                    p.add(gwtPrincipal);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        
        return p;
    }

    /**
     * Get all roles for the specified site or for the server if the site is not
     * specified.
     * 
     * @param site target site key or {@code null} if the server level roles are
     *            requested
     * @return all roles for the specified site or for the server if the site is
     *         not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    private List<GWTJahiaRole> getRoles(String site,String search) throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving roles for " + (site != null ? "site " + site : "server"));
        }

        List<GWTJahiaRole> roles = new LinkedList<GWTJahiaRole>();
        try {
            for (RoleImpl role : roleService.getRoles(site)) {
                if("*".equals(search))
                    roles.add(toRole(role));
                else if(role.getName().matches(search)){
                    roles.add(toRole(role));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return roles;
    }

    public List<GWTJahiaRole> getRoles(String siteKey, boolean isGroup, String principalKey)
            throws GWTJahiaServiceException {
        // get all roles for the site first
        List<GWTJahiaRole> roles =  getRoles(siteKey,"*");
        
        // add server-level roles
        roles.addAll(getRoles(null,"*"));


        for(GWTJahiaRole role: roles){
            // add the check to know of the role is granted or not to the principal
            if(isGrant(role,isGroup,principalKey)){
                role.set("grant","true");
            }
        }

        return roles;
    }

    /**
     * Get all roles and all permissions
     * 
     * @param site the site key to retrieve roles and permissions
     * @return all roles and all permissions
     * @throws GWTJahiaServiceException in case of an error
     */
    public GWTRolesPermissions getRolesAndPermissions(String site)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("getting roles and permission for " + (site != null ? "site " + site : "server"));
        }

        GWTRolesPermissions rp = new GWTRolesPermissions();
        rp.setRoles(getRoles(site,"*"));
        rp.setPermissions(getPermissions(site));
        return rp;
    }

    public void grantRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals)
            throws GWTJahiaServiceException {
        List<JahiaPrincipal> subjects = new LinkedList<JahiaPrincipal>();
        for (GWTJahiaPrincipal principal : principals) {
            subjects.add(lookupPrincipal(principal));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("grantRoleToPrincipals() ," + role.getName() + "," + principals);
        }
        RepositoryException ex = null;
        for (JahiaPrincipal jahiaPrincipal : subjects) {
            try {
                rbacService.grantRole(jahiaPrincipal, new RoleIdentity(role.getName(), role.getSite()));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                ex = e;
            }
        }
        if (ex != null) {
            throw new GWTJahiaServiceException(ex.getMessage());
        }
        
    }

    public void grantRoleToUser(GWTJahiaRole role, boolean isGroup, String principalKey)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("grantRoleToUser() ," + role + "," + principalKey);
        }
        try {
            rbacService.grantRole(lookupPrincipal(principalKey, isGroup), new RoleIdentity(role.getName(), role
                    .getSite()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Check if the role is granted to the user
     * 
     * @param role
     * @param isGroup
     * @param principalKey
     * @return
     */
    public boolean isGrant(GWTJahiaRole role, boolean isGroup, String principalKey) {
        JahiaPrincipal p = lookupPrincipal(principalKey, isGroup);
        return p != null && p.hasRole(new RoleIdentity(role.getName(), role.getSite()));
    }

    private JahiaPrincipal lookupPrincipal(GWTJahiaPrincipal principal) {
        if (principal instanceof GWTJahiaUser) {
            return userManagerService.lookupUserByKey(((GWTJahiaUser) principal).getKey());
        } else if (principal instanceof GWTJahiaGroup) {
            return groupManagerService.lookupGroup(((GWTJahiaGroup) principal).getGroupKey());
        } else {
            logger.warn("Unknown principal type " + principal);
        }
        return null;
    }

    private JahiaPrincipal lookupPrincipal(String principalKey, boolean isGroup) {
        return isGroup ? groupManagerService.lookupGroup(principalKey) : userManagerService.lookupUserByKey(principalKey);
    }

    /**
     * Revokes specified permissions from a role.
     * 
     * @param role the role to revoke permissions from
     * @param gwtPermissions permissions to be revoked
     * @throws GWTJahiaServiceException in case of an error
     */
    public void removeRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> gwtPermissions) throws GWTJahiaServiceException {
        List<Permission> permissions = new LinkedList<Permission>();
        for (GWTJahiaPermission perm : gwtPermissions) {
            permissions.add(new PermissionIdentity(perm.getName(), perm.getGroup(), perm.getSite()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("removeRolePermissions() ," + role.getName() + "," + permissions);
        }
        try {
            roleService.revokePermissions(new RoleIdentity(role.getName(), role.getSite()), permissions);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void removeRoleToPrincipal(GWTJahiaRole role, boolean isGroup, String principalKey) throws GWTJahiaServiceException {
        JahiaPrincipal p = lookupPrincipal(principalKey, isGroup);
        if (p != null) {
            try {
                rbacService.revokeRole(p, new RoleIdentity(role.getName(), role.getSite()));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(e.getMessage()); 
            }
        }
    }

    /**
     * REmove role to principals
     * 
     * @param role
     * @param principals
     * @throws GWTJahiaServiceException in case of an error
     */
    public void removeRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals) throws GWTJahiaServiceException {
        List<JahiaPrincipal> subjects = new LinkedList<JahiaPrincipal>();
        for (GWTJahiaPrincipal principal : principals) {
            subjects.add(lookupPrincipal(principal));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("removeRoleToPrincipals() ," + role.getName() + "," + principals);
        }
        RoleIdentity roleId = new RoleIdentity(role.getName(), role.getSite());
        RepositoryException ex = null;
        for (JahiaPrincipal jahiaPrincipal : subjects) {
            try {
                rbacService.revokeRole(jahiaPrincipal, roleId);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                ex = e;
            }
        }
        if (ex != null) {
            throw new GWTJahiaServiceException(ex.getMessage());
        }
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    /**
     * @param roleBasedAccessControlService the roleBasedAccessControlService to set
     */
    public void setRoleBasedAccessControlService(RoleBasedAccessControlService roleBasedAccessControlService) {
        this.rbacService = roleBasedAccessControlService;
    }

    /**
     * Injects the role management service instance.
     * 
     * @param roleService the role management service instance
     */
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    private GWTJahiaPermission toPermission(PermissionImpl permission) {
        return new GWTJahiaPermission(permission.getName(), permission.getGroup(), permission.getSite());
    }

    private GWTJahiaRole toRole(RoleImpl role) {
        GWTJahiaRole gwtRole = new GWTJahiaRole(role.getName(), role.getSite());
        List<GWTJahiaPermission> gwtPermissions = new LinkedList<GWTJahiaPermission>();
        for (PermissionImpl perm : role.getPermissions()) {
            gwtPermissions.add(toPermission(perm));
        }
        gwtRole.setPermissions(gwtPermissions);

        return gwtRole;
    }

    public PagingLoadResult<GWTJahiaRole> searchRolesInContext(String search, int offset, int limit, String context,
                                                               JCRSiteNode currentSite)
            throws GWTJahiaServiceException {
        if (context != null) {
            String site = null;
            if (context.equals("currentSite")) {
                site = currentSite.getSiteKey();
            } else if (context.startsWith("site:")) {
                site = context.substring(5);
            }
                List<GWTJahiaRole> result = getRoles(site,search);
                int size = result.size();
                result = new ArrayList<GWTJahiaRole>(result.subList(offset, Math.min(size, offset + limit)));
                return new BasePagingLoadResult<GWTJahiaRole>(result, offset, size);
        }
        return null;
    }
}