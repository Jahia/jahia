/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.widget.grid;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

/**
 * Custom grouping view that sets dedicated classes for grid and grouping view context menu items.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaGroupingView extends GroupingView {

    @Override
    protected Menu createContextMenu(int colIndex) {
        Menu menu = super.createContextMenu(colIndex);
        if (menu != null) {
            for (Component component : menu.getItems()) {
                if (component instanceof MenuItem) {
                    String label = ((MenuItem) component).getHtml();
                    if (label != null) {
                        String style = null;
                        if (GXT.MESSAGES.gridView_sortAscText().equals(label)) {
                            style = "grid-view-sort-asc";
                        } else if (GXT.MESSAGES.gridView_sortDescText().equals(label)) {
                            style = "grid-view-sort-desc";
                        } else if (GXT.MESSAGES.gridView_columnsText().equals(label)) {
                            style = "grid-view-columns";
                        } else if (GXT.MESSAGES.groupingView_groupByText().equals(label)) {
                            style = "grouping-view-group-by";
                        } else if (GXT.MESSAGES.groupingView_showGroupsText().equals(label)) {
                            style = "grouping-view-show-groups";
                        }
                        if (style != null) {
                            component.addStyleName("toolbar-item-" + style);
                        }
                    }
                }
            }
        }

        return menu;
    }
}
