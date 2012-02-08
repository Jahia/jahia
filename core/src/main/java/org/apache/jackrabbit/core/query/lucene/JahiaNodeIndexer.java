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

package org.apache.jackrabbit.core.query.lucene;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.Executor;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.core.value.InternalValueFactory;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.schema.DateField;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.textextraction.TextExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node} and use Jahia sepecific definitions for index creation.
 */
public class JahiaNodeIndexer extends NodeIndexer {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaNodeIndexer.class);

    /**
     * Prefix for all field names that are facet indexed by property name.
     */
    public static final String FACET_PREFIX = "FACET:";
    
    public static final String TRANSLATED_NODE_PARENT = "_:TRANSLATED_PARENT".intern();
    public static final String TRANSLATION_LANGUAGE = "_:TRANSLATION_LANGUAGE".intern();

    public static final String FACET_HIERARCHY = "_:FACET_HIERARCHY".intern();

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
     * If set to <code>true</code> the fulltext field is also stored with site/locale suffix
     */
    protected boolean supportSpellchecking = false;

    private static Name siteTypeName = null;

    private static Name siteFolderTypeName = null;

    private static final DateField dateType = new DateField();
    private static final Name MIXIN_TYPES = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "mixinTypes");
    private static final Name PRIMARY_TYPE = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "primaryType");

    /**
     * Creates a new node indexer.
     * 
     * @param node
     *            the node state to index.
     * @param stateProvider
     *            the persistent item state manager to retrieve properties.
     * @param mappings
     *            internal namespace mappings.
     * @param executor
     * @param parser
     * @param context
     *            the query handler context (for getting other services and registries)
     */
    public JahiaNodeIndexer(NodeState node, ItemStateManager stateProvider,
            NamespaceMappings mappings, Executor executor, Parser parser,
            QueryHandlerContext context) {
        super(node, stateProvider, mappings, executor, parser);
        this.nodeTypeRegistry = NodeTypeRegistry.getInstance();
        this.namespaceRegistry = context.getNamespaceRegistry();
        try {
            Name nodeTypeName = node.getNodeTypeName();
            nodeType = nodeTypeRegistry != null ? nodeTypeRegistry.getNodeType(namespaceRegistry
                    .getPrefix(nodeTypeName.getNamespaceURI())
                    + ":" + nodeTypeName.getLocalName()) : null;
            if (siteTypeName == null && nodeTypeRegistry != null) {
                ExtendedNodeType nodeType = nodeTypeRegistry
                        .getNodeType(Constants.JAHIANT_VIRTUALSITE);
                if (nodeType != null) {
                    siteTypeName = NameFactoryImpl.getInstance().create(
                            nodeType.getNameObject().getUri(), nodeType.getLocalName());
                    nodeType = nodeTypeRegistry.getNodeType(Constants.JAHIANT_VIRTUALSITES_FOLDER);
                    siteFolderTypeName = NameFactoryImpl.getInstance().create(
                            nodeType.getNameObject().getUri(), nodeType.getLocalName());
                }
            }
        } catch (NoSuchNodeTypeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
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
        boolean useInExcerpt = super.useInExcerpt(propertyName);
        if (useInExcerpt) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(nodeType, node, getPropertyName(propertyName));
            useInExcerpt = propDef == null || propDef.isFullTextSearchable();
        }
        return useInExcerpt;
    }

    protected String getPropertyName(Name name) {
        StringBuilder propertyNameBuilder = new StringBuilder();

        try {
            propertyNameBuilder.append(namespaceRegistry.getPrefix(name.getNamespaceURI()));
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot get namespace prefix for: " + name.getNamespaceURI(), e);
            }
        }

        if (propertyNameBuilder.length() > 0) {
            propertyNameBuilder.append(":");
        }
        propertyNameBuilder.append(name.getLocalName());
        return propertyNameBuilder.toString();
    }

    protected String resolveSite() {
        String site = null;
        try {
            NodeState current = node;
            do {
                if (isNodeType(current, siteTypeName)) {
                    NodeState siteParent = (NodeState) stateProvider.getItemState(current
                            .getParentId());
                    if (isNodeType(siteParent, siteFolderTypeName)) {
                        return siteParent.getChildNodeEntry(current.getNodeId()).getName()
                                .getLocalName();
                    }
                }
                NodeId id = current.getParentId();
                if (id != null) {
                    current = (NodeState) stateProvider.getItemState(id);
                } else {
                    current = null;
                }
            } while (current != null);
        } catch (RepositoryException e) {
        } catch (NoSuchItemStateException e) {
        } catch (ItemStateException e) {
        }

        return site;
    }

    private boolean isNodeType(NodeState nodeState, Name typeName) throws RepositoryException {
        if (typeName != null) {
            Name primary = nodeState.getNodeTypeName();
            if (primary.equals(typeName)) {
                return true;
            }
            Set<Name> mixins = nodeState.getMixinTypeNames();
            if (mixins.contains(typeName)) {
                return true;
            }
        }
        return false;
    }

    protected String resolveLanguage() {
        String language = null;
        if (nodeType != null && Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
            try {
                for (Name propName : node.getPropertyNames()) {
                    if ("language".equals(propName.getLocalName())
                            && Name.NS_JCR_URI.equals(propName.getNamespaceURI())) {
                        PropertyId id = new PropertyId(node.getNodeId(), propName);
                        PropertyState propState = (PropertyState) stateProvider.getItemState(id);
                        language = propState.getValues()[0].getString();
                        break;
                    }
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error finding language property", e);
                }
            }
        }
        return language;
    }

    protected ExtendedPropertyDefinition findDefinitionForPropertyInNode(ExtendedNodeType nodeType,
            NodeState givenNode, String fieldName) throws RepositoryException {
        ExtendedPropertyDefinition propDef = null;
        if (givenNode == null && nodeType != null) {
            propDef = nodeType.getPropertyDefinitionsAsMap().get(fieldName);
            if (propDef == null) {
                for (Name mixinTypeName : node.getMixinTypeNames()) {
                    ExtendedNodeType mixinType = nodeTypeRegistry != null ? nodeTypeRegistry
                            .getNodeType(namespaceRegistry.getPrefix(mixinTypeName
                                    .getNamespaceURI())
                                    + ":" + mixinTypeName.getLocalName()) : null;
                    propDef = mixinType.getPropertyDefinitionsAsMap().get(fieldName);
                    if (propDef != null) {
                        break;
                    }
                }
            }
        } else if (givenNode != null) {
            nodeType = nodeTypeRegistry != null ? nodeTypeRegistry
                    .getNodeType(namespaceRegistry.getPrefix(givenNode.getNodeTypeName()
                            .getNamespaceURI())
                            + ":" + givenNode.getNodeTypeName().getLocalName()) : null;
            propDef = nodeType.getPropertyDefinitionsAsMap().get(fieldName);
            if (propDef == null) {
                for (Name mixinTypeName : givenNode.getMixinTypeNames()) {
                    ExtendedNodeType mixinType = nodeTypeRegistry != null ? nodeTypeRegistry
                            .getNodeType(namespaceRegistry.getPrefix(mixinTypeName
                                    .getNamespaceURI())
                                    + ":" + mixinTypeName.getLocalName()) : null;
                    propDef = mixinType.getPropertyDefinitionsAsMap().get(fieldName);
                    if (propDef != null) {
                        break;
                    }
                }
            }
        }

        return propDef;
    }

    protected ExtendedPropertyDefinition getExtendedPropertyDefinition(ExtendedNodeType nodeType, NodeState node, String fieldName) {
        ExtendedPropertyDefinition propDef = null;
        if (nodeType != null) {
            try {
                String language = resolveLanguage();
                if (language != null) {
                    propDef = findDefinitionForPropertyInNode(nodeType,
                            (NodeState) stateProvider.getItemState(node.getParentId()),
                            fieldName.endsWith("_" + language) ? fieldName.substring(0, fieldName
                                    .lastIndexOf("_" + language)) : fieldName);
                    if (propDef == null) {
                        propDef = findDefinitionForPropertyInNode(nodeType, null, fieldName);
                    }
                } else {
                    propDef = findDefinitionForPropertyInNode(nodeType, null, fieldName);
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
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
        float scoreBoost = super.getPropertyBoost(propertyName);
        if (Float.compare(scoreBoost, 1.0F) == 0) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(nodeType, node, getPropertyName(propertyName));
            if (propDef != null) {
                scoreBoost = (float) propDef.getScoreboost();
            }
        }
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
        boolean isIncludedInNodeIndex = super.isIncludedInNodeIndex(propertyName);
        if (isIncludedInNodeIndex) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(nodeType, node,
                    getPropertyName(propertyName));
            isIncludedInNodeIndex = propDef == null || propDef.isFullTextSearchable();
        }
        return isIncludedInNodeIndex;
    }

    /**
     * Returns <code>true</code> if the property with the given name should be indexed.
     * 
     * @param propertyName
     *            name of a property.
     * @return <code>true</code> if the property should be fulltext indexed; <code>false</code> otherwise.
     */
    protected boolean isIndexed(Name propertyName) {
        boolean isIndexed = super.isIndexed(propertyName);
        if (isIndexed) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(nodeType, node,
                    getPropertyName(propertyName));
            isIndexed = propDef == null
                    || propDef.getIndex() != ExtendedPropertyDefinition.INDEXED_NO;
        }
        return isIndexed;
    }

    /**
     * Adds the string value to the document both as the named field and optionally for full text indexing if <code>tokenized</code> is
     * <code>true</code>.
     * 
     * The Jahia specific functionality is to strip off HTML markup from richtext fields.
     * 
     * @param doc
     *            The document to which to add the field
     * @param fieldName
     *            The name of the field to add
     * @param internalValue
     *            The value for the field to add to the document.
     * @param tokenized
     *            If <code>true</code> the string is also tokenized and fulltext indexed.
     * @param includeInNodeIndex
     *            If <code>true</code> the string is also tokenized and added to the node scope fulltext index.
     * @param boost
     *            the boost value for this string field.
     * @param useInExcerpt
     *            If <code>true</code> the string may show up in an excerpt.
     */
    @Override
    protected void addStringValue(Document doc, String fieldName, Object internalValue,
            boolean tokenized, boolean includeInNodeIndex, float boost, boolean useInExcerpt) {

        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));

        if (definition != null && SelectorType.RICHTEXT == definition.getSelector()) {
            try {
                Metadata metadata = new Metadata();
                metadata.set(Metadata.CONTENT_TYPE, "text/html");
                metadata.set(Metadata.CONTENT_ENCODING, InternalValueFactory.DEFAULT_ENCODING);

                TextExtractionService textExtractor = (TextExtractionService) SpringContextSingleton.getBean("org.jahia.services.textextraction.TextExtractionService");
                internalValue = textExtractor.parse(new ByteArrayInputStream(((String) internalValue)
                        .getBytes(InternalValueFactory.DEFAULT_ENCODING)), metadata);
            } catch (Exception e) {
                internalValue = StringEscapeUtils.unescapeHtml((String) internalValue);
            }
        }
        if (internalValue == null) {
            return;
        }
        super.addStringValue(doc, fieldName, internalValue, tokenized, includeInNodeIndex, boost,
                useInExcerpt);
        if (tokenized) {
            String stringValue = (String) internalValue;
            if (stringValue.length() == 0) {
                return;
            }

            if (includeInNodeIndex && isSupportSpellchecking()) {
                String site = resolveSite();
                String language = resolveLanguage();
                StringBuilder fulltextNameBuilder = new StringBuilder(FieldNames.FULLTEXT);
                if (site != null) {
                    fulltextNameBuilder.append("-").append(site);
                }
                if (language != null) {
                    fulltextNameBuilder.append("-").append(language);
                }
                String fulltextName = fulltextNameBuilder.toString();
                if (!FieldNames.FULLTEXT.equals(fulltextName)) {
                    doc.add(createFulltextField(fulltextName, stringValue, false));
                }
            }
        }
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue);
        }
    }

    private String getPropertyNameFromFieldname(String fieldName) {
        String propertyName = fieldName;
        if (fieldName.contains(":")) {
            try {
                String prefix = namespaceRegistry.getPrefix(mappings.getURI(fieldName.substring(0,
                        fieldName.indexOf(':'))));
                if (StringUtils.isEmpty(prefix)) {
                    propertyName = fieldName.substring(fieldName.indexOf(':') + 1);
                } else {
                    propertyName = fieldName.replaceFirst("(\\d+)", prefix);
                }
            } catch (RepositoryException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Cannot convert Lucene fieldName '" + fieldName + "' to property name", e);
                }
            }
        }
        return propertyName;
    }

    /**
     * Adds the value to the document both as faceted field which will be indexed with a keyword analyzer which does not modify the term.
     * 
     * @param doc
     *            The document to which to add the field
     * @param fieldName
     *            The name of the field to add
     * @param internalValue
     *            The value for the field to add to the document.
     */
    protected void addFacetValue(Document doc, String fieldName, Object internalValue) {
        // simple String
        String stringValue = (String) internalValue;
        if (stringValue.length() == 0) {
            return;
        }
        // create facet index on property
        int idx = fieldName.indexOf(':');
        fieldName = fieldName.substring(0, idx + 1) + FACET_PREFIX + fieldName.substring(idx + 1);
        Field f = new Field(fieldName, stringValue, Field.Store.NO, Field.Index.ANALYZED,
                Field.TermVector.NO);
        doc.add(f);

    }

    /**
     * Adds the value to the document both as faceted field which will be indexed with a keyword analyzer which does not modify the term.
     *
     * @param doc
     *            The document to which to add the field
     * @param fieldName
     *            The name of the field to add
     * @param internalValue
     *            The value for the field to add to the document.
     */
    protected void addHierarchicalFacetValue(Document doc, String fieldName, Object internalValue) {
        final String stringValue = (String) internalValue;
        if (stringValue.length() == 0) {
            return;
        }

        int idx = fieldName.indexOf(':');
        fieldName = fieldName.substring(0, idx + 1) + FACET_PREFIX + fieldName.substring(idx + 1);

        final List<String> hierarchyPaths = new ArrayList<String>();
        final List<String> parentIds = new ArrayList<String>(); 
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<Object>>() {
                public List<Object> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNodeByIdentifier(stringValue);
                    String typeName = node.getPrimaryNodeTypeName();
                    while (typeName.equals(node.getParent().getPrimaryNodeTypeName())) {
                        hierarchyPaths.add(node.getPath());
                        parentIds.add(node.getIdentifier());
                        node = node.getParent();
                    }
                    while (!"/".equals(node.getPath())) {
                        parentIds.add(node.getIdentifier());
                        node = node.getParent();
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        int hierarchyIndex = hierarchyPaths.size();
        for (String path : hierarchyPaths) {
            doc.add(new Field(fieldName, hierarchyIndex + path, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
            hierarchyIndex--;
        }
        for (String id : parentIds) {
            doc.add(new Field(FACET_HIERARCHY, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
        }
    }

    /**
     * Creates a fulltext field for the string <code>value</code>.
     * 
     * @param value
     *            the string value.
     * @param store
     *            if the value of the field should be stored.
     * @return a lucene field.
     */
    protected Field createFulltextField(String fieldName, String value, boolean store) {
        if (store) {
            // store field compressed if greater than 16k
            Field.Store stored;
            if (value.length() > 0x4000) {
                stored = Field.Store.COMPRESS;
            } else {
                stored = Field.Store.YES;
            }
            return new Field(fieldName, value, stored, Field.Index.ANALYZED, Field.TermVector.NO);
        } else {
            return new Field(fieldName, value, Field.Store.NO, Field.Index.ANALYZED,
                    Field.TermVector.NO);
        }
    }

    public boolean isSupportSpellchecking() {
        return supportSpellchecking;
    }

    public void setSupportSpellchecking(boolean supportSpellchecking) {
        this.supportSpellchecking = supportSpellchecking;
    }

    @Override
    protected void addCalendarValue(Document doc, String fieldName, Object internalValue) {
        super.addCalendarValue(doc, fieldName, internalValue);
        Calendar value = (Calendar) internalValue;
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, dateType.toInternal(new Date(value.getTimeInMillis())));
        }
    }    
    
    @Override
    protected void addBooleanValue(Document doc, String fieldName, Object internalValue) {
        super.addBooleanValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue.toString());
        }        
    }

    @Override
    protected void addDoubleValue(Document doc, String fieldName, Object internalValue) {
        super.addDoubleValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue.toString());
        }                        
    }

    @Override
    protected void addLongValue(Document doc, String fieldName, Object internalValue) {
        super.addLongValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue.toString());
        }                                
    }

    @Override
    protected void addReferenceValue(Document doc, String fieldName, Object internalValue,
            boolean weak) {
        super.addReferenceValue(doc, fieldName, internalValue, weak);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            if (definition.isHierarchical()) {
                addHierarchicalFacetValue(doc, fieldName, internalValue.toString());
            } else {
                addFacetValue(doc, fieldName, internalValue.toString());
            }
        }                                
    }

    @Override
    protected void addNameValue(Document doc, String fieldName, Object internalValue) {
        super.addNameValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(nodeType, node, getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, ((Name)internalValue).getNamespaceURI());
        }                                        
    }

    @Override
    public Document createDoc() throws RepositoryException {
        Document doc = super.createDoc();
        if (nodeType != null && Constants.JAHIANT_TRANSLATION.equals(nodeType.getName())) {
            doNotUseInExcerpt.clear();
            try {
                NodeState parentNode = (NodeState) stateProvider.getItemState(node.getParentId());

//                /// Move translation node on its parent place
//                doc.removeField(FieldNames.UUID);
//                doc.add(new Field(
//                    FieldNames.UUID, parentNode.getNodeId().toString(),
//                    Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

                /// add direct parent for translation
//                doc.removeField(FieldNames.PARENT);
                doc.add(new Field(
                        TRANSLATED_NODE_PARENT, parentNode.getParentId().toString(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));

                doc.add(new Field(
                        TRANSLATION_LANGUAGE, resolveLanguage(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));

                // copy properties from parent into translation node, including node types
                Set<Name> parentNodePropertyNames = new HashSet<Name>(parentNode.getPropertyNames());
                final Set<Name> localNames = new HashSet<Name>(node.getPropertyNames());
                localNames.remove(PRIMARY_TYPE);
                localNames.remove(MIXIN_TYPES);
                parentNodePropertyNames.removeAll(localNames);
                parentNodePropertyNames.removeAll(((JahiaIndexingConfigurationImpl)indexingConfig).getExcludesFromI18NCopy());
                
                for (Name propName : parentNodePropertyNames) {
                    try {
                        PropertyId id = new PropertyId(parentNode.getNodeId(), propName);
                        PropertyState propState = (PropertyState) stateProvider.getItemState(id);

                        // add each property to the _PROPERTIES_SET for searching
                        // beginning with V2
                        if (indexFormatVersion.getVersion() >= IndexFormatVersion.V2.getVersion()) {
                            addPropertyName(doc, propState.getName());
                        }

                        InternalValue[] values = propState.getValues();
                        for (InternalValue value : values) {
                            addValue(doc, value, propState.getName());
                        }
                        if (values.length > 1) {
                            // real multi-valued
                            addMVPName(doc, propState.getName());
                        }
                    } catch (NoSuchItemStateException e) {
                        throwRepositoryException(e);
                    } catch (ItemStateException e) {
                        throwRepositoryException(e);
                    }
                }

                // now add fields that are not used in excerpt (must go at the end)
                for (Fieldable field : doNotUseInExcerpt) {
                    doc.add(field);
                }
            } catch (ItemStateException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return doc;
    }
}
