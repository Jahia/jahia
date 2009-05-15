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

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.widget.toolbar.ToolbarManager;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

/**
 * Item provider for creating a list of toolbars to have a possibility to
 * show/hide then.
 *
 * @author Sergiy Shyrkov
 */
public class ToobarsJahiaToolItemProvider extends
        AbstractJahiaToolItemProvider {

    private ToolbarManager toolbarManager;

    /**
     * Initializes an instance of this class.
     */
    public ToobarsJahiaToolItemProvider() {
        super();
        toolbarManager = JahiaPageEntryPoint.getToolbarManager();
    }

    @Override
    public MenuItem createMenuItem(GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup,GWTJahiaToolbarItem gwtToolbarItem) {
        final MenuItem thisItem = new MenuItem(gwtToolbarItem.getTitle());
        toolbarManager.addContextMenuReadyListener(new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                Menu ctxMenu = ((Menu) be.source);
                List<Item> items = new ArrayList<Item>(ctxMenu.getItems());
                for (Item ctxMenuItem : items) {
                    ctxMenu.remove(ctxMenuItem);
                    thisItem.getParentMenu().add(ctxMenuItem);
                }
            }
        });
        return thisItem;
    }

    @Override
    public ToolItem createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        return new TextToolItem();
    }

    @Override
    public SelectionListener<ComponentEvent> getSelectListener(
            final GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

}
