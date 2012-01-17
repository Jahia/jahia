/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
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
public class ActionContextMenu extends ActionToolbarMenu {

    public ActionContextMenu(final GWTJahiaToolbar toolbar, final Linker linker) {
        super(linker);
        this.actionItems = new ArrayList<ActionItem>();
        createMenu(toolbar);

        // add listener on BedoreShow Event
        addListener(Events.BeforeShow, new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent baseEvent) {
                baseEvent.setCancelled(!beforeShow());
            }
        });
    }

    /**
     * Override this method to provide a custom "beforeShow" behaviour
     */
    public boolean beforeShow() {
        checkLinkerSelection();
        return true;
    }

    /**
     * Create Menu
     * @param gwtJahiaToolbar
     */
    private void createMenu(GWTJahiaToolbar gwtJahiaToolbar) {
        for (GWTJahiaToolbarItem item : gwtJahiaToolbar.getGwtToolbarItems()) {
            addItem(item);
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

    @Override
    protected void onClick(ComponentEvent ce) {
        super.onClick(ce);
        hide();
    }

    protected Item createActionItem(ActionItem actionItem) {
        return actionItem.getContextMenuItem();
    }

}

