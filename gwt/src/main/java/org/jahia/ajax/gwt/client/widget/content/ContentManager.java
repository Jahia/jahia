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

package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionContextMenu;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import com.extjs.gxt.ui.client.widget.Component;

import java.util.List;

public class ContentManager extends TriPanelBrowserLayout {

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
        LeftComponent tree = null;
        Component leftTree = null;

        if (!config.isHideLeftPanel()) {
            tree = new ContentRepositoryTabs(config, selectedPaths);
            leftTree = tree.getComponent();
        } else {
            tree = null;
            leftTree = null;
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    JahiaContentManagementService.App.getInstance().getRoot(config.getRepositories().get(0).getPaths(), null,null,null,null,
                            selectedPaths,null,false, linker.isDisplayHiddenTypes(), config.getHiddenTypes(), config.getHiddenRegex(), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                        public void onSuccess(List<GWTJahiaNode> gwtJahiaNode) {
                            linker.setLeftPanelSelectionWhenHidden(gwtJahiaNode.get(0));
                            linker.refresh(Linker.REFRESH_ALL);
                        }

                        public void onApplicationFailure(Throwable throwable) {
                            Log.error("Unable to load node with path", throwable);
                        }
                    });
                }
            });
        }


        final ContentViews contentViews = new ContentViews(config);
        final BottomRightComponent tabs = new ContentDetails(config,linker);
        final TopBar toolbar = new ContentToolbar(config, linker) {

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

        if (config.getContextMenu() != null) {
            final ActionContextMenu actionContextMenu = new ActionContextMenu(config.getContextMenu(), linker);
            if (tree != null) {
                tree.getComponent().setContextMenu(actionContextMenu);
            }

            contentViews.getComponent().setContextMenu(actionContextMenu);
        }

        linker.handleNewSelection();
        if (config.isExpandRoot()) {
        } else {
            linker.handleNewSelection();
        }
        addStyleName("x-viewport-"+ config.getName());
    }

}
