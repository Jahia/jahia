/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.TreeGridEvent;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: toto
* Date: 4/10/13
* Time: 17:19
* To change this template use File | Settings | File Templates.
*/
class SelectMainNodeTreeLoadListener extends LoadListener {
    protected transient Map<String,GWTJahiaNode> nodesByPath;

    protected TreeGrid<GWTJahiaNode> tree;
    protected String selectedPath = null;

    SelectMainNodeTreeLoadListener(final TreeGrid<GWTJahiaNode> tree) {

        nodesByPath = new HashMap<String, GWTJahiaNode>();
        this.tree = tree;

        tree.getTreeStore().getLoader().addLoadListener(new LoadListener() {
            @Override
            public void loaderLoad(LoadEvent le) {
                addRecursively((List<ModelData>) le.getData());
                if (selectedPath != null && tree.getSelectionModel().getSelectedItem() == null && nodesByPath.containsKey(selectedPath)) {
                    tree.getSelectionModel().setSelection(Arrays.asList(nodesByPath.get(selectedPath)));
                }
            }

            private void addRecursively(List<ModelData> data) {
                for (ModelData model : data) {
                    GWTJahiaNode node = (GWTJahiaNode) model;
                    nodesByPath.put(node.getPath(), node);
                    addRecursively(node.getChildren());
                }
            }
        });

        tree.addListener(Events.Expand, new Listener<TreeGridEvent>() {
            public void handleEvent(TreeGridEvent le) {
                if (selectedPath != null && tree.getSelectionModel().getSelectedItem() == null &&
                        nodesByPath.containsKey(selectedPath) && tree.getStore().contains(nodesByPath.get(selectedPath))) {
                    SelectMainNodeTreeLoadListener.this.tree.getSelectionModel().setSelection(Arrays.asList(nodesByPath.get(selectedPath)));
                }
            }
        });
    }

    public void handleNewMainSelection(String path) {
        if (tree != null && (tree.getSelectionModel().getSelectedItem() == null || !path.equals(
                tree.getSelectionModel().getSelectedItem().getPath()))) {
            selectedPath = path;
            if (nodesByPath.containsKey(path) && tree.getStore().contains(nodesByPath.get(path))) {
                tree.getSelectionModel().setSelection(Arrays.asList(nodesByPath.get(path)));
            } else {
                tree.getSelectionModel().deselectAll();
            }
        }
    }

}
