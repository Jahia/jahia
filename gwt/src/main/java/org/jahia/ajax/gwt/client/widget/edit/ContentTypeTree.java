/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.Arrays;
import java.util.List;

/**
 * 
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 21 déc. 2009
 */
public class ContentTypeTree extends LayoutContainer {
    private TreeGrid<GWTJahiaNode> treeGrid;
	private StoreFilterField<GWTJahiaNode> nameFilterField;
    private TreeStore<GWTJahiaNode> store;

    public ContentTypeTree() {
        setBorders(false);

        ColumnConfig name = new ColumnConfig("label", "Label", 400);
        name.setRenderer(new WidgetTreeGridCellRenderer<GWTJahiaNode>() {
            @Override
            public Widget getWidget(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label;
                GWTJahiaNodeType gwtJahiaNodeType = (GWTJahiaNodeType) modelData.get("componentNodeType");
                HorizontalPanel panel = new HorizontalPanel();
                panel.setTableWidth("100%");
                label = new Label(modelData.getDisplayName());
                TableData tableData;
                String descr = modelData.getDescription();
                if (gwtJahiaNodeType != null) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("5%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon(gwtJahiaNodeType).createImage());
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("95%");
                    if ((descr == null || descr.length() == 0) && !"".equals(gwtJahiaNodeType.getDescription())) {
                        descr = gwtJahiaNodeType.getDescription();  
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

        store = new TreeStore<GWTJahiaNode>();

        treeGrid = new TreeGrid<GWTJahiaNode>(store, new ColumnModel(Arrays.asList(name)));
        treeGrid.setBorders(true);
        treeGrid.setAutoExpandColumn("name");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        treeGrid.getTreeView().setBufferEnabled(true);
        treeGrid.getTreeView().setCacheSize(128);
        treeGrid.getTreeView().setScrollDelay(5);
        treeGrid.getStyle().setNodeCloseIcon(null);
        treeGrid.getStyle().setNodeOpenIcon(null);
        Layout layout = new BorderLayout();
        setLayout(layout);
        
        setBorders(false);
        
		nameFilterField = new StoreFilterField<GWTJahiaNode>() {
			@Override
			protected boolean doSelect(Store<GWTJahiaNode> store, GWTJahiaNode parent,
			        GWTJahiaNode record, String property, String filter) {


				if (record.getNodeTypes().contains("jnt:componentFolder")) {
                    return false;
                }

                String s = filter.toLowerCase();
                return record.getName().toLowerCase().contains(s) || record.getDisplayName().toLowerCase().contains(s);
			}
		};
		nameFilterField.bind(store);
        nameFilterField.setHeight(18);

        add(nameFilterField, new BorderLayoutData(Style.LayoutRegion.NORTH,22));
        add(treeGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
        setScrollMode(Style.Scroll.AUTOY);

    }

    public void fillStore(List<GWTJahiaNode> nodes) {
        store.add(nodes, true);
    }

    public void fillStore(List<String> paths, List<String> types, boolean includeSubTypes) {
        store.removeAll();
        JahiaContentManagementService.App.getInstance().getContentTypesAsTree(paths, types, Arrays.asList("name"), includeSubTypes,
                new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    public void onSuccess(List<GWTJahiaNode> result) {
                        store.add(result, true);
                    }
                }
        );
    }

    public TreeGrid<GWTJahiaNode> getTreeGrid() {
        return treeGrid;
    }

    public StoreFilterField<GWTJahiaNode> getNameFilterField() {
    	return nameFilterField;
    }

}
