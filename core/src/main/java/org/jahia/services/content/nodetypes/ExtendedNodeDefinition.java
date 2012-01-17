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

package org.jahia.services.content.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:08:56
 * 
 */
public class ExtendedNodeDefinition extends ExtendedItemDefinition implements NodeDefinition {
    private static final transient Logger logger = LoggerFactory.getLogger(ExtendedItemDefinition.class);

    private NodeTypeRegistry registry;

    private String[] requiredPrimaryTypes;
    private String defaultPrimaryType;
    private boolean allowsSameNameSiblings;
//    private boolean liveContent = false;
    private String workflow;

    public ExtendedNodeDefinition(NodeTypeRegistry registry) {
        this.registry = registry;
    }

    public void setDeclaringNodeType(ExtendedNodeType declaringNodeType) {
        super.setDeclaringNodeType(declaringNodeType);
        declaringNodeType.setNodeDefinition(getName(), this);
    }

    public ExtendedNodeType[] getRequiredPrimaryTypes() {
        if (requiredPrimaryTypes == null) {
            return null;
        }
        ExtendedNodeType[] res = new ExtendedNodeType[requiredPrimaryTypes.length];
        for (int i = 0; i < requiredPrimaryTypes.length; i++) {
            try {
                res[i] = registry.getNodeType(requiredPrimaryTypes[i]);
            } catch (NoSuchNodeTypeException e) {
                logger.error("Nodetype not found",e);
            }
        }
        return res;
    }

    public String[] getRequiredPrimaryTypeNames() {
        return requiredPrimaryTypes;
    }


    public void setRequiredPrimaryTypes(String[] requiredPrimaryTypes) {
        this.requiredPrimaryTypes = requiredPrimaryTypes;
    }

    public ExtendedNodeType getDefaultPrimaryType() {
        if (defaultPrimaryType != null) {
            try {
                return registry.getNodeType(defaultPrimaryType);
            } catch (NoSuchNodeTypeException e) {
                logger.error("Nodetype not found",e);
            }
        }
        return null;
    }

    public String getDefaultPrimaryTypeName() {
        return defaultPrimaryType;
    }

    public void setDefaultPrimaryType(String defaultPrimaryType) {
        this.defaultPrimaryType = defaultPrimaryType;
    }

    public boolean allowsSameNameSiblings() {
        return allowsSameNameSiblings;
    }

    public void setAllowsSameNameSiblings(boolean allowsSameNameSiblings) {
        this.allowsSameNameSiblings = allowsSameNameSiblings;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

//    public boolean isLiveContent() {
//        return liveContent;
//    }
//
//    public void setLiveContent(boolean liveContent) {
//        this.liveContent = liveContent;
//    }

    public boolean isNode() {
        return true;
    }

}
