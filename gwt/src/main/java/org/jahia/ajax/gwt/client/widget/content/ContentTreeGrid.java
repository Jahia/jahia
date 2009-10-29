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
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridSelectionModel;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TreeTable file picker for use within classic engines.
 */
public class ContentTreeGrid extends ContentPanel {
    private ManagerConfiguration configuration;
    private  ManagerLinker linker;
    protected ActionToolbarLayoutContainer toolbars;
    protected TreeLoader<GWTJahiaNode> loader;
    protected TreeGrid<GWTJahiaNode> m_treeGrid;
    protected TreeTableStore<GWTJahiaNode> store;
    private String rootPath;
    private boolean multiple;

    protected boolean init = true;

    protected String selectPathAfterUpload = null;


    /**
     * Content tree table
     *
     * @param rootPath
     * @param selectedNodes
     * @param multiple
     * @param configuration
     */
    public ContentTreeGrid(String rootPath, List<GWTJahiaNode> selectedNodes, boolean multiple, ManagerConfiguration configuration) {
        this.rootPath = rootPath != null && rootPath.length() > 0 ? rootPath : null;
        this.multiple = multiple;
        this.configuration = configuration;

        setLayout(new FitLayout());
        setHeaderVisible(false);
        setBorders(false);
        setBodyBorder(false);

        // add toolbar
        toolbars = new ActionToolbarLayoutContainer(configuration.getToolbarGroup());
        toolbars.init();
        setTopComponent(toolbars);

        // create loader
        loader = createTreeLoader();

        // tree store
        store = new TreeTableStore<GWTJahiaNode>(loader);

        m_treeGrid = new TreeGrid<GWTJahiaNode>(store, getHeaders());
        m_treeGrid.getTreeView().setRowHeight(25);
        //m_treeGrid.getTreeView().setForceFit(true);
        m_treeGrid.setIconProvider(ContentModelIconProvider.getInstance());
        m_treeGrid.setBorders(false);
        add(m_treeGrid);
    }

