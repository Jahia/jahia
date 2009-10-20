package org.jahia.services.query.impl.jackrabbit;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

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

public class JahiaNodeIndexer extends NodeIndexer {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(JahiaNodeIndexer.class);

    /**
     * The persistent item state provider
     */
    protected final NodeTypeRegistry nodeTypeRegistry;

    /**
     * The persistent item state provider
     */
    protected final NamespaceRegistry namespaceRegistry;    
    
    /**
     * The <code>ExtendedNodeType</code> of the node to index
     */
    protected ExtendedNodeType nodeType;

    public JahiaNodeIndexer(NodeState node, ItemStateManager stateProvider,
            NamespaceMappings mappings, TextExtractor extractor,
            NodeTypeRegistry nodeTypeRegistry, NamespaceRegistry namespaceRegistry) {
        super(node, stateProvider, mappings, extractor);
        this.nodeTypeRegistry = nodeTypeRegistry;
        this.namespaceRegistry = namespaceRegistry;
        try {
            Name nodeTypeName = node.getNodeTypeName();
            nodeType = nodeTypeRegistry.getNodeType(namespaceRegistry
                    .getPrefix(nodeTypeName.getNamespaceURI())
                    + ":" + nodeTypeName.getLocalName());
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
                propDef = nodeType.getPropertyDefinition(namespaceRegistry
                        .getPrefix(propertyName.getNamespaceURI())
                        + ":" + propertyName.getLocalName());
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
