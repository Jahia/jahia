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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.*;
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
    private List<GWTJahiaNode> removedNodes = new ArrayList<GWTJahiaNode>();

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

    public List<GWTJahiaNode> getRemovedNodes() {
        return removedNodes;
    }

    private void init() {
        setBodyBorder(true);
        List<GWTColumn> columnNames = new ArrayList<GWTColumn>();
        columnNames.add(new GWTColumn(GWTJahiaNode.ICON,Messages.get("label.icon"),30));
        columnNames.add(new GWTColumn(GWTJahiaNode.DISPLAY_NAME,Messages.get("label.title"),350));
        columnNames.add(new GWTColumn(GWTJahiaNode.NAME,Messages.get("label.name"),100));
        columnNames.add(new GWTColumn("jcr:created",Messages.get("label.created"),100));
        columnNames.add(new GWTColumn("jcr:createdBy",Messages.get("column.createdBy.label"),75));
        columnNames.add(new GWTColumn("jcr:lastModified",Messages.get("label.lastModif"),100));
        columnNames.add(new GWTColumn("jcr:lastModifiedBy",Messages.get("column.modifiedBy.label"),75));


        final NodeColumnConfigList columnConfigList = new NodeColumnConfigList(columnNames);
        columnConfigList.init();
        final CheckBoxSelectionModel<GWTJahiaNode> sm = new CheckBoxSelectionModel<GWTJahiaNode>();

        columnConfigList.add(0, new RowNumberer());
        columnConfigList.add(1, sm.getColumn());

        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            protected void load(Object gwtJahiaFolder, AsyncCallback<PagingLoadResult<GWTJahiaNode>> listAsyncCallback) {
                String path = ((GWTJahiaNode) gwtJahiaFolder).getPath();
                Log.debug("retrieving children of " + path);
                JahiaContentManagementService.App.getInstance().lsLoad(path, JCRClientUtils.MANUALLY_ORDERABLE_NODETYPES, null, null,
                        Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.DISPLAY_NAME, GWTJahiaNode.NAME, "jcr:created", "jcr:createdBy", "jcr:lastModified", "jcr:lastModifiedBy"), false, -1, -1, false, null, null,false, false, listAsyncCallback);
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
        Button moveUp = new Button(Messages.get("label.move.up", "move up"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node);
                }
                childrenGrid.getView().refresh(false);
            }

            public void execute(GWTJahiaNode selectedNode) {
                // find a better way to get index
                removeSorter();
                int index = childrenGrid.getStore().indexOf(selectedNode);
                if (index > 0) {
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index - 1);
                    childrenGrid.getSelectionModel().select(index - 1, true);
                }
            }


        });
        moveUp.setIcon(StandardIconsProvider.STANDARD_ICONS.moveUp());
        moveUp.addStyleName("button-moveup");
        toolBar.add(moveUp);

        Button moveFirst = new Button(Messages.get("label.move.first", "move first"), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                int iteration = 0;
                for (GWTJahiaNode node : getOrderedSelectedList()) {
                    execute(node, iteration);
                    iteration++;
                }
            }

            public void execute(GWTJahiaNode node, int index) {
                removeSorter();
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, index);
                childrenGrid.getSelectionModel().select(index, true);
                childrenGrid.getView().refresh(false);
            }
        });
        moveFirst.setIcon(StandardIconsProvider.STANDARD_ICONS.moveFirst());
        moveFirst.addStyleName("button-movefirst");
        toolBar.add(moveFirst);

        Button moveDown = new Button(Messages.get("label.move.down", "move down"), new SelectionListener<ButtonEvent>() {
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
                    removeSorter();
                    childrenGrid.getStore().remove(selectedNode);
                    childrenGrid.getStore().insert(selectedNode, index + 1);
                    childrenGrid.getSelectionModel().select(index + 1, true);
                    childrenGrid.getView().refresh(false);
                }
            }
        });
        moveDown.setIcon(StandardIconsProvider.STANDARD_ICONS.moveDown());
        moveDown.addStyleName("button-movedown");
        toolBar.add(moveDown);

        Button moveLast = new Button(Messages.get("label.move.last", "move last"), new SelectionListener<ButtonEvent>() {
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
                removeSorter();
                childrenGrid.getStore().remove(node);
                childrenGrid.getStore().insert(node, index);
                childrenGrid.getSelectionModel().select(index, true);

            }
        });
        moveLast.setIcon(StandardIconsProvider.STANDARD_ICONS.moveLast());
        moveLast.addStyleName("button-movelast");
        toolBar.add(moveLast);

        setLayout(new FitLayout());
        setHeaderVisible(false);
        setTopComponent(toolBar);
        childrenGrid.setSelectionModel(sm);
        childrenGrid.setBorders(true);
        childrenGrid.addPlugin(sm);
        add(childrenGrid);
    }

    private void removeSorter() {
        if (childrenGrid.getStore().getStoreSorter() != null ) {
            childrenGrid.getStore().setSortField(null);
            childrenGrid.getStore().setStoreSorter(null);
            childrenGrid.getView().refresh(true);
        }
        else {
            childrenGrid.getView().refresh(false);
        }
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
