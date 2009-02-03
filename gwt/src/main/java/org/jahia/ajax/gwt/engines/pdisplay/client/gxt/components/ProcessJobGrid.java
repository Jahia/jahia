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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.pdisplay.client.gxt.components;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.engines.pdisplay.client.ProcessDisplayEntryPoint;
import org.jahia.ajax.gwt.engines.pdisplay.client.ProcessDisplayService;
import org.jahia.ajax.gwt.engines.pdisplay.client.ProcessDisplayServiceAsync;
import org.jahia.ajax.gwt.engines.pdisplay.client.gxt.ProcessDisplayPanel;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.engines.pdisplay.client.bean.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;

import java.util.*;

/**
 * User: jahia
 * Date: 14 juil. 2008
 * Time: 16:35:49
 */
public class ProcessJobGrid extends TopRightComponent {
    private ContentPanel m_component;
    private ColumnModel m_columnModel;
    private PagingToolBar pagingToolBar;
    private Grid<GWTJahiaProcessJob> grid;
    private BasePagingLoader loader;

    public ProcessJobGrid() {
    }

    /**
     * Create UI
     */
    public void createUI() {
        m_component = new ContentPanel(new FitLayout());
        m_columnModel = getHeaders();
        final ProcessDisplayServiceAsync processDisplayService = ProcessDisplayService.App.getInstance();

        final RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaProcessJob>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaProcessJob>>() {
            @Override
            public void load(PagingLoadConfig pageConfig, AsyncCallback<PagingLoadResult<GWTJahiaProcessJob>> callback) {
                int offset = pageConfig.getOffset();
                String sortParameter = pageConfig.getSortInfo().getSortField();
                boolean isAscending = pageConfig.getSortInfo().getSortDir().equals(Style.SortDir.ASC);

                if (sortParameter == null) {
                    sortParameter = GWTJahiaProcessJob.START;
                    isAscending = true;
                }

                // make the ajax call
                processDisplayService.findGWTProcessJobs(offset, sortParameter, isAscending, callback);

            }
        };

        // loader
        loader = new BasePagingLoader<PagingLoadConfig, PagingLoadResult<GWTJahiaProcessJob>>(proxy);
        loader.setRemoteSort(true);

        // store
        GroupingStore<GWTJahiaProcessJob> groupingStore = new GroupingStore<GWTJahiaProcessJob>(loader);
        groupingStore.setStoreSorter(new StoreSorter<GWTJahiaProcessJob>() {
            @Override
            public int compare(Store<GWTJahiaProcessJob> gwtJahiaProcessJobStore, GWTJahiaProcessJob gwtJahiaProcessJob, GWTJahiaProcessJob gwtJahiaProcessJob1, String s) {
                if (s.equalsIgnoreCase(GWTJahiaProcessJob.STATUS)) {
                    String status = gwtJahiaProcessJob.getJobStatus();
                    String status1 = gwtJahiaProcessJob1.getJobStatus();

                    String orderedStatus = "" + ProcessDisplayPanel.STATUS_OPTIONS_NAMES.length;
                    String orderedStatus1 = "" + ProcessDisplayPanel.STATUS_OPTIONS_NAMES.length;
                    int index = 0;
                    for (String currentStatus : ProcessDisplayPanel.STATUS_OPTIONS_VALUES) {
                        if (status.equalsIgnoreCase(currentStatus)) {
                            orderedStatus = "" + index;
                        }
                        if (status1.equalsIgnoreCase(currentStatus)) {
                            orderedStatus1 = "" + index;
                        }
                        index++;
                    }
                    return orderedStatus.compareTo(orderedStatus1);

                }
                return super.compare(gwtJahiaProcessJobStore, gwtJahiaProcessJob, gwtJahiaProcessJob1, s);
            }
        });
        groupingStore.groupBy(GWTJahiaProcessJob.STATUS);

        // binder
        grid = new Grid<GWTJahiaProcessJob>(groupingStore, m_columnModel);

        GroupingView view = new GroupingView();
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            public String render(GroupColumnData data) {
                String f = m_columnModel.getColumnById(data.field).getHeader();
                return f + ": " + data.group + " (" + data.models.size()+")";
            }
        });
        grid.setView(view);

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);
        contentPanel.setAnimCollapse(false);
        contentPanel.setHeaderVisible(false);
        contentPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        contentPanel.setLayout(new FitLayout());

        // add process job table
        contentPanel.add(grid);


        // bottom component
        pagingToolBar = new PagingToolBar(getGWTJahiaProcessJobPreference().getJobsPerPage());
        pagingToolBar.bind(loader);
        contentPanel.setBottomComponent(pagingToolBar);

        //add table item selected item
        grid.addListener(Events.RowClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                getLinker().onTableItemSelected();
            }
        });

        // load after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                refresh();
            }
        });

        // add table to main contentPanel

        m_component.add(contentPanel);
    }

    public void initContextMenu() {
       
    }

    public void setContent(Object root) {
        refresh();
    }


    public void clearTable() {
        grid.getStore().removeAll();
    }

    /**
     * Get selected item
     *
     * @return
     */
    public Object getSelection() {
        Log.debug("********************"+getSelectionModel().getSelectedItem());
        return getSelectionModel().getSelectedItem();
    }

    public GWTJahiaProcessJob getSelectedGWTJahiaProcessJob() {
        return getSelectionModel().getSelectedItem();
    }

    /**
     * get Selection model
     *
     * @return
     */
    private GridSelectionModel<GWTJahiaProcessJob> getSelectionModel() {
        return grid.getSelectionModel();
    }

    /**
     * Refresh selection
     */
    public void refresh() {
        pagingToolBar.setPageSize(getGWTJahiaProcessJobPreference().getJobsPerPage());
        loader.load(0, getGWTJahiaProcessJobPreference().getMaxJobs());

    }

    /**
     * Get main component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    private GWTJahiaProcessJobPreference getGWTJahiaProcessJobPreference() {
        return getPdisplayBrowserLinker().getGwtJahiaProcessJobPreference();
    }

    /**
     * Get PdisplayBrowserLinker
     *
     * @return
     */
    private ProcessdisplayBrowserLinker getPdisplayBrowserLinker() {
        return ((ProcessdisplayBrowserLinker) getLinker());
    }

    private ProcessJobTopBar getProcessJobTopBar() {
        return (ProcessJobTopBar) getLinker().getTopObject();
    }

    /**
     * Get headers
     *
     * @return
     */
    private static ColumnModel getHeaders() {
        final List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();

        //status
        String columnName = ProcessDisplayEntryPoint.getResource("pd_column_status");
        ColumnConfig col = new ColumnConfig(GWTJahiaProcessJob.STATUS, columnName, 100);
        col.setRenderer(new GridCellRenderer<GWTJahiaProcessJob>() {
            public String render(GWTJahiaProcessJob gwtJahiaProcessJob, String property, ColumnData columnData, int rowIndex, int colIndex, ListStore<GWTJahiaProcessJob> gwtJahiaProcessJobListStore) {

                String rowStyle = "gwt-pdisplay-table-row-status-" + gwtJahiaProcessJob.getJobStatus();
                return "<span class='" + rowStyle + "'>" + gwtJahiaProcessJob.getJobStatus() + "</span>";
            }


        });
        col.setSortable(false);
        headerList.add(col);

        columnName = ProcessDisplayEntryPoint.getResource("pd_column_title");
        col = new ColumnConfig(GWTJahiaProcessJob.JOB_TITLE, columnName, 100);
        headerList.add(col);

        columnName = ProcessDisplayEntryPoint.getResource("pd_column_sitekey");
        col = new ColumnConfig(GWTJahiaProcessJob.JOB_SITE_KEY, columnName, 100);
        headerList.add(col);

        //owner
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_owner");
        col = new ColumnConfig(GWTJahiaProcessJob.OWNER, columnName, 100);
        headerList.add(col);

        //type
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_type");
        col = new ColumnConfig(GWTJahiaProcessJob.TYPE, columnName, 100);
        col.setRenderer(new GridCellRenderer<GWTJahiaProcessJob>() {
            public String render(GWTJahiaProcessJob gwtJahiaProcessJob, String property, ColumnData columnData, int rowIndex, int colIndex, ListStore<GWTJahiaProcessJob> gwtJahiaProcessJobListStore) {
                String value = gwtJahiaProcessJob.getJobType();
                for (int i = 0; i < ProcessDisplayPanel.TYPE_OPTIONS_VALUES.length; i++) {
                    if (value.equalsIgnoreCase(ProcessDisplayPanel.TYPE_OPTIONS_VALUES[i])) {
                        return ProcessDisplayPanel.TYPE_OPTIONS_NAMES[i];
                    }
                }
                return value;
            }
        });
        headerList.add(col);

        //created
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_created");
        col = new ColumnConfig(GWTJahiaProcessJob.CREATED, columnName, 100);
        headerList.add(col);

        //start
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_start");
        col = new ColumnConfig(GWTJahiaProcessJob.START, columnName, 100);
        headerList.add(col);

        //end
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_end");
        col = new ColumnConfig(GWTJahiaProcessJob.END, columnName, 100);
        headerList.add(col);

        //duration
        columnName = ProcessDisplayEntryPoint.getResource("pd_column_duration");
        col = new ColumnConfig(GWTJahiaProcessJob.DURATION, columnName, 100);
        headerList.add(col);


        return new ColumnModel(headerList);
    }


}
