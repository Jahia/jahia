package org.jahia.ajax.gwt.client.widget.edit;

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
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.content.ThumbsListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 * To change this template use File | Settings | File Templates.
 */
class MashupBrowseTabItem extends BrowseTabItem {
    protected LayoutContainer contentContainer;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected ListStore contentStore;
    protected ImageDragSource dragSource;

    public MashupBrowseTabItem() {
        super(JCRClientUtils.ALL_MASHUPS, JCRClientUtils.FOLDER_NODETYPES);
        setText("&nbsp;");
        setIcon(ContentModelIconProvider.CONTENT_ICONS.mashup());

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName());
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, "jnt:portlet", null, null, null, true, listAsyncCallback);
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
                contentContainer.mask("Loading", "x-mask-loading");
                listLoader.load(event.getSelectedItem());
            }
        });

        ThumbsListView listView = new ThumbsListView(true);
        listView.setStyleAttribute("overflow-x", "hidden");
        listView.setStore(contentStore);
        contentContainer.add(listView);

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        add(contentContainer, contentVBoxData);

        dragSource = new ImageDragSource(listView);
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        dragSource.addDNDListener(linker.getDndListener());
    }

    public class ImageDragSource extends ListViewDragSource {
        public ImageDragSource(ListView listView) {
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

}