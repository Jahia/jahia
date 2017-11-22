/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridView;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Hover;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Side panel tab item for browsing the pages tree.
 * @autor toto
 */
@SuppressWarnings("serial")
public class TemplatesTabItem extends BrowseTabItem {

    private transient TreeGrid<GWTJahiaNode> detailTree;
    private transient TreeLoader<GWTJahiaNode> detailLoader;
    private transient TreeStore<GWTJahiaNode> detailStore;
    protected transient LayoutContainer contentContainer;
    protected transient SelectMainNodeTreeLoadListener selectMainNodeTreeLoadListener;

    private  List<String> displayedDetailTypes;
    private  List<String> hiddenDetailTypes;


    /**
     * Performs the creation of the tab item and populates its content. The tab contains two panes: one with the tree of templates,
     * available in the module, and the second pane with the detail structure of the template content.
     *
     * @param config
     *            the tab configuration
     * @return the created tab item
     */
    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        this.tree.setSelectionModel(new TreeGridSelectionModel<GWTJahiaNode>() {
            @Override
            protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
                super.handleMouseClick(e);
                if (!getSelectedItem().getPath().equals(editLinker.getMainModule().getPath())) {
                    if (!getSelectedItem().getNodeTypes().contains("jnt:virtualsite") && !getSelectedItem().getNodeTypes().contains("jnt:templatesFolder")) {
                        MainModule.staticGoTo(getSelectedItem().getPath(), null);
                    }
                }
            }
        });
        this.tree.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        this.tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                detailTree.getSelectionModel().deselectAll();
                if (se.getSelectedItem() != null) {
                    detailLoader.load(se.getSelectedItem());
                } else {
                    detailStore.removeAll();
                }
            }
        });


        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));
        selectMainNodeTreeLoadListener = new SelectMainNodeTreeLoadListener(tree);

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);

        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object currentPage, final AsyncCallback<List<GWTJahiaNode>> callback) {
                if (currentPage != null) {
                    GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) currentPage;
                    List<String> fields = new ArrayList<String>();
                    fields.add(GWTJahiaNode.LOCKS_INFO);
                    fields.add(GWTJahiaNode.PERMISSIONS);
                    fields.add(GWTJahiaNode.CHILDREN_INFO);
                    fields.add(GWTJahiaNode.ICON);
                    JahiaContentManagementService.App.getInstance()
                            .lsLoad(gwtJahiaNode.getPath(), displayedDetailTypes, null, null, fields, false, 0, 0, false,
                                    hiddenDetailTypes, null, false, false, new AsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    callback.onFailure(caught);
                                }

                                @Override
                                public void onSuccess(PagingLoadResult<GWTJahiaNode> nodes) {
                                    callback.onSuccess(nodes.getData());
                                }
                            });
                }
            }
        };

        detailLoader = new BaseTreeLoader<GWTJahiaNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasChildren();
            }
        };
        detailStore = new TreeStore<GWTJahiaNode>(detailLoader);
        detailTree = new TreeGrid<GWTJahiaNode>(detailStore, new ColumnModel(columns));


        detailTree.setAutoExpandColumn("displayName");
        detailTree.getTreeView().setRowHeight(25);
        detailTree.getTreeView().setForceFit(true);
        detailTree.setHeight("100%");
        detailTree.setIconProvider(ContentModelIconProvider.getInstance());
        detailTree.setAutoExpand(true);
        detailTree.setContextMenu(createContextMenu(config.getTableContextMenu(), detailTree.getSelectionModel()));
        detailTree.setView(new TreeGridView() {

            @Override
            protected void handleComponentEvent(GridEvent ge) {
                switch (ge.getEventTypeInt()) {
                    case Event.ONMOUSEOVER:
                        GWTJahiaNode selection = (GWTJahiaNode) ge.getModel();
                        if (selection != null) {
                            List<Module> modules = ModuleHelper.getModulesByPath().get(selection.getPath());
                            if (modules != null) {
                                for (Module module : modules) {
                                    Hover.getInstance().addHover(module, ge);
                                }
                            }
                        }
                        break;
                    case Event.ONMOUSEOUT:
                        selection = (GWTJahiaNode) ge.getModel();
                        if (selection != null) {
                            List<Module> modules = ModuleHelper.getModulesByPath().get(selection.getPath());
                            if (modules != null) {
                                for (Module module : modules) {
                                    Hover.getInstance().removeHover(module);
                                }
                            }
                        }
                        break;
                }
                editLinker.getMainModule().setCtrlActive(ge);
                super.handleComponentEvent(ge);
            }

        });
        detailTree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                if (se.getSelection() != null) {
                    for (GWTJahiaNode selection : se.getSelection()) {
                        List<Module> modules = ModuleHelper.getModulesByPath().get(selection.getPath());
                        if (modules != null) {
                            for (Module module : modules) {
                                if (!editLinker.getMainModule().getSelections().containsKey(module))  {
                                    editLinker.getMainModule().handleNewModuleSelection(module);
                                }
                            }
                        }
                    }
                }
            }
        });

        contentContainer=new LayoutContainer();
        contentContainer.setBorders(false);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());
        contentContainer.setTitle(Messages.get("label.detail", "detail"));
        contentContainer.add(detailTree);
        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);

        return tab;
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.isPage() || node.getNodeTypes().contains("jnt:externalLink")
                    || node.getNodeTypes().contains("jnt:nodeLink")
                    || node.getNodeTypes().contains("jnt:template") || node.getInheritedNodeTypes().contains("jnt:template")
                    || node.getInheritedNodeTypes().contains("jmix:visibleInPagesTree")) {
                return true;
            }
        }
        if (data.containsKey("event") && "languageChanged".equals(data.get("event"))) {
            return true;
        }
        return false;
    }

    @Override
    public void doRefresh() {
        super.doRefresh();
        detailTree.getTreeStore().removeAll();
        detailTree.getTreeStore().getLoader().load();
        detailLoader.load();
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getNodeTypes().contains("jnt:page");
    }

    public native String getTemplate() /*-{
        return ['<tpl for=".">',
            '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
            '<div><b>{type}: </b>{name}</div>',
            '<div><b>Template: </b>{template}</div>',
            '<div><b>Key: </b>{key}</div>',
            '<div><b>Apply on : </b>{applyOn}</div>',
            '<div style="padding-left: 10px; padding-top: 10px; clear: left">{description}</div></div></tpl>',
            '<div class="x-clear"></div>'].join("");
    }-*/;

    public void setDisplayedDetailTypes(List<String> displayedDetailTypes) {
        this.displayedDetailTypes = displayedDetailTypes;
    }

    public void setHiddenDetailTypes(List<String> hiddenDetailTypes) {
        this.hiddenDetailTypes = hiddenDetailTypes;
    }

    @Override
    public void handleNewMainSelection(String path) {
        selectMainNodeTreeLoadListener.handleNewMainSelection(path);
    }
}