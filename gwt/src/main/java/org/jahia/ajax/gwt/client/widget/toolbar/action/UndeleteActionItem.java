/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTHooks;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action item to undelete a node by removing locks and mixins
 */
public class UndeleteActionItem extends NodeTypeAwareBaseActionItem {
    @Override
    public void onComponentSelection() {
        final LinkerSelectionContext lh = linker.getSelectionContext();
        if (!lh.getMultipleSelection().isEmpty()) {
            final List<String> l = new ArrayList<String>();
            for (GWTJahiaNode node : lh.getMultipleSelection()) {
                l.add(node.getPath());
            }

            if (JahiaGWTHooks.hasHook("undelete")) {
                Map<String, Object> params = new HashMap<>();
                // Provide the path
                params.put("paths", l);
                JahiaGWTHooks.callHook("undelete", params);
                return;
            }

            String message = null;
            if (lh.getMultipleSelection().size() > 1) {
                message = Messages.getWithArgs(
                        "message.undelete.multiple.confirm",
                        "Do you really want to undelete the {0} selected resources?",
                        new String[] { String.valueOf(lh.getMultipleSelection().size()) });
            } else {
                message = Messages.getWithArgs(
                        "message.undelete.confirm",
                        "Do you really want to undelete the selected resource {0}?",
                        new String[] { lh.getSingleSelection().getDisplayName() });
            }
            MessageBox.confirm(
                    Messages.get("label.information", "Information"),
                    message,
                                     new Listener<MessageBoxEvent>() {
                                         public void handleEvent(MessageBoxEvent be) {
                                             if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.YES)) {
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
                                                         Map<String, Object> data = new HashMap<String, Object>();
                                                         if (el != null && l.contains(el.getSelectionContext().getMainNode().getPath())) {
                                                             data.put("node", el.getSelectionContext().getMainNode());
                                                         } else {
                                                             data.put(Linker.REFRESH_ALL, true);
                                                         }
                                                         linker.refresh(data);
                                                     }
                                                 });
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
        if (selection != null
                && selection.size() > 0
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions())
                && isNodeTypeAllowed(selection)
                ) {
            canUndelete = true;
            for (GWTJahiaNode gwtJahiaNode : selection) {
                canUndelete &= gwtJahiaNode.isMarkedForDeletionRoot();
                canUndelete = canUndelete && (!gwtJahiaNode.isLocked() || isLockedForDeletion(gwtJahiaNode));
                if (!canUndelete) {
                    break;
                }
            }
        }
        setEnabled(canUndelete);
    }

    static boolean isLockedForDeletion(GWTJahiaNode node) {
        Map<String, List<String>> lockInfos = node.getLockInfos();
        return lockInfos != null  && lockInfos.containsKey(null)
                && !lockInfos.get(null).isEmpty()
                && lockInfos.get(null).size() == 1
                && lockInfos.get(null).get(0).equals("label.locked.by.deletion");
    }
    @Override
    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(selectedNode.getNodeTypes().get(0));
        if (nodeType != null) {
            Boolean canUseComponentForCreate = (Boolean) nodeType.get("canUseComponentForCreate");
            if (canUseComponentForCreate != null && !canUseComponentForCreate) {
                return false;
            }
        }

        return super.isNodeTypeAllowed(selectedNode);
    }
}
