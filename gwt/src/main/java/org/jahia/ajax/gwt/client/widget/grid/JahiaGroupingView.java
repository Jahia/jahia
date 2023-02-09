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
