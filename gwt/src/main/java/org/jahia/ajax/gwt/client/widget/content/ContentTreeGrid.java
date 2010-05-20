/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * TreeTable file picker for use within classic engines.
 */
public class ContentTreeGrid extends LayoutContainer {
    private ManagerLinker linker;
    private ListStore<GWTJahiaNode> mainListStore;
    private final GWTManagerConfiguration configuration;
    private List<GWTJahiaNode> selectedNodes;
    private boolean multiple;
    private String repositoryType;

    /**
     * Content tree table
     *
     * @param repositoryType
     * @param selectedNodes
     * @param multiple
     * @param configuration
     */
    public ContentTreeGrid(String repositoryType, List<GWTJahiaNode> selectedNodes, boolean multiple, final GWTManagerConfiguration configuration) {
        this.multiple = multiple;
        this.linker = new ManagerLinker(configuration);
        this.repositoryType = repositoryType;
        this.configuration = configuration;
        this.selectedNodes = selectedNodes;

    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
        setLayout(new FitLayout());
        final ContentPanel mainContent = new ContentPanel();
        mainContent.setLayout(new BorderLayout());
        mainContent.setHeaderVisible(false);
        mainContent.setBorders(false);
        mainContent.setBodyBorder(false);

        // add toolbar
        final ContentToolbar contentToolbar = new ContentToolbar(configuration, linker);
        final TreeGridTopRightComponent treeGridTopRightComponent = new TreeGridTopRightComponent(repositoryType, configuration, selectedNodes);


        // register component linker
        linker.registerComponents(null, treeGridTopRightComponent, null, contentToolbar, null);

        // add grid
        BorderLayoutData borderLayoutData = new BorderLayoutData(Style.LayoutRegion.WEST, 300);
        borderLayoutData.setSplit(true);
        mainContent.add(treeGridTopRightComponent.getComponent(), borderLayoutData);

        // center
        final LayoutContainer contentContainer = new LayoutContainer();
        final ThumbsListView thumbsListView = new ThumbsListView(false);
        BaseListLoader<ListLoadResult<GWTJahiaNode>> listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                contentContainer.mask(Messages.get("label_loading","Loading"));
                JahiaContentManagementService.App.getInstance()
                        .lsLoad((GWTJahiaNode) gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), null,
                                GWTJahiaNode.DEFAULT_FIELDS, listAsyncCallback);

            }
        });
        mainListStore = new ListStore<GWTJahiaNode>(listLoader);
        mainListStore.addStoreListener(new StoreListener<GWTJahiaNode>() {
            public void storeDataChanged(StoreEvent<GWTJahiaNode> se) {
                for (GWTJahiaNode selectedNode : selectedNodes) {
                    if (mainListStore.contains(selectedNode)) {
                        thumbsListView.getSelectionModel().select(true, selectedNode);
                    }
                }
            }
        });
        listLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                if (!le.isCancelled()) {
                    contentContainer.unmask();
                }
            }
        });

        thumbsListView.setStore(mainListStore);
        thumbsListView.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event != null && event.getSelectedItem() != null) {
                    onContentPicked(event.getSelectedItem());
                }
            }
        });

        BorderLayoutData centerLayoutData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        centerLayoutData.setSplit(true);

        contentContainer.setId("images-view");
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);
        contentContainer.add(thumbsListView);
        mainContent.add(contentContainer, centerLayoutData);

        mainContent.setTopComponent(contentToolbar.getComponent());

        add(mainContent);
        

    }


    /**
     * Override thi method to customize "add" button behaviour
     *
     * @param gwtJahiaNode the Picked Node
     */
    public void onContentPicked(final GWTJahiaNode gwtJahiaNode) {

    }


    /**
     * Get repository type
     *
     * @return String
     */
    protected String getRepoType() {
        return repositoryType;
    }


    /**
     * Tree Grid TopRightComponent wrapper
     */
    private class TreeGridTopRightComponent extends TopRightComponent {
        private String repositoryType;
        private GWTManagerConfiguration configuration;
        private boolean init = true;
        private TreeGrid<GWTJahiaNode> m_treeGrid;
        private TreeLoader<GWTJahiaNode> loader;
        private List<GWTJahiaNode> selectedNodes;

        private TreeGridTopRightComponent(String repositoryType, GWTManagerConfiguration configuration, List<GWTJahiaNode> selectedNodes) {
            this.repositoryType = repositoryType;
            this.configuration = configuration;
            this.selectedNodes = selectedNodes;
            init();
        }

        /**
         * init
         */
        private void init() {

            GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(repositoryType != null ? repositoryType : JCRClientUtils.GLOBAL_REPOSITORY);
            factory.setNodeTypes(configuration.getFolderTypes());
            factory.setMimeTypes(configuration.getMimeTypes());
            factory.setFilters(configuration.getFilters());
            factory.setFields(GWTJahiaNode.DEFAULT_FIELDS);
            List<String> selectedPath = new ArrayList<String>();
            for (GWTJahiaNode node : selectedNodes) {
                final String s = node.getPath();
                selectedPath.add(s.substring(0, s.lastIndexOf("/")));
            }
            factory.setSelectedPath(selectedPath);
            loader = factory.getLoader();
            NodeColumnConfigList columnConfigList = new NodeColumnConfigList(configuration.getTableColumns());
            columnConfigList.init();
            columnConfigList.get(0).setRenderer(new TreeGridCellRenderer());

            if (multiple) {
                ColumnConfig picker = new ColumnConfig("action", "action", 100);

                picker.setAlignment(Style.HorizontalAlignment.RIGHT);
                picker.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        if (gwtJahiaNode.isMatchFilters()) {
                            final Button pickContentButton = new Button(Messages.get("label.add", "Add"));
                            pickContentButton.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
                            pickContentButton.setEnabled(true);
                            pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent buttonEvent) {
                                    onContentPicked(gwtJahiaNode);
                                }
                            });
                            return pickContentButton;
                        } else {
                            return new Text("");
                        }
                    }

                });
                picker.setFixed(true);

                columnConfigList.add(picker);
            }

            m_treeGrid = factory.getTreeGrid(new ColumnModel(columnConfigList));
            m_treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                    if (mainListStore != null) {
                        mainListStore.getLoader().load(m_treeGrid.getSelectionModel().getSelectedItem());
                    }
                }
            });
            m_treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                    getLinker().onTreeItemSelected();
                }
            });

            m_treeGrid.getTreeView().setRowHeight(25);
            m_treeGrid.setIconProvider(ContentModelIconProvider.getInstance());
            /* m_treeGrid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
               @Override
               public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                   if (event != null && event.getSelectedItem() != null) {
                       onContentPicked(event.getSelectedItem());
                   }
               }
           }); */
            m_treeGrid.setHideHeaders(true);
            m_treeGrid.setBorders(false);


        }

        public void setContent(Object root) {
            // not implemented
        }

        public void clearTable() {
            // not implemebented
        }

        public Object getSelection() {
            if (m_treeGrid == null) {
                return null;
            }
            return m_treeGrid.getSelectionModel().getSelectedItems();
        }

        public void refresh() {
            m_treeGrid.getStore().removeAll();
            init = true;
            loader.load(null);
        }

        public Component getComponent() {
            return m_treeGrid;
        }
    }

}
