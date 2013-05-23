/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
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
import javax.jcr.query.qom.*;
import javax.jcr.version.VersionException;
import java.util.*;

/**
 * An implementation of the JCR {@link Query} for multiple providers.
 *
 * @author Thomas Draier
 */
public class QueryWrapper implements Query {

    public static final Logger logger = LoggerFactory.getLogger(QueryWrapper.class);

    private String statement;
    private QueryObjectModel jrQOM;
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

    public QueryWrapper(QueryObjectModel qom, JCRSessionWrapper session, JCRSessionFactory service) throws InvalidQueryException, RepositoryException {
        this.jrQOM = qom;
        this.statement = qom.getStatement();
        this.language = Query.JCR_SQL2;
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
            Query query = getQuery(jcrStoreProvider);
            if (query != null) {
                queries.put(jcrStoreProvider, query);
            }
        }
    }

    protected Query getQuery(JCRStoreProvider jcrStoreProvider) throws RepositoryException {
        Query query = null;
        QueryManager qm = jcrStoreProvider.getQueryManager(session);
        if (qm != null && ArrayUtils.contains(qm.getSupportedQueryLanguages(), language)) {
            if (jcrStoreProvider.isDefault() && jrQOM != null) {
                query = jrQOM;
            } else {
                query = qm.createQuery(statement, language);
            }
            QueryObjectModelFactory factory = qm.getQOMFactory();
            if (!jcrStoreProvider.getMountPoint().equals("/") && query instanceof QueryObjectModel) {
                try {
                    QueryObjectModel qom = (QueryObjectModel) query;
                    query = factory.createQuery(qom.getSource(), convertPath(qom.getConstraint(), jcrStoreProvider.getMountPoint(), factory), qom.getOrderings(), qom.getColumns());
                } catch (ConstraintViolationException e) {
                    // Provider path incompatible with constraints, skip query
                    return null;
                }
            }
            if (jcrStoreProvider.isDefault()) {
                if (Query.JCR_SQL2.equals(language)) {
                    query = QueryServiceImpl.getInstance().modifyAndOptimizeQuery(
                            (QueryObjectModel) query, factory, session);
                }
                if (query instanceof JahiaQueryObjectModelImpl) {
                    JahiaLuceneQueryFactoryImpl lqf = (JahiaLuceneQueryFactoryImpl) ((JahiaQueryObjectModelImpl) query)
                            .getLuceneQueryFactory();
                    lqf.setLocale(session.getLocale());
                }
            }
        }
        return query;
    }


    private Constraint convertPath(Constraint constraint, String mountPoint, QueryObjectModelFactory f) throws RepositoryException {
        if (constraint instanceof ChildNode) {
            String root = ((ChildNode)constraint).getParentPath();
            if (mountPoint.startsWith(root)) {
                // Mount point in under path constraint -> remove constraint
                return null;
            }
            if (root.startsWith(mountPoint)) {
                // Path constraint is under mount point -> create new constraint with local path
                return f.childNode(((ChildNode)constraint).getSelectorName(), root.substring(mountPoint.length()));
            }
            // Path constraint incompatible with mount point
            throw new ConstraintViolationException();
        } else if (constraint instanceof DescendantNode) {
            String root = ((DescendantNode)constraint).getAncestorPath();
            if (mountPoint.startsWith(root)) {
                // Mount point in under path constraint -> remove constraint
                return null;
            }
            if (root.startsWith(mountPoint)) {
                // Path constraint is under mount point -> create new constraint with local path
                return f.descendantNode(((DescendantNode) constraint).getSelectorName(), root.substring(mountPoint.length()));
            }
            // Path constraint incompatible with mount point
            throw new ConstraintViolationException();
        } else if (constraint instanceof And) {
            Constraint c1 = convertPath(((And) constraint).getConstraint1(), mountPoint, f);
            Constraint c2 = convertPath(((And) constraint).getConstraint2(), mountPoint, f);
            if (c1 == null) {
                return c2;
            }
            if (c2 == null) {
                return c1;
            }
            return f.and(c1,c2);
        } else if (constraint instanceof Or) {
            Constraint c1 = null;
            try {
                c1 = convertPath(((Or) constraint).getConstraint1(), mountPoint, f);
            } catch (ConstraintViolationException e) {
                return convertPath(((Or) constraint).getConstraint2(), mountPoint, f);
            }
            Constraint c2 = null;
            try {
                c2 = convertPath(((Or) constraint).getConstraint2(), mountPoint, f);
            } catch (ConstraintViolationException e) {
                return convertPath(((Or) constraint).getConstraint1(), mountPoint, f);
            }
            if (c1 == null || c2 == null) {
                return null;
            }
            return f.or(c1, c2);
        } else if (constraint instanceof Not) {
            Constraint notConstraint = null;
            try {
                notConstraint = convertPath(((Not) constraint).getConstraint(), mountPoint, f);
            } catch (ConstraintViolationException e) {
                return null;
            }
            if (notConstraint == null) {
                throw new ConstraintViolationException();
            }
            return f.not(notConstraint);
        }
        return constraint;
    }


    public QueryResult execute() throws RepositoryException {
        long queryOffset = offset;
        long queryLimit = limit;
        List<QueryResultAdapter> results = new LinkedList<QueryResultAdapter>();
        Iterator<Map.Entry<JCRStoreProvider, Query>> it = queries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<JCRStoreProvider, Query> entry = it.next();
            Query query = entry.getValue();
            if (queryLimit >= 0) {
                query.setLimit(queryLimit);
            }
            if (queryOffset >= 0) {
                query.setOffset(queryOffset);
            }
            QueryResultAdapter queryResult = new QueryResultAdapter(query.execute(), entry.getKey(), session);
            results.add(queryResult);
            long resultCount = getResultCount(queryResult);
            if (queryLimit >= 0) {
                if (resultCount >= queryLimit) {
                    break;
                }
                queryLimit -= resultCount;
            }
            if (resultCount == 0 && queryOffset > 0) {
                Query noLimitQuery = getQuery(entry.getKey());
                queryOffset -= getResultCount(new QueryResultAdapter(noLimitQuery.execute(), entry.getKey(), session));
            } else {
                queryOffset = 0;
            }
        }
        return QueryResultWrapperImpl.wrap(results, limit);
    }

    protected long getResultCount(QueryResultAdapter queryResult) throws RepositoryException {
        NodeIterator nodes = queryResult.getNodes();
        long size = nodes.getSize();
        if (size < 0) {
            size = 0;
            while (nodes.hasNext()) {
                size++;
                nodes.next();
            }
        }
        return size;
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