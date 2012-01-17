/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

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
                }

                if (selected.get("everPublished") != null && ((Boolean)selected.get("everPublished"))
                        ||  !selected.getNodeTypes().contains("jmix:markedForDeletionRoot")) {
                    // the node is already published or it is locked (not for deletion)
                    enabled = false;
                    break;
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
        boolean enabled = selection != null && selection.size() > 0
                && !lh.isSecondarySelection()
                && !lh.isRootNode()
                && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions())
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
                                String icon = MessageBox.WARNING;
                                String message;
                                if (l.size() > 1) {
                                    message = Messages.getWithArgs("message.remove.multiple.confirm",
                                            "Do you really want to remove the {0} selected resources?",
                                            new String[]{String.valueOf(
                                                    l.size())});
                                } else {
                                    if (lh.getMultipleSelection().get(0).getNodeTypes().contains("jnt:page")) {
                                        message = Messages.getWithArgs(
                                                "message.remove.single.page.confirm",
                                                "Do you really want to remove the selected PAGE {0}?",
                                                new String[]{lh.getSingleSelection().getName()});
                                        icon = "ext-mb-delete-page";
                                    } else {
                                        message = Messages.getWithArgs(
                                                "message.remove.single.confirm",
                                                "Do you really want to remove the selected resource {0}?",
                                                new String[]{lh.getSingleSelection().getName()});
                                    }
                                }
                                if (l.size() > 1) {
                                    message += "<br/><br/>";
                                    int i = 0;
                                    for (GWTJahiaNode node : lh.getMultipleSelection()) {
                                        if (i > 4) {
                                            message += "<br/>...";
                                            break;
                                        }
                                        message += "<br/>" + node.getName();
                                        i++;
                                    }
                                }
                                message+="<br/><br/>";
                                String n = "";
                                int size = result.size();
                                if(size>0) {
                                    message +=l.size() > 1 ? Messages.get("message.remove.multiple.usage",
                                            "Those nodes are still used in:") : Messages.get(
                                            "message.remove.single.usage",
                                            "This node is still used by:");
                                }
                                int i = 0;
                                for (int j = 0; j < (size>4?4:size); j++) {
                                    GWTJahiaNodeUsage nodeUsage = result.get(j);
                                    if (!nodeUsage.getNodeName().equals(n)) {
                                        message += "<br/><span style=\"font-style:italic;\">" + nodeUsage.getNodeTitle() + " " + Messages.get(
                                                "label.remove.used", "is using this node in page(s)") + "<br/>" +nodeUsage.getPageTitle()+"</span>";
                                        i++;
                                    } else {
                                        message += "<br/><span style=\"font-style:italic;\">" + nodeUsage.getPageTitle()+"</span>";
                                    }
                                    n = nodeUsage.getNodeName();
                                }
                                if(i>4) {
                                    message+="<br/>.<br/>.<br/>.";
                                }
                                if (i > 0) {
                                    message+="<br/>";
                                }
                                if (permanentlyDelete) {
                                    message+=Messages.get("message.remove.warning","<br/><span style=\"font-style:bold;color:red;\">Warning: this will erase the content definitively from the repository<br/>So it will not be displayed anymore anywere</span>");
                                } else {
                                    message += "<br/>" + Messages.get("label.comment","Comment") + ":";
                                }

                                final MessageBox box = new MessageBox();
                                box.setTitle(Messages.get("label.information", "Information"));
                                box.setMessage(message);
                                if (!permanentlyDelete) {
                                    box.setType(MessageBoxType.MULTIPROMPT);
                                }
                                box.setButtons(MessageBox.YESNO);
                                box.setIcon(icon);
                                box.addCallback(new Listener<MessageBoxEvent>() {
                                    public void handleEvent(MessageBoxEvent be) {
                                        if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
                                            BaseAsyncCallback<Object> baseAsyncCallback = new BaseAsyncCallback<Object>() {
                                                public void onApplicationFailure(Throwable throwable) {
                                                    Log.error(throwable.getMessage(), throwable);
                                                    MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
                                                }

                                                public void onSuccess(Object o) {
                                                    EditLinker el = null;
                                                    if (linker instanceof SidePanelTabItem.SidePanelLinker) {
                                                        el = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
                                                    } else if (linker instanceof EditLinker) {
                                                        el = (EditLinker) linker;
                                                    }
                                                    if (el != null && l.contains(el.getSelectionContext().getMainNode().getPath())) {
                                                        linker.refresh(EditLinker.REFRESH_PAGES);
                                                        linker.select(null);
                                                    } else {
                                                        linker.refresh(EditLinker.REFRESH_ALL);
                                                        linker.select(null);
                                                    }
                                                }
                                            };
                                            if (permanentlyDelete) {
                                                async.deletePaths(l, baseAsyncCallback);
                                            } else {
                                                async.markForDeletion(l, box.getTextArea().getValue(), baseAsyncCallback);
                                            }
                                        }
                                    }
                                });
                                box.show();

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
}
