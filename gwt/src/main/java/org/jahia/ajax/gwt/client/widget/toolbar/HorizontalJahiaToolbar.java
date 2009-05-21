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
package org.jahia.ajax.gwt.client.widget.toolbar;

import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.ProviderHelper;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.JahiaToolItemProvider;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Element;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * User: jahia
 * Date: 15 juil. 2008
 * Time: 15:48:01
 */
public class HorizontalJahiaToolbar extends JahiaToolbar {
    private final ToolBarContextMenu toolBar = new ToolBarContextMenu();
    private final TabPanelContextMenu tabPanel = new TabPanelContextMenu();

    public HorizontalJahiaToolbar(GWTJahiaToolbar gwtToolbar) {
        super(gwtToolbar);
    }

    public HorizontalJahiaToolbar(ToolbarManager toolbarManager, GWTJahiaToolbar gwtToolbar) {
        super(toolbarManager, gwtToolbar);
    }

    public void setAutoWidth(boolean autoWidth) {
        if (autoWidth) {
            toolBar.setWidth("100px");
        } else {
            toolBar.setWidth("100%");
        }

    }


    public void createToolBarUI() {
        if (!loaded) {
            super.createToolBarUI();
            // add drag area
            HorizontalPanel draggablePane = new HorizontalPanel();
            draggablePane.add(createDragSeparator());
            draggablePane.add(createDragSeparator());

            draggableArea = new FocusPanel();

            draggableArea.add(draggablePane);
            ToolItem item = new AdapterToolItem(draggableArea);
            toolBar.add(item);

            // deal with diplay title
            Log.debug("---- is display title: " + gwtToolbar.isDisplayTitle());            
            if(gwtToolbar.isDisplayTitle() && gwtToolbar.getTitle()!= null && gwtToolbar.getTitle().length()>0){
                LabelToolItem titleItem = new LabelToolItem(gwtToolbar.getTitle()+":");
                titleItem.addStyleName("gwt-toolbar-title");
                toolBar.add(titleItem);         
            }

            // add items
            List<GWTJahiaToolbarItemsGroup> itemsGroupList = gwtToolbar.getGwtToolbarItemsGroups();
            Log.debug("---- Nb items groupe: " + itemsGroupList.size());
            for (int i = 0; i < itemsGroupList.size(); i++) {
                GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = itemsGroupList.get(i);
                Log.debug("---- items group type: " + gwtToolbarItemsGroup.getType());
                if (gwtToolbarItemsGroup.getType() != null && gwtToolbarItemsGroup.getType().equalsIgnoreCase(ToolbarConstants.ITEMSGROUP_FILL)) {
                    // special items type: fill type
                    toolBar.add(new FillToolItem());
                } else {
                    fillToolBar(gwtToolbarItemsGroup, toolBar, tabPanel);
                    if (i != itemsGroupList.size() - 1 && itemsGroupList.get(i + 1).isNeedSeparator()) {
                        // add separator
                        if (gwtToolbarItemsGroup.getLayout() != ToolbarConstants.ITEMSGROUP_TABS) {
                            toolBar.add(new SeparatorToolItem());
                        }
                    }
                }

            }

            // if this toolbar has tab that wraps in a tabpanel
            if (hasTabs()) {
                // workaround
                Log.debug("has tab");
                toolBar.setBorders(false);
                tabPanel.setAutoHeight(true);
                tabPanel.setPlain(true);
                tabPanel.getSelectedItem().add(toolBar);
                add(tabPanel);
            } else {
                Log.debug("no tab");
                toolBar.setBorders(true);
                add(toolBar);
            }

        }
    }

