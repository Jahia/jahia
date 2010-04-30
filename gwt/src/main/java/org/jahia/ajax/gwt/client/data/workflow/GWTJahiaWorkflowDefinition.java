package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowDefinition extends BaseModelData implements Serializable {
    public GWTJahiaWorkflowDefinition() {
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider",provider);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name",name);
    }

    public String getFormResourceName() {
        return get("formResourceName");
    }

    public void setFormResourceName(String formResourceName) {
        set("formResourceName", formResourceName);
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        return o!=null &&  (super.equals(o) || ((GWTJahiaWorkflowDefinition)o).getName().equals(getName()));  
    }
}
