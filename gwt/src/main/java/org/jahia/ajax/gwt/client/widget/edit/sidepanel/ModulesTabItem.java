package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class ModulesTabItem extends BrowseTabItem {

    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        refreshFlag = EditLinker.REFRESH_PAGES;
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
            @Override public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> se) {
                listLoader.load(se.getSelectedItem());
            }
        });


        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(final Object gwtJahiaFolder, final AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
            }
        };

        listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(listProxy);

        contentStore = new ListStore<GWTJahiaNode>(listLoader);
        contentStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
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
        }));
        contentStore.setSortField("display");

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));

        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
    }

    @Override
    public void refresh() {
        tree.getTreeStore().removeAll();
        tree.getTreeStore().getLoader().load();
        listLoader.load();
        setRefreshed();
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return true;
    }
}
