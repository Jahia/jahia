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
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObject;
import org.jahia.content.CrossReferenceManager;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.*;
import org.jahia.hibernate.model.JahiaCtnEntryPK;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.containers.ContentContainer;
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
 * Time: 14:37:48
 * To change this template use File | Settings | File Templates.
 */
public class JahiaContainerManager {
    // ------------------------------ FIELDS ------------------------------
    public static final String CONTENTCONTAINER_CACHE_NAME = "ContentContainerManagerCache";
    public static final String JAHIACONTAINER_CACHE_NAME = "JahiaContainerManagerCache";
    public static final String CONTAINERIDSLIST_CACHE_NAME = "ContainerIdsListManagerCache";
    public static final String CACHE_KEY_PREFIX = "ContentContainer_";
    public static final String PAGE_CACHE_KEY_PREFIX = "ListContentContainer_";
    private JahiaContainerDAO dao = null;
    private JahiaContainerDefinitionDAO definitionDAO = null;
    private JahiaObjectDAO jahiaObjectDAO = null;
    private JahiaWorkflowDAO jahiaWorkflowDAO = null;

    private WorkflowInstanceDAO workflowInstanceDAO;
    private WorkflowHistoryDAO workflowHistoryDAO;

    private JahiaGroupManager groupManager;

