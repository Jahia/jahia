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
        if (gwtJahiaNode.get("alternativePath") != null) {
            openPath.removeAll((List<String>)gwtJahiaNode.get("alternativePath"));
        }
        Log.debug("Save Path on collapse " + openPath);
        gwtJahiaNode.setExpandOnLoad(false);
        savePaths();
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
            String openPaths = "";
            for (String s : openPath) {
                openPaths += s + "|";
            }
            storage.setItem("openPath-"+storageName, openPaths);
        }
    }


}
