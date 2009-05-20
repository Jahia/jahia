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
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.FocusPanel;

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
            HorizontalPanel draggablePane = new HorizontalPanel();
            draggablePane.add(createDragSeparator());
            draggablePane.add(createDragSeparator());
            draggableArea = new FocusPanel();
            draggableArea.add(draggablePane);
            ToolItem item = new AdapterToolItem(draggableArea);
            item.setToolTip(gwtToolbar.getTitle());
            ToolBar toolBar = new ToolBar();
            toolBar.add(item);
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
            } else {
                if (!isSeparator(gwtToolbarItem)) {
                    int layout = gwtToolbarItemsGroup.getLayout();

                    if (layout == ToolbarConstants.ITEMSGROUP_BOX) {
                        itemsGroupPanel.add(toolbarItemWidgetProvider.createWidget(gwtToolbarItemsGroup, gwtToolbarItem));

                    } else {
                        final DataListItem item = toolbarItemWidgetProvider.createDataListItem(list, gwtToolbarItemsGroup, gwtToolbarItem);

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
                        }
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
