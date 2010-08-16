/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.form.AutoCompleteComboBox;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

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
public class CategoriesEditor extends ContentPanel {
    private TreeStore<GWTJahiaNode> catStore;
    private static final int TREE_COLUMN_SIZE = 200;


    public CategoriesEditor(final GWTJahiaNode node) {
        setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        setWidth("100%");
        setHeight("100%");
        setBorders(false);
        setHeaderVisible(false);
        initCategoriesStoreA(node);
        final ContentPanel catPanel = new ContentPanel();
        catPanel.setHeaderVisible(false);
        catPanel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
        catPanel.add(createCategoriedPickerPanel(), new RowData(1, -1, new Margins(4)));
        catPanel.add(createSelectedCategoriesPanel(), new RowData(1, -1, new Margins(4)));

        add(catPanel, new RowData(1, 1, new Margins(4)));
        layout();

    }

    /**
     * Create Browser tree Grid
     *
     * @return
     */
    private TreeGrid<GWTJahiaNode> createCategoriedPickerPanel() {
        GWTJahiaNodeTreeFactory treeGridFactory = new GWTJahiaNodeTreeFactory(Arrays.asList("/categories"), GWTJahiaNode.DEFAULT_REFERENCE_FIELDS);
        treeGridFactory.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        ColumnConfig name = new ColumnConfig("name", "Name",500);
        name.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
        name.setFixed(true);
        ColumnConfig action = new ColumnConfig("action", "Action", 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) modelData;
                Button button = null;
                if (gwtJahiaNode.getNodeTypes().contains("jnt:category")) {
                    button = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
                        @Override
                        public void componentSelected(ButtonEvent buttonEvent) {
                            final GWTJahiaNode node1 = (GWTJahiaNode) buttonEvent.getButton().getData("associatedNode");
                            if (catStore.findModel(node1) == null) {
                                catStore.add(node1, false);
                            }
                        }
                    });
                    button.setData("associatedNode", modelData);
                    button.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
                }
                return button != null ? button : new Label("");
            }
        });
        action.setFixed(true);
        TreeGrid<GWTJahiaNode> treeGrid = treeGridFactory.getTreeGrid(new ColumnModel(Arrays.asList(name, action)));

        treeGrid.setIconProvider(ContentModelIconProvider.getInstance());

        treeGrid.setBorders(true);
        treeGrid.setHeight(250);
        treeGrid.setAutoExpandColumn("name");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        return treeGrid;
    }

    /**
     * init categories store
     *
     * @param node
     */
    private void initCategoriesStoreA(final GWTJahiaNode node) {
        TreeLoader<GWTJahiaNode> catLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (node != null) {
                    final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                    async.getProperties(node.getPath(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
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
            }
        });

        catStore = new TreeStore<GWTJahiaNode>(catLoader);
    }

    private Component createSelectedCategoriesPanel() {
        ColumnConfig columnConfig = new ColumnConfig("name", "Name", 500);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());


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
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                return button;
            }
        });
        action.setFixed(true);
        TreeGrid<GWTJahiaNode> catGrid = new TreeGrid<GWTJahiaNode>(catStore, new ColumnModel(Arrays.asList(columnConfig, action)));
        catGrid.setIconProvider(ContentModelIconProvider.getInstance());
        catGrid.setHeight(360);
        catGrid.setAutoExpandColumn("name");
        catGrid.getTreeView().setRowHeight(25);
        catGrid.getTreeView().setForceFit(true);


        return catGrid;
    }

    public TreeStore<GWTJahiaNode> getCatStore() {
        return catStore;
    }

}
