/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
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
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.PagesTabItem;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 7:02:42 PM
 * To change this template use File | Settings | File Templates.
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

    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_REFERENCE_TYPE = "targetRefType";
    public static final String TARGET_PATH = "targetPath";
    public static final String TARGET_NODE = "targetNode";
    public static final String TARGET_NEXT_NODE = "targetNextNode";
    public static final String TARGET_PARENT = "targetParent";
    public static final String TARGET_CALLBACK = "callback";

    public static final String SOURCE_QUERY = "query";

    public static final String SOURCE_NODES = "sourceNodes";
    public static final String SOURCE_TEMPLATE = "sourceTemplate";
    public static final String SOURCE_NODETYPE = "sourceNodeType";
    public static final String OPERATION_CALLED = "operationCalled";

    public EditModeDNDListener(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    @Override
    public void dragDrop(final DNDEvent e) {
        if ("true".equals(e.getStatus().getData(OPERATION_CALLED))) {
            return;
        }
        final String sourceType = e.getStatus().getData(SOURCE_TYPE);
        final String targetType = e.getStatus().getData(TARGET_TYPE);

        AsyncCallback callback = new DropAsyncCallback();
        final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        if (PLACEHOLDER_TYPE.equals(targetType)) {
            final String targetPath = e.getStatus().getData(TARGET_PATH);
            int i = targetPath.lastIndexOf('/');
            String name = targetPath.substring(i + 1);
            final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
            String parentPath = parent.getPath();

            if (e.getStatus().getData(EditModeDNDListener.TARGET_REFERENCE_TYPE) != null) {
                e.getStatus().setData(OPERATION_CALLED, "true");
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                final GWTJahiaNode selectedNode = nodes.get(0);
                JahiaContentDefinitionService.App.getInstance().getNodeTypes((List<String>) e.getStatus().getData(EditModeDNDListener.TARGET_REFERENCE_TYPE), new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onApplicationFailure(Throwable caught) {
                        Window.alert("Cannot retrieve node type. Cause: " + caught.getLocalizedMessage());
                        Log.error("Cannot retrieve node type. Cause: " + caught.getLocalizedMessage(), caught);
                    }

                    public void onSuccess(List<GWTJahiaNodeType> result) {
                        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                        props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(selectedNode.getDisplayName(), GWTJahiaNodePropertyType.STRING)));
                        props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(selectedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                        if (result.size() == 1) {
                            EngineLoader.showCreateEngine(editLinker, parent, result.get(0), props, selectedNode.getName(), false);
                        } else {
                            Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> m = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
                            m.put(null, result);
                            new ContentTypeWindow(editLinker, parent, m, props, selectedNode.getName(), false).show();
                        }
                    }
                });
//            } else if (CONTENT_SOURCE_TYPE.equals(sourceType)) {
//                // Existing item from content list
//                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
//
//                e.getStatus().setData(OPERATION_CALLED, "true");
//                async.pasteReferences(JCRClientUtils.getPathesList(nodes), parentPath, null, callback);
            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                GWTJahiaNode selectedNode = nodes.get(0);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    async.moveAtEnd(selectedNode.getPath(), parentPath, callback);
                } else {
                    async.move(selectedNode.getPath(), targetPath, callback);
                }
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                GWTJahiaNodeType type = e.getStatus().getData(SOURCE_NODETYPE);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ((type.getItems() == null || type.getItems().size() == 0) && (type.getInheritedItems() == null || type.getInheritedItems().size() == 0)) {
                    async.createNode(parent.getPath(), null,
                            type.getName(), null, null,
                            new ArrayList<GWTJahiaNodeProperty>(),
                            new BaseAsyncCallback<GWTJahiaNode>() {
                                public void onApplicationFailure(Throwable throwable) {
                                    Window.alert( "Properties save failed\n\n" + throwable.getLocalizedMessage());
                                    Log.error( "failed", throwable);
                                }

                                public void onSuccess(GWTJahiaNode o) {
                                    Info.display("", "Node created");
                                    editLinker.refresh(Linker.REFRESH_MAIN);
                                }
                            });
                } else {
                    EngineLoader.showCreateEngine(editLinker, parent, type, new HashMap<String, GWTJahiaNodeProperty>(),
                            targetPath.substring(targetPath.lastIndexOf(
                                                                    "/") + 1), false);
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                GWTJahiaSearchQuery q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    async.saveSearch(q, parentPath, "jnt_query", false, callback);
                } else {
                    async.saveSearch(q, parentPath, name, false,  callback);
                }
            }
        } else if (SIMPLEMODULE_TYPE.equals(targetType)) {
            final String targetPath = e.getStatus().getData(TARGET_PATH);

            if (e.getStatus().getData(EditModeDNDListener.TARGET_REFERENCE_TYPE) != null) {
                final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
                e.getStatus().setData(OPERATION_CALLED, "true");
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                final GWTJahiaNode selectedNode = nodes.get(0);
                JahiaContentDefinitionService.App.getInstance().getNodeTypes((List<String>) e.getStatus().getData(EditModeDNDListener.TARGET_REFERENCE_TYPE), new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onApplicationFailure(Throwable caught) {
                        Window.alert(
                                "Cannot retrieve node type. Cause: " + caught.getLocalizedMessage());
                        Log.error("Cannot retrieve node type. Cause: " + caught.getLocalizedMessage(),
                                caught);
                    }

                    public void onSuccess(List<GWTJahiaNodeType> result) {
                        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                        props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(selectedNode.getDisplayName(), GWTJahiaNodePropertyType.STRING)));
                        props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(selectedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                        if (result.size() == 1) {
                            EngineLoader.showCreateEngine(editLinker, parent, result.get(0), props, selectedNode.getName(), true);
                        } else {
                            Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> m = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
                            m.put(null, result);
                            new ContentTypeWindow(editLinker, parent, m, props, selectedNode.getName(), true).show();
                        }
                    }
                });
