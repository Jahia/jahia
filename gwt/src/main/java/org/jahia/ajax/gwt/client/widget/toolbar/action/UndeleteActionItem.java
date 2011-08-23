package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Action item to undelete a node by removing locks and mixins
 */
public class UndeleteActionItem extends BaseActionItem {
    @Override
    public void onComponentSelection() {
        final LinkerSelectionContext lh = linker.getSelectionContext();
        if (!lh.getMultipleSelection().isEmpty()) {
            final List<String> l = new ArrayList<String>();
            for (GWTJahiaNode node : lh.getMultipleSelection()) {
                l.add(node.getPath());
            }
            JahiaContentManagementService.App.getInstance().undeletePaths(l, new BaseAsyncCallback() {
                @Override
                public void onApplicationFailure(Throwable throwable) {
                    Log.error(throwable.getMessage(), throwable);
                    MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                }

                public void onSuccess(Object result) {
                    EditLinker el = null;
                    if (linker instanceof SidePanelTabItem.SidePanelLinker) {
                        el = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
                    } else if (linker instanceof EditLinker) {
                        el = (EditLinker) linker;
                    }
                    if (el != null && l.contains(el.getSelectionContext().getMainNode().getPath())) {
                        linker.refresh(EditLinker.REFRESH_PAGES);
                    } else {
                        linker.refresh(EditLinker.REFRESH_ALL);
                    }
                }
            });
        }
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        List<GWTJahiaNode> selection = lh.getMultipleSelection();
        boolean canUndelete = false;
        if (selection != null && selection.size() > 0) {
            canUndelete = true;
            for (GWTJahiaNode gwtJahiaNode : selection) {
                canUndelete &= gwtJahiaNode.getInheritedNodeTypes().contains("jmix:markedForDeletionRoot");
            }
        }
        setEnabled(canUndelete && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions()));
    }
}
