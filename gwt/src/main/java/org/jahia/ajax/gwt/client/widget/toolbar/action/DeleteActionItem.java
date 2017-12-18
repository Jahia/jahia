/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.List;

/**
 * Action item responsible for deleting the content.
 * 
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
                    if (!noMarkForDeletion && !selected.isNodeType("jmix:markedForDeletionRoot")) {
                        enabled = false;
                        break;
                    }
    
                    if (selected.get("everPublished") != null && ((Boolean)selected.get("everPublished"))) {
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
                    if (!selected.canMarkForDeletion() || selected.getNodeTypes().contains("jmix:markedForDeletionRoot")) {
                        enabled = false;
                        break;
                    }
                }
            }
        }
        
        return enabled;
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        List<GWTJahiaNode> selection = lh.getMultipleSelection();
        if (selection != null && selection.size() > 0) {
            if (selection.size() == 1) {
                if (selection.get(0).getInheritedNodeTypes().contains("jmix:nodeReference")) {
                    updateTitle(Messages.get(referenceTitleKey,referenceTitleKey));
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

    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                final LinkerSelectionContext lh = linker.getSelectionContext();
                if (!lh.getMultipleSelection().isEmpty()) {
                    // Usages
                    final List<String> l = new ArrayList<String>();
                    for (GWTJahiaNode node : lh.getMultipleSelection()) {
                        l.add(node.getPath());
                    }

                    final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();

                        async.getUsages(l, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {
                            public void onApplicationFailure(Throwable caught) {
                                com.google.gwt.user.client.Window.alert("Cannot get status: " + caught.getMessage());
                            }

                            public void onSuccess(List<GWTJahiaNodeUsage> result) {
                            	new DeleteItemWindow(linker, linker.getSelectionContext(), permanentlyDelete).show();
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
