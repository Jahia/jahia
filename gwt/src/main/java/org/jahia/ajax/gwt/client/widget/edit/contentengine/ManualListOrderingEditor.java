package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowNumberer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 22, 2010
 * Time: 5:42:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManualListOrderingEditor extends ContentPanel {
    private GWTJahiaNode node;
    private ListLoader<ListLoadResult<GWTJahiaNode>> loader;
    private Grid<GWTJahiaNode> childrenGrid;

    public ManualListOrderingEditor() {
        this(null);
    }

    public ManualListOrderingEditor(GWTJahiaNode node) {
        super();
        this.node = node;
        init();
    }

    public List<GWTJahiaNode> getOrderedNodes(){
       return childrenGrid.getStore().getModels();
    }

    private void init() {
        setBodyBorder(true);
        List<String> columnNames = new ArrayList<String>();
        //columnNames.add("providerKey");
        columnNames.add("ext");
        columnNames.add("name");
        //columnNames.add("locked");
        //columnNames.add("path");
        //columnNames.add("size");
        //columnNames.add("publicationInfo");
        //columnNames.add("created");
        //columnNames.add("createdBy");
        columnNames.add("lastModified");
        //columnNames.add("lastModifiedBy");
        //columnNames.add("lastPublished");
        //columnNames.add("lastPublishedBy");

        final List<ColumnConfig> columnConfigList = new NodeColumnConfigList(columnNames);
        columnConfigList.add(0, new RowNumberer());

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getPath());
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, "jnt:content", null, null, true, listAsyncCallback);
                childrenGrid.unmask();
            }
        };

        loader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(privateProxy);

        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(loader);

        childrenGrid = new Grid<GWTJahiaNode>(store, new ColumnModel(columnConfigList));
        childrenGrid.setBorders(true);
        childrenGrid.setHeight(200);
        new GridDragSource(childrenGrid);

        GridDropTarget target = new GridDropTarget(childrenGrid);
        target.setAllowSelfAsSource(true);
        target.setFeedback(DND.Feedback.INSERT);


        childrenGrid.mask();

        if (node != null) {
            loader.load(node);
        }

        ToolBar toolBar = new ToolBar();

        Button moveUp = new Button(Messages.get("label_moveUp", "move up"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final GWTJahiaNode selectedNode = childrenGrid.getSelectionModel().getSelectedItem();

                // find a better way to get index
                int index = getIndex(selectedNode);
                if (index > 0) {
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index - 1);
                    childrenGrid.getSelectionModel().select(index - 1, false);
                    childrenGrid.getView().refresh(false);
                }
            }

            /**
             * Get index of a node
             * @param selectedNode
             * @return
             */
            private int getIndex(GWTJahiaNode selectedNode) {
                int index = 0;
                for (GWTJahiaNode currentNode : childrenGrid.getStore().getModels()) {
                    if (currentNode == selectedNode) {
                        break;
                    }
                    index++;
                }
                return index;
            }
        });
        moveUp.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
        toolBar.add(moveUp);

        Button moveFirst = new Button(Messages.get("label_moveFirst", "move first"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWTJahiaNode node = childrenGrid.getSelectionModel().getSelectedItem();
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, 0);
                childrenGrid.getSelectionModel().select(0, false);
                childrenGrid.getView().refresh(false);
            }
        });
        moveFirst.setIcon(ContentModelIconProvider.getInstance().getPlusRound());
        toolBar.add(moveFirst);

        Button moveDown = new Button(Messages.get("label_moveDown", "move down"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                final GWTJahiaNode selectedNode = childrenGrid.getSelectionModel().getSelectedItem();

                // find a better way to get index
                int index = getIndex(selectedNode);
                if (index < childrenGrid.getStore().getCount() - 1) {
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index + 1);
                    childrenGrid.getSelectionModel().select(index + 1, false);
                    childrenGrid.getView().refresh(false);
                }
            }

            /**
             * Get index of a node
             * @param selectedNode
             * @return
             */
            private int getIndex(GWTJahiaNode selectedNode) {
                int index = 0;
                for (GWTJahiaNode currentNode : childrenGrid.getStore().getModels()) {
                    if (currentNode == selectedNode) {
                        break;
                    }
                    index++;
                }
                return index;
            }
        });
        moveDown.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
        toolBar.add(moveDown);

        Button moveLast = new Button(Messages.get("label_moveLast", "move last"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWTJahiaNode node = childrenGrid.getSelectionModel().getSelectedItem();
                int lastIndex = childrenGrid.getStore().getCount() - 1;
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, lastIndex);
                childrenGrid.getSelectionModel().select(lastIndex, false);
                childrenGrid.getView().refresh(false);
            }
        });
        moveLast.setIcon(ContentModelIconProvider.getInstance().getMinusRound());
        toolBar.add(moveLast);

        setLayout(new FitLayout());
        setHeaderVisible(false);
        setTopComponent(toolBar);
        add(childrenGrid);
    }

}
