package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTaskComment;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 28, 2010
 * Time: 4:32:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowDashboardEngine extends Window {
    private final Linker linker;
    private ListStore<GWTJahiaWorkflowHistoryTask> contentStore;
    private JahiaContentManagementServiceAsync contentManager;
    private ColumnModel cm;
    private Grid<GWTJahiaWorkflowHistoryTask> grid;

    public WorkflowDashboardEngine(Linker linker) {
        super();
        this.linker = linker;
        setLayout(new FitLayout());
        init();
    }

    /**
     * init
     */
    private void init() {
        setHeading(Messages.get("label.workflowdashboard", "Workflow Dashboard"));
        setLayout(new FitLayout());
        setSize(800, 600);

        contentManager = JahiaContentManagementService.App.getInstance();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();
        RowExpander expander = new RowExpander(XTemplate.create("<div></div>"));
        expander.addListener(Events.Expand, new Listener<RowExpanderEvent>() {
            /**
             * Sent when an event that the listener has registered for occurs.
             *
             * @param be the event which occurred
             */
            public void handleEvent(final RowExpanderEvent be) {
                GWTJahiaNode node = (GWTJahiaNode) be.getModel().get("node");
                String language = (String) be.getModel().get("language");
                final GWTJahiaWorkflowHistoryItem item = (GWTJahiaWorkflowHistoryItem) be.getModel();
                contentManager.getWorkflowHistoryItems(node.getUUID(), item, language,
                        new BaseAsyncCallback<List<GWTJahiaWorkflowHistoryItem>>() {
                            public void onSuccess(List<GWTJahiaWorkflowHistoryItem> result) {
                                Element bodyElement = be.getBodyElement();
                                StringBuilder html = new StringBuilder();
                                for (GWTJahiaWorkflowHistoryItem item : result) {
                                    if (item instanceof GWTJahiaWorkflowHistoryTask) {
                                        GWTJahiaWorkflowHistoryTask task = (GWTJahiaWorkflowHistoryTask) item;
                                        if (task.getAssignee() != null && task.getAssignee().length() > 0) {
                                            html.append("<p><b>"+task.getDisplayName()+ " started&nbsp;</b>")
                                                    .append(DateTimeFormat.getMediumDateTimeFormat().format(
                                                            item.getStartDate()));
                                            html.append("<b>&nbsp;by&nbsp;</b>").append(task.getAssignee());
                                            if (task.getOutcome() != null) {
                                                html.append("&nbsp;has&nbsp;").append(task.getOutcome())
                                                        .append("");
//                                                html.append("&nbsp;at&nbsp;")
//                                                        .append(DateTimeFormat.getMediumDateTimeFormat().format(
//                                                                item.getEndDate())).append("");
                                            }
                                            html.append("</p><br/>");
                                        }
                                    } else {
                                        GWTJahiaWorkflowHistoryProcess wf = (GWTJahiaWorkflowHistoryProcess) item;
                                        html.append("<p><b>Workflow started&nbsp;</b>")
                                                .append(DateTimeFormat.getMediumDateTimeFormat().format(
                                                        item.getStartDate()));
                                        html.append("<b>&nbsp;by&nbsp;</b>").append(wf.getWorkflowStartUser());
                                        html.append("</p><br/>");
                                    }
                                }
                                bodyElement.appendChild(new HTML(html.toString()).getElement());
                            }
                        });
                be.setCancelled(false);
            }
        });
        displayColumns.add(expander);

        displayColumns.add(new ColumnConfig("node.displayName", Messages.get("label.name"), 150));

        ColumnConfig config = new ColumnConfig("displayName", Messages.get("label.status"), 140);
//        config.setRenderer(new GridCellRenderer() {
//            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
//                                 ListStore listStore, Grid grid) {
//                Menu menu = new Menu();
//                final GWTJahiaNode node = (GWTJahiaNode) model;
//                String actionName = null;
//                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
//                for (final GWTJahiaWorkflowAction action : actions) {
//                    if (actionName == null) {
//                        actionName = action.getName();
//                    }
//                    MenuItem item = new MenuItem(action.getName());
//                    item.addSelectionListener(new SelectionListener<MenuEvent>() {
//                        @Override
//                        public void componentSelected(MenuEvent ce) {
//                            WorkflowActionDialog dialog = new WorkflowActionDialog(node, action);
//                            dialog.setWorkflowDashboard(WorkflowDashboardEngine.this);
//                            dialog.show();
//                        }
//                    });
//                    menu.add(item);
//
//                }
//                Button splitButton = new Button(actionName);
//                splitButton.setMenu(menu);
//                return splitButton;
//            }
//        });
//        config.setSortable(false);
        displayColumns.add(config);

        config = new ColumnConfig("startDate", Messages.get("label.workflow.taskStartTime"), 140);
//        config.setSortable(true);
//        config.setRenderer(new GridCellRenderer() {
//            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
//                                 ListStore listStore, Grid grid) {
//                final GWTJahiaNode node = (GWTJahiaNode) model;
//                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
//                return new Label(DateTimeFormat.getMediumDateTimeFormat().format(actions.get(0).getCreateTime()));
//            }
//        });
        displayColumns.add(config);

        config = new ColumnConfig("title", Messages.get("label.workflow.titleOfInstance"), 2000);
//        config.setSortable(true);
//        config.setRenderer(new GridCellRenderer() {
//            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
//                                 ListStore listStore, Grid grid) {
//                final GWTJahiaNode node = (GWTJahiaNode) model;
//                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
//                final GWTJahiaWorkflowAction action = actions.get(0);
//                Label label;
//                if (action.getVariables().containsKey("jcr:title")) {
//                    String s = action.getVariables().get("jcr:title").getValues().get(0).getString();
//                    if (s == null || "".equals(s.trim())) {
//                        s = action.getName();
//                    }
//                    label = new Label(s);
//                } else {
//                    label = new Label(action.getName());
//                }
//                return label;
//            }
//        });
        displayColumns.add(config);

//        config = new ColumnConfig("dateOfLastComment", Messages.getResource("label.workflow.dateOfLastComment"), 150);
//        config.setSortable(true);
//        config.setRenderer(new GridCellRenderer() {
//            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
//                                 ListStore listStore, Grid grid) {
//                final GWTJahiaNode node = (GWTJahiaNode) model;
//                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
//                GWTJahiaWorkflowAction action = actions.get(0);
//                final Label label = new Label();
//                contentManager.getTaskComments(action, new BaseAsyncCallback<List<GWTJahiaWorkflowTaskComment>>() {
//                    /**
//                     * Called when an asynchronous call completes successfully.
//                     *
//                     * @param result the return value of the remote produced call
//                     */
//                    public void onSuccess(List<GWTJahiaWorkflowTaskComment> result) {
//                        if (result.size() > 0) {
//                            label.setText(DateTimeFormat.getMediumDateTimeFormat().format(
//                                    result.get(result.size() - 1).getTime()));
//                        }
//                    }
//                });
//                return label;
//            }
//        });
//        displayColumns.add(config);


        contentStore = new ListStore<GWTJahiaWorkflowHistoryTask>();
        cm = new ColumnModel(displayColumns);
        grid = new Grid<GWTJahiaWorkflowHistoryTask>(contentStore, cm);
        grid.getStore().setMonitorChanges(true);
        grid.setBorders(true);
        grid.setHeight(500);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.addListener(Events.CellDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent ge) {
                if (ge.getColIndex() == 4) {
                    GWTJahiaNode node = (GWTJahiaNode) ge.getGrid().getSelectionModel().getSelectedItem();
                    GWTJahiaWorkflowAction action = node.getWorkflowInfo().getAvailableActions().get(0);
                    final Window window = new Window();
                    window.setScrollMode(Style.Scroll.AUTO);
                    window.setSize(800, 600);
                    contentManager.getRenderedContent(node.getPath(), action.getWorkspace(), action.getLocale(), null,
                            "gwt", null, false, null, new BaseAsyncCallback<GWTRenderResult>() {

                                public void onSuccess(GWTRenderResult result) {
                                    HTML html = new HTML(result.getResult());
                                    window.add(html);
                                    window.layout(true);
                                }
                            });
                    window.show();
                }
            }
        });
        grid.addPlugin(expander);
       grid.setAutoExpandColumn("node.displayName");
        grid.sinkEvents(Event.ONDBLCLICK);
        grid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent baseEvent) {
                GWTJahiaWorkflowHistoryTask task = (GWTJahiaWorkflowHistoryTask) (((GridEvent) baseEvent).getModel());

                final GWTJahiaNode node = (GWTJahiaNode) task.get("node");

                for (GWTJahiaWorkflowAction action : node.getWorkflowInfos().get(task.get("language")).getAvailableActions()) {
                    if (action.getName().equals(task.getName())) {
                        WorkflowActionDialog dialog = new WorkflowActionDialog(node, action);
                        dialog.setWorkflowDashboard(WorkflowDashboardEngine.this);
                        dialog.show();
                        break;
                    }
                }
            }
        });

        add(grid);
        refreshData();
    }

    public void refreshData() {
        contentStore.removeAll();
        contentManager.getTasksForUser(new BaseAsyncCallback<List<GWTJahiaWorkflowHistoryTask>>() {
            public void onApplicationFailure(Throwable caught) {
                Info.display("Workflow not started", "Workflow not started");
            }

            public void onSuccess(List<GWTJahiaWorkflowHistoryTask> result) {
                for (GWTJahiaWorkflowHistoryTask task : result) {
                    contentStore.add(task);
                }

                contentStore.sort("node.displayName", Style.SortDir.ASC);
                WorkflowDashboardEngine.this.layout(true);
            }
        });
    }


}
