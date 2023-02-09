/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.*;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.value.AbstractQValueFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.solr.schema.DateField;
import org.apache.solr.util.NumberUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.textextraction.TextExtractionService;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * Creates a lucene <code>Document</code> object from a {@link javax.jcr.Node} and use Jahia sepecific definitions for index creation.
 */
@java.lang.SuppressWarnings("java:S1166")
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

    public static final String ACL_UUID = "_:ACL_UUID".intern();
    public static final Name J_ACL = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "acl");
    public static final Name J_ACL_INHERITED = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "inherit");
    public static final Name J_ACE_PRINCIPAL = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "principal");
    public static final Name J_ACE_GRANT = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "aceType");
    public static final Name J_ACE_ROLES = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "roles");

    private static final String USER_PRINCIPAL_PREFIX = "u:";

    public static final String CHECK_VISIBILITY = "_:CHECK_VISIBILITY".intern();

    private static final Name J_EXTRACTED_TEXT = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "extractedText");

    public static final Name J_VISIBILITY = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "conditionalVisibility");

    public static final String PUBLISHED = "_:PUBLISHED".intern();
    public static final Name J_PUBLISHED = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "published");

    public static final String FACET_HIERARCHY = "_:FACET_HIERARCHY".intern();

    public static final Name J_INVALID_LANGUAGES = NameFactoryImpl.getInstance().create(Constants.JAHIA_NS, "invalidLanguages");
    public static final String INVALID_LANGUAGES = "_:INVALID_LANGUAGES".intern();

    private static final int INITIAL_COLLECTION_SIZE = 17;
    /**
     * The persistent node type registry
     */
    protected final NodeTypeRegistry nodeTypeRegistry;

    /**
     * The persistent namespace registry
     */
    protected final NamespaceRegistry namespaceRegistry;

    /**
     * The hierarchy manager
     */
    protected final HierarchyManager hierarchyMgr;

    /**
     * The <code>ExtendedNodeType</code> of the node to index, lazily resolved from its type name
     */
    private ExtendedNodeType nodeType;
    /**
     * The associated node's type name.
     */
    private final Name nodeTypeName;

    /**
     * If set to <code>true</code> the fulltext field is also stored with site/locale suffix
     */
    protected boolean supportSpellchecking = false;

    private Name siteTypeName = null;

    private Name siteFolderTypeName = null;

    private static final DateField dateType = new DateField();

    private boolean addAclUuidInIndex = true;

    private boolean useOptimizedACEIndexation = false;

    private String site;

    private Map<String, ExtendedPropertyDefinition> fieldNameToPropDef = new HashMap<>(INITIAL_COLLECTION_SIZE);

    protected JahiaNodeIndexer(NodeState node, ItemStateManager stateProvider, NamespaceMappings mappings, Executor executor, Parser parser,
            QueryHandlerContext context) {

        super(node, stateProvider, mappings, executor, parser);
        this.nodeTypeRegistry = NodeTypeRegistry.getInstance();
        this.namespaceRegistry = context.getNamespaceRegistry();
        this.hierarchyMgr = context.getHierarchyManager();
        this.nodeTypeName = node.getNodeTypeName();

        try {
            if (siteTypeName == null && nodeTypeRegistry != null) {
                ExtendedNodeType siteNodeType = nodeTypeRegistry.getNodeType(Constants.JAHIANT_VIRTUALSITE);
                if (siteNodeType != null) {
                    siteTypeName = NameFactoryImpl.getInstance().create(siteNodeType.getNameObject().getUri(), siteNodeType.getLocalName());
                    siteNodeType = nodeTypeRegistry.getNodeType(Constants.JAHIANT_VIRTUALSITES_FOLDER);
                    siteFolderTypeName = NameFactoryImpl.getInstance().create(siteNodeType.getNameObject().getUri(), siteNodeType.getLocalName());
                }
            }
        } catch (NoSuchNodeTypeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
        }
    }

    protected String getTypeNameAsString() throws RepositoryException {
        return getTypeNameAsString(nodeTypeName, namespaceRegistry);
    }

    /**
     * Convert name object to jcr-name string
     * @param nodeTypeName the name
     * @param namespaceRegistry the namespace registry
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    protected static String getTypeNameAsString(Name nodeTypeName, NamespaceRegistry namespaceRegistry) throws RepositoryException {
        return namespaceRegistry.getPrefix(nodeTypeName.getNamespaceURI()) + ":" + nodeTypeName.getLocalName();
    }

    /**
     * Returns <code>true</code> if the content of the property with the given name should the used to create an excerpt.
     *
     * @param propertyName the name of a property.
     * @return <code>true</code> if it should be used to create an excerpt; <code>false</code> otherwise.
     */
    @Override
    protected boolean useInExcerpt(Name propertyName) {
        boolean useInExcerpt = super.useInExcerpt(propertyName);
        if (useInExcerpt) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
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
                logger.debug("Cannot get namespace prefix for: {}", name.getNamespaceURI(), e);
            }
        }

        if (propertyNameBuilder.length() > 0) {
            propertyNameBuilder.append(":");
        }
        propertyNameBuilder.append(name.getLocalName());
        return propertyNameBuilder.toString();
    }

    protected String resolveSite() {
        if (site != null) {
            return site;
        }
        try {
            NodeState current = node;
            do {
                if (isNodeType(current, siteTypeName)) {
                    NodeState siteParent = (NodeState) stateProvider.getItemState(current.getParentId());
                    if (isNodeType(siteParent, siteFolderTypeName)) {
                        site = siteParent.getChildNodeEntry(current.getNodeId()).getName().getLocalName();
                        break;
                    }
                }
                NodeId id = current.getParentId();
                if (id != null) {
                    current = (NodeState) stateProvider.getItemState(id);
                } else {
                    current = null;
                }
            } while (current != null);

        } catch (ItemStateException e) {
            // ignored
        }

        return site;
    }

    private boolean isNodeType(NodeState nodeState, Name typeName) {
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

    protected ExtendedNodeType getNodeType() {
        if (nodeType == null) {
            try {
                nodeType = nodeTypeRegistry.getNodeType(getTypeNameAsString());
            } catch (RepositoryException e) {
                logger.error("Couldn't resolve type: {}:{}", nodeTypeName.getNamespaceURI(), nodeTypeName.getLocalName());
            }
        }

        return nodeType;
    }

    protected ExtendedPropertyDefinition getExtendedPropertyDefinition(String fieldName) {
        ExtendedPropertyDefinition propDef = fieldNameToPropDef.get(fieldName);
        if (propDef == null) {
            try {
                propDef = getPropertyDefinition(fieldName);
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error finding property associated with field named {}", fieldName, e);
                }
            }

            if (propDef != null) {
                fieldNameToPropDef.put(fieldName, propDef);
            }
        }
        return propDef;
    }

    @java.lang.SuppressWarnings("java:S1130")
    protected ExtendedPropertyDefinition getPropertyDefinition(String fieldName) throws RepositoryException, ItemStateException {
        return getPropertyDefinitionFor(fieldName, getNodeType(), node);
    }

    protected ExtendedPropertyDefinition getPropertyDefinitionFor(String fieldName, ExtendedNodeType nodeType, NodeState node) throws RepositoryException {
        ExtendedPropertyDefinition propDef = nodeType.getPropertyDefinitionsAsMap().get(fieldName);
        if (propDef == null) {
            for (Name mixinTypeName : node.getMixinTypeNames()) {
                ExtendedNodeType mixinType = nodeTypeRegistry.getNodeType(getTypeNameAsString(mixinTypeName, namespaceRegistry));
                propDef = mixinType.getPropertyDefinitionsAsMap().get(fieldName);
                if (propDef != null) {
                    break;
                }
            }
        }
        return propDef;
    }


    /**
     * Returns the boost value for the given property name.
     *
     * @param propertyName the name of a property.
     * @return the boost value for the given property name.
     */
    @Override
    protected float getPropertyBoost(Name propertyName) {
        float scoreBoost = super.getPropertyBoost(propertyName);
        if (Float.compare(scoreBoost, 1.0F) == 0) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
            if (propDef != null) {
                scoreBoost = (float) propDef.getScoreboost();
            }
        }
        return scoreBoost;
    }

    /**
     * Returns <code>true</code> if the property with the given name should also be added to the node scope index.
     *
     * @param propertyName the name of a property.
     * @return <code>true</code> if it should be added to the node scope index; <code>false</code> otherwise.
     */
    @Override
    protected boolean isIncludedInNodeIndex(Name propertyName) {
        boolean isIncludedInNodeIndex = super.isIncludedInNodeIndex(propertyName);
        if (isIncludedInNodeIndex) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
            isIncludedInNodeIndex = propDef == null || propDef.isFullTextSearchable();
        }
        return isIncludedInNodeIndex;
    }

    /**
     * Returns <code>true</code> if the property with the given name should be indexed.
     *
     * @param propertyName name of a property.
     * @return <code>true</code> if the property should be fulltext indexed; <code>false</code> otherwise.
     */
    @Override
    protected boolean isIndexed(Name propertyName) {
        boolean isIndexed = super.isIndexed(propertyName);
        if (isIndexed) {
            ExtendedPropertyDefinition propDef = getExtendedPropertyDefinition(getPropertyName(propertyName));
            isIndexed = propDef == null || propDef.getIndex() != ExtendedPropertyDefinition.INDEXED_NO;
        }
        return isIndexed;
    }

    /**
     * Adds the string value to the document both as the named field and optionally for full text indexing if <code>tokenized</code> is
     * <code>true</code>.
     * <p/>
     * The Jahia specific functionality is to strip off HTML markup from richtext fields.
     *
     * @param doc                The document to which to add the field
     * @param fieldName          The name of the field to add
     * @param internalValue      The value for the field to add to the document.
     * @param tokenized          If <code>true</code> the string is also tokenized and fulltext indexed.
     * @param includeInNodeIndex If <code>true</code> the string is also tokenized and added to the node scope fulltext index.
     * @param boost              the boost value for this string field.
     * @param useInExcerpt       If <code>true</code> the string may show up in an excerpt.
     */
    @Override
    protected void addStringValue(Document doc, String fieldName, String internalValue,
                                  boolean tokenized, boolean includeInNodeIndex, float boost, boolean useInExcerpt) {

        final String propertyName = getPropertyNameFromFieldname(fieldName);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(propertyName);

        if (definition != null && SelectorType.RICHTEXT == definition.getSelector()) {
            internalValue = extractTextFromHtml(internalValue);
        }
        if (internalValue != null) {
            super.addStringValue(doc, fieldName, internalValue, tokenized, includeInNodeIndex, boost, useInExcerpt);
        }

        if (StringUtils.isNotEmpty(internalValue)) {
            if (tokenized && includeInNodeIndex && isSupportSpellchecking()) {
                addFieldForSpellchecking(doc, propertyName, internalValue);
            }
            if (definition != null && definition.isFacetable()) {
                addFacetValue(doc, fieldName, internalValue);
            }
        }
    }

    private String extractTextFromHtml(String internalValue) {
        try {
            Metadata metadata = new Metadata();
            metadata.set(HttpHeaders.CONTENT_TYPE, "text/html");
            metadata.set(HttpHeaders.CONTENT_ENCODING, AbstractQValueFactory.DEFAULT_ENCODING);

            TextExtractionService textExtractor = (TextExtractionService) SpringContextSingleton.getBean("org.jahia.services.textextraction.TextExtractionService");
            return textExtractor.parse(
                    new ByteArrayInputStream(("<!DOCTYPE html>"+internalValue).getBytes(StandardCharsets.UTF_8)),
                    metadata
            );
        } catch (Exception e) {
            return StringEscapeUtils.unescapeHtml(internalValue);
        }
    }

    private void addFieldForSpellchecking(Document doc, String propertyName, String internalValue) {
        if (getIndexingConfig().shouldPropertyBeSpellchecked(propertyName) && resolveSite() != null) {
            doc.add(createFulltextField(getFullTextFieldName(site), internalValue, false));
        }
    }

    protected String getFullTextFieldName(String site) {
        return LuceneUtils.getFullTextFieldName(site, null);
    }

    private String getPropertyNameFromFieldname(String fieldName) {
        String propertyName = fieldName;
        int pos = fieldName.indexOf(':');
        if (pos > -1) {
            try {
                String prefix = namespaceRegistry.getPrefix(mappings.getURI(fieldName.substring(0, pos)));
                propertyName = !StringUtils.isEmpty(prefix) ? (prefix + fieldName.substring(pos)) : fieldName.substring(pos + 1);
            } catch (RepositoryException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot convert Lucene fieldName '{}' to property name", fieldName, e);
                }
            }
        }
        return propertyName;
    }

    /**
     * Adds the value to the document both as faceted field which will be indexed with a keyword analyzer which does not modify the term.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addFacetValue(Document doc, String fieldName, Object internalValue) {
        // simple String
        String stringValue = internalValue.toString();
        if (stringValue.length() == 0) {
            return;
        }
        // create facet index on property
        int idx = fieldName.indexOf(':');
        fieldName = fieldName.substring(0, idx + 1) + FACET_PREFIX + fieldName.substring(idx + 1);
        Field f = new Field(fieldName, true, stringValue, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO);
        doc.add(f);

    }

    /**
     * Adds the value to the document both as faceted field which will be indexed with a keyword analyzer which does not modify the term.
     *
     * @param doc           The document to which to add the field
     * @param fieldName     The name of the field to add
     * @param internalValue The value for the field to add to the document.
     */
    protected void addHierarchicalFacetValue(Document doc, String fieldName, Object internalValue) {
        final String stringValue = internalValue.toString();
        if (stringValue.length() == 0) {
            return;
        }
        ItemId itemId = (ItemId) internalValue;
        int idx = fieldName.indexOf(':');
        fieldName = fieldName.substring(0, idx + 1) + FACET_PREFIX + fieldName.substring(idx + 1);

        final List<String> hierarchyPaths = new ArrayList<>();
        final List<String> parentIds = new ArrayList<>();
        try {
            NodeState node = (NodeState) stateProvider.getItemState(itemId);
            Name typeName = node.getNodeTypeName();
            boolean isCategoryType = typeName.toString().equals("{" + Constants.JAHIANT_NS + "}" + "category");
            NodeState parent = (NodeState) stateProvider.getItemState(node.getParentId());

            while (typeName.equals(parent.getNodeTypeName())) {
                hierarchyPaths.add(StringUtils.remove(resolver.getJCRPath(hierarchyMgr.getPath(node.getNodeId())), "0:"));
                parentIds.add(node.getNodeId().toString());
                node = parent;
                parent = (NodeState) stateProvider.getItemState(node.getParentId());
            }
            String jcrPath = resolver.getJCRPath(hierarchyMgr.getPath(node.getNodeId()));
            // we stop either at the root (/) or in case of categories also at /sites/systemsite
            while (!"/".equals(jcrPath) && (!isCategoryType || !("0:/0:sites/0:systemsite").equals(jcrPath))) {
                parentIds.add(node.getNodeId().toString());
                node = (NodeState) stateProvider.getItemState(node.getParentId());
                jcrPath = resolver.getJCRPath(hierarchyMgr.getPath(node.getNodeId()));
            }
        } catch (NoSuchItemStateException e) {
            logger.debug(e.getMessage(), e);
        } catch (ItemStateException | RepositoryException e) {
            logger.warn("Error while indexing hierarchical facet value for node {}: {}", node.getNodeId(), e.getMessage());
        }

        int hierarchyIndex = hierarchyPaths.size();
        for (String path : hierarchyPaths) {
            doc.add(new Field(fieldName, true, hierarchyIndex + path, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO));
            hierarchyIndex--;
        }
        for (String id : parentIds) {
            doc.add(new Field(FACET_HIERARCHY, false, id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
        }
    }

    /**
     * Creates a fulltext field for the string <code>value</code>.
     *
     * @param value the string value.
     * @param store if the value of the field should be stored.
     * @return a lucene field.
     */
    protected Field createFulltextField(String fieldName, String value, boolean store) {
        if (store) {
            // We would be able to store the field compressed or not depending
            // on a criterion but then we could not determine later is this field
            // has been compressed or not, so we choose to store it uncompressed
            return new Field(fieldName, true, value, Field.Store.YES,
                    Field.Index.ANALYZED, Field.TermVector.NO);
        } else {
            return new Field(fieldName, true, value,
                    Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.NO);
        }
    }

    public boolean isSupportSpellchecking() {
        return supportSpellchecking;
    }

    public void setSupportSpellchecking(boolean supportSpellchecking) {
        this.supportSpellchecking = supportSpellchecking;
    }

    @Override
    protected void addCalendarValue(Document doc, String fieldName, Calendar internalValue) {
        super.addCalendarValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, dateType.toInternal(new Date(internalValue.getTimeInMillis())));
        }
    }

    @Override
    protected void addBinaryValue(Document doc, String fieldName, InternalValue internalValue) {
        // we disable the binary indexing by Jackrabbit and only index our j:extractedText property (see QA-4709)
        try {
            String propName = mappings.getPrefix(Constants.JAHIA_NS) + ":extractedText";
            if (!propName.equals(fieldName)) {
                return;
            }
            long timer = System.currentTimeMillis();
            String value = internalValue.getString();
            addStringValue(doc, fieldName, value, true, isIncludedInNodeIndex(J_EXTRACTED_TEXT),
                    getPropertyBoost(J_EXTRACTED_TEXT), useInExcerpt(J_EXTRACTED_TEXT));
            if (logger.isDebugEnabled()) {
                logger.debug("Indexed j:extractedText of length {} in {} ms", value.length(), System.currentTimeMillis() - timer);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error indexing content of the j:extractedText property", e);
            } else {
                logger.warn("Error indexing content of the j:extractedText property. Cause: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void addBooleanValue(Document doc, String fieldName, Object internalValue) {
        super.addBooleanValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue.toString());
        }
    }

    @Override
    protected void addDoubleValue(Document doc, String fieldName, double internalValue) {
        super.addDoubleValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, NumberUtils.double2sortableStr(Double.toString(internalValue)));
        }
    }

    @Override
    protected void addLongValue(Document doc, String fieldName, long internalValue) {
        super.addLongValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, NumberUtils.long2sortableStr(Long.toString(internalValue)));
        }
    }

    @Override
    protected void addReferenceValue(Document doc, String fieldName, NodeId internalValue,
                                     boolean weak) {
        super.addReferenceValue(doc, fieldName, internalValue, weak);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            if (definition.isHierarchical()) {
                addHierarchicalFacetValue(doc, fieldName, internalValue);
            } else {
                addFacetValue(doc, fieldName, internalValue);
            }
        }
    }

    @Override
    protected void addNameValue(Document doc, String fieldName, Name internalValue) {
        super.addNameValue(doc, fieldName, internalValue);
        ExtendedPropertyDefinition definition = getExtendedPropertyDefinition(getPropertyNameFromFieldname(fieldName));
        if (definition != null && definition.isFacetable()) {
            addFacetValue(doc, fieldName, internalValue.getNamespaceURI());
        }
    }

    @Override
    public Document createDoc() throws RepositoryException {
        Document doc = createDocIgnoringMissingProperties();

        if (isAddAclUuidInIndex() && isIndexed(J_ACL)) {
            addAclUuid(doc);
        }

        if (isIndexed(J_VISIBILITY) && node.hasChildNodeEntry(J_VISIBILITY)) {
            doc.add(new Field(CHECK_VISIBILITY, false, "1",
                    Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                    Field.TermVector.NO));
        }

        if (isIndexed(J_PUBLISHED) && node.getPropertyNames().contains(J_PUBLISHED)) {
            PropertyId id = new PropertyId(node.getNodeId(), J_PUBLISHED);
            try {
                PropertyState propState = (PropertyState) stateProvider.getItemState(id);

                doc.add(new Field(PUBLISHED, false, propState.getValues()[0].getString(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                        Field.TermVector.NO));
            } catch (ItemStateException e) {
                logger.warn("Error while indexing j:published property for node {}: {}", node.getNodeId(), e.getMessage());
            }
        }

        if (isIndexed(J_INVALID_LANGUAGES) && node.getPropertyNames().contains(J_INVALID_LANGUAGES)) {
            PropertyId id = new PropertyId(node.getNodeId(), J_INVALID_LANGUAGES);
            try {
                PropertyState propState = (PropertyState) stateProvider.getItemState(id);

                final InternalValue[] values = propState.getValues();
                for (InternalValue value : values) {
                    doc.add(new Field(INVALID_LANGUAGES, false, value.getString(),
                            Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                            Field.TermVector.NO));
                }
            } catch (ItemStateException e) {
                logger.warn("Error while indexing j:invalidLanguages for node {}: {}", node.getNodeId(), e.getMessage());
            }
        }

        return doc;
    }

    @Override
    protected void addParentChildRelation(Document doc, NodeId parentId) throws ItemStateException {
        try {
            super.addParentChildRelation(doc, parentId);
        } catch (RepositoryException | NoSuchItemStateException e) {
            logger.warn("Error while indexing parent child relation for node {}: {}", node.getNodeId(), e.getMessage());
            // Sometime in cluster inconsistencies can happen inside the itmMgr because node have been created, and move consecutively,
            // in that case sometimes it's not possible to create parent-child relationship because:
            // - the added node have a parent
            // - but this parent doesn't have the child anymore
            // So we just ignore this because the next events will fix the index and the relationship will be good at the end
        }
    }

    private Document createDocIgnoringMissingProperties() throws RepositoryException {
        try {
            return super.createDoc();
        } catch (RepositoryException e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof NoSuchItemStateException) {
                // Clean up nodestate before starting indexing, as ISM cache may contain removed entries
                cleanupNodeProperties(node);
                return super.createDoc();
            } else {
                throw e;
            }
        }
    }

    protected void cleanupNodeProperties(NodeState node) {
        Set<Name> props = node.getPropertyNames();
        Set<Name> toRemove = null;
        NodeId nodeId = node.getNodeId();
        for (Name propName : props) {
            try {
                if (!stateProvider.hasItemState(new PropertyId(nodeId, propName))) {
                    if (toRemove == null) {
                        toRemove = new HashSet<>();
                    }
                    toRemove.add(propName);
                }
            } catch (Exception e) {
                //
            }
        }

        if (toRemove != null) {
            for (Name name : toRemove) {
                logger.debug("Removed non-existing property {} from {}", name, nodeId);
                node.removePropertyName(name);
            }
        }
    }

    protected JahiaIndexingConfigurationImpl getIndexingConfig() {
        return (JahiaIndexingConfigurationImpl) indexingConfig;
    }

    protected void addAclUuid(Document doc) throws RepositoryException {
        List<String> acls = new ArrayList<>();
        NodeState currentNode = node;
        try {
            while (currentNode != null) {
                if (currentNode.hasChildNodeEntry(J_ACL)) {
                    ChildNodeEntry aclChildNode = currentNode.getChildNodeEntry(J_ACL, 1);
                    if (aclChildNode != null) {
                        acls.add(0, getAce(currentNode, aclChildNode));

                        if (isAclNotInherited(aclChildNode)) {
                            break;
                        }
                    }
                }
                currentNode = currentNode.getParentId() != null ? (NodeState) stateProvider.getItemState(currentNode.getParentId()) : null;
            }
        } catch (ItemStateException e) {
            logger.warn("Error while indexing ACL for node {}: {}", node.getNodeId(), e.getMessage());
        }

        doc.add(new Field(ACL_UUID, false, StringUtils.join(acls, " "), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                Field.TermVector.NO));
    }

    private boolean isAclNotInherited(ChildNodeEntry aclChildNode) throws RepositoryException, ItemStateException {
        boolean aclNotInherited = false;
        PropertyId propId = new PropertyId(aclChildNode.getId(), J_ACL_INHERITED);
        if (stateProvider.hasItemState(propId)) {
            PropertyState ps = (PropertyState) stateProvider.getItemState(propId);
            aclNotInherited = ps.getValues().length == 1 && !ps.getValues()[0].getBoolean();
        }
        return aclNotInherited;
    }

    private String getAce(NodeState node, ChildNodeEntry aclChildNode) throws RepositoryException, ItemStateException {
        StringBuilder ace = new StringBuilder(node.getId().toString());
        NodeState ns = (NodeState) stateProvider.getItemState(aclChildNode.getId());
        if (ns.getChildNodeEntries().size() == 1 && useOptimizedACEIndexation) {
            ChildNodeEntry childNodeEntry = ns.getChildNodeEntries().get(0);
            PropertyId principalPropId = new PropertyId(childNodeEntry.getId(), J_ACE_PRINCIPAL);
            PropertyState principal = (PropertyState) stateProvider.getItemState(principalPropId);
            InternalValue internalValue = principal.getValues()[0];
            final String principalValue = internalValue.getString();
            if (principalValue.startsWith(USER_PRINCIPAL_PREFIX)) {
                PropertyId grantPropId = new PropertyId(childNodeEntry.getId(), J_ACE_GRANT);
                PropertyState grant = (PropertyState) stateProvider.getItemState(grantPropId);

                PropertyId rolesPropId = new PropertyId(childNodeEntry.getId(), J_ACE_ROLES);
                PropertyState roles = (PropertyState) stateProvider.getItemState(rolesPropId);

                ace.append("/");
                if (grant.getValues()[0].getString().equals("GRANT")) {
                    for (InternalValue value : roles.getValues()) {
                        ace.append(value.getName().getLocalName()).append("/");
                    }
                }
                ace.append(principalValue.substring(USER_PRINCIPAL_PREFIX.length()));
            }
        }
        return ace.toString();
    }

    /**
     * Returns <code>true</code> if ACL-UUID should be resolved and stored in index.
     * This can have a negative effect on performance, when setting rights on a node,
     * which has a large subtree using the same rights, as all these nodes will need
     * to be reindexed. On the other side the advantage is that the queries are faster,
     * as the user rights are resolved faster.
     *
     * @return Returns <code>true</code> if ACL-UUID should be resolved and stored in index.
     */
    public boolean isAddAclUuidInIndex() {
        return addAclUuidInIndex;
    }

    public void setAddAclUuidInIndex(boolean addAclUuidInIndex) {
        this.addAclUuidInIndex = addAclUuidInIndex;
    }

    /**
     * Does this indexer use optimized ACE indexation. Set by the JahiaSearchIndex based
     * on a list of node types allowing this optimization.
     * @return
     */
    public boolean isUseOptimizedACEIndexation() {
        return useOptimizedACEIndexation;
    }

    public void setUseOptimizedACEIndexation(boolean useOptimizedACEIndexation) {
        this.useOptimizedACEIndexation = useOptimizedACEIndexation;
    }

    /**
     * If given node to index is of type jnt:translation create a JahiaTranslationNodeIndexer otherwise a JahiaNodeIndexer is created
     *
     * @param node The <code>NodeState</code> of the node to index
     * @param itemStateManager The persistent item state provider
     * @param nsMappings Namespace mappings to use for indexing
     * @param executor Background task executor used for full text extraction
     * @param parser Parser used for extracting text content from binary properties
     * @param context the <code>QueryHandlerContext</code> instance
     * @return the <code>NodeIndexer</code> to be used for indexing the given node
     * @throws RepositoryException repository exception
     */
    public static JahiaNodeIndexer createNodeIndexer(NodeState node, ItemStateManager itemStateManager, NamespaceMappings nsMappings,
            Executor executor, Parser parser, QueryHandlerContext context) throws RepositoryException {

        if (Constants.JAHIANT_TRANSLATION.equals(getTypeNameAsString(node.getNodeTypeName(), context.getNamespaceRegistry()))) {
            return new JahiaTranslationNodeIndexer(node, itemStateManager, nsMappings, executor, parser, context);
        } else {
            return new JahiaNodeIndexer(node, itemStateManager, nsMappings, executor, parser, context);
        }
    }

}
