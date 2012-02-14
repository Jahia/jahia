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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

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

    public static String getConstraints(JCRNodeWrapper node, String targetChildName) throws RepositoryException {
        List<ExtendedNodeType> list = new ArrayList<ExtendedNodeType>(Arrays.asList(node.getMixinNodeTypes()));
        list.add(node.getPrimaryNodeType());
        return getConstraints(list, targetChildName);
    }

    public static String getConstraints(List<ExtendedNodeType> nodeType, String targetChildName) {
        String constraints = "";
        Set<String> nodeTypeNames = new HashSet<String>();

        if (targetChildName == null) {
            // first let's retrieve the child node types for unstructured node definitions
            for (ExtendedNodeType type : nodeType) {
                Set<String> cons = type.getUnstructuredChildNodeDefinitions().keySet();
                for (String s : cons) {
                    if (!s.equals(Constants.JAHIANT_TRANSLATION) &&
                            !s.equals(Constants.JAHIANT_REFERENCEINFIELD) &&
                            !nodeTypeNames.contains(s)) {
                        constraints = (StringUtils.isEmpty(constraints)) ? s : constraints + " " + s;
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
                            if (!typeName.equals("jnt:conditionalVisibility") &&
                                    !typeName.equals("jnt:vanityUrls") &&
                                    !typeName.equals("jnt:acl") &&
                                    !nodeTypeNames.contains(typeName)) {
                                constraints = (StringUtils.isEmpty(constraints)) ? typeName : constraints + " " + typeName;
                                nodeTypeNames.add(typeName);
                            }
                        }
                    }
                }
            }
        }
        return constraints;
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
        StringBuffer buffer = new StringBuffer();
        List<ExtendedNodeType> refs =
                NodeTypeRegistry.getInstance().getNodeType("jmix:nodeReference").getSubtypesAsList();

        String[] nodeTypesArray;
        if (StringUtils.isEmpty(nodeTypes)) {
            nodeTypesArray = new String[]{"nt:base"};
        } else {
            nodeTypesArray = nodeTypes.split(" ");
        }
        final String[] constraintsArray = constraints.split(" ");
        for (ExtendedNodeType ref : refs) {
            if (ref.getPropertyDefinitionsAsMap().get("j:node") != null) {
                for (String s : constraintsArray) {
                    if (s.equals("nt:base") && constraintsArray.length > 1) {
                        continue;
                    }
                    if (ref.isNodeType(s)) {
                        String[] refConstraints = ref.getPropertyDefinitionsAsMap().get("j:node").getValueConstraints();
                        if (refConstraints.length == 0) {
                            refConstraints = new String[]{"jmix:droppableContent"};
                        }
                        List<String> finalConstraints = new ArrayList<String>();
                        for (String refConstraint : refConstraints) {
                            for (String nt : nodeTypesArray) {
                                // if a node type is descending from a reference constraint, then use the node type
                                if (NodeTypeRegistry.getInstance().getNodeType(nt).isNodeType(refConstraint)) {
                                    finalConstraints.add(nt);
                                    // otherwise if the reference constraint is descending from a node type, use the constraint
                                } else if (NodeTypeRegistry.getInstance().getNodeType(refConstraint).isNodeType(nt)) {
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
        return buffer.toString().trim();
    }
}
