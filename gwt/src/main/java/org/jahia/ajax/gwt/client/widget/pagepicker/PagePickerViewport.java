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
package org.jahia.ajax.gwt.client.widget.pagepicker;

import org.jahia.ajax.gwt.client.widget.pagepicker.PagePathBar;
import org.jahia.ajax.gwt.client.widget.pagepicker.PageStatusBar;
import org.jahia.ajax.gwt.client.widget.pagepicker.PageTreeTable;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 2 sept. 2008
 * Time: 17:44:05
 * To change this template use File | Settings | File Templates.
 */
public class PagePickerViewport extends TriPanelBrowserViewport {

    public PagePickerViewport(String operation, String pagePath, String parentPath, int homePageId, int siteId, String callback) {
        super() ;

        // construction of the UI components
        TopRightComponent treeTable = new PageTreeTable(homePageId, siteId, operation, pagePath, parentPath) ;
        TopBar pathBar = new PagePathBar(operation, parentPath, callback) ;
        BottomBar statusBar = new PageStatusBar() ;

        // setup widgets in layout
        initWidgets(null,
                    treeTable.getComponent(),
                    null,
                    pathBar.getComponent(),
                    statusBar.getComponent());

        // linker initializations
        linker.registerComponents(null, treeTable, null, pathBar, statusBar) ;
        treeTable.initContextMenu();
        linker.handleNewSelection();
    }
}