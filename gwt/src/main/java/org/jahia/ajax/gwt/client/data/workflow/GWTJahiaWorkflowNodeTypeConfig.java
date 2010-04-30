package org.jahia.ajax.gwt.client.data.workflow;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 30, 2010
 * Time: 7:15:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowNodeTypeConfig implements Serializable {
    private List<GWTJahiaWorkflowDefinition> workflowDefinitions;
    private List<GWTJahiaNodeType> contentTypeList;
    private List<GWTJahiaWorflowNodeType> worflowNodeTypes;

    public List<GWTJahiaWorkflowDefinition> getWorkflowDefinitions() {
        return workflowDefinitions;
    }

    public void setWorkflowDefinitions(List<GWTJahiaWorkflowDefinition> workflowDefinitions) {
        this.workflowDefinitions = workflowDefinitions;
    }

    public List<GWTJahiaNodeType> getContentTypeList() {
        return contentTypeList;
    }

    public void setContentTypeList(List<GWTJahiaNodeType> contentTypeList) {
        this.contentTypeList = contentTypeList;
    }

    public List<GWTJahiaWorflowNodeType> getWorflowNodeTypes() {
        return worflowNodeTypes;
    }

    public void setWorflowNodeTypes(List<GWTJahiaWorflowNodeType> worflowNodeTypes) {
        this.worflowNodeTypes = worflowNodeTypes;
    }
}
