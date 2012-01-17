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
