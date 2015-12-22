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
            return new ActionContextMenu(toolbarBean, linker);
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
