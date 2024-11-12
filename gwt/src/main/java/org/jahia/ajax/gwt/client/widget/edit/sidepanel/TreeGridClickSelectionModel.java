/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.List;

/**
 *
 * User: toto
 * Date: Oct 27, 2010
 * Time: 2:09:55 PM
 *
 */
class TreeGridClickSelectionModel extends TreeGridSelectionModel<GWTJahiaNode> {
    private TreeGridSelectionModel<GWTJahiaNode> rightClickSelectionModel;

    TreeGridClickSelectionModel() {
        rightClickSelectionModel = new TreeGridSelectionModel<GWTJahiaNode>() {
            protected void onSelectChange(GWTJahiaNode model, boolean select) {
            }

            protected void setLastFocused(GWTJahiaNode lastFocused) {
            }
        };
        rightClickSelectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
    }

    @Override
    protected void handleMouseDown(GridEvent<GWTJahiaNode> e) {
        if (!e.isRightClick()) {
            super.handleMouseDown(e);
        } else {
            if (!MainModule.isGlobalSelectionDisabled()) {
                if (selectionMode != Style.SelectionMode.SINGLE && isSelected(listStore.getAt(e.getRowIndex()))) {
                    return;
                }
                if (e.isRightClick()) {
                    rightClickSelectionModel.select(e.getModel(), false);
                }
            }
        }
    }

    @Override protected void handleMouseClick(GridEvent<GWTJahiaNode> e) {
        if (!e.isRightClick()) {
            super.handleMouseClick(e);
        }
    }

    public TreeGridSelectionModel<GWTJahiaNode> getRightClickSelectionModel() {
        return rightClickSelectionModel;
    }

    @Override protected void setLastFocused(GWTJahiaNode lastFocused) {
    }

    @Override public void bindGrid(Grid grid) {
        super.bindGrid(grid);
        rightClickSelectionModel.bind(grid.getStore());
    }

    @Override public void select(int start, int end, boolean keepExisting) {
        super.select(start, end, keepExisting);
        rightClickSelectionModel.select(start, end, keepExisting);
    }

    @Override public void select(List<GWTJahiaNode> items, boolean keepExisting) {
        super.select(items, keepExisting);
        rightClickSelectionModel.select(items, keepExisting);
    }

    @Override public void refresh() {
        super.refresh();
        rightClickSelectionModel.refresh();
    }
}
