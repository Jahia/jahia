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

import org.jahia.content.FieldDefinitionKey;
import org.jahia.hibernate.model.JahiaFieldsDef;
import org.jahia.hibernate.model.JahiaFieldsDefExtprop;
import org.jahia.hibernate.model.JahiaFieldsDefExtpropPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 8 févr. 2005
 * Time: 11:02:37
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldsDefinitionDAO extends AbstractGeneratorDAO {
    public JahiaFieldsDef loadDefinition(Integer id) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        final JahiaFieldsDef jahiaFieldsDef = (JahiaFieldsDef) template.load(JahiaFieldsDef.class, id);
//        jahiaFieldsDef.getSubDefinitions().isEmpty();
        jahiaFieldsDef.getProperties().isEmpty();
        return jahiaFieldsDef;
    }

    public JahiaFieldsDef loadDefinition(Integer siteId, String definitionName) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaFieldsDef jahiaFieldsDef = null;
        String hql = "from JahiaFieldsDef def where def.jahiaSiteId=? and def.name=?";
        if (definitionName != null) {
            List<JahiaFieldsDef> list = template.find(hql.toString(), new Object[]{siteId, definitionName});
            if (list.size() > 0) {
                jahiaFieldsDef = list.get(0);
//                jahiaFieldsDef.getSubDefinitions().isEmpty();
                jahiaFieldsDef.getProperties().isEmpty();
            }
        }
        return jahiaFieldsDef;
    }

    public JahiaFieldsDef loadDefinition(String definitionName) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaFieldsDef jahiaFieldsDef = null;
        String hql = "from JahiaFieldsDef def where def.jahiaSiteId is null and def.name=?";
        if (definitionName != null) {
            List<JahiaFieldsDef> list = template.find(hql.toString(), new Object[]{definitionName});
            if (list.size() > 0) {
                jahiaFieldsDef = list.get(0);
//                jahiaFieldsDef.getSubDefinitions().isEmpty();
                jahiaFieldsDef.getProperties().isEmpty();
            }
        }
        return jahiaFieldsDef;
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionName
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getDefinitionIds(String definitionName, boolean isMetadata) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer hql = new StringBuffer("select distinct def.id from JahiaFieldsDef def where def.name=?");
        if (isMetadata){
             hql.append(" AND def.isMetadata = 1");
        } else {
            hql.append(" AND def.isMetadata <> 1");
        }
        return  template.find(hql.toString(), new Object[]{definitionName});
    }

    /**
     * Returns all field def id of the given ctnName ( JahiaField Ctn Name, not Container Def name).
     *
     * @param ctnType
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String ctnType, boolean isMetadata) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer hql = new StringBuffer("select distinct def.id from JahiaFieldsDef def where def.ctnName = ?");
        if (isMetadata){
             hql.append(" AND def.isMetadata = 1");
        } else {
            hql.append(" AND def.isMetadata <> 1");
        }
        return template.find(hql.toString(), ctnType);
    }

    /**
     * Returns all field def id of the given ctnName ( JahiaField Ctn Name, not Container Def name).
     *
     * @param ctnTypes
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getFieldDefinitionNameFromCtnType(String[] ctnTypes, boolean isMetadata) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer hql = new StringBuffer("select distinct def.id from JahiaFieldsDef def where def.ctnName IN (:ctnTypes)");
        if (isMetadata){
             hql.append(" AND def.isMetadata = 1");
        } else {
            hql.append(" AND def.isMetadata <> 1");
        }
        return template.findByNamedParam(hql.toString(), "ctnTypes", ctnTypes);
    }

    /**
     * Load all def Ids of the given name.
     *
     * @param definitionNames
     * @param isMetadata if true, return only metadata field definition
     * @return
     */
    public List<Integer> getDefinitionIds(String[] definitionNames, boolean isMetadata) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer hql = new StringBuffer("select distinct def.id from JahiaFieldsDef def where ( ");
        for ( int i=0; i< definitionNames.length; i++ ){
            hql.append(" def.name=? ");
            if (i<definitionNames.length-1){
                hql.append(" OR ");
            }
        }
        hql.append(" ) ");
        if (isMetadata){
             hql.append(" AND def.isMetadata = 1");
        } else {
            hql.append(" AND def.isMetadata <> 1");
        }
        return  template.find(hql.toString(), definitionNames);
    }

    private void saveProperties(JahiaFieldsDef ctnDef, final HibernateTemplate template) {
        Map<Object, Object> properties = ctnDef.getProperties();
        int size = properties.size();
        if (size > 0) {
            template.flush();
            template.clear();
            List<JahiaFieldsDefExtprop> list = template.find("from JahiaFieldsDefExtprop where comp_id.jahiaFieldsDef=?", ctnDef.getId());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                JahiaFieldsDefExtprop fieldsDefExtprop = new JahiaFieldsDefExtprop(new JahiaFieldsDefExtpropPK(ctnDef.getId(),
                        (String)entry.getKey()),
                        (String)entry.getValue());
                template.merge(fieldsDefExtprop);
                list.remove(fieldsDefExtprop);
            }
            template.deleteAll(list);
            template.flush();
        }
    }

    public void save(JahiaFieldsDef fieldsDef) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (fieldsDef.getId() == null) {
            fieldsDef.setId(getNextInteger(fieldsDef));
        }

        try {
            hibernateTemplate.save(fieldsDef);
            hibernateTemplate.flush();
        } catch (Exception e) {
            hibernateTemplate.merge(fieldsDef);
        }
        saveProperties(fieldsDef, hibernateTemplate);
        hibernateTemplate.flush();
    }

    public void delete(Integer ctnDefId) {
        final JahiaFieldsDef entity = loadDefinition(ctnDefId);
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaFieldsDefExtprop where comp_id.jahiaFieldsDef=?", ctnDefId));
        hibernateTemplate.delete(entity);
    }

    public List<Integer> findAllFieldDefinitions(int siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct f.id from JahiaFieldsDef f "
                + (siteId > 0 ? "where jahiaid_jahia_fields_def is null or jahiaid_jahia_fields_def = " + siteId : "")
                + " order by f.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        retVal = template.find(hql.toString());
        return retVal;
    }

    public List<FieldDefinitionKey> deleteAllDefinitionsFromSite(Integer siteID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        // First delete prop
        String hql = "select def.id from JahiaFieldsDef def where def.jahiaSiteId=?";
        List<Integer> list = template.find(hql, siteID);
        List<FieldDefinitionKey> map = new ArrayList<FieldDefinitionKey>(list.size());
        for (Integer fieldsDefId : list) {
            try {
                template.deleteAll(template.find("from JahiaFieldsDefExtprop where comp_id.jahiaFieldsDef=?", fieldsDefId));
                template.flush();
                template.deleteAll(template.find("from JahiaFieldsDef def where def.id=?",fieldsDefId));
                template.flush();
            } catch (Exception e) {
               logger.error("Cannot delete definition",e);
            }
            map.add(new FieldDefinitionKey(fieldsDefId.intValue()));
        }
        return map;
    }
}
