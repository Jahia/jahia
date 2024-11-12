/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.GridDropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeSelectorType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.WindowUtil;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.*;

/**
 * Table view component for the content manager widget.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class TableView extends AbstractView {
    private Grid<GWTJahiaNode> m_grid;

    public TableView(final GWTManagerConfiguration config) {
        super(config);

        final NodeColumnConfigList columns = new NodeColumnConfigList(configuration.getTableColumns());
        columns.init();
        CheckBoxSelectionModel<GWTJahiaNode> checkboxSelectionModel = null;
        if (configuration.isAllowsMultipleSelection()) {
            checkboxSelectionModel = new CheckBoxSelectionModel<GWTJahiaNode>();
            columns.add(0, checkboxSelectionModel.getColumn());
        }

        if (configuration.isEditableGrid()) {
            m_grid = new EditorGrid<GWTJahiaNode>(store, new ColumnModel(columns));
        } else {
            m_grid = new Grid<GWTJahiaNode>(store, new ColumnModel(columns));
        }

        m_grid.setBorders(true);
        if (columns.getAutoExpand() != null) {
            m_grid.setAutoExpandColumn(columns.getAutoExpand());
            store.setSortField(columns.getAutoExpand());
            m_grid.setAutoExpandMax(1000);
        }

        if (checkboxSelectionModel != null) {
            m_grid.setSelectionModel(checkboxSelectionModel);
            m_grid.addPlugin(checkboxSelectionModel);
        }
        selectionModel = m_grid.getSelectionModel();
        if (configuration.isAllowsMultipleSelection()) {
            selectionModel.setSelectionMode(Style.SelectionMode.MULTI);
        } else {
            selectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
        }
        m_grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                List<GWTJahiaNode> sel = selectionModel.getSelectedItems();
                if (sel != null && sel.size() == 1) {
                    GWTJahiaNode el = sel.get(0);
                    if (el.isFile()) {
                        BottomRightComponent buttonBar = getLinker().getBottomRightObject();
                        if (buttonBar instanceof PickedContentView) {
                            ((PickedContentView) buttonBar).setSelection(sel);
                            ((PickedContentView) buttonBar).getSaveButton().fireEvent(Events.Select);
                            return;
                        }
                        if (config.isEnableFileDoubleClick()) {
                            if (el.isDisplayable()) {
                                ImagePopup.popImage(el, getLinker());
                            } else {
                                ContentActions.download(getLinker());
                            }
                        }
                    } else {
                        if (!config.isHideLeftPanel()) {
                            getLinker().onTableItemDoubleClicked(sel.get(0));
                            setContent(sel.get(0));
                        } else {
                            Log.debug("Double click disable if left panle is hidden");
                        }
                    }
                }
            }
        });

        if (config.isEditableGrid()) {
            loader.addLoadListener(new LoadListener() {
                @Override
                public void loaderLoad(LoadEvent le) {
                    BasePagingLoadResult<GWTJahiaNode> r = le.getData();
                    List<String> nodeTypes = null;
                    for (GWTJahiaNode node : r.getData()) {
                        List<String> currentNodeTypes = new ArrayList<String>(node.getNodeTypes());
                        currentNodeTypes.addAll(node.getInheritedNodeTypes());
                        if (nodeTypes == null) {
                            nodeTypes = currentNodeTypes;
                        } else {
                            nodeTypes.retainAll(currentNodeTypes);
                        }
                    }
                    generateEditableColumns(columns, nodeTypes);
                }
            });
        }
    }

    private void generateEditableColumns(final NodeColumnConfigList columns, List<String> nodeTypes) {
        JahiaContentManagementService.App.getInstance().getNodeTypes(nodeTypes, new AsyncCallback<List<GWTJahiaNodeType>>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(List<GWTJahiaNodeType> result) {
                ColumnModel columnModel = new ColumnModel(columns);
                for (GWTJahiaNodeType gwtJahiaNodeType : result) {
                    for (GWTJahiaItemDefinition definition : gwtJahiaNodeType.getItems()) {
                        if (definition.getDataType().equals("content") && !definition.isHidden() && !definition.isProtected()) {
                            ColumnConfig e = new ColumnConfig(definition.getName(), definition.getLabel(), 200);
                            switch (definition.getSelector()) {
                                case GWTJahiaNodeSelectorType.SMALLTEXT:
                                    break;
                                case GWTJahiaNodeSelectorType.TEXTAREA:
                                    break;
                                case GWTJahiaNodeSelectorType.RICHTEXT:
                                    continue;
                                case GWTJahiaNodeSelectorType.DATETIMEPICKER:
                                case GWTJahiaNodeSelectorType.DATEPICKER:
                                    e.setWidth(100);
                                    break;
                                case GWTJahiaNodeSelectorType.CHECKBOX:
                                case GWTJahiaNodeSelectorType.COLOR:
                                case GWTJahiaNodeSelectorType.CRON:
                                case GWTJahiaNodeSelectorType.CATEGORY:
                                    break;
                                case GWTJahiaNodeSelectorType.PICKER:
                                    e.setRenderer(new GridCellRenderer<GWTJahiaNode>() {
                                        public Object render(GWTJahiaNode model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaNode> store, Grid<GWTJahiaNode> grid) {
                                            return ((String)model.get(property)).substring((((String) model.get(property)).indexOf("/")));
                                        }
                                    });
                                    break;
                                case GWTJahiaNodeSelectorType.CHOICELIST:
                                    break;                                    
                            }
                            Field f = FormFieldCreator.createField(definition, null, null, false, root.getPermissions(), null);
                            if (f != null) {
                                e.setEditor(new CellEditor(f));
                                columnModel.getColumns().add(e);
                            }
                        }
                    }
                }
                m_grid.reconfigure(m_grid.getStore(), columnModel);
            }
        });
    }

    @Override
    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        if (getLinker().getDndListener() != null) {
            GridDragSource source = new GridDragSource(m_grid) {
                @Override
                protected void onDragDrop(DNDEvent e) {
                }

                @Override
                protected void onDragStart(DNDEvent e) {
                    super.onDragStart(e);
                    List<GWTJahiaNode> l = e.getData();
                    if (l != null) {
                        for (GWTJahiaNode node : l) {
                            if (!isNodeTypeAllowed(node) || !PermissionsUtils.isPermitted("jcr:removeNode", node.getPermissions())) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            };
            source.addDNDListener(linker.getDndListener());

            GridDropTarget target = new MyGridDropTarget(m_grid);
            target.setAllowSelfAsSource(true);
            target.addDNDListener(linker.getDndListener());
        }
    }

    public void setContextMenu(Menu menu) {
        m_grid.setContextMenu(menu);
    }

    public Component getComponent() {
        return m_grid;
    }

    private class MyGridDropTarget extends GridDropTarget {
        private boolean before;

        public MyGridDropTarget(Grid grid) {
            super(grid);
        }

        @Override
        protected void showFeedback(DNDEvent event) {
            final GWTJahiaNode node = root;
            if ("index".equals(store.getSortField()) && store.getSortDir() == Style.SortDir.ASC &&
                    Boolean.TRUE.equals(node.get("hasOrderableChildNodes"))) {
                feedback = DND.Feedback.INSERT;
            } else {
                feedback = DND.Feedback.APPEND;
            }
            event.getStatus().setStatus(true);

            Element row = grid.getView().findRow(event.getTarget()).cast();

            if (row != null) {
                if (feedback == DND.Feedback.INSERT) {
                    int height = row.getOffsetHeight();
                    int quarter = height / 4;
                    int top = WindowUtil.getAbsoluteTop(row);
                    int y = event.getClientY();

                    before = y < (top + quarter);
                    boolean after = y > (top + 3 * quarter);

                    if (before || after) {
                        int idx = grid.getView().findRowIndex(row);

                        showInsert(event, row);

                        if (after) {
                            activeItem = grid.getStore().getAt(idx + 1);
                            before = true;
                        } else {
                            activeItem = grid.getStore().getAt(idx);
                        }

                        insertIndex = adjustIndex(event, idx);
                    } else {
                        activeItem = grid.getStore().getAt(grid.getView().findRowIndex(row));
                        Insert.get().hide();
                    }
                } else {
                    activeItem = grid.getStore().getAt(grid.getView().findRowIndex(row));
                }
            } else {
                activeItem = null;
                before = false;
                insertIndex = 0;
                if (feedback == DND.Feedback.INSERT) {
                    showInsert(event, (Element) grid.getView().getRow(grid.getStore().getCount() - 1));
                }
            }
            if (!before) {
                GWTJahiaNode target;
                if (activeItem != null) {
                    target = (GWTJahiaNode) activeItem;
                } else {
                    target = node;
                }
                event.getStatus().setStatus(checkTarget(((List<GWTJahiaNode>) event.getData()).get(0), target));
            }
        }

        @Override
        protected void onDragDrop(DNDEvent dndEvent) {
            if (dndEvent.getStatus().getStatus()) {

                final BaseAsyncCallback callback = new BaseAsyncCallback() {
                    public void onSuccess(Object o) {
                        getLinker().loaded();
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_ALL, true);
                        getLinker().refresh(data);
                    }
                };

                final List<GWTJahiaNode> gwtJahiaNodes = (List<GWTJahiaNode>) dndEvent.getData();
                List<String> sources = new ArrayList<String>();
                for (GWTJahiaNode n :gwtJahiaNodes) {
                    sources.add(n.getPath());
                }
                if (activeItem != null) {
                    final GWTJahiaNode target = (GWTJahiaNode) activeItem;
                    if (before) {
                        JahiaContentManagementService.App.getInstance().moveOnTopOf(sources, target.getPath(), callback);
                    } else {
                        JahiaContentManagementService.App.getInstance().moveAtEnd(sources, target.getPath(), callback);
                    }
                } else {
                    final GWTJahiaNode target = root;
                    JahiaContentManagementService.App.getInstance().moveAtEnd(sources, target.getPath(), callback);
                }
            }
        }

        private boolean checkTarget(GWTJahiaNode source, GWTJahiaNode target) {
            final Set<String> constraints = new HashSet(Arrays.asList(target.getChildConstraints().split(" ")));
            constraints.retainAll(source.getInheritedNodeTypes());
            return !constraints.isEmpty();
        }

        private int adjustIndex(DNDEvent event, int index) {
            Object data = event.getData();
            List<ModelData> models = prepareDropData(data, true);
            for (ModelData m : models) {
                int idx = grid.getStore().indexOf(m);
                if (idx > -1 && (before ? idx < index : idx <= index)) {
                    index--;
                }
            }
            return before ? index : index + 1;
        }

        private void showInsert(DNDEvent event, Element row) {
            Insert insert = Insert.get();
            insert.show(row);
            Rectangle rect = El.fly(row).getBounds();
            int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
            insert.el().setBounds(rect.x, y, rect.width, 6);
        }

    }
}
