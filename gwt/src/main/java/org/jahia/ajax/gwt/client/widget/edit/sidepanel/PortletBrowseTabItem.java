/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.ListViewDragSource;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.content.ThumbsListView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.*;

/**
 * Side panel tab item for browsing portlet repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
class PortletBrowseTabItem extends BrowseTabItem {
    protected transient LayoutContainer contentContainer;
    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient ImageDragSource dragSource;
    protected transient ThumbsListView listView;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);

        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setId("images-view");
        contentContainer.setScrollMode(Style.Scroll.AUTOY);

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                if (gwtJahiaFolder != null) {
                    String path = ((GWTJahiaNode) gwtJahiaFolder).getPath();
                    Log.debug("retrieving children of " + path);
                    JahiaContentManagementService.App.getInstance()
                            .lsLoad(path, JCRClientUtils.PORTLET_NODETYPES, null, null, Arrays.asList(GWTJahiaNode.PERMISSIONS,GWTJahiaNode.ICON, GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.THUMBNAILS, GWTJahiaNode.TAGS), false, -1, -1, false, null, null, false, false, listAsyncCallback);
                } else {
                    contentContainer.unmask();
                }
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
        tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                contentContainer.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
                listLoader.load(event.getSelectedItem());
            }
        });

        listView = new ThumbsListView(true);
        listView.setStyleAttribute("overflow-x", "hidden");
        listView.setStore(contentStore);
        contentContainer.add(listView);

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));
        listView.setContextMenu(createContextMenu(config.getTableContextMenu(), listView.getSelectionModel()));

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);

        tab.setId("JahiaGxtPortletBrowseTab");
        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        if (linker.getConfig().isEnableDragAndDrop()) {
            dragSource = new ImageDragSource(listView);
            dragSource.addDNDListener(linker.getDndListener());
        }
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

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains("jnt:portlet");
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.getNodeTypes().contains("jnt:portlet")
                    || node.getNodeTypes().contains("jnt:portletFolder")) {
                return true;
            }
        }
        return super.needRefresh(data);
    }

}
