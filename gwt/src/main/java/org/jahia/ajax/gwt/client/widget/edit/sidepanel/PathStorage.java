package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathStorage {

    private Storage storage;
    private String storageName;

    private transient List<String> selectedPath = new ArrayList<String>();
    private transient List<String> openPath = new ArrayList<String>();

    public PathStorage(String storageName) {
        storage = Storage.getSessionStorageIfSupported();
        this.storageName = storageName;

        if (storage != null && storage.getItem("openPath-"+storageName) != null) {
            String openPaths = storage.getItem("openPath-"+storageName);
            openPath = new ArrayList<String>(Arrays.asList(openPaths.split("\\|")));
        }

    }

    public List<String> getSelectedPath() {
        return selectedPath;
    }

    public void setSelectedPath(String selectedPath) {
        this.selectedPath.add(selectedPath);
    }

    public void setSelectedPath(List<String> selectedPath) {
        this.selectedPath = selectedPath;
    }

    public List<String> getOpenPath() {
        return openPath;
    }

    public void setOpenPath(String openPath) {
        this.openPath.add(openPath);
    }

    public void setOpenPath(List<String> openPath) {
        this.openPath = openPath;
    }


    /**
     * init method()
     */
    public void addStorageListener(final TreePanel widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getItem();
                        expand(gwtJahiaNode);
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getItem();
                        collapse(gwtJahiaNode);
                    }
                });

                widget.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> selectionChangedEvent) {
                        if (selectionChangedEvent.getSelectedItem() != null) {
                            selectedPath = (Arrays.asList(selectionChangedEvent.getSelectedItem().getPath()));
                        }
                    }
                });
            }
        });
    }

    public void addStorageListener(final TreeGrid widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getModel();
                        expand(gwtJahiaNode);
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getModel();
                        collapse(gwtJahiaNode);
                    }
                });

                widget.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> selectionChangedEvent) {
                        if (selectionChangedEvent.getSelectedItem() != null) {
                            selectedPath = (Arrays.asList(selectionChangedEvent.getSelectedItem().getPath()));
                        }
                    }
                });
            }
        });
    }


    private void collapse(GWTJahiaNode gwtJahiaNode) {
        String path = gwtJahiaNode.getPath();
        openPath.remove(path);
        Log.debug("Save Path on collapse " + openPath);
        gwtJahiaNode.setExpandOnLoad(false);
        savePaths();
    }

    private void expand(GWTJahiaNode gwtJahiaNode) {
        String path = gwtJahiaNode.getPath();
        if (!openPath.contains(path)) {
            openPath.add(path);
        }
        Log.debug("Save Path on expand " + openPath);
        gwtJahiaNode.setExpandOnLoad(true);
        savePaths();
    }

    public void savePaths() {
        if (storage != null) {
            String openPaths = "";
            for (String s : openPath) {
                openPaths += s + "|";
            }
            storage.setItem("openPath-"+storageName, openPaths);
        }
    }


}
