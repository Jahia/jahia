/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
