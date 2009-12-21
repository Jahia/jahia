/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.WidgetTreeGridCellRenderer;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 21 déc. 2009
 */
public class ContentTypeTree extends LayoutContainer{
    private TreeGrid<ContentTypeModelData> treeGrid;
    private final Linker linker;
    private final GWTJahiaNodeType nodeType;
    private final String baseType;
    private final GWTJahiaNode parentNode;
    private final boolean displaySchmurtz;
    private final boolean displayPreview;
    private final int width;
    private final int height;
    private final int rowHeight;

    public ContentTypeTree(Linker linker, GWTJahiaNodeType nodeType, String baseType, GWTJahiaNode parentNode,
                           boolean displaySchmurtz,boolean displayPreview,int width,int height,int rowHeight) {
        this.linker = linker;
        this.nodeType = nodeType;
        this.baseType = baseType;
        this.parentNode = parentNode;
        this.displaySchmurtz = displaySchmurtz;
        this.displayPreview = displayPreview;
        this.width = width;
        this.height = height;
        this.rowHeight = rowHeight;
        createUI();
    }


    public void createUI() {
        final TreeStore<ContentTypeModelData> store = new TreeStore<ContentTypeModelData>();
        ContentTypeModelData rootEmptyContent = null;
        if (nodeType == null) {
            JahiaContentDefinitionService.App.getInstance().getNodeSubtypes(baseType, parentNode,
                                                                            new AsyncCallback<List<GWTJahiaNodeType>>() {
                                                                                public void onFailure(
                                                                                        Throwable caught) {
                                                                                    MessageBox.alert("Alert",
                                                                                                     "Unable to load content definitions for base type '" + baseType + "' and parent node '" + parentNode.getPath() + "'. Cause: " + caught.getLocalizedMessage(),
                                                                                                     null);
                                                                                }

                                                                                public void onSuccess(
                                                                                        List<GWTJahiaNodeType> result) {
                                                                                    for (GWTJahiaNodeType gwtJahiaNodeType : result) {
                                                                                        final ContentTypeModelData data = new ContentTypeModelData(
                                                                                                gwtJahiaNodeType);
                                                                                        store.add(data, false);
                                                                                        final GWTJahiaNode schmurtzFolder = new GWTJahiaNode();
                                                                                        schmurtzFolder.setPath(
                                                                                                "/schmurtzs/" + gwtJahiaNodeType.getName().replaceAll(
                                                                                                        ":", "_"));
                                                                                        JahiaContentManagementService.App.getInstance().ls(
                                                                                                JCRClientUtils.SCHMURTZ_REPOSITORY,
                                                                                                schmurtzFolder,
                                                                                                "jnt:schmurtz", null,
                                                                                                null, null, false,
                                                                                                new AsyncCallback<List<GWTJahiaNode>>() {
                                                                                                    public void onFailure(
                                                                                                            Throwable caught) {
                                                                                                    }

                                                                                                    public void onSuccess(
                                                                                                            List<GWTJahiaNode> result) {
                                                                                                        for (GWTJahiaNode gwtJahiaNode : result) {
                                                                                                            store.add(
                                                                                                                    data,
                                                                                                                    new ContentTypeModelData(
                                                                                                                            gwtJahiaNode),
                                                                                                                    false);
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
        } else {
            rootEmptyContent = new ContentTypeModelData(nodeType);
            store.add(rootEmptyContent, false);
        }
        if (displaySchmurtz) {
            final GWTJahiaNode schmurtzFolder = new GWTJahiaNode();
            if (nodeType != null && rootEmptyContent != null) {
                schmurtzFolder.setPath("/schmurtzs/" + nodeType.getName().replaceAll(":", "_"));
                final ContentTypeModelData finalRootEmptyContent = rootEmptyContent;
                JahiaContentManagementService.App.getInstance().ls(JCRClientUtils.SCHMURTZ_REPOSITORY, schmurtzFolder,
                                                                   "jnt:schmurtz", null, null, null, false,
                                                                   new AsyncCallback<List<GWTJahiaNode>>() {
                                                                       public void onFailure(Throwable caught) {
                                                                       }

                                                                       public void onSuccess(
                                                                               List<GWTJahiaNode> result) {
                                                                           for (GWTJahiaNode gwtJahiaNode : result) {
                                                                               store.add(finalRootEmptyContent,
                                                                                         new ContentTypeModelData(
                                                                                                 gwtJahiaNode), false);
                                                                           }
                                                                       }
                                                                   });
            }
        }
        ColumnConfig name = new ColumnConfig("label", "Label", width-15);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                ContentTypeModelData gwtJahiaNode = (ContentTypeModelData) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(width);
                panel.setTableWidth("100%");
                TableData tableData;
                if (gwtJahiaNode.getGwtJahiaNodeType() != null || gwtJahiaNode.getGwtJahiaNode() != null) {
                    tableData = new TableData(Style.HorizontalAlignment.RIGHT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("3%");
                    panel.add(ContentModelIconProvider.getInstance().getIcon(
                            gwtJahiaNode.getGwtJahiaNodeType()).createImage());
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("97%");
                    panel.add(label, tableData);
                } else {
                    tableData = new TableData(Style.HorizontalAlignment.LEFT, Style.VerticalAlignment.MIDDLE);
                    tableData.setWidth("100%");
                    panel.add(label, tableData);
                }
                return panel;
            }
        });
        name.setFixed(true);
        treeGrid = new TreeGrid<ContentTypeModelData>(store, new ColumnModel(Arrays.asList(name)));
        treeGrid.setBorders(true);
        treeGrid.setHeight(height);
        treeGrid.setAutoExpandColumn("label");
        treeGrid.getTreeView().setRowHeight(rowHeight);
        treeGrid.getTreeView().setForceFit(true);
        final LayoutContainer window = this;

        treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
        treeGrid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent baseEvent) {
                ContentTypeModelData contentTypeModelData = (ContentTypeModelData) (((TreeGridEvent) baseEvent).getModel());
                final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelData.getGwtJahiaNodeType();
                final GWTJahiaNode gwtJahiaNode = contentTypeModelData.getGwtJahiaNode();
                if (gwtJahiaNodeType != null) {
                    new EditContentEngine(linker, parentNode, gwtJahiaNodeType, null, false).show();
                    window.hide();
                } else if (gwtJahiaNode != null) {
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                    instance.getNode(gwtJahiaNode.getPath() + "/j:target", new AsyncCallback<GWTJahiaNode>() {
                        public void onFailure(Throwable caught) {
                            MessageBox.alert("Alert",
                                             "Unable to copy schmurtz to destination. Cause: " + caught.getLocalizedMessage(),
                                             null);
                        }

                        public void onSuccess(GWTJahiaNode result) {
                            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
                            result.setName(gwtJahiaNode.getName());
                            nodes.add(result);
                            instance.paste(nodes, parentNode.getPath(), false, new AsyncCallback() {
                                public void onFailure(Throwable caught) {
                                    //To change body of implemented methods use File | Settings | File Templates.
                                }

                                public void onSuccess(Object result) {
                                    hide();
                                    linker.refreshMainComponent();
                                }
                            });
                        }
                    });
                }
            }
        });
        ContentPanel previewPanel = null;
        if(displayPreview) {
            previewPanel = new ContentPanel();
            final LayoutContainer layoutContainer = new LayoutContainer();
        previewPanel.add(layoutContainer);
        treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ContentTypeModelData>() {
            @Override
            public void selectionChanged(
                    SelectionChangedEvent<ContentTypeModelData> contentTypeModelDataSelectionChangedEvent) {
                final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelDataSelectionChangedEvent.getSelectedItem().getGwtJahiaNodeType();
                final GWTJahiaNode gwtJahiaNode = contentTypeModelDataSelectionChangedEvent.getSelectedItem().getGwtJahiaNode();
                if (gwtJahiaNodeType != null) {
                    layoutContainer.removeAll();
                    layout();
                } else if (gwtJahiaNode != null) {
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                    instance.getRenderedContent(gwtJahiaNode.getPath() + "/j:target", null,
                                                ((EditLinker) linker).getLocale(), null, "wrapper.previewwrapper", null,
                                                false, new AsyncCallback<String>() {
                                public void onSuccess(String result) {
                                    HTML html = new HTML(result);
                                    layoutContainer.removeAll();
                                    layoutContainer.add(html);
                                    layout();
                                }

                                public void onFailure(Throwable caught) {
                                    Log.error("", caught);
//                    com.google.gwt.user.client.Window.alert("-update preview pp->" + caught.getMessage());
                                }
                            });
                }
            }
        });
        }
        add(treeGrid);
        if(previewPanel!=null)
            add(previewPanel);
    }

    public TreeGrid<ContentTypeModelData> getTreeGrid() {
        return treeGrid;
    }
}
