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

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.jahia.content.ContentContainerListKey;
import org.jahia.hibernate.model.JahiaContainerList;
import org.jahia.hibernate.model.JahiaContainerListProperty;
import org.jahia.hibernate.model.JahiaContainerListPropertyPK;
import org.jahia.hibernate.model.JahiaCtnListPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 6 janv. 2005
 * Time: 17:20:14
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerListDAO extends AbstractGeneratorDAO {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(getClass());

// -------------------------- OTHER METHODS --------------------------

    public void backupContainerList(Integer containerID, Integer versionID) {
        try {
            JahiaContainerList container = loadPublishedContainerList(containerID);
            if (container != null && !container.getComp_id().getVersionId().equals(versionID)) {
                JahiaContainerList jahiaContainer = (JahiaContainerList) container.clone();
                jahiaContainer.getComp_id().setWorkflowState((0));
                HibernateTemplate hibernateTemplate = getHibernateTemplate();
                hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
                hibernateTemplate.save(jahiaContainer);
            }
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaContainerList");
        }
    }

    public JahiaContainerList createStagedContainerList(JahiaContainerList containerList, Integer versionId,
                                                        Integer workflowState) {
        try {
            JahiaContainerList jahiaContainer = (JahiaContainerList) containerList.clone();
            jahiaContainer.getComp_id().setVersionId(versionId);
            jahiaContainer.getComp_id().setWorkflowState(workflowState);
            HibernateTemplate hibernateTemplate = getHibernateTemplate();
            hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            hibernateTemplate.save(jahiaContainer);
            return jahiaContainer;
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaContainerList");
            return null;
        }
    }

    public void delete(JahiaCtnListPK jahiaCtnListPK) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerList l " +
                "where l.comp_id.id = ? and l.comp_id.versionId =? and l.comp_id.workflowState = ?",
                new Object[]{jahiaCtnListPK.getId(),
                        jahiaCtnListPK.getVersionId(),
                        jahiaCtnListPK.getWorkflowState()}));
    }

    public void deleteAllEntriesForContainerListId(Integer integer) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerList l where l.comp_id.id=?", integer));
    }

    public void deleteContainerList(JahiaContainerList jahiaContainer) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.delete(jahiaContainer);
    }

    public void deleteContainerLists(List stagedContainers) {
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.deleteAll(stagedContainers);
    }

    public JahiaContainerList findById(JahiaCtnListPK jahiaCtnListPK) {
        JahiaContainerList containerList = null;
        if (jahiaCtnListPK != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            return (JahiaContainerList) template.load(JahiaContainerList.class, jahiaCtnListPK);
        }
        return containerList;
    }

    public List<Integer> getAclContainerListIdsInSite(Integer siteID) {
        List<Integer> retval = null;
        String hql = "select distinct l.jahiaAclId from JahiaContainerList l " +
                "where l.containerDefinition.jahiaSiteId=?";
        if (siteID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{siteID});
        }
        return retval;
    }

    public List getAllContainerListIds(Integer pageID, Integer parentId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and l.parentId=?" +
                " order by l.comp_id.id asc";
        if (pageID != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID, parentId});
        }
        return retval;
    }

    public List<Integer> getAllContainerTopLevelListIds(Integer pageID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and (l.parentId is null or l.parentId = 0)" +
                " order by l.comp_id.id asc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID});
        }
        return retval;
    }

    public List<Integer> getAllContainerListIds(Integer pageID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? " +
                " order by l.comp_id.id asc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID});
        }
        return retval;
    }

    public Map<Integer, List<Integer>> getAllContainerListIdsForAllContainerInPage(Integer pageId) {
        Map<Integer, List<Integer>> retval = null;
        String hql = "select distinct l.comp_id.id,l.parentId from JahiaContainerList l where l.pageid=?";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            template.setQueryCacheRegion("containerlist");
            List<Object[]> objects = template.find(hql, new Object[]{pageId});
            retval = new FastHashMap(objects.size());
            for (Object object : objects) {
                Object[] objects1 = (Object[]) object;
                final Integer parent = (Integer) objects1[1];
                List<Integer> list = (List<Integer>) retval.get(parent);
                if (list == null) {
                    list = new ArrayList<Integer>(10);
                    retval.put(parent, list);
                }
                list.add((Integer)objects1[0]);
            }
            ((FastHashMap)retval).setFast(true);
        }
        return retval;
    }

    /**
     * Get the entry state for this container.
     *
     * @param listId
     * @return List of objectArray each array contains an integer (workflowState) and a long (version)
     */
    public List<Object[]> getAllInactiveVersionedEntry(Integer listId) {
        String queryString = "select l.comp_id.workflowState, l.comp_id.versionId from JahiaContainerList l " +
                "where l.comp_id.id=? and l.comp_id.workflowState<=0 ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, new Object[]{listId});
    }

    public List<Integer> getAllListByDefinitionID(Integer defID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where (l.parentId is null or l.parentId = 0) and l.containerDefinition.id=?" +
                " order by l.comp_id.id asc";
        if (defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{defID});
        }
        return retval;
    }

    public List<Integer> getAllStagedContainerListIds(Integer pageID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and l.comp_id.workflowState>1" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    private List fillContainerListIds(List list) {
        FastArrayList retval = new FastArrayList(list.size());
        for (Object aList : list) {
            Object[] objects = (Object[]) aList;
            retval.add(objects[0]);
        }
        retval.setFast(true);
        return retval;
    }

    public List getAllStagingContainerListIds(Integer pageID, Integer parentId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and l.parentId=? and l.comp_id.workflowState>0" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, parentId});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List getAllStagingContainerListIds(Integer pageID) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and (l.parentId is null or l.parentId = 0) and l.comp_id.workflowState>0" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getAllSubContainerListIds(Integer parentId) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.parentId=?" +
                " order by l.comp_id.id asc";
        if (parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
//            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{parentId});
        }
        return retval;
    }

    /**
     * Get the entry state for this container.
     *
     * @param listId
     * @return List of objectArray each array contains an integer (workflowState) and a long (version)
     */
    public List<Object[]> getAllVersionedEntry(Integer listId) {
        String queryString = "select l.comp_id.workflowState, l.comp_id.versionId from JahiaContainerList l " +
                "where l.comp_id.id=? and l.comp_id.workflowState<=1 ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, new Object[]{listId});
    }

    public List<Integer> getContainerListIdsInContainer(Integer containerId) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.parentId=?";
        if (containerId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{containerId});
        }
        return retval;
    }

    public List getNonDeletedContainerListIdsInContainer(Integer containerId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.parentId=? " +
                "and l.comp_id.workflowState >=1 and l.comp_id.versionId <> -1 " +
                "order by l.comp_id.workflowState desc";
        if (containerId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{containerId});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List getNonDeletedContainerListIdsInPage(Integer pageId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? " +
                "and l.comp_id.versionId <> -1 " +
                "order by l.comp_id.workflowState desc";
        if (pageId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageId});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    /**
     * Get id of a container list by its page id and definitionName.
     *
     * @param pageId
     * @param definitionName
     * @return Integer
     */
    public Integer getIdByPageIdAndDefinitionName(Integer pageId, String definitionName) {
        Integer retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and l.containerDefinition.name=?";
        if (pageId != null && definitionName != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List objList = template.find(hql, new Object[]{pageId, definitionName});
            if (!objList.isEmpty()) {
                retval = (Integer) objList.get(0);
            }
        }
        return retval;
    }

    /**
     * Get list of containerlist by page id and definition id.
     *
     * @param pageID
     * @param defID
     * @return List
     */
    public List<Integer> getListByPageAndDefinitionID(Integer pageID, Integer defID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and l.containerDefinition.id=? and l.comp_id.workflowState=1" +
                " order by l.comp_id.id asc";
        if (pageID != null && defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID, defID});
        }
        return retval;
    }

    public List getNonDeletedStagingContainerListIds(Integer pageID, Integer parentId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and l.parentId=? and l.comp_id.workflowState>0 and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, parentId});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getNonDeletedStagingContainerListIds(Integer pageID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and (l.parentId is null or l.parentId = 0) and l.comp_id.workflowState>0 and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List getNonDeletedStagingListByPageAndDefinitionID(Integer pageID, Integer defID) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and l.containerDefinition.id=? and l.comp_id.workflowState>0 and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null && defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, defID});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getNonDeletedStagingSubContainerListIds(Integer parentId) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.parentId=? and l.comp_id.workflowState>0 and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{parentId});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List getPublishedContainerListIds(Integer pageID, Integer parentId) {
        List retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and l.parentId=? and l.comp_id.workflowState=1" +
                " order by l.comp_id.id asc";
        if (pageID != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID, parentId});
        }
        return retval;
    }

    public List<Integer> getPublishedContainerListIds(Integer pageID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and (l.parentId is null or l.parentId=0) and l.comp_id.workflowState=1" +
                " order by l.comp_id.id asc";
        if (pageID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{pageID});
        }
        return retval;
    }

    public List<Integer> getPublishedListByDefinitionID(Integer defID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where (l.parentId is null or l.parentId = 0) and l.containerDefinition.id=? and l.comp_id.workflowState=1" +
                " order by l.comp_id.id asc";
        if (defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{defID});
        }
        return retval;
    }

    public List<Integer> getPublishedSubContainerListIds(Integer parentId) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.parentId=? and l.comp_id.workflowState=1" +
                " order by l.comp_id.id asc";
        if (parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{parentId});
        }
        return retval;
    }

    public List<Integer> getStagingListByDefinitionID(Integer defID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where (l.parentId is null or l.parentId = 0) and l.containerDefinition.id=? and l.comp_id.workflowState>=1" +
                " order by l.comp_id.id asc";
        if (defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{defID});
        }
        return retval;
    }

    public List<Integer> getStagingListByPageAndDefinitionID(Integer pageID, Integer defID) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.workflowState from JahiaContainerList l where l.pageid=? and l.containerDefinition.id=? and l.comp_id.workflowState>0" +
                " order by l.comp_id.id asc, l.comp_id.workflowState desc";
        if (pageID != null && defID != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, defID});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List getVersionedContainerListIds(Integer pageID, Integer parentId, Integer version) {
        List retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.versionId from JahiaContainerList l where l.pageid=? and l.parentId=? and l.comp_id.workflowState<=1 and l.comp_id.versionId<=? and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.versionId desc";
        if (pageID != null && version != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, parentId, version});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getVersionedContainerListIds(Integer pageID, Integer version) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.versionId from JahiaContainerList l where l.pageid=? and (l.parentId is null or l.parentId = 0) and l.comp_id.workflowState<=1 and l.comp_id.versionId<=? and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.versionId desc";
        if (pageID != null && version != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, version});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getVersionedListByDefinitionID(Integer defID, Integer version) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where (l.parentId is null or l.parentId = 0) and l.containerDefinition.id=? and l.comp_id.workflowState<=1 and l.comp_id.versionId<=? and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc";
        if (defID != null && version != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            retval = template.find(hql, new Object[]{defID, version});
        }
        return retval;
    }

    public List<Integer> getVersionedListByPageAndDefinitionID(Integer pageID, Integer defID, Integer version) {
        List<Integer> retval = null;
        String hql = "select distinct l.comp_id.id, l.comp_id.versionId from JahiaContainerList l where l.pageid=? and l.containerDefinition.id=? and l.comp_id.workflowState<=1 and l.comp_id.versionId<=? and l.comp_id.versionId>-1" +
                " order by l.comp_id.id asc, l.comp_id.versionId desc";
        if (pageID != null && defID != null && version != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{pageID, defID, version});
            retval = fillContainerListIds(list);
        }
        return retval;
    }

    public List<Integer> getVersionedSubContainerListIds(Integer parentId, Integer version) {
        List retval = null;
        String hql ="select distinct l.comp_id.id from JahiaContainerList l where l.parentId=? and l.comp_id.workflowState<=1 and l.comp_id.versionId<=? and l.comp_id.versionId>-1"+
                    " order by l.comp_id.id asc";
        if (version != null && parentId != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List list = template.find(hql, new Object[]{parentId, version});
            retval = list;
        }
        return retval;
    }

    public JahiaContainerList loadContainerList(Integer integer) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List list = template.find(queryString.toString(), integer);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainerList> loadInactiveContainerLists(Integer listId) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState<=0");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString.toString(), listId);
    }

    public JahiaContainerList loadPublishedContainerList(Integer listId) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState=1");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List list = template.find(queryString.toString(), listId);
        return getJahiaContainerFromList(list);
    }

    public JahiaContainerList loadStagedContainerList(Integer listId) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState>1");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List list = template.find(queryString.toString(), listId);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainerList> loadStagedContainerLists(Integer listId) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState>1");
        final HibernateTemplate template = getHibernateTemplate();
