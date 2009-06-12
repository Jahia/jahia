/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.linkchecker;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.jahia.ajax.gwt.client.service.linkchecker.LinkCheckerService;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 12:01:28
 */
public class LinkChecker extends ContentPanel {

    private ListStore<GWTJahiaCheckedLink> m_store;
    private Timer m_timer;
    private TextToolItem stop;

    public LinkChecker() {
        super(new FitLayout());
        setSize(980, 740);

        // data container initialization
        m_store = new ListStore<GWTJahiaCheckedLink>();
        final Grid<GWTJahiaCheckedLink> linkTable = new Grid<GWTJahiaCheckedLink>(m_store, getHeaders());
        linkTable.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        linkTable.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                GWTJahiaCheckedLink selection = linkTable.getSelectionModel().getSelectedItem();
                if (selection != null) {
                    Window.open(selection.getPageUrl(), "_blank", "");
                }
            }
        });

        m_timer = new Timer() {
            public void run() {
                Log.debug("polling...");
                LinkCheckerService.App.getInstance().lookForCheckedLinks(new AsyncCallback<List<GWTJahiaCheckedLink>>() {
                    public void onFailure(Throwable throwable) {
                        Log.error(throwable.toString());
                    }

                    public void onSuccess(List<GWTJahiaCheckedLink> gwtJahiaCheckedLinks) {
                        for (GWTJahiaCheckedLink link: gwtJahiaCheckedLinks) {
                            if (!m_store.contains(link)) {
                                m_store.add(link);
                            }
                        }
                    }
                });
            }
        };

        // ui layout
        setHeaderVisible(false);
        ToolBar toolBar = new ToolBar();
        stop = new TextToolItem("Stop", new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent event) {
                stopPolling();
            }
        });
        stop.setEnabled(false);
        final TextToolItem csvExport = new TextToolItem("Export as CSV", new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent toolBarEvent) {
                new CSVExporter(m_store.getModels()).show();
            }
        });
        csvExport.setEnabled(false);
        TextToolItem checkLinks = new TextToolItem("Check links", new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent event) {
                LinkCheckerService.App.getInstance().checkLinks(new AsyncCallback<Boolean>() {
                    public void onFailure(Throwable throwable) {
                        Log.error(throwable.toString());
                    }

                    public void onSuccess(Boolean o) {
                        if (o != null && o.booleanValue()) {
                            Log.debug("starting polling...");
                            stop.setEnabled(true);
                            csvExport.setEnabled(true);
                            startPolling();
                        } else {
                            Window.alert("Access denied, only admin members are allowed.");
                        }
                    }
                });
            }
        });

        toolBar.add(checkLinks);
        toolBar.add(stop);
        toolBar.add(csvExport);

        setTopComponent(toolBar);
        add(linkTable);
    }

    private void startPolling() {
        Log.debug("scheduled every second");
        m_timer.scheduleRepeating(1000);
    }

    private void stopPolling() {
        LinkCheckerService.App.getInstance().stopCheckingLinks(new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Log.error(throwable.toString());
                m_timer.cancel();
                stop.setEnabled(false);
            }

            public void onSuccess(Object o) {
                Log.debug("cancelled successfully");
                m_timer.cancel();
                stop.setEnabled(false);
            }
        });

    }

    private ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        ColumnConfig col = new ColumnConfig("link", "Link", 500);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("pageTitle", "Page title", 300);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("code", "Code", 60);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        return new ColumnModel(headerList);
    }

}
