/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.content.ThumbsListView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.*;

/**
 * Side panel tab item for browsing the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
class ContentBrowseTabItem extends BrowseTabItem {
    protected transient LayoutContainer contentContainer;
    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient DragSource displayGridSource;
    protected transient Grid<GWTJahiaNode> grid;
    protected transient ThumbsListView  listView;
    private List<String> displayGridForTypes;

    @Override
    public TabItem create(final GWTSidePanelTab config) {
        super.create(config);

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());

        // data proxy
        final List<String> loadedNodeTypes = new ArrayList<String>();
        loadedNodeTypes.addAll(JCRClientUtils.FILE_NODETYPES);
        loadedNodeTypes.addAll(JCRClientUtils.CONTENT_NODETYPES);

        RpcProxy<PagingLoadResult<GWTJahiaNode>> listProxy = getPagingLoadResultRpcProxy(config, loadedNodeTypes);

        listLoader = new BaseListLoader<>(listProxy);
        listLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                if (!le.isCancelled()) {
                    contentContainer.unmask();
                }
            }
        });

        contentStore = new ListStore<GWTJahiaNode>(listLoader);
        contentStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));

        addSelectionChangedListener();

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));


        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);

        if (config.getTableColumns().size() == 1)  {
            listView = new ThumbsListView(true);
            listView.setStyleAttribute("overflow-x", "hidden");
            listView.setStore(contentStore);
            contentStore.setSortField("display");
            listView.addListener(Events.DoubleClick, new Listener<ListViewEvent<GWTJahiaNode>>() {

                @Override
                public void handleEvent(ListViewEvent<GWTJahiaNode> be) {
                    Window w = new Window();
                    w.addStyleName("content-browse-preview");
                    GWTJahiaNode node = listView.getSelectionModel().getSelectedItem();

                    final String text = "Preview of " + node.getDisplayName();
                    w.setHeadingHtml(text);
                    w.setScrollMode(Style.Scroll.AUTO);
                    w.setModal(true);
                    w.setClosable(true);
                    w.setMaximizable(true);
                    w.setSize(Math.max(Integer.parseInt((String) node.get("j:width")) + 60, 400), Math.max(Integer.parseInt((String) node.get("j:height")) + 80, 50));
                    w.setBlinkModal(true);
                    w.setPlain(true);
                    w.setToolTip(text);
                    w.setLayout(new CenterLayout());
                    w.add(new Image(listView.getSelectionModel().getSelectedItem().getUrl()));
                    w.show();
                }
            });
            listView.setContextMenu(createContextMenu(config.getTableContextMenu(), listView.getSelectionModel()));
            contentContainer.add(listView);

        } else {
            NodeColumnConfigList displayColumns = new NodeColumnConfigList(config.getTableColumns(), true);
            grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));
            grid.setAutoExpandColumn(displayColumns.getAutoExpand());
            contentContainer.add(grid);


            grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));

        }

        contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);
        tab.setId("JahiaGxtContentBrowseTab");
        return tab;
    }

    private void addSelectionChangedListener() {
        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                final GWTJahiaNode node = event.getSelectedItem();
                if (node != null) {
                    boolean displayGrid = displayGridForTypes == null || node.getInheritedNodeTypes().stream()
                            .anyMatch(type -> displayGridForTypes.contains(type));
                    if (displayGrid) {
                        listLoader.load(node);
                        contentContainer.mask(Messages.get("label.loading", "Loading..."), "x-mask-loading");
                    } else {
                        contentStore.removeAll();
                    }
                    if (!node.getPath().equals(editLinker.getMainModule().getPath()) && node.getNodeTypes().contains("jnt:page")
                            && hasPermission()) {
                        MainModule.staticGoTo(node.getPath(), null);
                    }
                } else {
                    contentStore.removeAll();
                }
            }
        });
    }

    private boolean hasPermission() {
        return PermissionsUtils.isPermitted("pageComposerAccess", JahiaGWTParameters.getSiteNode())
                || PermissionsUtils.isPermitted("jContentAccess", JahiaGWTParameters.getSiteNode())
                || PermissionsUtils.isPermitted("studioModeAccess", JahiaGWTParameters.getSiteNode());
    }

    private RpcProxy<PagingLoadResult<GWTJahiaNode>> getPagingLoadResultRpcProxy(GWTSidePanelTab config, List<String> loadedNodeTypes) {
        return new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                if (gwtJahiaFolder != null) {
                    String path = ((GWTJahiaNode) gwtJahiaFolder).getPath();
                    Log.debug("retrieving children of " + path);
                    List<String> tableColumnKeys = new ArrayList<> (config.getTableColumnKeys());
                    tableColumnKeys.addAll(Arrays.asList(GWTJahiaNode.PERMISSIONS, GWTJahiaNode.ICON, GWTJahiaNode.LOCKABLE, GWTJahiaNode.LOCKED, GWTJahiaNode.LOCKS_INFO));
                    JahiaContentManagementService.App.getInstance()
                            .lsLoad(path, loadedNodeTypes, null, null, tableColumnKeys, false, -1, -1, false, null, null, false, false, listAsyncCallback);
                } else {
                    contentContainer.unmask();
                }
            }
        };
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        if (linker.getConfig().isDragAndDropEnabled()) {
            if (grid != null) {
                displayGridSource = new DisplayGridDragSource(grid);
            }
            if (listView != null) {
                displayGridSource = new ImageDragSource(listView);
            }
            displayGridSource.addDNDListener(editLinker.getDndListener());
        }
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains("jnt:content");
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        data.put(Linker.REFRESH_MAIN, false);
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.getNodeTypes().contains("jnt:contentFolder")
                    || node.getInheritedNodeTypes().contains("jmix:droppableContent")) {
                return true;
            }
        }
        return super.needRefresh(data);
    }

    public void setDisplayGridForTypes(List<String> displayGridForTypes) {
        this.displayGridForTypes = displayGridForTypes;
    }
}
