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
