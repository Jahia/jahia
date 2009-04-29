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
package org.jahia.ajax.gwt.client.widget.category;

import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.widget.category.CategoriesTreeTable;
import org.jahia.ajax.gwt.client.widget.category.CategoryDetails;
import org.jahia.ajax.gwt.client.widget.category.CategoriesTopBar;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 16:54:40
 */
public class CategoriesManager extends TriPanelBrowserLayout {

    public CategoriesManager(final String rootKey, String importActionUrl, String exportActionUrl, String updateActionUrl) {
        super();
        setBorders(false);
        setBodyBorder(false);
        setWidth("100%");
        setHeight("500px");

        // construction of the UI components
        TopRightComponent treeTable = new CategoriesTreeTable(rootKey);
        BottomRightComponent tabs = new CategoryDetails();
        TopBar toolbar = new CategoriesTopBar(treeTable, exportActionUrl, importActionUrl);
        BottomBar statusBar = null;

        // setup widgets in layout
        initWidgets(null,
                treeTable.getComponent(),
                tabs.getComponent(),
                toolbar.getComponent(),
                null);

        // linker initializations
        linker.registerComponents(null, treeTable, tabs, toolbar, null);
        treeTable.initContextMenu();
        linker.handleNewSelection();
    }
}
