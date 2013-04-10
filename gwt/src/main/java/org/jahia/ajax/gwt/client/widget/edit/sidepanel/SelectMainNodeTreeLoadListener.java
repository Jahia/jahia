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