//        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString.toString(), listId);
    }

    public JahiaContainerList loadStagingContainerList(Integer integer) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState>=1 ");
        queryString.append("order by l.comp_id.workflowState desc");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List list = template.find(queryString.toString(), integer);
        return getJahiaContainerFromList(list);
    }

    public List<JahiaContainerList> loadStagingContainerLists(Integer listId) {
        String queryString = "from JahiaContainerList l where l.comp_id.id=? and l.comp_id.workflowState>=1 order by l.comp_id.workflowState";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find(queryString, listId);
    }

    public JahiaContainerList loadVersionedContainerList(Integer integer, Integer version) {
        StringBuffer queryString = new StringBuffer("from JahiaContainerList l ");
        queryString.append("where l.comp_id.id=? ");
        queryString.append("and l.comp_id.workflowState<2 ");
        queryString.append("and l.comp_id.versionId<=? ");
        queryString.append("order by l.comp_id.versionId desc");
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        final List list = template.find(queryString.toString(), new Object[]{integer, version});
        return getJahiaContainerFromList(list);
    }

    public void save(JahiaContainerList containerList) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        if (containerList.getComp_id().getId() != null) {
            hibernateTemplate.merge(containerList);
        } else {
            synchronized (this) {
                /*
                List list = hibernateTemplate.find("select max(l.comp_id.id) from JahiaContainerList l");
                int i = 0;
                if(list!=null && list.size()>0) {
                    Object o = list.get(0);
                    if(o!=null)
                        i=((Integer) o);
                }
                containerList.getComp_id().setId((i + 1));
                */
                containerList.getComp_id().setId(getNextInteger(containerList));
                hibernateTemplate.merge(containerList);
            }
        }
    }

    public void mergeVersion(JahiaContainerList containerList) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.merge(containerList);
    }

    public void saveNewVersion(JahiaContainerList containerList) {
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.save(containerList);
    }

    public void update(JahiaContainerList containerList) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.update(containerList);
    }

    public Map<String, String> getProperties(Integer id) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCacheQueries(true);
        List props = template.find("select p.comp_id.name,p.value from JahiaContainerListProperty p " +
                "where p.comp_id.containerListId=?", id);
        FastHashMap properties = new FastHashMap(props.size());
        for (Object prop : props) {
            Object[] objects = (Object[]) prop;
            properties.put(objects[0], objects[1]);
        }
        properties.setFast(true);
        return properties;
    }

    private JahiaContainerList getJahiaContainerFromList(final List list) {
        return (!list.isEmpty() ? (JahiaContainerList) list.get(0) : null);
    }

    public void saveProperties(Integer ctnID, Map properties) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCheckWriteOperations(false);
        if (properties != null) {
            properties = new HashMap(properties);
            if (!properties.isEmpty()) {
                List list = template.find("from JahiaContainerListProperty p where p.comp_id.containerListId=?",
                        ctnID);
                for (Object aList : list) {
                    JahiaContainerListProperty jahiaContainerListProperty = (JahiaContainerListProperty) aList;
                    String name = jahiaContainerListProperty.getComp_id().getName();
                    if (!properties.containsKey(name)) {
                        template.delete(jahiaContainerListProperty);
                    } else {
                        jahiaContainerListProperty.setValue((String) properties.remove(name));
                        template.save(jahiaContainerListProperty);
                    }
                }
                JahiaContainerList containerList = null;
                for (Object o : properties.entrySet()) {
                    if (containerList == null) {
                        containerList = loadContainerList(ctnID);
                    }
                    Map.Entry entry = (Map.Entry) o;
                    template.saveOrUpdate(new JahiaContainerListProperty(new JahiaContainerListPropertyPK(ctnID,
                            (String) entry.getKey()),
                            containerList.getContainerDefinition().getJahiaSiteId(),
                            (String) entry.getValue()));
                }
                template.flush();
            } else {
                deleteProperties(ctnID);
            }
        }
    }

    public int getNBContainerList(int containerListId) {
        List nbPages = getHibernateTemplate().find("select count(c.comp_id.id) from JahiaContainerList c where c.comp_id.id=?", (containerListId));
        return ((Long) nbPages.get(0)).intValue();
    }

    public Map deleteAllListsFromSite(Integer siteID) {
        String queryString = "from JahiaContainerList c where c.containerDefinition.jahiaSiteId=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        List list = template.find(queryString, siteID);
        Map map = new HashMap(list.size());
        for (Object aList : list) {
            JahiaContainerList data = (JahiaContainerList) aList;
            Map properties = getProperties(data.getComp_id().getId());
            if (properties != null) {
                for (Object o1 : properties.entrySet()) {
                    Map.Entry o = (Map.Entry) o1;
                    String key = (String) o.getKey();
                    if (key.startsWith("view_field_acl")) {
                        Integer value = Integer.valueOf((String) o.getValue());
                        map.put(key, value);
                    }
                }
            }
            deleteProperties(data.getComp_id().getId());
            map.put(new ContentContainerListKey(data.getComp_id().getId()), data.getJahiaAclId());
        }
        template.deleteAll(list);
        return map;
    }

    public void deleteProperties(Integer ctnListId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerListProperty p where p.comp_id.containerListId=?", ctnListId));
    }

    public Integer getIdByPageIdAndDefinitionNameAndParentID(Integer pageId, String containerListName, Integer parentId) {
        Integer retval = null;
        String hql = "select distinct l.comp_id.id from JahiaContainerList l where l.pageid=? and l.containerDefinition.name=? and l.parentId=?";
        if (pageId != null && containerListName != null) {
            final HibernateTemplate template = getHibernateTemplate();
            template.setCacheQueries(true);
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            List objList = template.find(hql, new Object[]{pageId, containerListName, parentId});
            if (!objList.isEmpty()) {
                retval = (Integer) objList.get(0);
            }
        }
        return retval;
    }


    public Object[] getParentIds(Integer ctnListId) {
        String hql = "select distinct l.pageid,l.parentId,l.comp_id.workflowState from JahiaContainerList l where l.comp_id.id=? " +
                " order by l.comp_id.workflowState desc";
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        List list = template.find(hql, new Object[]{ctnListId});
        if (!list.isEmpty()) {
            return (Object[]) list.iterator().next();
        }
        return null;
    }

    private static Boolean isMySQLDB = null;

    public List<Integer> getContainerListIDsOnPagesHavingAcls(Set pageIDs, Set aclIDs) {
        List<Integer> retList = Collections.emptyList();
        if (!pageIDs.isEmpty() && !aclIDs.isEmpty()) {
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
                    // Here we rely on Mysql automatic casting capabilities
                    Query query = this.getSession()
                            .createQuery(
                                    "select distinct l.comp_id.id from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid in (:pageIDs) and (l.jahiaAclId in (:aclIDs) or ((p.comp_id.name like 'view_field_acl_%') and (p.value in (:aclIDs)))) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1");

                    query.setParameterList("pageIDs", pageIDs);
                    query.setParameterList("aclIDs", aclIDs);
                    retList = query.list();
                } else {
                    Query query = this.getSession()
                            .createQuery(
                                    "select distinct l.comp_id.id from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and l.pageid in (:pageIDs) and (l.jahiaAclId in (:aclIDs) or ((p.comp_id.name like 'view_field_acl_%') and (cast(p.value as integer) in (:aclIDs)))) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1");

                    query.setParameterList("pageIDs", pageIDs);
                    query.setParameterList("aclIDs", aclIDs);
                    retList = query.list();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return retList;
    }

    public List<Integer> getContainerListIDsHavingAcls(Set aclIDs) {
        List<Integer> retList = Collections.emptyList();
        if (!aclIDs.isEmpty()) {
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
                    // Here we rely on Mysql automatic casting capabilities
                    Query query = this.getSession()
                            .createQuery(
                                    "select distinct l.comp_id.id from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and (l.jahiaAclId in (:aclIDs) or ((p.comp_id.name like 'view_field_acl_%') and (p.value in (:aclIDs)))) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1");

                    query.setParameterList("aclIDs", aclIDs);
                    retList = query.list();
                } else {
                    Query query = this.getSession()
                            .createQuery(
                                    "select distinct l.comp_id.id from JahiaContainerList l, JahiaContainerListProperty p where p.comp_id.containerListId=l.comp_id.id and (l.jahiaAclId in (:aclIDs) or ((p.comp_id.name like 'view_field_acl_%') and (cast(p.value as integer) in (:aclIDs)))) and l.comp_id.workflowState >= 1 and l.comp_id.versionId != -1");

                    query.setParameterList("aclIDs", aclIDs);
                    retList = query.list();
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return retList;
    }

    public Map<String, String> getVersions(int site) {
        final HibernateTemplate template = getHibernateTemplate();
        List items = template.find("select uuid.comp_id.name, uuid.value, version.comp_id.name, version.value " +
                "from JahiaContainerListProperty uuid, JahiaContainerListProperty version " +
                "where uuid.siteId=? and uuid.comp_id.containerListId=version.comp_id.containerListId and uuid.comp_id.name='originalUuid' " +
                "and version.comp_id.name='lastImportedVersion'", site);
        Map<String, String> results = new HashMap<String, String>();
        for (Object item : items) {
            Object[] o = (Object[]) item;
            results.put((String)o[1], (String)o[3]);
        }
        return results;
    }


    public List<Integer> findContainerListIdByPropertyNameAndValue(String name, String value) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select distinct p.comp_id.containerListId from JahiaContainerListProperty p " +
                             "where p.comp_id.name=? and p.value=?", new Object[]{name, value});
    }

    public List<Object[]> getContainerListPropertiesByName(String name) {
        final HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        return template.find("select p.comp_id.containerListId,p.comp_id.name, p.value " +
                             "from JahiaContainerListProperty p " +
                             "where p.comp_id.name=?", name);
    }

    public void deleteContainerListProperty(Integer ctnId, String name) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find("from JahiaContainerListProperty p where p.comp_id.name=? " +
                                         "and p.comp_id.containerListId=?", new Object[]{name, ctnId}));
    }

}

