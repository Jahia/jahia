package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
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
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Dec 21, 2009
* Time: 2:22:24 PM
* To change this template use File | Settings | File Templates.
*/
class FilesBrowseTabItem extends BrowseTabItem {
    protected LayoutContainer contentContainer;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected ListStore contentStore;
    protected DisplayGridDragSource displayGridSource;

    public FilesBrowseTabItem() {
        super(JCRClientUtils.ALL_FILES, JCRClientUtils.FOLDER_NODETYPES);
        setText("&nbsp;");
        setIcon(ContentModelIconProvider.CONTENT_ICONS.file());

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName()) ;
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, "nt:file", null, null, null, true, listAsyncCallback);
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

        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("ext", "", 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("displayName", Messages.getResource("fm_info_name"), 280));
        final Grid grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));

        contentContainer.add(grid);

        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                listLoader.load(event.getSelectedItem());
                contentContainer.mask("Loading","x-mask-loading");
            }
        });

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
}