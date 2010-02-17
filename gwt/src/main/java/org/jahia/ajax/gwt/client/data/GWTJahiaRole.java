package org.jahia.ajax.gwt.client.data;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT bean that represents a single role. 
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:42:48 AM
 */
public class GWTJahiaRole extends GWTJahiaRolePermissionBase {
  
    public GWTJahiaRole() {
        super();
        setPermissions(new ArrayList<GWTJahiaPermission>());
    }

    public GWTJahiaRole(String name, String site) {
        super(name, site);
        setPermissions(new ArrayList<GWTJahiaPermission>());
    }

    public List<GWTJahiaPermission> getPermissions() {
        return get("permissions");
    }

    public void setPermissions(List<GWTJahiaPermission> permissions) {
        set("permissions",permissions);
    }

    public boolean hasPermission(GWTJahiaPermission permission) {
        return getPermissions() != null && getPermissions().contains(permission);
    }
}
