package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Dec 21, 2009
* Time: 2:22:30 PM
* To change this template use File | Settings | File Templates.
*/
class BrowseTabItem extends SidePanelTabItem {
    private boolean init = true;
    protected LayoutContainer treeContainer;
    protected TreeLoader<GWTJahiaNode> treeLoader;
    protected TreeStore<GWTJahiaNode> treeStore;
    protected TreeGrid<GWTJahiaNode> tree;

    public BrowseTabItem(final String repositoryType, final String nodetypes) {
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        setLayout(l);

        treeContainer = new LayoutContainer();
        treeContainer.setBorders(true);
        treeContainer.setScrollMode(Style.Scroll.AUTO);
        treeContainer.setLayout(new FitLayout());
        // data proxy
        RpcProxy<List<GWTJahiaNode>> treeProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    JahiaContentManagementService.App.getInstance().getRoot(repositoryType, nodetypes, "", "", null, listAsyncCallback);
                } else {
                    JahiaContentManagementService.App.getInstance().ls(repositoryType,(GWTJahiaNode) gwtJahiaFolder, nodetypes, "", "", null, false, listAsyncCallback);
                }
            }
        };

        treeLoader = new BaseTreeLoader<GWTJahiaNode>(treeProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren();
            }

            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false");
                    init = false;
                }

            }
        };
        treeStore = new TreeStore<GWTJahiaNode>(treeLoader);
        ColumnConfig columnConfig = new ColumnConfig("displayName","Name",80);
        columnConfig.setRenderer(new TreeGridCellRenderer());
//        ColumnConfig author = new ColumnConfig("createdBy", "Author", 40);
        tree = new TreeGrid<GWTJahiaNode>(treeStore, new ColumnModel(Arrays.asList(columnConfig)));
        tree.setAutoExpandColumn("displayName");
        tree.getTreeView().setRowHeight(25);
        tree.getTreeView().setForceFit(true);
        tree.setHeight("100%");
        tree.setIconProvider(ContentModelIconProvider.getInstance());

        treeContainer.add(tree);

        VBoxLayoutData treeVBoxData = new VBoxLayoutData();
        treeVBoxData.setFlex(1);

        add(treeContainer, treeVBoxData);
        
    }
}
