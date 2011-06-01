package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 4/28/11
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class ClearAllLocksActionItem extends BaseActionItem {

    private boolean doSubNodes = false;

    public void setDoSubNodes(boolean doSubNodes) {
        this.doSubNodes = doSubNodes;
    }

    public void onComponentSelection() {
        ContentActions.lock(false, linker);
        String selectedPaths = linker.getSelectionContext().getSingleSelection().getPath();
        JahiaContentManagementService.App.getInstance().clearAllLocks(selectedPaths, doSubNodes, new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable throwable) {
                MessageBox.alert(Messages.get("label.error", "Error"), throwable.getLocalizedMessage(), null);
                linker.loaded();
                linker.refresh(Linker.REFRESH_MAIN);
            }

            public void onSuccess(Object o) {
                linker.loaded();
                linker.refresh(Linker.REFRESH_MAIN);
            }
        });


    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection!=null && singleSelection.isLockable() &&
                PermissionsUtils.isPermitted("jcr:lockManagement", lh.getSelectionPermissions()) && singleSelection.getLockInfos() != null &&
                !lh.getSingleSelection().getLockInfos().isEmpty() && !lh.isSecondarySelection());
    }
}
