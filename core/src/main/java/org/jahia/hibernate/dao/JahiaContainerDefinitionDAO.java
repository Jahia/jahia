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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jahia.content.ContainerDefinitionKey;
import org.jahia.hibernate.model.JahiaCtnDef;
import org.jahia.hibernate.model.JahiaCtnDefProperty;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 16:56:09
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerDefinitionDAO extends AbstractGeneratorDAO {
// -------------------------- OTHER METHODS --------------------------

    public void delete(Integer ctnDefId) {
        final JahiaCtnDef entity = fullyLoadContainerDefinition(ctnDefId);
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        // hibernateTemplate.deleteAll(hibernateTemplate.find("from JahiaCtndefProp p where p.comp_id.idJahiaCtnDef=?", entity.getId()));
        hibernateTemplate.delete(entity);
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public List<Integer> findAllContainerDefinitionId() {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("select distinct def.id from JahiaCtnDef def");
    }

    public JahiaCtnDef findDefinitionById(Integer id) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaCtnDef) template.load(JahiaCtnDef.class, id);
    }

    public JahiaCtnDef fullyLoadContainerDefinition(Integer definitionId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaCtnDef ctnDef = null;
        ctnDef = (JahiaCtnDef) template.load(JahiaCtnDef.class, definitionId);
        fullyloadSubComponent(ctnDef, template);
        return ctnDef;
    }

    public JahiaCtnDef fullyLoadContainerDefinition(Integer siteId, String definitionName) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        JahiaCtnDef ctnDef = null;
        String hql = "from JahiaCtnDef def where def.jahiaSiteId=? and def.name=?";
        if (siteId != null && definitionName != null) {
            List<JahiaCtnDef> list = template.find(hql.toString(), new Object[]{siteId, definitionName});
            if (list.size() > 0) {
                ctnDef = list.get(0);
                fullyloadSubComponent(ctnDef, template);
            }
        }
        return ctnDef;
    }

    public JahiaCtnDef save(JahiaCtnDef ctnDef) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.flush();
        try {
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            if (ctnDef.getId() == null) {
                ctnDef.setId(getNextInteger(ctnDef));
            }
            for (JahiaCtnDefProperty property : ctnDef.getSubDefinitions()) {
                if (property.getIdJahiaCtnDefProperties() == null) {
                    property.setIdJahiaCtnDefProperties(getNextInteger(property));
                    hibernateTemplate.save(property);
                }
            }
            hibernateTemplate.save(ctnDef);
//            saveSubDefinition(ctnDef, hibernateTemplate);
//            saveProperties(ctnDef, hibernateTemplate);
            hibernateTemplate.flush();
            hibernateTemplate.clear();
        } catch (RuntimeException e) {
            hibernateTemplate.clear();
            // Maybe the definition has already been added by another node ? lets try to find it
            JahiaCtnDef ctnDef2 = fullyLoadContainerDefinition(ctnDef.getJahiaSiteId(), ctnDef.getName());
            if (ctnDef2 != null) return ctnDef2;
            else throw e;
        }
        return ctnDef;
    }

    public void update(JahiaCtnDef ctnDef) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        for (JahiaCtnDefProperty property : ctnDef.getSubDefinitions()) {
            if (property.getIdJahiaCtnDefProperties() == null) {
                property.setIdJahiaCtnDefProperties(getNextInteger(property));
                template.save(property);
            }
        }
//        saveSubDefinition(ctnDef, template);
        template.merge(ctnDef);
        template.flush();
        template.clear();
    }

    private void fullyloadSubComponent(JahiaCtnDef ctnDef, HibernateTemplate template) {
        try {
        ctnDef.getSubDefinitions().isEmpty();
        ctnDef.getProperties().isEmpty();
        } catch(Exception e){
        }
    }

    public List<JahiaCtnDef> fullyLoadContainerDefinitionInTemplate(Integer templateId) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
//        String hql = "from JahiaCtnDef def where def.subDefinitions.pageDefinitionId=? order by def.id";
        String hql = "select jahiaCtnDef from JahiaCtnDefProperty def where def.pageDefinitionId=? order by def.id";
        List<JahiaCtnDef> retList = null;
        if (templateId != null) {
            retList = template.find(hql.toString(), new Object[]{templateId});
            for (int i = 0; i < retList.size(); i++) {
                JahiaCtnDef jahiaCtnDef = (JahiaCtnDef) retList.get(i);
                fullyloadSubComponent(jahiaCtnDef, template);
            }
        }
        return retList;
    }

    public List<JahiaCtnDef> fullyLoadContainerDefinitionInTemplateForTestingPurpose(Integer templateId) {
        final Session template = getSession(true);
        template.setCacheMode(CacheMode.GET);
        String hql = "from JahiaCtnDef def where def.subDefinitions.pageDefinitionId=:id  order by def.id";
        List<JahiaCtnDef> retList = null;
        if (templateId != null) {
            Query query = template.createQuery(hql.toString());
            query.setInteger("id", templateId.intValue());
            retList = query.list();
            for (int i = 0; i < retList.size(); i++) {
                JahiaCtnDef jahiaCtnDef = (JahiaCtnDef) retList.get(i);
                jahiaCtnDef.getSubDefinitions().isEmpty();
                jahiaCtnDef.getProperties().isEmpty();
            }
        }
        template.close();
        return retList;
    }

    public JahiaCtnDef fullyLoadContainerDefinitionForTestingPurpose(Integer templateId) {
        final Session template = getSession(true);
        template.setCacheMode(CacheMode.GET);
        JahiaCtnDef retCtnDef = null;
        if (templateId != null) {
            retCtnDef = (JahiaCtnDef) template.get(JahiaCtnDef.class, templateId);
            retCtnDef.getSubDefinitions().isEmpty();
            retCtnDef.getProperties().isEmpty();
        }
        template.close();
        return retCtnDef;
    }

    public List<ContainerDefinitionKey> deleteAllDefinitionsFromSite(Integer siteID) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        String hql = "select def.id from JahiaCtnDef def where def.jahiaSiteId=?";
        List<Integer> list = template.find(hql.toString(), siteID);
        List<ContainerDefinitionKey> retList = new ArrayList<ContainerDefinitionKey>(list.size());
        for (Integer o : list) {
            delete(o);
            retList.add(new ContainerDefinitionKey(o.intValue()));
        }
        return retList;
    }

    public List<Integer> getContainerDefinitionsWithType(Set<String> types) {
        if ( types == null){
            return new ArrayList<Integer>();
        }
        final HibernateTemplate template = getHibernateTemplate();
        List<Integer> result = template.findByNamedParam("select def.id FROM JahiaCtnDef def WHERE def.containerType in (:types)", "types", types);

        return result;
    }
}

