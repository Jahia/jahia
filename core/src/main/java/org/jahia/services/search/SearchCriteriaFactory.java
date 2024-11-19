/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.SearchCriteria.DateValue;
import org.jahia.services.search.SearchCriteria.NodeProperty;
import org.jahia.services.search.SearchCriteria.NodePropertyDescriptor;
import org.jahia.services.search.SearchCriteria.Ordering;
import org.jahia.services.search.SearchCriteria.Term;

/**
 * Factory for retrieving {@link SearchCriteria} object data.
 *
 * @author Sergiy Shyrkov
 */
public class SearchCriteriaFactory {

    private static final String ATTR_QUERY_PARAMS = SearchCriteria.class
            .getName();

    private static final ConvertUtilsBean CONVERTER_UTILS_BEAN = new ConvertUtilsBean();

    private static Converter ENUM_CONVERTER = new Converter() {
        @SuppressWarnings("unchecked")
        public Object convert(@SuppressWarnings("rawtypes") Class type, Object value) {
            return Enum.valueOf(type, String.valueOf(value).toUpperCase());
        }
    };

    private static Logger logger = LoggerFactory
            .getLogger(SearchCriteriaFactory.class);

    private static final String PARAM_NAME_PREFIX = "src_";

    static {
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, DateValue.Type.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER,
                NodeProperty.Type.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, Term.MatchType.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, Ordering.Operand.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, Ordering.CaseConversion.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, Ordering.Order.class);

        ArrayConverter converter = new ArrayConverter(String[].class, new StringConverter());
        converter.setAllowedChars(new char[] {'.', '-', '_', ':', ' '});
        CONVERTER_UTILS_BEAN.register(converter, String[].class);
    }

    /**
     * Looks up the {@link SearchCriteria} object in the request scope. If it is
     * not found, instantiates it and populates it using current request
     * parameters.
     *
     * @param ctx
     *            current context object
     * @return the {@link SearchCriteria} bean with the current search
     *         parameters
     */
    public static SearchCriteria getInstance(RenderContext ctx) {

        SearchCriteria searchParams = (SearchCriteria) ctx.getRequest().getAttribute(ATTR_QUERY_PARAMS);

        if (null == searchParams && isRequestDataPresent(ctx.getRequest())) {
            searchParams = new SearchCriteria();
            try {
                Map<String, Object> properties = new HashMap<String, Object>();
                Enumeration<String> params = ctx.getRequest().getParameterNames();
                while (params.hasMoreElements()) {
                    String param = params.nextElement();
                    if (param.startsWith(PARAM_NAME_PREFIX)) {
                        String[] pValues = ctx.getRequest().getParameterValues(param);
                        properties.put(param.substring(PARAM_NAME_PREFIX.length()), pValues != null && pValues.length == 1 ? pValues[0]
                                : pValues);
                    }
                }

                new BeanUtilsBean(CONVERTER_UTILS_BEAN, new PropertyUtilsBean())
                        .populate(searchParams, properties);

            } catch (Exception e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof InvocationTargetException
                        && ((InvocationTargetException) cause)
                                .getTargetException() != null) {
                    cause = ((InvocationTargetException) cause)
                            .getTargetException();
                }
                if (logger.isDebugEnabled()) {
                    logger.warn("Error parsing search parameters", cause);
                } else {
                    logger.warn("Error parsing search parameters: " + (cause.getCause() != null ? cause.getCause() : cause));
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(searchParams.toString());
            }

            // initialize node properties
            initNodeProperties(searchParams, ctx.getMainResource().getLocale());

            ctx.getRequest().setAttribute(ATTR_QUERY_PARAMS, searchParams);
        }

        return searchParams;
    }

    private static NodePropertyDescriptor getPropertyDescriptor(
            ExtendedItemDefinition itemDef, ExtendedNodeType nodeType,
            Locale locale) throws RepositoryException {

        ExtendedPropertyDefinition propDefExt = (ExtendedPropertyDefinition) itemDef;
        PropertyDefinition propDef = JCRContentUtils.getPropertyDefinition(
                nodeType, propDefExt.getName());

        NodeProperty.Type type = NodeProperty.Type.TEXT;
        switch (propDef.getRequiredType()) {
        case PropertyType.BOOLEAN:
            type = NodeProperty.Type.BOOLEAN;
            break;
        case PropertyType.DATE:
            type = NodeProperty.Type.DATE;
            break;
        case PropertyType.STRING:
        case PropertyType.WEAKREFERENCE:
            if (SelectorType.CATEGORY == (propDefExt.getSelector())) {
                type = NodeProperty.Type.CATEGORY;
            }
            break;
        }

        NodePropertyDescriptor descriptor = new NodePropertyDescriptor(itemDef.getName(), itemDef
                .getLabel(locale != null ? locale : Locale.ENGLISH, nodeType), type);

        descriptor.setMultiple(propDef.isMultiple());
        if (propDef.getValueConstraints().length > 0) {
            descriptor.setConstrained(true);
            descriptor.setAllowedValues(propDef.getValueConstraints());
        }
        descriptor.setSelectorOptions(propDefExt.getSelectorOptions());

        // TODO handle multiple default values
        Value[] defaultValues = propDef.getDefaultValues();
        if (defaultValues != null && defaultValues.length > 0) {
            descriptor.setDefaultValue(defaultValues[0].getString());
        }

        return descriptor;
    }

    public static NodePropertyDescriptor getPropertyDescriptor(
            String nodeType, String propertyName, Locale locale)
            throws RepositoryException {
        PropertyDefinition propDef = JCRContentUtils.getPropertyDefinition(
                nodeType, propertyName);
        NodePropertyDescriptor descriptor = null;
        if (propDef != null) {
            descriptor = getPropertyDescriptor(
                    (ExtendedItemDefinition) propDef, NodeTypeRegistry
                            .getInstance().getNodeType(nodeType), locale);
        }

        return descriptor;
    }

    private static void initNodeProperties(SearchCriteria searchParams,
            Locale locale) {

        List<NodeProperty> props = new LinkedList<NodeProperty>();
        for (Map.Entry<String, Map<String, NodeProperty>> docTypeEntry : searchParams
                .getProperties().entrySet()) {
            for (Map.Entry<String, NodeProperty> propEntry : docTypeEntry
                    .getValue().entrySet()) {
                NodeProperty prop = propEntry.getValue();
                // set node type and property name
                prop.setNodeType(docTypeEntry.getKey());
                prop.setName(propEntry.getKey());
                if (!prop.isAllEmpty()) {
                    try {
                        // retrieve property descriptor
                        NodePropertyDescriptor descriptor = getPropertyDescriptor(
                                prop.getNodeType(), prop.getName(), locale);
                        // set additional properties
                        prop.setConstrained(descriptor.isConstrained());
                        prop.setMultiple(descriptor.isMultiple());
                        prop.setType(descriptor.getType());
                    } catch (RepositoryException e) {
                        logger.error(
                                "Error retrieving property descriptor for node type '"
                                        + prop.getNodeType()
                                        + "' and property name '"
                                        + prop.getName() + "'", e);
                    }

                }
                props.add(prop);
            }
        }

    }

    private static boolean isRequestDataPresent(HttpServletRequest request) {
        boolean present = false;
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            if (params.nextElement().startsWith(PARAM_NAME_PREFIX)) {
                present = true;
                break;
            }
        }

        return present;
    }
}
