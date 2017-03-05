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

import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Abstract action item that considers allowed node types, i.e. a selected node should have of the allowed node types for this action to be
 * enabled.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public abstract class NodeTypeAwareBaseActionItem extends BaseActionItem {

    protected List<String> allowedNodeTypes;

    protected List<String> forbiddenNodeTypes;

    /**
     * Returns <code>true</code> if the provided node has none of the forbidden types and has one of the allowed node types.
     * 
     * @param selectedNode
     *            the currently selected node
     * @return <code>true</code> if the provided node has none of the forbidden types and has one of the allowed node types
     */
    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        if (selectedNode == null) {
            return true;
        }
        return (forbiddenNodeTypes == null || !selectedNode.isNodeType(forbiddenNodeTypes))
                && (allowedNodeTypes == null || selectedNode.isNodeType(allowedNodeTypes));

    }

    /**
     * Returns <code>true</code> if all of the selected nodes pass the {@link #isNodeTypeAllowed(GWTJahiaNode)} check.
     * 
     * @param selection
     *            currently selected nodes
     * @return <code>true</code> if all of the selected nodes pass the {@link #isNodeTypeAllowed(GWTJahiaNode)} check
     */
    protected boolean isNodeTypeAllowed(List<GWTJahiaNode> selection) {
        if (selection == null) {
            return true;
        }
        boolean allowed = true;
        for (GWTJahiaNode selected : selection) {
            if (!isNodeTypeAllowed(selected)) {
                allowed = false;
                break;
            }
        }
        return allowed;

    }

    public void setAllowedNodeTypes(List<String> allowedNodeTypes) {
        this.allowedNodeTypes = allowedNodeTypes != null && allowedNodeTypes.isEmpty() ? null
                : allowedNodeTypes;
    }

    public void setForbiddenNodeTypes(List<String> forbiddenNodeTypes) {
        this.forbiddenNodeTypes = forbiddenNodeTypes != null && forbiddenNodeTypes.isEmpty() ? null
                : forbiddenNodeTypes;
    }

}
