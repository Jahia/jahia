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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryProcess;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * GWT panel that displays a list of workflow process and task history records.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowHistoryPanel extends LayoutContainer {

    /**
     * Initializes an instance of this class.
     * 
     * @param nodeId
     * @param locale
     */
    public WorkflowHistoryPanel(String nodeId, String locale) {
        super(new FitLayout());
        this.nodeId = nodeId;
        this.locale = locale;
    }

    private String nodeId;
    private String locale;

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setHeight(610);

        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<List<GWTJahiaWorkflowHistoryItem>> proxy = new RpcProxy<List<GWTJahiaWorkflowHistoryItem>>() {
            @Override
            protected void load(Object loadConfig, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> callback) {
                service.getWorkflowHistoryItems(nodeId, (GWTJahiaWorkflowHistoryItem) loadConfig, locale, callback);
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

        List<ColumnConfig> config = new ArrayList<ColumnConfig>();

        ColumnConfig column = new ColumnConfig("displayName", Messages.get("label.name", "Name"), 100);
        column.setRenderer(new TreeGridCellRenderer<GWTJahiaWorkflowHistoryItem>());
        config.add(column);

        column = new ColumnConfig("assignee", Messages.get("label.user", "User"), 100);
        config.add(column);

        column = new ColumnConfig("startDate", Messages.get("org.jahia.engines.processDisplay.tab.startdate", "Start date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("endDate", Messages.get("org.jahia.engines.processDisplay.tab.enddate", "End date"), 100);
        column.setDateTimeFormat(Formatter.DEFAULT_DATETIME_FORMAT);
        config.add(column);

        column = new ColumnConfig("duration", Messages.get("org.jahia.engines.processDisplay.column.duration", "Duration"), 100);
        column.setRenderer(new GridCellRenderer<GWTJahiaWorkflowHistoryItem>() {
            public Object render(GWTJahiaWorkflowHistoryItem historyItem, String property, ColumnData config, int rowIndex,
                    int colIndex, ListStore<GWTJahiaWorkflowHistoryItem> store, Grid<GWTJahiaWorkflowHistoryItem> grid) {
                Long duration = historyItem.getDuration();
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
                return display;
            }
        });
        config.add(column);

        ColumnModel cm = new ColumnModel(config);

        ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeaderVisible(false);
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        cp.setLayout(new FitLayout());
        cp.setFrame(true);

        TreeGrid<ModelData> tree = new TreeGrid<ModelData>(store, cm);
        tree.setStateful(true);
        tree.setBorders(true);
        tree.getStyle().setNodeOpenIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setNodeCloseIcon(StandardIconsProvider.STANDARD_ICONS.workflow());
        tree.getStyle().setLeafIcon(StandardIconsProvider.STANDARD_ICONS.workflowTask());
        tree.setAutoExpandColumn("displayName");
        tree.setTrackMouseOver(false);
        cp.add(tree);

        add(cp);
    }

}