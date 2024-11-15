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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.node.GWTBitSet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.security.PermissionsResolver;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 13:32:32
 */
@SuppressWarnings("serial")
public abstract class BaseActionItem implements ActionItem {
    private GWTJahiaToolbarItem gwtToolbarItem;

    private transient Component textToolitem = null;
    private transient MenuItem menuItem = null;
    private transient MenuItem contextMenuItem = null;
    protected transient Linker linker;
    private transient boolean enabled;

    public BaseActionItem() {
    }

    public Component getCustomItem() {
        return null;
    }

    /**
     * Get text toolitem
     *
     * @return
     */
    public Component getTextToolItem() {
        if (textToolitem != null) {
            return textToolitem;
        }
        textToolitem = createNewToolItem();
        final GWTJahiaToolbarItem gwtJahiaToolbarItem = getGwtToolbarItem();
        int layout = gwtJahiaToolbarItem.getLayout();

        // set properties that are specific to a ToggleToolItem
        if (textToolitem instanceof ToggleButton) {
            ((ToggleButton) textToolitem).toggle(gwtJahiaToolbarItem.isSelected());
        }

        // set properties that are specific to a Button
        if (textToolitem instanceof Button) {
            if (layout == Constants.LAYOUT_BUTTON_LABEL || layout == Constants.LAYOUT_ONLY_LABEL) {
                if (gwtJahiaToolbarItem.isDisplayTitle()) {
                    ((Button) textToolitem).setText(gwtJahiaToolbarItem.getTitle());
                }
            }
            if (layout == Constants.LAYOUT_BUTTON_LABEL || layout == Constants.LAYOUT_BUTTON) {
                if (gwtJahiaToolbarItem.getIcon() != null) {
                    ((Button) textToolitem).setIcon(ToolbarIconProvider.getInstance().getIcon(gwtJahiaToolbarItem.getIcon()));
                }
            }

            // add listener
            SelectionListener<ButtonEvent> listener = getSelectListener();
            if (listener != null) {
                ((Button) textToolitem).addSelectionListener(listener);
            }
        }

        // description
        String description = gwtJahiaToolbarItem.getDescription();
        if (gwtJahiaToolbarItem.getDescription() != null && description.length() > 0 && textToolitem.getToolTip() == null && !gwtJahiaToolbarItem.getTitle().equals(description)) {
            textToolitem.setToolTip(description);
        }

        return textToolitem;
    }


    public MenuItem getMenuItem() {
        if (menuItem != null) {
            return menuItem;
        }
        menuItem = createMenuItem();
        initMenuItem(menuItem);
        return menuItem;

    }


    public MenuItem getContextMenuItem() {
        if (contextMenuItem != null) {
            return contextMenuItem;
        }
        contextMenuItem = createMenuItem();
        initMenuItem(contextMenuItem);
        return contextMenuItem;
    }

    private void initMenuItem(final MenuItem menuItem) {
        GWTJahiaToolbarItem toolbarItem = getGwtToolbarItem();

        if (toolbarItem.getIcon() != null) {
            menuItem.setIcon(ToolbarIconProvider.getInstance().getIcon(toolbarItem.getIcon()));
        }

        // selection
        menuItem.setText(toolbarItem.getTitle());
        if (!toolbarItem.getTitle().equals(toolbarItem.getDescription())) {
            menuItem.setToolTip(toolbarItem.getDescription());
        }
        SelectionListener<MenuEvent> listener = getSelectListener();
        menuItem.addSelectionListener(listener);
    }

    public void setSubMenu(Menu menu) {
        if (isTextToolItem()) {
            Button button = (Button) getTextToolItem();
            button.setMenu(menu);
        }

        if (isMenuItem()) {
            MenuItem mi = getMenuItem();
            mi.setHideOnClick(false);
            mi.setSubMenu(menu);
        }

        if (isContextMenuItem()) {
            MenuItem mi = getContextMenuItem();
            mi.setHideOnClick(false);
            mi.setSubMenu(menu);
        }
    }

    public boolean isTextToolItem() {
        return textToolitem != null;
    }

    public boolean isMenuItem() {
        return menuItem != null;
    }

    public boolean isContextMenuItem() {
        return contextMenuItem != null;
    }

