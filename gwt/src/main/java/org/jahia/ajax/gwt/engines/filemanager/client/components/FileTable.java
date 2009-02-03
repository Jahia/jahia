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

package org.jahia.ajax.gwt.engines.filemanager.client.components;

import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.commons.client.util.Formatter;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.FileActions;
import org.jahia.ajax.gwt.filemanagement.client.util.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.filemanagement.client.util.FileStoreSorter;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.commons.client.ui.form.CalendarField;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.filemanagement.client.ui.ImagePopup;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class FileTable extends TopRightComponent {

    private LayoutContainer m_component ;
    private Grid<GWTJahiaNode> m_table ;
    private ListStore<GWTJahiaNode> store ;
    private ManagerConfiguration configuration ;

    public FileTable(ManagerConfiguration config) {
        m_component = new LayoutContainer(new FitLayout()) ;
        m_component.setBorders(false);

        configuration = config ;

        store = new ListStore<GWTJahiaNode>();
        store.setStoreSorter(new FileStoreSorter());

        m_table = new Grid<GWTJahiaNode>(store, getHeaders()) ;
        m_table.setBorders(true);
        m_table.setAutoExpandColumn("path");
        m_table.getSelectionModel().setSelectionMode(Style.SelectionMode.MULTI);
        m_table.addListener(Events.RowClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                getLinker().onTableItemSelected();
            }
        });
        m_table.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                List<GWTJahiaNode> sel = m_table.getSelectionModel().getSelectedItems() ;
                if (sel != null && sel.size()==1) {
                    GWTJahiaNode el = sel.get(0) ;
                    if (el.isFile()) {
                        if (el.isDisplayable()) {
                            ImagePopup.popImage(el);
                        } else {
                            FileActions.download(getLinker());
                        }
                    } else {
                        getLinker().onTableItemDoubleClicked(sel.get(0)) ;
                    }
                }
            }
        });

        m_component.add(m_table) ;
    }

    public void setContextMenu(Menu menu) {
         m_table.setContextMenu(menu);
    }

    public void setContent(final Object root) {
        clearTable();
        if (root != null) {
            final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance() ;
            if (getLinker() != null) {
                getLinker().loading("listing directory content...");
            }
            service.ls((GWTJahiaNode) root, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, false, new AsyncCallback<List<GWTJahiaNode>>() {
                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage()) ;
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }

                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null && gwtJahiaNodes.size() > 0) {
                        store.add(gwtJahiaNodes);
                        //store.sort("ext", Style.SortDir.ASC);
                    }
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                    List<GWTJahiaNode> treeSelection = new ArrayList<GWTJahiaNode>(1) ;
                    treeSelection.add((GWTJahiaNode) root) ;
                    getLinker().getBottomRightObject().fillData(treeSelection);
                }
            });
        }
    }

    public void setProcessedContent(Object content) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content ;
            store.add(gwtJahiaNodes);
            getLinker().onTableItemSelected();
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        List<GWTJahiaNode> elts = m_table.getSelectionModel().getSelectedItems() ;
        if (elts != null && elts.size()>0) {
            return elts ;
        } else {
            return null ;
        }
    }

    public void refresh() {
        //m_table.getView().refresh(true);
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component ;
    }

    private static ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        ColumnConfig col = new ColumnConfig("ext", "Type", 50) ;
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                if (modelData.getExt() == null || modelData.getExt().equals("zzzzzz")) {
                    return "<img src='../../css/gwt/images/xtheme-jahia-andromeda/gxt/icons/folder-closed.gif'>" ;
                } else {
                    return new StringBuilder("<img src='../images/types/gwt/").append(modelData.getExt()).append(".png'>").toString() ;
                }
            }
        });
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col) ;

        col = new ColumnConfig("locked", "Lock", 32) ;
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                if (modelData.isLocked().booleanValue()) {
                    String lockOwner = modelData.getLockOwner();
                    return lockOwner != null
                            && lockOwner.equals(JahiaGWTParameters.SYSTEM_USER) ? "<img src='../images/icons/gwt/lock_information.png'>"
                            : "<img src='../images/icons/gwt/lock.png'>";
                } else {
                    return "" ;
                }
            }
        });
        col.setSortable(false);
        col.setResizable(true) ;
        headerList.add(col) ;

        col = new ColumnConfig("name", "Name", 140) ;
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col) ;

        col = new ColumnConfig("path", "Path", 300) ;
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col) ;

        col = new ColumnConfig("size", "Size", 60) ;
        col.setResizable(true);
        col.setAlignment(Style.HorizontalAlignment.LEFT);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                if (modelData.getSize() != null) {
                    long size = modelData.getSize().longValue() ;
                    return Formatter.getFormattedSize(size) ;
                } else {
                    return "-" ;
                }
            }
        });
        col.setResizable(true);
        col.setSortable(true);
        headerList.add(col) ;

        col = new ColumnConfig("date", "Date", 90) ;
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
        col.setResizable(true);
        headerList.add(col) ;

        return new ColumnModel(headerList);
    }

}
