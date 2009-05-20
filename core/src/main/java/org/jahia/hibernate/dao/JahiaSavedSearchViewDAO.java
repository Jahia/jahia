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

import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearchView;
import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearchViewPK;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.util.List;

/**
 *
 */
public class JahiaSavedSearchViewDAO extends HibernateDaoSupport {


    public JahiaSavedSearchView getView(JahiaSavedSearchViewPK comp_id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaSavedSearchView)template.get(JahiaSavedSearchView.class,comp_id);
    }

    public void save(final JahiaSavedSearchView view) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Transaction transaction = session.beginTransaction();
                try {
                    session.saveOrUpdate(view);
                    transaction.commit();
                } catch(Exception t) {
                    transaction.rollback();
                }
                return null;
            }
        });
    }

    public List getViews() {
        List retList = null;
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        retList = getHibernateTemplate().find("from JahiaSavedSearchView v");
        return retList;
    }

    public void deleteView(JahiaSavedSearchViewPK comp_id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSavedSearchView v where v.comp_id = ?", comp_id));
    }

}
