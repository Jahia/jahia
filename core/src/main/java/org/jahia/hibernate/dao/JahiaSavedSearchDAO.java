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

import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class JahiaSavedSearchDAO extends AbstractGeneratorDAO {

    public JahiaSavedSearch getSearch(Integer searchId) {
        HibernateTemplate template = getHibernateTemplate();
        return (JahiaSavedSearch)template.get(JahiaSavedSearch.class,searchId);
    }    
    

    public JahiaSavedSearch getSearch(String searchTitle) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<JahiaSavedSearch> result = template.find("from JahiaSavedSearch s where s.title=?",
                new Object[] { searchTitle });
        return result.isEmpty() ? null : result.get(0);
    }

    public JahiaSavedSearch save(final JahiaSavedSearch search) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Transaction transaction = session.beginTransaction();
                try {
                    if (search.getId() == null
                        || search.getId().intValue() == -1) {
                        search.setId(getNextInteger(search));
                        session.save(search);
                    } else {
                        session.merge(search);
                    }
                    transaction.commit();
                } catch(Exception t) {
                    transaction.rollback();
                }
                return null;
            }
        });
        return search;
    }

    public List<JahiaSavedSearch> getSearches() {
        List<JahiaSavedSearch> retList = null;
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        retList = template.find("from JahiaSavedSearch s order by s.title asc");
        return retList;
    }

    public List<JahiaSavedSearch> getSearches(String owner) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("from JahiaSavedSearch s where s.ownerKey=?"
                + " order by s.title asc", owner);
    }

    public void deleteSearch(Integer searchId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaSavedSearch s where s.id = ?", searchId));
    }

    public Map<Serializable, Integer> deleteAllFromSite(Integer siteID) {
        String queryString = "from JahiaSavedSearch c where c.jahiaSite.id=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List<JahiaSavedSearch> list = template.find(queryString,siteID);
        Map<Serializable, Integer> map = new HashMap<Serializable, Integer>(list.size());
        for (JahiaSavedSearch data : list) {
            map.put(data.getJahiaAcl().getId(),data.getJahiaAcl().getId());
        }
        template.deleteAll(list);
        return map;
    }
}
