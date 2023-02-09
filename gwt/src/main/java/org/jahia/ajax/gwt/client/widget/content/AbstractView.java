/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: toto
 * Date: Sep 27, 2010
 * Time: 6:27:01 PM
 */
public abstract class AbstractView extends TopRightComponent {
    protected GWTJahiaNode root;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> loader;
    protected GWTManagerConfiguration configuration;
    protected List<GWTJahiaNode> hiddenSelection;
    protected List<GWTJahiaNode> visibleSelection;
    protected ListStore<GWTJahiaNode> store;
    protected AbstractStoreSelectionModel<GWTJahiaNode> selectionModel;
    protected ListStore<ModelData> typeStore = null;
    protected ContentSource contentSource = null;

    public AbstractView(final GWTManagerConfiguration config) {
        typeStore = new ListStore<ModelData>();
        BaseModelData data = new TypeModelData();
        data.set(GWTJahiaNode.PRIMARY_TYPE_LABEL, Messages.get("label.all","All"));
        typeStore.add(data);
        configuration = config;
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object config, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children with type " + configuration.getNodeTypes() + " of " +
                        root.getPath());
                if (config instanceof ListLoadConfig) {
                    ListLoadConfig listLoadConfig = (ListLoadConfig) config;
                    listLoadConfig.setSortField(store.getSortField());
                    listLoadConfig.setSortDir(store.getSortDir());
                }
                List<String> keys = new ArrayList<String>(configuration.getTableColumnKeys());
                if (configuration.isEditableGrid()) {
                    keys.add("fields-content");
                }
                int indexOf = keys.indexOf(GWTJahiaNode.PUBLICATION_INFO);
                if (indexOf == -1 && !configuration.isSuppressTablePublicationInfo()) {
                    keys.add(GWTJahiaNode.PUBLICATION_INFO);
                } else if (indexOf != -1 && configuration.isSuppressTablePublicationInfo()) {
                    keys.remove(indexOf);
                }
                keys.add(GWTJahiaNode.PERMISSIONS);
                keys.add(GWTJahiaNode.LOCKS_INFO);
                keys.add(GWTJahiaNode.ICON);
                keys.add("jcr:createdBy");
                if (configuration.isUseLargeThumbnails()) {
                    keys.add(GWTJahiaNode.PREVIEW_LARGE);
                }
                JahiaContentManagementService.App.getInstance().lsLoad(root.getPath(),
                        configuration.getNodeTypes(),
                        configuration.getMimeTypes(), configuration.getFilters(), keys,
                        false, -1, -1, getLinker().isDisplayHiddenTypes(), configuration.getHiddenTypes(), configuration.getHiddenRegex(), configuration.isShowOnlyNodesWithTemplates(), false, listAsyncCallback);
            }
        };

        loader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(privateProxy) {
            @Override
            protected void onLoadSuccess(Object gwtJahiaNode, ListLoadResult<GWTJahiaNode> gwtJahiaNodeListLoadResult) {
                super.onLoadSuccess(gwtJahiaNode, gwtJahiaNodeListLoadResult);
                if (getLinker() != null) {
                    getLinker().loaded();
                }
                if (hiddenSelection != null) {
                    visibleSelection = new ArrayList<GWTJahiaNode>(store.getModels());
                    visibleSelection.retainAll(hiddenSelection);
                    if (visibleSelection.isEmpty()) {
                        getLinker().onTableItemSelected();
                    } else {
                        selectionModel.setSelection(visibleSelection);
                    }
                    for (GWTJahiaNode node : store.getModels()) {
                        node.setParent((TreeModel) AbstractView.this.root);
                    }
                }
                updateTypeStore(gwtJahiaNodeListLoadResult.getData());
                getLinker().getTopRightObject().getToolBar().clear();
                getLinker().getTopRightObject().getToolBar().disable();

            }

        };

        store = new ListStore<GWTJahiaNode>(loader) {
            protected void onBeforeLoad(LoadEvent e) {
                if (getLinker() != null) {
                    getLinker().loading(Messages.get("statusbar.loadingContent.label"));
                }
                super.onBeforeLoad(e);
            }

            @Override
            protected void onLoadException(LoadEvent loadEvent) {
                super.onLoadException(loadEvent);
                Log.error("Error listing directory content ", loadEvent.exception);
            }
        };

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

    private void updateTypeStore(List<GWTJahiaNode> nodes) {
        if (typeStore != null) {
            for (GWTJahiaNode o : nodes) {
                BaseModelData data = new TypeModelData();
                data.set(GWTJahiaNode.PRIMARY_TYPE_LABEL, o.get(GWTJahiaNode.PRIMARY_TYPE_LABEL));
                if (!typeStore.contains(data)) {
                    typeStore.add(data);
                }
            }
            typeStore.sort(GWTJahiaNode.PRIMARY_TYPE_LABEL, Style.SortDir.ASC);
        }
    }

    public List<GWTJahiaNode> getSelection() {
        List<GWTJahiaNode> elts = visibleSelection;
        if (elts != null && elts.size() > 0) {
            return elts;
        } else {
            return null;
        }
    }

    public List<GWTJahiaNode> getHiddenSelection() {
        return hiddenSelection;
    }

    void setHiddenSelection(List<GWTJahiaNode> hiddenSelection) {
        this.hiddenSelection = hiddenSelection;
    }

    List<GWTJahiaNode> getVisibleSelection() {
        return visibleSelection;
    }

    void setVisibleSelection(List<GWTJahiaNode> visibleSelection) {
        this.visibleSelection = visibleSelection;
    }

    public AbstractStoreSelectionModel<GWTJahiaNode> getSelectionModel() {
        return selectionModel;
    }

    public void setContent(final Object root) {
        clearTable();
        this.root = (GWTJahiaNode) root;
        if (root != null) {
            loader.load();
        }
    }

    public void setProcessedContent(Object content, ContentSource source) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content;
            store.add(gwtJahiaNodes);
            updateTypeStore(gwtJahiaNodes);
            getLinker().onTableItemSelected();
        }
        this.contentSource = source;
    }

    public void selectNodes(List<GWTJahiaNode> nodes) {
        hiddenSelection = nodes;
    }

    public void clearTable() {
        // reset content source
        contentSource = null;
        store.removeAll();
    }

    public void refresh(AbstractView.ContentSource contentSource) {
        if (contentSource != null) {
            contentSource.refreshTable();
        } else {
            setContent(getLinker().getTreeSelection());
        }
    }

    @Override
    public void initWithLinker(final ManagerLinker linker) {
        super.initWithLinker(linker);

        selectionModel.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                if (event.getSelection() != null) { // && !event.getSelection().isEmpty() ) {
                    if (!event.getSelection().isEmpty()) {
                        hiddenSelection = event.getSelection();
                    }
                    visibleSelection = event.getSelection();
                }
                linker.onTableItemSelected();
            }
        });

    }

    @Override
    public void clearSelection() {
        super.clearSelection();
        if (hiddenSelection != null) {
            hiddenSelection.clear();
        }
        if (visibleSelection != null) {
            visibleSelection.clear();
        }

    }

    public ListStore<GWTJahiaNode> getStore() {
        return store;
    }

    public ListStore<ModelData> getTypeStore() {
        return typeStore;
    }

    public interface ContentSource {
        public void refreshTable();
    }

    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        if (selectedNode == null) {
            return true;
        }
        return (configuration.getForbiddenNodeTypesForDragAndDrop() == null || !selectedNode.isNodeType(configuration.getForbiddenNodeTypesForDragAndDrop()))
                && (configuration.getAllowedNodeTypesForDragAndDrop() == null || selectedNode.isNodeType(configuration.getAllowedNodeTypesForDragAndDrop()));

    }


    private static class TypeModelData extends BaseModelData {
        @Override
        public boolean equals(Object obj) {
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            Object o1 = get(GWTJahiaNode.PRIMARY_TYPE_LABEL);
            Object o2 = ((ModelData) obj).get(GWTJahiaNode.PRIMARY_TYPE_LABEL);
            if (o1 == o2)
                return true;
            if (o1 == null || o2 == null)
                return false;
            return o1.equals(o2);
        }

        @Override
        public int hashCode() {
            return get(GWTJahiaNode.PRIMARY_TYPE_LABEL) != null ? get(GWTJahiaNode.PRIMARY_TYPE_LABEL).hashCode() : 0;
        }
    }
}
