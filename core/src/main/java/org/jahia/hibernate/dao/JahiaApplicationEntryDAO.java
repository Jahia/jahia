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

import org.jahia.hibernate.model.JahiaAppentry;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 16 mars 2005
 * Time: 15:04:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaApplicationEntryDAO extends AbstractGeneratorDAO {
    public JahiaAppentry getEntryPointInstanceByID(Integer entryId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaAppentry) template.get(JahiaAppentry.class,entryId);
    }

    public void save(JahiaAppentry jahiaAppentry) {
        if (jahiaAppentry.getId() == null) {
            jahiaAppentry.setId(getNextInteger(jahiaAppentry));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(jahiaAppentry);
    }

    public void delete(JahiaAppentry entryPointInstanceByID) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(entryPointInstanceByID);
    }
}
