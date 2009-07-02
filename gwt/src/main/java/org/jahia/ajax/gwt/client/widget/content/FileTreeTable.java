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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TreeTableBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;
import com.extjs.gxt.ui.client.widget.table.DateTimeCellRenderer;
import com.extjs.gxt.ui.client.widget.treetable.TreeTable;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumn;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumnModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.service.content.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.content.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeBinder;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

/**
 * TreeTable file picker for use within classic engines.
 */
public class FileTreeTable extends TopRightComponent {

    private ManagerConfiguration configuration ;

    protected ContentPanel m_component ;
    protected TreeLoader<GWTJahiaNode> loader ;
    protected TreeTable m_treeTable ;
    protected TreeTableStore<GWTJahiaNode> store ;
    protected MyTreeTableBinder<GWTJahiaNode> binder ;

    private PreviousPathsOpener<GWTJahiaNode> previousPathsOpener = null ;
    private Listener<TreeEvent> tempListener = null ;
    
    private String rootPath;

    protected boolean init = true ;

    protected String selectPathAfterUpload = null ;

    protected TreeLoader<GWTJahiaNode> getTreeLoader(final String startPath) {
        final JahiaNodeServiceAsync service = JahiaNodeService.App.getInstance() ;

        // data proxy
        RpcProxy<GWTJahiaNode, List<GWTJahiaNode>> proxy = new RpcProxy<GWTJahiaNode, List<GWTJahiaNode>>() {
            @Override
            protected void load(GWTJahiaNode gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    if (rootPath != null) {
                        service.getRoot(rootPath, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), startPath, listAsyncCallback);
                    } else {
                        service.getRoot(JCRClientUtils.GLOBAL_REPOSITORY, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), startPath, listAsyncCallback);
                    }
                    init = false ;
                } else {
                    service.ls(gwtJahiaFolder, configuration.getNodeTypes(), configuration.getMimeTypes(), configuration.getFilters(), null, false, listAsyncCallback);
                }
            }
        };

        // tree loader
        return new CustomTreeLoader<GWTJahiaNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasChildren() ;
            }

            @Override
            protected void onLoadSuccess(GWTJahiaNode parent, List<GWTJahiaNode> children) {
                super.onLoadSuccess(parent, children);
                for (GWTJahiaNode n: children) {
                    n.setParent(parent);
                }
                if (selectPathAfterUpload != null) {
                    selectPath(selectPathAfterUpload);
                    ((FilePickerContainer) getLinker().getTopRightObject()).handleNewSelection();
                    setSelectPathAfterDataUpdate(null);
                }
            }

            protected void expandPreviousPaths() {
                expandAllPreviousPaths(startPath);
            }
        };
    }
    
    public FileTreeTable(String rootPath, String startPath, ManagerConfiguration config) {
        this.rootPath = rootPath != null && rootPath.length() > 0 ? rootPath : null;
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setHeaderVisible(false);
        m_component.setBorders(false);
        m_component.setBodyBorder(false);
        loader = getTreeLoader(startPath);

        // tree store
        store = new TreeTableStore<GWTJahiaNode>(loader) ;

        configuration = config ;

        String columns = null ;
        if (config.getTableColumns().size() > 0) {
            StringBuilder cols = new StringBuilder(config.getTableColumns().get(0)) ;
            for (int i=1; i<config.getTableColumns().size(); i++) {
                cols.append(",").append(config.getTableColumns().get(i)) ;
            }
            columns = cols.toString() ;
        }
        m_treeTable = new TreeTable(getHeaders(columns));
        m_treeTable.setBorders(false);
        m_treeTable.setHorizontalScroll(true);
        m_treeTable.setAnimate(false);
        m_treeTable.getStyle().setLeafIconStyle("tree-folder");

        binder = new MyTreeTableBinder<GWTJahiaNode>(m_treeTable, store) ;
        binder.init() ;
        binder.setCaching(false);
        binder.setDisplayProperty("displayName");
        binder.setIconProvider(new ModelStringProvider<GWTJahiaNode>() {
            public String getStringValue(GWTJahiaNode modelData, String s) {
                return modelData.getExt() ;
            }
        });

        binder.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                getLinker().onTableItemSelected();
                ((FilePickerContainer) getLinker().getTopRightObject()).handleNewSelection();
            }
        });

        m_component.add(m_treeTable) ;
    }

    protected void expandAllPreviousPaths(String path) {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaNode>(m_treeTable, store, binder) ;
        }
        previousPathsOpener.expandPreviousPaths();
        selectPath(path);
    }

    public void setSelectPathAfterDataUpdate(String path) {
        if (path != null) {
            Log.debug("set path to select : " + path) ;
        } else {
            Log.debug("set path to select : null") ;
        }
        selectPathAfterUpload = path ;
    }

    private void selectPath(String path) {
        if (path != null && path.length() > 0) {
            Log.debug("selecting path : " + path) ;
            String[] pathToSelection = path.split("/") ;
            TreeItem item = m_treeTable.getRootItem() ;
            boolean selected = false ;
            StringBuilder itemNameBuf = new StringBuilder() ;
            for (int j=0; j<pathToSelection.length && !selected; j++) {
                itemNameBuf.append(pathToSelection[j]) ;
                if (j < pathToSelection.length-1) {
                    itemNameBuf.append("/") ;
                }
                Log.debug("searching for path component : " + itemNameBuf.toString()) ;
                List<TreeItem> subItems = item.getItems() ;
                for (int i=0; i<subItems.size(); i++) {
                    TreeItem subItem = subItems.get(i) ;
                    String soughtPath = (String) subItem.getModel().get("path") ;
                    Log.debug(i + ") checking " + soughtPath + " against " + itemNameBuf.toString()) ;
                    if (itemNameBuf.toString().contains(soughtPath)) {
                        if (j == pathToSelection.length-1) {
                            Log.debug("setting selection : " + subItem.getText()) ;
                            m_treeTable.setSelectedItem(subItem);
                            selected = true ;
                        }
                        item = subItem ;
                        break ;
                    }
                }
            }
        }
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load() ;
    }

    public void initContextMenu() {
         m_treeTable.setContextMenu(new FileListContextMenu(getLinker(), configuration));
    }

    public void setContent(Object root) {
    }

    public void clearTable() {
        store.removeAll();
    }

    public Object getSelection() {
        List<GWTJahiaNode> elts = binder.getSelection() ;
        if (elts != null && elts.size()>0) {
            return elts ;
        } else {
            return null ;
        }
    }

    public void refresh() {
        List<GWTJahiaNode> selection = (List<GWTJahiaNode>) getSelection() ;
        if (selection != null) {
            GWTJahiaNode select = selection.get(0) ;
            if (!select.isFile()) {
                Log.debug("direct selection not null, refreshing from " + select.getName()) ;
                TreeItem item = (TreeItem) binder.findItem(select) ;
                if (item.isExpanded()) {
                    Log.debug("loading children of " + select.getName() + " with " + item.getText()) ;
                    loader.loadChildren(select) ;
                } else { // todo messy part, item not expanded won't be expanded after loadChildren (bug ?)
                    if (selectPathAfterUpload != null) {
                        final String tempPath = selectPathAfterUpload ;
                        setSelectPathAfterDataUpdate(null);
                        if (item.isLeaf()) {
                            item.setLeaf(false);
                            item.setExpanded(true);
                            setSelectPathAfterDataUpdate(tempPath);
                            loader.loadChildren(select) ;
                        } else {
                            if (tempListener == null) {
                                tempListener = new Listener<TreeEvent>() {
                                    public void handleEvent(TreeEvent event) {
                                        if (tempListener != null) {
                                            m_treeTable.removeListener(Events.Expand, tempListener);
                                        }
                                        setSelectPathAfterDataUpdate(tempPath);
                                    }
                                };
                            }
                            m_treeTable.addListener(Events.Expand, tempListener) ;
                            item.setExpanded(true);
                        }
                    } else {
                        TreeItem parentItem = item.getParentItem() ;
                        if (parentItem != null) {
                            GWTJahiaNode parent = (GWTJahiaNode) parentItem.getModel() ;
                            if (parent == null) {
                                init = true ;
                                loader.loadChildren(null) ;
                            } else {
                                loader.loadChildren(parent) ;
                            }
                        }
                    }
                }
            } else {

                TreeItem item = (TreeItem) binder.findItem(select) ;
                if (item != null) {
                    item = item.getParentItem() ;
                    if (item != null) {
                        item.setExpanded(true);
                        Log.debug("direct selection not null, refreshing from parent " + item.getModel().get("name")) ;
                        loader.loadChildren((GWTJahiaNode) item.getModel()) ;
                    }
                }
            }
        } else {
            Log.debug("reloading entire tree") ;
            loader.load(null) ;
        }
    }

    public Component getComponent() {
        return m_component ;
    }

    public void clearSelection() {
        m_treeTable.getSelectionModel().deselectAll();
    }

    private static TreeTableColumnModel getHeaders(String config) {
        List<TreeTableColumn> headerList = new ArrayList<TreeTableColumn>();
        if (config == null || config.length()==0) {
            config = "name,size,date";
        }

        String[] s = config.split(",");
        for (String s1 : s) {
            if (s1.equals("name")) {
                TreeTableColumn col = new TreeTableColumn("displayName", Messages.getResource("fm_column_name"), 500) ;
                headerList.add(col) ;
            } else if (s1.equals("size")) {
                TreeTableColumn col = new TreeTableColumn("size", Messages.getResource("fm_column_size"), 70) ;
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new CellRenderer() {
                    public String render(Component component, String s, Object o) {
                        if (o != null) {
                            long size = ((Long)o).longValue() ;
                            return Formatter.getFormattedSize(size) ;
                        } else {
                            return "-" ;
                        }
                    }
                });
                headerList.add(col) ;
            } else if (s1.equals("date")) {
                TreeTableColumn col = new TreeTableColumn("date", Messages.getResource("fm_column_date"), 80) ;
                col.setAlignment(Style.HorizontalAlignment.CENTER);
                col.setRenderer(new DateTimeCellRenderer("d/MM/y"));
                headerList.add(col) ;
            }
        }
        return new TreeTableColumnModel(headerList);
    }

    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class TreeTableStore<M extends ModelData> extends TreeStore<M> {
        public TreeTableStore(TreeLoader loader) {
            super(loader) ;
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
            if (getLinker() != null) {
                getLinker().loading("loading...") ;
            }
        }

        /**
         * This allows selection after tree items have been loaded (asynchronous call is 'blocking' here)
         */
        protected void onLoad(TreeLoadEvent e) {
            super.onLoad(e) ;
            if (getLinker() != null) {
                getLinker().loaded() ;
            }
        }
    }

    protected String getRootPath() {
        return rootPath;
    }

    private class MyTreeTableBinder<M extends TreeModel> extends TreeTableBinder<M> implements CustomTreeBinder<M>  {

        public MyTreeTableBinder(TreeTable t, TreeStore<M> s) {
            super(t, s) ;
        }

        public void renderChildren(M parent, List<M> children) {
            super.renderChildren(parent, children);
        }

    }
}
