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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 13:32:32
 */
public abstract class BaseActionItem implements ActionItem {
    private GWTJahiaToolbarItem gwtToolbarItem;

    private transient Component textToolitem = null;
    private transient MenuItem menuItem = null;
    private transient MenuItem contextMenuItem = null;
    protected transient Linker linker;

    public BaseActionItem() {
    }


    /**
     * Get texxt tooliem
     *
     * @return
     */
    public Component getTextToolitem() {
        if (textToolitem != null) {
            return textToolitem;
        }
        textToolitem = createNewToolItem();
        int layout = getGwtToolbarItem().getLayout();
        if (layout == -1) {
            layout = getGwtToolbarItem().getParentItemsGroup().getLayout();
        }

        // set properties that are specific to a ToggleToolItem
        if (textToolitem instanceof ToggleButton) {
            ((ToggleButton) textToolitem).toggle(getGwtToolbarItem().isSelected());
        }

        // set properties that are specific to a Button
        if (textToolitem instanceof Button) {
            if (layout == ToolbarConstants.LAYOUT_BUTTON_LABEL || layout == ToolbarConstants.LAYOUT_ONLY_LABEL) {
                if (getGwtToolbarItem().isDisplayTitle()) {
                    ((Button) textToolitem).setText(getGwtToolbarItem().getTitle());
                }
            }
            if (layout == ToolbarConstants.LAYOUT_BUTTON_LABEL) {
                ((Button) textToolitem).setIconStyle(getGwtToolbarItem().getMinIconStyle());
            }
            if (layout == ToolbarConstants.LAYOUT_BUTTON) {
                ((Button) textToolitem).setIconStyle(getGwtToolbarItem().getMinIconStyle());
                // toolbarItem.setHeight("30px");
            }

            // add listener
            SelectionListener<ButtonEvent> listener = getSelectListener();
            if (listener != null) {
                ((Button) textToolitem).addSelectionListener(listener);
            }
        }

        // description
        String description = getGwtToolbarItem().getDescription();
        if (getGwtToolbarItem().getDescription() != null && description.length() > 0 && textToolitem.getToolTip() == null) {
            textToolitem.setToolTip(description);
        }

        return textToolitem;
    }


    public MenuItem getMenuItem() {
        if (menuItem != null) {
            return menuItem;
        }
        menuItem = createMenuItem();
        return menuItem;

    }


    public MenuItem getContextMenuItem() {
        if (contextMenuItem != null) {
            return contextMenuItem;
        }
        contextMenuItem = createMenuItem();
        return contextMenuItem;
    }

    /**
     * Create a menuItem
     *
     * @return
     */
    private MenuItem createMenuItem() {
        final MenuItem menuItem;
        int layout = getGwtToolbarItem().getParentItemsGroup().getLayout();
        if (layout == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU) {
            menuItem = new MenuItem();
            menuItem.setIconStyle(getGwtToolbarItem().getMinIconStyle());
        } else if (layout == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU_RADIO) {
            menuItem = new CheckMenuItem();
            ((CheckMenuItem) menuItem).setGroup(getGwtToolbarItem().getParentItemsGroup().getId());
            ((CheckMenuItem) menuItem).setChecked(getGwtToolbarItem().isSelected());
        } else if (layout == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU_CHECKBOX) {
            menuItem = new CheckMenuItem();
            ((CheckMenuItem) menuItem).setChecked(getGwtToolbarItem().isSelected());
        } else {
            menuItem = new MenuItem();
            menuItem.setIconStyle(getGwtToolbarItem().getMinIconStyle());
        }

        // selection
        menuItem.setText(getGwtToolbarItem().getTitle());
        SelectionListener<MenuEvent> listener = getSelectListener();
        menuItem.addSelectionListener(listener);
        return menuItem;
    }

    public void setEnabled(boolean enabled) {
        Formatter.setButtonEnabled(getTextToolitem(), enabled);
        Formatter.setMenuItemEnabled(getMenuItem(), enabled);
        Formatter.setMenuItemEnabled(getContextMenuItem(), enabled);
    }


    /**
     * Executed when the item is clicked
     *
     * @return
     */

    private <T extends ComponentEvent> SelectionListener<T> getSelectListener() {
        return new SelectionListener<T>() {
            public void componentSelected(T event) {
                onSelection();
            }
        };
    }

    /**
     * Create a new componet
     *
     * @return
     */
    public Component createNewToolItem() {
        return new Button();
    }


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


    public GWTJahiaToolbarItem getGwtToolbarItem() {
        return gwtToolbarItem;
    }

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        this.linker = linker;
        this.gwtToolbarItem = gwtToolbarItem;
    }

    /**
     * Het property value
     *
     * @param gwtToolbarItem
     * @param propertyName
     * @return
     */
    public String getPropertyValue(GWTJahiaToolbarItem gwtToolbarItem, String propertyName) {
        Map properties = gwtToolbarItem.getProperties();
        GWTJahiaProperty property = properties != null ? (GWTJahiaProperty) properties
                .get(propertyName)
                : null;
        return property != null ? property.getValue() : null;
    }


    public abstract void onSelection();

}
