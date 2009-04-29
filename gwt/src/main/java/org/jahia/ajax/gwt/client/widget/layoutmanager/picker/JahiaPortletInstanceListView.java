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
