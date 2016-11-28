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

    private Map<String, Set<String>> missingMandatoryProperties = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> missingMandatoryI18NProperties = new TreeMap<String, Set<String>>();
    private Map<String, String> otherConstraintViolations = new HashMap<>();
    private String parentType;

    @Override
    public ValidationResult getResult() {
        return new ConstraintsValidatorResult(missingMandatoryProperties, missingMandatoryI18NProperties, otherConstraintViolations);
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {
        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);
        // hold the primary type when processing translation nodes
        if (!Constants.JAHIANT_TRANSLATION.equals(pt)) {
            parentType = pt;
        }
        if (pt != null) {
            checkTypeConstraints(pt, currentPath, false, atts);
        }
        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        if (m != null) {
            StringTokenizer st = new StringTokenizer(m, " ,");
            while (st.hasMoreTokens()) {
                String mixin = st.nextToken();
                checkTypeConstraints(mixin, currentPath, true, atts);
            }
        }
    }

    private void checkTypeConstraints(String type, String currentPath, boolean mixin, Attributes atts) {
        try {
            ExtendedNodeType extendedNodeType = NodeTypeRegistry.getInstance().getNodeType(type);
            ExtendedNodeType extendedParentType = NodeTypeRegistry.getInstance().getNodeType(parentType);
            ExtendedPropertyDefinition[] extendedPropertyDefinitions = extendedNodeType.getPropertyDefinitions();
            if (Constants.JAHIANT_TRANSLATION.equals(extendedNodeType.getName())) {
                extendedPropertyDefinitions = extendedParentType.getPropertyDefinitions();
                // let's retrieve the missing I18N properties from the parent, and remove them if we find at least
                // a property value that matches
                String parentPath = StringUtils.substringBeforeLast(currentPath, "/");
                Set<String> parentMissingMandatoryI18NProperties = missingMandatoryI18NProperties.get(parentPath);
                if (parentMissingMandatoryI18NProperties != null && parentMissingMandatoryI18NProperties.size() > 0) {
                    boolean valuesFound = false;
                    for (int i = 0; i < atts.getLength(); i++) {
                        String attValue = atts.getValue(i);
                        String attName = atts.getQName(i);
                        if (attValue != null && parentMissingMandatoryI18NProperties.contains(attName)) {
                            parentMissingMandatoryI18NProperties.remove(attName);
                            valuesFound = true;
                        }
                    }
                    if (valuesFound) {
                        if (parentMissingMandatoryI18NProperties.size() == 0) {
                            missingMandatoryI18NProperties.remove(parentPath);
                        } else {
                            missingMandatoryI18NProperties.put(parentPath, parentMissingMandatoryI18NProperties);
                        }
                    }
                }
            }
            for (ExtendedPropertyDefinition extendedPropertyDefinition : extendedPropertyDefinitions) {
                String propertyName = extendedPropertyDefinition.getName();
                String value = atts.getValue(propertyName);
                 // Check mandatory constraint
                if (!Constants.JAHIANT_TRANSLATION.equals(extendedNodeType.getName()) &&
                        extendedPropertyDefinition.isMandatory() &&
                        !extendedPropertyDefinition.isAutoCreated() &&
                        !extendedPropertyDefinition.isProtected() &&
                        !extendedPropertyDefinition.hasDynamicDefaultValues()) {
                    if (extendedPropertyDefinition.isInternationalized()) {
                        Set<String> missingProperties = missingMandatoryI18NProperties.get(currentPath);
                        if (missingProperties == null) {
                            missingProperties = new TreeSet<String>();
                        }
                        missingProperties.add(propertyName);
                        missingMandatoryI18NProperties.put(currentPath, missingProperties);
                    } else {
                        if (value == null) {
                            if (Constants.JCR_DATA.equals(propertyName)) {
                                // @todo for jcr:data property we need to check if a file exists in the import, not yet implemented
                            } else {
                                Set<String> constraintsViolatedOnPath = missingMandatoryProperties.get(currentPath);
                                if (constraintsViolatedOnPath == null) {
                                    constraintsViolatedOnPath = new TreeSet<String>();
                                }
                                constraintsViolatedOnPath.add(propertyName);
                                missingMandatoryProperties.put(currentPath, constraintsViolatedOnPath);
                            }
                        }
                    }
                }
                // Check other contraint
                if (value != null) {
                    // process the value constraints for all property type but references
                    String propertyPath = currentPath + "/" + propertyName;
                    boolean hasConstraints = extendedPropertyDefinition.getValueConstraints() != null && extendedPropertyDefinition.getValueConstraints().length > 0;
                    boolean isNotReference = extendedPropertyDefinition.getRequiredType() != PropertyType.WEAKREFERENCE && extendedPropertyDefinition
                            .getRequiredType() != PropertyType.REFERENCE;
                    if (hasConstraints && isNotReference) {
                        // for i18n nodes we check on the parent node but for the mixins, they are directly on the i18n node
                        ExtendedNodeType typeToCheck = mixin ? extendedNodeType : extendedParentType;
                        // in all cases we check first on the primary nodetype that can miss the property, then on the mixin that can set it, in this case the
                        // entry is removed from otherConstraintViolations
                        if (!extendedPropertyDefinition.isMultiple()) {
                            if (!typeToCheck.canSetProperty(propertyName, JCRValueFactoryImpl.getInstance().createValue(value))) {
                                otherConstraintViolations.put(propertyPath, propertyName + CONSTRAINT_SEPARATOR + value);
                            } else if (otherConstraintViolations.containsKey(propertyPath)) {
                                otherConstraintViolations.remove(propertyPath);
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
                            } else if (otherConstraintViolations.containsKey(propertyPath)) {
                                otherConstraintViolations.remove(propertyPath);
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
