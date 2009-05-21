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
