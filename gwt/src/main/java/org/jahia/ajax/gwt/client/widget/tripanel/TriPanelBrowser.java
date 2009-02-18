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
