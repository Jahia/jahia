package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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


public class ContentManagerEmbedded extends TriPanelBrowserLayout {

    public ContentManagerEmbedded(final String rootPath,String types, String filters, String mimeTypes, String conf) {
        // superclass constructor (define linker)
        super();
        setWidth("100%");

        setHeight("700px");
        setCenterData(new BorderLayoutData(Style.LayoutRegion.SOUTH, 500));
        ManagerConfiguration config ;
        if (conf != null && conf.length() > 0) {
            config = ManagerConfigurationFactory.getConfiguration(conf, linker) ;
        } else {
            config = ManagerConfigurationFactory.getFileManagerConfiguration(linker) ;
        }

        if (types != null && types.length() > 0) {
            config.setNodeTypes(types);
        }
        if (mimeTypes != null && mimeTypes.length() > 0) {
            config.setMimeTypes(mimeTypes);
        }
        if (filters != null && filters.length() > 0) {
            config.setFilters(filters);
        }

        // construction of the UI components
        LeftComponent tree = null;
        Component leftTree = null;

        if(!config.isHideLeftPanel()){
            tree = new ContentRepositoryTabs(config);
            leftTree = tree.getComponent();
        }
      

        final ContentViews contentViews = new ContentViews(config);
        BottomRightComponent tabs = new ContentDetails(config);
        TopBar toolbar = new ContentToolbar(config, linker) {
            protected void setListView() {
                contentViews.switchToListView();
            }

            protected void setThumbView() {
                contentViews.switchToThumbView();
            }

            protected void setDetailedThumbView() {
                contentViews.switchToDetailedThumbView();
            }

            protected void setTemplateView() {
                contentViews.switchToTemplateView();
            }
        };
        BottomBar statusBar = new ContentStatusBar();

        // setup widgets in layout

        initWidgets(leftTree,
                contentViews.getComponent(),
                tabs.getComponent(),
                toolbar.getComponent(),
                statusBar.getComponent());

        // linker initializations
        linker.registerComponents(tree, contentViews, tabs, toolbar, statusBar);
        contentViews.initContextMenu();
        linker.handleNewSelection();
        if (config.isExpandRoot()) {
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    JahiaContentManagementService.App.getInstance().getNode(rootPath, new AsyncCallback<GWTJahiaNode>() {
                        public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                            linker.setLeftPanelSelectionWhenHidden(gwtJahiaNode);
                            linker.refresh();
                        }

                        public void onFailure(Throwable throwable) {
                            Log.error("Unable to load node with path " + rootPath, throwable);
                        }
                    });
                }
            });
        } else {
            linker.handleNewSelection();
        }
    }
}
