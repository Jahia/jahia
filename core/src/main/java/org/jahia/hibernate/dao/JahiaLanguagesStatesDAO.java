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
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.hibernate.exception.ConstraintViolationException;
import org.jahia.hibernate.model.JahiaLanguagesStates;
import org.jahia.hibernate.model.JahiaLanguagesStatesPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

public class JahiaLanguagesStatesDAO extends AbstractGeneratorDAO {

    public void save(JahiaLanguagesStates workflow) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        try {
            hibernateTemplate.save(workflow);
            hibernateTemplate.flush();
        } catch (ConstraintViolationException e) {
            hibernateTemplate.merge(workflow);
            hibernateTemplate.flush();
        }
    }

    public void saveOrUpdate(JahiaLanguagesStates workflow) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.saveOrUpdate(workflow);
        hibernateTemplate.flush();
    }

    public void update(JahiaLanguagesStates ls) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(ls);
        hibernateTemplate.flush();
    }

    public JahiaLanguagesStates findByPK(String objectKey, String languageCode) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaLanguagesStates) template.load(JahiaLanguagesStates.class, new JahiaLanguagesStatesPK(objectKey, languageCode));
    }

    public List findByObjectKey(String objectKey) {
        String hql = "from JahiaLanguagesStates l where l.comp_id.objectkey=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{objectKey});
    }

    public List findAllStagingAndWaitingObjects(final int siteID) {
        String hql = "from JahiaLanguagesStates l where l.workflowState>1 and l.siteID=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{new Integer(siteID)});
    }

    public List findAllStagingObjects(final int siteID) {
        String hql = "from JahiaLanguagesStates l where l.workflowState=2 and l.siteID=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{new Integer(siteID)});
    }

    public List findAllWaitingObjects(final int siteID) {
        String hql = "from JahiaLanguagesStates l where l.workflowState=3 and l.siteID=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{new Integer(siteID)});
    }

    public void clearEntries(String objectKey) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List entries = template.find(
                "from JahiaLanguagesStates l where l.comp_id.objectkey=?",
                new Object[] { objectKey });

        final HibernateTemplate deleteTemplate = getHibernateTemplate();
        deleteTemplate.setCacheQueries(false);
        deleteTemplate.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        deleteTemplate.deleteAll(entries);
    }

    public void clearEntriesForSite(Integer siteid) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List entries = template.find(
                "from JahiaLanguagesStates l where l.siteID=?",
                new Object[] { siteid });

        final HibernateTemplate deleteTemplate = getHibernateTemplate();
        deleteTemplate.setCacheQueries(false);
        deleteTemplate.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        deleteTemplate.deleteAll(entries);
    }
}