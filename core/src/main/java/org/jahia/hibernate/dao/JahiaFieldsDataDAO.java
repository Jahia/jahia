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

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.FieldTypes;
import org.jahia.hibernate.model.*;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.version.EntryLoadRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 17:11:57
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldsDataDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(getClass());

// -------------------------- OTHER METHODS --------------------------

    public void backupFieldsData(Integer fieldId) {
        try {
            JahiaFieldsData container = loadPublishedField(fieldId);
            if (container != null) {
                JahiaFieldsData jahiaContainer = (JahiaFieldsData) container.clone();
                jahiaContainer.getComp_id().setWorkflowState((0));
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
                hibernateTemplate.save(jahiaContainer);
            }
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaFieldsData");
        }
    }

    public void backupFieldsData(Integer fieldId, Integer versionId, Integer workflowState, String languageCode) {
        JahiaFieldsData data = findJahiaFieldsDataByPK(
                new JahiaFieldsDataPK(fieldId, versionId, workflowState, languageCode));
        if (data != null) {
            try {
                JahiaFieldsData fieldsData = (JahiaFieldsData) data.clone();
                fieldsData.getComp_id().setWorkflowState((0));
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
                hibernateTemplate.save(fieldsData);
                hibernateTemplate.flush();
            } catch (CloneNotSupportedException e) {
                log.error("Clone of fields data not supported", e);
            }
        }
    }

    public JahiaFieldsData createStagedField(JahiaFieldsData container, Integer version, Integer workflowState) {
        try {
            JahiaFieldsData jahiaContainer = (JahiaFieldsData) container.clone();
            jahiaContainer.getComp_id().setVersionId(version);
            jahiaContainer.getComp_id().setWorkflowState(workflowState);
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.save(jahiaContainer);
            return jahiaContainer;
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaFieldsData");
            return null;
        }
    }

    public void deleteJahiaField(JahiaFieldsData fieldsData) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(fieldsData);
    }

    public void deleteJahiaFields(List<JahiaFieldsData> stagedContainers) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(stagedContainers);
    }

    public void deleteValue(Integer id, Integer workflowState, String language) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState=? and f.comp_id.languageCode=?",
                new Object[]{id, workflowState, language}));
    }

    public JahiaFieldsData fillProperties(final JahiaFieldsData jahiaContainer) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Object[]> props = template.find("select p.comp_id.name,p.value from JahiaFieldsProp p where p.comp_id.fieldId=?", jahiaContainer.getComp_id().getId());
        FastHashMap properties = new FastHashMap(props.size());
        for (Object[] objects : props) {
            properties.put(objects[0], objects[1]);
        }
        properties.setFast(true);
        jahiaContainer.setProperties(properties);
        return jahiaContainer;
    }

    public List<Integer> findAllAclsIdInSite(Integer siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct f.jahiaAclId from JahiaFieldsData f where f.siteId=? order by f.jahiaAclId";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{siteId});
        }
        return retVal;
    }

    public List<Integer> findAllFieldsId() {
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f order by f.comp_id.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql);
    }

    public boolean hasActiveFieldsWithoutContainer() {
        String hql = "select count(*) from JahiaFieldsData f where f.metadataOwnerId is null and f.containerId=0 and f.comp_id.workflowState>=1 and f.comp_id.versionId != -1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return ((Long) template.find(hql).iterator().next()) > 0;
    }

    public List<Integer> findAllFieldsIdInContainer(Integer containerID) {
        return findAllFieldsIdInContainer(containerID, false);
    }

    public List findAllFieldsIdInContainer(Integer containerID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.containerId=?";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.containerId=?";
        }
        if (containerID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{containerID});
            retVal = fillFieldsDataIds(list, withTypes);
        }
        return retVal;
    }

    private List fillFieldsDataIds(List list, boolean withTypes) {
        FastArrayList retVal = new FastArrayList(list.size());
        if (!withTypes) {
            for (Object object : list) {
                if (object instanceof Object[]) {
                    object = ((Object[]) object)[0];
                }
                retVal.add(object);
            }
        } else {
            for (Object aList : list) {
                Object[] objects = (Object[]) aList;
                retVal.add(new Object[]{objects[0], objects[1]});
            }
        }
        retVal.setFast(true);
        return retVal;
    }

    public List<Integer> findAllFieldsIdInSite(Integer siteId) {
        List<Integer> retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.siteId=? order by f.comp_id.id";
        if (siteId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{siteId});
        }
        return retVal;
    }

    public List<Object[]> findAllValues(Integer fieldId) {
        String hql = "select f.value, f.comp_id.versionId,f.comp_id.workflowState,f.comp_id.languageCode from JahiaFieldsData f where f.comp_id.id=?";
        List<Object[]> retVal = null;
        if (fieldId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{fieldId});
        }
        return retVal;
    }

    /**
     * Returns all fields id's which values correspond to the requested value and of the given field type
     *
     * @param value
     * @param type
     * @return List
     */
    public List<Integer> findFieldsByValuesAndType(String value, Integer type) {
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.type=? AND f.value=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{type, value});
    }

    public List<Object[]> findMetadataObjectKeyByFieldValuesAndType(String value, Integer type) {        
        String hql = "select distinct f.metadataOwnerId, f.metadataOwnerType from JahiaFieldsData f where f.type=? AND f.value like ? AND f.metadataOwnerId > 0";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{type,value});
    }

    public List<Integer> findFieldsByDefinitionName(String definitionName) {
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.fieldDefinition.name=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{definitionName});
    }

    public Integer findFieldsIdInPageByDefinitionNameAndSite(Integer siteId, String definitionName, Integer pageId) {
        Integer retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.fieldDefinition.jahiaSiteId=? and f.fieldDefinition.name=?  and f.pageId=? order by f.comp_id.id";
        if (siteId != null && definitionName != null && pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Integer> list = template.find(hql,
                    new Object[]{siteId, definitionName, pageId});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal;
    }

    public Integer findMetadataByOwnerAndName(String name, JahiaObjectPK ownerKey) {
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.fieldDefinition.name=? AND f.metadataOwnerId=? AND f.metadataOwnerType=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Integer> list = template.find(hql,
                new Object[]{name, ownerKey.getId(), ownerKey.getType()});
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<Integer> findMetadatasByOwner(JahiaObjectPK ownerKey) {
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.metadataOwnerId=? AND f.metadataOwnerType=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{ownerKey.getId(), ownerKey.getType()});
    }

    public List<Integer> findFieldsIdInPageByType(Integer pageId, Integer type) {
        List<Integer> retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.type=? and f.pageId=? order by f.comp_id.id";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{type, pageId});
        }
        return retVal;
    }

    public List<Integer> findFieldsIdInPageWithoutContainer(Integer pageID) {
        return findFieldsIdInPageWithoutContainer(pageID, false);
    }

    public List findFieldsIdInPageWithoutContainer(Integer pageID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.containerId=0 order by f.comp_id.id";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.pageId=? and f.containerId=0 order by f.comp_id.id";
        }
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public JahiaFieldsData findJahiaFieldsDataByPK(JahiaFieldsDataPK jahiaFieldsDataPK) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return (JahiaFieldsData) template.get(JahiaFieldsData.class, jahiaFieldsDataPK);
    }

    public List<Object[]> findNonDeletedFieldsIdInContainer(Integer containerID) {
        List<Object[]> retVal = null;
        String hql = "select distinct f.comp_id.id,f.comp_id.workflowState from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState>=1 and f.comp_id.versionId <> -1 order by f.comp_id.workflowState desc";
        if (containerID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{containerID});
            retVal = fillFieldsDataIds(list, false);
        }
        return retVal;
    }

    public List<Object[]> findNonDeletedFieldsIdInPage(Integer pageId) {
        List<Object[]> retVal = null;
        String hql = "select distinct f.comp_id.id,f.comp_id.workflowState from JahiaFieldsData f where f.pageId=? and f.comp_id.versionId <> -1 order by f.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql, new Object[]{pageId});
            retVal = fillFieldsDataIds(list, false);
        }
        return retVal;
    }

    public List<Object[]> findOldEntryState(Integer fieldId) {
        String hql = "select f.comp_id.versionId,f.comp_id.workflowState,f.comp_id.languageCode from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState<=0";
        List<Object[]> retVal = null;
        if (fieldId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{fieldId});
        }
        return retVal;
    }

    public List<Object[]> findOldEntryStateForMetadatas(JahiaObjectPK ownerKey) {
        String hql = "select f.comp_id.id,f.comp_id.versionId,f.comp_id.workflowState,f.comp_id.languageCode from JahiaFieldsData f where f.metadataOwnerId=? AND f.metadataOwnerType=? AND f.comp_id.workflowState<=0";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{ownerKey.getId(), ownerKey.getType()});
    }

    public List<Integer> findPublishedFieldsIdInContainer(Integer containerID) {
        return findPublishedFieldsIdInContainer(containerID, false);
    }

    public List findPublishedFieldsIdInContainer(Integer containerID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState=1";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState=1";

        }
        if (containerID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{containerID});
            retVal = fillFieldsDataIds(list, withTypes);
        }
        return retVal;
    }

    public List<Integer> findPublishedFieldsIdInPageWithoutContainer(Integer pageID) {
        return findPublishedFieldsIdInPageWithoutContainer(pageID, false);
    }

    public List findPublishedFieldsIdInPageWithoutContainer(Integer pageID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState=1 order by f.comp_id.id";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState=1 order by f.comp_id.id";
        }
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public List<Integer> findStagedFieldsIdInPage(Integer pageID) {
        List<Integer> retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.comp_id.workflowState>1 order by f.comp_id.id";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public List<Integer> findStagedFieldsIdInPageWithoutContainer(Integer pageID) {
        List<Integer> retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState>1 order by f.comp_id.id";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public List<Integer> findStagingFieldsIdInContainer(Integer containerID) {
        return findStagingFieldsIdInContainer(containerID, false);
    }

    public List findStagingFieldsIdInContainer(Integer containerID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id,f.comp_id.workflowState from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState>=1 order by f.comp_id.workflowState desc";
        } else {
            hql = "select distinct f.comp_id.id,f.type,f.comp_id.workflowState from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState>=1 order by f.comp_id.workflowState desc";
        }
        if (containerID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{containerID});
            retVal = fillFieldsDataIds(list, withTypes);
        }
        return retVal;
    }

    public List<Integer> findStagingFieldsIdInPage(Integer pageID) {
        List<Integer> retVal = null;
        String hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.comp_id.workflowState>=1 order by f.comp_id.id";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public List<Integer> findStagingFieldsIdInPageWithoutContainer(Integer pageID) {
        return findStagingFieldsIdInPageWithoutContainer(pageID, false);
    }

    public List findStagingFieldsIdInPageWithoutContainer(Integer pageID, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState >= 1 order by f.comp_id.id";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState >= 1 order by f.comp_id.id";
        }
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID});
        }
        return retVal;
    }

    public List<Integer> findVersionedFieldsIdInContainer(Integer containerID, Integer version) {
        return findVersionedFieldsIdInContainer(containerID, version, false);
    }

    public List findVersionedFieldsIdInContainer(Integer containerID, Integer version, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState<=1 and f.comp_id.versionId between 0 and ?";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState<=1 and f.comp_id.versionId between 0 and ?";
        }
        if (containerID != null && version != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{containerID, version});
            retVal = fillFieldsDataIds(list, withTypes);
        }
        return retVal;
    }

    public List<Integer> findVersionedFieldsIdInPageWithoutContainer(Integer pageID, Integer versionId) {
        return findVersionedFieldsIdInPageWithoutContainer(pageID, versionId, false);
    }

    public List findVersionedFieldsIdInPageWithoutContainer(Integer pageID, Integer versionId, boolean withTypes) {
        List retVal = null;
        String hql;
        if (!withTypes) {
            hql = "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState<=1 and f.comp_id.versionId<=?";
        } else {
            hql = "select distinct f.comp_id.id,f.type from JahiaFieldsData f where f.pageId=? and f.containerId=0 and f.comp_id.workflowState<=1 and f.comp_id.versionId<=?";
        }
        if (pageID != null && versionId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql,
                    new Object[]{pageID, versionId});
        }
        return retVal;
    }

    public Integer getActivePageFieldID(Integer pageID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState=")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Integer> list = template.find(hql.toString(), new Object[]{pageID.toString()});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public Map<Integer, List<Integer>> getAllFieldsIdsFromContainerForPage(Integer pageId) {
        Map<Integer, List<Integer>> retval = null;
        String hql = "select distinct f.containerId, f.comp_id.id, from JahiaFieldsData f where f.pageId=? order by f.containerId, f.comp_id.id";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> objects = template.find(hql, new Object[]{pageId});
            retval = new FastHashMap(objects.size());
            for (Object[] objects1 : objects) {
                final Integer parent = (Integer) objects1[0];
                List<Integer> list = retval.get(parent);
                if (list == null) {
                    list = new ArrayList<Integer>(10);
                    retval.put(parent, list);
                }
                list.add((Integer)objects1[1]);
            }
            ((FastHashMap)retval).setFast(true);
        }
        return retval;
    }

    public Integer getAnyPageFieldID(Integer pageID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id,f.comp_id.versionId from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState > 0 order by f.comp_id.versionId desc");
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql.toString(), new Object[]{pageID.toString()});
            if (!list.isEmpty()) {
                boolean first = true;
                for (Object[] objects : list) {
                    if (first || (Integer) objects[1] == 0) {
                        retInteger = (Integer) objects[0];
                    } else if ((Integer) objects[1] < 0) {
                        retInteger = -1;
                    }
                    first = false;
                }
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public Integer getPageFieldID(Integer pageID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id,f.comp_id.workflowState from JahiaFieldsData f ")
                .append("where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState>=")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                .append(" and f.comp_id.versionId<>-1 ")
                .append("order by f.comp_id.workflowState desc");
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Object[]> list = template.find(hql.toString(), new Object[]{pageID.toString()});
            if (!list.isEmpty()) {
                retInteger = (Integer) list.get(0)[0];
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public Integer getStagedPageFieldID(Integer pageID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f ")
                .append("where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState>")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                .append(" and f.comp_id.versionId=0");
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Integer> list = template.find(hql.toString(), new Object[]{pageID.toString()});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public Integer getDeletedPageFieldID(Integer pageID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f ")
                .append("where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and ((f.comp_id.workflowState>")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                .append(" and f.comp_id.versionId=-1) or f.comp_id.workflowState=-1)");
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Integer> list = template.find(hql.toString(), new Object[]{pageID.toString()});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public List<Integer> getStagingAndActivePageFieldIDs(Integer pageID) {
        List<Integer> retVal = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState>=")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql.toString(), new Object[]{pageID.toString()});
        }
        return retVal;
    }

    public List<Integer> getStagingPageFieldIDsInPage(Integer pageID) {
        List<Integer> retVal = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.pageId=? and f.comp_id.workflowState>")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql.toString(), new Object[]{pageID});
        }
        return retVal;
    }

    public Integer getVersionedOrActivePageFieldID(Integer pageID, Integer parentPageID, Integer versionID) {
        Integer retInteger = null;
        StringBuffer hql = new StringBuffer("select distinct f.comp_id.id from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState<=")
                .append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
                .append(" and f.comp_id.versionId<=? and f.pageId=?");
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<Integer> list = template.find(hql.toString(), new Object[]{pageID.toString(), versionID, parentPageID});
            if (!list.isEmpty()) {
                retInteger = list.get(0);
            } else {
                throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
            }
        }
        return retInteger;
    }

    public Integer getVersionedPageFieldID(Integer pageID, Integer parentPageID, Integer versionID,
                                           boolean withActive) {

        int result = -1;

        StringBuffer hql = new StringBuffer("select f.comp_id from JahiaFieldsData f where f.type=")
                .append(FieldTypes.PAGE)
                .append(" and f.value=? and f.comp_id.workflowState<=");
        if (withActive) {
            hql.append(EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
        } else {
            hql.append(EntryLoadRequest.VERSIONED_WORKFLOW_STATE);
        }
        hql.append(" AND f.comp_id.versionId<=? and f.pageId=?");
        hql.append(" ORDER BY f.comp_id.versionId DESC, f.comp_id.workflowState DESC");

        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<JahiaFieldsDataPK> list = template.find(hql.toString(), new Object[]{pageID.toString(), versionID, parentPageID});

        long previousVersionId = 0;
        if (!list.isEmpty()) {
            for (JahiaFieldsDataPK compId : list) {
                if (result == -1) {
                    result = compId.getId();
                } else {
                    if (compId.getVersionId() < previousVersionId) {
                        break;
                    }
                }
                previousVersionId = compId.getVersionId();
            }
        }
        if (result != -1) {
            return (result);
        } else {
            throw new ObjectRetrievalFailureException("Cannot retrieve jahia fields for this page", pageID);
        }
    }

    public List<JahiaFieldsData> loadAllActiveOrStagedFieldEntry(Integer fieldId) {
        List<JahiaFieldsData> retVal = null;
        String hql = "from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState>=1";
        if (fieldId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            return template.find(hql, new Object[]{fieldId});
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllActiveOrStagedFields(List<Integer> fieldIds) {
        List<JahiaFieldsData> retVal = null;
        String hql = "from JahiaFieldsData f where f.comp_id.id in (:fieldIds) and f.comp_id.workflowState>=1 order by f.comp_id.id";
        if (fieldIds != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.findByNamedParam(hql, "fieldIds", fieldIds);
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllInactiveVersionedFields(Integer fieldId) {
        List<JahiaFieldsData> retVal = null;
        if (fieldId != null) {
            String hql = "from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState<=0 ";
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{fieldId});
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllInactiveVersionedFields(List<Integer> fieldIds) {
        List<JahiaFieldsData> retVal = null;
        if (fieldIds != null) {
            String hql = "from JahiaFieldsData f where f.comp_id.id in (:fieldIds) and f.comp_id.workflowState<=0 ";
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.findByNamedParam(hql, "fieldIds", fieldIds);
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllMetadataActiveOrStagedFieldEntryByOwnerAndName(String name, JahiaObjectPK owner) {
        String hql = "from JahiaFieldsData f where f.fieldDefinition.name=? AND f.metadataOwnerId=? AND f.metadataOwnerType=? and f.comp_id.workflowState>=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{name, owner.getId(), owner.getType()});
    }

    public List<JahiaFieldsData> loadAllMetadataActiveOrStagedFieldEntryByOwnerAndName(List<String> names, JahiaObjectPK owner) {
        String hql = "from JahiaFieldsData f where f.fieldDefinition.name in (:fieldNames) AND f.metadataOwnerId=(:metadataOwnerId) AND f.metadataOwnerType=(:metadataOwnerType) and f.comp_id.workflowState>=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.findByNamedParam(hql, new String[]{"fieldNames",
                "metadataOwnerId", "metadataOwnerType"}, new Object[]{names,
                owner.getId(), owner.getType()});
    }

    public List<JahiaFieldsData> loadAllMetadataInactiveVersionedFieldsByOwnerAndName(String name, JahiaObjectPK owner) {
        String hql = "from JahiaFieldsData f where f.fieldDefinition.name=? AND f.metadataOwnerId=? AND f.metadataOwnerType=? and f.comp_id.workflowState<=0";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{name, owner.getId(), owner.getType()});
    }

    public List<JahiaFieldsData> loadAllMetadataInactiveVersionedFieldsByOwnerAndName(List<String> names, JahiaObjectPK owner) {
        String hql = "from JahiaFieldsData f where f.fieldDefinition.name in (:fieldNames) AND f.metadataOwnerId=(:metadataOwnerId) AND f.metadataOwnerType=(:metadataOwnerType) and f.comp_id.workflowState<=0";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.findByNamedParam(hql, new String[]{"fieldNames",
                "metadataOwnerId", "metadataOwnerType"}, new Object[]{names,
                owner.getId(), owner.getType()});
    }

    public List<JahiaFieldsData> loadAllActiveOrStagedFieldInContainer(Integer containerId) {
        List<JahiaFieldsData> retVal = null;
        String hql = "from JahiaFieldsData f where f.containerId=? and f.comp_id.workflowState>=1 order by f.comp_id.id";
        if (containerId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{containerId});
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllActiveOrStagedFieldInPage(Integer pageId) {
        List<JahiaFieldsData> retVal = null;
        String hql = "from JahiaFieldsData f where f.pageId=? and f.comp_id.workflowState>=1 order by f.comp_id.id";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retVal = template.find(hql, new Object[]{pageId});
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadAllActiveOrStagedFieldsByMetadataOwner(JahiaObjectPK ownerKey) {
        String hql = "from JahiaFieldsData f where f.metadataOwnerId=? AND f.metadataOwnerType=? AND f.comp_id.workflowState>=1 order by f.comp_id.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{ownerKey.getId(), ownerKey.getType()});
    }

    public List<JahiaFieldsData> loadAllStagedFieldsByMetadataOwner(JahiaObjectPK ownerKey) {
        String hql = "from JahiaFieldsData f where f.metadataOwnerId=? AND f.metadataOwnerType=? AND f.comp_id.workflowState>1 order by f.comp_id.id";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{ownerKey.getId(), ownerKey.getType()});
    }

    public List<Integer> findStagedFieldsByMetadataOwner(JahiaObjectPK ownerKey) {
        String hql = "select f.comp_id.id from JahiaFieldsData f where f.metadataOwnerId=? AND f.metadataOwnerType=? AND f.comp_id.workflowState>1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql, new Object[]{ownerKey.getId(), ownerKey.getType()});
    }

    public JahiaFieldsData loadPublishedField(Integer fieldId) {
        String queryString = "from JahiaFieldsData f where f.comp_id.id=? and c.comp_id.workflowState=1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List<JahiaFieldsData> list = template.find(queryString, fieldId);
        return getJahiaFieldsDataFromList(list);
    }

    public String loadPublishedValue(Integer fieldId, Integer version, String language) {
        String retVal = null;
        String hql = "select distinct f.value from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState=1 and f.comp_id.versionId=? and f.comp_id.languageCode=?";
        if (fieldId != null && version != null && language != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<String> list = template.find(hql,
                    new Object[]{fieldId, version, language});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal;
    }

    public List<JahiaFieldsData> loadStagedFields(Integer fieldId) {
        String queryString = "from JahiaFieldsData f where f.comp_id.id=? and c.comp_id.workflowState>1";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, fieldId);
    }

    public String loadStagingValue(Integer fieldId, Integer version, String language) {
        String retVal = null;
        String hql = "select distinct f.value from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState>1 and f.comp_id.versionId=? and f.comp_id.languageCode=?";
        if (fieldId != null && version != null && language != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<String> list = template.find(hql,
                    new Object[]{fieldId, version, language});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal;
    }

    public String loadVersionedValue(Integer fieldId, Integer version, String language) {
        String retVal = null;
        String hql = "select distinct f.value from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState<=0 and f.comp_id.versionId=? and f.comp_id.languageCode=?";
        if (fieldId != null && version != null && language != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List<String> list = template.find(hql,
                    new Object[]{fieldId, version, language});
            if (!list.isEmpty()) {
                retVal = list.get(0);
            }
        }
        return retVal;
    }

    public void purgeJahiaFields(Integer fieldId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.delete(template.find("from JahiaFieldsData f where f.comp_id.id=?", fieldId));
    }

    public synchronized JahiaFieldsData save(JahiaFieldsData data) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setCheckWriteOperations(false);
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (data.getComp_id().getId() != null) {
            hibernateTemplate.save(data);
        } else {
            data.getComp_id().setId(getNextInteger(data));
            hibernateTemplate.save(data);
        }
        saveProperties(data, hibernateTemplate);
        hibernateTemplate.flush();
        return data;
    }

    public synchronized JahiaFieldsData saveNewVersion(JahiaFieldsData data) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(data);
        hibernateTemplate.flush();
        return data;
    }

    public synchronized void update(JahiaFieldsData data) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        saveProperties(data, hibernateTemplate);
        hibernateTemplate.update(data);
        hibernateTemplate.flush();
    }

    private void deleteProperties(JahiaFieldsData ctnDef, final HibernateTemplate template) {
        template.deleteAll(template.find(
                "from JahiaFieldsProp p where p.comp_id.fieldId=?", ctnDef
                .getComp_id().getId()));
    }

    private JahiaFieldsData getJahiaFieldsDataFromList(final List<JahiaFieldsData> list) {
        if (!list.isEmpty()) {
            final JahiaFieldsData jahiaContainer = list.get(0);
            return fillProperties(jahiaContainer);
        } else {
            return null;
        }
    }

    private void saveProperties(JahiaFieldsData data, final HibernateTemplate template) {
        if (data.getProperties() != null && !data.getProperties().isEmpty()) {
            template.setCheckWriteOperations(false);
            template.deleteAll(template.find(
                    "from JahiaFieldsProp p where p.comp_id.fieldId=?", data
                    .getComp_id().getId()));
            for (Object o : data.getProperties().entrySet()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
                template.save(new JahiaFieldsProp(new JahiaFieldsPropPK(data
                        .getComp_id().getId(), (String) entry.getKey()),
                        (String) entry.getValue()));
            }
            template.flush();
        }
    }

    public void saveProperties(Integer id, Map<Object, Object> properties) {
        String queryString = "from JahiaFieldsData f where f.comp_id.id=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCacheQueries(true);
        final List<JahiaFieldsData> list = template.find(queryString, id);
        JahiaFieldsData data = getJahiaFieldsDataFromList(list);
        data.setProperties(properties);
        saveProperties(data, template);
    }

    public int getNBFields(int id) {
        List<Long> nbPages = getHibernateTemplate().find("select count(f.comp_id.id) from JahiaFieldsData f where f.comp_id.id=?", (id));
        return nbPages.get(0).intValue();
    }

    public List<Object[]> findUsages(String sourceUri, boolean onlyLockedUsages) {
        StringBuffer hql = new StringBuffer().append("select f.comp_id.id, f.comp_id.workflowState, f.comp_id.versionId, f.comp_id.languageCode, f.value from JahiaFieldsData f ");
        hql.append("where f.type=").append(ContentFieldTypes.FILE).append(" and f.comp_id.workflowState <> ").append(EntryLoadRequest.DELETED_WORKFLOW_STATE);
        hql.append(" and f.value like ?");
        if (onlyLockedUsages) {
            hql.append(" and (f.comp_id.workflowState = " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE + " or f.comp_id.workflowState=" + EntryLoadRequest.WAITING_WORKFLOW_STATE + ")");
        }
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(hql.toString(), new Object[]{sourceUri});
    }

    public int countUsages(Integer siteID, String sourceUri, boolean onlyLockedUsages) {
        StringBuffer hql = new StringBuffer().append("select count(*) from JahiaFieldsData f ");
        hql.append("where f.fieldDefinition.jahiaSiteId=? and f.type=").append(ContentFieldTypes.FILE).append(" and f.comp_id.workflowState <> ").append(EntryLoadRequest.DELETED_WORKFLOW_STATE);
        hql.append(" and f.value=?");
        if (onlyLockedUsages) {
            hql.append(" and (f.comp_id.workflowState = " + EntryLoadRequest.ACTIVE_WORKFLOW_STATE + " or f.comp_id.workflowState=" + EntryLoadRequest.WAITING_WORKFLOW_STATE + ")");
        }
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Long> l = template.find(hql.toString(), new Object[]{siteID, sourceUri});
        return l.get(0).intValue();
    }

    public JahiaFieldsData findJahiaFieldsDataByIdAndWorkflowStateAndLanguage(Integer fieldID, Integer workflowState, String languageCode) {
        String hql = "from JahiaFieldsData f where f.comp_id.id=? and f.comp_id.workflowState = ? and f.comp_id.languageCode=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<JahiaFieldsData> list = template.find(hql, new Object[]{fieldID, workflowState, languageCode});
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public Map<Serializable, Integer> deleteAllFieldsFromSite(Integer siteID) {
        String queryString = "from JahiaFieldsData f where f.siteId=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List<JahiaFieldsData> list = template.find(queryString, siteID);
        Map<Serializable, Integer> map = new HashMap<Serializable, Integer>(list.size());
        for (JahiaFieldsData data : list) {
            deleteProperties(data, template);
            if (data.getContainerId() != 0 || data.getMetadataOwnerId() != null) {
                continue;
            }
        }
        template.deleteAll(list);
        return map;
    }

    public JahiaObjectPK findJahiaObjectPKByMetadata(Integer fieldId) {
        String hql = "select distinct f.metadataOwnerId,f.metadataOwnerType from JahiaFieldsData f where f.comp_id.id=?";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Object[]> list = template.find(hql, new Object[]{fieldId});
        if (!list.isEmpty()) {
            Object[] objs = list.get(0);
            return new JahiaObjectPK((String) objs[1], (Integer) objs[0]);
        }
        return null;
    }

    public Object[] getParentIds(Integer fieldId) {
        String hql = "select distinct f.pageId,f.containerId,f.comp_id.workflowState from JahiaFieldsData f where f.comp_id.id=? and " +
                "f.comp_id.workflowState>0 and f.comp_id.versionId>-1 " +
                "order by f.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Object[]> list = template.find(hql, new Object[]{fieldId});
        if (!list.isEmpty()) {
            return list.iterator().next();
        }
        return null;
    }

    public Object[] getDeletedParentIds(Integer fieldId) {
        String hql = "select distinct f.pageId,f.containerId,f.comp_id.workflowState from JahiaFieldsData f where f.comp_id.id=? and " +
                "((f.comp_id.workflowState>0 and f.comp_id.versionId=-1) or f.comp_id.workflowState=-1)" +
                "order by f.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List<Object[]> list = template.find(hql, new Object[]{fieldId});
        if (!list.isEmpty()) {
            return list.iterator().next();
        }
        return null;
    }

    private static boolean implicitDataConversion = true;
    private static Boolean isMySQLDB = null;

    public Integer[] getSubPageId(Integer fieldId, Integer workflowState) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        if (implicitDataConversion) {
            String hql;
            try {
                if (isMySQLDB == null) {
                    isMySQLDB = getSession().connection()
                            .getMetaData()
                            .getDatabaseProductName()
                            .toLowerCase()
                            .indexOf("mysql") >= 0;
                }
                //Todo remove that test when going to upper hibernate version by an extending dialect if hibernate bug not close around mysql cast error
                if (isMySQLDB) {
                    SQLQuery sqlQuery =
                            getSession().createSQLQuery(
                                    "select distinct jahiafield0_.value_jahia_fields_data as col_0_0_, " +
                                            "jahiapages1_.pagetype_jahia_pages_data as col_1_0_, jahiafield0_.workflow_state as col_2_0_ " +
                                            "from jahia_fields_data jahiafield0_, jahia_pages_data jahiapages1_ " +
                                            "where jahiafield0_.id_jahia_fields_data=? and jahiafield0_.type_jahia_fields_data= " +
                                            ContentFieldTypes.PAGE +
                                            " and jahiafield0_.version_id>-1 and jahiafield0_.workflow_state>0 " +
                                            "and jahiafield0_.workflow_state<=? and jahiapages1_.workflow_state>0 " +
                                            "and jahiapages1_.workflow_state<=? " +
                                            "and cast(jahiafield0_.value_jahia_fields_data as unsigned)=jahiapages1_.id_jahia_pages_data " +
                                            "order by jahiafield0_.workflow_state desc;");
                    sqlQuery.setInteger(0, fieldId);
                    sqlQuery.setInteger(1, workflowState);
                    sqlQuery.setInteger(2, workflowState);
                    sqlQuery.addScalar("col_0_0_", Hibernate.STRING);
                    sqlQuery.addScalar("col_1_0_", Hibernate.INTEGER);
                    sqlQuery.addScalar("col_2_0_", Hibernate.INTEGER);
                    List list = sqlQuery.list();
                    if (!list.isEmpty()) {
                        Object[] objects = (Object[]) list.iterator().next();
                        String s = (String) objects[0];
                        Integer i = (Integer) objects[1];
                        if (s != null) {
                            try {
                                return new Integer[]{new Integer(s), i};
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                } else {
                    hql =
                            "select distinct f.value, p.pageType, f.comp_id.workflowState from JahiaFieldsData f, JahiaPagesData p where f.comp_id.id=? and f.type=" +
                                    ContentFieldTypes
                                            .PAGE +
                                    " and f.comp_id.versionId>-1 and f.comp_id.workflowState>0 and f.comp_id.workflowState<=? and p.comp_id.workflowState>0 and p.comp_id.workflowState<=? and cast(f.value as integer)=p.comp_id.id order by f.comp_id.workflowState desc";
                    try {

                        List list = template.find(hql, new Object[]{fieldId, workflowState, workflowState});
                        if (!list.isEmpty()) {
                            Object[] objects = (Object[]) list.iterator().next();
                            String s = (String) objects[0];
                            Integer i = (Integer) objects[1];
                            if (s != null) {
                                try {
                                    return new Integer[]{new Integer(s), i};
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    } catch (DataAccessException e) {
                        implicitDataConversion = false;
                    }
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }
        if (!implicitDataConversion) {
            String hql =
                    "select distinct f.value, f.comp_id.workflowState from JahiaFieldsData f where f.comp_id.id=? and f.type=" +
                            ContentFieldTypes
                                    .PAGE +
                            " and " +
                            "f.comp_id.versionId>-1 and f.comp_id.workflowState>0 and f.comp_id.workflowState<=? order by f.comp_id.workflowState desc";
            List list = template.find(hql, new Object[]{fieldId, workflowState});
            if (!list.isEmpty()) {
                Object[] objects = (Object[]) list.iterator().next();
                String s = (String) objects[0];
                if (s != null) {
                    try {
                        return new Integer[]{new Integer(s), -1};
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return null;
    }

    public List<Integer> getFieldIDsOnPagesHavingAcls(Set pageIDs, Set aclIDs) {
        List retList = Collections.emptyList();
        if (!pageIDs.isEmpty() && !aclIDs.isEmpty()) {
            Query query = this
                    .getSession()
                    .createQuery(
                            "select distinct f.comp_id.id from JahiaFieldsData f where f.pageId in (:pageIDs) and f.jahiaAclId in (:aclIDs) and f.comp_id.workflowState >= 1 and f.comp_id.versionId != -1");

            query.setParameterList("pageIDs", pageIDs);
            query.setParameterList("aclIDs", aclIDs);
            retList = query.list();
        }
        return retList;
    }

    public <E> List<E> executeQuery(String queryString, Map<String, Object> parameters) {
        Query query = this.getSession().createQuery(queryString);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object parameter = entry.getValue();
            if (parameter instanceof Collection) {
                query.setParameterList(entry.getKey(), (Collection) parameter);
            } else if (parameter instanceof Object[]) {
                query.setParameterList(entry.getKey(), (Object[]) parameter);
            } else {
                query.setParameter(entry.getKey(), parameter);
            }
        }
        return query.list();
    }

    public Map getAllProperties(String name) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List props = template.find("select p.comp_id.fieldId,p.value from JahiaFieldsProp p " +
                "where p.comp_id.name=?", new Object[]{name});
        FastHashMap properties = new FastHashMap(props.size());
        for (Object prop : props) {
            Object[] objects = (Object[]) prop;
            properties.put(objects[0], objects[1]);
        }
        properties.setFast(true);
        return properties;
    }

    public Map<String, String> getVersions(int site, String lang) {
        final HibernateTemplate template = getHibernateTemplate();
        List items = template.find("select uuid.comp_id.name, uuid.value, version.comp_id.name, version.value " +
                "from JahiaFieldsProp uuid, JahiaFieldsProp version, JahiaFieldsData data " +
                "where data.comp_id.id=uuid.comp_id.fieldId and data.siteId=? and uuid.comp_id.fieldId=version.comp_id.fieldId and uuid.comp_id.name='originalUuid' " +
                "and (version.comp_id.name='lastImportedVersion-" + lang + "' or version.comp_id.name='lastImportedVersion')", site);
        Map results = new HashMap();
        for (Object item : items) {
            Object[] o = (Object[]) item;
            results.put(o[1], o[3]);
        }
        return results;
    }

    public List<Integer> findFieldIdByPropertyNameAndValue(String name, String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.fieldId from JahiaFieldsProp p " +
                             "where p.comp_id.name=? and p.value=?", new Object[]{name, value});
    }

    public List<Object[]> getFieldPropertiesByName(String name) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.fieldId,p.comp_id.name, p.value " +
                             "from JahiaFieldsProp p " +
                             "where p.comp_id.name=?", name);
    }

    public void deleteFieldProperty(Integer ctnId, String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaFieldsProp p where p.comp_id.name=? " +
                                         "and p.comp_id.fieldId=?", new Object[]{name, ctnId}));
    }



}

