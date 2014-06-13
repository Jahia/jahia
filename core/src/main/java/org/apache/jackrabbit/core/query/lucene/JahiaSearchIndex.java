/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.apache.jackrabbit.core.query.lucene;

import com.google.common.collect.Sets;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.constraint.NoDuplicatesConstraint;
import org.apache.jackrabbit.core.query.lucene.hits.AbstractHitCollector;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.state.*;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.shiro.util.StringUtils;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.QueryObjectModel;
import java.io.IOException;
import java.util.*;

/**
 * Implements a {@link org.apache.jackrabbit.core.query.QueryHandler} using Lucene and handling Jahia specific definitions.
 */
public class JahiaSearchIndex extends SearchIndex {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JahiaSearchIndex.class);

    private static final Name JNT_ACL = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "acl");
    private static final Name JNT_ACE = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "ace");

    private int maxClauseCount = 1024;

    private Boolean versionIndex;

    private int batchSize = 100;

    private boolean addAclUuidInIndex = true;

    private Set<String> typesUsingOptimizedACEIndexation = new HashSet<String>();

    public int getMaxClauseCount() {
        return maxClauseCount;
    }

    public void setMaxClauseCount(int maxClauseCount) {
        this.maxClauseCount = maxClauseCount;
        BooleanQuery.setMaxClauseCount(maxClauseCount);
    }

    @Override
    protected AnalyzerRegistry getAnalyzerRegistry() {
        final IndexingConfiguration indexingConfig = getIndexingConfig();
        if (indexingConfig instanceof JahiaIndexingConfigurationImpl) {
            JahiaIndexingConfigurationImpl config = (JahiaIndexingConfigurationImpl) indexingConfig;
            return config.getAnalyzerRegistry();
        } else {
            return super.getAnalyzerRegistry();
        }
    }

    @Override
    protected IndexingConfiguration createIndexingConfiguration(NamespaceMappings namespaceMappings) {
        final IndexingConfiguration configuration = super.createIndexingConfiguration(namespaceMappings);

        // make sure the AnalyzerRegistry configured in the configuration gets the proper Analyzer
        if (configuration instanceof JahiaIndexingConfigurationImpl) {
            JahiaIndexingConfigurationImpl jahiaConfiguration = (JahiaIndexingConfigurationImpl) configuration;
            final LanguageCustomizingAnalyzerRegistry registry = jahiaConfiguration.getAnalyzerRegistry();

            // retrieve the default analyzer from the Jackrabbit configuration.
            // Should be a JackrabbitAnalyzer instance set with the default Analyzer configured using the 'analyzer'
            // param of the 'SearchIndex' section in repository.xml
            final Analyzer analyzer = super.getTextAnalyzer();
            registry.setDefaultAnalyzer(analyzer);

            // attempt to get a default language specific Analyzer
            final SettingsBean settings = SettingsBean.getInstance();
            final Locale defaultLocale = settings.getDefaultLocale();
            Analyzer specific = registry.getAnalyzer(defaultLocale.toString());
            if (specific == null) {
                specific = registry.getAnalyzer(defaultLocale.getLanguage());
            }

            if (specific != null) {
                // if we've found one, use it
                if (analyzer instanceof JackrabbitAnalyzer) {
                    JackrabbitAnalyzer jrAnalyzer = (JackrabbitAnalyzer) analyzer;
                    jrAnalyzer.setDefaultAnalyzer(specific);
                } else {
                    throw new IllegalArgumentException("Analyzer wasn't a JackrabbitAnalyzer. Couldn't set default language Analyzer as a consequence.");
                }
            }
        }

        return configuration;
    }

    /**
     * Set the maximum number of documents that will be sent in one batch to the index for certain
     * mass indexing requests, like after ACL change
     *
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * We override this method in order to trigger re-indexing on translation nodes, when their
     * parent node is getting re-indexed.
     * <p/>
     * After that we just call the updateNodes from the Jackrabbut SearchIndex implementation.
     *
     * @param remove ids of nodes to remove.
     * @param add    NodeStates to add. Calls to <code>next()</code> on this
     *               iterator may return <code>null</code>, to indicate that a
     *               node could not be indexed successfully.
     * @throws RepositoryException if an error occurs while indexing a node.
     * @throws IOException         if an error occurs while updating the index.
     */
    @Override
    public void updateNodes(Iterator<NodeId> remove, Iterator<NodeState> add)
            throws RepositoryException, IOException {

        if (isVersionIndex()) {
            super.updateNodes(remove, add);
            return;
        }

        final List<NodeState> addList = new ArrayList<NodeState>();
        final List<NodeId> removeList = new ArrayList<NodeId>();
        final Set<NodeId> removedIds = new HashSet<NodeId>();
        final Set<NodeId> addedIds = new HashSet<NodeId>();
        final List<NodeId> aclChangedList = new ArrayList<NodeId>();

        boolean hasAclOrAce = false;
        while (add.hasNext()) {
            final NodeState state = add.next();
            if (state != null) {
                addedIds.add(state.getNodeId());
                addList.add(state);

                if (!hasAclOrAce
                        && (JNT_ACL.equals(state.getNodeTypeName()) ||
                        JNT_ACE.equals(state.getNodeTypeName()))) {
                    hasAclOrAce = true;
                }
            }
        }
        while (remove.hasNext()) {
            NodeId nodeId = remove.next();
            removedIds.add(nodeId);
            removeList.add(nodeId);
        }

        if (isAddAclUuidInIndex() && hasAclOrAce) {
            final ItemStateManager itemStateManager = getContext().getItemStateManager();
            for (final NodeState node : new ArrayList<NodeState>(addList)) {
                try {
                    // if acl node is added for the first time we need to add our ACL_UUID field
                    // to parent's and all affected subnodes' index documents
                    if (JNT_ACL.equals(node.getNodeTypeName())) {
                        NodeState nodeParent = (NodeState) itemStateManager.getItemState(node
                                .getParentId());
                        addIdToBeIndexed(nodeParent.getNodeId(), addedIds, removedIds, addList, removeList);
                        recurseTreeForAclIdSetting(nodeParent, addedIds, removedIds, aclChangedList, itemStateManager);
                    }
                    // if an acl is modified, we need to reindex all its subnodes only if we use the optimized ACE
                    if (JNT_ACE.equals(node.getNodeTypeName())) {
                        NodeState acl = (NodeState) itemStateManager.getItemState(node.getParentId());
                        NodeState nodeParent = (NodeState) itemStateManager.getItemState(acl.getParentId());
                        if (canUseOptimizedACEIndexation(nodeParent)) {
                            addIdToBeIndexed(nodeParent.getNodeId(), addedIds, removedIds, addList, removeList);
                            recurseTreeForAclIdSetting(nodeParent, addedIds, removedIds, aclChangedList, itemStateManager);
                        }
                    }
                } catch (ItemStateException e) {
                    log.warn("ACL_UUID field in documents may not be updated, so access rights check in search may not work correctly", e);
                }
            }
        }

        long timer = System.currentTimeMillis();

        super.updateNodes(removeList.iterator(), addList.iterator());

        if (log.isDebugEnabled()) {
            log.debug("Re-indexed nodes in {} ms: {} removed, {} added", new Object[]{
                    (System.currentTimeMillis() - timer), removeList.size(), addList.size()});
        }

        if (!aclChangedList.isEmpty()) {
            int aclSubListStart = 0;
            int aclSubListEnd = Math.min(aclChangedList.size(), batchSize);
            while (aclSubListStart < aclChangedList.size()) {
                if (aclSubListStart > 0) {
                    Thread.yield();
                }
                List<NodeState> aclAddList = new ArrayList<NodeState>();
                List<NodeId> aclRemoveList = new ArrayList<NodeId>();
                for (final NodeId node : aclChangedList.subList(aclSubListStart, aclSubListEnd)) {
                    try {
                        addIdToBeIndexed(node, addedIds, removedIds,
                                aclAddList, aclRemoveList);
                    } catch (ItemStateException e) {
                        log.warn("ACL_UUID field in document for nodeId '" + node.toString() + "' may not be updated, so access rights check in search may not work correctly", e);
                    }
                }

                super.updateNodes(aclRemoveList.iterator(), aclAddList.iterator());

                aclSubListStart += batchSize;
                aclSubListEnd = Math.min(aclChangedList.size(), aclSubListEnd + batchSize);
            }
            if (log.isDebugEnabled()) {
                log.debug("Re-indexed {} nodes after ACL change in {} ms", new Object[]{aclChangedList.size(),
                        (System.currentTimeMillis() - timer)});
            }
        }
    }

    private void recurseTreeForAclIdSetting(NodeState node, Set<NodeId> addedIds, Set<NodeId> removedIds, List<NodeId> aclChangedList, ItemStateManager itemStateManager) {
        for (ChildNodeEntry childNodeEntry : node.getChildNodeEntries()) {
            try {
                NodeState childNode = (NodeState) getContext().getItemStateManager().getItemState(childNodeEntry.getId());
                boolean breakInheritance = false;
                if (childNode.hasPropertyName(JahiaNodeIndexer.J_ACL_INHERITED)) {
                    PropertyId propId = new PropertyId((NodeId) childNode.getId(), JahiaNodeIndexer.J_ACL_INHERITED);
                    PropertyState ps = (PropertyState) itemStateManager.getItemState(propId);
                    if (ps.getValues().length == 1) {
                        if (ps.getValues()[0].getBoolean()) {
                            breakInheritance = true;
                        }
                    }
                }
                if (!breakInheritance) {
                    if (!addedIds.contains(childNodeEntry.getId()) && !removedIds.contains(childNodeEntry.getId())) {
                        aclChangedList.add(childNodeEntry.getId());
                    }
                    recurseTreeForAclIdSetting(childNode, addedIds, removedIds, aclChangedList, itemStateManager);
                }
            } catch (ItemStateException e) {
                log.warn("ACL_UUID field in document for nodeId '{}' may not be updated, so access rights check in search may not work correctly", childNodeEntry.getId().toString());
                log.debug("Exception when checking for creating ACL_UUID in index", e);
            } catch (RepositoryException e) {
                log.warn("ACL_UUID field in document for nodeId '{}' may not be updated, so access rights check in search may not work correctly", childNodeEntry.getId().toString());
                log.debug("Exception when checking for creating ACL_UUID in index", e);
            }
        }
    }

    private void addIdToBeIndexed(NodeId id, Set<NodeId> addedIds, Set<NodeId> removedIds, List<NodeState> addList, List<NodeId> removeList) throws ItemStateException {
        if (!removedIds.contains(id)
                && !addedIds.contains(id)) {
            removeList.add(id);
            removedIds.add(id);
        }
        if (!addedIds.contains(id)
                && getContext().getItemStateManager().hasItemState(id)) {
            addList.add((NodeState) getContext().getItemStateManager().getItemState(id));
            addedIds.add(id);
        }
    }

    @Override
    protected Document createDocument(final NodeState node, NamespaceMappings nsMappings,
                                      IndexFormatVersion indexFormatVersion) throws RepositoryException {
        JahiaNodeIndexer indexer = JahiaNodeIndexer.createNodeIndexer(node, getContext().getItemStateManager(),
                nsMappings, getContext().getExecutor(), getParser(), getContext());
        indexer.setSupportHighlighting(getSupportHighlighting());
        indexer.setIndexingConfiguration(getIndexingConfig());
        indexer.setIndexFormatVersion(indexFormatVersion);
        indexer.setMaxExtractLength(getMaxExtractLength());
        indexer.setSupportSpellchecking(getSpellCheckerClass() != null);
        indexer.setAddAclUuidInIndex(addAclUuidInIndex);
        indexer.setUseOptimizedACEIndexation(canUseOptimizedACEIndexation(node));
        Document doc = indexer.createDoc();
        mergeAggregatedNodeIndexes(node, doc, indexFormatVersion);
        return doc;
    }

    /**
     * Check if this node state can use the optimized ACE indexation, based on the configured nodetypes
     *
     * @param currentNode
     * @return
     * @throws RepositoryException
     */
    private boolean canUseOptimizedACEIndexation(NodeState currentNode) throws RepositoryException {
        final ExtendedNodeType nodeType = NodeTypeRegistry.getProviderNodeTypeRegistry().getNodeType(JahiaNodeIndexer.getTypeNameAsString(currentNode.getNodeTypeName(), getContext().getNamespaceRegistry()));
        for (String type : typesUsingOptimizedACEIndexation) {
            if (nodeType.isNodeType(type)) {
                return true;
            }
        }
        return false;
    }

