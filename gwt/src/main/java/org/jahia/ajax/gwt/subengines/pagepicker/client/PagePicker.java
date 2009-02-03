/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.subengines.pagepicker.client;

import org.jahia.ajax.gwt.subengines.pagepicker.client.components.PagePathBar;
import org.jahia.ajax.gwt.subengines.pagepicker.client.components.PageStatusBar;
import org.jahia.ajax.gwt.subengines.pagepicker.client.components.PageTreeTable;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import org.jahia.ajax.gwt.tripanelbrowser.client.TriPanelBrowserLayout;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.BottomBar;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopBar;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 2 sept. 2008
 * Time: 17:44:05
 * To change this template use File | Settings | File Templates.
 */
public class PagePicker extends TriPanelBrowserLayout {

    public PagePicker(String operation, String pagePath, String parentPath, int homePageId, int siteId, String callback) {
        super() ;
        setWidth("714px");
        setHeight("400px");

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
