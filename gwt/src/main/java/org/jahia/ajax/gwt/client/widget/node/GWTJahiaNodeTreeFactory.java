/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.node;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Tree factory for loading {@link GWTJahiaNode} items.
 * User: toto
 * Date: Dec 24, 2009
 * Time: 3:14:02 PM
 */
public class GWTJahiaNodeTreeFactory {
    protected String repository;
    protected List<String> paths;
    protected List<String> nodeTypes;
    protected List<String> filters;
    protected List<String> fields = GWTJahiaNode.DEFAULT_FIELDS;
    protected List<String> mimeTypes;
    protected List<String> selectedPath = new ArrayList<String>();
    protected List<String> openPath = new ArrayList<String>();
    protected boolean saveOpenPath = false;
    protected GWTJahiaNodeTreeLoader loader;
    protected TreeStore<GWTJahiaNode> store;
    private boolean checkSubchilds = false;
    private List<String> hiddenTypes = new ArrayList<String>();
    private String hiddenRegex;
    private boolean displayHiddenTypes = false;
    private boolean showOnlyNodesWithTemplates = false;

    public GWTJahiaNodeTreeFactory(final List<String> paths) {
        this(paths, GWTJahiaNode.DEFAULT_FIELDS);
    }

    public GWTJahiaNodeTreeFactory(final List<String> paths, List<String> fields) {
        this.paths = paths;
        this.fields = fields;
        this.repository = paths.toString();
    }

    public GWTJahiaNodeTreeFactory(final List<String> paths, boolean checkSubchilds) {
        this(paths);
        this.checkSubchilds = checkSubchilds;
    }

    public GWTJahiaNodeTreeLoader getLoader() {
        if (loader == null) {
            loader = new GWTJahiaNodeTreeLoader(new GWTJahiaNodeProxy());
        }
        return loader;
    }


    public TreeStore<GWTJahiaNode> getStore() {
        if (store == null) {
            store = new TreeStore<GWTJahiaNode>(getLoader());
            store.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    if (o1 instanceof String && o2 instanceof String) {
                        String s1 = (String) o1;
                        String s2 = (String) o2;
                        return Collator.getInstance().localeCompare(s1, s2);
                    } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(o2);
                    }
                    return 0;
                }
            }));
        }
        return store;
    }

    public GWTJahiaNodeTreeGrid getTreeGrid(ColumnModel cm) {
        GWTJahiaNodeTreeGrid grid = new GWTJahiaNodeTreeGrid(getStore(), cm);
        grid.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        initOpenPathSaverTreeGrid(grid);

        return grid;
    }

    public GWTJahiaNodeTreePanel getTreePanel() {
        GWTJahiaNodeTreePanel panel = new GWTJahiaNodeTreePanel(getStore());
        panel.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        panel.setAutoSelect(false);
        initOpenPathSaverTreePanel(panel);

        return panel;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void setFields(List<String> fields) {
        this.fields = new ArrayList<String>(fields);
        this.fields.add(GWTJahiaNode.CHILDREN_INFO);
        this.fields.add(GWTJahiaNode.ICON);
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }


    public void setSelectedPath(String selectedPath) {
        this.selectedPath.add(selectedPath);
    }

    public void setSelectedPath(List<String> selectedPath) {
        this.selectedPath = selectedPath;
    }

    public void setOpenPath(String openPath) {
        this.openPath.add(openPath);
    }

    public void setOpenPath(List<String> openPath) {
        this.openPath = openPath;
    }

    public void setSaveOpenPath(boolean saveOpenPath) {
        this.saveOpenPath = saveOpenPath;
    }

    public void setDisplayHiddenTypes(boolean displayHiddenTypes) {
        this.displayHiddenTypes = displayHiddenTypes;
    }

    public void setHiddenRegex(String hiddenRegex) {
        this.hiddenRegex = hiddenRegex;
    }

    public void setHiddenTypes(List<String> hiddenTypes) {
        this.hiddenTypes = hiddenTypes;
    }

    public void setShowOnlyNodesWithTemplates(boolean showOnlyNodesWithTemplates) {
        this.showOnlyNodesWithTemplates = showOnlyNodesWithTemplates;
    }
    /**
     * init method()
     */
    public void initOpenPathSaverTreePanel(final GWTJahiaNodeTreePanel widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getItem();
                        String path = gwtJahiaNode.getPath();
                        if (!openPath.contains(path)) {
                            openPath.add(path);
                        }
                        Log.debug("Save Path on expand " + openPath);
                        gwtJahiaNode.setExpandOnLoad(true);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreePanelEvent>() {
                    public void handleEvent(TreePanelEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getItem();
                        String path = gwtJahiaNode.getPath();
                        openPath.remove(path);
                        Log.debug("Save Path on collapse " + openPath);
                        gwtJahiaNode.setExpandOnLoad(false);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> selectionChangedEvent) {
                        if (selectionChangedEvent.getSelectedItem() != null) {
                            setSelectedPath(Arrays.asList(selectionChangedEvent.getSelectedItem().getPath()));
                        }
                    }
                });
            }
        });
    }

    public void initOpenPathSaverTreeGrid(final GWTJahiaNodeTreeGrid widget) {
        // add listener after rendering
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                widget.addListener(Events.Expand, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent le) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) le.getModel();
                        String path = gwtJahiaNode.getPath();
                        if (!openPath.contains(path)) {
                            openPath.add(path);
                        }
                        Log.debug("Save Path on expand " + openPath);
                        gwtJahiaNode.setExpandOnLoad(true);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.addListener(Events.Collapse, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent el) {
                        GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) el.getModel();
                        String path = gwtJahiaNode.getPath();
                        openPath.remove(path);
                        Log.debug("Save Path on collapse " + openPath);
                        gwtJahiaNode.setExpandOnLoad(false);
