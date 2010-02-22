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
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.SearchField;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:53:43
 */
public class SearchGrid extends ContentPanel {

    //private ContentPanel m_component;
    private Grid<GWTJahiaNode> grid;
    private ListStore<GWTJahiaNode> store;

    private ManagerConfiguration config;
    private boolean multiple = false;

    public SearchGrid(ManagerConfiguration config, boolean multiple) {
        this(config);
        this.multiple = multiple;
    }

    public SearchGrid(ManagerConfiguration config) {
        this.config = config;
        setLayout(new FitLayout());
        setBorders(false);
        setBodyBorder(false);
        SearchField searchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                setSearchContent(value);
            }

            public void onSaveButtonClicked(String value) {
                // ... no save button
            }
        };
        setHeaderVisible(false);
        setTopComponent(searchField);
        store = new ListStore<GWTJahiaNode>();
        grid = new Grid<GWTJahiaNode>(store, getHeaders());
        grid.getView().setForceFit(true);
        grid.setBorders(false);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });
        add(grid);
    }

    public void setSearchContent(final String text) {
        clearTable();
        if (text != null && text.length() > 0) {
            final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

            service.search(text, 0, config.getNodeTypes(), config.getMimeTypes(), config.getFilters(), new AsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                    if (gwtJahiaNodes != null) {
                        Log.debug("Found " + gwtJahiaNodes.size() + " results with text: " + text);
                        setProcessedContent(gwtJahiaNodes);
                    } else {
                        Log.debug("No result found in search");
                    }
                }

                public void onFailure(Throwable throwable) {
                    Window.alert("Element list retrieval failed :\n" + throwable.getLocalizedMessage());

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
        store.add((List<GWTJahiaNode>) content);
        if (store.getSortState().getSortField() != null && store.getSortState().getSortDir() != null) {
            store.sort(store.getSortState().getSortField(), store.getSortState().getSortDir());
        } else {
            store.sort("date", Style.SortDir.DESC);
        }
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return grid.getSelectionModel().getSelectedItems();
    }

    public void refresh() {
        // nothing here
    }


    public void clearSelection() {
        grid.getSelectionModel().deselectAll();
    }

    /**
     * Get heder from config
     *
     * @return
     */
    private ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        List<String> columnIds = config.getTableColumns();
        if (columnIds == null || columnIds.size() == 0) {
            columnIds.add("name");
            columnIds.add("size");
            columnIds.add("date");
            columnIds.add("version");
            columnIds.add("picker");
        }
        for (String s1 : columnIds) {
            if (s1.equals("name")) {
                ColumnConfig col = new ColumnConfig("displayName", Messages.getResource("fm_column_name"), 300);
                headerList.add(col);
            } else if (s1.equals("size")) {
                ColumnConfig col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 70);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        if (gwtJahiaNode != null && gwtJahiaNode.getSize() != null) {
                            long size = gwtJahiaNode.getSize().longValue();
                            return Formatter.getFormattedSize(size);
                        } else {
                            return "-";
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("date")) {
                ColumnConfig col = new ColumnConfig("date", Messages.getResource("fm_column_date"), 80);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        Date d = gwtJahiaNode.getLastModified();
                        if (d != null) {
                            return DateTimeFormat.getFormat("d/MM/y").format(d).toString();
                        } else {
                            return "-";
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("version")) {
                ColumnConfig col = new ColumnConfig("version", Messages.getResource("versioning_versionLabel"), 150);
                col.setFixed(true);
                col.setHidden(true);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
                        if (versions != null) {
                            SimpleComboBox<String> combo = new SimpleComboBox<String>();
                            combo.setForceSelection(true);
                            combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                            for (GWTJahiaNodeVersion version : versions) {
                                combo.add(version.getVersionNumber() + " (" + DateTimeFormat.getFormat("d/MM/y hh:mm").format(version.getDate()).toString() + ")");
                            }
                            final String s2 = "Always Latest Version";
                            combo.add(s2);
                            combo.setSimpleValue(s2);
                            combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                                @Override
                                public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                                    SimpleComboValue<String> value = simpleComboValueSelectionChangedEvent.getSelectedItem();
                                    String value1 = value.getValue();
                                    if (!s2.equals(value1))
                                        gwtJahiaNode.setSelectedVersion(value1.split("\\(")[0].trim());
                                }
                            });
                            combo.setDeferHeight(true);
                            return combo;
                        } else {
                            SimpleComboBox<String> combo = new SimpleComboBox<String>();
                            combo.setForceSelection(false);
                            combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                            combo.add("No version");
                            combo.setSimpleValue("No version");
                            combo.setEnabled(false);
                            combo.setDeferHeight(true);
                            return combo;
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("picker") && multiple) {
                ColumnConfig col = new ColumnConfig("action", "action", 100);

                col.setAlignment(Style.HorizontalAlignment.RIGHT);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        if (gwtJahiaNode.isFile() || gwtJahiaNode.isMatchFilters()) {
                            final Button pickContentButton = new Button(Messages.get("label_add", "Add"));
                            pickContentButton.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
                            pickContentButton.setEnabled(true);
                            pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent buttonEvent) {
                                    onContentPicked(gwtJahiaNode);
                                }
                            });
                            return pickContentButton;
                        } else {
                            return new Text("");
                        }
                    }

                    /**
                     * Return true id the node is selectable
                     * @param gwtJahiaNode
                     * @return
                     */

                });
                col.setFixed(true);
                headerList.add(col);
            }
        }
        return new ColumnModel(headerList);
    }

    /**
     * Override this method to customize "add" button behavirou
     *
     * @param gwtJahiaNode
     */
    public void onContentPicked(final GWTJahiaNode gwtJahiaNode) {

    }
}