//    /**
//     * Executes the query on the search index.
//     *
//     * @param session         the session that executes the query.
//     * @param query           the query.
//     * @param orderings       the order specs for the sort order.
//     * @param resultFetchHint a hint on how many results should be fetched.
//     * @return the query hits.
//     * @throws IOException if an error occurs while searching the index.
//     */
//    public MultiColumnQueryHits executeQuery(SessionImpl session, MultiColumnQuery query,
//                                             Ordering[] orderings, long resultFetchHint, boolean createFacets) throws IOException {
//        checkOpen();
//
//        final IndexReader reader = getIndexReader();
//        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(session, reader,
//                getContext().getItemStateManager());
//        searcher.setSimilarity(getSimilarity());
//        MultiColumnQueryHits hits = query.execute(searcher, orderings, resultFetchHint);
//        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits,
//                query instanceof JahiaQueryImpl ? ((JahiaQueryImpl) query).getConstraint()
//                        : null, searcher) {
//            public void close() throws IOException {
//                try {
//                    super.close();
//                } finally {
//                    PerQueryCache.getInstance().dispose();
//                    Util.closeOrRelease(reader);
//                }
//            }
//        };
//        return filteredHits;
//    }
//
//    /**
//     * Executes the query on the search index.
//     *
//     * @param session         the session that executes the query.
//     * @param queryImpl       the query impl.
//     * @param query           the lucene query.
//     * @param orderProps      name of the properties for sort order.
//     * @param orderSpecs      the order specs for the sort order properties. <code>true</code> indicates ascending order, <code>false</code> indicates
//     *                        descending.
//     * @param resultFetchHint a hint on how many results should be fetched.
//     * @return the query hits.
//     * @throws IOException if an error occurs while searching the index.
//     */
//    @Override
//    public MultiColumnQueryHits executeQuery(SessionImpl session, AbstractQueryImpl queryImpl,
//                                             Query query, Path[] orderProps, boolean[] orderSpecs, long resultFetchHint)
//            throws IOException {
//        checkOpen();
//
//        Sort sort = new Sort(createSortFields(orderProps, orderSpecs));
//
//        final IndexReader reader = getIndexReader(queryImpl.needsSystemTree());
//        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(session, reader,
//                getContext().getItemStateManager());
//        searcher.setSimilarity(getSimilarity());
//        MultiColumnQueryHits hits = searcher.execute(query, sort, resultFetchHint,
//                QueryImpl.DEFAULT_SELECTOR_NAME);
//        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits,
//                queryImpl instanceof JahiaQueryImpl ? ((JahiaQueryImpl) queryImpl).getConstraint()
//                        : null, searcher) {
//            public void close() throws IOException {
//                try {
//                    super.close();
//                } finally {
//                    PerQueryCache.getInstance().dispose();
//                    Util.closeOrRelease(reader);
//                }
//            }
//        };
//
//        return filteredHits;
//    }

    @Override
    public QueryObjectModel createQueryObjectModel(
            SessionContext sessionContext,
            QueryObjectModelTree qomTree, String language, Node node) throws RepositoryException {
        JahiaQueryObjectModelImpl query = new JahiaQueryObjectModelImpl();
        query.init(sessionContext, this, qomTree, language, node);
        return query;
    }

    @Override
    public ExecutableQuery createExecutableQuery(SessionContext sessionContext,
                                                 String statement, String language) throws InvalidQueryException {
        JahiaQueryImpl query = new JahiaQueryImpl(sessionContext, this,
                getContext().getPropertyTypeRegistry(), statement, language, getQueryNodeFactory());
        query.setConstraint(new NoDuplicatesConstraint());
        query.setRespectDocumentOrder(getRespectDocumentOrder());
        return query;
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<NodeId> getWeaklyReferringNodes(NodeId id)
            throws RepositoryException, IOException {
        final List<Integer> docs = new ArrayList<Integer>();
        final List<NodeId> ids = new ArrayList<NodeId>();
        final IndexReader reader = getIndexReader(false);
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            try {
                Query q = new TermQuery(new Term(
                        FieldNames.WEAK_REFS, id.toString()));
                searcher.search(q, new AbstractHitCollector() {
                    public void collect(int doc, float score) {
                        docs.add(doc);
                    }
                });
            } finally {
                searcher.close();
            }
            for (Integer doc : docs) {
                Document d = reader.document(doc, FieldSelectors.UUID);
                ids.add(new NodeId(d.get(FieldNames.UUID)));
            }
        } finally {
            Util.closeOrRelease(reader);
        }
        return ids;
    }

    /**
     * Returns <code>true</code> if the current search index corresponds to the index of the version store.
     *
     * @return <code>true</code> if the current search index corresponds to the index of the version store
     */
    private boolean isVersionIndex() {
        if (versionIndex == null) {
            versionIndex = getIndexingConfigurationClass().equals(IndexingConfigurationImpl.class.getName());
        }

        return versionIndex;
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
     * Return the list of types which can benefit of the optimized ACE indexation.
     *
     * @return
     */
    public Set<String> getTypesUsingOptimizedACEIndexation() {
        return typesUsingOptimizedACEIndexation;
    }

    public void setTypesUsingOptimizedACEIndexation(String typesUsingOptimizedACEIndexation) {
        this.typesUsingOptimizedACEIndexation = Sets.newHashSet(StringUtils.split(typesUsingOptimizedACEIndexation));
    }

    @Override
    protected Parser createParser() {
        // we disable binary indexing by Jackrabbit (is done by Jahia), so we do not need the parser
        return null;
    }
}
