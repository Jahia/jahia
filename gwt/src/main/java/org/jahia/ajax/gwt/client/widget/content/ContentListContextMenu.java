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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 8 juil. 2008 - 11:42:43
 */

/**
 * ToDo: [ManagerConfiguration-Spring] get the set of the toolbar from the ManagerConfiguration
 */
public class ContentListContextMenu extends Menu {
    final ManagerLinker linker;
    final ManagerConfiguration config;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();

    public ContentListContextMenu(final ManagerLinker linker, final ManagerConfiguration config) {
        super();
        this.linker = linker;
        this.config = config;
        ToolbarService.App.getInstance().getGWTToolbars(config.getToolbarGroup(), JahiaGWTParameters.getGWTJahiaPageContext(), new AsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                if (gwtJahiaToolbarSet != null) {
                    createMenu(gwtJahiaToolbarSet);
                }
                layout();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to get toobar due to", throwable);
            }
        });


        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                checkLinkerSelection();
            }
        });
    }

    /**
     * Create Menue
     *
     * @param gwtJahiaToolbarSet
     */
    private void createMenu(final GWTJahiaToolbarSet gwtJahiaToolbarSet) {
        // add all items found in the defined menus
        for (GWTJahiaToolbar gwtJahiaToolbar : gwtJahiaToolbarSet.getToolbarList()) {
            if (gwtJahiaToolbar.isContextMenu()) {
                for (int i = 0; i < gwtJahiaToolbar.getGwtToolbarItemsGroups().size(); i++) {
                    GWTJahiaToolbarItemsGroup itemsGroup = gwtJahiaToolbar.getGwtToolbarItemsGroups().get(i);
                    if (i > 0 && !itemsGroup.getGwtToolbarItems().isEmpty()) {
                        add(new SeparatorMenuItem());
                    }
                    for (GWTJahiaToolbarItem gwtJahiaToolbarItem : itemsGroup.getGwtToolbarItems()) {
                        ActionItem actionItem = gwtJahiaToolbarItem.getActionItem();
                        if (actionItem != null) {
                            actionItem.init(gwtJahiaToolbarItem, linker);
                            add(actionItem.getContextMenuItem());
                            actionItems.add(actionItem);
                        }
                    }
                }
            }
        }
    }


    /**
     * check linker seletion
     */
    private void checkLinkerSelection() {
        for (ActionItem item : actionItems) {
            item.handleNewLinkerSelection();
        }
    }

}
