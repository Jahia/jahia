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
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaSite;
import org.jahia.hibernate.model.JahiaSiteProp;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 23 déc. 2004
 * Time: 14:05:18
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSitePropertyDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private static final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger (JahiaSitePropertyDAO.class);

// -------------------------- OTHER METHODS --------------------------

    public List<JahiaSiteProp> getSitePropById(Integer id) {
        List<JahiaSiteProp> retval = null;
        String hql = "from JahiaSiteProp aTable WHERE aTable.comp_id.id = ?";
        if (id == null) {
            log.error("Error: Cannot use null in query");
            throw new RuntimeException("Error: Cannot use null in query");
        } else {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            retval = template.find(hql, id);
        }
        return retval;
    }

    public JahiaSiteProp getSitePropByKey(JahiaSite id, String name) {
        JahiaSiteProp retval = null;
        String hql = "from JahiaSiteProp aTable WHERE aTable.comp_id.id = ? and aTable.comp_id.name = ?";
        if (id == null || name == null) {
            log.error("Error: Cannot use null in query for unique column site "+id+" key= "+name);
            throw new RuntimeException("Error: Cannot use null in query for unique column");
        } else {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            List<JahiaSiteProp> objects = template.find(hql, new Object[]{id.getId(), name});
            if (objects.size() == 1) {
                retval = objects.get(0);
            } else if (objects.size() > 1) {
                log.error("Error: multiple values returned for unique column:" + objects);
                throw new RuntimeException("Error: multiple values returned for unique column:" + objects);
            } else {
                throw new ObjectRetrievalFailureException(JahiaSiteProp.class, id + ", " + name);
            }
        }

        return retval;
    }

    public void remove(JahiaSite id, String name) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(getSitePropByKey(id, name));
    }

    public void save(JahiaSiteProp siteProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(siteProp);
    }

    public void update(JahiaSiteProp siteProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.update(siteProp);
    }

    public void deleteAllFromSite(Integer siteID) {
        String queryString = "from JahiaSiteProp c where c.siteId=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find(queryString,siteID));
    }

    public List<Integer> findSiteIdByPropertyNameAndValue(String name, String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.id from JahiaSiteProp p " +
                             "where p.comp_id.name=? and p.value=?", new Object[]{name, value});
    }

}

