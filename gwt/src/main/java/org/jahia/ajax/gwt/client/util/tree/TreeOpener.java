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
