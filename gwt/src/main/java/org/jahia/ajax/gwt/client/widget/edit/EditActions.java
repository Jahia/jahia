package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 4:14:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditActions {

    /**
     * Create page
     * @param editLinker
     */
    public static void createPage(final EditLinker editLinker) {
        if (editLinker.getMainModule().getNode() != null) {
            JahiaContentDefinitionService.App.getInstance().getNodeType("jnt:page", new AsyncCallback<GWTJahiaNodeType>() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    com.google.gwt.user.client.Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {
                    new EditContentEngine(editLinker, editLinker.getMainModule().getNode(), gwtJahiaNodeType,null,false,true).show();
                }
            });
        }

    }


    /**
     * Dispay edit content window
     * @param editLinker
     */
    public static void edit(EditLinker editLinker) {
        if (editLinker.getMainModule() != null) {
            new EditContentEngine(editLinker.getMainModule().getNode()).show();
        }
    }


    /**
     * Publish selected content
     * @param editLinker
     */
    public static void publish(final EditLinker editLinker) {
        if (editLinker.getMainModule().getNode() != null) {
            JahiaContentManagementService.App.getInstance().publish(editLinker.getSelectedModule().getNode().getPath(), new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot publish "+caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display("Content published", "Content published.");
                    editLinker.refresh();

                }
            });
        }
    }

    /**
     * Unpublish selected content
     * @param editLinker
     */
    public static void unpublish(final EditLinker editLinker) {
        if (editLinker.getMainModule().getNode() != null) {
            JahiaContentManagementService.App.getInstance().unpublish(editLinker.getSelectedModule().getNode().getPath(), new AsyncCallback() {
                public void onFailure(Throwable caught) {
                    Log.error("Cannot publish", caught);
                    com.google.gwt.user.client.Window.alert("Cannot unpublish "+caught.getMessage());
                }

                public void onSuccess(Object result) {
                    Info.display("Content unpublished", "Content unpublished.");
                    editLinker.refresh();
                }
            });
        }
    }


    /**
     * Switch lock
     * @param editLinker
     */
    public static void switchLock(final EditLinker editLinker) {
        if (editLinker.getSelectedModule() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(editLinker.getSelectedModule().getNode().getPath());
            final boolean isLock = !editLinker.getSelectedModule().getNode().isLocked();
            JahiaContentManagementService.App.getInstance().setLock(paths, isLock, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    editLinker.getMainModule().refresh();
                }
            });
        }

    }


    /**
     * Delete content
     * @param editLinker
     */
    public static void delete(final EditLinker editLinker) {
        if (editLinker.getSelectedModule() != null) {
            List<String> paths = new ArrayList<String>(1);
            paths.add(editLinker.getSelectedModule().getNode().getPath());
            JahiaContentManagementService.App.getInstance().deletePaths(paths, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    Log.error("", throwable);
                    Window.alert("-->" + throwable.getMessage());
                }

                public void onSuccess(Object o) {
                    editLinker.refresh();
                    editLinker.onModuleSelection(null);
                }
            });
        }
    }
}
