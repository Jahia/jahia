package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortComparatorSource;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.StaticOperand;

public class JahiaLuceneQueryFactoryImpl extends LuceneQueryFactoryImpl {

    private final SessionImpl session;

    public JahiaLuceneQueryFactoryImpl(SessionImpl session, SortComparatorSource scs, HierarchyManager hmgr,
                                       NamespaceMappings nsMappings, Analyzer analyzer, SynonymProvider synonymProvider,
                                       IndexFormatVersion version) {
        super(session, scs, hmgr, nsMappings, analyzer, synonymProvider, version);
        this.session = session;
    }

    public Query create(FullTextSearchImpl fts) throws RepositoryException {
        Query qobj = null;

        if (StringUtils.startsWith(fts.getPropertyName(), "rep:filter(")) {
            try {
                StaticOperand expr = fts.getFullTextSearchExpression();
                if (expr instanceof Literal) {
                    QueryParser qp = new QueryParser(FieldNames.FULLTEXT, new KeywordAnalyzer());
                    qp.setLowercaseExpandedTerms(false);
                    qobj = qp.parse(((Literal) expr).getLiteralValue().getString());
                } else {
                    throw new RepositoryException("Unknown static operand type: " + expr);
                }
            } catch (ParseException e) {
                throw new RepositoryException(e);
            }
        } else {
            qobj = super.create(fts);
        }
        return qobj;
    }

    public Query create(ChildNodeImpl cn) throws RepositoryException {
        Query q1 = new JackrabbitTermQuery(
                new Term(FieldNames.PARENT, session.getNode(cn.getParentPath()).getIdentifier()));
        Query q2 = new JackrabbitTermQuery(
                new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, session.getNode(cn.getParentPath()).getIdentifier()));
        BooleanQuery or = new BooleanQuery();
        or.add(q1, BooleanClause.Occur.SHOULD);
        or.add(q2, BooleanClause.Occur.SHOULD);
        return or;
    }

    public Query create(DescendantNodeImpl dn) throws RepositoryException {
        return new JackrabbitTermQuery(
                new Term(JahiaNodeIndexer.ANCESTOR, session.getNode(dn.getAncestorPath()).getIdentifier()));
    }

    /**
     * {@inheritDoc}
     */
    public MultiColumnQuery create(SourceImpl source) throws RepositoryException {
        // source is either selector or join
        try {
            return (MultiColumnQuery) source.accept(new DefaultQOMTreeVisitor() {
                public Object visit(JoinImpl node, Object data) throws Exception {
                    return create(node);
                }

                public Object visit(SelectorImpl node, Object data) throws Exception {
                    return MutableMultiColumnQueryAdapter.adapt(create(node), node.getSelectorQName());
                }
            }, null);
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    public MultiColumnQuery create(QueryObjectModelTree tree) throws RepositoryException {
        final MultiColumnQuery query = create(tree.getSource());
        if (query instanceof MutableMultiColumnQueryAdapter) {
            final MutableMultiColumnQueryAdapter adapter = (MutableMultiColumnQueryAdapter) query;

            try {
                if (tree.getConstraint() != null) {
                    Query q = (Query) tree.getConstraint().accept(new DefaultQOMTreeVisitor() {
                        public Object visit(AndImpl node, Object data) throws Exception {
                            Query q1 = (Query) ((ConstraintImpl) node.getConstraint1()).accept(this, data);
                            Query q2 = (Query) ((ConstraintImpl) node.getConstraint2()).accept(this, data);

                            if (q1 != null && q2 != null) {
                                BooleanQuery and = new BooleanQuery();
                                and.add(q1, BooleanClause.Occur.MUST);
                                and.add(q2, BooleanClause.Occur.MUST);
                                return and;
                            } else if (q1 != null) {
                                return q1;
                            } else {
                                return q2;
                            }
                        }

                        public Object visit(OrImpl node, Object data) throws Exception {
                            Query q1 = (Query) ((ConstraintImpl) node.getConstraint1()).accept(this, data);
                            Query q2 = (Query) ((ConstraintImpl) node.getConstraint2()).accept(this, data);

                            if (q1 != null && q2 != null) {
                                BooleanQuery or = new BooleanQuery();
                                or.add(q1, BooleanClause.Occur.SHOULD);
                                or.add(q2, BooleanClause.Occur.SHOULD);
                                return or;
                            } else {
                                return null;
                            }
                        }

                        public Object visit(NotImpl node, Object data) throws Exception {
                            Query q = (Query) ((ConstraintImpl) node.getConstraint()).accept(this, data);
                            if (q != null) {
                                BooleanQuery not = new BooleanQuery();
                                not.add(q, BooleanClause.Occur.MUST_NOT);
                                return not;
                            } else {
                                return null;
                            }
                        }


                        public Object visit(FullTextSearchImpl node, Object data) throws Exception {
                            return create(node);
                        }

                        public Object visit(ChildNodeImpl node, Object data) throws Exception {
                            return create(node);
                        }

                        public Object visit(DescendantNodeImpl node, Object data) throws Exception {
                            return create(node);
                        }


                    }, null);

                    if (q != null) {
                        BooleanQuery and = new BooleanQuery();
                        and.add(adapter.getQuery(), BooleanClause.Occur.MUST);
                        and.add(q, BooleanClause.Occur.MUST);
                        adapter.setQuery(and);
                    }
                }
            } catch (RepositoryException e) {
                throw e;
            } catch (Exception e) {
                throw new RepositoryException(e);
            }
        }
        return query;
    }


}
