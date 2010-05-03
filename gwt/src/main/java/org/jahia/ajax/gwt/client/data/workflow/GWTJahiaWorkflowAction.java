package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowAction extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowOutcome> outcomes;
    private List<GWTJahiaWorkflowTaskComment> taskComments;
    private Map<String, GWTJahiaNodeProperty> properties;

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

    public void setTaskComments(List<GWTJahiaWorkflowTaskComment> taskComments) {
        this.taskComments = taskComments;
    }

    public List<GWTJahiaWorkflowTaskComment> getTaskComments() {
        return taskComments;
    }

    public void setFormResourceName(String formResourceName) {
        set("formResourceName",formResourceName);
    }

    public String getFormResourceName() {
        return get("formResourceName");
    }

    public void setVariables(Map<String, GWTJahiaNodeProperty> properties) {
        this.properties = properties;
    }

    public Map<String, GWTJahiaNodeProperty> getVariables() {
        return properties;
    }

    public void setCreateTime(Date createTime) {
        set("createTime",createTime);
    }
    
    public Date getCreateTime() {
        return get("createTime");
    }

    public void setLocale(String locale) {
        set("locale",locale);
    }

    public String getLocale() {
        return get("locale");
    }

    public void setWorkspace(String workspace) {
        set("workspace",workspace);
    }

    public String getWorkspace() {
        return get("workspace");
    }

    public void setProcessId(String processId) {
        set("processId",processId);
    }

    public String getProcessId() {
        return get("processId");
    }
}
