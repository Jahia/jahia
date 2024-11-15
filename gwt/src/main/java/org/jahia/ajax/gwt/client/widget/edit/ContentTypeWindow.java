/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.google.gwt.user.client.Event;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTHooks;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.ButtonItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;

import java.util.*;

/**
 *
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 12 nov. 2009
 */
public class ContentTypeWindow extends Window {
    private GWTJahiaNode parentNode;
    private final Linker linker;
    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    private ContentTypeTree contentTypeTree;

    public ContentTypeWindow(final Linker linker, GWTJahiaNode parent, List<GWTJahiaNodeType> components, final Map<String, GWTJahiaNodeProperty> props, final String nodeName, final boolean createInParentAndMoveBefore, final boolean skipRefreshOnSave, final boolean systemNameReadOnly) {
        addStyleName("content-type-window");
        this.linker = linker;
        this.parentNode = parent;
        setLayout(new FitLayout());
        setBodyBorder(false);
        setSize(400, 650);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        contentTypeTree = new ContentTypeTree();
        contentTypeTree.fillStore(components);
        TreeGrid<?> treeGrid = contentTypeTree.getTreeGrid();
        treeGrid.sinkEvents(Event.ONDBLCLICK + Event.ONCLICK);
        treeGrid.addListener(Events.OnDoubleClick, new Listener<TreeGridEvent<GWTJahiaNodeType>>() {

            @Override
            public void handleEvent(TreeGridEvent<GWTJahiaNodeType> baseEvent) {
                GWTJahiaNodeType gwtJahiaNodeType = baseEvent.getModel();
                if (gwtJahiaNodeType != null && linker != null && !gwtJahiaNodeType.isMixin()) {
                    EngineLoader.showCreateEngine(linker, parentNode, gwtJahiaNodeType, props, nodeName, createInParentAndMoveBefore, null, skipRefreshOnSave, systemNameReadOnly);
                    hide();
                }
            }
        });

        add(contentTypeTree);
        setFocusWidget(contentTypeTree.getNameFilterField());
        contentTypeTree.layout(true);
        layout();

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);
        final Window window = this;
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        ok = new Button(Messages.get("label.ok"));
        ok.addStyleName("button-save");
        ok.setHeight(ButtonItem.BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                GWTJahiaNodeType selectedItem = contentTypeTree.getTreeGrid().getSelectionModel().getSelectedItem();
                if (selectedItem != null && !selectedItem.isMixin()) {
                    EngineLoader.showCreateEngine(ContentTypeWindow.this.linker, parentNode, selectedItem, props, nodeName, createInParentAndMoveBefore, null, skipRefreshOnSave, systemNameReadOnly);
                    window.hide();
                }
            }
        });


        buttonBar.add(ok);

        cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(ButtonItem.BUTTON_HEIGHT);
        cancel.addStyleName("button-cancel");
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                window.hide();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        setBottomComponent(buttonsPanel);


        setFooter(true);
        ok.setEnabled(true);
    }

    public static void createContent(final Linker linker, final String name, final List<String> nodeTypes, final Map<String, GWTJahiaNodeProperty> props, final GWTJahiaNode targetNode, boolean includeSubTypes, final boolean createInParentAndMoveBefore) {
        createContent(linker, name, nodeTypes, props, targetNode, includeSubTypes, createInParentAndMoveBefore, null, false);
    }

    public static void createContent(final Linker linker, final String name, final List<String> nodeTypes, final Map<String, GWTJahiaNodeProperty> props, final GWTJahiaNode targetNode, boolean includeSubTypes, final boolean createInParentAndMoveBefore, final Set<String> displayedNodeTypes, final boolean skipRefreshOnSave) {
        createContent(linker, name, nodeTypes, props, targetNode, includeSubTypes, createInParentAndMoveBefore, displayedNodeTypes, skipRefreshOnSave, false);
    }

    public static void createContent(final Linker linker, final String name, final List<String> nodeTypes, final Map<String, GWTJahiaNodeProperty> props, final GWTJahiaNode targetNode, boolean includeSubTypes, final boolean createInParentAndMoveBefore, final Set<String> displayedNodeTypes, final boolean skipRefreshOnSave, final boolean systemNameReadOnly) {
        linker.loading(Messages.get("label.loading", "Loading"));
        List<String> excluded = new ArrayList<String>();
        if (linker.getConfig().getNonEditableTypes() != null) {
            excluded.addAll(linker.getConfig().getNonEditableTypes());
        }
        if (linker.getConfig().getNonVisibleTypes() != null) {
            excluded.addAll(linker.getConfig().getNonVisibleTypes());
        }
        if (linker.getConfig().getExcludedNodeTypes() != null) {
            excluded.addAll(linker.getConfig().getExcludedNodeTypes());
        }

        if (JahiaGWTHooks.hasHook("create")) {
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            params.put("path", targetNode.getPath());
            params.put("uuid", targetNode.getUUID());
            params.put("excludedNodeTypes", excluded);
            params.put("includeSubTypes", includeSubTypes);
            params.put("contentTypes", nodeTypes);
            try {
                JahiaGWTHooks.callHook("create", params);
            } catch (Exception e) {
                // Always work
                Log.error("Unable to execute hook Create because of ", e);
            }
            linker.loaded();
            return;
        }
        JahiaContentManagementService.App.getInstance().getContentTypesAsTree(nodeTypes, excluded, includeSubTypes, new BaseAsyncCallback<List<GWTJahiaNodeType>>() {

            @Override
            public void onSuccess(List<GWTJahiaNodeType> result) {
                linker.loaded();
                if (result.size() == 1 && result.get(0).getChildren().isEmpty()) {
                    EngineLoader.showCreateEngine(linker, targetNode, result.get(0), props,
                            name, createInParentAndMoveBefore, null, skipRefreshOnSave, systemNameReadOnly);

                } else {
                    if (nodeTypes != null && nodeTypes.size() == 1 && displayedNodeTypes != null) {
                        GWTJahiaNodeType targetNodeType = getTargetNodeType(nodeTypes.get(0), result, displayedNodeTypes);
                        if (targetNodeType != null) {
                            EngineLoader.showCreateEngine(linker, targetNode, targetNodeType, props, name, createInParentAndMoveBefore, null, skipRefreshOnSave, systemNameReadOnly);
                            return;
                        }
                    }
                    new ContentTypeWindow(linker, targetNode, result, props, name, createInParentAndMoveBefore, skipRefreshOnSave, systemNameReadOnly).show();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                linker.loaded();
                super.onFailure(caught);
            }

            private GWTJahiaNodeType getTargetNodeType(String nodeTypeName, List<GWTJahiaNodeType> result,
                                                       Set<String> displayedNodeTypes) {
                GWTJahiaNodeType targetNodeType = null;
                for (GWTJahiaNodeType nd : result) {
                    Object[] target = getTargetNodeType(nodeTypeName, nd, displayedNodeTypes);
                    if (!((Boolean) target[0])) {
                        return null;
                    } else {
                        if (targetNodeType == null && target[1] != null) {
                            targetNodeType = (GWTJahiaNodeType) target[1];
                        }
                    }
                }

                return targetNodeType;
            }

            private Object[] getTargetNodeType(String nodeTypeName, GWTJahiaNodeType startNode, Set<String> displayedNodeTypes) {
                boolean sinlgeTarget = true;
                GWTJahiaNodeType targetNodeType = null;
                if (startNode.getChildren().size() > 0) {
                    for (ModelData child : startNode.getChildren()) {
                        Object[] result = getTargetNodeType(nodeTypeName, (GWTJahiaNodeType) child, displayedNodeTypes);
                        if (!((Boolean) result[0])) {
                            return result;
                        }
                        if (targetNodeType == null && result[1] != null) {
                            targetNodeType = (GWTJahiaNodeType) result[1];
                        }
                    }
                } else if (!startNode.isMixin()) {
                    if (nodeTypeName.equals(startNode.getName())) {
                        targetNodeType = startNode;
                    }
                    if (!displayedNodeTypes.contains(startNode.getName())) {
                        sinlgeTarget = false;
                    }
                }
                return new Object[]{sinlgeTarget, targetNodeType};
            }
        });
    }
}
