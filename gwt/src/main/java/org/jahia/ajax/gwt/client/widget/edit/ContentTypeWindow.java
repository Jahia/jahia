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
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
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
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 nov. 2009
 */
public class ContentTypeWindow extends Window {
    private String baseType;
    private GWTJahiaNode parentNode;
    private final Linker linker;
    private final GWTJahiaNodeType nodeType;
    private final boolean displaySchmurtz;
    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    private TreeGrid<ContentTypeModelData> treeGrid;

    public ContentTypeWindow(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType nodeType, boolean displaySchmurtz) {
        this.linker = linker;
        this.nodeType = nodeType;
        this.displaySchmurtz = displaySchmurtz;
        if (nodeType != null) {
            this.baseType = nodeType.getName();
        } else {
            baseType = null;
        }
        if (parent != null) {
            this.parentNode = parent;
        } else {
            this.parentNode = linker.getMainNode();
        }
        setLayout(new FillLayout());
        setBodyBorder(false);
        setSize(950, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        createUI();
        initFooter();
        setFooter(true);
        ok.setEnabled(true);
    }

    public void createUI() {
        final TreeStore<ContentTypeModelData> store = new TreeStore<ContentTypeModelData>();
        final ContentTypeModelData rootEmptyContent = new ContentTypeModelData("New Empty Content");
        store.add(rootEmptyContent, true);
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
                                                                                        store.add(rootEmptyContent,
                                                                                                  new ContentTypeModelData(
                                                                                                          gwtJahiaNodeType),
                                                                                                  false);
                                                                                    }
                                                                                }
                                                                            });
        } else {
            store.add(rootEmptyContent, new ContentTypeModelData(nodeType), false);
        }
        if (displaySchmurtz) {
            final ContentTypeModelData rootSchmurtzContent = new ContentTypeModelData("New Schmurtz Content");
            store.add(rootSchmurtzContent, true);
            final GWTJahiaNode schmurtzFolder = new GWTJahiaNode();
            if(nodeType!=null) {
                schmurtzFolder.setPath("/schmurtzs/"+nodeType.getName().replaceAll(":","_"));
                JahiaContentManagementService.App.getInstance().ls(JCRClientUtils.SCHMURTZ_REPOSITORY, schmurtzFolder,
                                                               "jnt:schmurtz", null, null, null, false,
                                                               new AsyncCallback<List<GWTJahiaNode>>() {
                                                                   public void onFailure(Throwable caught) {
                                                                   }

                                                                   public void onSuccess(List<GWTJahiaNode> result) {
                                                                       for (GWTJahiaNode gwtJahiaNode : result) {
                                                                           store.add(rootSchmurtzContent,
                                                                                     new ContentTypeModelData(
                                                                                             gwtJahiaNode), false);
                                                                       }
                                                                   }
                                                               });
            } else {
                JahiaContentManagementService.App.getInstance().getNodesOfType("jnt:schmurtz",
                                                               new AsyncCallback<List<GWTJahiaNode>>() {
                                                                   public void onFailure(Throwable caught) {
                                                                       MessageBox.alert("Alert",
                                                                                        "Unable to load available schmurtzs. Cause: " + caught.getLocalizedMessage(),
                                                                                        null);
                                                                   }

                                                                   public void onSuccess(List<GWTJahiaNode> result) {
                                                                       for (GWTJahiaNode gwtJahiaNode : result) {
                                                                           if(!gwtJahiaNode.getPath().contains("jnt_page")) {
                                                                           store.add(rootSchmurtzContent,
                                                                                     new ContentTypeModelData(
                                                                                             gwtJahiaNode), false);
                                                                           }
                                                                       }
                                                                   }
                                                               });
            }

        }
        ColumnConfig name = new ColumnConfig("label", "Label", 680);
        name.setRenderer(new WidgetTreeGridCellRenderer() {
            @Override
            public Widget getWidget(ModelData modelData, String s, ColumnData columnData, int i, int i1,
                                    ListStore listStore, Grid grid) {
                Label label = new Label((String) modelData.get(s));
                ContentTypeModelData gwtJahiaNode = (ContentTypeModelData) modelData;
                HorizontalPanel panel = new HorizontalPanel();
                panel.setWidth(695);
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
        treeGrid.setHeight(500);
        treeGrid.setAutoExpandColumn("label");
        treeGrid.getTreeView().setRowHeight(25);
        treeGrid.getTreeView().setForceFit(true);
        final Window window = this;
        final ContentPanel previewPanel = new ContentPanel();
        final LayoutContainer layoutContainer = new LayoutContainer();
        previewPanel.add(layoutContainer);
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
        add(treeGrid);
        add(previewPanel);
        layout();
    }

    /**
     * init buttons
     */
    private void initFooter() {
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);
        final Window window = this;
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        ok = new Button(Messages.getResource("fm_ok"));
        ok.setHeight(EditContentEngine.BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final ContentTypeWindow.ContentTypeModelData contentTypeModelData = treeGrid.getSelectionModel().getSelectedItem();
                if (contentTypeModelData != null) {
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
            }
        });


        buttonBar.add(ok);

        cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(EditContentEngine.BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                window.hide();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        // copyrigths
        Text copyright = new Text(Messages.getResource("fm_copyright"));
        ButtonBar container = new ButtonBar();
        container.setAlignment(Style.HorizontalAlignment.CENTER);
        container.add(copyright);
        buttonsPanel.add(container);
        setBottomComponent(buttonsPanel);


    }

    private class ContentTypeModelData extends BaseModelData implements Serializable {
        private GWTJahiaNodeType gwtJahiaNodeType;
        private GWTJahiaNode gwtJahiaNode;

        public ContentTypeModelData(String label) {
            set("label", label);
        }

        public ContentTypeModelData(GWTJahiaNodeType gwtJahiaNodeType) {
            this.gwtJahiaNodeType = gwtJahiaNodeType;
            set("label", gwtJahiaNodeType.getLabel());
        }

        public ContentTypeModelData(GWTJahiaNode gwtJahiaNode) {
            this.gwtJahiaNode = gwtJahiaNode;
            set("label", gwtJahiaNode.getDisplayName());
        }

        public String getLabel() {
            if (gwtJahiaNodeType != null) {
                return gwtJahiaNodeType.getLabel();
            } else if (gwtJahiaNode != null) {
                return gwtJahiaNode.getDisplayName();
            }
            return get("label");
        }

        public GWTJahiaNodeType getGwtJahiaNodeType() {
            return gwtJahiaNodeType;
        }

        public GWTJahiaNode getGwtJahiaNode() {
            return gwtJahiaNode;
        }
    }
}
