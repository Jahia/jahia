package org.jahia.ajax.gwt.client.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 11:57:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTRolesPermissions implements Serializable {
    private List<GWTJahiaRole> roles;
    private List<GWTJahiaPermission> permissions;

    public GWTRolesPermissions() {

    }

    public List<GWTJahiaRole> getRoles() {
        return roles;
    }

    public void setRoles(List<GWTJahiaRole> roles) {
        this.roles = roles;
    }

    public List<GWTJahiaPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<GWTJahiaPermission> permissions) {
        this.permissions = permissions;
    }


}
