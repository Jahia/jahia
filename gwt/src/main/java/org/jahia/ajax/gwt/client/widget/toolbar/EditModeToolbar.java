package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.ProviderHelper;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.JahiaToolItemProvider;
import org.jahia.ajax.gwt.client.widget.toolbar.provider.JahiaProviderFactory;
import org.jahia.ajax.gwt.client.widget.toolbar.edit.EditModeSelectionHandler;
import org.jahia.ajax.gwt.client.widget.toolbar.edit.EditProviderFactory;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 11:43:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeToolbar extends ToolBar {
    protected GWTJahiaToolbar gwtToolbar;
    protected boolean loaded = false;
    private EditModeSelectionHandler editModeSelectionHandler;
    private EditLinker editLinker;

    public EditModeToolbar(GWTJahiaToolbar gwtToolbar, EditLinker editLinker, EditModeSelectionHandler editModeSelectionHandler) {
        this.gwtToolbar = gwtToolbar;
        this.editModeSelectionHandler = editModeSelectionHandler;
        this.editLinker = editLinker;
    }


    /**
     * Create ui
     */
    public void createToolBarUI() {
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
            Log.debug("---- Nb items groupe: " + itemsGroupList.size());
            for (int i = 0; i < itemsGroupList.size(); i++) {
                GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = itemsGroupList.get(i);
                Log.debug("---- items group type: " + gwtToolbarItemsGroup.getType());
                if (gwtToolbarItemsGroup.getType() != null && gwtToolbarItemsGroup.getType().equalsIgnoreCase(ToolbarConstants.ITEMSGROUP_FILL)) {
                    // special items type: fill type
                    add(new FillToolItem());
                } else {
                    fillToolBar(gwtToolbarItemsGroup);
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
    public void fillToolBar(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup) {
        final ProviderHelper jahiaProviderHelper = getProviderHelper();
        final List toolbarItemsGroupList = gwtToolbarItemsGroup.getGwtToolbarItems();
        final Menu menu = new Menu();
        boolean addMenu = false;
        // add toolItem
        for (int i = 0; i < toolbarItemsGroupList.size(); i++) {
            // add items
            Component createdToolItem = null;
            GWTJahiaToolbarItem gwtToolbarItem = (GWTJahiaToolbarItem) toolbarItemsGroupList.get(i);
            JahiaToolItemProvider toolbarItemWidgetProvider = jahiaProviderHelper.getJahiaToolItemProvider(gwtToolbarItem.getType());
            if (toolbarItemWidgetProvider == null && !isSeparator(gwtToolbarItem)) {
                printProviderNotFoundError(gwtToolbarItem);
            } else if (toolbarItemWidgetProvider != null) {
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
                        createdToolItem = toolbarItemWidgetProvider.createMenuItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        if (createdToolItem != null) {
                            if (gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)) {
                                createdToolItem.setEnabled(false);
                            }
                            menu.add(createdToolItem);
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
                        createdToolItem = toolbarItemWidgetProvider.createMenuItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        if (createdToolItem != null) {
                            if (gwtToolbarItem.getType().equalsIgnoreCase(ToolbarConstants.ITEMS_TOOLBARLABEL)) {
                                createdToolItem.setEnabled(false);
                            }
                            menu.add(createdToolItem);
                        }
                        addMenu = true;
                    }
                } else if (gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_LABEL || gwtToolbarItemsGroup.getLayout() == ToolbarConstants.ITEMSGROUP_BUTTON_LABEL) {
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        createdToolItem = toolbarItemWidgetProvider.createToolItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        add(createdToolItem);
                    }
                } else {
                    gwtToolbarItemsGroup.setLayout(ToolbarConstants.ITEMSGROUP_BUTTON);
                    if (isSeparator(gwtToolbarItem)) {
                        add(new SeparatorToolItem());
                    } else {
                        createdToolItem = toolbarItemWidgetProvider.createToolItem(gwtToolbarItemsGroup, gwtToolbarItem);
                        add(createdToolItem);
                    }
                }


            }

            // register selection handler
            editModeSelectionHandler.registerComponent(gwtToolbarItem,createdToolItem);

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
        return gwtToolbarItem.getType() != null && gwtToolbarItem.getType().equalsIgnoreCase(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_SEPARATOR);
    }

    /**
     * Get provider helper
     *
     * @return
     */
    protected ProviderHelper getProviderHelper() {
        ProviderHelper jahiaProviderHelper = new EditProviderFactory(editLinker);
        return jahiaProviderHelper;
    }


    /**
     * Handle module selection
     *
     * @param selectedModule
     */
    public void handleNewModuleSelection(Module selectedModule) {
       editModeSelectionHandler.handleNewModuleSelection(selectedModule);
    }

    /**
     * Handle new side panel selection
     * @param node
     */
    public void handleNewSidePanelSelection(GWTJahiaNode node) {
        editModeSelectionHandler.handleNewSidePanelSelection(node);
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
