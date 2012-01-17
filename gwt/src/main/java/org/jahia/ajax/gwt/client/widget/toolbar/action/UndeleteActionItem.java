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
import java.util.Map;

/**
 * Action item to undelete a node by removing locks and mixins
 */
public class UndeleteActionItem extends BaseActionItem {
    @Override
    public void onComponentSelection() {
        final LinkerSelectionContext lh = linker.getSelectionContext();
        if (!lh.getMultipleSelection().isEmpty()) {
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
                                             if (be.getButtonClicked().getText().equalsIgnoreCase(Dialog.YES)) {
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
                                     });
        }
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        List<GWTJahiaNode> selection = lh.getMultipleSelection();
        boolean canUndelete = false;
        if (selection != null && selection.size() > 0 && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions())) {
            canUndelete = true;
            for (GWTJahiaNode gwtJahiaNode : selection) {
                canUndelete &= gwtJahiaNode.getNodeTypes().contains("jmix:markedForDeletionRoot");
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
}
