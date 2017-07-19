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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

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
            ModuleHelper.loadNodeType(refNodeTypeName, new BaseAsyncCallback<GWTJahiaNodeType>() {
                public void onSuccess(GWTJahiaNodeType result) {
                    if (ModuleHelper.canUseComponentForEdit(result)) {
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
        if (enabled) {
            String refNodeTypeName = singleSelection.getReferencedNode().getNodeTypes().get(0);
            GWTJahiaNodeType refNodeType = ModuleHelper.getNodeType(refNodeTypeName);
            if (refNodeType != null) {
                // we have the reference node type here
                enabled = ModuleHelper.canUseComponentForEdit(refNodeType);
            } else {
                // we need to request the reference node type from server
                // as it is done asynchronously, we enable the action item, but will do the check once again in the onComponentSelection() method
                ModuleHelper.loadNodeType(refNodeTypeName, new BaseAsyncCallback<GWTJahiaNodeType>() {
                    public void onSuccess(GWTJahiaNodeType result) {
                        if (!ModuleHelper.canUseComponentForEdit(result)) {
                            // we disable this action item
                            setEnabled(false);
                        }
                    }
                });
            }
        }
        
        setEnabled(enabled);
	}
}
