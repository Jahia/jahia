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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.content.FileStoreSorter;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Table view component for the content manager widget.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class TableView extends TopRightComponent {

    private static String[] STATE_IMAGES = new String[]{"000", "111", "121", "000", "000", "000"};
    
	private static final GridCellRenderer<GWTJahiaNode> EXT_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
        public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
            return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
        }
    };
	private static final GridCellRenderer<GWTJahiaNode> LOCKED_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
		public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
		        ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
			if (modelData.isLocked().booleanValue()) {
				String lockOwner = modelData.getLockOwner();
				return lockOwner != null && lockOwner.equals(JahiaGWTParameters.SYSTEM_USER) ? "<img src='../images/icons/gwt/lock_information.png'>"
				        : ContentModelIconProvider.getInstance().getLockIcon().getHTML();
			} else {
				return "";
			}
		}
	};
	private static final GridCellRenderer<GWTJahiaNode> SIZE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
		public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
		        ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
			if (modelData.getSize() != null) {
				long size = modelData.getSize().longValue();
				return Formatter.getFormattedSize(size);
			} else {
				return "-";
			}
		}
	};
	private static final GridCellRenderer<GWTJahiaNode> DATE_RENDERER = new GridCellRenderer<GWTJahiaNode>() {
		public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
		        ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
			Date d = modelData.get(s);
			if (d != null) {
				return new DateTimePropertyEditor(DateTimeFormat.getFormat(CalendarField.DEFAULT_DATE_FORMAT)).getStringValue(d);
			} else {
				return "-";
			}
		}
	};

	private LayoutContainer m_component;
    private Grid<GWTJahiaNode> m_table;
    private ListStore<GWTJahiaNode> store;
    private ListLoader<ListLoadResult<GWTJahiaNode>> loader ;
    private ManagerConfiguration configuration;

    public TableView(final ManagerConfiguration config) {
        m_component = new LayoutContainer(new FitLayout());
        m_component.setBorders(false);

        configuration = config;

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName()) ;
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, !configuration.isAllowCollections(), listAsyncCallback);
            }
        };

        loader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(privateProxy) {
            @Override
            protected void onLoadSuccess(Object gwtJahiaNode, ListLoadResult<GWTJahiaNode> gwtJahiaNodeListLoadResult) {
                super.onLoadSuccess((GWTJahiaNode) gwtJahiaNode, gwtJahiaNodeListLoadResult);
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
        List<ColumnConfig> columns = getHeaders();
        CheckBoxSelectionModel<GWTJahiaNode> checkboxSelectionModel = null;
        if (configuration.isUseCheckboxForSelection()) {
        	checkboxSelectionModel = new CheckBoxSelectionModel<GWTJahiaNode>();
            columns.add(0, checkboxSelectionModel.getColumn());
        }
        m_table = new Grid<GWTJahiaNode>(store, new ColumnModel(columns));
        m_table.setBorders(true);
        m_table.setAutoExpandColumn(configuration.getTableColumns().isEmpty() || configuration.getTableColumns().contains("path") ? "path" : "name");
        if (checkboxSelectionModel != null) {
        	m_table.setSelectionModel(checkboxSelectionModel);
        	m_table.addPlugin(checkboxSelectionModel);
        }
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
                                ContentActions.download(getLinker());
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

    @Override
    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
//        DragSource source = new GridDragSource(m_table);
//        source.addDNDListener(linker.getDndListener());
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
            if (store.getSortState().getSortField() != null && store.getSortState().getSortDir() != null) {
                store.sort(store.getSortState().getSortField(), store.getSortState().getSortDir());
            } else {
                store.sort("date", Style.SortDir.DESC);
            }
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

    private List<ColumnConfig> getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        List<String> columnNames = new ArrayList<String>(configuration.getTableColumns());
        if (columnNames.isEmpty()) {
        	columnNames.add("providerKey");
        	columnNames.add("ext");
        	columnNames.add("locked");
        	columnNames.add("name");
        	columnNames.add("path");
        	columnNames.add("size");
        	columnNames.add("publicationInfo");
        	columnNames.add("created");
        	columnNames.add("createdBy");
        	columnNames.add("lastModified");
        	columnNames.add("lastModifiedBy");
        	columnNames.add("lastPublished");
        	columnNames.add("lastPublishedBy");
        }

        for (String columnName : columnNames) {
            ColumnConfig col = null;
	        
            if ("providerKey".equals(columnName)) {
                col = new ColumnConfig("providerKey", Messages.getResource("fm_column_provider"), 60);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("ext".equals(columnName)) {
                col = new ColumnConfig("ext", Messages.getResource("fm_column_type"), 40);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(EXT_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("locked".equals(columnName)) {
                col = new ColumnConfig("locked", Messages.getResource("fm_column_locked"), 40);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(LOCKED_RENDERER);
                col.setSortable(false);
                col.setResizable(true);
            } else if ("name".equals(columnName)) {
	            col = new ColumnConfig("name", Messages.getResource("fm_column_name"), 140);
	            col.setSortable(true);
	            col.setResizable(true);
            } else if ("path".equals(columnName)) {
	            col = new ColumnConfig("path", Messages.getResource("fm_column_path"), 270);
	            col.setSortable(true);
	            col.setResizable(true);
            } else if ("size".equals(columnName)) {
                col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 60);
                col.setResizable(true);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(SIZE_RENDERER);
                col.setResizable(true);
                col.setSortable(true);
            } else if ("created".equals(columnName)) {
                col = new ColumnConfig("created", Messages.get("fm_column_created", "Created"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("createdBy".equals(columnName)) {
                col = new ColumnConfig("createdBy", Messages.get("fm_column_created_by", "Created by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastModified".equals(columnName)) {
                col = new ColumnConfig("lastModified", Messages.get("fm_column_modified", "Modified"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastModifiedBy".equals(columnName)) {
                col = new ColumnConfig("lastModifiedBy", Messages.get("fm_column_modified_by", "Modified by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastPublished".equals(columnName)) {
                col = new ColumnConfig("lastPublished", Messages.get("fm_column_published", "Published"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setRenderer(DATE_RENDERER);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("lastPublishedBy".equals(columnName)) {
                col = new ColumnConfig("lastPublishedBy", Messages.get("fm_column_published_by", "Published by"), 100);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
                col.setSortable(true);
                col.setResizable(true);
            } else if ("publicationInfo".equals(columnName)) {
                col = new ColumnConfig("publicationInfo", Messages.get("fm_column_publication_info", "State"), 30);
                col.setAlignment(Style.HorizontalAlignment.LEFT);
				col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
					public String render(GWTJahiaNode node, String property, ColumnData config, int rowIndex,
					        int colIndex, ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
						int state = node.getPublicationInfo() != null ? node.getPublicationInfo().getStatus() : 0;
						String title = Messages.get("fm_column_publication_info_" + state, String.valueOf(state));
						return "<img src=\"../../gwt/resources/images/workflow/" + STATE_IMAGES[state]
						        + ".png\" height=\"12\" width=\"12\" title=\"" + title + "\" alt=\"" + title + "\"/>";
					}
				});
				col.setSortable(true);
				col.setResizable(true);
            }
            if (col != null) {
            	headerList.add(col);            	
            }
        }

        return headerList;
    }

}
