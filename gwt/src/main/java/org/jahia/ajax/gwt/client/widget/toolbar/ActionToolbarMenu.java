/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
                    }
                    subMenu.setVisible(show);
                }
            });
            subMenu.setHideOnClick(false);
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
