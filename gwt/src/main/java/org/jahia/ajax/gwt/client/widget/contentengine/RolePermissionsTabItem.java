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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.*;

/**
 * Represents a dedicated tab for viewing and managing role to principal assignment.
 * 
 * @author Sergiy Shyrkov
 */
public class RolePermissionsTabItem extends EditEngineTabItem {

    private transient TreeStore<GWTJahiaNode> store;
    private transient Set<GWTJahiaNode> selection;
    private transient Map<String, List<String>> dependencies;
    private transient Map<String, CheckBox> boxes;
    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getNode() == null || tab.isProcessed()) {
            return;
        }
        GWTJahiaNodeProperty p = engine.getProperties().get("j:permissions");

        selection = new HashSet<GWTJahiaNode>();
        List<String> openPath = new ArrayList<String>();
        if (p != null) {
            for (GWTJahiaNodePropertyValue value : p.getValues()) {
                selection.add(value.getNode());
                openPath.add(value.getNode().getPath());
            }
        }
        dependencies = new HashMap<String, List<String>>();
        boxes = new HashMap<String, CheckBox>();

        GWTJahiaNodeTreeFactory treeGridFactory = new GWTJahiaNodeTreeFactory(Arrays.asList("/permissions"), Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.CHILDREN_INFO, "j:dependencies"));
        treeGridFactory.setOpenPath(openPath);
        treeGridFactory.setNodeTypes(Arrays.asList("jnt:permission"));

        BaseTreeLoader<GWTJahiaNode> loader = treeGridFactory.getLoader();
        store = treeGridFactory.getStore();
        final List<ColumnConfig> configs1 = new ArrayList<ColumnConfig>();
        ColumnConfig column = new ColumnConfig();
        column.setId("name");
        column.setHeader("");
        column.setRenderer(new TreeGridCellRenderer());
        column.setWidth(350);
        configs1.add(column);
        final GridCellRenderer<GWTJahiaNode> rolePermissionRenderer = new GridCellRenderer<GWTJahiaNode>() {
            public Object render(final GWTJahiaNode currentNode, String property, ColumnData config,
                                 final int rowIndex, final int colIndex, ListStore<GWTJahiaNode> s,
                                 final Grid<GWTJahiaNode> grid) {

                final CheckBox checkbox = new CheckBox();
                checkbox.setValue(selection.contains(currentNode));

                final GWTJahiaNode parentItem = store.getParent(currentNode);

                checkbox.setToolTip(currentNode.getName());
                boxes.put(currentNode.getPath(), checkbox);
                checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        try {
                            if ((Boolean) ((FieldEvent) event).getValue()) {
                                selection.add(currentNode);

                                // Update dependencies
                                List<String> toCheck = dependencies.get(currentNode.getPath());
                                if (toCheck != null) {
                                    for (String s1 : toCheck) {
                                        CheckBox checkBox = boxes.get(s1);
                                        if (checkBox!=null && !checkBox.getValue()) {
                                            checkBox.setValue(true);
                                        }
                                    }
                                }

                                // Update children
                                checkbox.setData("partial", null);
                                List<GWTJahiaNode> l = store.getChildren(currentNode, true);
                                for (GWTJahiaNode node : l) {
                                    CheckBox b = boxes.get(node.getPath());
                                    if (b != null) {
                                        b.setValue(true);
                                    }
                                    selection.remove(node);
                                }

                                // If all siblings set, set parent
                                if (parentItem != null) {
                                    CheckBox parentBox = boxes.get(parentItem.getPath());
                                    if (!parentBox.getValue()) {
                                        List<GWTJahiaNode> siblings = store.getChildren(parentItem);
                                        boolean checkParent = true;
                                        for (GWTJahiaNode sibling : siblings) {
                                            checkParent &= selection.contains(sibling);
                                        }
                                        if (checkParent) {
                                            parentBox.setData("partial", null);
                                            parentBox.setValue(true);
                                        }
                                    }
                                }
                            } else {
                                selection.remove(currentNode);

                                // Update dependencies
                                Set<String> toCheck = dependencies.keySet();
                                for (String s1 : toCheck) {
                                    if (dependencies.get(s1).contains(currentNode.getPath())) {
                                        CheckBox checkBox = boxes.get(s1);
                                        if (checkBox != null && checkBox.getValue()) {
                                            checkBox.setValue(false);
                                        }
                                    }
                                }

                                // Uncheck parent
                                if (parentItem != null) {
                                    CheckBox parentBox = boxes.get(parentItem.getPath());
                                    if (parentBox.getValue()) {
                                        parentBox.setData("partial", Boolean.TRUE);
                                        parentBox.setValue(false);

                                        List<GWTJahiaNode> siblings = store.getChildren(parentItem);
                                        for (GWTJahiaNode node : siblings) {
                                                if (!node.getPath().equals(currentNode.getPath())) {
                                                    selection.add(node);
                                                }
                                        }
                                    }
                                }

                                // Update children
                                if (checkbox.getData("partial") == null) {
                                    checkbox.setData("partial", null);
                                    List<GWTJahiaNode> l = store.getChildren(currentNode, true);
                                    for (GWTJahiaNode node : l) {
                                        CheckBox b = boxes.get(node.getPath());
                                        if (b != null) {
                                            b.setValue(false);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.debug("Exception on "+currentNode.getPath(), e);
                        }
                    }
                });

                if (parentItem != null) {
                    CheckBox parentBox = boxes.get(parentItem.getPath());
                    if (parentBox.getValue()) {
                        checkbox.setValue(true);
                        selection.remove(currentNode);
                    }
                }

                return checkbox;
            }
        };

        column = new ColumnConfig();
        column.setRenderer(rolePermissionRenderer);
        column.setId(engine.getNode().getName());
        column.setHeader(engine.getNode().getName());
        column.setWidth(100);
        column.setSortable(false);
        column.setGroupable(false);
        configs1.add(column);

        List<ColumnConfig> configs = configs1;
        TreeGrid<GWTJahiaNode> treeGrid = treeGridFactory.getTreeGrid(new ColumnModel(configs));

        treeGrid.setIconProvider(ContentModelIconProvider.getInstance());
        treeGrid.setBorders(true);
        treeGrid.setAutoExpandColumn("name");
        treeGrid.setAutoExpandMax(1000);
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);

        tab.setLayout(new FitLayout());
        tab.add(treeGrid);

        loader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                List<GWTJahiaNode> data = (List<GWTJahiaNode>) le.getData();
                for (GWTJahiaNode node : data) {
                    List<String> paths = node.get("j:dependencies");
                    if (paths != null) {
                        dependencies.put(node.getPath(), paths);
                    }
                }
            }
        });

        loader.load();

        tab.setProcessed(true);
        tab.layout();
    }

    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, GWTJahiaNodeACL acl) {
        if (selection != null) {
            List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(selection.size());
            for (GWTJahiaNode gwtJahiaNode : selection) {
                values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.WEAKREFERENCE));
            }
            GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
            gwtJahiaNodeProperty.setMultiple(true);
            gwtJahiaNodeProperty.setValues(values);
            gwtJahiaNodeProperty.setName("j:permissions");
            changedProperties.add(gwtJahiaNodeProperty);
        }
    }


}
