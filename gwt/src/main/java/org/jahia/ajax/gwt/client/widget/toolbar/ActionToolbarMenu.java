/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarMenu;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

import java.util.List;

/**
* 
* User: toto
* Date: Aug 31, 2010
* Time: 7:35:40 PM
* 
*/
public class ActionToolbarMenu extends Menu implements ToolbarGroup {
    protected Linker linker;
    protected List<ActionItem> actionItems;

    public ActionToolbarMenu(Linker linker) {
        this.linker = linker;
    }

    public void setActionItems(List<ActionItem> actionItems) {
        this.actionItems = actionItems;
    }

    public void addItem(GWTJahiaToolbarItem gwtToolbarItem) {
        if (gwtToolbarItem instanceof GWTJahiaToolbarMenu) {
            final MenuItem subMenu = new MenuItem();
            subMenu.addStyleName("action-bar-menu-item");
            gwtToolbarItem.addClasses(subMenu);
            if (gwtToolbarItem.getIcon() != null) {
                subMenu.setIcon(ToolbarIconProvider.getInstance().getIcon(gwtToolbarItem.getIcon()));
            }

            GWTJahiaToolbarMenu gwtToolbarMenu = (GWTJahiaToolbarMenu) gwtToolbarItem;

            subMenu.setText(gwtToolbarMenu.getItemsGroupTitle());
            ActionToolbarMenu menu = new ActionToolbarMenu(linker);
            menu.addStyleName("menu-"+gwtToolbarMenu.getClassName());
            menu.addStyleName("action-bar-menu");
            menu.setActionItems(actionItems);
            for (GWTJahiaToolbarItem subItem : gwtToolbarMenu.getGwtToolbarItems()) {
                menu.addItem(subItem);
            }
            subMenu.setSubMenu(menu);
            subMenu.addListener(Events.Attach, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    boolean show = false;
                    for (com.extjs.gxt.ui.client.widget.Component item : subMenu.getSubMenu().getItems()) {
                        for (ActionItem baseItem : actionItems) {
                            if (baseItem.getMenuItem().equals(item)) {
                                if (baseItem instanceof  BaseActionItem && ((BaseActionItem) baseItem).isEnabled()) {
                                    show = true;
                                    break;
                                }
                            }
                        }
                        if (item.isVisible()) {
                        }
                    }
                    subMenu.setVisible(show);
                }
            });
            add(subMenu);
        } else {
            final ActionItem actionItem = gwtToolbarItem.getActionItem();
            actionItems.add(actionItem);
            if (actionItem != null) {
                actionItem.init(gwtToolbarItem, linker);
                if (ActionToolbar.isSeparator(gwtToolbarItem)) {
                    add(new SeparatorMenuItem());
                } else if (actionItem.getCustomItem() != null) {
                    add(actionItem.getCustomItem());
                } else {
                    add(createActionItem(actionItem));
                }
            }
        }

    }

    protected Item createActionItem(ActionItem actionItem) {
        return actionItem.getMenuItem();
    }
}
