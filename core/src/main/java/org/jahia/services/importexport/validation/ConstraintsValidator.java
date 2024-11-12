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
package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRMultipleValueUtils;
import org.jahia.services.content.JCRValueFactoryImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.Patterns;
import org.xml.sax.Attributes;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Validator that checks if all mandatory property constraints are satisfied or not before the import
 * @author Serge Huber
 */
public class ConstraintsValidator implements ImportValidator {

    public final static String CONSTRAINT_SEPARATOR = "@@";

    private final static String COMMA_SPLIT_REGEX = "\\s*,\\s*";

    private Map<String, Set<String>> missingMandatoryProperties = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> missingMandatoryI18NProperties = new TreeMap<String, Set<String>>();
    private Map<String, String> otherConstraintViolations = new HashMap<>();
    private Map<String, String> parentType = new HashMap<>(); // Map<path, nodetype>
    private Map<String, Set<String>> parentMixins = new HashMap<>(); // Map<path, mixins>

    private String previousPath;

    @Override
    public ValidationResult getResult() {
        return new ConstraintsValidatorResult(missingMandatoryProperties, missingMandatoryI18NProperties, otherConstraintViolations);
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {

        // Clean up maps of processed path
        if (!StringUtils.startsWith(currentPath + "/", previousPath + "/")) {
            while (!StringUtils.startsWith(currentPath + "/", previousPath + "/") && !StringUtils.isEmpty(previousPath)) {
                parentType.remove(previousPath);
                parentMixins.remove(previousPath);
                previousPath = StringUtils.substringBeforeLast(previousPath, "/");
            }
        }

        previousPath = currentPath;

        String primaryType = atts.getValue(Constants.JCR_PRIMARYTYPE);

        String mixinTypes = atts.getValue(Constants.JCR_MIXINTYPES);
        Set<String> mixins = new HashSet<>();
        if (mixinTypes != null) {
            mixins.addAll(Arrays.asList(mixinTypes.split(COMMA_SPLIT_REGEX)));
        }

        // hold the primary type and mixins when processing translation nodes
        boolean isI18n = Constants.JAHIANT_TRANSLATION.equals(primaryType);
        if (isI18n) {
            currentPath = StringUtils.substringBeforeLast(currentPath, "/");
            if (parentMixins.get(currentPath) != null) {
                mixins.addAll(parentMixins.get(currentPath));
            }
            primaryType = parentType.get(currentPath);
        } else {
            parentType.put(currentPath, primaryType);
            parentMixins.put(currentPath, mixins);
        }

        checkTypeConstraints(primaryType, currentPath, atts, isI18n);

        for (String mixin : mixins) {
            checkTypeConstraints(mixin, currentPath, atts, isI18n);
        }
    }

    private void checkTypeConstraints(String type, String currentPath, Attributes atts, boolean i18nEntry) {
        try {
            ExtendedNodeType typeToCheck = NodeTypeRegistry.getInstance().getNodeType(type);
            ExtendedPropertyDefinition[] extendedPropertyDefinitions = typeToCheck.getPropertyDefinitions();
            for (ExtendedPropertyDefinition extendedPropertyDefinition : extendedPropertyDefinitions) {
                String propertyName = extendedPropertyDefinition.getName();
                String value = atts.getValue(propertyName);

                // Check if the property is mandatory
                boolean isMandatory = extendedPropertyDefinition.isMandatory() && !extendedPropertyDefinition.isAutoCreated() &&
                        !extendedPropertyDefinition.isProtected() && !extendedPropertyDefinition.hasDynamicDefaultValues();

                // check if the property def and the kind of entry (i18n or not) match
                boolean doCheck = extendedPropertyDefinition.isInternationalized() == i18nEntry;

                if (isMandatory && doCheck && !Constants.JCR_DATA.equals(propertyName)) {
                    if (value == null) {
                        Set<String> missingProperties = missingMandatoryProperties.get(currentPath);
                        if (missingProperties == null) {
                            missingProperties = new TreeSet<>();
                            missingMandatoryProperties.put(currentPath, missingProperties);
                        }
                        missingProperties.add(propertyName);
                    }
                }
                // Check other contraint
                if (value != null && doCheck) {
                    // process the value constraints for all property type but references
                    String propertyPath = currentPath + "/" + propertyName;
                    boolean hasConstraints = extendedPropertyDefinition.getValueConstraints() != null && extendedPropertyDefinition.getValueConstraints().length > 0;
                    boolean isNotReference = extendedPropertyDefinition.getRequiredType() != PropertyType.WEAKREFERENCE && extendedPropertyDefinition
                            .getRequiredType() != PropertyType.REFERENCE;
                    if (hasConstraints && isNotReference) {
                        // for i18n nodes we check on the parent node but for the mixins, they are directly on the i18n node
                        // in all cases we check first on the primary nodetype that can miss the property, then on the mixin that can set it, in this case the
                        // entry is removed from otherConstraintViolations
                        try {
                            int requiredType = typeToCheck.getPropertyDefinitionsAsMap().get(propertyName).getRequiredType();
                            if (!extendedPropertyDefinition.isMultiple()) {
                                if (!typeToCheck.canSetProperty(propertyName, JCRValueFactoryImpl.getInstance().createValue(value, requiredType))) {
                                    otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + value);
                                }
                            } else {
                                String[] valuesAsString = "".equals(value) ? new String[0] : Patterns.SPACE.split(value);
                                Value[] values = new Value[valuesAsString.length];
                                int i = 0;
                                for (String s : valuesAsString) {
                                    values[i++] = JCRValueFactoryImpl.getInstance().createValue(JCRMultipleValueUtils.decode(s), requiredType);
                                }
                                if (!typeToCheck.canSetProperty(propertyName, values)) {
                                    otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + Arrays.toString(valuesAsString));
                                }
                            }
                        } catch (ValueFormatException e) {
                            String v = extendedPropertyDefinition.isMultiple() ? Arrays.toString("".equals(value) ? new String[0] : Patterns.SPACE.split(value)) : value;
                            otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + v);
                        }
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            // MissingNodetypesValidator will return an error in this case
        }
    }
}
