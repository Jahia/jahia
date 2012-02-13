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

package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleDropTarget;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.PagesTabItem;

import java.util.*;


/**
 * 
 * User: toto
 * Date: Aug 19, 2009
 * Time: 7:02:42 PM
 *
 */
public class EditModeDNDListener extends DNDListener {
    private EditLinker editLinker;

    public static final String SOURCE_TYPE = "sourceType";

    public static final String CONTENT_SOURCE_TYPE = "content";
    public static final String CREATE_CONTENT_SOURCE_TYPE = "createContent";

    public static final String QUERY_SOURCE_TYPE = "query";

    public static final String PAGETREE_TYPE = "pageTree";
    public static final String TEMPLATETREE_TYPE = "templateTree";
    public static final String BROWSETREE_TYPE = "browseTree";
    public static final String SIMPLEMODULE_TYPE = "simpleModule";
    public static final String PLACEHOLDER_TYPE = "placeholder";
    public static final String EMPTYAREA_TYPE = "emptyarea";

    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_REFERENCE_TYPE = "targetRefType";
    public static final String TARGET_PATH = "targetPath";
    public static final String TARGET_NODE = "targetNode";
    public static final String TARGET_NEXT_NODE = "targetNextNode";
    public static final String TARGET_PARENT = "targetParent";
    public static final String TARGET_CALLBACK = "callback";
    public static final String TYPE = "type";

    public static final String SOURCE_QUERY = "query";

    public static final String SOURCE_NODES = "sourceNodes";
    public static final String SOURCE_MODULE = "sourceModule";
    public static final String SOURCE_TEMPLATE = "sourceTemplate";
    public static final String SOURCE_NODETYPE = "sourceNodeType";
    public static final String OPERATION_CALLED = "operationCalled";

