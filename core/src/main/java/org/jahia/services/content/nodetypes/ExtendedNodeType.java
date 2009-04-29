/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.exceptions.JahiaException;
import org.jahia.api.Constants;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.Value;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:02:22
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedNodeType implements NodeType {
    
    private static final transient Logger logger = Logger.getLogger(ExtendedNodeType.class);
    
    private NodeTypeRegistry registry;
    private String systemId;
    private List<ExtendedItemDefinition> items = new ArrayList<ExtendedItemDefinition>();
    private List<String> groupedItems;

    private Map<String, ExtendedNodeDefinition> nodes = new ListOrderedMap();
    private Map<String, ExtendedPropertyDefinition> properties = new ListOrderedMap();

    private Name name;
    private boolean isAbstract;
    private boolean isMixin;
    private boolean hasOrderableChildNodes;
    private String primaryItemName;
    private ExtendedNodeType[] declaredSupertypes = new ExtendedNodeType[0];
    private List<ExtendedNodeType> declaredSubtypes = new ArrayList<ExtendedNodeType>();
    private String validator;

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

    public Name getNameObject() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
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

    public String getPrimaryItemName() {
        return primaryItemName;
    }

    public void setPrimaryItemName(String primaryItemName) {
        this.primaryItemName = primaryItemName;
    }


    public ExtendedNodeType[] getSupertypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        boolean primaryFound = false;
        for (int i = 0; i < declaredSupertypes.length; i++) {
            ExtendedNodeType s = declaredSupertypes[i];
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
        for (int i = 0; i < declaredSupertypes.length; i++) {
            ExtendedNodeType s = declaredSupertypes[i];
            if (s != null && !s.isMixin()) {
                l.add(s);
                l.addAll(Arrays.asList(s.getPrimarySupertypes()));
                primaryFound = true;
            }
        }
        if (!primaryFound && !Constants.NT_BASE.equals(name) && !isMixin) {
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
        this.declaredSupertypes = new ExtendedNodeType[declaredSupertypes.length];
        for (int i = 0; i < declaredSupertypes.length; i++) {
            try {
                this.declaredSupertypes[i] = registry.getNodeType(declaredSupertypes[i]);
                this.declaredSupertypes[i].addSubType(this);
            } catch (NoSuchNodeTypeException e) {
                logger.error("No such supertype for "+getName(),e);
            }
        }
    }

    void addSubType(ExtendedNodeType subType) {
        declaredSubtypes.add(subType);
    }

    public ExtendedNodeType[] getSubtypes() {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = declaredSubtypes.iterator(); iterator.hasNext();) {
            ExtendedNodeType s =  iterator.next();
            l.add(s);
            l.addAll(Arrays.asList(s.getSubtypes()));
        }
        return l.toArray(new ExtendedNodeType[l.size()]);
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
        for (int i = 0; i < declaredSupertypes.length; i++) {
            ExtendedNodeType s = declaredSupertypes[i];
            if (s.isNodeType(typeName)) {
                return true;
            }
        }
        return false;
    }

    public List<ExtendedItemDefinition>  getItems() {
        List<ExtendedItemDefinition> l = new ArrayList<ExtendedItemDefinition>();

        for (int i = 0; i < getSupertypes().length; i++) {
            ExtendedNodeType nodeType = getSupertypes()[i];
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
        Map<String, ExtendedPropertyDefinition> l = new ListOrderedMap();

        for (int i = 0; i < getSupertypes().length; i++) {
            ExtendedNodeType nodeType = getSupertypes()[i];
            Map<String, ExtendedPropertyDefinition> c = nodeType.getDeclaredPropertyDefinitionsAsMap();
            l.putAll(c);
        }

        l.putAll(properties);

        return l;
    }

    public ExtendedPropertyDefinition[] getPropertyDefinitions() {
        Map<String, ExtendedPropertyDefinition> l = getPropertyDefinitionsAsMap();
        return l.values().toArray(new ExtendedPropertyDefinition[l.size()]);
    }

    public Map<String, ExtendedPropertyDefinition> getDeclaredPropertyDefinitionsAsMap() {
        return properties;
    }

    public ExtendedPropertyDefinition[] getDeclaredPropertyDefinitions() {
        return properties.values().toArray(new ExtendedPropertyDefinition[properties.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getChildNodeDefinitionsAsMap() {
        Map<String, ExtendedNodeDefinition> l = new ListOrderedMap();
        for (int i = 0; i < getSupertypes().length; i++) {
            ExtendedNodeType nodeType = getSupertypes()[i];
            Map<String, ExtendedNodeDefinition> c = nodeType.getDeclaredChildNodeDefinitionsAsMap();
            l.putAll(c);
        }

        l.putAll(nodes);

        return l;
    }

    public ExtendedNodeDefinition[] getChildNodeDefinitions() {
        Map<String, ExtendedNodeDefinition> l = getChildNodeDefinitionsAsMap();
        return l.values().toArray(new ExtendedNodeDefinition[l.size()]);
    }

    public Map<String, ExtendedNodeDefinition> getDeclaredChildNodeDefinitionsAsMap() {
        return nodes;
    }

    public ExtendedNodeDefinition[] getDeclaredChildNodeDefinitions() {
        return nodes.values().toArray(new ExtendedNodeDefinition[nodes.size()]);
    }

    public List<String> getGroupedItems() {
        return groupedItems;
    }

    public void setGroupedItems(List<String> groupedItems) {
        this.groupedItems = groupedItems;
    }

    public boolean canSetProperty(String s, Value value) {
        return false;
    }

    public boolean canSetProperty(String s, Value[] values) {
        return false;
    }

    public boolean canAddChildNode(String s) {
        return false;
    }

    public boolean canAddChildNode(String s, String s1) {
        return false;
    }

    public boolean canRemoveItem(String s) {
        return false;
    }

    void setPropertyDefinition(String name, ExtendedPropertyDefinition p) {
        properties.put(name, p);
        items.add(p);
    }

    public ExtendedPropertyDefinition getPropertyDefinition(String name) {
        return properties.get(name);
    }

    void setNodeDefinition(String name, ExtendedNodeDefinition p) {
        nodes.put(name, p);
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

    public String getResourceBundleId() {
        if (getSystemId().startsWith("system-")) {
            return "JahiaTypesResources";
        }
        JahiaTemplatesPackage pkg = null;
        try {
            pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(getSystemId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pkg == null) {
            return "JahiaTypesResources";
        } else {
            return pkg.getResourceBundleName();
        }
    }

    public String getResourceBundleMarker() {
        String key = getName().replace(':', '_');
        return ResourceBundleMarker.drawMarker(getResourceBundleId(), key, getName().replace(':', '_'));
    }

    public String getLabel(Locale locale) {
        try {
            return ResourceBundleMarker.parseMarkerValue(getResourceBundleMarker()).getValue(locale);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return getName().replace(':','_');
    }
}
