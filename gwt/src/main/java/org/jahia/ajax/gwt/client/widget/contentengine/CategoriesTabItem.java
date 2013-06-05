/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.wizard.ContentDefinitionCard;
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
    private transient List<GWTJahiaNode> catStore;
    private transient GWTJahiaNodeProperty categoryProperty;
    private transient ContentPanel topPanel = new ContentPanel(new ColumnLayout());
    private transient GWTJahiaNodeTreeFactory treeGridFactory;



    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            tab.setProcessed(true);
            tab.setLayout(new BorderLayout());
            topPanel.removeAll();
            topPanel.setScrollMode(Style.Scroll.AUTOY);
            topPanel.setHeading(Messages.get("label.selected.categories"));
            catStore = new ArrayList<GWTJahiaNode>();
            treeGridFactory = new GWTJahiaNodeTreeFactory(Arrays.asList("$systemsite/categories/*"),
                    Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.CHILDREN_INFO,GWTJahiaNode.NAME,GWTJahiaNode.DISPLAY_NAME));
            final GWTJahiaNode node = engine.getNode();
            if (node != null) {
                categoryProperty = engine.getProperties().get("j:defaultCategory");
                if (categoryProperty != null) {
                    final List<GWTJahiaNodePropertyValue> propertyValues = categoryProperty.getValues();
                    for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                        catStore.add(propertyValue.getNode());
                        topPanel.add(getOrCreateButton(propertyValue.getNode()));
                    }
                }
                tab.add(topPanel, new BorderLayoutData(Style.LayoutRegion.NORTH,75));
                if (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties",node) && !node.isLocked())) {
                    tab.add(createCategoriedPickerPanel(), new BorderLayoutData(Style.LayoutRegion.CENTER, 250));
                }
                tab.layout();

            }
        }
    }

    /**
     * Create Browser tree Grid
     *
     * @return
     */
    private Component createCategoriedPickerPanel() {

        treeGridFactory.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        final ColumnConfig name = new ColumnConfig();
        name.setId("displayName");
        name.setHeader(Messages.get("label.title"));
        name.setWidth(5000);
        name.setRenderer(new WidgetTreeGridCellRenderer<GWTJahiaNode>() {
            public String getId(TreeGrid<GWTJahiaNode> grid, GWTJahiaNode model, String property, int rowIndex, int colIndex) {
                return "JahiaGxtCategory_" + model.getName().replace(":", "_");
            }

            @Override
            public Widget getWidget(final GWTJahiaNode model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                CheckBox checkBox = new CheckBox();
                checkBox.setValue(catStore.contains(model));
                checkBox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        Button button = getOrCreateButton(model);
                        if (!catStore.contains(model)) {
                            catStore.add(model);
                            topPanel.add(button);
                        } else {
                            catStore.remove(model);
                            topPanel.remove(button);
                        }
                        topPanel.layout();
                    }
                });
                checkBox.setBoxLabel(getText((TreeGrid<GWTJahiaNode>) grid, model, property, rowIndex, colIndex));
                return checkBox;
            }
        });

        name.setFixed(true);
        TreeGrid<GWTJahiaNode> treeGrid = treeGridFactory.getTreeGrid(new ColumnModel(Arrays.asList(name)));
        treeGrid.setLayoutData(new FillLayout());
        treeGrid.setIconProvider(null);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        treeGrid.setBorders(true);
        treeGrid.setAutoExpandMax(5000);
        treeGrid.setAutoExpandColumn("displayName");
        treeGrid.getTreeView().setRowHeight(25);

        return treeGrid;
    }

    private Button getOrCreateButton(GWTJahiaNode n) {

        for (Component c : topPanel.getItems()) {
            if (c instanceof Button) {
                Button b = (Button) c;
                if (n.equals(b.getData("associatedNode"))) {
                    return b;
                }
            }
        }

        Button button = new Button(n.getDisplayName(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final GWTJahiaNode cat = buttonEvent.getButton().getData("associatedNode");
                topPanel.remove(buttonEvent.getButton());
                topPanel.layout();
                catStore.remove(cat);
                treeGridFactory.getLoader().load();
            }
        });
        button.setTitle(Messages.get("label.remove"));
        button.setData("associatedNode", n);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
        button.setId("JahiaGxtCategoryRemoveButton_"+((GWTJahiaNode) button.getData("associatedNode")).getName().replace(":","_"));
        return button;
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (catStore == null) {
            return;
        }

        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(catStore.size());
        for (GWTJahiaNode gwtJahiaNode : catStore) {
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

    @Override
    public void setProcessed(boolean processed) {
        if (!processed && catStore != null) {
            catStore = null;
            categoryProperty = null;
        }
        super.setProcessed(processed);
    }

}
