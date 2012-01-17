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
