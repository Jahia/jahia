package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.selection.AbstractStoreSelectionModel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 27, 2010
 * Time: 6:27:01 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractView extends TopRightComponent {
    protected ListLoader<ListLoadResult<GWTJahiaNode>> loader;
    protected GWTManagerConfiguration configuration;
    protected List<GWTJahiaNode> hiddenSelection;
    protected List<GWTJahiaNode> visibleSelection;
    protected ListStore<GWTJahiaNode> store;
    protected AbstractStoreSelectionModel<GWTJahiaNode> selectionModel;

    public AbstractView(final GWTManagerConfiguration config) {
        configuration = config;
        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children with type " + configuration.getNodeTypes() + " of " +
                        ((GWTJahiaNode) gwtJahiaFolder).getPath());
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder,
                        configuration.getAllNodeTypes(),
                        configuration.getMimeTypes(), configuration.getFilters(), configuration.getTableColumnKeys(),
                        false, listAsyncCallback);
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
                    visibleSelection = new ArrayList<GWTJahiaNode>(hiddenSelection);
                    visibleSelection.retainAll(store.getModels());
                    if (visibleSelection.isEmpty()) {
                        getLinker().onTableItemSelected();
                    } else {
                        selectionModel.setSelection(visibleSelection);
                    }
                    for (GWTJahiaNode node : store.getModels()) {
                        node.setParent((TreeModel) gwtJahiaNode);
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

    public void setContent(final Object root) {
        clearTable();
        if (root != null) {
            loader.load(root);
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
