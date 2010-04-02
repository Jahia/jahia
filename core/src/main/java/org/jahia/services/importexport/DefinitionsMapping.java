package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA. User: toto Date: Oct 29, 2009 Time: 7:09:03 PM To change this template use File | Settings | File Templates.
 */
public class DefinitionsMapping {
    private static Logger logger = Logger.getLogger(LegacyImportHandler.class);

    public DefinitionsMapping() {
        metadataProperties.put("jahia:defaultCategory", new PropertyMapping(
                "jahia:defaultCategory", "j:defaultCategory", "jmix:categorized"));
        metadataProperties.put("jahia:keywords", new PropertyMapping("jahia:keywords",
                "j:keywords", "jmix:keywords"));
        metadataProperties.put("jahia:createdBy", new PropertyMapping("jahia:createdBy",
                "jcr:createdBy", "mix:created"));
        metadataProperties.put("jahia:lastModifiedBy", new PropertyMapping("jahia:lastModifiedBy",
                "jcr:lastModifiedBy", "mix:lastModified"));
        metadataProperties.put("jahia:lastPublishingDate", new PropertyMapping(
                "jahia:lastPublishingDate", "j:lastPublished", "jmix:lastPublished"));
        metadataProperties.put("jahia:lastPublisher", new PropertyMapping("jahia:lastPublisher",
                "j:lastPublishedBy", "jmix:lastPublished"));
    }

    String WORD = "([\\w:#]+)";
    String WORD_WITH_DOT = "([\\w:#.]+)";    
    String WS = "\\s*";
    String WS_OR_COMMA = "[\\s,]*";
    String NODETYPE = "\\[" + WORD + "\\]";
    String EQ = WS + "=" + WS;
    String NODETYPE_MAPPING = NODETYPE + EQ + WORD;
    String NODE = WORD + WS + "\\(" + WS + WORD + WS + "(?:," + WS + WORD + WS + ")?" + WS + "\\)";
    String NODE_MAPPING = WS + "\\+" + WS + WORD + EQ + NODE + "(\\." + NODE + ")*";
    String PROPERTY_MAPPING = WS + "-" + WS + WORD + EQ + "(" + WORD + "\\." + ")?" + WORD;
    String VALUE_MAPPING = WS + WORD_WITH_DOT + EQ + WORD_WITH_DOT;
    String PROPS = "(?:\\[((?:" + WS_OR_COMMA + "(" + WORD + "\\." + ")?" + WORD + EQ + WORD + WS + ")*)\\])";
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
    private Map<String, PropertyMapping> metadataProperties = new HashMap<String, PropertyMapping>();

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
                    nodeMapping = new NodeMapping(matcher.group(1), matcher.group(2), matcher
                            .group(3));
                    int i = 5;
                    while (i < matcher.groupCount()) {
                        nodeMapping.next = new NodeMapping("", matcher.group(i), matcher
                                .group(i + 1));
                        nodeMapping = nodeMapping.next;
                        i += 3;
                    }
                    typeMapping.nodes.put(nodeMapping.originalName, nodeMapping);
                    currentMapping = nodeMapping;
                } else if ((matcher = PROPERTY_PATTERN.matcher(line)).matches()) {
                    propMapping = new PropertyMapping(matcher.group(1), matcher.group(4), matcher
                            .group(3));
                    typeMapping.properties.put(propMapping.originalName, propMapping);
                    currentMapping = propMapping;
                } else if ((matcher = VALUE_PATTERN.matcher(line)).matches()) {
                    ValueMapping vm = new ValueMapping(matcher.group(1), matcher.group(2));
                    propMapping.values.put(vm.originalName, vm);
                } else if ((matcher = ADD_NODE_PATTERN.matcher(line)).matches()) {
                    String nodeName = matcher.group(1);
                    String parentNodeName = matcher.group(2);
                    String nodeType = matcher.group(3);
                    if (nodeType == null) {
                        nodeType = parentNodeName;
                        parentNodeName = null;
                    }
                    Map<String, String> properties = createPropertyValueMap(matcher.group(4));
                    currentMapping.actions.add(new AddNode(nodeName, nodeType, parentNodeName,
                            properties));
                } else if ((matcher = ADD_MIXIN_PATTERN.matcher(line)).matches()) {
                    currentMapping.actions.add(new AddMixin(matcher.group(1)));
                } else if ((matcher = SET_PROPERTY_PATTERN.matcher(line)).matches()) {
                    currentMapping.actions.add(new SetProperties(createPropertyValueMap(matcher
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
                return "#box";
            }
            return type.getName();
        }
        return s.newName;
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
        return n.newName;
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
        return n.newName;
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
        if (n == null) {
            return name;
        }
        return n.newName;
    }

    public String getMappedPropertyValue(ExtendedNodeType type, String name, String value) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return value;
        }
        PropertyMapping n = s.properties.get(name);
        if (n == null) {
            return value;
        }
        ValueMapping v = n.values.get(value);
        if (v == null) {
            return value;
        }
        return v.newName;
    }

    public List<Action> getActions(ExtendedNodeType type) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        return s.actions;
    }

    public List<Action> getActions(ExtendedNodeType type, String name) {
        TypeMapping s = types.get(type.getName());
        if (s == null) {
            return Collections.emptyList();
        }
        PropertyMapping n = s.properties.get(name);
        if (n == null) {
            return Collections.emptyList();
        }
        return n.actions;
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
        return v.actions;
    }

    class Action {

    }

    class AddNode extends Action {
        String name;
        String nodeType;
        String parentNodeName;
        Map<String, String> properties = new HashMap<String, String>();

        AddNode(String name, String nodeType, String parentNodeName, Map<String, String> properties) {
            this.name = name;
            this.nodeType = nodeType;
            this.parentNodeName = parentNodeName;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public String getNodeType() {
            return nodeType;
        }

        public String getParentNodeName() {
            return parentNodeName;
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

    class Mapping {
        List<Action> actions = new ArrayList<Action>();
        String originalName;
        String newName;
    }

    class TypeMapping extends Mapping {
        Map<String, NodeMapping> nodes = new HashMap<String, NodeMapping>();
        Map<String, PropertyMapping> properties = new HashMap<String, PropertyMapping>();

        TypeMapping(String originalType, String newType) {
            this.originalName = originalType;
            this.newName = newType;
        }
    }

    class NodeMapping extends Mapping {
        String newType;
        NodeMapping next;

        NodeMapping(String originalName, String newName, String newType) {
            this.originalName = originalName;
            this.newName = newName;
            this.newType = newType;
        }
    }

    class PropertyMapping extends Mapping {
        String mixinType;
        Map<String, ValueMapping> values = new HashMap<String, ValueMapping>();

        PropertyMapping(String originalName, String newName, String mixinType) {
            this.originalName = originalName;
            this.newName = newName;
            this.mixinType = mixinType;
        }
    }

    class ValueMapping extends Mapping {
        ValueMapping(String originalValue, String newValue) {
            this.originalName = originalValue;
            this.newName = newValue;
        }
    }

}