//            } else if (CONTENT_SOURCE_TYPE.equals(sourceType)) {
//                // Drop into an existing module
//                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
//
//                e.getStatus().setData(OPERATION_CALLED, "true");
//                async.pasteReferencesOnTopOf(JCRClientUtils.getPathesList(nodes), targetPath, null, callback);
            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                async.moveOnTopOf(nodes.get(0).getPath(), targetPath, callback);
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                e.getStatus().setData(OPERATION_CALLED, "true");
                final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
                GWTJahiaNodeType type = e.getStatus().getData(SOURCE_NODETYPE);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ((type.getItems() == null || type.getItems().size() == 0) && (type.getInheritedItems() == null || type.getInheritedItems().size() == 0)) {
                    async.createNodeAndMoveBefore(parent.getPath(), null,
                            type.getName(), null, null,
                            new ArrayList<GWTJahiaNodeProperty>(), new HashMap<String, List<GWTJahiaNodeProperty>>(), 
                            new BaseAsyncCallback<GWTJahiaNode>() {
                                public void onApplicationFailure(Throwable throwable) {
                                    com.google.gwt.user.client.Window.alert( "Properties save failed\n\n" + throwable.getLocalizedMessage());
                                    Log.error("failed", throwable);
                                }

                                public void onSuccess(GWTJahiaNode o) {
                                    Info.display("", "Node created");
                                    editLinker.refresh(Linker.REFRESH_MAIN);
                                }
                            });
                } else {
                    EngineLoader.showCreateEngine(editLinker, parent, type, new HashMap<String, GWTJahiaNodeProperty>(), "*",
                            true);
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                GWTJahiaSearchQuery q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");

                async.saveSearch(q, targetPath, "jnt_query", true, callback);
            }
        } else if (PAGETREE_TYPE.equals(targetType)) {
            if (PAGETREE_TYPE.equals(sourceType)) {
                e.getStatus().setData(OPERATION_CALLED, "true");
                GWTJahiaNode source = ((List<GWTJahiaNode>) e.getStatus().getData(SOURCE_NODES)).get(0);

                String targetPath = (String) e.getStatus().getData(TARGET_PATH);

                if (e.getDropTarget() instanceof PagesTabItem.PageTreeGridDropTarget) {
                    callback = ((PagesTabItem.PageTreeGridDropTarget) e.getDropTarget()).getCallback();
                }

                if (e.getStatus().<Object>getData("type").equals(-1)) {
                    async.moveAtEnd(source.getPath(), targetPath, callback);
                } else if (e.getStatus().<Object>getData("type").equals(0)) {
                    async.moveOnTopOf(source.getPath(), targetPath, callback);
                } else if (e.getStatus().<Object>getData("type").equals(1)) {
                    GWTJahiaNode node = e.getStatus().getData(TARGET_NEXT_NODE);
                    if (node == null) {
                        GWTJahiaNode parent = e.getStatus().getData(TARGET_PARENT);
                        async.moveAtEnd(source.getPath(), parent.getPath(), callback);
                    } else {
                        async.moveOnTopOf(source.getPath(), node.getPath(), callback);
                    }
                }
            }
        } else if (BROWSETREE_TYPE.equals(targetType)) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                async.pasteReferences(JCRClientUtils.getPathesList(nodes), targetPath, null, callback);
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
