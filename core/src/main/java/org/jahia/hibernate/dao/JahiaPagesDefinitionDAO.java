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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastArrayList;
import org.jahia.content.PageDefinitionKey;
import org.jahia.hibernate.model.JahiaPagesDef;
import org.jahia.hibernate.model.JahiaPagesDefProp;
import org.jahia.hibernate.model.JahiaPagesDefPropPK;
import org.jahia.services.pages.JahiaPageDefinition;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 25 févr. 2005
 * Time: 14:15:42
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPagesDefinitionDAO extends AbstractGeneratorDAO {
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllPageTemplateIDs() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Object[]> list = template.find("select def.id from JahiaPagesDef def order by def.name");
        FastArrayList retList = new FastArrayList(list.size());
        for (Object[] objects : list) {
            retList.add(objects[0]);
        }
        retList.setFast(true);
        return retList;
    }

    public Integer getNbPageTemplates() {
        Long retInteger = null;
        String hql = "select count(def.id) from JahiaPagesDef def ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<Long> list = template.find(hql.toString());
        if (!list.isEmpty()) {
            retInteger = list.get(0);
        }
        return new Integer(retInteger.intValue());
    }

// -------------------------- OTHER METHODS --------------------------

    public void delete(Integer templateId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaPagesDefProp def where def.comp_id.id=?",templateId));
        template.deleteAll(template.find("from JahiaPagesDef def where def.id=?", templateId));
    }

    public JahiaPagesDef findByPK(Integer definitionID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaPagesDef) template.load(JahiaPagesDef.class, definitionID);
    }

    public List<Integer> getAllAclId(Integer siteId) {
        List<Integer> retList = null;
        String hql = "select def.value from JahiaPagesDefProp def where def.comp_id.site=? and def.comp_id.name=?";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            retList = template.find(hql.toString(), new Object[]{siteId, JahiaPageDefinition.ACLID_PROP});
        }
        return retList;
    }

    public Integer getNbPageTemplates(Integer siteId) {
        Long retInteger = null;
        String hql = "select count(def.id) from JahiaPagesDef def where def.site.id=? ";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            List<Long> list = template.find(hql.toString(), new Object[]{siteId});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            }
        }
        return new Integer(retInteger.intValue());
    }

    public Integer getPageTemplateIDMatchingSourcePath(Integer siteId, String path) {
        Integer retInteger = null;
        String hql = "select def.id from JahiaPagesDef def where def.site.id=? and def.sourcePath=?";
        if (siteId != null && path != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            List<Integer> list = template.find(hql.toString(), new Object[]{siteId, path});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            }
        }
        return retInteger;
    }

    public JahiaPagesDef loadPageTemplate(Integer templateId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaPagesDef) template.get(JahiaPagesDef.class, templateId);
    }

    public JahiaPagesDef loadPageTemplate(String templateName, Integer siteId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List<JahiaPagesDef> defs = template.find(
                "from JahiaPagesDef d where d.name=? and d.site.id=?",
                new Object[] { templateName, siteId });
        return (defs.size() > 0 ? defs.get(0) : null);
    }

    public void save(JahiaPagesDef def) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (def.getId() == null) {
            def.setId(getNextInteger(def));
        }
        template.merge(def);
        saveProperties(def, template);
    }

    public void update(JahiaPagesDef def) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.update(def);
        saveProperties(def, template);
    }

    private void saveProperties(JahiaPagesDef ctnDef, final HibernateTemplate template) {
        final Map<String, String> properties = ctnDef.getProperties();
        if (properties != null && !properties.isEmpty()) {
            template.deleteAll(template.find(
                    "from JahiaPagesDefProp p where p.comp_id.id=?", ctnDef
                            .getId()));
            template.flush();
            
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                template.save(new JahiaPagesDefProp(new JahiaPagesDefPropPK(
                        ctnDef.getId(), ctnDef.getSite().getId(),
                        entry.getKey()), entry.getValue()));
            }
            template.flush();
        }
    }

    public List<PageDefinitionKey> deleteAllTemplatesFromSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaPagesDefProp def where def.comp_id.site=?",siteID));
        List<JahiaPagesDef> entities = template.find("from JahiaPagesDef def where def.site.id=?", siteID);
        List<PageDefinitionKey> retList = new ArrayList<PageDefinitionKey>(entities.size());
        for (JahiaPagesDef def : entities) {
            retList.add(new PageDefinitionKey(def.getId().intValue()));
        }
        template.deleteAll(entities);
        return retList;
    }

    /**
     * Returns a list of page definition IDs for the given site, considering
     * visibility.
     * 
     * @param siteId
     *            current site ID
     * @param availableOnly
     *            do return only visible templates?
     * @return a list of page definition IDs for the given site, considering
     *         visibility
     */
    public List<Integer> getPageTemplateIDs(int siteId, boolean availableOnly) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return availableOnly ? template
                .find(
                        "select def.id from JahiaPagesDef def where def.site.id=? and def.visible=? order by def.name",
                        new Object[] { new Integer(siteId),
                                Boolean.valueOf(availableOnly)})
                : template
                        .find(
                                "select def.id from JahiaPagesDef def where def.site.id=? order by def.name",
                                new Integer(siteId));
    }
}