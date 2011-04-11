/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.form.AutoCompleteComboBox;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jun 30, 2010
 * Time: 10:04:47 AM
 *
 */
public class TagsTabItem extends EditEngineTabItem {
    private transient String locale = "en";
    private transient TreeStore<GWTJahiaNode> tagStore;
    private transient GWTJahiaNodeProperty tagProperty;
    private transient boolean tagAreI15d;

    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            tab.setLayout(new BorderLayout());
            final GWTJahiaNode node = engine.getNode();
            TreeLoader<GWTJahiaNode> tagLoader = new BaseTreeLoader<GWTJahiaNode>(new RpcProxy<List<GWTJahiaNode>>() {
                @Override
                protected void load(Object o, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                    if (node != null) {
                        tagProperty = engine.getProperties().get("j:tags");
                        if (tagProperty != null) {
                            final List<GWTJahiaNodePropertyValue> propertyValues = tagProperty.getValues();
                            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(propertyValues.size());
                            for (GWTJahiaNodePropertyValue propertyValue : propertyValues) {
                                nodes.add(propertyValue.getNode());
                            }
                            listAsyncCallback.onSuccess(nodes);
                        }
                    }
                }
            });
            tagStore = new TreeStore<GWTJahiaNode>(tagLoader);
            layout(engine, tab, node);
        }
    }

    private void layout(NodeHolder engine, AsyncTabItem tab, GWTJahiaNode node) {
        ColumnConfig columnConfig;
        columnConfig = new ColumnConfig("name", Messages.get("label.name"), 500);
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
                        tagStore.remove(node1);
                        tagProperty.getValues().remove(new GWTJahiaNodePropertyValue(node1, GWTJahiaNodePropertyType.WEAKREFERENCE));
                    }
                });
                button.setData("associatedNode", modelData);
                button.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
                return button;
            }
        });

        // Add a new tag
        final AutoCompleteComboBox autoCompleteComboBox = new AutoCompleteComboBox(JCRClientUtils.TAG_NODETYPES, 15);
        autoCompleteComboBox.setMaxLength(120);
        autoCompleteComboBox.setWidth(200);
        autoCompleteComboBox.setName("tagName");

        //panel.add(name, data);
        Button addTag = new Button(Messages.get("label.add", "Add"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
                async.getTagNode(autoCompleteComboBox.getRawValue(), true, new BaseAsyncCallback<GWTJahiaNode>() {
                    /**
                     * On success
                     * @param result
                     */
                    public void onSuccess(GWTJahiaNode result) {
                        if (tagStore.findModel(result) == null) {
                            tagStore.add(result, false);
                            tagProperty.getValues().add(new GWTJahiaNodePropertyValue(result,
                                    GWTJahiaNodePropertyType.WEAKREFERENCE));
                        }
                    }
                });

            }
        });

        if (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !node.isLocked())) {
            ButtonBar bar = new ButtonBar();
            bar.add(new FillToolItem());
            bar.add(new Text(Messages.get("label.add", "Add Tag") + ":"));
            bar.add(autoCompleteComboBox);
            bar.add(addTag);
            tab.add(bar, new BorderLayoutData(Style.LayoutRegion.NORTH, 45));
        }

        // Sub grid
        List<ColumnConfig> configs;
        if (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !node.isLocked())) {
            configs = Arrays.asList(columnConfig, action);
        } else {
            configs = Arrays.asList(columnConfig);
        }

        TreeGrid<GWTJahiaNode> tagGrid = new TreeGrid<GWTJahiaNode>(tagStore, new ColumnModel(configs));
        tagGrid.setIconProvider(ContentModelIconProvider.getInstance());
        tagGrid.setAutoExpandColumn("name");
        tagGrid.getTreeView().setRowHeight(25);
        tagGrid.getTreeView().setForceFit(true);
        tab.add(tagGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
        tab.layout();
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties) {
        updateProperties(changedProperties, node.getNodeTypes());
    }

    public void updateProperties(List<GWTJahiaNodeProperty> list, List<String> mixin) {
        if (tagStore == null) {
            return;
        }
        List<GWTJahiaNode> gwtJahiaNodes = tagStore.getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.REFERENCE));
        }
        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        gwtJahiaNodeProperty.setName("j:tags");
        if (this.tagProperty != null) {
            if (values.isEmpty()) {
                mixin.remove("jmix:tagged");
                getRemovedTypes().add("jmix:tagged");

            } else {
                list.add(gwtJahiaNodeProperty);
            }
        } else {
            if (!values.isEmpty()) {
                mixin.add("jmix:tagged");
                list.add(gwtJahiaNodeProperty);
            }
        }
    }

    public void setProcessed(boolean processed) {
        if (!processed && tagStore != null) {
            tagProperty = null;
        }
        super.setProcessed(processed);
    }
}
