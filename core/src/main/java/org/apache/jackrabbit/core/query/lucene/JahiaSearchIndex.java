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

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.constraint.NoDuplicatesConstraint;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.state.ChildNodeEntry;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.slf4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

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
    private static final String TRANSLATION_LOCALNODENAME_PREFIX = "translation_";

    private int maxClauseCount = 1024;

    public int getMaxClauseCount() {
        return maxClauseCount;
    }

    public void setMaxClauseCount(int maxClauseCount) {
        this.maxClauseCount = maxClauseCount;
        BooleanQuery.setMaxClauseCount(maxClauseCount);
    }

    /**
     * We override this method in order to trigger re-indexing on translation nodes, when their
     * parent node is getting re-indexed.
     * 
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

        final List<NodeState> addList = new ArrayList<NodeState>();
        final List<NodeId> removeList = new ArrayList<NodeId>();
        final Set<NodeId> removedIds = new HashSet<NodeId>();
        final Set<NodeId> addedIds = new HashSet<NodeId>();

        while (add.hasNext()) {
            final NodeState state = add.next();
            if (state != null) {
                addedIds.add(state.getNodeId());
                addList.add(state);
            }
        }
        while (remove.hasNext()) {
            NodeId nodeId = remove.next();
            removedIds.add(nodeId);
            removeList.add(nodeId);
        }

        final ItemStateManager itemStateManager = getContext().getItemStateManager();
        if (!removeList.isEmpty()) {
            final IndexReader reader = getIndexReader();
            final Searcher searcher = new IndexSearcher(reader);
            int removeSubListStart = 0;
            int removeSubListEnd = Math.min(removeList.size(), BooleanQuery.getMaxClauseCount());
            while (removeSubListStart < removeList.size()) {
                BooleanQuery query = new BooleanQuery();
                for (final NodeId nodeId : new ArrayList<NodeId>(removeList.subList(removeSubListStart, removeSubListEnd))) {
                    TermQuery termQuery = new TermQuery(new Term(JahiaNodeIndexer.FACET_HIERARCHY, nodeId.toString()));
                    query.add(new BooleanClause(termQuery, BooleanClause.Occur.SHOULD));
                }
                searcher.search(query, new HitCollector() {
                    public void collect(int doc, float score) {
                        try {
                            String uuid = reader.document(doc).get("_:UUID");
                            final NodeId id = new NodeId(uuid);
                            if (!removedIds.contains(id) && !addedIds.contains(id)) {
                                removeList.add(id);
                                removedIds.add(id);
                            }
                            if (!addedIds.contains(id) && itemStateManager.hasItemState(id)) {
                                addList.add((NodeState) itemStateManager.getItemState(id));
                                addedIds.add(id);
                            }
                        } catch (Exception e) {
                            log.warn("Documents referencing moved/renamed hierarchy facet nodes may not be updated", e);
                        }
                    }
                });
                removeSubListStart += BooleanQuery.getMaxClauseCount();
                removeSubListEnd =  Math.min(removeList.size(), removeSubListEnd + BooleanQuery.getMaxClauseCount());

            }
        }

        for (final NodeState node : new ArrayList<NodeState>(addList)) {
            for (ChildNodeEntry childNodeEntry : node.getChildNodeEntries()) {
                if (childNodeEntry.getName().getLocalName().startsWith(TRANSLATION_LOCALNODENAME_PREFIX)) {
                    try {
                        if (!removedIds.contains(childNodeEntry.getId())
                                && !addedIds.contains(childNodeEntry.getId())) {
                            removeList.add(childNodeEntry.getId());
                            removedIds.add(childNodeEntry.getId());
                        }
                        if (!addedIds.contains(childNodeEntry.getId())
                                && itemStateManager.hasItemState(childNodeEntry.getId())) {
                            addList.add((NodeState) itemStateManager.getItemState(childNodeEntry.getId()));
                            addedIds.add(childNodeEntry.getId());
                        }
                    } catch (Exception e) {
                        log.warn("Index of translation node may not be updated", e);
                    }
                }
            }
        }

        super.updateNodes(removeList.iterator(), addList.iterator());
    }

    @Override
    protected Document createDocument(final NodeState node, NamespaceMappings nsMappings,
                                      IndexFormatVersion indexFormatVersion) throws RepositoryException {
        JahiaNodeIndexer indexer = new JahiaNodeIndexer(node, getContext().getItemStateManager(),
                nsMappings, getContext().getExecutor(), getParser(),
                getContext());
        indexer.setSupportHighlighting(getSupportHighlighting());
        indexer.setIndexingConfiguration(getIndexingConfig());
        indexer.setIndexFormatVersion(indexFormatVersion);
        indexer.setMaxExtractLength(getMaxExtractLength());
        indexer.setSupportSpellchecking(getSpellChecker() != null);
        Document doc = indexer.createDoc();
        mergeAggregatedNodeIndexes(node, doc, indexFormatVersion);
        return doc;
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
                searcher.search(q, new HitCollector() {
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
}
