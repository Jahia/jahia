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

import org.jahia.hibernate.model.JahiaRetentionRule;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:54:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaRetentionRuleDAO extends AbstractGeneratorDAO {

    public List getRetentionRules() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaRetentionRule");
    }

    public JahiaRetentionRule findByPK(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaRetentionRule) template.load(JahiaRetentionRule.class, id);
    }

    public void delete(Integer id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaRetentionRule h where h.id=?", id));
    }

    public void save(final JahiaRetentionRule retentionRule) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Transaction transaction = session.beginTransaction();
                try {
        if ( retentionRule.getId() == null || retentionRule.getId().intValue() == -1 ){
            retentionRule.setId(getNextInteger(retentionRule));            
        }
        template.merge(retentionRule);
                    transaction.commit();
                } catch(Exception t) {
                    transaction.rollback();
                }
                return null;
            }
        });
    }
}
