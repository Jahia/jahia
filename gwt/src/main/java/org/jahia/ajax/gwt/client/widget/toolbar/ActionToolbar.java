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
package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarMenu;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SeparatorActionItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 11:43:54 AM
 */
public class ActionToolbar extends ToolBar implements ToolbarGroup {
    private Linker linker;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();
    private GWTJahiaToolbar gwtToolbar;
    private boolean loaded = false;

    public ActionToolbar(GWTJahiaToolbar gwtToolbar, Linker linker) {
        this.gwtToolbar = gwtToolbar;
        this.linker = linker;
    }


    /**
     * Create ui
     */
    public void createToolBar() {
        if (!loaded) {
            loaded = true;
            if (gwtToolbar.isDisplayTitle() && gwtToolbar.getTitle() != null && gwtToolbar.getTitle().length() > 0) {
                LabelToolItem titleItem = new LabelToolItem(gwtToolbar.getTitle() + ":");
                titleItem.addStyleName("gwt-toolbar-title");
                add(titleItem);
            }

            // add items
            List<GWTJahiaToolbarItem> itemsGroupList = gwtToolbar.getGwtToolbarItems();
            for (int i = 0; i < itemsGroupList.size(); i++) {
                GWTJahiaToolbarItem item = itemsGroupList.get(i);
                addItem(item);
            }
        }
    }

    public void addItem(GWTJahiaToolbarItem gwtToolbarItem) {
        if (gwtToolbarItem instanceof GWTJahiaToolbarMenu) {
            GWTJahiaToolbarMenu gwtToolbarMenu = (GWTJahiaToolbarMenu) gwtToolbarItem;
            ActionToolbarMenu menu = new ActionToolbarMenu(linker);
            menu.setActionItems(actionItems);

            for (GWTJahiaToolbarItem subItem : gwtToolbarMenu.getGwtToolbarItems()) {
                menu.addItem(subItem);
            }

            Button menuToolItem = new Button(gwtToolbarMenu.getItemsGroupTitle());
            String minIconStyle = gwtToolbarMenu.getIcon();
            if (minIconStyle != null) {
                menuToolItem.setIcon(ToolbarIconProvider.getInstance().getIcon(gwtToolbarMenu.getIcon()));
            }
            menuToolItem.setMenu(menu);
            add(menuToolItem);
        } else {
            final ActionItem actionItem = gwtToolbarItem.getActionItem();
            actionItems.add(actionItem);
            if (actionItem != null) {
                actionItem.init(gwtToolbarItem, linker);
                if (actionItem.getCustomItem() != null) {
                    add(actionItem.getCustomItem());
                } else if (isSeparator(gwtToolbarItem)) {
                    add(new SeparatorToolItem());
                } else {
                    add(actionItem.getTextToolItem());
                }
            }
        }
    }

    /**
     * return true if it's a Separator
     *
     * @param gwtToolbarItem
     * @return
     */
    public static boolean isSeparator(GWTJahiaToolbarItem gwtToolbarItem) {
        return gwtToolbarItem.getActionItem() != null && gwtToolbarItem.getActionItem() instanceof SeparatorActionItem;
    }


    /**
     * Handle linker selection
     */
    public void handleNewLinkerSelection() {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewLinkerSelection();
            } catch (Exception e) {
            }
        }
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewMainNodeLoaded(node);
            } catch (Exception e) {
            }
        }
    }


}
