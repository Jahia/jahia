/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.query.impl.jackrabbit;

import java.io.ByteArrayInputStream;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.axis.utils.StringUtils;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.query.lucene.NodeIndexer;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValueFactory;
import org.apache.jackrabbit.extractor.HTMLTextExtractor;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.document.Document;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.utils.FileUtils;
import org.jahia.utils.TextHtml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node}
 * and use Jahia sepecific definitions for index creation.
 */
public class JahiaNodeIndexer extends NodeIndexer {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaNodeIndexer.class);

    /**
     * The persistent node type registry
     */
    protected final NodeTypeRegistry nodeTypeRegistry;

    /**
     * The persistent namespace registry
     */
    protected final NamespaceRegistry namespaceRegistry;

    /**
     * The <code>ExtendedNodeType</code> of the node to index
     */
    protected ExtendedNodeType nodeType;

    /**
     * Creates a new node indexer.
     * 
     * @param node
     *            the node state to index.
     * @param stateProvider
     *            the persistent item state manager to retrieve properties.
     * @param mappings
     *            internal namespace mappings.
     * @param extractor
     *            content extractor
     * @param nodeTypeRegistry
     *            Jahia's node type registry
     * @param context
     *            the query handler context (for getting other services and
     *            registries)
     */
    public JahiaNodeIndexer(NodeState node, ItemStateManager stateProvider, NamespaceMappings mappings,
            TextExtractor extractor, NodeTypeRegistry nodeTypeRegistry, QueryHandlerContext context) {
        super(node, stateProvider, mappings, extractor);
        this.nodeTypeRegistry = nodeTypeRegistry;
        this.namespaceRegistry = context.getNamespaceRegistry();
        try {
            Name nodeTypeName = node.getNodeTypeName();
            nodeType = nodeTypeRegistry != null ? nodeTypeRegistry.getNodeType(namespaceRegistry.getPrefix(nodeTypeName
                    .getNamespaceURI())
                    + ":" + nodeTypeName.getLocalName()) : null;
        } catch (NoSuchNodeTypeException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Returns <code>true</code> if the content of the property with the given
     * name should the used to create an excerpt.
     * 
     * @param propertyName
     *            the name of a property.
     * @return <code>true</code> if it should be used to create an excerpt;
     *         <code>false</code> otherwise.
     */
    protected boolean useInExcerpt(Name propertyName) {
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));

        boolean useInExcerpt = propDef != null ? propDef.isFullTextSearchable() : super.useInExcerpt(propertyName);
        return useInExcerpt;
    }

    protected String getPropertyName(Name name) {
        StringBuilder propertyNameBuilder = new StringBuilder();

        try {
            propertyNameBuilder.append(namespaceRegistry.getPrefix(name.getNamespaceURI()));
        } catch (RepositoryException e) {
            logger.debug("Cannot get namespace prefix for: " + name.getNamespaceURI(), e);
        }

        if (propertyNameBuilder.length() > 0) {
            propertyNameBuilder.append(":");
        }
        propertyNameBuilder.append(name.getLocalName());
        return propertyNameBuilder.toString();
    }

    protected ExtendedPropertyDefinition getExtendedPropertyDefinition(String fieldName) {
        ExtendedPropertyDefinition propDef = null;
        if (nodeType != null) {
            propDef = nodeType.getPropertyDefinitionsAsMap().get(fieldName);

            if (propDef == null && node.getParentId() != null
                    && Constants.JAHIANT_TRANSLATION.equals(nodeType.getName()) && fieldName.contains("_")) {
                try {
                    String language = null;
                    for (Name propName : node.getPropertyNames()) {
                        if ("language".equals(propName.getLocalName())
                                && Constants.JCR_NS.equals(propName.getNamespaceURI())) {
                            PropertyId id = new PropertyId(node.getNodeId(), propName);
                            PropertyState propState = (PropertyState) stateProvider.getItemState(id);
                            language = propState.getValues()[0].getString();
                            break;
                        }
                    }
                    if (language != null && fieldName.endsWith("_" + language)) {
                        NodeState parent = (NodeState) stateProvider.getItemState(node.getParentId());
                        Name nodeTypeName = parent.getNodeTypeName();
                        ExtendedNodeType parentNodeType = nodeTypeRegistry != null ? nodeTypeRegistry
                                .getNodeType(namespaceRegistry.getPrefix(nodeTypeName.getNamespaceURI()) + ":"
                                        + nodeTypeName.getLocalName()) : null;
                        propDef = parentNodeType.getPropertyDefinitionsAsMap().get(
                                fieldName.substring(0, fieldName.lastIndexOf("_" + language)));
                    }
                } catch (Exception e) {
                    logger.debug("Error finding language property", e);
                }
            }
        }
        return propDef;
    }

    /**
     * Returns the boost value for the given property name.
     * 
     * @param propertyName
     *            the name of a property.
     * @return the boost value for the given property name.
     */
    protected float getPropertyBoost(Name propertyName) {
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
        float scoreBoost = propDef != null ? (float) propDef.getScoreboost() : super.getPropertyBoost(propertyName);
        return scoreBoost;
    }

    /**
     * Returns <code>true</code> if the property with the given name should also
     * be added to the node scope index.
     * 
     * @param propertyName
     *            the name of a property.
     * @return <code>true</code> if it should be added to the node scope index;
     *         <code>false</code> otherwise.
     */
    protected boolean isIncludedInNodeIndex(Name propertyName) {
        return useInExcerpt(propertyName);
    }

    /**
     * Returns <code>true</code> if the property with the given name should be
     * indexed.
     * 
     * @param propertyName
     *            name of a property.
     * @return <code>true</code> if the property should be fulltext indexed;
     *         <code>false</code> otherwise.
     */
    protected boolean isIndexed(Name propertyName) {
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
        boolean isIndexed = propDef != null ? propDef.getIndex() != ExtendedPropertyDefinition.INDEXED_NO : super
                .isIndexed(propertyName);
        return isIndexed;
    }

    /**
     * Adds the string value to the document both as the named field and
     * optionally for full text indexing if <code>tokenized</code> is
     * <code>true</code>.
     * 
     * The Jahia specific functionality is to strip off HTML markup from
     * richtext fields.
     * 
     * @param doc
     *            The document to which to add the field
     * @param fieldName
     *            The name of the field to add
     * @param internalValue
     *            The value for the field to add to the document.
     * @param tokenized
     *            If <code>true</code> the string is also tokenized and fulltext
     *            indexed.
     * @param includeInNodeIndex
     *            If <code>true</code> the string is also tokenized and added to
     *            the node scope fulltext index.
     * @param boost
     *            the boost value for this string field.
     * @param useInExcerpt
     *            If <code>true</code> the string may show up in an excerpt.
     */
    @Override
    protected void addStringValue(Document doc, String fieldName, Object internalValue, boolean tokenized,
            boolean includeInNodeIndex, float boost, boolean useInExcerpt) {
        String propertyName = fieldName;
        if (fieldName.contains(":")) {
            try {
                String prefix = namespaceRegistry.getPrefix(mappings.getURI(fieldName.substring(0, fieldName
                        .indexOf(':'))));
                if (StringUtils.isEmpty(prefix)) {
                    propertyName = fieldName.substring(fieldName.indexOf(':') + 1);
                } else {
                    propertyName = fieldName.replaceFirst("(\\d+)", prefix);
                }
            } catch (RepositoryException e) {
                logger.debug("Cannot convert Lucene fieldName '" + fieldName + "' to property name", e);
            }
        }
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(propertyName);
        if (definition != null && SelectorType.RICHTEXT == definition.getSelector()) {
            try {
                internalValue = FileUtils.readerToString((new HTMLTextExtractor()).extractText(
                        new ByteArrayInputStream(((String) internalValue)
                                .getBytes(InternalValueFactory.DEFAULT_ENCODING)), "text/html",
                        InternalValueFactory.DEFAULT_ENCODING));
            } catch (Exception e) {
                internalValue = TextHtml.html2text((String) internalValue);
            }
        }
        super.addStringValue(doc, fieldName, internalValue, tokenized, includeInNodeIndex, boost, useInExcerpt);
    }
}