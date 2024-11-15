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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.util.Assert;

/**
 * Convenient super class for Hibernate-based data access objects.
 *
 * <p>Requires a {@link SessionFactory} to be set, providing a
 * {@link org.springframework.orm.hibernate5.HibernateTemplate} based on it to
 * subclasses through the {@link #getHibernateTemplate()} method.
 * Can alternatively be initialized directly with a HibernateTemplate,
 * in order to reuse the latter's settings such as the SessionFactory,
 * exception translator, flush mode, etc.
 *
 * <p>This class will create its own HibernateTemplate instance if a SessionFactory
 * is passed in. The "allowCreate" flag on that HibernateTemplate will be "true"
 * by default. A custom HibernateTemplate instance can be used through overriding
 * {@link #createHibernateTemplate}.
 *
 * <p><b>NOTE: Hibernate access code can also be coded in plain Hibernate style.
 * Hence, for newly started projects, consider adopting the standard Hibernate
 * style of coding data access objects instead, based on
 * {@link SessionFactory#getCurrentSession()}.
 * This HibernateTemplate primarily exists as a migration helper for Hibernate 3
 * based data access code, to benefit from bug fixes in Hibernate 5.x.</b>
 *
 * @author Juergen Hoeller
 * @see #setSessionFactory
 * @see #getHibernateTemplate
 * @see org.springframework.orm.hibernate5.HibernateTemplate
 * @since 4.2
 */
public abstract class HibernateDaoSupport extends DaoSupport {

    private HibernateTemplate hibernateTemplate;

    /**
     * Create a HibernateTemplate for the given SessionFactory.
     * Only invoked if populating the DAO with a SessionFactory reference!
     * <p>Can be overridden in subclasses to provide a HibernateTemplate instance
     * with different configuration, or a custom HibernateTemplate subclass.
     *
     * @param sessionFactory the Hibernate SessionFactory to create a HibernateTemplate for
     * @return the new HibernateTemplate instance
     * @see #setSessionFactory
     */
    protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }

    /**
     * Return the Hibernate SessionFactory used by this DAO.
     */
    public final SessionFactory getSessionFactory() {
        return (this.hibernateTemplate != null ? this.hibernateTemplate.getSessionFactory() : null);
    }

    /**
     * Set the Hibernate SessionFactory to be used by this DAO.
     * Will automatically create a HibernateTemplate for the given SessionFactory.
     *
     * @see #createHibernateTemplate
     * @see #setHibernateTemplate
     */
    public final void setSessionFactory(SessionFactory sessionFactory) {
        if (this.hibernateTemplate == null || sessionFactory != this.hibernateTemplate.getSessionFactory()) {
            this.hibernateTemplate = createHibernateTemplate(sessionFactory);
        }
    }

    /**
     * Return the HibernateTemplate for this DAO,
     * pre-initialized with the SessionFactory or set explicitly.
     * <p><b>Note: The returned HibernateTemplate is a shared instance.</b>
     * You may introspect its configuration, but not modify the configuration
     * (other than from within an {@link #initDao} implementation).
     * Consider creating a custom HibernateTemplate instance via
     * {@code new HibernateTemplate(getSessionFactory())}, in which case
     * you're allowed to customize the settings on the resulting instance.
     */
    public final HibernateTemplate getHibernateTemplate() {
        return this.hibernateTemplate;
    }

    /**
     * Set the HibernateTemplate for this DAO explicitly,
     * as an alternative to specifying a SessionFactory.
     *
     * @see #setSessionFactory
     */
    public final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    @Override
    protected final void checkDaoConfig() {
        if (this.hibernateTemplate == null) {
            throw new IllegalArgumentException("'sessionFactory' or 'hibernateTemplate' is required");
        }
    }


    /**
     * Conveniently obtain the current Hibernate Session.
     *
     * @return the Hibernate Session
     * @throws DataAccessResourceFailureException if the Session couldn't be created
     * @see SessionFactory#getCurrentSession()
     */
    protected final Session currentSession() throws DataAccessResourceFailureException {
        SessionFactory sessionFactory = getSessionFactory();
        Assert.state(sessionFactory != null, "No SessionFactory set");
        return sessionFactory.getCurrentSession();
    }

}
