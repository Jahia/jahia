/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.job;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.grid.JahiaGroupingView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Panel that displays a list of background jobs, future, current and past, along with a split -view to display details on a selected job.
 * In the future this UI might also include actions such as deleting a job, etc...
 * <p/>
 * User: loom
 * Date: Sep 21, 2010
 * Time: 12:28:42 PM
 */
public class JobListPanel extends LayoutContainer {

    private FormPanel detailsPanel;

    private List<GWTJahiaJobDetail> selectedItems = null;
    private transient Timer timer;
    private Button deleteButton;
    private PagingToolBar pagingToolBar;
    private static final String STATUS_EXECUTING = "executing";
    public static final String STATUS_ADDED = "added";
    public static final String STATUS_SCHEDULED = "scheduled";
    private int autoRefreshInterval = 10;
    private boolean autoRefreshActivated = false;
    private List<String> activeGroupNames = null;

	private boolean adminMode;
    private Grid<GWTJahiaJobDetail> grid;

    public JobListPanel(boolean adminMode) {
        super(new BorderLayout());
        this.adminMode = adminMode;
        init();
    }

    private void init() {
        setBorders(false);
        activeGroupNames = new ArrayList<String>();
        activeGroupNames.add("ActionJob");
        activeGroupNames.add("PublicationJob");
        activeGroupNames.add("ImportJob");
        activeGroupNames.add("SitemapJob");
        activeGroupNames.add("RuleJob");
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaJobDetail>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaJobDetail>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaJobDetail>> callback) {
                if (loadConfig == null) {
                    service.getJobs(0, Integer.MAX_VALUE, null, null, null, activeGroupNames, callback);
                } else if (loadConfig instanceof BasePagingLoadConfig) {
                    BasePagingLoadConfig pagingLoadConfig = (BasePagingLoadConfig) loadConfig;
                    int limit = pagingLoadConfig.getLimit();
                    int offset = pagingLoadConfig.getOffset();
                    Style.SortDir sortDir = pagingLoadConfig.getSortDir();
                    String sortField = pagingLoadConfig.getSortField();
                    service.getJobs(offset, limit, sortField, sortDir.name(), ((GroupingLoadConfig) loadConfig).getGroupBy(), activeGroupNames, callback);
                } else {
                    callback.onSuccess(new BasePagingLoadResult<GWTJahiaJobDetail>(new ArrayList<GWTJahiaJobDetail>()));
                }
            }
        };

        // tree loader
        final BasePagingLoader<BasePagingLoadResult<ModelData>> loader = new BasePagingLoader<BasePagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);
        loader.setReuseLoadConfig(true);

        // trees store
        final GroupingStore<GWTJahiaJobDetail> store = new GroupingStore<GWTJahiaJobDetail>(loader) {
            @Override
            public void groupBy(String field) {
                super.groupBy(field);
                grid.getState().put("groupBy", field);
                grid.saveState();
            }
        };

        pagingToolBar = new PagingToolBar(50);
        pagingToolBar.bind(loader);
        pagingToolBar.addStyleName("jobs-paging-toolbar");

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("creationTime", Messages.get("label.creationTime", "Start date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        column.setSortable(true);
        config.add(column);

        column = new ColumnConfig("group", Messages.get("label.type", "Type"), 100);
        column.setSortable(false);
        column.setRenderer(new GridCellRenderer<GWTJahiaJobDetail>() {
            public Object render(GWTJahiaJobDetail jobDetail, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaJobDetail> gwtJahiaJobDetailListStore, Grid<GWTJahiaJobDetail> gwtJahiaJobDetailGrid) {
                return new Label(Messages.get("label." + jobDetail.getGroup() + ".task", jobDetail.getGroup()));
            }
        });
        config.add(column);

        column = new ColumnConfig("description", Messages.get("label.description", "Description"), 100);
        column.setSortable(false);
        column.setGroupable(false);
        config.add(column);

        column = new ColumnConfig("status", Messages.get("label.status", "Status"), 100);
        column.setRenderer(new GridCellRenderer<GWTJahiaJobDetail>() {
            @Override
            public Object render(GWTJahiaJobDetail jobDetail, String property, ColumnData config, int rowIndex, int colIndex, ListStore store, Grid grid) {
                return Messages.get("label.job.status." + jobDetail.getStatus());
            }
        });
        column.setSortable(false);
        config.add(column);

        column = new ColumnConfig("duration", Messages.get("label.duration", "Duration"), 140);
        column.setSortable(true);
        column.setGroupable(false);
        column.setRenderer(new GridCellRenderer<GWTJahiaJobDetail>() {
            public Object render(GWTJahiaJobDetail jobDetail, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore<GWTJahiaJobDetail> store, Grid<GWTJahiaJobDetail> grid) {
                Long duration = jobDetail.getDuration();
                String display = "-";
                if (duration != null) {
					if (duration < 1000) {
						display = duration + " " + Messages.get("label.milliseconds.short", "ms");
					} else if (duration < 60 * 1000L) {
						display = ((int) (duration / 1000L)) + " "
						        + Messages.get("label.seconds.short", "sec");
					} else if (duration < 60 * 60 * 1000L) {
						display = ((int) (duration / (60 * 1000L))) + " "
						        + Messages.get("label.minutes.short", "min") + " "
						        + (((int) (duration / 1000L)) % 60) + " "
						        + Messages.get("label.seconds.short", "sec");
					} else {
						display = ((int) (duration / (60 * 60 * 1000L))) + " "
						        + Messages.get("label.hours.short", "h") + " "
						        + (((int) (duration / 60 * 1000L)) % 60) + " "
						        + Messages.get("label.minutes.short", "min");
					}
                }
                if (STATUS_EXECUTING.equals(jobDetail.getStatus())) {
                    return new Label(Messages.get("label.executingSince", "Executing since ") + display + "...");
                } else {
                    return new Label(display);
                }
            }
        });
        config.add(column);

        column = new ColumnConfig("user", Messages.get("label.user", "User"), 100);
        column.setSortable(false);
        column.setGroupable(true);
        column.setHidden(true);
        config.add(column);

        column = new ColumnConfig("message", Messages.get("label.message", "Message"), 100);
        column.setSortable(false);
        column.setGroupable(false);
        column.setHidden(true);
        config.add(column);

        column = new ColumnConfig("name", Messages.get("label.name", "Name"), 100);
        column.setSortable(false);
        column.setGroupable(false);
        column.setHidden(true);
        config.add(column);

        final ColumnModel cm = new ColumnModel(config);

        GroupingView view = new JahiaGroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                String f = cm.getColumnById(data.field).getHeaderHtml();
                String l = data.models.size() == 1 ? Messages.get("label.item", "Item") : Messages.get("label.items", "Items");
                if (data.field.equals("status")) {
                    return f + ": " + Messages.get("label.job.status." + data.group) + " (" + data.models.size() + " " + l + ")";
                } else {
                    return f + ": " + (data.group) + " (" + data.models.size() + " " + l + ")";
                }
            }
        });

        grid = new Grid<GWTJahiaJobDetail>(store, cm);
        grid.setBorders(true);
        grid.setView(view);
        grid.setAutoExpandColumn("description");
        grid.setTrackMouseOver(false);
        grid.setStateId("jobPagingGrid");
        grid.setStateful(true);

        grid.addListener(Events.Attach, new Listener<GridEvent<GWTJahiaJobDetail>>() {
            public void handleEvent(GridEvent<GWTJahiaJobDetail> be) {
                LoadConfig config = new LoadConfig();
                config.setOffset(0);
                config.setLimit(50);
                Map<String, Object> state = grid.getState();

                String groupBy = (String) (state.get("groupBy") != null ? state.get("groupBy") : "status");
                config.setGroupBy(groupBy);
                store.groupBy(groupBy);
                store.setRemoteGroup(true);

                if (state.containsKey("offset")) {
                    int offset = (Integer) state.get("offset");
                    int limit = (Integer) state.get("limit");
                    config.setOffset(offset);
                    config.setLimit(limit);
                }
                if (state.containsKey("sortField")) {
                    config.setSortField((String) state.get("sortField"));
                    config.setSortDir(Style.SortDir.valueOf((String) state.get("sortDir")));
                }
                loader.load(config);
            }
        });
        grid.setLoadMask(true);
        grid.setBorders(true);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaJobDetail>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaJobDetail> gwtJahiaJobDetailSelectionChangedEvent) {
                selectedItems = gwtJahiaJobDetailSelectionChangedEvent.getSelection();
                if (adminMode) {
	                for (GWTJahiaJobDetail jobDetail : selectedItems) {
	                    if (STATUS_EXECUTING.equals(jobDetail.getStatus()) ||
	                            STATUS_SCHEDULED.equals(jobDetail.getStatus())) {
	                        deleteButton.disable();
	                        break;
	                    } else {
	                        deleteButton.enable();
	                    }
	                }
                }
                updateDetails();
            }
        });

        ToolBar topToolBar = new ToolBar();
        topToolBar.addStyleName("jobs-top-toolbar");
        Button filterButton = new Button(Messages.get("label.typeFilter", "Type filter"));
        final Menu filterMenu = new Menu();
        filterMenu.addStyleName("filter-menu");
        service.getAllJobGroupNames(new BaseAsyncCallback<List<String>>() {

            public void onSuccess(List<String> groupNames) {
                for (String groupName : groupNames) {
                    final CheckMenuItem groupActivated = new CheckMenuItem(Messages.get("label." + groupName + ".task", groupName));
                    groupActivated.setStateId(groupName);
                    if (activeGroupNames.contains(groupName)) {
                        groupActivated.setChecked(true);
                    } else {
                        groupActivated.setChecked(false);
                    }
                    groupActivated.addListener(Events.CheckChange, new Listener<MenuEvent>() {

                        public void handleEvent(MenuEvent be) {
                            String groupName = groupActivated.getStateId();
                            if (groupActivated.isChecked()) {
                                if (!activeGroupNames.contains(groupName)) {
                                    activeGroupNames.add(groupName);
                                }
                            } else {
                                activeGroupNames.remove(groupName);
                            }
                            pagingToolBar.refresh();
                        }
                    });
                    filterMenu.add(groupActivated);
                }
            }
        });
        filterButton.setMenu(filterMenu);

        filterButton.addStyleName("jobs-filter-combo");
        topToolBar.add(filterButton);

        topToolBar.add(new SeparatorToolItem());

        final CheckBox autoRefreshCheckBox = new CheckBox();
        autoRefreshCheckBox.addStyleName("jobs-auto-refresh");
        autoRefreshCheckBox.setValue(autoRefreshActivated);
        autoRefreshCheckBox.setBoxLabel(Messages.get("label.autoRefresh", "Auto-refresh every"));
        autoRefreshCheckBox.addListener(Events.Change, new Listener<FieldEvent>() {

            public void handleEvent(FieldEvent be) {
                Boolean autoRefreshEnabled = autoRefreshCheckBox.getValue();
                if (autoRefreshEnabled.booleanValue()) {
                    autoRefreshActivated = true;
                    stopAutoRefresh();
                    startAutoRefresh();
                } else {
                    autoRefreshActivated = false;
                    stopAutoRefresh();
                }
            }
        });
        topToolBar.add(autoRefreshCheckBox);

        final TextBox refreshIntervalTextBox = new TextBox();
        refreshIntervalTextBox.setWidth("3em");
        refreshIntervalTextBox.setText(Integer.toString(autoRefreshInterval));
        refreshIntervalTextBox.addChangeHandler(new ChangeHandler() {

            public void onChange(com.google.gwt.event.dom.client.ChangeEvent event) {
                String refreshTextValue = refreshIntervalTextBox.getText();
                int newRefreshInterval = -1;
                try {
                    newRefreshInterval = Integer.parseInt(refreshTextValue);
                } catch (NumberFormatException nfe) {
                    newRefreshInterval = -1;
                }
                if ((newRefreshInterval >= 5) && (newRefreshInterval <= 120)) {
                    autoRefreshInterval = newRefreshInterval;
                    if (autoRefreshActivated) {
                        stopAutoRefresh();
                        startAutoRefresh();
                    }
                } else {
                    refreshIntervalTextBox.setText(Integer.toString(autoRefreshInterval));
                }
            }
        });
        topToolBar.add(new WidgetComponent(refreshIntervalTextBox));

        Label secondsLabel = new Label(" " + Messages.get("label.seconds", "seconds"));
        topToolBar.add(secondsLabel);

        if (adminMode) {
	        topToolBar.add(new SeparatorToolItem());
			deleteButton = new Button(Messages.get("label.delete", "Delete"),
			        StandardIconsProvider.STANDARD_ICONS.delete(),
			        new SelectionListener<ButtonEvent>() {
				        @Override
				        public void componentSelected(ButtonEvent ce) {
					        showDeleteConfirmation(false);
				        }
			        });
	        deleteButton.disable();
	        topToolBar.add(deleteButton);

			topToolBar.add(new Button(Messages.get("label.deleteCompletedJobs",
			        "Delete all completed"), StandardIconsProvider.STANDARD_ICONS.delete(),
			        new SelectionListener<ButtonEvent>() {
				        @Override
				        public void componentSelected(ButtonEvent ce) {
					        showDeleteConfirmation(true);
				        }
			        }));
        }

        ContentPanel listPanel = new ContentPanel();
        listPanel.setFrame(true);
        listPanel.setCollapsible(false);
        listPanel.setAnimCollapse(false);
        // panel.setIcon(Resources.ICONS.table());
        // panel.setHeadingHtml("");
        listPanel.setHeaderVisible(false);
        listPanel.setLayout(new FitLayout());
        listPanel.add(grid);
        listPanel.setSize(600, 350);
        listPanel.setBottomComponent(pagingToolBar);
        listPanel.setTopComponent(topToolBar);
        grid.getAriaSupport().setLabelledBy(listPanel.getId());
        add(listPanel);

        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        add(listPanel, centerData);

        FormPanel detailPanel = new FormPanel();
        detailPanel.setBorders(true);
        detailPanel.setBodyBorder(true);
        detailPanel.setHeaderVisible(true);
        detailPanel.setHeadingHtml(Messages.get("label.detailed", "Details"));
        detailPanel.setScrollMode(Style.Scroll.AUTOY);
        detailPanel.setLabelWidth(100);
        detailsPanel = detailPanel;

        BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
        southData.setSplit(true);
        southData.setCollapsible(true);
        add(detailPanel, southData);

        addListener(Events.Detach, new Listener<ComponentEvent>() {

            public void handleEvent(ComponentEvent be) {
                stopAutoRefresh();
            }

        });
    }

    public void startAutoRefresh() {
        timer = new Timer() {
            public void run() {
                pagingToolBar.refresh();
                schedule(autoRefreshInterval * 1000);
            }
        };
        timer.run();
    }

    public void stopAutoRefresh() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    protected void showDeleteConfirmation(final boolean deleteAllCompleted) {
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
        final Dialog dialog = new Dialog();
        dialog.addStyleName(deleteAllCompleted ? "modal-deletealljob-confirm" : "modal-deletejob-confirm");
        dialog.setModal(true);
        dialog.setHeadingHtml(Messages.get("label.delete", "Delete"));
        dialog.setButtons(Dialog.YESNO);
        dialog.addText(deleteAllCompleted ? Messages.get("label.deleteCompletedJobs.confirm", "Do you really want to delete all completed jobs?") : Messages.get("label.delete.confirm", "Do you really want to delete the current selection?"));
        dialog.setHideOnButtonClick(true);
        dialog.addListener(Events.Hide, new Listener<WindowEvent>() {
            public void handleEvent(WindowEvent be) {
                if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.YES)) {
                	if (deleteAllCompleted) {
                        service.deleteAllCompletedJobs(new BaseAsyncCallback<Integer>() {
                            public void onApplicationFailure(Throwable caught) {
                                com.google.gwt.user.client.Window.alert(Messages.get("fm_fail") + "\n" + caught.getLocalizedMessage());
                                Log.error(Messages.get("fm_fail"), caught);
                            }
                            public void onSuccess(Integer result) {
                            	MessageBox.info(Messages.get("label.delete", "Delete"), Messages.getWithArgs("label.deleteCompletedJobs.success", "Deleted {0} completed jobs", new Object[] {result}), null);
                            	if (result != null && result > 0) {
                                    pagingToolBar.refresh();
                            	}
                            }
                        });
                	} else {
	                    for (GWTJahiaJobDetail jobDetail : selectedItems) {
	                        service.deleteJob(jobDetail.getName(), jobDetail.getGroup(), new BaseAsyncCallback<Boolean>() {

	                            public void onApplicationFailure(Throwable caught) {
	                                com.google.gwt.user.client.Window.alert(Messages.get("fm_fail") + "\n" + caught.getLocalizedMessage());
	                                Log.error(Messages.get("fm_fail"), caught);
	                            }

	                            public void onSuccess(Boolean result) {
	                                pagingToolBar.refresh();
	                            }
	                        });
	                    }
                	}
                }
            }
        });
        dialog.show();
    }

    public void addDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value != null) {
            TextField textField = new TextField();
            textField.setFieldLabel(Messages.get(labelKey, labelDefaultValue));
            textField.setReadOnly(true);
            if (value instanceof String) {
                textField.setValue(value);
            } else if (value instanceof Date) {
                textField.setValue(org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate((Date) value));
            } else {
                textField.setValue(value.toString());
            }
            detailsPanel.add(textField, new FormData("98%"));
        }
    }

    public void addTimeDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value instanceof Long) {
            Date date = new Date((Long) value);
            addDetail(labelKey, labelDefaultValue, date);
        } else {
            addDetail(labelKey, labelDefaultValue, value);
        }
    }

    public void updateDetails() {

        if (detailsPanel == null) {
            // maybe we clicked before it was created properly ?
            return;
        }

        if (selectedItems == null || selectedItems.size() == 0) {
            return;
        }

        detailsPanel.removeAll();
        if (selectedItems.size() == 1) {
            GWTJahiaJobDetail jobDetail = selectedItems.get(0);

            addDetail("label.description", "Description", jobDetail.getDescription());
            addDetail("label.status", "Status", jobDetail.getStatus());
            StringBuffer paths = new StringBuffer();
            for (String path : jobDetail.getTargetPaths()) {
                paths.append(path);
                paths.append(" ");
            }
            addDetail("label.targetPaths", "Target paths", paths.toString());
            addDetail("label.fileName", "File name", jobDetail.getFileName());
            addDetail("label.name", "Name", jobDetail.getName());
            addDetail("label.creationTime", "Creation time", jobDetail.getCreationTime());
            addDetail("label.user", "User key", jobDetail.getUser());
            addDetail("label.group", "Group", jobDetail.getGroup());
            // addDetail("label.jobLabel", "Job label", jobDetail.getLabel());
            // addDetail("label.className", "Job class", jobDetail.getJobClassName());
            addDetail("label.message", "Message", jobDetail.getMessage());
            addTimeDetail("label.beginTime", "Start time", jobDetail.getBeginTime());
            addTimeDetail("label.endTime", "End time", jobDetail.getEndTime());
            addDetail("label.duration", "Duration", jobDetail.getDuration());
            addDetail("label.locale", "Locale code", jobDetail.getLocale());
            addDetail("label.targetNodeIdentifier", "Target node identifier", jobDetail.getTargetNodeIdentifier());
            addDetail("label.targetAction", "Target action", jobDetail.getTargetAction());
            addDetail("label.targetWorkspace", "Target workspace", jobDetail.getTargetWorkspace());
        } else {
            detailsPanel.add(new HTML("<b>" + Messages.get("label.selectedJobCount", "Number of selected jobs") + " :</b> " + selectedItems.size()));
        }
        detailsPanel.layout();

    }

    static class LoadConfig extends BasePagingLoadConfig implements GroupingLoadConfig {
        public String getGroupBy() {
            return (String) get("groupBy");
        }

        public void setGroupBy(String groupBy) {
            set("groupBy", groupBy);
        }

    }
}

