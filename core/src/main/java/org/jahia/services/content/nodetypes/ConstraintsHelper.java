package org.jahia.services.content.nodetypes;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Help to find allowed sub node types and references
 */
public class ConstraintsHelper {
    public static String getConstraints(JCRNodeWrapper node) throws RepositoryException {
        List<ExtendedNodeType> list = new ArrayList<ExtendedNodeType>(Arrays.asList(node.getMixinNodeTypes()));
        list.add(node.getPrimaryNodeType());
        return getConstraints(list);
    }

    public static String getConstraints(List<ExtendedNodeType> nodeType) {
        String constraints = "";
        for (ExtendedNodeType type : nodeType) {
            Set<String> cons = type.getUnstructuredChildNodeDefinitions().keySet();
            for (String s : cons) {
                if (!s.equals(Constants.JAHIANT_TRANSLATION)) {
                    constraints = (StringUtils.isEmpty(constraints)) ? s : constraints + " " + s;
                }
            }

        }
        return constraints;
    }

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
                                if (NodeTypeRegistry.getInstance().getNodeType(nt).isNodeType(refConstraint)) {
                                    finalConstraints.add(nt);
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
