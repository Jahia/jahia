/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
 * User: toto
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
        selectedPath = path;
        if (tree != null && (tree.getSelectionModel().getSelectedItem() == null || !path.equals(
                tree.getSelectionModel().getSelectedItem().getPath()))) {
            if (nodesByPath.containsKey(path) && tree.getStore().contains(nodesByPath.get(path))) {
                tree.getSelectionModel().setSelection(Arrays.asList(nodesByPath.get(path)));
            } else {
                tree.getSelectionModel().deselectAll();
            }
        }
    }

}
