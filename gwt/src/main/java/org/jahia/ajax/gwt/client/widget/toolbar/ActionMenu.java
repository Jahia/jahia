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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Action menu component.
 * User: ktlili
 * Date: Mar 15, 2010
 * Time: 5:05:34 PM
 */
public class ActionMenu extends Menu {
    private Linker linker;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();
    private GWTJahiaToolbarSet toolbarSet;
    private GWTJahiaToolbar toolbar;


    public ActionMenu(final GWTJahiaToolbar toolbar, final Linker linker) {
        super();
        this.linker = linker;
        this.toolbar = toolbar;
       // createMenu(toolbar);

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    public ActionMenu(final GWTJahiaToolbarSet toolbarSet, final Linker linker) {
        super();
        this.linker = linker;
        this.toolbarSet = toolbarSet;
       // createMenu(toolbarSet);

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        if(toolbar != null){
           createMenu(toolbar);
        } else if(toolbarSet != null){
           createMenu(toolbarSet);
        }
        
    }

    public ActionMenu(final String toolbar, final Linker linker) {
        super();
        this.linker = linker;
        ToolbarService.App.getInstance().getGWTToolbars(toolbar, new BaseAsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                if (gwtJahiaToolbarSet != null && !gwtJahiaToolbarSet.getToolbarList().isEmpty()) {
                    createMenu(gwtJahiaToolbarSet);
                }
                layout();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Unable to get toolbar bean '" + toolbar + "'", throwable);
            }
        });

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    /**
     * Override this method to provide a custom "beforeShow" behaviour
     */
    public void beforeShow() {
        linker.syncSelectionContext();
        checkLinkerSelection();
    }

    /**
     * Create menu
     *
     * @param gwtJahiaToolbarSet
     */
    private void createMenu(final GWTJahiaToolbarSet gwtJahiaToolbarSet) {
        // add all items found in the defined menus
        for (GWTJahiaToolbar gwtJahiaToolbar : gwtJahiaToolbarSet.getToolbarList()) {
            if (gwtJahiaToolbar.isContextMenu()) {
                createMenu(gwtJahiaToolbar);
            }
        }
    }

    /**
     * Create Menu
     * @param gwtJahiaToolbar
     */
    private void createMenu(GWTJahiaToolbar gwtJahiaToolbar) {
        List<Item> allItems = new ArrayList<Item>();
        for (int i = 0; i < gwtJahiaToolbar.getGwtToolbarItemsGroups().size(); i++) {
            List<Item> groupItems = new ArrayList<Item>();
            GWTJahiaToolbarItemsGroup itemsGroup = gwtJahiaToolbar.getGwtToolbarItemsGroups().get(i);
            if (!itemsGroup.isContextMenu()) {
                continue;
            }
            for (GWTJahiaToolbarItem gwtJahiaToolbarItem : itemsGroup.getGwtToolbarItems()) {
                if (!gwtJahiaToolbarItem.isContextMenu()) {
                    continue;
                }
                ActionItem actionItem = gwtJahiaToolbarItem.getActionItem();
                if (actionItem != null) {
                    actionItem.init(gwtJahiaToolbarItem, linker);
                    actionItems.add(actionItem);
                    if (Log.isDebugEnabled()) {
                        Log.debug("add action-menu : " + gwtJahiaToolbarItem.getTitle());
                    }
                    groupItems.add(actionItem.getContextMenuItem());
                }
            }
            if (!groupItems.isEmpty()) {
                if (itemsGroup.isAutoInsertSeparator() && allItems.size() > 0 && !(allItems.get(allItems.size() - 1) instanceof SeparatorMenuItem) && !(groupItems.get(0) instanceof SeparatorMenuItem)) {
                    allItems.add(new SeparatorMenuItem());
                }
                allItems.addAll(groupItems);
            }
        }
        if (allItems.size() > 0 && (allItems.get(0) instanceof SeparatorMenuItem)) {
            allItems.remove(0);
        }
        if (allItems.size() > 0 && (allItems.get(allItems.size() - 1) instanceof SeparatorMenuItem)) {
            allItems.remove(allItems.size() - 1);
        }
        for (Item item : allItems) {
            add(item);
        }
    }

    /**
     * Check linker selection
     */
    private void checkLinkerSelection() {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewLinkerSelection();
            } catch (Exception e) {
            }
        }
    }
}

