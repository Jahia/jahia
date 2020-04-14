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
package com.extjs.gxt.ui.client.widget.treegrid;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.Joint;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A <code>GridCellRenderer</code> that renders a tree into it's column.
 * Renderer may be used in any column, not just the first.
 */
public class TreeGridCellRenderer<M extends ModelData> implements GridCellRenderer<M> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object render(M model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<M> store,
                         Grid<M> grid) {
        config.css = "x-treegrid-column";

        assert grid instanceof TreeGrid : "TreeGridCellRenderer can only be used in a TreeGrid";

        TreeGrid tree = (TreeGrid) grid;
        TreeStore ts = tree.getTreeStore();

        int level = ts.getDepth(model);

        String id = getId(tree, model, property, rowIndex, colIndex);
        String text = getText(tree, model, property, rowIndex, colIndex);
        AbstractImagePrototype icon = calculateIconStyle(tree, model, property, rowIndex, colIndex);
        Joint j = calcualteJoint(tree, model, property, rowIndex, colIndex);

        return tree.getTreeView().getTemplate(model, id, text, icon, false, j, level - 1);
    }

    protected Joint calcualteJoint(TreeGrid<M> grid, M model, String property, int rowIndex, int colIndex) {
        return grid.calcualteJoint(model);
    }

    protected AbstractImagePrototype calculateIconStyle(TreeGrid<M> grid, M model, String property, int rowIndex,
                                                        int colIndex) {
        return grid.calculateIconStyle(model);
    }

    protected String getId(TreeGrid<M> grid, M model, String property, int rowIndex, int colIndex) {
        return grid.findNode(model).id;
    }

    protected String getText(TreeGrid<M> grid, M model, String property, int rowIndex, int colIndex) {
        // Jahia GWT 2.8 fix
        // return String.valueOf(model.get(property));
        return model.get(property);
    }
}
