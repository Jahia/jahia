/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.impl.StringBufferImplConcat;
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:38 PM
* 
*/
public class NewContentActionItem extends BaseActionItem  {
    private String nodeTypes = "";
    protected String parentTypes = "jnt:contentList jnt:contentFolder jmix:editorialContent";
    protected List<String> parentTypesAsList;
    private boolean useEngine = true;
    private String label;
    private boolean useMainNode = false;
    private boolean includeSubTypes = true;

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
                if (m == null) {
                    m = ((EditLinker) linker).getMainModule();
                }
                if (m instanceof ListModule) {
                    nodeTypes = m.getNodeTypes();
                } else if (m instanceof AreaModule) {
                    nodeTypes = m.getNodeTypes();
                }
            }

            if (nodeTypes.length() > 0) {
                ContentActions.showContentWizard(linker, nodeTypes, includeSubTypes);
            } else {
                ContentActions.showContentWizard(linker,
                        linker.getSelectionContext().getSingleSelection().getChildConstraints(), includeSubTypes);
            }
        } else {
            ContentActions.createNode(linker,getGwtToolbarItem().getTitle(),nodeTypes, useMainNode);
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
}
