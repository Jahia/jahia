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

package org.jahia.services.query;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.query.lucene.join.QueryEngine;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.version.VersionException;
import java.util.*;

/**
 * An implementation of the JCR {@link Query} for multiple providers.
 *
 * @author Thomas Draier
 */
class QueryWrapper implements Query {

    public static final Logger logger = LoggerFactory.getLogger(QueryWrapper.class);

    private static final String FACET_FUNC_LPAR = "facet(";
    private static final Name REP_FACET_LPAR = NameFactoryImpl.getInstance().create(
            Name.NS_REP_URI, FACET_FUNC_LPAR);

    private String statement;
    private String language;
    private long limit = -1;
    private long offset = 0;
    private Map<JCRStoreProvider, Query> queries;
    private Map<String, Value> vars;
    private Node node;
    private JCRSessionFactory service;
    private JCRSessionWrapper session;

    public QueryWrapper(String statement, String language, JCRSessionWrapper session, JCRSessionFactory service) throws InvalidQueryException, RepositoryException {
        this.statement = statement;
        this.language = language;
        this.vars = new HashMap<String, Value>();
        this.session = session;
        this.service = service;
        init();
    }

    public QueryWrapper(Node node, JCRSessionWrapper session, JCRSessionFactory service) throws InvalidQueryException, RepositoryException {
        this(node.getProperty("jcr:statement").getString(), node.getProperty("jcr:language").getString(), session, service);
        this.node = node;
    }

    private void init() throws InvalidQueryException, RepositoryException {
        queries = new HashMap<JCRStoreProvider, Query>();

        Collection<JCRStoreProvider> providers = service.getProviders().values();

        if (language.equals(Query.XPATH)) {
            if (!statement.startsWith("//")) {
                JCRStoreProvider p = service.getProvider("/" + statement);
                providers = Collections.singletonList(p);
            }
        }
        for (JCRStoreProvider jcrStoreProvider : providers) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session);
            if (qm != null) {
                Query query = qm.createQuery(statement, language);
                if (jcrStoreProvider.isDefault()) {
                    if (Query.JCR_SQL2.equals(language)) {
                        query = QueryServiceImpl.getInstance().modifyAndOptimizeQuery(
                                (QueryObjectModel) query, qm.getQOMFactory(), session);
                    }
                    if (query instanceof JahiaQueryObjectModelImpl) {
                        JahiaLuceneQueryFactoryImpl lqf = (JahiaLuceneQueryFactoryImpl) ((JahiaQueryObjectModelImpl) query)
                                .getLuceneQueryFactory();
                        
                        lqf.setProvider(jcrStoreProvider);
                        lqf.setJcrSession(session);
                    }
                }
                queries.put(jcrStoreProvider, query);
            }
        }
    }

    /*
    // @todo This is an ugly copy & paste from JahiaMultiColumnQueryResult, we should put these methods in a utility class somewhere and avoid code duplication.
    private boolean isFacetFunction(String columnName, SessionImpl sessionImpl) {
        try {
            return columnName.trim().startsWith(sessionImpl.getJCRName(REP_FACET_LPAR));
        } catch (NamespaceException e) {
            // will never happen
            return false;
        }
    }

    public void setImplicitLimit(QueryObjectModel qom, SessionImpl sessionImpl) {
        // first let's check if faceted search is being used, because we cannot set an implicit limit if faceting
        // is active.
        boolean hasFacetRequest = false;
        for (Column column : qom.getColumns()) {
            if ((column.getColumnName() != null) && isFacetFunction(column.getColumnName(), sessionImpl)) {
                hasFacetRequest = true;
                break;
            }
        }
        // We only set the limit if the limit has not been set and offset neither. Setting the limit to -2 for example
        // will avoid this behavior.
        if ((!hasFacetRequest) && (limit == -1) && (offset == 0)) {
            qom.setLimit(100);
            logger.warn("No limit set, will limit to 100 query results by default !");
        }
    }
    */

    public QueryResult execute() throws RepositoryException {
        List<QueryResultWrapper> results = new LinkedList<QueryResultWrapper>();
        for (Map.Entry<JCRStoreProvider, Query> entry : queries.entrySet()) {
            // should gather results
            final Query query = entry.getValue();
            if (limit > 0) {
                query.setLimit(limit);
            }
            if (offset > 0) {
                query.setOffset(offset);
            }
            QueryResultWrapper subResults = null;
            /*
            if (query instanceof QueryObjectModel) {
                QueryObjectModel qom = (QueryObjectModel) query;
                Session providerSession = session.getProviderSession(entry.getKey());
                if (providerSession instanceof SessionImpl) {
                    setImplicitLimit(qom, (SessionImpl) providerSession);
                }
                subResults = new QueryResultWrapper(query.execute(), entry.getKey(), session);
            } else {
            */
                subResults = new QueryResultWrapper(query.execute(), entry.getKey(), session);
            /*
            }
            */
            results.add(subResults);
        }
        return MultipleQueryResultAdapter.decorate(results);
    }

    public String getStatement() {
        return statement;
    }

    public String getLanguage() {
        return language;
    }

    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        if (node == null) {
            throw new ItemNotFoundException();
        }
        return node.getPath();
    }

    public Node storeAsNode(String s) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        String path = StringUtils.substringBeforeLast(s, "/");
        String name = StringUtils.substringAfterLast(s, "/");
        Node n = (Node) session.getItem(path);
//        if (!n.isCheckedOut()) {
//            n.checkout();
//        }
        node = n.addNode(name, "jnt:query");

        node.setProperty("jcr:statement", statement);
        node.setProperty("jcr:language", language);

        return node;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
        vars.put(varName, value);
    }

    public String[] getBindVariableNames() throws RepositoryException {
        return vars.keySet().toArray(new String[vars.size()]);
    }

    public Map<JCRStoreProvider, Query> getQueries() {
        return queries;
    }
}