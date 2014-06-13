/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        LeftComponent leftTree = null;
        Component leftTreeComponent = null;
        BottomRightComponent bottomTabs = null;
        Component bottomTabsComponent = null;

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
    }

}
