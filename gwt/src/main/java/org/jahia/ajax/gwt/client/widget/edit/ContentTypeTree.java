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

package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
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

    private static final StoreSorter<GWTJahiaNode> SORTER = new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
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
        public int compare(Store<GWTJahiaNode> store, GWTJahiaNode n1, GWTJahiaNode n2,
                String property) {
            return super.compare(store, n1, n2, "displayName");
        }
    };

    private TreeGrid<GWTJahiaNode> treeGrid;
    private StoreFilterField<GWTJahiaNode> nameFilterField;
    private TreeStore<GWTJahiaNode> store;
    private boolean moduleFiltering;

    public ContentTypeTree() {
        this(Arrays.asList(new GWTColumn("label", Messages.get("label.displayName", "Name"), -1)));
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
                name.setRenderer(new WidgetTreeGridCellRenderer<GWTJahiaNode>() {
                    @Override
                    public Widget getWidget(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                            ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> grid) {
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

                    @Override
                    protected TreePanel.Joint calcualteJoint(TreeGrid<GWTJahiaNode> gwtJahiaNodeTreeGrid, GWTJahiaNode model, String property, int rowIndex, int colIndex) {
                        if (model.getNodeTypes().contains("jnt:virtualsite")) {
                            boolean checked = JahiaGWTParameters.getSiteNode().getName().equals(model.getName()) || (JahiaGWTParameters.getSiteNode().get("j:dependencies") != null && ((List<String>) JahiaGWTParameters.getSiteNode().get("j:dependencies")).contains(model.getName()));
                            if (!checked) {
                                model.set("cannotexpand", Boolean.TRUE);
                                return TreePanel.Joint.NONE;
                            } else {
                                model.set("cannotexpand", null);
                            }
                        }
                        return super.calcualteJoint(gwtJahiaNodeTreeGrid, model, property, rowIndex, colIndex);
                    }
                });
                columnList.add(name);
            } else if (column.getKey().equals("dependency")) {

                CheckColumnConfig chk = new CheckColumnConfig(column.getKey(), column.getTitle(), i) {

                    protected void init() {
                        setRenderer(new GridCellRenderer<ModelData>() {
                            public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,
                                                 ListStore<ModelData> store, Grid<ModelData> grid) {
                                if (((GWTJahiaNode) model).getNodeTypes().contains("jnt:virtualsite")) {
                                    String s = onRender(model, property, config, rowIndex, colIndex, store);
                                    Text text = new Text(s);
                                    boolean checked = JahiaGWTParameters.getSiteNode().get("j:dependencies") != null && ((List<String>) JahiaGWTParameters.getSiteNode().get("j:dependencies")).contains(((GWTJahiaNode) model).getName());
                                    String tooltip = checked?Messages.get("label.removeDependency","Click to remove the dependency"):Messages.get("label.addDependency","Click to add the dependency");
                                    text.setToolTip(tooltip);
                                    return text;
                                } else {
                                    return "";
                                }
                            }
                        });
                    }


                    @Override
                    protected String getCheckState(ModelData model, String property, int rowIndex,
                                                   int colIndex) {
                        boolean checked = JahiaGWTParameters.getSiteNode().get("j:dependencies") != null && ((List<String>) JahiaGWTParameters.getSiteNode().get("j:dependencies")).contains(((GWTJahiaNode) model).getName());
                        boolean disabled = JahiaGWTParameters.getSiteNode().getName().equals(((GWTJahiaNode) model).getName());
                        if (disabled) {
                            return "-disabled";
                        } else if (checked) {
                            return "-on";
                        } else {
                            return "";
                        }
                    }

                    @Override
                    protected void onMouseDown(GridEvent<ModelData> ge) {
                        El el = ge.getTargetEl();
                        if (el != null && el.hasStyleName("x-grid3-cc-" + getId()) && !el.hasStyleName("x-grid3-check-col-disabled")) {
                            ge.stopEvent();
                            GWTJahiaNode model = (GWTJahiaNode) ge.getModel();

                            List<String> depends = (List<String>) JahiaGWTParameters.getSiteNode().get("j:dependencies");
                            // init depends if not
                            if (depends == null) {
                                depends = new ArrayList<String>();
                                JahiaGWTParameters.getSiteNode().set("j:dependencies", depends);
                            }
                            String modelName = model.getName();

                            boolean checked = depends.contains(modelName);

                            if (checked) {
                                depends.remove(modelName);
                                treeGrid.setExpanded(model, false);
                            } else {
                                depends.add(modelName);
                            }

                            StoreEvent<GWTJahiaNode> evt = new StoreEvent<GWTJahiaNode>(treeGrid.getStore());
                            evt.setModel(model);
                            treeGrid.getStore().fireEvent(Store.Update, evt);

                            List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
                            final GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                            gwtJahiaNodeProperty.setName("j:dependencies");

                            final List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>();
                            for (String s : depends) {
                                values.add(new GWTJahiaNodePropertyValue(s, GWTJahiaNodePropertyType.STRING));
                            }
                            gwtJahiaNodeProperty.setMultiple(true);
                            gwtJahiaNodeProperty.setValues(values);
                            properties.add(gwtJahiaNodeProperty);
                            treeGrid.mask(Messages.get("label.saving", "Saving..."));
                            JahiaContentManagementService.App.getInstance().saveProperties(Arrays.asList(JahiaGWTParameters.getSiteNode()), properties, null, new BaseAsyncCallback() {
                                public void onSuccess(Object result) {
                                    Log.debug("ok");
                                    treeGrid.unmask();
                                }

                                @Override
                                public void onApplicationFailure(Throwable caught) {
                                    Log.debug("ko");
                                    treeGrid.unmask();
                                }
                            });

                        }
                    }
                };
                moduleFiltering = true;
                columnList.add(chk);
                pluginList.add(chk);
            }
        }


        store = new TreeStore<GWTJahiaNode>();
        store.setStoreSorter(SORTER);

        treeGrid = new TreeGrid<GWTJahiaNode>(store, new ColumnModel(columnList)) {
            @Override
            public void setExpanded(GWTJahiaNode model, boolean expand, boolean deep) {
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

        nameFilterField = new StoreFilterField<GWTJahiaNode>() {
            @Override
            protected boolean doSelect(Store<GWTJahiaNode> store, GWTJahiaNode parent,
                    GWTJahiaNode record, String property, String filter) {

                if (record.getNodeTypes().contains("jnt:componentFolder")) {
                    return false;
                }
                if (moduleFiltering) {
                    String module = record.getPath().substring(record.getPath().indexOf('/',1)+1);
                    if (module.indexOf("/") > -1) {
                        module = module.substring(0, module.indexOf("/"));
                    }
                }
                String s = filter.toLowerCase();
                return record.getName().toLowerCase().contains(s)
                        || record.getDisplayName().toLowerCase().contains(s);
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

    public void fillStore(List<String> paths, List<String> types, boolean includeSubTypes, boolean includeNonDependentModules) {
        store.removeAll();
        JahiaContentManagementService.App.getInstance().getContentTypesAsTree(paths, types, Arrays.asList("name"), includeSubTypes,  includeNonDependentModules,
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
