/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.definitions.server;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaPropertyDefinition;
import org.jahia.ajax.gwt.content.server.helper.Utils;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.*;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeType;
import javax.jcr.Value;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Helper class for accessing node types and definitions. 
 *
 * @author Thomas Draier
 * Date: Sep 12, 2008 - 11:48:20 AM
 */
public class ContentDefinitionHelper {

    private final static Logger logger = Logger.getLogger(ContentDefinitionHelper.class) ;

    private static List<String> excludedItems = Arrays.asList("j:locktoken", "jcr:lockOwner", "jcr:lockIsDeep",
            "j:nodename", "j:fullpath", "j:applyAcl", "jcr:uuid", "j:fieldsinuse");
    private static List<String> excludedTypes = Arrays.asList("nt:base", "");


    public static GWTJahiaNodeType getNodeType(String name, ProcessingContext context) {
        ExtendedNodeType nodeType = null;
        try {
            nodeType = NodeTypeRegistry.getInstance().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            return null;
        }
        GWTJahiaNodeType gwt = new GWTJahiaNodeType();
        gwt.setName(nodeType.getName());
        Locale loc = context.getLocale() ;
        String label = nodeType.getLabel(loc) ;
        gwt.setLabel(label);

        List<ExtendedItemDefinition> defs;

        defs = nodeType.getItems();

        List<GWTJahiaItemDefinition> items = new ArrayList<GWTJahiaItemDefinition>();
        List<GWTJahiaItemDefinition> inheritedItems = new ArrayList<GWTJahiaItemDefinition>();
        for (ExtendedItemDefinition def : defs) {
            if (!excludedTypes.contains(def.getDeclaringNodeType().getName()) && !excludedItems.contains(def.getName())) {
                GWTJahiaItemDefinition item;
                if (def.isNode()) {
                    GWTJahiaNodeDefinition node = new GWTJahiaNodeDefinition();
                    ExtendedNodeDefinition end = (ExtendedNodeDefinition)def;
                    item = node;
                    node.setRequiredPrimaryTypes(end.getRequiredPrimaryTypesNames());
                    node.setDefaultPrimaryType(end.getDefaultPrimaryTypeName());
                    node.setAllowsSameNameSiblings(end.allowsSameNameSiblings());
                    node.setWorkflow(end.getWorkflow());
                } else {
                    GWTJahiaPropertyDefinition prop = new GWTJahiaPropertyDefinition();
                    ExtendedPropertyDefinition epd = (ExtendedPropertyDefinition)def;
                    prop.setInternationalized(epd.isInternationalized());
                    prop.setRequiredType(epd.getRequiredType());
                    prop.setMultiple(epd.isMultiple());
                    String[] constr = epd.getValueConstraints() ;
                    boolean constrained = constr != null && constr.length > 0 ;
                    prop.setConstrained(constrained);
                    if (constrained) {
                        List<String> l = new ArrayList<String>();
                        for (String s : constr) {
                            try {
                                ResourceBundleMarker resourceBundleMarker = ResourceBundleMarker.parseMarkerValue(s);
                                if (resourceBundleMarker != null) {
                                    l.add(resourceBundleMarker.getValue(context.getLocale()));
                                } else {
                                    l.add(s);
                                }
                            } catch (JahiaException e) {
                                l.add(s);
                            }
                        }
                        prop.setValueConstraints(l);
                    }
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    for (Value value : epd.getDefaultValues()) {
                        try {
                            gwtValues.add(Utils.convertValue(value));
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }
                    prop.setDefaultValues(gwtValues);
                    item = prop;
                }
                item.setAutoCreated(def.isAutoCreated());
                item.setLabel(def.getLabel(context.getLocale()));
                item.setMandatory(def.isMandatory());
                item.setHidden(def.isHidden());
                item.setName(def.getName());
                item.setProtected(def.isProtected());
                item.setDeclaringNodeType(def.getDeclaringNodeType().getName());
                item.setSelector(def.getSelector());
                item.setSelectorOptions(new HashMap<String, String>(def.getSelectorOptions()));
                if (def.getDeclaringNodeType().getName().equals(name)) {
                items.add(item);
                } else {
                    inheritedItems.add(item);
                }
            }
        }
        gwt.setItems(items);
        gwt.setInheritedItems(inheritedItems);
        return gwt;
    }

    public static List<GWTJahiaNodeType> getNodeTypes(List<String> names, ProcessingContext context) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            for (String name : names) {
                list.add(getNodeType(name, context));
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static List<GWTJahiaNodeType> getNodeTypes(ProcessingContext context) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            NodeTypeIterator nti = NodeTypeRegistry.getInstance().getAllNodeTypes();
            while (nti.hasNext()) {
                NodeType nt = nti.nextNodeType();
                list.add(getNodeType(nt.getName(), context));
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns a list of node types with name and label populated that are the
     * sub-types of the specified base type or that are allowed to be created in
     * the specified parent node (if the baseType parameter is null).
     * 
     * @param baseType
     *            the node type name to find sub-types
     * @param parentNode
     *            the parent node, where the wizard was called
     * @param ctx
     *            current processing context instance
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type or that are allowed to be
     *         created in the specified parent node (if the baseType parameter
     *         is null)
     */
    public static List<GWTJahiaNodeType> getNodeSubtypes(String baseType,
            GWTJahiaNode parentNode, ProcessingContext ctx) {

        List<GWTJahiaNodeType> gwtNodeTypes = new ArrayList<GWTJahiaNodeType>();
        NodeTypeRegistry registry = NodeTypeRegistry.getInstance();
        try {
            if (baseType == null && parentNode != null) {
                Map<String, ExtendedNodeDefinition> definitions = new HashMap<String, ExtendedNodeDefinition>();
                for (String nodeTypeName : parentNode.getNodeTypes()) {
                    ExtendedNodeType nodeType = registry
                            .getNodeType(nodeTypeName);
                    definitions.putAll(nodeType.getChildNodeDefinitionsAsMap());
                }
                for (ExtendedNodeDefinition nodeDef : definitions.values()) {
                    if (nodeDef.getDeclaringNodeType().getName().equals(
                            Constants.JAHIANT_PAGE)
                            || nodeDef.getDeclaringNodeType().getName().equals(
                                    Constants.JAHIANT_JAHIACONTENT)) {
                        continue;
                    }
                    ExtendedNodeType[] requiredPrimaryTypes = nodeDef
                            .getRequiredPrimaryTypes();
//                    if (requiredPrimaryTypes[0]
//                            .isNodeType(Constants.JAHIANT_CONTAINERLIST)) {
//                        nodeDef = requiredPrimaryTypes[0]
//                                .getDeclaredChildNodeDefinitionsAsMap()
//                                .get("*");
//                        requiredPrimaryTypes = nodeDef
//                                .getRequiredPrimaryTypes();
//                    }
                    for (ExtendedNodeType extendedNodeType : requiredPrimaryTypes) {
                        if (!excludedTypes.contains(extendedNodeType.getName())) {
                            gwtNodeTypes
                                    .add(new GWTJahiaNodeType(extendedNodeType
                                            .getName(), extendedNodeType
                                            .getLabel(ctx.getLocale())));
                        }
                    }
                }
            } else {
                baseType = baseType != null ? baseType : "jnt:container";
                ExtendedNodeType baseNodeType = null;
                try {
                    baseNodeType = NodeTypeRegistry.getInstance().getNodeType(
                            baseType);
                } catch (NoSuchNodeTypeException e) {
                    logger.warn("Node type with the name '" + baseType
                            + "' cannot be found in the registry", e);
                }
                if (baseNodeType != null) {
                    ExtendedNodeType[] types = baseNodeType.getSubtypes();
                    for (int i = 0; i < types.length; i++) {
                        ExtendedNodeType nodeType = types[i];
                        if (!excludedTypes.contains(nodeType.getName())) {
                            gwtNodeTypes.add(new GWTJahiaNodeType(nodeType
                                    .getName(), nodeType.getLabel(ctx
                                    .getLocale())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return gwtNodeTypes;
    }

}
