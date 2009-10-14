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
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;

import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 28 nov. 2008 - 10:09:32
 */
public class RepositoryTab extends ContentPanel {
    // ToDo : find a better way to handle autoExpand tree (without multiple ajax call)
    private boolean init = true;
    private String repositoryType;
    private TreeLoader<GWTJahiaNode> loader;
    private TreeStore<GWTJahiaNode> store;
    private FolderTree folderTreeContainer;
    private CustomTreePanel m_tree;
    private JahiaContentManagementServiceAsync contentManagementService;

    /**
     * Constructor
     *
     * @param container the parent container
     * @param service   the repository rpc service
     * @param type      the repository type (see constants)
     * @param label     the repository label
     * @param config    the configuration to use
     */
    public RepositoryTab(FolderTree container, final JahiaContentManagementServiceAsync service, String type, String label, final ManagerConfiguration config) {
        super(new FitLayout());
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        folderTreeContainer = container;
        contentManagementService = service;
        repositoryType = type;
        getHeader().setIconStyle("fm-" + repositoryType);

        // data proxy
        RpcProxy<List<GWTJahiaNode>> privateProxy = new RpcProxy<List<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
                if (init) {
                    Log.debug("retrieving root for " + repositoryType);
                    contentManagementService.getRoot(repositoryType, config.getFolderTypes(), config.getMimeTypes(), config.getFilters(), null, listAsyncCallback);
                } else {
                    Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getName() + " for " + repositoryType);
                    contentManagementService.ls(repositoryType, (GWTJahiaNode) gwtJahiaFolder, config.getFolderTypes(), config.getMimeTypes(), config.getFilters(), null, !config.isAllowCollections(), listAsyncCallback);
                }
            }
        };

        // tree globalRepositoryLoader
        loader = new BaseTreeLoader<GWTJahiaNode>(privateProxy) {
            @Override
            public boolean hasChildren(GWTJahiaNode parent) {
                return parent.hasFolderChildren();
            }

            public void onLoadSuccess(Object gwtJahiaNode, List<GWTJahiaNode> gwtJahiaNodes) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodes);
                if (init) {
                    Log.debug("setting init to false");
                    init = false;
                }
            }
        };

        // tree store
        store = new FolderTreeStore<GWTJahiaNode>(loader);

        // tree component
        m_tree = new CustomTreePanel(store);
        m_tree.init();

        setScrollMode(Style.Scroll.AUTO);
        setHeading(label);
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                refresh();
            }
        }));
        add(m_tree);
    }

    /**
     * init
     */
    public void init() {
        loader.load();

        TreePanelDragSource source = new TreePanelDragSource(m_tree) {
            @Override
            protected void onDragStart(DNDEvent e) {
                super.onDragStart(e);
                List<BaseTreeModel> l = e.getData();
                List<GWTJahiaNode> r = new ArrayList<GWTJahiaNode>();
                for (BaseTreeModel model : l) {
                    r.add((GWTJahiaNode) model.get("model"));
                }
                e.setData(r);
            }
        };
        source.addDNDListener(getLinker().getDndListener());

        TreePanelDropTarget target = new TreePanelDropTarget(m_tree) {
            @Override
            protected void handleInsert(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                handleAppend(dndEvent, treeNode);
            }

            @Override
            protected void handleAppend(DNDEvent event, TreePanel.TreeNode item) {
                super.handleAppend(event, item);
                if (((List) event.getData()).contains(activeItem.getModel())) {
                    event.getStatus().setStatus(false);
                }
            }

            @Override
            protected void handleAppendDrop(DNDEvent dndEvent, TreePanel.TreeNode treeNode) {
                if (dndEvent.getStatus().getStatus()) {
                    ContentActions.move(getLinker(), (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
                    boolean folder = false;
                    for (GWTJahiaNode node : (List<GWTJahiaNode>) dndEvent.getData()) {
                        folder |= node.isCollection();
                    }
                    if (folder) {
                        init = true;
                        loader.load();
                    }
                }
            }

            @Override
            protected void handleInsertDrop(DNDEvent event, TreePanel.TreeNode item, int index) {
            }
        };
        target.setFeedback(DND.Feedback.BOTH);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
    }

    /**
     * Save Paths
     */
    public void savePaths() {
        contentManagementService.saveOpenPathsForRepository(repositoryType, getOpenedPaths(), new AsyncCallback() {
            public void onSuccess(Object o) {
                // nothing to do
            }

            public void onFailure(Throwable throwable) {
                Log.error("Could not save expanded paths into user preferences:\n\n" + throwable.getLocalizedMessage(), throwable);
            }
        });
    }


    /**
     * Open and select iem
     *
     * @param item
     */
    public void openAndSelectItem(Object item) {
        if (item != null && this.isExpanded()) {
            GWTJahiaNode gItem = store.findModel((GWTJahiaNode) item);
            Log.debug("expand: "+gItem.getPath());
            m_tree.addToOpenPaths(gItem);
            m_tree.setExpanded(gItem,true);
            m_tree.getSelectionModel().select(gItem,false);
        }
    }

    /**
     * Refresh
     */
    public void refresh() {
        init = true;
        store.removeAll();
        loader.load();
    }

    /**
     * Get repository type
     *
     * @return
     */
    public String getRepositoryType() {
        return repositoryType;
    }


    /**
     * Get selected item
     *
     * @return
     */
    public GWTJahiaNode getSelectedItem() {
        List<GWTJahiaNode> selection = m_tree.getSelectionModel().getSelection();
        if (selection != null && selection.size() > 0) {
            return selection.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get the linker manager
     *
     * @return
     */
    private ManagerLinker getLinker() {
        return folderTreeContainer.getLinker();
    }

    /**
     * deselecte on free search
     */
    public void deselectOnFreeSearch() {
        m_tree.getSelectionModel().deselectAll();
    }

    /**
     * Get list of the opened Path
     *
     * @return
     */
    public List<String> getOpenedPaths() {
        return m_tree.getOpenedPaths();
    }

    /**
     * This class extends the standard load listener to allow automated child selection once the children are retrieved.
     */
    private class FolderTreeStore<M extends ModelData> extends TreeStore<M> {
        public FolderTreeStore(TreeLoader<M> loader) {
            super(loader);
        }

        protected void onBeforeLoad(LoadEvent e) {
            super.onBeforeLoad(e);
            if (getLinker() != null) {
                getLinker().loading("loading sub directories...");
            }
        }

        /**
         * This allows selection after tree items have been loaded (asynchronous call is 'blocking' here)
         */
        protected void onLoad(TreeLoadEvent e) {
            super.onLoad(e);
            if (getLinker() != null) {
                getLinker().loaded();
            }
        }
    }

    /**
     * This class extends the standard TreePanel to allow expand node if nested
     */
    private class CustomTreePanel extends TreePanel<GWTJahiaNode> {
        private List<String> openedPaths = new ArrayList<String>();

        private CustomTreePanel(TreeStore<GWTJahiaNode> gwtJahiaNodeTreeStore) {
            super(gwtJahiaNodeTreeStore);

        }

        /**
         * init method()
         */
        public void init() {
            setIconProvider(ContentModelIconProvider.getInstance());


            setDisplayProperty("displayName");
            setBorders(false);
            getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                    getLinker().onTreeItemSelected();
                }
            });

            // add listener after rendering
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    addListener(Events.Expand, new Listener<TreePanelEvent>() {
                        public void handleEvent(TreePanelEvent le) {
                            GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getItem();
                            String path = gwtJahiaNode.getPath();
                            if (!openedPaths.contains(path)) {
                                openedPaths.add(path);
                            }
                            Log.debug("Save Path on expand " + openedPaths);
                            gwtJahiaNode.setExpandOnLoad(true);
                            refresh(gwtJahiaNode);
                            savePaths();
                        }
                    });

                    addListener(Events.Collapse, new Listener<TreePanelEvent>() {
                        public void handleEvent(TreePanelEvent el) {
                            GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getItem();
                            String path = gwtJahiaNode.getPath();
                            openedPaths.remove(path);
                            Log.debug("Save Path on collapse " + openedPaths);
                            gwtJahiaNode.setExpandOnLoad(false);
                            refresh(gwtJahiaNode);
                            savePaths();
                        }
                    });
                }
            });
        }

        @Override
        protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
            super.onDataChanged(mTreeStoreEvent);
            GWTJahiaNode p = mTreeStoreEvent.getParent();
            if (p == null) {
                expandChildren(store.getRootItems());
            } else {
                expandChildren(store.getChildren(p));
            }
        }

        /**
         * Expand children
         *
         * @param children
         */
        private void expandChildren(List<GWTJahiaNode> children) {
            for (GWTJahiaNode child : children) {
                if (child.isExpandOnLoad()) {
                    setExpanded(child, true);
                    addToOpenPaths(child);
                    // ToDo : restore select state
                   /* if (child.isSelectedOnLoad()) {
                        getSelectionModel().select(child, true);
                    }*/
                }
            }
        }

        /**
         * Add to open paths
         * @param child
         */
        public void addToOpenPaths(GWTJahiaNode child) {
            if (!openedPaths.contains(child.getPath())) {
                openedPaths.add(child.getPath());
            }
        }

        /**
         * Get opened path
         *
         * @return
         */
        public List<String> getOpenedPaths() {
            return openedPaths;
        }
    }
}