    public void setTextToolitem(Component textToolitem) {
        this.textToolitem = textToolitem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public void setContextMenuItem(MenuItem contextMenuItem) {
        this.contextMenuItem = contextMenuItem;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (isTextToolItem()) {
            if (gwtToolbarItem.isHideWhenDisabled()) {
                getTextToolItem().setVisible(enabled);
            } else {
                Formatter.setButtonEnabled(getTextToolItem(), enabled);
            }
        }
        if (isMenuItem()) {
            if (gwtToolbarItem.isHideWhenDisabled()) {
                getMenuItem().setVisible(enabled);
            } else {
                Formatter.setMenuItemEnabled(getMenuItem(), enabled);
            }
        }
        if (isContextMenuItem()) {
            getContextMenuItem().setVisible(enabled);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVisible(boolean visible) {
        if (isTextToolItem()) {
            getTextToolItem().setVisible(visible);
        }
        if (isMenuItem()) {
            getMenuItem().setVisible(visible);
        }
        if (isContextMenuItem()) {
            getContextMenuItem().setVisible(visible);
        }
    }


    /**
     * Executed when the item is clicked
     *
     * @return
     */

    private <T extends ComponentEvent> SelectionListener<T> getSelectListener() {
        return new SelectionListener<T>() {
            public void componentSelected(T event) {
                onComponentSelection();
            }
        };
    }

    /**
     * Create a new componet
     *
     * @return
     */
    public Component createNewToolItem() {
        Button button = new Button();
        gwtToolbarItem.addClasses(button);
        button.addStyleName("action-bar-tool-item");
        return button;
    }

    /**
     * Create a menuItem
     *
     * @return
     */
    public MenuItem createMenuItem() {
        MenuItem menuItem = new MenuItem();
        gwtToolbarItem.addClasses(menuItem);
        menuItem.addStyleName("action-bar-menu-item");
        return menuItem;
    }

    /**
     * Get the corresponding gwt item
     * @return
     */
    public GWTJahiaToolbarItem getGwtToolbarItem() {
        return gwtToolbarItem;
    }

    /**
     * Init the action item.
     * @param gwtToolbarItem
     * @param linker
     */
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
        Map<String, GWTJahiaProperty> properties = gwtToolbarItem.getProperties();
        GWTJahiaProperty property = properties != null ? properties.get(propertyName) : null;
        return property != null ? property.getValue() : null;
    }

    /**
     * Called when the action component is selected. Override this method to provide custom behaviour
     */
    public void onComponentSelection(){

    }

    /**
     *  Called when there is a new liker selection. Override this method to provide custom behaviour
     */
    public void handleNewLinkerSelection(){
        if (getGwtToolbarItem().getRequiredModule() != null) {
            @SuppressWarnings("unchecked")
            List<String> installedModules = (List<String>) JahiaGWTParameters.getSiteNode().get("j:installedModules");
            setVisible(installedModules != null && installedModules.contains(getGwtToolbarItem().getRequiredModule()));
        }
    }

    public void handleNewMainNodeLoaded(GWTJahiaNode node){

    }

    public boolean hasPermission(final GWTJahiaNode node) {
        return hasSitePermission() && gwtToolbarItem.getRequiredPermissionsResolver()
                .resolve(gwtToolbarItem.getRequiredPermissions(), new PermissionsResolver.Matcher() {
            @Override
            public boolean matches(String permission) {
                return PermissionsUtils.isPermitted(permission, node);
            }
        });
    }

    public boolean hasPermission(final GWTBitSet permissions) {
        return hasSitePermission() && gwtToolbarItem.getRequiredPermissionsResolver()
                .resolve(gwtToolbarItem.getRequiredPermissions(), new PermissionsResolver.Matcher() {
            @Override
            public boolean matches(String permission) {
                return PermissionsUtils.isPermitted(permission, permissions);
            }
        });
    }

    public boolean hasSitePermission() {
        return PermissionsResolver.MATCH_ALL.resolve(gwtToolbarItem.getRequiredSitePermissions(), new PermissionsResolver.Matcher() {
            @Override
            public boolean matches(String permission) {
                return PermissionsUtils.isPermitted(permission, JahiaGWTParameters.getSiteNode());
            }
        });
    }

    protected void updateTitle(String title) {
        if (textToolitem != null) {
            ((Button) textToolitem).setText(title);
        }
        if (menuItem != null) {
            menuItem.setText(title);
            if (menuItem.getParentMenu() != null) {
                menuItem.getParentMenu().recalculate();
            }
        }
        if (contextMenuItem != null) {
            contextMenuItem.setText(title);
            if (menuItem !=null && menuItem.getParentMenu() != null) {
                menuItem.getParentMenu().recalculate();
            }
        }
    }
}
