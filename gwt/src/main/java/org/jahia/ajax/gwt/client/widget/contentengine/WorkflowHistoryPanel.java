/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowTask;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryTask;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.poller.Poller;
import org.jahia.ajax.gwt.client.widget.poller.TaskEvent;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.CustomWorkflow;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowDashboardEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT panel that displays a list of workflow process and task history records.
 *
 * @author Sergiy Shyrkov
 */
public class WorkflowHistoryPanel extends LayoutContainer {

    private WorkflowDashboardEngine engine;
    private boolean dashboard = false;

    private Linker linker;

    private String nodeId;
    private String locale;

    private Poller.PollListener<TaskEvent> listener;

    /**
     * Initializes an instance of this class.
     *
     * @param nodeId
     * @param locale
     */
    public WorkflowHistoryPanel(final String nodeId, final String locale) {
        super(new FitLayout());

        this.nodeId = nodeId;
        this.locale = locale;
        init();
    }

    public WorkflowHistoryPanel(WorkflowDashboardEngine engine, Linker linker) {
        super(new FitLayout());

        dashboard = true;
        this.linker = linker;
        this.engine = engine;
        init();
    }


    private void init() {
        setBorders(false);
        removeAll();
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<List<GWTJahiaWorkflowHistoryItem>> proxy = new RpcProxy<List<GWTJahiaWorkflowHistoryItem>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> callback) {
                if (loadConfig == null) {
                    if (dashboard) {
                        service.getWorkflowHistoryForUser(callback);
                    } else {
                        service.getWorkflowHistoryProcesses(nodeId, locale, callback);
                    }
                } else if (loadConfig instanceof GWTJahiaWorkflowHistoryProcess) {
                    final GWTJahiaWorkflowHistoryProcess process = (GWTJahiaWorkflowHistoryProcess) loadConfig;
                    service.getWorkflowHistoryTasks(process.getProvider(), process.getProcessId(), callback);
                } else {
                    callback.onSuccess(new ArrayList<GWTJahiaWorkflowHistoryItem>());
                }
            }
        };

        // tree loader
        final TreeLoader<GWTJahiaWorkflowHistoryItem> loader = new BaseTreeLoader<GWTJahiaWorkflowHistoryItem>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaWorkflowHistoryItem parent) {
                return parent instanceof GWTJahiaWorkflowHistoryProcess;
            }
        };

        // trees store
        final TreeStore<GWTJahiaWorkflowHistoryItem> store = new TreeStore<GWTJahiaWorkflowHistoryItem>(loader);
        store.setStoreSorter(new StoreSorter<GWTJahiaWorkflowHistoryItem>() {
            @Override
            public int compare(Store<GWTJahiaWorkflowHistoryItem> store, GWTJahiaWorkflowHistoryItem m1, GWTJahiaWorkflowHistoryItem m2, String property) {
                return m1.getStartDate().compareTo(m2.getStartDate());
            }
        });
        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("displayName", Messages.get("label.name", "Name"), 160);
        column.setRenderer(new WidgetTreeGridCellRenderer<GWTJahiaWorkflowHistoryItem>() {
            @Override
            public Widget getWidget(GWTJahiaWorkflowHistoryItem historyItem, String property, ColumnData config,
                                    int rowIndex, int colIndex,
                                    ListStore<GWTJahiaWorkflowHistoryItem> gwtJahiaWorkflowHistoryItemListStore,
                                    Grid<GWTJahiaWorkflowHistoryItem> grid) {
                if (dashboard && historyItem instanceof GWTJahiaWorkflowHistoryTask) {
                    final GWTJahiaWorkflowHistoryProcess parent = (GWTJahiaWorkflowHistoryProcess) ((TreeGrid<GWTJahiaWorkflowHistoryItem>) grid).getTreeStore().getParent(
                            historyItem);
                    for (final GWTJahiaWorkflowTask task : parent.getAvailableTasks()) {
                        if (task.getId().equals(historyItem.getId())) {
                            Button b = new Button(historyItem.<String>get("displayName"));
                            b.addStyleName("button-details");
                            b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                @Override
                                public void componentSelected(ButtonEvent ce) {
                                    EnginePanel container = new EnginePanel();
                                    // get path from the publication info, not used for unpublished
                                    CustomWorkflow customWorkflowInfo = parent.getRunningWorkflow().getCustomWorkflowInfo();
                                    String path = "";
                                    if (customWorkflowInfo instanceof PublicationWorkflow && ((PublicationWorkflow) customWorkflowInfo).getPublicationInfos().size() > 0) {
                                        path = ((PublicationWorkflow) customWorkflowInfo).getPublicationInfos().get(0).getMainPath();
                                    }
                                    new WorkflowActionDialog(path, parent.getRunningWorkflow(), task, linker, customWorkflowInfo, container);
                                    container.showEngine();
                                    container.addListener(Events.Close, new Listener<BaseEvent>() {
                                        @Override
                                        public void handleEvent(BaseEvent be) {
                                            engine.show();
                                            init();
                                            layout(true);
                                        }
                                    });
                                    engine.hide();
                                }
                            });
                            return b;
                        }
                    }
                }
                return new Label(historyItem.<String>get("displayName"));
            }
        });
        config.add(column);
        column = new ColumnConfig("locale", 40);
        column.setRenderer(new GridCellRenderer<ModelData>() {
            @Override
            public Object render(ModelData historyItem, String property, ColumnData config, int rowIndex, int colIndex, ListStore<ModelData> store, Grid<ModelData> grid) {
                if (dashboard && historyItem.get("workflow") instanceof GWTJahiaWorkflow) {
                    String lang = ((GWTJahiaWorkflow) historyItem.get("workflow")).get("locale");
                    if (lang != null && JahiaGWTParameters.getLanguage(lang) != null) {
                        return "<img src=\"" + JahiaGWTParameters.getLanguage(lang).getImage() + "\"/>&nbsp;";
                    }
                }
                return "";
            }
        });
        config.add(column);

        column = new ColumnConfig("user", Messages.get("label.user", "User"), 70);
        config.add(column);

        column = new ColumnConfig("nodeWrapper", Messages.get("label.workflow.start.node", "Workflow Starting Node"),
                200);
        column.setRenderer(new GridCellRenderer<GWTJahiaWorkflowHistoryItem>() {

            /**
             * Returns the HTML to be used in a grid cell.
             *
             * @param model    the model
             * @param property the model property
             * @param config   the column config
             * @param rowIndex the row index
             * @param colIndex the cell index
             * @param store    the data store
             * @param grid     the grid
             * @return the cell HTML or Component instance
             */
            @Override
            public Object render(GWTJahiaWorkflowHistoryItem model, String property, ColumnData config,
                                 int rowIndex, int colIndex,
                                 ListStore<GWTJahiaWorkflowHistoryItem> store,
                                 Grid<GWTJahiaWorkflowHistoryItem> grid) {
                final GWTJahiaNode wrapper = (GWTJahiaNode) model.getProperties().get("nodeWrapper");

                if (wrapper != null) {
                    return new Label(wrapper.getDisplayName() + " (" + wrapper.getPath() + ")");
                }
                List<GWTJahiaWorkflowHistoryItem> models = store.getModels();
                for (final GWTJahiaWorkflowHistoryItem historyItem : models) {
                    final GWTJahiaNode nodewrapper = (GWTJahiaNode) historyItem.getProperties().get("nodeWrapper");
                    if (nodewrapper != null && historyItem.getProcessId().equals(model.getProcessId()) &&
                            historyItem instanceof GWTJahiaWorkflowHistoryProcess) {
                        ButtonBar buttonBar = new ButtonBar();
                        Button previewButton = new Button(Messages.get("label.preview"));
                        previewButton.addStyleName("button-preview");
                        previewButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                String path = nodewrapper.getPath();
                                String locale = JahiaGWTParameters.getLanguage();
                                JahiaContentManagementService.App.getInstance().getNodeURL("render", path, null, null,
                                        "default", locale, false, new BaseAsyncCallback<String>() {

                                            @Override
                                            public void onSuccess(String url) {
                                                Window window = new Window();
                                                window.addStyleName("content-preview");
                                                window.setMaximizable(true);
                                                window.setSize(800, 600);
                                                window.setUrl(url);
                                                window.setPosition(engine.getPosition(true).x + 50, engine.getPosition(true).y + 50);
                                                window.show();
                                            }

                                        });
                            }
                        });
                        buttonBar.add(previewButton);

                        Button inContextButton = new Button(Messages.get("label.preview.context"));
                        inContextButton.addStyleName("button-incontext-preview");
                        inContextButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                String path = nodewrapper.getPath();
                                String locale = JahiaGWTParameters.getLanguage();
                                JahiaContentManagementService.App.getInstance().getNodeURL("render", path, null, null,
                                        "default", locale, true, new BaseAsyncCallback<String>() {

                                            @Override
                                            public void onSuccess(String url) {
                                                Window window = new Window();
                                                window.addStyleName("content-incontext-preview");
                                                window.setMaximizable(true);
                                                window.setSize(1000, 750);
                                                window.setUrl(url);
                                                window.setPosition(engine.getPosition(true).x + 50, engine.getPosition(true).y + 50);
                                                window.show();
                                            }

                                        });
                            }
                        });
                        buttonBar.add(inContextButton);
                        return buttonBar;
                    }
                }
                return new Label("");
            }
        });
        config.add(column);

        column = new ColumnConfig("startDate", Messages.get("org.jahia.engines.processDisplay.tab.startdate",
                "Start date"), 90);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("endDate", Messages.get("org.jahia.engines.processDisplay.tab.enddate", "End date"),
                90);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("duration", Messages.get("org.jahia.engines.processDisplay.column.duration",
                "Duration"), 60);
        column.setRenderer(new GridCellRenderer<GWTJahiaWorkflowHistoryItem>() {

            @Override
            public Object render(GWTJahiaWorkflowHistoryItem historyItem, String property, ColumnData config,
                                 int rowIndex, int colIndex, ListStore<GWTJahiaWorkflowHistoryItem> store,
                                 Grid<GWTJahiaWorkflowHistoryItem> grid) {
                Long duration = historyItem.getDuration();
                String display = "-";
                if (duration != null) {
                    long time = duration.longValue();
                    if (time < 1000) {
                        display = time + " " + Messages.get("label.milliseconds", "ms");
                    } else if (time < 1000 * 60L) {
                        display = ((long) (time / 1000)) + " " + Messages.get("label.seconds", "sec");
                    } else if (time < 1000 * 60 * 60L) {
                        display = ((long) (time / (1000 * 60L))) + " " + Messages.get("label.minutes", "min") + " " +
                                ((long) ((time % (1000 * 60L)) / 1000)) + " " + Messages.get("label.seconds", "sec");
                    } else {
                        display = ((long) (time / (1000 * 60 * 60L))) + " " + Messages.get("label_hours", "h") + " " +
                                ((long) ((time % (1000 * 60 * 60L)) / (1000 * 60L))) + " " + Messages.get(
                                "label.minutes", "min");
                    }
                }
                return new Label(display);
            }
        });
        config.add(column);

        if (PermissionsUtils.isPermitted("viewWorkflowTab", JahiaGWTParameters.getSiteNode().getPermissions())) {
            column = new ColumnConfig("operation", Messages.get("label.operation", "Operation"), 100);
            column.setRenderer(new GridCellRenderer<GWTJahiaWorkflowHistoryItem>() {

                @Override
                public Object render(final GWTJahiaWorkflowHistoryItem model, String property, ColumnData config,
                                     int rowIndex, int colIndex,
                                     ListStore<GWTJahiaWorkflowHistoryItem> gwtJahiaWorkflowHistoryItemListStore,
                                     Grid<GWTJahiaWorkflowHistoryItem> gwtJahiaWorkflowHistoryItemGrid) {
                    if (model instanceof GWTJahiaWorkflowHistoryProcess && !((GWTJahiaWorkflowHistoryProcess) model).isFinished()) {
                        Button button = new Button(Messages.get("label.abort", "Abort"));
                        button.addStyleName("button-abort");

                        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                JahiaContentManagementService.App.getInstance().abortWorkflow(model.getId(), model.getProvider(), new BaseAsyncCallback<String>() {

                                    @Override
                                    public void onSuccess(String url) {
                                        store.removeAll();
                                        loader.load();
                                    }

                                });
                            }
                        });
                        button.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
                        return button;
                    }
                    return new Label("");
                }
            });
            config.add(column);
        }

        ColumnModel cm = new ColumnModel(config);

        final TreeGrid<GWTJahiaWorkflowHistoryItem> tree = new TreeGrid<GWTJahiaWorkflowHistoryItem>(store, cm);
        tree.setStateful(true);
        tree.setBorders(true);
        tree.getStyle().setNodeOpenIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setNodeCloseIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setLeafIcon(StandardIconsProvider.STANDARD_ICONS.workflowTask());
        tree.setAutoExpandColumn("displayName");
        tree.getTreeView().setRowHeight(25);
        tree.setTrackMouseOver(false);
        tree.setAutoExpandMax(5000);
        add(tree);

        listener = new Poller.PollListener<TaskEvent>() {
            @Override
            public void handlePollingResult(TaskEvent result) {
                if (result.getEndedTask() != null) {
                    for (GWTJahiaWorkflowHistoryItem item : new ArrayList<GWTJahiaWorkflowHistoryItem>(store.getAllItems())) {

                        if (item instanceof GWTJahiaWorkflowHistoryProcess) {
                            for (GWTJahiaWorkflowTask task : new ArrayList<GWTJahiaWorkflowTask>(item.getAvailableTasks())) {
                                if (task.getId().equals(result.getEndedTask())) {
                                    item.getAvailableTasks().remove(task);
                                }
                            }
                            if (item.getAvailableTasks().isEmpty()) {
                                store.remove(item);
                            }
                        } else if (item instanceof GWTJahiaWorkflowHistoryTask) {
                            if (item.getId().equals(result.getEndedTask())) {
                                store.remove(item);
                            }
                        }
                    }
                }
            }
        };
        Poller.getInstance().registerListener(listener, TaskEvent.class);

        if (engine != null) {
            engine.addListener(Events.Hide, new Listener<BaseEvent>() {

                @Override
                public void handleEvent(BaseEvent be) {
                    if (listener != null) {
                        Poller.getInstance().unregisterListener(listener, TaskEvent.class);
                    }
                }
            });
        }

    }

}