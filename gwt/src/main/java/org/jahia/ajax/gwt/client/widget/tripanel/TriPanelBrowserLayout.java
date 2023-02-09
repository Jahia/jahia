/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.tripanel;

import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;

/**
 *
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 11:54:43
 * <p/>
 * This is the Layout version of the tripanel browser, allowing the component to be placed
 * among existing elements.
 */
public class TriPanelBrowserLayout extends ContentPanel {

    protected ManagerLinker linker;
    protected BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 320);
    protected BorderLayoutData northData = new BorderLayoutData(Style.LayoutRegion.CENTER);
    protected BorderLayoutData westData = new BorderLayoutData(Style.LayoutRegion.WEST, 350);

    public TriPanelBrowserLayout(GWTManagerConfiguration configuration) {
        super(new FillLayout());
        setHeaderVisible(false);
        linker = new ManagerLinker(configuration) ;
    }

    public void initWidgets(Component leftTree, Component topTable, Component bottomTabs, Component topToolbar, Component statusBar) {
        if (topToolbar != null) {
            topToolbar.setId("JahiaGxtManagerToolbar");
            setTopComponent(topToolbar);
        }

        if (statusBar != null) {
            statusBar.setId("JahiaGxtManagerStatusbar");
            setBottomComponent(statusBar);
        }

        // east panels may contain either a table and the details, or only a table
        LayoutContainer eastPanels = new LayoutContainer();
        eastPanels.setLayout(new BorderLayout());
        if (bottomTabs != null) {
            centerData.setSplit(true);
            bottomTabs.setId("JahiaGxtManagerBottomTabs");
            eastPanels.add(bottomTabs, centerData);
        }

        if (topTable != null) {

            northData.setSplit(true);
            topTable.setId("JahiaGxtManagerTobTable");
            eastPanels.add(topTable, northData);
        }

        // this is the main layout, containing a toolbar at the top, an optional tree on the left,
        // and the layout defined previously (table + details)
        LayoutContainer layout = new LayoutContainer();
        layout.setLayout(new BorderLayout());

        if (leftTree != null) {
            westData.setSplit(true);
            westData.setCollapsible(true);
            leftTree.setId("JahiaGxtManagerLeftTree");
            layout.add(leftTree, westData);
        }

        layout.add(eastPanels, new BorderLayoutData(Style.LayoutRegion.CENTER));

        layout.setId("JahiaGxtManagerMain");

        // layout is the main widget contained in the viewport
        add(layout);


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

    public ManagerLinker getLinker() {
        return linker;
    }


}
