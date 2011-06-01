package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 9, 2010
 * Time: 5:29:14 PM
 * 
 */
public class GWTJahiaWorkflow extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowTask> availableTasks;
    private Map<String, GWTJahiaNodeProperty> variables;
    private CustomWorkflow customWorkflowInfo;

    public GWTJahiaWorkflow() {
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

    public GWTJahiaNode getNode() {
        return get("node");
    }

    public void setNode(GWTJahiaNode node) {
        set("node",node);
    }

    public Date getStartTime() {
        return get("startTime");
    }

    public void setStartTime(Date startTime) {
        set("startTime",startTime);
    }

    public GWTJahiaWorkflowDefinition getDefinition() {
        return get("definition");
    }

    public void setDefinition(GWTJahiaWorkflowDefinition definition) {
        set("definition",definition);
    }

    public Date getDuedate() {
        return get("duedate");
    }

    public void setDuedate(Date duedate) {
        set("duedate",duedate);
    }

    public void setVariables(Map<String, GWTJahiaNodeProperty> variables) {
        this.variables = variables;
    }

    public Map<String, GWTJahiaNodeProperty> getVariables() {
        return variables;
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

    public List<GWTJahiaWorkflowTask> getAvailableTasks() {
        return availableTasks;
    }

    public void setAvailableTasks(List<GWTJahiaWorkflowTask> availableTasks) {
        this.availableTasks = availableTasks;
    }

    public CustomWorkflow getCustomWorkflowInfo() {
        return customWorkflowInfo;
    }

    public void setCustomWorkflowInfo(CustomWorkflow customWorkflowInfo) {
        this.customWorkflowInfo = customWorkflowInfo;
    }
}
