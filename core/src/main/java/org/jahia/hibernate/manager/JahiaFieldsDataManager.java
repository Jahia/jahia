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
import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;
import org.hibernate.ObjectDeletedException;
import org.hibernate.exception.ConstraintViolationException;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.CrossReferenceManager;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaFieldsDataDAO;
import org.jahia.hibernate.dao.JahiaFieldsDefinitionDAO;
import org.jahia.hibernate.dao.JahiaObjectDAO;
import org.jahia.hibernate.model.JahiaFieldsData;
import org.jahia.hibernate.model.JahiaFieldsDataPK;
import org.jahia.hibernate.model.JahiaFieldsDef;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.webdav.UsageEntry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 janv. 2005
 * Time: 17:32:02
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldsDataManager {
// ------------------------------ FIELDS ------------------------------

    private static final String CACHE_NAME = "JahiaFieldsDataManagerCache";
    private static final String CACHE_KEY_PREFIX = "ContentField_";
    private static final String MD_KEY_PREFIX = "MetaData_";
    private static final String FIELDIDSINCONTAINER_KEY_PREFIX = "FieldIdsInContainer_";
    private JahiaFieldsDataDAO dao;
    private JahiaFieldsDefinitionDAO definitionDAO;
    private Logger log = Logger.getLogger(getClass());
    private CacheService cacheService = null;
    private Cache<Object, Object> fieldCache = null;

    private JahiaObjectDAO jahiaObjectDAO;

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<Integer> getAllFieldsId() {
        return dao.findAllFieldsId();
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setJahiaFieldsDataDAO(JahiaFieldsDataDAO dao) {
        this.dao = dao;
    }

    public void setJahiaFieldsDefinitionDAO(JahiaFieldsDefinitionDAO definitionDAO) {
        this.definitionDAO = definitionDAO;
    }

    public JahiaObjectDAO getJahiaObjectDAO() {
        return jahiaObjectDAO;
    }

    public void setJahiaObjectDAO(JahiaObjectDAO jahiaObjectDAO) {
        this.jahiaObjectDAO = jahiaObjectDAO;
    }

    /**
     * @param value
     * @param type  field type
     * @return
     */
    public List<Integer> findFieldsByValuesAndType(String value, int type) {
        return dao.findFieldsByValuesAndType(value, type);
    }

    /**
     *
     * @param value
     * @param type
     * @return List of object arrays 0 is id 1 is object type
     */
    public List<Object[]> findMetadataObjectKeyByFieldValuesAndType(String value, int type) {
        return dao.findMetadataObjectKeyByFieldValuesAndType(value,new Integer(type));
    }

// -------------------------- OTHER METHODS --------------------------

    public ContentObjectEntryState backupField(ContentField contentField, ContentObjectEntryState entryState) {
        try {
            dao.backupFieldsData((contentField.getID()), (entryState.getVersionID()),
                    (entryState.getWorkflowState()), entryState.getLanguageCode());
        } catch (DataIntegrityViolationException e) {
            // The object is already backuped
            log.warn("Warning field already backuped : " + contentField.getID() + " " +
                    entryState.getLanguageCode());
        } catch (ConstraintViolationException e) {
            // The object is already backuped
            log.warn("Warning field already backuped : " + contentField.getID() + " " +
                    entryState.getLanguageCode());
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Warning field not found for backuping : " + contentField.getID() + " " +
                    entryState.getLanguageCode(),
                    e);
            return null;
        } catch (Exception e) {
            log.warn("Unknown error, field has not been backuped : " + contentField.getID() + " " +
                    entryState.getLanguageCode(),
                    e);
        }
        flushCache(contentField.getID(), contentField.getSiteID(), contentField.getContainerID());
        return new ContentObjectEntryState(0, entryState.getVersionID(), entryState.getLanguageCode());
    }

    private void flushCache(int id, int siteID, int containerId) {
        if (fieldCache != null) {
            fieldCache.remove(CACHE_KEY_PREFIX + id);
            fieldCache.flushGroup(FIELDIDSINCONTAINER_KEY_PREFIX + containerId);
        }
        Cache<String, Object> cache = cacheService.getCache(JahiaObjectManager.CACHE_NAME);
        if (cache != null) {
            synchronized (cache) {
                cache.remove(JahiaObjectManager.CACHE_KEY_PREFIX + new ContentFieldKey(id));
                cache.remove(JahiaObjectManager.OBJECTDELEGATE_KEY_PREFIX + new ContentFieldKey(id));
                cache.remove(ContentFieldKey.FIELD_TYPE);
            }
        }
        cache = cacheService.getCache(JahiaContainerManager.JAHIACONTAINER_CACHE_NAME);
        if (cache != null) {
            synchronized (cache) {
                cache.flushGroup(JahiaContainerManager.CACHE_KEY_PREFIX + containerId);
            }
        }
    }

    public synchronized void changeEntryState(ContentField contentField, ContentObjectEntryState entryState,
                                              ContentObjectEntryState newEntryState) {
        try {
            JahiaFieldsData data = findJahiaFieldsData(contentField, entryState);
            if (data != null) {
                JahiaFieldsDataPK pk = new JahiaFieldsDataPK((contentField.getID()),
                        (newEntryState.getVersionID()),
                        (newEntryState.getWorkflowState()),
                        newEntryState.getLanguageCode());
                if (!pk.equals(data.getComp_id())) {
                    JahiaFieldsData fieldsData = (JahiaFieldsData) data.clone();
                    dao.deleteJahiaField(data);
                    fieldsData.setComp_id(pk);
                    dao.saveNewVersion(fieldsData);
                    flushCache(contentField.getID(), contentField.getSiteID(), contentField.getContainerID());
                }
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Field not found in database ", e);
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaFieldsData", e);
        }
    }

    public void copyEntry(ContentField cf, ContentObjectEntryState from, ContentObjectEntryState to) {
        int id = cf.getID();
        try {
            JahiaFieldsData fieldsData = dao.findJahiaFieldsDataByPK(new JahiaFieldsDataPK((id),
                    (
                            from.getVersionID()),
                    (
                            from.getWorkflowState()),
                    from.getLanguageCode()));
            if (fieldsData != null) {
                JahiaFieldsData data = (JahiaFieldsData) fieldsData.clone();
                data.setComp_id(new JahiaFieldsDataPK((id),
                        (to.getVersionID()),
                        (to.getWorkflowState()),
                        to.getLanguageCode()));
                dao.save(data);
                flushCache(id, data.getSiteId() != null ? data.getSiteId() : 0, data.getContainerId());
                fieldCache.put(CACHE_KEY_PREFIX + id, cf);
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("field not found " + id + " entry " + from.toString(), e);
        } catch (CloneNotSupportedException e) {
            log.error("Could not clone JahiaFieldsData", e);
        }
    }

    public void createField(JahiaField theField, JahiaSaveVersion saveVersion) {
        JahiaFieldsData data = new JahiaFieldsData();
        data.setConnectType((theField.getConnectType()));
        data.setContainerId((theField.getctnid()));
        JahiaFieldsDef fieldDefinition = null;
        if (theField.getFieldDefID() > 0) {
            fieldDefinition = definitionDAO.loadDefinition((theField.getFieldDefID()));
        }
        data.setFieldDefinition(fieldDefinition);
        if (theField.getMetadataOwnerObjectKey() != null) {
            ObjectKey objKey = theField.getMetadataOwnerObjectKey();
            org.jahia.hibernate.model.JahiaObject jahiaObject =
                    jahiaObjectDAO.findByPK(new JahiaObjectPK(objKey.getType(), new Integer(objKey.getIDInType())));
            if (jahiaObject != null) {
                data.setMetadataOwnerId(jahiaObject.getComp_id().getId());
                data.setMetadataOwnerType(jahiaObject.getComp_id().getType());
            }
        }
        data.setPageId((theField.getPageID()));
        data.setProperties((Map<Object, Object>)theField.getProperties());
        data.setSiteId((theField.getJahiaID()));
        data.setType((theField.getType()));
        String value = theField.getValue();
        if (value.equals("")) {
            value = "<empty>";
        }
        if (value.length() > 250) {
            value = "<empty>";
        }
        data.setValue(value);
        JahiaFieldsDataPK pk = new JahiaFieldsDataPK();
        final int workflowState = saveVersion.getWorkflowState();
        pk.setWorkflowState((workflowState));
        pk.setLanguageCode(theField.getLanguageCode());
        if (workflowState > 1) {
            pk.setVersionId((0));
        } else {
            pk.setVersionId((saveVersion.getVersionID()));
        }
        data.setComp_id(pk);
        dao.save(data);
        theField.setID(data.getComp_id().getId());
        flushCache(theField.getID(), data.getSiteId() != null ? data.getSiteId() : 0, theField.getctnid());

        ContentObjectEntryState entryState = new ContentObjectEntryState(
                data.getComp_id().getWorkflowState(),
                data.getComp_id().getVersionId(),
                data.getComp_id().getLanguageCode());
        List<ContentObjectEntryState> activeEntryStates = new ArrayList<ContentObjectEntryState>();
        activeEntryStates.add(entryState);
        Map<ContentObjectEntryState, String> activeValues = new HashMap<ContentObjectEntryState, String>();
        activeValues.put(entryState, value);
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            final ContentField contentFieldInstance = ContentFieldTools.getInstance().createContentFieldInstance(
                    data.getComp_id().getId(),
                    theField.getJahiaID(),
                    data.getPageId(),
                    data.getContainerId(),
                    data.getFieldDefinition().getId(),
                    data.getType(),
                    data.getConnectType(),
                    -1,
                    activeEntryStates, activeValues);
            if (data.getMetadataOwnerId() != null) {
                try {
                    ObjectKey key = new JahiaObjectPK(data.getMetadataOwnerType(), data.getMetadataOwnerId()).toObjectKey();
                    contentFieldInstance.setMetadataOwnerObjectKey(key);
                    fieldCache.put(MD_KEY_PREFIX + key + fieldDefinition.getName(), contentFieldInstance);
                } catch (Exception t) {
                }
            }
            if (data.getProperties() == null) {
                contentFieldInstance.setProperties(new HashMap<Object, Object>());
            } else {
                contentFieldInstance.setProperties(data.getProperties());
            }
            fieldCache.put(CACHE_KEY_PREFIX + theField.getID(), contentFieldInstance);
        } catch (JahiaException e) {
            log.error("Error creating field", e);
        }
    }

    public int createValue(ContentField contentField, ContentObjectEntryState entryState, String dbValue) {
        JahiaFieldsData fieldsData = new JahiaFieldsData();
        Integer idJahiaFieldsData = null;
        int fieldId = contentField.getID();
        if (fieldId > 0)
            idJahiaFieldsData = (fieldId);
        fieldsData.setComp_id(new JahiaFieldsDataPK(idJahiaFieldsData,
                (entryState.getVersionID()),
                (entryState.getWorkflowState()),
                entryState.getLanguageCode()));
        fieldsData.setConnectType((contentField.getConnectType()));
        fieldsData.setContainerId((contentField.getContainerID()));
        final JahiaFieldsDef fieldDefinition = definitionDAO.loadDefinition((contentField.getFieldDefID()));
        fieldsData.setFieldDefinition(fieldDefinition);
        if (contentField.getMetadataOwnerObjectKey() != null) {
            ObjectKey objKey = contentField.getMetadataOwnerObjectKey();
            fieldsData.setMetadataOwnerId((objKey.getIdInType()));
            fieldsData.setMetadataOwnerType(objKey.getType());
        }
        fieldsData.setPageId((contentField.getPageID()));
        fieldsData.setSiteId((contentField.getSiteID()));
        fieldsData.setType((contentField.getType()));
        fieldsData.setValue(dbValue != null ? dbValue : "");
        dao.save(fieldsData);
        flushCache(fieldId, contentField.getSiteID(), contentField.getContainerID());
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            fieldCache.put(CACHE_KEY_PREFIX + fieldsData.getComp_id().getId(), contentField);
        } catch (JahiaInitializationException e) {
            log.error("Cannot get cache", e);
        }
        return fieldsData.getComp_id().getId();
    }

    public void deleteEntry(ContentField cf, EntryStateable entryState) {
        int id = cf.getID();
        try {
            JahiaFieldsData jahiaFieldsDataByPK = dao.findJahiaFieldsDataByPK(new JahiaFieldsDataPK((id), (entryState.getVersionID()),
                    (entryState.getWorkflowState()), entryState.getLanguageCode()));
            if (jahiaFieldsDataByPK != null) {
                dao.deleteJahiaField(jahiaFieldsDataByPK);
                flushCache(id, jahiaFieldsDataByPK.getSiteId() != null ? jahiaFieldsDataByPK.getSiteId() : 0, jahiaFieldsDataByPK.getContainerId());
                fieldCache.put(CACHE_KEY_PREFIX + id, cf);
            }
        } catch (ObjectDeletedException e) {

        }
        // remove all links for page if no pages exist at all with this id
        deleteFieldReferences(id);
    }

    private void deleteFieldReferences(int id) {
        if (dao.getNBFields(id) == 0) {
            try {
                ContentFieldKey key = new ContentFieldKey(id);
                CrossReferenceManager.getInstance().removeAllObjectXRefs(key);
                JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
                fieldLinkManager.deleteReferencesForField(id);
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Delete a field.
     *
     * @param fieldId
     * @param saveVersion
     */
    public void deleteField(int fieldId, JahiaSaveVersion saveVersion) {
        // Load active container
        JahiaFieldsData field = null;
        try {
            field = dao.loadPublishedField((fieldId));
        } catch (ObjectRetrievalFailureException e) {
        }
        if (saveVersion.isStaging()) {
            if (field != null &&
                    field.getComp_id().getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                // We have an active version so we create a staged version if none exist
                List<JahiaFieldsData> stagedFields = null;
                try {
                    stagedFields = dao.loadStagedFields((fieldId));
                } catch (ObjectRetrievalFailureException e) {
                }
                if (stagedFields == null || stagedFields.isEmpty()) {
                    JahiaFieldsData jahiaField = dao.createStagedField(field,
                            (saveVersion.getVersionID()),
                            (saveVersion.getWorkflowState()));
                    stagedFields = new ArrayList<JahiaFieldsData>(1);
                    stagedFields.add(jahiaField);
                }
                // we update the container
                for (Object stagedField : stagedFields) {
                    JahiaFieldsData jahiaField = (JahiaFieldsData) stagedField;
                    if (jahiaField.getComp_id().getVersionId() != -1) {
                        dao.deleteJahiaField(jahiaField);
                        try {
                            JahiaFieldsData jahiaField2 = (JahiaFieldsData) jahiaField.clone();
                            jahiaField2.getComp_id().setVersionId((-1));
                            dao.save(jahiaField2);
                        } catch (CloneNotSupportedException e) {
                            log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
                        }
                    }
                }
                flushCache(fieldId, field.getSiteId() != null ? field.getSiteId() : 0, field.getContainerId());
            } else {
                List<JahiaFieldsData> stagedFields = null;
                try {
                    stagedFields = dao.loadStagedFields(fieldId);
                } catch (ObjectRetrievalFailureException e) {
                }
                dao.deleteJahiaFields(stagedFields);
                if (!stagedFields.isEmpty()) {
                    field = stagedFields.get(0);
                    flushCache(fieldId, field.getSiteId() != null ? field.getSiteId() : 0, field.getContainerId());
                }
            }
        } else if (saveVersion.isVersioned()) {
            dao.backupFieldsData(fieldId);
            dao.deleteJahiaField(field);
            try {
                org.jahia.hibernate.model.JahiaContainer jahiaContainer =
                        (org.jahia.hibernate.model.JahiaContainer) field.clone();
                jahiaContainer.getComp_id().setWorkflowState((-1));
                jahiaContainer.getComp_id().setVersionId((saveVersion.getVersionID()));
                dao.save(field);
                flushCache(fieldId, field.getSiteId() != null ? field.getSiteId() : 0, field.getContainerId());
            } catch (CloneNotSupportedException e) {
                log.error("Could not clone org.jahia.hibernate.model.JahiaContainer");
            }
        } else {
            dao.deleteJahiaField(field);
            flushCache(fieldId, field.getSiteId() != null ? field.getSiteId() : 0, field.getContainerId());
        }
        // remove all links for page if no pages exist at all with this id
        deleteFieldReferences(fieldId);
    }

    public void deleteJahiaFields(ContentField contentField, ContentObjectEntryState entryState) {
        final int fieldId = contentField.getID();
        dao.deleteValue((fieldId), (entryState.getWorkflowState()),
                entryState.getLanguageCode());
        flushCache(fieldId, contentField.getSiteID(), contentField.getContainerID());
        fieldCache.put(CACHE_KEY_PREFIX + contentField.getID(), contentField);

        // remove all links for page if no pages exist at all with this id
        deleteFieldReferences(fieldId);
    }

    public List<Integer> getActiveOrStagedFieldIDsInPage(int pageID) {
        return dao.findStagingFieldsIdInPage((pageID));
    }

    public List<Integer> getAllAclId(int siteID) {
        return dao.findAllAclsIdInSite((siteID));
    }

    public List<Integer> getAllFieldsId(int siteID) {
        return dao.findAllFieldsIdInSite((siteID));
    }

    public int getFieldId(String fieldName, int pageID) {
        int ret = -1;
        try {
            ContentPage page = ServicesRegistry.getInstance().
                    getJahiaPageService().lookupContentPage(pageID, true);

            if (page != null) {
                Integer integer = dao.findFieldsIdInPageByDefinitionNameAndSite((page.getJahiaID()),
                        fieldName, (pageID));
                if (integer != null) {
                    ret = integer;
                }
            }
        } catch (JahiaException e) {
            log.warn("Page not found", e);
        }
        return ret;
    }

    public List<Object[]> getFieldsIdsAndTypesInContainer(int ctnid, EntryLoadRequest loadVersion) {
        return getFieldsIdsInContainer(ctnid, loadVersion, true);
    }

    public List<Integer> getFieldsIdsInContainer(int ctnid, EntryLoadRequest loadVersion) {
        return getFieldsIdsInContainer(ctnid, loadVersion, false);
    }

    public List getFieldsIdsInContainer(int ctnid, EntryLoadRequest loadVersion, boolean withTypes) {

        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            boolean compareMode = (Jahia.getThreadParamBean() != null
                    && ProcessingContext.COMPARE.equals(Jahia.getThreadParamBean().getOpMode()));
            List retVal;
            Set<String> set = new HashSet<String>();
            set.add(FIELDIDSINCONTAINER_KEY_PREFIX + ctnid);
            GroupCacheKey key = new GroupCacheKey(FIELDIDSINCONTAINER_KEY_PREFIX + ctnid + loadVersion + withTypes, set);
            if (!compareMode && fieldCache.containsKey(key)) {
                List result = (List) fieldCache.get(key);
                if (result != null) {
                    return result;
                }
            }
            if (loadVersion == null) {
                retVal = dao.findAllFieldsIdInContainer(ctnid, withTypes);
            } else if (loadVersion.isStaging()) {
                retVal = dao.findStagingFieldsIdInContainer(ctnid, withTypes);
            } else if (loadVersion.isVersioned()) {
                if (!compareMode) {
                    retVal = dao.findVersionedFieldsIdInContainer(ctnid, loadVersion.getVersionID());
                } else {
                    retVal = dao.findAllFieldsIdInContainer(ctnid);
                }
            } else {
                retVal = dao.findPublishedFieldsIdInContainer(ctnid, withTypes);
            }
            if (!compareMode) {
                fieldCache.put(key, retVal);
            }
            return retVal;
        } catch (JahiaInitializationException e) {
            log.error("Error gettinf fields ids", e);
        }
        return null;
    }

    public List<Object[]> getNonContainerFieldIDsAndTypesInPageByWorkflowState(int ctnid, EntryLoadRequest loadVersion) {
        return getNonContainerFieldIDsInPageByWorkflowState(ctnid, loadVersion, true);
    }

    public List<Integer> getNonContainerFieldIDsInPageByWorkflowState(int ctnid, EntryLoadRequest loadVersion) {
        return getNonContainerFieldIDsInPageByWorkflowState(ctnid, loadVersion, false);
    }

    public List getNonContainerFieldIDsInPageByWorkflowState(int pageID, EntryLoadRequest loadRequest, boolean withTypes) {
        List list = null;
        if (loadRequest == null) {
            list = dao.findFieldsIdInPageWithoutContainer(pageID, withTypes);
        } else if (loadRequest.isVersioned()) {
            list = dao.findVersionedFieldsIdInPageWithoutContainer(pageID,
                    loadRequest.getVersionID(), withTypes);
        } else if (loadRequest.isStaging()) {
            list = dao.findStagingFieldsIdInPageWithoutContainer(pageID, withTypes);
        } else if (loadRequest.isCurrent()) {
            list = dao.findPublishedFieldsIdInPageWithoutContainer(pageID, withTypes);
        }
        return list;
    }

    public List<Integer> getOnlyActiveNonContainerFieldIdsInPage(int pageID) {
        return dao.findPublishedFieldsIdInPageWithoutContainer(pageID);
    }

    public List<Integer> getOnlyStagedFieldIdsInPage(int pageID) {
        return dao.findStagedFieldsIdInPage(pageID);
    }

    public List<Integer> getOnlyStagedNonContainerFieldIdsInPage(int pageID) {
        return dao.findStagedFieldsIdInPageWithoutContainer(pageID);
    }

    public List<Integer> getPageFieldIdsInPage(int pageID) {
        return dao.findFieldsIdInPageByType((pageID), (FieldTypes.PAGE));
    }

    public List<Integer> findMetadatasByOwner(ObjectKey objectKey) {
        return dao.findMetadatasByOwner(new JahiaObjectPK(objectKey.getType(),
                (objectKey.getIdInType())));
    }

    public ContentObject findJahiaObjectByMetadata(Integer fieldId) {
        org.jahia.hibernate.model.JahiaObjectPK hibJahiaObject = dao.findJahiaObjectPKByMetadata(fieldId);
        if (hibJahiaObject != null) {
            try {
                return ContentObject.getContentObjectInstance(hibJahiaObject.toObjectKey());
            } catch (Exception t) {
                log.debug("Exception loading JahiaObject from Hibernate JahiaObject", t);
            }
        }
        return null;
    }

    public Map<Integer, List<Integer>> loadAllFieldsFromContainerFromPage(int pageID) {
        return dao.getAllFieldsIdsFromContainerForPage((pageID));
    }

    public Map<ContentObjectEntryState, String> loadAllValues(int id) {
        List<Object[]> list = dao.findAllValues((id));
        Map<ContentObjectEntryState, String> map = null;
        if (list != null) {
            map = new FastHashMap(list.size());
            for (Object aList : list) {
                Object[] objects = (Object[]) aList;
                map.put(new ContentObjectEntryState(((Integer) objects[2]),
                        ((Integer) objects[1]),
                        (String) objects[3]), (String)objects[0]);
            }
            ((FastHashMap)map).setFast(true);
        }
        return map;
    }

    public ContentField loadContentField(int fieldID) {
        return loadContentField(fieldID, false);
    }

    public ContentField loadContentFieldFromCacheOnly(int fieldId) {
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            return (ContentField) fieldCache
                    .get(CACHE_KEY_PREFIX + fieldId);
        } catch (JahiaException e) {
            log.warn("Exception during load of field " + fieldId, e);
        }
        return null;
    }

    public ContentField loadContentField(int fieldID, boolean forceLoadFromDB) {
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            if (!forceLoadFromDB) {
                ContentField retContentField = (ContentField) fieldCache
                        .get(CACHE_KEY_PREFIX + fieldID);
                if (retContentField != null) {
                    return retContentField;
                }
            }
            List<JahiaFieldsData> list = dao.loadAllActiveOrStagedFieldEntry(fieldID);
            FastArrayList activeEntryStates = new FastArrayList(53);
            FastHashMap activeValues = new FastHashMap(53);
            JahiaFieldsData data = null;
            if (list.isEmpty()) {
                list = dao.loadAllInactiveVersionedFields((fieldID));
            }

            for (Object aList : list) {
                data = (JahiaFieldsData) aList;
                ContentObjectEntryState entryState = new ContentObjectEntryState(
                        data.getComp_id().getWorkflowState(), data
                        .getComp_id().getVersionId(), data
                        .getComp_id().getLanguageCode());
                activeEntryStates.add(entryState);
                String value = data.getValue();
                if (value == null) {
                    // this ugly hack is necessary because Oracle is incapable of
                    // distinguishing between NULL and empty string values.
                    value = JahiaField.NULL_STRING_MARKER;
                }
                activeValues.put(entryState, value);
            }
            if (data != null) {
                activeEntryStates.setFast(true);
                activeValues.setFast(true);
                return fillCache(data, activeEntryStates, activeValues);
            }

        } catch (JahiaException e) {
            log.warn("Exception during load of field " + fieldID, e);
        }
        return null;
    }

    public List<ContentField> loadContentFields(List<Integer> fieldIDs, EntryLoadRequest loadVersion, boolean forceLoadFromDB) {
        List<ContentField> result = new ArrayList<ContentField>(
                fieldIDs.size());
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            List<Integer> fieldIDsToLoadFromDB = new ArrayList<Integer>(
                    fieldIDs.size());
            if (forceLoadFromDB) {
                fieldIDsToLoadFromDB = fieldIDs;
            } else {
                for (Integer fieldID : fieldIDs) {
                    ContentField retContentField = (ContentField) fieldCache
                            .get(CACHE_KEY_PREFIX + fieldID);
                    if (retContentField != null) {
                        result.add(retContentField);
                    } else {
                        fieldIDsToLoadFromDB.add(fieldID);
                    }
                }
            }

            if (!fieldIDsToLoadFromDB.isEmpty()) {
                Collection<ContentField> fields = createContentFieldsList(dao
                        .loadAllActiveOrStagedFields(fieldIDsToLoadFromDB));
                for (ContentField field : fields) {
                    fieldIDsToLoadFromDB.remove(new Integer(field.getID()));
                }
                result.addAll(fields);
            }

            if (!loadVersion.isCurrent() && !fieldIDsToLoadFromDB.isEmpty()) {
                result.addAll(createContentFieldsList(dao
                        .loadAllInactiveVersionedFields(fieldIDsToLoadFromDB)));
            }

        } catch (JahiaException e) {
            log.warn("Exception during load of fields: " + fieldIDs, e);
        }
        return result;
    }

    private Collection<ContentField> createContentFieldsList(
            List<JahiaFieldsData> fieldsList)
            throws JahiaException {

        return createContentFieldsMap(fieldsList).values();
    }

    private Map<String, ContentField> createContentFieldsMap(
            List<JahiaFieldsData> fieldsList)
            throws JahiaException {
        Map<String, ContentField> contentFields = new HashMap<String, ContentField>(
                fieldsList.size());
        FastArrayList activeEntryStates = new FastArrayList(53);
        FastHashMap activeValues = new FastHashMap(53);
        JahiaFieldsData oldData = null;

        for (JahiaFieldsData data : fieldsList) {
            if (oldData != null
                    && !oldData.getComp_id().getId().equals(data
                    .getComp_id().getId())) {
                activeEntryStates.setFast(true);
                activeValues.setFast(true);
                contentFields.put(oldData.getFieldDefinition().getName(),
                        fillCache(oldData, activeEntryStates, activeValues));
                activeEntryStates = new FastArrayList(53);
                activeValues = new FastHashMap(53);
            }

            ContentObjectEntryState entryState = new ContentObjectEntryState(
                    data.getComp_id().getWorkflowState(), data
                    .getComp_id().getVersionId(), data
                    .getComp_id().getLanguageCode());
            activeEntryStates.add(entryState);
            String value = data.getValue();
            if (value == null) {
                // this ugly hack is necessary because Oracle is incapable of
                // distinguishing between NULL and empty string values.
                value = JahiaField.NULL_STRING_MARKER;
            }
            activeValues.put(entryState, value);
            oldData = data;
        }
        if (oldData != null) {
            activeEntryStates.setFast(true);
            activeValues.setFast(true);
            contentFields.put(oldData.getFieldDefinition().getName(),
                    fillCache(oldData, activeEntryStates, activeValues));
        }

        return contentFields;
    }

    public ContentField loadMetadataByOwnerAndName(String name, ObjectKey objectKey, boolean forceLoadFromDB) {
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            if (!forceLoadFromDB) {
                ContentField retContentField = (ContentField) fieldCache.get(MD_KEY_PREFIX + objectKey + name);
                if (retContentField != null) {
                    return retContentField;
                }
            }
            JahiaObjectPK jpk = new JahiaObjectPK(objectKey.getType(), (objectKey.getIdInType()));
            List<JahiaFieldsData> list = dao.loadAllMetadataActiveOrStagedFieldEntryByOwnerAndName(name, jpk);
            FastArrayList activeEntryStates = new FastArrayList(53);
            FastHashMap activeValues = new FastHashMap(53);
            JahiaFieldsData data = null;
            if (list.isEmpty()) {
                list = dao.loadAllMetadataInactiveVersionedFieldsByOwnerAndName(name, jpk);
            }

            for (Object aList : list) {
                data = (JahiaFieldsData) aList;
                ContentObjectEntryState entryState = new ContentObjectEntryState(
                        data.getComp_id().getWorkflowState(), data
                        .getComp_id().getVersionId(), data
                        .getComp_id().getLanguageCode());
                activeEntryStates.add(entryState);
                String value = data.getValue();
                if (value == null) {
                    // this ugly hack is necessary because Oracle is incapable of
                    // distinguishing between NULL and empty string values.
                    value = JahiaField.NULL_STRING_MARKER;
                }
                activeValues.put(entryState, value);
            }
            if (data != null) {
                activeEntryStates.setFast(true);
                activeValues.setFast(true);
                ContentField f = fillCache(data, activeEntryStates,
                        activeValues);
                fieldCache.put(MD_KEY_PREFIX + objectKey + name, f);
                return f;
            }

        } catch (JahiaException e) {
            log.warn("Exception during load of metadata " + name + " / " + objectKey, e);
        }
        return null;
    }

    public Map<String, ContentField> loadMetadataByOwnerAndNames(
            String[] names, ObjectKey objectKey, EntryLoadRequest loadVersion, boolean forceLoadFromDB) {
        Map<String, ContentField> result = new HashMap<String, ContentField>(names.length);
        try {
            if (fieldCache == null) {
                fieldCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            List<String> fieldNamesToLoadFromDB = new ArrayList<String>(
                    names.length);
            if (!forceLoadFromDB) {
                for (String name : names) {
                    ContentField retContentField = (ContentField) fieldCache
                            .get(MD_KEY_PREFIX + objectKey + name);
                    if (retContentField != null) {
                        result.put(name, retContentField);
                    } else {
                        fieldNamesToLoadFromDB.add(name);
                    }
                }
            } else {
                fieldNamesToLoadFromDB = Arrays.asList(names);
            }

            JahiaObjectPK jpk = new JahiaObjectPK(objectKey.getType(),
                    (objectKey.getIdInType()));

            if (!fieldNamesToLoadFromDB.isEmpty()) {
                Map<String, ContentField> fields = createContentFieldsMap(dao
                        .loadAllMetadataActiveOrStagedFieldEntryByOwnerAndName(
                        fieldNamesToLoadFromDB, jpk));
                for (Map.Entry<String, ContentField> fieldEntry : fields.entrySet()) {
                    fieldNamesToLoadFromDB.remove(fieldEntry.getKey());
                    fieldCache.put(MD_KEY_PREFIX + objectKey + fieldEntry.getKey(), fieldEntry.getValue());
                }
                result.putAll(fields);
            }

            if (!loadVersion.isCurrent() && !fieldNamesToLoadFromDB.isEmpty()) {
                Map<String, ContentField> fields = createContentFieldsMap(dao
                        .loadAllMetadataInactiveVersionedFieldsByOwnerAndName(
                        fieldNamesToLoadFromDB, jpk));
                for (Map.Entry<String, ContentField> fieldEntry : fields.entrySet()) {
                    fieldCache.put(MD_KEY_PREFIX + objectKey + fieldEntry.getKey(), fieldEntry.getValue());
                }
                result.putAll(fields);
            }
        } catch (JahiaException e) {
            log.warn("Exception during load of metadata " + Arrays.toString(names) + " / "
                    + objectKey, e);
        }
        return result;
    }

    public List<ContentObjectEntryState> loadOldEntryStates(int id) {
        List<Object[]> list = dao.findOldEntryState(id);
        List<ContentObjectEntryState> retList = null;
        if (list != null) {
            retList = new FastArrayList(list.size());
            for (Object aList : list) {
                Object[] objects = (Object[]) aList;
                retList.add(new ContentObjectEntryState(((Integer) objects[1]),
                        ((Integer) objects[0]),
                        (String) objects[2]));
            }
            ((FastArrayList)retList).setFast(true);
        }
        return retList;
    }

    public Map<Integer, List<ContentObjectEntryState>> findOldEntryStateForMetadatas(ObjectKey objectKey) {
        Map<Integer, List<ContentObjectEntryState>> entryStates = new HashMap<Integer, List<ContentObjectEntryState>>();
        List<Object[]> list = dao.findOldEntryStateForMetadatas(new JahiaObjectPK(objectKey.getType(),
                (Integer.parseInt(objectKey.getIDInType()))));
        for (Object aList : list) {
            Object[] objects = (Object[]) aList;
            Integer fieldId = (Integer) objects[0];
            List<ContentObjectEntryState> entryStateList = entryStates.get(fieldId);
            if (entryStateList == null) {
                entryStateList = new ArrayList<ContentObjectEntryState>();
                entryStates.put(fieldId, entryStateList);
            }
            entryStateList.add(new ContentObjectEntryState(((Integer) objects[2]),
                    ((Integer) objects[1]),
                    (String) objects[3]));
        }
        return entryStates;
    }


    public String loadValue(ContentField contentField, ContentObjectEntryState entryState) {
        final int workflowState = entryState.getWorkflowState();
        try {
            if (workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                return dao.loadStagingValue((contentField.getID()), (entryState.getVersionID()),
                        entryState.getLanguageCode());
            } else if (workflowState <= EntryLoadRequest.VERSIONED_WORKFLOW_STATE) {
                return dao.loadVersionedValue((contentField.getID()), (entryState.getVersionID()),
                        entryState.getLanguageCode());
            } else {
                return dao.loadPublishedValue((contentField.getID()), (entryState.getVersionID()),
                        entryState.getLanguageCode());
            }
        } catch (ObjectRetrievalFailureException e) {
            log.warn("Field not found in database " + contentField.getID() + " entry :" + entryState.toString(), e);
            return null;
        }
    }

    public void preloadActiveOrStagedFieldsByContainerID(int containerID) {
        try {
            fillCache(dao.loadAllActiveOrStagedFieldInContainer(containerID));
        } catch (JahiaException e) {
            log.warn("Exception during preload of fileds for container " + containerID, e);
        }
    }

    public void preloadActiveOrStagedFieldsByPageID(int pageID) {
        try {
            fillCache(dao.loadAllActiveOrStagedFieldInPage(pageID));
        } catch (JahiaException e) {
            log.warn("Exception during preload of fileds for page " + pageID, e);
        }
    }

    public void preloadActiveOrStagedFieldsByMetadataOwner(ObjectKey metadataOwner) {
        try {
            fillCache(dao.loadAllActiveOrStagedFieldsByMetadataOwner(new JahiaObjectPK(metadataOwner.getType(),
                    metadataOwner.getIdInType())));
        } catch (JahiaException e) {
            log.warn("Exception during preload of fieds by metadata owner " + metadataOwner, e);
        }
    }

    public void preloadStagedFieldsByMetadataOwner(ObjectKey metadataOwner) {
        try {
            fillCache(dao.loadAllStagedFieldsByMetadataOwner(new JahiaObjectPK(metadataOwner.getType(),
                    metadataOwner.getIdInType())));
        } catch (JahiaException e) {
            log.warn("Exception during preload of fieds by metadata owner " + metadataOwner, e);
        }
    }

    public List<Integer> findStagedFieldsByMetadataOwner(ObjectKey metadataOwner) {
        try {
            return dao.findStagedFieldsByMetadataOwner(new JahiaObjectPK(metadataOwner.getType(),
                    metadataOwner.getIdInType()));
        } catch (RuntimeException e) {
            log.warn("Exception during find of staged fields by metadata owner " + metadataOwner, e);
            return null;
        }
    }

    public void purgeField(ContentField id) {
        dao.purgeJahiaFields((id.getID()));
        flushCache(id.getID(), id.getSiteID(), id.getContainerID());
    }

    public void updateField(JahiaField theField, JahiaSaveVersion saveVersion) {
        JahiaFieldsDataPK pk = new JahiaFieldsDataPK();
        pk.setLanguageCode(theField.getLanguageCode());
        if (saveVersion.isStaging()) {
            pk.setWorkflowState((saveVersion.getWorkflowState()));
            pk.setVersionId((0));
        } else {
            if (saveVersion.isVersioned()) {
                dao.backupFieldsData((theField.getID()));
            }
            pk.setWorkflowState((1));
            pk.setVersionId((saveVersion.getVersionID()));
        }
        pk.setId((theField.getID()));

        JahiaFieldsData data = dao.findJahiaFieldsDataByPK(pk);
        boolean isNew = false;
        if (data == null) {
            data = new JahiaFieldsData();
            data.setComp_id(pk);
            isNew = true;
        }
        data.setConnectType((theField.getConnectType()));
        data.setContainerId((theField.getctnid()));
        final JahiaFieldsDef fieldDefinition = definitionDAO.loadDefinition((theField.getFieldDefID()));
        data.setFieldDefinition(fieldDefinition);
        if (theField.getMetadataOwnerObjectKey() != null) {
            ObjectKey objKey = theField.getMetadataOwnerObjectKey();
            data.setMetadataOwnerId((objKey.getIdInType()));
            data.setMetadataOwnerType(objKey.getType());
        }
        data.setPageId((theField.getPageID()));
        data.setProperties(theField.getProperties());
        data.setSiteId((theField.getJahiaID()));
        data.setType((theField.getType()));
        String value = theField.getValue();
        if (value.equals("")) {
            value = "<empty>";
        }
        if (value.length() > 250) {
            value = "<empty>";
        }
        data.setValue(value);
        if (isNew) {
            dao.saveNewVersion(data);
        } else {
            dao.update(data);
        }
        theField.setID(data.getComp_id().getId());
        flushCache(theField.getID(), data.getSiteId() != null ? data.getSiteId() : 0, theField.getctnid());
    }

    public synchronized void updateValue(ContentField contentField, ContentObjectEntryState newEntryState, String value, boolean cache) {
        JahiaFieldsData fieldsData;
        if (newEntryState.getVersionID() != 0) {
            fieldsData = findJahiaFieldsDataWithoutVersionId(contentField, newEntryState);
        } else {
            fieldsData = findJahiaFieldsData(contentField, newEntryState);
        }
        if (fieldsData != null) {
            fieldsData.setValue(value);

            if (fieldsData.getComp_id().getVersionId() != newEntryState.getVersionID()) {
                try {
                    JahiaFieldsData fieldsData2 = (JahiaFieldsData) fieldsData.clone();
                    dao.deleteJahiaField(fieldsData);
                    fieldsData2.getComp_id().setVersionId((newEntryState.getVersionID()));
                    dao.saveNewVersion(fieldsData2);
                    fieldsData = fieldsData2;
                } catch (CloneNotSupportedException e) {
                    log.error("Error updating field", e);
                }
            } else {
                dao.update(fieldsData);
            }
            flushCache(contentField.getID(), fieldsData.getSiteId() != null ? fieldsData.getSiteId() : 0, contentField.getContainerID());
            if (cache) {
                this.fieldCache.put(CACHE_KEY_PREFIX + contentField.getID(), contentField);
            }
        }
    }

    private JahiaFieldsData findJahiaFieldsDataWithoutVersionId(ContentField contentField, ContentObjectEntryState newEntryState) {
        return dao.findJahiaFieldsDataByIdAndWorkflowStateAndLanguage((contentField.getID()),
                (newEntryState.getWorkflowState()),
                newEntryState.getLanguageCode());
    }

    private void fillCache(List<JahiaFieldsData> list) throws JahiaException {
        if (!list.isEmpty()) {
            JahiaFieldsData data = list.get(0);
            Integer fieldId = data.getComp_id().getId();
            FastArrayList activeEntryStates = new FastArrayList(53);
            FastHashMap activeValues = new FastHashMap(53);
            for (JahiaFieldsData fieldsData : list) {
                if (!fieldId.equals(fieldsData.getComp_id().getId())) {
                    activeEntryStates.setFast(true);
                    activeValues.setFast(true);
                    fillCache(data, activeEntryStates, activeValues);
                    activeEntryStates = new FastArrayList(53);
                    activeValues = new FastHashMap(53);
                }
                data = fieldsData;
                ContentObjectEntryState entryState = new ContentObjectEntryState(
                        data.getComp_id().getWorkflowState(),
                        data.getComp_id().getVersionId(),
                        data.getComp_id().getLanguageCode());
                activeEntryStates.add(entryState);
                String value = data.getValue();
                if (value == null) {
                    // this ugly hack is necessary because Oracle is incapable of
                    // distinguishing between NULL and empty string values.
                    value = JahiaField.NULL_STRING_MARKER;
                }
                activeValues.put(entryState, value);
                fieldId = data.getComp_id().getId();
            }
            activeEntryStates.setFast(true);
            activeValues.setFast(true);
            fillCache(data, activeEntryStates, activeValues);
        }
    }

    private ContentField fillCache(JahiaFieldsData data, List<ContentObjectEntryState> activeEntryStates, Map<ContentObjectEntryState, String> activeValues) throws JahiaException {
        if (fieldCache == null) {
            fieldCache = cacheService.createCacheInstance(CACHE_NAME);
        }

        final ContentFieldTools instance = ContentFieldTools.getInstance();
        int jahiaID = 0;
        if (data.getSiteId() != null) {
            // Metadata field are not bind to one site
            jahiaID = data.getSiteId();
        }
        final ContentField contentFieldInstance = instance.createContentFieldInstance(
                data.getComp_id().getId(),
                jahiaID,
                data.getPageId(),
                data.getContainerId(),
                data.getFieldDefinition().getId(),
                data.getType(),
                data.getConnectType(),
                -1,
                activeEntryStates,
                activeValues);
        if (data.getMetadataOwnerId() != null) {
            try {
                ObjectKey key = new JahiaObjectPK(data.getMetadataOwnerType(), data.getMetadataOwnerId()).toObjectKey();
                contentFieldInstance.setMetadataOwnerObjectKey(key);
            } catch (Exception t) {
            }
        } else {
            data = dao.fillProperties(data);
        }
        contentFieldInstance.setProperties(data.getProperties());
        this.fieldCache.put(CACHE_KEY_PREFIX + data.getComp_id().getId(),
                contentFieldInstance);
        return contentFieldInstance;
    }

    private JahiaFieldsData findJahiaFieldsData(ContentField contentField, ContentObjectEntryState entryState) {
        return dao.findJahiaFieldsDataByPK(new JahiaFieldsDataPK((contentField.getID()),
                (entryState.getVersionID()),
                (entryState.getWorkflowState()),
                entryState.getLanguageCode()));
    }

    public void saveProperties(ContentField cf, Map<Object, Object> properties) {
        dao.saveProperties((cf.getID()), properties);
        cf = loadContentField(cf.getID());
        cf.setProperties(properties);
        this.fieldCache.put(CACHE_KEY_PREFIX + cf.getID(), cf);
    }

    public List<ContentField> loadContentFieldInContainer(int ctnId) {
        try {
            List<JahiaFieldsData>list = dao.loadAllActiveOrStagedFieldInContainer((ctnId));
            FastArrayList activeEntryStates = new FastArrayList(53);
            FastHashMap activeValues = new FastHashMap(53);
            JahiaFieldsData oldData = null;
            List<ContentField> retList = new FastArrayList(53);

            for (JahiaFieldsData data : list) {
                if (oldData != null
                        && oldData.getComp_id().getId().equals(data
                        .getComp_id().getId())) {
                    int jahiaID = 0;
                    if (oldData.getSiteId() != null) {
                        // Metadata field are not bind to one site
                        jahiaID = oldData.getSiteId();
                    }
                    activeEntryStates.setFast(true);
                    activeValues.setFast(true);
                    final ContentField contentFieldInstance = ContentFieldTools
                            .getInstance().createContentFieldInstance(
                            oldData.getComp_id().getId(),
                            jahiaID,
                            oldData.getPageId(),
                            oldData.getContainerId(),
                            oldData.getFieldDefinition().getId()
                            ,
                            oldData.getType(),
                            oldData.getConnectType(),
                            -1,
                            activeEntryStates, activeValues);
                    oldData = dao.fillProperties(data);
                    if (data.getMetadataOwnerId() != null) {
                        try {
                            ObjectKey key = new JahiaObjectPK(data
                                    .getMetadataOwnerType(), data
                                    .getMetadataOwnerId()).toObjectKey();
                            contentFieldInstance.setMetadataOwnerObjectKey(key);
                        } catch (Exception t) {
                        }
                    }
                    contentFieldInstance.setProperties(oldData.getProperties());
                    retList.add(contentFieldInstance);
                }
                ContentObjectEntryState entryState = new ContentObjectEntryState(
                        data.getComp_id().getWorkflowState(), data
                        .getComp_id().getVersionId(), data
                        .getComp_id().getLanguageCode());
                activeEntryStates.add(entryState);
                String value = data.getValue();
                if (value == null) {
                    // this ugly hack is necessary because Oracle is incapable of
                    // distinguishing between NULL and empty string values.
                    value = JahiaField.NULL_STRING_MARKER;
                }
                activeValues.put(entryState, value);
                oldData = data;
            }
            ((FastArrayList)retList).setFast(true);

            return retList;
        } catch (JahiaException e) {
            log.warn("Exception during load of fields in container " + ctnId, e);
        }
        return null;
    }

    public List<UsageEntry> findUsages(String sourceUri, boolean onlyLockedUsages, ProcessingContext jParams) {
//        String sourceUriExtendedSearch = (sourceUri.endsWith("/"))?sourceUri+"%":sourceUri+"/%";
        List<Object[]> list = dao.findUsages(sourceUri, onlyLockedUsages);
        List<UsageEntry> retList = new ArrayList<UsageEntry>(list.size());
        for (Object[] objects : list) {
            UsageEntry curUsage;
            try {
                curUsage = new UsageEntry(((Integer) objects[0]), ((Integer) objects[2]),
                        ((Integer) objects[1]), ((String) objects[3]),
                        ((String) objects[4]), jParams);
            } catch (JahiaException e) {
                curUsage = null;
                log.error("Error while trying to find field usage of sourceUri=" + sourceUri, e);
            }
            if (curUsage != null) {
                retList.add(curUsage);
            }
        }
        return retList;
    }

    public boolean isUsed(int siteId, String sourceUri, boolean onlyLockedUsages) {
        return dao.countUsages((siteId), sourceUri, onlyLockedUsages) > 0;
    }

    public int countUsages(int siteId, String sourceUri, boolean onlyLockedUsages) {
        return dao.countUsages((siteId), sourceUri, onlyLockedUsages);
    }

    public int[] getParentIds(int id, EntryLoadRequest req) {
        Object[] i = dao.getParentIds((id));
        if (i == null && (req == null || req.isWithDeleted() || req.isWithMarkedForDeletion())) {
            i = dao.getDeletedParentIds((id));
        }
        if (i != null) {
            if (i[1] == null) {
                return new int[]{((Integer) i[0]), -1};
            } else {
                return new int[]{((Integer) i[0]), ((Integer) i[1])};
            }
        }
        return new int[]{-1, -1};
    }

    public int[] getSubPageId(int id, EntryLoadRequest req) {
        int ws = req.getWorkflowState();
        if (ws == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
            ws++; // waiting states
        }
        Integer[] i = dao.getSubPageId((id), (ws));
        if (i != null) {
            return new int[]{i[0], i[1]};
        }
        return new int[]{-1, -1};

    }

    public List<Integer> getFieldIDsOnPagesHavingAcls(Set<Integer> pageIDs, Set<Integer> aclIDs) {
        return dao.getFieldIDsOnPagesHavingAcls(pageIDs, aclIDs);
    }

    public boolean hasActiveFieldsWithoutContainer() {
        return dao.hasActiveFieldsWithoutContainer();
    }

    public <E> List<E> executeQuery(String queryString, Map<String, Object> parameters) {
        return dao.executeQuery(queryString, parameters);
    }

    public Map<String, String> getVersions(int site, String lang) {
        return dao.getVersions(site, lang);
    }

    public List<Integer> findFieldIdByPropertyNameAndValue(String name, String value) {
        return dao.findFieldIdByPropertyNameAndValue(name, value);
    }

    public List<Object[]> getFieldPropertiesByName(String name) {
        return dao.getFieldPropertiesByName(name);
    }

    public void removeFieldProperty(int id, String name) {
        dao.deleteFieldProperty(id, name);
    }


}

