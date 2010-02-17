package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SeparatorActionItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 11:43:54 AM
 */
public class ActionToolbar extends ToolBar {
    private Linker linker;
    private List<ActionItem> items = new ArrayList<ActionItem>();
    private GWTJahiaToolbar gwtToolbar;
    private boolean loaded = false;

    public ActionToolbar(GWTJahiaToolbar gwtToolbar, Linker linker) {
        this.gwtToolbar = gwtToolbar;
        this.linker = linker;
    }


    /**
     * Create ui
     */
    public void createToolBar() {
        if (!loaded) {
            loaded = true;
            Log.debug("---- is display title: " + gwtToolbar.isDisplayTitle());
            if (gwtToolbar.isDisplayTitle() && gwtToolbar.getTitle() != null && gwtToolbar.getTitle().length() > 0) {
                LabelToolItem titleItem = new LabelToolItem(gwtToolbar.getTitle() + ":");
                titleItem.addStyleName("gwt-toolbar-title");
                add(titleItem);
            }

            // add items
            List<GWTJahiaToolbarItemsGroup> itemsGroupList = gwtToolbar.getGwtToolbarItemsGroups();
            Log.debug("---- Nb items group: " + itemsGroupList.size());
            for (int i = 0; i < itemsGroupList.size(); i++) {
                GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = itemsGroupList.get(i);
                Log.debug("---- items group type: " + gwtToolbarItemsGroup.getType());
                if (gwtToolbarItemsGroup.getType() != null && gwtToolbarItemsGroup.getType().equalsIgnoreCase(ToolbarConstants.ITEMSGROUP_FILL)) {
                    // special items type: fill type
                    add(new FillToolItem());
                } else {
                    createItemGroup(gwtToolbarItemsGroup);
                    if (i != itemsGroupList.size() - 1 && itemsGroupList.get(i + 1).isNeedSeparator()) {
                        // add separator
                        add(new SeparatorToolItem());
                    }
                }

            }
        }
    }

    /**
     * Fill toolbar
     *
     * @param gwtToolbarItemsGroup
     */
    public void createItemGroup(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup) {
        final List<GWTJahiaToolbarItem> toolbarItemsGroupList = gwtToolbarItemsGroup.getGwtToolbarItems();
        final Menu menu = new Menu();
        boolean addMenu = false;
        // add toolItem
        for (int i = 0; i < toolbarItemsGroupList.size(); i++) {
            // add items
            final GWTJahiaToolbarItem gwtToolbarItem = toolbarItemsGroupList.get(i);
            Component toolItem = null;
            final ActionItem actionItem = gwtToolbarItem.getActionItem();

            if (actionItem == null && !isSeparator(gwtToolbarItem)) {
                printProviderNotFoundError(gwtToolbarItem);
            } else if (actionItem != null) {
                actionItem.init(gwtToolbarItem, linker);

                Log.debug(gwtToolbarItem.getType() + " - items group layout =" + gwtToolbarItemsGroup.getLayout());
                if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU_RADIO || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_ITEMSGROUP_MENU_CHECKBOX) {
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
                        toolItem = actionItem.getMenuItem();
                        if (toolItem != null) {
                            if (gwtToolbarItem.getType() != null && gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)) {
                                toolItem.setEnabled(false);
                            }
                            menu.add(toolItem);
                        }
                        addMenu = true;
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_BUTTON || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_ONLY_LABEL || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.LAYOUT_BUTTON_LABEL) {
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        toolItem = actionItem.getTextToolItem();
                        add(toolItem);
                    }
                } else {
                    gwtToolbarItemsGroup.setLayout(ToolbarConstants.LAYOUT_BUTTON);
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        toolItem = actionItem.getTextToolItem();
                        add(toolItem);
                    }
                }
            }

            if (actionItem != null) {
                items.add(actionItem);
            }
        }

        // add menu
        if (addMenu) {
            Button menuToolItem = new Button(gwtToolbarItemsGroup.getItemsGroupTitle());
            String minIconStyle = gwtToolbarItemsGroup.getMinIconStyle();
            if (minIconStyle != null) {
                menuToolItem.setIconStyle(minIconStyle);
            }
            menuToolItem.setMenu(menu);
            add(menuToolItem);
        }

        // handle selectetion

    }

    public void setContextMenu(Menu menu) {
        setContextMenu(menu);
    }

    /**
     * return true if it's a Separator
     *
     * @param gwtToolbarItem
     * @return
     */
    protected boolean isSeparator(GWTJahiaToolbarItem gwtToolbarItem) {
        return gwtToolbarItem.getActionItem() != null && gwtToolbarItem.getActionItem() instanceof SeparatorActionItem;
    }


    /**
     * Handle linker selection
     */
    public void handleNewLinkerSelection() {
        for (ActionItem item : items) {
            item.handleNewLinkerSelection();
        }
    }

    /**
     * diable all
     */
    public void allDisable() {
        for (ActionItem item : items) {
            item.setEnabled(false);
        }
    }


    /**
     * ToolItem type unknown
     *
     * @param gwtToolbarItem
     */
    protected void printProviderNotFoundError(GWTJahiaToolbarItem gwtToolbarItem) {
        /* Window.alert("toolbar item widget " + gwtToolbarItem.getType() + " unknown. " +
       "\nPlease register it first in" +
       "\n'org.jahia.ajax.gwt.toolbar.client.util.reflection.CustomToolbarItemWidgetInstanceProvider' or" +
       "\n'org.jahia.ajax.gwt.toolbar.client.util.reflection.JahiaToolbarItemWidgetInstanceProvider'."); */
    }


}
