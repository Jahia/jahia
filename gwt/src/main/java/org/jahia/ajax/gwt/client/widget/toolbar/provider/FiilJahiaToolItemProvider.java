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

import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;

/**
 * User: ktlili
 * Date: 18 sept. 2008
 * Time: 12:29:05
 */
public class FiilJahiaToolItemProvider extends JahiaToolItemProvider {
    public ToolItem createToolItem(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        return new FillToolItem();
    }

    public MenuItem createMenuItem(final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        MenuItem menuItem = new MenuItem();
        return menuItem;
    }

    public DataListItem createDataListItem(final DataList list, final GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, final GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public Widget createWidget(GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return new FillToolItem();
    }

    public TabItem createTabItem(TabPanel tabPanel, GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup, GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }
}
