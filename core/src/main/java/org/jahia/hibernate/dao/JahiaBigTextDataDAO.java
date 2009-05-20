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

import org.jahia.hibernate.model.JahiaBigTextData;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 juin 2005
 * Time: 18:16:40
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBigTextDataDAO extends AbstractGeneratorDAO {
    public JahiaBigTextData load(String fileName) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        try {
            return (JahiaBigTextData) hibernateTemplate.get(JahiaBigTextData.class, fileName);
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public boolean doesBigTextAlreadyExist(String filename) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCacheQueries(true);
        List o = hibernateTemplate.find("select b.id from JahiaBigTextData b where b.id=?", filename);
        if (o == null || o.size() < 1)
            return false;
        else
            return true;

    }

    public void update(JahiaBigTextData jahiaBigTextData) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.update(jahiaBigTextData);
    }

    public void save(JahiaBigTextData jahiaBigTextData) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(jahiaBigTextData);
    }

    public void delete(String oldFileName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaBigTextData b where b.id=?", oldFileName));
    }

    public void deleteAllFromSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaBigTextData b where b.id like ?", siteID+"%"));
    }
}
