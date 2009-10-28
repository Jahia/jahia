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

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.query.lucene.NodeIndexer;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.apache.jackrabbit.spi.Name;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node} and use
 * Jahia sepecific definitions for index creation.
 */
public class JahiaNodeIndexer extends NodeIndexer {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(JahiaNodeIndexer.class);

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
     * @param node          the node state to index.
     * @param stateProvider the persistent item state manager to retrieve properties.
     * @param mappings      internal namespace mappings.
     * @param extractor     content extractor
     * @param nodeTypeRegistry  Jahia's node type registry
     * @param context       the query handler context (for getting other services and registries)
     */
    public JahiaNodeIndexer(NodeState node, ItemStateManager stateProvider,
            NamespaceMappings mappings, TextExtractor extractor,
            NodeTypeRegistry nodeTypeRegistry,
            QueryHandlerContext context) {
        super(node, stateProvider, mappings, extractor);
        this.nodeTypeRegistry = nodeTypeRegistry;
        this.namespaceRegistry = context.getNamespaceRegistry();
        try {
            Name nodeTypeName = node.getNodeTypeName();
            nodeType = nodeTypeRegistry != null ? nodeTypeRegistry.getNodeType(namespaceRegistry
                    .getPrefix(nodeTypeName.getNamespaceURI())
                    + ":" + nodeTypeName.getLocalName()) : null;
        } catch (NoSuchNodeTypeException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * Returns <code>true</code> if the content of the property with the given name should the used to create an excerpt.
     * 
     * @param propertyName
     *            the name of a property.
     * @return <code>true</code> if it should be used to create an excerpt; <code>false</code> otherwise.
     */
    protected boolean useInExcerpt(Name propertyName) {
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(propertyName);

        boolean useInExcerpt = propDef != null ? propDef.isFullTextSearchable()
                : super.useInExcerpt(propertyName);
        return useInExcerpt;
    }

    protected ExtendedPropertyDefinition getExtendedPropertyDefinition(
            Name propertyName) {
        ExtendedPropertyDefinition propDef = null;
        if (nodeType != null) {
            try {
                StringBuilder propertyNameBuilder = new StringBuilder();
                propertyNameBuilder.append(namespaceRegistry
                        .getPrefix(propertyName.getNamespaceURI()));
                if (propertyNameBuilder.length() > 0) {
                    propertyNameBuilder.append(":");
                }
                propertyNameBuilder.append(propertyName.getLocalName());
                propDef = nodeType.getPropertyDefinition(propertyNameBuilder
                        .toString());
            } catch (RepositoryException e) {
                logger.debug(e.getMessage(), e);
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
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(propertyName);
        float scoreBoost = propDef != null ? (float) propDef.getScoreboost()
                : super.getPropertyBoost(propertyName);
        return scoreBoost;
    }

    /**
     * Returns <code>true</code> if the property with the given name should also be added to the node scope index.
     * 
     * @param propertyName
     *            the name of a property.
     * @return <code>true</code> if it should be added to the node scope index; <code>false</code> otherwise.
     */
    protected boolean isIncludedInNodeIndex(Name propertyName) {
        return useInExcerpt(propertyName);
    }

    /**
     * Returns <code>true</code> if the property with the given name should be indexed.
     * 
     * @param propertyName
     *            name of a property.
     * @return <code>true</code> if the property should be fulltext indexed; <code>false</code> otherwise.
     */
    protected boolean isIndexed(Name propertyName) {
        ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(propertyName);
        boolean isIndexed = propDef != null ? propDef.getIndex() != ExtendedPropertyDefinition.INDEXED_NO
                : super.isIndexed(propertyName);
        return isIndexed;
    }
}