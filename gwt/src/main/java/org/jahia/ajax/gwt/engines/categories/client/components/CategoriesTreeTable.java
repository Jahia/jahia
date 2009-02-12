/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.categories.client.components;

import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopRightComponent;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;
import org.jahia.ajax.gwt.commons.client.util.Formatter;
import org.jahia.ajax.gwt.engines.categories.client.model.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryServiceAsync;
import org.jahia.ajax.gwt.engines.categories.client.service.CategoryService;
import org.jahia.ajax.gwt.engines.categories.client.util.CategoriesManagerActions;
import org.jahia.ajax.gwt.engines.categories.client.CategoriesManagerEntryPoint;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treetable.TreeTable;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumnModel;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableColumn;
import com.extjs.gxt.ui.client.widget.treetable.TreeTableItem;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.binder.TreeTableBinder;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 16:50:39
 */
public class CategoriesTreeTable extends TopRightComponent {

    private LayoutContainer m_component;
    private TreeLoader<GWTJahiaCategoryNode> loader;
    private TreeTable m_treeTable;
    private TreeTableStore<GWTJahiaCategoryNode> treeTableStore;
    private TreeTableBinder<GWTJahiaCategoryNode> treeTableBinder;
    private TreeItem lastSelection = null;
    private String rootKey;

    public CategoriesTreeTable(String rootKey) {
        m_component = new LayoutContainer(new FitLayout());
        final CategoryServiceAsync service = CategoryService.App.getInstance();

        // data proxy
        RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>> proxy = new RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>>() {
            @Override
            protected void load(GWTJahiaCategoryNode gwtJahiaCategoryNode, AsyncCallback<List<GWTJahiaCategoryNode>> listAsyncCallback) {
                service.ls(gwtJahiaCategoryNode, null, listAsyncCallback);
            }
        };

        // tree loader
        loader = new BaseTreeLoader<GWTJahiaCategoryNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaCategoryNode parent) {
                return !parent.isLeaf();
            }


            public boolean load(GWTJahiaCategoryNode gwtJahiaCategoryNode, int depth) {
                List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes = gwtJahiaCategoryNode.getChildren();
                for (GWTJahiaCategoryNode currentNode : gwtJahiaCategoryNodes) {
                    loadChildren(currentNode);
                }
                return super.load(gwtJahiaCategoryNode);
            }

