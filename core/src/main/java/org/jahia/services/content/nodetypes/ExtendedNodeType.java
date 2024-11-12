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
package org.jahia.services.content.nodetypes;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.commons.nodetype.constraint.ValueConstraint;
import org.apache.jackrabbit.spi.commons.value.QValueValue;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jahia extended JCR node type information.
 *
 * @author Thomas Draier
 * Date: 4 janv. 2008
 * Time: 14:02:22
 */
public class ExtendedNodeType implements NodeType {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExtendedNodeType.class);

    private final NodeTypeRegistry registry;
    private final String systemId;
    private final List<ExtendedItemDefinition> items = new ArrayList<>();
    private final Map<String, ExtendedNodeDefinition> nodes = new ConcurrentHashMap<>();
    private final Map<String, ExtendedPropertyDefinition> properties = new ConcurrentHashMap<>();
    private final Map<String, ExtendedNodeDefinition> unstructuredNodes = new ConcurrentHashMap<>();
    private final Map<Integer, ExtendedPropertyDefinition> unstructuredProperties = new ConcurrentHashMap<>();
    private final List<ExtendedNodeType> declaredSubtypes = new ArrayList<>();
    private List<String> mixinExtendNames = new ArrayList<>();
    private List<ExtendedNodeType> mixinExtend = new ArrayList<>();
    private final Map<Locale, String> labels = new ConcurrentHashMap<>(1);
    private final Map<Locale, String> descriptions = new ConcurrentHashMap<>(1);
    private final boolean systemType;
    private List<String> groupedItems;
    private Map<String, ExtendedNodeDefinition> allNodes;
    private volatile Map<String, ExtendedPropertyDefinition> allProperties;
    private Map<String, ExtendedNodeDefinition> allUnstructuredNodes;
    private Map<Integer, ExtendedPropertyDefinition> allUnstructuredProperties;
    private Name name;
    private String alias;
    private boolean isAbstract;
    private boolean isMixin;
    private boolean hasOrderableChildNodes;
    private String primaryItemName;
    private String[] declaredSupertypeNames = new String[0];
    private ExtendedNodeType[] declaredSupertypes = new ExtendedNodeType[0];
    private boolean queryable = true;
    private String itemsType;
    private JahiaTemplatesPackage templatesPackage;

    public ExtendedNodeType(NodeTypeRegistry registry, String systemId) {
        this.registry = registry;
        this.systemId = systemId;
        this.systemType = systemId.startsWith("system-");
    }

    /**
     * Tests if the value constraints defined in the property definition
     * <code>pd</code> are satisfied by the the specified <code>values</code>.
     * <p/>
     * Note that the <i>protected</i> flag is not checked. Also note that no
     * type conversions are attempted if the type of the given values does not
     * match the required type as specified in the given definition.
     */
    private static void checkSetPropertyValueConstraints(ExtendedPropertyDefinition pd, InternalValue[] values) throws RepositoryException {
        // check multi-value flag
        if (!pd.isMultiple() && values != null && values.length > 1) {
            throw new ConstraintViolationException("the property is not multi-valued");
        }

        ValueConstraint[] constraints = pd.getValueConstraintObjects();
        if (constraints == null || constraints.length == 0) {
            // no constraints to check
            return;
        }
        if (values != null && values.length > 0) {
            // check value constraints on every value
            for (InternalValue value : values) {
                // constraints are OR-ed together
                boolean satisfied = false;
                ConstraintViolationException cve = null;
                for (ValueConstraint constraint : constraints) {
                    try {
                        constraint.check(value);
                        satisfied = true;
                        break;
                    } catch (ConstraintViolationException e) {
                        cve = e;
                    }
                }
                if (!satisfied && cve != null) {
                    // re-throw last exception we encountered
                    throw cve;
                }
            }
        }
    }

    public String getSystemId() {
        return systemId;
    }

    public String getName() {
        return name.toString();
    }

    public void setName(Name name) {
        this.name = name;
        this.alias = name != null ? name.toString() : null;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Name getNameObject() {
        return name;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public boolean isMixin() {
        return isMixin;
    }

    public void setMixin(boolean mixin) {
        isMixin = mixin;
    }

    public boolean hasOrderableChildNodes() {
        if (!hasOrderableChildNodes) {
            return hasOrderableChildNodes(true);
        }
        return hasOrderableChildNodes;
    }

    private boolean hasOrderableChildNodes(boolean checkSupertypes) {
        if (checkSupertypes) {
            final ExtendedNodeType[] supertypes = getSupertypes();
            for (ExtendedNodeType supertype : supertypes) {
                if (supertype.hasOrderableChildNodes(false)) {
                    return true;
                }
            }
            return false;
        }
        return hasOrderableChildNodes;
    }

    public void setHasOrderableChildNodes(boolean hasOrderableChildNodes) {
        this.hasOrderableChildNodes = hasOrderableChildNodes;
    }

    public boolean isQueryable() {
        return queryable;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    public String getPrimaryItemName() {
        return primaryItemName;
    }

    public void setPrimaryItemName(String primaryItemName) {
        this.primaryItemName = primaryItemName;
    }

    public ExtendedNodeType[] getSupertypes() {
        Set<ExtendedNodeType> l = getSupertypeSet();
        return l.toArray(new ExtendedNodeType[0]);
    }

    public Set<ExtendedNodeType> getSupertypeSet() {
        Set<ExtendedNodeType> l = new LinkedHashSet<>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (ExtendedNodeType s : d) {
            if (s != null && !s.getNameObject().equals(getNameObject())) {
                l.add(s);
                l.addAll(s.getSupertypeSet());
                if (!s.isMixin()) {
                    primaryFound = true;
                }
            }
            if (s != null && s.getNameObject().equals(getNameObject())) {
                logger.error("Loop detected in definition {}", getName());
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(getName()) && !isMixin) {
            try {
                l.add(registry.getNodeType(Constants.NT_BASE));
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for " + getName(), e);
            }
        }
        return l;
    }

    public ExtendedNodeType[] getPrimarySupertypes() {
        List<ExtendedNodeType> l = new ArrayList<>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (ExtendedNodeType s : d) {
            if (s != null && !s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getPrimarySupertypes()));
                primaryFound = true;
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(name.toString()) && !isMixin) {
            try {
                l.add(registry.getNodeType(Constants.NT_BASE));
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for " + getName(), e);
            }
        }
        return l.toArray(new ExtendedNodeType[0]);
    }

    public ExtendedNodeType[] getDeclaredSupertypes() {
        return declaredSupertypes;
    }

    public void setDeclaredSupertypes(String[] declaredSupertypes) {
        this.declaredSupertypeNames = declaredSupertypes;
    }

    /**
     * Used only for setting supertypes during validation.
     * Otherwise, declaredSupertypes are set through {@link ExtendedNodeType#validate()}
     * @see org.jahia.services.modulemanager.impl.DefinitionsBundleChecker
     */
    public void setDeclaredSupertypes(ExtendedNodeType[] declaredSupertypes) {
        this.declaredSupertypes = declaredSupertypes;
    }

    public void validate() throws NoSuchNodeTypeException {
        this.declaredSupertypes = new ExtendedNodeType[declaredSupertypeNames.length];
        int mixIndex = 0;

        for (int i = 0; i < declaredSupertypeNames.length; i++) {
            final ExtendedNodeType nodeType = registry.getNodeType(declaredSupertypeNames[i]);
            if (!nodeType.isMixin && i > 0) {
                System.arraycopy(this.declaredSupertypes, mixIndex, this.declaredSupertypes, mixIndex + 1, i - mixIndex);
                this.declaredSupertypes[mixIndex] = nodeType;
                mixIndex++;
            } else {
                this.declaredSupertypes[i] = nodeType;
            }
            nodeType.addSubType(this);
        }

        // only add mixin extensions if current nodetype is a mixin
        mixinExtend = this.isMixin ? registry.addMixinExtensions(this, mixinExtendNames) : Collections.<ExtendedNodeType>emptyList();

        for (ExtendedItemDefinition itemDefinition : items) {
            if (itemDefinition.getItemType() != null) {
                registry.addTypedItem(itemDefinition);
            }
        }
    }

    public void checkConflicts() throws InvalidNodeTypeDefinitionException {
        Map<String, ExtendedItemDefinition> definitionMap = new HashMap<>();
        for (ExtendedNodeType type : getDeclaredSupertypes()) {
            checkConflict(definitionMap, type);
        }
    }

    private void checkConflict(Map<String, ExtendedItemDefinition> definitionMap, ExtendedNodeType nodeType) throws InvalidNodeTypeDefinitionException {
        for (ExtendedItemDefinition def : nodeType.getItems()) {
            ExtendedItemDefinition existingDef = definitionMap.get(def.getName());
            if (existingDef != null && !existingDef.equals(def) && !def.isUnstructured()) {
                if (def.isAutoCreated() || existingDef.isAutoCreated()) {
                    // conflict
                    String msg = MessageFormat.format("The item definition for ''{0}'' in node type ''{1}'' conflicts with node type ''{2}'': name collision with auto-create definition", def.getName(), def.getDeclaringNodeType(), existingDef.getDeclaringNodeType());
                    throw new InvalidNodeTypeDefinitionException(msg);
                }
                // check ambiguous definitions
                if (def.isNode() == existingDef.isNode()) {
                    if (!def.isNode()) {
                        // property definition
                        ExtendedPropertyDefinition pd = (ExtendedPropertyDefinition) def;
                        ExtendedPropertyDefinition epd = (ExtendedPropertyDefinition) existingDef;
                        // compare type & multiValued flag
                        if (pd.getRequiredType() == epd.getRequiredType()
                                && pd.isMultiple() == epd.isMultiple()) {
                            // conflict
                            String msg = MessageFormat.format("The property definition for ''{0}'' in node type ''{1}'' conflicts with node type ''{2}'': ambiguous property definition", def.getName(), def.getDeclaringNodeType(), existingDef.getDeclaringNodeType());
                            throw new InvalidNodeTypeDefinitionException(msg);
                        }
                    } else {
                        // child node definition
                        String msg = MessageFormat.format("The child node definition for ''{0}'' in node type ''{1}'' conflicts with node type ''{2}'': ambiguous child node definition", def.getName(), def.getDeclaringNodeType(), existingDef.getDeclaringNodeType());
                        throw new InvalidNodeTypeDefinitionException(msg);
                    }
                }
            }
            definitionMap.put(def.getName(), def);
        }
    }

    void removeSubType(ExtendedNodeType subType) {
        declaredSubtypes.remove(subType);
        //todo ????!!
        declaredSubtypes.add(subType);
    }

    void addSubType(ExtendedNodeType subType) {
        declaredSubtypes.remove(subType);
        declaredSubtypes.add(subType);
    }

    public NodeTypeIterator getDeclaredSubtypes() {
        return new NodeTypeIteratorImpl(declaredSubtypes.iterator(), declaredSubtypes.size());
    }

    public String[] getDeclaredSupertypeNames() {
        return declaredSupertypeNames;
    }

    public NodeTypeIterator getSubtypes() {
        List<ExtendedNodeType> l = getSubtypesAsList();
        return new NodeTypeIteratorImpl(l.iterator(), l.size());
    }

    public List<ExtendedNodeType> getSubtypesAsList() {
        List<ExtendedNodeType> l = new ArrayList<>();
        for (ExtendedNodeType s : declaredSubtypes) {
            l.add(s);
            NodeTypeIterator subtypes = s.getSubtypes();
            while (subtypes.hasNext()) {
                l.add((ExtendedNodeType) subtypes.next());
            }
        }
        return l;
    }

    public ExtendedNodeType[] getMixinSubtypes() {
        List<ExtendedNodeType> l = new ArrayList<>();
        for (ExtendedNodeType s : declaredSubtypes) {
            if (s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getMixinSubtypes()));
            }
        }
        return l.toArray(new ExtendedNodeType[0]);
    }

    public boolean isNodeType(String typeName) {
        if (getName().equals(typeName) || Constants.NT_BASE.equals(typeName)) {
            return true;
        }
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (ExtendedNodeType s : d) {
            if (s != null && s.isNodeType(typeName)) {
                return true;
            }
        }
        return false;
    }

    public List<ExtendedItemDefinition> getItems() {
        List<ExtendedItemDefinition> l = new ArrayList<>(getDeclaredItems());

        ExtendedNodeType[] supertypes = getSupertypes();
        for (ExtendedNodeType supertype : supertypes) {
            l.addAll(supertype.getDeclaredItems());
        }

        return l;
    }

    public List<ExtendedItemDefinition> getDeclaredItems() {
        return getDeclaredItems(false);
    }

    /**
     * Get declared items
     *
     * @param includeOverride if false, do not return items that are already declared in parent types
     * @return list of declared items
     */
    public List<ExtendedItemDefinition>  getDeclaredItems(boolean includeOverride) {
        getPropertyDefinitionsAsMap();
        getChildNodeDefinitionsAsMap();
        List<ExtendedItemDefinition> res;
        if (includeOverride) {
            res = items;
        } else {
            res = new ArrayList<>();
            for (ExtendedItemDefinition item : items) {
                if (!item.isOverride()) {
                    res.add(item);
                }
            }
        }
        return Collections.unmodifiableList(res);
    }

    public Map<String, ExtendedPropertyDefinition> getPropertyDefinitionsAsMap() {
        if (allProperties == null) {
            synchronized (this) {
                if (allProperties == null) {
                    LinkedHashMap<String, ExtendedPropertyDefinition> props = new LinkedHashMap<>(properties);

                    ExtendedNodeType[] supertypes = getSupertypes();
                    for (int i = supertypes.length - 1; i >= 0; i--) {
                        ExtendedNodeType superType = supertypes[i];
                        Map<String, ExtendedPropertyDefinition> superTypeProps = new HashMap<>(superType.getDeclaredPropertyDefinitionsAsMap());

                        Map<String, ExtendedPropertyDefinition> overrideProps = new HashMap<>(properties);
                        overrideProps.keySet().retainAll(superTypeProps.keySet());

                        for (Map.Entry<String, ExtendedPropertyDefinition> overridePropEntry : overrideProps.entrySet()) {
                            overridePropEntry.getValue().setOverride(true);

                            if (superTypeProps.get(overridePropEntry.getKey()).isMandatory()) {
                                // if super is mandatory, override should be mandatory
                                overridePropEntry.getValue().setMandatory(true);
                            }
                        }
                        superTypeProps.keySet().removeAll(overrideProps.keySet());

                        props.putAll(superTypeProps);
                    }

                    allProperties = Collections.unmodifiableMap(props);
                }
            }
        }

        return allProperties;
    }

    public Map<Integer, ExtendedPropertyDefinition> getUnstructuredPropertyDefinitions() {
        if (allUnstructuredProperties == null) {
            allUnstructuredProperties = new LinkedHashMap<>();

            allUnstructuredProperties.putAll(unstructuredProperties);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length - 1; i >= 0; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<Integer, ExtendedPropertyDefinition> c = new HashMap<>(nodeType.getDeclaredUnstructuredPropertyDefinitions());
                Map<Integer, ExtendedPropertyDefinition> over = new HashMap<>(unstructuredProperties);
                over.keySet().retainAll(c.keySet());
                for (ExtendedPropertyDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allUnstructuredProperties.putAll(c);
            }
        }
        return Collections.unmodifiableMap(allUnstructuredProperties);
    }

    public ExtendedPropertyDefinition[] getPropertyDefinitions() {
        List<ExtendedPropertyDefinition> list = new ArrayList<>();
        Set<String> keys = new HashSet<>();

        final List<ExtendedItemDefinition> i = getItems();
        Collections.reverse(i);
        for (ExtendedItemDefinition item : i) {
            if (!item.isNode() && (item.isUnstructured() || !keys.contains(item.getName()))) {
                list.add((ExtendedPropertyDefinition) item);
                keys.add(item.getName());
            }
        }
        Collections.reverse(list);
        return list.toArray(new ExtendedPropertyDefinition[0]);
    }

    public Map<String, ExtendedPropertyDefinition> getDeclaredPropertyDefinitionsAsMap() {
        getPropertyDefinitionsAsMap();
        return properties;
    }

    public Map<Integer, ExtendedPropertyDefinition> getDeclaredUnstructuredPropertyDefinitions() {
        getUnstructuredPropertyDefinitions();
        return unstructuredProperties;
    }

    public ExtendedPropertyDefinition[] getDeclaredPropertyDefinitions() {
        List<ExtendedPropertyDefinition> list = new ArrayList<>();

        final List<ExtendedItemDefinition> i = getDeclaredItems();
        for (ExtendedItemDefinition item : i) {
            if (!item.isNode()) {
                list.add((ExtendedPropertyDefinition) item);
            }
        }
        return list.toArray(new ExtendedPropertyDefinition[0]);
    }

    public Map<String, ExtendedNodeDefinition> getChildNodeDefinitionsAsMap() {
        if (allNodes == null) {
            LinkedHashMap<String, ExtendedNodeDefinition> allNodesMap = new LinkedHashMap<>();
            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length - 1; i >= 0; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedNodeDefinition> c = new HashMap<>(nodeType.getDeclaredChildNodeDefinitionsAsMap());
                Map<String, ExtendedNodeDefinition> over = new HashMap<>(nodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allNodesMap.putAll(c);
            }
            allNodesMap.putAll(nodes);
            this.allNodes = Collections.unmodifiableMap(allNodesMap);
        }

        return allNodes;
    }

    public Map<String, ExtendedNodeDefinition> getUnstructuredChildNodeDefinitions() {
        if (allUnstructuredNodes == null) {
            LinkedHashMap<String, ExtendedNodeDefinition> allUnstructuredNodesMap = new LinkedHashMap<>(unstructuredNodes);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length - 1; i >= 0; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedNodeDefinition> c = new HashMap<>(nodeType.getDeclaredUnstructuredChildNodeDefinitions());
                Map<String, ExtendedNodeDefinition> over = new HashMap<>(unstructuredNodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allUnstructuredNodesMap.putAll(c);
            }
            this.allUnstructuredNodes = Collections.unmodifiableMap(allUnstructuredNodesMap);
        }
        return allUnstructuredNodes;
    }

    public ExtendedNodeDefinition[] getChildNodeDefinitions() {
        List<ExtendedNodeDefinition> list = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        final List<ExtendedItemDefinition> i = getItems();
        Collections.reverse(i);
        for (ExtendedItemDefinition item : i) {
            if (item.isNode() && (item.isUnstructured() || !keys.contains(item.getName()))) {
                list.add((ExtendedNodeDefinition) item);
                keys.add(item.getName());
            }
        }
        Collections.reverse(list);
        return list.toArray(new ExtendedNodeDefinition[0]);
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredChildNodeDefinitionsAsMap() {
        getChildNodeDefinitionsAsMap();
        return nodes;
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredUnstructuredChildNodeDefinitions() {
        getUnstructuredChildNodeDefinitions();
        return unstructuredNodes;
    }

    public ExtendedNodeDefinition[] getDeclaredChildNodeDefinitions() {
        List<ExtendedNodeDefinition> list = new ArrayList<>();
        final List<ExtendedItemDefinition> i = getDeclaredItems();
        for (ExtendedItemDefinition item : i) {
            if (item.isNode()) {
                list.add((ExtendedNodeDefinition) item);
            }
        }
        return list.toArray(new ExtendedNodeDefinition[0]);
    }

    public List<String> getGroupedItems() {
        return groupedItems;
    }

    public void setGroupedItems(List<String> groupedItems) {
        this.groupedItems = groupedItems;
    }

    public boolean canSetProperty(String propertyName, Value value) {
        if (value == null) {
            // setting a property to null is equivalent of removing it
            return canRemoveItem(propertyName);
        }
        try {
            ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap()
                    .containsKey(propertyName) ? getPropertyDefinitionsAsMap().get(propertyName)
                    : getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                    value.getType(), false);
            if (def == null) {
                def = getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                        PropertyType.UNDEFINED, false);
            }
            if (def != null) {
                if (def.isMultiple() || def.isProtected()) {
                    return false;
                }
                InternalValue internalValue = null;
                if (value.getType() != PropertyType.BINARY && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                    internalValue = InternalValue.create(value, null, null);
                }
                if (internalValue != null) {
                    checkSetPropertyValueConstraints(def, new InternalValue[]{internalValue});
                }
                return true;
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }

    public boolean canSetProperty(String propertyName, Value[] values) {
        if (values == null) {
            // setting a property to null is equivalent of removing it
            return canRemoveItem(propertyName);
        }
        try {
            // determine type of values
            int type = PropertyType.UNDEFINED;
            for (Value value : values) {
                if (value == null) {
                    // skip null values as those would be purged
                    continue;
                }
                if (type == PropertyType.UNDEFINED) {
                    type = value.getType();
                } else if (type != value.getType()) {
                    // inhomogeneous types
                    return false;
                }
            }
            ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap()
                    .containsKey(propertyName) ? getPropertyDefinitionsAsMap().get(propertyName)
                    : getMatchingPropDef(getUnstructuredPropertyDefinitions().values(), type, true);
            if (def == null) {
                def = getMatchingPropDef(getUnstructuredPropertyDefinitions().values(),
                        PropertyType.UNDEFINED, true);
            }
            if (def != null) {
                if (!def.isMultiple() || def.isProtected()) {
                    return false;
                }
                List<InternalValue> list = new ArrayList<>();
                for (Value value : values) {
                    if (value != null) {
                        // perform type conversion as necessary and create InternalValue
                        // from (converted) Value
                        InternalValue internalValue = null;
                        if (value.getType() != PropertyType.BINARY
                                && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                            internalValue = InternalValue.create(value, null, null);
                        }
                        list.add(internalValue);
                    }
                }
                if (!list.isEmpty()) {
                    InternalValue[] internalValues = list.toArray(new InternalValue[0]);
                    checkSetPropertyValueConstraints(def, internalValues);
                }
                return true;
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }

    public boolean canAddChildNode(String childNodeName) {
        return getChildNodeDefinitionsAsMap().containsKey(childNodeName) && getChildNodeDefinitionsAsMap().get(childNodeName).getDefaultPrimaryType() != null;
    }

    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
            if (!nt.isAbstract() && !nt.isMixin()) {
                if (getChildNodeDefinitionsAsMap().containsKey(childNodeName)) {
                    if (canAddChildNode(nt, getChildNodeDefinitionsAsMap().get(childNodeName))) {
                        return true;
                    }
                }
                Collection<ExtendedNodeDefinition> unstruct = getUnstructuredChildNodeDefinitions().values();
                for (ExtendedNodeDefinition definition : unstruct) {
                    if (canAddChildNode(nt, definition)) {
                        return true;
                    }
                }
            }
        } catch (RepositoryException e) {
            // fall through
        }
        return false;
    }

    private boolean canAddChildNode(ExtendedNodeType nt, ExtendedNodeDefinition nodeDef) {
        String[] epd = nodeDef.getRequiredPrimaryTypeNames();
        for (String s : epd) {
            if (!nt.isNodeType(s)) {
                return false;
            }
        }
        return true;
    }

    public boolean canRemoveItem(String s) {
        try {
            checkRemoveItemConstraints(s);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        return false;
    }

    public boolean canRemoveNode(String nodeName) {
        try {
            checkRemoveNodeConstraints(nodeName);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        // todo ????!!
        return true;
    }

    public boolean canRemoveProperty(String propertyName) {
        try {
            checkRemovePropertyConstraints(propertyName);
            return true;
        } catch (RepositoryException re) {
            // fall through
        }
        return false;
    }

    void setPropertyDefinition(String name, ExtendedPropertyDefinition p) {
        if (name.equals("*")) {
            if (p.isMultiple()) {
                unstructuredProperties.put(ExtendedPropertyType.MULTIPLE_OFFSET + p.getRequiredType(), p);
            } else {
                unstructuredProperties.put(p.getRequiredType(), p);
            }
            allUnstructuredProperties = null;
        } else {
            properties.put(name, p);
            allProperties = null;
        }
        items.add(p);
    }

    void removePropertyDefinition(ExtendedPropertyDefinition p) {
        String pdName = p.getName();
        if (p.isUnstructured()) {
            if (p.isMultiple()) {
                unstructuredProperties.remove(256 + p.getRequiredType());
            } else {
                unstructuredProperties.remove(p.getRequiredType());
            }
            allUnstructuredProperties = null;
        } else {
            properties.remove(pdName);
            allProperties = null;
        }
        items.remove(p);
    }

    public ExtendedPropertyDefinition getPropertyDefinition(String name) {
        return properties.get(name);
    }

    void setNodeDefinition(String name, ExtendedNodeDefinition p) {
        if (p.isUnstructured()) {
            StringBuilder s = new StringBuilder();
            if (p.getRequiredPrimaryTypeNames() == null) {
                logger.error("Required primary type names is null for extended node definition {}", p);
            }
            for (String s1 : p.getRequiredPrimaryTypeNames()) {
                s.append(s1).append(" ");
            }
            unstructuredNodes.put(s.toString().trim(), p);
            allUnstructuredNodes = null;
        } else {
            nodes.put(name, p);
            allNodes = null;
        }
        items.add(p);
    }

    void removeNodeDefinition(ExtendedNodeDefinition p) {
        String ndName = p.getName();
        if (p.isUnstructured()) {
            unstructuredNodes.remove(StringUtils.join(p.getRequiredPrimaryTypeNames(), " "));
            allUnstructuredNodes = null;
        } else {
            nodes.remove(ndName);
            allNodes = null;
        }
        items.remove(p);
    }

    public ExtendedNodeDefinition getNodeDefinition(String name) {
        return nodes.get(name);
    }

    public String getItemsType() {
        return itemsType;
    }

    public void setItemsType(String itemsType) {
        this.itemsType = itemsType;
    }

    public void sortItems(Comparator<ExtendedItemDefinition> c) {
        Collections.sort(items, c);
    }

    public void addMixinExtend(String mixinExtension) {
        this.mixinExtendNames.add(mixinExtension);
    }

    public List<String> getMixinExtendNames() {
        return mixinExtendNames;
    }

    public void setMixinExtendNames(List<String> mixinExtendNames) {
        this.mixinExtendNames = mixinExtendNames;
    }

    public List<ExtendedNodeType> getMixinExtends() {
        return mixinExtend;
    }

    public JahiaTemplatesPackage getTemplatePackage() {
        if (!systemType && templatesPackage == null) {
            try {
                templatesPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                        .getTemplatePackageById(getSystemId());
            } catch (Exception e) {
                logger.warn("Unable to get the template package for the node with system id '" + getSystemId() + "'", e);
            }
        }

        return templatesPackage;
    }

    protected String lookupLabel(String key, Locale locale, String defaultValue) {
        JahiaTemplatesPackage pkg = getTemplatePackage();
        return pkg != null ? Messages.get(pkg, key, locale, defaultValue) : Messages.getTypes(key, locale,
                defaultValue);
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            String key = JCRContentUtils.replaceColon(getName());
            label = lookupLabel(key, locale, StringUtils.substringAfter(getName(), ":"));
            labels.put(locale, label);
        }
        return label;
    }

    public void addLabel(Locale locale, String label){
        labels.put(locale, label);
    }

    public String getDescription(Locale locale) {
        String description = descriptions.get(locale);
        if (description == null) {
            String key = JCRContentUtils.replaceColon(getName()) + "_description";
            description = lookupLabel(key, locale, StringUtils.EMPTY);
            descriptions.put(locale, description);
        }
        return description;
    }

    public void addDescription(Locale locale, String description){
        descriptions.put(locale, description);
    }

    public NodeTypeDefinition getNodeTypeDefinition() {
        return new Definition();
    }

    public String getLocalName() {
        return this.name.getLocalName();
    }

    public String getPrefix() {
        return this.name.getPrefix();
    }

    /**
     * @param s
     * @throws ConstraintViolationException
     */
    private void checkRemoveItemConstraints(String s) throws ConstraintViolationException {
        ExtendedItemDefinition def = getPropertyDefinitionsAsMap().get(s);
        if (def == null) {
            def = getChildNodeDefinitionsAsMap().get(s);
        }
        if (def != null) {
            if (def.isMandatory()) {
                throw new ConstraintViolationException("can't remove mandatory item");
            }
            if (def.isProtected()) {
                throw new ConstraintViolationException("can't remove protected item");
            }
        }
    }

    /**
     * @param name
     * @throws ConstraintViolationException
     */
    private void checkRemoveNodeConstraints(String name) throws ConstraintViolationException {
        ExtendedNodeDefinition def = getChildNodeDefinitionsAsMap().get(name);
        if (def != null) {
            if (def.isMandatory()) {
                throw new ConstraintViolationException("can't remove mandatory node");
            }
            if (def.isProtected()) {
                throw new ConstraintViolationException("can't remove protected node");
            }
        }
    }

    /**
     * @param propertyName
     * @throws ConstraintViolationException
     */
    private void checkRemovePropertyConstraints(String propertyName)
            throws ConstraintViolationException {
        ExtendedPropertyDefinition def = getPropertyDefinitionsAsMap().get(propertyName);
        if (def != null) {
            if (def.isMandatory()) {
                throw new ConstraintViolationException("can't remove mandatory property");
            }
            if (def.isProtected()) {
                throw new ConstraintViolationException("can't remove protected property");
            }
        }
    }

    private ExtendedPropertyDefinition getMatchingPropDef(Collection<ExtendedPropertyDefinition> defs, int type,
                                                          boolean multiValued) {
        ExtendedPropertyDefinition match = null;
        for (ExtendedPropertyDefinition pd : defs) {
            int reqType = pd.getRequiredType();
            // match type
            if (reqType == PropertyType.UNDEFINED || type == PropertyType.UNDEFINED
                    || reqType == type) {
                // match multiValued flag
                if (multiValued == pd.isMultiple()) {
                    // found match
                    if (pd.getRequiredType() != PropertyType.UNDEFINED) {
                        // found best possible match, get outta here
                        return pd;
                    } else {
                        if (match == null) {
                            match = pd;
                        }
                    }
                }
            }
        }
        return match;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final ExtendedNodeType other = (ExtendedNodeType) obj;
        return (getName() != null ? getName().equals(other.getName()) : other.getName() == null);
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public void clearLabels() {
        labels.clear();
        descriptions.clear();
        if (allProperties != null) {
            for (ExtendedPropertyDefinition propertyDefinition : allProperties.values()) {
                propertyDefinition.clearLabels();
            }
        }
        if (properties != null) {
            for (ExtendedPropertyDefinition propertyDefinition : properties.values()) {
                propertyDefinition.clearLabels();
            }
        }
        if (items != null) {
            for (ExtendedItemDefinition item : items) {
                item.clearLabels();
            }
        }
    }

    public List<ExtendedItemDefinition> getRawItems() {
        return items;
    }

    public Map<String, ExtendedNodeDefinition> getRawNodes() {
        return nodes;
    }

    public Map<String, ExtendedPropertyDefinition> getRawProperties() {
        return properties;
    }

    public Map<String, ExtendedNodeDefinition> getRawUnstructuredNodes() {
        return unstructuredNodes;
    }

    public Map<Integer, ExtendedPropertyDefinition> getRawUnstructuredProperties() {
        return unstructuredProperties;
    }

    @Override
    public String toString() {
        return getName();
    }

    class Definition implements NodeTypeDefinition {
        public String getName() {
            return name.toString();
        }

        public String[] getDeclaredSupertypeNames() {
            String[] d = declaredSupertypeNames;

            ExtendedPropertyDefinition[] defs = ExtendedNodeType.this.getDeclaredPropertyDefinitions();
            for (ExtendedPropertyDefinition def : defs) {
                if (def.isInternationalized()) {
                    String[] newRes = new String[d.length + 1];
                    System.arraycopy(d, 0, newRes, 0, d.length);
                    newRes[d.length] = "jmix:i18n";
                    return newRes;
                }
            }

            return d;
        }

        public boolean isAbstract() {
            return isAbstract;
        }

        public boolean isMixin() {
            return isMixin;
        }

        public boolean hasOrderableChildNodes() {
            return hasOrderableChildNodes;
        }

        public boolean isQueryable() {
            return true;
        }

        public String getPrimaryItemName() {
            return primaryItemName;
        }

        public PropertyDefinition[] getDeclaredPropertyDefinitions() {
            ExtendedPropertyDefinition[] defs = ExtendedNodeType.this.getDeclaredPropertyDefinitions();
            List<PropertyDefinition> r = new ArrayList<>();
            for (final ExtendedPropertyDefinition def : defs) {
                if (!def.isInternationalized() && !def.isOverride()) {
                    r.add(new PropertyDefinition() {
                        public int getRequiredType() {
                            return def.getRequiredType() != PropertyType.REFERENCE ? def.getRequiredType() : PropertyType.WEAKREFERENCE;
                        }

                        public String[] getValueConstraints() {
                            return def.getValueConstraints();
                        }

                        public Value[] getDefaultValues() {
                            List<Value> res = new ArrayList<>();
                            Value[] defaultValues = def.getDefaultValuesAsUnexpandedValue();
                            for (Value defaultValue : defaultValues) {
                                if (!(defaultValue instanceof DynamicValueImpl)) {
                                    res.add(defaultValue);
                                } else {
                                    // Use fake default value - value will be calculated by JahiaNodeTypeInstanceHandler
                                    switch (getRequiredType()) {
                                        case PropertyType.LONG:
                                        case PropertyType.DOUBLE:
                                        case PropertyType.DECIMAL:
                                            res.add(new ValueImpl("0", getRequiredType()));
                                            break;
                                        case PropertyType.DATE:
                                            res.add(new ValueImpl(new GregorianCalendar()));
                                            break;
                                        case PropertyType.BOOLEAN:
                                            res.add(new ValueImpl(true));
                                            break;
                                        default:
                                            res.add(new ValueImpl("", getRequiredType()));
                                    }
                                }
                            }
                            return res.toArray(new Value[0]);
                        }

                        public boolean isMultiple() {
                            return def.isMultiple();
                        }

                        public String[] getAvailableQueryOperators() {
                            return def.getAvailableQueryOperators();
                        }

                        public boolean isFullTextSearchable() {
                            return def.isFullTextSearchable();
                        }

                        public boolean isQueryOrderable() {
                            return def.isQueryOrderable();
                        }

                        public NodeType getDeclaringNodeType() {
                            return def.getDeclaringNodeType();
                        }

                        public String getName() {
                            return def.getName();
                        }

                        public boolean isAutoCreated() {
                            return def.isAutoCreated();
                        }

                        public boolean isMandatory() {
                            return false;
                        }

                        public int getOnParentVersion() {
                            return def.getOnParentVersion();
                        }

                        public boolean isProtected() {
                            return false;
                        }
                    });
                }
            }
            return r.toArray(new PropertyDefinition[0]);
        }

        public NodeDefinition[] getDeclaredChildNodeDefinitions() {
            ExtendedNodeDefinition[] defs = ExtendedNodeType.this.getDeclaredChildNodeDefinitions();


            List<NodeDefinition> r = new ArrayList<>();
            for (final ExtendedNodeDefinition def : defs) {
                if (!def.isOverride()) {
                    r.add(new NodeDefinition() {
                        public NodeType[] getRequiredPrimaryTypes() {
                            return def.getRequiredPrimaryTypes();
                        }

                        public String[] getRequiredPrimaryTypeNames() {
                            return def.getRequiredPrimaryTypeNames();
                        }

                        public NodeType getDefaultPrimaryType() {
                            return def.getDefaultPrimaryType();
                        }

                        public String getDefaultPrimaryTypeName() {
                            return def.getDefaultPrimaryTypeName();
                        }

                        public boolean allowsSameNameSiblings() {
                            return def.allowsSameNameSiblings();
                        }

                        public NodeType getDeclaringNodeType() {
                            return def.getDeclaringNodeType();
                        }

                        public String getName() {
                            return def.getName();
                        }

                        public boolean isAutoCreated() {
                            return false;
                        }

                        public boolean isMandatory() {
                            return false;
                        }

                        public int getOnParentVersion() {
                            return def.getOnParentVersion();
                        }

                        public boolean isProtected() {
                            return false;
                        }
                    });
                }
            }
            return r.toArray(new NodeDefinition[0]);
        }
    }
}
