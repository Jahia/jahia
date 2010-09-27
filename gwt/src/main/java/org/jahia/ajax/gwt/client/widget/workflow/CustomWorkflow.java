package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;

import java.io.Serializable;
import java.util.List;

/**
 * Custom GUI interface for Workflow dialogs
 */
public interface CustomWorkflow extends Serializable, IsSerializable {
    public List<TabItem> getAdditionalTabs();

    public Button getStartWorkflowButton(GWTJahiaWorkflowDefinition wf, WorkflowActionDialog dialog);

    public Button getBypassWorkflowButton(final GWTJahiaWorkflowDefinition wf, final WorkflowActionDialog dialog);

}
