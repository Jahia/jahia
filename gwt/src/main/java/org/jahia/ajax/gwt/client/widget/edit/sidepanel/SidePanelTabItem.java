/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import java.io.Serializable;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
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
 * User: toto
 * Date: Dec 21, 2009
 * Time: 2:21:40 PM
 */
public class SidePanelTabItem implements Serializable {
    protected GWTSidePanelTab config;

    protected transient TabItem tab;
    protected transient EditLinker editLinker;

    public SidePanelTabItem() {
    }

    public TabItem create(GWTSidePanelTab sidePanelTab) {
        this.config = sidePanelTab;

        tab = new TabItem("&nbsp;");
        tab.setIcon(ToolbarIconProvider.getInstance().getIcon(config.getIcon()));
        tab.getHeader().setToolTip(config.getTooltip());
        tab.getHeader().addStyleName("x-tab-strip-iconOnly");
        tab.setBorders(false);
        return tab;
    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
    }

    /**
     * Refreshes the content of this tab if applicable. Does nothing by default.
     * Should be overridden in subclasses to implement the refresh.
     * @param flag
     */
    public void refresh(int flag) {
        // do nothing by default
    }

    public void handleNewModuleSelection(Module selectedModule) {
        // do nothing by default
    }

    public void handleNewMainSelection(String path) {
        // do nothing by default
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        tab.setEnabled(config.getRequiredPermission() == null || PermissionsUtils.isPermitted(config.getRequiredPermission(), node));
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
            // do nothing
        }

        public void loading(String resource) {
            // do nothing
        }

        public void refresh(int flag) {
            editLinker.refresh(flag);
        }

        public void select(Object o) {
        }

        public void syncSelectionContext(int context) {
            ctx.setMainNode(selectionModel.getSelectedItem());
            ctx.setSelectedNodes(selectionModel.getSelectedItems());
            ctx.refresh(context);
        }

        public void setSelectPathAfterDataUpdate(List<String> paths) {
            // do nothing
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
