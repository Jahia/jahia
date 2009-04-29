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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * User: Serge Huber
 * Date: 14 nov. 2005
 * Time: 19:22:26
 * Copyright (C) Jahia Inc.
 */
public class AbstractGeneratorDAO extends HibernateDaoSupport {
    private IDGeneratorDAO idGeneratorDAO;

    public IDGeneratorDAO getIdGeneratorDAO() {
        return idGeneratorDAO;
    }

    public void setIdGeneratorDAO(IDGeneratorDAO idGeneratorDAO) {
        this.idGeneratorDAO = idGeneratorDAO;
    }

    public Integer getNextInteger(Object object) {
        try {
            return idGeneratorDAO.getNextInteger(object.getClass().getName());
        } catch (Exception e) {
            logger.error("SERIOUS ERROR : Error while trying to generate new ID for sequence " + object.getClass().getName() + ", returning ID null", e);
            return null;
        }
    }

    public Long getNextLong(Object object) {
        try {
            return idGeneratorDAO.getNextLong(object.getClass().getName());
        } catch (Exception e) {
            logger.error("SERIOUS ERROR : Error while trying to generate new ID for sequence " + object.getClass().getName() + ", returning ID null", e);
            return null;
        }
    }

    public Integer getNextIntegerByClass(Class clazz) {
        try {
            return idGeneratorDAO.getNextInteger(clazz.getName());
        } catch (Exception e) {
            logger.error("SERIOUS ERROR : Error while trying to generate new ID for sequence " + clazz.getName() + ", returning ID null", e);
            return null;
        }
    }

    public Long getNextLongByClass(Class clazz) {
        try {
            return idGeneratorDAO.getNextLong(clazz.getName());
        } catch (Exception e) {
            logger.error("SERIOUS ERROR : Error while trying to generate new ID for sequence " + clazz.getName() + ", returning ID null", e);
            return null;
        }
    }

    protected List findByCriteria(final HibernateTemplate template,
            final DetachedCriteria detachedCriteria) {
        return (List) findByCriteria(template, detachedCriteria, false);
    }

    protected Object findByCriteriaUnique(final HibernateTemplate template,
            final DetachedCriteria detachedCriteria) {
        return findByCriteria(template, detachedCriteria, true);
    }

    private Object findByCriteria(final HibernateTemplate template,
            final DetachedCriteria detachedCriteria, final boolean uniqueResult) {
        return template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session)
                    throws HibernateException {
                Criteria criteria = detachedCriteria
                        .getExecutableCriteria(session);
                if (template.isCacheQueries()) {
                    criteria.setCacheable(true);
                    if (template.getQueryCacheRegion() != null) {
                        criteria.setCacheRegion(template.getQueryCacheRegion());
                    }
                }
                SessionFactoryUtils.applyTransactionTimeout(criteria,
                        getSessionFactory());
                return uniqueResult ? criteria.uniqueResult() : criteria.list();
            }
        }, true);
    }

}
