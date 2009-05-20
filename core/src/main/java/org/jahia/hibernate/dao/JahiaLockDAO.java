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
