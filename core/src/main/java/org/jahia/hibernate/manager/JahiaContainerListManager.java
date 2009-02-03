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
package org.jahia.hibernate.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObject;
import org.jahia.content.CrossReferenceManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.*;
import org.jahia.hibernate.model.JahiaContainerList;
import org.jahia.hibernate.model.JahiaCtnListPK;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.spring.advice.CacheAdvice;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.dao.WorkflowHistoryDAO;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 6 janv. 2005
 * Time: 14:38:03
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerListManager {
    // ------------------------------ FIELDS ------------------------------
    public static final String JAHIA_CONTAINER_LIST_CACHE = "JahiaContainerListCache";
    public static final String JAHIA_CONTAINER_LIST_ID_CACHE = "JahiaContainerListIdCache";
    public static final String JAHIA_CONTAINER_LIST_CACHE_PREFIX = "JahiaContainerListId_";
    public static final String CONTENT_CONTAINER_LIST_CACHE_PREFIX = "ContentContainerListId_";
    protected JahiaContainerListDAO dao = null;
    private JahiaContainerDefinitionDAO definitionDAO = null;
    private JahiaObjectDAO jahiaObjectDAO = null;
    private JahiaWorkflowDAO jahiaWorkflowDAO = null;
    private JahiaPagesDAO jahiaPagesDAO = null;

    private WorkflowInstanceDAO workflowInstanceDAO;
    private WorkflowHistoryDAO workflowHistoryDAO;

    private JahiaGroupManager groupManager;

    private Log log = LogFactory.getLog(JahiaContainerListDAO.class);
    private CacheService cacheService = null;
    private Cache listCache = null;
    private Cache<GroupCacheKey, Integer> idsCache;
    public static final String PAGE_ID_CACHE_PREFIX = "PageId_";
// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaContainerDefinitionDAO(JahiaContainerDefinitionDAO definitionDAO) {
        this.definitionDAO = definitionDAO;
    }

    public void setJahiaContainerListDAO(JahiaContainerListDAO dao) {
        this.dao = dao;
    }

    public JahiaObjectDAO getJahiaObjectDAO() {
        return jahiaObjectDAO;
    }

    public void setJahiaObjectDAO(JahiaObjectDAO jahiaObjectDAO) {
        this.jahiaObjectDAO = jahiaObjectDAO;
    }

    public JahiaWorkflowDAO getJahiaWorkflowDAO() {
        return jahiaWorkflowDAO;
    }

    public void setJahiaWorkflowDAO(JahiaWorkflowDAO jahiaWorkflowDAO) {
        this.jahiaWorkflowDAO = jahiaWorkflowDAO;
    }

    public JahiaPagesDAO getJahiaPagesDAO() {
        return jahiaPagesDAO;
    }

    public void setJahiaPagesDAO(JahiaPagesDAO jahiaPagesDAO) {
        this.jahiaPagesDAO = jahiaPagesDAO;
    }

    public WorkflowInstanceDAO getWorkflowInstanceDAO() {
        return workflowInstanceDAO;
    }

    public void setWorkflowInstanceDAO(WorkflowInstanceDAO workflowInstanceDAO) {
        this.workflowInstanceDAO = workflowInstanceDAO;
    }

    public WorkflowHistoryDAO getWorkflowHistoryDAO() {
        return workflowHistoryDAO;
    }

    public void setWorkflowHistoryDAO(WorkflowHistoryDAO workflowHistoryDAO) {
        this.workflowHistoryDAO = workflowHistoryDAO;
    }

    public JahiaGroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(JahiaGroupManager groupManager) {
        this.groupManager = groupManager;
    }

