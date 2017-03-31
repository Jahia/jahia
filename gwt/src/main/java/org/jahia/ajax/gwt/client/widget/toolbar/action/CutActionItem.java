/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:14 PM
 */
@SuppressWarnings("serial")
public class CutActionItem extends NodeTypeAwareBaseActionItem  {
    public void onComponentSelection() {
        final List<GWTJahiaNode> selectedItems = linker.getSelectionContext().getMultipleSelection();
        if (selectedItems != null && selectedItems.size() > 0) {
            final List<GWTJahiaNode> actualSelection = new ArrayList<GWTJahiaNode>();
            final List<GWTJahiaNode> lockedFiles = new ArrayList<GWTJahiaNode>();
            for (GWTJahiaNode node : selectedItems) {
                if (node.isLocked()) {
                    lockedFiles.add(node);
                } else {
                    actualSelection.add(node);
                }
            }
            if (!lockedFiles.isEmpty()) {
                StringBuilder s = new StringBuilder(Messages.get("warning.lock.label"));
                for (GWTJahiaNode node : lockedFiles) {
                    s.append("\n").append(node.getName());
                }
                Window.alert(s.toString());
            }
            if (!actualSelection.isEmpty()) {
                JahiaContentManagementService.App.getInstance().checkWriteable(JCRClientUtils.getPathesList(actualSelection), new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Window.alert(Messages.get("failure.cut.label") + "\n" + throwable.getLocalizedMessage());
                    }

                    public void onSuccess(Object o) {
                        CopyPasteEngine.getInstance().setCutPaths(actualSelection);
                        linker.select(null);
                        ClipboardActionItem.setCopied(actualSelection);
                    }
                });
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getMultipleSelection() != null
                && lh.getMultipleSelection().size() > 0
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:removeNode", lh.getSelectionPermissions())
                && !lh.isRootNode()
                && !lh.isLocked()
                && !lh.getMultipleSelection()
                        .get(0)
                        .getPath()
                        .equals("/sites/" + lh.getMultipleSelection().get(0).getSiteKey() + "/"
                                + lh.getMultipleSelection().get(0).getName())
                && !lh.getMultipleSelection().get(0).getPath()
                        .equals("/" + lh.getMultipleSelection().get(0).getName())
                && isNodeTypeAllowed(lh.getMultipleSelection()));
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
