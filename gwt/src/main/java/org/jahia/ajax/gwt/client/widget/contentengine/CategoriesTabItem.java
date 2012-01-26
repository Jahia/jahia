/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.*;

/**
 * 
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * 
 */
public class CategoriesTabItem extends EditEngineTabItem {
    private transient TreeStore<GWTJahiaNode> catStore;
    private transient GWTJahiaNodeProperty categoryProperty;

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            tab.setProcessed(true);
            tab.setLayout(new BorderLayout());
            final GWTJahiaNode node = engine.getNode();
            initCategoriesStoreA(node, engine);
            if (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties",node) && !node.isLocked())) {
                tab.add(createCategoriedPickerPanel(), new BorderLayoutData(Style.LayoutRegion.NORTH, 250));
            }
            tab.add(createSelectedCategoriesPanel(engine), new BorderLayoutData(Style.LayoutRegion.CENTER));
            tab.layout();
        }
    }

    /**
     * Create Browser tree Grid
     *
     * @return
     */
    private TreeGrid<GWTJahiaNode> createCategoriedPickerPanel() {
        GWTJahiaNodeTreeFactory treeGridFactory =
                new GWTJahiaNodeTreeFactory(Arrays.asList("$systemsite/categories"), Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.CHILDREN_INFO,GWTJahiaNode.NAME,GWTJahiaNode.DISPLAY_NAME));
        treeGridFactory.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        ColumnConfig name = new ColumnConfig("displayName", Messages.get("label.title"), 500);
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
        treeGrid.setAutoExpandColumn("displayName");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        return treeGrid;
    }

    /**
     * init categories store
     *
     * @param node
     * @param engine
     */
    private void initCategoriesStoreA(final GWTJahiaNode node, final NodeHolder engine) {
        TreeLoader<GWTJahiaNode> catLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (node != null) {
                    categoryProperty = engine.getProperties().get("j:defaultCategory");
                    if (categoryProperty != null) {
                        final List<GWTJahiaNodePropertyValue> propertyValues = categoryProperty.getValues();
                        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(propertyValues.size());
                        for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                            nodes.add(propertyValue.getNode());
                        }
                        listAsyncCallback.onSuccess(nodes);
                    }
                }
            }
        });

        catStore = new TreeStore<GWTJahiaNode>(catLoader);
        catStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1,s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));
    }

    private Component createSelectedCategoriesPanel(NodeHolder engine) {
        ColumnConfig columnConfig = new ColumnConfig("displayName", Messages.get("label.title"), 500);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());


        ColumnConfig action = new ColumnConfig("action", Messages.get("label.action"), 100);
        action.setAlignment(Style.HorizontalAlignment.RIGHT);
        action.setRenderer(new GridCellRenderer() {
            public Object render(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore listStore, Grid grid) {
                Button button = new Button(Messages.get("label.remove"), new SelectionListener<ButtonEvent>() {
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

        List<ColumnConfig> configs;
        final GWTJahiaNode node = engine.getNode();
        if (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties",node) && !node.isLocked())) {
            configs = Arrays.asList(columnConfig, action);
        } else {
            configs = Arrays.asList(columnConfig);
        }

        TreeGrid<GWTJahiaNode> catGrid = new TreeGrid<GWTJahiaNode>(catStore, new ColumnModel(configs));
        catGrid.setIconProvider(ContentModelIconProvider.getInstance());
        catGrid.setAutoExpandColumn("displayName");
        catGrid.getTreeView().setRowHeight(25);
        catGrid.getTreeView().setForceFit(true);


        return catGrid;
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        if (catStore == null) {
            return;
        }

        List<GWTJahiaNode> gwtJahiaNodes = catStore.getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.REFERENCE));
        }
        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        gwtJahiaNodeProperty.setName("j:defaultCategory");
        if (this.categoryProperty != null) {
            if (values.isEmpty()) {
                addedTypes.remove("jmix:categorized");
                removedTypes.add("jmix:categorized");
            } else {
                changedProperties.add(gwtJahiaNodeProperty);
            }
        } else {
            if (!values.isEmpty()) {
                addedTypes.add("jmix:categorized");
                changedProperties.add(gwtJahiaNodeProperty);
            }
        }
    }

    public void setProcessed(boolean processed) {
        if (!processed && catStore != null) {
            catStore = null;
            categoryProperty = null;
        }
        super.setProcessed(processed);
    }
    
}
