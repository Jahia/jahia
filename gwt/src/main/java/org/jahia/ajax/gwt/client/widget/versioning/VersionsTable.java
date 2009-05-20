/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.versioning;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.versioning.RPCVersioningServiceAsync;
import org.jahia.ajax.gwt.client.service.versioning.RPCVersioningService;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 14 juil. 2008
 * Time: 16:35:49
 */
public class VersionsTable extends TopRightComponent {

    private String versionableUUID;
    private ContentPanel m_component;
    private Table m_table;
    private PagingToolBar pagingToolBar;
    private ListStore<GWTJahiaVersion> store = new ListStore<GWTJahiaVersion>();
    private TableBinder<GWTJahiaVersion> binder;
    private BasePagingLoader loader;
    private int nbItemsPerPage = 20;
    private int maxVersions = Integer.MAX_VALUE;
    public VersionsTable(String versionableUUID) {
        this.versionableUUID = versionableUUID;
    }

    /**
     * Create UI
     */
    public void createUI() {
        m_component = new ContentPanel(new FitLayout());
        m_table = new Table(getHeaders());
        m_table.setHorizontalScroll(true);
        m_table.setWidth(400);
        m_table.setHeight(400);
        m_table.setVerticalLines(true);
        //m_table.setAutoWidth(true);
        final RPCVersioningServiceAsync service = RPCVersioningService.App.getInstance();

        final RpcProxy proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaVersion>>() {
            @Override
            public void load(PagingLoadConfig pageConfig, AsyncCallback<PagingLoadResult<GWTJahiaVersion>> callback) {
                int offset = pageConfig.getOffset();
                String sortParameter = pageConfig.getSortInfo().getSortField();
                boolean isAscending = pageConfig.getSortInfo().getSortDir().equals(Style.SortDir.ASC);

                if (sortParameter == null) {
                    sortParameter = GWTJahiaVersion.VERSION_LABEL;
                }

                GWTJahiaPageContext page = new GWTJahiaPageContext();
                page.setPid(JahiaGWTParameters.getPID());
                page.setMode(JahiaGWTParameters.getOperationMode());

                // make the ajax call
                service.getRevisions(page,versionableUUID,JahiaGWTParameters.getLanguage(), true, false, true, true, 
                        offset, sortParameter, isAscending, nbItemsPerPage, callback);
            }
        };

        // loader
        loader = new BasePagingLoader<PagingLoadConfig, BasePagingLoadResult>(proxy);
        loader.setRemoteSort(true);

        // store
        store = new ListStore<GWTJahiaVersion>(loader);

        // binder
        binder = new TableBinder<GWTJahiaVersion>(m_table, store);


        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setFrame(true);
        contentPanel.setCollapsible(false);
        contentPanel.setAnimCollapse(false);
        contentPanel.setHeaderVisible(false);
        contentPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        contentPanel.setLayout(new FitLayout());
        //contentPanel.setAutoWidth(true);
        contentPanel.setWidth(500);
        //contentPanel.setAutoHeight(true);
        // add process job table
        contentPanel.add(m_table);

        // add table to main contentPanel
        m_component.add(contentPanel);

        // bottom component
        pagingToolBar = new PagingToolBar(nbItemsPerPage);
        pagingToolBar.bind(loader);
        contentPanel.setBottomComponent(pagingToolBar);

        //add table item selected item
        binder.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaVersion>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaVersion> event) {
                if (getLinker()!=null){
                    getLinker().onTableItemSelected();
                }
            }
        });

        // load after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                refresh();
            }
        });
    }

    public void setContent(Object root) {
        refresh();
    }


    public void clearTable() {
        binder.getStore().removeAll();
    }

    /**
     * Get selected item
     *
     * @return
     */
    public Object getSelection() {
        List<GWTJahiaVersion> elts = binder.getSelection();
        if (elts != null && elts.size() > 0) {
            return elts.get(0);
        } else {
            return null;
        }
    }

    /**
     * Refresh selection
     */
    public void refresh() {
        pagingToolBar.setPageSize(this.nbItemsPerPage);
        loader.load(0, this.maxVersions);
    }

    /**
     * Get main component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * Get headers
     *
     * @return
     */
    private static TableColumnModel getHeaders() {
        final List<TableColumn> headerList = new ArrayList<TableColumn>();

        //version label
        String columnName = GWTJahiaVersion.VERSION_LABEL;
        TableColumn col = new TableColumn(columnName, Messages.getNotEmptyResource("versioning_"+ GWTJahiaVersion.VERSION_LABEL,
                "Version label"), .2f);
        col.setMinWidth(100);
        headerList.add(col);

        //workflow state
        //columnName = Version.WORKFLOW_STATE;
        //col = new TableColumn(Version.WORKFLOW_STATE, columnName, .2f);
        //col.setMinWidth(80);
        //headerList.add(col);

        //author
        columnName = GWTJahiaVersion.AUTHOR;
        col = new TableColumn(columnName, Messages.getNotEmptyResource("versioning_"+ GWTJahiaVersion.AUTHOR,"Author"), .2f);
        col.setMinWidth(100);
        headerList.add(col);

        //date
        columnName = GWTJahiaVersion.DATE;
        col = new TableColumn(columnName, Messages.getNotEmptyResource("versioning_"+ GWTJahiaVersion.DATE,"Version date"), .25f);
        col.setMinWidth(150);
        headerList.add(col);

        //lang
        columnName = GWTJahiaVersion.LANG;
        col = new TableColumn(columnName, Messages.getNotEmptyResource("versioning_"+ GWTJahiaVersion.LANG,"Language"), .15f);
        col.setMinWidth(80);
        headerList.add(col);

        return new TableColumnModel(headerList);
    }

}