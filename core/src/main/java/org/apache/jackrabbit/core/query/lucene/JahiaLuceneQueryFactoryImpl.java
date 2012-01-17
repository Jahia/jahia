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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.lucene.join.SelectorRow;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.search.facets.JahiaQueryParser;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.query.qom.*;

import java.io.IOException;
import java.util.*;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
import static org.apache.jackrabbit.core.query.lucene.FieldNames.LOCAL_NAME;
import static org.apache.jackrabbit.core.query.lucene.TransformConstants.TRANSFORM_NONE;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

/**
 * Override LuceneQueryFactory
 *
 * - optimize descendantNode constraint (use index)
 * - optimize childNode constraint (use index)
 * - handles rep:facet when executing (todo : might handle that in a constraint, as rep:filter ?)
 * - handles rep:filter in fulltext constraint  
 */
public class JahiaLuceneQueryFactoryImpl extends LuceneQueryFactory {
    private JCRStoreProvider provider;    
    private JCRSessionWrapper jcrSession;

    public JahiaLuceneQueryFactoryImpl(SessionImpl session, SearchIndex index, Map<String, Value> bindVariables) throws RepositoryException {
        super(session, index, bindVariables);
    }

    /**
     * Override LuceneQueryFactory.execute()
     */
    public List<Row> execute(
            Map<String, PropertyValue> columns, Selector selector,
            final Constraint constraint) throws RepositoryException, IOException {
        final IndexReader reader = index.getIndexReader(true);
        try {
            JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(
                    session, reader, index.getContext().getItemStateManager());
            searcher.setSimilarity(index.getSimilarity());

            Predicate filter = Predicate.TRUE;
            BooleanQuery query = new BooleanQuery();

            QueryPair qp = new QueryPair(query);

            query.add(create(selector), MUST);
            if (constraint != null) {
                String name = selector.getSelectorName();
                NodeType type =
                    ntManager.getNodeType(selector.getNodeTypeName());
                filter = mapConstraintToQueryAndFilter(qp,
                        constraint, Collections.singletonMap(name, type),
                        searcher, reader);
            }


            // Added by jahia
            Set<String> foundIds = new HashSet<String>();
            List<ScoreNode> nodes = new ArrayList<ScoreNode>();
            // End

            List<Row> rows = new ArrayList<Row>();
            QueryHits hits = searcher.evaluate(qp.mainQuery);
            ScoreNode node = hits.nextScoreNode();
            while (node != null) {
                if (foundIds.add(getId(node, reader))) {  // <-- Added by jahia
                    try {
                        NodeImpl objectNode = session.getNodeById(node.getNodeId());
                        if (objectNode.isNodeType("jnt:translation")) {
                            objectNode = (NodeImpl) objectNode.getParent();
                        }
                        Row row = new SelectorRow(
                                columns, evaluator, selector.getSelectorName(),
                                provider.getNodeWrapper(objectNode, jcrSession),
                                node.getScore());
                        if (filter.evaluate(row)) {
                            rows.add(row);
                            nodes.add(node); // <-- Added by jahia
                        }
                    } catch (PathNotFoundException e) {
                    } catch (ItemNotFoundException e) {
                        // skip the node
                    }
                }  // <-- Added by jahia
                node = hits.nextScoreNode();
            }

            // Added by jahia
            FacetHandler h = new FacetHandler(columns, selector, nodes, index, session);
            if (h.hasFacetFunctions()) {
                h.handleFacets(reader);
                rows.add(0, h.getFacetsRow());
            }
            // End

            return rows;
        } finally {
            Util.closeOrRelease(reader);
        }
    }

    private String getId (ScoreNode sn, IndexReader reader) throws IOException {
        String id;
        int docNb = sn.getDoc(reader);
        Document doc = reader.document(docNb);
        if (doc.getField(JahiaNodeIndexer.TRANSLATED_NODE_PARENT) != null) {
            id = doc.getField(FieldNames.PARENT).stringValue();
        } else {
            id = sn.getNodeId().toString();
        }
        return id;
    }

