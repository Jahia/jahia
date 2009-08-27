package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 7:02:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditModeDNDListener extends DNDListener {
    private EditManager editManager;

    public static final String SOURCE_TYPE = "sourceType";

    public static final String CONTENT_SOURCE_TYPE = "content";
    public static final String CREATE_CONTENT_SOURCE_TYPE = "createContent";

    public static final String QUERY_SOURCE_TYPE = "query";

    public static final String SIMPLEMODULE_TYPE = "simpleModule";
    public static final String PLACEHOLDER_TYPE = "placeholder";

    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_PATH = "targetPath";
    public static final String TARGET_NODE = "targetNode";

    public static final String SOURCE_QUERY = "query";

    public static final String SOURCE_NODES = "sourceNodes";
    public static final String SOURCE_TEMPLATE = "sourceTemplate";
    public static final String SOURCE_NODETYPE = "sourceNodeType";
    public static final String OPERATION_CALLED = "operationCalled";

    public EditModeDNDListener(EditManager editManager) {
        this.editManager = editManager;
    }

    @Override
    public void dragDrop(final DNDEvent e) {
        if ("true".equals(e.getStatus().getData(OPERATION_CALLED))) {
            return;
        }
        if (PLACEHOLDER_TYPE.equals(e.getStatus().getData(TARGET_TYPE))) {
            String targetPath = e.getStatus().getData(TARGET_PATH);
            int i = targetPath.lastIndexOf('/');
            String name = targetPath.substring(i +1);
            String parentPath = targetPath.substring(0,i);
            GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);

            // Drop into empty placeholder
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Existing item from content list
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().pasteReferences(nodes, parentPath, new DropAsyncCallback());
                } else if (nodes.size() == 1) {
                    JahiaContentManagementService.App.getInstance().pasteReference(nodes.get(0), parentPath, name, new DropAsyncCallback());
                }

            } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);
                GWTJahiaNode selectedNode = nodes.get(0);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().moveAtEnd(selectedNode.getPath(), parentPath, new DropAsyncCallback());
                } else {
                    JahiaContentManagementService.App.getInstance().move(selectedNode.getPath(), targetPath, new DropAsyncCallback());
                }
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                GWTJahiaNodeType type = e.getStatus().getData(SOURCE_NODETYPE);
                e.getStatus().setData(OPERATION_CALLED, "true");
                new EditContentEngine(editManager, parent, type, targetPath.substring(targetPath.lastIndexOf("/")+1)).show();
            } else if (QUERY_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                String q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, "jnt_query", new DropAsyncCallback());
                } else {
                    JahiaContentManagementService.App.getInstance().saveSearch(q, parentPath, name, new DropAsyncCallback());
                }
            }
        } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(TARGET_TYPE))){
            String targetPath = e.getStatus().getData(TARGET_PATH);
            // Drop into an existing module
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = (List<GWTJahiaNode>) e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if (nodes.size()>1) {
                    JahiaContentManagementService.App.getInstance().pasteReferencesOnTopOf(nodes, targetPath, new DropAsyncCallback());
                } else if (nodes.size() == 1) {
                    final GWTJahiaNode node = nodes.get(0);
                    JahiaContentManagementService.App.getInstance().pasteReferenceOnTopOf(node, targetPath, node.getName(), new DropAsyncCallback());
                }
            } else if (SIMPLEMODULE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item move
                List<GWTJahiaNode> nodes = e.getStatus().getData(SOURCE_NODES);

                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().moveOnTopOf(nodes.get(0).getPath(), targetPath, new DropAsyncCallback());
            } else if (CREATE_CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                GWTJahiaNodeType type = e.getStatus().getData(SOURCE_NODETYPE);
                e.getStatus().setData(OPERATION_CALLED, "true");
                GWTJahiaNode parent = e.getStatus().getData(TARGET_NODE);
                new EditContentEngine(editManager, parent, type,targetPath.substring(targetPath.lastIndexOf("/")+1),true,false).show();
            } else if (QUERY_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                // Item creation
                String q = e.getStatus().getData(SOURCE_QUERY);
                e.getStatus().setData(OPERATION_CALLED, "true");
                JahiaContentManagementService.App.getInstance().saveSearchOnTopOf(q, targetPath, "jnt_query", new DropAsyncCallback());
            }
        }
        super.dragDrop(e);
    }

    private class DropAsyncCallback implements AsyncCallback {
        public void onSuccess(Object o) {
            editManager.getMainModule().refresh();
        }

        public void onFailure(Throwable throwable) {
            Window.alert("Failed : "+throwable);
        }

    }
}
