package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

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
    public static final String TARGET_TYPE = "targetType";
    public static final String TARGET_PATH = "targetPath";
    public static final String SOURCE_NODES = "sourceNodes";
    public static final String OPERATION_CALLED = "operationCalled";

    public EditModeDNDListener(EditManager editManager) {
        this.editManager = editManager;
    }

    @Override
    public void dragMove(DNDEvent e) {
        super.dragMove(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void dragStart(DNDEvent e) {
        if (e.getSource() instanceof ContentTreeDragSource) {
            e.getStatus().setData(SOURCE_TYPE, CONTENT_SOURCE_TYPE);

            List list = (List) e.getData();
            e.getStatus().setData("size", list.size());

            List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
            for (Object o : list) {
                l.add((GWTJahiaNode) ((BaseTreeModel) o).get("model"));
            }
            e.getStatus().setData(SOURCE_NODES, l);

        } else if (e.getSource() instanceof DisplayGridDragSource) {
            e.getStatus().setData(SOURCE_TYPE, CONTENT_SOURCE_TYPE);

            List<GWTJahiaNode> list = (List<GWTJahiaNode>) e.getData();
            e.getStatus().setData("size", list.size());

            e.getStatus().setData(SOURCE_NODES, list);

        }

        super.dragStart(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void dragEnter(DNDEvent e) {
        if (e.getDropTarget().getComponent() instanceof PlaceholderModule) {
            e.getStatus().setData(TARGET_TYPE, "placeholder");
            e.getStatus().setData(TARGET_PATH, ((PlaceholderModule)e.getDropTarget().getComponent()).getPath());
        }else if (e.getDropTarget() instanceof SimpleModule.SimpleModuleDropTarget) {
            e.getStatus().setData(TARGET_TYPE, "simpleModule");
            e.getStatus().setData(TARGET_PATH, ((SimpleModule.SimpleModuleDropTarget)e.getDropTarget()).getModule().getPath());
        }
        super.dragEnter(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void dragLeave(DNDEvent e) {
        super.dragLeave(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void dragDrop(final DNDEvent e) {
        if ("placeholder".equals(e.getStatus().getData(TARGET_TYPE))) {
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = (List<GWTJahiaNode>) e.getStatus().getData(SOURCE_NODES);
                String path = e.getStatus().getData(TARGET_PATH);
                int i = path.lastIndexOf('/');
                String name = path.substring(i +1);
                path = path.substring(0,i);

                e.getStatus().setData(OPERATION_CALLED, "true");
                if ("*".equals(name)) {
                    JahiaContentManagementService.App.getInstance().pasteReferences(nodes, path, new AsyncCallback() {
                        public void onSuccess(Object result) {
                            editManager.getMainModule().refresh();
                        }

                        public void onFailure(Throwable caught) {
                            Window.alert("Failed : "+caught);
                        }
                    });

                } else if (nodes.size() == 1) {
                    JahiaContentManagementService.App.getInstance().pasteReference(nodes.get(0), path, name, new AsyncCallback() {
                        public void onSuccess(Object result) {
                            editManager.getMainModule().refresh();
                        }

                        public void onFailure(Throwable caught) {
                            Window.alert("Failed : "+caught);
                        }
                    });

                }

            }
        } else if (e.getDragSource() instanceof SimpleModule.SimpleModuleDragSource &&
                   e.getDropTarget() instanceof SimpleModule.SimpleModuleDropTarget) {
            String sourcePath = ((SimpleModule.SimpleModuleDragSource) e.getDragSource()).getModule().getPath();
            String targetPath = ((SimpleModule.SimpleModuleDropTarget) e.getDropTarget()).getModule().getPath();
            e.getStatus().setData(OPERATION_CALLED, "true");
            JahiaContentManagementService.App.getInstance().moveOnTopOf(sourcePath, targetPath, new AsyncCallback() {

                public void onSuccess(Object o) {
                    editManager.getMainModule().refresh();
                }

                public void onFailure(Throwable throwable) {
                    Window.alert("Failed : "+throwable);
                }

            });
        } else if("simpleModule".equals(e.getStatus().getData(TARGET_TYPE))){
            if (CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(SOURCE_TYPE))) {
                List<GWTJahiaNode> nodes = (List<GWTJahiaNode>) e.getStatus().getData(SOURCE_NODES);
                String path = e.getStatus().getData(TARGET_PATH);
                int i = path.lastIndexOf('/');

                e.getStatus().setData(OPERATION_CALLED, "true");
                if (nodes.size()>1) {
                    JahiaContentManagementService.App.getInstance().pasteReferencesOnTopOf(nodes, path, new AsyncCallback() {
                        public void onSuccess(Object result) {
                            editManager.getMainModule().refresh();
                        }

                        public void onFailure(Throwable caught) {
                            Window.alert("Failed : "+caught);
                        }
                    });

                } else if (nodes.size() == 1) {
                    final GWTJahiaNode node = nodes.get(0);
                    JahiaContentManagementService.App.getInstance().pasteReferenceOnTopOf(node, path, node.getName(), new AsyncCallback() {
                        public void onSuccess(Object result) {
                            editManager.getMainModule().refresh();
                        }

                        public void onFailure(Throwable caught) {
                            Window.alert("Failed : "+caught);
                        }
                    });

                }

            }
        }
        Log.info("xx"+e.getStatus().getData("sourceNode"));
        super.dragDrop(e);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
