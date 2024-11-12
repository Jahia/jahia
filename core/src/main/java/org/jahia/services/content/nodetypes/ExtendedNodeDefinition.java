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
package org.jahia.services.content.nodetypes;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ExtendedNodeDefinition that = (ExtendedNodeDefinition) o;

        if (getName().equals("*")) {
            if (!Arrays.equals(requiredPrimaryTypes, that.requiredPrimaryTypes)) return false;
        }

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Same as super.hashCode();
        return super.hashCode();
    }

    public void remove() {
        getDeclaringNodeType().removeNodeDefinition(this);
    }
}