    private Log log = LogFactory.getLog(getClass());
    private CacheService cacheService = null;
    private Cache<String, Object> contentCache;
    private Cache<Object, Object> containerCache;
    private Cache<GroupCacheKey, List<Integer>> idsCache;
// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllContainersIds() {
        return dao.getAllContainerIds();
    }

    public List<Integer> getAllContainersIds(BitSet ids, EntryLoadRequest loadVersion) {
        if (ids == null || ids.length() == 0) {
            return Collections.emptyList();
        } else if (ids.cardinality() == 1) {
            List<Integer> retval = new ArrayList<Integer>();
            retval.add((ids.nextSetBit(0)));
            return retval;
        }

        boolean compareMode = (Jahia.getThreadParamBean() != null
                && ProcessingContext.COMPARE.equals(Jahia.getThreadParamBean().getOpMode()));

        List<Integer> retval;

        if (loadVersion == null || loadVersion.isVersioned() && compareMode) {
            retval = dao.getAllContainerIds(ids);
        } else if (loadVersion.isStaging()) {
            if (loadVersion.isWithMarkedForDeletion()) {
                retval = dao.getAllStagingContainerIds(ids);
            } else {
                retval = dao.getAllNonDeletedStagingContainerIds(ids);
            }
        } else if (loadVersion.isVersioned() && !compareMode) {
            retval = dao.getAllVersionedContainerIds((loadVersion
                    .getVersionID()), ids);
        } else {
            retval = dao.getAllPublishedContainerIds(ids);
        }

        int dbMaxElementsForInClause = org.jahia.settings.SettingsBean.getInstance().getDBMaxElementsForInClause();
        boolean applyBitsetCheck = ids.cardinality() > dbMaxElementsForInClause;

        if (loadVersion != null
                && (loadVersion.isStaging() || (loadVersion.isVersioned() && !compareMode))) {
            Integer previousCtnID = (-2);

            List list = retval;
            for (ListIterator<Object[]> it = list.listIterator(); it.hasNext();) {
                Object[] objects = it.next();
                Integer ctnID = (Integer) objects[0];
                if (previousCtnID.equals(ctnID)) {
                    it.remove();
                } else {
                    previousCtnID = ctnID;
                }
            }

            Collections.sort(list, new Comparator<Object[]>() {
                public int compare(Object[] o1, Object[] o2) {
                    Integer rank1 = (Integer) o1[2];
                    Integer rank2 = (Integer) o2[2];
                    return rank1.compareTo(rank2);
                }
            });

            retval = new ArrayList<Integer>(list.size());
            int index = 0;

            for (Object aList : list) {
                Object[] objects = (Object[]) aList;

                Integer ctnID = (Integer) objects[0];

                if (applyBitsetCheck && !ids.get(ctnID)) {
                    continue;
                }

                if (loadVersion.isWithMarkedForDeletion() || ((Integer) objects[1])
                        != -1) {
                    Integer rank = ((Integer) objects[2]);
                    if (rank != 0) {
                        retval.add(index++, ctnID);
                    } else {
                        retval.add(ctnID);
                    }
                }
            }
        } else {
            if (applyBitsetCheck) {
                List list = retval;
                retval = new ArrayList<Integer>(list.size());

                for (Object aList : list) {
                    Object[] objects = (Object[]) aList;

                    Integer ctnID = (Integer) objects[0];

                    if (ids.get(ctnID)) {
                        retval.add(ctnID);
                    }
                }
            } else {
                List list = retval;
                retval = new ArrayList<Integer>(list.size());
                for (Object aList : list) {
                    retval.add((Integer)((Object[]) aList)[0]);
                }
            }
        }


        return retval;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaContainerDAO(JahiaContainerDAO dao) {
        this.dao = dao;
    }

    public void setJahiaContainerDefinitionDAO(JahiaContainerDefinitionDAO definitionDAO) {
        this.definitionDAO = definitionDAO;
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

    public void copyContainerEntry(int containerId, EntryStateable from, EntryStateable to) {
        try {
            final org.jahia.hibernate.model.JahiaContainer containerById = dao.findContainerById((containerId), (from.getVersionID()), (from.getWorkflowState()));
            org.jahia.hibernate.model.JahiaContainer container = (org.jahia.hibernate.model.JahiaContainer) containerById.clone();
            container.getComp_id().setWorkflowState((to.getWorkflowState()));
            container.getComp_id().setVersionId((to.getVersionID()));
            dao.save(container);
            flushCache(containerId, container.getSiteId(), container.getListid(), container.getPageid());
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Try to delete an unexisting entry " + containerId + " " + from.toString());
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
        }
    }

    /**
     * Delete a container.
     *
     * @param containerId
     * @param saveVersion
     */
    public void deleteContainer(int containerId, JahiaSaveVersion saveVersion) {
        // Load active container
        org.jahia.hibernate.model.JahiaContainer container = null;
        try {
            container = dao.loadPublishedContainer((containerId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (saveVersion.isStaging()) {
            if (container != null &&
                    container.getComp_id().getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                // We have an active version so we create a staged version if none exist
                List<org.jahia.hibernate.model.JahiaContainer> stagedContainers = null;
                try {
                    stagedContainers = dao.loadStagedContainers((containerId));
                } catch (ObjectRetrievalFailureException e) {
                }
                if (stagedContainers == null || stagedContainers.isEmpty()) {
                    org.jahia.hibernate.model.JahiaContainer jahiaContainer = dao.createStagedContainer(container,
                            (saveVersion.getVersionID()),
                            (saveVersion.getWorkflowState()));
                    stagedContainers = new ArrayList<org.jahia.hibernate.model.JahiaContainer>(1);
                    stagedContainers.add(jahiaContainer);
                }
                // we update the container
                for (Object stagedContainer : stagedContainers) {
                    org.jahia.hibernate.model.JahiaContainer jahiaContainer =
                            (org.jahia.hibernate.model.JahiaContainer) stagedContainer;
                    if (jahiaContainer.getComp_id().getVersionId() != -1) {
                        dao.deleteContainer(jahiaContainer);
                        try {
                            org.jahia.hibernate.model.JahiaContainer jahiaContainer2 =
                                    (org.jahia.hibernate.model.JahiaContainer) jahiaContainer.clone();
                            jahiaContainer2.getComp_id().setVersionId((-1));
                            dao.save(jahiaContainer2);
                        } catch (CloneNotSupportedException e) {
                            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                        }
                    }
                }
                flushCache(containerId, 0, container != null ? container.getListid() : 0, container != null ? container.getPageid() : 0);
            } else {
                List<org.jahia.hibernate.model.JahiaContainer> stagedContainers = null;
                try {
                    stagedContainers = dao.loadStagedContainers((containerId));
                } catch (ObjectRetrievalFailureException e) {
                }
                dao.deleteContainers(stagedContainers);
                if (!stagedContainers.isEmpty()) {
                    container = stagedContainers.get(0);
                    flushCache(containerId, 0, container != null ? container.getListid() : 0, container != null ? container.getPageid() : 0);
                }
            }
        } else if (saveVersion.isVersioned()) {
            dao.backupContainer((containerId));
            dao.deleteContainer(container);
            try {
                org.jahia.hibernate.model.JahiaContainer jahiaContainer =
                        (org.jahia.hibernate.model.JahiaContainer) container.clone();
                jahiaContainer.getComp_id().setWorkflowState((-1));
                jahiaContainer.getComp_id().setVersionId((saveVersion.getVersionID()));
                dao.save(container);
                flushCache(containerId, 0, container != null ? container.getListid() : 0, container != null ? container.getPageid() : 0);
            } catch (CloneNotSupportedException e) {
                log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
            }
        } else {
            dao.deleteContainer(container);
            flushCache(containerId, 0, container != null ? container.getListid() : 0, container != null ? container.getPageid() : 0);
        }
        // remove all links for page if no pages exist at all with this id
        if (dao.getNBContainer(containerId) == 0) {
            deleteContainerReferences(containerId);
        }
    }

    public void deleteContainerEntry(int containerId, EntryStateable request) {
        try {
            org.jahia.hibernate.model.JahiaContainer containerById = dao.findContainerById((containerId), (request.getVersionID()),
                    (request.getWorkflowState()));
            dao.deleteContainer(containerById);
            flushCache(containerId, containerById.getSiteId(), containerById.getListid(), containerById.getPageid());
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Try to delete an unexisting entry " + containerId + " " + request.toString());
        }
        // remove all links for page if no pages exist at all with this id
        if (dao.getNBContainer(containerId) == 0) {
            deleteContainerReferences(containerId);
        }
    }

    private void deleteContainerReferences(int containerId) {
        try {
            jahiaObjectDAO.delete(new JahiaObjectPK(ContentContainerKey.CONTAINER_TYPE, (containerId)));
            String key = ContentContainerKey.toObjectKeyString(containerId);
            jahiaWorkflowDAO.delete(key);
            workflowHistoryDAO.removeWorkflowHistory(key);
            workflowInstanceDAO.removeWorkflowInstance(key);
            groupManager.searchAndDelete("workflowrole_" + key+"_%", null);
            dao.deleteProperties((containerId));
            CrossReferenceManager.getInstance().removeAllObjectXRefs(new ContentContainerKey(containerId));
        } catch (JahiaException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> getAllContainerFromSite(int siteID) {
        return dao.getAllContainerIdsFromSite((siteID));
    }

    public Map<Integer, Integer> getAllContainersAclid(int listId) {
        return dao.getAllContainerAclIdsForContainerList((listId));
    }

    public ContentContainer getContainer(int containerId) {
        ContentContainer resultContainer = null;
        if (contentCache == null) {
            try {
                contentCache = cacheService.createCacheInstance(CONTENTCONTAINER_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.warn("Error could not initialize cache for " + CONTENTCONTAINER_CACHE_NAME, e);
            }
        }
        if (contentCache != null) {
            resultContainer = (ContentContainer) contentCache.get(CACHE_KEY_PREFIX + containerId);
            if (resultContainer != null) return resultContainer;
        }
        // try to get all active or staged version
        List<org.jahia.hibernate.model.JahiaContainer> list = dao.loadStagingContainers((containerId));
        if (!list.isEmpty()) {
            org.jahia.hibernate.model.JahiaContainer jahiaContainer = list.get(0);
            List<ContentObjectEntryState> activeAndStagedEntry = new ArrayList<ContentObjectEntryState>(list.size());
            for (org.jahia.hibernate.model.JahiaContainer container : list) {
                activeAndStagedEntry.add(new ContentObjectEntryState(container.getComp_id().getWorkflowState(),
                        container.getComp_id().getVersionId(),
                        ContentObject.SHARED_LANGUAGE));
            }
            resultContainer = convertToContentContainer(jahiaContainer, activeAndStagedEntry);
        } else {
            list = dao.loadAllInactiveVersionedContainers((containerId));
            if (!list.isEmpty()) {
                org.jahia.hibernate.model.JahiaContainer jahiaContainer = list.get(0);
                resultContainer = convertToContentContainer(jahiaContainer, new ArrayList<ContentObjectEntryState>());
            }
        }
        if (contentCache != null) {
            contentCache.put(CACHE_KEY_PREFIX + containerId, resultContainer);
        }
        return resultContainer;
    }

    public void updateContainerAclId(int containerId, int aclId) {
        List<org.jahia.hibernate.model.JahiaContainer> list = dao.loadStagingContainers((containerId));
        for (org.jahia.hibernate.model.JahiaContainer jahiaContainer : list) {
            jahiaContainer.setJahiaAclId((aclId));
            dao.save(jahiaContainer);
            flushCache(jahiaContainer.getComp_id().getId(), jahiaContainer.getSiteId(), jahiaContainer.getListid(), jahiaContainer.getPageid());
        }
    }

    public int getContainerACLID(int i) {
        ContentContainer contentContainer = getContainer(i);
        if (contentContainer != null) {
            return contentContainer.getAclID();
        }
        return -1;
    }

    public List<Integer> getContainerIdsInContainerList(int listID,
                                               EntryLoadRequest loadVersion, boolean fromAllLists) {

        boolean compareMode = (Jahia.getThreadParamBean() != null && ProcessingContext.COMPARE
                .equals(Jahia.getThreadParamBean().getOpMode()));

        List<Integer> retval = null;
        GroupCacheKey cacheKey = CacheAdvice.toGroupCacheKey(new Object[]{
                PAGE_CACHE_KEY_PREFIX + listID,
                this.getCtnIDsByCtnListCacheKey(listID, loadVersion),
                fromAllLists});
        Cache<GroupCacheKey, List<Integer>> cache = getListIdsCache();
        if (!compareMode
                && cache != null
                && !(loadVersion != null && loadVersion.getWorkflowState() <= EntryLoadRequest.VERSIONED_WORKFLOW_STATE)) {
            retval = (List<Integer>) cache.get(cacheKey);
        }
        if (retval == null) {
            if (fromAllLists) {
                if (loadVersion == null || loadVersion.isVersioned()
                        && compareMode) {
                    retval = dao.getAllContainerIds();
                } else if (loadVersion.isStaging()) {
                    if (loadVersion.isWithMarkedForDeletion()) {
                        retval = dao.getAllStagingContainerIds();
                    } else {
                        retval = dao.getAllNonDeletedStagingContainerIds();
                    }
                } else if (loadVersion.isVersioned() && !compareMode) {
                    retval = dao.getAllVersionedContainerIds((
                            loadVersion.getVersionID()));
                } else {
                    retval = dao.getAllPublishedContainerIds();
                }
            } else {
                Integer id = (listID);
                if (loadVersion == null || loadVersion.isVersioned()
                        && compareMode) {
                    retval = dao.getAllContainerIdsFromList(id);
                } else if (loadVersion.isStaging()) {
                    if (loadVersion.isWithMarkedForDeletion()) {
                        retval = dao.getStagingContainerIdsFromList(id);
                    } else {
                        retval = dao
                                .getNonDeletedStagingContainerIdsFromList(id);
                    }
                } else if (loadVersion.isVersioned() && !compareMode) {
                    retval = dao.getVersionedContainerIdsFromList(id, (
                            loadVersion.getVersionID()));
                } else {
                    retval = dao.getPublishedContainerIdsFromList(id);
                }
            }
            if (loadVersion != null
                    && (loadVersion.isStaging() || (loadVersion.isVersioned() && !compareMode))) {
                Integer previousCtnID = (-2);

                List list = retval;
                for (ListIterator<Object[]> it = list.listIterator(); it.hasNext();) {
                    Object[] objects = (Object[]) it.next();
                    Integer ctnID = (Integer) objects[0];
                    if (previousCtnID.equals(ctnID)) {
                        it.remove();
                    } else {
                        previousCtnID = ctnID;
                    }
                }
                Collections.sort(list, new Comparator<Object[]>() {
                    public int compare(Object[] o1, Object[] o2) {
                        Integer rank1 = (Integer) o1[2];
                        Integer rank2 = (Integer) o2[2];
                        return rank1.compareTo(rank2);
                    }
                });
                retval = new ArrayList<Integer>(list.size());
                int index = 0;
                for (Object aList : list) {
                    Object[] objects = (Object[]) aList;
                    Integer ctnID = (Integer) objects[0];
                    if (loadVersion.isWithMarkedForDeletion()
                            || ((Integer) objects[1]) != -1) {
                        Integer rank = (Integer) objects[2];
                        if (rank != 0) {
                            retval.add(index++, ctnID);
                        } else {
                            retval.add(ctnID);
                        }
                    }
                }
            }
            if (!compareMode
                    && cache != null
                    && !(loadVersion != null && loadVersion.getWorkflowState() <= EntryLoadRequest.VERSIONED_WORKFLOW_STATE)) {
                cache.put(cacheKey, retval);
            }
        }
        return retval;
    }

    public List<Integer> getContainerIdsInContainerListSortedByFieldValue(int listID, String fieldName, boolean asc,
                                                                 EntryLoadRequest loadVersion) {
        List<Integer> retVal = null;
        try {
            if (loadVersion.isStaging()) {
                retVal = dao.getAllContainerIdsInListSortedByFieldValue((listID), fieldName, asc);
            } else {
                retVal = dao.getPublishedContainerIdsInListSortedByFieldValue((listID), fieldName, asc);
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("ContainerIds Not Found for fieldName " + fieldName + " in list " + listID, e);
        }
        return retVal;
    }

    public List<Integer> getDeletedContainerIdsInContainerList(int listID) {
        return dao.getDeletedContainerIdsFromList((listID));
    }

    public List<Integer> getPublishedContainerInPage(int pageID) {
        return dao.getPublishedContainerIdsFromPage((pageID));
    }

    public List<Integer> getStagedContainerInPage(int pageID) {
        return dao.getStagedContainerInPage((pageID));
    }

    public List<Integer> getDeletedContainerIdsBySiteAndCtnDef(Integer siteId,
                                                               String ctnDefName) {
        return dao.getDeletedContainerIdsBySiteAndCtnDef(siteId, ctnDefName);
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
            retList.add(new ContentObjectEntryState((Integer) objects[0],
                    (Integer) objects[1],
                    ContentObject.SHARED_LANGUAGE));
        }
        return retList;
    }

    public List<JahiaContainer> loadAllActiveContainerInPage(int pageId) {
        List<JahiaContainer> retVal = null;
        Cache<Object, Object> cache = getJahiaContainerCache();
        if (cache != null) {
            retVal = (List<JahiaContainer>) cache.get(PAGE_CACHE_KEY_PREFIX + pageId);
        }
        if (retVal == null) {
            try {
                List<org.jahia.hibernate.model.JahiaContainer> containers = dao.findContainers(pageId);
                retVal = new ArrayList<JahiaContainer>(containers.size());
                for (org.jahia.hibernate.model.JahiaContainer container : containers) {
                    JahiaContainer o = convertToOldJahiaContainer(container);
                    if (cache != null) {
                        cache.put(CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_PREFIX + o.getID(), EntryLoadRequest.CURRENT}), o);
                    }
                    retVal.add(o);
                }
                if (cache != null && !retVal.isEmpty()) {
                    cache.put(PAGE_CACHE_KEY_PREFIX + pageId, retVal);
                }
            } catch (ObjectRetrievalFailureException e) {
                log.warn("Containers can not be found in page " + pageId, e);
            }
        }
        return retVal;
    }

    public Map<Integer, List<Integer>> loadAllContainersFromContainerListFromPage(int pageID) {
        return dao.getAllContainerIdsFromContainerListForPage((pageID));
    }

    /**
     * loadRequest.isCurrent() == true or loadRequest is null :
     * return live container only
     * loadRequest.isStaging() == true :
     * return staging if exists or live if staging does not exist.
     * But if marked for delete present, return null unless loadRequest.isWithDeleted() == true
     * loadRequest.isVersioned() == true :
     * return versioned entry, but if deleted and loadRequest.isWithDeleted() == false, return null.
     * <p/>
     * COMPARE MODE behavior :
     * if Jahia.getThreadParamBean().getOpMode() is equals to ProcessingContext.COMPARE
     * then :
     * containers currently deleted and that won't be restored if restoring are not returned.
     * containers currently deleted but that will be restored if restoring are returned
     *
     * @param containerId
     * @param request
     * @return
     */
    public JahiaContainer loadContainer(int containerId, EntryLoadRequest request) {
        return loadContainer(containerId, request, true);
    }

    private JahiaContainer loadContainer(int containerId, EntryLoadRequest request, boolean checkCompareMode) {
        return loadContainer(containerId, request, checkCompareMode, org.jahia.settings.SettingsBean.getInstance().isBatchLoadingEnabled());
    }

    /**
     * loadRequest.isCurrent() == true or loadRequest is null :
     * return live container only
     * loadRequest.isStaging() == true :
     * return staging if exists or live if staging does not exist.
     * But if marked for delete present, return null unless loadRequest.isWithDeleted() == true
     * loadRequest.isVersioned() == true :
     * return versioned entry, but if deleted and loadRequest.isWithDeleted() == false, return null.
     * <p/>
     * COMPARE MODE behavior :
     * if Jahia.getThreadParamBean().getOpMode() is equals to ProcessingContext.COMPARE
     * then :
     * containers currently deleted and that won't be restored if restoring are not returned.
     * containers currently deleted but that will be restored if restoring are returned
     *
     * @param containerId
     * @param request
     * @param checkCompareMode
     * @return
     */
    private JahiaContainer loadContainer(int containerId, EntryLoadRequest request, boolean checkCompareMode,
                                         boolean enableBatchLoading) {

        JahiaContainer jahiaContainer = null;
        boolean compareMode = (Jahia.getThreadParamBean() != null
                && ProcessingContext.COMPARE.equals(Jahia.getThreadParamBean().getOpMode()));

        Cache<Object, Object> cache = getJahiaContainerCache();
        final GroupCacheKey key = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_PREFIX + containerId, getCtnCacheKey(containerId, request)});

        if (cache != null && request != null && !request.isVersioned() && !request.isDeleted()) {
            jahiaContainer = (JahiaContainer) cache.get(key);
            if (jahiaContainer != null && !(checkCompareMode && compareMode)) {
                return (JahiaContainer) jahiaContainer.clone();
            }
        }

        int diffVersionID = 0;
        if (Jahia.getThreadParamBean() != null) {
            diffVersionID = Jahia.getThreadParamBean().getDiffVersionID();
        }

        if (jahiaContainer == null) {
            try {
                if (request == null) {
                    jahiaContainer = convertToOldJahiaContainer(dao.loadPublishedContainer((containerId)));
                } else if (request.isCurrent()) {
                    //jahiaContainer = convertToOldJahiaContainer(dao.loadPublishedContainer((containerId)));
                    if (cache == null) {
                        jahiaContainer = convertToOldJahiaContainer(dao.loadPublishedContainer((containerId)));
                    } else {
                        if (enableBatchLoading) {
                            jahiaContainer = handleBatchLoading(containerId,
                                    dao.loadPublishedContainer((containerId),
                                            org.jahia.settings.SettingsBean.getInstance().getBatchLoadingSize()), cache, request,
                                    checkCompareMode, compareMode);
                        } else {
                            jahiaContainer = convertToOldJahiaContainer(dao.loadPublishedContainer((containerId)));
                        }
                    }
                } else if (request.isStaging()) {
                    /*
                    jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                            request));*/
                    if (cache == null) {
                        jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                                request));
                    } else {
                        /*
                        jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                                request));*/
                        if (enableBatchLoading) {
                            jahiaContainer = handleBatchLoading(containerId,
                                    dao.loadStagingContainer((containerId), request,
                                            org.jahia.settings.SettingsBean.getInstance().getBatchLoadingSize()), cache, request,
                                    checkCompareMode, compareMode);
                        } else {
                            jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                                    request));
                        }
                    }
                } else if (request.isDeleted()) {
                    jahiaContainer = convertToOldJahiaContainer(dao.loadDeletedContainer((containerId)));
                } else if (request.isVersioned()) {
                    jahiaContainer = convertToOldJahiaContainer(dao.loadVersionedContainer((containerId),
                            request));
                }
            } catch (ObjectRetrievalFailureException e) {
                log.warn("Container not found for id " + containerId, e);
            }
        }
        if (cache != null && request != null && !request.isVersioned() && !request.isDeleted() && jahiaContainer != null
                && jahiaContainer.getWorkflowState() >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                && !(checkCompareMode && compareMode)) {
            cache.put(key, jahiaContainer);
        }
        if (jahiaContainer != null && checkCompareMode
                && jahiaContainer.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
            if (compareMode
                    && (jahiaContainer.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE
                    || jahiaContainer.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                EntryLoadRequest staged = (EntryLoadRequest) EntryLoadRequest.STAGED.clone();
                staged.setWithDeleted(true);

                if (diffVersionID > 2) {
                    if (request != null) {
                        staged = new EntryLoadRequest(0, diffVersionID, request.getLocales());
                        staged.setWithDeleted(false);
                        staged.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                    }
                }
                JahiaContainer container = this.loadContainer(containerId, staged, false);
                if (container == null
                        || (container.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE
                        && container.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                    jahiaContainer = null;
                }
            }
        } else if (jahiaContainer == null && checkCompareMode) {
            if (compareMode) {
                EntryLoadRequest staged = (EntryLoadRequest) EntryLoadRequest.STAGED.clone();
                staged.setWithDeleted(true);
                if (diffVersionID > 2) {
                    if (request != null) {
                        staged = new EntryLoadRequest(0, diffVersionID, request.getLocales());
                        staged.setWithDeleted(false);
                        staged.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
                    }
                }
                JahiaContainer container = this.loadContainer(containerId, staged, false);
                if (container != null
                        && !(container.getVersionID() == -1 || container.getWorkflowState() == -1)) {
                    jahiaContainer = container;
                }
            }
        }
        if (jahiaContainer != null) {
            return (JahiaContainer) jahiaContainer.clone();
        } else return null;
    }

    /**
     * @param ids
     * @param request
     * @return
     */
    public Map<Integer, JahiaContainer> loadContainers(Collection<Integer> ids, EntryLoadRequest request) {
        return loadContainers(ids, request, true);
    }

    /**
     * @param ids
     * @param request
     * @param checkCompareMode
     * @return
     */
    private Map<Integer, JahiaContainer> loadContainers(Collection<Integer> ids, EntryLoadRequest request, boolean checkCompareMode) {

        Map<Integer, JahiaContainer> containers = new HashMap<Integer, JahiaContainer>();
        Iterator<Integer> it = ids.iterator();
        Integer id;
        List<Integer> idList = new ArrayList<Integer>();
        int batchSize = 16;
        Cache<Object, Object> cache = getJahiaContainerCache();
        JahiaContainer jahiaContainer = null;
        boolean compareMode = (Jahia.getThreadParamBean() != null
                && ProcessingContext.COMPARE.equals(Jahia.getThreadParamBean().getOpMode()));
        GroupCacheKey key;

        int diffVersionID = Jahia.getThreadParamBean() != null ? Jahia
                .getThreadParamBean().getDiffVersionID() : 0;

        while (it.hasNext()) {
            id = (Integer) it.next();
            key = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_PREFIX + id,
                    getCtnCacheKey(id, request)});
            if (cache != null && request != null && !request.isVersioned() && !request.isDeleted()) {
                jahiaContainer = (JahiaContainer) cache.get(key);
                if (jahiaContainer != null && !(checkCompareMode && compareMode)) {
                    containers.put(id, (JahiaContainer)jahiaContainer.clone());
                }
            }
            if (jahiaContainer == null) {
                idList.add(id);
            }
            if (idList.size() == batchSize || (!it.hasNext() && !idList.isEmpty())) {
                List<JahiaContainer> containersList = null;
                try {
                    if (request == null) {
                        containersList = handleBatchLoading(dao.loadPublishedContainer(idList), cache, request,
                                checkCompareMode, compareMode);
                    } else if (request.isCurrent()) {
                        //jahiaContainer = convertToOldJahiaContainer(dao.loadPublishedContainer((containerId)));
                        if (cache == null) {
                            containersList = handleBatchLoading(dao.loadPublishedContainer(idList), cache, request,
                                    checkCompareMode, compareMode);
                        } else {
                            containersList = handleBatchLoading(dao.loadPublishedContainer(idList), cache, request,
                                    checkCompareMode, compareMode);
                        }
                    } else if (request.isStaging()) {
                        /*
                        jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                                request));*/
                        if (cache == null) {
                            containersList = handleBatchLoading(dao.loadStagingContainer(idList, request),
                                    cache, request, checkCompareMode, compareMode);
                        } else {
                            /*
                            jahiaContainer = convertToOldJahiaContainer(dao.loadStagingContainer((containerId),
                                    request));*/
                            containersList = handleBatchLoading(dao.loadStagingContainer(idList, request), cache, request,
                                    checkCompareMode, compareMode);
                        }
                    } else if (request.isDeleted()) {
                        containersList = handleBatchLoading(dao.loadDeletedContainer(idList), cache, request,
                                checkCompareMode, compareMode);
                    } else if (request.isVersioned()) {
                        containersList = handleBatchLoading(dao.loadVersionedContainer(idList, request), cache, request,
                                checkCompareMode, compareMode);
                    }
                } catch (ObjectRetrievalFailureException e) {
                    log.warn("Error retrieving containers", e);
                }
                idList.clear();
                if (containersList != null && !containersList.isEmpty()) {
                    for (Object aContainersList : containersList) {
                        jahiaContainer = (JahiaContainer) aContainersList;
                        containers.put((jahiaContainer.getID()), jahiaContainer);
                    }
                }
            }
        }
        it = ids.iterator();

        while (it.hasNext()) {
            id = (Integer) it.next();
            jahiaContainer = containers.get(id);
            if (jahiaContainer != null && checkCompareMode
                    && jahiaContainer.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                if (compareMode
                        && (jahiaContainer.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE
                        || jahiaContainer.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                    EntryLoadRequest staged = (EntryLoadRequest) EntryLoadRequest.STAGED.clone();
                    staged.setWithDeleted(true);
                    if (diffVersionID > 2) {
                        if (request != null) {
                            staged = new EntryLoadRequest(0, diffVersionID, request.getLocales());
                            staged.setWithDeleted(false);
                            staged.setWithMarkedForDeletion(false);
                        }
                    }
                    JahiaContainer container = this.loadContainer(id, staged, false,
                            org.jahia.settings.SettingsBean.getInstance().isBatchLoadingEnabled());
                    if (container == null
                            || (container.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE
                            && container.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                        jahiaContainer = null;
                    }
                }
                if (jahiaContainer == null) {
                    containers.remove(id);
                }
            } else if (jahiaContainer == null && checkCompareMode) {
                if (compareMode) {
                    EntryLoadRequest staged = (EntryLoadRequest) EntryLoadRequest.STAGED.clone();
                    staged.setWithDeleted(true);
                    if (diffVersionID > 2) {
                        if (request != null) {
                            staged = new EntryLoadRequest(0, diffVersionID, request.getLocales());
                            staged.setWithDeleted(false);
                            staged.setWithMarkedForDeletion(false);
                        }
                    }
                    JahiaContainer container = this.loadContainer(id, staged, false,
                            org.jahia.settings.SettingsBean.getInstance().isBatchLoadingEnabled());
                    if (container != null
                            && !(container.getVersionID() == -1 || container.getWorkflowState() == -1)) {
                        containers.put(id, container);
                    }
                }
            }
        }
        return containers;
    }


    private JahiaContainer handleBatchLoading(int containerId, List<org.jahia.hibernate.model.JahiaContainer> containers,
                                              Cache<Object, Object> cache, EntryLoadRequest request,
                                              boolean checkCompareMode, boolean compareMode) {
        JahiaContainer jahiaContainer = null;
        JahiaContainer container;
        GroupCacheKey cacheKey;
        for (org.jahia.hibernate.model.JahiaContainer containerModel : containers) {
            container = convertToOldJahiaContainer(containerModel);
            if (container.getID() == containerId) {
                jahiaContainer = container;
            } else if (cache != null && request != null && !request.isVersioned() && !request.isDeleted()
                    && container.getWorkflowState() >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                    && !(checkCompareMode && compareMode)) {
                cacheKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_PREFIX + container.getID(),
                        getCtnCacheKey(container.getID(), request)});
                cache.put(cacheKey, container);
            }
        }
        return jahiaContainer;
    }

    private List<JahiaContainer> handleBatchLoading(List<org.jahia.hibernate.model.JahiaContainer> containers, Cache<Object, Object> cache, EntryLoadRequest request,
                                    boolean checkCompareMode, boolean compareMode) {
        List<JahiaContainer> result = new ArrayList<JahiaContainer>();
        for (org.jahia.hibernate.model.JahiaContainer containerModel : containers) {
            JahiaContainer container = convertToOldJahiaContainer((org.jahia.hibernate.model.JahiaContainer) containerModel);
            if (cache != null && request != null && !request.isVersioned() && !request.isDeleted()
                    && container.getWorkflowState() >= EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                    && !(checkCompareMode && compareMode)) {
                GroupCacheKey cacheKey = CacheAdvice.toGroupCacheKey(new Object[]{CACHE_KEY_PREFIX + container.getID(),
                        getCtnCacheKey(container.getID(), request)});
                cache.put(cacheKey, container);
            }
            result.add(container);
        }
        return result;
    }

    private Cache<Object, Object> getJahiaContainerCache() {
        Cache<Object, Object> cache = containerCache;
        if (cache == null) {
            try {
                containerCache = cacheService.createCacheInstance(JAHIACONTAINER_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.warn("Error could not initialize cache for " + JAHIACONTAINER_CACHE_NAME, e);
            }
            cache = containerCache;
        }
        return cache;
    }

    private Cache<GroupCacheKey, List<Integer>> getListIdsCache() {
        Cache<GroupCacheKey, List<Integer>> cache = idsCache;
        if (cache == null) {
            try {
                idsCache = cacheService.createCacheInstance(CONTAINERIDSLIST_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.warn("Error could not initialize cache for " + CONTAINERIDSLIST_CACHE_NAME, e);
            }
            cache = idsCache;
        }
        return cache;
    }

    public void purgeContainer(int id) {
        org.jahia.hibernate.model.JahiaContainer container = dao.loadContainer((id));
        dao.deleteAllEntriesForContainerId((id));
        flushCache(id, container.getSiteId(), container.getListid(), container.getPageid());
    }

    public JahiaContainer saveContainer(JahiaContainer jahiaContainer, JahiaSaveVersion saveVersion) {
        org.jahia.hibernate.model.JahiaContainer container = new org.jahia.hibernate.model.JahiaContainer();
        container.setJahiaAclId((jahiaContainer.getAclID()));
        container.setCtndef(definitionDAO.findDefinitionById((jahiaContainer.getctndefid())));
        container.setListid((jahiaContainer.getListID()));
        container.setSiteId((jahiaContainer.getSiteID()));
        container.setPageid((jahiaContainer.getPageID()));
        if (jahiaContainer.getRank() == 0) {
            container.setRank((dao.getMaxRankingValue() + 1));
        } else {
            container.setRank((jahiaContainer.getRank()));
        }
        JahiaCtnEntryPK pk = new JahiaCtnEntryPK();
        final int workflowState = saveVersion.getWorkflowState();
        pk.setWorkflowState((workflowState));
        if (workflowState > 1) {
            pk.setVersionId((0));
        } else {
            pk.setVersionId((saveVersion.getVersionID()));
        }
        container.setComp_id(pk);
        boolean isNew = (container.getComp_id().getId() == null);
        dao.save(container);
        dao.saveProperties(container.getComp_id().getId(), container.getSiteId(), jahiaContainer.getProperties());
        jahiaContainer.setID(container.getComp_id().getId());
        if (isNew) {
            jahiaObjectDAO.create(ContentContainerKey.CONTAINER_TYPE, (jahiaContainer.getID()), (jahiaContainer.getSiteID()));
        }
        flushCache(jahiaContainer.getID(), jahiaContainer.getSiteID(), jahiaContainer.getListID(), jahiaContainer.getPageID());
        return jahiaContainer;
    }

    public JahiaContainer updateContainer(JahiaContainer jahiaContainer, JahiaSaveVersion saveVersion) {
        JahiaCtnEntryPK pk = new JahiaCtnEntryPK();
        if (saveVersion.isStaging()) {
            pk.setWorkflowState((saveVersion.getWorkflowState()));
            pk.setVersionId((0));
        } else {
            if (saveVersion.isVersioned()) {
                dao.backupContainer((jahiaContainer.getID()));
            }
            pk.setWorkflowState((1));
            pk.setVersionId((saveVersion.getVersionID()));

        }
        pk.setId((jahiaContainer.getID()));
        org.jahia.hibernate.model.JahiaContainer container;
        container = new org.jahia.hibernate.model.JahiaContainer();
        container.setComp_id(pk);
        container.setJahiaAclId((jahiaContainer.getAclID()));
        container.setCtndef(definitionDAO.findDefinitionById((jahiaContainer.getctndefid())));
        container.setListid((jahiaContainer.getListID()));
        container.setSiteId((jahiaContainer.getJahiaID()));
        container.setPageid((jahiaContainer.getPageID()));
        container.setRank((jahiaContainer.getRank()));
        dao.save(container);
        dao.saveProperties(container.getComp_id().getId(), container.getSiteId(), jahiaContainer.getProperties());
        jahiaContainer.setID(container.getComp_id().getId());
        flushCache(jahiaContainer.getID(), jahiaContainer.getSiteID(), jahiaContainer.getListID(), jahiaContainer.getPageID());
        return jahiaContainer;
    }

    public void validateStagedContainer(int containerId, JahiaSaveVersion saveVersion) {
        org.jahia.hibernate.model.JahiaContainer container = null;
        try {
            container = dao.loadStagedContainer((containerId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (container != null) {
            org.jahia.hibernate.model.JahiaContainer publishedContainer = dao.loadPublishedContainer((containerId));

            int versionID = saveVersion.getVersionID();
            boolean isDeleted = container.getComp_id().getVersionId() == -1;

            if ((publishedContainer != null) && (publishedContainer.getComp_id().getVersionId() == versionID) && !isDeleted) {
                try {
                    org.jahia.hibernate.model.JahiaContainer jahiaContainer = (org.jahia.hibernate.model.JahiaContainer) container.clone();
                    jahiaContainer.getComp_id().setVersionId((versionID));
                    jahiaContainer.getComp_id().setWorkflowState((1));
                    dao.mergeVersion(jahiaContainer);
                } catch (CloneNotSupportedException e) {
                    log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                }
            } else {
                // We have something to validate
                if (saveVersion.isVersioned()) {
                    try {
                        if (publishedContainer != null && !publishedContainer.getComp_id().getVersionId().equals((versionID))) {
                            org.jahia.hibernate.model.JahiaContainer jahiaContainer = (org.jahia.hibernate.model.JahiaContainer) publishedContainer.clone();
                            jahiaContainer.getComp_id().setWorkflowState((0));
                            dao.saveNewVersion(jahiaContainer);
                        }
                    } catch (CloneNotSupportedException e) {
                        log.error("Could not clone JahiaContainer");
                    }
                }
                // Is it a delete
                if (publishedContainer != null) {
                    dao.deleteContainer(publishedContainer);
                }
                if (isDeleted) {
                    if (saveVersion.isVersioned()) {
                        try {
                            org.jahia.hibernate.model.JahiaContainer jahiaContainer = (org.jahia.hibernate.model.JahiaContainer) container.clone();
                            jahiaContainer.getComp_id().setVersionId((saveVersion.getVersionID()));
                            jahiaContainer.getComp_id().setWorkflowState((-1));
                            dao.saveNewVersion(jahiaContainer);
                        } catch (CloneNotSupportedException e) {
                            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                        }
                    }
                } else {
                    try {
                        org.jahia.hibernate.model.JahiaContainer jahiaContainer = (org.jahia.hibernate.model.JahiaContainer) container.clone();
                        jahiaContainer.getComp_id().setVersionId((saveVersion.getVersionID()));
                        jahiaContainer.getComp_id().setWorkflowState((1));
                        dao.save(jahiaContainer);
                    } catch (CloneNotSupportedException e) {
                        log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                    }
                }
            }

            dao.deleteContainer(container);

            flushCache(containerId, container.getSiteId(), container.getListid(), container.getPageid());
        }
    }

    public void putToWaitingStateContainer(int containerId, int newWorkflowState, int versionID) {
        org.jahia.hibernate.model.JahiaContainer container = null;
        try {
            container = dao.loadStagedContainer((containerId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (container != null) {
            dao.deleteContainers(dao.loadStagedContainers((containerId)));
            try {
                org.jahia.hibernate.model.JahiaContainer jahiaContainer = (org.jahia.hibernate.model.JahiaContainer) container.clone();
                jahiaContainer.getComp_id().setVersionId((versionID));
                jahiaContainer.getComp_id().setWorkflowState((newWorkflowState));
                dao.save(jahiaContainer);
            } catch (CloneNotSupportedException e) {
                log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
            }
            flushCache(containerId, container.getSiteId(), container.getListid(), container.getPageid());
        }
    }

    private ContentContainer convertToContentContainer(org.jahia.hibernate.model.JahiaContainer jahiaContainer,
                                                       List<ContentObjectEntryState> activeAndStagedEntry) {
        ContentContainer resultContainer;
        resultContainer = new ContentContainer(jahiaContainer.getComp_id().getId(), jahiaContainer.getSiteId(), jahiaContainer.getPageid(),
                jahiaContainer.getCtndef().getId(), jahiaContainer.getListid(), jahiaContainer.getJahiaAclId(),
                activeAndStagedEntry);
        return resultContainer;
    }

    private JahiaContainer convertToOldJahiaContainer(org.jahia.hibernate.model.JahiaContainer container) {
        JahiaContainer jahiaContainer = null;
        if (container != null) {
            jahiaContainer = new JahiaContainer(container.getComp_id().getId(),
                    container.getSiteId(),
                    container.getPageid(),
                    container.getListid(),
                    container.getRank(),
                    container.getJahiaAclId(),
                    container.getCtndef().getId(),
                    container.getComp_id().getVersionId(),
                    container.getComp_id().getWorkflowState());
        }
        return jahiaContainer;
    }

    public Map<String, String> getProperties(int containerID) {
        return dao.getProperties((containerID));
    }

    public void setProperties(int containerID, int jahiaID, Map<String, String> containerProperties) {
        dao.saveProperties((containerID), (jahiaID), containerProperties);
    }

    public void flushCache(int id, int siteID, int listId, int pageId) {
        Cache<String, Object> cache = contentCache;
        if (cache != null) {
            cache.remove(CACHE_KEY_PREFIX + id);
        }
        Cache<Object, Object> containerCache = getJahiaContainerCache();
        if (containerCache != null) {
            containerCache.flushGroup(CACHE_KEY_PREFIX + id);
            if (pageId > 0) {
                containerCache.remove(PAGE_CACHE_KEY_PREFIX + pageId);
            }
        }
        Cache<GroupCacheKey, List<Integer>> idsCache = getListIdsCache();
        if (idsCache != null) {
            idsCache.flushGroup(PAGE_CACHE_KEY_PREFIX + listId);
            idsCache.flushGroup(PAGE_CACHE_KEY_PREFIX + -1);
        }
        cache = cacheService.getCache(JahiaContainerListManager.JAHIA_CONTAINER_LIST_CACHE);
        if (cache != null) {
            if (listId > 0) {
                cache.flushGroup(JahiaContainerListManager.JAHIA_CONTAINER_LIST_CACHE_PREFIX + listId);
                cache.remove(JahiaContainerListManager.CONTENT_CONTAINER_LIST_CACHE_PREFIX + listId);
            } else {
                cache.flush(true);
            }
        }
        cache = cacheService.getCache(JahiaContainerListManager.JAHIA_CONTAINER_LIST_ID_CACHE);
        if (cache != null) {
            if (pageId > 0) {
                cache.flushGroup(JahiaContainerListManager.PAGE_ID_CACHE_PREFIX + pageId);
            } else {
                cache.flush(true);
            }
        }
        cache = cacheService.getCache(JahiaObjectManager.CACHE_NAME);
        if (cache != null) {
            synchronized (cache) {
                cache.remove(JahiaObjectManager.CACHE_KEY_PREFIX + new ContentContainerKey(id));
                cache.remove(JahiaObjectManager.OBJECTDELEGATE_KEY_PREFIX + new ContentContainerKey(id));
                cache.remove(ContentContainerKey.CONTAINER_TYPE);
            }
        }
    }

    private String getCtnIDsByCtnListCacheKey(int listID,
                                              EntryLoadRequest loadRequest) {
        StringBuffer buff = new StringBuffer(String.valueOf(listID));
        if (loadRequest != null) {
            buff.append("_");
            buff.append(loadRequest.getWorkflowState());
            buff.append("_");
            buff.append(loadRequest.isWithMarkedForDeletion() ? "yes" : "no");
        }
        return buff.toString();
    }

    private String getCtnCacheKey(int ctnID, EntryLoadRequest loadRequest) {
        StringBuffer buff = new StringBuffer(String.valueOf(ctnID));
        if (loadRequest != null) {
            buff.append("_");
            buff.append(loadRequest.getWorkflowState());
            buff.append("_");
            buff.append(loadRequest.isWithMarkedForDeletion() || loadRequest.isWithDeleted() ? "yes" : "no");
        }
        return buff.toString();
    }

    public int getParentContainerListId(int id, EntryLoadRequest req) {
        Integer i = dao.getParentContainerListId((id));
        if (i == null && (req.isWithDeleted() || req.isWithMarkedForDeletion())) {
            i = dao.getDeletedParentContainerListId((id));
        }
        if (i != null) {
            return i;
        }
        return -1;
    }

    public List<Integer> findContainerIdByPropertyNameAndValue(String name, String value) {
        return dao.findContainerIdByPropertyNameAndValue(name, value);
    }


    public List<Object[]> getContainerPropertiesByName(String name) {
        return dao.getContainerPropertiesByName(name);
    }

    public void removeContainerProperty(int id, String name) {
        dao.deleteContainerProperty(id, name);
    }


    public List<Object[]> getSortedContainerIds(Integer ctListId, Integer[] siteIds, Boolean siteLevel,
                                                String[] containerDefinitionNames, String[] fieldNames, boolean sortByMetadata,
                                                EntryLoadRequest loadRequest, boolean ignoreLang, boolean stagingOnly,
                                                boolean ascendingOrder, BitSet ctnIdsBitset, int dbMaxResult) {

        List<Integer> fieldDefIDs = new ArrayList<Integer>();
        try {
            List<Integer> defs = ServicesRegistry.getInstance().getJahiaFieldService()
                    .getFieldDefinitionNameFromCtnType(fieldNames,sortByMetadata);
            for (Integer defId : defs) {
                fieldDefIDs.add(defId);
            }
        } catch (Exception t) {
        }
        if (fieldDefIDs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> siteIdsList = new ArrayList<Integer>();
        if (siteIds != null && siteIds.length > 0) {
            siteIdsList = Arrays.asList(siteIds);
        }

        List<Integer> ctnDefIDs = new ArrayList<Integer>();
        if (containerDefinitionNames != null && containerDefinitionNames.length > 0) {
            ctnDefIDs = new ArrayList<Integer>();
            List<String> ctnDefNamesList = Arrays.asList(containerDefinitionNames);
            try {
                for (Integer defID : ServicesRegistry.getInstance().getJahiaContainersService()
                        .getAllContainerDefinitionIDs()) {
                    JahiaContainerDefinition currentDefinition =
                            ServicesRegistry.getInstance().getJahiaContainersService()
                                    .loadContainerDefinition(defID);
                    if (currentDefinition != null && ctnDefNamesList.contains(currentDefinition.getName())) {
                        if (siteIds != null && siteIds.length > 0) {
                            if (siteIdsList == null || siteIdsList.contains((currentDefinition.getJahiaID()))) {
                                ctnDefIDs.add((currentDefinition.getID()));
                            }
                        } else {
                            ctnDefIDs.add((currentDefinition.getID()));
                        }
                    }
                }
            } catch (Exception t) {
                log.debug("Exception occured retrieving container definition", t);
                return Collections.emptyList();
            }
            if (ctnDefIDs.isEmpty()) {
                return Collections.emptyList();
            }
        }

        return dao.getSortedContainerIds(ctListId, siteIds, siteLevel,
                ctnDefIDs, fieldDefIDs, sortByMetadata,
                loadRequest, ignoreLang, stagingOnly,
                ascendingOrder, ctnIdsBitset, dbMaxResult);
    }

    public List<Object[]> getContainerIds(Integer ctListId, Integer[] siteIds, Boolean siteLevel, String[] containerDefinitionNames,
                                EntryLoadRequest loadRequest, boolean ignoreLang, boolean stagingOnly, boolean ascendingOrder,
                                boolean orderByPage,
                                Set<Integer> pagesID) {

        return dao.getContainerIds(ctListId, siteIds, siteLevel,
                containerDefinitionNames, loadRequest, ignoreLang, stagingOnly,
                ascendingOrder, orderByPage, pagesID);
    }

    /**
     * @param queryString
     * @param parameters
     * @return
     */
    public <E> List<E> executeQuery(String queryString, Map parameters) {
        return dao.executeQuery(queryString, parameters);
    }

    public List<Integer> getContainerIDsOnPagesHavingAcls(Set pageIDs, Set aclIDs) {
        return dao.getContainerIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public List<Integer> getContainerIDsHavingAcls(Set aclIDs) {
        return dao.getContainerIDsHavingAcls(aclIDs);
    }

    /**
     * Returns a map of acl ids for the given list of ctnIds
     *
     * @param ctnIds
     * @return
     */
    public Map<Integer, Integer> getContainerACLIDs(List<Integer> ctnIds) {
        return dao.getContainerACLIDs(ctnIds);
    }

    public Map<String, String> getVersions(int site) {
        return dao.getVersions(site);
    }

}

