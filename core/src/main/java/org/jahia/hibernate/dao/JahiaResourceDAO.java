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

import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.jahia.hibernate.model.JahiaResource;
import org.jahia.hibernate.model.JahiaResourcePK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 avr. 2005
 * Time: 16:41:18
 * To change this template use File | Settings | File Templates.
 */
public class JahiaResourceDAO extends AbstractGeneratorDAO {
// -------------------------- OTHER METHODS --------------------------

    public void deleteAllResourcesForName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaResource r where r.comp_id.name=?", name));
    }

    public JahiaResource findByPK(JahiaResourcePK jahiaResourcePK) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCacheQueries(true);
        return (JahiaResource) template.get(JahiaResource.class, jahiaResourcePK);
    }

    public List<JahiaResource> findByName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaResource r where r.comp_id.name=?", name);
    }

    public List<JahiaResource> searchByStartingNameInLanguage(final String name, final String language) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaResource r where r.value like ? and r.comp_id.languageCode = ?", new Object[]{name+"%",language});
    }

    public List<JahiaResource> searchResourcesByContainingStringInLanguage(final String name, final String language) {
        return getSession().createCriteria(JahiaResource.class)
                .add(Expression.ilike("value", name, MatchMode.START))
                .add(Expression.eq("comp_id.languageCode", language)).list();
    }

    public void save(JahiaResource jahiaResource) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.save(jahiaResource);
    }

    public void update(JahiaResource jahiaResource) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.update(jahiaResource);
    }

    public void delete(JahiaResource resource) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.delete(resource);
    }
}

