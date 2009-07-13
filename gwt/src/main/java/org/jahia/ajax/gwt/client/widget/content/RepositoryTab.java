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
import com.extjs.gxt.ui.client.dnd.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreeStyle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.tree.TreeOpener;
import org.jahia.ajax.gwt.client.util.icons.Util;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * User: rfelden
 * Date: 28 nov. 2008 - 10:09:32
 */
public class RepositoryTab extends ContentPanel {

    // TODO GXT 2 !!!
    private boolean init = true ;
    private String repositoryType ;
    private CustomTreeLoader<GWTJahiaNode> loader ;
    private TreeStore<GWTJahiaNode> store ;
    private FolderTree folderTreeContainer ;
    private TreePanel<GWTJahiaNode> m_tree ;
    //private PreviousPathsOpener<GWTJahiaNode> previousPathsOpener = null ;
//    private TreeItem lastSelection = null ;
    private JahiaContentManagementServiceAsync contentManagementService;

    /**
     * Constructor
     *
     * @param container the parent container
     * @param service the repository rpc service
     * @param type the repository type (see constants)
     * @param label the repository label
     * @param config the configuration to use
     */
    public RepositoryTab(FolderTree container, final JahiaContentManagementServiceAsync service, String type, String label, final ManagerConfiguration config) {
        super(new FitLayout()) ;
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        folderTreeContainer = container ;
        contentManagementService = service ;
        repositoryType = type ;
        getHeader().setIconStyle("fm-" + repositoryType);

        // data proxy
        RpcProxy<List<GWTJahiaNode>> privateProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    Log.debug("retrieving root for " + repositoryType) ;
                    contentManagementService.getRoot(repositoryType, JCRClientUtils.FOLDER_NODETYPES, config.getMimeTypes(), config.getFilters(), null, listAsyncCallback);
                } else {
                    Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName() + " for " + repositoryType) ;
                    contentManagementService.ls((GWTJahiaNode) gwtJahiaFolder, JCRClientUtils.FOLDER_NODETYPES, config.getMimeTypes(), config.getFilters(), null, false, listAsyncCallback);
                }
            }
        };

        // tree globalRepositoryLoader
        loader = new CustomTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren() ;
            }

            protected void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false") ;
                    init = false ;
                }
            }

            protected void expandPreviousPaths() {
//                expandAllPreviousPaths() ;
            }
        };

        // tree store
        store = new FolderTreeStore<GWTJahiaNode>(loader);

        // tree component
        m_tree = new TreePanel<GWTJahiaNode>(store);
        TreeStyle treeStyle = new TreeStyle();
        m_tree.setStyle(treeStyle);
        m_tree.setDisplayProperty("displayName");
        treeStyle.setLeafIcon(Util.getContentIcons("extension"));
        m_tree.setBorders(false);

//        binder.setIconProvider(new ModelStringProvider<GWTJahiaNode>() {
//            public String getStringValue(GWTJahiaNode modelData, String s) {
//                if ("icon-dir".equals(modelData.getExt())) {
//                    return "tree-folder";
//                }
//                return modelData.getExt();
//            }
//        });
//
        // tree selection listener
        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                getLinker().onTreeItemSelected();
            }
        });

        setScrollMode(Style.Scroll.AUTO);
        setHeading(label) ;
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                init = true ;
                loader.load() ;
            }
        }));
        add(m_tree) ;
    }

    public void init() {
        loader.load() ;

        TreePanelDragSource source = new TreePanelDragSource(m_tree);
        source.addDNDListener(getLinker().getDndListener());

        TreePanelDropTarget target = new TreePanelDropTarget(m_tree) {
            @Override
            protected void handleInsert(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                super.handleInsert(dndEvent, treeNode);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            protected void handleAppendDrop(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                ContentActions.move(getLinker(), (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
                super.handleAppendDrop(dndEvent, treeNode);    //To change body of overridden methods use File | Settings | File Templates.
            }
        };
        target.setFeedback(DND.Feedback.BOTH);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
    }

//    public void expandAllPreviousPaths() {
//        if (previousPathsOpener == null) {
//            previousPathsOpener = new PreviousPathsOpener<GWTJahiaNode>(m_tree, store, binder) ;
//        }
//        previousPathsOpener.expandPreviousPaths();
//    }

//    public List<String> getOpenedPaths() {
//        List<TreeItem> expandedItems = getOpenedPaths(store.getRootItems().get(0)) ;
//        List<String> expandedPaths = null ;
//        if (expandedItems.size() > 0) {
//            expandedPaths = new ArrayList<String>() ;
//            for (TreeItem item: expandedItems) {
//                expandedPaths.add(((GWTJahiaNode) item.getModel()).getPath()) ;
//            }
//        }
//        return expandedPaths ;
//    }

//    private List<TreeItem> getOpenedPaths(GWTJahiaNode root) {
//        List<TreeItem> items = new ArrayList<TreeItem>() ;
//        boolean oneHasAlreadyBeenAdded = false ;
//        for (ModelData md: root.getChildren()) {
//            GWTJahiaNode it = (GWTJahiaNode) md;
//            if (it.hasChildren()) {
//                items.addAll(getOpenedPaths(it)) ;
//                oneHasAlreadyBeenAdded = true ;
////            } else if (it.getDepth() > 1 && !oneHasAlreadyBeenAdded) {
////                items.add(it) ;
////                oneHasAlreadyBeenAdded = true ;
//            }
//        }
//        return items ;
//    }

    public void openAndSelectItem(Object item) {
        if (item != null && this.isExpanded()) {
            Log.debug("trying to expand") ;
//            new TreeOpener(m_tree, "name", reducePath(m_tree, ((GWTJahiaNode) item).getPath()));
        }
    }

    public void refresh() {
//        if (this.isExpanded()) {
//            TreeItem current = m_tree.getSelectedItem() ;
//            if (current != null) {
//                if (current.isExpanded()) {
//                    loader.loadChildren(binder.getSelection().get(0)) ;
//                } else {
//                    if (repositoryType.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
//                        TreeItem parent = current.getParentItem() ;
//                        if (parent != null) {
//                            loader.loadChildren((GWTJahiaNode) parent.getModel()) ;
//                        }
//                    } else {
//                        refreshSubTree(current);
//                    }
//                }
//            }
//        }
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    private void refreshSubTree(TreeItem current) {
//        List<TreeItem> privates = m_tree.getRootItem().getItems() ;
//        if (privates.contains(current)) {
//            if (current.isExpanded()) {
//                loader.loadChildren((GWTJahiaNode) current.getModel()) ;
//            } else {
//                reloadSubTree();
//            }
//        } else {
//            TreeItem parent = current.getParentItem() ;
//            if (parent != null) {
//                loader.loadChildren((GWTJahiaNode) parent.getModel()) ;
//            }
//        }
    }

    private void reloadSubTree() {
        init = true ;
        loader.load() ;
    }

    private String reducePath(TreePanel t, String path) {
//        List<TreeItem> items = store.getRootItems().get(0).getChildren() ;
//        if (items.size() > 0) {
//            TreeItem privateRoot = items.get(0) ;
//            String absPath = ((GWTJahiaNode) privateRoot.getModel()).getPath() ;
//            int ind = absPath.lastIndexOf("/") ;
//            String parentPath = absPath.substring(0, ind) ;
//            return path.replace(parentPath, "") ;
//        } else {
            return path ;
//        }
    }

    public GWTJahiaNode getSelectedItem() {
        List<GWTJahiaNode> selection = m_tree.getSelectionModel().getSelection() ;
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
        public FolderTreeStore(TreeLoader<M> loader) {
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


}
