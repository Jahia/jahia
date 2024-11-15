/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
                        } else {
                            selectedPath = new ArrayList<String>();
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
                        } else {
                            selectedPath = new ArrayList<String>();
                        }
                    }
                });
            }
        });
    }


    private void collapse(GWTJahiaNode gwtJahiaNode) {
        String path = gwtJahiaNode.getPath();
        collapsePath(path);
        if (gwtJahiaNode.get("alternativePath") != null) {
            for (String alternativePath : (List<String>) gwtJahiaNode.get("alternativePath")) {
                collapsePath(alternativePath);
            }
            openPath.removeAll((List<String>)gwtJahiaNode.get("alternativePath"));
        }
        Log.debug("Save Path on collapse " + openPath);
        gwtJahiaNode.setExpandOnLoad(false);
        savePaths();
    }

    private void collapsePath(String path) {
        openPath.remove(path);
        for (String subPath : new ArrayList<String>(openPath)) {
            if (subPath.startsWith(path+"/")) {
                openPath.remove(subPath);
            }
        }
    }

    private void expand(GWTJahiaNode gwtJahiaNode) {
        String path = gwtJahiaNode.getPath();
        if (!openPath.contains(path)) {
            openPath.add(path);
        }
        if (gwtJahiaNode.get("alternativePath") != null) {
            openPath.addAll((List<String>)gwtJahiaNode.get("alternativePath"));
        }

        Log.debug("Save Path on expand " + openPath);
        gwtJahiaNode.setExpandOnLoad(true);
        savePaths();
    }

    public void savePaths() {
        if (storage != null) {
            StringBuilder openPaths = new StringBuilder();
            for (String s : openPath) {
                openPaths.append(s).append("|");
            }
            storage.setItem("openPath-"+storageName, openPaths.toString());
        }
    }


}
