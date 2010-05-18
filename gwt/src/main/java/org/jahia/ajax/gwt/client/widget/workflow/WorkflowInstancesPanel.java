/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.workflow;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTaskComment;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
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
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 3 mai 2010
 */
public class WorkflowInstancesPanel extends ContentPanel {
    private final Linker linker;
    private ListStore<GWTJahiaNode> contentStore;
    private JahiaContentManagementServiceAsync async;
    private ColumnModel cm;
    private Grid<GWTJahiaNode> grid;

    public WorkflowInstancesPanel(Linker linker) {
        //To change body of created methods use File | Settings | File Templates.
        this.linker = linker;
        setBodyBorder(true);
        setLayout(new FitLayout());
        init(this);
    }

    private void init(final WorkflowInstancesPanel workflowInstancesPanel) {
        async = JahiaContentManagementService.App.getInstance();
        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();
        RowExpander expander = new RowExpander(XTemplate.create("<div></div>"));
        expander.addListener(Events.Expand, new Listener<RowExpanderEvent>() {
            /**
             * Sent when an event that the listener has registered for occurs.
             *
             * @param be the event which occurred
             */
            public void handleEvent(final RowExpanderEvent be) {
                GWTJahiaNode node = (GWTJahiaNode) be.getModel();
                GWTJahiaWorkflowAction action = node.getWorkflowInfo().getAvailableActions().get(0);
                GWTJahiaWorkflowHistoryItem item = new GWTJahiaWorkflowHistoryItem("", action.getProcessId(),
                                                                                   action.getProvider(), false, null,
                                                                                   null, null);
                async.getWorkflowHistoryItems(node.getUUID(), item, action.getLocale(),
                                              new AsyncCallback<List<GWTJahiaWorkflowHistoryItem>>() {
                                                  public void onFailure(Throwable caught) {
                                                      //To change body of implemented methods use File | Settings | File Templates.
                                                  }

                                                  public void onSuccess(List<GWTJahiaWorkflowHistoryItem> result) {
                                                      Element bodyElement = be.getBodyElement();
                                                      StringBuilder html = new StringBuilder();
                                                      for (GWTJahiaWorkflowHistoryItem item : result) {
                                                          GWTJahiaWorkflowHistoryTask task = (GWTJahiaWorkflowHistoryTask) item;
                                                          html.append("<p style=\"background-color:white").append(
                                                                  "\"><span>Start:&nbsp;").append(
                                                                  DateTimeFormat.getMediumDateTimeFormat().format(
                                                                          item.getStartDate())).append("</span>");
                                                          if (task.getAssignee() != null) {
                                                              html.append("<span>&nbsp;by&nbsp;").append(
                                                                      task.getAssignee()).append("</span>");
                                                          }
                                                          if (task.getOutcome() != null) {
                                                              html.append("<span>&nbsp;has&nbsp;").append(
                                                                      task.getOutcome()).append("</span>");
                                                              html.append("<span>&nbsp;at&nbsp;").append(
                                                                      DateTimeFormat.getMediumDateTimeFormat().format(
                                                                          item.getEndDate())).append("</span>");
                                                          }
                                                          html.append("</p><br/>");
                                                      }
                                                      bodyElement.appendChild(new HTML(html.toString()).getElement());
                                                  }
                                              });
                be.setCancelled(false);
            }
        });
        displayColumns.add(expander);

        displayColumns.add(new ColumnConfig("displayName", Messages.getResource("label.name"), 150));

        ColumnConfig config = new ColumnConfig("workflowStatus", Messages.getResource("label.status"), 140);
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
                            WorkflowActionDialog dialog = new WorkflowActionDialog(node, action);
                            dialog.setWorkflowInstancesPanel(workflowInstancesPanel);
                            dialog.show();
                        }
                    });
                    menu.add(item);

                }
                Button splitButton = new Button(actionName);
                splitButton.setMenu(menu);
                return splitButton;
            }
        });
        config.setSortable(false);
        displayColumns.add(config);

        config = new ColumnConfig("taskStartTime", Messages.getResource("label.workflow.taskStartTime"), 140);
        config.setSortable(true);
        config.setRenderer(new GridCellRenderer() {
            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                final GWTJahiaNode node = (GWTJahiaNode) model;
                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
                return new Label(DateTimeFormat.getMediumDateTimeFormat().format(actions.get(0).getCreateTime()));
            }
        });
        displayColumns.add(config);

        config = new ColumnConfig("titleOfWorkflow", Messages.getResource("label.workflow.titleOfInstance"), 150);
        config.setSortable(true);
        config.setRenderer(new GridCellRenderer() {
            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                final GWTJahiaNode node = (GWTJahiaNode) model;
                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
                final GWTJahiaWorkflowAction action = actions.get(0);
                Label label;
                if (action.getVariables().containsKey("jcr:title")) {
                    label = new Label(action.getVariables().get("jcr:title").getValues().get(0).getString());
                } else {
                    label = new Label(action.getName());
                }
                return label;
            }
        });
        displayColumns.add(config);

        config = new ColumnConfig("dateOfLastComment", Messages.getResource("label.workflow.dateOfLastComment"), 150);
        config.setSortable(true);
        config.setRenderer(new GridCellRenderer() {
            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                 ListStore listStore, Grid grid) {
                final GWTJahiaNode node = (GWTJahiaNode) model;
                List<GWTJahiaWorkflowAction> actions = node.getWorkflowInfo().getAvailableActions();
                GWTJahiaWorkflowAction action = actions.get(0);
                final Label label = new Label();
                async.getTaskComments(action, new AsyncCallback<List<GWTJahiaWorkflowTaskComment>>() {
                    /**
                     * Called when an asynchronous call fails to complete normally.
                     * {@link com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException}s, {@link com.google.gwt.user.client.rpc.InvocationException}s,
                     * or checked exceptions thrown by the service method are examples of the type
                     * of failures that can be passed to this method.
                     * <p/>
                     * <p>
                     * If <code>caught</code> is an instance of an
                     * {@link com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException} the application should try to
                     * get into a state where a browser refresh can be safely done.
                     * </p>
                     *
                     * @param caught failure encountered while executing a remote procedure call
                     */
                    public void onFailure(Throwable caught) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    /**
                     * Called when an asynchronous call completes successfully.
                     *
                     * @param result the return value of the remote produced call
                     */
                    public void onSuccess(List<GWTJahiaWorkflowTaskComment> result) {
                        if (result.size() > 0) {
                            label.setText(DateTimeFormat.getMediumDateTimeFormat().format(result.get(
                                    result.size() - 1).getTime()));
                        }
                    }
                });
                return label;
            }
        });
        displayColumns.add(config);


        contentStore = new ListStore<GWTJahiaNode>();
        cm = new ColumnModel(displayColumns);
        grid = new Grid<GWTJahiaNode>(contentStore, cm);
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
                    async.getRenderedContent(node.getPath(), action.getWorkspace(), action.getLocale(), null,
                                             "bodywrapper", null, false, null, new AsyncCallback<GWTRenderResult>() {
                                public void onFailure(Throwable caught) {
                                }

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
        add(grid);
        refreshData();
    }

    public void refreshData() {
        contentStore.removeAll();
        final WorkflowInstancesPanel workflowInstancesPanel = this;
        async.getTasksForUser(new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable caught) {
                Info.display("Workflow not started", "Workflow not started");
            }

            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode node : result) {
                    if (node.getWorkflowInfo().getAvailableActions().size() > 0) {
                        contentStore.add(node);
                    }
                }
                contentStore.sort("displayName", Style.SortDir.ASC);
                workflowInstancesPanel.layout(true);
            }
        });
    }
}
