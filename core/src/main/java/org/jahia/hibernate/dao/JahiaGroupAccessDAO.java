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

import org.jahia.hibernate.model.JahiaGrpAccess;
import org.jahia.hibernate.model.JahiaGrpAccessPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 17:50:33
 * To change this template use File | Settings | File Templates.
 */
public class JahiaGroupAccessDAO extends AbstractGeneratorDAO {
    public List<Object[]> findMemberIdsFromGroupName(String key) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Object[]> list = null;
        if (key != null) {
            list = template.find("select m.comp_id.memberKey,m.comp_id.memberType from JahiaGrpAccess m " +
                                 "where m.comp_id.groupKey=? order by m.comp_id.memberType", key);
        }
        return list;
    }

    public void delete(String groupKey) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaGrpAccess a where a.comp_id.groupKey=?", groupKey));
    }

    public void removeUserFromAllGroups(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaGrpAccess a where a.comp_id.memberKey=?",name));
    }

    public List<String> getUserMembership(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCacheQueries(true);
        return template.find("select a.comp_id.groupKey from JahiaGrpAccess a where a.comp_id.memberKey=?",name);
    }

    public void save(JahiaGrpAccess access) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(access);
    }

    public void delete(JahiaGrpAccess access) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(access);
    }

    public JahiaGrpAccess findById(JahiaGrpAccessPK pk) {
        return (JahiaGrpAccess) getHibernateTemplate().load(JahiaGrpAccess.class,pk);
    }
}
