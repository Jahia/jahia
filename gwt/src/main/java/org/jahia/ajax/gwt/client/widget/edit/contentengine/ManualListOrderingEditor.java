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
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;

import java.util.*;

/**
 * GWT control for performing manual list item ordering and deletion of a single or multiple items.
 * User: ktlili
 * Date: Apr 22, 2010
 * Time: 5:42:18 PM
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

    public List<GWTJahiaNode> getOrderedNodes() {
        return childrenGrid.getStore().getModels();
    }

    private void init() {
        setBodyBorder(true);
        List<GWTColumn> columnNames = new ArrayList<GWTColumn>();
        columnNames.add(new GWTColumn("icon","icon",50));
        columnNames.add(new GWTColumn("name","name",100));
        columnNames.add(new GWTColumn("created","created",75));
        columnNames.add(new GWTColumn("createdBy","createdBy",75));
        columnNames.add(new GWTColumn("lastModified","lastModified",75));
        columnNames.add(new GWTColumn("lastModifiedBy","lastModifiedBy",75));


        final NodeColumnConfigList columnConfigList = new NodeColumnConfigList(columnNames);
        columnConfigList.init();
        final CheckBoxSelectionModel<GWTJahiaNode> sm = new CheckBoxSelectionModel<GWTJahiaNode>();

        columnConfigList.add(0, new RowNumberer());
        columnConfigList.add(1, sm.getColumn());

        // data proxy
        RpcProxy<ListLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<ListLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<ListLoadResult<GWTJahiaNode>> listAsyncCallback) {
                Log.debug("retrieving children of " + ((GWTJahiaNode) gwtJahiaFolder).getPath());
                JahiaContentManagementService.App.getInstance().lsLoad((GWTJahiaNode) gwtJahiaFolder, JCRClientUtils.CONTENT_NODETYPES, null, null,
                        Arrays.asList("icon","name","created","createdBy","lastModified","lastModifiedBy"), listAsyncCallback);
                childrenGrid.unmask();
            }


        };

        loader = new BaseListLoader<ListLoadResult<GWTJahiaNode>>(privateProxy);

        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>(loader);

        childrenGrid = new Grid<GWTJahiaNode>(store, new ColumnModel(columnConfigList));
        childrenGrid.setBorders(true);
        childrenGrid.setHeight(400);
        new GridDragSource(childrenGrid);

        GridDropTarget target = new GridDropTarget(childrenGrid);
        target.setAllowSelfAsSource(true);
        target.setFeedback(DND.Feedback.INSERT);


        childrenGrid.mask();

        if (node != null) {
            loader.load(node);
        }

        final ToolBar toolBar = new ToolBar();
        Button moveUp = new Button(Messages.get("label_moveUp", "move up"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node);
                }
                childrenGrid.getView().refresh(false);
            }

            public void execute(GWTJahiaNode selectedNode) {
                // find a better way to get index
                int index = childrenGrid.getStore().indexOf(selectedNode);
                if (index > 0) {
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index - 1);
                    childrenGrid.getSelectionModel().select(index - 1, true);
                }
            }


        });
        moveUp.setIcon(StandardIconsProvider.STANDARD_ICONS.moveUp());
        toolBar.add(moveUp);

        Button moveFirst = new Button(Messages.get("label_moveFirst", "move first"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                int iteration = 0;
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node, iteration);
                    iteration++;
                }
            }

            public void execute(GWTJahiaNode node, int index) {
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, index);
                childrenGrid.getSelectionModel().select(index, true);
                childrenGrid.getView().refresh(false);
            }
        });
        moveFirst.setIcon(StandardIconsProvider.STANDARD_ICONS.moveFirst());
        toolBar.add(moveFirst);

        Button moveDown = new Button(Messages.get("label_moveDown", "move down"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<GWTJahiaNode> ordered = getOrderedSelectedList();
                Collections.reverse(ordered);
                for (GWTJahiaNode node : ordered) {
                    execute(node);
                }
            }

            public void execute(GWTJahiaNode selectedNode) {
                // find a better way to get index
                int index = childrenGrid.getStore().indexOf(selectedNode);
                if (index < childrenGrid.getStore().getCount() - 1) {
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index + 1);
                    childrenGrid.getSelectionModel().select(index + 1, true);
                    childrenGrid.getView().refresh(false);
                }
            }
        });
        moveDown.setIcon(StandardIconsProvider.STANDARD_ICONS.moveDown());
        toolBar.add(moveDown);

        Button moveLast = new Button(Messages.get("label_moveLast", "move last"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                List<GWTJahiaNode> ordered = getOrderedSelectedList();
                Collections.reverse(ordered);
                int index = childrenGrid.getStore().getCount() - 1;
                for (GWTJahiaNode node : ordered) {
                    execute(node, index);
                    index--;
                }
                childrenGrid.getSelectionModel().setSelection(childrenGrid.getSelectionModel().getSelection());
                childrenGrid.getView().refresh(false);
            }


            public void execute(GWTJahiaNode node, int index) {
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, index);
                childrenGrid.getSelectionModel().select(index, true);

            }
        });
        moveLast.setIcon(StandardIconsProvider.STANDARD_ICONS.moveLast());
        toolBar.add(moveLast);

        Button remove = new Button(Messages.get("label_remove", "Delete"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                for (GWTJahiaNode node : childrenGrid.getSelectionModel().getSelectedItems()) {
                    childrenGrid.getStore().remove(node);
                }
                childrenGrid.getView().refresh(false);
            }
        });
        remove.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
        toolBar.add(remove);

        setLayout(new FitLayout());
        setHeaderVisible(false);
        setTopComponent(toolBar);
        childrenGrid.setSelectionModel(sm);
        childrenGrid.setBorders(true);
        childrenGrid.addPlugin(sm);
        add(childrenGrid);
    }

    /**
     * Get ordered selected list
     *
     * @return
     */
    private List<GWTJahiaNode> getOrderedSelectedList() {
        List<GWTJahiaNode> selectedNodes = childrenGrid.getSelectionModel().getSelection();
        Comparator<GWTJahiaNode> c = new Comparator<GWTJahiaNode>() {
            public int compare(GWTJahiaNode gwtJahiaNode, GWTJahiaNode gwtJahiaNode1) {
                int index = childrenGrid.getStore().indexOf(gwtJahiaNode);
                int index2 = childrenGrid.getStore().indexOf(gwtJahiaNode1);
                if (index == index2) {
                    return 0;
                }

                if (index > index2) {
                    return 1;
                }

                return -1;
            }
        };
        Collections.sort(selectedNodes, c);
        return selectedNodes;
    }

}
