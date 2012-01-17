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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.commons.nodetype.constraint.ValueConstraint;
import org.apache.jackrabbit.spi.commons.value.QValueValue;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.ExternalReferenceValue;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jahia extended JCR node type information.
 * @author Thomas Draier
 * Date: 4 janv. 2008
 * Time: 14:02:22
 */
public class ExtendedNodeType implements NodeType {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ExtendedNodeType.class);
    public static final Name NT_BASE_NAME = new Name("base", org.apache.jackrabbit.spi.Name.NS_NT_PREFIX, org.apache.jackrabbit.spi.Name.NS_NT_URI);
    
    private NodeTypeRegistry registry;
    private String systemId;
    private List<ExtendedItemDefinition> items = new ArrayList<ExtendedItemDefinition>();
    private List<String> groupedItems;

    private Map<String, ExtendedNodeDefinition> nodes = new ConcurrentHashMap<String, ExtendedNodeDefinition>();
    private Map<String, ExtendedPropertyDefinition> properties = new ConcurrentHashMap<String, ExtendedPropertyDefinition>();
    private Map<String, ExtendedNodeDefinition> unstructuredNodes = new ConcurrentHashMap<String, ExtendedNodeDefinition>();
    private Map<Integer, ExtendedPropertyDefinition> unstructuredProperties = new ConcurrentHashMap<Integer, ExtendedPropertyDefinition>();

    private Map<String, ExtendedNodeDefinition> allNodes;
    private Map<String, ExtendedPropertyDefinition> allProperties;
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
    private List<ExtendedNodeType> declaredSubtypes = new ArrayList<ExtendedNodeType>();
    private String validator;
    private boolean queryable = true;
    private String itemsType;
    private List<String> mixinExtendNames = new ArrayList<String>();
    private List<ExtendedNodeType> mixinExtend = new ArrayList<ExtendedNodeType>();
