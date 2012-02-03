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

package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.utils.i18n.ResourceBundleMarker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  User: toto Date: Oct 29, 2009 Time: 7:09:03 PM
 */
public class DefinitionsMapping {
    private static Logger logger = LoggerFactory.getLogger(LegacyImportHandler.class);

    @SuppressWarnings("unchecked")
    public DefinitionsMapping() {
        metadataProperties = (Map<String, PropertyMapping>) SpringContextSingleton
                .getBean("ImportDefinitionsMappingMetadata");
    }

    String WORD = "([\\w:#-]+)";
    String WORD_WITH_DOTS = "([\\w:#-.]+)";
    String WORD_WITH_DOTS_AND_SLASHES = "([\\w:#-.//]+)";
    String WORD_WITH_DOTS_SPACES_AND_BRACKETS = "([\\w:#-.\\(\\)\\s//]+)";
    String WS = "\\s*";
    String WS_OR_COMMA = "[\\s,]*";
    String NODETYPE = "\\[" + WORD + "\\]";
    String EQ = WS + "=" + WS;
    String NODETYPE_MAPPING = NODETYPE + EQ + WORD;
    String NODE = WORD_WITH_DOTS_AND_SLASHES + WS + "(?:\\(" + WS + WORD + WS + "\\))?";
    String NODE_MAPPING = WS + "\\+" + WS + WORD + EQ + NODE;
    String PROPERTY_MAPPING = WS + "-" + WS + WORD + EQ + "(" + WORD + "\\|" + ")?" + WORD_WITH_DOTS_AND_SLASHES;
    String VALUE_MAPPING = WS + WORD_WITH_DOTS_SPACES_AND_BRACKETS + EQ + WORD_WITH_DOTS_SPACES_AND_BRACKETS;
    String PROPS = "(?:\\[((?:" + WS_OR_COMMA + "(" + WORD + "\\|" + ")?" + WORD_WITH_DOTS_AND_SLASHES + EQ + WORD_WITH_DOTS + WS + ")*)\\])";
    String ADD_NODE = "\\{" + WS + "addNode" + WS + NODE + WS + PROPS + "?" + WS + "\\}";
    String ADD_MIXIN = "\\{" + WS + "addMixin" + WS + WORD + WS + "\\}";
    String SET_PROPERTY = "\\{" + WS + "setProperty" + WS + PROPS + "?" + WS + "\\}";

    Pattern NODETYPE_PATTERN = Pattern.compile(NODETYPE_MAPPING);
    Pattern NODE_PATTERN = Pattern.compile(NODE_MAPPING);
    Pattern PROPERTY_PATTERN = Pattern.compile(PROPERTY_MAPPING);
    Pattern VALUE_PATTERN = Pattern.compile(VALUE_MAPPING);
    Pattern ADD_NODE_PATTERN = Pattern.compile(ADD_NODE);
    Pattern ADD_MIXIN_PATTERN = Pattern.compile(ADD_MIXIN);
    Pattern SET_PROPERTY_PATTERN = Pattern.compile(SET_PROPERTY);

    private Map<String, TypeMapping> types = new HashMap<String, TypeMapping>();
    private Map<String, PropertyMapping> metadataProperties;