// -------------------------- OTHER METHODS --------------------------

    public void copyEntry(int id, EntryStateable fromE, EntryStateable toE) {
        try {
            JahiaContainerList jahiaContainerList = dao.findById(new JahiaCtnListPK((id),
                    (fromE.getVersionID()),
                    (fromE.getWorkflowState())));
            JahiaContainerList jahiaContainerListClone = (JahiaContainerList) jahiaContainerList.clone();
            jahiaContainerListClone.getComp_id().setVersionId((toE.getVersionID()));
            jahiaContainerListClone.getComp_id().setWorkflowState((toE.getWorkflowState()));
            dao.save(jahiaContainerListClone);
            flushCache(id, jahiaContainerListClone.getContainerDefinition().getJahiaSiteId(),
                    jahiaContainerListClone.getContainerDefinition().getName());
        } catch (CloneNotSupportedException e) {
            log.error("Clone not supported for object JahiaContainerList", e);
        }
    }

    public void createContainerList(org.jahia.data.containers.JahiaContainerList containerList,
                                    JahiaSaveVersion saveVersion, int siteId) {
        JahiaContainerList jahiaContainerList = new JahiaContainerList();
        jahiaContainerList.setContainerDefinition(definitionDAO.findDefinitionById((containerList.getctndefid())));
        jahiaContainerList.setPageid((containerList.getPageID()));
        final int parentEntryID = containerList.getParentEntryID();
        if (parentEntryID > 0) {
            jahiaContainerList.setParentId((parentEntryID));
        }
        jahiaContainerList.setJahiaAclId((containerList.getAclID()));
        JahiaCtnListPK pk = new JahiaCtnListPK();
        final int workflowState = saveVersion.getWorkflowState();
        pk.setWorkflowState((workflowState));
        if (workflowState > 1) {
            pk.setVersionId((0));
        } else {
            pk.setVersionId((saveVersion.getVersionID()));
        }
        jahiaContainerList.setComp_id(pk);

        boolean isNew = (jahiaContainerList.getComp_id().getId() == null);
        dao.save(jahiaContainerList);
        dao.saveProperties(jahiaContainerList.getComp_id().getId(), containerList.getProperties());
        containerList.setID(jahiaContainerList.getComp_id().getId());

        if (isNew) {
            jahiaObjectDAO.create(ContentContainerListKey.CONTAINERLIST_TYPE,
                    (containerList.getID()), (siteId));

            if (listCache == null) {
                try {
                    listCache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_CACHE);
                } catch (JahiaInitializationException e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (listCache != null) {
                List<ContentObjectEntryState> activeAndStagedEntry = new ArrayList<ContentObjectEntryState>(1);
                activeAndStagedEntry.add(new ContentObjectEntryState(jahiaContainerList.getComp_id().getWorkflowState(),
                        jahiaContainerList.getComp_id().getVersionId(), ContentObject.SHARED_LANGUAGE));
                ContentContainerList contentContainerList = convertToContentContainerList(jahiaContainerList, activeAndStagedEntry);
                listCache.put(CONTENT_CONTAINER_LIST_CACHE_PREFIX + containerList.getID(), contentContainerList);
            }
        }

        flushCache(containerList.getID(), siteId,
                jahiaContainerList.getContainerDefinition().getName());
    }

    public void deleteContainerList(int containerListId, JahiaSaveVersion saveVersion) {
        // Load active container
        JahiaContainerList container = null;
        try {
            container = dao.loadPublishedContainerList((containerListId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (saveVersion.isStaging()) {
            if (container != null &&
                    container.getComp_id().getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                // We have an active version so we create a staged version if none exist
                List<JahiaContainerList> stagedContainers = null;
                try {
                    stagedContainers = dao.loadStagedContainerLists((containerListId));
                } catch (ObjectRetrievalFailureException e) {
                }
                if (stagedContainers == null || stagedContainers.size() == 0) {
                    JahiaContainerList jahiaContainer = dao.createStagedContainerList(container,
                            (saveVersion.getVersionID()),
                            (saveVersion.getWorkflowState()));
                    stagedContainers = new ArrayList<JahiaContainerList>(1);
                    stagedContainers.add(jahiaContainer);
                }
                // we update the container
                for (Object stagedContainer : stagedContainers) {
                    JahiaContainerList jahiaContainer = (JahiaContainerList) stagedContainer;
                    if (jahiaContainer.getComp_id().getVersionId() != -1) {
                        dao.deleteContainerList(jahiaContainer);
                        try {
                            JahiaContainerList jahiaContainer2 = (JahiaContainerList) jahiaContainer.clone();
                            jahiaContainer2.getComp_id().setVersionId((-1));
                            dao.save(jahiaContainer2);
                        } catch (CloneNotSupportedException e) {
                            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                        }
                    }
                }
            } else {
                List<JahiaContainerList> stagedContainers = null;
                try {
                    stagedContainers = dao.loadStagedContainerLists((containerListId));
                } catch (ObjectRetrievalFailureException e) {
                }
                dao.deleteContainerLists(stagedContainers);
            }
        } else if (saveVersion.isVersioned()) {
            dao.backupContainerList((containerListId), (saveVersion.getVersionID()));
            dao.deleteContainerList(container);
            try {
                org.jahia.hibernate.model.JahiaContainer jahiaContainer =
                        (org.jahia.hibernate.model.JahiaContainer) container.clone();
                jahiaContainer.getComp_id().setWorkflowState((-1));
                jahiaContainer.getComp_id().setVersionId((saveVersion.getVersionID()));
                dao.save(container);
            } catch (CloneNotSupportedException e) {
                log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
            }
        } else {
            dao.deleteContainerList(dao.loadPublishedContainerList((containerListId)));
        }
        flushCache(containerListId, container != null ? container.getContainerDefinition().getJahiaSiteId() : 0,
                container != null ? container.getContainerDefinition().getName() : null);
        // remove all links for page if no pages exist at all with this id
        if (dao.getNBContainerList(containerListId) == 0) {
            deleteContainerListReferences(containerListId);
        }
    }

    public void deleteEntry(int id, EntryStateable deleteEntryState) {
        dao.delete(new JahiaCtnListPK((id), (deleteEntryState.getVersionID()),
                (deleteEntryState.getWorkflowState())));
        flushCache(id, 0, null);
        // remove all links for page if no pages exist at all with this id
        if (dao.getNBContainerList(id) == 0) {
            deleteContainerListReferences(id);
        }
    }

    private void deleteContainerListReferences(int containerListId) {
        try {
            jahiaObjectDAO.delete(new JahiaObjectPK(ContentContainerListKey.CONTAINERLIST_TYPE, (containerListId)));
            String key = ContentContainerListKey.toObjectKeyString(containerListId);
            jahiaWorkflowDAO.delete(key);
            dao.deleteProperties((containerListId));
            workflowHistoryDAO.removeWorkflowHistory(key);
            workflowInstanceDAO.removeWorkflowInstance(key);
            groupManager.searchAndDelete("workflowrole_" + key+"_%", null);
            CrossReferenceManager.getInstance().removeAllObjectXRefs(new ContentContainerListKey(containerListId));
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }
    }


    public Collection<Integer> getAllPageTopLevelContainerListIDs(int pageID) {
        return dao.getAllContainerTopLevelListIds((pageID));
    }

    public Collection<Integer> getAllPageContainerListIDs(int pageID) {
        return dao.getAllContainerListIds((pageID));
    }

    public ContentContainerList getContainerList(int containerListID) {
        ContentContainerList containerList = null;
        if (listCache == null) {
            try {
                listCache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_CACHE);
            } catch (JahiaInitializationException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (listCache != null) {
            containerList = (ContentContainerList) listCache.get(CONTENT_CONTAINER_LIST_CACHE_PREFIX + containerListID);
        }
        if (containerList == null) {
            List<JahiaContainerList> list = dao.loadStagingContainerLists((containerListID));
            if (!list.isEmpty()) {
                JahiaContainerList jahiaContainerList = list.get(0);
                List<ContentObjectEntryState> activeAndStagedEntry = new ArrayList<ContentObjectEntryState>(list.size());
                for (JahiaContainerList jahiaContainerList1 : list) {
                    activeAndStagedEntry.add(new ContentObjectEntryState(jahiaContainerList1.getComp_id().getWorkflowState(),
                            jahiaContainerList1.getComp_id().getVersionId(),
                            ContentObject.SHARED_LANGUAGE));
                }
                containerList = convertToContentContainerList(jahiaContainerList, activeAndStagedEntry);
            } else {
                list = dao.loadInactiveContainerLists((containerListID));
                if (!list.isEmpty()) {
                    JahiaContainerList jahiaContainerList = list.get(0);
                    containerList = convertToContentContainerList(jahiaContainerList, new ArrayList<ContentObjectEntryState>());
                }
            }
            if (listCache != null) {
                listCache.put(CONTENT_CONTAINER_LIST_CACHE_PREFIX + containerListID, containerList);
            }
        }
        return containerList;
    }

    public void updateContainerListAclId(int containerListId, int aclId) {
        List<JahiaContainerList> list = dao.loadStagingContainerLists((containerListId));
        for (Object aList : list) {
            JahiaContainerList jahiaContainerList = (JahiaContainerList) aList;
            jahiaContainerList.setJahiaAclId((aclId));
            dao.save(jahiaContainerList);
            flushCache(jahiaContainerList.getComp_id().getId(), 0, jahiaContainerList.getContainerDefinition().getName());
        }
    }

    public List<Integer> getContainerListIdsByDefinition(int pageID, int defID, EntryLoadRequest loadVersion) {
        try {
            if (loadVersion != null) {
                if (loadVersion.isStaging()) {
                    return dao.getStagingListByPageAndDefinitionID((pageID), (defID));
                } else if (loadVersion.isVersioned()) {
                    return dao.getVersionedListByPageAndDefinitionID((pageID), (defID),
                            (loadVersion.getVersionID()));
                }
            }
            return dao.getListByPageAndDefinitionID((pageID), (defID));
        } catch (ObjectRetrievalFailureException e) {
            log.warn("ContainerList not found for pageId = " + pageID + " and definitionId = " + defID, e);
        }
        return null;
    }

    public List<Integer> getContainerListIdsInContainer(int containerId) {
        return dao.getContainerListIdsInContainer((containerId));
    }

    /**
     * @param containerName
     * @param pageID
     * @return the id if found, -1 if not found
     */
    public int getIdByPageIdAndDefinitionName(String containerName, int pageID) {
        Integer idByPageIdAndDefinitionName = null;
        Set<String> groups = new HashSet<String>();
        groups.add("ContainerListName_" + containerName);
        groups.add(PAGE_ID_CACHE_PREFIX + (pageID));
        GroupCacheKey groupCacheKey = new GroupCacheKey("JahiaContainerListIdByNameAndPageId_" + containerName + pageID,
                groups);
        if (idsCache == null) {
            try {
                idsCache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_ID_CACHE);
            } catch (JahiaInitializationException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (idsCache != null) {
            idByPageIdAndDefinitionName = (Integer) idsCache.get(groupCacheKey);
        }
        if (idByPageIdAndDefinitionName == null) {
            idByPageIdAndDefinitionName = dao.getIdByPageIdAndDefinitionName((pageID), containerName);
            if (idByPageIdAndDefinitionName != null) {
                if (idsCache != null) {
                    idsCache.put(groupCacheKey, idByPageIdAndDefinitionName);
                }
                return idByPageIdAndDefinitionName;
            } else {
                if (idsCache != null) {
                    idsCache.put(groupCacheKey, (-1));
                }
                return -1;
            }
        }
        return idByPageIdAndDefinitionName;
    }

    public List<Integer> getOnlyStagedContainerIdsInPage(int pageID) {
        return dao.getAllStagedContainerListIds((pageID));
    }

    public List<Integer> getPageTopLevelContainerListIDs(int pageID, EntryLoadRequest request) {
        List<Integer> retval = null;
        try {
            if (request != null) {
                if (request.isStaging()) {
                    if (request.isWithMarkedForDeletion()) {
                        retval = dao.getNonDeletedStagingContainerListIds((pageID));
                    } else {
                        retval = dao.getAllContainerTopLevelListIds((pageID));
                    }
                } else if (request.isVersioned()) {
                    retval = dao.getVersionedContainerListIds((pageID),
                            (request.getVersionID()));
                } else if (request.isCurrent()) {
                    retval = dao.getPublishedContainerListIds((pageID));
                }
            } else {
                retval = dao.getAllContainerTopLevelListIds((pageID));
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("ContainerList not found for pageId = " + pageID, e);
        }
        return retval;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<Integer> getSubContainerListIDs(int containerID, EntryLoadRequest request) {
        List<Integer> retval = null;
        try {
            if (request != null && !request.isCurrent()) {
                if (request.isStaging()) {
                    if (request.isWithMarkedForDeletion()) {
                        retval = dao.getNonDeletedStagingSubContainerListIds((containerID));
                    } else {
                        retval = dao.getAllSubContainerListIds((containerID));
                    }
                } else if (request.isVersioned()) {
                    retval = dao.getVersionedSubContainerListIds((containerID),
                            (request.getVersionID()));
                } else {
                    retval = dao.getPublishedSubContainerListIds((containerID));
                }
            } else {
                retval = dao.getAllSubContainerListIds((containerID));
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("ContainerList not found for containerID = " + containerID, e);
        }
        return retval;
    }

    public List<Integer> getTopLevelContainerListIDsByDefinitionID(int definitionId, EntryLoadRequest request) {
        List<Integer> retval = null;
        try {
            if (request != null) {
                if (request.isStaging()) {
                    retval = dao.getStagingListByDefinitionID((definitionId));
                } else if (request.isVersioned()) {
                    retval = dao.getVersionedListByDefinitionID((definitionId),
                            (request.getVersionID()));
                } else if (request.isCurrent()) {
                    retval = dao.getPublishedListByDefinitionID((definitionId));
                }
            } else {
                retval = dao.getAllListByDefinitionID((definitionId));
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("ContainerList not found for definitionId = " + definitionId, e);
        }
        return retval;
    }

    public List<ContentObjectEntryState> getVersionedEntryStates(int containerId, boolean withActive) {
        List<Object[]> list;
        if (withActive) {
            list = dao.getAllVersionedEntry((containerId));
        } else {
            list = dao.getAllInactiveVersionedEntry((containerId));
        }
        List<ContentObjectEntryState> retList = new ArrayList<ContentObjectEntryState>(list.size());
        for (Object aList : list) {
            Object[] objects = (Object[]) aList;
            retList.add(new ContentObjectEntryState(((Integer) objects[0]),
                    ((Integer) objects[1]),
                    ContentObject.SHARED_LANGUAGE));
        }
        return retList;
    }

    public Map<Integer, List<Integer>> loadAllContainerListsFromContainerFromPage(int pageID) {
        return dao.getAllContainerListIdsForAllContainerInPage((pageID));
    }

    public org.jahia.data.containers.JahiaContainerList loadContainerList(int containerListID,
                                                                          EntryLoadRequest loadVersion) {
        JahiaContainerList jahiaContainerList = null;
        org.jahia.data.containers.JahiaContainerList containerList = null;
        if (listCache == null) {
            try {
                listCache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_CACHE);
            } catch (JahiaInitializationException e) {
                log.error(e.getMessage(), e);
            }
        }
        GroupCacheKey entryKey = CacheAdvice.toGroupCacheKey(new Object[]{JAHIA_CONTAINER_LIST_CACHE_PREFIX + containerListID, loadVersion});
        if (listCache != null) {
            containerList = (org.jahia.data.containers.JahiaContainerList) listCache.get(entryKey);
        }
        if (containerList == null) {
            if (loadVersion != null) {
                if (loadVersion.isStaging()) {
                    jahiaContainerList = dao.loadStagingContainerList((containerListID));
                } else if (loadVersion.isVersioned()) {
                    jahiaContainerList = dao.loadVersionedContainerList((containerListID),
                            (loadVersion.getVersionID()));
                }
            }
            if (jahiaContainerList == null) {
                jahiaContainerList = dao.loadContainerList((containerListID));
            }
            if (jahiaContainerList != null) {
                final Integer parentId = jahiaContainerList.getParentId();
                int parentEntryID = 0;
                if (parentId != null) {
                    parentEntryID = parentId;
                }
                containerList = new org.jahia.data.containers.JahiaContainerList(jahiaContainerList.getComp_id().getId(),
                        parentEntryID, jahiaContainerList.getPageid(),
                        jahiaContainerList.getContainerDefinition().getId(),
                        jahiaContainerList.getJahiaAclId());

                if (listCache != null) {
                    listCache.put(entryKey, containerList);
                }
            }
        }
        return containerList;
    }

    public void purgeContainerList(int listID) {
        dao.deleteAllEntriesForContainerListId((listID));
        flushCache(listID, 0, null);
    }

    public void updateContainerList(org.jahia.data.containers.JahiaContainerList containerList,
                                    JahiaSaveVersion saveVersion) {
        JahiaCtnListPK pk = new JahiaCtnListPK();
        final int workflowState = saveVersion.getWorkflowState();
        if (saveVersion.isStaging()) {
            pk.setWorkflowState((workflowState));
            pk.setVersionId((0));
        } else {
            if (saveVersion.isVersioned()) {
                dao.backupContainerList((containerList.getID()), (saveVersion.getVersionID()));
            }
            pk.setWorkflowState((1));
            pk.setVersionId((saveVersion.getVersionID()));
        }
        pk.setId((containerList.getID()));
        JahiaContainerList jahiaContainerList;
        jahiaContainerList = new JahiaContainerList();
        jahiaContainerList.setComp_id(pk);
        jahiaContainerList.setContainerDefinition(definitionDAO.findDefinitionById((containerList.getctndefid())));
        jahiaContainerList.setPageid((containerList.getPageID()));
        final int parentEntryID = containerList.getParentEntryID();
        if (parentEntryID > 0) {
            jahiaContainerList.setParentId((parentEntryID));
        }
        jahiaContainerList.setJahiaAclId((containerList.getAclID()));

        dao.save(jahiaContainerList);
        dao.saveProperties(jahiaContainerList.getComp_id().getId(), containerList.getProperties());
        containerList.setID(jahiaContainerList.getComp_id().getId());
        flushCache(containerList.getID(), jahiaContainerList.getContainerDefinition().getJahiaSiteId(),
                jahiaContainerList.getContainerDefinition().getName());
    }

    public void validateStagedContainerList(int containerListId, JahiaSaveVersion saveVersion) {
        JahiaContainerList containerList = null;
        try {
            containerList = dao.loadStagedContainerList((containerListId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (containerList != null) {
            JahiaContainerList publishedContainer = dao.loadPublishedContainerList((containerListId));

            int versionID = saveVersion.getVersionID();
            boolean isDeleted = (containerList.getComp_id().getVersionId() == -1);

            if ((publishedContainer != null) && (publishedContainer.getComp_id().getVersionId() == versionID) && !isDeleted) {
                try {
                    JahiaContainerList jahiaContainerList = (JahiaContainerList) containerList.clone();
                    jahiaContainerList.getComp_id().setVersionId((versionID));
                    jahiaContainerList.getComp_id().setWorkflowState((1));
                    dao.mergeVersion(jahiaContainerList);
                } catch (CloneNotSupportedException e) {
                    log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                }
            } else {
                // We have something to validate
                if (saveVersion.isVersioned()) {
                    try {
                        if (publishedContainer != null && !publishedContainer.getComp_id().getVersionId().equals((versionID))) {
                            JahiaContainerList jahiaContainerList = (JahiaContainerList) publishedContainer.clone();
                            jahiaContainerList.getComp_id().setWorkflowState((0));
                            dao.saveNewVersion(jahiaContainerList);
                        }
                    } catch (CloneNotSupportedException e) {
                        log.error("Could not clone JahiaContainerList");
                    }
                }

                // Is it a delete
                if (publishedContainer != null) {
                    dao.deleteContainerList(publishedContainer);
                }
                if (isDeleted) {
                    if (saveVersion.isVersioned()) {
                        try {
                            JahiaContainerList jahiaContainer = (JahiaContainerList) containerList.clone();
                            jahiaContainer.getComp_id().setVersionId((versionID));
                            jahiaContainer.getComp_id().setWorkflowState((-1));
                            dao.saveNewVersion(jahiaContainer);
                        } catch (CloneNotSupportedException e) {
                            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                        }
                    }
                } else {
                    try {
                        JahiaContainerList jahiaContainer = (JahiaContainerList) containerList.clone();
                        jahiaContainer.getComp_id().setVersionId((versionID));
                        jahiaContainer.getComp_id().setWorkflowState((1));
                        dao.save(jahiaContainer);
                    } catch (CloneNotSupportedException e) {
                        log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                    }
                }
            }

            dao.deleteContainerList(containerList);

            flushCache(containerListId, containerList.getContainerDefinition().getJahiaSiteId(),
                    containerList.getContainerDefinition().getName());
        }
    }

    private ContentContainerList convertToContentContainerList(JahiaContainerList jahiaContainerList,
                                                               List<ContentObjectEntryState> activeAndStagedEntry) {
        ContentContainerList containerList;
        final Integer parentId = jahiaContainerList.getParentId();
        int parentContainerID = 0;
        if (parentId != null) {
            parentContainerID = parentId;
        }
        containerList = new ContentContainerList(jahiaContainerList.getComp_id().getId(),
                parentContainerID,
                jahiaContainerList.getPageid(),
                jahiaContainerList.getContainerDefinition().getId(),
                jahiaContainerList.getJahiaAclId(),
                activeAndStagedEntry);
        return containerList;
    }

    public Map<String, String> getProperties(int containerListID) {
        return dao.getProperties((containerListID));
    }

    public void setProperties(int containerListID, int jahiaID, Map<String, String> containerProperties) {
        dao.saveProperties((containerListID), containerProperties);
        flushCache(containerListID, jahiaID, "");
    }

    public void flushCache(int id, int siteID, String containerName) {
        Cache cache = listCache;
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_CACHE);
            } catch (JahiaInitializationException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (cache != null) {
            cache.flushGroup(JAHIA_CONTAINER_LIST_CACHE_PREFIX + id);
            cache.remove(CONTENT_CONTAINER_LIST_CACHE_PREFIX + id);
        }
        if (idsCache != null) {
            if (containerName != null) {
                idsCache.flushGroup("ContainerListName_" + containerName);
            } else {
                idsCache.flush();
            }
        }
        cache = cacheService.getCache(JahiaObjectManager.CACHE_NAME);
        if (cache != null) {
            synchronized (cache) {
                cache.remove(JahiaObjectManager.CACHE_KEY_PREFIX + new ContentContainerListKey(id));
                cache.remove(JahiaObjectManager.OBJECTDELEGATE_KEY_PREFIX + new ContentContainerListKey(id));
                cache.remove(ContentContainerListKey.CONTAINERLIST_TYPE);
            }
        }
    }

    public int getIdByPageIdAndDefinitionNameAndParentId(String containerListName, int pageID, int containerParentID) {
        Integer idByPageIdAndDefinitionName = null;
        Set<String> groups = new HashSet<String>();
        groups.add("ContainerListName_" + containerListName);
        groups.add(PAGE_ID_CACHE_PREFIX + (pageID));
        groups.add("ContainerParentID" + containerParentID);
        GroupCacheKey groupCacheKey = new GroupCacheKey("JahiaContainerListIdByNameAndPageId_" + containerListName + pageID + containerParentID,
                groups);
        if (idsCache == null) {
            try {
                idsCache = cacheService.createCacheInstance(JAHIA_CONTAINER_LIST_ID_CACHE);
            } catch (JahiaInitializationException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (idsCache != null) {
            idByPageIdAndDefinitionName = (Integer) idsCache.get(groupCacheKey);
        }
        if (idByPageIdAndDefinitionName == null) {
            idByPageIdAndDefinitionName = dao.getIdByPageIdAndDefinitionNameAndParentID((pageID), containerListName, (containerParentID));
            if (idByPageIdAndDefinitionName != null) {
                if (idsCache != null) {
                    idsCache.put(groupCacheKey, idByPageIdAndDefinitionName);
                }
                return idByPageIdAndDefinitionName;
            } else {
                if (idsCache != null) {
                    idsCache.put(groupCacheKey, (-1));
                }
                return -1;
            }
        }
        return idByPageIdAndDefinitionName;
    }

    public void changeEntryState(ContentContainerList contentContainerList, ContentObjectEntryState entryState, ContentObjectEntryState newEntryState) {
        try {
            JahiaContainerList data = findJahiaContainerList(contentContainerList, entryState);
            if (data != null) {
                JahiaCtnListPK pk = new JahiaCtnListPK((contentContainerList.getID()),
                        (newEntryState.getVersionID()),
                        (newEntryState.getWorkflowState()));
                JahiaContainerList fieldsData = (JahiaContainerList) data.clone();
                dao.deleteContainerList(data);
                fieldsData.setComp_id(pk);
                dao.save(fieldsData);
                flushCache(contentContainerList.getID(),
                        data.getContainerDefinition().getJahiaSiteId(),
                        data.getContainerDefinition().getName());
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Field not found in database ", e);
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaFieldsData", e);
        }
    }

    private JahiaContainerList findJahiaContainerList(ContentContainerList contentContainerList, ContentObjectEntryState entryState) {
        return dao.findById(new JahiaCtnListPK((contentContainerList.getID()),
                (entryState.getVersionID()),
                (entryState.getWorkflowState())));
    }

    public int[] getParentIds(int id, EntryLoadRequest req) {
        Object[] i = dao.getParentIds((id));
        if (i != null) {
            if (i[1] == null) {
                return new int[]{((Integer) i[0]), -1};
            } else {
                return new int[]{((Integer) i[0]), ((Integer) i[1])};
            }
        }
        return new int[]{-1, -1};
    }

    public List<Integer> getContainerListIDsOnPagesHavingAcls(Set<Integer> pageIDs, Set<Integer> aclIDs) {
        return dao.getContainerListIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public List<Integer> getContainerListIDsHavingAcls(Set<Integer> aclIDs) {
        return dao.getContainerListIDsHavingAcls(aclIDs);
    }

    public Map<String, String> getVersions(int site) {
        return dao.getVersions(site);
    }

    public List<Integer> findContainerListIdByPropertyNameAndValue(String name, String value) {
        return dao.findContainerListIdByPropertyNameAndValue(name, value);
    }

    public List<Object[]> getContainerListPropertiesByName(String name) {
        return dao.getContainerListPropertiesByName(name);
    }

    public void removeContainerListProperty(int id, String name) {
        dao.deleteContainerListProperty(id, name);
    }


}