    protected Query getNodeIdQuery(String field, String path) throws RepositoryException {
        BooleanQuery or = null;
        try {
            if (field.equals(FieldNames.PARENT)) {
                Query q1 = new JackrabbitTermQuery(
                        new Term(FieldNames.PARENT, session.getNode(path).getIdentifier()));
                Query q2 = new JackrabbitTermQuery(
                        new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, session.getNode(path).getIdentifier()));
                or = new BooleanQuery();
                or.add(q1, BooleanClause.Occur.SHOULD);
                or.add(q2, BooleanClause.Occur.SHOULD);
            } else {
                return super.getNodeIdQuery(field, path);
            }
        } catch (AccessDeniedException e) {
            return new JackrabbitTermQuery(new Term(FieldNames.UUID, "invalid-node-id")); // never matches
        } catch (PathNotFoundException e) {
            return new JackrabbitTermQuery(new Term(FieldNames.UUID, "invalid-node-id")); // never matches
        }
        return or;
    }

//    protected Query getDescendantNodeQuery(
//            DescendantNode dn, JackrabbitIndexSearcher searcher)
//            throws RepositoryException, IOException {
//
////        new DescendantSelfAxisQuery()
//        Query query = null;
//        try {
//            query = new JackrabbitTermQuery(
//                    new Term(JahiaNodeIndexer.ANCESTOR, session.getNode(dn.getAncestorPath()).getIdentifier()));
//        } catch (PathNotFoundException e) {
//            query = new JackrabbitTermQuery(new Term(FieldNames.UUID, "invalid-node-id")); // never matches
//        }
//        return query;
////        return super.getDescendantNodeQuery(dn, searcher);
//    }

    protected Query getFullTextSearchQuery(FullTextSearch fts) throws RepositoryException {
        Query qobj = null;

        if (StringUtils.startsWith(fts.getPropertyName(), "rep:filter(")) {
            try {
                StaticOperand expr = fts.getFullTextSearchExpression();
                if (expr instanceof Literal) {
                    QueryParser qp = new JahiaQueryParser(FieldNames.FULLTEXT, new KeywordAnalyzer());
                    qp.setLowercaseExpandedTerms(false);
                    qobj = qp.parse(((Literal) expr).getLiteralValue().getString());
                } else {
                    throw new RepositoryException("Unknown static operand type: " + expr);
                }
            } catch (ParseException e) {
                throw new RepositoryException(e);
            }
        } else {
            qobj = super.getFullTextSearchQuery(fts);
        }
        return qobj;
    }

    protected Predicate mapConstraintToQueryAndFilter(
            QueryPair query, Constraint constraint,
            Map<String, NodeType> selectorMap,
            JackrabbitIndexSearcher searcher, IndexReader reader)
            throws RepositoryException, IOException {
        try {
            if (constraint instanceof DescendantNode) {
                query.subQuery.add(new TermQuery(new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, session.getNode(((DescendantNode) constraint).getAncestorPath()).getParent().getIdentifier())),
                        BooleanClause.Occur.MUST_NOT);
            } else if (constraint instanceof ChildNode) {
                query.subQuery.add(new TermQuery(new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, session.getNode(((ChildNode)constraint).getParentPath()).getParent().getIdentifier())),
                        BooleanClause.Occur.MUST_NOT);
            }
        } catch (AccessDeniedException e) {
            // denied
            // todo : should find another way to test that we are not in a translation sub node
        } catch (PathNotFoundException e) {
            // not found
            query.subQuery.add(new JackrabbitTermQuery(new Term(
                    FieldNames.UUID, "invalid-node-id")), // never matches
                    MUST);
        }

        return super.mapConstraintToQueryAndFilter(query,constraint, selectorMap, searcher, reader);
    }

    public JCRStoreProvider getProvider() {
        return provider;
    }

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public JCRSessionWrapper getJcrSession() {
        return jcrSession;
    }

    public void setJcrSession(JCRSessionWrapper jcrSession) {
        this.jcrSession = jcrSession;
    }

    @Override
    protected Query getComparisonQuery(DynamicOperand left, int transform,
            String operator, StaticOperand rigth,
            Map<String, NodeType> selectorMap) throws RepositoryException {
        if (left instanceof PropertyValue) {
            PropertyValue pv = (PropertyValue) left;
            if (pv.getPropertyName().equals("_PARENT")) {
                return new JackrabbitTermQuery(new Term(FieldNames.PARENT, getValueString(evaluator.getValue(rigth), PropertyType.REFERENCE)));
            }
        } 
        return super.getComparisonQuery(left, transform, operator, rigth, selectorMap);
    }


}
