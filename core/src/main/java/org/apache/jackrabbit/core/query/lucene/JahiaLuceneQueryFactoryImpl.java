package org.apache.jackrabbit.core.query.lucene;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.StaticOperand;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.query.qom.FullTextSearchImpl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortComparatorSource;

public class JahiaLuceneQueryFactoryImpl extends LuceneQueryFactoryImpl {

    public JahiaLuceneQueryFactoryImpl(SessionImpl session, SortComparatorSource scs,
            HierarchyManager hmgr, NamespaceMappings nsMappings, Analyzer analyzer,
            SynonymProvider synonymProvider, IndexFormatVersion version) {
        super(session, scs, hmgr, nsMappings, analyzer, synonymProvider, version);
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
    
}
