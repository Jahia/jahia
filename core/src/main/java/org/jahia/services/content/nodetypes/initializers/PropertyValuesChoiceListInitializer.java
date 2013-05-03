/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.nodetypes.initializers;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Choice list initializer that uses values of the specified node property (multiple).
 * 
 * @author Sergiy Shyrkov
 * 
 * @since Jahia 6.6.1.0
 */
public class PropertyValuesChoiceListInitializer implements ChoiceListInitializer {

    private static final Logger logger = LoggerFactory
            .getLogger(PropertyValuesChoiceListInitializer.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
            List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        if (param == null || !param.contains(";")) {
            throw new IllegalArgumentException(
                    "Parameter format is wrong. Expecting 'targetNode;targetProperty' or 'targetNode;targetProperty;valueType'");
        }

        List<ChoiceListValue> choices = null;

        JCRNodeWrapper contextNode = context != null ? (JCRNodeWrapper) context.get("contextNode")
                : null;

        if (contextNode != null) {
            try {
                choices = getChoices(contextNode, param);
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        } else if (context.containsKey("contextParent")) {
            String propertyName = context.containsKey("dependentProperties") ? ((List<String>) context
                    .get("dependentProperties")).get(0) : null;
            List<String> currentSelection = (List<String>) (propertyName != null
                    && context.containsKey(propertyName) ? context.get(propertyName) : null);
            if (currentSelection != null) {
                try {
                    choices = getChoices((JCRNodeWrapper) context.get("contextParent"),
                            currentSelection, param);
                } catch (RepositoryException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        return choices != null ? choices : new LinkedList<ChoiceListValue>();
    }

    private List<ChoiceListValue> getChoices(JCRNodeWrapper parentNode,
            List<String> currentSelection, String param) throws RepositoryException {
        String targetProperty = StringUtils.substringAfter(param, ";").trim();
        boolean isReference = targetProperty.contains(";");
        String valueType = isReference ? StringUtils.substringAfter(targetProperty, ";").trim()
                : "uuid";

        List<ChoiceListValue> choices = new LinkedList<ChoiceListValue>();
        for (String selected : currentSelection) {
            if (isReference) {
                try {
                    String listValue = null;
                    JCRNodeWrapper referencedNode = parentNode.getSession().getNodeByIdentifier(
                            selected);
                    if ("name".equalsIgnoreCase(valueType)) {
                        listValue = referencedNode.getName();
                    } else if ("path".equalsIgnoreCase(valueType)) {
                        listValue = referencedNode.getPath();
                    } else {
                        listValue = referencedNode.getIdentifier();
                    }
                    choices.add(new ChoiceListValue(referencedNode.getDisplayableName(), listValue));
                } catch (ItemNotFoundException e) {
                    logger.warn("Unable to find node by UUID {}. Skipping it.", selected);
                }
            } else {
                choices.add(new ChoiceListValue(selected, selected));
            }
        }

        return choices;
    }

    private List<ChoiceListValue> getChoices(JCRNodeWrapper contextNode, String param)
            throws RepositoryException {
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
                logger.warn("Node {} cannot be found. The choice list for the {} will be empty",
                        targetNode, contextNode.getPath());
            }
        }

        JCRPropertyWrapper prop = null;
        if (target != null && target.hasProperty(targetProperty)) {
            prop = target.getProperty(targetProperty);
            values = prop.isMultiple() ? prop.getValues() : new Value[] { prop.getValue() };
        }

        List<ChoiceListValue> choices = null;

        if (values != null && values.length > 0) {
            choices = new LinkedList<ChoiceListValue>();
            boolean isReference = prop.getType() == PropertyType.REFERENCE
                    || prop.getType() == PropertyType.WEAKREFERENCE;
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

                        choices.add(new ChoiceListValue(referencedNode.getDisplayableName(),
                                listValue));
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
