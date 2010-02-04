package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowOutcome extends BaseModelData implements Serializable {
    private String name;
    private String label;

    public GWTJahiaWorkflowOutcome() {
    }
    

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name",name);
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label",label);
    }

}
