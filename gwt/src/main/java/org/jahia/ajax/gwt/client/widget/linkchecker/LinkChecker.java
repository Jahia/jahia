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
import com.extjs.gxt.ui.client.widget.StatusBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaLinkCheckerStatus;
import org.jahia.ajax.gwt.client.service.linkchecker.LinkCheckerService;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 12:01:28
 */
public class LinkChecker extends ContentPanel {

    private ListStore<GWTJahiaCheckedLink> m_store;
    private Timer m_timer;
    private TextToolItem stop;
    private StatusBar m_status;
    private Grid<GWTJahiaCheckedLink> linkTable;

    public LinkChecker() {
        super(new FitLayout());
        setSize(980, 740);

        // data container initialization
        m_store = new ListStore<GWTJahiaCheckedLink>();
        
        linkTable = new Grid<GWTJahiaCheckedLink>(m_store, getHeaders());
        linkTable.addPlugin((RowExpander)linkTable.getColumnModel().getColumn(0));
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
                LinkCheckerService.App.getInstance().lookForCheckedLinks(new AsyncCallback<GWTJahiaLinkCheckerStatus>() {
                    public void onFailure(Throwable throwable) {
                        Log.error(throwable.toString());
                    }

                    public void onSuccess(GWTJahiaLinkCheckerStatus status) {
                        List<GWTJahiaCheckedLink> gwtJahiaCheckedLinks = status.getLinks();
                        for (GWTJahiaCheckedLink link: gwtJahiaCheckedLinks) {
                            if (!m_store.contains(link)) {
                                m_store.add(link);
                            }
                        }
                        if (!status.isActive()) {
                            Log.debug("polling over");
                            stop();
                            m_status.setMessage(Messages.getResource("lc_processed") + " "
                                            + status.getProcessed()
                                            + Messages.getResource("lc_linksfound") + " "
                                            + status.getFailed()
                                            + Messages.getResource("lc_invalid"));
                        } else {
                            m_status.showBusy(Messages.getResource("lc_checking")+"... "
                                            + Messages.getResource("lc_processed") + " "
                                            + status.getProcessed() + Messages.getResource("lc_of") +" "
                                            + status.getTotal() +" "
                                            + Messages.getResource("lc_linksfound") + " "
                                            + status.getFailed() + " "
                                            + Messages.getResource("lc_invalid"));
                        }
                    }
                });
            }
        };

        // ui layout
        setHeaderVisible(false);
        ToolBar toolBar = new ToolBar();
        stop = new TextToolItem(Messages.getResource("lc_stop"), new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent event) {
                stopPolling();
            }
        });
        stop.setEnabled(false);
        final TextToolItem csvExport = new TextToolItem(Messages.getResource("lc_exportcsv"), new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent toolBarEvent) {
                new CSVExporter(m_store.getModels()).show();
            }
        });
        csvExport.setEnabled(false);
        TextToolItem checkLinks = new TextToolItem(Messages.getResource("lc_checkLinks"), new SelectionListener<ToolBarEvent>() {
            public void componentSelected(ToolBarEvent event) {
                m_store.removeAll();
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

        m_status = new StatusBar();
        m_status.setMessage("idle");

        setBottomComponent(m_status);

        add(linkTable);
    }

    private void startPolling() {
        Log.debug("scheduled every 2 seconds");
        m_timer.scheduleRepeating(2000);
        m_status.showBusy("checking links");
    }

    private void stopPolling() {
        LinkCheckerService.App.getInstance().stopCheckingLinks(new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Log.error(throwable.toString());
                stop();
            }

            public void onSuccess(Object o) {
                Log.debug("cancelled successfully");
                stop();
            }
        });
    }

    private void stop() {
        m_timer.cancel();
        stop.setEnabled(false);
        m_status.clear();
        m_status.setMessage("done");
    }

    private ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();

        headerList
                .add(new RowExpander(
                        XTemplate
                                .create("<p><b>Link:</b> {link}</p><br/>"
                                        + "<p><b>Page:</b> <a href=\"{pageUrl}\" target=\"_blank\">{pageTitle} [{pageId}]</a></p><br/>"
                                        + "<p><b>Field:</b> {fieldType} [{fieldId}]</p><br/>"
                                        + "<p><b>Language:</b> {languageCode}</p><br/>"
                                        + "<p><b>Workflow state:</b> {workflowState}</p><br/>"
                                        + "<p><b>Error code:</b> {code} {codeText}</p><br/>"
                                        + "<p><b>Details:</b><br/>{errorDetails}</p><br/>"
                                        + "<p><b>Edit:</b> <a href=\"{updateUrl}\" target=\"_blank\">link</a></p>")));  
        
        ColumnConfig col = new ColumnConfig("link", Messages.getResource("lc_link"), 380);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("pageTitle", Messages.getResource("lc_pageTitle"), 200);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>(){
            public String render(GWTJahiaCheckedLink link, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GWTJahiaCheckedLink> store) {
                return "<a href=\"" + link.getPageUrl()
                        + "\" target=\"_blank\">" + link.getPageTitle() + " ["
                        + link.getPageId() + "]</a>";
            }});
        headerList.add(col);

        col = new ColumnConfig("languageCode", Messages.getResource("lc_language"), 60);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);

        col = new ColumnConfig("workflowState", Messages.getResource("lc_workflow"), 60);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink gwtJahiaCheckedLink, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaCheckedLink> gwtJahiaCheckedLinkListStore) {
                String[] ws = new String[]{Messages.getResource("lc_versioned"), Messages.getResource("lc_live"), Messages.getResource("lc_staging"), Messages.getResource("lc_notify")};
                String[] images = new String[]{"600", "111", "121", "130"};
                return "<img src=\"../../engines/images/icons/workflow/" + images[gwtJahiaCheckedLink.getWorkflowState()] + ".png\">&nbsp;" + ws[gwtJahiaCheckedLink.getWorkflowState()];
            }
        });

        headerList.add(col);

        col = new ColumnConfig("code", Messages.getResource("lc_code"), 50);
        col.setSortable(true);
        col.setResizable(true);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GWTJahiaCheckedLink> store) {
                return "<span title=\"" + link.getCodeText() + "\">"
                        + link.getCode() + "</span>";
            }
        });
        headerList.add(col);

        col = new ColumnConfig("edit", Messages.getResource("lc_edit"), 50);
        col.setSortable(false);
        col.setResizable(false);
        col.setRenderer(new GridCellRenderer<GWTJahiaCheckedLink>() {
            public String render(GWTJahiaCheckedLink link, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GWTJahiaCheckedLink> store) {
                return "<a href=\""
                        + link.getUpdateUrl()
                        + "\" target=\"_blank\"><img src=\""
                        + URL.getJahiaContext()
                        + "/gwt/resources/org/jahia/ajax/gwt/public/images/actions/update.png\""
                        + " height=\"16\" width=\"16\" alt=\"edit\"/></a>";
            }
        });
        headerList.add(col);
        
        return new ColumnModel(headerList);
    }

}
