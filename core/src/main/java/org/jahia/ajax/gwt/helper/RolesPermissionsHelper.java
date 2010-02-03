package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaPermission;
import org.jahia.ajax.gwt.client.data.GWTJahiaPrincipal;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTRolesPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 4:15:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RolesPermissionsHelper {
    private static Logger logger = Logger.getLogger(RolesPermissionsHelper.class);

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
     * Get roles with permission
     *
     * @param site
     * @param server
     * @param principalKey
     * @return
     */
    public List<GWTJahiaRole> getRoles(boolean site, boolean server, String principalKey) {
        logger.debug("getRoles() ," + site + "," + server + "," + principalKey);

        List<GWTJahiaRole> roles = new ArrayList<GWTJahiaRole>();
        for (int i = 0; i < 5; i++) {
            GWTJahiaRole r = new GWTJahiaRole();
            r.setLabel("Role " + i);
            roles.add(r);
        }
        return roles;
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
     * @param site
     * @param server
     * @return
     */
    public GWTRolesPermissions getRolesAndPermissions(boolean site, boolean server) {
        logger.debug("getRolesAndPermissions() ," + site + "," + server);

        GWTRolesPermissions rp = new GWTRolesPermissions();
        rp.setRoles(getRoles(site, server, null));
        rp.setPermissions(getPermission(site));
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

}
