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
package org.jahia.ajax.gwt.client.widget.node;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeDragSource;
import com.extjs.gxt.ui.client.dnd.TreeDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeServiceAsync;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.nodes.actions.FileActions;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeBinder;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.client.util.tree.TreeOpener;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * User: rfelden
 * Date: 28 nov. 2008 - 10:09:32
 */
public class RepositoryTab extends ContentPanel {

    private boolean init = true ;
    private String repositoryType ;
    private CustomTreeLoader<GWTJahiaNode> loader ;
    private TreeStore<GWTJahiaNode> store ;
    private FolderTree folderTreeContainer ;
    private MyTreeBinder<GWTJahiaNode> binder ;
    private Tree m_tree ;
    private PreviousPathsOpener<GWTJahiaNode> previousPathsOpener = null ;
    private TreeItem lastSelection = null ;
    private JahiaNodeServiceAsync nodeService ;

    /**
     * Constructor
     *
     * @param container the parent container
     * @param service the repository rpc service
     * @param type the repository type (see constants)
     * @param label the repository label
     * @param config the configuration to use
     */
    public RepositoryTab(FolderTree container, final JahiaNodeServiceAsync service, String type, String label, final ManagerConfiguration config) {
        super(new FitLayout()) ;
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        folderTreeContainer = container ;
        nodeService = service ;
        repositoryType = type ;
        getHeader().setIconStyle("fm-" + repositoryType);

        // data proxy
        RpcProxy<GWTJahiaNode, List<GWTJahiaNode>> privateProxy = new RpcProxy<GWTJahiaNode, List<GWTJahiaNode>>() {
            @Override
            protected void load(GWTJahiaNode gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    Log.debug("retrieving root for " + repositoryType) ;
                    nodeService.getRoot(repositoryType, JCRClientUtils.FOLDER_NODETYPES, config.getMimeTypes(), config.getFilters(), null, listAsyncCallback);
                } else {
                    Log.debug("retrieving children of " + gwtJahiaFolder.getName() + " for " + repositoryType) ;
                    nodeService.ls(gwtJahiaFolder, JCRClientUtils.FOLDER_NODETYPES, config.getMimeTypes(), config.getFilters(), null, false, listAsyncCallback);
                }
            }
        };

