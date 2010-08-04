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
import org.apache.jackrabbit.core.query.QueryHandler;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.Path;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a {@link org.apache.jackrabbit.core.query.QueryHandler} using Lucene and handling Jahia specific definitions.
 */
public class JahiaSearchIndex extends SearchIndex {
    private static final Logger log = Logger.getLogger(JahiaSearchIndex.class);

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
    public void updateNodes(Iterator<NodeId> remove, Iterator<NodeState> add)
            throws RepositoryException, IOException {

        final List<NodeState> addList = new ArrayList<NodeState>();
        final List<NodeId> removeList = new ArrayList<NodeId>();
        while (add.hasNext()) {
            final NodeState state = add.next();
            addList.add(state);
        }
        while (remove.hasNext()) {
            removeList.add(remove.next());
        }

        final IndexReader reader = getIndexReader();
        final Searcher searcher = new IndexSearcher(reader);

        for (final NodeState node : new ArrayList<NodeState>(addList)) {
            if (node.getParentId() != null) {
                try {
                    searcher.search(new TermQuery(new Term(FieldNames.UUID, node.getNodeId().toString())),new HitCollector() {
                        public void collect(int thisdoc, float score) {
                            try {
                                String oldUuid = reader.document(thisdoc).get("_:PARENT");
                                String uuid = node.getParentId().toString();
                                if (!oldUuid.equals(uuid)) {
                                    searcher.search(new TermQuery(new Term(JahiaNodeIndexer.ANCESTOR, node.getId().toString())),new HitCollector() {
                                        public void collect(int doc, float score) {
                                            try {
                                                String uuid = reader.document(doc).get("_:UUID");

                                                final NodeId id = new NodeId(uuid);
                                                addList.add((NodeState) getContext().getItemStateManager().getItemState(id));
                                                removeList.add(id);
                                            } catch (Exception e) {
                                                log.error("Cannot search moved nodes",e);
                                            }
                                        }
                                    });
                                }

                            } catch (Exception e) {
                                log.error("Cannot search moved nodes",e);
                            }
                        }
                    });

                } catch (Exception e) {
                    log.error("Cannot search moved nodes",e);
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
        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits, reader) {
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
        JahiaFilterMultiColumnQueryHits filteredHits = new JahiaFilterMultiColumnQueryHits(hits, reader) {
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
     * Creates a new query by specifying the query object model. If the query
     * object model is considered invalid for the implementing class, an
     * InvalidQueryException is thrown.
     *
     * @param session the session of the current user creating the query
     *                object.
     * @param itemMgr the item manager of the current user.
     * @param qomTree query query object model tree.
     * @return A <code>Query</code> object.
     * @throws javax.jcr.query.InvalidQueryException
     *          if the query object model tree is invalid.
     * @see QueryHandler#createExecutableQuery(SessionImpl, ItemManager, QueryObjectModelTree)
     */
    public ExecutableQuery createExecutableQuery(
            SessionImpl session,
            ItemManager itemMgr,
            QueryObjectModelTree qomTree) throws InvalidQueryException {
        QueryObjectModelImpl query = new JahiaQueryObjectModelImpl(session, itemMgr, this,
                getContext().getPropertyTypeRegistry(), qomTree);
        query.setRespectDocumentOrder(getRespectDocumentOrder());
        return query;
    }    
}
