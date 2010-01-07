package org.jahia.ajax.gwt.client.widget.edit;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Window;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.ContentTypeModelData;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.PagesTabItem;


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
        AsyncCallback callback = new DropAsyncCallback();
        if (PLACEHOLDER_TYPE.equals(e.getStatus().getData(TARGET_TYPE))) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            int i = targetPath.lastIndexOf('/');
            String name = targetPath.substring(i +1);
            String parentPath = targetPath.substring(0,i);
            final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);

            // Drop into empty placeholder
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Existing item from content list
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
//                if ("*".equals(name)) {
                JahiaContentManagementService.App.getInstance().pasteReferences(nodes, parentPath, callback);
//                } else if (nodes.size() == 1) {
//                    JahiaContentManagementService.App.getInstance().pasteReferences(nodes, parentPath+"/"+name, callback);
//                }

            } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                GWTJahiaNode selectedNode = nodes.get(0);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().moveAtEnd(selectedNode.getPath(), parentPath, callback);
                } else {
                    JahiaContentManagementService.App.getInstance().move(selectedNode.getPath(), targetPath, callback);
                }
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                ContentTypeModelData modelData = e.getStatus().getData(SOURCE_NODETYPE);
                if(modelData.getGwtJahiaNode()==null) {
                    GWTJahiaNodeType type = modelData.getGwtJahiaNodeType();
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    if (type.getItems() == null || type.getItems().size() == 0) {
                        JahiaContentManagementService.App.getInstance().createNode(parent.getPath(), null,
                                                                                                type.getName(), null,
                                                                                                new ArrayList<GWTJahiaNodeProperty>(), null,
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
                        new EditContentEngine(editLinker, parent, type, targetPath.substring(targetPath.lastIndexOf(
                                "/") + 1)).show();
                    }
                } else {
                    final GWTJahiaNode gwtJahiaNode = modelData.getGwtJahiaNode();
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                        instance.getNode(gwtJahiaNode.getPath() + "/j:target", new AsyncCallback<GWTJahiaNode>() {
                            public void onFailure(Throwable caught) {
                                MessageBox.alert("Alert",
                                                 "Unable to copy reusable component to destination. Cause: " + caught.getLocalizedMessage(),
                                                 null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
                                result.setName(gwtJahiaNode.getName());
                                nodes.add(result);
                                instance.paste(nodes, parent.getPath(), false,new DropAsyncCallback());
                            }
                        });
                }
            } else if (QUERY_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                String q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, "jnt_query", callback);
                } else {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, name, callback);
                }
            }
        } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(TARGET_TYPE))){
            String targetPath = e.getStatus().getData(TARGET_PATH);
            // Drop into an existing module
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = (List<GWTJahiaNode>) e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().pasteReferencesOnTopOf(nodes, targetPath, callback);
            } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, callback);
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                e.getStatus().setData(OPERATION_CALLED, "true");
                final GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
                ContentTypeModelData modelData = e.getStatus().getData(SOURCE_NODETYPE);
                if(modelData.getGwtJahiaNode()==null) {
                    GWTJahiaNodeType type = modelData.getGwtJahiaNodeType();
                    e.getStatus().setData(OPERATION_CALLED, "true");
                    if (type.getItems() == null || type.getItems().size() == 0) {
                        JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parent.getPath(), null,
                                                                                                type.getName(), null,
                                                                                                new ArrayList<GWTJahiaNodeProperty>(), null,
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
                        new EditContentEngine(editLinker, parent, type, targetPath.substring(targetPath.lastIndexOf("/")+1),true).show();
                    }
                } else {
                    final GWTJahiaNode gwtJahiaNode = modelData.getGwtJahiaNode();
                    final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                        instance.getNode(gwtJahiaNode.getPath() + "/j:target", new AsyncCallback<GWTJahiaNode>() {
                            public void onFailure(Throwable caught) {
                                MessageBox.alert("Alert",
                                                 "Unable to copy reusable component to destination. Cause: " + caught.getLocalizedMessage(),
                                                 null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
                                result.setName(gwtJahiaNode.getName());
                                nodes.add(result);
                                instance.pasteOnTopOf(nodes, parent.getPath(),new DropAsyncCallback());
                            }
                        });
                }
            } else if (QUERY_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                String q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().saveSearchOnTopOf(q, targetPath, "jnt_query", callback);
            }
        } else if (PAGETREE_TYPE.equals(e.getStatus().getData(TARGET_TYPE))){
            if (PAGETREE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                e.getStatus().setData(OPERATION_CALLED, "true");
                GWTJahiaNode source = ((List<GWTJahiaNode>)e.getStatus().getData(SOURCE_NODES)).get(0);

                String targetPath = (String) e.getStatus().getData(TARGET_PATH);

                if (e.getDropTarget() instanceof PagesTabItem.PageTreeGridDropTarget) {
                    callback = ((PagesTabItem.PageTreeGridDropTarget)e.getDropTarget()).getCallback();
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
        } else if (BROWSETREE_TYPE.equals(e.getStatus().getData(TARGET_TYPE))) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().pasteReferences(nodes, targetPath, callback);
            }
        }
        super.dragDrop(e);
    }

    private class DropAsyncCallback implements AsyncCallback {
        public void onSuccess(Object o) {
            editLinker.getMainModule().refresh();
        }

        public void onFailure(Throwable throwable) {
            Window.alert("Failed : "+throwable);
        }

    }
}
