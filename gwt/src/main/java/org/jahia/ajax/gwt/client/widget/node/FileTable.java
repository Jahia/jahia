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
import org.jahia.ajax.gwt.client.util.nodes.FileStoreSorter;
import org.jahia.ajax.gwt.client.util.nodes.actions.FileActions;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.messages.Messages;
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
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.allen_sauer.gwt.log.client.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class FileTable extends TopRightComponent {

    private LayoutContainer m_component;
    private Grid<GWTJahiaNode> m_table;
    private ListStore<GWTJahiaNode> store;
    private ListLoader<GWTJahiaNode> loader ;
    private ManagerConfiguration configuration;

    public FileTable(final ManagerConfiguration config) {
        m_component = new LayoutContainer(new FitLayout());
        m_component.setBorders(false);

        configuration = config;

        // data proxy
        RpcProxy<GWTJahiaNode, ListLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<GWTJahiaNode, ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(GWTJahiaNode gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + gwtJahiaFolder.getName()) ;
                JahiaNodeService.App.getInstance().lsLoad(gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, false, listAsyncCallback);
            }
        };

        loader = new BaseListLoader<GWTJahiaNode, ListLoadResult<GWTJahiaNode>>(privateProxy) {
            @Override
            protected void onLoadSuccess(GWTJahiaNode gwtJahiaNode, ListLoadResult<GWTJahiaNode> gwtJahiaNodeListLoadResult) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodeListLoadResult);
                if (getLinker() != null) {
                    getLinker().loaded() ;
                }
            }
        };
        store = new ListStore<GWTJahiaNode>(loader) {
            protected void onBeforeLoad(LoadEvent e) {
                if (getLinker() != null) {
                    getLinker().loading("listing directory content...") ;
                }
                super.onBeforeLoad(e);
            }

            @Override
            protected void onLoadException(LoadEvent loadEvent) {
                super.onLoadException(loadEvent);
                Log.error("Error listing directory content " + loadEvent.exception.toString()) ;
            }
        };
        store.setStoreSorter(new FileStoreSorter());

        m_table = new Grid<GWTJahiaNode>(store, getHeaders());
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
                List<GWTJahiaNode> sel = m_table.getSelectionModel().getSelectedItems();
                if (sel != null && sel.size() == 1) {
                    GWTJahiaNode el = sel.get(0);
                    if (el.isFile()) {
                        if (config.isEnableFileDoubleClick()) {
                            if (el.isDisplayable()) {
                                ImagePopup.popImage(el);
                            } else {
                                FileActions.download(getLinker());
                            }
                        }
                    } else {
                        getLinker().onTableItemDoubleClicked(sel.get(0));
                    }
                }
            }
        });

        m_component.add(m_table);
    }

    public void setContextMenu(Menu menu) {
        m_table.setContextMenu(menu);
    }

    public void setContent(final Object root) {
        clearTable();
        if (root != null) {
            loader.load((GWTJahiaNode) root) ;
        }
    }

    public void setProcessedContent(Object content) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content;
            store.add(gwtJahiaNodes);
            getLinker().onTableItemSelected();
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        List<GWTJahiaNode> elts = m_table.getSelectionModel().getSelectedItems();
        if (elts != null && elts.size() > 0) {
            return elts;
        } else {
            return null;
        }
    }

    public void refresh() {
        //m_table.getView().refresh(true);
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component;
    }

    private ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        ColumnConfig col;
        if (configuration.isDisplayExt()) {
            col = new ColumnConfig("ext", Messages.getResource("fm_column_type"), 40);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    return new StringBuilder("<img src='../images/types/gwt/").append(modelData.getExt()).append(".png'>").toString();
                }
            });
            col.setSortable(true);
            col.setResizable(true);
            headerList.add(col);
        }

        if (configuration.isDisplayLock()) {
            col = new ColumnConfig("locked", Messages.getResource("fm_column_locked"), 40);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    if (modelData.isLocked().booleanValue()) {
                        String lockOwner = modelData.getLockOwner();
                        return lockOwner != null
                                && lockOwner.equals(JahiaGWTParameters.SYSTEM_USER) ? "<img src='../images/icons/gwt/lock_information.png'>"
                                : "<img src='../images/icons/gwt/lock.png'>";
                    } else {
                        return "";
                    }
                }
            });
            col.setSortable(false);
            col.setResizable(true);
            headerList.add(col);
        }

        col = new ColumnConfig("name", Messages.getResource("fm_column_name"), 140);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);


        col = new ColumnConfig("path", Messages.getResource("fm_column_path"), 270);
        col.setSortable(true);
        col.setResizable(true);
        headerList.add(col);


        if (configuration.isDisplaySize()) {
            col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 60);
            col.setResizable(true);
            col.setAlignment(Style.HorizontalAlignment.LEFT);
            col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    if (modelData.getSize() != null) {
                        long size = modelData.getSize().longValue();
                        return Formatter.getFormattedSize(size);
                    } else {
                        return "-";
                    }
                }
            });
            col.setResizable(true);
            col.setSortable(true);
            headerList.add(col);
        }

        if (configuration.isDisplayDate()) {
            col = new ColumnConfig("date", Messages.getResource("fm_column_date"), 100);
            col.setAlignment(Style.HorizontalAlignment.LEFT);
            col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore) {
                    if (modelData.getDate() != null) {
                        return new DateTimePropertyEditor(DateTimeFormat.getFormat(CalendarField.DEFAULT_DATE_FORMAT)).
                                getStringValue(modelData.getDate());
                    } else {
                        return "-";
                    }
                }
            });
            col.setSortable(true);
            col.setResizable(true);
            headerList.add(col);
        }

        return new ColumnModel(headerList);
    }

}
