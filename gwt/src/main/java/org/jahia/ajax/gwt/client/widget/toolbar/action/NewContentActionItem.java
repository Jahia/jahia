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

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.AreaModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ListModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:38 PM
* 
*/
@SuppressWarnings("serial")
public class NewContentActionItem extends BaseActionItem  {
    private String nodeTypes = "";
    private List<String> mixins;
    private List<GWTJahiaNodeProperty> nodeProperties;
    protected String parentTypes = "jnt:contentList jnt:contentFolder jmix:editorialContent";
    protected List<String> parentTypesAsList;
    private boolean useEngine = true;
    private boolean useMainNode = false;
    private boolean includeSubTypes = true;
    private String newNodeName;

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setParentTypes(String parentType) {
        this.parentTypes = parentType;
    }

    public void setIncludeSubTypes(boolean includeSubTypes) {
        this.includeSubTypes = includeSubTypes;
    }

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        parentTypesAsList = Arrays.asList(parentTypes.split(" "));
    }

    public void onComponentSelection() {
        if (useEngine) {
            String nodeTypes = this.nodeTypes;
            if (linker instanceof EditLinker) {
                Module m = ((EditLinker) linker).getSelectedModule();
                if (m == null || useMainNode) {
                    m = ((EditLinker) linker).getMainModule();
                }
                if (m instanceof ListModule) {
                    nodeTypes = m.getNodeTypes();
                } else if (m instanceof AreaModule) {
                    nodeTypes = m.getNodeTypes();
                }
            }
            GWTJahiaNode parent;
            if (useMainNode) {
                parent = linker.getSelectionContext().getMainNode();
            } else {
                parent = linker.getSelectionContext().getSingleSelection();
            }

            if (nodeTypes.length() > 0) {
                ContentActions.showContentWizard(linker, nodeTypes, parent, includeSubTypes);
            } else {
                ContentActions.showContentWizard(linker, parent.getChildConstraints(), parent, includeSubTypes);
            }
        } else {
            ContentActions.createNode(newNodeName, linker,getGwtToolbarItem().getTitle(),nodeTypes, mixins, nodeProperties, useMainNode);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        GWTJahiaNode n;
        if (useMainNode) {
            n = lh.getMainNode();
        }   else {
            n = lh.getSingleSelection();
        }
        if (n != null) {
            boolean isValidParent = false;
            for (String s : parentTypesAsList) {
                isValidParent = n.getNodeTypes().contains(s) || n.getInheritedNodeTypes().contains(s);
                if (isValidParent) {
                    break;
                }
            }
            setEnabled(isValidParent && !"".equals(n.getChildConstraints().trim())
                    && !lh.isLocked()
                    && hasPermission(JahiaGWTParameters.getSiteNode())
                    && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()));
        } else {
            setEnabled(false);
        }
    }

    public void setUseEngine(boolean useEngine) {
        this.useEngine = useEngine;
    }

    public void setUseMainNode(boolean useMainNode) {
        this.useMainNode = useMainNode;
    }

    public void setNewNodeName(String newNodeName) {
        this.newNodeName = newNodeName;
    }

    public void setMixins(List<String> mixins) {
        this.mixins = mixins;
    }

    public void setNodeProperties(Map<String, String> properties) {
        if (properties == null) {
            this.nodeProperties = null;
            return;
        }
        this.nodeProperties = new ArrayList<GWTJahiaNodeProperty>(properties.size());
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            this.nodeProperties.add(new GWTJahiaNodeProperty(entry.getKey(), entry.getValue()));
        }
    }

    public void setNodePropertyList(List<GWTJahiaNodeProperty> nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

}
