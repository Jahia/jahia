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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.store.ListStore;

import java.util.List;

/**
 *
 * User: jahia
 * Date: 16 janv. 2009 - 14:56:56
 */
public class JahiaPortletInstanceListView extends ListView<GWTJahiaNode> {
    private ListStore<GWTJahiaNode> listStore;

    public JahiaPortletInstanceListView() {
        listStore = new ListStore<GWTJahiaNode>();
        setStore(listStore);
        setBorders(false);
        addStyleName("gwt-portlet-listview");
        

    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        model.set("shortName", Util.ellipse(model.getName(), 14));
        if (model.getPreview() == null || model.getPreview().equalsIgnoreCase("")) {
            model.set("preview", "/css/images/portlets/window_application.png");
        }
        return model;
    }

    public void setContextMenu(Menu menu) {
        super.setContextMenu(menu);
    }

    public void addPortlets(List<GWTJahiaNode> gwtJahiaNodes) {
        listStore.add(gwtJahiaNodes);
        refresh();
    }

    public void clear() {
        listStore.removeAll();
        refresh();
    }
}
