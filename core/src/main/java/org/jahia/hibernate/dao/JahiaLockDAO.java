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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.JahiaLock;
import org.jahia.hibernate.model.JahiaLockPK;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 17:28:28
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLockDAO extends AbstractGeneratorDAO {
    public JahiaLock findByPK(JahiaLockPK pk) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaLock) template.load(JahiaLock.class,pk);
    }

    public List<JahiaLock> findByLockKey(String name, Integer targetID, String action) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaLock l where l.comp_id.name=? and l.comp_id.targetID=?  and l.comp_id.action=?",
                new Object[]{name, targetID, action});
    }

    public List<Object[]> findKeysByLockAction(String action) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct l.comp_id.name, l.comp_id.targetID from JahiaLock l where l.comp_id.action=?",
                new Object[]{action});
    }

    public List<JahiaLock> findByLockNameAndId(String name, Integer targetID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaLock l where l.comp_id.name=? and l.comp_id.targetID=?",
                new Object[]{name, targetID});
    }

    public void purgeLockForContext(String contextId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        template.deleteAll(template.find("from JahiaLock l where l.comp_id.contextID=?", new Object[]{contextId}));
    }

    public void purgeLockForServer(String serverId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        template.deleteAll(template.find("from JahiaLock l where l.serverId=?", new Object[]{serverId}));
    }

    public void update(JahiaLock jahiaLock) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        hibernateTemplate.update(jahiaLock);
    }

    public void save(JahiaLock jahiaLock) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        hibernateTemplate.save(jahiaLock);
    }

    public void delete(JahiaLock jahiaLock) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        template.delete(jahiaLock);
    }

    public void deleteAllLocks() {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        template.deleteAll(template.find("from JahiaLock"));
    }

    public void merge(JahiaLock jahiaLock) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_EAGER);
        try {
            template.merge(jahiaLock);
        } catch (DataIntegrityViolationException ex) {
            // try a second time in a race condition
            template.merge(jahiaLock);
        } 
    }
}
