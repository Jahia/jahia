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
package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 20 oct. 2009
 */
public class ClassificationEditor extends LayoutContainer {
    private TreeStore<GWTJahiaNode> catStore;
    private TreeStore<GWTJahiaNode> tagStore;
    private TreeStore<GWTJahiaNode> treeStore;
    private TabPanel selectionTabPanel;
    private AsyncTabItem tagTab;
    private AsyncTabItem categoriesTab;


    public ClassificationEditor(final GWTJahiaNode node) {
        setLayout(new RowLayout(Style.Orientation.VERTICAL));
        TreeStore<GWTJahiaNode> treeStore = createStoreAndLoader(node);

        this.setBorders(false);
        TableData tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.TOP);
        tableData.setWidth("100%");
        tableData.setHeight("50%");
        this.add(createTreeGrid(treeStore));

        // Now manage the tab for categories and tags
        selectionTabPanel = new TabPanel();
        selectionTabPanel.setWidth("100%");
        selectionTabPanel.setHeight(380);
        createCategoriesPanel(selectionTabPanel);
        createTagsPanel(selectionTabPanel);
        setHeight(750);
        add(selectionTabPanel, tableData);
    }

    /**
     * Create Browser tree Grid
     *
     * @param treeStore
     * @return
     */
    private TreeGrid<GWTJahiaNode> createTreeGrid(TreeStore<GWTJahiaNode> treeStore) {
        ColumnConfig name = new ColumnConfig("name", "Name", 680);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(695);
                panel.setTableWidth("100%");
                TableData tableData;
                if (gwtJahiaNode.getNodeTypes().contains("jnt:category") || gwtJahiaNode.getNodeTypes().contains(
                        "jnt:tag")) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("10%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon((GWTJahiaNode) modelData).createImage());
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("90%");
                    panel.add(label, tableData);
                } else {
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("100%");
                    panel.add(label, tableData);
                }
                return panel;
            }
        });
        name.setFixed(true);
        ColumnConfig action = new ColumnConfig("action", "Action", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                Button button = null;
                if (gwtJahiaNode.getNodeTypes().contains("jnt:category")) {
                    button = new Button("Add", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                            if (catStore.findModel(node1) == null) {
                                catStore.add(node1, false);
                                selectionTabPanel.setSelection(categoriesTab);
                            }
                        }
                    });
                    button.setData("associatedNode", modelData);
                    button.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
                } else if (gwtJahiaNode.getNodeTypes().contains("jnt:tag")) {
                    button = new Button("Add", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                            if (tagStore.findModel(node1) == null) {
                                tagStore.add(node1, false);
                                selectionTabPanel.setSelection(tagTab);
                            }
                        }
                    });
                    button.setData("associatedNode", modelData);
                    button.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
                }
                return button != null ? button : new Label("");
            }
        });
        action.setFixed(true);
        TreeGrid<GWTJahiaNode> treeGrid = new TreeGrid<GWTJahiaNode>(treeStore, new ColumnModel(Arrays.asList(name, action)));
        treeGrid.addListener(Events.RowClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent gridEvent) {
                GWTJahiaNode selectedNode = (GWTJahiaNode) gridEvent.getModel();
                doSelectTab(selectedNode);
            }
        });
        

        treeGrid.setBorders(true);
        treeGrid.setHeight(250);
        treeGrid.setAutoExpandColumn("name");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        return treeGrid;
    }

    private void doSelectTab(GWTJahiaNode selectedNode) {
        String path = selectedNode.getPath();
        if (path != null && path.indexOf("/categories") == 0) {
            selectionTabPanel.setSelection(categoriesTab);
        } else {
            selectionTabPanel.setSelection(tagTab);
        }
    }

    private TreeStore<GWTJahiaNode> createStoreAndLoader(final GWTJahiaNode node) {
        // data proxy
        RpcProxy<List<GWTJahiaNode>> privateProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                if (gwtJahiaFolder == null) {
                    async.getRoot(JCRClientUtils.CATEGORY_REPOSITORY + ";" + JCRClientUtils.TAG_REPOSITORY, "", "", "",
                            null, listAsyncCallback);
                } else {
                    async.ls(JCRClientUtils.CATEGORY_REPOSITORY, (GWTJahiaNode) gwtJahiaFolder,
                            JCRClientUtils.CATEGORY_NODETYPES + "," + JCRClientUtils.TAG_NODETYPES, "", "", null,
                            false, listAsyncCallback);
                }
            }
        };
        TreeLoader<GWTJahiaNode> loader = new BaseTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasChildren();
            }

            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
            }
        };

        TreeLoader<GWTJahiaNode> catLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getProperties(node.getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                    public void onFailure(Throwable caught) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        final GWTJahiaNodeProperty gwtJahiaNodeProperty = result.getProperties().get(
                                "j:defaultCategory");
                        if (gwtJahiaNodeProperty != null) {
                            final List<GWTJahiaNodePropertyValue> propertyValues = gwtJahiaNodeProperty.getValues();
                            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(propertyValues.size());
                            for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                                nodes.add(propertyValue.getNode());
                            }
                            listAsyncCallback.onSuccess(nodes);
                        }
                    }
                });
            }
        });
        treeStore = new TreeStore<GWTJahiaNode>(loader);
        catStore = new TreeStore<GWTJahiaNode>(catLoader);
        TreeLoader<GWTJahiaNode> tagLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getProperties(node.getPath(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
                    public void onFailure(Throwable caught) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        final GWTJahiaNodeProperty gwtJahiaNodeProperty = result.getProperties().get("j:tags");
                        if (gwtJahiaNodeProperty != null) {
                            final List<GWTJahiaNodePropertyValue> propertyValues = gwtJahiaNodeProperty.getValues();
                            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(propertyValues.size());
                            for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                                nodes.add(propertyValue.getNode());
                            }
                            listAsyncCallback.onSuccess(nodes);
                        }
                    }
                });
            }
        });

        tagStore = new TreeStore<GWTJahiaNode>(tagLoader);
        return treeStore;
    }

    private void createCategoriesPanel(TabPanel tabPanel) {
        categoriesTab = new AsyncTabItem(Messages.get("ece_categories", "Categories"));
        ColumnConfig columnConfig = new ColumnConfig("name", "Name", 680);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));

                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(680);
                panel.setTableWidth("100%");
                TableData tableData;
                tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                tableData.setWidth("10%");
                panel.add(ContentModelIconProvider.getInstance().getIcon((GWTJahiaNode) modelData).createImage());
                tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                tableData.setWidth("90%");
                panel.add(label, tableData);
                return panel;
            }
        });
        ColumnConfig action = new ColumnConfig("action", "Action", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                Button button = new Button("Remove", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                        catStore.remove(node1);
                    }
                });
                button.setData("associatedNode", modelData);
                button.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
                return button;
            }
        });
        action.setFixed(true);
        TreeGrid<GWTJahiaNode> catGrid = new TreeGrid<GWTJahiaNode>(catStore, new ColumnModel(Arrays.asList(
                columnConfig, action)));
        catGrid.setHeight(360);
        catGrid.setAutoExpandColumn("name");
        catGrid.getTreeView().setRowHeight(25);
        catGrid.getTreeView().setForceFit(true);
        categoriesTab.add(catGrid);
        tabPanel.add(categoriesTab);
    }

    private void createTagsPanel(TabPanel tabPanel) {
        ColumnConfig columnConfig;
        columnConfig = new ColumnConfig("name", "Name", 680);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(680);
                panel.setTableWidth("100%");
                TableData tableData;
                tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                tableData.setWidth("10%");
                panel.add(ContentModelIconProvider.getInstance().getIcon((GWTJahiaNode) modelData).createImage());
                tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                tableData.setWidth("90%");
                panel.add(label, tableData);
                return panel;
            }
        });
        ColumnConfig action = new ColumnConfig("action", "Action", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                Button button = new Button("Remove", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                        tagStore.remove(node1);
                    }
                });
                button.setData("associatedNode", modelData);
                button.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
                return button;
            }
        });
        tagTab = new AsyncTabItem(Messages.get("ece_tags", "Tags"));

        ContentPanel panel = new ContentPanel();
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setHeaderVisible(false);
        // Add a new tag
        final TextField<String> name = new TextField<String>();
        name.setMaxLength(120);
        name.setWidth(300);
        name.setName("tagName");

        //panel.add(name, data);
        Button addTag = new Button("Add", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                final String s = treeStore.getRootItems().get(1).getPath();
                async.getNode(s + "/" + name.getRawValue(), new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable caught) {

                        async.createNode(s, name.getRawValue(), "jnt:tag", null, new ArrayList<GWTJahiaNodeProperty>(), null,
                                new AsyncCallback<GWTJahiaNode>() {
                                    public void onFailure(Throwable caught) {
                                        com.google.gwt.user.client.Window.alert(
                                                "New tag creation failed\n\n" + caught.getLocalizedMessage());
                                        Log.error("failed", caught);
                                    }

                                    public void onSuccess(GWTJahiaNode result) {
                                        if (tagStore.findModel(result) == null) {
                                            tagStore.add(result, false);
                                        }
                                        treeStore.add(treeStore.getRootItems().get(1), result, true);
                                    }
                                });
                    }

                    public void onSuccess(GWTJahiaNode result) {
                        if (tagStore.findModel(result) == null) {
                            tagStore.add(result, false);
                        }
                    }
                });

            }
        });

        ButtonBar bar = new ButtonBar();
        bar.add(new FillToolItem());
        bar.add(new Text("Add Tag : "));
        bar.add(name);
        bar.add(addTag);
        panel.setTopComponent(bar);
        // Sub grid

        TreeGrid<GWTJahiaNode> tagGrid = new TreeGrid<GWTJahiaNode>(tagStore, new ColumnModel(Arrays.asList(
                columnConfig, action)));
        tagGrid.setHeight(360);

        tagGrid.setAutoExpandColumn("name");
        tagGrid.getTreeView().setRowHeight(25);
        tagGrid.getTreeView().setForceFit(true);
        panel.add(tagGrid);

        tagTab.add(panel);
        tabPanel.add(tagTab);
    }

    public TreeStore<GWTJahiaNode> getCatStore() {
        return catStore;
    }

    public TreeStore<GWTJahiaNode> getTagStore() {
        return tagStore;
    }
}
