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

package org.jahia.ajax.gwt.client.widget.toolbar;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
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
    private List<GWTJahiaToolbar> toolbarSet;

    public ActionToolbarLayoutContainer(GWTJahiaToolbar toolbar) {
        super();
        this.toolbarSet = new ArrayList<GWTJahiaToolbar>();
        this.toolbarSet.add(toolbar);
        setLayout(new RowLayout());
    }

    public ActionToolbarLayoutContainer(List<GWTJahiaToolbar> toolbarSet) {
        super();
        setLayout(new RowLayout());
        this.toolbarSet = toolbarSet;
    }

    /**
     * Create Toolbar UI
     *
     */
    private void createToolbarUI(List<GWTJahiaToolbar> toolbarList) {
        if (toolbarList != null && !toolbarList.isEmpty()) {
            Log.debug(toolbarList.size() + " toolbar(s).");
            for (int i = 0; i < toolbarList.size(); i++) {
                GWTJahiaToolbar gwtToolbar = toolbarList.get(i);
                List<GWTJahiaToolbarItem> toolbarItemsGroups = gwtToolbar.getGwtToolbarItems();
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

    /**
     * Handle module selection
     */
    public void handleNewLinkerSelection() {
        for (ActionToolbar actionToolbar : actionToolbars) {
            actionToolbar.handleNewLinkerSelection();
        }
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (ActionToolbar actionToolbar : actionToolbars) {
            actionToolbar.handleNewMainNodeLoaded(node);
        }
    }

    public void initWithLinker(Linker linker) {
        this.linker = linker;
        if (toolbarSet != null) {
            createToolbarUI(toolbarSet);
        }
    }
}