            @Override
            protected void onLoadSuccess(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
                super.onLoadSuccess(gwtJahiaCategoryNode, gwtJahiaCategoryNodes);
                if (gwtJahiaCategoryNode == null) {
                    for (GWTJahiaCategoryNode currentNode : gwtJahiaCategoryNodes) {
                        loadChildren(currentNode);
                    }
                } else {
                    TreeTableItem item = (TreeTableItem) treeTableBinder.findItem(gwtJahiaCategoryNode);
                    item.setExpanded(true);
                }
            }
        };

        // tree store
        treeTableStore = new TreeTableStore<GWTJahiaCategoryNode>(loader, null);

        // tree table
        m_treeTable = new TreeTable(getHeaders());
        m_treeTable.setHorizontalScroll(true);
        treeTableBinder = new TreeTableBinder<GWTJahiaCategoryNode>(m_treeTable, treeTableStore);

        treeTableBinder.init();
        treeTableBinder.setCaching(true);
        treeTableBinder.setDisplayProperty("name");

        m_treeTable.addListener(Events.SelectionChange, new Listener() {
            public void handleEvent(BaseEvent event) {
                TreeItem newSelection = m_treeTable.getSelectedItem();
                if (lastSelection != newSelection) {
                    lastSelection = newSelection;
                    getLinker().onTableItemSelected();
                }
            }
        });

        m_component.add(m_treeTable);
    }

    /**
     * init with linker
     *
     * @param linker the linker
     */
    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        loader.load(null);
    }

    /**
     * initContext Menu
     */
    public void initContextMenu() {
        m_treeTable.setContextMenu(new CatgeoryListContextMenu(getLinker()));
    }

    /**
     * Set content
     *
     * @param root
     */
    public void setContent(Object root) {
    }

    /**
     * Expabdpath
     *
     * @param path
     */
    public void expandPath(String path) {
        if (path == null || path.trim().length() == 0) {
            return;
        }
        String[] pathComps = path.split("/");
        List<String> pathList = new ArrayList<String>();
        for (String pathComponent : pathComps) {
            if (pathComponent.trim().length() > 0) {
                pathList.add(pathComponent);
            }
        }
        openAndSelectIfLast(pathList, m_treeTable.getRootItem());
    }

    /**
     * Open path and select if leaf
     *
     * @param path
     * @param current
     */
    private void openAndSelectIfLast(List<String> path, TreeItem current) {
        if (path == null || path.size() == 0 || current.isLeaf()) {
            return;
        }

        List<TreeItem> currentChildren = current.getItems();
        TreeItem foundItem = null;
        for (TreeItem currentChild : currentChildren) {
            if (currentChild.getText().equals(path.get(0))) {
                foundItem = currentChild;
                Log.debug("path component was found: " + foundItem.getText());
                break;
            }
        }

        if (foundItem != null) {
            if (path.size() == 1) {
                Log.debug("last component");
                treeTableStore.clearExpansionListener();
                m_treeTable.setSelectedItem(foundItem);
            } else {
                List<String> subList = new ArrayList<String>(path.size() - 1);
                for (int i = 1; i < path.size(); i++) {
                    subList.add(path.get(i));
                }
                treeTableStore.putExpansionListener(foundItem, subList);
                Log.debug("expanding children: " + foundItem.getText());
                foundItem.setExpanded(true);
            }
        }


    }

    /**
     * Clear
     */
    public void clearTable() {
        treeTableStore.removeAll();
    }

    /**
     * Get selection
     *
     * @return
     */
    public Object getSelection() {
        List<GWTJahiaCategoryNode> elts = treeTableBinder.getSelection();
        if (elts != null && elts.size() > 0) {
            return elts;
        } else {
            return null;
        }
    }

    /**
     * Refresh
     */
    public void refresh() {
        clearSelection();
        loader.load(null);
    }

    public void clearSelection() {
        m_treeTable.getSelectionModel().deselectAll();
    }


    /**
     * Get main component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * Get table headers
     *
     * @return
     */
    private static TreeTableColumnModel getHeaders() {
        List<TreeTableColumn> headerList = new ArrayList<TreeTableColumn>();

        // key
        TreeTableColumn col = new TreeTableColumn("name", getResource("cat_title"), 250);
        headerList.add(col);

        // name
        col = new TreeTableColumn("key", getResource("cat_key"), 150);
        headerList.add(col);

        // name
        col = new TreeTableColumn("path", getResource("cat_path"), 280);
        headerList.add(col);

        return new TreeTableColumnModel(headerList);
    }

    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class TreeTableStore<M extends ModelData> extends TreeStore<M> {
        private TreeItem expansionListener = null;
        private List<String> pathToSelect = null;
        private String startPath = null;

        public TreeTableStore(TreeLoader loader, String startPath) {
            super(loader);
            this.startPath = startPath;

        }

        public void putExpansionListener(TreeItem parentItem, List<String> path) {
            expansionListener = parentItem;
            pathToSelect = path;
        }

        public void clearExpansionListener() {
            pathToSelect = null;
            expansionListener = null;
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
            if (getLinker() != null) {
                getLinker().loading("loading...");
            }
        }

        /**
         * This allows selection after tree items have been loaded (asynchronous call is 'blocking' here)
         */
        protected void onLoad(TreeLoadEvent e) {
            super.onLoad(e);
            if (startPath != null) {
                expandPath(startPath);
                startPath = null;
            } else if (expansionListener != null && pathToSelect != null) {
                openAndSelectIfLast(pathToSelect, expansionListener);
            }
            if (getLinker() != null) {
                getLinker().loaded();
            }
        }


    }

    /**
     * Category list context menu
     */
    public class CatgeoryListContextMenu extends Menu {
        public CatgeoryListContextMenu(final BrowserLinker linker) {
            super();

            final List<MenuItem> topTableSingleSelectionButtons = new ArrayList<MenuItem>();
            final List<MenuItem> topTableMultipleSelectionButtons = new ArrayList<MenuItem>();

            final MenuItem newCategory = new MenuItem();
            final MenuItem cut = new MenuItem();
            final MenuItem updateInfo = new MenuItem();
            final MenuItem remove = new MenuItem();
            final MenuItem updateACL = new MenuItem();

            newCategory.setText(getResource("cat_create"));
            newCategory.setIconStyle("fm-newfolder");
            newCategory.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    CategoriesManagerActions.createCategory(linker);
                }
            });

            updateInfo.setText(getResource("cat_update"));
            updateInfo.setIconStyle("fm-rename");
            updateInfo.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    CategoriesManagerActions.updateInfo(linker);
                }
            });

            updateACL.setText(getResource("cat_update_acl"));
            updateACL.setIconStyle("fm-rename");
            updateACL.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    CategoriesManagerActions.openUpdateACL(linker);
                }
            });

            cut.setText(getResource("cat_cut"));
            cut.setIconStyle("fm-cut");
            cut.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    CategoriesManagerActions.cut(linker);
                }
            });


            remove.setText(getResource("cat_remove"));
            remove.setIconStyle("fm-remove");
            remove.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent ce) {
                    CategoriesManagerActions.remove(linker);
                }
            });

            // handle visibility of each item
            addListener(Events.BeforeShow, new Listener() {
                public void handleEvent(BaseEvent baseEvent) {
                    GWTJahiaCategoryNode leftTreeSelection = null;
                    List<GWTJahiaCategoryNode> topTableSelection = null;
                    if (linker != null) {
                        leftTreeSelection = (GWTJahiaCategoryNode) linker.getTreeSelection();
                        topTableSelection = (List<GWTJahiaCategoryNode>) linker.getTableSelection();
                    }
                    if (topTableSelection != null) {
                        // enable all items
                        for (MenuItem ti : topTableSingleSelectionButtons) {
                            Formatter.enableMenuItem(ti);
                        }
                        for (MenuItem ti : topTableMultipleSelectionButtons) {
                            Formatter.enableMenuItem(ti);
                        }

                        // multiple selection items
                        if (topTableSelection.size() > 1) {
                            for (MenuItem ti : topTableMultipleSelectionButtons) {
                                Formatter.enableMenuItem(ti);
                            }

                            for (MenuItem ti : topTableSingleSelectionButtons) {
                                Formatter.disableMenuItem(ti);
                            }
                        }
                        // single selection
                        else if (topTableSelection.size() == 1) {
                            for (MenuItem ti : topTableMultipleSelectionButtons) {
                                Formatter.enableMenuItem(ti);
                            }
                            for (MenuItem ti : topTableSingleSelectionButtons) {
                                Formatter.enableMenuItem(ti);
                            }
                        }
                    } else if (leftTreeSelection != null) {
                        for (MenuItem ti : topTableMultipleSelectionButtons) {
                            Formatter.disableMenuItem(ti);
                        }

                        for (MenuItem ti : topTableSingleSelectionButtons) {
                            Formatter.disableMenuItem(ti);
                        }
                    } else {
                        for (MenuItem ti : topTableMultipleSelectionButtons) {
                            Formatter.disableMenuItem(ti);
                        }
                        for (MenuItem ti : topTableSingleSelectionButtons) {
                            Formatter.disableMenuItem(ti);
                        }
                    }

                    // check if one of the selected categories is only read access
                    for (GWTJahiaCategoryNode gwtJahiaCategoryNode : topTableSelection) {
                        if (!gwtJahiaCategoryNode.isWriteable()) {
                            for (MenuItem ti : topTableSingleSelectionButtons) {
                                Formatter.disableMenuItem(ti);
                            }
                            for (MenuItem ti : topTableMultipleSelectionButtons) {
                                Formatter.disableMenuItem(ti);
                            }
                            break;
                        }
                    }

                }
            });

            topTableSingleSelectionButtons.add(newCategory);
            topTableSingleSelectionButtons.add(updateInfo);
            topTableSingleSelectionButtons.add(updateACL);
            topTableMultipleSelectionButtons.add(cut);
            topTableMultipleSelectionButtons.add(remove);

            // add items
            add(newCategory);
            add(cut);
            add(updateInfo);
            add(remove);

        }

    }

    private static String getResource(String key) {
        return CategoriesManagerEntryPoint.getResource(key);
    }
}
