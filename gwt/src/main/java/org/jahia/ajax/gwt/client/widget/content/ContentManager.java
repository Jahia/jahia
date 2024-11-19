/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;
import org.jahia.ajax.gwt.client.widget.tripanel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentManager extends TriPanelBrowserLayout {

    private static ContentManager manager;

    public ContentManager(final List<String> filters, final List<String> mimeTypes, final List<String> selectedPaths, final GWTManagerConfiguration config,
                                  final int southSize) {
        // superclass constructor (define linker)
        super(config);

        JahiaGWTParameters.setSiteNode(config.getSiteNode());

        setWidth("100%");
        setHeight("500px");
        setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, southSize));

        if (mimeTypes != null && mimeTypes.size() > 0) {
            config.getMimeTypes().addAll(mimeTypes);
        }
        if (filters != null && filters.size() > 0) {
            config.getFilters().addAll(filters);
        }

        // construction of the UI components
        LeftComponent leftTree = null;
        Component leftTreeComponent = null;
        BottomRightComponent bottomTabs = null;
        Component bottomTabsComponent = null;

        if(selectedPaths.isEmpty()){
            // Try to retrieve the last opened item for this config
            Storage storage = Storage.getLocalStorageIfSupported();
            String lastpath = storage != null ? storage.getItem("lastSavedPath_" + getLinker().getConfig().getName() + "_" + JahiaGWTParameters.getSiteKey()) : null;
            if(lastpath != null && lastpath.length() > 0){
                selectedPaths.add(lastpath);
            }
        }

        if (!config.isHideLeftPanel()) {
            leftTree = new ContentRepositoryTabs(config, selectedPaths);
            leftTreeComponent = leftTree.getComponent();
        } else {
            leftTree = null;
            leftTreeComponent = null;
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    JahiaContentManagementService.App.getInstance().getRoot(config.getRepositories().get(0).getPaths(), null,null,null,GWTJahiaNode.DEFAULT_FIELDS,
                            selectedPaths,null,false, linker.isDisplayHiddenTypes(), config.getHiddenTypes(), config.getHiddenRegex(), false, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                        public void onSuccess(List<GWTJahiaNode> gwtJahiaNode) {
                            linker.setLeftPanelSelectionWhenHidden(gwtJahiaNode.get(0));
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            linker.refresh(data);
                        }

                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("Unable to load node with path", throwable);
                        }
                    });
                }
            });
        }


        final ContentViews contentViews = new ContentViews(config);
        if (config.getManagerEngineTabs() != null) {
            bottomTabs = new ContentDetails(config,linker);
            bottomTabsComponent = bottomTabs.getComponent();
        }
        final TopBar toolbar = new ContentToolbar(config, linker) {

        };
        BottomBar statusBar = new ContentStatusBar();

        // setup widgets in layout

        initWidgets(leftTreeComponent,
                contentViews.getComponent(),
                bottomTabsComponent,
                toolbar.getComponent(),
                statusBar.getComponent());

        // linker initializations
        linker.registerComponents(leftTree, contentViews, bottomTabs, toolbar, statusBar);

        if (config.getContextMenu() != null) {
            final ActionContextMenu actionContextMenu = new ActionContextMenu(config.getContextMenu(), linker);
            if (leftTree != null) {
                leftTree.getComponent().setContextMenu(actionContextMenu);
            }

            contentViews.getComponent().setContextMenu(actionContextMenu);
        }

        linker.handleNewSelection();
        if (config.isExpandRoot()) {
        } else {
            linker.handleNewSelection();
        }
        addStyleName("x-viewport-"+ config.getName());

        manager = this;
    }

    public static void refreshContent() {
        Map<String, Object> data = new HashMap<>();
        data.put(Linker.REFRESH_ALL, true);

        getInstance().getLinker().refresh(data);
    }

    public static ContentManager getInstance() {
        return manager;
    }

    public static native void exportStaticMethod() /*-{
        var nsAuthoringApi;
        if ($wnd.top.authoringApi) {
            nsAuthoringApi = $wnd.top.authoringApi;
        } else {
            nsAuthoringApi = $wnd.top.authoringApi = {};
        }

        nsAuthoringApi.refreshContent = function () {
            return @org.jahia.ajax.gwt.client.widget.content.ContentManager::refreshContent(*)();
        }
    }-*/;

    public static native void removeStaticMethod() /*-{
       if ($wnd.top.authoringApi) {
           delete $wnd.top.authoringApi.refreshContent;
       }
    }-*/;

    @Override
    protected void onLoad() {
        super.onLoad();
        exportStaticMethod();
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        removeStaticMethod();
    }
}
