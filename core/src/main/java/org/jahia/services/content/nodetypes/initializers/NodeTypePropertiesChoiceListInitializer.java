package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.Patterns;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * ChoiceListInitializer implementation that generates a list of node types properties.
 * Those properties can be filtered on node types, name pattern (regex) and/or property conditions (isHidden, isFacetable, etc.).
 */
public class NodeTypePropertiesChoiceListInitializer implements ChoiceListInitializer {
    public static final Pattern EQUALS = Pattern.compile("=", Pattern.LITERAL);

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        String[] confEntries = StringUtils.isNotBlank(param) ? Patterns.SEMICOLON.split(param) : new String[0];
        ChoiceListBuilder builder = new ChoiceListBuilder(locale);

        String dependentProperties = epd.getSelectorOptions().get("dependentProperties");
        if (dependentProperties != null) {
            handleDependentProperties(context, dependentProperties, builder);
        }

        for (String confEntry : confEntries) {
            String[] confEntryParts = EQUALS.split(confEntry);
            if (confEntryParts.length == 2) {
                String key = confEntryParts[0];
                String value = confEntryParts[1];
                switch (key) {
                    case "namePattern":
                        builder.addNamePattern(value);
                        break;
                    case "nodeTypes":
                        builder.addNodeTypes(value);
                        break;
                    default:
                        builder.addPropertyCondition(key, value);
                }
            } else {
                throw new IllegalArgumentException("Invalid configuration entry: " + confEntry);
            }
        }
        return builder.build();
    }

    /**
     * Handles dependent properties from the context and adds their types to the builder.
     *
     * @param context             The context map
     * @param dependentProperties Comma-separated property names
     * @param builder             The builder to add types to
     */
    private static void handleDependentProperties(Map<String, Object> context, String dependentProperties, ChoiceListBuilder builder) {
        String[] dependentPropertyNames = Patterns.COMMA.split(dependentProperties);
        for (String dependentPropertyName : dependentPropertyNames) {
            if (context.containsKey(dependentPropertyName)) {
                // creation/edition: the dependent property is passed in the context (not persisted yet in the JCR)
                Object value = context.get(dependentPropertyName);
                if (value instanceof Collection) {
                    ((Collection<String>) value).forEach(builder::addNodeTypes);
                } else if (value instanceof String) {
                    builder.addNodeTypes((String) value);
                } else {
                    throw new IllegalStateException("Invalid context type for dependent property " + dependentPropertyName + ": " + value);
                }
            } else {
                // reading: get the dependent property's value directly from the property of the context node
                JCRNodeWrapper contextNode = (JCRNodeWrapper) context.get("contextNode");
                if (contextNode != null) {
                    try {
                        if (contextNode.hasProperty(dependentPropertyName)) {
                            JCRPropertyWrapper property = contextNode.getProperty(dependentPropertyName);
                            // only support single-value and multi-value string properties
                            if (property.getType() != PropertyType.STRING) {
                                throw new IllegalStateException("Dependent property " + dependentPropertyName + " must be of type string");
                            }
                            if (property.isMultiple()) {
                                for (Value value : property.getValues()) {
                                    builder.addNodeTypes(value.getString());
                                }
                            } else {
                                builder.addNodeTypes(property.getValue().getString());
                            }
                        }
                    } catch (RepositoryException e) {
                        throw new IllegalStateException("Failed to get dependent property " + dependentPropertyName + " from context", e);
                    }
                }
            }
        }
    }

    /**
     * Builder class for constructing filtered property choice lists.
     */
    private static class ChoiceListBuilder {
        private final Locale locale;
        private final List<ExtendedNodeType> nodeTypes = new ArrayList<>();
        private final List<Pattern> namePatterns = new ArrayList<>();
        private final Map<String, Boolean> propertyConditionsValues = new HashMap<>();
        private static final Map<String, Predicate<ExtendedPropertyDefinition>> propertyNameToPredicate = createPropertyNameToPredicate();

        /**
         * Initializes the builder with the given locale.
         *
         * @param locale The locale for display names
         */
        private ChoiceListBuilder(Locale locale) {
            this.locale = locale;
        }

        /**
         * Adds node types by their names (comma-separated).
         *
         * @param typesValues Comma-separated node type names
         */
        private void addNodeTypes(String typesValues) {
            String[] types = Patterns.COMMA.split(typesValues);
            for (String typeName : types) {
                try {
                    ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(typeName.trim());
                    nodeTypes.add(nodeType);
                } catch (NoSuchNodeTypeException e) {
                    throw new IllegalArgumentException("Unknown node type " + typeName, e);
                }
            }
        }

        private void addNamePattern(String namesRegexp) {
            this.namePatterns.add(Pattern.compile(namesRegexp));
        }

        /**
         * Adds a property condition to filter properties.
         *
         * @param propertyName  The property name (e.g. isFacetable)
         * @param propertyValue The expected value (true/false)
         */
        private void addPropertyCondition(String propertyName, String propertyValue) {
            if (propertyNameToPredicate.containsKey(propertyName)) {
                boolean value = Boolean.parseBoolean(propertyValue.trim());
                propertyConditionsValues.put(propertyName, value);
            } else {
                throw new IllegalArgumentException("Unknown property condition " + propertyName);
            }
        }

        /**
         * Builds the list of choice values based on node types and property conditions.
         *
         * @return List of ChoiceListValue objects
         */
        private List<ChoiceListValue> build() {
            List<ChoiceListValue> choiceListValues = new ArrayList<>();
            for (ExtendedNodeType nodeType : nodeTypes) {
                for (ExtendedPropertyDefinition propertyDefinition : nodeType.getPropertyDefinitions()) {
                    if (isPropertyAllowed(propertyDefinition)) {
                        choiceListValues.add(new ChoiceListValue(getDisplayName(nodeType, propertyDefinition), propertyDefinition.getName()));
                    }
                }
            }
            return choiceListValues;
        }

        /**
         * Checks if a property definition matches all property conditions, including the naming patterns.
         *
         * @param propertyDefinition The property definition
         * @return <code>true</code> if allowed, <code>false</code> otherwise
         */
        private boolean isPropertyAllowed(ExtendedPropertyDefinition propertyDefinition) {
            for (Map.Entry<String, Boolean> propertyConditionValue : propertyConditionsValues.entrySet()) {
                if (propertyNameToPredicate.get(propertyConditionValue.getKey()).test(propertyDefinition) != propertyConditionValue.getValue()) {
                    return false;
                }
            }
            for (Pattern pattern : namePatterns) {
                if (!pattern.matcher(propertyDefinition.getName()).matches()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Gets the display name for a property definition.
         *
         * @param nodeType           The node type
         * @param propertyDefinition The property definition
         * @return the display name
         */
        private String getDisplayName(ExtendedNodeType nodeType, ExtendedPropertyDefinition propertyDefinition) {
            return propertyDefinition.getLabel(locale) + " (" + nodeType.getName() + "." + propertyDefinition.getName() + ")";
        }

        /**
         * Creates the property name to predicate mapping.
         *
         * @return Map of property names to predicates
         */
        private static Map<String, Predicate<ExtendedPropertyDefinition>> createPropertyNameToPredicate() {
            Map<String, Predicate<ExtendedPropertyDefinition>> map = new HashMap<>();
            map.put("isAutoCreated", ExtendedPropertyDefinition::isAutoCreated);
            map.put("isContentItem", ExtendedPropertyDefinition::isContentItem);
            map.put("isFacetable", ExtendedPropertyDefinition::isFacetable);
            map.put("isFullTextSearchable", ExtendedPropertyDefinition::isFullTextSearchable);
            map.put("isHidden", ExtendedPropertyDefinition::isHidden);
            map.put("isHierarchical", ExtendedPropertyDefinition::isHierarchical);
            map.put("isInternationalized", ExtendedPropertyDefinition::isInternationalized);
            map.put("isMandatory", ExtendedPropertyDefinition::isMandatory);
            map.put("isMultiple", ExtendedPropertyDefinition::isMultiple);
            map.put("isNode", ExtendedPropertyDefinition::isNode);
            map.put("isOverride", ExtendedPropertyDefinition::isOverride);
            map.put("isProtected", ExtendedPropertyDefinition::isProtected);
            map.put("isQueryOrderable", ExtendedPropertyDefinition::isQueryOrderable);
            map.put("isUnstructured", ExtendedPropertyDefinition::isUnstructured);
            return map;
        }
    }
}
