/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTHooks;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.DeleteItemWindow;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action item responsible for deleting the content.
 * <p>
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:06 PM
 */
@SuppressWarnings("serial")
public class DeleteActionItem extends NodeTypeAwareBaseActionItem {

    private boolean noMarkForDeletion;

    private boolean permanentlyDelete;

    private String referenceTitleKey;

    private boolean checkEnabledWithMarkedForDeletion(LinkerSelectionContext lh) {
        boolean enabled = true;
        if (permanentlyDelete) {
            // we are dealing with permanent deletion action
            for (GWTJahiaNode selected : lh.getMultipleSelection()) {
                if (!selected.canMarkForDeletion()) {
                    // the node does not support marking for deletion
                    if (!selected.isLocked()) {
                        // it is not locked -> we can permanently delete it
                        continue;
                    } else {
                        // it is locked -> cannot delete it permanently
                        enabled = false;
                        break;
                    }
                } else {
                    if (!noMarkForDeletion && !selected.isMarkedForDeletionRoot()) {
                        enabled = false;
                        break;
                    }

                    if (selected.get("everPublished") != null && ((Boolean) selected.get("everPublished"))) {
                        // the node is already published or it is locked (not for deletion)
                        enabled = false;
                        break;
                    }
                }
            }
        } else {
            // we are dealing with mark for delete action

            enabled = !lh.isLocked();
            if (enabled) {

                // if one of the selected nodes cannot be marked for deletion -> do not display the delete action
                for (GWTJahiaNode selected : lh.getMultipleSelection()) {
                    if (!selected.canMarkForDeletion() || selected.isMarkedForDeletionRoot()) {
                        enabled = false;
                        break;
                    }
                }
            }
        }

        return enabled;
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        List<GWTJahiaNode> selection = lh.getMultipleSelection();
        if (selection != null && selection.size() > 0) {
            if (selection.size() == 1) {
                if (selection.get(0).getInheritedNodeTypes().contains("jmix:nodeReference")) {
                    updateTitle(Messages.get(referenceTitleKey, referenceTitleKey));
                } else {
                    updateTitle(getGwtToolbarItem().getTitle() + " : " + selection.get(0).getDisplayName());
                }
            } else {
                updateTitle(getGwtToolbarItem().getTitle() + " " + selection.size() + " " + Messages.get("label.items"));
            }
        }
        boolean autoCreated = false;
        if (ModuleHelper.getModulesByPath() != null && JahiaGWTParameters.isAreaAutoActivated()) {
            for (GWTJahiaNode selectedNode : lh.getMultipleSelection()) {
                String selectedNodePath = selectedNode.getPath();
                List<Module> selectedModule = ModuleHelper.getModulesByPath().get(selectedNodePath);
                if (!selectedNodePath.startsWith("/modules/") && selectedModule != null && selectedModule.size() > 0) {
                    for (Module module : selectedModule) {
                        if (module instanceof AreaModule) {
                            autoCreated = true;
                            break;
                        }
                    }
                    if (autoCreated) {
                        break;
                    }
                }
            }
        }
        boolean enabled = !autoCreated && selection != null && selection.size() > 0
                && !lh.isRootNode()
                && hasPermission(lh.getSelectionPermissions()) && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions())
                && isNodeTypeAllowed(selection);

        if (enabled) {
            enabled = checkEnabledWithMarkedForDeletion(lh);
        }

        setEnabled(enabled);
    }

    @Override
    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onFailure(Throwable reason) {
            }

            @Override
            public void onSuccess() {
                final LinkerSelectionContext lh = linker.getSelectionContext();
                if (!lh.getMultipleSelection().isEmpty()) {
                    // Usages
                    final List<String> l = new ArrayList<String>();
                    for (GWTJahiaNode node : lh.getMultipleSelection()) {
                        l.add(node.getPath());
                    }

                    String hook = permanentlyDelete ? "deletePermanently" : "delete";
                    if (JahiaGWTHooks.hasHook(hook)) {
                        Map<String, Object> params = new HashMap<>();
                        // Provide the path
                        params.put("paths", l);
                        JahiaGWTHooks.callHook(hook, params);
                        return;
                    }

                    final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();

                    async.getUsages(l, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {

                        @Override
                        public void onApplicationFailure(Throwable caught) {
                            com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(List<GWTJahiaNodeUsage> result) {
                            new DeleteItemWindow(linker, linker.getSelectionContext(), permanentlyDelete, false, JahiaGWTParameters.getBaseUrl()).show();
                        }
                    });
                }
            }
        });
    }

    public void setPermanentlyDelete(boolean permanentlyDelete) {
        this.permanentlyDelete = permanentlyDelete;
    }

    public void setReferenceTitleKey(String referenceTitleKey) {
        this.referenceTitleKey = referenceTitleKey;
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

    public void setNoMarkForDeletion(boolean noMarkForDeletion) {
        this.noMarkForDeletion = noMarkForDeletion;
    }
}
