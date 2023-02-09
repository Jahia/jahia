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

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;

/**
 *
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:20 PM
 */
public class PasteActionItem extends NodeTypeAwareBaseActionItem {
    private boolean pasteInMainNode;
    private List<String> childNodeTypesToSkip;

    public boolean isPasteInMainNode() {
        return pasteInMainNode;
    }

    public void setPasteInMainNode(boolean pasteInMainNode) {
        this.pasteInMainNode = pasteInMainNode;
    }

    public void onComponentSelection() {
        GWTJahiaNode m = linker.getSelectionContext().getSingleSelection();
        if (pasteInMainNode) {
            m = linker.getSelectionContext().getMainNode();
        }
        if (m != null) {
            final String pastingMessage = Messages.get("statusbar.pasting.label");
            final String messageStyleName = "x-mask-loading";
            if (linker instanceof ManagerLinker) {
                ManagerLinker managerLinker = (ManagerLinker) linker;
                managerLinker.getLeftComponent().mask(pastingMessage, messageStyleName);
                managerLinker.getTopRightComponent().mask(pastingMessage, messageStyleName);
            } else {
                linker.loading(pastingMessage);
            }
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            copyPasteEngine.paste(m, linker, childNodeTypesToSkip, null);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b = false;
        final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
        if (!copyPasteEngine.getCopiedNodes().isEmpty()) {
            if (pasteInMainNode) {
                b = lh.getMainNode() != null && hasPermission(lh.getMainNode()) && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getMainNode());
            } else {
                b = lh.getSingleSelection() != null && hasPermission(lh.getSelectionPermissions()) &&
                        PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()) && lh.isPasteAllowed();
            }

            if (b && linker instanceof EditLinker) {
                final GWTJahiaNode singleSelection = linker.getSelectionContext().getSingleSelection();
                final List<String> nodeTypeNames = singleSelection.getNodeTypes();
                final List<String> inheritedNodeTypeNames = singleSelection.getInheritedNodeTypes();
                if (inheritedNodeTypeNames != null) {
                    nodeTypeNames.addAll(inheritedNodeTypeNames);
                }
                final StringBuilder nodeTypes = new StringBuilder(nodeTypeNames.size() * 15);
                boolean isFirst = true;
                for (String nodeType : nodeTypeNames) {
                    if (isFirst) {
                        nodeTypes.append(nodeType);
                        isFirst = false;
                    } else {
                        nodeTypes.append(",").append(nodeType);
                    }
                }
                b = copyPasteEngine.checkNodeType(nodeTypes.toString(), true);
            }
        }
        boolean isCutWithNodeTypesSkipped = childNodeTypesToSkip != null && copyPasteEngine.isCut();
        setEnabled(b && !isCutWithNodeTypesSkipped && super.isNodeTypeAllowed(copyPasteEngine.getCopiedNodes()));
    }

    public void setChildNodeTypesToSkip(List childNodeTypesToSkip) {
        this.childNodeTypesToSkip = childNodeTypesToSkip;
    }
}
