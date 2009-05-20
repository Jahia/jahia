/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
