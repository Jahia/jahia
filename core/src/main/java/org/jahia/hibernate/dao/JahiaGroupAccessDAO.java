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
