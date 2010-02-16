package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowActionItem extends BaseActionItem {

    @Override
    public void handleNewLinkerSelection() {
        Menu menu = new Menu();
        final GWTJahiaNode node;

        boolean isEnabled = false;

        if (linker.getSelectedNode() != null) {
            node = linker.getSelectedNode();
        } else {
            node = linker.getMainNode();
        }
        if (node != null) {
            menu.removeAll();
            GWTJahiaWorkflowInfo info = node.getWorkflowInfo();
            List<GWTJahiaWorkflowDefinition> wfs = info.getPossibleWorkflows();
            if (!node.isLocked() && node.isWriteable()) {
                for (final GWTJahiaWorkflowDefinition wf : wfs) {
                    isEnabled = true;
                    MenuItem item = new MenuItem("Start new : " + wf.getName());
                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            JahiaContentManagementService.App.getInstance().startWorkflow(node.getPath(), wf, new AsyncCallback() {
                                public void onSuccess(Object result) {
                                    Info.display("Workflow started","Workflow started");
                                    linker.refresh();
                                }

                                public void onFailure(Throwable caught) {
                                    Info.display("Workflow not started","Workflow not started");
                                }
                            }
                            );
                        }
                    });
                    menu.add(item);
                }
            }
            ViewWorkflowStatusActionItem statusActionItem = new ViewWorkflowStatusActionItem();
            statusActionItem.init(getGwtToolbarItem(), linker);
            MenuItem workflowItem = statusActionItem.getMenuItem();
            statusActionItem.updateTitle("Show Workflow Status");
            menu.add(workflowItem);

        } else {
            menu.removeAll();
        }
        Button button = (Button) getTextToolitem();

        button.setEnabled(true);
        button.setMenu(menu);


    }


}
