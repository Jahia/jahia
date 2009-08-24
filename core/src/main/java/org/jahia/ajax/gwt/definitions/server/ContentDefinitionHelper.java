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

import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaPropertyDefinition;
import org.jahia.ajax.gwt.content.server.helper.Utils;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.*;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.apache.log4j.Logger;

import javax.jcr.nodetype.NoSuchNodeTypeException;
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

    private static final Logger logger = Logger.getLogger(ContentDefinitionHelper.class) ;

    private static final List<String> excludedItems = Arrays.asList("j:locktoken", "jcr:lockOwner", "jcr:lockIsDeep",
            "j:nodename", "j:fullpath", "j:applyAcl", "jcr:uuid", "j:fieldsinuse");
    
    private static final List<String> excludedTypes = Arrays.asList("nt:base", "jnt:workflow", "jnt:extraResource");
    
    private static final Comparator<GWTJahiaNodeType> gwtJahiaNodeTypeNameComparator = new Comparator<GWTJahiaNodeType>() {
        public int compare(GWTJahiaNodeType o1, GWTJahiaNodeType o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public static GWTJahiaNodeType getNodeType(String name, ProcessingContext context) {
        ExtendedNodeType nodeType = null;
        try {
            nodeType = NodeTypeRegistry.getInstance().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            return null;
        }
        GWTJahiaNodeType gwt = getGWTJahiaNodeType(context, nodeType);
        return gwt;
    }

    private static GWTJahiaNodeType getGWTJahiaNodeType(ProcessingContext context, ExtendedNodeType nodeType) {
        GWTJahiaNodeType gwt = new GWTJahiaNodeType();
        gwt.setName(nodeType.getName());
        Locale loc = context.getLocale() ;
        String label = nodeType.getLabel(loc) ;
        gwt.setLabel(label);

        List<ExtendedItemDefinition> defs;

        defs = nodeType.getItems();

        List<GWTJahiaItemDefinition> items = new ArrayList<GWTJahiaItemDefinition>();
        List<GWTJahiaItemDefinition> inheritedItems = new ArrayList<GWTJahiaItemDefinition>();
        JahiaResourceBundle rb = null;
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
                        boolean useResourceBundle = epd.getSelectorOptions().containsKey("resourceBundle");
                        if (useResourceBundle && rb == null) {
                            rb = new JahiaResourceBundle(null, context.getLocale(), context.getSite() != null ? context.getSite().getTemplatePackageName() : null);
                        }
                        List<GWTJahiaValueDisplayBean> l = new ArrayList<GWTJahiaValueDisplayBean>();
                        for (String s : constr) {
                            l.add(new GWTJahiaValueDisplayBean(s, useResourceBundle ? rb.get(s, s) : s));
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
                if (def.isJahiaContentItem()) {
                    item.setDataType(GWTJahiaItemDefinition.CONTENT);
                } else if (def.isMetadataItem())  {
                    item.setDataType(GWTJahiaItemDefinition.METADATA);
                } else if (def.isSystemItem()) {
                    item.setDataType(GWTJahiaItemDefinition.SYSTEM);

                }
                if (def.getDeclaringNodeType().getName().equals(nodeType.getName())) {
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

    public static Map<GWTJahiaNodeType,List<GWTJahiaNodeType>> getNodeTypes(ProcessingContext ctx) {
        Map<GWTJahiaNodeType,List<GWTJahiaNodeType>> map = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType("jmix:content");
            ExtendedNodeType[] nts = nt.getDeclaredSubtypes();

            for (ExtendedNodeType mainType : nts) {
                List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>();
                map.put(getGWTJahiaNodeType(ctx,mainType), l);
                for (ExtendedNodeType subType : mainType.getSubtypes()) {
                    l.add(getGWTJahiaNodeType(ctx,subType));
                };
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return map;
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
                        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>(Arrays.asList(extendedNodeType.getSubtypes()));
                        types.add(extendedNodeType);
                        for (ExtendedNodeType nodeType : types) {
                            if (!excludedTypes.contains(nodeType.getName()) && !nodeType.isMixin() && !nodeType.isAbstract()) {
                                gwtNodeTypes.add(getGWTJahiaNodeType(ctx, nodeType));
                            }
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
                            gwtNodeTypes.add(getGWTJahiaNodeType(ctx, nodeType));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Collections.sort(gwtNodeTypes, gwtJahiaNodeTypeNameComparator);
        
        return gwtNodeTypes;
    }

}
