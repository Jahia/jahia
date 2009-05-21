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
