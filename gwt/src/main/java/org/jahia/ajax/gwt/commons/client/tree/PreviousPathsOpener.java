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

package org.jahia.ajax.gwt.commons.client.tree;

import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: rfelden
 * Date: 19 nov. 2008 - 14:51:50
 */
public class PreviousPathsOpener<T extends TreeModel<T>> {

    private Tree m_tree ;
    private TreeStore<T> store ;
    private CustomTreeBinder<T> binder ;

    public PreviousPathsOpener(Tree t, TreeStore<T> s, CustomTreeBinder<T> b) {
        m_tree = t ;
        store = s ;
        binder = b ;
    }

    public void expandPreviousPaths() {
        List<T> nodes = new ArrayList<T>() ;
        for (T aNode: store.getAllItems()) {
            nodes.add(aNode) ;
        }
        // check if sublevels are available
        for (T root : nodes) {
            if (root.getChildren().size() > 0) {
                for (T node: nodes) {
                    appendChildrenNodesToStore(node);
                }
                for (T node: nodes) {
                    renderChildrenNodesRec(node);
                }
                expandAllExistingChildrenRec(m_tree.getRootItem());
            }
        }
        binder.setCaching(false);
    }

    private void appendChildrenNodesToStore(T node) {
        Log.debug("Appending children of " + node.get("name")) ;
        List<T> nodes = node.getChildren() ;
        store.add(node, nodes, true) ;
    }

    private void renderChildrenNodesRec(T node) {
        Log.debug("Rendering children of " + node.get("name")) ;
        List<T> nodes = node.getChildren() ;
        if (nodes.size() > 0) {
            binder.renderChildren(node, nodes) ;
            for (T aNode: nodes) {
                renderChildrenNodesRec(aNode);
            }
        }
    }

    private void expandAllExistingChildrenRec(TreeItem item) {
        if (item != null) {
            Log.debug("Expanding children of " + item.getText()) ;
            for (TreeItem it: item.getItems()) {
                if (it.getItemCount() > 0 && !it.isExpanded()) {
                    it.setExpanded(true);
                    expandAllExistingChildrenRec(it);
                }
            }
        }
    }

}
