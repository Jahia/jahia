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
package org.jahia.services.content.nodetypes;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

import javax.jcr.Value;
import javax.jcr.nodetype.*;
import java.util.*;

/**
 * Jahia extended JCR node type information.
 * @author Thomas Draier
 * Date: 4 janv. 2008
 * Time: 14:02:22
 */
public class ExtendedNodeType implements NodeType {

    private static final transient Logger logger = Logger.getLogger(ExtendedNodeType.class);

    private NodeTypeRegistry registry;
    private String systemId;
    private List<ExtendedItemDefinition> items = new ArrayList<ExtendedItemDefinition>();
    private List<String> groupedItems;

    private Map<String, ExtendedNodeDefinition> nodes = new ListOrderedMap();
    private Map<String, ExtendedPropertyDefinition> properties = new ListOrderedMap();
    private Map<String, ExtendedNodeDefinition> unstructuredNodes = new ListOrderedMap();
    private Map<Integer, ExtendedPropertyDefinition> unstructuredProperties = new ListOrderedMap();

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

    private Map<Locale, String> labels = new HashMap<Locale, String>(1);
    
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

    public boolean hasOrderableChildNodes() {
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
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        boolean primaryFound = false;
        ExtendedNodeType[] d = getDeclaredSupertypes();
        for (int i = 0; i < d.length; i++) {
            ExtendedNodeType s = d[i];
            if (s != null) {
                l.addAll(Arrays.asList(s.getSupertypes()));
                l.add(s);
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
        return l.toArray(new ExtendedNodeType[l.size()]);
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
        Arrays.sort(declaredSupertypes);
        this.declaredSupertypeNames = declaredSupertypes;
    }


    void validateSupertypes() throws NoSuchNodeTypeException {
        this.declaredSupertypes = new ExtendedNodeType[declaredSupertypeNames.length];
        for (int i = 0; i < declaredSupertypes.length; i++) {
            this.declaredSupertypes[i] = registry.getNodeType(declaredSupertypeNames[i]);
            this.declaredSupertypes[i].addSubType(this);
        }
    }

    void addSubType(ExtendedNodeType subType) {
        if (!declaredSubtypes.contains(subType)) {
            declaredSubtypes.add(subType);
        }
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

        ExtendedNodeType[] supertypes = getSupertypes();
        for (int i = 0; i < supertypes.length; i++) {
            ExtendedNodeType nodeType = supertypes[i];
            List<ExtendedItemDefinition> c = nodeType.getDeclaredItems();
            l.addAll(c);
        }

        l.addAll(items);

        return l;
    }

    public List<ExtendedItemDefinition>  getDeclaredItems() {
        return Collections.unmodifiableList(items);
    }

    public Map<String, ExtendedPropertyDefinition> getPropertyDefinitionsAsMap() {
        if (allProperties == null) {
            allProperties = new ListOrderedMap();

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = 0; i < supertypes.length; i++) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedPropertyDefinition> c = nodeType.getDeclaredPropertyDefinitionsAsMap();
                allProperties.putAll(c);
            }

            allProperties.putAll(properties);
        }

        return allProperties;
    }

    public Map<Integer,ExtendedPropertyDefinition> getUnstructuredPropertyDefinitions() {
        if (allUnstructuredProperties == null) {
            allUnstructuredProperties = new ListOrderedMap();

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = 0; i < supertypes.length; i++) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<Integer,ExtendedPropertyDefinition> c = nodeType.getDeclaredUnstructuredPropertyDefinitions();
                allUnstructuredProperties.putAll(c);
            }

