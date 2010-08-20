/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTRepository;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: rfelden
 * Date: 28 nov. 2008 - 10:09:32
 */
public class RepositoryTab extends ContentPanel {
    private GWTRepository repository;
    private TreeLoader<GWTJahiaNode> loader;
    private TreeStore<GWTJahiaNode> store;
    private ContentRepositoryTabs folderTreeContainer;
    private TreeGrid<GWTJahiaNode> m_tree;

    /**
     * Constructor
     *
     * @param container the parent container
     * @param repo      the repository type (see constants)
     * @param config    the configuration to use
     */
    public RepositoryTab(ContentRepositoryTabs container, GWTRepository repo, final List<String> selectedPaths, final GWTManagerConfiguration config) {
        super(new FitLayout());
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        folderTreeContainer = container;
        repository = repo;
        getHeader().setIcon(ToolbarIconProvider.getInstance().getIcon(repo.getKey()));

        // tree component
        GWTJahiaNodeTreeFactory factory = new GWTJahiaNodeTreeFactory(repository.getPaths());
        factory.setNodeTypes(config.getFolderTypes());
        factory.setMimeTypes(config.getMimeTypes());
        factory.setFilters(config.getFilters());
        factory.setFields(config.getTableColumnKeys());
        factory.setSelectedPath(selectedPaths);
        factory.setSaveOpenPath(true);
        loader = factory.getLoader();
        store = factory.getStore();

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(new TreeGridCellRenderer());
        m_tree = factory.getTreeGrid(new ColumnModel(columns));
        m_tree.setHideHeaders(true);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        if (columns.getAutoExpand() != null) {
            m_tree.setAutoExpandColumn(columns.getAutoExpand());
        }

//        m_tree.setDisplayProperty("displayName");
        m_tree.setBorders(false);
        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                getLinker().onTreeItemSelected();
            }
        });
//        m_tree.init();

        setScrollMode(Style.Scroll.NONE);
        setHeading(repo.getTitle());
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

        TreeGridDragSource source = new TreeGridDragSource(m_tree) {
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

        TreeGridDropTarget target = new TreeGridDropTarget(m_tree) {
            @Override
            protected void handleInsert(DNDEvent dndEvent, TreeGrid.TreeNode treeNode) {
                handleAppend(dndEvent, treeNode);
            }

            @Override
            protected void handleAppend(DNDEvent event, TreeGrid.TreeNode item) {
                super.handleAppend(event, item);
                final List<GWTJahiaNode> list = (List<GWTJahiaNode>) event.getData();
                for (GWTJahiaNode source : list) {
                    final GWTJahiaNode target = (GWTJahiaNode) activeItem.getModel();
                    if (target.getPath().startsWith(source.getPath())) {
                        event.getStatus().setStatus(false);
                    } else {
                        final Set<String> constraints = new HashSet(source.getInheritedNodeTypes());
                        constraints.addAll(source.getNodeTypes());
                        constraints.retainAll(target.getChildConstraints());
                        if (constraints.isEmpty()) {
                            event.getStatus().setStatus(false);
                        }
                    }
                }
            }

            @Override
            protected void handleAppendDrop(DNDEvent dndEvent, TreeGrid.TreeNode treeNode) {
                if (dndEvent.getStatus().getStatus()) {
                    ContentActions.move(getLinker(), (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
                    loader.load();
                }
            }

            @Override
            protected void handleInsertDrop(DNDEvent event, TreeGrid.TreeNode item, int index) {
            }
        };
        target.setFeedback(DND.Feedback.BOTH);
        target.setAllowSelfAsSource(true);
        target.setAutoExpand(true);
    }

    /**
     * Open and select iem
     *
     * @param item
     */
    public void openAndSelectItem(Object item) {
        if (item != null && this.isExpanded()) {
            GWTJahiaNode gItem = store.findModel((GWTJahiaNode) item);
            Log.debug("expand: " + gItem.getPath());
//            m_tree.addToOpenPaths(gItem);
            m_tree.setExpanded(gItem, true);
            m_tree.getSelectionModel().select(gItem, false);
        }
    }

    /**
     * Refresh
     */
    public void refresh() {
//        init = true;
        store.removeAll();
        loader.load();
    }

    /**
     * Get repository type
     *
     * @return
     */
    public GWTRepository getRepository() {
        return repository;
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

}
