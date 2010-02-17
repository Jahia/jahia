package org.jahia.ajax.gwt.client.data;


/**
 * GWT bean that represents single permission.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:43:21 AM
 */
public class GWTJahiaPermission extends GWTJahiaRolePermissionBase {

    public GWTJahiaPermission() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name
     * @param group
     * @param site
     */
    public GWTJahiaPermission(String name, String group, String site) {
        super(name, site);
        setGroup(group);
    }

    
    public String getGroup() {
        return get("group");
    }

    public void setGroup(String g) {
        set("group",g);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaPermission that = (GWTJahiaPermission) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getGroup() != null ? !getGroup().equals(that.getGroup()) : that.getGroup() != null) return false;        
        if (getSite() != null ? !getSite().equals(that.getSite()) : that.getSite() != null) return false;        

        return true;
    }
}