    /**
     * Create tree loader
     *
     * @return
     */
    private TreeLoader<GWTJahiaNode> createTreeLoader() {
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();

        // data proxy
        RpcProxy<List<GWTJahiaNode>> proxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    if (rootPath != null) {
                        service.getRoot(rootPath, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, listAsyncCallback);
                    } else {
                        service.getRoot(JCRClientUtils.GLOBAL_REPOSITORY, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, listAsyncCallback);
                    }
                    init = false;
                } else {
                    service.ls(JCRClientUtils.GLOBAL_REPOSITORY, (GWTJahiaNode) gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, !configuration.isAllowCollections(), listAsyncCallback);
                }
            }
        };


        TreeLoader<GWTJahiaNode> loader = new BaseTreeLoader<GWTJahiaNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasChildren();
            }

            public void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false");
                    init = false;
                }
            }
        };

        return loader;
    }


    public void setSelectPathAfterDataUpdate(String path) {
        if (path != null) {
            Log.debug("set path to select : " + path);
        } else {
            Log.debug("set path to select : null");
        }
        selectPathAfterUpload = path;
    }

    public void initWithLinker(ManagerLinker linker) {
        this.linker = linker;
        toolbars.initWithLinker(linker);
    }

    public void initContextMenu(ManagerLinker linker) {
        m_treeGrid.setContextMenu(new ContentListContextMenu(linker, configuration));
    }

    public void handleNewLinkerSelection() {
        // TODo: getSelection should alwas return a list of GWTJahiaNode
        toolbars.handleNewLinkerSelection();

    }

    public void setContent(Object root) {
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        List<GWTJahiaNode> elts = m_treeGrid.getSelectionModel().getSelection();
        if (elts != null && elts.size() > 0) {
            return elts;
        } else {
            return null;
        }
    }

    public void refresh() {
        Log.debug("reloading entire tree");
        loader.load(null);
    }


    public void clearSelection() {
        m_treeGrid.getSelectionModel().deselectAll();
    }

    /**
     * Get header from configuration
     *
     * @return
     */
    private ColumnModel getHeaders() {
        List<ColumnConfig> headerList = new ArrayList<ColumnConfig>();
        List<String> columnIds = configuration.getTableColumns();
        if (columnIds == null || columnIds.size() == 0) {
            columnIds.add("name");
            columnIds.add("size");
            columnIds.add("date");
            columnIds.add("version");
            columnIds.add("picker");
        }
        for (String s1 : columnIds) {
            if (s1.equals("name")) {
                ColumnConfig col = new ColumnConfig("displayName", Messages.getResource("fm_column_name"), 300);
                col.setRenderer(new TreeGridCellRenderer<GWTJahiaNode>());
                headerList.add(col);
            } else if (s1.equals("size")) {
                ColumnConfig col = new ColumnConfig("size", Messages.getResource("fm_column_size"), 70);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        if (gwtJahiaNode != null && gwtJahiaNode.getSize() != null) {
                            long size = gwtJahiaNode.getSize().longValue();
                            return Formatter.getFormattedSize(size);
                        } else {
                            return "-";
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("date")) {
                ColumnConfig col = new ColumnConfig("date", Messages.getResource("fm_column_date"), 80);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        Date d = gwtJahiaNode.getLastModified();
                        if (d != null) {
                            return DateTimeFormat.getFormat("d/MM/y").format(d).toString();
                        } else {
                            return "-";
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("version")) {
                ColumnConfig col = new ColumnConfig("version", Messages.getResource("versioning_versionLabel"), 250);
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        List<GWTJahiaNodeVersion> versions = gwtJahiaNode.getVersions();
                        if (versions != null) {
                            SimpleComboBox<String> combo = new SimpleComboBox<String>();
                            combo.setForceSelection(true);
                            combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                            for (GWTJahiaNodeVersion version : versions) {
                                combo.add(version.getVersionNumber() + " (" + DateTimeFormat.getFormat("d/MM/y hh:mm").format(version.getDate()).toString() + ")");
                            }
                            final String s2 = "Always Latest Version";
                            combo.add(s2);
                            combo.setSimpleValue(s2);
                            combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                                @Override
                                public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> simpleComboValueSelectionChangedEvent) {
                                    SimpleComboValue<String> value = simpleComboValueSelectionChangedEvent.getSelectedItem();
                                    String value1 = value.getValue();
                                    if (!s2.equals(value1))
                                        gwtJahiaNode.setSelectedVersion(value1.split("\\(")[0].trim());
                                }
                            });
                            combo.setDeferHeight(true);
                            return combo;
                        } else {
                            SimpleComboBox<String> combo = new SimpleComboBox<String>();
                            combo.setForceSelection(false);
                            combo.setTriggerAction(ComboBox.TriggerAction.ALL);
                            combo.add("No version");
                            combo.setSimpleValue("No version");
                            combo.setEnabled(false);
                            combo.setDeferHeight(true);
                            return combo;
                        }
                    }
                });
                headerList.add(col);
            } else if (s1.equals("picker")) {
                ColumnConfig col = new ColumnConfig("action", "action", 100);

                col.setAlignment(Style.HorizontalAlignment.RIGHT);
                col.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                    public Object render(final GWTJahiaNode gwtJahiaNode, String s, ColumnData columnData, int i, int i1, ListStore<GWTJahiaNode> gwtJahiaNodeListStore, Grid<GWTJahiaNode> gwtJahiaNodeGrid) {
                        if (isSelectable(gwtJahiaNode)) {
                            final Button pickContentButton = new Button("Add");
                            if(!multiple){
                                pickContentButton.setText("Select");
                            }
                            pickContentButton.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
                            pickContentButton.setEnabled(true);
                            pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                public void componentSelected(ButtonEvent buttonEvent) {
                                    onContentPicked(gwtJahiaNode);
                                }
                            });
                            return pickContentButton;
                        } else {
                            return new Text("");
                        }
                    }

                    /**
                     * Return true id the node is selectable
                     * @param gwtJahiaNode
                     * @return
                     */
                    private boolean isSelectable(GWTJahiaNode gwtJahiaNode) {
                        if(gwtJahiaNode.isFile()){
                            // Do nothing here: file are filtered at server part
                            return true;
                        }
                        // check if node type is selectable or not.
                        return gwtJahiaNode.getNodeTypes().contains(configuration.getNodeTypes()) || gwtJahiaNode.getInheritedNodeTypes().contains(configuration.getNodeTypes());
                    }
                });
                col.setFixed(true);
                headerList.add(col);
            }
        }
        return new ColumnModel(headerList);
    }

    /**
     * Override thi method to customize "add" button behaviour
     * @param gwtJahiaNode
     */
    public void onContentPicked(final GWTJahiaNode gwtJahiaNode){

    }


    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class TreeTableStore<M extends ModelData> extends TreeStore<M> {
        public TreeTableStore(TreeLoader<M> loader) {
            super(loader);
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
        }

        /**
         * This allows selection after tree items have been loaded (asynchronous call is 'blocking' here)
         */
        protected void onLoad(TreeLoadEvent e) {
            super.onLoad(e);           
        }
    }

    protected String getRootPath() {
        return rootPath;
    }


}
