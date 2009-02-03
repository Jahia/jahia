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

package org.jahia.services.cache;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.engines.pages.PageProperties_Engine;
import org.jahia.engines.shared.Page_Field;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.clusterservice.ClusterCacheMessage;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.workflow.WorkflowService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The CacheEventListener class listen to events in jahia and flush contianer html and skeleton caches in consequences.
 * @author thomas.draier at jahia.com
 * @author cedric.mailleux at jahia.com
 * @version 5.1
 * @since 5.1
 */
public class CacheEventListener extends JahiaEventListener {
// ------------------------------ FIELDS ------------------------------

    private static transient Category logger = Logger.getLogger(CacheEventListener.class);

    private ContainerHTMLCache chtmlCache;

    private SkeletonCache skeletonCache;
    private JahiaFieldXRefManager fieldXRefManager;

// --------------------- GETTER / SETTER METHODS ---------------------

    public ContainerHTMLCache getChtmlCache() {
        if (chtmlCache == null) {
            try {
                chtmlCache = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            } catch (JahiaInitializationException e) {
                logger.error("Cannot initialize container html cache",e);
            }
        }
        return chtmlCache;
    }

    private SkeletonCache getSkeletonCache() {
        if (skeletonCache == null) {
            try {
                skeletonCache = ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance();
            } catch (JahiaInitializationException e) {
                logger.error("Cannot initialize skeleton cache",e);
            }
        }
        return skeletonCache;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JahiaEventListenerInterface ---------------------


    public void aggregatedContentActivation(JahiaEvent je) {
        logger.debug("Aggregated Content Activation event received");
        List<ContentActivationEvent> allEvents = (List<ContentActivationEvent>) je.getObject();
        List<Object[]> myEvents = new ArrayList<Object[]>();

        Set<String> viewed = new HashSet<String>();
        List<Locale> locales = null;
        for (ContentActivationEvent ce : allEvents) {
            ContentObjectKey object = (ContentObjectKey) ce.getObjectKey();
            addObjectKey(object,
                         ce.getLanguageCodes(),
                         myEvents,
                         viewed,
                         new ContentPageKey(ce.getContentObject().getPageID()));
            try {
                locales = ServicesRegistry.getInstance()
                        .getJahiaSitesService()
                        .getSite(ce.getProcessingContext().getSiteID())
                        .getLanguageSettingsAsLocales(true);
            } catch (JahiaException e) {
                logger.error(e);
            }
        }
        for (Object[] objs : myEvents) {
            final String locale = (String) objs[1];
            final ContentObjectKey object = (ContentObjectKey) objs[0];
            invalidate(object, ProcessingContext.NORMAL, locale);
            invalidate(object, ProcessingContext.COMPARE, locale);
            invalidate(object, ProcessingContext.PREVIEW, locale);
            invalidate(object, ProcessingContext.EDIT, locale);
            final ContentObjectKey contentObjectKey = (ContentObjectKey) objs[2];
            invalidateSkeleton(contentObjectKey, ProcessingContext.COMPARE, locale);
            invalidateSkeleton(contentObjectKey, ProcessingContext.NORMAL, locale);
            invalidateSkeleton(contentObjectKey, ProcessingContext.PREVIEW, locale);
            invalidateSkeleton(contentObjectKey, ProcessingContext.EDIT, locale);
            try {
                ContentPage contentPage = (ContentPage) ContentObject.getContentObjectInstance(contentObjectKey);
                if (!contentPage.hasArchivedEntryStateInLocale(contentPage.getActiveVersionID(), true, locale)) {
                    // page is new in this language flush all lnaguage of the site
                    if (locales != null) {
                        for (Locale locale1 : locales) {
                            String locale2 = locale1.toString();
                            invalidateSkeleton(contentObjectKey, ProcessingContext.COMPARE, locale2);
                            invalidateSkeleton(contentObjectKey, ProcessingContext.NORMAL, locale2);
                            invalidateSkeleton(contentObjectKey, ProcessingContext.PREVIEW, locale2);
                            invalidateSkeleton(contentObjectKey, ProcessingContext.EDIT, locale2);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.error(e);
            } catch (JahiaException e) {
                logger.error(e);
            }
        }
    }

    public void aggregatedObjectChanged(JahiaEvent je) {
        logger.debug("Aggregated Object Changed event received");
        List<WorkflowEvent> allEvents = (List<WorkflowEvent>) je.getObject();
        List<Object[]> myEvents = new ArrayList<Object[]>();

        Set<String> viewed = new HashSet<String>();

        for (WorkflowEvent we : allEvents) {
            ContentObject object = (ContentObject) we.getObject();
            if (object != null) {
                String languageCode = we.getLanguageCode();
                ContentObjectKey objectKey = (ContentObjectKey) object.getObjectKey();
                Set<String> languageCodes = getLanguageCodes(languageCode, object);
                addObjectKey(objectKey, languageCodes, myEvents, viewed, new ContentPageKey(object.getPageID()));
            }
        }
        for (Object[] objs : myEvents) {
            invalidate((ContentObjectKey) objs[0], ProcessingContext.EDIT, (String) objs[1]);
            invalidate((ContentObjectKey) objs[0], ProcessingContext.PREVIEW, (String) objs[1]);
            invalidate((ContentObjectKey) objs[0], ProcessingContext.COMPARE, (String) objs[1]);
            invalidateSkeleton((ContentObjectKey) objs[2], ProcessingContext.COMPARE, (String) objs[1]);
            invalidateSkeleton((ContentObjectKey) objs[2], ProcessingContext.EDIT, (String) objs[1]);
            invalidateSkeleton((ContentObjectKey) objs[2], ProcessingContext.PREVIEW, (String) objs[1]);
        }
    }

    public void containerDeleted(JahiaEvent je) {
        JahiaContainer container = (JahiaContainer) je.getObject();
        if (container != null) {
            Set<String> languageCodes =
                    getLanguageCodes(je.getProcessingContext().getLocale().toString(), container.getContentContainer());
            final ContentPageKey contentPageKey = new ContentPageKey(container.getPageID());
            for (String locale : languageCodes) {
                invalidateSkeleton(contentPageKey, ProcessingContext.EDIT, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.PREVIEW, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.COMPARE, locale);
            }
        }
    }

    public void containerListPropertiesSet(JahiaEvent je) {
        JahiaContainerList containerList = (JahiaContainerList) je.getObject();
        if (containerList != null) {
            final ContentContainerList list = containerList.getContentContainerList();
            ContentObjectKey key = (ContentObjectKey) list.getObjectKey();
            ContentPageKey contentPageKey = new ContentPageKey(containerList.getPageID());
            Set<String> languageCodes =
                    getLanguageCodes(je.getProcessingContext().getLocale().toString(), list);
            for (String locale : languageCodes) {
                invalidate(key, ProcessingContext.EDIT, locale);
                invalidate(key, ProcessingContext.NORMAL, locale);
                invalidate(key, ProcessingContext.PREVIEW, locale);
                invalidate(key, ProcessingContext.COMPARE, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.EDIT, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.NORMAL, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.PREVIEW, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.COMPARE, locale);
            }
        }
    }

    public void contentObjectUpdated(JahiaEvent je) {
        //for time base publishnig event this is very dirty we need a specific event for that I think
        if (je.getSource() instanceof JahiaObjectManager) {
            ContentObject container = (ContentObject) je.getObject();
            Set<String> languageCodes = getLanguageCodes(je.getProcessingContext().getLocale().toString(), container);
            if (container != null) {
                if (container instanceof ContentContainer) {
                    final ContentContainerKey containerKey = new ContentContainerKey(container.getID());
                    final ContentPageKey pageKey = new ContentPageKey(container.getPageID());
                    final ContentContainerListKey contentListKey =
                            (ContentContainerListKey) ((ContentObjectKey) container.getObjectKey()).getParent(
                                    EntryLoadRequest.STAGED);
                    for (String locale : languageCodes) {
                        invalidate(containerKey, ProcessingContext.EDIT, locale);
                        invalidate(containerKey, ProcessingContext.PREVIEW, locale);
                        invalidate(containerKey, ProcessingContext.COMPARE, locale);
                        invalidate(containerKey, ProcessingContext.NORMAL, locale);
                        invalidate(contentListKey, ProcessingContext.EDIT, locale);
                        invalidate(contentListKey, ProcessingContext.PREVIEW, locale);
                        invalidate(contentListKey, ProcessingContext.COMPARE, locale);
                        invalidate(contentListKey, ProcessingContext.NORMAL, locale);
                        invalidateSkeleton(pageKey, ProcessingContext.EDIT, locale);
                        invalidateSkeleton(pageKey, ProcessingContext.PREVIEW, locale);
                        invalidateSkeleton(pageKey, ProcessingContext.COMPARE, locale);
                        invalidateSkeleton(pageKey, ProcessingContext.NORMAL, locale);
                    }
                }
                else if (container instanceof ContentPage) {
                    invalidatePage(container, languageCodes, true);
                }
            }
        } else if (je.getSource() instanceof PageProperties_Engine
                || je.getSource() instanceof Page_Field) {
            ContentObject container = (ContentObject) je.getObject();
            Set<String> languageCodes = getLanguageCodes(je
                    .getProcessingContext().getLocale().toString(), container);
            if (container != null) {
                invalidatePage(container, languageCodes, true);
                // invalidate bigtexts referencing pages
                try {
                    if (fieldXRefManager == null) {
                        fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
                    }
                    Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.PAGE + container.getID());
                    for (JahiaFieldXRef fieldXRef : c) {
                        ContentField contentField = ContentField.getField(fieldXRef.getComp_id().getFieldId());
                        for (String languageCode : languageCodes) {
                            invalidate((ContentObjectKey) contentField.getParent(EntryLoadRequest.STAGED).getObjectKey(), ProcessingContext.EDIT, languageCode);
                            invalidate((ContentObjectKey) contentField.getParent(EntryLoadRequest.STAGED).getObjectKey(), ProcessingContext.COMPARE, languageCode);
                            invalidate((ContentObjectKey) contentField.getParent(EntryLoadRequest.STAGED).getObjectKey(), ProcessingContext.PREVIEW, languageCode);
                            invalidate((ContentObjectKey) contentField.getParent(EntryLoadRequest.STAGED).getObjectKey(), ProcessingContext.NORMAL, languageCode);
                        }
                    }
                } catch (JahiaException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void invalidatePage(ContentObject container, Set<String> languageCodes, boolean flushLive) {
        final ContentPageKey contentPageKey = new ContentPageKey(container.getPageID());
        // get Parent Container and container List
        ContentObjectKey objectKey = contentPageKey.getParent(EntryLoadRequest.STAGED);
        if (objectKey != null && ContentFieldKey.FIELD_TYPE.equals(objectKey.getType())) {
            objectKey = objectKey.getParent(EntryLoadRequest.STAGED);
        }
        if (objectKey != null && ContentContainerKey.CONTAINER_TYPE.equals(objectKey.getType())) {
            ContentContainerListKey cclk =
                    (ContentContainerListKey) objectKey.getParent(EntryLoadRequest.STAGED);
            for (String locale : languageCodes) {
                invalidateSkeleton(contentPageKey, ProcessingContext.EDIT, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.PREVIEW, locale);
                invalidateSkeleton(contentPageKey, ProcessingContext.COMPARE, locale);
                invalidate(objectKey, ProcessingContext.EDIT, locale);
                invalidate(objectKey, ProcessingContext.PREVIEW, locale);
                invalidate(objectKey, ProcessingContext.COMPARE, locale);
                invalidate(cclk, ProcessingContext.EDIT, locale);
                invalidate(cclk, ProcessingContext.PREVIEW, locale);
                invalidate(cclk, ProcessingContext.COMPARE, locale);
                if (flushLive) {
                    invalidateSkeleton(contentPageKey, ProcessingContext.NORMAL, locale);
                    invalidate(objectKey, ProcessingContext.NORMAL, locale);
                    invalidate(cclk, ProcessingContext.NORMAL, locale);
                }
            }
        }
    }

    public void rightsSet(JahiaEvent je) {
        try {
            ServicesRegistry.getInstance().getCacheKeyGeneratorService().rightsUpdated();
            final Object source = je.getSource();
            if (source instanceof JahiaPage) {
                JahiaPage jahiaPage = (JahiaPage) source;
                int pageID = jahiaPage.getID();
                List<Locale> languageSettings = je.getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                try {
                    ContentObject object = jahiaPage.getContentPage().getParent(EntryLoadRequest.STAGED);
                    if (object != null) {
                        ContentObject parent = object.getParent(EntryLoadRequest.STAGED);
                        for (Locale languageSetting : languageSettings) {
                            final String s = languageSetting.toString();
                            chtmlCache.invalidateContainerEntriesInAllModes((new ContentContainerKey(parent.getID())).toString(),
                                                                            s);
                            skeletonCache
                                    .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                         s);
                        }
                        Set<ContentObject> pickerObjects = parent.getPickerObjects();
                        if (pickerObjects != null && pickerObjects.size() > 0) {
                            for (ContentObject pickerObject : pickerObjects) {
                                for (Locale languageSetting : languageSettings) {
                                    final String s = languageSetting.toString();
                                    chtmlCache.invalidateContainerEntriesInAllModes((new ContentContainerKey(
                                            pickerObject.getID())).toString(), s);
                                    skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(pickerObject.getPageID())).toString(), s);
                                }
                            }
                        }
                    }
                } catch (JahiaException e) {
                    logger.warn("Could not found the page parent field");
                }
            } else if (source instanceof JahiaContainer) {
                JahiaContainer jahiaContainer = (JahiaContainer) source;
                    int pageID = jahiaContainer.getPageID();
                        int parentContainerListID = jahiaContainer.getListID();
                        List<Locale> languageSettings = je.getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                        if (parentContainerListID > 0) {
                            if (logger.isDebugEnabled()) logger.debug("ACL [" +
                                                                      je.getObject() +
                                                                      "] was altered so triggering invalidation of parent container :" +
                                                                      parentContainerListID);
                            for (Locale languageSetting : languageSettings) {
                                final String s = languageSetting.toString();
                                chtmlCache
                                        .invalidateContainerEntriesInAllModes((new ContentContainerListKey(
                                                parentContainerListID)).toString(), s);

                                skeletonCache
                                        .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                             s);
                            }
                        }
                        Set<ContentObject> pickerObjects = jahiaContainer.getContentContainer().getPickerObjects();
                        if (pickerObjects != null && pickerObjects.size() > 0) {
                            for (ContentObject pickerObject : pickerObjects) {
                                for (Locale languageSetting : languageSettings) {
                                    final String s = languageSetting.toString();
                                    chtmlCache.invalidateContainerEntriesInAllModes((new ContentContainerKey(pickerObject.getID())).toString(),
                                                                                    s);
                                    skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(pickerObject.getPageID())).toString(), s);
                                }
                            }
                        }

            } else if (source instanceof JahiaContainerList) {
                JahiaContainerList jahiaContainerList = (JahiaContainerList) source;
                List<ContentObject> list = jahiaContainerList.getContentContainerList()
                            .getChilds(JahiaAdminUser.getAdminUser(0), EntryLoadRequest.STAGED);
                    List<Locale> languageSettings = je.getProcessingContext().getSite().getLanguageSettingsAsLocales(true);
                    for (ContentObject contentObject : list) {
                        if (contentObject instanceof ContentContainer) {
                            ContentContainer contentContainer = (ContentContainer) contentObject;
                            ContentObject ctnList = contentContainer.getParent(EntryLoadRequest.STAGED);
                            int pageID = ctnList.getPageID();
                            for (Locale languageSetting : languageSettings) {
                                final String s = languageSetting.toString();
                                chtmlCache.invalidateContainerEntriesInAllModes(contentContainer.getObjectKey().toString(),
                                                                                s);
                                chtmlCache.invalidateContainerEntriesInAllModes(ctnList.getObjectKey().toString(),
                                                                                s);
                                skeletonCache
                                        .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                             s);
                            }
                            Set<ContentObject> pickerObjects = contentContainer.getPickerObjects();
                            if (pickerObjects != null && pickerObjects.size() > 0) {
                                for (ContentObject pickerObject : pickerObjects) {
                                    for (Locale languageSetting : languageSettings) {
                                        final String s = languageSetting.toString();
                                        chtmlCache
                                                .invalidateContainerEntriesInAllModes((new ContentContainerKey(
                                                        pickerObject.getID())).toString(), s);
                                        skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(pickerObject.getPageID())).toString(), s);
                                    }
                                }
                            }
                        }
            }
            }
            informCluster(ClusterCacheMessage.KEYGENERATOR_ACLUPDATE_EVENT);
        } catch (JahiaInitializationException e) {
            logger.error(e);
        } catch (JahiaException e) {
            logger.error(e);
        }
    }

    public void siteAdded(JahiaEvent je) {
        try {
            ServicesRegistry.getInstance().getCacheKeyGeneratorService().rightsUpdated();
            informCluster(ClusterCacheMessage.KEYGENERATOR_ACLUPDATE_EVENT);
        } catch (JahiaInitializationException e) {
            logger.error(e);
        }
    }

    public void siteDeleted(JahiaEvent je) {
        try {
            ServicesRegistry.getInstance().getCacheKeyGeneratorService().rightsUpdated();
            informCluster(ClusterCacheMessage.KEYGENERATOR_ACLUPDATE_EVENT);
        } catch (JahiaInitializationException e) {
            logger.error(e);
        }
    }

    private void informCluster(int msgType) {
        ClusterService clusterService = ServicesRegistry.getInstance()
                .getClusterService();
        ClusterMessage msg = new ClusterMessage(new ClusterCacheMessage(null,
                msgType, null, null));
        clusterService.sendMessage(msg);
    }
    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param je JahiaEvent
     */
    public void contentObjectUndoStaging (ContentUndoStagingEvent je) {
        handleObjectChanged(je);
        if (je==null){
            return;
        }
        ContentObject eventObject = (ContentObject)je.getObject();
        if (eventObject!=null){
            WorkflowService.getInstance().clearLanguageStateEntries((ContentObjectKey)eventObject.getObjectKey());
            WorkflowService.getInstance().flushCacheForObjectChanged((ContentObjectKey)eventObject.getObjectKey());
        }
    }

    /**
     * Event fired on content object restore version
     *
     * @param je JahiaEvent
     */
    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent je) {
        handleObjectChanged(je);
    }

    protected void handleObjectChanged(JahiaEvent je){
        if (je==null){
            return;
        }

        ContentObject contentObject = null;
        Object eventObject = je.getObject();
        if ( eventObject instanceof JahiaContainer ){
            JahiaContainer jahiaContainer = (JahiaContainer)eventObject;
            try {
                contentObject = ContentContainer.getContainer(jahiaContainer.getID());
            } catch ( Exception t ){
                logger.debug("Error retrieving ContentContainer from JahiaContainer",t);
            }
        } else {
            contentObject = (ContentObject) je.getObject();
        }

        if (contentObject != null){

            ProcessingContext context = je.getProcessingContext();
            if (context == null){
                context = Jahia.getThreadParamBean();
            }
            if (context == null){
                return;
            }

            Set<String> languageCodes = getLanguageCodes(je.getProcessingContext().getLocale().toString(), contentObject);
            if (contentObject instanceof ContentContainer) {
                final ContentContainerKey contentContainerKey = new ContentContainerKey(contentObject.getID());
                final ContentContainerListKey contentListKey =
                        (ContentContainerListKey) ((ContentObjectKey) contentObject.getObjectKey()).getParent(
                                EntryLoadRequest.STAGED);
                for (String locale : languageCodes) {
                    invalidate(contentContainerKey, ProcessingContext.EDIT, locale);
                    invalidate(contentContainerKey, ProcessingContext.PREVIEW, locale);
                    invalidate(contentContainerKey, ProcessingContext.COMPARE, locale);
                    invalidate(contentContainerKey, ProcessingContext.NORMAL, locale);
                    invalidate(contentListKey, ProcessingContext.EDIT, locale);
                    invalidate(contentListKey, ProcessingContext.PREVIEW, locale);
                    invalidate(contentListKey, ProcessingContext.COMPARE, locale);
                    invalidate(contentListKey, ProcessingContext.NORMAL, locale);
                }
            }
            else if (contentObject instanceof ContentPage) {
                invalidatePage(contentObject, languageCodes, false);
            }
        }
    }

    public void groupAdded(JahiaEvent je) {
        try {
                ServicesRegistry.getInstance().getCacheKeyGeneratorService().start();
                informCluster(ClusterCacheMessage.KEYGENERATOR_RESTART_EVENT);
            } catch (JahiaInitializationException e) {
                logger.warn("Jahia is not correctly initialized");
            }
    }

    public void groupDeleted(JahiaEvent je) {
        try {
                ServicesRegistry.getInstance().getCacheKeyGeneratorService().start();
                informCluster(ClusterCacheMessage.KEYGENERATOR_RESTART_EVENT);
            } catch (JahiaInitializationException e) {
                logger.warn("Jahia is not correctly initialized");
            }
    }

    public void groupUpdated(JahiaEvent je) {
        try {
                ServicesRegistry.getInstance().getCacheKeyGeneratorService().start();
                informCluster(ClusterCacheMessage.KEYGENERATOR_RESTART_EVENT);
            } catch (JahiaInitializationException e) {
                logger.warn("Jahia is not correctly initialized");
            }
    }

    // -------------------------- OTHER METHODS --------------------------

    private void addObjectKey(ContentObjectKey objectKey,
                              Set<String> languageCodes,
                              List<Object[]> myEvents,
                              Set<String> viewed,
                              ContentPageKey contentPageKey) {
        if (objectKey != null && ContentPageKey.PAGE_TYPE.equals(objectKey.getType())) {
            objectKey = objectKey.getParent(EntryLoadRequest.STAGED);
        }
        if (objectKey != null && ContentFieldKey.FIELD_TYPE.equals(objectKey.getType())) {
            objectKey = objectKey.getParent(EntryLoadRequest.STAGED);
        }
        if (objectKey != null && ContentContainerKey.CONTAINER_TYPE.equals(objectKey.getType())) {
            ContentContainerListKey cclk = (ContentContainerListKey) objectKey.getParent(EntryLoadRequest.STAGED);
            for (String languageCode : languageCodes) {
                String key = objectKey + languageCode;
                if (!viewed.contains(key)) {
                    myEvents.add(new Object[]{objectKey, languageCode, contentPageKey});
                    viewed.add(key);
                }
                key = cclk + languageCode;
                if (!viewed.contains(key)) {
                    myEvents.add(new Object[]{cclk, languageCode, contentPageKey});
                    viewed.add(key);
                }
            }
        }
        if (objectKey != null
                && ContentContainerListKey.CONTAINERLIST_TYPE.equals(objectKey.getType())) {
            for (String languageCode : languageCodes) {
                String key = objectKey + languageCode;
                if (!viewed.contains(key)) {
                    myEvents.add(new Object[]{objectKey, languageCode, contentPageKey});
                    viewed.add(key);
                }
            }
        }
    }

    private Set<String> getLanguageCodes(String languageCode, ContentObject object) {
        Set<String> languageCodes = new HashSet<String>();
        if (ContentField.SHARED_LANGUAGE.equals(languageCode)) {
            try {
                List<Locale> locales = ServicesRegistry.getInstance()
                        .getJahiaSitesService()
                        .getSite(object.getSiteID())
                        .getLanguageSettingsAsLocales(true);
                for (Locale locale : locales) {
                    languageCodes.add(locale.toString());
                }
            } catch (JahiaException e) {
                logger.error("Error in language codes",e);
            }
        }
        else {
            languageCodes.add(languageCode);
        }
        return languageCodes;
    }

    private void invalidate(ContentObjectKey object, String mode, String locale) {
        logger.debug("Invalide container " + object + " in mode " + mode + " for language " + locale);
        getChtmlCache().invalidateContainerEntries(object != null ? object.toString() : null, mode, locale);
    }

    private void invalidateSkeleton(ContentObjectKey object, String mode, String locale) {
        logger.debug("Invalide skeleton " + object + " in mode " + mode + " for language " + locale);
        getSkeletonCache().invalidateSkeletonEntries(object != null ? object.toString() : null, mode, locale);
    }
}
