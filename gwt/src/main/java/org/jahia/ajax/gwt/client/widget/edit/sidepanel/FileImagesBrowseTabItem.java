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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.ThumbsListView;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;

import java.util.*;

/**
 * Side panel tab item for browsing image resources in the repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
class FileImagesBrowseTabItem extends BrowseTabItem {
    protected transient LayoutContainer contentContainer;
    protected transient ListLoader<ListLoadResult<GWTJahiaNode>> listLoader;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient ImageDragSource dragSource;
    protected transient ThumbsListView listView;

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);

        contentContainer = new LayoutContainer();
        contentContainer.setId("images-view");
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTOY);
        contentContainer.setLayout(new FitLayout());

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> listProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                if (gwtJahiaFolder != null) {
                    String path = ((GWTJahiaNode) gwtJahiaFolder).getPath();
                    Log.debug("retrieving children of " + path);
                    JahiaContentManagementService.App.getInstance()
                            .lsLoad(path, JCRClientUtils.FILE_NODETYPES, null, null, Arrays.asList(GWTJahiaNode.PERMISSIONS,GWTJahiaNode.ICON, GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.THUMBNAILS, GWTJahiaNode.TAGS, "j:width", "j:height"), false, -1, -1, false, null, null, false, false, listAsyncCallback);
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

        tree.setContextMenu(createContextMenu(config.getTreeContextMenu(), tree.getSelectionModel()));

        listView = new ThumbsListView(true);
        listView.setStyleAttribute("overflow-x", "hidden");
        listView.setStore(contentStore);
        contentStore.setSortField("display");
        contentContainer.add(listView);

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);

        listView.addListener(Events.DoubleClick, new Listener<ListViewEvent<GWTJahiaNode>>() {
            public void handleEvent(ListViewEvent<GWTJahiaNode> be) {
                Window w = new Window();
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

        tab.setId("JahiaGxtFileImagesBrowseTab");
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

    @Override
    protected boolean acceptNode(GWTJahiaNode node) {
        return node.getInheritedNodeTypes().contains("nt:file");
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        data.put(Linker.REFRESH_MAIN, false);
        if (data.containsKey("node")) {
            GWTJahiaNode node = (GWTJahiaNode) data.get("node");
            if (node.getNodeTypes().contains("jnt:folder")
                    || node.getNodeTypes().contains("jnt:file")) {
                return true;
            }
        }
        return super.needRefresh(data);
    }


}
