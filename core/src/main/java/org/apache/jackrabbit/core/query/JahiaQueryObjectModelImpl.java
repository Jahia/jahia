package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;

/**
 * Override QueryObjectModelImpl :
 *  - set JahiaLuceneQueryFactoryImpl instead of LuceneQueryFactoryImpl in init()
 *  - use JahiaQueryEngine instead of QueryEngine in execute()
 */
public class JahiaQueryObjectModelImpl extends QueryObjectModelImpl {

    @Override
    public void init(SessionContext sessionContext, QueryHandler handler, QueryObjectModelTree qomTree, String language, Node node) throws InvalidQueryException, RepositoryException {
        super.init(sessionContext, handler, qomTree, language, node);    //To change body of overridden methods use File | Settings | File Templates.

        this.lqf = new JahiaLuceneQueryFactoryImpl(sessionContext.getSessionImpl(), (SearchIndex) handler,
                variables);

    }

    public QueryResult execute() throws RepositoryException {
        JahiaQueryEngine engine = new JahiaQueryEngine(
                sessionContext.getSessionImpl(), lqf, variables);
        return engine.execute(
                getColumns(), getSource(), getConstraint(),
                getOrderings(), offset, limit);
    }
}
