package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.definition.ContentTypeModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreateContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.PagesTabItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public static final String BROWSETREE_TYPE = "browseTree";
    public static final String SIMPLEMODULE_TYPE = "simpleModule";
    public static final String PLACEHOLDER_TYPE = "placeholder";

    public static final String TARGET_TYPE = "targetType";
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
        if (PLACEHOLDER_TYPE.equals(targetType)) {
            final String targetPath = e.getStatus().getData(TARGET_PATH);
            int i = targetPath.lastIndexOf('/');
            String name = targetPath.substring(i + 1);
            final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
            String parentPath = parent.getPath();

            // Drop into empty placeholder
            if (CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Existing item from content list
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
//                if ("*".equals(name)) {
                JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(nodes), parentPath, null, callback);
//                } else if (nodes.size() == 1) {
//                    JahiaContentManagementService.App.getInstance().pasteReferences(nodes, parentPath+"/"+name, callback);
//                }

            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                GWTJahiaNode selectedNode = nodes.get(0);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().moveAtEnd(selectedNode.getPath(), parentPath, callback);
                } else {
                    JahiaContentManagementService.App.getInstance().move(selectedNode.getPath(), targetPath, callback);
                }
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                ContentTypeModelData modelData = e.getStatus().getData(SOURCE_NODETYPE);
                if (modelData.getGwtJahiaNode() == null) {
                    GWTJahiaNodeType type = modelData.getGwtJahiaNodeType();
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    if ((type.getItems() == null || type.getItems().size() == 0) && (type.getInheritedItems() == null || type.getInheritedItems().size() == 0)) {
                        JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), null,
                                type.getName(), null, null,
                                new ArrayList<GWTJahiaNodeProperty>(), null,
                                new AsyncCallback<GWTJahiaNode>() {
                                    public void onFailure(
                                            Throwable throwable) {
                                        Window.alert(
                                                "Properties save failed\n\n" + throwable.getLocalizedMessage());
                                        Log.error(
                                                "failed",
                                                throwable);
                                    }

                                    public void onSuccess(
                                            GWTJahiaNode o) {
                                        Info.display("",
                                                "Node created");
                                        editLinker.refreshMainComponent();
                                    }
                                });
                    } else {
                        new CreateContentEngine(editLinker, parent, type, targetPath.substring(targetPath.lastIndexOf(
                                "/") + 1)).show();
                    }
                } else {
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    final GWTJahiaNode gwtJahiaNode = modelData.getGwtJahiaNode();
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                    instance.paste(Arrays.asList(gwtJahiaNode.getPath() + "/j:target"), parent.getPath(), gwtJahiaNode.getName(), false, new DropAsyncCallback());
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                GWTJahiaSearchQuery q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, "jnt_query", callback);
                } else {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, name, callback);
                }
            } else if (parent.getNodeTypes().contains("jnt:navMenu") && (PAGETREE_TYPE.equals(sourceType) || CONTENT_SOURCE_TYPE.equals(sourceType))) {
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                final GWTJahiaNode selectedNode = nodes.get(0);
                JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:navMenuNodeLink", new AsyncCallback<GWTJahiaNodeType>() {
                    public void onFailure(Throwable caught) {
                        Window.alert(
                                "Cannot retrieve node type 'jnt:navMenuNodeLink'. Cause: " + caught.getLocalizedMessage());
                        Log.error("Cannot retrieve node type 'jnt:navMenuNodeLink'. Cause: " + caught.getLocalizedMessage(),
                                caught);
                    }
                    public void onSuccess(GWTJahiaNodeType result) {
                        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
                        props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(selectedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                        new CreateContentEngine(editLinker, parent, result, props, targetPath.substring(targetPath.lastIndexOf("/") + 1), false).show();
                    }
                });
            }
        } else if (SIMPLEMODULE_TYPE.equals(targetType)) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            // Drop into an existing module
            if (CONTENT_SOURCE_TYPE.equals(sourceType)) {
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().pasteReferencesOnTopOf(JCRClientUtils.getPathesList(nodes), targetPath, null, callback);
            } else if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, callback);
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                e.getStatus().setData(OPERATION_CALLED, "true");
                final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
                ContentTypeModelData modelData = e.getStatus().getData(SOURCE_NODETYPE);
                if (modelData.getGwtJahiaNode() == null) {
                    GWTJahiaNodeType type = modelData.getGwtJahiaNodeType();
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    if ((type.getItems() == null || type.getItems().size() == 0) && (type.getInheritedItems() == null || type.getInheritedItems().size() == 0)) {
                        JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), null,
                                type.getName(), null, null,
                                new ArrayList<GWTJahiaNodeProperty>(),new HashMap<String,List<GWTJahiaNodeProperty>>(), null,
                                new AsyncCallback<GWTJahiaNode>() {
                                    public void onFailure(
                                            Throwable throwable) {
                                        com.google.gwt.user.client.Window.alert(
                                                "Properties save failed\n\n" + throwable.getLocalizedMessage());
                                        Log.error(
                                                "failed",
                                                throwable);
                                    }

                                    public void onSuccess(
                                            GWTJahiaNode o) {
                                        Info.display("",
                                                "Node created");
                                        editLinker.refreshMainComponent();
                                    }
                                });
                    } else {
                        new CreateContentEngine(editLinker, parent, type, "*", true).show();
                    }
                } else {
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    final GWTJahiaNode gwtJahiaNode = modelData.getGwtJahiaNode();
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                    instance.pasteOnTopOf(Arrays.asList(gwtJahiaNode.getPath() + "/j:target"), parent.getPath(), gwtJahiaNode.getName(), false, callback);
                }
            } else if (QUERY_SOURCE_TYPE.equals(sourceType)) {
                // Item creation
                String q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().saveSearchOnTopOf(q, targetPath, "jnt_query", callback);
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
                    JahiaContentManagementService.App.getInstance().moveAtEnd(source.getPath(), targetPath, callback);
                } else if (e.getStatus().<Object>getData("type").equals(0)) {
                    JahiaContentManagementService.App.getInstance().moveOnTopOf(source.getPath(), targetPath, callback);
                } else if (e.getStatus().<Object>getData("type").equals(1)) {
                    GWTJahiaNode node = e.getStatus().getData(TARGET_NEXT_NODE);
                    if (node == null) {
                        GWTJahiaNode parent = e.getStatus().getData(TARGET_PARENT);
                        JahiaContentManagementService.App.getInstance().moveAtEnd(source.getPath(), parent.getPath(), callback);
                    } else {
                        JahiaContentManagementService.App.getInstance().moveOnTopOf(source.getPath(), node.getPath(), callback);
                    }
                }
//
//                if ("append".equals(e.getStatus().getData("type"))) {
//                } else if ("insert".equals(e.getStatus().getData("type"))) {
//                    Window.alert("insert");
//                }
//                JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, new DropAsyncCallback());
            }
        } else if (BROWSETREE_TYPE.equals(targetType)) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            if (SIMPLEMODULE_TYPE.equals(sourceType)) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().pasteReferences(JCRClientUtils.getPathesList(nodes), targetPath, null, callback);
            }
        }
        super.dragDrop(e);
    }

    private class DropAsyncCallback implements AsyncCallback {
        public void onSuccess(Object o) {
            editLinker.getMainModule().refresh();
        }

        public void onFailure(Throwable throwable) {
            Window.alert("Failed : " + throwable);
        }

    }

}
