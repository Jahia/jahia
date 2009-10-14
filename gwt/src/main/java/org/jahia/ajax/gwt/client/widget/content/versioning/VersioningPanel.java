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
 **/

package org.jahia.ajax.gwt.client.widget.content.versioning;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import java.util.List;
import java.util.ArrayList;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.content.ImagePopup;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jul 7, 2009
 * Time: 12:02:39 PM
 */
public class VersioningPanel extends ContentPanel {
    private GWTJahiaNode selectedNode;
    private boolean enableDoubleClick;
    private Button restoreVersion;

    public VersioningPanel(GWTJahiaNode selectedNode, boolean enableDoubleClick) {
        this.selectedNode = selectedNode;
        this.enableDoubleClick = enableDoubleClick;
        render();
    }

    /**
     * Render panel
     */
    public void render() {
        // ToDo : Add resource-bundles in versioning panel
        setLayout(new FitLayout());
        setCollapsible(false);
        setFrame(false);
        setAnimCollapse(false);
        setBorders(false);
        setBodyBorder(false);
        setHeaderVisible(false);

        // grid that displays all versions
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("versionNumber", "version number", 100);
        columns.add(col);
        col = new ColumnConfig("date", "date", 200);
        columns.add(col);
        col = new ColumnConfig("author", "author", 200);
        columns.add(col);

        final ListStore<GWTJahiaNodeVersion> store = new ListStore<GWTJahiaNodeVersion>();

        ColumnModel cm = new ColumnModel(columns);
        final Grid<GWTJahiaNodeVersion> nodeVersionGrid = new Grid(store, cm);

        // main version panel
        restoreVersion = new Button(Messages.getResource(("ae_restore")));
        restoreVersion.setIconStyle("gwt-icons-restore");
        restoreVersion.setEnabled(true);
        restoreVersion.setToolTip("Restore selected version");
        restoreVersion.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // get selected version
                List<GWTJahiaNodeVersion> sel = nodeVersionGrid.getSelectionModel().getSelectedItems();
                if (sel != null && sel.size() == 1) {
                    if (selectedNode.isLocked()) {
                        MessageBox.alert("Alert", "Selected node is locked.", null);
                    } else {
                        GWTJahiaNodeVersion gwtJahiaNodeVersion = sel.get(0);
                        JahiaContentManagementService.App.getInstance().restoreNode(gwtJahiaNodeVersion,new AsyncCallback() {
                            public void onSuccess(Object o) {
                                // refresh on restore
                                Info.display("Restore",getSelectedNode().getDisplayName()+ "has been restored sucessfully");
                                afterRestore();
                            }

                            public void onFailure(Throwable throwable) {
                                MessageBox.alert("Alert", "Unable to restore selected version", null);
                                Log.error("Unbale to restore selected version",throwable);
                            }
                        });
                    }
                    // restore
                } else {
                    MessageBox.alert("Alert", "Please select a version.", null);
                }

            }
        });

        // toolbar
        ToolBar toolbar = new ToolBar();
        toolbar.add(new FillToolItem());
        toolbar.add(restoreVersion);
        setTopComponent(toolbar);
        add(nodeVersionGrid);


        // a double click represents display a preview of the node or lauch the download
        nodeVersionGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                List<GWTJahiaNodeVersion> sel = nodeVersionGrid.getSelectionModel().getSelectedItems();
                if (sel != null && sel.size() == 1) {
                    GWTJahiaNodeVersion el = sel.get(0);
                    if (enableDoubleClick) {
                        onRowDoubleClick(el);
                    }
                }
            }
        });

        // load all versions of the selected node
        JahiaContentManagementService.App.getInstance().getVersions(selectedNode.getPath(), new AsyncCallback<List<GWTJahiaNodeVersion>>() {
            public void onFailure(Throwable caught) {
                 MessageBox.alert("Error", "Unable to load versions of the selected node.", null);
            }

            public void onSuccess(List<GWTJahiaNodeVersion> result) {
                store.add(result);
            }
        });
    }

    /**
     * Called when a row is double clicked. This method can be overriden
     * @param version
     */
    public void onRowDoubleClick(GWTJahiaNodeVersion version) {
        if (getSelectedNode().isDisplayable()) {
            ImagePopup.popImage(version.getNode());
        }
    }

    /**
     * Called after if the restore of a version succeed
     */
    public void afterRestore(){
        // override this methode
    }

    /**
     * Get the selected node
     * @return
     */
    public GWTJahiaNode getSelectedNode() {
        return selectedNode;
    }

    /**
     * Set the selected node
     * @param selectedNode
     */
    public void setSelectedNode(GWTJahiaNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}
