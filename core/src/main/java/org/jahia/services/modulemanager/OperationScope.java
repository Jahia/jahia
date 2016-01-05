/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * Defines the scope of the module operations, specifying, for example, which node types, this module should be deployed on.
 * 
 * @author Sergiy Shyrkov
 */
public class OperationScope implements Serializable {

    /**
     * This kind of operation will be executed on any kind of node.
     */
    public static final OperationScope ALL = new OperationScope(true, true, null);

    /**
     * Operation is targeted for all non-processing nodes.
     */
    public static final OperationScope NON_PROCESSING = new OperationScope(false, true, null);

    /**
     * The operation target is a processing node only.
     */
    public static final OperationScope PROCESSING = new OperationScope(true, false, null);

    private static final long serialVersionUID = -2467484655985467278L;

    private Set<String> onNodeTypes;

    private boolean onNonProcessingNode;

    private boolean onProcessingNode;

    /**
     * Initializes an instance of this class.
     * 
     * @param onProcessingNode
     *            <code>true</code> if the operation should be performed on processing node
     * @param onNonProcessingNode
     *            <code>true</code> if the operation should be performed on any non-processing node
     * @param onNodeTypes
     *            set of node types, the operation should be performed on; if none are specified (<code>null</code> case) the operation
     *            considered to be done any any node type
     */
    public OperationScope(boolean onProcessingNode, boolean onNonProcessingNode, Set<String> onNodeTypes) {
        super();
        this.onProcessingNode = onProcessingNode;
        this.onNonProcessingNode = onNonProcessingNode;
        this.onNodeTypes = onNodeTypes;
    }

    public Set<String> getOnNodeTypes() {
        return onNodeTypes;
    }

    public boolean isOnNonProcessingNode() {
        return onNonProcessingNode;
    }

    public boolean isOnProcessingNode() {
        return onProcessingNode;
    }

    public void setOnNodeTypes(Set<String> onNodeTypes) {
        this.onNodeTypes = onNodeTypes;
    }

    public void setOnNonProcessingNode(boolean onNonProcessingNode) {
        this.onNonProcessingNode = onNonProcessingNode;
    }

    public void setOnProcessingNode(boolean onProcessingNode) {
        this.onProcessingNode = onProcessingNode;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
