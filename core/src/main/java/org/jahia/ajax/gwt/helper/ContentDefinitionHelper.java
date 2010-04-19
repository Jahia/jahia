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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.jackrabbit.value.*;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;

/**
 * Helper class for accessing node types and definitions.
 *
 * @author Thomas Draier
 *         Date: Sep 12, 2008 - 11:48:20 AM
 */
public class ContentDefinitionHelper {
    private static final Logger logger = Logger.getLogger(ContentDefinitionHelper.class);

    private NavigationHelper navigation;
    private ChoiceListInitializerService choiceListInitializerService;

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setChoiceListInitializerService(ChoiceListInitializerService choiceListInitializerService) {
        this.choiceListInitializerService = choiceListInitializerService;
    }

    private static final List<String> excludedItems = Arrays.asList("j:locktoken", "jcr:lockOwner", "jcr:lockIsDeep",
            "j:nodename", "j:fullpath", "j:applyAcl", "jcr:uuid", "j:fieldsinuse");

    private static final List<String> excludedTypes = Arrays.asList("nt:base", "mix:versionable", "jnt:workflow", "jnt:extraResource");

    public GWTJahiaNodeType getNodeType(String name, Map<String, Object> context, Locale uiLocale) {
        ExtendedNodeType nodeType = null;
        try {
            nodeType = NodeTypeRegistry.getInstance().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            return null;
        }
        GWTJahiaNodeType gwt = getGWTJahiaNodeType(nodeType, context, uiLocale);
        return gwt;
    }

