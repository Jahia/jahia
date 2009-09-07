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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;

import java.util.List;

/**
 * User: jahia
 * Date: 15 juil. 2008
 * Time: 15:47:21
 */
public class VerticalJahiaToolbar extends JahiaToolbar {
    public VerticalJahiaToolbar(GWTJahiaToolbar gwtToolbar) {
        super(gwtToolbar);
    }

    public VerticalJahiaToolbar(ToolbarManager toolbarManager, GWTJahiaToolbar gwtToolbar) {
        super(toolbarManager, gwtToolbar);
    }

    public void setAutoWidth(boolean autoWidth) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setContextMenu(Menu menu) {

    }


    /**
     * Create north toolbar UI
     */
    public void createToolBarUI() {
        if (!loaded) {
            super.createToolBarUI();

            String width = "200px";
            String heigth = "300px";
            final ContentPanel toolbarAccordion = new ContentPanel();
            toolbarAccordion.setAutoHeight(true);
            toolbarAccordion.setHeading(gwtToolbar.getTitle());
            toolbarAccordion.setLayout(new AccordionLayout());
            toolbarAccordion.setSize(width, heigth);
            toolbarAccordion.setHeaderVisible(false);

            // add drag area
            draggableArea = new HorizontalPanel();
            draggableArea.add(createDragSeparator());
            draggableArea.add(createDragSeparator());
            ToolBar toolBar = new ToolBar();
            toolBar.add(draggableArea);
            toolbarAccordion.setTopComponent(toolBar);

            // add items
            List toolbarList = gwtToolbar.getGwtToolbarItemsGroups();
            for (int i = 0; i < toolbarList.size(); i++) {
                GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = (GWTJahiaToolbarItemsGroup) toolbarList.get(i);
                ContentPanel itemsGroupPanel = createContentPanel(gwtToolbarItemsGroup, toolbarAccordion);
                if (itemsGroupPanel != null) {
                    toolbarAccordion.add(itemsGroupPanel);
                }
            }

            // add the conent panel
            toolbarAccordion.setAutoHeight(true);
            add(toolbarAccordion);
        }
    }

    /**
     * Create contentPanel that represent the current ItemsGroup
     *
     * @param gwtToolbarItemsGroup
     * @param toolbarAccordion
     * @return
     */
    private ContentPanel createContentPanel(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final ContentPanel toolbarAccordion) {
        final ContentPanel itemsGroupPanel = new ContentPanel();
        itemsGroupPanel.setAutoHeight(true);
        itemsGroupPanel.setHeading(gwtToolbarItemsGroup.getItemsGroupTitle());
        itemsGroupPanel.setIconStyle(gwtToolbarItemsGroup.getMinIconStyle());


        // get items list
        final ProviderHelper jahiaProviderHelper = getProviderHelper();
        final List toolbarItemsGroupList = gwtToolbarItemsGroup.getGwtToolbarItems();
        final DataList list = new DataList();
        list.setBorders(false);
        list.setWidth(190);
        boolean addedToTollbar = false;
        // add toolItem
        for (int i = 0; i < toolbarItemsGroupList.size(); i++) {
            // add items
            GWTJahiaToolbarItem gwtToolbarItem = (GWTJahiaToolbarItem) toolbarItemsGroupList.get(i);
            JahiaToolItemProvider toolbarItemWidgetProvider = jahiaProviderHelper.getJahiaToolItemProvider(gwtToolbarItem.getType());
            if (toolbarItemWidgetProvider == null && !isSeparator(gwtToolbarItem)) {
                printProviderNotFoundError(gwtToolbarItem);
            } else if (toolbarItemWidgetProvider != null) {
                if (!isSeparator(gwtToolbarItem)) {
                    int layout = gwtToolbarItemsGroup.getLayout();

                    if (layout == ToolbarConstants.ITEMSGROUP_BOX) {
                        itemsGroupPanel.add(toolbarItemWidgetProvider.createWidget(gwtToolbarItemsGroup, gwtToolbarItem));

                    } else {
                     /*   final DataListItem item = toolbarItemWidgetProvider.createDataListItem(list, gwtToolbarItemsGroup, gwtToolbarItem);

                        //make list checkable
                        if (layout == ToolbarConstants.ITEMSGROUP_MENU_RADIO) {
                            list.setSelectionMode(Style.SelectionMode.SINGLE);
                        } else if (layout == ToolbarConstants.ITEMSGROUP_MENU_CHECKBOX) {
                            list.setCheckable(true);
                            list.setSelectionMode(Style.SelectionMode.MULTI);
                        }

                        // add item
                        if (item != null) {
                            list.add(item);
                        }  */
                    }
                }
            }
        }

        // if added to toolbar return null
        if (addedToTollbar) {
            return null;
        }

        //create a content itemsGroupPanel
        itemsGroupPanel.add(list);
        itemsGroupPanel.setAutoHeight(true);
        return itemsGroupPanel;
    }

}
