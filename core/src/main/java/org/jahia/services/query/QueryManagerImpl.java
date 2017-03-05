/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.QueryManagerWrapper;

/**
 * Implementation of the {@link QueryManager} to support multiple providers.
 * 
 * @author Thomas Draier
 */
public class QueryManagerImpl implements QueryManagerWrapper {

    /**
     * Invocation handler to decorate the {@link QueryObjectModelFactory}
     * instance.
     * 
     * @author Sergiy Shyrkov
     */
    private class QOMFactoryInvocationHandler implements InvocationHandler {
        private final QueryObjectModelFactory underlying;

        QOMFactoryInvocationHandler(QueryObjectModelFactory underlying) {
            super();
            this.underlying = underlying;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            if ("createQuery".equals(method.getName())) {
                final QueryObjectModel qom = ServicesRegistry.getInstance().getQueryService().modifyAndOptimizeQuery(
                        (Source) args[0], (Constraint) args[1], (Ordering[]) args[2], (Column[]) args[3],
                        underlying, session);
                return Proxy.newProxyInstance(qom.getClass().getClassLoader(), new Class[] { QueryObjectModel.class },
                        new QOMInvocationHandler(qom));
            } else {
                try {
                    return method.invoke(underlying, args);
                } catch (InvocationTargetException e) {
                    // lets unwrap the exception
                    Throwable throwable = e.getCause();
                    if (throwable instanceof Exception) {
                        Exception exception = (Exception) throwable;
                        throw exception;
                    } else {
                        Error error = (Error) throwable;
                        throw error;
                    }
                }
            }
        }
    }

    /**
     * Invocation handler to decorate the {@link QueryObjectModel} instance in
     * order to wrap the query result.
     * 
     * @author Sergiy Shyrkov
     */
    private class QOMInvocationHandler implements InvocationHandler {
        private final QueryObjectModel underlying;
        private long limit = -1;
        private long offset = 0;

        QOMInvocationHandler(QueryObjectModel underlying) {
            super();
            this.underlying = underlying;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            try {
                if ("execute".equals(method.getName())) {
                    QueryWrapper queryWrapper = new QueryWrapper(underlying, session, sessionFactory);
                    queryWrapper.setOffset(offset);
                    queryWrapper.setLimit(limit);
                    return queryWrapper.execute();
                } else if ("setLimit".equals(method.getName())) {
                    limit = (Long) args[0];
                } else if ("setOffset".equals(method.getName())) {
                    offset = (Long) args[0];
                }
                return method.invoke(underlying, args);
            } catch (InvocationTargetException e) {
                // lets unwrap the exception
                Throwable throwable = e.getCause();
                if (throwable instanceof Exception) {
                    Exception exception = (Exception) throwable;
                    throw exception;
                } else {
                    Error error = (Error) throwable;
                    throw error;
                }
            }
        }
    }

    private JCRSessionWrapper session;
    private JCRSessionFactory sessionFactory;

    public QueryManagerImpl(JCRSessionWrapper session, JCRSessionFactory sessionFactory) {
        super();
        this.session = session;
        this.sessionFactory = sessionFactory;
    }

    public QueryWrapper createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
        QueryWrapper queryWrapper = new QueryWrapper(statement, language, session, sessionFactory);
        if (queryWrapper.getQueries().isEmpty()) {
            throw new InvalidQueryException(sessionFactory.getProviders().isEmpty() ? "Query could not be created. Store provider is not initialized yet" : "No query could be created for the unknown query language '" + language + "'");
        }
        return queryWrapper;
    }

    public QueryWrapper createDualQuery(String statement, String language, String sqlFallbackStatement) throws InvalidQueryException, RepositoryException {
        QueryWrapper queryWrapper = new QueryWrapper(statement, language, sqlFallbackStatement, session, sessionFactory);
        if (queryWrapper.getQueries().isEmpty()) {
            throw new InvalidQueryException(sessionFactory.getProviders().isEmpty() ? "Query could not be created. Store provider is not initialized yet" : "No query could be created for the unknown query language '" + language + "'");
        }
        return queryWrapper;
    }


    public QueryObjectModelFactory getQOMFactory() {
        try {
            final JCRStoreProvider provider = sessionFactory.getProvider("/");
            final QueryObjectModelFactory qomFactory = provider.getQueryManager(session).getQOMFactory();

            return (QueryObjectModelFactory) Proxy.newProxyInstance(qomFactory.getClass().getClassLoader(),
                    new Class[] { QueryObjectModelFactory.class },
                    new QOMFactoryInvocationHandler(qomFactory));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public QueryWrapper getQuery(Node node) throws InvalidQueryException, RepositoryException {
        try {
            return new QueryWrapper(node, session, sessionFactory);
        } catch (PathNotFoundException e) {
            throw new InvalidQueryException("Node is not of type nt:query");
        }
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        Set<String> res = new HashSet<String>();
        for (JCRStoreProvider jcrStoreProvider : sessionFactory.getProviders().values()) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session);
            if (qm != null) {
                for (String lang : qm.getSupportedQueryLanguages()) {
                    res.add(lang);
                }
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
}