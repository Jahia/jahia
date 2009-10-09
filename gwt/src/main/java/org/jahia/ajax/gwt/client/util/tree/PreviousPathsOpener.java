/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.tree;

import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * User: rfelden
 * Date: 19 nov. 2008 - 14:51:50
 */
public class PreviousPathsOpener<T extends ModelData> {

    // TODO GXT 2

    private TreePanel m_tree;
    private TreeStore<T> store;

    public PreviousPathsOpener(TreePanel t, TreeStore<T> s) {
        m_tree = t;
        store = s;
    }

    public void expandPreviousPaths() {
        List<T> nodes = new ArrayList<T>();
        for (T aNode : store.getAllItems()) {
            nodes.add(aNode);
        }
        // check if sublevels are available
        for (T root : nodes) {
            if (store.getChildren(root).size() > 0) {
                for (T node : nodes) {
                    appendChildrenNodesToStore(node);
                }
                for (T node : nodes) {
                    renderChildrenNodesRec(node);
                }
                expandAllExistingChildrenRec(store.getRootItems());
            }
        }
    }


    public void expandPreviousPaths2() {
      /**  List<GWTJahiaNode> rootNodes = (List<GWTJahiaNode>) store.getRootItems();
        Log.debug("Register children " + rootNodes.size());
        for (GWTJahiaNode rootNode : rootNodes) {
            registerLoadedChildren(rootNode);
        }  **/

       /*DeferredCommand.addCommand(new Command() {
            public void execute() {
                Log.debug("deferred command called");
                m_tree.expandAll();

              //  expandAll(nodes);

            }
        });*/
    }

    private void expandAll(List<T> nodes) {
        for (T child : nodes) {
            List<T> subNodes = store.getChildren(child);
            Log.debug("******** nb of subNodes: "+subNodes.size());
            m_tree.setExpanded(child, true, true);
        }
    }

    private void registerLoadedChildren(GWTJahiaNode parentNode) {
        List<ModelData> rootNodesChilden = parentNode.getChildren();
        Log.debug("Register children " + rootNodesChilden.size() + " of " + parentNode.getPath());
        appendChildrenNodesToStore((T)parentNode);
        for (ModelData node : rootNodesChilden) {
            registerLoadedChildren((GWTJahiaNode) node);
        }
    }


    private void appendChildrenNodesToStore(T node) {
        Log.debug("Appending children of " + node.get("name"));
        List<T> nodes = store.getChildren(node);
        store.add(node, nodes, true);
    }

    private void renderChildrenNodesRec(T node) {
        Log.debug("Rendering children of " + node.get("name"));
        List<T> nodes = store.getChildren(node);
        if (nodes.size() > 0) {
            List<T> l = new ArrayList<T>();
            for (ModelData aNode : nodes) {
                l.add((T) aNode);
            }
            for (ModelData aNode : nodes) {
                renderChildrenNodesRec((T) aNode);
            }
        }
    }

    private void expandAllExistingChildrenRec(List<T> items) {
        if (items != null) {
            for (T item : items) {
                if (store.getChildCount(item) > 0 && !m_tree.isExpanded(item)) {
                    m_tree.setExpanded(item, true);
                    expandAllExistingChildrenRec(store.getChildren(item));
                }
            }
        }
    }

}
