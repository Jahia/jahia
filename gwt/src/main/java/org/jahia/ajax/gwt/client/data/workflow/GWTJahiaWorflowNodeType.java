package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:26:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorflowNodeType extends BaseModelData implements Serializable {
    public GWTJahiaWorflowNodeType() {
        setWorkflowDefinitions(new ArrayList<GWTJahiaWorkflowDefinition>());
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path);
    }

    public String getKey() {
        return get("key");
    }

    public void setKey(String key) {
        set("key", key);
    }

    public GWTJahiaNodeType getNodeType() {
        return get("nodeType");
    }

    public void setNodeType(GWTJahiaNodeType nodeTypes) {
        set("nodeType", nodeTypes);
    }


    public List<GWTJahiaWorkflowDefinition> getWorkflowDefinitions() {
        return get("workflowDefinitions");
    }

    public void setWorkflowDefinitions(List<GWTJahiaWorkflowDefinition> workflowDefinitions) {
        set("workflowDefinitions", workflowDefinitions);
    }
}
