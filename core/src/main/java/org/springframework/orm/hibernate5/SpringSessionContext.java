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
package org.springframework.orm.hibernate5;

import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * Implementation of Hibernate 3.1's {@link CurrentSessionContext} interface
 * that delegates to Spring's {@link SessionFactoryUtils} for providing a
 * Spring-managed current {@link Session}.
 *
 * <p>This CurrentSessionContext implementation can also be specified in custom
 * SessionFactory setup through the "hibernate.current_session_context_class"
 * property, with the fully qualified name of this class as value.
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
@SuppressWarnings("serial")
public class SpringSessionContext implements CurrentSessionContext {

    private final SessionFactoryImplementor sessionFactory;

    private TransactionManager transactionManager;

    private CurrentSessionContext jtaSessionContext;


    /**
     * Create a new SpringSessionContext for the given Hibernate SessionFactory.
     *
     * @param sessionFactory the SessionFactory to provide current Sessions for
     */
    public SpringSessionContext(SessionFactoryImplementor sessionFactory) {
        this.sessionFactory = sessionFactory;
        try {
            JtaPlatform jtaPlatform = sessionFactory.getServiceRegistry().getService(JtaPlatform.class);
            this.transactionManager = jtaPlatform.retrieveTransactionManager();
            if (this.transactionManager != null) {
                this.jtaSessionContext = new SpringJtaSessionContext(sessionFactory);
            }
        } catch (Exception ex) {
            LogFactory.getLog(SpringSessionContext.class).warn(
                    "Could not introspect Hibernate JtaPlatform for SpringJtaSessionContext", ex);
        }
    }


    /**
     * Retrieve the Spring-managed Session for the current thread, if any.
     */
    @Override
    public Session currentSession() throws HibernateException {
        Object value = TransactionSynchronizationManager.getResource(this.sessionFactory);
        if (value instanceof Session) {
            return (Session) value;
        } else if (value instanceof SessionHolder) {
            // HibernateTransactionManager
            SessionHolder sessionHolder = (SessionHolder) value;
            Session session = sessionHolder.getSession();
            if (!sessionHolder.isSynchronizedWithTransaction() &&
                    TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new SpringSessionSynchronization(sessionHolder, this.sessionFactory, false));
                sessionHolder.setSynchronizedWithTransaction(true);
                // Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
                // with FlushMode.MANUAL, which needs to allow flushing within the transaction.
                FlushMode flushMode = session.getHibernateFlushMode();
                if (flushMode.equals(FlushMode.MANUAL) &&
                        !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                    session.setHibernateFlushMode(FlushMode.AUTO);
                    sessionHolder.setPreviousFlushMode(flushMode);
                }
            }
            return session;
        } else if (value instanceof EntityManagerHolder) {
            // JpaTransactionManager
            return ((EntityManagerHolder) value).getEntityManager().unwrap(Session.class);
        }

        if (this.transactionManager != null && this.jtaSessionContext != null) {
            try {
                if (this.transactionManager.getStatus() == Status.STATUS_ACTIVE) {
                    Session session = this.jtaSessionContext.currentSession();
                    if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        TransactionSynchronizationManager.registerSynchronization(
                                new SpringFlushSynchronization(session));
                    }
                    return session;
                }
            } catch (SystemException ex) {
                throw new HibernateException("JTA TransactionManager found but status check failed", ex);
            }
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Session session = this.sessionFactory.openSession();
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                session.setHibernateFlushMode(FlushMode.MANUAL);
            }
            SessionHolder sessionHolder = new SessionHolder(session);
            TransactionSynchronizationManager.registerSynchronization(
                    new SpringSessionSynchronization(sessionHolder, this.sessionFactory, true));
            TransactionSynchronizationManager.bindResource(this.sessionFactory, sessionHolder);
            sessionHolder.setSynchronizedWithTransaction(true);
            return session;
        } else {
            throw new HibernateException("Could not obtain transaction-synchronized Session for current thread");
        }
    }

}
