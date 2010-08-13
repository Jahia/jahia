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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.ExecutableQuery;
import org.apache.jackrabbit.core.state.ChildNodeEntry;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.*;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implements a {@link org.apache.jackrabbit.core.query.QueryHandler} using Lucene and handling Jahia specific definitions.
 */
public class JahiaSearchIndex extends SearchIndex {
    private static final Logger log = Logger.getLogger(JahiaSearchIndex.class);
    private static final String TRANSLATION_LOCALNODENAME_PREFIX = "translation_"; 

    /**
     * This implementation forwards the call to
     * {@link MultiIndex#update(java.util.Collection , java.util.Collection)} and
     * transforms the two iterators to the required types.
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
            }
            addList.add(state);
        }
        while (remove.hasNext()) {
            NodeId nodeId = remove.next();
            removedIds.add(nodeId);            
            removeList.add(nodeId);
        }

        final IndexReader reader = getIndexReader();
        final Searcher searcher = new IndexSearcher(reader);

        for (final NodeState node : new ArrayList<NodeState>(addList)) {
            if (node.getParentId() != null) {
                try {
                    int doc = -1;
                    TermDocs docs = reader.termDocs(new Term(FieldNames.UUID, node.getNodeId().toString()));
                    try {
                        if (docs.next()) {
                            doc = docs.doc();
                            
                            String oldUuid = reader.document(doc).get(FieldNames.PARENT);
                            String uuid = node.getParentId().toString();
                            if (!oldUuid.equals(uuid)) {
                                searcher.search(new TermQuery(new Term(JahiaNodeIndexer.ANCESTOR, node.getId().toString())),new HitCollector() {
                                    public void collect(int doc, float score) {
                                        try {
                                            String uuid = reader.document(doc).get(FieldNames.UUID);

                                            final NodeId id = new NodeId(uuid);
                                            if (!addedIds.contains(id)) {
                                                addList.add((NodeState) getContext()
                                                        .getItemStateManager().getItemState(id));
                                            }
                                            if (!removedIds.contains(id)) {
                                                removeList.add(id);
                                            }
                                        } catch (Exception e) {
                                            log.error("Cannot search moved nodes",e);
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        log.error("Cannot search moved nodes",e);
                    } finally {
                        docs.close();
                    }                        
                } catch (Exception e) {
                    log.error("Cannot search moved nodes",e);
                }
            }
            for (ChildNodeEntry childNodeEntry : node.getChildNodeEntries()) {
                if (childNodeEntry.getName().getLocalName().startsWith(TRANSLATION_LOCALNODENAME_PREFIX)) {
                    try {
                        if (!addedIds.contains(childNodeEntry.getId())) {
                            addList.add((NodeState) getContext().getItemStateManager()
                                    .getItemState(childNodeEntry.getId()));
                        }
                        if (!removedIds.contains(childNodeEntry.getId())
                                && reader.termDocs(
                                                new Term(FieldNames.UUID, childNodeEntry.getId()
                                                        .toString())).next()) {
                            removeList.add(childNodeEntry.getId());
                        }
                    } catch (Exception e) {
                        log.warn("Index of translation node may not be updated",e);
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
        indexer.setSupportSpellchecking(getSpellChecker() != null);
        Document doc = indexer.createDoc();
        mergeAggregatedNodeIndexes(node, doc, indexFormatVersion);
        return doc;
    }

    /**
     * Executes the query on the search index.
     * 
     * @param session
     *            the session that executes the query.
     * @param query
     *            the query.
     * @param orderings
     *            the order specs for the sort order.
     * @param resultFetchHint
     *            a hint on how many results should be fetched.
     * @return the query hits.
     * @throws IOException
     *             if an error occurs while searching the index.
     */
    public MultiColumnQueryHits executeQuery(SessionImpl session, MultiColumnQuery query,
            Ordering[] orderings, long resultFetchHint, boolean createFacets) throws IOException {
        checkOpen();

        final IndexReader reader = getIndexReader();
        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(session, reader,
                getContext().getItemStateManager());
        searcher.setSimilarity(getSimilarity()); 
        MultiColumnQueryHits hits = query.execute(searcher, orderings, resultFetchHint);
        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits,
                query instanceof JahiaQueryImpl ? ((JahiaQueryImpl) query).getConstraint()
                        : null, searcher) {
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    PerQueryCache.getInstance().dispose();
                    Util.closeOrRelease(reader);
                }
            }
        };
        return filteredHits;
    }

    /**
     * Executes the query on the search index.
     * 
     * @param session
     *            the session that executes the query.
     * @param queryImpl
     *            the query impl.
     * @param query
     *            the lucene query.
     * @param orderProps
     *            name of the properties for sort order.
     * @param orderSpecs
     *            the order specs for the sort order properties. <code>true</code> indicates ascending order, <code>false</code> indicates
     *            descending.
     * @param resultFetchHint
     *            a hint on how many results should be fetched.
     * @return the query hits.
     * @throws IOException
     *             if an error occurs while searching the index.
     */
    @Override
    public MultiColumnQueryHits executeQuery(SessionImpl session, AbstractQueryImpl queryImpl,
            Query query, Path[] orderProps, boolean[] orderSpecs, long resultFetchHint)
            throws IOException {
        checkOpen();

        Sort sort = new Sort(createSortFields(orderProps, orderSpecs));

        final IndexReader reader = getIndexReader(queryImpl.needsSystemTree());
        JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(session, reader,
                getContext().getItemStateManager());
        searcher.setSimilarity(getSimilarity());
        MultiColumnQueryHits hits = searcher.execute(query, sort, resultFetchHint,
                QueryImpl.DEFAULT_SELECTOR_NAME);
        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits,
                queryImpl instanceof JahiaQueryImpl ? ((JahiaQueryImpl) queryImpl).getConstraint()
                        : null, searcher) {
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    PerQueryCache.getInstance().dispose();
                    Util.closeOrRelease(reader);
                }
            }
        };
        
        return filteredHits;
    }
    
    @Override
    public ExecutableQuery createExecutableQuery(
            SessionImpl session,
            ItemManager itemMgr,
            QueryObjectModelTree qomTree) throws InvalidQueryException {
        QueryObjectModelImpl query = new JahiaQueryObjectModelImpl(session, itemMgr, this,
                getContext().getPropertyTypeRegistry(), qomTree);
        query.setRespectDocumentOrder(getRespectDocumentOrder());
        return query;
    }

    @Override
    public ExecutableQuery createExecutableQuery(SessionImpl session, ItemManager itemMgr,
            String statement, String language) throws InvalidQueryException {
        JahiaQueryImpl query = new JahiaQueryImpl(session, itemMgr, this,
                getContext().getPropertyTypeRegistry(), statement, language, getQueryNodeFactory());
        query.setConstraint(new NoDuplicatesConstraint());
        query.setRespectDocumentOrder(getRespectDocumentOrder());
        return query;
    }    
}
