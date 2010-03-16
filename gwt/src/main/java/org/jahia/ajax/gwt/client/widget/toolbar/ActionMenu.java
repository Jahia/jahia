package org.jahia.ajax.gwt.client.widget.toolbar;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
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
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 15, 2010
 * Time: 5:05:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActionMenu extends Menu {
    private List<ActionItem> actionItems = new ArrayList<ActionItem>();

    public ActionMenu(final String toolbar,final  Linker linker) {
        super();

        Log.debug("load action menu for toolbar: "+toolbar);
        ToolbarService.App.getInstance().getGWTToolbars(toolbar, JahiaGWTParameters.getGWTJahiaPageContext(),
                new AsyncCallback<GWTJahiaToolbarSet>() {
                    public void onSuccess(GWTJahiaToolbarSet gwtJahiaToolbarSet) {
                        if (gwtJahiaToolbarSet != null && !gwtJahiaToolbarSet.getToolbarList().isEmpty()) {
                            Log.debug("found : "+gwtJahiaToolbarSet.getToolbarList().size()+" toolbars for "+toolbar);
                            createMenu(gwtJahiaToolbarSet, linker);
                        }
                        layout();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to get toolbar bean '" + toolbar + "'", throwable);
                    }
                });

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                linker.syncSelectionContext();
                checkLinkerSelection();
            }
        });
    }

    /**
     * Create menu
     * @param gwtJahiaToolbarSet
     * @param linker
     */
    private void createMenu(final GWTJahiaToolbarSet gwtJahiaToolbarSet, final Linker linker) {
        // add all items found in the defined menus
        for (GWTJahiaToolbar gwtJahiaToolbar : gwtJahiaToolbarSet.getToolbarList()) {
            if (gwtJahiaToolbar.isContextMenu()) {
                for (int i = 0; i < gwtJahiaToolbar.getGwtToolbarItemsGroups().size(); i++) {
                    GWTJahiaToolbarItemsGroup itemsGroup = gwtJahiaToolbar.getGwtToolbarItemsGroups().get(i);
                    if (i > 0 && i <= gwtJahiaToolbar.getGwtToolbarItemsGroups().size()
                            && itemsGroup.getGwtToolbarItems().isEmpty()) {
                        add(new SeparatorMenuItem());
                    }
                    for (GWTJahiaToolbarItem gwtJahiaToolbarItem : itemsGroup.getGwtToolbarItems()) {
                        ActionItem actionItem = gwtJahiaToolbarItem.getActionItem();
                        if (actionItem != null) {
                            actionItem.init(gwtJahiaToolbarItem, linker);
                            actionItems.add(actionItem);
                            Log.debug("add action-menu : "+gwtJahiaToolbarItem.getTitle());
                            add(actionItem.getContextMenuItem());
                        }
                    }
                }
            }
        }
    }

    /**
     * Check linker selection
     */
    private void checkLinkerSelection() {
        for (ActionItem item : actionItems) {
            item.handleNewLinkerSelection();
        }
    }
}

