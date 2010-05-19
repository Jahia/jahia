package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 29, 2010
 * Time: 9:41:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowDashboardActionItem extends BaseActionItem{

    public void onComponentSelection() {
        EditActions.showWorkflowDashboard(linker);
    }

    public void handleNewLinkerSelection() {
        final GWTJahiaNode gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode != null) {
            setEnabled(gwtJahiaNode.isWriteable());
        }
    }

}