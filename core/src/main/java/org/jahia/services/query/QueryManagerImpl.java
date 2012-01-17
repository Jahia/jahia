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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;

import org.apache.jackrabbit.core.query.JahiaQueryObjectModelImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;

/**
 * Implementation of the {@link QueryManager} to support multiple providers.
 * 
 * @author Thomas Draier
 */
public class QueryManagerImpl implements QueryManager {

    /**
     * Invocation handler to decorate the {@link QueryObjectModelFactory}
     * instance.
     * 
     * @author Sergiy Shyrkov
     */
    private class QOMFactoryInvocationHandler implements InvocationHandler {
        private final JCRStoreProvider provider;
        private final QueryObjectModelFactory underlying;

        QOMFactoryInvocationHandler(QueryObjectModelFactory underlying, JCRStoreProvider provider) {
            super();
            this.underlying = underlying;
            this.provider = provider;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            if ("createQuery".equals(method.getName())) {
                final QueryObjectModel qom = ServicesRegistry.getInstance().getQueryService().modifyAndOptimizeQuery(
                        (Source) args[0], (Constraint) args[1], (Ordering[]) args[2], (Column[]) args[3],
                        underlying, session);
                if (provider.isDefault() && qom instanceof JahiaQueryObjectModelImpl) {
                    JahiaLuceneQueryFactoryImpl lqf = (JahiaLuceneQueryFactoryImpl) ((JahiaQueryObjectModelImpl) qom)
                            .getLuceneQueryFactory();
                    
                    lqf.setProvider(provider);
                    lqf.setJcrSession(session);
                }                
                return Proxy.newProxyInstance(qom.getClass().getClassLoader(), new Class[] { QueryObjectModel.class },
                        new QOMInvocationHandler(qom, provider));
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
        private final JCRStoreProvider provider;
        private final QueryObjectModel underlying;

        QOMInvocationHandler(QueryObjectModel underlying, JCRStoreProvider provider) {
            super();
            this.underlying = underlying;
            this.provider = provider;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            try {
                Object result = method.invoke(underlying, args);
                if ("execute".equals(method.getName())) {
                    result = new QueryResultWrapper((QueryResult) result, provider, session);
                }
                return result;
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

    public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
        return new QueryWrapper(statement, language, session, sessionFactory);
    }

    public QueryObjectModelFactory getQOMFactory() {
        try {
            final JCRStoreProvider provider = sessionFactory.getProvider("/");
            final QueryObjectModelFactory qomFactory = provider.getQueryManager(session).getQOMFactory();

            return (QueryObjectModelFactory) Proxy.newProxyInstance(qomFactory.getClass().getClassLoader(),
                    new Class[] { QueryObjectModelFactory.class },
                    new QOMFactoryInvocationHandler(qomFactory, provider));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
        try {
            return new QueryWrapper(node, session, sessionFactory);
        } catch (PathNotFoundException e) {
            throw new InvalidQueryException("Node is not of type nt:query");
        }
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        List<String> res = new ArrayList<String>();
        for (JCRStoreProvider jcrStoreProvider : sessionFactory.getProviders().values()) {
            QueryManager qm = jcrStoreProvider.getQueryManager(session);
            if (qm != null) {
                res.addAll(Arrays.asList(qm.getSupportedQueryLanguages()));
            }
        }
        return res.toArray(new String[res.size()]);
    }
}