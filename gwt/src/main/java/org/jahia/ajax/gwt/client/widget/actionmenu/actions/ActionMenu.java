/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.actionmenu.actions;

import java.util.List;

import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaAction;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaDisplayPickersAction;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaRedirectAction;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 14:20:11
 */
public class ActionMenu {

    private PopupPanel menu ;

    /**
     * GWTJahiaAction menu construction from a list of GWTJahiaAction objects, opening either a engine or a url in the current page
     * (might be an existing javascript function call)
     *
     * @param actions a list of actions
     *
     */
    public ActionMenu(final List<GWTJahiaAction> actions) {
        menu = new PopupPanel(true) ;
        menu.setStylePrimaryName("action-menu");
        menu.setAnimationEnabled(true);
        MenuBar actionMenu = new MenuBar(true) ;
        actionMenu.setAutoOpen(true);
        for (final GWTJahiaAction action : actions) {
            MenuItem item ;
            if (action instanceof GWTJahiaDisplayPickersAction) {
                MenuBar pickersMenu = new MenuBar(true) ;
                for (final GWTJahiaRedirectAction picker: ((GWTJahiaDisplayPickersAction) action).getPickers()) {
                    MenuItem it = new MenuItem(picker.getLabel(), new Command() {
                        public void execute() {
                            picker.execute() ;
                            menu.hide() ;
                        }
                    }) ;
                    it.setStylePrimaryName("action-menu-item");
                    it.addStyleName("action-menu-" + action.getItem());
                    pickersMenu.addItem(it) ;
                }
                pickersMenu.setStylePrimaryName("action-menu");
                item = new MenuItem("Content pickers", pickersMenu) ;
            } else {
                item = new MenuItem(action.getLabel(), new Command() {
                    public void execute() {
                        action.execute();
                        menu.hide() ;
                    }
                });
            }

            item.setStylePrimaryName("action-menu-item");
            if (action.isLocked()) {
                item.addStyleName("action-menu-locked");
            } else {
                item.addStyleName("action-menu-" + action.getItem());
            }

            actionMenu.addItem(item);
        }
        menu.add(actionMenu);
    }

    /**
     * Mock action menu, built from a String representing an action label
     *
     * @param action a fake action
     */
    public ActionMenu(final String action) {
        menu = new PopupPanel(true) ;
        /*Menu actionMenu = new Menu() ;
        MenuItem item = new MenuItem() ;
        item.setIconStyle("action-menu-" + action);
        item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    Window.alert("Fake action !");
                }
            });
        actionMenu.add(item) ;*/
        menu.add(new Label(action));
    }

    public void show(final int left, final int top) {
        menu.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            public void setPosition(int i, int i1) {
                menu.setPopupPosition(left, top);
            }
        });
    }

    public void hide() {
        menu.hide();
    }

}
