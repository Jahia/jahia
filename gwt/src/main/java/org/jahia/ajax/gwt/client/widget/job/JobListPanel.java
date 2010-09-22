package org.jahia.ajax.gwt.client.widget.job;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Sep 21, 2010
 * Time: 12:28:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobListPanel extends LayoutContainer {

    private JobListWindow window;

    private Linker linker;

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
        RpcProxy<List<GWTJahiaJobDetail>> proxy = new RpcProxy<List<GWTJahiaJobDetail>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaJobDetail>> callback) {
                if (loadConfig == null) {
                    service.getAllJobs(callback);
                    /*
                } else if (loadConfig instanceof GWTJahiaWorkflowHistoryProcess) {
                    final GWTJahiaWorkflowHistoryProcess process = (GWTJahiaWorkflowHistoryProcess) loadConfig;
                    service.getWorkflowHistoryTasks(process.getProvider(), process.getProcessId(), locale, callback);
                    */
                } else {
                    callback.onSuccess(new ArrayList<GWTJahiaJobDetail>());
                }
            }
        };

        // tree loader
        final TreeLoader<GWTJahiaJobDetail> loader = new BaseTreeLoader<GWTJahiaJobDetail>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaJobDetail parent) {
                return false;
            }
        };

        // trees store
        final TreeStore<GWTJahiaJobDetail> store = new TreeStore<GWTJahiaJobDetail>(loader);

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("creationTime", Messages.get("org.jahia.engines.processDisplay.tab.startdate", "Start date"), 100);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaJobDetail>());
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("type", Messages.get("label.type", "Type"), 100);
        config.add(column);

        column = new ColumnConfig("user", Messages.get("label.user", "User"), 100);
        config.add(column);

        column = new ColumnConfig("description", Messages.get("label.description", "Description"), 100);
        config.add(column);

        column = new ColumnConfig("status", Messages.get("label.status", "Status"), 100);
        config.add(column);

        column = new ColumnConfig("message", Messages.get("label.message", "Message"), 100);
        config.add(column);

        column = new ColumnConfig("group", Messages.get("label.group", "Group"), 100);
        config.add(column);

        column = new ColumnConfig("name", Messages.get("label.name", "Name"), 100);
        config.add(column);

        column = new ColumnConfig("endDate", Messages.get("org.jahia.engines.processDisplay.tab.enddate", "End date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
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

        TreeGrid<GWTJahiaJobDetail> tree = new TreeGrid<GWTJahiaJobDetail>(store, cm);
        tree.setStateful(true);
        tree.setBorders(true);
        tree.getStyle().setNodeOpenIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setNodeCloseIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setLeafIcon(StandardIconsProvider.STANDARD_ICONS.workflowTask());
        tree.setAutoExpandColumn("description");
        tree.getTreeView().setRowHeight(25);
        tree.setTrackMouseOver(false);
        tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaJobDetail>() {
            @Override
            protected void onSelectChange(GWTJahiaJobDetail model, boolean select) {
                super.onSelectChange(model, select);
            }
        });
        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        add(tree, centerData);

        ContentPanel detailPanel = new ContentPanel();
        detailPanel.setBorders(true);
        detailPanel.setBodyBorder(true);
        detailPanel.setHeaderVisible(true);
        detailPanel.setHeading(Messages.get("label.details", "Details"));
        detailPanel.add(createDetails());

        BorderLayoutData southData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
        southData.setSplit(true);
        southData.setCollapsible(true);
        add(detailPanel, southData);

    }

    public FlowPanel createDetails() {
        FlowPanel infoPanel;
        infoPanel = new FlowPanel();
        infoPanel.addStyleName("infoPane");
        add(infoPanel);

        Grid g = new Grid(1, 2);
        g.setCellSpacing(10);
        FlowPanel flowPanel = new FlowPanel();

        /*
        if (!engine.isMultipleSelection()) {
            final GWTJahiaNode selectedNode = engine.getNode();

            String preview = selectedNode.getPreview();
            if (preview != null) {
                g.setWidget(0, 0, new Image(preview));
            }
            String name = selectedNode.getName();
            if (name != null) {
                flowPanel.add(new HTML("<b>" + Messages.get("label.name") + ":</b> " + name));
            }
            String path = selectedNode.getPath();
            if (path != null) {
                flowPanel.add(new HTML("<b>" + Messages.get("label.path") + ":</b> " + path));
            }
            String id = selectedNode.getUUID();
            if (id != null) {
                flowPanel.add(new HTML("<b>" + Messages.get("label.id", "ID") + ":</b> " + id));
            }
            if (selectedNode.isFile()) {
                Long s = selectedNode.getSize();
                if (s != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.size") + ":</b> " +
                            Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                }
            }
            Date date = selectedNode.get("jcr:lastModified");
            if (date != null) {
                flowPanel.add(new HTML("<b>" + Messages.get("label.lastModif") + ":</b> " +
                        org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
            }
            if (selectedNode.isLocked() && selectedNode.getLockOwner() != null) {
                flowPanel.add(new HTML(
                        "<b>" + Messages.get("info.lock.label") + ":</b> " + selectedNode.getLockOwner()));
            }

            flowPanel.add(new HTML("<b>" + Messages.get("nodes.label", "Types") + ":</b> " + selectedNode.getNodeTypes()));
            flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.jcr.edit.tags.tab", "Tags") + ":</b> " + selectedNode.getTags() != null ? selectedNode.getTags() : ""));
        } else {
            int numberFiles = 0;
            int numberFolders = 0;
            long size = 0;

            for (GWTJahiaNode selectedNode : engine.getNodes()) {
                if (selectedNode.isFile()) {
                    numberFiles++;
                    size += selectedNode.getSize();
                } else {
                    numberFolders++;
                }
            }
            flowPanel.add(new HTML("<b>" + Messages.get("info.nbFiles.label") + " :</b> " + numberFiles));
            flowPanel.add(new HTML("<b>" + Messages.get("info.nbFolders.label") + " :</b> " + numberFolders));
            flowPanel.add(new HTML("<b>" + Messages.get("info.totalSize.label") + " :</b> " +
                    org.jahia.ajax.gwt.client.util.Formatter.getFormattedSize(size)));
        }
        */
        g.setWidget(0, 1, flowPanel);
        infoPanel.add(g);
        return flowPanel;
    }

}
