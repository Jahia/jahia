/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.hibernate.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.jahia.hibernate.model.SubscriptionData;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * DAO service for managing the persistence of user subscriptions.
 * 
 * @author Sergiy Shyrkov
 */
public class SubscriptionDAO extends AbstractGeneratorDAO {

    public void delete(int subscriptionId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        SubscriptionData subscription = findById(subscriptionId);
        if (subscription != null) {
            template.delete(subscription);
        }
    }

    public void delete(SubscriptionData subscription) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.delete(subscription);
    }

    public void deleteAll(List<SubscriptionData> subscriptions) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(subscriptions);
    }

    /**
     * Find subscriptions "by example".
     * 
     * @param criteria
     *            the subscription data object template with the available
     *            search criteria
     * @param excludedProperties
     *            properties to ignore in the criteria
     * @return list of subscription data objects matching the criteria
     */
    public List<SubscriptionData> findAll(SubscriptionData subscription,
            String... excludedProperties) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        Example ex = Example.create(subscription);
        for (String excluded : excludedProperties) {
            ex.excludeProperty(excluded);
        }
        return template.findByCriteria(DetachedCriteria.forClass(
                SubscriptionData.class).add(ex));
    }

    /**
     * Find parent subscriptions "by example" and considering parent object
     * keys.
     * 
     * @param criteria
     *            the subscription data object template with the available
     *            search criteria
     * @param parentObjectKeys
     *            object key to search for
     * @param excludedProperties
     *            properties to ignore in the criteria
     * @return list of subscription data objects matching the criteria
     */
    public List<SubscriptionData> findAllParents(SubscriptionData subscription,
            String[] parentObjectKeys, String... excludedProperties) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        Example ex = Example.create(subscription);
        for (String excluded : excludedProperties) {
            ex.excludeProperty(excluded);
        }
        ex.excludeProperty("objectKey");
        DetachedCriteria criteria = DetachedCriteria.forClass(
                SubscriptionData.class).add(ex);
        if (parentObjectKeys != null && parentObjectKeys.length > 0) {
            criteria.add(Restrictions.in("objectKey", parentObjectKeys));
        }

        return template.findByCriteria(criteria);
    }

    public SubscriptionData findById(int id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        return (SubscriptionData) template.get(SubscriptionData.class, Integer
                .valueOf(id));
    }

    public SubscriptionData update(SubscriptionData subscription) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.saveOrUpdate(subscription);
        return subscription;
    }

}
