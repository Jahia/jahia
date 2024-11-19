/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.jackrabbit.commons.query.sql2.QOMFormatter;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.lucene.join.JahiaQueryEngine;
import org.apache.jackrabbit.core.session.SessionContext;
import org.apache.jackrabbit.core.session.SessionOperation;
import org.apache.jackrabbit.stats.RepositoryStatisticsImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.List;

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
        // wrap columns facet statement with double quotes

        QueryObjectModelFactory qomQueryFactory = sessionContext.getSessionImpl().getWorkspace().getQueryManager().getQOMFactory();
        boolean hasFacet = false;
        if (getColumns() != null) {
            List<Column> columns = new ArrayList<>();
            for (Column col : getColumns()) {
                if (col.getColumnName() != null && col.getColumnName().startsWith("rep:facet")) {
                    try {
                        hasFacet = true;
                        columns.add(qomQueryFactory.column(col.getSelectorName(), col.getPropertyName(), String.format("\"%s\"", col.getColumnName())));
                    } catch (Exception e) {
                        log.warn("Unable to generate statement for column {} with column name {}", col, col.getColumnName());
                        columns.add(col);
                    }
                }  else {
                    columns.add(col);
                }
            }
            // Build statement with wrapped facet columns
            if (hasFacet) {
                this.statement = QOMFormatter.format(qomQueryFactory.createQuery(getSource(), getConstraint(), getOrderings(), columns.toArray(new Column[0])));
            }
        }
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
