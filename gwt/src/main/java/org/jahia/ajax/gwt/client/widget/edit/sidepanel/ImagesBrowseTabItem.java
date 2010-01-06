package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.content.ThumbsListView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;

/**
* Side panel tab item for browsing image resources in the repository.
* User: toto
* Date: Dec 21, 2009
* Time: 2:22:24 PM
*/
class ImagesBrowseTabItem extends BrowseTabItem {
    protected LayoutContainer contentContainer;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected ListStore<GWTJahiaNode> contentStore;
    protected ImageDragSource dragSource;

    public ImagesBrowseTabItem() {
        super(JCRClientUtils.ALL_FILES, JCRClientUtils.FOLDER_NODETYPES);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.img());

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName()) ;
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, "nt:file", "image/*", null, true, listAsyncCallback);
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

        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                contentContainer.mask("Loading","x-mask-loading");
                listLoader.load(event.getSelectedItem());
            }
        });

        tree.setContextMenu(createContextMenu("org.jahia.toolbar.sidePanel.images", tree.getSelectionModel()));

        final ThumbsListView listView = new ThumbsListView(true);
        listView.setStyleAttribute("overflow-x",  "hidden");
        listView.setStore(contentStore);
        contentStore.setSortField("display");
        contentContainer.add(listView);

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        add(contentContainer, contentVBoxData);

        listView.addListener(Events.DoubleClick, new Listener<ListViewEvent<GWTJahiaNode>>(){
            public void handleEvent(ListViewEvent<GWTJahiaNode> be) {
                Window w = new Window();
                GWTJahiaNode node = listView.getSelectionModel().getSelectedItem();

                final String text = "Preview of " + node.getDisplayName();
                w.setHeading(text);
                w.setScrollMode(Style.Scroll.AUTO);
                w.setModal(true);
                w.setClosable(true);
                w.setMaximizable(true);
                w.setSize(Math.max(node.getWidth()+60, 400), Math.max(node.getHeight()+80, 50));
                w.setBlinkModal(true);
                w.setPlain(true);
                w.setToolTip(text);
                w.setLayout(new CenterLayout());
                w.add(new Image(listView.getSelectionModel().getSelectedItem().getUrl()));
                w.show();

            }
        });

        listView.setContextMenu(createContextMenu("org.jahia.toolbar.sidePanel.images.preview", listView.getSelectionModel()));
        
        dragSource = new ImageDragSource(listView);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        dragSource.addDNDListener(linker.getDndListener());
    }

    public class ImageDragSource extends ListViewDragSource {
        public ImageDragSource(ListView<GWTJahiaNode> listView) {
            super(listView);
            DragListener listener = new DragListener() {
                public void dragEnd(DragEvent de) {
                    DNDEvent e = new DNDEvent(ImageDragSource.this, de.getEvent());
                    e.setData(data);
                    e.setDragEvent(de);
                    e.setComponent(component);
                    e.setStatus(statusProxy);

                    onDragEnd(e);
                }
            };
            draggable.addDragListener(listener);
        }

        @Override
        protected void onDragStart(DNDEvent e) {
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.CONTENT_SOURCE_TYPE);
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
            nodes.add((GWTJahiaNode) listView.getSelectionModel().getSelectedItem());
            e.setData(nodes);
            List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>(1);
            list.add((GWTJahiaNode) listView.getSelectionModel().getSelectedItem());
            e.getStatus().setData("size", list.size());
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, list);
            e.setOperation(DND.Operation.COPY);
            super.onDragStart(e);
        }

        @Override
        protected void onDragCancelled(DNDEvent dndEvent) {
            super.onDragCancelled(dndEvent);
            onDragEnd(dndEvent);
        }

        protected void onDragEnd(DNDEvent e) {
            StatusProxy sp = e.getStatus();
            sp.setData(EditModeDNDListener.SOURCE_TYPE, null);
            sp.setData(EditModeDNDListener.CONTENT_SOURCE_TYPE, null);
            sp.setData(EditModeDNDListener.TARGET_TYPE, null);
            sp.setData(EditModeDNDListener.TARGET_NODE, null);
            sp.setData(EditModeDNDListener.TARGET_PATH, null);
            sp.setData(EditModeDNDListener.SOURCE_NODES, null);
            sp.setData(EditModeDNDListener.SOURCE_QUERY, null);
            sp.setData(EditModeDNDListener.SOURCE_TEMPLATE, null);
            sp.setData(EditModeDNDListener.OPERATION_CALLED, null);
            e.setData(null);
        }


    }

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains("nt:file");
    }


}
