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
