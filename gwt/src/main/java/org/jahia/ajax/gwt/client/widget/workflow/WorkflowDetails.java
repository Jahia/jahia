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

package org.jahia.ajax.gwt.client.widget.workflow;

import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResultItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowHistoryEntry;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowManager;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.table.Table;
import com.extjs.gxt.ui.client.widget.table.TableColumn;
import com.extjs.gxt.ui.client.widget.table.TableColumnModel;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 29 juil. 2008 - 14:36:36
 */
public class WorkflowDetails extends BottomRightComponent {

    private ContentPanel m_component ;
    private AsyncTabItem validation;
    private AsyncTabItem history;
    private TabPanel tabs;
    private GWTJahiaWorkflowElement selection = null ;

    public WorkflowDetails() {
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        tabs = new TabPanel();
        validation = new AsyncTabItem();
        validation.setText(WorkflowManager.getResource("wf_validation"));
        validation.setLayout(new TableLayout(2));
        validation.setScrollMode(Style.Scroll.AUTO);
        validation.addStyleName("raw-content");
        history = new AsyncTabItem();
        history.setText(WorkflowManager.getResource("wf_history"));
        history.setLayout(new FitLayout());
        history.setScrollMode(Style.Scroll.AUTO);
        history.addStyleName("raw-content");
        tabs.add(validation) ;
        tabs.add(history) ;
        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (selection != null) {
                    fillCurrentTab();
                }
            }
        });
        m_component.add(tabs) ;
    }

    public void clear() {
        m_component.setHeading("&nbsp;") ;
        validation.removeAll() ;
        validation.setProcessed(false);
        history.removeAll() ;
        history.setProcessed(false);
        selection = null ;
    }

    public void fillData(Object selectedItem) {
        clear();
        selection = (GWTJahiaWorkflowElement) selectedItem ;
        if (selection != null) {
            m_component.setHeading(selection.getTitle());
            fillCurrentTab();
        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (currentTab == validation) {
            displayValidation();
        } else if (currentTab == history) {
            displayHistory();            
        }
    }

    private void displayValidation() {
        if (!validation.isProcessed()) {
            validation.removeAll();
            FlowPanel flowPanel = new FlowPanel();
            validation.add(flowPanel);
            Map<String, GWTJahiaNodeOperationResult> valid = selection.getValidation() ;
            for (String lang: valid.keySet()) {
                GWTJahiaNodeOperationResult validForLang = valid.get(lang) ;
                if (!validForLang.getErrorsAndWarnings().isEmpty()) {
                    List<String> errs = new ArrayList<String>() ;
                    List<String> wars = new ArrayList<String>() ;
                    // remove duplicates
                    for (GWTJahiaNodeOperationResultItem err : validForLang.getErrorsAndWarnings()) {
                        String message = err.getMessage() ;
                        if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.ERROR && !errs.contains(message)) {
                            errs.add(message) ;
                        } else if (err.getLevel().intValue() == GWTJahiaNodeOperationResultItem.WARNING && !wars.contains(message)) {
                            wars.add(message) ;
                        }
                    }
                    flowPanel.add(new HTML("<div><span class=\"flag_" + lang + "\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><span>&nbsp;" + lang + "</span></div>"));
                    StringBuilder sbErrs = new StringBuilder("<ul>");
                    for (String errMessage: errs) {
                        sbErrs.append("\n<li>").append(errMessage).append("</li>");
                    }
                    sbErrs.append("\n</ul>");
                    StringBuilder sbWars = new StringBuilder("<ul>");
                    for (String warMessage: wars) {
                        sbWars.append("\n<li>").append(warMessage).append("</li>");
                    }
                    sbWars.append("\n</ul>");
                    flowPanel.add(new HTML(new StringBuilder(sbErrs.toString()).append("\n").append(sbWars.toString()).toString())) ;
                }
            }
            validation.setProcessed(true);
            validation.layout() ;
        }
    }

    private void displayHistory() {
        if (!history.isProcessed()) {
            ArrayList<TableColumn> headerList = new ArrayList<TableColumn>();
            TableColumn col = new TableColumn("date", "Date", 200) ;
            headerList.add(col) ;

            col = new TableColumn("user", WorkflowManager.getResource("wf_user"), 80) ;
            headerList.add(col) ;

            col = new TableColumn("action", WorkflowManager.getResource("wf_action"), 100) ;
            headerList.add(col) ;

            col = new TableColumn("language", WorkflowManager.getResource("wf_language"), 51) ;
            headerList.add(col) ;

            col = new TableColumn("comment", WorkflowManager.getResource("wf_comment"), 260) ;
            headerList.add(col) ;

            Log.debug("Resetting column model...");

            Table table = new Table((new TableColumnModel(headerList)));
            table.setBorders(false);
            history.add(table) ;
            table.setBulkRender(false);
            table.setHorizontalScroll(true) ;



            // data proxy
            RpcProxy<ListLoadConfig, ListLoadResult<GWTJahiaWorkflowHistoryEntry>> proxy = new RpcProxy<ListLoadConfig, ListLoadResult<GWTJahiaWorkflowHistoryEntry>>() {
                protected void load(ListLoadConfig listLoadConfig, AsyncCallback<ListLoadResult<GWTJahiaWorkflowHistoryEntry>> listLoadResultAsyncCallback) {
                    WorkflowService.App.getInstance().getHistory(selection, listLoadResultAsyncCallback);
                }
            };

            // tree loader
            ListLoader loader = new BaseListLoader<ListLoadConfig, ListLoadResult<GWTJahiaWorkflowHistoryEntry>>(proxy);
            ListStore<GWTJahiaWorkflowHistoryEntry> store = new ListStore<GWTJahiaWorkflowHistoryEntry>(loader);
            TableBinder binder = new TableBinder<GWTJahiaWorkflowHistoryEntry>(table, store) ;
            binder.init() ;

            loader.load();
            history.setProcessed(true);
            history.layout() ;

        }

    }


    public Component getComponent() {
        return m_component ;
    }

}
