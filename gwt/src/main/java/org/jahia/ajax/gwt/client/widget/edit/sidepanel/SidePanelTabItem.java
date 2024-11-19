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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;

/**
 * Represents a single tab item in the side panel.
 * @author toto
 */
@SuppressWarnings("serial")
public class SidePanelTabItem implements Serializable {
    protected GWTSidePanelTab config;

    protected transient TabItem tab;
    protected transient EditLinker editLinker;
    protected transient Map<String, Object> autoRefreshData;
    protected transient boolean needManualRefresh;
    private transient Menu contextMenu = null;

    /**
     * Performs the creation of the tab item and populates its content
     *
     * @param sidePanelTab
     *            the tab configuration
     * @return the created tab item
     */
    public TabItem create(GWTSidePanelTab sidePanelTab) {
        this.config = sidePanelTab;

        tab = new TabItem();
        tab.addStyleName("tab_"+sidePanelTab.getName());
        tab.setHtml("&nbsp;");
        tab.setIcon(ToolbarIconProvider.getInstance().getIcon(config.getIcon()));
        tab.getHeader().setToolTip(config.getTooltip());
        tab.getHeader().addStyleName("x-tab-strip-iconOnly");
        tab.setBorders(false);
        tab.setData("tabItem", this);
        return tab;
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }

    public void markForManualRefresh(Map<String, Object> data) {
        if (data.containsKey(Linker.REFRESH_ALL) || needRefresh(data)) {
            needManualRefresh = true;
        }
    }

    public void markForAutoRefresh(Map<String, Object> data) {
        if (autoRefreshData == null) {
            autoRefreshData = new HashMap<String, Object>();
        }
        autoRefreshData.putAll(data);
    }

    public void refresh(Map<String, Object> data) {
        // hide context menu when the context is refreshed
        if (contextMenu != null) {
            contextMenu.hide();
        }
        if (editLinker != null && data != null && (data.containsKey(Linker.REFRESH_ALL) || needRefresh(data))) {
            doRefresh();
            autoRefreshData = null;
            needManualRefresh = false;
        }
    }

    /**
     * Determines if a refresh is needed.
     * Should be overridden in subclasses.
     */
    public boolean needRefresh(Map<String, Object> data) {
        return false;
    }

    /**
     * Refreshes the content of this tab if applicable. Does nothing by default.
     * Should be overridden in subclasses to implement the refresh.
     */
    public void doRefresh() {}

    public Map<String, Object> getAutoRefreshData() {
        return autoRefreshData;
    }

    public boolean isNeedManualRefresh() {
        return needManualRefresh;
    }

    /**
     * Callback for module selection event.
     *
     * @param selectedModule
     *            the selected module
     */
    public void handleNewModuleSelection(Module selectedModule) {
        // do nothing by default
    }

    /**
     * Callback for the selection of a main module.
     *
     * @param path
     *            the path of the new main module node
     */
    public void handleNewMainSelection(String path) {
        // do nothing by default
    }

    /**
     * Callback for the main module loaded event
     *
     * @param node
     *            the main module node
     */
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        tab.setEnabled(config.getRequiredPermission() == null || PermissionsUtils.isPermitted(config.getRequiredPermission(), JahiaGWTParameters.getSiteNode()));
    }

    /**
     * Creates the context menu using specified Spring toolbar bean name.
     *
     * @param toolbarBean    the Spring bean ID to look for in the
     *                       <code>applicationcontext-toolbar-sidepanel.xml</code> file
     * @param selectionModel the tree selection model
     * @return the context menu using specified Spring toolbar bean name
     */
    protected final Menu createContextMenu(GWTJahiaToolbar toolbarBean, AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
        if (toolbarBean != null) {
            if (selectionModel instanceof TreeGridClickSelectionModel) {
                selectionModel = ((TreeGridClickSelectionModel)selectionModel).getRightClickSelectionModel();
            }
            final SidePanelLinker linker = new SidePanelLinker(selectionModel);
            contextMenu = new ActionContextMenu(toolbarBean, linker);
            return contextMenu;
        }
        return null;
    }

    public void disable() {
        tab.disable();
    }

    public void enable() {
        tab.enable();
    }

    public class SidePanelLinker implements Linker {

        private AbstractStoreSelectionModel<GWTJahiaNode> selectionModel;

        private LinkerSelectionContext ctx;

        public SidePanelLinker(AbstractStoreSelectionModel<GWTJahiaNode> selectionModel) {
            super();
            this.selectionModel = selectionModel;
            ctx = new LinkerSelectionContext();

            selectionModel.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                    syncSelectionContext(LinkerSelectionContext.BOTH);
                }
            });

            select(null);
        }

        public LinkerSelectionContext getSelectionContext() {
            return ctx;
        }

        public void loaded() {
            tab.unmask();
            editLinker.loaded();
        }

        public void loading(String resource) {
            tab.mask(resource, "x-mask-loading");
            editLinker.loading(resource);
        }

        public void refresh(Map<String, Object> data) {
            editLinker.refresh(data);
        }

        public void select(Object o) {
        }

        public void syncSelectionContext(int context) {
            ctx.setMainNode(selectionModel.getSelectedItem());
            ctx.setSelectedNodes(selectionModel.getSelectedItems());
            ctx.refresh(context);
        }
        /**
         * Callback for the event of data update.
         *
         * @param paths
         *            the updated items paths
         *
         */

        public void setSelectPathAfterDataUpdate(List<String> paths) {

        }

        public GWTConfiguration getConfig() {
            return editLinker.getConfig();
        }

        public EditLinker getEditLinker() {
        	return editLinker;
        }

        public boolean isDisplayHiddenProperties() {
            return false;
        }
    }




}
