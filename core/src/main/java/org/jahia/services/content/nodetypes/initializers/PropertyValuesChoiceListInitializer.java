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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Choice list initializer that uses values of the specified node property (multiple).
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6.1.0
 */
public class PropertyValuesChoiceListInitializer implements ChoiceListInitializer {

    private static final Logger logger = LoggerFactory.getLogger(PropertyValuesChoiceListInitializer.class);

    private static final String CONTEXT_NODE = "contextNode";

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
            Locale locale, Map<String, Object> context) {
        if (param == null || !param.contains(";")) {
            throw new IllegalArgumentException(
                    "Parameter format is wrong. Expecting 'targetNode;targetProperty' or 'targetNode;targetProperty;valueType'");
        }

        List<ChoiceListValue> choices = null;

        if (context != null) {
            JCRNodeWrapper contextNode = context.containsKey(CONTEXT_NODE) && context.get(CONTEXT_NODE) != null ?
                    (JCRNodeWrapper) context.get(CONTEXT_NODE) :
                    (JCRNodeWrapper) context.get("contextParent");
            try {
                choices = contextNode != null ? getChoices(contextNode, param) : new LinkedList<>();
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return choices;
    }

    private List<ChoiceListValue> getChoices(JCRNodeWrapper contextNode, String param) throws RepositoryException {
        Value[] values = null;

        String targetNode = StringUtils.substringBefore(param, ";").trim();
        String targetProperty = StringUtils.substringAfter(param, ";").trim();
        String valueType = null;
        if (targetProperty.contains(";")) {
            valueType = StringUtils.substringAfter(targetProperty, ";").trim();
            targetProperty = StringUtils.substringBefore(targetProperty, ";").trim();
        }

        JCRNodeWrapper target = null;
        if ("this".equals(targetNode)) {
            target = contextNode;
        } else {
            try {
                target = contextNode.getSession().getNode(targetNode);
            } catch (PathNotFoundException e) {
                logger.warn("Node {} cannot be found. The choice list for the {} will be empty", targetNode, contextNode.getPath());
            }
        }

        JCRPropertyWrapper prop = null;
        if (target != null && target.hasProperty(targetProperty)) {
            prop = target.getProperty(targetProperty);
            values = prop.isMultiple() ? prop.getValues() : new Value[] { prop.getValue() };
        }

        List<ChoiceListValue> choices = null;

        if (values != null && values.length > 0) {
            choices = new LinkedList<>();
            boolean isReference = prop.getType() == PropertyType.REFERENCE || prop.getType() == PropertyType.WEAKREFERENCE;
            for (Value val : values) {
                if (isReference) {
                    JCRNodeWrapper referencedNode = ((JCRValueWrapper) val).getNode();
                    if (referencedNode != null) {
                        String listValue = null;
                        if (valueType != null) {
                            if ("name".equalsIgnoreCase(valueType)) {
                                listValue = referencedNode.getName();
                            } else if ("path".equalsIgnoreCase(valueType)) {
                                listValue = referencedNode.getPath();
                            }
                        }
                        listValue = listValue == null ? referencedNode.getIdentifier() : listValue;

                        choices.add(new ChoiceListValue(referencedNode.getDisplayableName(), listValue));
                    }
                } else {
                    String listValue = val.getString();
                    choices.add(new ChoiceListValue(listValue, listValue));
                }
            }
        }

        return choices;
    }
}
