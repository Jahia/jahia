/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper.CanUseComponentForEditCallback;

/**
 * Action item used to open the edit engine for the original content source.
 *
 * @author Sergiy Shyrkov
 */
public class EditSourceContentActionItem extends BaseActionItem {

	private static final long serialVersionUID = -2912157212228173779L;

	public void onComponentSelection() {
	    final GWTJahiaNode referencedNode = linker.getSelectionContext().getSingleSelection().getReferencedNode();
        String refNodeTypeName = referencedNode.getNodeTypes().get(0);
        GWTJahiaNodeType refNodeType = ModuleHelper.getNodeType(refNodeTypeName);
        if (refNodeType != null) {
            if (ModuleHelper.canUseComponentForEdit(refNodeType)) {
                EngineLoader.showEditEngine(linker, referencedNode, null);
            }
        } else {
            // we need to request the reference node type from server
            ModuleHelper.checkCanUseComponentForEdit(refNodeTypeName, new CanUseComponentForEditCallback() {
                @Override
                public void handle(boolean canUseComponentForEdit) {
                    if (canUseComponentForEdit) {
                        // we allow editing the referenced node
                        EngineLoader.showEditEngine(linker, referencedNode, null);
                    } else {
                        setEnabled(false);
                    }
                }
            });
        }
    }

	public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        final GWTJahiaNode singleSelection = lh.getSingleSelection();
        boolean enabled = singleSelection != null
                && singleSelection.isReference()
                && !lh.isRootNode()
                && hasPermission(lh.getSelectionPermissions())
                && PermissionsUtils.isPermitted("jcr:modifyProperties", lh.getSelectionPermissions());

        setEnabled(enabled);

        if (enabled) {
            ModuleHelper.checkCanUseComponentForEdit(singleSelection.getReferencedNode().getNodeTypes().get(0),
                    new CanUseComponentForEditCallback() {
                        @Override
                        public void handle(boolean canUseComponentForEdit) {
                            if (isEnabled() && !canUseComponentForEdit) {
                                setEnabled(false);
                            }
                        }
                    });
        }
	}
}
