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

import com.allen_sauer.gwt.log.client.Log;
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
import com.google.gwt.user.client.ui.HTML;
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
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEngine;

import java.util.ArrayList;
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
    private TreeGrid<ContentTypeModelData> treeGrid;
    private Linker linker;
    private final GWTJahiaNodeType nodeType;
    private final String baseType;
    private GWTJahiaNode parentNode;
    private final boolean displayReusableComponents;
    private final boolean displayPreview;
    private final int width;
    private final int height;
    private final int rowHeight;
    private final boolean popup;
    private final Window window;
    private TreeStore<ContentTypeModelData> store;

    public ContentTypeTree(Linker linker, GWTJahiaNodeType nodeType, String baseType, GWTJahiaNode parentNode,
                           boolean displayReusableComponents, boolean displayPreview, int width, int height, int rowHeight,
                           boolean isPopup, Window window) {
        this.linker = linker;
        this.nodeType = nodeType;
        this.baseType = baseType;
        this.parentNode = parentNode;
        this.displayReusableComponents = displayReusableComponents;
        this.displayPreview = displayPreview;
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
        popup = isPopup;
        this.window = window;
        createUI();
    }

    public ContentTypeTree(Linker linker, GWTJahiaNodeType nodeType, String baseType, GWTJahiaNode parentNode,
                           boolean displayReusableComponents, boolean displayPreview, int width, int height, int rowHeight) {
        this(linker, nodeType, baseType, parentNode, displayReusableComponents, displayPreview, width, height, rowHeight,false,null);
    }


    public void createUI() {
        store = new TreeStore<ContentTypeModelData>();
        ContentTypeModelData rootEmptyContent = null;
        if (nodeType == null) {
            if (linker != null) {
                if (parentNode != null) {
                    filldataStore(parentNode);
                } else {
                    filldataStore();
                }
            }
        } else {
            rootEmptyContent = new ContentTypeModelData(nodeType);
            store.add(rootEmptyContent, false);
        }
        if (displayReusableComponents) {
            final GWTJahiaNode reusableComponentsFolder = new GWTJahiaNode();
            if (nodeType != null && rootEmptyContent != null) {
                reusableComponentsFolder.setPath("/reusableComponents/" + nodeType.getName().replaceAll(":", "_"));
                final ContentTypeModelData finalRootEmptyContent = rootEmptyContent;
                JahiaContentManagementService.App.getInstance().ls(reusableComponentsFolder,
                                                                   "jnt:reusableComponent", null, null, false,
                                                                   new AsyncCallback<List<GWTJahiaNode>>() {
                                                                       public void onFailure(Throwable caught) {
                                                                       }

                                                                       public void onSuccess(
                                                                               List<GWTJahiaNode> result) {
                                                                           for (GWTJahiaNode gwtJahiaNode : result) {
                                                                               store.add(finalRootEmptyContent,
                                                                                         new ContentTypeModelData(
                                                                                                 gwtJahiaNode), false);
                                                                           }
                                                                       }
                                                                   });
            }
        }
        ColumnConfig name = new ColumnConfig("label", "Label", width - 40);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                ContentTypeModelData gwtJahiaNode = (ContentTypeModelData) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(width - 40);
                panel.setTableWidth("100%");
                TableData tableData;
                if (gwtJahiaNode.getGwtJahiaNodeType() != null || gwtJahiaNode.getGwtJahiaNode() != null) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("5%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon(
                            gwtJahiaNode.getGwtJahiaNodeType()).createImage());
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
        treeGrid = new TreeGrid<ContentTypeModelData>(store, new ColumnModel(Arrays.asList(name, author)));
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
                    ContentTypeModelData contentTypeModelData = (ContentTypeModelData) (((TreeGridEvent) baseEvent).getModel());
                    final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelData.getGwtJahiaNodeType();
                    final GWTJahiaNode gwtJahiaNode = contentTypeModelData.getGwtJahiaNode();
                    if (gwtJahiaNodeType != null && linker != null) {
                        new EditContentEngine(linker, parentNode, gwtJahiaNodeType, null, false).show();
                        window.hide();
                    } else if (gwtJahiaNode != null) {
                        final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                        instance.getNode(gwtJahiaNode.getPath() + "/j:target", new AsyncCallback<GWTJahiaNode>() {
                            public void onFailure(Throwable caught) {
                                MessageBox.alert("Alert",
                                                 "Unable to copy reusable component to destination. Cause: " + caught.getLocalizedMessage(),
                                                 null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
                                result.setName(gwtJahiaNode.getName());
                                nodes.add(result);
                                instance.paste(nodes, parentNode.getPath(), false, new AsyncCallback() {
                                    public void onFailure(Throwable caught) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    public void onSuccess(Object result) {
                                        hide();
                                        if (linker != null) {
                                            linker.refreshMainComponent();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
        ContentPanel previewPanel = null;
        if (displayPreview) {
            previewPanel = new ContentPanel();
            final LayoutContainer layoutContainer = new LayoutContainer();
            previewPanel.add(layoutContainer);
            treeGrid.getSelectionModel().addSelectionChangedListener(
                    new SelectionChangedListener<ContentTypeModelData>() {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<ContentTypeModelData> contentTypeModelDataSelectionChangedEvent) {
                            final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelDataSelectionChangedEvent.getSelectedItem().getGwtJahiaNodeType();
                            final GWTJahiaNode gwtJahiaNode = contentTypeModelDataSelectionChangedEvent.getSelectedItem().getGwtJahiaNode();
                            if (gwtJahiaNodeType != null) {
                                layoutContainer.removeAll();
                                layout();
                            } else if (gwtJahiaNode != null) {
                                final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                                if (linker != null) {
                                    instance.getRenderedContent(gwtJahiaNode.getPath() + "/j:target", null,
                                                                ((EditLinker) linker).getLocale(), null,
                                                                "wrapper.previewwrapper", null, false,
                                                                new AsyncCallback<String>() {
                                                                    public void onSuccess(String result) {
                                                                        HTML html = new HTML(result);
                                                                        layoutContainer.removeAll();
                                                                        layoutContainer.add(html);
                                                                        layout();
                                                                    }

                                                                    public void onFailure(Throwable caught) {
                                                                        Log.error("", caught);
//                    com.google.gwt.user.client.Window.alert("-update preview pp->" + caught.getMessage());
                                                                    }
                                                                });
                                }
                            }
                        }
                    });
        }
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
                new AsyncCallback<Map<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>>>() {
                    public void onFailure(Throwable caught) {
                        MessageBox.alert("Alert",
                                         "Unable to load content definitions for base type '" + baseType + "' and parent node '" + ContentTypeTree.this.parentNode.getPath() + "'. Cause: " + caught.getLocalizedMessage(),
                                         null);
                    }

                    public void onSuccess(Map<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>> result) {
                        for (Map.Entry<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>> entry : result.entrySet()) {
                            final ContentTypeModelData rootData = new ContentTypeModelData(entry.getKey());
                            store.add(rootData, true);
                            for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNode>> gwtJahiaNodeType : entry.getValue().entrySet()) {
                                final ContentTypeModelData data = new ContentTypeModelData(gwtJahiaNodeType.getKey());
                                store.add(rootData, data, true);

                                for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodeType.getValue()) {
                                    store.add(data, new ContentTypeModelData(gwtJahiaNode), true);
                                }
                            }
                            store.sort("label", Style.SortDir.ASC);
                        }
                    }
                });
    }

    private void filldataStore(GWTJahiaNode parentNode) {
        store.removeAll();
        JahiaContentDefinitionService.App.getInstance().getNodeSubtypes(baseType, parentNode,
                                                                        new AsyncCallback<Map<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>>>() {
                                                                            public void onFailure(Throwable caught) {
                                                                                MessageBox.alert("Alert",
                                                                                                 "Unable to load content definitions for base type '" + baseType + "' and parent node '" + ContentTypeTree.this.parentNode.getPath() + "'. Cause: " + caught.getLocalizedMessage(),
                                                                                                 null);
                                                                            }

                                                                            public void onSuccess(
                                                                                    Map<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>> result) {
                                                                                for (Map.Entry<GWTJahiaNodeType, Map<GWTJahiaNodeType, List<GWTJahiaNode>>> entry : result.entrySet()) {
                                                                                    final ContentTypeModelData rootData = new ContentTypeModelData(
                                                                                            entry.getKey());
                                                                                    store.add(rootData, true);
                                                                                    for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNode>> gwtJahiaNodeType : entry.getValue().entrySet()) {
                                                                                        final ContentTypeModelData data = new ContentTypeModelData(
                                                                                                gwtJahiaNodeType.getKey());
                                                                                        store.add(rootData, data, true);

                                                                                        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodeType.getValue()) {
                                                                                            store.add(data,
                                                                                                      new ContentTypeModelData(
                                                                                                              gwtJahiaNode),
                                                                                                      true);
                                                                                        }
                                                                                    }
                                                                                    store.sort("label",
                                                                                               Style.SortDir.ASC);
                                                                                }
                                                                            }
                                                                        });
    }

    public TreeGrid<ContentTypeModelData> getTreeGrid() {
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
