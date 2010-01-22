/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;

/**
 * An implementation of the JCR {@link Query} for multiple providers.
 * 
 * @author Thomas Draier
 */
class QueryWrapper implements Query {
    private String statement;
    private String language;
    private long limit = -1;
    private long offset = 0;
    private Map<JCRStoreProvider, Query> queries;
    private Map<String, Value> vars;
    private Node node;
    private JCRSessionFactory service;
    private JCRSessionWrapper session;

    public QueryWrapper(String statement, String language, JCRSessionWrapper session, JCRSessionFactory service) throws InvalidQueryException, RepositoryException  {
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
                JCRStoreProvider p = service.getProvider("/"+statement);
                providers = Collections.singletonList(p);
            }
        } 
        for (JCRStoreProvider jcrStoreProvider : providers) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session);
            if (qm != null) {
                Query query = qm.createQuery(statement,language);
                if (Query.JCR_SQL2.equals(language)) {
                    query = QueryServiceImpl.getInstance().modifyAndOptimizeQuery(
                            (QueryObjectModel) query, session.getLocale(), qm.getQOMFactory());
                }
                queries.put(jcrStoreProvider, query);
            }
        }
    }

    public QueryResult execute() throws RepositoryException {
        List<QueryResultWrapper> results = new LinkedList<QueryResultWrapper>(); 
        for (Map.Entry<JCRStoreProvider,Query> entry : queries.entrySet()) {
            // should gather results
            final Query query = entry.getValue();
            if (limit > 0) {query.setLimit(limit);}
            if (offset > 0) {query.setOffset(offset);}
            QueryResultWrapper subResults = new QueryResultWrapper(query.execute(), entry.getKey(), session);
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
        String path = StringUtils.substringBeforeLast(s,"/");
        String name = StringUtils.substringAfterLast(s,"/");
        Node n = (Node) session.getItem(path);
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