package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaPrincipal;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.rbac.impl.PermissionImpl;
import org.jahia.services.rbac.impl.RoleImpl;
import org.jahia.services.rbac.impl.RoleManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;

/**
 * Roles and permission GWT helper class.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 4:15:49 PM
 */
public class RolesPermissionsHelper {
    private static Logger logger = Logger.getLogger(RolesPermissionsHelper.class);
    
    private RoleManager roleManager;

    /**
     * Get permissions
     *
     * @param site if false, returns server permissions
     * @return
     */
    public List<GWTJahiaPermission> getPermission(boolean site) {
        logger.debug("getPermission() ," + site);
        List<GWTJahiaPermission> data = new ArrayList<GWTJahiaPermission>();


        GWTJahiaPermission p = new GWTJahiaPermission();
        p.setGroup("Admin");
        p.setId("Role " + 0);
        p.setLabel("Can enter Jahia administration GUI ");
        data.add(p);

        p = new GWTJahiaPermission();
        p.setGroup("Admin");
        p.setId("Role " + 1);
        p.setLabel("Page settings ");
        data.add(p);

        p = new GWTJahiaPermission();
        p.setGroup("Admin");
        p.setId("Role " + 2);
        p.setLabel("Manage templates");
        data.add(p);

        p = new GWTJahiaPermission();
        p.setGroup("Edit");
        p.setId("Role " + 3);
        p.setLabel("Content edition");
        data.add(p);

        p = new GWTJahiaPermission();
        p.setGroup("Edit");
        p.setId("Role " + 4);
        p.setLabel("Time-based publishing");
        data.add(p);
        return data;
    }

    /**
     * Get all roles for the specified site or for the server if the site is not specified. 
     *
     * @param site target site key or {@code null} if the server level roles are requested
     * @param jcrSession current JCR session
     * @return all roles for the specified site or for the server if the site is not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaRole> getRoles(String site, JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving roles for " + (site != null ? "site " + site : "server"));
        }

        List<GWTJahiaRole> roles = new LinkedList<GWTJahiaRole>();
        try {
            for(RoleImpl role : roleManager.getRoles(site, jcrSession)) {
                roles.add(toRole(role));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return roles;
    }

    /**
     * Get all permissions for the specified site or for the server if the site
     * is not specified.
     * 
     * @param site target site key or {@code null} if the server level
     *            permissions are requested
     * @param jcrSession current JCR session
     * @return all permissions for the specified site or for the server if the
     *         site is not specified
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaPermission> getPermissions(String site, JCRSessionWrapper jcrSession)
            throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving permissions for " + (site != null ? "site " + site : "server"));
        }

        List<GWTJahiaPermission> permissions = new LinkedList<GWTJahiaPermission>();
        try {
            for (PermissionImpl permission : roleManager.getPermissions(site, jcrSession)) {
                permissions.add(toPermission(permission));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return permissions;
    }

    /**
     * Get principal in role
     *
     * @param role
     * @return
     */
    public List<GWTJahiaPrincipal> getPrincipalsInRole(GWTJahiaRole role) {
        logger.debug("getPrincipalsInRole() ," + role);

        List<GWTJahiaPrincipal> p = new ArrayList<GWTJahiaPrincipal>();
        return p;
    }


    /**
     * Get all roles and all permissions
     *
     * @param site the site key to retrieve roles and permissions
     * @param jcrSession current user JCR session
     * @return
     * @throws GWTJahiaServiceException in case of an error
     */
    public GWTRolesPermissions getRolesAndPermissions(String site, JCRSessionWrapper jcrSession) throws GWTJahiaServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("getting roles and permission for " + (site != null ? "site " + site : "server"));
        }

        GWTRolesPermissions rp = new GWTRolesPermissions();
        rp.setRoles(getRoles(site, jcrSession));
        rp.setPermissions(getPermissions(site, jcrSession));
        return rp;
    }

    /**
     * add permission to role
     *
     * @param role
     */
    public void addRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions) {
        logger.debug("addRolePermissions() ," + role + "," + permissions);

    }

    /**
     * remove permissin from role
     *
     * @param role
     * @param permissions
     */
    public void removeRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions) {
        logger.debug("removeRolePermissions() ," + role + "," + permissions);

    }

    /**
     * Grant role toe users
     *
     * @param role
     */
    public void grantRoleToUser(GWTJahiaRole role, String principalKey) {
        logger.debug("grantRoleToUser() ," + role + "," + principalKey);

    }

    /**
     * Remove role to user
     *
     * @param role
     */
    public void removeRoleToUser(GWTJahiaRole role, String principalKey) {
        logger.debug("removeRoleToUser() ," + role + "," + principalKey);
    }

    /**
     * Grant role to principals
     * @param role
     * @param principals
     */
    public void grantRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals) {
        logger.debug("grantRoleToPrincipals() ," + role + "," + principals);
    }

    /**
     * REmove role to principals
     * @param role
     * @param principals
     */
    public void removeRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals) {
        logger.debug("removeRoleToPrincipals() ," + role + "," + principals);
    }

    private GWTJahiaPermission toPermission(PermissionImpl permission) {
        GWTJahiaPermission gwtPermission = new GWTJahiaPermission();
        gwtPermission.setId(permission.getPath());
        gwtPermission.setLabel(permission.getTitle() != null ? permission.getTitle() : permission.getName());
        gwtPermission.setGroup(permission.getGroup());

        return gwtPermission;
    }

    private GWTJahiaRole toRole(RoleImpl role) {
        GWTJahiaRole gwtRole = new GWTJahiaRole();
        gwtRole.setId(role.getPath());
        gwtRole.setLabel(role.getTitle() != null ? role.getTitle() : role.getName());
        List<GWTJahiaPermission> gwtPermissions = new LinkedList<GWTJahiaPermission>();
        for (PermissionImpl perm : role.getPermissions()) {
            gwtPermissions.add(toPermission(perm));
        }
        gwtRole.setPermissions(gwtPermissions);

        return gwtRole;
    }

    /**
     * Injects the role management service instance.
     * 
     * @param roleManager the role management service instance
     */
    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
}