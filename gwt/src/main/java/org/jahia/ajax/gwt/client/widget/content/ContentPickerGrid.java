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

package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Aug 21, 2009
 * Time: 9:58:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContentPickerGrid extends TabPanel {
    private TabItem m_component = new TabItem("Selected");
    private ListStore<GWTJahiaNode> store;
    private Grid<GWTJahiaNode> grid;
    private ManagerConfiguration config;
    private  String callback;
    private boolean readOnly= false;

    public ContentPickerGrid(final ManagerConfiguration config,final String callback) {
        this.config = config;
        this.callback = callback;
        createUI();
    }

    /**
     * Set content
     *
     * @param root
     */
    public void setContent(Object root) {
        if (root instanceof List) {
            setSelection((List<GWTJahiaNode>) root);
        } else {
            if (root instanceof GWTJahiaNode) {
                List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
                list.add((GWTJahiaNode) root);
                setSelection(list);
            }
        }
    }

    /**
     * Remove all categories
     */
    public void clearTable() {
        store.removeAll();
    }

    /**
     * Get selected categories
     *
     * @return
     */
    public Object getSelection() {
        return store.getModels();
    }

    public void setSelection(final List<GWTJahiaNode> selection) {

        if (selection != null && selection.size() > 0 && (selection.get(0).getNodeTypes().contains(config.getNodeTypes()) || selection.get(0).getInheritedNodeTypes().contains(config.getNodeTypes()))) {
            final String path = selection.get(0).getPath();
            JahiaContentManagementService.App.getInstance().isFileAccessibleForCurrentContainer(path, new AsyncCallback<Boolean>() {
                public void onFailure(Throwable throwable) {
                    Log.error("unable to check ACLs");
                }

                public void onSuccess(Boolean accessible) {
                    boolean doIt = true;
                    if (!accessible.booleanValue()) {
                        doIt = com.google.gwt.user.client.Window.confirm("The file may not be readable by everyone, resulting in a broken link.\nDo you wish to continue ?");
                    }
                    if (doIt) {
                        store.removeAll();
                        store.add(selection);
                        if (callback != null && callback.length() > 0) {
                            //nativeCallback(callback, selectedPath.getRawValue());
                        }
                    }
                }
            });

        }else{
            Log.error("********************* false"+config.getNodeTypes());
        }

    }

    /**
     * refresh
     */
    public void refresh() {
        Log.warn("Method refresh() no implemented");
    }

    /**
     * Get component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * Create UI
     */
    private void createUI() {

        setPlain(true);
        setHeight(300);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        //name
        ColumnConfig column = new ColumnConfig();
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                return "<input type=\"hidden\" name=\"content_id\" value=\"" + gwtJahiaNode.getUUID() + "\"/> " + gwtJahiaNode.getDisplayName();
            }
        });
        column.setId("name");
        column.setHeader(Messages.getResource("name"));
        configs.add(column);

        // displaName
        column = new ColumnConfig();
        column.setId("displayName");
        column.setHidden(true);
        column.setHeader(Messages.getResource("displayName"));
        configs.add(column);

        // path
        column = new ColumnConfig();
        column.setId("path");
        column.setHeader(Messages.getResource("path"));
        configs.add(column);

        // path
        column = new ColumnConfig();
        column.setId("remove");
        column.setHeader("");
        column.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
            public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                final Button pickContentButton = new Button("remove");
                pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent buttonEvent) {
                        store.remove(gwtJahiaNode);
                    }
                });
                return pickContentButton;

            }
        });
        configs.add(column);


        // list loader
        store = new ListStore<GWTJahiaNode>();
        final List<GWTJahiaNode> selectedContentNodes = ContentHelper.getSelectedContentNodesFromHTML();
        store.add(selectedContentNodes);
        store.setModelComparer(new ModelComparer<GWTJahiaNode>() {
            public boolean equals(GWTJahiaNode gwtJahiaNode, GWTJahiaNode gwtJahiaNode1) {
                String path = gwtJahiaNode.getPath();
                String path2 = gwtJahiaNode1.getPath();
                if (path == null && path2 == null) {
                    return true;
                } else if (path == null) {
                    return false;
                }
                return path.equalsIgnoreCase(path2);
            }
        });
        ColumnModel columnModel = new ColumnModel(configs);
        store.sort("name", Style.SortDir.ASC);

        // main component
        m_component.setLayout(new FitLayout());
        m_component.setBorders(false);

        // grid
        grid = new Grid<GWTJahiaNode>(store, columnModel);
        grid.setBorders(true);
        grid.getView().setForceFit(true);

        m_component.add(grid);
        add(m_component);
    }

    /**
     * Add categories to the UI
     *
     * @param gwtJahiaNodeList
     */
    public void addContentNode(List<GWTJahiaNode> gwtJahiaNodeList) {
        if (readOnly) {
            return;
        }
        if (gwtJahiaNodeList.size() > 0) {
            store.removeAll();
            store.add(gwtJahiaNodeList.get(0));
        } else {
            List<GWTJahiaNode> toAdd = new ArrayList<GWTJahiaNode>();
            for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodeList) {
                boolean add = true;
                for (GWTJahiaNode n : store.getModels()) {
                    if (gwtJahiaNode.getPath().equals(n.getPath())) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    toAdd.add(gwtJahiaNode);
                }
            }
            store.add(toAdd);
            store.sort("displayName", Style.SortDir.ASC);
        }
    }


    /**
     * Init context menu
     */
    public void initContextMenu() {
        Menu m = new Menu();
        MenuItem menuItem = new MenuItem(Messages.getResource("information"));
        m.add(menuItem);
        menuItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent event) {
                GWTJahiaNode node = grid.getSelectionModel().getSelectedItem();
                if (node != null) {
                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.OK);
                    box.setIcon(MessageBox.INFO);
                    box.setTitle(Messages.getResource("information") + ": " + node.getDisplayName());
                    box.setMessage(node.getPath().replace("/root", ""));
                    box.show();
                } else {
                    MessageBox box = new MessageBox();
                    box.setButtons(MessageBox.OK);
                    box.setIcon(MessageBox.WARNING);
                    box.setMessage("Please select a category");
                    box.show();
                }
            }
        });
        grid.setContextMenu(m);
    }

     public static native void nativeCallback(String callback, String path) /*-{
        try {
            eval('$wnd.' + callback)(path);
        } catch (e) {};
    }-*/;

}

