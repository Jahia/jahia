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
        return new JackrabbitTermQuery(new Term("_:PARENT", session.getNode(cn.getParentPath()).getIdentifier()));
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
        final MutableMultiColumnQueryAdapter adapter = (MutableMultiColumnQueryAdapter) create(tree.getSource());

        try {
            if (tree.getConstraint() != null) {
                tree.getConstraint().accept(new DefaultQOMTreeVisitor() {
                    public Object visit(AndImpl node, Object data) throws Exception {
                        Object d1 = ((ConstraintImpl) node.getConstraint1()).accept(this, data);
                        Object d2 = ((ConstraintImpl) node.getConstraint2()).accept(this, data);
                        return null;
                    }

                    public Object visit(ChildNodeImpl node, Object data) throws Exception {
                        BooleanQuery and = new BooleanQuery();
                        and.add(create(node), BooleanClause.Occur.MUST);
                        and.add(adapter.getQuery(), BooleanClause.Occur.MUST);
                        adapter.setQuery(and);

                        // todo : remove matched constraint from the tree to avoid double check

                        return null;
                    }
                }, null);
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }

        return adapter;
    }


}
