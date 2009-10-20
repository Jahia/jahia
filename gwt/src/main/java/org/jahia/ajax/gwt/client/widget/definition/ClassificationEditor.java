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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.TableData;
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
public class ClassificationEditor extends VerticalPanel {
    private TreeStore<GWTJahiaNode> catStore;
    private TreeStore<GWTJahiaNode> tagStore;

    public ClassificationEditor(final GWTJahiaNode node) {
        TreeStore<GWTJahiaNode> treeStore = createStoreAndLoader(node);
        createTreeGrid(treeStore);
        // Now manage the tab for categories and tags
        TabPanel tabPanel = new TabPanel();
        createCategoriesPanel(tabPanel);
        createTagsPanel(tabPanel);
    }

    private void createTreeGrid(TreeStore<GWTJahiaNode> treeStore) {
        ColumnConfig name = new ColumnConfig("name", "Name", 500);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(400);
                panel.setTableWidth("100%");
                TableData tableData;
                tableData = new TableData();
                tableData.setWidth("80%");
                panel.add(label);
                if (gwtJahiaNode.getNodeTypes().contains("jnt:category")) {
                    Button button = new Button("Add", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                            if(catStore.findModel(node1)==null)
                            catStore.add(node1, false);
                        }
                    });
                    button.setData("associatedNode", modelData);
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    panel.add(button, tableData);
                } else if (gwtJahiaNode.getNodeTypes().contains("jnt:tag")) {
                    Button button = new Button("Add", new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                            if(tagStore.findModel(node1)==null)
                            tagStore.add(node1, false);
                        }
                    });
                    button.setData("associatedNode", modelData);
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    panel.add(button, tableData);
                }
                return panel;
            }
        });
        TreeGrid<GWTJahiaNode> treeGrid = new TreeGrid<GWTJahiaNode>(treeStore, new ColumnModel(Arrays.asList(name)));
        treeGrid.setBorders(true);
        treeGrid.setWidth(500);
        treeGrid.setHeight(250);
        treeGrid.setAutoExpandColumn("name");
        treeGrid.getTreeView().setRowHeight(25);
        this.setBorders(false);
        this.setWidth(500);
        this.setHeight(500);
        TableData tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.TOP);
        tableData.setWidth("100%");
        tableData.setHeight("50%");
        this.add(treeGrid, tableData);
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
        TreeStore<GWTJahiaNode> treeStore = new TreeStore<GWTJahiaNode>(loader);
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
        AsyncTabItem categoriesTab = new AsyncTabItem(Messages.get("ece_categories", "Categories"));
        ColumnConfig columnConfig = new ColumnConfig("name", "Name", 500);
        columnConfig.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                Button button = new Button("Remove", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                        catStore.remove(node1);
                    }
                });
                button.setData("associatedNode", modelData);
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(400);
                panel.setTableWidth("100%");
                TableData tableData;
                tableData = new TableData();
                tableData.setWidth("80%");
                panel.add(label);
                tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                panel.add(button, tableData);
                return panel;
            }
        });
        TreeGrid<GWTJahiaNode> catGrid = new TreeGrid<GWTJahiaNode>(catStore, new ColumnModel(Arrays.asList(
                columnConfig)));
        catGrid.setHeight(250);
        catGrid.setWidth(500);
        catGrid.setAutoExpandColumn("name");
        catGrid.getTreeView().setRowHeight(25);
        categoriesTab.add(catGrid);
        tabPanel.add(categoriesTab);
        tabPanel.setWidth(500);
        tabPanel.setHeight(250);
    }

    private void createTagsPanel(TabPanel tabPanel) {
        ColumnConfig columnConfig;
        TableData tableData;
        columnConfig = new ColumnConfig("name", "Name", 500);
        columnConfig.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                Button button = new Button("Remove", new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                        tagStore.remove(node1);
                    }
                });
                button.setData("associatedNode", modelData);
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(400);
                panel.setTableWidth("100%");
                TableData tableData;
                tableData = new TableData();
                tableData.setWidth("80%");
                panel.add(label);
                tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                panel.add(button, tableData);
                return panel;
            }
        });
        AsyncTabItem tagTab = new AsyncTabItem(Messages.get("ece_tags", "Tags"));
        HorizontalPanel panel = new HorizontalPanel();
        panel.setWidth(500);
        panel.setTableWidth("100%");
        // Add a new tag
        final TextField<String> name = new TextField<String>();
        name.setMaxLength(60);
        TableData data = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        data.setWidth("20%");
        panel.add(new Label("Add Tag"), data);
        name.setName("tagName");
        data = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
        data.setWidth("60%");
        panel.add(name,data);
        Button addTag = new Button("Add", new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getNode("/content/tags/" + name.getRawValue(), new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable caught) {
                        async.createNode("/content/tags", name.getRawValue(), "jnt:tag",
                                         new ArrayList<GWTJahiaNodeProperty>(), null,
                                         new AsyncCallback<GWTJahiaNode>() {
                                             public void onFailure(Throwable caught) {
                                                 com.google.gwt.user.client.Window.alert(
                                                         "New Tag creation failed\n\n" + caught.getLocalizedMessage());
                                                 Log.error("failed", caught);
                                             }

                                             public void onSuccess(GWTJahiaNode result) {
                                                 if(tagStore.findModel(result)==null)
                                                 tagStore.add(result, false);
                                             }
                                         });
                    }

                    public void onSuccess(GWTJahiaNode result) {
                        if(tagStore.findModel(result)==null)
                        tagStore.add(result,false);
                    }
                });

            }
        });
        data = new TableData(Style.HorizontalAlignment.CENTER, Style.VerticalAlignment.MIDDLE);
        data.setWidth("20%");
        panel.add(addTag,data);
        tagTab.add(panel);
        // Sub grid
        TreeGrid<GWTJahiaNode> tagGrid = new TreeGrid<GWTJahiaNode>(tagStore, new ColumnModel(Arrays.asList(
                columnConfig)));
        tagGrid.setHeight(250);
        tagGrid.setWidth(500);
        tagGrid.setAutoExpandColumn("name");
        tagGrid.getTreeView().setRowHeight(25);
        tagTab.add(tagGrid);
        tabPanel.add(tagTab);
        tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.TOP);
        tableData.setWidth("100%");
        tableData.setHeight("50%");
        this.add(tabPanel, tableData);
    }

    public TreeStore<GWTJahiaNode> getCatStore() {
        return catStore;
    }

    public TreeStore<GWTJahiaNode> getTagStore() {
        return tagStore;
    }
}
