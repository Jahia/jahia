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

import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.hibernate.model.JahiaFieldXRefPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:27:23
 */
public class JahiaFieldXRefDAO extends AbstractGeneratorDAO {
    public void save(JahiaFieldXRef fieldXRef) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(fieldXRef);
    }

    public JahiaFieldXRef getFieldReference(int fieldId, String language, Integer workflow, String target) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaFieldXRef) template.get(JahiaFieldXRef.class, new JahiaFieldXRefPK(fieldId, language, workflow, target));
    }

    public void delete(JahiaFieldXRef fieldXRef) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.delete(fieldXRef);
    }

    public void deleteForField(int fieldId, String language, Integer workflow) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.deleteAll(getFieldReferences(fieldId, language, workflow));
    }

    public void deleteForField(int fieldId, String language) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.deleteAll(getFieldReferences(fieldId, language));
    }

    public void deleteForField(int fieldId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.deleteAll(getFieldReferences(fieldId));
    }

    public List<JahiaFieldXRef> getFieldReferences(int fieldId, String language, Integer workflow) {
        String hql = "select x from JahiaFieldXRef x where x.comp_id.fieldId=? and x.comp_id.language=? and x.comp_id.workflow=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{fieldId, language, workflow});
    }

    public List<JahiaFieldXRef> getFieldReferences(int fieldId, String language) {
        String hql = "select x from JahiaFieldXRef x where x.comp_id.fieldId=? and x.comp_id.language=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{fieldId, language});
    }

    public List<JahiaFieldXRef> getFieldReferences(int fieldId) {
        String hql = "select x from JahiaFieldXRef x where x.comp_id.fieldId=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{fieldId});
    }

    public List<JahiaFieldXRef> getReferencesForTarget(String target) {
        String hql = "select x from JahiaFieldXRef x where x.comp_id.target=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{target});
    }

    public List<JahiaFieldXRef> getReferencesForTarget(String target, int workflow) {
        String hql = "select x from JahiaFieldXRef x where x.comp_id.target=? and x.comp_id.workflow=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(hql, new Object[]{target, workflow});
    }

    public void deleteFromSite(int siteId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.deleteAll(template.find("select x from JahiaFieldXRef x where x.siteId=?", new Object[]{siteId}));
    }


}