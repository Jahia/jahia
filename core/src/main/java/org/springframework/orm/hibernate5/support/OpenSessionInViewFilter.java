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
package org.springframework.orm.hibernate5.support;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet Filter that binds a Hibernate Session to the thread for the entire
 * processing of the request. Intended for the "Open Session in View" pattern,
 * i.e. to allow for lazy loading in web views despite the original transactions
 * already being completed.
 *
 * <p>This filter makes Hibernate Sessions available via the current thread, which
 * will be autodetected by transaction managers. It is suitable for service layer
 * transactions via {@link org.springframework.orm.hibernate5.HibernateTransactionManager}
 * as well as for non-transactional execution (if configured appropriately).
 *
 * <p><b>NOTE</b>: This filter will by default <i>not</i> flush the Hibernate Session,
 * with the flush mode set to {@code FlushMode.MANUAL}. It assumes to be used
 * in combination with service layer transactions that care for the flushing: The
 * active transaction manager will temporarily change the flush mode to
 * {@code FlushMode.AUTO} during a read-write transaction, with the flush
 * mode reset to {@code FlushMode.MANUAL} at the end of each transaction.
 *
 * <p><b>WARNING:</b> Applying this filter to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes with already loaded instances of the same objects.
 *
 * <p>Looks up the SessionFactory in Spring's root web application context.
 * Supports a "sessionFactoryBeanName" filter init-param in {@code web.xml};
 * the default bean name is "sessionFactory".
 *
 * @author Juergen Hoeller
 * @see #lookupSessionFactory
 * @see OpenSessionInViewInterceptor
 * @see OpenSessionInterceptor
 * @see org.springframework.orm.hibernate5.HibernateTransactionManager
 * @see TransactionSynchronizationManager
 * @see SessionFactory#getCurrentSession()
 * @since 4.2
 */
public class OpenSessionInViewFilter extends OncePerRequestFilter {

    /**
     * The default bean name used for the session factory.
     */
    public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";

    private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

    /**
     * Return the bean name of the SessionFactory to fetch from Spring's
     * root application context.
     */
    protected String getSessionFactoryBeanName() {
        return this.sessionFactoryBeanName;
    }

    /**
     * Set the bean name of the SessionFactory to fetch from Spring's
     * root application context. Default is "sessionFactory".
     *
     * @see #DEFAULT_SESSION_FACTORY_BEAN_NAME
     */
    public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
        this.sessionFactoryBeanName = sessionFactoryBeanName;
    }

    /**
     * Returns "false" so that the filter may re-bind the opened Hibernate
     * {@code Session} to each asynchronously dispatched thread and postpone
     * closing it until the very last asynchronous dispatch.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /**
     * Returns "false" so that the filter may provide a Hibernate
     * {@code Session} to each error dispatches.
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        SessionFactory sessionFactory = lookupSessionFactory(request);
        boolean participate = false;

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        String key = getAlreadyFilteredAttributeName();

        if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
            // Do not modify the Session: just set the participate flag.
            participate = true;
        } else {
            boolean isFirstRequest = !isAsyncDispatch(request);
            if (isFirstRequest || !applySessionBindingInterceptor(asyncManager, key)) {
                logger.debug("Opening Hibernate Session in OpenSessionInViewFilter");
                Session session = openSession(sessionFactory);
                SessionHolder sessionHolder = new SessionHolder(session);
                TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);

                AsyncRequestInterceptor interceptor = new AsyncRequestInterceptor(sessionFactory, sessionHolder);
                asyncManager.registerCallableInterceptor(key, interceptor);
                asyncManager.registerDeferredResultInterceptor(key, interceptor);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!participate) {
                SessionHolder sessionHolder =
                        (SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
                if (!isAsyncStarted(request)) {
                    logger.debug("Closing Hibernate Session in OpenSessionInViewFilter");
                    SessionFactoryUtils.closeSession(sessionHolder.getSession());
                }
            }
        }
    }

    /**
     * Look up the SessionFactory that this filter should use,
     * taking the current HTTP request as argument.
     * <p>The default implementation delegates to the {@link #lookupSessionFactory()}
     * variant without arguments.
     *
     * @param request the current request
     * @return the SessionFactory to use
     */
    protected SessionFactory lookupSessionFactory(HttpServletRequest request) {
        return lookupSessionFactory();
    }

    /**
     * Look up the SessionFactory that this filter should use.
     * <p>The default implementation looks for a bean with the specified name
     * in Spring's root application context.
     *
     * @return the SessionFactory to use
     * @see #getSessionFactoryBeanName
     */
    protected SessionFactory lookupSessionFactory() {
        if (logger.isDebugEnabled()) {
            logger.debug("Using SessionFactory '" + getSessionFactoryBeanName() + "' for OpenSessionInViewFilter");
        }
        WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        return wac.getBean(getSessionFactoryBeanName(), SessionFactory.class);
    }

    /**
     * Open a Session for the SessionFactory that this filter uses.
     * <p>The default implementation delegates to the {@link SessionFactory#openSession}
     * method and sets the {@link Session}'s flush mode to "MANUAL".
     *
     * @param sessionFactory the SessionFactory that this filter uses
     * @return the Session to use
     * @throws DataAccessResourceFailureException if the Session could not be created
     * @see FlushMode#MANUAL
     */
    protected Session openSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
        try {
            Session session = sessionFactory.openSession();
            session.setHibernateFlushMode(FlushMode.MANUAL);
            return session;
        } catch (HibernateException ex) {
            throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
        }
    }

    private boolean applySessionBindingInterceptor(WebAsyncManager asyncManager, String key) {
        CallableProcessingInterceptor cpi = asyncManager.getCallableInterceptor(key);
        if (cpi == null) {
            return false;
        }
        ((AsyncRequestInterceptor) cpi).bindSession();
        return true;
    }

}
