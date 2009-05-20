/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.tree;

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
