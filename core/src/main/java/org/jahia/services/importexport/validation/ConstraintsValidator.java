/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * http://www.jahia.com
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ==================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);

        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        Set<String> mixins = new HashSet<>();
        if (m != null) {
            mixins.addAll(Arrays.asList(m.split(COMMA_SPLIT_REGEX)));
        }

        // hold the primary type and mixins when processing translation nodes
        boolean isI18n = Constants.JAHIANT_TRANSLATION.equals(pt);
        if (isI18n) {
            currentPath = StringUtils.substringBeforeLast(currentPath, "/");
            if (parentMixins.get(currentPath) != null) {
                mixins.addAll(parentMixins.get(currentPath));
            }
            pt = parentType.get(currentPath);
        } else {
            parentType.put(currentPath, pt);
            parentMixins.put(currentPath, mixins);
        }

        checkTypeConstraints( pt, currentPath, atts, isI18n);

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
                        if (!extendedPropertyDefinition.isMultiple()) {
                            if (!typeToCheck.canSetProperty(propertyName, JCRValueFactoryImpl.getInstance().createValue(value))) {
                                otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + value);
                            }
                        } else {
                            String[] valuesAsString = "".equals(value) ? new String[0] : Patterns.SPACE.split(value);
                            Value[] values = new Value[valuesAsString.length];
                            int i = 0;
                            for (String s : valuesAsString) {
                                values[i++] = JCRValueFactoryImpl.getInstance().createValue(JCRMultipleValueUtils.decode(s));
                            }
                            if (!typeToCheck.canSetProperty(propertyName, values)) {
                                otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + Arrays.toString(valuesAsString));
                            }
                        }
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            // MissingNodetypesValidator will return an error in this case
        }
    }
}
