package org.jahia.ajax.gwt.client.data.workflow;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 3:50:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowInfo implements Serializable {
    private List<GWTJahiaWorkflowDefinition> possibleWorkflows;
    private List<GWTJahiaWorkflowAction> availableActions;

    public GWTJahiaWorkflowInfo() {
    }

    public List<GWTJahiaWorkflowDefinition> getPossibleWorkflows() {
        return possibleWorkflows;
    }

    public void setPossibleWorkflows(List<GWTJahiaWorkflowDefinition> possibleWorkflows) {
        this.possibleWorkflows = possibleWorkflows;
    }

    public List<GWTJahiaWorkflowAction> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List<GWTJahiaWorkflowAction> availableActions) {
        this.availableActions = availableActions;
    }
}