//    private boolean liveContent = false;

    private Map<Locale, String> labels = new ConcurrentHashMap<Locale, String>(1);
    private Map<Locale, String> descriptions = new ConcurrentHashMap<Locale, String>(1);
    public ExtendedNodeType(NodeTypeRegistry registry, String systemId) {
        this.registry = registry;
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getName() {
        return name.toString();
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

    public void setName(Name name) {
        this.name = name;
        this.alias = name != null ? name.toString() : null;
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

//    public boolean isLiveContent() {
//        return liveContent;
//    }
//
//    public void setLiveContent(boolean liveContent) {
//        this.liveContent = liveContent;
//    }

    public boolean hasOrderableChildNodes() {
        if(!hasOrderableChildNodes) {
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
        Set<ExtendedNodeType> l = new LinkedHashSet<ExtendedNodeType>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s != null) {
                l.add(s);
                l.addAll(Arrays.asList(s.getSupertypes()));
                if (!s.isMixin()) {
                    primaryFound = true;
                }
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(getName()) && !isMixin) {
            try {
                l.add(registry.getNodeType(Constants.NT_BASE));
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for "+getName(),e);
            }
        }
        return new ArrayList<ExtendedNodeType>(l).toArray(new ExtendedNodeType[l.size()]);
    }

    public ExtendedNodeType[] getPrimarySupertypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
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
                logger.error("No such supertype for "+getName(),e);
            }
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    public ExtendedNodeType[] getDeclaredSupertypes() {
        return declaredSupertypes;
    }

    public void setDeclaredSupertypes(String[] declaredSupertypes) {
        this.declaredSupertypeNames = declaredSupertypes;
    }


    void validate() throws NoSuchNodeTypeException {
        this.declaredSupertypes = new ExtendedNodeType[declaredSupertypeNames.length];
        int mixIndex = 0;
        for (int i = 0; i < declaredSupertypeNames.length; i++) {
            final ExtendedNodeType nodeType = registry.getNodeType(declaredSupertypeNames[i]);
            if (!nodeType.isMixin && i>0) {
                System.arraycopy(this.declaredSupertypes, mixIndex, this.declaredSupertypes, mixIndex+1, i-mixIndex);
                this.declaredSupertypes[mixIndex] = nodeType;
                mixIndex ++;
            } else {
                this.declaredSupertypes[i] = nodeType;
            }
            nodeType.addSubType(this);
        }
        for (String s : mixinExtendNames) {
            final ExtendedNodeType type = registry.getNodeType(s);
            registry.addMixinExtension(this, type);
            mixinExtend.add(type);
        }
        for (ExtendedItemDefinition itemDefinition : items) {
            if (itemDefinition.getItemType() != null) {
                registry.addTypedItem(itemDefinition);
            }
        }
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
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            l.add(s);
            NodeTypeIterator subtypes = s.getSubtypes();
            while (subtypes.hasNext()) {
                l.add((ExtendedNodeType) subtypes.next());
            }
        }
        return new NodeTypeIteratorImpl(l.iterator(), l.size());
    }

    public List<ExtendedNodeType> getSubtypesAsList() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            l.add(s);
            NodeTypeIterator subtypes = s.getSubtypes();
            while (subtypes.hasNext()) {
                l.add((ExtendedNodeType) subtypes.next());
            }
        }
        return l;
    }

    public ExtendedNodeType[] getMixinSubtypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            if (s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getMixinSubtypes()));
            }
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
    }

    public boolean isNodeType(String typeName) {
        if (getName().equals(typeName) || Constants.NT_BASE.equals(typeName)) {
            return true;
        }
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s.isNodeType(typeName)) {
                return true;
            }
        }
        return false;
    }

    public List<ExtendedItemDefinition>  getItems() {
        List<ExtendedItemDefinition> l = new ArrayList<ExtendedItemDefinition>();
        l.addAll(getDeclaredItems());

        ExtendedNodeType[] supertypes = getSupertypes();
        for (int i = 0; i < supertypes.length ; i++) {
            l.addAll(supertypes[i].getDeclaredItems());
        }

        return l;
    }

    public List<ExtendedItemDefinition>  getDeclaredItems() {
        getPropertyDefinitionsAsMap();
        getChildNodeDefinitionsAsMap();
        List<ExtendedItemDefinition> res = new ArrayList<ExtendedItemDefinition>();
        for (ExtendedItemDefinition item : items) {
            if (!item.isOverride()) {
                res.add(item);
            }
        }
        return Collections.unmodifiableList(res);
    }

    public Map<String, ExtendedPropertyDefinition> getPropertyDefinitionsAsMap() {
        if (allProperties == null) {
        	synchronized (this) {
        		if (allProperties == null) {
        			LinkedHashMap<String, ExtendedPropertyDefinition> props = new LinkedHashMap<String, ExtendedPropertyDefinition>();
		
		            props.putAll(properties);
		
		            ExtendedNodeType[] supertypes = getSupertypes();
		            for (int i = supertypes.length-1; i >=0 ; i--) {
		                ExtendedNodeType nodeType = supertypes[i];
		                Map<String, ExtendedPropertyDefinition> c = new HashMap<String, ExtendedPropertyDefinition>(nodeType.getDeclaredPropertyDefinitionsAsMap());
		                Map<String, ExtendedPropertyDefinition> over = new HashMap<String, ExtendedPropertyDefinition>(properties);
		                over.keySet().retainAll(c.keySet());
		                for (ExtendedPropertyDefinition s : over.values()) {
		                    s.setOverride(true);
		                }
		                c.keySet().removeAll(over.keySet());
		                props.putAll(c);
		            }
		            
		            allProperties = Collections.unmodifiableMap(props);
        		}
        	}
        }

        return allProperties;
    }

    public Map<Integer,ExtendedPropertyDefinition> getUnstructuredPropertyDefinitions() {
        if (allUnstructuredProperties == null) {
            allUnstructuredProperties = new LinkedHashMap<Integer,ExtendedPropertyDefinition>();

            allUnstructuredProperties.putAll(unstructuredProperties);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<Integer,ExtendedPropertyDefinition> c = new HashMap<Integer,ExtendedPropertyDefinition>(nodeType.getDeclaredUnstructuredPropertyDefinitions());
                Map<Integer,ExtendedPropertyDefinition> over = new HashMap<Integer,ExtendedPropertyDefinition>(unstructuredProperties);
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
        List<ExtendedPropertyDefinition> list = new ArrayList<ExtendedPropertyDefinition>();
        Set<String> keys = new HashSet<String>();

        final List<ExtendedItemDefinition> i = getItems();
        Collections.reverse(i);
        for (ExtendedItemDefinition item : i) {
            if (!item.isNode() && ("*".equals(item.getName()) || !keys.contains(item.getName()))) {
                list.add((ExtendedPropertyDefinition) item);
                keys.add(item.getName());
            }
        }
        Collections.reverse(list);
        return list.toArray(new ExtendedPropertyDefinition[list.size()]);
    }

    public Map<String, ExtendedPropertyDefinition> getDeclaredPropertyDefinitionsAsMap() {
        getPropertyDefinitionsAsMap();
        return properties;
    }

    public Map<Integer,ExtendedPropertyDefinition> getDeclaredUnstructuredPropertyDefinitions() {
        getUnstructuredPropertyDefinitions();
        return unstructuredProperties;
    }

    public ExtendedPropertyDefinition[] getDeclaredPropertyDefinitions() {
        List<ExtendedPropertyDefinition> list = new ArrayList<ExtendedPropertyDefinition>();

        final List<ExtendedItemDefinition> i = getDeclaredItems();
        for (ExtendedItemDefinition item : i) {
            if (!item.isNode()) { // && ("*".equals(item.getName()) || !keys.contains(item.getName()))) {
                    list.add((ExtendedPropertyDefinition) item);
            }
        }
        return list.toArray(new ExtendedPropertyDefinition[list.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getChildNodeDefinitionsAsMap() {
        if (allNodes == null) {
            allNodes = new LinkedHashMap<String, ExtendedNodeDefinition>();
            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedNodeDefinition> c = new HashMap<String, ExtendedNodeDefinition>(nodeType.getDeclaredChildNodeDefinitionsAsMap());
                Map<String, ExtendedNodeDefinition> over = new HashMap<String, ExtendedNodeDefinition>(nodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allNodes.putAll(c);
            }

            allNodes.putAll(nodes);
        }

        return Collections.unmodifiableMap(allNodes);
    }

    public Map<String,ExtendedNodeDefinition> getUnstructuredChildNodeDefinitions() {
        if (allUnstructuredNodes == null) {
            allUnstructuredNodes = new ConcurrentHashMap<String,ExtendedNodeDefinition>();
            allUnstructuredNodes.putAll(unstructuredNodes);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = supertypes.length-1; i >=0 ; i--) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String,ExtendedNodeDefinition> c = new HashMap<String,ExtendedNodeDefinition>(nodeType.getDeclaredUnstructuredChildNodeDefinitions());
                Map<String,ExtendedNodeDefinition> over = new HashMap<String,ExtendedNodeDefinition>(unstructuredNodes);
                over.keySet().retainAll(c.keySet());
                for (ExtendedNodeDefinition s : over.values()) {
                    s.setOverride(true);
                }
                c.keySet().removeAll(over.keySet());
                allUnstructuredNodes.putAll(c);
            }
        }
        return Collections.unmodifiableMap(allUnstructuredNodes);
    }

    public ExtendedNodeDefinition[] getChildNodeDefinitions() {
        List<ExtendedNodeDefinition> list = new ArrayList<ExtendedNodeDefinition>();
        Set<String> keys = new HashSet<String>();
        final List<ExtendedItemDefinition> i = getItems();
        Collections.reverse(i);
        for (ExtendedItemDefinition item : i) {
            if (item.isNode() && ("*".equals(item.getName()) || !keys.contains(item.getName()))) {
                list.add((ExtendedNodeDefinition) item);
                keys.add(item.getName());
            }
        }
        Collections.reverse(list);
        return list.toArray(new ExtendedNodeDefinition[list.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredChildNodeDefinitionsAsMap() {
        getChildNodeDefinitionsAsMap();
        return nodes;
    }

    public Map<String,ExtendedNodeDefinition> getDeclaredUnstructuredChildNodeDefinitions() {
        getUnstructuredChildNodeDefinitions();
        return unstructuredNodes;
    }

    public ExtendedNodeDefinition[] getDeclaredChildNodeDefinitions() {
        List<ExtendedNodeDefinition> list = new ArrayList<ExtendedNodeDefinition>();
        final List<ExtendedItemDefinition> i = getDeclaredItems();
        for (ExtendedItemDefinition item : i) {
            if (item.isNode()) { // && ("*".equals(item.getName()) || !keys.contains(item.getName()))) {
                    list.add((ExtendedNodeDefinition) item);
            }
        }
        return list.toArray(new ExtendedNodeDefinition[list.size()]);
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
                int targetType;
                if (def.getRequiredType() != PropertyType.UNDEFINED
                        && def.getRequiredType() != value.getType()) {
                    // type conversion required
                    targetType = def.getRequiredType();
                } else {
                    // no type conversion required
                    targetType = value.getType();
                }
                // perform type conversion as necessary and create InternalValue
                // from (converted) Value
                InternalValue internalValue = null;
                if (targetType != value.getType()) {
                    // type conversion required, but Jahia cannot do it, because we have no valueFactory, resolver, store or session object here
                    // Value targetVal = ValueHelper.convert(
                    // value, targetType,
                    // valueFactory);
                    if (value.getType() != PropertyType.BINARY
                            && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                        internalValue = InternalValue.create(value, null, null);
                    }
                } else {
                    // no type conversion required
                    if (value.getType() != PropertyType.BINARY
                            && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                        internalValue = InternalValue.create(value, null, null);
                    }
                }
                if (internalValue != null) {
                    checkSetPropertyValueConstraints(def, new InternalValue[] { internalValue });
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
                int targetType;
                if (def.getRequiredType() != PropertyType.UNDEFINED
                        && def.getRequiredType() != type) {
                    // type conversion required, but Jahia cannot do it, because we have no valueFactory, resolver, store or session object here
                    targetType = def.getRequiredType();
                } else {
                    // no type conversion required
                    targetType = type;
                }
                List<InternalValue> list = new ArrayList<InternalValue>();
                for (Value value : values) {
                    if (value != null && !(value instanceof ExternalReferenceValue)) {
                        // perform type conversion as necessary and create InternalValue
                        // from (converted) Value
                        InternalValue internalValue = null;
                        if (targetType != value.getType()) {
                            // type conversion required
                            // Value targetVal = ValueHelper.convert(
                            // value, targetType,
                            // valueFactory);
                            if (value.getType() != PropertyType.BINARY
                                    && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                                internalValue = InternalValue.create(value, null, null);
                            }
                        } else {
                            // no type conversion required
                            if (value.getType() != PropertyType.BINARY
                                    && !((value.getType() == PropertyType.PATH || value.getType() == PropertyType.NAME) && !(value instanceof QValueValue))) {
                                internalValue = InternalValue.create(value, null, null);
                            }
                        }
                        list.add(internalValue);
                    }
                }
                if (!list.isEmpty()) {
                    InternalValue[] internalValues = list.toArray(new InternalValue[list.size()]);
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
        if (getChildNodeDefinitionsAsMap().containsKey(childNodeName)) {
            if (getChildNodeDefinitionsAsMap().get(childNodeName).getDefaultPrimaryType() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
        try {
            ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
            if (!nt.isAbstract() && !nt.isMixin()) {
                if (getChildNodeDefinitionsAsMap().containsKey(childNodeName)) {
                    if (canAddChildNode(nt,getChildNodeDefinitionsAsMap().get(childNodeName)))  {
                        return true;
                    }
                }
                Collection<ExtendedNodeDefinition> unstruct = getUnstructuredChildNodeDefinitions().values();
                for (ExtendedNodeDefinition definition : unstruct) {
                    if (canAddChildNode(nt,definition))  {
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
                unstructuredProperties.put(256 + p.getRequiredType(), p);
            } else {
                unstructuredProperties.put(p.getRequiredType(), p);
            }
        } else {
            properties.put(name, p);
        }
        items.add(p);
    }

    public ExtendedPropertyDefinition getPropertyDefinition(String name) {
        return properties.get(name);
    }

    void setNodeDefinition(String name, ExtendedNodeDefinition p) {
        if (name.equals("*")) {
            StringBuffer s = new StringBuffer("");
            if (p.getRequiredPrimaryTypeNames() == null) {
                logger.error("Required primary type names is null for extended node definition " + p);
            }
            for (String s1 : p.getRequiredPrimaryTypeNames()) {
                s.append(s1).append(" ");
            }
            unstructuredNodes.put(s.toString().trim(), p);
        } else {
            nodes.put(name, p);
        }
        items.add(p);
    }

    public ExtendedNodeDefinition getNodeDefinition(String name) {
        return nodes.get(name);
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public String getItemsType() {
        return itemsType;
    }

    public void setItemsType(String itemsType) {
        this.itemsType = itemsType;
    }

    public void addMixinExtend(String mixinExtension) {
        this.mixinExtendNames.add(mixinExtension);
    }

    public List<ExtendedNodeType> getMixinExtends() {
        return mixinExtend;
    }

    public JahiaTemplatesPackage getTemplatePackage() {
        JahiaTemplatesPackage pkg = null;
        if (!getSystemId().startsWith("system-")) {
            try {
                pkg = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService().getTemplatePackage(
                                getSystemId());
            } catch (Exception e) {
                logger.warn(
                        "Unable to get the template package for the node with system id '"
                                + getSystemId() + "'", e);
            }
        }

        return pkg;
    }

    protected String getResourceBundleId() {
        JahiaTemplatesPackage pkg = getTemplatePackage();
        return pkg != null ? "modules." + pkg.getRootFolder() + "." + pkg.getResourceBundleName() : "JahiaTypesResources";
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            String key = JCRContentUtils.replaceColon(getName());
            String tpl = getTemplatePackage() != null ? getTemplatePackage().getName() : null;
            label = new JahiaResourceBundle(getResourceBundleId(), locale, tpl, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), tpl)).getString(key, StringUtils.substringAfter(getName(),":"));
            labels.put(locale, label);
        }
        return label;
    }

    public String getDescription(Locale locale) {
        String description = descriptions.get(locale);
        if (description == null) {
            String key = JCRContentUtils.replaceColon(getName()) + "_description";
            String tpl = getTemplatePackage() != null ? getTemplatePackage().getName() : null;
            description = new JahiaResourceBundle(getResourceBundleId(), locale, tpl, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), tpl)).getString(key, "");
            descriptions.put(locale, description);
        }
        return description;
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

    class Definition implements NodeTypeDefinition {
        public String getName() {
            return name.toString();
        }

        public String[] getDeclaredSupertypeNames() {
            String[] d = declaredSupertypeNames;

            ExtendedPropertyDefinition[] defs = ExtendedNodeType.this.getDeclaredPropertyDefinitions();
            for (ExtendedPropertyDefinition def : defs) {
                if (def.isInternationalized()) {
                    String[] newRes = new String[d.length+1];
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
            List<PropertyDefinition> r = new ArrayList<PropertyDefinition>();
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
                            return def.getDefaultValues();
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
            return r.toArray(new PropertyDefinition[r.size()]);
        }

        public NodeDefinition[] getDeclaredChildNodeDefinitions() {
            ExtendedNodeDefinition[] defs = ExtendedNodeType.this.getDeclaredChildNodeDefinitions();


            List<NodeDefinition> r = new ArrayList<NodeDefinition>();
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
            return r.toArray(new NodeDefinition[r.size()]);
        }
    }
    
    /**
     * @param s
     * @throws ConstraintViolationException
     */
    private void checkRemoveItemConstraints(String s) throws ConstraintViolationException {
        ExtendedItemDefinition def = getPropertyDefinitionsAsMap().get(name);
        if (def == null) {
            def = getChildNodeDefinitionsAsMap().get(name);
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
    
    /**
     * Tests if the value constraints defined in the property definition
     * <code>pd</code> are satisfied by the the specified <code>values</code>.
     * <p/>
     * Note that the <i>protected</i> flag is not checked. Also note that no
     * type conversions are attempted if the type of the given values does not
     * match the required type as specified in the given definition.
     */
    private static void checkSetPropertyValueConstraints(ExtendedPropertyDefinition pd,
                                                        InternalValue[] values)
            throws ConstraintViolationException, RepositoryException {
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
                if (!satisfied) {
                    // re-throw last exception we encountered
                    throw cve;
                }
            }
        }
    }    
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final ExtendedNodeType castOther = (ExtendedNodeType) obj;
            return new EqualsBuilder()
                .append(this.getName(), castOther.getName())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .toHashCode();
    }
    
    public void clearLabels() {
        labels.clear();
    }
}
