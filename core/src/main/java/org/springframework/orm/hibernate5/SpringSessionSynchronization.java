/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.springframework.orm.hibernate5;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Callback for resource cleanup at the end of a Spring-managed transaction
 * for a pre-bound Hibernate Session.
 *
 * @author Juergen Hoeller
 * @since 4.2
 */
public class SpringSessionSynchronization implements TransactionSynchronization, Ordered {

    private final SessionHolder sessionHolder;

    private final SessionFactory sessionFactory;

    private final boolean newSession;

    private boolean holderActive = true;


    public SpringSessionSynchronization(SessionHolder sessionHolder, SessionFactory sessionFactory) {
        this(sessionHolder, sessionFactory, false);
    }

    public SpringSessionSynchronization(SessionHolder sessionHolder, SessionFactory sessionFactory, boolean newSession) {
        this.sessionHolder = sessionHolder;
        this.sessionFactory = sessionFactory;
        this.newSession = newSession;
    }


    private Session getCurrentSession() {
        return this.sessionHolder.getSession();
    }


    @Override
    public int getOrder() {
        return SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER;
    }

    @Override
    public void suspend() {
        if (this.holderActive) {
            TransactionSynchronizationManager.unbindResource(this.sessionFactory);
            // Eagerly disconnect the Session here, to make release mode "on_close" work on JBoss.
            getCurrentSession().disconnect();
        }
    }

    @Override
    public void resume() {
        if (this.holderActive) {
            TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
        }
    }

    @Override
    public void flush() {
        SessionFactoryUtils.flush(getCurrentSession(), false);
    }

    @Override
    public void beforeCommit(boolean readOnly) throws DataAccessException {
        if (!readOnly) {
            Session session = getCurrentSession();
            // Read-write transaction -> flush the Hibernate Session.
            // Further check: only flush when not FlushMode.MANUAL.
            if (!FlushMode.MANUAL.equals(session.getHibernateFlushMode())) {
                SessionFactoryUtils.flush(getCurrentSession(), true);
            }
        }
    }

    @Override
    public void beforeCompletion() {
        try {
            Session session = this.sessionHolder.getSession();
            if (this.sessionHolder.getPreviousFlushMode() != null) {
                // In case of pre-bound Session, restore previous flush mode.
                session.setHibernateFlushMode(this.sessionHolder.getPreviousFlushMode());
            }
            // Eagerly disconnect the Session here, to make release mode "on_close" work nicely.
            session.disconnect();
        } finally {
            // Unbind at this point if it's a new Session...
            if (this.newSession) {
                TransactionSynchronizationManager.unbindResource(this.sessionFactory);
                this.holderActive = false;
            }
        }
    }

    @Override
    public void afterCommit() {
    }

    @Override
    public void afterCompletion(int status) {
        try {
            if (status != STATUS_COMMITTED) {
                // Clear all pending inserts/updates/deletes in the Session.
                // Necessary for pre-bound Sessions, to avoid inconsistent state.
                this.sessionHolder.getSession().clear();
            }
        } finally {
            this.sessionHolder.setSynchronizedWithTransaction(false);
            // Call close() at this point if it's a new Session...
            if (this.newSession) {
                SessionFactoryUtils.closeSession(this.sessionHolder.getSession());
            }
        }
    }

}
