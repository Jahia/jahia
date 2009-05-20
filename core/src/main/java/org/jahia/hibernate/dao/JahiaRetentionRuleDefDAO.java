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

import org.jahia.hibernate.model.JahiaRetentionRuleDef;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:54:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRetentionRuleDefDAO extends AbstractGeneratorDAO {

    public List getRetentionRuleDefs() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaRetentionRuleDef");
    }

    public JahiaRetentionRuleDef findByPK(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaRetentionRuleDef) template.load(JahiaRetentionRuleDef.class, id);
    }

    public JahiaRetentionRuleDef findName(String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List retList = template.find("from JahiaRetentionRuleDef r where r.name=?",name);
        if ( !retList.isEmpty() ){
            return (JahiaRetentionRuleDef)retList.get(0);
        }
        return null;
    }

    public void delete(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaRetentionRuleDef h where h.id=?", id));
    }

    public void save(JahiaRetentionRuleDef retentionRuleDef) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (retentionRuleDef.getId() == null) {
            retentionRuleDef.setId(getNextInteger(retentionRuleDef));
        }
        template.merge(retentionRuleDef);
    }
}