            allUnstructuredProperties.putAll(unstructuredProperties);
        }
        return allUnstructuredProperties;
    }

    public ExtendedPropertyDefinition[] getPropertyDefinitions() {
        Collection<ExtendedPropertyDefinition> c = new ArrayList<ExtendedPropertyDefinition>(getPropertyDefinitionsAsMap().values());
        c.addAll(getUnstructuredPropertyDefinitions().values());
        return c.toArray(new ExtendedPropertyDefinition[c.size()]);
    }

    public Map<String, ExtendedPropertyDefinition> getDeclaredPropertyDefinitionsAsMap() {
        return properties;
    }

    public Map<Integer,ExtendedPropertyDefinition> getDeclaredUnstructuredPropertyDefinitions() {
        return unstructuredProperties;
    }

    public ExtendedPropertyDefinition[] getDeclaredPropertyDefinitions() {
        Collection<ExtendedPropertyDefinition> c = new ArrayList<ExtendedPropertyDefinition>(properties.values());
        c.addAll(unstructuredProperties.values());
        return c.toArray(new ExtendedPropertyDefinition[c.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getChildNodeDefinitionsAsMap() {
        if (allNodes == null) {
            allNodes = new ListOrderedMap();
            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = 0; i < supertypes.length; i++) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String, ExtendedNodeDefinition> c = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                allNodes.putAll(c);
            }

            allNodes.putAll(nodes);
        }

        return allNodes;
    }

    public Map<String,ExtendedNodeDefinition> getUnstructuredChildNodeDefinitions() {
        if (allUnstructuredNodes == null) {
            allUnstructuredNodes = new ListOrderedMap();
            allUnstructuredNodes.putAll(unstructuredNodes);

            ExtendedNodeType[] supertypes = getSupertypes();
            for (int i = 0; i < supertypes.length; i++) {
                ExtendedNodeType nodeType = supertypes[i];
                Map<String,ExtendedNodeDefinition> c = nodeType.getDeclaredUnstructuredChildNodeDefinitions();
                allUnstructuredNodes.putAll(c);
            }
        }
        return allUnstructuredNodes;
    }

    public ExtendedNodeDefinition[] getChildNodeDefinitions() {
        Collection<ExtendedNodeDefinition> c = new ArrayList<ExtendedNodeDefinition>(getChildNodeDefinitionsAsMap().values());
        c.addAll(getUnstructuredChildNodeDefinitions().values());
        return c.toArray(new ExtendedNodeDefinition[c.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredChildNodeDefinitionsAsMap() {
        return nodes;
    }

    public Map<String,ExtendedNodeDefinition> getDeclaredUnstructuredChildNodeDefinitions() {
        return unstructuredNodes;
    }

    public ExtendedNodeDefinition[] getDeclaredChildNodeDefinitions() {
        Collection<ExtendedNodeDefinition> c = new ArrayList<ExtendedNodeDefinition>(nodes.values());
        c.addAll(unstructuredNodes.values());
        return c.toArray(new ExtendedNodeDefinition[c.size()]);
    }

    public List<String> getGroupedItems() {
        return groupedItems;
    }

    public void setGroupedItems(List<String> groupedItems) {
        this.groupedItems = groupedItems;
    }

    public boolean canSetProperty(String propertyName, Value value) {
        if (getPropertyDefinitionsAsMap().containsKey(propertyName)) {
            ExtendedPropertyDefinition propertyDefinition = getPropertyDefinitionsAsMap().get(propertyName);
            if (!propertyDefinition.isMultiple()) {
                if (canSetProperty(value, propertyDefinition))  {
                    return true;
                }
            }
        }
        Collection<ExtendedPropertyDefinition> unstruct = getUnstructuredPropertyDefinitions().values();
        for (ExtendedPropertyDefinition definition : unstruct) {
            if (!definition.isMultiple()) {
                if (canSetProperty(value,definition))  {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canSetProperty(Value v , ExtendedPropertyDefinition propDef) {
        return !propDef.isMultiple() && v.getType() == propDef.getRequiredType();
    }


    public boolean canSetProperty(String propertyName, Value[] values) {
        if (getPropertyDefinitionsAsMap().containsKey(propertyName)) {
            ExtendedPropertyDefinition propertyDefinition = getPropertyDefinitionsAsMap().get(propertyName);
            if (propertyDefinition.isMultiple()) {
                if (canSetProperty(values, propertyDefinition))  {
                    return true;
                }
            }
        }
        Collection<ExtendedPropertyDefinition> unstruct = getUnstructuredPropertyDefinitions().values();
        for (ExtendedPropertyDefinition definition : unstruct) {
            if (definition.isMultiple()) {
                if (canSetProperty(values,definition))  {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canSetProperty(Value[] values , ExtendedPropertyDefinition propDef) {
        if (!propDef.isMultiple()) {
            return false;
        }
        for (Value value : values) {
            if (value.getType() != propDef.getRequiredType()) {
                return false;
            }
        }
        return true;
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
        } catch (NoSuchNodeTypeException e) {
            logger.error("Cannot find node type : "+nodeTypeName);
            return false;
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
        return true;
    }

    public boolean canRemoveNode(String nodeName) {
        return true;
    }

    public boolean canRemoveProperty(String propertyName) {
        return true;
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

    protected String getResourceBundleId() {
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

        return pkg != null ? "templates." + pkg.getRootFolder() + "." + pkg.getResourceBundleName() : "JahiaTypesResources";
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            JahiaSite site = Jahia.getThreadParamBean().getSite();
            final String packageName = site != null ? site.getTemplatePackageName() : null;
            String key = getName().replace(':', '_');
            label = new JahiaResourceBundle(getResourceBundleId(), locale, packageName, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), packageName)).getString(key, key);
            labels.put(locale, label);
        }
        return label;
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
                if (!def.isInternationalized()) {
                    r.add(new PropertyDefinition() {
                        public int getRequiredType() {
                            return def.getRequiredType();
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
                            return def.isMandatory();
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
            return ExtendedNodeType.this.getDeclaredChildNodeDefinitions();
        }
    }

}
