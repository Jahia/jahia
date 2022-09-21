/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import org.apache.commons.lang3.StringUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * representation of a nodeType tree entry
 */
public class NodeTypeTreeEntry  implements Comparable<NodeTypeTreeEntry> {
    private String label;
    private String name;
    private ExtendedNodeType nodeType;
    private Set<NodeTypeTreeEntry> children;

    public NodeTypeTreeEntry(ExtendedNodeType nodeType, Locale uiLocale) {
        this.nodeType = nodeType;
        this.name = nodeType.getName();
        this.label = nodeType.getLabel(uiLocale);

    }
    public Set<NodeTypeTreeEntry> getChildren() {
        return children;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setChildren(Set<NodeTypeTreeEntry> children) {
        this.children = children;
    }

    public void add(NodeTypeTreeEntry entry) {
        if (children == null) {
            children = new HashSet<>();
        }
        children.add(entry);
    }

    public ExtendedNodeType getNodeType() {
        return nodeType;
    }

    @Override
    public int compareTo(NodeTypeTreeEntry otherNodeTypeTreeEntry) {
        return StringUtils.compareIgnoreCase(getLabel(), otherNodeTypeTreeEntry.getLabel());
    }
}
