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
package org.jahia.ajax.gwt.client.widget.tripanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:25:39
 */
public abstract class TriPanelBrowser extends LayoutContainer {

    protected TriPanelBrowser() {
        super() ;
        setLayout(new FitLayout());
    }

    protected void initWidgets(Component leftTree, Component topTable, Component bottomTabs, Component topToolbar, Component statusBar) {
        // east panels may contain either a table and the details, or only a table
        LayoutContainer eastPanels = new LayoutContainer() ;
        eastPanels.setLayout(new BorderLayout());

        if (bottomTabs != null) {
            BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200) ;
            centerData.setSplit(true);
            eastPanels.add(bottomTabs, centerData) ;
        }

        if (topTable != null) {
            BorderLayoutData northData = new BorderLayoutData(Style.LayoutRegion.CENTER) ;
            northData.setSplit(true) ;
            eastPanels.add(topTable, northData) ;
        }

        // this is the main layout, containing a toolbar at the top, an optional tree on the left,
        // and the layout defined previously (table + details)
        LayoutContainer layout = new LayoutContainer() ;
        layout.setLayout(new BorderLayout());

        if (topToolbar != null) {
            BorderLayoutData northData2 = new BorderLayoutData(Style.LayoutRegion.NORTH, 28, 28, 28) ;
            northData2.setSplit(false) ;
            layout.add(topToolbar, northData2) ;
        }

        if (statusBar != null) {
            BorderLayoutData statusData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 20, 20, 20) ;
            statusData.setSplit(false);
            layout.add(statusBar, statusData) ;
        }

        if (leftTree != null) {
            BorderLayoutData westData = new BorderLayoutData(Style.LayoutRegion.WEST, 200) ;
            westData.setSplit(true);
            westData.setCollapsible(true);
            layout.add(leftTree, westData) ;
        }

        layout.add(eastPanels, new BorderLayoutData(Style.LayoutRegion.CENTER)) ;

        // layout is the main widget contained in the viewport
        add(layout) ;
    }

}
