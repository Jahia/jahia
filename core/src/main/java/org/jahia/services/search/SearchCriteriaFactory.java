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
        public Object convert(Class type, Object value) {
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
        
        ArrayConverter converter = new ArrayConverter(String[].class, new StringConverter());
        converter.setAllowedChars(new char[] {'.', '-', '_'});
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
    @SuppressWarnings("unchecked")
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
                logger.warn("Error parsing search parameters", cause);
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

    @SuppressWarnings("unchecked")
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
