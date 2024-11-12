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
package org.jahia.ajax.gwt.helper;

import com.google.common.collect.Lists;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.*;
import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.utils.NodeTypeTreeEntry;
import org.jahia.utils.NodeTypesUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class for accessing node types and definitions.
 *
 * @author Thomas Draier
 *         Date: Sep 12, 2008 - 11:48:20 AM
 */
public class ContentDefinitionHelper {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ContentDefinitionHelper.class);

    private NavigationHelper navigation;
    private ChoiceListInitializerService choiceListInitializerService;

    public static final DateTimeFormat dateTimeFormat = new ServerDateTimeFormat("dd.MM.yyyy HH:mm");
    public static final DateTimeFormat dateFormat = new ServerDateTimeFormat("dd.MM.yyyy");

    static class ServerDateTimeFormat extends DateTimeFormat {
        ServerDateTimeFormat(String pattern) {
            super(pattern, new DefaultDateTimeFormatInfo());
        }
    }

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
        if (StringUtils.isEmpty(name)) {
            return null;
        }
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

    public GWTJahiaNodeType getGWTJahiaNodeType(ExtendedNodeType nodeType, Locale uiLocale) {
        GWTJahiaNodeType gwt = new GWTJahiaNodeType();
        gwt.setName(nodeType.getName());
        gwt.setMixin(nodeType.isMixin());
        gwt.setAbstract(nodeType.isAbstract());
        gwt.setDescription(nodeType.getDescription(uiLocale));
        String label = nodeType.getLabel(uiLocale);
        gwt.setLabel(label);

        List<ExtendedItemDefinition> defs = nodeType.getItems();

        List<GWTJahiaItemDefinition> items = new ArrayList<GWTJahiaItemDefinition>();
        List<GWTJahiaItemDefinition> inheritedItems = new ArrayList<GWTJahiaItemDefinition>();

        for (ExtendedItemDefinition def : defs) {
            ExtendedItemDefinition overrideDef = def;
            if (!excludedTypes.contains(def.getDeclaringNodeType().getName()) &&
                    !excludedItems.contains(def.getName())) {
                GWTJahiaItemDefinition item;
                if (def.isNode()) {
                    GWTJahiaNodeDefinition node = new GWTJahiaNodeDefinition();
                    overrideDef = nodeType.getChildNodeDefinitionsAsMap().get(def.getName());
                    if (overrideDef == null) {
                        overrideDef = def;
                    }
                    ExtendedNodeDefinition end = (ExtendedNodeDefinition) overrideDef;
                    item = node;
                    node.setRequiredPrimaryTypes(end.getRequiredPrimaryTypeNames());
                    node.setDefaultPrimaryType(end.getDefaultPrimaryTypeName());
                    node.setAllowsSameNameSiblings(end.allowsSameNameSiblings());
                    node.setWorkflow(end.getWorkflow());
                } else {
                    GWTJahiaPropertyDefinition prop = new GWTJahiaPropertyDefinition();
                    overrideDef = nodeType.getPropertyDefinitionsAsMap().get(def.getName());
                    if (overrideDef == null) {
                        overrideDef = def;
                    }
                    ExtendedPropertyDefinition epd = (ExtendedPropertyDefinition) overrideDef;
                    prop.setInternationalized(epd.isInternationalized());
                    prop.setRequiredType(epd.getRequiredType());
                    prop.setMultiple(epd.isMultiple());
                    String[] constr = epd.getValueConstraints();
                    if (constr != null && constr.length > 0) {
                        prop.setConstrained(true);
                        prop.setValueConstraints(Arrays.asList(constr));
                        switch (prop.getRequiredType()) {
                            case GWTJahiaNodePropertyType.LONG:
                            case GWTJahiaNodePropertyType.DOUBLE:
                            case GWTJahiaNodePropertyType.DECIMAL:
                            case GWTJahiaNodePropertyType.BINARY:
                                resolveNumericConstraint(prop);
                                break;

                            case GWTJahiaNodePropertyType.DATE:
                                resolveDateConstraint(prop);
                                break;

                            case GWTJahiaNodePropertyType.STRING:
                                if (prop.getValueConstraints().size() == 1 && GWTJahiaNodeSelectorType.CHOICELIST != def.getSelector()) {
                                    prop.setConstraintErrorMessage(epd.getMessage("constraint.error.message", uiLocale));
                                }
                                break;
                        }
                    }
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    for (Value value : epd.getDefaultValues()) {
                        try {
                            GWTJahiaNodePropertyValue convertedValue = convertValue(value, epd);
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
                item.setAutoCreated(overrideDef.isAutoCreated());
                item.setLabel(def.getLabel(uiLocale, nodeType));
                item.setMandatory(overrideDef.isMandatory());
                item.setHidden(overrideDef.isHidden());
                item.setName(overrideDef.getName());
                item.setProtected(overrideDef.isProtected());
                item.setDeclaringNodeType(def.getDeclaringNodeType().getName());
                item.setOverrideDeclaringNodeType(overrideDef.getDeclaringNodeType().getName());
                if ("jcr:description".equals(def.getName())) {
                    item.setDeclaringNodeTypeLabel(def.getLabel(uiLocale));
                } else {
                    item.setDeclaringNodeTypeLabel(def.getDeclaringNodeType().getLabel(uiLocale));
                }
                item.setSelector(overrideDef.getSelector());
                item.setSelectorOptions(new HashMap<String, String>(overrideDef.getSelectorOptions()));
                item.setDataType(overrideDef.getItemType());
                item.setTooltip(def.getTooltip(uiLocale, nodeType));
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
            gwt.setIcon(JCRContentUtils.getIconWithContext(nodeType));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return gwt;
    }

    public static void resolveNumericConstraint(GWTJahiaPropertyDefinition prop) {
        for (String valueConstraint : prop.getValueConstraints()) {
            final boolean lowerInclusive;
            Double lowerLimit = null;
            final boolean upperInclusive;
            Double upperLimit = null;
            Pattern pattern = Pattern
                    .compile("([\\(\\[]) *(\\-?\\d+\\.?\\d*)? *, *(\\-?\\d+\\.?\\d*)? *([\\)\\]])");
            Matcher matcher = pattern.matcher(valueConstraint);
            if (matcher.matches()) {
                try {
                    // group 1 is lower inclusive/exclusive
                    String s = matcher.group(1);
                    lowerInclusive = s.equals("[");
                    // group 2 is lower limit
                    s = matcher.group(2);
                    if (s == null || s.length() == 0) {
                        lowerLimit = null;
                    } else {
                        lowerLimit = Double.valueOf(matcher.group(2));
                    }
                    // group 3 is upper limit
                    s = matcher.group(3);
                    if (s == null || s.length() == 0) {
                        upperLimit = null;
                    } else {
                        upperLimit = Double.valueOf(matcher.group(3));
                    }
                    // group 4 is lower inclusive/exclusive
                    s = matcher.group(4);
                    upperInclusive = s.equals("]");
                    if (lowerLimit == null && upperLimit == null) {
                        String msg = "'"
                                + valueConstraint
                                + "' is not a valid value constraint"
                                + " format for numeric types: neither lower- nor upper-limit specified";
                        logger.debug(msg);
                        continue;
                    }
                    if (lowerLimit != null && upperLimit != null) {
                        if (lowerLimit > upperLimit) {
                            String msg = "'"
                                    + valueConstraint
                                    + "' is not a valid value constraint format for numeric types: lower-limit exceeds upper-limit";
                            logger.debug(msg);
                            continue;
                        }
                    }
                } catch (NumberFormatException nfe) {
                    String msg = "'" + valueConstraint
                            + "' is not a valid value constraint format for numeric types";
                    logger.debug(msg);
                    continue;
                }
                if (upperLimit != null) {
                    prop.setMaxValue(Double.toString(upperInclusive ? upperLimit : --upperLimit));
                }
                if (lowerLimit != null) {
                    prop.setMinValue(Double.toString(lowerInclusive ? lowerLimit : ++lowerLimit));
                }
            }
        }
    }

    public static void resolveDateConstraint(GWTJahiaPropertyDefinition prop) {
        for (String valueConstraint : prop.getValueConstraints()) {
            final boolean lowerInclusive;
            Calendar lowerLimit;
            final boolean upperInclusive;
            Calendar upperLimit;

            Pattern pattern = Pattern.compile("([\\(\\[]) *([0-9TZ\\.\\+-:]*)? *, *([0-9TZ\\.\\+-:]*)? *([\\)\\]])");
            Matcher matcher = pattern.matcher(valueConstraint);
            if (matcher.matches()) {
                try {
                    // group 1 is lower inclusive/exclusive
                    String s = matcher.group(1);
                    lowerInclusive = s.equals("[");
                    // group 2 is lower limit
                    s = matcher.group(2);
                    if (s == null || s.length() == 0) {
                        lowerLimit = null;
                    } else {
                        lowerLimit = DateValue.valueOf(matcher.group(2)).getDate();
                    }
                    // group 3 is upper limit
                    s = matcher.group(3);
                    if (s == null || s.length() == 0) {
                        upperLimit = null;
                    } else {
                        upperLimit = DateValue.valueOf(matcher.group(3)).getDate();
                    }
                    // group 4 is upper inclusive/exclusive
                    s = matcher.group(4);
                    upperInclusive = s.equals("]");

                    if (lowerLimit == null && upperLimit == null) {
                        String msg = "'" + valueConstraint
                                + "' is not a valid value constraint format for dates: neither min- nor max-date specified";
                        logger.debug(msg);
                        continue;
                    }
                    if (lowerLimit != null && upperLimit != null) {
                        if (lowerLimit.after(upperLimit)) {
                            String msg = "'" + valueConstraint
                                    + "' is not a valid value constraint format for dates: min-date > max-date";
                            logger.debug(msg);
                            continue;
                        }
                    }
                    if (upperLimit != null) {
                        prop.setMaxValue(String.valueOf(upperInclusive ? upperLimit.getTimeInMillis() : upperLimit.getTimeInMillis() - 1));
                    }
                    if (lowerLimit != null) {
                        prop.setMinValue(String.valueOf(lowerInclusive ? lowerLimit.getTimeInMillis() : lowerLimit.getTimeInMillis() + 1));
                    }
                } catch (ValueFormatException vfe) {
                    String msg = "'" + valueConstraint
                            + "' is not a valid value constraint format for dates";
                    logger.debug(msg);
                    continue;
                } catch (RepositoryException re) {
                    String msg = "'" + valueConstraint
                            + "' is not a valid value constraint format for dates";
                    logger.debug(msg);
                    continue;
                }
            }
        }
    }

    public List<GWTJahiaNodeType> getNodeTypes(List<String> names, Locale uiLocale) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            for (String name : names) {
                if (!StringUtils.isEmpty(name)) {
                    GWTJahiaNodeType nodeType = getNodeType(name, uiLocale);
                    if (nodeType != null) {
                        list.add(nodeType);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public List<GWTJahiaNodeType> getSubNodeTypes(List<String> names, Locale uiLocale) {
        try {
            List<GWTJahiaNodeType> list = new ArrayList<GWTJahiaNodeType>();
            for (String name : names) {
                ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(name);
                NodeTypeIterator nti = t.getSubtypes();
                while (nti.hasNext()) {
                    ExtendedNodeType type = (ExtendedNodeType) nti.next();
                    GWTJahiaNodeType nodeType = getNodeType(type.getName(), uiLocale);
                    list.add(nodeType);
                }
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
     * @param baseTypes            the node type name to find sub-types
     * @param ctx                  current processing context instance
     * @param uiLocale             Locale of the UI
     * @param includeSubTypes      if true, include sub types
     * @param displayStudioElement if true, add the jmix:studio types.
     * @return a list of node types with name and label populated that are the
     * sub-types of the specified base type or that are allowed to be
     * created in the specified parent node (if the baseType parameter
     * is null)
     */
    public Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getContentTypes(List<String> baseTypes, Map<String, Object> ctx,
                                                                         Locale uiLocale, boolean includeSubTypes, boolean displayStudioElement) {
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
                types = baseTypes;
            }


            Set<ExtendedNodeType> nodeTypes = new HashSet<ExtendedNodeType>();
            if (baseTypes != null) {
                for (String type : types) {
                    recurseAdd(registry.getNodeType(type), types, contentTypes, nodeTypes, includeSubTypes);
                }
            } else {
                recurseAdd(content, types, contentTypes, nodeTypes, includeSubTypes);
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
                        if (!displayStudioElement && !Arrays.asList(nodeType.getDeclaredSupertypeNames()).contains("jmix:studioOnly")) {
                            l.add(nt);
                        } else if (displayStudioElement) {
                            l.add(nt);
                        }
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
                    if (!displayStudioElement && !Arrays.asList(nodeType.getDeclaredSupertypeNames()).contains("jmix:studioOnly")) {
                        final GWTJahiaNodeType nt = getGWTJahiaNodeType(nodeType, uiLocale);
                        l.add(nt);
                    }
                }
                map.put(null, l);
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
     * @param baseTypes            the node type name to find sub-types
     * @param ctx                  current processing context instance
     * @param uiLocale
     * @param displayStudioElement
     * @return a list of node types with name and label populated that are the
     * sub-types of the specified base type or that are allowed to be
     * created in the specified parent node (if the baseType parameter
     * is null)
     */
    public List<GWTJahiaNodeType> getContentTypesAsList(List<String> baseTypes, Map<String, Object> ctx,
                                                              Locale uiLocale, boolean includeSubTypes, boolean displayStudioElement) {
        Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> typesAsMap = getContentTypes(baseTypes, ctx, uiLocale, includeSubTypes, displayStudioElement);
        List<GWTJahiaNodeType> contentTypes = typesAsMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        disambiguateLabels(contentTypes);
        return contentTypes;
    }


    private void recurseAdd(ExtendedNodeType req, List<String> baseTypes, Map<String, ExtendedNodeType> contentTypes,
                            Collection<ExtendedNodeType> result, boolean includeSubTypes) {
        boolean excludeNonDroppable = false;
        if (req.getName().equals("jmix:droppableContent") || contentTypes.keySet().contains(req.getName())) {
            excludeNonDroppable = true;
        }

        add(req, baseTypes, contentTypes, result, excludeNonDroppable);
        if (includeSubTypes) {
            NodeTypeIterator subtypes = req.getSubtypes();
            while (subtypes.hasNext()) {
                ExtendedNodeType subtype = (ExtendedNodeType) subtypes.next();
                add(subtype, baseTypes, contentTypes, result, excludeNonDroppable);
            }
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

    public List<ExtendedNodeType> getAvailableMixin(String type, JCRSiteNode site) throws NoSuchNodeTypeException {
        ArrayList<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        Set<String> foundTypes = new HashSet<String>();

        Set<String> installedModules = site != null && site.getPath().startsWith("/sites/") ? site.getInstalledModulesWithAllDependencies()
                : null;

        Map<ExtendedNodeType, Set<ExtendedNodeType>> m = NodeTypeRegistry.getInstance().getMixinExtensions();

        ExtendedNodeType realType = NodeTypeRegistry.getInstance().getNodeType(type);
        for (ExtendedNodeType nodeType : m.keySet()) {
            if (realType.isNodeType(nodeType.getName())) {
                for (ExtendedNodeType extension : m.get(nodeType)) {
//                        ctx.put("contextType", realType);
                    if (installedModules == null || extension.getTemplatePackage() == null ||
                            extension.getTemplatePackage().getModuleType().equalsIgnoreCase("system") ||
                            installedModules.contains(extension.getTemplatePackage().getId())) {
                        res.add(extension);
                        foundTypes.add(extension.getName());
                    }
                }
            }
        }

        return res;
    }

    public GWTJahiaNodePropertyValue convertValue(Value val, ExtendedPropertyDefinition def) throws RepositoryException {
        String theValue;
        int type;

        switch (def.getRequiredType()) {
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
                Calendar date = val.getDate();
                if (date == null) {
                    theValue = null;
                } else {
                    DateTimeFormat format;
                    if (def.getSelectorOptions().get("format") != null) {
                        format = new ServerDateTimeFormat(def.getSelectorOptions().get("format"));
                    } else {
                        format = def.getSelector() == SelectorType.DATEPICKER ? dateFormat : dateTimeFormat;
                    }
                    theValue = format.format(date.getTime());
                }
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
            case PropertyType.REFERENCE:
                GWTJahiaNodePropertyValue convertedValue = null;
                JCRNodeWrapper node = ((JCRValueWrapper) val).getNode();
                // check if the referenced node exists
                if (node != null) {
                    int refType = ( def.getRequiredType() == PropertyType.WEAKREFERENCE ) ? GWTJahiaNodePropertyType.WEAKREFERENCE :
                            GWTJahiaNodePropertyType.REFERENCE;
                    convertedValue = new GWTJahiaNodePropertyValue(navigation.getGWTJahiaNode(node),
                                                                   refType);
                }
                return convertedValue;
            case PropertyType.STRING:
                type = GWTJahiaNodePropertyType.STRING;
                theValue = val.getString();
                if (def.getSelector() == GWTJahiaNodeSelectorType.PICKER) {
                    JCRValueWrapper value = val instanceof JCRValueWrapper ? (JCRValueWrapper) val : new JCRValueWrapperImpl(val, def,
                            JCRSessionFactory.getInstance().getCurrentUserSession());
                    if (value.getNode() != null) {
                        return new GWTJahiaNodePropertyValue(theValue, navigation.getGWTJahiaNode((JCRNodeWrapper) value.getNode()), type);
                    } else {
                        return null;
                    }
                }
                break;
            case PropertyType.UNDEFINED:
                type = GWTJahiaNodePropertyType.UNDEFINED;
                theValue = val.getString();
                break;
            default:
                type = GWTJahiaNodePropertyType.UNDEFINED;
                theValue = val.getString();
                break;
        }

        return new GWTJahiaNodePropertyValue(theValue, type);
    }

    public Value convertValue(GWTJahiaNodePropertyValue val, ExtendedPropertyDefinition def) throws RepositoryException {
        Value value;
        switch (val.getType()) {
            case GWTJahiaNodePropertyType.BINARY:
                value = new BinaryValue(val.getBinary());
                break;
            case GWTJahiaNodePropertyType.BOOLEAN:
                value = new BooleanValue(val.getBoolean());
                break;
            case GWTJahiaNodePropertyType.DATE:
                DateTimeFormat format;
                if (def.getSelectorOptions().get("format") != null) {
                    format = new ServerDateTimeFormat(def.getSelectorOptions().get("format"));
                } else {
                    format = def.getSelector() == SelectorType.DATEPICKER ? dateFormat : dateTimeFormat;
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(val.getDate(format));
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
                break;
        }

        return value;
    }

    public Map<String, GWTChoiceListInitializer> getAllChoiceListInitializersValues(List<ExtendedNodeType> items,
                                                                                    ExtendedNodeType contextType, JCRNodeWrapper contextNode,
                                                                                    JCRNodeWrapper contextParent,
                                                                                    Locale uiLocale) throws RepositoryException {
        Map<String, GWTChoiceListInitializer> results = new HashMap<String, GWTChoiceListInitializer>();

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("contextType", contextType);
        context.put("contextNode", contextNode);
        context.put("contextParent", contextParent);


        for (Map.Entry<String, ExtendedPropertyDefinition> item : getChoiceListItems(items).entrySet()) {
            GWTChoiceListInitializer initializer = getChoiceListInitializerValues(item.getValue(), context, uiLocale);
            if (initializer != null) {
                results.put(item.getKey(), initializer);
            }
        }
        return results;
    }

    public GWTChoiceListInitializer getInitializerValues(ExtendedPropertyDefinition epd,
                                                         ExtendedNodeType contextType, JCRNodeWrapper contextNode, JCRNodeWrapper contextParent,
                                                         Map<String, List<GWTJahiaNodePropertyValue>> dependentValues, Locale uiLocale) throws RepositoryException {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("contextType", contextType);
        context.put("contextNode", contextNode);
        context.put("contextParent", contextParent);
        for (Map.Entry<String, List<GWTJahiaNodePropertyValue>> entry : dependentValues.entrySet()) {
            context.put(entry.getKey(), CollectionUtils.collect(entry.getValue(), new Transformer() {
                public Object transform(Object input) {
                    return input.toString();
                }
            }));
        }

        return getChoiceListInitializerValues(epd, context, uiLocale);
    }

    private GWTChoiceListInitializer getChoiceListInitializerValues(ExtendedPropertyDefinition epd, Map<String, Object> context, Locale uiLocale) {
        GWTChoiceListInitializer initializer = null;
        if (!epd.isHidden()) {
            Map<String, String> map = epd.getSelectorOptions();
            if (map.size() > 0) {
                final List<GWTJahiaValueDisplayBean> displayBeans = new ArrayList<GWTJahiaValueDisplayBean>(32);
                final Map<String, ChoiceListInitializer> initializers = choiceListInitializerService.getInitializers();
                List<String> dependentProperties = null;
                if (map.containsKey("dependentProperties")) {
                    dependentProperties = Lists.newArrayList(StringUtils.split(map.get("dependentProperties"), ','));
                    context.put("dependentProperties", dependentProperties);
                }
                List<ChoiceListValue> listValues = null;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (initializers.containsKey(entry.getKey())) {
                        listValues = initializers.get(entry.getKey()).getChoiceListValues(epd, entry.getValue(),
                                listValues, uiLocale, context);
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
                initializer = new GWTChoiceListInitializer(displayBeans, dependentProperties);
            }
        }
        return initializer;
    }

    private Map<String, ExtendedPropertyDefinition> getChoiceListItems(List<ExtendedNodeType> allTypes) {
        Map<String, ExtendedPropertyDefinition> items = new HashMap<String, ExtendedPropertyDefinition>();
        for (ExtendedNodeType nodeType : allTypes) {
            Collection<ExtendedPropertyDefinition> c = nodeType.getPropertyDefinitionsAsMap().values();
            for (ExtendedPropertyDefinition definition : c) {
                if (definition.getSelector() == SelectorType.CHOICELIST && !definition.getSelectorOptions().isEmpty()) {
                    items.put(definition.getDeclaringNodeType().getName() + "."
                            + definition.getName(), definition);
                }
            }
        }
        return items;
    }

    /**
     * Get both dynamic and static default values for the specified types and locales
     *
     * @param items   a list of nodeTypes
     * @param locales a list of locales
     * @return the default values per type, per locale
     * @throws RepositoryException in case of JCR-related errors
     */
    public Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getAllDefaultValues(List<ExtendedNodeType> items, List<Locale> locales) throws RepositoryException {
        Set<Map.Entry<String, ExtendedPropertyDefinition>> entries = getInitializedItems(items).entrySet();
        Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> results = new HashMap<String, Map<String, List<GWTJahiaNodePropertyValue>>>();
        for (Locale locale : locales) {
            Map<String, List<GWTJahiaNodePropertyValue>> defaultValues = new HashMap<String, List<GWTJahiaNodePropertyValue>>();
            for (Map.Entry<String, ExtendedPropertyDefinition> entry : entries) {
                List<GWTJahiaNodePropertyValue> propertyDefaultValues = new ArrayList<GWTJahiaNodePropertyValue>();
                for (Value value : entry.getValue().getDefaultValues(locale)) {
                    propertyDefaultValues.add(convertValue(value, entry.getValue()));
                }
                defaultValues.put(entry.getKey(), propertyDefaultValues);
            }
            results.put(locale.toString(), defaultValues);
        }
        return results;
    }

    private Map<String, ExtendedPropertyDefinition> getInitializedItems(List<ExtendedNodeType> allTypes) {
        Map<String, ExtendedPropertyDefinition> items = new HashMap<String, ExtendedPropertyDefinition>();
        for (ExtendedNodeType nodeType : allTypes) {
            for (ExtendedPropertyDefinition definition : nodeType.getPropertyDefinitionsAsMap().values()) {
                if (definition.getDefaultValues().length > 0) {
                    items.put(definition.getDeclaringNodeType().getName() + "." + definition.getName(), definition);
                }
            }
        }
        return items;
    }

    /**
     * Returns a tree of node types and sub-types with name and label populated that are the
     * sub-types of the specified node types.
     *
     * @param nodeTypes
     * @param excludedNodeTypes
     * @param includeSubTypes
     * @param site
     * @param uiLocale
     * @param session
     * @return a tree of node types
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNodeType> getContentTypesAsTree(final List<String> nodeTypes, final List<String> excludedNodeTypes, final boolean includeSubTypes, final JCRSiteNode site,
                                                        final Locale uiLocale, final JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            Set<NodeTypeTreeEntry> nodetypeEntries = NodeTypesUtils.getContentTypesAsTree(nodeTypes, excludedNodeTypes, includeSubTypes, site.getPath(), session, uiLocale);
            return nodetypeEntries.stream().map(nodeTypeTreeEntry -> transformNodeTypeToGWT(nodeTypeTreeEntry, uiLocale, true)).collect(Collectors.toList());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.translate", uiLocale, e.getLocalizedMessage()));
        }
    }

    private GWTJahiaNodeType transformNodeTypeToGWT(NodeTypeTreeEntry nodeTypeTreeEntry, final Locale uiLocale, boolean recurseOnChildren){
        GWTJahiaNodeType rootType = getGWTJahiaNodeType(nodeTypeTreeEntry.getNodeType(), uiLocale);
        rootType.setLabel(nodeTypeTreeEntry.getLabel());
        if (recurseOnChildren && nodeTypeTreeEntry.getChildren() != null) {
            rootType.setChildren(nodeTypeTreeEntry.getChildren().stream().map(child -> transformNodeTypeToGWT(child, uiLocale, false)).collect(Collectors.toList()));
        }
        return rootType;
    }

    /*
     * Appends its name to a {@link GWTJahiaNodeType}'s label for disambiguation if any sibling has the same one.
     */
    private static Collection<GWTJahiaNodeType> disambiguateLabels(Collection<GWTJahiaNodeType> nodeTypes) {
        List<GWTJahiaNodeType> ambiguousNodeTypes = nodeTypes
                .stream()
                .collect(Collectors.groupingBy(GWTJahiaNodeType::getLabel))
                .values()
                .stream()
                .filter(l -> l.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (GWTJahiaNodeType nodeType : ambiguousNodeTypes) {
            nodeType.setLabel(nodeType.getLabel() + " (" + nodeType.getName() + ")");
        }
        return nodeTypes;
    }

}
