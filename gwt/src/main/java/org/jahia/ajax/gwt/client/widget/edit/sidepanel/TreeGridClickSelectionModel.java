package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.aria.FocusFrame;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
* Created by IntelliJ IDEA.
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

    @Override protected void handleMouseDown(GridEvent<GWTJahiaNode> e) {
        if (!e.isRightClick()) {
            super.handleMouseDown(e);           
        } else {
            if (!tree.getTreeView().isSelectableTarget(e.getModel(), e.getTarget())) {
                return;
            }
            if (selectionMode != Style.SelectionMode.SINGLE && isSelected(listStore.getAt(e.getRowIndex()))) {
                return;
            }
            if (e.isRightClick()) {
                rightClickSelectionModel.select(e.getModel(), false);
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
