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
package org.jahia.ajax.gwt.client.widget.category;

import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.allen_sauer.gwt.log.client.Log;
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
    private List<GWTJahiaCategoryNode> selectedCategories;
    private ListStore<GWTJahiaCategoryNode> store;
    private Grid<GWTJahiaCategoryNode> grid;
    private boolean readOnly;
    private boolean multiple;

    public PickedCategoriesGrid(final List<GWTJahiaCategoryNode> selectedCategories, final boolean readonly, final boolean multiple) {
        this.selectedCategories = selectedCategories;
        this.readOnly = readonly;
        this.multiple = multiple;
        createUI();
    }

    /**
     * Set content
     *
     * @param root
     */
    public void setContent(Object root) {
        Log.warn("Method setContent() not yet implemented");
    }

    /**
     * Remove all categories
     */
    public void clearTable() {
        store.removeAll();
    }

    /**
     * Get selected categories
     *
     * @return
     */
    public Object getSelection() {
        return store.getModels();
    }

    /**
     * refresh
     */
    public void refresh() {
        Log.warn("Method refresh() no implemented");
    }

    /**
     * Get component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * Create UI
     */
    private void createUI() {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        //name
        ColumnConfig column = new ColumnConfig();
        column.setRenderer(new GridCellRenderer<GWTJahiaCategoryNode>() {
            public String render(GWTJahiaCategoryNode categoryNode, String property, ColumnData columnData, int rowIndex, int colIndex, ListStore<GWTJahiaCategoryNode> categoryNodeListStore) {
                return "<input type=\"hidden\" name=\"category_" + categoryNode.getCategoryId() + "\"/> " + categoryNode.getExtendedName();
            }
        });
        column.setId("name");
        column.setHeader(Messages.getResource("name"));
        configs.add(column);

        // key
        column = new ColumnConfig();
        column.setId("key");
        column.setHeader(Messages.getResource("key"));
        configs.add(column);

        // path
        column = new ColumnConfig();
        column.setId("path");
        column.setRenderer(new GridCellRenderer<GWTJahiaCategoryNode>() {
            public String render(GWTJahiaCategoryNode categoryNode, String property, ColumnData columnData, int rowIndex, int colIndex, ListStore<GWTJahiaCategoryNode> categoryNodeListStore) {
                return categoryNode.getPath().replace("/root","");
            }
        });

        column.setHeader(Messages.getResource("path"));
        configs.add(column);


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
        store.sort("extendedName", Style.SortDir.ASC);

        // main component
        m_component.setHeading(Messages.getResource("categories"));
        m_component.setFrame(false);
        m_component.setHeaderVisible(false);
        m_component.setLayout(new FitLayout());
        m_component.setSize(600, 300);
        m_component.setBodyBorder(false);
        m_component.setBorders(false);

        // grid
        grid = new Grid<GWTJahiaCategoryNode>(store, columnModel);
        grid.setBorders(true);
        grid.getView().setForceFit(true);

        m_component.add(grid);
    }

    /**
     * Add categories to the UI
     *
     * @param gwtJahiaCategoryNodes
     */
    public void addCategories(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
        if (readOnly) {
            return;
        }
        if (!multiple && gwtJahiaCategoryNodes.size() > 0) {
            store.removeAll();
            store.add(gwtJahiaCategoryNodes.get(0));
        } else {
            List<GWTJahiaCategoryNode> toAdd = new ArrayList<GWTJahiaCategoryNode>();
            for (GWTJahiaCategoryNode gwtJahiaCategoryNode : gwtJahiaCategoryNodes) {
                boolean add = true;
                for (GWTJahiaCategoryNode n : store.getModels()) {
                    if (gwtJahiaCategoryNode.getPath().equals(n.getPath())) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    toAdd.add(gwtJahiaCategoryNode);
                }
            }
            store.add(toAdd);
            store.sort("extendedName", Style.SortDir.ASC);
        }
    }

    /**
     * Remove categories from the UI
     */
    public void removeSelectedCategories() {
        if (readOnly) {
            return;
        }
        List<GWTJahiaCategoryNode> toremove = new ArrayList<GWTJahiaCategoryNode>();
        for (GWTJahiaCategoryNode selectedNode : grid.getSelectionModel().getSelection()) {
            toremove.add(selectedNode);
        }
        for (GWTJahiaCategoryNode n : toremove) {
            store.remove(n);
        }
    }

    /**
     * Get all categories
     *
     * @return
     */
    public List<GWTJahiaCategoryNode> getCategories() {
        return store.getModels();
    }

    public void initContextMenu() {
        Menu m = new Menu();
        MenuItem menuItem = new MenuItem(Messages.getResource("information"));
        m.add(menuItem);
        menuItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                GWTJahiaCategoryNode node = grid.getSelectionModel().getSelectedItem();
                if (node != null) {
                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.OK);
                    box.setIcon(MessageBox.INFO);
                    box.setTitle(Messages.getResource("information")+": "+node.getExtendedName());
                    box.setMessage(node.getPath().replace("/root",""));
                    box.show();
                }else{
                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.OK);
                    box.setIcon(MessageBox.WARNING);
                    box.setMessage("Please select a category");
                    box.show();
                }
            }
        });
        grid.setContextMenu(m);
    }

}
