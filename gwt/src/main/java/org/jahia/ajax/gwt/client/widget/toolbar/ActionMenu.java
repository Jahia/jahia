package org.jahia.ajax.gwt.client.widget.toolbar;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Action menu component.
 * User: ktlili
 * Date: Mar 15, 2010
 * Time: 5:05:34 PM
 */
public class ActionMenu extends Menu {
    private Linker linker;
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();
    private GWTJahiaToolbarSet toolbarSet;
    private GWTJahiaToolbar toolbar;


    public ActionMenu(final GWTJahiaToolbar toolbar, final Linker linker) {
        super();
        this.linker = linker;
        this.toolbar = toolbar;
       // createMenu(toolbar);

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    public ActionMenu(final GWTJahiaToolbarSet toolbarSet, final Linker linker) {
        super();
        this.linker = linker;
        this.toolbarSet = toolbarSet;
       // createMenu(toolbarSet);

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        if(toolbar != null){
           createMenu(toolbar);
        } else if(toolbarSet != null){
           createMenu(toolbarSet);
        }
        
    }

    public ActionMenu(final String toolbar, final Linker linker) {
        super();
        this.linker = linker;
        ToolbarService.App.getInstance().getGWTToolbars(toolbar, new BaseAsyncCallback<GWTJahiaToolbarSet>() {
            public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                if (gwtJahiaToolbarSet != null && !gwtJahiaToolbarSet.getToolbarList().isEmpty()) {
                    createMenu(gwtJahiaToolbarSet);
                }
                layout();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Unable to get toolbar bean '" + toolbar + "'", throwable);
            }
        });

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                beforeShow();
            }
        });
    }

    /**
     * Override this method to provide a custom "beforeShow" behaviour
     */
    public void beforeShow() {
        linker.syncSelectionContext();
        checkLinkerSelection();
    }

    /**
     * Create menu
     *
     * @param gwtJahiaToolbarSet
     */
    private void createMenu(final GWTJahiaToolbarSet gwtJahiaToolbarSet) {
        // add all items found in the defined menus
        for (GWTJahiaToolbar gwtJahiaToolbar : gwtJahiaToolbarSet.getToolbarList()) {
            if (gwtJahiaToolbar.isContextMenu()) {
                createMenu(gwtJahiaToolbar);
            }
        }
    }

    /**
     * Create Menu
     * @param gwtJahiaToolbar
     */
    private void createMenu(GWTJahiaToolbar gwtJahiaToolbar) {
        List<Item> allItems = new ArrayList<Item>();
        for (int i = 0; i < gwtJahiaToolbar.getGwtToolbarItemsGroups().size(); i++) {
            List<Item> groupItems = new ArrayList<Item>();
            GWTJahiaToolbarItemsGroup itemsGroup = gwtJahiaToolbar.getGwtToolbarItemsGroups().get(i);
            if (!itemsGroup.isContextMenu()) {
                continue;
            }
            for (GWTJahiaToolbarItem gwtJahiaToolbarItem : itemsGroup.getGwtToolbarItems()) {
                if (!gwtJahiaToolbarItem.isContextMenu()) {
                    continue;
                }
                ActionItem actionItem = gwtJahiaToolbarItem.getActionItem();
                if (actionItem != null) {
                    actionItem.init(gwtJahiaToolbarItem, linker);
                    actionItems.add(actionItem);
                    if (Log.isDebugEnabled()) {
                        Log.debug("add action-menu : " + gwtJahiaToolbarItem.getTitle());
                    }
                    groupItems.add(actionItem.getContextMenuItem());
                }
            }
            if (!groupItems.isEmpty()) {
                if (allItems.size() > 0 && !(allItems.get(allItems.size() - 1) instanceof SeparatorMenuItem) && !(groupItems.get(0) instanceof SeparatorMenuItem)) {
                    allItems.add(new SeparatorMenuItem());
                }
                allItems.addAll(groupItems);
            }
        }
        if (allItems.size() > 0 && (allItems.get(0) instanceof SeparatorMenuItem)) {
            allItems.remove(0);
        }
        if (allItems.size() > 0 && (allItems.get(allItems.size() - 1) instanceof SeparatorMenuItem)) {
            allItems.remove(allItems.size() - 1);
        }
        for (Item item : allItems) {
            add(item);
        }
    }

    /**
     * Check linker selection
     */
    private void checkLinkerSelection() {
        for (ActionItem item : actionItems) {
            try {
                item.handleNewLinkerSelection();
            } catch (Exception e) {
            }
        }
    }
}

