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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.definition.ContentTypeModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreateContentEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 21 déc. 2009
 */
public class ContentTypeTree extends LayoutContainer {
    private TreeGrid<GWTJahiaNodeType> treeGrid;
    private Linker linker;
    private final GWTJahiaNodeType nodeType;
    private final String baseType;
    private GWTJahiaNode parentNode;
    private final int width;
    private final int height;
    private final int rowHeight;
    private final boolean popup;
    private final Window window;
    private TreeStore<GWTJahiaNodeType> store;

    public ContentTypeTree(Linker linker, GWTJahiaNodeType nodeType, String baseType, GWTJahiaNode parentNode,
                           int width, int height, int rowHeight,
                           boolean isPopup, Window window) {
        this.linker = linker;
        this.nodeType = nodeType;
        this.baseType = baseType;
        this.parentNode = parentNode;
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
        popup = isPopup;
        this.window = window;
        createUI();
    }

    public ContentTypeTree(Linker linker, GWTJahiaNodeType nodeType, String baseType, GWTJahiaNode parentNode,
                           int width, int height, int rowHeight) {
        this(linker, nodeType, baseType, parentNode, width, height, rowHeight, false, null);
    }


    public void createUI() {
        store = new TreeStore<GWTJahiaNodeType>();
        GWTJahiaNodeType rootEmptyContent = null;
        if (nodeType == null) {
            if (linker != null) {
                if (parentNode != null) {
                    filldataStore(parentNode);
                } else {
                    filldataStore();
                }
            }
        } else {
            rootEmptyContent = nodeType;
            store.add(rootEmptyContent, false);
        }
        ColumnConfig name = new ColumnConfig("label", "Label", width - 40);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(width - 40);
                panel.setTableWidth("100%");
                TableData tableData;
                if (gwtJahiaNodeType != null) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("5%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon(gwtJahiaNodeType).createImage());
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("95%");
                    panel.add(label, tableData);
                } else {
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("100%");
                    panel.add(label, tableData);
                }
                panel.layout();
                return panel;
            }
        });
        ColumnConfig author = new ColumnConfig("author", "Author", 40);
        treeGrid = new TreeGrid<GWTJahiaNodeType>(store, new ColumnModel(Arrays.asList(name, author)));
        treeGrid.setBorders(true);
        if (height > 0) {
            treeGrid.setHeight(height);
        } else {
            treeGrid.setHeight("100%");
        }
        treeGrid.setAutoExpandColumn("label");
        treeGrid.getTreeView().setRowHeight(rowHeight);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        if (popup) {
            treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
            treeGrid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
                public void handleEvent(BaseEvent baseEvent) {
                    GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) (((TreeGridEvent) baseEvent).getModel());
                    if (gwtJahiaNodeType != null && linker != null) {
                        new CreateContentEngine(linker, parentNode, gwtJahiaNodeType, null, false).show();
                        window.hide();
                    }
                }
            });
        }
        ContentPanel previewPanel = null;
        if (popup) {
            VBoxLayout vBoxLayout = new VBoxLayout();
            vBoxLayout.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
            setLayout(vBoxLayout);
        } else {
            FitLayout vBoxLayout = new FitLayout();
            setLayout(vBoxLayout);
            setBorders(true);
        }
        add(treeGrid);
        setWidth("100%");
        if (previewPanel != null) {
            add(previewPanel);
        }
    }

    private void filldataStore() {
        store.removeAll();
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(
                new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
                    public void onFailure(Throwable caught) {
                        MessageBox.alert("Alert",
                                "Unable to load content definitions for base type '" + baseType + "' and parent node '" + ContentTypeTree.this.parentNode.getPath() + "'. Cause: " + caught.getLocalizedMessage(),
                                null);
                    }

                    public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                        for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNodeType>> entry : result.entrySet()) {
                            store.add(entry.getKey(), true);
                            for (GWTJahiaNodeType gwtJahiaNodeType : entry.getValue()) {
                                store.add(entry.getKey(), gwtJahiaNodeType, true);
                            }
                        }
                        store.sort("label", Style.SortDir.ASC);
                    }
                });
    }

    private void filldataStore(GWTJahiaNode parentNode) {
        store.removeAll();
        JahiaContentDefinitionService.App.getInstance().getNodeSubtypes(baseType, parentNode,
                new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
                    public void onFailure(Throwable caught) {
                        MessageBox.alert("Alert",
                                "Unable to load content definitions for base type '" + baseType + "' and parent node '" + ContentTypeTree.this.parentNode.getPath() + "'. Cause: " + caught.getLocalizedMessage(),
                                null);
                    }

                    public void onSuccess(
                            Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                        for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNodeType>> entry : result.entrySet()) {
                            store.add(entry.getKey(), true);
                            for (GWTJahiaNodeType gwtJahiaNodeType : entry.getValue()) {
                                store.add(entry.getKey(), gwtJahiaNodeType, true);
                            }
                        }
                        store.sort("label", Style.SortDir.ASC);
                    }
                });
    }

    public TreeGrid<GWTJahiaNodeType> getTreeGrid() {
        return treeGrid;
    }

    public void setLinker(Linker linker) {
        this.linker = linker;
        this.parentNode = linker.getMainNode();
        filldataStore();
    }

    public void refresh() {
        filldataStore();
    }
}
