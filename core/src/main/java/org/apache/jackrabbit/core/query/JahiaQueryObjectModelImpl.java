/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.api.stats.RepositoryStatistics;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.lucene.join.JahiaQueryEngine;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.session.SessionOperation;
import org.apache.jackrabbit.core.stats.RepositoryStatisticsImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(QueryObjectModelImpl.class);

    @Override
    public void init(SessionContext sessionContext, QueryHandler handler, QueryObjectModelTree qomTree, String language, Node node) throws InvalidQueryException, RepositoryException {
        super.init(sessionContext, handler, qomTree, language, node);    

        this.lqf = new JahiaLuceneQueryFactoryImpl(sessionContext.getSessionImpl(), (SearchIndex) handler,
                variables);

    }

    @Override
    public QueryResult execute() throws RepositoryException {
        long time = System.nanoTime();
        final QueryResult result = sessionContext.getSessionState().perform(
                new SessionOperation<QueryResult>() {
                    public QueryResult perform(SessionContext context)
                            throws RepositoryException {
                        final JahiaQueryEngine engine = new JahiaQueryEngine(
                                sessionContext.getSessionImpl(), lqf, variables);
                        return engine.execute(getColumns(), getSource(),
                                getConstraint(), getOrderings(), offset, limit);
                    }

                    public String toString() {
                        return "query.execute(" + statement + ")";
                    }
                });
        time = System.nanoTime() - time;
        final long timeMs = time / 1000000;
        log.debug("executed in {} ms. ({})", timeMs, statement);
        RepositoryStatisticsImpl statistics = sessionContext
                .getRepositoryContext().getRepositoryStatistics();
        statistics.getCounter(RepositoryStatistics.Type.QUERY_COUNT).incrementAndGet();
        statistics.getCounter(RepositoryStatistics.Type.QUERY_DURATION).addAndGet(timeMs);
        sessionContext.getRepositoryContext().getStatManager().getQueryStat()
                .logQuery(language, statement, timeMs);
        return result;
    }
    
    public LuceneQueryFactory getLuceneQueryFactory() {
        return this.lqf;
    }
}