    public List<GWTJahiaNode> getPageTemplates(JCRSessionWrapper currentUserSession, JCRSiteNode site) throws GWTJahiaServiceException {
        try {
            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            JCRNodeWrapper node = site.getNode("templates");
            NodeIterator iterator = node.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper jcrNodeWrapper = (JCRNodeWrapper) iterator.next();
                nodes.add(navigation.getGWTJahiaNode(jcrNodeWrapper));
            }
            return nodes;
        } catch (RepositoryException e) {
            logger.error("Cannot find type", e);
            throw new GWTJahiaServiceException(e.toString());
        }
    }

    private GWTJahiaNodeType getGWTJahiaNodeType(ExtendedNodeType nodeType, Map<String, Object> context, Locale uiLocale) {
        return getGWTJahiaNodeType(nodeType, nodeType, context, uiLocale);
    }

    private GWTJahiaNodeType getGWTJahiaNodeType(ExtendedNodeType nodeType, ExtendedNodeType realNodeType, Map<String, Object> context, Locale uiLocale) {
        GWTJahiaNodeType gwt = new GWTJahiaNodeType();
        gwt.setName(nodeType.getName());
        gwt.setMixin(nodeType.isMixin());
        Locale loc = uiLocale;
        String label = nodeType.getLabel(loc);
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
                    ExtendedNodeDefinition end = (ExtendedNodeDefinition) def;
                    item = node;
                    node.setRequiredPrimaryTypes(end.getRequiredPrimaryTypeNames());
                    node.setDefaultPrimaryType(end.getDefaultPrimaryTypeName());
                    node.setAllowsSameNameSiblings(end.allowsSameNameSiblings());
                    node.setWorkflow(end.getWorkflow());
                } else {
                    GWTJahiaPropertyDefinition prop = new GWTJahiaPropertyDefinition();
                    ExtendedPropertyDefinition epd = (ExtendedPropertyDefinition) def;
                    prop.setInternationalized(epd.isInternationalized());
                    prop.setRequiredType(epd.getRequiredType());
                    prop.setMultiple(epd.isMultiple());
                    context.put("contextDefinition", nodeType);
                    String[] constr = epd.getValueConstraints();
                    boolean constrained = constr != null && constr.length > 0;
                    prop.setConstrained(constrained);
                    final Map<String, String> map = epd.getSelectorOptions();
                    final ArrayList<GWTJahiaValueDisplayBean> displayBeans = new ArrayList<GWTJahiaValueDisplayBean>(
                            32);
                    if (map.size() > 0) {
                        final Map<String, ChoiceListInitializer> initializers = choiceListInitializerService.getInitializers();
                        List<ChoiceListValue> listValues = null;
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if (initializers.containsKey(entry.getKey())) {
                                listValues = initializers.get(entry.getKey()).getChoiceListValues(epd, realNodeType, entry.getValue(), listValues, uiLocale, context);
                            }
                        }
                        if (listValues != null) {
                            for (ChoiceListValue choiceListValue : listValues) {
                                try {
                                    final GWTJahiaValueDisplayBean displayBean = new GWTJahiaValueDisplayBean(
                                            choiceListValue.getValue().getString(), choiceListValue.getDisplayName());
                                    final Map<String, Object> props = choiceListValue.getProperties();
                                    if (props != null) {
                                        for (Map.Entry<String, Object> objectEntry : props.entrySet()) {
                                            if (objectEntry.getKey() == null || objectEntry.getValue() == null) {
                                                logger.error("Null value : " + objectEntry.getKey() + " / " + objectEntry.getValue());
                                            } else {
                                                displayBean.set(objectEntry.getKey(), objectEntry.getValue());
                                            }
                                        }
                                    }
                                    displayBeans.add(displayBean);
                                } catch (RepositoryException e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        }
                    } else {
                        if (constrained) {
                            for (String s : constr) {
                                displayBeans.add(new GWTJahiaValueDisplayBean(s, s));
                            }
                        }
                    }
                    prop.setValueConstraints(displayBeans);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    for (Value value : epd.getDefaultValues()) {
                        try {
                            GWTJahiaNodePropertyValue convertedValue = convertValue(value, epd.getRequiredType());
                            if (convertedValue != null) {
                                gwtValues.add(convertedValue);
                            }
                        } catch (RepositoryException e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                    prop.setDefaultValues(gwtValues);
                    item = prop;
                }
                item.setAutoCreated(def.isAutoCreated());
                item.setLabel(def.getLabel(uiLocale));
                item.setMandatory(def.isMandatory());
                item.setHidden(def.isHidden());
                item.setName(def.getName());
                item.setProtected(def.isProtected());
                item.setDeclaringNodeType(def.getDeclaringNodeType().getName());
                if ("jcr:description".equals(def.getName())) {
                    item.setDeclaringNodeTypeLabel(def.getLabel(uiLocale));
                } else {
                    item.setDeclaringNodeTypeLabel(def.getDeclaringNodeType().getLabel(uiLocale));
                }
                item.setSelector(def.getSelector());
                item.setSelectorOptions(new HashMap<String, String>(def.getSelectorOptions()));
                if (def.isContentItem()) {
                    item.setDataType(GWTJahiaItemDefinition.CONTENT);
                } else if (def.isLayoutItem()) {
                    item.setDataType(GWTJahiaItemDefinition.LAYOUT);
                } else if (def.isMetadataItem()) {
                    item.setDataType(GWTJahiaItemDefinition.METADATA);
                } else if (def.isOptionItem()) {
                    item.setDataType(GWTJahiaItemDefinition.OPTIONS);
                } else if (def.isTemplateItem()) {
                    item.setDataType(GWTJahiaItemDefinition.TEMPLATE);
                } else if (def.isPublicationItem() || def.isCacheItem()) {
                    item.setDataType(GWTJahiaItemDefinition.PUBLICATION);
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
        List<String> supertypesNames = new ArrayList<String>();
        ExtendedNodeType[] nodeTypes = nodeType.getSupertypes();
        for (ExtendedNodeType type : nodeTypes) {
            supertypesNames.add(type.getName());
        }
        gwt.setSuperTypes(supertypesNames);
        String icon = navigation.getNodetypeIcons().get(nodeType.getName());
        if (icon != null) {
            gwt.setIcon("icon-" + icon);
        } else {
            gwt.setIcon("icon-" + navigation.getNodetypeIcons().get("default"));
        }


        return gwt;
    }

    public List<GWTJahiaNodeType> getNodeTypes(List<String> names, Map<String, Object> context, Locale uiLocale) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            for (String name : names) {
                list.add(getNodeType(name, context, uiLocale));
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getNodeTypes(Map<String, Object> ctx, Locale uiLocale) {
        Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> map = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType("jmix:droppableContent");
            NodeTypeIterator typeIterator = nt.getDeclaredSubtypes();
            while (typeIterator.hasNext()) {
                ExtendedNodeType mainType = (ExtendedNodeType) typeIterator.next();
                List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>();
                map.put(getGWTJahiaNodeType(mainType, ctx, uiLocale), l);
                NodeTypeIterator subtypes = mainType.getDeclaredSubtypes();
                while (subtypes.hasNext()) {
                    ExtendedNodeType nodeType = (ExtendedNodeType) subtypes.next();
                    l.add(getGWTJahiaNodeType(nodeType, ctx, uiLocale));
                }
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
     * @param baseTypes  the node type name to find sub-types
     * @param ctx        current processing context instance
     * @param uiLocale
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type or that are allowed to be
     *         created in the specified parent node (if the baseType parameter
     *         is null)
     */
    public Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getNodeSubtypes(String baseTypes, Map<String, Object> ctx, Locale uiLocale) {
        Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> map = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
        NodeTypeRegistry registry = NodeTypeRegistry.getInstance();
        try {
            ExtendedNodeType content = registry.getNodeType("jmix:droppableContent");
            NodeTypeIterator typeIterator = content.getDeclaredSubtypes();
            Map<String, ExtendedNodeType> contentTypes = new HashMap<String, ExtendedNodeType>();
            while (typeIterator.hasNext()) {
                ExtendedNodeType type = (ExtendedNodeType) typeIterator.next();
                contentTypes.put(type.getName(), type);
            }

            List<String> types;
            if (baseTypes == null) {
                types = new ArrayList<String>();
            } else {
                types = Arrays.asList(baseTypes.split(" "));
            }


            List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();
//            if (parentNode != null) {
//                Collection<ExtendedNodeDefinition> definitions = new ArrayList<ExtendedNodeDefinition>();
//
//                for (String nodeTypeName : parentNode.getNodeTypes()) {
//                    ExtendedNodeType nodeType = registry
//                            .getNodeType(nodeTypeName);
//                    definitions.addAll(nodeType.getUnstructuredChildNodeDefinitions().values());
//                }
//                for (ExtendedNodeDefinition nodeDef : definitions) {
//                    ExtendedNodeType[] requiredPrimaryTypes = nodeDef.getRequiredPrimaryTypes();
//                    for (ExtendedNodeType req : requiredPrimaryTypes) {
//                        recurseAdd(req, types, contentTypes, nodeTypes);
//                    }
//                }
//            } else
            if (baseTypes != null) {
                for (String type : types) {
                    recurseAdd(registry.getNodeType(type), types, contentTypes, nodeTypes);
                }
            } else {
                recurseAdd(content, types, contentTypes, nodeTypes);
            }

            typeIterator = content.getDeclaredSubtypes();
            while (typeIterator.hasNext()) {
                ExtendedNodeType mainType = (ExtendedNodeType) typeIterator.next();
                List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>();

                NodeTypeIterator subtypes = mainType.getDeclaredSubtypes();
                while (subtypes.hasNext()) {
                    ExtendedNodeType nodeType = (ExtendedNodeType) subtypes.next();
                    if (nodeTypes.contains(nodeType)) {
                        final GWTJahiaNodeType nt = getGWTJahiaNodeType(nodeType, ctx, uiLocale);
                        l.add(nt);
                        nodeTypes.remove(nodeType);
                    }
                }
                if (!l.isEmpty()) {
                    map.put(getGWTJahiaNodeType(mainType, ctx, uiLocale), l);
                }
            }
            if (!nodeTypes.isEmpty()) {
                List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>();
                for (ExtendedNodeType nodeType : nodeTypes) {
                    final GWTJahiaNodeType nt = getGWTJahiaNodeType(nodeType, ctx, uiLocale);
                    l.add(nt);
                }
                map.put(null, l);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return map;
    }

    private void recurseAdd(ExtendedNodeType req, List<String> baseTypes, Map<String, ExtendedNodeType> contentTypes, List<ExtendedNodeType> result) {
        boolean excludeNonDroppable = false;
        if (req.getName().equals("jmix:droppableContent") || contentTypes.keySet().contains(req.getName())) {
            excludeNonDroppable = true;
        }

        add(req, baseTypes, contentTypes, result, excludeNonDroppable);

        NodeTypeIterator subtypes = req.getSubtypes();
        while (subtypes.hasNext()) {
            ExtendedNodeType subtype = (ExtendedNodeType) subtypes.next();
            add(subtype, baseTypes, contentTypes, result, excludeNonDroppable);
        }
    }

    private void add(ExtendedNodeType type, List<String> baseTypes, Map<String, ExtendedNodeType> contentTypes, List<ExtendedNodeType> result, boolean excludeNonDroppable) {
        if (!excludedTypes.contains(type.getName()) && !type.isMixin() &&
                !type.isAbstract() &&
                (!excludeNonDroppable || CollectionUtils.containsAny(Arrays.asList(type.getDeclaredSupertypeNames()), contentTypes.keySet()))) {
            if (!baseTypes.isEmpty()) {
                for (String t : baseTypes) {
                    if (type.isNodeType(t)) {
                        result.add(type);
                        return;
                    }
                }
            } else {
                result.add(type);
            }
        }
    }

    public GWTJahiaNodePropertyValue convertValue(Value val, int requiredType) throws RepositoryException {
        String theValue;
        int type;

        switch (requiredType) {
            case PropertyType.BINARY:
                type = GWTJahiaNodePropertyType.BINARY;
                theValue = val.getString();
                break;
            case PropertyType.BOOLEAN:
                type = GWTJahiaNodePropertyType.BOOLEAN;
                theValue = String.valueOf(val.getBoolean());
                break;
            case PropertyType.DATE:
                type = GWTJahiaNodePropertyType.DATE;
                theValue = String.valueOf(val.getDate().getTimeInMillis());
                break;
            case PropertyType.DOUBLE:
                type = GWTJahiaNodePropertyType.DOUBLE;
                theValue = String.valueOf(val.getDouble());
                break;
            case PropertyType.LONG:
                type = GWTJahiaNodePropertyType.LONG;
                theValue = String.valueOf(val.getLong());
                break;
            case PropertyType.NAME:
                type = GWTJahiaNodePropertyType.NAME;
                theValue = val.getString();
                break;
            case PropertyType.PATH:
                type = GWTJahiaNodePropertyType.PATH;
                theValue = val.getString();
                break;
            case PropertyType.WEAKREFERENCE:
                GWTJahiaNodePropertyValue convertedValue = null;
                JCRNodeWrapper node = (JCRNodeWrapper) ((JCRValueWrapper) val).getNode();
                // check if the referenced node exists
                if (node != null) {
                    convertedValue = new GWTJahiaNodePropertyValue(navigation.getGWTJahiaNode(node), GWTJahiaNodePropertyType.WEAKREFERENCE);
                }
                return convertedValue;
            case PropertyType.REFERENCE:
                return new GWTJahiaNodePropertyValue(navigation.getGWTJahiaNode((JCRNodeWrapper) ((JCRValueWrapper) val).getNode()), GWTJahiaNodePropertyType.REFERENCE);
            case PropertyType.STRING:
                type = GWTJahiaNodePropertyType.STRING;
                theValue = val.getString();
                break;
            case PropertyType.UNDEFINED:
                type = GWTJahiaNodePropertyType.UNDEFINED;
                theValue = val.getString();
                break;
            default:
                type = GWTJahiaNodePropertyType.UNDEFINED;
                theValue = val.getString();
        }

        return new GWTJahiaNodePropertyValue(theValue, type);
    }

    public Value convertValue(GWTJahiaNodePropertyValue val) throws RepositoryException {
        Value value;
        switch (val.getType()) {
            case GWTJahiaNodePropertyType.BINARY:
                value = new BinaryValue(val.getBinary());
                break;
            case GWTJahiaNodePropertyType.BOOLEAN:
                value = new BooleanValue(val.getBoolean());
                break;
            case GWTJahiaNodePropertyType.DATE:
                Calendar cal = Calendar.getInstance();
                cal.setTime(val.getDate());
                value = new DateValue(cal);
                break;
            case GWTJahiaNodePropertyType.DOUBLE:
                value = new DoubleValue(val.getDouble());
                break;
            case GWTJahiaNodePropertyType.LONG:
                value = new LongValue(val.getLong());
                break;
            case GWTJahiaNodePropertyType.NAME:
                value = NameValue.valueOf(val.getString());
                break;
            case GWTJahiaNodePropertyType.PATH:
                value = PathValue.valueOf(val.getString());
                break;
            case GWTJahiaNodePropertyType.REFERENCE:
                value = ReferenceValue.valueOf(val.getString());
                break;
            case GWTJahiaNodePropertyType.WEAKREFERENCE:
                value = WeakReferenceValue.valueOf(val.getString());
                break;
            case GWTJahiaNodePropertyType.STRING:
                value = new StringValue(val.getString());
                break;
            case GWTJahiaNodePropertyType.UNDEFINED:
                value = new StringValue(val.getString());
                break;
            default:
                value = new StringValue(val.getString());
        }

        return value;
    }

    public List<GWTJahiaNodeType> getAvailableMixin(GWTJahiaNodeType type, Map<String, Object> ctx, Locale uiLocale) {
        ArrayList<GWTJahiaNodeType> res = new ArrayList<GWTJahiaNodeType>();
        Set<String> foundTypes = new HashSet<String>();
        try {
            ExtendedNodeType realType = NodeTypeRegistry.getInstance().getNodeType(type.getName());
            if (type.getSuperTypes().contains("jmix:list")) {
                ExtendedNodeType baseMixin = NodeTypeRegistry.getInstance().getNodeType("jmix:listMixin");
                NodeTypeIterator it = baseMixin.getSubtypes();
                while (it.hasNext()) {
                    ExtendedNodeType nodeType = (ExtendedNodeType) it.next();
                    if (nodeType.isMixin() && !foundTypes.contains(nodeType.getName())) {
                        res.add(getGWTJahiaNodeType(nodeType, realType, ctx, uiLocale));
                        foundTypes.add(nodeType.getName());
                    }
                }
            } else if (type.getName().equals("jnt:page")) {
                ExtendedNodeType baseMixin = NodeTypeRegistry.getInstance().getNodeType("jmix:pageMixin");
                NodeTypeIterator it = baseMixin.getSubtypes();
                while (it.hasNext()) {
                    ExtendedNodeType nodeType = (ExtendedNodeType) it.next();
                    if (nodeType.isMixin() && !foundTypes.contains(nodeType.getName())) {
                        res.add(getGWTJahiaNodeType(nodeType, realType, ctx, uiLocale));
                        foundTypes.add(nodeType.getName());
                    }
                }
//            } else if (type.getName().equals("jnt:nodeReference")) {
//                ExtendedNodeType baseMixin = NodeTypeRegistry.getInstance().getNodeType("jmix:renderableReference");
//                res.add(getGWTJahiaNodeType(baseMixin, realType, ctx, uiLocale));
//                foundTypes.add("jmix:renderable");
//                foundTypes.add("jmix:renderableReference");
            }


            addGWTJahiaNodeType(ctx, res, foundTypes, realType, "jmix:contentMixin", uiLocale);
        } catch (NoSuchNodeTypeException e) {

        }
        return res;
    }

    private void addGWTJahiaNodeType(Map<String, Object> ctx, ArrayList<GWTJahiaNodeType> res, Set<String> foundTypes, ExtendedNodeType realType, String nodeType, Locale uiLocale) throws NoSuchNodeTypeException {
        ExtendedNodeType baseMixin = NodeTypeRegistry.getInstance().getNodeType(nodeType);
        ExtendedNodeType[] mixins = baseMixin.getMixinSubtypes();
        for (ExtendedNodeType nt : mixins) {
            if (!foundTypes.contains(nt.getName())) {
                res.add(getGWTJahiaNodeType(nt, realType, ctx, uiLocale));
                foundTypes.add(nt.getName());
            }
        }
    }

}
