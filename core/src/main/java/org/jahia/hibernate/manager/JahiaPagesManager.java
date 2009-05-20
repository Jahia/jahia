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
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ContentPageXRefManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.dao.*;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.jahia.hibernate.model.JahiaPagesData;
import org.jahia.hibernate.model.JahiaPagesDataPK;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageContentRights;
import org.jahia.services.pages.JahiaPageInfo;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.workflow.nstep.dao.WorkflowInstanceDAO;
import org.jahia.workflow.nstep.dao.WorkflowHistoryDAO;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 25 févr. 2005
 * Time: 10:25:55
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPagesManager {
    // ------------------------------ FIELDS ------------------------------
    private static final String PAGESMANAGER_CACHENAME = "PagesManagerCache";
    private static final String PAGESPROPERTY_KEYPREFIX = "PagesPropertyPrefix";
    private JahiaFieldsDataDAO fieldDAO = null;
    private JahiaObjectDAO jahiaObjectDAO = null;
    private JahiaWorkflowDAO jahiaWorkflowDAO = null;

    private WorkflowInstanceDAO workflowInstanceDAO;
    private WorkflowHistoryDAO workflowHistoryDAO;

    private JahiaGroupManager groupManager;

    private JahiaPagesDAO dao = null;
    private JahiaPagesDefinitionDAO definitionDAO = null;
    private CacheService cacheService = null;
    private Log log = LogFactory.getLog(getClass());

// --------------------- GETTER / SETTER METHODS ---------------------

    public synchronized int getNbPages() {
        return getNbPages(-1);
    }

    public synchronized int getRealActiveNbPages() {
        return getRealActiveNbPages(-1);
    }

    public void setJahiaFieldsDataDAO(JahiaFieldsDataDAO dao) {
        this.fieldDAO = dao;
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

    public void setJahiaPagesDAO(JahiaPagesDAO dao) {
        this.dao = dao;
    }

    public void setJahiaPagesDefinitionDAO(JahiaPagesDefinitionDAO dao) {
        this.definitionDAO = dao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JahiaGroupManager getGroupManager() {
        return groupManager;
    }

    public void setGroupManager(JahiaGroupManager groupManager) {
        this.groupManager = groupManager;
    }

    // -------------------------- OTHER METHODS --------------------------

    private void flushCache(int pageId, int siteID) {
//        WorkflowService.getInstance().flushCacheForObjectStateChange(new ContentPageKey(pageId));
        ServicesRegistry.getInstance().getJahiaPageService().invalidatePageCache(pageId);
        Cache<String, Object> cache = cacheService.getCache(JahiaObjectManager.CACHE_NAME);
        if (cache != null) {
            synchronized (cache) {
                cache.remove(JahiaObjectManager.CACHE_KEY_PREFIX + new ContentPageKey(pageId));
                cache.remove(JahiaObjectManager.OBJECTDELEGATE_KEY_PREFIX + new ContentPageKey(pageId));
                cache.remove(ContentPageKey.PAGE_TYPE);
            }
        }
    }

    public JahiaPageInfo copyEntry(int id, EntryStateable fromEntryState, EntryStateable toEntryState) {
        JahiaPagesDataPK pk = new JahiaPagesDataPK(id, fromEntryState.getVersionID(), fromEntryState.getWorkflowState(),
                fromEntryState.getLanguageCode());
        JahiaPagesData data;
        JahiaPageInfo pageInfo = null;
        try {
            data = dao.findByPK(pk);
            if (data != null) {
                try {
                    JahiaPagesData pagesData = (JahiaPagesData) data.clone();
                    pagesData.getComp_id().setVersionId(toEntryState.getVersionID());
                    pagesData.getComp_id().setWorkflowState(toEntryState.getWorkflowState());
                    pagesData.getComp_id().setLanguageCode(toEntryState.getLanguageCode());
                    dao.save(pagesData);
                    pageInfo = convertJahiaPagesDataToJahiaPageInfo(pagesData);
                } catch (CloneNotSupportedException e) {
                    log.error("This object is not cloneable", e);
                }
                flushCache(id, data.getSiteId());
            }
        } catch (ObjectRetrievalFailureException e) {
            log.error("JahiaPagesData not found or we are trying to update !!!!" + pk);
        }
        return pageInfo;
    }

    public void createPageInfo(JahiaPageInfo newStagingInfo) {
        Integer idJahiaPagesData;
        idJahiaPagesData = newStagingInfo.getID();
        JahiaPagesDataPK pk = new JahiaPagesDataPK(idJahiaPagesData,
                newStagingInfo.getVersionID(),
                newStagingInfo.getWorkflowState(),
                newStagingInfo.getLanguageCode());

        boolean isNew = newStagingInfo.getID() == 0;
        JahiaPagesData data;
        data = new JahiaPagesData();
        data.setComp_id(pk);
        if (isNew) {
            data.getComp_id().setId(null);
        }

        data.setJahiaAclId(newStagingInfo.getAclID());
        data.setPageDefinition(definitionDAO.findByPK(newStagingInfo.getPageTemplateID()));
        data.setPageLinkId(newStagingInfo.getPageLinkID());
        data.setPageType(newStagingInfo.getPageType());
        data.setParentID(newStagingInfo.getParentID());
        data.setRemoteURL(newStagingInfo.getRemoteURL());
        data.setSiteId(newStagingInfo.getJahiaID());
        data.setTitle(newStagingInfo.getTitle());
        dao.save(data);
        newStagingInfo.setID(data.getComp_id().getId());

        if (isNew) {
            jahiaObjectDAO.create(ContentPageKey.PAGE_TYPE, newStagingInfo.getID(), newStagingInfo.getJahiaID());
        }
        flushCache(newStagingInfo.getID(), newStagingInfo.getJahiaID());
    }

    public void deletePageInfo(JahiaPageInfo activeInfo) {
        dao.delete(activeInfo.getID(), activeInfo.getWorkflowState(),
                activeInfo.getLanguageCode());
        flushCache(activeInfo.getID(), activeInfo.getJahiaID());
        // remove all links for page if no pages exist at all with this id
        if (dao.getNBPages(activeInfo.getID()) == 0) {
            try {
                jahiaObjectDAO.delete(new JahiaObjectPK(ContentPageKey.PAGE_TYPE, activeInfo.getID()));
                String key = ContentPageKey.toObjectKeyString(activeInfo.getID());
                jahiaWorkflowDAO.delete(key);
                workflowHistoryDAO.removeWorkflowHistory(key);
                workflowInstanceDAO.removeWorkflowInstance(key);
                groupManager.searchAndDelete("workflowrole_" + key+"_%", null);

                dao.deleteProperties(activeInfo.getID());

                ContentPageXRefManager.getInstance().removeAllPageLinks(activeInfo.getID());;
                JahiaContainerListManager containerListManager = (JahiaContainerListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaContainerListManager.class.getName());
                JahiaContainerManager containerManager = (JahiaContainerManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaContainerManager.class.getName());
                int jahiaID = activeInfo.getJahiaID();
                flushCacheForMoving(activeInfo.getParentID(), jahiaID, containerListManager, containerManager);
                flushCacheForMoving(activeInfo.getID(), jahiaID, containerListManager, containerManager);
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Integer> getActivePageChildIDs(int pageID) {
        return dao.getActivePageChildIDs(pageID);
    }

    public int getActivePageFieldID(int pageID) {
        try {
            return fieldDAO.getActivePageFieldID((pageID));
        } catch (ObjectRetrievalFailureException e) {
            log.error("JahiaFields not found for page " + pageID);
            return -1;
        }
    }

    public List<Integer> getAllAclId(int siteID) {
        return dao.getAllAclId((siteID));
    }

    public synchronized int getNbPages(int siteId) {
        if (siteId != -1) {
            return dao.getNbPages((siteId));
        } else {
            return dao.getNbPages();
        }
    }

    public List<Integer> getPageChildIDs(int pageID) {
        return getPageChildIDs(pageID, true);
    }

    public List<Integer> getNonDeletedPageChildIDs(int pageID) {
        return getPageChildIDs(pageID, false);
    }

    public int getPageFieldID(int pageId) {
        try {
            return fieldDAO.getPageFieldID((pageId));
        } catch (ObjectRetrievalFailureException e) {
            log.error("JahiaFields not found for page " + pageId);
            return -1;
        }
    }

    public int getPageFieldID(int pageID, EntryLoadRequest loadRequest) {
        int pageFieldID = -1;
        try {
            if (loadRequest == null) {
                pageFieldID = fieldDAO.getAnyPageFieldID((pageID));
            } else if (loadRequest.isCurrent()) {
                pageFieldID = fieldDAO.getActivePageFieldID((pageID));
            } else if (loadRequest.isStaging()) {
                // here we must check the version ID and make sure it's not -1
                // because we only retrieve the parent field for the new position,
                // not all the old positions
                try {
                    pageFieldID = fieldDAO.getStagedPageFieldID(
                            (pageID));
                } catch (ObjectRetrievalFailureException e) {
                    try {
                        // return the active if the staging not found
                        pageFieldID = fieldDAO.getActivePageFieldID(
                                (pageID));
                    } catch (ObjectRetrievalFailureException ex) {
                        // this is for the case, where staging is deleted (not
                        // moved) and the active version has already been archived
                        if (loadRequest.isWithDeleted()
                                || loadRequest.isWithMarkedForDeletion()) {
                            pageFieldID = fieldDAO.getDeletedPageFieldID(
                                    (pageID));
                        }
                    }
                }
            } else if (loadRequest.isVersioned()) {
                try {
                    ContentPage contentPage = ContentPage.getPage(pageID, false);
                    int parentPageID = contentPage.getParentID(loadRequest);

                    pageFieldID = fieldDAO.getVersionedPageFieldID(pageID,
                            parentPageID,
                            loadRequest.getVersionID(),
                            true);
                } catch (JahiaPageNotFoundException pnfe) {
                    log.debug("ContentPage not found pid[" + pageID + "] returning -1 ");
                } catch (JahiaException e) {
                    log.debug("ContentPage not found pid[" + pageID + "] returning -1 ");
                }
            }
        } catch (ObjectRetrievalFailureException e) {
            log.debug("Page Field not found " + pageID);
        }
        return pageFieldID;
    }

    public List<Integer> getPageIDsInSiteWithSpecifiedLink(int siteID, int linkType) {
        return dao.getPageIDsInSiteWithSpecifiedLink((siteID), (linkType));
    }

    public List<Integer> getPageIDsPointingOnPage(int pageID, EntryLoadRequest loadRequest) {
        if (loadRequest.isVersioned()) {
            return dao.getVersionedPageIDsPointingOnPage((pageID));
        } else {
            return dao.getActivePageIDsPointingOnPage((pageID));
        }
    }

    public List<Integer> getPageIDsWithTemplate(int templateID) {
        return dao.getPageIDsWithTemplate((templateID));
    }

    public List<JahiaPageContentRights> getPageIDsWithAclIDs(Set<Integer> aclIDs) {
        return dao.getPageIDsWithAclIDs(aclIDs);
    }

    public List<Integer> getPageIdsInSite(int siteID) {
        return dao.getPageIdsInSite((siteID));
    }

    public List<Integer> getPageIdsInSiteOrderById(int siteID) {
        return dao.getPageIdsInSiteOrderById((siteID));
    }

    public synchronized int getRealActiveNbPages(int siteId) {
        if (siteId != -1) {
            return dao.getRealActiveNbPages((siteId));
        } else {
            return dao.getRealActiveNbPages();
        }
    }

    public int getStagedPageFieldID(int pageID) {
        try {
            return fieldDAO.getStagedPageFieldID((pageID));
        } catch (ObjectRetrievalFailureException e) {
            log.error("JahiaFields not found for page " + pageID);
            return -1;
        }
    }

    public List<Integer> getStagingAndActivePageFieldIDs(int pageId) {
        return fieldDAO.getStagingAndActivePageFieldIDs(pageId);
    }

    public List<Integer> getStagingPageChildIDs(int pageID) {
        return dao.getStagingPageChildIDs(pageID);
    }

    public Set<Integer> getStagingPageFieldIDsInPage(int pageID) {
        return new TreeSet<Integer>(fieldDAO.getStagingPageFieldIDsInPage(pageID));
    }

    public List<Integer> getVersioningPageChildIDs(int pageID, int version) {
        return dao.getVersioningPageChildIDs(pageID, version);
    }

    public List<JahiaPageInfo> loadPageInfos(int pageId, EntryLoadRequest entryLoadRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Try to load page infos for page " + pageId + " with request " + entryLoadRequest.toString());
        }
        List<JahiaPageInfo> pageInfos;
        if (entryLoadRequest.getWorkflowState() < EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            List<JahiaPagesData> list = dao.getVersionedPageInfo((pageId));
            if (log.isDebugEnabled()) {
                log.debug("We have found " + list.size() + " version of this page");
            }
            pageInfos = new FastArrayList(list.size());
            convertToListOfJahiaPageInfo(list, pageInfos);
        } else {
            List<JahiaPagesData> list = dao.getActivePageInfo((pageId));
            if (log.isDebugEnabled()) {
                log.debug("We have found " + list.size() + " version of this page");
            }
            pageInfos = new FastArrayList(list.size());
            convertToListOfJahiaPageInfo(list, pageInfos);
        }
        ((FastArrayList)pageInfos).setFast(true);
        return pageInfos;
    }

    public void preloadingPageInfos(Cache<Integer, FastArrayList> pageInfosCache, Cache<Integer, FastArrayList> versioningPageInfosCache) {
        List<JahiaPagesData> list = dao.getAllPagesInfos();
        int prevPageId = 0;
        int pageId;
        FastArrayList pageInfos = null;
        FastArrayList versioningPageInfos = null;
        for (Object aList : list) {
            JahiaPagesData data = (JahiaPagesData) aList;
            pageId = data.getComp_id().getId();
            if (prevPageId != pageId) {
                if (prevPageId > 0) {
                    versioningPageInfos.setFast(true);
                    pageInfos.setFast(true);
                    versioningPageInfosCache.put((prevPageId), versioningPageInfos);
                    pageInfosCache.put((prevPageId), pageInfos);
                }
                prevPageId = pageId;
                pageInfos = new FastArrayList(11);
                versioningPageInfos = new FastArrayList(11);
            }

            JahiaPageInfo pageInfo = convertJahiaPagesDataToJahiaPageInfo(data);
            if (pageInfo.getWorkflowState() < EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                versioningPageInfos.add(pageInfo);
            } else {
                pageInfos.add(pageInfo);
            }
        }
        if (prevPageId > 0) {
            versioningPageInfos.setFast(true);
            pageInfos.setFast(true);
            versioningPageInfosCache.put((prevPageId), versioningPageInfos);
            pageInfosCache.put((prevPageId), pageInfos);
        }
    }

    public void updatePageInfo(JahiaPageInfo newStagingInfo, int versionId, int workflowState) {
        JahiaPagesDataPK oldPk = new JahiaPagesDataPK((newStagingInfo.getID()),
                (newStagingInfo.getVersionID()),
                (newStagingInfo.getWorkflowState()),
                newStagingInfo.getLanguageCode());
        JahiaPagesData oldData = null;
        try {
            oldData = dao.findByPK(oldPk);
        } catch (ObjectRetrievalFailureException e) {
            log.error("JahiaPagesData not found or we are trying to update !!!!" + oldPk);
        }
        if (oldData == null) {
            return;
        }
        JahiaPagesDataPK pk = new JahiaPagesDataPK((newStagingInfo.getID()),
                (versionId),
                (workflowState),
                newStagingInfo.getLanguageCode());

        JahiaPagesData data = new JahiaPagesData();
        data.setComp_id(pk);
        data.setJahiaAclId((newStagingInfo.getAclID()));
        data.setPageDefinition(definitionDAO.findByPK((newStagingInfo.getPageTemplateID())));
        data.setPageLinkId((newStagingInfo.getPageLinkID()));
        data.setPageType((newStagingInfo.getPageType()));
        // Have we move the page ? if so flsuh all caches because we do not know yet which cache must be flushed
        // @Todo find a better way of flushing when moving page
        int parentID = 0;
        if (oldData.getParentID() != null)
            parentID = oldData.getParentID();
        int jahiaID = newStagingInfo.getJahiaID();
        if (newStagingInfo.getParentID() != parentID) {
            try {
                JahiaContainerListManager containerListManager = (JahiaContainerListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaContainerListManager.class.getName());
                JahiaContainerManager containerManager = (JahiaContainerManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaContainerManager.class.getName());
                flushCacheForMoving(parentID, jahiaID, containerListManager, containerManager);
                flushCacheForMoving(newStagingInfo.getParentID(), jahiaID, containerListManager, containerManager);
                flushCacheForMoving(newStagingInfo.getID(), jahiaID, containerListManager, containerManager);
            } catch (JahiaException e) {
                log.warn("Error flushing cache", e);  //To change body of catch statement use File | Settings | File Templates.
            }
//            cacheService.flushAllCaches();
        }
        data.setParentID((newStagingInfo.getParentID()));
        data.setRemoteURL(newStagingInfo.getRemoteURL());
        data.setSiteId((jahiaID));
        data.setTitle(newStagingInfo.getTitle());
        if (pk.equals(oldPk)) {
            dao.update(data);
        } else {
            dao.delete(oldData);
            dao.save(data);
        }
        flushCache(newStagingInfo.getID(), jahiaID);
    }

    private void flushCacheForMoving(int parentID, int jahiaID, JahiaContainerListManager containerListManager, JahiaContainerManager containerManager) throws JahiaException {
        Set<Integer> pageIDs = ContentPageXRefManager.getInstance().getPageIDs(parentID);
        for (Object pageID1 : pageIDs) {
            Integer pageID = (Integer) pageID1;
            flushCache(pageID, jahiaID);
            Collection<Integer> absoluteContainerListsFromPageID = containerListManager.getAllPageContainerListIDs(pageID);
            for (Integer containerListKey : absoluteContainerListsFromPageID) {
                containerListManager.flushCache(containerListKey, jahiaID, null);
                containerManager.flushCache(0, jahiaID, containerListKey, pageID);
            }
        }
    }

    private JahiaPageInfo convertJahiaPagesDataToJahiaPageInfo(JahiaPagesData data) {
        JahiaPageInfo pageInfo = null;
        if (data != null) {
            pageInfo = new JahiaPageInfo(data.getComp_id().getId(),
                    data.getSiteId(), data.getParentID(),
                    data.getPageType(), data.getTitle(),
                    data.getPageDefinition().getId(),
                    data.getRemoteURL(), data.getPageLinkId(),
                    data.getJahiaAclId(),
                    data.getComp_id().getVersionId(),
                    data.getComp_id().getWorkflowState(),
                    data.getComp_id().getLanguageCode(), this);
        }
        return pageInfo;
    }

    private void convertToListOfJahiaPageInfo(List<JahiaPagesData> list, List<JahiaPageInfo> pageInfos) {
        for (JahiaPagesData data : list) {
            if (log.isDebugEnabled()) {
                log.debug("JahiaPagesData : " + data.toString());
            }
            JahiaPageInfo pageInfo = convertJahiaPagesDataToJahiaPageInfo(data);
            pageInfos.add(pageInfo);
        }
    }

    private List<Integer> fillChildList(List<Object[]> list, int pageID) {
        List<Integer> retList = new ArrayList<Integer>(list.size());
        for (Object[] tupe : list) {
            Integer id = (Integer) tupe[0];
            Integer version = (Integer) tupe[1];
            Integer workflow = (Integer) tupe[2];
            if (!retList.contains(id)) {
                try {
                    if (workflow == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                        ContentPage contentPage = ContentPage.getPage(id, false);
                        if (!contentPage.isDeletedOrDoesNotExist(version) ||
                                pageID != contentPage.getParentID(EntryLoadRequest.STAGED)) {
                            continue;
                        }
                    }
                    retList.add(id);
                } catch (JahiaException e) {
                    log.debug("Exception retrieving page childs for pid[" + pageID + "]", e);
                }
            }
        }
        return retList;
    }

    private List<Integer> getPageChildIDs(int pageID, boolean withDeleted) {
        List<Integer> retList;
        if (withDeleted) {
            List<Object[]> list = dao.getAllPageChildIDs((pageID));
            retList = fillChildList(list, pageID);
        } else {
            List<Object[]> list = dao.getAllNonDeletedPageChildIDs((pageID));
            retList = fillChildList(list, pageID);
        }
        return retList;
    }

    public Map<String, PageProperty> getPageProperties(int pageId) {
        Map<String, PageProperty> properties = null;
        Cache<String, Map<String, PageProperty>> cache = cacheService.getCache(PAGESMANAGER_CACHENAME);
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(PAGESMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        if (cache != null) {
            properties = cache.get(PAGESPROPERTY_KEYPREFIX + pageId);
        }
        if (properties == null) {
            final List<Object[]> pageProperties = dao.getPageProperties((pageId));
            properties = new ConcurrentHashMap<String, PageProperty>(53);
            for (Object[] objects : pageProperties) {
                String name = (String) objects[0];
                String language = (String) objects[1];
                String value = (String) objects[2];
                if (!properties.containsKey(name)) {
                    properties.put(name, new PageProperty(pageId, name));
                }
                PageProperty pageProperty = properties.get(name);
                pageProperty.setValue(value, language);
            }
            if (cache != null) {
                cache.put(PAGESPROPERTY_KEYPREFIX + pageId, properties);
            }
        }
        return properties;
    }

    public List<PageProperty> getPagePropertiesByValue(String propertyValue) {
        List<Object[]> pageProperties = dao.getPagePropertiesByValue(propertyValue);
        List<PageProperty> properties = new ArrayList<PageProperty>(53);
        if (!pageProperties.isEmpty()) {
            String oldname = (String) ((Object[]) pageProperties.get(0))[1];
            Integer oldPageId = (Integer) ((Object[]) pageProperties.get(0))[0];
            List<String> values = new ArrayList<String>(11);
            List<String> languages = new ArrayList<String>(11);
            for (Object pageProperty1 : pageProperties) {
                Object[] objects = (Object[]) pageProperty1;
                String name = (String) objects[1];
                String language = (String) objects[2];
                String value = (String) objects[3];
                Integer pageID = (Integer) objects[0];
                if (!oldname.equals(name)) {
                    PageProperty pageProperty = new PageProperty(oldPageId, oldname);
                    for (int j = 0; j < languages.size(); j++) {
                        pageProperty.setValue(values.get(j), languages.get(j));
                    }
                    properties.add(pageProperty);
                    values = new ArrayList<String>(11);
                    languages = new ArrayList<String>(11);
                }
                values.add(value);
                languages.add(language);
                oldname = name;
                oldPageId = pageID;
            }
            PageProperty pageProperty = new PageProperty(oldPageId, oldname);
            for (int j = 0; j < languages.size(); j++) {
                pageProperty.setValue(values.get(j), languages.get(j));
            }
            properties.add(pageProperty);
        }
        return properties;
    }

    public List<Object[]> getPagePropertiesByName(String name) {
        return dao.getPagePropertiesByName(name);
    }
    

    public void removePageProperty(PageProperty targetProperty) {
        Cache<String, Object> cache = cacheService.getCache(PAGESMANAGER_CACHENAME);
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(PAGESMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        if (cache != null) {
            cache.remove(PAGESPROPERTY_KEYPREFIX + targetProperty.getPageID());
        }
        dao.deletePageProperty((targetProperty.getPageID()), targetProperty.getName());
    }

    public void setPageProperty(PageProperty targetProperty) {
        Cache<String, Object> cache = cacheService.getCache(PAGESMANAGER_CACHENAME);
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(PAGESMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        if (cache != null) {
            cache.remove(PAGESPROPERTY_KEYPREFIX + targetProperty.getPageID());
        }
        dao.savePageProperty((targetProperty.getPageID()), targetProperty.getName(),
                targetProperty.getLanguageCodes());
    }

    public List<Integer> findPageIdByPropertyNameAndValue(String name, String value) {
        return dao.findPageIdByPropertyNameAndValue(name, value);
    }

    public List<PageProperty> getPagePropertiesByValueAndSiteID(String propertyValue, int siteID) {
        List<Object[]> pageProperties = dao.getPagePropertiesByValueAndSiteID(propertyValue, (siteID));
        List<PageProperty> properties = new ArrayList<PageProperty>(53);
        if (!pageProperties.isEmpty()) {
            String oldname = (String) ((Object[]) pageProperties.get(0))[1];
            Integer oldPageId = (Integer) ((Object[]) pageProperties.get(0))[0];
            List<String> values = new ArrayList<String>(11);
            List<String> languages = new ArrayList<String>(11);
            for (Object pageProperty1 : pageProperties) {
                Object[] objects = (Object[]) pageProperty1;
                String name = (String) objects[1];
                String language = (String) objects[2];
                String value = (String) objects[3];
                Integer pageID = (Integer) objects[0];
                if (!oldname.equals(name)) {
                    PageProperty pageProperty = new PageProperty(oldPageId, oldname);
                    for (int j = 0; j < languages.size(); j++) {
                        pageProperty.setValue(values.get(j), languages.get(j));
                    }
                    properties.add(pageProperty);
                    values = new ArrayList<String>(11);
                    languages = new ArrayList<String>(11);
                }
                values.add(value);
                languages.add(language);
                oldname = name;
                oldPageId = pageID;
            }
            PageProperty pageProperty = new PageProperty(oldPageId, oldname);
            for (int j = 0; j < languages.size(); j++) {
                pageProperty.setValue(values.get(j), languages.get(j));
            }
            properties.add(pageProperty);
        }
        return properties;
    }

    public List<PageProperty> getPagePropertiesByNameValueSiteIDAndParentID(String propertyName, String propertyValue, int siteID, int parentID) {
        List<Object[]> pageProperties = dao.getPagePropertiesByNameValueSiteIDAndParentID(propertyName, propertyValue, (siteID), (parentID));
        List<PageProperty> properties = new ArrayList<PageProperty>(53);
        if (!pageProperties.isEmpty()) {
            for (Object pageProperty1 : pageProperties) {
                Object[] objects = (Object[]) pageProperty1;
                String language = (String) objects[1];
                Integer pageID = (Integer) objects[0];
                PageProperty pageProperty = new PageProperty(pageID, propertyName);
                pageProperty.setValue(propertyValue, language);
                properties.add(pageProperty);
            }
        }
        return properties;
    }

    /**
     * Returns the page ID with the specified URL key value for the given site.
     *
     * @param pageURLKey the page URL key value to search for
     * @param siteID     the target site ID
     * @return the page ID with the specified URL key value for the given site
     */
    public int getPageIDByURLKeyAndSiteID(String pageURLKey, int siteID) {
        return dao.getPageIDByURLKeyAndSiteID(pageURLKey, (siteID));
    }

    public Map<String, String> getVersions(int site, String lang) {
        return dao.getVersions(site, lang);
    }

}