//                        refresh(gwtJahiaNode);
                        if (saveOpenPath) {
                            savePaths();
                        }
                    }
                });

                widget.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> selectionChangedEvent) {
                        if (selectionChangedEvent.getSelectedItem() != null) {
                            setSelectedPath(Arrays.asList(selectionChangedEvent.getSelectedItem().getPath()));
                        }
                    }
                });
            }
        });
    }

    public void savePaths() {
        JahiaContentManagementService.App.getInstance()
                .saveOpenPathsForRepository(repository, openPath, new BaseAsyncCallback() {
                    public void onSuccess(Object o) {
                        // nothing to do
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        Log.error("Could not save expanded paths into user preferences:\n\n" +
                                throwable.getLocalizedMessage(), throwable);
                    }
                });
    }


    class GWTJahiaNodeProxy extends RpcProxy<List<GWTJahiaNode>> {
        public GWTJahiaNodeProxy() {
        }

        @Override
        protected void load(Object currentPage, final AsyncCallback<List<GWTJahiaNode>> listAsyncCallback) {
            if (currentPage == null) {
                JahiaContentManagementService.App.getInstance()
                        .getRoot(paths, nodeTypes, mimeTypes, filters, fields, selectedPath, openPath,checkSubchilds,
                                displayHiddenTypes, hiddenTypes, hiddenRegex, listAsyncCallback);
            } else {
                GWTJahiaNode gwtJahiaNode = (GWTJahiaNode) currentPage;
                if (gwtJahiaNode.isExpandOnLoad()) {
                    List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
                    for (ModelData modelData : gwtJahiaNode.getChildren()) {
                        list.add((GWTJahiaNode) modelData);
                    }
                    listAsyncCallback.onSuccess(list);
                } else {
                    try {
                        JahiaContentManagementService.App.getInstance()
                                .lsLoad(gwtJahiaNode, nodeTypes, mimeTypes, filters, fields, checkSubchilds, -1, -1, displayHiddenTypes,
                                        hiddenTypes, hiddenRegex,false, new BaseAsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
                                            public void onSuccess(PagingLoadResult<GWTJahiaNode> result) {
                                                listAsyncCallback.onSuccess(result.getData());
                                            }

                                            @Override public void onApplicationFailure(Throwable caught) {
                                                listAsyncCallback.onFailure(caught);
                                            }

                                        });
                    } catch (org.jahia.ajax.gwt.client.service.GWTJahiaServiceException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

    }


    public class GWTJahiaNodeTreeLoader extends BaseTreeLoader<GWTJahiaNode> {
        GWTJahiaNodeTreeLoader(DataProxy proxy) {
            super(proxy);
        }

        @Override
        public boolean hasChildren(GWTJahiaNode parent) {
            return parent.hasChildren();
        }
    }

    public class GWTJahiaNodeTreeGrid extends TreeGrid<GWTJahiaNode> {
        GWTJahiaNodeTreeGrid(TreeStore store, ColumnModel cm) {
            super(store, cm);
        }

        protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
            super.onDataChanged(mTreeStoreEvent);
            final GWTJahiaNode p = mTreeStoreEvent.getParent();
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    if (p == null) {
                        expandChildren(treeStore.getRootItems());
                    } else {
                        if (treeStore.getChildren(p) != null) { expandChildren(treeStore.getChildren(p)); }
                    }
                }
            });
        }

        private void expandChildren(List<GWTJahiaNode> children) {
            for (GWTJahiaNode child : children) {
                if (child.isExpandOnLoad()) {
                    setExpanded(child, true);
                }
                if (child.isSelectedOnLoad()) {
                    getSelectionModel().select(true, child);
                }
            }
        }

    }

    public class GWTJahiaNodeTreePanel extends TreePanel<GWTJahiaNode> {
        GWTJahiaNodeTreePanel(TreeStore store) {
            super(store);
            setAutoSelect(false);
        }

        protected void onDataChanged(TreeStoreEvent<GWTJahiaNode> mTreeStoreEvent) {
            super.onDataChanged(mTreeStoreEvent);
            final GWTJahiaNode p = mTreeStoreEvent.getParent();
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    if (p == null) {
                        expandChildren(store.getRootItems());
                    } else {
                        expandChildren(store.getChildren(p));
                    }
                }
            });
        }

        private void expandChildren(List<GWTJahiaNode> children) {
            for (GWTJahiaNode child : children) {
                if (child.isExpandOnLoad()) {
                    setExpanded(child, true);
                }
                if (child.isSelectedOnLoad()) {
                    getSelectionModel().select(true, child);
                }
            }
        }

    }

}
