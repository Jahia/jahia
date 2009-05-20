/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:08:56
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedNodeDefinition extends ExtendedItemDefinition implements NodeDefinition {
    private static final transient Logger logger = Logger
            .getLogger(ExtendedItemDefinition.class);

    private NodeTypeRegistry registry;

    private String[] requiredPrimaryTypes;
    private String defaultPrimaryType;
    private boolean allowsSameNameSiblings;
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

    public String[] getRequiredPrimaryTypesNames() {
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

    public boolean isNode() {
        return true;
    }

}
