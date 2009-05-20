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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.SplitToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToggleToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 13:32:32
 */
public abstract class AbstractJahiaToolItemProvider extends JahiaToolItemProvider {
    public Widget createWidget(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        return createToolItem(gwtToolbarItemsGroup, gwtToolbarItem);
    }

    public ToolItem createToolItem(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        ToolItem toolbarItem = createNewToolItem(gwtToolbarItem);
        int layout = gwtToolbarItemsGroup.getLayout();

        // set properties that are specific to a ToggleToolItem
        if (toolbarItem instanceof ToggleToolItem) {
            Log.debug("Toogle item");
            ((ToggleToolItem) toolbarItem).toggle(gwtToolbarItem.isSelected());
            //((ToggleToolItem) toolbarItem).pressed = gwtToolbarItem.isSelected();
            // hack: toogle doesn't work
            if (gwtToolbarItem.isSelected()) {
                toolbarItem.addStyleName("x-btn-pressed");
            }
        } else if (toolbarItem instanceof SplitToolItem) {
            // hack: toogle split button
            if (gwtToolbarItem.isSelected()) {
                toolbarItem.addStyleName("x-btn-pressed");
            }
        }

        // set properties that are specific to a TextToolItem
        if (toolbarItem instanceof TextToolItem) {
            if (layout == ToolbarConstants.ITEMSGROUP_BUTTON_LABEL || layout == ToolbarConstants.ITEMSGROUP_LABEL) {
                if (gwtToolbarItem.isDisplayTitle()) {
                    ((TextToolItem) toolbarItem).setText(gwtToolbarItem.getTitle());
                }
            }
            if (layout == ToolbarConstants.ITEMSGROUP_BUTTON_LABEL) {
                ((TextToolItem) toolbarItem).setIconStyle(gwtToolbarItem.getMinIconStyle());
            }
            if (layout == ToolbarConstants.ITEMSGROUP_BUTTON) {
                ((TextToolItem) toolbarItem).setIconStyle(gwtToolbarItem.getMediumIconStyle());
                toolbarItem.setHeight("30px");
            }

            // add listener
            SelectionListener<ComponentEvent> listener = getSelectListener(gwtToolbarItem);
            if (listener != null) {
                ((TextToolItem) toolbarItem).addSelectionListener(listener);
            }
        }

        // description
        String description = gwtToolbarItem.getDescription();
        if (gwtToolbarItem.getDescription() != null && description.length() > 0 && toolbarItem.getToolTip() != null) {
            toolbarItem.setToolTip(description);
        }

        return toolbarItem;
    }

    /**
     * Create a menuItem
     *
     * @param gwtToolbarItem
     * @return
     */
    public MenuItem createMenuItem(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        final MenuItem menuItem;
        int layout = gwtToolbarItemsGroup.getLayout();
        if (layout == ToolbarConstants.ITEMSGROUP_MENU) {
            menuItem = new MenuItem();
            menuItem.setIconStyle(gwtToolbarItem.getMinIconStyle());
        } else if (layout == ToolbarConstants.ITEMSGROUP_MENU_RADIO) {
            menuItem = new CheckMenuItem();
            ((CheckMenuItem) menuItem).setGroup(gwtToolbarItemsGroup.getId());
            ((CheckMenuItem) menuItem).setChecked(gwtToolbarItem.isSelected());
        } else if (layout == ToolbarConstants.ITEMSGROUP_MENU_CHECKBOX) {
            menuItem = new CheckMenuItem();
            ((CheckMenuItem) menuItem).setChecked(gwtToolbarItem.isSelected());
        } else {
            menuItem = new MenuItem();
            menuItem.setIconStyle(gwtToolbarItem.getMinIconStyle());
        }

        // selection
        menuItem.setText(gwtToolbarItem.getTitle());
        SelectionListener listener = getSelectListener(gwtToolbarItem);
        menuItem.addSelectionListener(listener);
        return menuItem;
    }

    /**
     * Create a ContentPanel
     *
     * @param gwtToolbarItem
     * @return
     */
    public DataListItem createDataListItem(final DataList list, final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        // create a dataListitem
        DataListItem item = new DataListItem();
        item.setText(gwtToolbarItem.getTitle());
        int layout = gwtToolbarItemsGroup.getLayout();
        if (layout != ToolbarConstants.ITEMSGROUP_MENU_RADIO && layout != ToolbarConstants.ITEMSGROUP_MENU_CHECKBOX) {
            item.setIconStyle(gwtToolbarItem.getMinIconStyle());
        }

        // description
        String description = gwtToolbarItem.getDescription();
        if (gwtToolbarItem.getDescription() != null && description.length() > 0) {
            item.setToolTip(description);
        }

        // deal with check
        item.setChecked(gwtToolbarItem.isSelected());

        // add listener
        SelectionListener listener = getSelectListener(gwtToolbarItem);
        if (listener != null) {
            list.addListener(Events.SelectionChange,new Listener<DataListEvent>(){
                public void handleEvent(DataListEvent event) {
                    if(list.getSelectedItem().getText().equalsIgnoreCase(gwtToolbarItem.getTitle())){
                       getSelectListener(gwtToolbarItem).componentSelected(event); 
                    }
                }
            });
        }
        return item;
    }

    /**
     * Create a tabItem
     *
     * @param gwtToolbarItemsGroup
     * @param gwtToolbarItem
     * @return
     */
    public TabItem createTabItem(final TabPanel tabPanel, final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        final TabItem item = new TabItem();

        // title
        if (gwtToolbarItem.isDisplayTitle()) {
            item.setText(gwtToolbarItem.getTitle());
        }

        // icon style
        item.setIconStyle(gwtToolbarItem.getMinIconStyle());

        // description   : no tooltip for tab panel
        /*String description = gwtToolbarItem.getDescription();
        if (gwtToolbarItem.getDescription() != null && description.length() > 0) {
            item.setToolTip(description);
        } */

        // handle selection
        if (gwtToolbarItem.isSelected()) {
            tabPanel.setSelection(item);
        }

        // add listener
        SelectionListener<ComponentEvent> listener = getSelectListener(gwtToolbarItem);
        if (listener != null) {
            tabPanel.addListener(Events.BeforeSelect, new Listener<TabPanelEvent>() {
                public void handleEvent(TabPanelEvent event) {
                    TabItem selectedItem = event.item;
                    if (selectedItem == item && !gwtToolbarItem.isSelected()) {
                        SelectionListener<ComponentEvent> listener = getSelectListener(gwtToolbarItem);
                        listener.componentSelected(event);
                        event.doit = false;
                    }
                }

            });
        }
        return item;
    }


    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public abstract SelectionListener<ComponentEvent> getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem);

    /**
     * Create a new toolItem
     *
     * @param gwtToolbarItem
     * @return
     */

    public abstract ToolItem createNewToolItem(final GWTJahiaToolbarItem gwtToolbarItem);

    /**
     * Gwt GWT JahiaPage
     *
     * @return
     */
    protected GWTJahiaPageContext getJahiaGWTPageContext() {
        // init panel
        GWTJahiaPageContext page = new GWTJahiaPageContext(URL.getRelativeURL());
        page.setPid(JahiaGWTParameters.getPID());
        page.setMode(JahiaGWTParameters.getOperationMode());
        return page;
    }

}
