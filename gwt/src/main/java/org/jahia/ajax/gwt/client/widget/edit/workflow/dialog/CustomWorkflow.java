package org.jahia.ajax.gwt.client.widget.edit.workflow.dialog;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 10, 2010
 * Time: 3:41:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CustomWorkflow {
    public List<TabItem> getAdditionalTabs();

    public Button getStartWorkflowButton(GWTJahiaWorkflowDefinition wf, WorkflowActionDialog dialog);
}
