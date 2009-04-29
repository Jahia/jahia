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
