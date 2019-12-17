/*
 * Sencha GXT 2.3.1a - Sencha for GWT
 * Copyright(c) 2007-2013, Sencha, Inc.
 * licensing@sencha.com
 *
 * http://www.sencha.com/products/gxt/license/
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
