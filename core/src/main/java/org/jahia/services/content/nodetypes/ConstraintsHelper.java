/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Help to find allowed sub node types and references
 */
public class ConstraintsHelper {

    public static String getConstraints(JCRNodeWrapper node) throws RepositoryException {
        return getConstraints(node, null);
    }

    public static Set<String> getConstraintSet(JCRNodeWrapper node) throws RepositoryException {
        return getConstraintSet(node, null);
    }

    public static String getConstraints(JCRNodeWrapper node, String targetChildName) throws RepositoryException {
        return StringUtils.join(getConstraintSet(node, targetChildName), ' ');
    }

    public static Set<String> getConstraintSet(JCRNodeWrapper node, String targetChildName) throws RepositoryException {
        List<ExtendedNodeType> list = new ArrayList<ExtendedNodeType>(Arrays.asList(node.getMixinNodeTypes()));
        list.add(node.getPrimaryNodeType());
        return getConstraintSet(list, targetChildName);
    }

    public static String getConstraints(List<ExtendedNodeType> nodeType, String targetChildName) {
        return StringUtils.join(getConstraintSet(nodeType, targetChildName), ' ');
    }

    public static Set<String> getConstraintSet(List<ExtendedNodeType> nodeType, String targetChildName) {

        Set<String> nodeTypeNames = new HashSet<String>();

        if (targetChildName == null) {
            // first let's retrieve the child node types for unstructured node definitions
            for (ExtendedNodeType type : nodeType) {
                Set<String> cons = type.getUnstructuredChildNodeDefinitions().keySet();
                for (String s : cons) {
                    if (!s.equals(Constants.JAHIANT_TRANSLATION) && !s.equals(Constants.JAHIANT_REFERENCEINFIELD)) {
                        nodeTypeNames.add(s);
                    }
                }
            }
        }

        if (targetChildName != null) {
            // now let's retrieve the structured child node definitions
            for (ExtendedNodeType type : nodeType) {
                Map<String, ExtendedNodeDefinition> nodeDefinitions = type.getChildNodeDefinitionsAsMap();
                for (Map.Entry<String, ExtendedNodeDefinition> nodeDefinitionEntry : nodeDefinitions.entrySet()) {
                    String childName = nodeDefinitionEntry.getKey();
                    boolean useCurrentChild = true;
                    if (targetChildName != null && !targetChildName.equals("*")) {
                        if (!childName.equals(targetChildName)) {
                            useCurrentChild = false;
                        }
                    }
                    if (useCurrentChild) {
                        String[] typeNames = nodeDefinitionEntry.getValue().getRequiredPrimaryTypeNames();
                        for (String typeName : typeNames) {
                            if (!typeName.equals("jnt:conditionalVisibility") && !typeName.equals("jnt:vanityUrls") && !typeName.equals("jnt:acl")) {
                                nodeTypeNames.add(typeName);
                            }
                        }
                    }
                }
            }
        }

        return nodeTypeNames;
    }

    /**
     * Builds a list of usable reference types for the specified node types and constraints. If the no node types
     * were specified it will default to the nt:base node type.
     * <p/>
     * This method will also inspect all the value constraints that are set on all reference nodes and if there
     * are none it will use the default jmix:droppableContent type.
     *
     * @param constraints
     * @param nodeTypes
     * @return
     * @throws NoSuchNodeTypeException
     */
    public static String getReferenceTypes(String constraints, String nodeTypes) throws NoSuchNodeTypeException {
        StringBuilder buffer = new StringBuilder();
        List<ExtendedNodeType> refs =
                NodeTypeRegistry.getInstance().getNodeType("jmix:nodeReference").getSubtypesAsList();

        List<ExtendedNodeType> nodeTypesList = new ArrayList<ExtendedNodeType>();
        List<ExtendedNodeType> referencesNodeTypesList = new ArrayList<ExtendedNodeType>();

        if (StringUtils.isEmpty(nodeTypes)) {
            nodeTypesList.add(NodeTypeRegistry.getInstance().getNodeType("nt:base"));
        } else {
            for (String s : Patterns.SPACE.split(nodeTypes)) {
                ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                if (nt.isNodeType("jmix:nodeReference")) {
                    referencesNodeTypesList.add(nt);
                } else {
                    nodeTypesList.add(nt);
                }
            }
        }
        final String[] constraintsArray = Patterns.SPACE.split(constraints);
        for (ExtendedNodeType ref : refs) {
            if (ref.getPropertyDefinitionsAsMap().get(Constants.NODE) != null) {
                for (String s : constraintsArray) {
                    if (s.equals(Constants.NT_BASE) && constraintsArray.length > 1) {
                        continue;
                    }
                    if (ref.isNodeType(s)) {
                        String[] refConstraints = ref.getPropertyDefinitionsAsMap().get(Constants.NODE).getValueConstraints();
                        if (refConstraints.length == 0) {
                            refConstraints = new String[]{"jmix:droppableContent"};
                        }
                        List<String> finalConstraints = new ArrayList<String>();
                        for (String refConstraint : refConstraints) {
                            for (ExtendedNodeType nt : nodeTypesList) {
                                // if a node type is descending from a reference constraint, then use the node type
                                if (nt.isNodeType(refConstraint)) {
                                    finalConstraints.add(nt.getName());
                                    // otherwise if the reference constraint is descending from a node type, use the constraint
                                } else if (NodeTypeRegistry.getInstance().getNodeType(refConstraint).isNodeType(nt.getName())) {
                                    finalConstraints.add(refConstraint);
                                }
                            }
                        }

                        refConstraints = finalConstraints.toArray(new String[finalConstraints.size()]);
                        if (refConstraints.length > 0) {
                            buffer.append(ref.getName());
                            buffer.append("[");
                            if (refConstraints.length > 0) {
                                for (int i = 0; i < refConstraints.length; i++) {
                                    buffer.append(refConstraints[i]);
                                    if (i + 1 < refConstraints.length) {
                                        buffer.append(",");
                                    }
                                }
                            }
                            buffer.append("] ");
                        }
                        break;
                    }
                }
            }
        }
        for (ExtendedNodeType ref : referencesNodeTypesList) {
            if (ref.getPropertyDefinitionsAsMap().get(Constants.NODE) != null) {
                String[] refConstraints = ref.getPropertyDefinitionsAsMap().get(Constants.NODE).getValueConstraints();
                buffer.append(ref.getName());
                buffer.append("[");
                if (refConstraints.length > 0) {
                    for (int i = 0; i < refConstraints.length; i++) {
                        buffer.append(refConstraints[i]);
                        if (i + 1 < refConstraints.length) {
                            buffer.append(",");
                        }
                    }
                }
                buffer.append("] ");
            }
        }
        return buffer.toString().trim();
    }
}
