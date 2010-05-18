package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Side panel tab item for browsing the pages tree.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:37 PM
 */
public class WorkflowTabItem extends SidePanelTabItem {
    private ListStore<GWTJahiaNode> contentStore;
    private Grid<GWTJahiaNode> grid;
    private ColumnModel cm;
    private LayoutContainer contentContainer;
    private JahiaContentManagementServiceAsync jahiaContentManagementServiceAsync;
    private boolean isInitialized;

    public WorkflowTabItem(GWTSidePanelTab c) {
        super(c);
        setIconStyle("gwt-toolbar-icon-workflowaction-min");
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(new FitLayout());
        jahiaContentManagementServiceAsync = JahiaContentManagementService.App.getInstance();
        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());
        contentStore = new ListStore<GWTJahiaNode>();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();
        displayColumns.add(new ColumnConfig("displayName", Messages.getResource("label.name"), 150));
        ColumnConfig config = new ColumnConfig("workflowStatus", Messages.getResource("label.status"), 100);
        config.setRenderer(new GridCellRenderer() {
            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                Menu menu = new Menu();
                final GWTJahiaNode node = (GWTJahiaNode) model;
                String actionName = null;
                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
                for (final GWTJahiaWorkflowAction action : actions) {
                    if (actionName == null) {
                        actionName = action.getName();
                    }
                    MenuItem item = new MenuItem(action.getName());
                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            new WorkflowActionDialog(node,action).show();
                        }
                    });
                    menu.add(item);

                }
                Button splitButton = new Button(actionName);
                splitButton.setMenu(menu);
                return splitButton;
            }
        });
        displayColumns.add(config);
        cm = new ColumnModel(displayColumns);
        grid = new Grid<GWTJahiaNode>(contentStore, cm);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                List<Module> moduleList = ModuleHelper.getModules();
                for (Module module : moduleList) {
                    if (module.getPath().equals(se.getSelectedItem().getPath())) {
                        editLinker.select(module);
                    }
                }
            }
        });
        grid.getStore().setMonitorChanges(true);
        contentContainer.add(grid);
        add(contentContainer);
        isInitialized = false;
    }

    public void initList(Module selectedModule) {
        if (!isInitialized) {
            fillStore();
            layout();
            isInitialized = true;
        }
    }

    private void fillStore() {

        contentStore.removeAll();
        List<Module> modules = ModuleHelper.getModules();
        List<String> list = new ArrayList<String>();
        for (Module m : modules) {
            if (!m.getPath().endsWith("*")) {
                list.add(m.getPath());
            }
        }

        jahiaContentManagementServiceAsync.getNodesWithPublicationInfo(list, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onApplicationFailure(Throwable caught) {
                Info.display("Workflow not started", "Workflow not started");
            }

            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode node : result) {
                    if (node.getWorkflowInfo().getAvailableActions().size() > 0) {
                        contentStore.add(node);
                    }
                }
                contentStore.sort("displayName", Style.SortDir.ASC);
                contentContainer.layout(true);
            }
        });
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
    }

    @Override
    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_WORKFLOW) != 0) {
            fillStore();
            layout();
        }
    }
}