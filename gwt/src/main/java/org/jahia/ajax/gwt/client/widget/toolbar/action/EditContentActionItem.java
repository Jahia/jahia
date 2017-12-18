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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.Arrays;
import java.util.List;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:03 PM
*/
public class EditContentActionItem extends NodeTypeAwareBaseActionItem {
    
    private static final long serialVersionUID = 1899385924986263120L;
    
    private boolean allowRootNodeEditing;
    private boolean useMainNode = false;
    private String path = null;
    private String configurationName;

    public void onComponentSelection() {
        GWTJahiaNode singleSelection;
        if (path != null) {
            String replacedPath = URL.replacePlaceholders(path, linker.getSelectionContext().getSingleSelection());
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(replacedPath), null, new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> result) {
                    EngineLoader.showEditEngine(linker, result.get(0), linker.getConfig().getEngineConfiguration(configurationName));
                }
            });
            return;
        } else if (useMainNode) {
            singleSelection = linker.getSelectionContext().getMainNode();
        }   else {
            singleSelection = linker.getSelectionContext().getSingleSelection();
        }
        EngineLoader.showEditEngine(linker, singleSelection, linker.getConfig().getEngineConfiguration(configurationName));
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode singleSelection = lh.getSingleSelection();
        setEnabled(singleSelection != null
                && isNodeTypeAllowed(singleSelection)
                && hasPermission(lh.getSelectionPermissions())
                && (allowRootNodeEditing || !lh.isRootNode())
                && PermissionsUtils.isPermitted("jcr:modifyProperties", lh.getSelectionPermissions()));
    }

    public void setAllowRootNodeEditing(boolean allowRootNodeEditing) {
        this.allowRootNodeEditing = allowRootNodeEditing;
    }

    public void setUseMainNode(boolean useMainNode) {
        this.useMainNode = useMainNode;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        GWTJahiaNodeType nodeType = ModuleHelper.getNodeType(selectedNode.getNodeTypes().get(0));
        if (nodeType != null && !ModuleHelper.canUseComponentForEdit(nodeType)) {
            return false;
        }

        return super.isNodeTypeAllowed(selectedNode);
    }
}
