package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowAction extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowOutcome> outcomes;

    public GWTJahiaWorkflowAction() {
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name",name);
    }

    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider",provider);
    }
    
    public List<GWTJahiaWorkflowOutcome> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<GWTJahiaWorkflowOutcome> outcomes) {
        this.outcomes = outcomes;
    }
}
