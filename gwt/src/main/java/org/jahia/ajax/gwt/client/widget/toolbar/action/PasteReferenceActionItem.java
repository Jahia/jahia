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

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.*;

/**
 *
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:42 PM
*
*/
public class PasteReferenceActionItem extends BaseActionItem  {
    protected transient List<String> allowedRefs;

    public void onComponentSelection() {
        if (CopyPasteEngine.getInstance().getCopiedNodes().size() == 1) {
            GWTJahiaNode copiedNode = CopyPasteEngine.getInstance().getCopiedNodes().get(0);
            Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
            props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(copiedNode.getDisplayName(), GWTJahiaNodePropertyType.STRING)));
            props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(copiedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
            ContentTypeWindow.createContent(linker, copiedNode.getName(), allowedRefs, props, linker.getSelectionContext().getSingleSelection(), true, false);
        } else {
            ContentActions.pasteReference(linker);
        }
    }

    public void handleNewLinkerSelection() {
        boolean b = false;
        if (!CopyPasteEngine.getInstance().getCopiedNodes().isEmpty()) {
            LinkerSelectionContext lh = linker.getSelectionContext();
            b = lh.getSingleSelection() != null
                    && !lh.isLocked()
                    && hasPermission(lh.getSelectionPermissions())
                    && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions())
                    && lh.isPasteAllowed();
            String refTypes = null;
            if (linker instanceof EditLinker && b) {
                final Module module = ((EditLinker) linker).getSelectedModule();
                refTypes = module.getReferenceTypes();
            } else if (lh.getSingleSelection() != null) {
                refTypes = lh.getSingleSelection().get("referenceTypes");
            }
            if (refTypes != null && refTypes.length() > 0) {
                String[] refs = refTypes.split(" ");
                allowedRefs = new ArrayList<String>();
                for (String ref : refs) {
                    String[] types = ref.split("\\[|\\]");
                    if (types[1] != null && Arrays.asList(types[1].split(" |,")).contains("jnt:contentReference")) {
                        allowedRefs.add("jnt:contentReference");
                        break;
                    } else if (checkNodeType(CopyPasteEngine.getInstance().getCopiedNodes(), types[1])) {
                        allowedRefs.add(types[0]);
                    }
                }
                if (this.allowedRefs.size() == 0) {
                    b = false;
                }
            } else {
                b = false;
            }
        }
        setEnabled(b);
    }

    private boolean checkNodeType(List<GWTJahiaNode> sources, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

}
