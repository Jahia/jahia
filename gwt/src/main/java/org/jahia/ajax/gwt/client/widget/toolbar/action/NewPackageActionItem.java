/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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


