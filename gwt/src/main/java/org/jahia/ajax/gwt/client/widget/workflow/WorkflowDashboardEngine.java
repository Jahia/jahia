/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.ArrayList;
import java.util.List;

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
                                            html.append("<div class=\"x-grid3-cell x-grid3-cell-inner\">" +
                                                    task.getDisplayName() + " started&nbsp;")
                                                    .append(DateTimeFormat.getMediumDateTimeFormat().format(
                                                            item.getStartDate()));
                                            html.append("&nbsp;by&nbsp;").append(task.getAssignee());
                                            if (task.getOutcome() != null) {
                                                html.append("&nbsp;has&nbsp;").append(task.getOutcome()).append("");
//                                                html.append("&nbsp;at&nbsp;")
//                                                        .append(DateTimeFormat.getMediumDateTimeFormat().format(
//                                                                item.getEndDate())).append("");
                                            }
                                            html.append("</div/>");
                                        }
                                    } else {
                                        GWTJahiaWorkflowHistoryProcess wf = (GWTJahiaWorkflowHistoryProcess) item;
                                        html.append(
                                                "<div class=\"x-grid3-cell x-grid3-cell-inner\">Workflow started&nbsp;")
                                                .append(DateTimeFormat.getMediumDateTimeFormat().format(
                                                        item.getStartDate()));
                                        html.append("&nbsp;by&nbsp;").append(wf.getWorkflowStartUser());
                                        html.append("</div>");
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
        config.setRenderer(new GridCellRenderer() {
            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                final GWTJahiaWorkflowHistoryTask node = (GWTJahiaWorkflowHistoryTask) model;
                return new Label(DateTimeFormat.getMediumDateTimeFormat().format(node.getStartDate()));
            }
        });
        displayColumns.add(config);

        config = new ColumnConfig("title", Messages.get("label.workflow.titleOfInstance"), 200);
        displayColumns.add(config);

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
                    contentManager
                            .getRenderedContent(node.getPath(), action.getWorkspace(), action.getLocale(), null, "gwt",
                                    null, false, null, new BaseAsyncCallback<GWTRenderResult>() {

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

                for (GWTJahiaWorkflowAction action : node.getWorkflowInfos().get(task.get("language"))
                        .getAvailableActions()) {
                    if (action.getName().equals(task.getName())) {
                        WorkflowActionDialog dialog = new WorkflowActionDialog(node, action, linker);
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
