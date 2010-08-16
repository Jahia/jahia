/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Action toolbar container widget.
 * User: ktlili
 * Date: Sep 4, 2009
 * Time: 4:17:57 PM
 */
public class ActionToolbarLayoutContainer extends LayoutContainer {
    private List<ActionToolbar> actionToolbars = new ArrayList<ActionToolbar>();
    private Linker linker;
    private String toolbarGroup;
    private GWTJahiaToolbar toolbar;
    private GWTJahiaToolbarSet toolbarSet;

    public ActionToolbarLayoutContainer(String toolbarGroup) {
        super();
        this.toolbarGroup = toolbarGroup;
        setLayout(new RowLayout());
    }

    public ActionToolbarLayoutContainer(GWTJahiaToolbar toolbar) {
        super();
        this.toolbar = toolbar;
        setLayout(new RowLayout());
    }

    public ActionToolbarLayoutContainer(GWTJahiaToolbarSet toolbarSet) {
        super();
        setLayout(new RowLayout());
        this.toolbarSet = toolbarSet;
    }

    /**
     * Load toolbar
     */
    private void loadToolbars() {
        // load toolbars
        ToolbarService.App.getInstance().getGWTToolbars(toolbarGroup, new BaseAsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                long begin = System.currentTimeMillis();
                if (gwtJahiaToolbarSet != null) {
                    createToolbarUI(gwtJahiaToolbarSet);
                }
                afterToolbarLoading();
                long end = System.currentTimeMillis();
                layout();
                Log.info("Toolbar loaded in " + (end - begin) + "ms");
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Unable to get toobar due to", throwable);
            }
        });
    }

    /**
     * Create Toolbar UI
     *
     * @param gwtJahiaToolbarSet
     */
    private void createToolbarUI(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
        final List<GWTJahiaToolbar> toolbarList = gwtJahiaToolbarSet.getToolbarList();
        if (toolbarList != null && !toolbarList.isEmpty()) {
            Log.debug(toolbarList.size() + " toolbar(s).");
            for (int i = 0; i < toolbarList.size(); i++) {
                GWTJahiaToolbar gwtToolbar = toolbarList.get(i);
                List<GWTJahiaToolbarItemsGroup> toolbarItemsGroups = gwtToolbar.getGwtToolbarItemsGroups();
                if (toolbarItemsGroups != null && !toolbarItemsGroups.isEmpty()) {
                    addActionToolbar(gwtToolbar, i==0);
                }
            }

            Log.debug("-- all tool bars added.");
        } else {
            Log.debug("There is no toolbar");
        }

        // no node is selected
        handleNewLinkerSelection();
    }

    /**
     * Add a toolbar widget
     *
     * @param gwtToolbar
     */
    public void addActionToolbar(GWTJahiaToolbar gwtToolbar, boolean first) {
        ActionToolbar actionToolbar = new ActionToolbar(gwtToolbar, linker);
        actionToolbar.createToolBar();
        if (first) {
            actionToolbar.addStyleName("x-toolbar-first");
        }
        // add to widget
        add(actionToolbar);

        // add to toolbars list
        actionToolbars.add(actionToolbar);

    }

    public void insertItem(Component item, int index) {
        if (actionToolbars != null && !actionToolbars.isEmpty()) {
            actionToolbars.get(0).insert(item, index);
        }
    }

    /**
     * Handle module selection
     */
    public void handleNewLinkerSelection() {
        for (ActionToolbar actionToolbar : actionToolbars) {
            actionToolbar.handleNewLinkerSelection();
        }
    }

    /**
     * Executed after the load of the toolbar
     */
    public void afterToolbarLoading() {

    }

    public void initWithLinker(Linker linker) {
        this.linker = linker;
        if (toolbar != null) {
            addActionToolbar(toolbar, true);
        } else if (toolbarSet != null) {
            createToolbarUI(toolbarSet);
        } else if (toolbarGroup != null) {
            loadToolbars();
        }
    }
}
