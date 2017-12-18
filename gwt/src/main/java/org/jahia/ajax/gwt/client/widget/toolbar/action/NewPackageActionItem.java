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

import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.*;

public class NewPackageActionItem extends NodeTypeAwareBaseActionItem {


    private static final long serialVersionUID = -2760798544350502175L;

    public void onComponentSelection() {
        GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        if (parent != null) {
            final String nodeName = Window.prompt(Messages.get("label.newJavaPackage"), "untitled");
            if (nodeName == null || nodeName.length() == 0 || !nodeName.matches("^([a-z0-9-_]+\\.)*[a-z0-9-_]+$")) {
                Info.display(Messages.get("label.error", "Error"), Messages.getWithArgs("label.newJavaPackage.wrong", "Wrong package name {0}", new Object[] {nodeName}));
                return;
            }
            linker.loading("");
            StringBuilder parentPath = new StringBuilder(parent.getPath());
            Map<String, String> parentNodesType = new HashMap<String, String>();
            List<String> packages = new ArrayList<String>(Arrays.asList(nodeName.split("\\.")));
            String newNodeName = packages.remove(packages.size() - 1);
            String nodeType = "jnt:folder";
            for (String packageName : packages) {
                if (parentPath.length() == 0 || parentPath.charAt(parentPath.length() - 1) != '/') {
                    parentPath.append("/");
                }
                parentPath.append(packageName);
                parentNodesType.put(parentPath.toString(), nodeType);
            }
            JahiaContentManagementService.App.getInstance().createNode(parentPath.toString(), newNodeName, nodeType, null, null, null, null, null, parentNodesType, false, new BaseAsyncCallback<GWTJahiaNode>() {
                public void onSuccess(GWTJahiaNode node) {
                    linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                    linker.loaded();
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("node", node);
                    linker.refresh(data);
                }

                public void onApplicationFailure(Throwable throwable) {
                    linker.loaded();
                    Info.display(Messages.get("label.error", "Error"), Messages.getWithArgs("label.newJavaPackage.failed", "Failed to create package {0}", new Object[]{nodeName}));
                }
            });
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null
                && !lh.isLocked()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions())
                && isNodeTypeAllowed(lh.getSingleSelection()));
    }
}


