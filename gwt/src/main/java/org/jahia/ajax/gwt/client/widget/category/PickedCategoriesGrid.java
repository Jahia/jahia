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
package org.jahia.ajax.gwt.client.widget.category;

import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:00:44
 */
public class PickedCategoriesGrid extends TopRightComponent {
    private ContentPanel m_component = new ContentPanel();
    private GridSelectionModel<GWTJahiaCategoryNode> selectionModel = new GridSelectionModel<GWTJahiaCategoryNode>();
    private List<GWTJahiaCategoryNode> selectedCategories;
    private ListStore<GWTJahiaCategoryNode> store;

    public PickedCategoriesGrid(final List<GWTJahiaCategoryNode> selectedCategories, final boolean readonly) {
        this.selectedCategories = selectedCategories;
        createCheckBox(readonly);
    }

    public void setContent(Object root) {
        //this methos is not implement for this componet
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        return store.getModels();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void refresh() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Component getComponent() {
        return m_component;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void createCheckBox(final boolean readonly) {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        //name
        ColumnConfig column = new ColumnConfig();
        column.setRenderer(new GridCellRenderer<GWTJahiaCategoryNode>() {
            public String render(GWTJahiaCategoryNode categoryNode, String property, ColumnData columnData, int rowIndex, int colIndex, ListStore<GWTJahiaCategoryNode> categoryNodeListStore) {
                return "<input type=\"hidden\" name=\"category_" + categoryNode.getCategoryId() + "\"/> " + categoryNode.getName();
            }


        });
        column.setId("name");
        column.setHeader(getResource("title"));
        configs.add(column);

        // path
//        column = new ColumnConfig();
//        column.setId("path");
//        column.setHeader(getResource("path"));
//        column.setWidth(300);
//        configs.add(column);

        // list loader
        store = new ListStore<GWTJahiaCategoryNode>();
        store.add(selectedCategories);
        store.setModelComparer(new ModelComparer<GWTJahiaCategoryNode>() {
            public boolean equals(GWTJahiaCategoryNode gwtJahiaCategoryNode, GWTJahiaCategoryNode gwtJahiaCategoryNode1) {
                String key1 = gwtJahiaCategoryNode.getKey();
                String key2 = gwtJahiaCategoryNode1.getKey();
                if (key1 == null && key2 == null) {
                    return true;
                } else if (key1 == null) {
                    return false;
                }
                return key1.equalsIgnoreCase(key2);
            }
        });
        ColumnModel columnModel = new ColumnModel(configs);
        store.setDefaultSort("name", Style.SortDir.ASC);

        // main component
        m_component.setHeading(getResource("categories_selected"));
        m_component.setFrame(true);
        m_component.setHeaderVisible(false);
        m_component.setLayout(new FillLayout());
        m_component.setSize(600, 300);

        // grid
        Grid<GWTJahiaCategoryNode> grid = new Grid<GWTJahiaCategoryNode>(store, columnModel);
        grid.setSelectionModel(selectionModel);
        grid.setBorders(true);
        grid.getView().setForceFit(true);

//        if (!readonly) {
//            TextToolItem unselectToolItem = new TextToolItem(getResource("remove"));
//            unselectToolItem.setIconStyle("remove");
//            unselectToolItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
//                public void componentSelected(ComponentEvent event) {
//                    List<GWTJahiaCategoryNode> categoryNodes = selectionModel.getSelectedItems();
//                    if (categoryNodes != null && !categoryNodes.isEmpty()) {
//                        Element ele = DOM.getElementById("removedCategories");
//                        if (ele != null) {
//                            String newValue = ele.getAttribute("value");
//                            for (GWTJahiaCategoryNode categoryNode : categoryNodes) {
//                                store.remove(categoryNode);
//                                // update hidden value
//
//                                newValue = newValue + "," + "category_" + categoryNode.getCategoryId();
//                            }
//
//                            ele.setAttribute("value", newValue);
//                        }
//                    }
//                }
//            });
//
//            ToolBar toolBar = new ToolBar();
//            toolBar.add(unselectToolItem);
//            m_component.setTopComponent(toolBar);
//        }
        m_component.add(grid);
    }

    public void addCategories(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
        for (GWTJahiaCategoryNode gwtJahiaCategoryNode : gwtJahiaCategoryNodes) {
            if (!store.contains(gwtJahiaCategoryNode)) {
                store.add(gwtJahiaCategoryNode);
            }
        }
        for (GWTJahiaCategoryNode gwtJahiaCategoryNode : store.getModels()) {
            if (!gwtJahiaCategoryNodes.contains(gwtJahiaCategoryNode)) {
                store.remove(gwtJahiaCategoryNode);
            }
        }
        store.sort("name", Style.SortDir.ASC);
    }

    public List<GWTJahiaCategoryNode> getCategories() {
        return store.getModels();
    }

    private String getResource(String key) {
        return Messages.getResource(key);
    }


}
