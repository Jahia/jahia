/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.springframework.orm.hibernate5.support;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Simple AOP Alliance {@link MethodInterceptor} implementation that binds a new
 * Hibernate {@link Session} for each method invocation, if none bound before.
 *
 * <p>This is a simple Hibernate Session scoping interceptor along the lines of
 * {@link OpenSessionInViewInterceptor}, just for use with AOP setup instead of
 * MVC setup. It opens a new {@link Session} with flush mode "MANUAL" since the
 * Session is only meant for reading, except when participating in a transaction.
 *
 * @author Juergen Hoeller
 * @see OpenSessionInViewInterceptor
 * @see OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate5.HibernateTransactionManager
 * @see TransactionSynchronizationManager
 * @see SessionFactory#getCurrentSession()
 * @since 4.2
 */
public class OpenSessionInterceptor implements MethodInterceptor, InitializingBean {

    private SessionFactory sessionFactory;

    /**
     * Return the Hibernate SessionFactory that should be used to create Hibernate Sessions.
     */
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    /**
     * Set the Hibernate SessionFactory that should be used to create Hibernate Sessions.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void afterPropertiesSet() {
        if (getSessionFactory() == null) {
            throw new IllegalArgumentException("Property 'sessionFactory' is required");
        }
    }


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        SessionFactory sf = getSessionFactory();
        Assert.state(sf != null, "No SessionFactory set");

        if (!TransactionSynchronizationManager.hasResource(sf)) {
            // New Session to be bound for the current method's scope...
            Session session = openSession(sf);
            try {
                TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
                return invocation.proceed();
            } finally {
                SessionFactoryUtils.closeSession(session);
                TransactionSynchronizationManager.unbindResource(sf);
            }
        } else {
            // Pre-bound Session found -> simply proceed.
            return invocation.proceed();
        }
    }

    /**
     * Open a Session for the given SessionFactory.
     * <p>The default implementation delegates to the {@link SessionFactory#openSession}
     * method and sets the {@link Session}'s flush mode to "MANUAL".
     *
     * @param sessionFactory the SessionFactory to use
     * @return the Session to use
     * @throws DataAccessResourceFailureException if the Session could not be created
     * @see FlushMode#MANUAL
     * @since 5.0
     */
    protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
        Session session = openSession();
        if (session == null) {
            try {
                session = sessionFactory.openSession();
                session.setHibernateFlushMode(FlushMode.MANUAL);
            } catch (HibernateException ex) {
                throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
            }
        }
        return session;
    }

    /**
     * Open a Session for the given SessionFactory.
     *
     * @deprecated as of 5.0, in favor of {@link #openSession(SessionFactory)}
     */
    @Deprecated
    protected Session openSession() throws DataAccessResourceFailureException {
        return null;
    }

}
