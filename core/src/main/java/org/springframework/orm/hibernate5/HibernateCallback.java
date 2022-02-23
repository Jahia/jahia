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

import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Callback interface for Hibernate code. To be used with {@link HibernateTemplate}'s
 * execution methods, often as anonymous classes within a method implementation.
 * A typical implementation will call {@code Session.load/find/update} to perform
 * some operations on persistent objects.
 *
 * @param <T> the result type
 * @author Juergen Hoeller
 * @see HibernateTemplate
 * @see HibernateTransactionManager
 * @since 4.2
 */
@FunctionalInterface
public interface HibernateCallback<T> {

    /**
     * Gets called by {@code HibernateTemplate.execute} with an active
     * Hibernate {@code Session}. Does not need to care about activating
     * or closing the {@code Session}, or handling transactions.
     * <p>Allows for returning a result object created within the callback,
     * i.e. a domain object or a collection of domain objects.
     * A thrown custom RuntimeException is treated as an application exception:
     * It gets propagated to the caller of the template.
     *
     * @param session active Hibernate session
     * @return a result object, or {@code null} if none
     * @throws HibernateException if thrown by the Hibernate API
     * @see HibernateTemplate#execute
     */
    T doInHibernate(Session session) throws HibernateException;

}
