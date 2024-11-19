/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 21 déc. 2009
 */
public class ContentTypeTree extends LayoutContainer {

    private static final StoreSorter<GWTJahiaNodeType> SORTER = new StoreSorter<GWTJahiaNodeType>(new Comparator<Object>() {
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
    }) {
        @Override
        public int compare(Store<GWTJahiaNodeType> store, GWTJahiaNodeType n1, GWTJahiaNodeType n2,
                String property) {
            return super.compare(store, n1, n2, "label");
        }
    };

    private TreeGrid<GWTJahiaNodeType> treeGrid;
    private StoreFilterField<GWTJahiaNodeType> nameFilterField;
    private TreeStore<GWTJahiaNodeType> store;
    private boolean moduleFiltering;

    public ContentTypeTree() {
        this(Arrays.asList(new GWTColumn("label", Messages.get("label.name", "Name"), -1)));
    }

    public ContentTypeTree(List<GWTColumn> columns) {
        setBorders(false);

        String autoExpand = null;
        List<ColumnConfig> columnList = new ArrayList<ColumnConfig>();
        List<ComponentPlugin> pluginList = new ArrayList<ComponentPlugin>();
        moduleFiltering = false;
        for (GWTColumn column : columns) {
            int i = column.getSize();
            if (i == -1) {
                autoExpand = column.getKey();
                i = 400;
            }
            if (column.getKey().equals("label")) {
                ColumnConfig name = new ColumnConfig(column.getKey(), column.getTitle(), i);
                name.setRenderer(new WidgetTreeGridCellRenderer<GWTJahiaNodeType>() {
                    @Override
                    public Widget getWidget(GWTJahiaNodeType modelData, String s, ColumnData columnData, int i, int i1,
                                            ListStore<GWTJahiaNodeType> listStore, Grid<GWTJahiaNodeType> grid) {
                        Label label;
                        HorizontalPanel panel = new HorizontalPanel();
                        panel.setTableWidth("100%");
                        label = new Label(modelData.getLabel());
                        label.setStyleName("gwt-content-label");
                        TableData tableData;
                        String descr = modelData.getDescription();
                        if (modelData != null) {
                            tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                            tableData.setWidth("5%");
                            panel.add(ContentModelIconProvider.getInstance().getIcon(modelData).createImage());
                            tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                            tableData.setWidth("95%");
                            if ((descr == null || descr.length() == 0) && !"".equals(modelData.getDescription())) {
                                descr = modelData.getDescription();
                            }
                        } else {
                            tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                            tableData.setWidth("100%");
                        }
                        if (descr != null && descr.length() > 0) {
                            panel.setToolTip(descr);
                        }
                        panel.add(label, tableData);
                        panel.layout();
                        return panel;
                    }
                });
                columnList.add(name);
            }
        }


        store = new TreeStore<GWTJahiaNodeType>();
        store.setStoreSorter(SORTER);

        treeGrid = new TreeGrid<GWTJahiaNodeType>(store, new ColumnModel(columnList)) {
            @Override
            public void setExpanded(GWTJahiaNodeType model, boolean expand, boolean deep) {
                if (expand && model !=null && model.get("cannotexpand") != null) {
                    return;
                }
                super.setExpanded(model,expand,deep);
            }
        };
        treeGrid.setBorders(true);
        if (autoExpand != null) {
            treeGrid.setAutoExpandColumn(autoExpand);
        }
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getTreeView().setBufferEnabled(true);
        treeGrid.getTreeView().setCacheSize(128);
        treeGrid.getTreeView().setScrollDelay(5);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        for (ComponentPlugin plugin : pluginList) {
            treeGrid.addPlugin(plugin);
        }

        Layout layout = new BorderLayout();
        setLayout(layout);

        setBorders(false);

        nameFilterField = new StoreFilterField<GWTJahiaNodeType>() {
            @Override
            protected boolean doSelect(Store<GWTJahiaNodeType> store, GWTJahiaNodeType parent,
                                       GWTJahiaNodeType record, String property, String filter) {

                String s = filter.toLowerCase();
                return record.getName().toLowerCase().contains(s)
                        || record.getLabel().toLowerCase().contains(s);
            }
        };
        nameFilterField.bind(store);
        nameFilterField.setHeight(18);

        add(nameFilterField, new BorderLayoutData(Style.LayoutRegion.NORTH,22));
        add(treeGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
        setScrollMode(Style.Scroll.AUTOY);

    }

    public void fillStore(List<GWTJahiaNodeType> nodes) {
        for (GWTJahiaNodeType node : nodes) {
            store.add(node, true);
        }
    }

    public void fillStore(List<String> types, List<String> excludedTypes, boolean includeSubTypes) {
        store.removeAll();
        JahiaContentManagementService.App.getInstance().getContentTypesAsTree(types, excludedTypes, includeSubTypes,
                new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onSuccess(List<GWTJahiaNodeType> result) {
                        fillStore(result);
                    }
                }
        );
    }

    public TreeGrid<GWTJahiaNodeType> getTreeGrid() {
        return treeGrid;
    }

    public StoreFilterField<GWTJahiaNodeType> getNameFilterField() {
    	return nameFilterField;
    }

}
