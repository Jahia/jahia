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

import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 *
 *
 * User: toto
 * Date: Oct 28, 2008 - 2:32:51 PM
 */
public class TreeOpener {

    private Tree tree;
    private String idProperty ;

    private TreeItem currentSelection;
    private String[] pathes;
    private int lev;
    private Listener listener;

    public TreeOpener(Tree tree, String idProperty, String path) {
        this.tree = tree;
        this.idProperty = idProperty ;

        Log.debug("path="+path);
        pathes = path.split("/");
        Log.debug("length = "+path.length());
        for (String pathe : pathes) {
            Log.debug("p = "+pathe);
        }
        currentSelection = tree.getRootItem();
        tree.getRootItem().setExpanded(true);
        lev=1;

        listener = new Listener() {
            public void handleEvent(BaseEvent event) {
                searchNextItem();
            }
        };
        tree.addListener(Events.Expand, listener);

        searchNextItem();
    }

    private void searchNextItem() {
        if (pathes.length > lev) {
            Log.debug("look for :"+pathes[lev]);

            List<TreeItem> children = currentSelection.getItems() ;
            for (TreeItem item: children) {
                String prop = (String) item.getModel().get(idProperty) ;
                if (prop.equals(pathes[lev])) {
                    expand(item) ;
                    return;
                }
            }
        } else {
            Log.debug("TreeOpener out of bounds");
        }
    }

    private void expand(TreeItem target) {
        currentSelection = target;
        lev ++;
        if (lev == pathes.length) {
            tree.setSelectedItem(target);
            tree.removeListener(Events.Expand, listener);
            return;
        }
        if (!target.isExpanded()) {
            target.setExpanded(true);
        } else {
            Log.debug("expanded");
            searchNextItem();
        }
    }

}
