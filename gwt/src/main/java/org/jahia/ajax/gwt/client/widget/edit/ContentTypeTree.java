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

package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 21 déc. 2009
 */
public class ContentTypeTree extends LayoutContainer {
    private TreeGrid<GWTJahiaNodeType> treeGrid;
    private TreeStore<GWTJahiaNodeType> store;

    public ContentTypeTree(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> types) {
        store = new TreeStore<GWTJahiaNodeType>();
        setBorders(false);
        filldataStore(types);
        ColumnConfig name = new ColumnConfig("label", "Label", 400);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) modelData;
                HorizontalPanel panel = new HorizontalPanel();
//                panel.setWidth(width - 40);
                panel.setTableWidth("100%");
                TableData tableData;
                if (gwtJahiaNodeType != null) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("5%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon(gwtJahiaNodeType).createImage());
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("95%");
                    panel.add(label, tableData);
                } else {
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("100%");
                    panel.add(label, tableData);
                }
                if (!"".equals(gwtJahiaNodeType.getDescription())) { panel.setToolTip(gwtJahiaNodeType.getDescription()); }
                panel.layout();
                return panel;
            }
        });
        treeGrid = new TreeGrid<GWTJahiaNodeType>(store, new ColumnModel(Arrays.asList(name)));
        treeGrid.setBorders(true);
        treeGrid.setAutoExpandColumn("label");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        Layout layout = new BorderLayout();
        setLayout(layout);
        
        setBorders(false);
        
		StoreFilterField<GWTJahiaNodeType> filter = new StoreFilterField<GWTJahiaNodeType>() {
			@Override
			protected boolean doSelect(Store<GWTJahiaNodeType> store, GWTJahiaNodeType parent,
			        GWTJahiaNodeType record, String property, String filter) {
				String name = record.getLabel();
				name = name.toLowerCase();
				return name.contains(filter.toLowerCase());
			}
		};
		filter.bind(store);
        filter.setHeight(18);

        add(filter, new BorderLayoutData(Style.LayoutRegion.NORTH,22));
        add(treeGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
    }

    public void filldataStore(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> types) {
        if (types != null) {
            if (types.get(null) != null || types.size() == 1) {
                for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNodeType>> entry : types.entrySet()) {
                    for (GWTJahiaNodeType gwtJahiaNodeType : entry.getValue()) {
                        store.add(gwtJahiaNodeType, true);
                    }
                }
            } else {
                for (Map.Entry<GWTJahiaNodeType, List<GWTJahiaNodeType>> entry : types.entrySet()) {
                    store.add(entry.getKey(), true);
                    for (GWTJahiaNodeType gwtJahiaNodeType : entry.getValue()) {
                        store.add(entry.getKey(), gwtJahiaNodeType, true);
                    }
                }
            }
            store.sort("label", Style.SortDir.ASC);
        }
    }

    public TreeGrid<GWTJahiaNodeType> getTreeGrid() {
        return treeGrid;
    }

}
