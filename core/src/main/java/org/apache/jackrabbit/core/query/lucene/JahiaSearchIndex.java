/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.JahiaSearchManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.constraint.NoDuplicatesConstraint;
import org.apache.jackrabbit.core.query.lucene.hits.AbstractHitCollector;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.state.*;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.NameFactory;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.tika.parser.Parser;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.search.spell.CompositeSpellChecker;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DateUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.QueryObjectModel;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements a {@link org.apache.jackrabbit.core.query.QueryHandler} using Lucene and handling Jahia specific definitions.
 */
public class JahiaSearchIndex extends SearchIndex {

    /**
     * Background job that performs re-indexing of the repository content for the specified workspaces.
     */
    public static class ReindexJob extends BackgroundJob implements StatefulJob {
        @Override
        public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
            JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            JahiaSearchIndex index = (JahiaSearchIndex) map.get("index");
            if (index != null) {
                index.reindexAndSwitch();
            } else {
                @SuppressWarnings("unchecked")
                List<JahiaSearchIndex> indexes = (List<JahiaSearchIndex>) map.get("indexes");
                if (indexes != null) {
                    long start = System.currentTimeMillis();
                    for (Iterator<JahiaSearchIndex> it = indexes.iterator(); it.hasNext(); ) {
                        JahiaSearchIndex searchIndex = it.next();
                        try {
                            searchIndex.reindexAndSwitch();
                        } catch (Exception e) {
                            // reset newIndex of every indexes that won't be processed,
                            // otherwise re-indexing of those ones won't be possible
                            // until restart (see prepareReindexing() and scheduleReindexing())
                            while (it.hasNext()) {
                                it.next().newIndex = null;
                            }
                            throw e;
                        }
                    }
                    log.info("Re-indexing of the whole repository content took {}",
                            DateUtils.formatDurationWords(System.currentTimeMillis() - start));
                }
            }
        }
    }

    /**
     * Returns <code>true</code> if the supplied {@link SearchIndex} implementation has ACL-UUID stored in index.
     *
     * @return <code>true</code> if the supplied {@link SearchIndex} implementation has ACL-UUID stored in index
     */
    public static boolean isAclUuidInIndex(SearchIndex index) {
        return index instanceof JahiaSearchIndex && ((JahiaSearchIndex) index).isAddAclUuidInIndex();
    }

    private static final Logger log = LoggerFactory.getLogger(JahiaSearchIndex.class);

    private static final Name JNT_ACL = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "acl");

    private static final Name JNT_ACE = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "ace");

    public static final String SKIP_VERSION_INDEX_SYSTEM_PROPERTY = "jahia.jackrabbit.searchIndex.skipVersionIndex";

    public static final boolean SKIP_VERSION_INDEX = Boolean.parseBoolean(System.getProperty(SKIP_VERSION_INDEX_SYSTEM_PROPERTY, "true"));

    private Boolean versionIndex;

    private int maxClauseCount = 1024;

    private int batchSize = 100;

    private boolean addAclUuidInIndex = true;

    private Set<String> typesUsingOptimizedACEIndexation = new HashSet<>();

    private Set<Name> ignoredTypes;

    private String ignoredTypesString;

    private volatile boolean switching = false;

    private int defaultWaitTime = 500;

    private volatile JahiaSecondaryIndex newIndex;

    public int getMaxClauseCount() {
        return maxClauseCount;
    }

    public void setMaxClauseCount(int maxClauseCount) {
        this.maxClauseCount = maxClauseCount;
        BooleanQuery.setMaxClauseCount(maxClauseCount);
    }

    public int getBatchSize() {
        return batchSize;
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

    public void setIgnoredTypes(String ignoredTypes) {
        this.ignoredTypesString = ignoredTypes;
    }

    public int getDefaultWaitTime() {
        return defaultWaitTime;
    }

    public void setDefaultWaitTime(int defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    @Override
    protected void doInit() throws IOException {
        Set<Name> ignoredTypes = new HashSet<>();
        NameFactory nf = NameFactoryImpl.getInstance();
        if (SKIP_VERSION_INDEX && isVersionIndex()) {
            ignoredTypes.add(nf.create(Name.NS_REP_URI, "versionStorage"));
            ignoredTypes.add(nf.create(Name.NS_NT_URI, "versionHistory"));
            ignoredTypes.add(nf.create(Name.NS_NT_URI, "version"));
            ignoredTypes.add(nf.create(Name.NS_NT_URI, "versionLabels"));
            ignoredTypes.add(nf.create(Name.NS_NT_URI, "frozenNode"));
            ignoredTypes.add(nf.create(Name.NS_NT_URI, "versionedChild"));
        }
        if (ignoredTypesString != null) {
            for (String s : StringUtils.split(ignoredTypesString, ", ")) {
                try {
                    if (!s.startsWith("{")) {
                        try {
                            ignoredTypes.add(nf.create(
                                    getContext().getNamespaceRegistry().getURI(StringUtils.substringBefore(s, ":")),
                                    StringUtils.substringAfter(s, ":")));
                        } catch (NamespaceException e) {
                            log.error("Cannot parse namespace for " + s, e);
                        }
                    } else {
                        ignoredTypes.add(nf.create(s));
                    }
                } catch (IllegalArgumentException iae) {
                    log.error("Illegal node type name: " + s, iae);
                }
            }
        }
        this.ignoredTypes = ignoredTypes.isEmpty() ? null : ignoredTypes;
        super.doInit();
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
            final LanguageCustomizingAnalyzerRegistry registry = ((JahiaIndexingConfigurationImpl) configuration).getAnalyzerRegistry();
            registry.setDefaultAnalyzer(super.getTextAnalyzer());

            // attempt to get a default language specific Analyzer
            Locale defaultLocale = SettingsBean.getInstance().getDefaultLocale();
            Analyzer analyzer = getLanguageSpecificAnalyzer(registry, defaultLocale);
            if (analyzer != null) {
                // Override default analyzer
                setAnalyzer(analyzer);
            }
        }

        return configuration;
    }

    private void waitForIndexSwitch() throws IOException {
        while (switching) {
            try {
                Thread.sleep(defaultWaitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Current thread has been interrupted", e);
            }
        }
    }

    @Override
    protected IndexReader getIndexReader(boolean includeSystemIndex) throws IOException {
        waitForIndexSwitch();
        return super.getIndexReader(includeSystemIndex);
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
        updateNodes(remove, add, true);
    }

    void updateNodes(Iterator<NodeId> remove, Iterator<NodeState> add, boolean waitForIndexSwitch)
            throws RepositoryException, IOException {

        if (waitForIndexSwitch) {
            waitForIndexSwitch();

            if (ignoredTypes != null && add.hasNext()) {
                List<NodeState> l = null;
                while (add.hasNext()) {
                    NodeState state = add.next();
                    if (state != null && !ignoredTypes.contains(state.getNodeTypeName())) {
                        if (l == null) {
                            l = new LinkedList<>();
                        }
                        l.add(state);
                    }
                }
                add = l != null ? l.iterator() : Collections.<NodeState>emptyIterator();
            }
            if (newIndex != null) {
                newIndex.addDelayedUpdated(remove, add);
            }
        }

        if (isVersionIndex()) {
            super.updateNodes(remove, add);
            return;
        }

        final List<NodeState> addList = new ArrayList<>();
        final List<NodeId> removeList = new ArrayList<>();
        final Set<NodeId> removedIds = new HashSet<>();
        final Set<NodeId> addedIds = new HashSet<>();
        final List<NodeId> aclChangedList = new ArrayList<>();
        final Set<NodeId> topIdsRecursedForAcl = new HashSet<>();

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

        boolean debugEnabled = log.isDebugEnabled();

        if (isAddAclUuidInIndex() && hasAclOrAce) {
            final ItemStateManager itemStateManager = getContext().getItemStateManager();
            for (final NodeState node : new ArrayList<NodeState>(addList)) {
                try {
                    // if an acl node is added, removed or j:inherit property is changed we need to add/modify ACL_UUID field
                    // into parent's and all affected subnodes' index documents
                    Event event = null;
                    if (add instanceof JahiaSearchManager.NodeStateIterator) {
                        event = ((JahiaSearchManager.NodeStateIterator) add).getEvent(node.getNodeId());
                        // skip adding subnodes if just a property changed and its not j:inherit
                        if (event != null && event.getType() != Event.NODE_ADDED && event.getType() != Event.NODE_REMOVED) {
                            if (!(JNT_ACL.equals(node.getNodeTypeName()) && event.getPath().endsWith("/j:inherit"))) {
                                continue;
                            }
                        }
                    }

                    if (JNT_ACL.equals(node.getNodeTypeName())) {
                        NodeState nodeParent = (NodeState) itemStateManager.getItemState(node
                                .getParentId());
                        addIdToBeIndexed(nodeParent.getNodeId(), addedIds, removedIds, addList, removeList);
                        if (!topIdsRecursedForAcl.contains(nodeParent.getNodeId()) && !aclChangedList.contains(nodeParent.getNodeId())) {
                            long startTime = debugEnabled ? System.currentTimeMillis() : 0;

                            recurseTreeForAclIdSetting(nodeParent, addedIds, removedIds, aclChangedList, itemStateManager);
                            topIdsRecursedForAcl.add(node.getParentId());

                            if (debugEnabled) {
                                log.debug("ACL updated {}. Recursed down the JCR tree to update the index in {} ms.",
                                        event != null ? event.getPath() : nodeParent.getId(),
                                        System.currentTimeMillis() - startTime);
                            }
                        }
                    }
                    // if an ace is modified, we need to reindex all its subnodes only if we can use the optimized ACE
                    if (JNT_ACE.equals(node.getNodeTypeName())) {
                        NodeState acl = (NodeState) itemStateManager.getItemState(node.getParentId());
                        NodeState nodeParent = (NodeState) itemStateManager.getItemState(acl.getParentId());
                        if (canUseOptimizedACEIndexation(nodeParent)) {
                            addIdToBeIndexed(nodeParent.getNodeId(), addedIds, removedIds, addList, removeList);
                            if (!topIdsRecursedForAcl.contains(nodeParent.getNodeId()) && !aclChangedList.contains(nodeParent.getNodeId())) {
                                long startTime = debugEnabled ? System.currentTimeMillis() : 0;

                                recurseTreeForAclIdSetting(nodeParent, addedIds, removedIds, aclChangedList, itemStateManager);
                                topIdsRecursedForAcl.add(node.getParentId());

                                if (debugEnabled) {
                                    log.debug(
                                            "ACE entry updated: {}. Recursed down the JCR tree to update the index in {} ms.",
                                            event != null ? event.getPath() : nodeParent.getId(),
                                            System.currentTimeMillis() - startTime);
                                }
                            }
                        }
                    }
                } catch (ItemStateException | RepositoryException e) {
                    log.warn("ACL_UUID field in documents may not be updated, so access rights check in search may not work correctly", e);
                }
            }
        }

        long timer = System.currentTimeMillis();

        try {
            super.updateNodes(removeList.iterator(), addList.iterator());
        } catch (AlreadyClosedException e) {
            if (!switching) {
                throw e;
            }
            // If switching index, updates will be handled in delayed updates
        }

        if (debugEnabled) {
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
                List<NodeState> aclAddList = new ArrayList<>();
                List<NodeId> aclRemoveList = new ArrayList<>();
                for (final NodeId node : aclChangedList.subList(aclSubListStart, aclSubListEnd)) {
                    try {
                        addIdToBeIndexed(node, addedIds, removedIds,
                                aclAddList, aclRemoveList);
                    } catch (ItemStateException e) {
                        log.warn("ACL_UUID field in document for nodeId '" + node.toString() + "' may not be updated, so access rights check in search may not work correctly", e);
                    }
                }

                try {
                    super.updateNodes(aclRemoveList.iterator(), aclAddList.iterator());
                } catch (AlreadyClosedException e) {
                    if (!switching) {
                        throw e;
                    }
                    // If switching index, updates will be handled in delayed updates
                }

                aclSubListStart += batchSize;
                aclSubListEnd = Math.min(aclChangedList.size(), aclSubListEnd + batchSize);
            }
            if (debugEnabled) {
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
            } catch (ItemStateException | RepositoryException e) {
                log.warn("ACL_UUID field in document for nodeId '{}' may not be updated, so access rights check in search may not work correctly", childNodeEntry.getId());
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

        if (getIndexingConfig() instanceof JahiaIndexingConfigurationImpl
                && !((JahiaIndexingConfigurationImpl) getIndexingConfig()).getExcludesTypesByPath().isEmpty() && isNodeExcluded(node)) {
            return null;
        }

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

    protected boolean isNodeExcluded(final NodeState node) throws RepositoryException {
        try {
            // manage translation nodes
            String nodeTypeName = JahiaNodeIndexer.getTypeNameAsString(node.getNodeTypeName(), getContext().getNamespaceRegistry());
            NodeState nodeToProcess = Constants.JAHIANT_TRANSLATION.equals(nodeTypeName)
                    ? (NodeState) getContext().getItemStateManager().getItemState(node.getParentId()) : node;
            String localPath = null;
            for (JahiaIndexingConfigurationImpl.ExcludedType excludedType : ((JahiaIndexingConfigurationImpl) getIndexingConfig())
                    .getExcludesTypesByPath()) {
                if (!excludedType.matchesNodeType(nodeToProcess)) {
                    continue;
                }
                if (localPath == null) {
                    localPath = StringUtils.remove(getNamespaceMappings()
                            .translatePath(getContext().getHierarchyManager().getPath(nodeToProcess.getId()).getNormalizedPath()), "0:");
                }
                if (excludedType.matchPath(localPath)) {
                    // do not index the content
                    return true;
                }
            }
        } catch (ItemStateException e) {
            log.debug("While indexing translation node unable to get its parent item", e);
            return true;
        }
        return false;
    }

    /**
     * Check if this node state can use the optimized ACE indexation, based on the configured nodetypes
     *
     * @param currentNode
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    private boolean canUseOptimizedACEIndexation(NodeState currentNode) throws RepositoryException {
        final ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(JahiaNodeIndexer.getTypeNameAsString(currentNode.getNodeTypeName(), getContext().getNamespaceRegistry()));
        for (String type : typesUsingOptimizedACEIndexation) {
            if (nodeType.isNodeType(type)) {
                return true;
            }
        }
        return false;
    }

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
    @Override
    public Iterable<NodeId> getWeaklyReferringNodes(NodeId id)
            throws RepositoryException, IOException {
        final List<Integer> docs = new ArrayList<>();
        final List<NodeId> ids = new ArrayList<>();
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

    @Override
    protected Parser createParser() {
        // we disable binary indexing by Jackrabbit (is done by Jahia), so we do not need the parser
        return null;
    }


    /**
     * Prepares for a re-indexing of the repository content, creating a secondary index instance.
     */
    public synchronized boolean prepareReindexing() {
        if (newIndex != null || switching) {
            return false;
        }

        newIndex = new JahiaSecondaryIndex(this);

        return true;
    }

    /**
     * Schedules the re-indexing of the repository content in a background job.
     */
    public void scheduleReindexing() {
        if (!prepareReindexing()) {
            return;
        }

        JobDetail jobDetail = BackgroundJob.createJahiaJob(
                "Re-indexing of the " + StringUtils.defaultIfEmpty(getContext().getWorkspace(), "system")
                        + " workspace content", ReindexJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put("index", this);
        try {
            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail, true);
        } catch (SchedulerException e) {
            log.error("Unable to schedule background job for re-indexing", e);
        }
    }

    synchronized void reindexAndSwitch() throws RepositoryException, IOException {
        final long startTime = System.currentTimeMillis();
        final File dest = new File(getPath() + ".old." + System.currentTimeMillis());
        final String workspace = StringUtils.defaultIfEmpty(getContext().getWorkspace(), "system");

        FileUtils.deleteQuietly(new File(newIndex.getPath()));

        log.info("Start initializing new index for {} workspace", workspace);
        try {
            newIndex.newIndexInit();
            log.info("New index for workspace {} initialized in {} ms", workspace, System.currentTimeMillis() - startTime);
            newIndex.replayDelayedUpdates(newIndex);
        } catch (IOException | RepositoryException e) {
            // cleanup state before aborting if anything goes wrong
            FileUtils.deleteQuietly(new File(newIndex.getPath()));
            newIndex = null;
            throw e;
        }
        log.info("Reindexing has finished for {} workspace, switching to new index...", workspace);

        long startTimeIntern = System.currentTimeMillis();
        boolean indexClosed = false;
        try {
            switching = true;
            quietClose(newIndex);

            if (!new File(getPath()).canWrite()) {
                // Verify that the existing index folder can be renamed before closing the related index
                throw new IOException("Unable to rename the existing index folder " + getPath());
            }

            // Close the existing index. If anything goes wrong while completing the switch, this index
            // won't be usable as this anymore!
            quietClose(this);
            indexClosed = true;

            if (!new File(getPath()).renameTo(dest)) {
                throw new IOException("Unable to rename the existing index folder " + getPath());
            }

            if (!new File(newIndex.getPath()).renameTo(new File(getPath()))) {
                // rename the index back
                log.info("Restored original index");
                dest.renameTo(new File(getPath()));
                throw new IOException("Unable to rename the newly created index folder " + newIndex.getPath());
            }

            log.info("New index deployed, reloading {}", getPath());
            init(fs, getContext());
            newIndex.replayDelayedUpdates(this);
            log.info("New index ready");

        } catch (IOException e) {
            FileUtils.deleteQuietly(new File(newIndex.getPath()));
            if (indexClosed) {
                // attempt to reopen
                init(fs, getContext());
            }
            throw e;
        } finally {
            newIndex = null;
            switching = false;
        }
        log.info("Switched to newly created index in {} ms", System.currentTimeMillis() - startTimeIntern);

        FileUtils.deleteQuietly(dest);

        SpellChecker spellChecker = getSpellChecker();
        if (spellChecker instanceof CompositeSpellChecker) {
            ((CompositeSpellChecker) spellChecker).updateIndex(false);
            log.info("Triggered update of the spellchecker index");
        }

        log.info("Re-indexing operation is completed for {} workspace in {}", workspace,
                DateUtils.formatDurationWords(System.currentTimeMillis() - startTime));
    }

    private void quietClose(JahiaSearchIndex index) {
        try {
            if (index.getSpellChecker() != null) {
                index.getSpellChecker().close();
            }
        } catch (Exception e) {
            log.warn("Unable to close spell checker", e);
        }
        try {
            index.index.close();
        } catch (Exception e) {
            log.warn("Unable to close index", e);
        }
    }

    /**
     * Re-indexes the full JCR sub-tree, starting from the specified node.
     *
     * @param startNodeId the UUID of the node to start re-indexing with
     * @throws RepositoryException      if an error occurs while indexing a node.
     * @throws NoSuchItemStateException in case of JCR errors
     * @throws IllegalArgumentException in case of JCR errors
     * @throws ItemStateException       in case of JCR errors
     * @throws IOException              if an error occurs while updating the index
     */
    public void reindexTree(String startNodeId) throws RepositoryException, NoSuchItemStateException,
            IllegalArgumentException, ItemStateException, IOException {
        long startTime = System.currentTimeMillis();
        log.info("Requested re-indexing of the JCR tree for node {}", startNodeId);
        ItemStateManager stateManager = getContext().getItemStateManager();
        List<NodeState> nodes = new LinkedList<>();
        collectChildren((NodeState) stateManager.getItemState(NodeId.valueOf(startNodeId)), stateManager, nodes);

        int totalCount = nodes.size();
        log.info("Collected {} node IDs to be re-indexed", totalCount);

        List<NodeId> removed = new LinkedList<>();
        for (NodeState n : nodes) {
            removed.add(n.getNodeId());
        }

        if (totalCount > batchSize) {
            // will process in batches
            log.info("Will process re-indexig of nodes in batches of {} nodes", batchSize);
            int listStart = 0;
            int listEnd = Math.min(totalCount, batchSize);
            while (listStart < totalCount) {
                if (listStart > 0) {
                    Thread.yield();
                }
                super.updateNodes(removed.subList(listStart, listEnd).iterator(),
                        nodes.subList(listStart, listEnd).iterator());

                if (listEnd % (10 * batchSize) == 0) {
                    log.info("Re-indexed {} nodes out of {}", listEnd, totalCount);
                }

                listStart += batchSize;
                listEnd = Math.min(totalCount, listEnd + batchSize);
            }
        } else {
            super.updateNodes(removed.iterator(), nodes.iterator());
        }

        log.info("Done re-indexed JCR sub-tree for node {} in {} ms", startNodeId,
                System.currentTimeMillis() - startTime);
    }

    private static void collectChildren(NodeState startNode, ItemStateManager stateManager, List<NodeState> nodes) {
        nodes.add(startNode);
        for (ChildNodeEntry child : startNode.getChildNodeEntries()) {
            try {
                collectChildren((NodeState) stateManager.getItemState(child.getId()), stateManager, nodes);
            } catch (ItemStateException e) {
                log.warn("Unable to obtain state for the node " + child.getId() + ". Cause: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Executes the query on the search index.
     *
     * @param session         the session that executes the query.
     * @param queryImpl       the query impl.
     * @param query           the lucene query.
     * @param orderProps      name of the properties for sort order.
     * @param orderSpecs      the order specs for the sort order properties.
     *                        <code>true</code> indicates ascending order,
     *                        <code>false</code> indicates descending.
     * @param orderFuncs      functions for the properties for sort order.
     * @param resultFetchHint a hint on how many results should be fetched.  @return the query hits.
     * @throws IOException if an error occurs while searching the index.
     */
    @Override
    public MultiColumnQueryHits executeQuery(SessionImpl session,
                                             AbstractQueryImpl queryImpl,
                                             Query query,
                                             Path[] orderProps,
                                             boolean[] orderSpecs,
                                             String[] orderFuncs, long resultFetchHint)
            throws IOException {
        checkOpen();

        Sort sort = new Sort(createSortFields(orderProps, orderSpecs, orderFuncs));

        final IndexReader reader = getIndexReader(queryImpl.needsSystemTree());
        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(
                session, reader, getContext().getItemStateManager());
        searcher.setSimilarity(getSimilarity());
        return new JahiaFilterMultiColumnQueryHits(
                searcher.execute(query, sort, resultFetchHint,
                        QueryImpl.DEFAULT_SELECTOR_NAME), reader) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    Util.closeOrRelease(reader);
                }
            }
        };
    }

    /**
     * Executes the query on the search index.
     *
     * @param session         the session that executes the query.
     * @param query           the query.
     * @param orderings       the order specs for the sort order.
     * @param resultFetchHint a hint on how many results should be fetched.
     * @return the query hits.
     * @throws IOException if an error occurs while searching the index.
     */
    @Override
    public MultiColumnQueryHits executeQuery(SessionImpl session,
                                             MultiColumnQuery query,
                                             Ordering[] orderings,
                                             long resultFetchHint)
            throws IOException {
        checkOpen();

        final IndexReader reader = getIndexReader();
        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(
                session, reader, getContext().getItemStateManager());
        searcher.setSimilarity(getSimilarity());
        return new JahiaFilterMultiColumnQueryHits(
                query.execute(searcher, orderings, resultFetchHint), reader) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    Util.closeOrRelease(reader);
                }
            }
        };
    }

    public void switchReadOnlyMode(boolean enable) {
        try {
            IndexMerger merger = getValue(index, "merger");
            if (enable) {
                // Enable read-only mode. Set indexMerger to "not started" to start queuing workers without starting them
                AtomicBoolean isStarted = getValue(merger, "isStarted");
                isStarted.set(false);

                // Get all queued workers, wait for running workers to be finished
                List busyMergers = getValue(merger, "busyMergers");
                Object[] workers = busyMergers.toArray(new Object[0]);

                for (Object worker : workers) {
                    CountDownLatch start = getValue(worker, "start");
                    // If worker is already started, wait until it's finished
                    if (start.getCount() == 0) {
                        getMethod(worker, "join", long.class).invoke(worker, 500);
                        if ((Boolean) getMethod(worker,"isAlive").invoke(worker)) {
                            log.info("Unable to stop IndexMerger.Worker. Daemon is busy.");
                        } else {
                            log.debug("Worker stopped");
                        }
                    }
                }
            } else {
                // Unblock waiting mergers
                getMethod(merger, "start").invoke(merger);
            }
        } catch (ReflectiveOperationException e) {
            throw new JahiaRuntimeException("Cannot switch index to read-only", e);
        }
    }

    /**
     * Sets the default analyzer in use for indexing.
     *
     * @param analyzer the new default analyzer
     */
    private void setAnalyzer(Analyzer analyzer) {
        JackrabbitAnalyzer jackrabbitAnalyzer = getJackrabbitAnalyzer();
        jackrabbitAnalyzer.setDefaultAnalyzer(analyzer);
    }

    private JackrabbitAnalyzer getJackrabbitAnalyzer() {
        // The following piece of code is attempting to access the private instance of JackrabbitAnalyzer
        // from SearchIndex using the reflection API. This is thus relying on implementation specific of
        // a given version of Jackrabbit (2.18.4) and my be incompatible with later revisions.
        try {
            Field field = SearchIndex.class.getDeclaredField("analyzer");
            field.setAccessible(true);
            return (JackrabbitAnalyzer) field.get(this);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException(String.format("Could not access JackrabbitAnalyzer from %s", SearchIndex.class), e);
        }
    }

    /**
     * Attempts to get a language specific analyzer from a registry.
     *
     * @param registry the registry
     * @param locale the targeted locale
     * @return a language specific analyzer if any is found, {@code null} otherwise
     */
    private static Analyzer getLanguageSpecificAnalyzer(AnalyzerRegistry registry, Locale locale) {
        Analyzer analyzer = registry.getAnalyzer(locale.toString());
        if (analyzer == null) {
            analyzer = registry.getAnalyzer(locale.getLanguage());
        }
        return analyzer;
    }

    private static <T> T getValue(Object object, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private static Method getMethod(Object object, String name, Class... params) throws NoSuchMethodException {
        Method method = object.getClass().getDeclaredMethod(name, params);
        method.setAccessible(true);
        return method;
    }

}
