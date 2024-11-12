/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper.CanUseComponentForEditCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Side panel tab item for browsing the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:22:24 PM
 */
@SuppressWarnings("serial")
class LastContentBrowseTabItem extends SidePanelTabItem {
    protected String search;
    protected int limit = -1;
    private String cssWrapper;

    protected transient LayoutContainer contentContainer;
    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient DisplayGridDragSource displayGridSource;
    private transient LayoutContainer previewLayoutContainer;
    protected transient Grid<GWTJahiaNode> grid;

    @Override
    public TabItem create(final GWTSidePanelTab config) {
        super.create(config);
        tab.addListener(Events.Select, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                fillStore();
                tab.removeListener(Events.Select, this);
            }
        });
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(l);
        contentContainer = new LayoutContainer();
        contentContainer.setBorders(true);
        contentContainer.setScrollMode(Style.Scroll.AUTO);
        contentContainer.setLayout(new FitLayout());
        contentStore = new ListStore<GWTJahiaNode>();
        contentStore.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));

        List<ColumnConfig> displayColumns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("icon", "", 40);
        col.setAlignment(Style.HorizontalAlignment.CENTER);
        col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {

            @Override
            public String render(GWTJahiaNode modelData, String s, ColumnData columnData, int i, int i1,
                                 ListStore<GWTJahiaNode> listStore, Grid<GWTJahiaNode> g) {
                return ContentModelIconProvider.getInstance().getIcon(modelData).getHTML();
            }
        });
        displayColumns.add(col);
        col = new ColumnConfig("displayName", Messages.get("label.name"), 170);
        col.setRenderer(NodeColumnConfigList.NAME_RENDERER);
        displayColumns.add(col);
        col = new ColumnConfig("jcr:lastModified", Messages.get("label.lastModif"),
                100);
        col.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT));
        displayColumns.add(col);
        grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(displayColumns));

        contentContainer.add(grid);

        grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));
        grid.getStore().setMonitorChanges(true);
        grid.setAutoExpandColumn("displayName");
        previewLayoutContainer = new LayoutContainer();
        previewLayoutContainer.setLayout(new FitLayout());
        previewLayoutContainer.addStyleName(cssWrapper);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> evt) {
                if (evt.getSelectedItem() != null) {
                    JahiaContentManagementService.App.getInstance().getRenderedContent(
                            evt.getSelectedItem().getPath(), null, editLinker.getLocale(),
                            "default", "preview", null, true, "editmode",
                            editLinker.getActiveChannelIdentifier(), null, new BaseAsyncCallback<GWTRenderResult>() {

                                @Override
                                public void onSuccess(GWTRenderResult gwtRenderResult) {
                                    previewLayoutContainer.removeAll();
                                    previewLayoutContainer.add(new HTML(gwtRenderResult.getResult()));
                                    previewLayoutContainer.layout(true);
                                }
                            });
                } else {
                    previewLayoutContainer.removeAll();
                    previewLayoutContainer.layout(true);
                }
            }
        });
        grid.addListener(Events.OnDoubleClick, new Listener<GridEvent<GWTJahiaNode>>() {

            @Override
            public void handleEvent(GridEvent<GWTJahiaNode> baseEvent) {
                final GWTJahiaNode gwtJahiaNode = baseEvent.getModel();
                if (gwtJahiaNode != null && editLinker != null) {
                    ModuleHelper.checkCanUseComponentForEdit(gwtJahiaNode.getNodeTypes().get(0), new CanUseComponentForEditCallback() {
                        @Override
                        public void handle(boolean canUseComponentForEdit) {
                            if (canUseComponentForEdit) {
                                EngineLoader.showEditEngine(editLinker, gwtJahiaNode, null);
                            }
                        }
                    });
                }
            }
        });
        VBoxLayoutData contentVBoxData = new VBoxLayoutData();
        contentVBoxData.setFlex(2);
        tab.add(contentContainer, contentVBoxData);
        contentVBoxData.setFlex(1);
        tab.add(previewLayoutContainer, contentVBoxData);

        tab.setId("JahiaGxtLastContentBrowseTab");
        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        if (linker.getConfig().isDragAndDropEnabled()) {
            displayGridSource = new DisplayGridDragSource(grid);
            displayGridSource.addDNDListener(editLinker.getDndListener());
        }
    }

    private void fillStore() {
        contentContainer.mask(Messages.get("label.loading", "Loading..."), "x-mask-loading");
        contentStore.setFiresEvents(false);
        if (previewLayoutContainer != null) {
            previewLayoutContainer.removeAll();
            previewLayoutContainer.layout(true);
        }
        contentStore.removeAll();
        contentStore.setFiresEvents(true);
        JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        String searchString = search;

        if (searchString.contains("$site") && JahiaGWTParameters.getSiteNode() != null) {
            searchString = searchString.replace("$site", JahiaGWTParameters.getSiteNode().getPath());
        }
        if (searchString.contains("$systemsite")) {
            searchString = searchString.replace("$systemsite", "/sites/systemsite");
        }

        async.searchSQL(searchString, limit, 0, JCRClientUtils.CONTENT_NODETYPES, Arrays.asList(GWTJahiaNode.ICON, "jcr:lastModified", GWTJahiaNode.PERMISSIONS), false, new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {

            @Override
            public void onSuccess(PagingLoadResult<GWTJahiaNode> gwtJahiaNodes) {
                contentStore.add(gwtJahiaNodes.getData());
                contentContainer.layout(true);
                contentContainer.unmask();
            }
        });
    }

    @Override
    public void doRefresh() {
        fillStore();
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

    public void setCssWrapper(String cssWrapper) {
        this.cssWrapper = cssWrapper;
    }
}
