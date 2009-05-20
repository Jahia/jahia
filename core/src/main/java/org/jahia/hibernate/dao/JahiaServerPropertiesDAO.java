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

import org.jahia.hibernate.model.jahiaserver.JahiaServerProp;
import org.jahia.hibernate.model.jahiaserver.JahiaServerPropPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 mars 2005
 * Time: 15:04:47
 * To change this template use File | Settings | File Templates.
 */
public class JahiaServerPropertiesDAO extends AbstractGeneratorDAO {
// -------------------------- OTHER METHODS --------------------------

    public void delete(String serverId) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaServerProp c where c.comp_id.serverId=?",
                                      serverId));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public void delete(String serverId, String propname) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaServerProp c where c.comp_id.serverId=? and c.comp_id.propName=?",
                new Object[]{serverId, propname}));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public JahiaServerProp getJahiaServerProp(String serverId, String propName) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaServerProp prop = null;
        if (serverId != null && !"".equals(serverId.trim())
                && propName != null && !"".equals(propName.trim())) {
            prop = (JahiaServerProp)template.get(
                    JahiaServerProp.class,new JahiaServerPropPK(serverId,propName));
        }
        return prop;
    }

    public List getServerProperties(String serverId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List list = new ArrayList(1);
        if (serverId != null && !"".equals(serverId.trim())) {
            list = template.find("from JahiaServerProp c where c.comp_id.serverId=?",
                                 serverId);
        }
        return list;
    }

    public void save(JahiaServerProp jahiaServerProp) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(jahiaServerProp);
    }
}

