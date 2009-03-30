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

