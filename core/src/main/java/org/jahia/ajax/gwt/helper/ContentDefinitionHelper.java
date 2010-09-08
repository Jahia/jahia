/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.jackrabbit.value.StringValue;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.ExternalReferenceValue;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.*;
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

    private static final List<String> excludedItems =
            Arrays.asList("j:locktoken", "jcr:lockOwner", "jcr:lockIsDeep", "j:nodename", "j:fullpath", "j:applyAcl",
                    "jcr:uuid", "j:fieldsinuse");

    private static final List<String> excludedTypes = Arrays.asList("nt:base", "mix:versionable", "jnt:workflow");

    public GWTJahiaNodeType getNodeType(String name, Locale uiLocale) {
        ExtendedNodeType nodeType = null;
        try {
            nodeType = NodeTypeRegistry.getInstance().getNodeType(name);
        } catch (NoSuchNodeTypeException e) {
            return null;
        }
        GWTJahiaNodeType gwt = getGWTJahiaNodeType(nodeType, uiLocale);
        return gwt;
    }

    public List<GWTJahiaNodeType> getGWTNodeTypes(List<ExtendedNodeType> availableMixins, Locale uiLocale) {
        List<GWTJahiaNodeType> gwtMixin = new ArrayList<GWTJahiaNodeType>();
        for (ExtendedNodeType extendedNodeType : availableMixins) {
            gwtMixin.add(getGWTJahiaNodeType(extendedNodeType, uiLocale));
        }
        return gwtMixin;
    }


    public List<GWTJahiaNode> getPageTemplates(JCRSessionWrapper currentUserSession, JCRSiteNode site)
            throws GWTJahiaServiceException {
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

    public GWTJahiaNodeType getGWTJahiaNodeType(ExtendedNodeType nodeType, Locale uiLocale) {
        GWTJahiaNodeType gwt = new GWTJahiaNodeType();
        gwt.setName(nodeType.getName());
        gwt.setMixin(nodeType.isMixin());
        gwt.setDescription(nodeType.getDescription(uiLocale));
        String label = nodeType.getLabel(uiLocale);
        gwt.setLabel(label);

        List<ExtendedItemDefinition> defs;

        defs = nodeType.getItems();

        List<GWTJahiaItemDefinition> items = new ArrayList<GWTJahiaItemDefinition>();
        List<GWTJahiaItemDefinition> inheritedItems = new ArrayList<GWTJahiaItemDefinition>();

        for (ExtendedItemDefinition def : defs) {
            if (!excludedTypes.contains(def.getDeclaringNodeType().getName()) &&
                    !excludedItems.contains(def.getName())) {
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
                    String[] constr = epd.getValueConstraints();
                    boolean constrained = constr != null && constr.length > 0;
                    prop.setConstrained(constrained);
                    if (constrained) {
                        prop.setValueConstraints(Arrays.asList(constr));
                    }
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
                item.setDataType(def.getItemType());
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
        try {
            gwt.setIcon(navigation.getIcon(nodeType));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return gwt;
    }

    public List<GWTJahiaNodeType> getNodeTypes(List<String> names, Locale uiLocale) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            for (String name : names) {
                list.add(getNodeType(name, uiLocale));
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
     * @param baseTypes the node type name to find sub-types
     * @param ctx       current processing context instance
     * @param uiLocale
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type or that are allowed to be
     *         created in the specified parent node (if the baseType parameter
     *         is null)
     */
    public Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getSubNodetypes(String baseTypes, Map<String, Object> ctx,
                                                                         Locale uiLocale) {
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


            Set<ExtendedNodeType> nodeTypes = new HashSet<ExtendedNodeType>();
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
                        final GWTJahiaNodeType nt = getGWTJahiaNodeType(nodeType, uiLocale);
                        l.add(nt);
                        nodeTypes.remove(nodeType);
                    }
                }
                if (!l.isEmpty()) {
                    map.put(getGWTJahiaNodeType(mainType, uiLocale), l);
                }
            }
            if (!nodeTypes.isEmpty()) {
                List<GWTJahiaNodeType> l = new ArrayList<GWTJahiaNodeType>();
                for (ExtendedNodeType nodeType : nodeTypes) {
                    final GWTJahiaNodeType nt = getGWTJahiaNodeType(nodeType, uiLocale);
                    l.add(nt);
                }
                map.put(null, l);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return map;
    }

    private void recurseAdd(ExtendedNodeType req, List<String> baseTypes, Map<String, ExtendedNodeType> contentTypes,
                            Collection<ExtendedNodeType> result) {
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

    private void add(ExtendedNodeType type, List<String> baseTypes, Map<String, ExtendedNodeType> contentTypes,
                     Collection<ExtendedNodeType> result, boolean excludeNonDroppable) {
        if (!excludedTypes.contains(type.getName()) && !type.isMixin() && !type.isAbstract() && (!excludeNonDroppable ||
                CollectionUtils.containsAny(Arrays.asList(type.getDeclaredSupertypeNames()), contentTypes.keySet()))) {
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

    public List<ExtendedNodeType> getAvailableMixin(String type) throws NoSuchNodeTypeException {
        ArrayList<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        Set<String> foundTypes = new HashSet<String>();

        Map<ExtendedNodeType, Set<ExtendedNodeType>> m = NodeTypeRegistry.getInstance().getMixinExtensions();

        ExtendedNodeType realType = NodeTypeRegistry.getInstance().getNodeType(type);
        for (ExtendedNodeType nodeType : m.keySet()) {
            if (realType.isNodeType(nodeType.getName())) {
                for (ExtendedNodeType extension : m.get(nodeType)) {
//                        ctx.put("contextType", realType);
                    res.add(extension);
                    foundTypes.add(extension.getName());
                }
            }
        }

        return res;
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
                    convertedValue = new GWTJahiaNodePropertyValue(navigation.getGWTJahiaNode(node),
                            GWTJahiaNodePropertyType.WEAKREFERENCE);
                }
                return convertedValue;
            case PropertyType.REFERENCE:
                return new GWTJahiaNodePropertyValue(
                        navigation.getGWTJahiaNode((JCRNodeWrapper) ((JCRValueWrapper) val).getNode()),
                        GWTJahiaNodePropertyType.REFERENCE);
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
                try {
                    value = ReferenceValue.valueOf(val.getString());
                } catch (ValueFormatException vfe) {
                    // this can happen in the case of a reference to an external repository which doesn't use UUIDs as identifiers
                    value = new ExternalReferenceValue(val.getString(), PropertyType.REFERENCE);
                }
                break;
            case GWTJahiaNodePropertyType.WEAKREFERENCE:
                try {
                    value = WeakReferenceValue.valueOf(val.getString());
                } catch (ValueFormatException vfe) {
                    // this can happen in the case of a reference to an external repository which doesn't use UUIDs as identifiers
                    value = new ExternalReferenceValue(val.getString(), PropertyType.WEAKREFERENCE);
                }
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

    public Map<String, List<GWTJahiaValueDisplayBean>> getInitializersValues(List<ExtendedNodeType> items,
                                                                             ExtendedNodeType contextType, JCRNodeWrapper contextNode,
                                                                             JCRNodeWrapper contextParent,
                                                                             Locale uiLocale) throws RepositoryException {
        Map<String, List<GWTJahiaValueDisplayBean>> results = new HashMap<String, List<GWTJahiaValueDisplayBean>>();

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("contextType", contextType);
        context.put("contextNode", contextNode);
        context.put("contextParent", contextParent);


        for (ExtendedPropertyDefinition epd : getChoiceListItems(items)) {
            Map<String, String> map = epd.getSelectorOptions();
            if (map.size() > 0) {
                final ArrayList<GWTJahiaValueDisplayBean> displayBeans = new ArrayList<GWTJahiaValueDisplayBean>(32);
                final Map<String, ChoiceListInitializer> initializers = choiceListInitializerService.getInitializers();
                List<ChoiceListValue> listValues = null;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (initializers.containsKey(entry.getKey())) {
                        listValues = initializers.get(entry.getKey())
                                .getChoiceListValues(epd, entry.getValue(), listValues, uiLocale, context);
                    }
                }
                if (listValues != null) {
                    for (ChoiceListValue choiceListValue : listValues) {
                        try {
                            final GWTJahiaValueDisplayBean displayBean =
                                    new GWTJahiaValueDisplayBean(choiceListValue.getValue().getString(),
                                            choiceListValue.getDisplayName());
                            final Map<String, Object> props = choiceListValue.getProperties();
                            if (props != null) {
                                for (Map.Entry<String, Object> objectEntry : props.entrySet()) {
                                    if (objectEntry.getKey() == null || objectEntry.getValue() == null) {
                                        logger.error("Null value : " + objectEntry.getKey() + " / " +
                                                objectEntry.getValue());
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
                results.put(epd.getDeclaringNodeType().getName() + "." + epd.getName(), displayBeans);
            }
        }
        return results;
    }

    private List<ExtendedPropertyDefinition> getChoiceListItems(List<ExtendedNodeType> allTypes) {
        List<ExtendedPropertyDefinition> items = new ArrayList<ExtendedPropertyDefinition>();
        for (ExtendedNodeType nodeType : allTypes) {
            Collection<ExtendedPropertyDefinition> c = nodeType.getPropertyDefinitionsAsMap().values();
            for (ExtendedPropertyDefinition definition : c) {
                if (definition.getSelector() == SelectorType.CHOICELIST && !definition.getSelectorOptions().isEmpty()) {
                    items.add(definition);
                }
            }
        }
        return items;
    }


}
