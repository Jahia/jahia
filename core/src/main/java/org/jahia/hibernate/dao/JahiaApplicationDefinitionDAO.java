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
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaAppDef;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 mars 2005
 * Time: 16:05:01
 * To change this template use File | Settings | File Templates.
 */
public class JahiaApplicationDefinitionDAO extends AbstractGeneratorDAO {
    public JahiaAppDef loadApplicationDefinition(Integer definitionId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaAppDef) template.get(JahiaAppDef.class, definitionId);
    }

    public List<Integer> getSiteIds() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct a.site.id from JahiaAppDef a order by a.site.id");
    }

    public JahiaAppDef loadApplicationDefinition(String context) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        final List<JahiaAppDef> list = template.find("from JahiaAppDef a where a.context=?", context);
        JahiaAppDef appDef = null;
        if (list.size() > 0) {
            appDef = (JahiaAppDef) list.get(0);
        }
        return appDef;
    }

    public void save(JahiaAppDef appDef) {
        if (appDef.getId() == null) {
            appDef.setId(getNextInteger(appDef));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(appDef);
    }

    public void update(JahiaAppDef appDef) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.update(appDef);
    }

    public void delete(JahiaAppDef jahiaAppDef) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(jahiaAppDef);
    }

    public List<JahiaAppDef> getVisibleApplications() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        final List<JahiaAppDef> list = template.find("from JahiaAppDef a where a.visible=1 order by a.name");
        return list;
    }

    public List<JahiaAppDef> getAllApplications() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        final List<JahiaAppDef> list = template.find("from JahiaAppDef a order by a.name");
        return list;
    }
}
