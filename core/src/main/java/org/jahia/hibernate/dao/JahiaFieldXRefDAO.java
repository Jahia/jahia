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