/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
                item = new MenuItem(action.getLabel(), pickersMenu) ;
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
