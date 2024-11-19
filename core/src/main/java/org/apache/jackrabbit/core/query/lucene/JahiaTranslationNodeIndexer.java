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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.*;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * A {@link org.apache.jackrabbit.core.query.lucene.NodeIndexer} implementation for {@link
 * Constants#JAHIANT_TRANSLATION} nodes.
 *
 * @author Christophe Laprun
 */
public class JahiaTranslationNodeIndexer extends JahiaNodeIndexer {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaTranslationNodeIndexer.class);

    private static final Name MIXIN_TYPES = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "mixinTypes");
    private static final Name PRIMARY_TYPE = NameFactoryImpl.getInstance().create(Name.NS_JCR_URI, "primaryType");

    private String language;
    private NodeState parentNode;
    private ExtendedNodeType parentNodeType;

    protected JahiaTranslationNodeIndexer(NodeState node, ItemStateManager stateProvider, NamespaceMappings mappings, Executor executor,
            Parser parser, QueryHandlerContext context) {

        super(node, stateProvider, mappings, executor, parser, context);
        try {
            for (Name propName : node.getPropertyNames()) {
                if ("language".equals(propName.getLocalName()) && Name.NS_JCR_URI.equals(propName.getNamespaceURI())) {
                    PropertyId id = new PropertyId(node.getNodeId(), propName);
                    PropertyState propState = (PropertyState) stateProvider.getItemState(id);
                    language = propState.getValues()[0].getString();
                    break;
                }
            }
        } catch (Exception e) {
            // shouldn't happen
            logger.debug("Error finding language property", e);
        }
    }

    @Override
    protected ExtendedPropertyDefinition getPropertyDefinition(String fieldName) throws RepositoryException, ItemStateException {
        final int endIndex = fieldName.lastIndexOf("_" + language);
        if (endIndex >= 0) {
            fieldName = fieldName.substring(0, endIndex);
        }

        // try to get the property definition on the parent first since we're dealing with a translation node
        ExtendedPropertyDefinition propDef = getPropertyDefinitionFor(fieldName, getParentNodeType(), parentNode);

        // if we haven't found the property on the parent, it might be on this node so try this
        return propDef != null ? propDef : getPropertyDefinitionFor(fieldName, getNodeType(), node);
    }

    private ExtendedNodeType getParentNodeType() throws RepositoryException, ItemStateException {
        if (parentNodeType == null) {
            final Name parenteNodeTypeName = getParentNodeState().getNodeTypeName();
            parentNodeType = nodeTypeRegistry.getNodeType(getTypeNameAsString(parenteNodeTypeName, namespaceRegistry));
        }

        return parentNodeType;
    }

    private NodeState getParentNodeState() throws ItemStateException {
        if (parentNode == null) {
            parentNode = (NodeState) stateProvider.getItemState(node.getParentId());
        }

        return parentNode;
    }

    @Override
    protected String getFullTextFieldName(String site) {
        return LuceneUtils.getFullTextFieldName(site, language);
    }

    @Override
    public Document createDoc() throws RepositoryException {
        final Document doc = super.createDoc();

        doNotUseInExcerpt.clear();

        try {
            final NodeState parentNodeState = getParentNodeState();
            cleanupNodeProperties(parentNodeState);
            final NodeId parentId = parentNodeState.getParentId();

            if (parentId == null) {
                logger.warn("The node {} is in 'free floating' state. You should run a consistency check/fix on the repository.",
                        parentNodeState.getId());
            } else {
                doc.add(new Field(
                        TRANSLATED_NODE_PARENT, false, parentId.toString(),
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
            }

            if (language == null) {
                logger.warn("The node {} is of type {} but doesn't contain a valid value for the jcr:language property!",
                        node.getId(), Constants.JAHIANT_TRANSLATION);
            } else {
                doc.add(new Field(TRANSLATION_LANGUAGE, language, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
            }

            addProperties(doc, parentNodeState);

            if (isIndexed(J_VISIBILITY) && parentNodeState.hasChildNodeEntry(J_VISIBILITY)) {
                doc.add(new Field(CHECK_VISIBILITY, false, "1",
                        Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS,
                        Field.TermVector.NO));
            }

            // now add fields that are not used in excerpt (must go at the end)
            for (Fieldable field : doNotUseInExcerpt) {
                doc.add(field);
            }
        } catch (ItemStateException e) {
            logger.warn("Error while indexing translation node {}: {}", node.getNodeId(), e.getMessage());
        }

        return doc;
    }

    private void addProperties(Document doc, NodeState parentNodeState) throws RepositoryException {
        // copy properties from parent into translation node, including node types
        final Set<Name> parentNodePropertyNames = new HashSet<>(parentNodeState.getPropertyNames());
        final Set<Name> localNames = new HashSet<>(node.getPropertyNames());
        localNames.remove(PRIMARY_TYPE);
        localNames.remove(MIXIN_TYPES);
        parentNodePropertyNames.removeAll(localNames);
        parentNodePropertyNames.removeAll(getIndexingConfig().getExcludesFromI18NCopy());
        for (Name propName : parentNodePropertyNames) {
            try {
                PropertyId id = new PropertyId(parentNodeState.getNodeId(), propName);
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
            } catch (ItemStateException e) {
                throwRepositoryException(e);
            }
        }
    }

}
