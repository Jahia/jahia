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
//
//  JahiaEventListener
//  EV      12.01.2001
//
package org.jahia.data.events;

import java.util.Set;

import org.jahia.services.notification.NotificationEvent;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

public class JahiaEventListener implements JahiaEventListenerInterface {
    private static transient Logger logger = Logger
            .getLogger(JahiaEventListener.class);

    protected Set<String> skipEvents;
    
    protected Set<String> handleEvents;
    
    protected void log(String eventType, JahiaEvent evt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received event '" + eventType + "'. Details: " + evt);
        }
    }

    protected boolean needToHandleEvent(String eventName) {
        return (skipEvents == null || !skipEvents.contains(eventName))
                && (handleEvents == null || handleEvents.contains(eventName));
    }
    
    public void addContainerEngineAfterInit(JahiaEvent je) {
        log("addContainerEngineAfterInit", je);
    }

    public void addContainerEngineAfterSave(JahiaEvent je) {
        log("addContainerEngineAfterSave", je);
    }

    public void addContainerEngineBeforeSave(JahiaEvent je) {
        log("addContainerEngineBeforeSave", je);
    }

    public void afterGroupActivation(ContentActivationEvent je) {
        log("afterGroupActivation", je);
    }

    public void afterServicesLoad(JahiaEvent je) {
        log("afterServicesLoad", je);
    }

    public void aggregatedContentActivation(JahiaEvent je) {
        log("aggregatedContentActivation", je);
    }
    public void aggregatedContentWorkflowStatusChanged(JahiaEvent je) {
        log("aggregatedContentWorkflowStatusChanged", je);
    }

    public void aggregatedContentObjectCreated(JahiaEvent je) {
        log("aggregatedContentObjectCreated", je);
    }

    /**
     * Event fired to notify all aggregated events will be processed. The
     * event's object is the list of all aggregated events.
     * 
     * @param je
     */
    public void aggregatedEventsFlush(JahiaEvent je) {
        log("aggregatedEventsFlush", je);
    }

    public void aggregatedMetadataEngineAfterInit(JahiaEvent je) {
        log("aggregatedMetadataEngineAfterInit", je);
    }

    public void aggregatedMetadataEngineAfterSave(JahiaEvent je) {
        log("aggregatedMetadataEngineAfterSave", je);
    }

    public void aggregatedMetadataEngineBeforeSave(JahiaEvent je) {
        log("aggregatedMetadataEngineBeforeSave", je);
    }

    public void aggregatedObjectChanged(JahiaEvent je) {
        log("aggregatedObjectChanged", je);
    }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,JahiaContainer)
     * 
     * @param je
     */
    public void beforeContainerActivation(JahiaEvent je) {
        log("beforeContainerActivation", je);
    }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,ContentField)
     * 
     * @param je
     */
    public void beforeFieldActivation(JahiaEvent je) {
        log("beforeFieldActivation", je);
    }

    public void beforeServicesLoad(JahiaEvent je) {
        log("beforeServicesLoad", je);

    }

    public void beforeStagingContentIsDeleted(JahiaEvent je) {
        log("beforeStagingContentIsDeleted", je);
    }

    public void categoryUpdated(JahiaEvent je) {
        log("categoryUpdated", je);
    }

    public void containerAdded(JahiaEvent je) {
        log("containerAdded", je);
    }

    public void containerDeleted(JahiaEvent je) {
        log("containerDeleted", je);
    }

    public void containerListPropertiesSet(JahiaEvent je) {
        log("containerListPropertiesSet", je);
    }

    public void containerUpdated(JahiaEvent je) {
        log("containerUpdated", je);
    }

    public void containerValidation(JahiaEvent je) {
        log("containerValidation", je);
    }

    public void contentActivation(ContentActivationEvent je) {
        log("contentActivation", je);
    }

    public void contentWorkflowStatusChanged(ContentActivationEvent je) {
        log("contentWorkflowStatusChanged", je);
    }

    public void contentObjectCreated(JahiaEvent je) {
        log("contentObjectCreated", je);
    }

    /**
     * Event fired after a call to contentObjectonce a content object has been
     * updated ( changes stored in persistence )
     * 
     * @param je
     *            JahiaEvent
     */
    public void contentObjectDelete(ContentObjectDeleteEvent je) {
        log("contentObjectDelete", je);
    }

    /**
     * Event fired on content object restore version
     * 
     * @param je
     *            JahiaEvent
     */
    public void contentObjectRestoreVersion(ContentObjectRestoreVersionEvent je) {
        log("contentObjectRestoreVersion", je);
    }

    /**
     * Event fired once a content object has been updated ( changes stored in
     * persistence )
     * 
     * @param je
     *            JahiaEvent
     */
    public void contentObjectUndoStaging(ContentUndoStagingEvent je) {
        log("contentObjectUndoStaging", je);
    }

    public void contentObjectUpdated(JahiaEvent je) {
        log("contentObjectUpdated", je);
    }

    public void errorOccurred(JahiaErrorEvent je) {
        log("errorOccurred", je);
    }

    public void fieldAdded(JahiaEvent je) {
        log("fieldAdded", je);
    }

    public void fieldDeleted(JahiaEvent je) {
        log("fieldDeleted", je);
    }

    public void fieldUpdated(JahiaEvent je) {
        log("fieldUpdated", je);
    }

    public void fileManagerAclChanged(JahiaEvent je) {
        log("fileManagerAclChanged", je);
    }

    public void flushEsiCacheEvent(JahiaEvent je) {
        log("flushEsiCacheEvent", je);
    }

    public void groupAdded(JahiaEvent je) {
        log("groupAdded", je);
    }

    public void groupDeleted(JahiaEvent je) {
        log("groupDeleted", je);
    }

    public void groupUpdated(JahiaEvent je) {
        log("groupUpdated", je);
        ServicesRegistry.getInstance().getJahiaACLManagerService().flushCache();

    }

    public void metadataEngineAfterInit(JahiaEvent je) {
        log("metadataEngineAfterInit", je);
    }

    public void metadataEngineAfterSave(JahiaEvent je) {
        log("metadataEngineAfterSave", je);
    }

    public void metadataEngineBeforeSave(JahiaEvent je) {
        log("metadataEngineBeforeSave", je);
    }

    public void pageAccepted(JahiaEvent je) {
        log("pageAccepted", je);
    }

    public void pageAdded(JahiaEvent je) {
        log("pageAdded", je);
    }

    public void pageDeleted(JahiaEvent je) {
        log("pageDeleted", je);
    }

    public void pageLoaded(JahiaEvent je) {
        log("pageLoaded", je);
    }

    public void pageLoadedFromCache(JahiaEvent je) {
        log("pageLoadedFromCache", je);

    }

    public void pagePropertiesSet(JahiaEvent je) {
        log("pagePropertiesSet", je);
    }

    public void pageRejected(JahiaEvent je) {
        log("pageRejected", je);
    }

    public void rightsSet(JahiaEvent je) {
        log("rightsSet", je);
    }

    public void siteAdded(JahiaEvent je) {
        log("afterServicesLoad", je);
    }

    public void siteDeleted(JahiaEvent je) {
        log("siteDeleted", je);
    }

    public void templateAdded(JahiaEvent je) {
        log("templateAdded", je);
    }

    public void templateDeleted(JahiaEvent je) {
        log("templateDeleted", je);
    }

    public void templateUpdated(JahiaEvent je) {
        log("templateUpdated", je);
    }

    public void updateContainerEngineAfterInit(JahiaEvent je) {
        log("updateContainerEngineAfterInit", je);
    }

    public void updateContainerEngineBeforeSave(JahiaEvent je) {
        log("updateContainerEngineBeforeSave", je);
    }

    public void userAdded(JahiaEvent je) {
        log("userAdded", je);
    }

    public void userDeleted(JahiaEvent je) {
        log("userDeleted", je);
    }

    public void userLoggedIn(JahiaEvent je) {
        log("userLoggedIn", je);
    }

    public void userLoggedOut(JahiaEvent je) {
        log("userLoggedOut", je);
    }

    /**
     * Event fired when a user property is set
     */
    public void userPropertiesSet(JahiaEvent je) {
        log("userPropertiesSet", je);
    }

    public void userUpdated(JahiaEvent je) {
        log("userUpdated", je);
    }

    public void setSkipEvents(Set<String> skipEvents) {
        this.skipEvents = skipEvents;
    }

    public void setHandleEvents(Set<String> handleEvents) {
        this.handleEvents = handleEvents;
    }

    public void aggregatedNotification(JahiaEvent evt) {
        log("aggregatedNotification", evt);
    }

    public void notification(NotificationEvent evt) {
        log("notification", evt);
    }

    public void beforeContentCopy(JahiaEvent theEvent) {
        log("beforeContentCopy", theEvent);
    }

    public void beforeFormHandling(JahiaEvent theEvent) {
        log("beforeFormHandling", theEvent);
    }
}