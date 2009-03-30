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

 package org.jahia.hibernate.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.jahia.hibernate.model.JahiaAclName;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User: Serge Huber
 * Date: 14 d�c. 2005
 * Time: 14:22:41
 * Copyright (C) Jahia Inc.
 */
public class JahiaAclNamesDAO extends AbstractGeneratorDAO {

    private Log log = LogFactory.getLog(JahiaAclNamesDAO.class);

    public JahiaAclName findAclNameByName(String name) {
        String hql = "from JahiaAclName a where a.aclName = ?";
        JahiaAclName aclName = null;
        if (name != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            template.setCacheQueries(true);
            List<JahiaAclName> objects = template.find(hql, name);
            if (objects.size() == 1) {
                aclName = objects.get(0);
            } else {
                throw new ObjectRetrievalFailureException(JahiaAclName.class, name);
            }
        }
        return aclName;
    }

    public List<JahiaAclName> findAclNamesStartingWith(String startWithStr) {
        String hql = "from JahiaAclName a where a.aclName like ?";
        if (startWithStr != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            template.setCacheQueries(true);
            List<JahiaAclName> objects = template.find(hql, startWithStr + "%");
            return objects;
        }
        throw new ObjectRetrievalFailureException(JahiaAclName.class, startWithStr);
    }

    public void saveAclName(JahiaAclName aclName) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        try {
            hibernateTemplate.merge(aclName);
            hibernateTemplate.flush();
        } catch (HibernateException e) {
            log.warn("Exception during save of acl name " + aclName, e);
        }
    }

    public void updateAclName(JahiaAclName aclName) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        try {
            hibernateTemplate.merge(aclName);
            hibernateTemplate.flush();
        } catch (HibernateException e) {
            log.warn("Exception during save of acl name " + aclName, e);
        }
    }

    public void removeAclName(String aclName) {
        JahiaAclName jahiaAclName = findAclNameByName(aclName);
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(jahiaAclName);
        hibernateTemplate.flush();
    }

    public Map<Serializable, Integer> removeBySiteID(Integer siteID) {
        String hql = "from JahiaAclName a where a.aclName like ?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List<JahiaAclName> entities = template.find(hql, "%." + siteID + ".%");
        Map<Serializable, Integer> map = new HashMap<Serializable, Integer>(entities.size());
        for (JahiaAclName aclName : entities) {
            Integer id = aclName.getAcl().getId();
            map.put(id,id);
        }
        template.deleteAll(entities);
        return map;
    }
}
