package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 14, 2010
 * Time: 12:29:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowType  extends BaseModelData implements Serializable {

    public GWTJahiaWorkflowType() {
    }

    public GWTJahiaWorkflowType(String name) {
        setName(name);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public void setDisplayName(String displayName) {
        set("displayName", displayName);
    }

    public String getDisplayName() {
        return get("displayName");
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (super.equals(o) || ((GWTJahiaWorkflowType) o).getName().equals(getName()));
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }



}
