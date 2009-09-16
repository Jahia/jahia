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
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItemItf;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.SidePanelSelectionHandler;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.Module;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 11:43:54 AM
 */
public class ActionToolbar extends ToolBar {
    private List<ActionItemItf> items = new ArrayList<ActionItemItf>();
    private GWTJahiaToolbar gwtToolbar;
    private boolean loaded = false;
    private ActionItemFactoryItf actionItemFactoryItf;


    public ActionToolbar(GWTJahiaToolbar gwtToolbar, ActionItemFactoryItf actionItemFactoryItf) {
        this.gwtToolbar = gwtToolbar;
        this.actionItemFactoryItf = actionItemFactoryItf;
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
        final ActionItemFactoryItf actionFactory = getActionItemFactory();
        final List<GWTJahiaToolbarItem> toolbarItemsGroupList = gwtToolbarItemsGroup.getGwtToolbarItems();
        final Menu menu = new Menu();
        boolean addMenu = false;
        // add toolItem
        for (int i = 0; i < toolbarItemsGroupList.size(); i++) {
            // add items
            final GWTJahiaToolbarItem gwtToolbarItem = toolbarItemsGroupList.get(i);
            Component toolItem = null;
            final ActionItemItf actionItem = actionFactory.createActionItem(gwtToolbarItem);

            if (actionItem == null && !isSeparator(gwtToolbarItem)) {
                printProviderNotFoundError(gwtToolbarItem);
            } else if (actionItem != null) {
                Log.debug(gwtToolbarItem.getType() + " - items group layout =" + gwtToolbarItemsGroup.getLayout());
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
                        toolItem = actionItem.getMenuItem();
                        if (toolItem != null) {
                            if (gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)) {
                                toolItem.setEnabled(false);
                            }
                            menu.add(toolItem);
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
                        toolItem = actionItem.getMenuItem();
                        if (toolItem != null) {
                            if (gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)) {
                                toolItem.setEnabled(false);
                            }
                            menu.add(toolItem);
                        }
                        addMenu = true;
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_LABEL || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON_LABEL) {
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        toolItem = actionItem.getTextToolitem();
                        add(toolItem);
                    }
                } else {
                    gwtToolbarItemsGroup.setLayout(ToolbarConstants.ITEMSGROUP_BUTTON);
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        toolItem = actionItem.getTextToolitem();
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
        return gwtToolbarItem.getType() != null && gwtToolbarItem.getType().equalsIgnoreCase(ActionItemFactory.ORG_JAHIA_TOOLBAR_ITEM_SEPARATOR);
    }

    /**
     * Get provider helper
     *
     * @return
     */
    public ActionItemFactoryItf getActionItemFactory() {
        return this.actionItemFactoryItf;
    }


    /**
     * Handle module selection
     *
     * @param selectedModule
     */
    public void handleNewModuleSelection(Module selectedModule) {
        for (ActionItemItf item : items) {
            if (item instanceof ModuleSelectionHandler) {
                ((ModuleSelectionHandler)item).handleNewModuleSelection(selectedModule);
            }
        }
    }

    /**
     * Handle new side panel selection
     *
     * @param node
     */
    public void handleNewSidePanelSelection(GWTJahiaNode node) {
        for (ActionItemItf item : items) {
            if (item instanceof SidePanelSelectionHandler) {
                ((SidePanelSelectionHandler)item).handleNewSidePanelSelection(node);
            }
        }
    }

    /**
     * To DO: REFACTOR THIS METHOD / take only 2 nodes as parameters (tree selection / table selection )
     *
     * @param isTreeSelection
     * @param isTableSelection
     * @param isWritable
     * @param isDeleteable
     * @param isParentWritable
     * @param isSingleFile
     * @param isSingleFolder
     * @param isPasteAllowed
     * @param isLockable
     * @param isLocked
     * @param isZip
     * @param isImage
     * @param isMount
     */
    public void enableOnConditions(boolean isTreeSelection, boolean isTableSelection, boolean isWritable, boolean isDeleteable, boolean isParentWritable, boolean isSingleFile, boolean isSingleFolder, boolean isPasteAllowed, boolean isLockable, boolean isLocked, boolean isZip, boolean isImage, boolean isMount) {
        for (ActionItemItf item : items) {
            if (item instanceof ManagerSelectionHandler) {
                ((ManagerSelectionHandler)item).enableOnConditions(isTreeSelection, isTableSelection, isWritable, isDeleteable, isParentWritable, isSingleFile, isSingleFolder, isPasteAllowed, isLockable, isLocked, isZip, isImage, isMount);
            }
        }
    }

    /**
     * diable all
     */
    public void allDisable() {
        for (ActionItemItf item : items) {
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
