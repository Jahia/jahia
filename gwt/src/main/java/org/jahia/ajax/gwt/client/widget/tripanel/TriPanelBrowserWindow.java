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

import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 11:54:43
 * <p/>
 * This is the Layout version of the tripanel browser, allowing the component to be placed
 * among existing elements.
 */
public class TriPanelBrowserWindow extends Window {

    protected BrowserLinker linker;
    protected BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 200);
    protected BorderLayoutData northData = new BorderLayoutData(Style.LayoutRegion.CENTER);
    protected BorderLayoutData westData = new BorderLayoutData(Style.LayoutRegion.WEST, 200);

    protected TriPanelBrowserWindow() {
        super();
        setLayout(new FitLayout());
        linker = new BrowserLinker() ;
    }

    protected void initWidgets(Component leftTree, Component topTable, Component bottomTabs, Component topToolbar, Component statusBar) {
        // east panels may contain either a table and the details, or only a table
        LayoutContainer eastPanels = new LayoutContainer();
        eastPanels.setLayout(new BorderLayout());

        if (bottomTabs != null) {
            centerData.setSplit(true);
            eastPanels.add(bottomTabs, centerData);
        }

        if (topTable != null) {
            northData.setSplit(true);
            eastPanels.add(topTable, northData);
        }

        // this is the main layout, containing a toolbar at the top, an optional tree on the left,
        // and the layout defined previously (table + details)
        LayoutContainer layout = new LayoutContainer();
        layout.setLayout(new BorderLayout());

        if (leftTree != null) {
            westData.setSplit(true);
            westData.setCollapsible(true);
            layout.add(leftTree, westData);
        }

        layout.add(eastPanels, new BorderLayoutData(Style.LayoutRegion.CENTER));

        // layout is the main widget contained in the viewport
        add(layout);

        if (topToolbar != null) {
            setTopComponent(topToolbar);
        }

        if (statusBar != null) {
            setBottomComponent(statusBar);
        }
    }

    public BorderLayoutData getCenterData() {
        return centerData;
    }

    public void setCenterData(BorderLayoutData centerData) {
        this.centerData = centerData;
    }

    public BorderLayoutData getNorthData() {
        return northData;
    }

    public void setNorthData(BorderLayoutData northData) {
        this.northData = northData;
    }

    public BorderLayoutData getWestData() {
        return westData;
    }

    public void setWestData(BorderLayoutData westData) {
        this.westData = westData;
    }

    public BrowserLinker getLinker() {
        return linker;
    }


}