    public void fillToolBar(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, ToolBar toolBar, TabPanel tabPanel) {
        final ProviderHelper jahiaProviderHelper = getProviderHelper();
        final List toolbarItemsGroupList = gwtToolbarItemsGroup.getGwtToolbarItems();
        final Menu menu = new Menu();
        boolean addMenu = false;
        // add toolItem
        for (int i = 0; i < toolbarItemsGroupList.size(); i++) {
            // add items
            GWTJahiaToolbarItem gwtToolbarItem = (GWTJahiaToolbarItem) toolbarItemsGroupList.get(i);
            JahiaToolItemProvider toolbarItemWidgetProvider = jahiaProviderHelper.getJahiaToolItemProvider(gwtToolbarItem.getType());
            if (toolbarItemWidgetProvider == null && !isSeparator(gwtToolbarItem)) {
                printProviderNotFoundError(gwtToolbarItem);
            } else {
                Log.debug(gwtToolbarItem.getType()+" - items group layout =" + gwtToolbarItemsGroup.getLayout());
                if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_MENU || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_MENU_RADIO || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_MENU_CHECKBOX) {
                    // handle case of menuSeparator
                    if (isSeparator(gwtToolbarItem)) {
                        // add menu separator only if we have at least one menuitem
                        if (menu.getItemCount() > 0) {
                            if (isSeparator(gwtToolbarItem)) {
                                menu.add(new SeparatorMenuItem());
                            } else {
                                Log.debug("Fill item not allowed in menu");
                            }
                        }
                    }
                    // case of other items
                    else {
                        Item toolbarMenuItem = toolbarItemWidgetProvider.createMenuItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        if (toolbarMenuItem != null) {
                            if(gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)){
                               toolbarMenuItem.setEnabled(false);
                            }
                            menu.add(toolbarMenuItem);
                        }
                        addMenu = true;
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_SELECT) {
                    // handle case of menuSeparator
                    if (isSeparator(gwtToolbarItem)) {
                        // add menu separator only if we have at least one menuitem
                        if (menu.getItemCount() > 0) {
                            menu.add(new SeparatorMenuItem());
                        } else {
                            Log.debug("Fill item not allowed in menu");
                        }
                    }
                    // case of other items
                    else {
                        Item toolbarMenuItem = toolbarItemWidgetProvider.createMenuItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        if (toolbarMenuItem != null) {
                            if(gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)){
                               toolbarMenuItem.setEnabled(false);
                            }
                            menu.add(toolbarMenuItem);
                        }
                        addMenu = true;
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_LABEL || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON_LABEL) {
                    if (isSeparator(gwtToolbarItem)) {
                        toolBar.add(new SeparatorToolItem());
                    } else {
                        ToolItem toolbarItem = toolbarItemWidgetProvider.createToolItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        toolBar.add(toolbarItem);
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_TABS) {
                    if (!isSeparator(gwtToolbarItem)) {
                        Log.debug("itemsgroup is the tabpanel");
                        TabItem tabItem = toolbarItemWidgetProvider.createTabItem(tabPanel, gwtToolbarItemsGroup, gwtToolbarItem);
                        if (tabItem != null) {
                            Log.debug("add tabitem");
                            tabPanel.add(tabItem);
                            setHasTabs(true);

                        }
                    }
                } else {
                    gwtToolbarItemsGroup.setLayout(ToolbarConstants.ITEMSGROUP_BUTTON);
                    if (isSeparator(gwtToolbarItem)) {
                        toolBar.add(new SeparatorToolItem());
                    } else {
                        ToolItem toolbarItem = toolbarItemWidgetProvider.createToolItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        toolBar.add(toolbarItem);
                    }
                }

                // if one of the itemsgroup is type button --> set size of the toolbar to 35px
                if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON) {
                    toolBar.setHeight(35);
                }
            }
        }

        // add menu
        if (addMenu) {
            TextToolItem menuToolItem = new TextToolItem(gwtToolbarItemsGroup.getItemsGroupTitle());
            String minIconStyle = gwtToolbarItemsGroup.getMinIconStyle();
            if (minIconStyle != null) {
                menuToolItem.setIconStyle(minIconStyle);
            }
            menuToolItem.setMenu(menu);
            toolBar.add(menuToolItem);
        }
    }

    public void setContextMenu(Menu menu) {
        if (hasTabs()) {
            tabPanel.setContextMenu(menu);
        } else {
            toolBar.setContextMenu(menu);
        }
    }

    /**
     * a toolbar with a context menu handler
     */
    private class ToolBarContextMenu extends ToolBar {
        @Override
        protected void onRender(Element target, int index) {
            super.onRender(target, index);
            el().addEventsSunk(Event.ONCLICK | Event.ONDBLCLICK | Event.KEYEVENTS | Event.MOUSEEVENTS);
        }

        @Override
        public void setContextMenu(Menu menu) {
            // make public
            super.setContextMenu(menu);
        }

        @Override
        public Menu getContextMenu() {
            return super.getContextMenu();
        }

        @Override
        public void onRightClick(final ComponentEvent ce) {
            super.onRightClick(ce);
        }
    }

    private class TabPanelContextMenu extends TabPanel {
        @Override
        protected void onRender(Element target, int index) {
            super.onRender(target, index);
            el().addEventsSunk(Event.ONCLICK | Event.ONDBLCLICK | Event.KEYEVENTS | Event.MOUSEEVENTS);
        }

        @Override
        public void setContextMenu(Menu menu) {
            // make public
            super.setContextMenu(menu);
        }

        @Override
        public Menu getContextMenu() {
            return super.getContextMenu();
        }

        @Override
        public void onRightClick(final ComponentEvent ce) {
            super.onRightClick(ce);
        }
    }
}
