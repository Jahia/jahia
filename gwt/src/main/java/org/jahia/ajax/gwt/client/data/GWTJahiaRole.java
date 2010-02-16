package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT bean that represents a single role. 
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:42:48 AM
 */
public class GWTJahiaRole extends BaseModel {
  
    public GWTJahiaRole() {
        setPermissions(new ArrayList<GWTJahiaPermission>());
    }

    public GWTJahiaRole(String id, String label) {
        this();
        setId(id);
        setLabel(label);
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getSite() {
        return get("site");
    }

    public void setSite(String site) {
        set("site",site);
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label",label);
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
