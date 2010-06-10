package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: May 11, 2010
 * Time: 12:08:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CategoryBrowseTabItem extends BrowseTabItem {
    protected LayoutContainer contentContainer;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected ListStore<GWTJahiaNode> contentStore;
    protected DisplayGridDragSource displayGridSource;

    public CategoryBrowseTabItem(final GWTSidePanelTab config) {
        super(config);

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaCategory, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                final String limitAsStrg = config.getParams() == null ? null : config.getParams().get("limit");
                final int limit = limitAsStrg == null ? 500 : Integer.parseInt(limitAsStrg);
                JahiaContentManagementService.App.getInstance().getNodesByCategory((GWTJahiaNode) gwtJahiaCategory, limit, 0, listAsyncCallback);
            }
        };

        listLoader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(listProxy);
        listLoader.addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                if (!le.isCancelled()) {
                    contentContainer.unmask();
                }
            }
        });

        contentStore = new ListStore<GWTJahiaNode>(listLoader);

        // grid
        final List<GWTColumn> columnNames = new ArrayList<GWTColumn>();
        columnNames.add(new GWTColumn("icon","icon",40));
        columnNames.add(new GWTColumn("displayName","displayName",300));
        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(new NodeColumnConfigList(columnNames,true)));
        contentContainer.add(grid);

  
        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                listLoader.load(event.getSelectedItem());
                contentContainer.mask("Loading", "x-mask-loading");
            }
        });

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));
        grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        add(contentContainer, contentVBoxData);

        displayGridSource = new DisplayGridDragSource(grid);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        displayGridSource.addDNDListener(editLinker.getDndListener());
    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains("jnt:content");
    }
}