        // tree globalRepositoryLoader
        loader = new CustomTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren() ;
            }

            @Override
            protected void onLoadSuccess(GWTJahiaNode gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false") ;
                    init = false ;
                }
            }

            protected void expandPreviousPaths() {
                expandAllPreviousPaths() ;
            }
        };

        // tree store
        store = new FolderTreeStore<GWTJahiaNode>(loader);

        // tree component
        m_tree = new Tree();
        m_tree.getStyle().setLeafIconStyle("tree-folder");
        m_tree.setAnimate(false);
        m_tree.setBorders(false);

        // tree binder
        binder = new MyTreeBinder<GWTJahiaNode>(m_tree, store) ;
        binder.init() ;
        binder.setCaching(false);
        binder.setDisplayProperty("displayName");

        // tree selection listener
        m_tree.addListener(Events.SelectionChange, new Listener() {
            public void handleEvent(BaseEvent event) {
                TreeItem newSelection = m_tree.getSelectedItem() ;
                if (lastSelection != newSelection) {
                    lastSelection = newSelection ;
                    getLinker().onTreeItemSelected();
                }
            }
        });
        this.setScrollMode(Style.Scroll.AUTO);
        this.setHeading(label) ;
        this.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                init = true ;
                loader.load() ;
            }
        }));
        this.add(m_tree) ;
    }

    public void init() {
        loader.load() ;

        TreeDragSource source = new TreeDragSource(binder);
        source.addDNDListener(getLinker().getDndListener());

        TreeDropTarget target = new TreeDropTarget(binder) {
            @Override
            protected void handleInsert(DNDEvent event, TreeItem item) {
                super.handleAppend(event, item);
            }

            @Override
            protected void handleAppendDrop(DNDEvent event, TreeItem item) {
                FileActions.move(getLinker(), (List<GWTJahiaNode>) event.data, (GWTJahiaNode) item.getModel());
                super.handleAppendDrop(event, item);
            }
        };
        target.setFeedback(DND.Feedback.BOTH);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
    }

    public void expandAllPreviousPaths() {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaNode>(m_tree, store, binder) ;
        }
        previousPathsOpener.expandPreviousPaths();
    }

    public List<String> getOpenedPaths() {
        List<TreeItem> expandedItems = getOpenedPaths(m_tree.getRootItem()) ;
        List<String> expandedPaths = null ;
        if (expandedItems.size() > 0) {
            expandedPaths = new ArrayList<String>() ;
            for (TreeItem item: expandedItems) {
                expandedPaths.add(((GWTJahiaNode) item.getModel()).getPath()) ;
            }
        }
        return expandedPaths ;
    }

    private List<TreeItem> getOpenedPaths(TreeItem root) {
        List<TreeItem> items = new ArrayList<TreeItem>() ;
        boolean oneHasAlreadyBeenAdded = false ;
        for (TreeItem it: root.getItems()) {
            if (it.hasChildren()) {
                items.addAll(getOpenedPaths(it)) ;
                oneHasAlreadyBeenAdded = true ;
            } else if (it.getDepth() > 1 && !oneHasAlreadyBeenAdded) {
                items.add(it) ;
                oneHasAlreadyBeenAdded = true ;
            }
        }
        return items ;
    }

    public void openAndSelectItem(Object item) {
        if (item != null && this.isExpanded()) {
            Log.debug("trying to expand") ;
            new TreeOpener(m_tree, "name", reducePath(m_tree, ((GWTJahiaNode) item).getPath()));
        }
    }

    public void refresh() {
        if (this.isExpanded()) {
            TreeItem current = m_tree.getSelectedItem() ;
            if (current != null) {
                if (current.isExpanded()) {
                    loader.loadChildren(binder.getSelection().get(0)) ;
                } else {
                    if (repositoryType.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
                        TreeItem parent = current.getParentItem() ;
                        if (parent != null) {
                            loader.loadChildren((GWTJahiaNode) parent.getModel()) ;
                        }
                    } else {
                        refreshSubTree(current);
                    }
                }
            }
        }
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    private void refreshSubTree(TreeItem current) {
        List<TreeItem> privates = m_tree.getRootItem().getItems() ;
        if (privates.contains(current)) {
            if (current.isExpanded()) {
                loader.loadChildren((GWTJahiaNode) current.getModel()) ;
            } else {
                reloadSubTree();
            }
        } else {
            TreeItem parent = current.getParentItem() ;
            if (parent != null) {
                loader.loadChildren((GWTJahiaNode) parent.getModel()) ;
            }
        }
    }

    private void reloadSubTree() {
        init = true ;
        loader.load() ;
    }

    private String reducePath(Tree t, String path) {
        List<TreeItem> items = t.getRootItem().getItems() ;
        if (items.size() > 0) {
            TreeItem privateRoot = items.get(0) ;
            String absPath = ((GWTJahiaNode) privateRoot.getModel()).getPath() ;
            int ind = absPath.lastIndexOf("/") ;
            String parentPath = absPath.substring(0, ind) ;
            return path.replace(parentPath, "") ;
        } else {
            return path ;
        }
    }

    public GWTJahiaNode getSelectedItem() {
        List<GWTJahiaNode> selection = binder.getSelection() ;
        if (selection != null && selection.size() > 0) {
            return selection.get(0) ;
        } else {
            return null ;
        }
    }

    private BrowserLinker getLinker() {
        return folderTreeContainer.getLinker() ;
    }

    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class FolderTreeStore<M extends ModelData> extends TreeStore<M> {
        public FolderTreeStore(TreeLoader loader) {
            super(loader) ;
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
            if (getLinker() != null) {
                getLinker().loading("loading sub directories...") ;
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

    /**
     * TreeBinder that has renderChildren public.
     *
     * @param <M>
     */
    private class MyTreeBinder<M extends BaseTreeModel> extends TreeBinder<M> implements CustomTreeBinder<M> {

        public MyTreeBinder(Tree t, TreeStore<M> s) {
            super(t, s) ;
        }

        public void renderChildren(M parent, List<M> children) {
            try {
                super.renderChildren(parent, children);
            } catch (ConcurrentModificationException e) {
                // weird harmless exception
            }
        }

    }

}
