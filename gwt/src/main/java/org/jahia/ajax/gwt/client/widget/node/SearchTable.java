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
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:53:43
 */
public class SearchTable extends TopRightComponent {

    private ContentPanel m_component ;
    private Grid<GWTJahiaNode> table ;
    private ListStore<GWTJahiaNode> store ;

    private ManagerConfiguration config;

    public SearchTable(ManagerConfiguration config) {
        this.config = config;
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBorders(false);
        m_component.setBodyBorder(false);
        SearchField searchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                setSearchContent(value);
            }

            public void onSaveButtonClicked(String value) {
                // ... no save button
            }
        };
        m_component.setHeaderVisible(false);
        m_component.setTopComponent(searchField);
        store = new ListStore<GWTJahiaNode>() ;
        table = new Grid<GWTJahiaNode>(store, getHeaders()) ;
        table.setBorders(false);
        table.setAutoExpandColumn("path");
        table.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        table.addListener(Events.RowClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                getLinker().onTableItemSelected();
				((FilePickerContainer) getLinker().getTopRightObject()).handleNewSelection();
            }
        });
        m_component.add(table) ;
    }

    public void setSearchContent(String text) {
        clearTable();
        if (text != null && text.length()>0) {
            final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance() ;
            if (getLinker() != null) {
                getLinker().loading("searching content...");
            }
            service.search(text, 0, config.getNodeTypes(), config.getMimeTypes(), config.getFilters(), new AsyncCallback<List<GWTJahiaNode>>() {
                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage()) ;
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        setProcessedContent(gwtJahiaNodes);
                    } else {

                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }
            });
        } else {
            refresh();
        }
    }

    public void setContent(Object root) {
        // do nothing, content is set with setProcessedContent
    }

    public void setProcessedContent(Object content) {
        clearTable();
        for (GWTJahiaNode node: (List<GWTJahiaNode>) content) {
            store.add(node) ;
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return table.getSelectionModel().getSelectedItems() ;
    }

    public void refresh() {
        // nothing here
    }

    public Component getComponent() {
        return m_component ;
    }

    public void clearSelection() {
        table.getSelectionModel().deselectAll();
    }

    private static ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        ColumnConfig col = new ColumnConfig("ext", "Type", 32) ;
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                return "<span class=\"" + modelData.getExt() + "\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>" ;
            }
        });
        col.setSortable(true);
        headerList.add(col) ;

        col = new ColumnConfig("name", "Name", 150) ;
        col.setResizable(true);
        headerList.add(col) ;

        col = new ColumnConfig("path", "Path", 300) ;
        col.setResizable(true);
        headerList.add(col) ;

        col = new ColumnConfig("size", "Size", 60) ;
        col.setResizable(true);
        col.setAlignment(Style.HorizontalAlignment.LEFT);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                if (modelData.getSize() != null) {
                    long size = modelData.getSize() ;
                    return Formatter.getFormattedSize(size) ;
                } else {
                    return "-" ;
                }
            }
        });
        col.setSortable(true);
        headerList.add(col) ;

        col = new ColumnConfig("date", "Date", 60) ;
        col.setAlignment(Style.HorizontalAlignment.LEFT);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                if (modelData.getDate() != null) {
                    return new DateTimePropertyEditor(DateTimeFormat.getFormat(CalendarField.DEFAULT_DATE_FORMAT)).
                            getStringValue(modelData.getDate()) ;
                } else {
                    return "-" ;
                }
            }
        });
        col.setSortable(true);
        headerList.add(col) ;

        return new ColumnModel(headerList);
    }
}
