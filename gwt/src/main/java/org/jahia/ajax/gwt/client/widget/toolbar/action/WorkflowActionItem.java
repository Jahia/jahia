package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowActionItem extends BaseActionItem {
    private List<String> processes;
    private boolean bypassWorkflow;
    private ActionItem bypassActionItem;

    public WorkflowActionItem() {
    }

    public WorkflowActionItem(List<String> processes, boolean bypassWorkflow, ActionItem bypassActionItem) {
        this.processes = processes;
        this.bypassWorkflow = bypassWorkflow;
        this.bypassActionItem = bypassActionItem;
    }

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
            if (!node.isLanguageLocked(node.getLanguageCode()) && node.isWriteable()) {
                for (final GWTJahiaWorkflowDefinition wf : wfs) {
                    if (processes == null || processes.contains(wf.getId())) {
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
                if (bypassWorkflow) {
                    MenuItem item = new MenuItem("Bypass workflow");
                    item.addSelectionListener( new SelectionListener<MenuEvent>() {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            bypassActionItem.onComponentSelection();
                        }
                    });
                    menu.add(item);
                }
            }
            if (processes == null) {
                isEnabled = true;
                ViewWorkflowStatusActionItem statusActionItem = new ViewWorkflowStatusActionItem();
                statusActionItem.init(getGwtToolbarItem(), linker);
                MenuItem workflowItem = statusActionItem.getMenuItem();
                statusActionItem.updateTitle("Show Workflow Status");
                menu.add(workflowItem);
            }
        } else {
            menu.removeAll();
        }

        if (isEnabled) {
            setEnabled(true);
        }
        if (bypassActionItem != null) {
            bypassActionItem.handleNewLinkerSelection();
        }
        if (!isEnabled) {
            setEnabled(false);
        }

        if (isTextToolItem()) {
            Button button = (Button) getTextToolItem();
            button.setMenu(menu);
        }

        if (isMenuItem()) {
            MenuItem mi = getMenuItem();
            mi.setSubMenu(menu);
        }

    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (bypassActionItem != null) {
            bypassActionItem.init(gwtToolbarItem, linker);
        }
    }

    @Override
    public Component getTextToolItem() {
        final Component item = super.getTextToolItem();
        if (bypassActionItem != null) {
            bypassActionItem.setTextToolitem(item);
        }
        return item;
    }

    @Override
    public MenuItem getMenuItem() {
        final MenuItem item = super.getMenuItem();
        if (bypassActionItem != null) {
            bypassActionItem.setMenuItem(item);
        }
        return item;
    }

    @Override
    public MenuItem getContextMenuItem() {
        final MenuItem item = super.getContextMenuItem();
        if (bypassActionItem != null) {
            bypassActionItem.setContextMenuItem(item);
        }
        return item;
    }
}
