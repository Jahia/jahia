/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.query;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
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
    
     // We will use this exception instance as a marker
    private static final ConstraintViolationException CONSTRAINT_VIOLATION_EXCEPTION = new ConstraintViolationException() {
        private static final long serialVersionUID = -523573814885597775L;

        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    };

    private String statement;
    private QueryObjectModel jrQOM;
    private String sqlStatement;
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

    public QueryWrapper(String statement, String language, String sqlStatement, JCRSessionWrapper session, JCRSessionFactory service) throws InvalidQueryException, RepositoryException {
        this.statement = statement;
        this.language = language;
        this.sqlStatement = sqlStatement;
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
        queries = new LinkedHashMap<JCRStoreProvider, Query>();

        Collection<JCRStoreProvider> providers = service.getProviders().values();

        if (language.equals(Query.XPATH)) {
//            if (!statement.startsWith("//")) {
//                JCRStoreProvider p = service.getProvider("/" + statement);
//                providers = Collections.singletonList(p);
//            }
        }
        for (JCRStoreProvider jcrStoreProvider : providers) {
            Query query = getQuery(jcrStoreProvider);
            if (query != null) {
                queries.put(jcrStoreProvider, query);
            }
        }
    }

    /**
     * Get the query for a specific provider
     *
     * @param jcrStoreProvider
     * @return
     * @throws RepositoryException
     */
    protected Query getQuery(JCRStoreProvider jcrStoreProvider) throws RepositoryException {
        Query query = null;
        QueryManager qm = jcrStoreProvider.getQueryManager(session);

        String statement = null;
        String language = this.language;

        if (qm != null) {
            if (ArrayUtils.contains(qm.getSupportedQueryLanguages(), language)) {
                statement = this.statement;
            } else if (sqlStatement != null && ArrayUtils.contains(qm.getSupportedQueryLanguages(), Query.JCR_SQL2)) {
                statement = this.sqlStatement;
                language = Query.JCR_SQL2;
            }
        }
        if (statement != null) {
            if (jcrStoreProvider.isDefault() && jrQOM != null) {
                query = jrQOM;
            } else {
                query = qm.createQuery(statement, language);
            }
            QueryObjectModelFactory factory = qm.getQOMFactory();
            if (!jcrStoreProvider.getMountPoint().equals("/")) {
                try {
                    QueryObjectModel qom;
                    if (query instanceof QueryObjectModel) {
                        qom = (QueryObjectModel) query;
                    } else if (language.equals(Query.JCR_SQL2)) {
                        qom = (QueryObjectModel) service.getDefaultProvider().getQueryManager(session).createQuery(statement, language);
                    } else if (sqlStatement != null) {
                        qom = (QueryObjectModel) service.getDefaultProvider().getQueryManager(session).createQuery(sqlStatement, Query.JCR_SQL2);
                    } else {
                        // Cannot create query on provider, skip
                        return null;
                    }
                    Constraint constraint = convertPath(qom.getConstraint(), jcrStoreProvider.getMountPoint(), jcrStoreProvider.getRelativeRoot(), factory);
                    if (!jcrStoreProvider.getRelativeRoot().equals("")) {
                        Constraint addRelativeRootConstraint = addRelativeRootConstraint(factory, jcrStoreProvider.getRelativeRoot(), qom.getSource());
                        constraint = (constraint == null) ? addRelativeRootConstraint : factory.and(addRelativeRootConstraint, constraint);
                    }
                    query = factory.createQuery(qom.getSource(), constraint, qom.getOrderings(), qom.getColumns());
                } catch (ConstraintViolationException e) {
                    if(logger.isDebugEnabled()){
                        logger.debug(e.getMessage(),e);
                    }
                    // Provider path incompatible with constraints, skip query
                    return null;
                } catch (NamespaceException e) {
                    if(logger.isDebugEnabled()){
                        logger.debug(e.getMessage(),e);
                    }
                    // Query nodetype / namespace does not exist on this provider, skip query
                    return null;
                }
            }
            if (query != null && query instanceof QueryObjectModel) {
                if (Query.JCR_SQL2.equals(language)) {
                    QueryObjectModel qom = QueryServiceImpl.getInstance().modifyAndOptimizeQuery((QueryObjectModel) query, factory, session);
                    Constraint constraint;
                    if (jcrStoreProvider.isDefault()) {
                        constraint = filterMountPoints(qom.getConstraint(), qom.getSource(), factory);
                    } else {
                        constraint = qom.getConstraint();
                    }
                    query = factory.createQuery(qom.getSource(), constraint, qom.getOrderings(), qom.getColumns());
                }
                if (query instanceof JahiaQueryObjectModelImpl) {
                    JahiaLuceneQueryFactoryImpl lqf = (JahiaLuceneQueryFactoryImpl) ((JahiaQueryObjectModelImpl) query)
                            .getLuceneQueryFactory();
                    lqf.setQueryLanguageAndLocale(LuceneUtils.extractLanguageOrNullFromStatement(statement), session.getLocale());
                }
            }
        }
        return query;
    }

    private Constraint addRelativeRootConstraint(QueryObjectModelFactory f, String relativeRoot, Source source) throws RepositoryException {
        if (source instanceof Selector) {
            String selectorName = ((Selector)source).getSelectorName();
            return f.or(f.sameNode(selectorName,relativeRoot), f.descendantNode(selectorName, relativeRoot));
        } else if (source instanceof Join) {
            return f.and(addRelativeRootConstraint(f,relativeRoot,((Join) source).getLeft()), addRelativeRootConstraint(f,relativeRoot, ((Join) source).getRight()));
        }
        throw new RepositoryException("Cannot parse source : "+source);
    }

    private Constraint filterMountPoints(Constraint constraint, Source source, QueryObjectModelFactory f) throws RepositoryException {
        if (source instanceof Selector) {
            Constraint c = f.not(f.propertyExistence(((Selector) source).getSelectorName(), "j:isExternalProviderRoot"));
            if (constraint == null) {
                return c;
            } else {
                return f.and(c, constraint);
            }
        } else if (source instanceof Join) {
            constraint = filterMountPoints(constraint, ((Join) source).getLeft(), f);
            constraint = filterMountPoints(constraint, ((Join) source).getRight(), f);
        }
        return constraint;
    }

    private Constraint convertPath(Constraint constraint, String mountPoint, String relativeRoot, QueryObjectModelFactory f) throws RepositoryException {
        if (constraint instanceof ChildNode) {
            String root = ((ChildNode) constraint).getParentPath();
            String rootWithSlash = root.endsWith("/") ? root : root + "/";
            String rootNoSlash = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
            if (mountPoint.equals(rootNoSlash)) {
                // Path constraint is the mount point -> create new constraint on root child nodes only
                return f.childNode(((ChildNode) constraint).getSelectorName(), relativeRoot.equals("") ? "/" : relativeRoot);
            }
            if (mountPoint.startsWith(rootWithSlash)) {
                if (root.equals(StringUtils.substringBeforeLast(mountPoint, "/"))) {
                    // Asked for root node
                    return f.sameNode(((ChildNode) constraint).getSelectorName(), relativeRoot.equals("") ? "/" : relativeRoot);
                }
                // Mount point in under path constraint -> do not search
                throw CONSTRAINT_VIOLATION_EXCEPTION;
            }
            if (rootWithSlash.startsWith(mountPoint + "/")) {
                // Path constraint is under mount point -> create new constraint with local path
                return f.childNode(((ChildNode) constraint).getSelectorName(), relativeRoot + rootNoSlash.substring(mountPoint.length()));
            }
            // Path constraint incompatible with mount point
            throw CONSTRAINT_VIOLATION_EXCEPTION;
        } else if (constraint instanceof DescendantNode) {
            String root = ((DescendantNode) constraint).getAncestorPath();
            String rootWithSlash = root.endsWith("/") ? root : root + "/";
            String rootNoSlash = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
            if (mountPoint.startsWith(rootWithSlash) || mountPoint.equals(rootNoSlash)) {
                // Mount point in under path constraint -> remove constraint
                if (!relativeRoot.equals("")) {
                    return f.descendantNode(((DescendantNode) constraint).getSelectorName(), relativeRoot);
                } else {
                    return null;
                }
            }
            if (rootWithSlash.startsWith(mountPoint + "/")) {
                // Path constraint is under mount point -> create new constraint with local path
                return f.descendantNode(((DescendantNode) constraint).getSelectorName(), relativeRoot + root.substring(mountPoint.length()));
            }
            // Path constraint incompatible with mount point
            throw CONSTRAINT_VIOLATION_EXCEPTION;
        } else if (constraint instanceof SameNode) {
            String root = ((SameNode) constraint).getPath();
            if (root.startsWith(mountPoint)) {
                return f.sameNode(((SameNode) constraint).getSelectorName(),relativeRoot + root.substring(mountPoint.length()));
            }
            throw CONSTRAINT_VIOLATION_EXCEPTION;
        } else if (constraint instanceof And) {
            Constraint c1 = convertPath(((And) constraint).getConstraint1(), mountPoint, relativeRoot, f);
            Constraint c2 = convertPath(((And) constraint).getConstraint2(), mountPoint, relativeRoot, f);
            if (c1 == null) {
                return c2;
            }
            if (c2 == null) {
                return c1;
            }
            return f.and(c1, c2);
        } else if (constraint instanceof Or) {
            Constraint c1 = null;
            try {
                c1 = convertPath(((Or) constraint).getConstraint1(), mountPoint, relativeRoot, f);
            } catch (ConstraintViolationException e) {
                return convertPath(((Or) constraint).getConstraint2(), mountPoint, relativeRoot, f);
            }
            Constraint c2 = null;
            try {
                c2 = convertPath(((Or) constraint).getConstraint2(), mountPoint, relativeRoot, f);
            } catch (ConstraintViolationException e) {
                return c1;
            }
            if (c1 == null || c2 == null) {
                return null;
            }
            return f.or(c1, c2);
        } else if (constraint instanceof Not) {
            Constraint notConstraint = null;
            try {
                notConstraint = convertPath(((Not) constraint).getConstraint(), mountPoint, relativeRoot, f);
            } catch (ConstraintViolationException e) {
                return null;
            }
            if (notConstraint == null) {
                throw CONSTRAINT_VIOLATION_EXCEPTION;
            }
            return f.not(notConstraint);
        }
        return constraint;
    }


    public QueryResultWrapper execute() throws RepositoryException {
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
            if (it.hasNext() && (queryLimit >= 0 || queryOffset > 0)) {
                // If we have another provider query and either a limit or offset set, we need to recalculate them.
                long resultCount = getResultCount(queryResult);
                if (queryLimit >= 0) {
                    if (resultCount >= queryLimit) {
                        // limit has already been reached -> return.
                        break;
                    }
                    // reduce the limit for the next query
                    queryLimit -= resultCount;
                }
                if (queryOffset > 0) {
                    if (resultCount == 0) {
                        // There were no results in the first query - it may be because the offset was greater than the
                        // full result count. We need to get the full result count to calculate the offset for the next provider.
                        Query noLimitNoOffsetQuery = getQuery(entry.getKey());
                        queryOffset -= getResultCount(new QueryResultAdapter(noLimitNoOffsetQuery.execute(), entry.getKey(), session));
                    } else {
                        // Results found already - next query start from 0
                        queryOffset = 0;
                    }
                }
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