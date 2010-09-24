package org.jahia.ajax.gwt.client.widget.job;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.widget.Linker;

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
 * todo : check for user name to display only our jobs (or if root all jobs), purge jobs UI, etc...
 */
public class JobListPanel extends LayoutContainer {

    private JobListWindow window;
    private ContentPanel detailsPanel;

    private Linker linker;

    private List<GWTJahiaJobDetail> selectedItems = null;

    public JobListPanel(JobListWindow window, Linker linker) {
        super(new BorderLayout());

        this.linker = linker;
        this.window = window;
        init();
    }

    private void init() {
        setBorders(false);
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<BasePagingLoadResult<GWTJahiaJobDetail>> proxy = new RpcProxy<BasePagingLoadResult<GWTJahiaJobDetail>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<BasePagingLoadResult<GWTJahiaJobDetail>> callback) {
                if (loadConfig == null) {
                    service.getJobs(0, Integer.MAX_VALUE, null, null, callback);
                } else if (loadConfig instanceof BasePagingLoadConfig) {
                    BasePagingLoadConfig pagingLoadConfig = (BasePagingLoadConfig) loadConfig;
                    int limit = pagingLoadConfig.getLimit();
                    int offset = pagingLoadConfig.getOffset();
                    Style.SortDir sortDir = pagingLoadConfig.getSortDir();
                    String sortField = pagingLoadConfig.getSortField();
                    service.getJobs(offset, limit, sortField, sortDir.name(), callback);
                } else {
                    callback.onSuccess(new BasePagingLoadResult<GWTJahiaJobDetail>(new ArrayList<GWTJahiaJobDetail>()));
                }
            }
        };

        // tree loader
        final PagingLoader<BasePagingLoadResult<ModelData>> loader = new BasePagingLoader<BasePagingLoadResult<ModelData>>(proxy);
        loader.setRemoteSort(true);

        // trees store
        final ListStore<GWTJahiaJobDetail> store = new ListStore<GWTJahiaJobDetail>(loader);

        final PagingToolBar toolBar = new PagingToolBar(50);
        toolBar.bind(loader);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("creationTime", Messages.get("org.jahia.engines.processDisplay.tab.startdate", "Start date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("type", Messages.get("label.type", "Type"), 100);
        config.add(column);

        column = new ColumnConfig("description", Messages.get("label.description", "Description"), 100);
        config.add(column);

        column = new ColumnConfig("status", Messages.get("label.status", "Status"), 100);
        config.add(column);

        column = new ColumnConfig("durationInSeconds", Messages.get("label.duration", "Duration (s)"), 100);
        config.add(column);

        column = new ColumnConfig("user", Messages.get("label.user", "User"), 100);
        column.setHidden(true);
        config.add(column);

        column = new ColumnConfig("message", Messages.get("label.message", "Message"), 100);
        column.setHidden(true);
        config.add(column);

        column = new ColumnConfig("group", Messages.get("label.group", "Group"), 100);
        column.setHidden(true);
        config.add(column);

        column = new ColumnConfig("name", Messages.get("label.name", "Name"), 100);
        column.setHidden(true);
        config.add(column);

        /*
        column = new ColumnConfig("duration", Messages.get("org.jahia.engines.processDisplay.column.duration", "Duration"), 100);
        column.setRenderer(new GridCellRenderer<GWTJahiaJobDetail>() {
            public Object render(GWTJahiaJobDetail historyItem, String property, ColumnData config, int rowIndex,
                                 int colIndex, ListStore<GWTJahiaJobDetail> store, Grid<GWTJahiaJobDetail> grid) {
                Long duration = historyItem.getStartTime().getTime();
                String display = "-";
                if (duration != null) {
                    long time = duration.longValue();
                    if (time < 1000) {
                        display = time + " " + Messages.get("label.milliseconds", "ms");
                    } else if (time < 1000 * 60L) {
                        display = ((long) (time / 1000)) + " " + Messages.get("label.seconds", "sec");
                    } else if (time < 1000 * 60 * 60L) {
                        display = ((long) (time / (1000 * 60L))) + " " + Messages.get("label.minutes", "min") + " "
                                + ((long) ((time % (1000 * 60L)) / 1000)) + " " + Messages.get("label.seconds", "sec");
                    } else {
                        display = ((long) (time / (1000 * 60 * 60L))) + " " + Messages.get("label_hours", "h") + " "
                                + ((long) ((time % (1000 * 60 * 60L)) / (1000 * 60L))) + " "
                                + Messages.get("label.minutes", "min");
                    }
                }
                return new Label(display);
            }
        });
        config.add(column);
        */

        ColumnModel cm = new ColumnModel(config);

        final Grid<GWTJahiaJobDetail> grid = new Grid<GWTJahiaJobDetail>(store, cm);
        grid.setBorders(true);
        grid.setAutoExpandColumn("description");
        grid.setTrackMouseOver(false);
        grid.setStateId("jobPagingGrid");
        grid.setStateful(true);
        grid.addListener(Events.Attach, new Listener<GridEvent<GWTJahiaJobDetail>>() {
            public void handleEvent(GridEvent<GWTJahiaJobDetail> be) {
                PagingLoadConfig config = new BasePagingLoadConfig();
                config.setOffset(0);
                config.setLimit(50);

                Map<String, Object> state = grid.getState();
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
                updateDetails();
            }
        });

        ContentPanel panel = new ContentPanel();
        panel.setFrame(true);
        panel.setCollapsible(false);
        panel.setAnimCollapse(false);
        // panel.setIcon(Resources.ICONS.table());
        // panel.setHeading("");
        panel.setHeaderVisible(false);
        panel.setLayout(new FitLayout());
        panel.add(grid);
        panel.setSize(600, 350);
        panel.setBottomComponent(toolBar);
        grid.getAriaSupport().setLabelledBy(panel.getId());
        add(panel);

        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        add(panel, centerData);

        ContentPanel detailPanel = new ContentPanel();
        detailPanel.setBorders(true);
        detailPanel.setBodyBorder(true);
        detailPanel.setHeaderVisible(true);
        detailPanel.setHeading(Messages.get("label.details", "Details"));
        detailPanel.setScrollMode(Style.Scroll.AUTOY);
        detailsPanel = detailPanel;

        BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
        southData.setSplit(true);
        southData.setCollapsible(true);
        add(detailPanel, southData);

    }

    public void addDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value != null) {
            if (value instanceof String) {
                detailsPanel.add(new HTML("<b>" + Messages.get(labelKey, labelDefaultValue) + ":</b> " + value));
            } else if (value instanceof Date) {
                detailsPanel.add(new HTML("<b>" + Messages.get(labelKey, labelDefaultValue) + ":</b> " + org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate((Date) value)));
            } else {
                detailsPanel.add(new HTML("<b>" + Messages.get(labelKey, labelDefaultValue) + ":</b> " + value.toString()));
            }
        }
    }

    public void addTimeDetail(String labelKey, String labelDefaultValue, Object value) {
        if (value == null) {
            addDetail(labelKey, labelDefaultValue, value);
        } else if (value instanceof Long) {
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
            for (String path : jobDetail.getRelatedPaths()) {
                paths.append(path);
                paths.append(" ");
            }
            addDetail("label.relatedPaths", "Related paths", paths.toString());
            addDetail("label.name", "Name", jobDetail.getName());
            addDetail("label.creationTime", "Creation time", jobDetail.getCreationTime());
            addDetail("label.user", "User key", jobDetail.getUser());
            addDetail("label.jobType", "Job type", jobDetail.getType());
            addDetail("label.group", "Group", jobDetail.getGroup());
            addDetail("label.jobLabel", "Job label", jobDetail.getLabel());
            addDetail("label.className", "Job class", jobDetail.getJobClassName());
            addDetail("label.message", "Message", jobDetail.getMessage());
            addTimeDetail("label.beginTime", "Start time", jobDetail.getBeginTime());
            addTimeDetail("label.endTime", "End time", jobDetail.getEndTime());
            addDetail("label.durationInSeconds", "Duration (seconds)", jobDetail.getDurationInSeconds());
            addDetail("label.locale", "Locale code", jobDetail.getLocale());
        } else {
            int nbJobs = 0;

            for (GWTJahiaJobDetail jobDetail : selectedItems) {
                nbJobs++;
            }
            detailsPanel.add(new HTML("<b>" + Messages.get("label.selectedJobCount", "Number of selected jobs") + " :</b> " + nbJobs));
        }
        detailsPanel.layout();

    }

}