    public void load(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        TypeMapping typeMapping = null;
        NodeMapping nodeMapping = null;
        PropertyMapping propMapping = null;
        Mapping currentMapping = null;
        for (String line; (line = br.readLine()) != null;) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 0) {
                Matcher matcher;
                if ((matcher = NODETYPE_PATTERN.matcher(line)).matches()) {
                    typeMapping = new TypeMapping(matcher.group(1), matcher.group(2));
                    currentMapping = typeMapping;
                    types.put(typeMapping.originalName, typeMapping);
                } else if ((matcher = NODE_PATTERN.matcher(line)).matches()) {
                    String originalName = matcher.group(1);
                    String nodeName = matcher.group(2);
                    String nodeType = matcher.group(3);
                    nodeMapping = new NodeMapping(originalName, nodeName, nodeType);
                    typeMapping.addNodeMapping(originalName, nodeMapping);
                    currentMapping = nodeMapping;
                } else if ((matcher = PROPERTY_PATTERN.matcher(line)).matches()) {
                    String mixinType = matcher.group(3);
                    propMapping = new PropertyMapping(matcher.group(1), (!StringUtils
                            .isEmpty(mixinType) ? mixinType + "|" : "")
                            + matcher.group(4));
                    typeMapping.addPropertyMapping(propMapping.originalName, propMapping);
                    currentMapping = propMapping;
                } else if ((matcher = VALUE_PATTERN.matcher(line)).matches()) {
                    ValueMapping vm = new ValueMapping(StringUtils.trim(matcher.group(1)),
                            StringUtils.trim(matcher.group(2)));
                    propMapping.addValueMapping(vm.originalName, vm);
                } else if ((matcher = ADD_NODE_PATTERN.matcher(line)).matches()) {
                    String nodeName = matcher.group(1);
                    String nodeType = matcher.group(2);
                    Map<String, String> properties = createPropertyValueMap(matcher.group(3));
                    currentMapping.addAction(new AddNode(nodeName, nodeType,
                            properties));
                } else if ((matcher = ADD_MIXIN_PATTERN.matcher(line)).matches()) {
                    currentMapping.addAction(new AddMixin(matcher.group(1)));
                } else if ((matcher = SET_PROPERTY_PATTERN.matcher(line)).matches()) {
                    currentMapping.addAction(new SetProperties(createPropertyValueMap(matcher
                            .group(1))));
                } else {
                    logger.error("Syntax error!! The following line does not match any valid syntax pattern for content mappings: "
                            + line);
                }
            }
        }
    }

    private Map<String, String> createPropertyValueMap(String commaSeperatedPropValues) {
        Map<String, String> properties = new HashMap<String, String>();
        if (commaSeperatedPropValues != null) {
            for (String propAndValue : StringUtils.split(commaSeperatedPropValues, ",")) {
                properties.put(StringUtils.substringBefore(propAndValue, "=").trim(), StringUtils
                        .substringAfter(propAndValue, "=").trim());
            }
        }
        return properties;
    }

    public String getMappedType(ExtendedNodeType type) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            if (type.isNodeType("jnt:box")) {
                return "jnt:contentList";
            }
            return type.getName();
        } else if ("jnt:box".equals(s.getNewName())) {
            return "jnt:contentList";
        }
        return s.getNewName();
    }

    public List<String> getMappedNodesForType(ExtendedNodeType type, boolean oldName) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        return oldName ? new ArrayList<String>(s.nodes.keySet()) : s.getNodeNames();
    }

    public String getMappedItem(ExtendedNodeType type, String name) {
        Mapping n = getItemMapping(type, name);
        if (n == null) {
            for (ExtendedNodeType superType : type.getSupertypes()) {
                n = getItemMapping(superType, name);
                if (n != null) {
                    break;
                }
            }
        }
        if (n == null) {
            return name;
        }
        return n.getNewName();
    }

    private Mapping getItemMapping(ExtendedNodeType type, String name) {
        TypeMapping s = types.get(type.getName());
        Mapping n = null;
        if (s != null) {
            n = s.nodes.get(name);
            if (n == null) {
                n = s.properties.get(name);
            }
        }

        return n;
    }


    public String getMappedNode(ExtendedNodeType type, String name) {
        NodeMapping n = getNodeMapping(type, name);
        if (n == null) {
            for (ExtendedNodeType superType : type.getSupertypes()) {
                n = getNodeMapping(superType, name);
                if (n != null) {
                    break;
                }
            }
        }
        if (n == null) {
            return name;
        }
        return n.getNewName();
    }

    private NodeMapping getNodeMapping(ExtendedNodeType type, String name) {
        TypeMapping s = types.get(type.getName());
        NodeMapping n = null;
        if (s != null) {
            n = s.nodes.get(name);
        }

        return n;
    }

    public String getMappedProperty(ExtendedNodeType type, String name) {
        PropertyMapping n = getPropertyMapping(type, name);
        if (n == null) {
            for (ExtendedNodeType superType : type.getSupertypes()) {
                n = getPropertyMapping(superType, name);
                if (n != null) {
                    break;
                }
            }
        }
        if (n == null) {
            return name;
        }
        return n.getNewName();
    }

    private PropertyMapping getPropertyMapping(ExtendedNodeType type, String name) {
        TypeMapping s = types.get(type.getName());
        PropertyMapping n = null;
        if (s != null) {
            n = s.properties.get(name);
        }

        return n;
    }

    public String getMappedMetadataProperty(String name) {
        PropertyMapping n = metadataProperties.get(name);
        return n != null ? n.getNewName() : name;
    }

    public String getMappedPropertyValue(ExtendedNodeType type, String name, String value) {
        PropertyMapping n = getPropertyMapping(type, name);
        if (n == null) {
            for (ExtendedNodeType superType : type.getSupertypes()) {
                n = getPropertyMapping(superType, name);
                if (n != null) {
                    break;
                }
            }
        }
        if (n == null) {
            return value;
        }
        ValueMapping v = n.values.get(value);
        if (v == null) {
            ResourceBundleMarker rbm = ResourceBundleMarker.parseMarkerValue(value);
            if (rbm != null) {
                v = n.values.get("resourceKey(" + rbm.getResourceKey() + ")");
            }
        }
        if (v == null) {
            return value;
        }
        return v.getNewName();
    }

    public List<Action> getActions(ExtendedNodeType type) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        return s.getActions();
    }

    public List<Action> getActions(ExtendedNodeType type, String name) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        Mapping n = s.properties.get(name);
        if (n == null) {
            n = s.nodes.get(name);
        }
        if (n == null) {
            return Collections.emptyList();
        }
        return n.getActions();
    }

    public List<Action> getActions(ExtendedNodeType type, String name, String value) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        PropertyMapping n = s.properties.get(name);
        if (n == null) {
            return Collections.emptyList();
        }
        ValueMapping v = n.values.get(value);
        if (v == null) {
            return Collections.emptyList();
        }
        return v.getActions();
    }

    class Action {

    }

    class AddNode extends Action {
        String name;
        String nodeType;
        Map<String, String> properties = new HashMap<String, String>();

        AddNode(String name, String nodeType, Map<String, String> properties) {
            this.name = name;
            this.nodeType = nodeType;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public String getNodeType() {
            return nodeType;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    class AddMixin extends Action {
        String nodeType;

        AddMixin(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getNodeType() {
            return nodeType;
        }
    }

    class SetProperties extends Action {
        Map<String, String> properties = new HashMap<String, String>();

        SetProperties(Map<String, String> properties) {
            this.properties = properties;
        }

        public Map<String, String> getProperties() {
            return properties;
        }
    }

    public static class Mapping {
        List<Action> actions = new ArrayList<Action>();
        String originalName;
        String newName;

        public boolean addAction(Action action) {
            boolean result = actions.add(action);
            return result;
        }

        public List<Action> getActions() {
            return actions;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getNewName() {
            return newName;
        }
    }

    class TypeMapping extends Mapping {
        Map<String, NodeMapping> nodes = new LinkedHashMap<String, NodeMapping>();
        List<String> nodeNames = new ArrayList<String>();
        Map<String, PropertyMapping> properties = new HashMap<String, PropertyMapping>();

        TypeMapping(String originalType, String newType) {
            this.originalName = originalType;
            this.newName = newType;
        }

        public NodeMapping addNodeMapping (String originalName, NodeMapping mapping) {
            NodeMapping result = nodes.put(originalName, mapping);
            nodeNames.add(StringUtils.contains(mapping.getNewName(), "/") ? StringUtils
                    .substringAfterLast(mapping.getNewName(), "/") : mapping.getNewName());
            return result;
        }

        public PropertyMapping addPropertyMapping (String originalName, PropertyMapping mapping) {
            PropertyMapping result = properties.put(originalName, mapping);
            return result;
        }

        public List<String> getNodeNames() {
            return nodeNames;
        }
    }

    class NodeMapping extends Mapping {
        String newType;

        NodeMapping(String originalName, String newName, String newType) {
            this.originalName = originalName;
            this.newName = newName;
            this.newType = newType;
        }

        public String getNewType() {
            return newType;
        }

        public String getNewName() {
            return (newType != null ? newType + "|" : "") + newName ;
        }
    }

    public static class PropertyMapping extends Mapping {
        Map<String, ValueMapping> values = new HashMap<String, ValueMapping>();

        public PropertyMapping(String originalName, String newName) {
            this.originalName = originalName;
            this.newName = newName;
        }

        public ValueMapping addValueMapping (String originalName, ValueMapping mapping) {
            ValueMapping result = values.put(originalName, mapping);
            return result;
        }
    }

    class ValueMapping extends Mapping {
        ValueMapping(String originalValue, String newValue) {
            this.originalName = originalValue;
            this.newName = newValue;
        }
    }

}