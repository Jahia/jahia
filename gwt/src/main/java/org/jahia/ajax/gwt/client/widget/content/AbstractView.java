/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 27, 2010
 * Time: 6:27:01 PM
 * 
 */
public abstract class AbstractView extends TopRightComponent {
    protected GWTJahiaNode root;
    protected ListLoader<ListLoadResult<GWTJahiaNode>> loader;
    protected GWTManagerConfiguration configuration;
    protected List<GWTJahiaNode> hiddenSelection;
    protected List<GWTJahiaNode> visibleSelection;
    protected ListStore<GWTJahiaNode> store;
    protected AbstractStoreSelectionModel<GWTJahiaNode> selectionModel;

    public AbstractView(final GWTManagerConfiguration config) {
        configuration = config;
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object config, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children with type " + configuration.getNodeTypes() + " of " +
                        root.getPath());
                try {
                    JahiaContentManagementService.App.getInstance().lsLoad(root,
                            configuration.getAllNodeTypes(),
                            configuration.getMimeTypes(), configuration.getFilters(), configuration.getTableColumnKeys(),
                            false, -1, -1,getLinker().isDisplayHiddenTypes() , configuration.getHiddenTypes(), configuration.getHiddenRegex(), listAsyncCallback);
                } catch (org.jahia.ajax.gwt.client.service.GWTJahiaServiceException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
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
            }
        };

        store = new ListStore<GWTJahiaNode>(loader) {
            protected void onBeforeLoad(LoadEvent e) {
                if (getLinker() != null) {
                    getLinker().loading("listing directory content...");
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
                    return Collator.getInstance().localeCompare(s1,s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));
    }

    public List<GWTJahiaNode> getSelection() {
        List<GWTJahiaNode> elts = visibleSelection;
        if (elts != null && elts.size() > 0) {
            return elts;
        } else {
            return null;
        }
    }

    List<GWTJahiaNode> getHiddenSelection() {
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

    public void setProcessedContent(Object content) {
        clearTable();
        if (content != null) {
            List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) content;
            store.add(gwtJahiaNodes);
            getLinker().onTableItemSelected();
        }
    }

    public void selectNodes(List<GWTJahiaNode> nodes) {
        hiddenSelection = nodes;
    }

    public void clearTable() {
        store.removeAll();
    }

    public void refresh() {
        setContent(getLinker().getTreeSelection());
    }

    @Override public void initWithLinker(final ManagerLinker linker) {
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
}
