/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

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
        List result = template.find("from JahiaSavedSearch s where s.title=?",
                new Object[] { searchTitle });
        return result.isEmpty() ? null : (JahiaSavedSearch) result.get(0);
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

    public List getSearches() {
        List retList = null;
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        retList = template.find("from JahiaSavedSearch s order by s.title asc");
        return retList;
    }

    public List getSearches(String owner) {
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

    public Map deleteAllFromSite(Integer siteID) {
        String queryString = "from JahiaSavedSearch c where c.jahiaSite.id=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List list = template.find(queryString,siteID);
        Map map = new HashMap(list.size());
        for (int i = 0; i < list.size(); i++) {
            JahiaSavedSearch data = (JahiaSavedSearch) list.get(i);
            map.put(data.getJahiaAcl().getId(),data.getJahiaAcl().getId());
        }
        template.deleteAll(list);
        return map;
    }
}
