/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.tripanel;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 28 ao�t 2008
 * Time: 11:53:24
 * <p/>
 * This is the Viewport version of the tripanel browser, replacing all the elements contained
 * in the target page.
 */
public class TriPanelBrowserViewport extends Viewport {
    private TriPanelBrowserLayout layout = new TriPanelBrowserLayout();
    protected ManagerLinker linker;

    protected TriPanelBrowserViewport() {
        super();
        setLayout(new FillLayout());
        linker = new ManagerLinker();
    }

    protected void initWidgets(Component leftTree, Component topTable, Component bottomTabs, Component topToolbar, Component statusBar) {

        layout.initWidgets(leftTree, topTable, bottomTabs, topToolbar, statusBar);

        // layout is the main widget contained in the viewport
        add(layout);
    }

    public ManagerLinker getLinker() {
        return linker;
    }

    public BorderLayoutData getCenterData() {
        return layout.getCenterData();
    }

    public void setCenterData(BorderLayoutData centerData) {
        layout.setCenterData(centerData);
    }

    public BorderLayoutData getNorthData() {
        return layout.getNorthData();
    }

    public void setNorthData(BorderLayoutData northData) {
        layout.setNorthData(northData);
    }

    public BorderLayoutData getWestData() {
        return layout.getWestData();
    }

    public void setWestData(BorderLayoutData westData) {
        layout.setWestData(westData);
    }

}