    public EditModeDNDListener(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    @Override
    public void dragDrop(final DNDEvent e) {
        final StatusProxy status = e.getStatus();
        if ("true".equals(status.getData(OPERATION_CALLED))) {
            return;
        }
        final String sourceType = status.getData(SOURCE_TYPE);
        final String targetType = status.getData(TARGET_TYPE);
        final String targetPath = status.getData(TARGET_PATH);
        final GWTJahiaNodeType sourceNodeType = status.getData(SOURCE_NODETYPE);
        final GWTJahiaNode targetNode = status.getData(TARGET_NODE);
        final List<String> referenceType = status.getData(TARGET_REFERENCE_TYPE);
        final List<GWTJahiaNode> sourceNodes = status.getData(SOURCE_NODES);
        final GWTJahiaSearchQuery searchQuery = status.getData(SOURCE_QUERY);

        AsyncCallback callback = new DropAsyncCallback();
        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
        if (EMPTYAREA_TYPE.equals(targetType)) {
            status.setData(OPERATION_CALLED, "true");
            GWTJahiaNode parent = (GWTJahiaNode) targetNode;
            ((AreaModule) ((ModuleDropTarget)e.getDropTarget()).getModule()).createNode(new BaseAsyncCallback<GWTJahiaNode>() {
                public void onSuccess(GWTJahiaNode result) {
                    e.getStatus().setData(SOURCE_TYPE, sourceType);
                    e.getStatus().setData(TARGET_TYPE, PLACEHOLDER_TYPE);
                    e.getStatus().setData(TARGET_PATH, "*");
                    e.getStatus().setData(SOURCE_NODETYPE, sourceNodeType);
                    e.getStatus().setData(TARGET_NODE, result);
                    e.getStatus().setData(TARGET_REFERENCE_TYPE, referenceType);
                    e.getStatus().setData(SOURCE_NODES, sourceNodes);
                    e.getStatus().setData(SOURCE_QUERY, searchQuery);
                    e.getStatus().setData(OPERATION_CALLED, "false");
                    dragDrop(e);
                }
            });
        } else if (PLACEHOLDER_TYPE.equals(targetType)) {
            int i = targetPath.lastIndexOf('/');
            String name = targetPath.substring(i + 1);
            String parentPath = targetNode.getPath();

            if (referenceType != null) {
                status.setData(OPERATION_CALLED, "true");
                GWTJahiaNode selectedNode = sourceNodes.get(0);
                if (selectedNode.getReferencedNode() != null) {
                    selectedNode = selectedNode.getReferencedNode();
                }
                Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(selectedNode.getDisplayName(),
                                GWTJahiaNodePropertyType.STRING)));
                props.put("j:node", new GWTJahiaNodeProperty("j:node",
                        new GWTJahiaNodePropertyValue(selectedNode,
                                GWTJahiaNodePropertyType.WEAKREFERENCE)));
                String nodeName = selectedNode.getName();
                if (name != null && !"*".equals(name)) {
                    nodeName = name;
                }
                ContentTypeWindow.createContent(editLinker, nodeName, referenceType, props, targetNode, true, false);
            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                GWTJahiaNode selectedNode = sourceNodes.get(0);

                status.setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    service.moveAtEnd(selectedNode.getPath(), parentPath, callback);
                } else {
                    if (!targetPath.startsWith("/")) {
                        // path is not absolute, let's build it.
                        service.move(selectedNode.getPath(), parentPath + "/" + targetPath, callback);
                    } else {
                        service.move(selectedNode.getPath(), targetPath, callback);
                    }
                }
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                status.setData(OPERATION_CALLED, "true");
                if ((sourceNodeType.getItems() == null || sourceNodeType.getItems().size() == 0) &&
                        (sourceNodeType.getInheritedItems() == null ||
                                sourceNodeType.getInheritedItems().size() == 0)) {
                    service.createNode(parentPath, null, sourceNodeType.getName(), null, null,
                            new ArrayList<GWTJahiaNodeProperty>(), null, new BaseAsyncCallback<GWTJahiaNode>() {
                                public void onApplicationFailure(Throwable throwable) {
                                    Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                    Log.error("failed", throwable);
                                }

                                public void onSuccess(GWTJahiaNode o) {
                                    Info.display(Messages.get("label.information", "Information"), Messages.get("message.success", "Node created"));
                                    editLinker.refresh(Linker.REFRESH_MAIN);
                                }
                            });
                } else {
                    EngineLoader.showCreateEngine(editLinker, targetNode, sourceNodeType,
                            new HashMap<String, GWTJahiaNodeProperty>(),
                            targetPath.substring(targetPath.lastIndexOf("/") + 1), false);
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                status.setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    service.saveSearch(searchQuery, parentPath, "jnt_query", false, callback);
                } else {
                    service.saveSearch(searchQuery, parentPath, name, false, callback);
                }
            }
        } else if (SIMPLEMODULE_TYPE.equals(targetType)) {
            if (referenceType != null) {
                status.setData(OPERATION_CALLED, "true");
                GWTJahiaNode selectedNode = sourceNodes.get(0);
                if (selectedNode.getReferencedNode() != null) {
                    selectedNode = selectedNode.getReferencedNode();
                }
                Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title",
                        new GWTJahiaNodePropertyValue(selectedNode.getDisplayName(),
                                GWTJahiaNodePropertyType.STRING)));
                props.put("j:node", new GWTJahiaNodeProperty("j:node",
                        new GWTJahiaNodePropertyValue(selectedNode,
                                GWTJahiaNodePropertyType.WEAKREFERENCE)));
                ContentTypeWindow.createContent(editLinker, selectedNode.getName(), referenceType, props, targetNode, true, true);
            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move

                status.setData(OPERATION_CALLED, "true");
                service.moveOnTopOf(sourceNodes.get(0).getPath(), targetPath, callback);
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                status.setData(OPERATION_CALLED, "true");
                if ((sourceNodeType.getItems() == null || sourceNodeType.getItems().size() == 0) &&
                        (sourceNodeType.getInheritedItems() == null ||
                                sourceNodeType.getInheritedItems().size() == 0)) {
                    service.createNodeAndMoveBefore(targetNode.getPath(), null, sourceNodeType.getName(), null, null,
                            new ArrayList<GWTJahiaNodeProperty>(), new HashMap<String, List<GWTJahiaNodeProperty>>(),
                            new BaseAsyncCallback<GWTJahiaNode>() {
                                public void onApplicationFailure(Throwable throwable) {
                                    Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                    Log.error("failed", throwable);
                                }

                                public void onSuccess(GWTJahiaNode o) {
                                    Info.display("", "Node created");
                                    editLinker.refresh(Linker.REFRESH_MAIN);
                                }
                            });
                } else {
                    EngineLoader.showCreateEngine(editLinker, targetNode, sourceNodeType,
                            new HashMap<String, GWTJahiaNodeProperty>(), "*", true);
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                status.setData(OPERATION_CALLED, "true");

                service.saveSearch(searchQuery, targetPath, "jnt_query", true, callback);
            }
        } else if (PAGETREE_TYPE.equals(targetType)) {
            if (PAGETREE_TYPE.equals(sourceType)) {
                status.setData(OPERATION_CALLED, "true");
                final GWTJahiaNode source = ((List<GWTJahiaNode>) sourceNodes).get(0);
                final GWTJahiaNode parent = status.getData(TARGET_PARENT);
                final int type = (Integer) status.getData(TYPE);
                callback = new BaseAsyncCallback() {
                    public void onSuccess(Object result) {
                        String selectedPath = editLinker.getSelectionContext().getMainNode().getPath();
                        String replacedPath;
                        if (type < 0) {
                            replacedPath = targetPath + "/" + source.getName();
                        } else {
                            replacedPath = selectedPath.replace(source.getPath(), parent.getPath() + "/" + source.getName());
                        }
                        if (!replacedPath.equals(selectedPath)) {
                            editLinker.onMainSelection(replacedPath, null, null);
                            editLinker.refresh(Linker.REFRESH_PAGES);
                        } else if (e.getDropTarget() instanceof PagesTabItem.PageTreeGridDropTarget) {
                            ((PagesTabItem.PageTreeGridDropTarget) e.getDropTarget()).getCallback().onSuccess(result);
                        }
                    }
                };

                if (status.<Object>getData("type").equals(-1)) {
                    service.moveAtEnd(source.getPath(), targetPath, callback);
                } else if (status.<Object>getData("type").equals(0)) {
                    service.moveOnTopOf(source.getPath(), targetPath, callback);
                } else if (status.<Object>getData("type").equals(1)) {
                    GWTJahiaNode node = status.getData(TARGET_NEXT_NODE);
                    if (node == null) {
                        service.moveAtEnd(source.getPath(), parent.getPath(), callback);
                    } else {
                        service.moveOnTopOf(source.getPath(), node.getPath(), callback);
                    }
                }
            }
        } else if (BROWSETREE_TYPE.equals(targetType)) {
            if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move

                status.setData(OPERATION_CALLED, "true");
                service.pasteReferences(JCRClientUtils.getPathesList(sourceNodes), targetPath, null, callback);
            }
        }
        super.dragDrop(e);
    }

    private class DropAsyncCallback implements AsyncCallback {
        public void onSuccess(Object o) {
            editLinker.getMainModule().refresh(Linker.REFRESH_MAIN);
        }

        public void onFailure(Throwable throwable) {
            Window.alert("Failed : " + throwable);
        }

    }

}
