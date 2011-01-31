/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Side panel tab item for browsing the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
class LastContentBrowseTabItem extends SidePanelTabItem {
    protected String search;
    protected int limit;

    protected transient LayoutContainer contentContainer;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient DisplayGridDragSource displayGridSource;

    public TabItem create(final GWTSidePanelTab config) {
        super.create(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(l);
        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());
        contentStore = new ListStore<GWTJahiaNode>();


        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("icon", "", 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        displayColumns.add(new ColumnConfig("displayName", Messages.get("label.name"), 170));
        ColumnConfig columnConfig = new ColumnConfig("jcr:lastModified", Messages.get("label.lastModif"),
                                                     100);
        columnConfig.setDateTimeFormat(DateTimeFormat.getShortDateTimeFormat());
        displayColumns.add(columnConfig);
        final Grid<GWTJahiaNode> grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));

        contentContainer.add(grid);

        grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));
        grid.getStore().setMonitorChanges(true);
        grid.setAutoExpandColumn("displayName");
        final LayoutContainer previewLayoutContainer = new LayoutContainer();
        previewLayoutContainer.setLayout(new FitLayout());
        previewLayoutContainer.setId("bodywrapper");
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> gwtJahiaNodeSelectionChangedEvent) {
                JahiaContentManagementService.App.getInstance().getRenderedContent(
                        gwtJahiaNodeSelectionChangedEvent.getSelectedItem().getPath(), null, editLinker.getLocale(),
                        "default", "preview", null, true, config.getName(),
                        new BaseAsyncCallback<GWTRenderResult>() {

                            public void onSuccess(GWTRenderResult gwtRenderResult) {
                                previewLayoutContainer.removeAll();
                                previewLayoutContainer.add(new HTML(gwtRenderResult.getResult()));
                                previewLayoutContainer.layout(true);
                            }
                        });
            }
        });

        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);
        contentVBoxData.setFlex(1);
        tab.add(previewLayoutContainer, contentVBoxData);

        displayGridSource = new DisplayGridDragSource(grid);
        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        displayGridSource.addDNDListener(editLinker.getDndListener());
    }

//    @Override
//    protected void onRender(Element parent, int index) {
//        super.onRender(parent, index);
//        fillStore();
//    }

    private void fillStore() {
        contentContainer.mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
        contentStore.removeAll();
        JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        async.searchSQL(search, limit,
                        JCRClientUtils.CONTENT_NODETYPES, null, null, Arrays.asList(GWTJahiaNode.ICON,"jcr:lastModified"),false, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                        contentStore.add(gwtJahiaNodes);
                        contentContainer.layout(true);
                        contentContainer.unmask();
                    }
                });
    }

    @Override
    public void refresh(int flag) {
        if ((flag & Linker.REFRESH_PAGES) != 0) {
            fillStore();
        }

    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}