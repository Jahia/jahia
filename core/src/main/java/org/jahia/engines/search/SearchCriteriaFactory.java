/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.engines.search;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.log4j.Logger;
import org.jahia.engines.search.SearchCriteria.DateValue;
import org.jahia.engines.search.SearchCriteria.DocumentProperty;
import org.jahia.engines.search.SearchCriteria.DocumentPropertyDescriptor;
import org.jahia.engines.search.SearchCriteria.HierarchicalValue;
import org.jahia.engines.search.SearchCriteria.SearchMode;
import org.jahia.engines.search.SearchCriteria.Term;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Factory for retrieving {@link SearchCriteria} object data.
 * 
 * @author Sergiy Shyrkov
 */
public class SearchCriteriaFactory {

    private static final String ATTR_QUERY_PARAMS = org.jahia.engines.search.SearchCriteria.class
            .getName();

    private static final ConvertUtilsBean CONVERTER_UTILS_BEAN = new ConvertUtilsBean();

    private static Converter ENUM_CONVERTER = new Converter() {
        public Object convert(Class type, Object value) {
            return Enum.valueOf(type, String.valueOf(value).toUpperCase());
        }
    };

    private static Logger logger = Logger
            .getLogger(SearchCriteriaFactory.class);

    private static final String PARAM_NAME_PREFIX = "src_";

    private static final XStream SERIALIZER;

    static {
        SERIALIZER = new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new CompactWriter(out, xmlFriendlyReplacer());
            }
        });
        SERIALIZER.alias("search-criteria", SearchCriteria.class);
        SERIALIZER.alias("date-value", DateValue.class);
        SERIALIZER.alias("document-property", DocumentProperty.class);
        SERIALIZER.alias("hierarchical-value", HierarchicalValue.class);
        SERIALIZER.alias("term", Term.class);
    }

    static {
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, DateValue.Type.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER,
                DocumentProperty.Type.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, Term.MatchType.class);
        CONVERTER_UTILS_BEAN.register(ENUM_CONVERTER, SearchMode.class);
    }

    public static SearchCriteria deserialize(String serializedSearch) {
        return (SearchCriteria) SERIALIZER.fromXML(serializedSearch);
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
    public static SearchCriteria getInstance(ProcessingContext ctx) {

        SearchCriteria searchParams = (SearchCriteria) ctx
                .getAttribute(ATTR_QUERY_PARAMS);

        if (null == searchParams && isRequestDataPresent(ctx)) {
            searchParams = new SearchCriteria();
            try {
                Map<String, Object> properties = new HashMap<String, Object>();

                for (Map.Entry<String, Object> param : (Set<Map.Entry<String, Object>>) ctx
                        .getParameterMap().entrySet()) {
                    if (param.getKey().startsWith(PARAM_NAME_PREFIX)) {
                        properties.put(param.getKey().substring(
                                PARAM_NAME_PREFIX.length()), param.getValue());
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
                logger.debug(searchParams);
            }

            // initialize document properties
            initDocumentProperties(searchParams, ctx);

            ctx.setAttribute(ATTR_QUERY_PARAMS, searchParams);
        }

        return searchParams;
    }

    private static DocumentPropertyDescriptor getPropertyDescriptor(
            ExtendedItemDefinition itemDef, ExtendedNodeType nodeType,
            ProcessingContext ctx) throws RepositoryException {

        ExtendedPropertyDefinition propDefExt = (ExtendedPropertyDefinition) itemDef;
        PropertyDefinition propDef = JCRContentUtils.getPropertyDefinition(
                nodeType, propDefExt.getName());

        DocumentProperty.Type type = DocumentProperty.Type.TEXT;
        switch (propDef.getRequiredType()) {
        case PropertyType.BOOLEAN:
            type = DocumentProperty.Type.BOOLEAN;
            break;
        case PropertyType.DATE:
            type = DocumentProperty.Type.DATE;
            break;
        case PropertyType.STRING:
            if (SelectorType.CATEGORY == (propDefExt.getSelector())) {
                type = DocumentProperty.Type.CATEGORY;
            }
            break;
        }

        DocumentPropertyDescriptor descriptor = new DocumentPropertyDescriptor(
                itemDef.getName(), itemDef.getLabel(ctx != null ? ctx
                        .getLocale() : Locale.getDefault()), type);

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

    public static DocumentPropertyDescriptor getPropertyDescriptor(
            String documentType, String propertyName, ProcessingContext ctx)
            throws RepositoryException {
        PropertyDefinition propDef = JCRContentUtils.getPropertyDefinition(
                documentType, propertyName);
        DocumentPropertyDescriptor descriptor = null;
        if (propDef != null) {
            descriptor = getPropertyDescriptor(
                    (ExtendedItemDefinition) propDef, NodeTypeRegistry
                            .getInstance().getNodeType(documentType), ctx);
        }

        return descriptor;
    }

    private static void initDocumentProperties(SearchCriteria searchParams,
            ProcessingContext ctx) {

        List<DocumentProperty> props = new LinkedList<DocumentProperty>();
        for (Map.Entry<String, Map<String, DocumentProperty>> docTypeEntry : searchParams
                .getProperties().entrySet()) {
            for (Map.Entry<String, DocumentProperty> propEntry : docTypeEntry
                    .getValue().entrySet()) {
                DocumentProperty prop = propEntry.getValue();
                // set document type and property name
                prop.setDocumentType(docTypeEntry.getKey());
                prop.setName(propEntry.getKey());
                if (!prop.isAllEmpty()) {
                    try {
                        // retrieve property descriptor
                        DocumentPropertyDescriptor descriptor = getPropertyDescriptor(
                                prop.getDocumentType(), prop.getName(), ctx);
                        // set additional properties
                        prop.setConstrained(descriptor.isConstrained());
                        prop.setMultiple(descriptor.isMultiple());
                        prop.setType(descriptor.getType());
                    } catch (RepositoryException e) {
                        logger.error(
                                "Error retrieving property descriptor for document type '"
                                        + prop.getDocumentType()
                                        + "' and property name '"
                                        + prop.getName() + "'", e);
                    }

                }
                props.add(prop);
            }
        }

    }

    private static boolean isRequestDataPresent(ProcessingContext ctx) {
        boolean present = false;
        for (String param : (Set<String>) ctx.getParameterMap().keySet()) {
            if (param.startsWith(PARAM_NAME_PREFIX)) {
                present = true;
                break;
            }
        }

        return present;
    }

    public static String serialize(SearchCriteria params) {
        return SERIALIZER.toXML(params);
    }

}
