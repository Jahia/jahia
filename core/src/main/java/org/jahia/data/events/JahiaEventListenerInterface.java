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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//  JahiaEventListenerInterface
//  EV      12.01.2001
//

package org.jahia.data.events;

import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.notification.NotificationEvent;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;


/**
 * Set of event-triggered methods to catch system events.
 */
public interface JahiaEventListenerInterface {

    public void beforeServicesLoad( JahiaEvent je );
    public void afterServicesLoad( JahiaEvent je );

    public void siteAdded( JahiaEvent je );
    public void siteDeleted( JahiaEvent je );

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,ContentField)
     * @param je
     */
    public void beforeFieldActivation( JahiaEvent je );

    public void fieldAdded( JahiaEvent je );
    public void fieldUpdated( JahiaEvent je );
    public void fieldDeleted( JahiaEvent je );

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,JahiaContainer)
     * @param je
     */
    public void beforeContainerActivation( JahiaEvent je );
    public void containerValidation(JahiaEvent je);
    
    public void addContainerEngineAfterSave( JahiaEvent je );    
    public void addContainerEngineBeforeSave( JahiaEvent je );
    public void addContainerEngineAfterInit( JahiaEvent je );

    public void updateContainerEngineBeforeSave( JahiaEvent je );
    public void updateContainerEngineAfterInit( JahiaEvent je );

    public void containerAdded( JahiaEvent je );
    public void containerUpdated( JahiaEvent je );
    public void containerDeleted( JahiaEvent je );

    public void pageAdded( JahiaEvent je );
    public void pageLoaded( JahiaEvent je );
    
    /**
     * Called when the page is loaded from the cache.
     * 
     * @param je
     *            the jahia event object
     */
    public void pageLoadedFromCache(JahiaEvent je);

    public void pagePropertiesSet( JahiaEvent je );
    public void containerListPropertiesSet( JahiaEvent je );
    /**
     * Event fired when a user property is set
     */
    public void userPropertiesSet( JahiaEvent je );

    public void rightsSet( JahiaEvent je);

    public void userLoggedIn( JahiaEvent je );
    public void userLoggedOut( JahiaEvent je );

    public void objectChanged( WorkflowEvent we );
    public void aggregatedObjectChanged( JahiaEvent je );

    public void beforeStagingContentIsDeleted(JahiaEvent je );

    /**
     * Event fired after the Metadata_Engine has been initialized ( engineMap init )
     * and before processing last and current engine request.
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineAfterInit (JahiaEvent theEvent);
    public void aggregatedMetadataEngineAfterInit (JahiaEvent theEvent);

    /**
     * Event fired before the Metadata_Engine start to save the metadata fields for
     * the current content metadata facade
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineBeforeSave (JahiaEvent theEvent);
    public void aggregatedMetadataEngineBeforeSave (JahiaEvent theEvent);

    /**
     * Event fired after the Metadata_Engine has saved the metadata fields for
     * the current content object
     * The Event source object is the calling Metadata_Engine, the event object is
     * the ObjectKey instance of the content object.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineAfterSave (JahiaEvent theEvent);
    public void aggregatedMetadataEngineAfterSave (JahiaEvent theEvent);

    /**
     * Event fired before WorkflowService.activate(...)
     *
     * @param theEvent ContentActivationEvent
     */
    public void afterGroupActivation (ContentActivationEvent theEvent);

    /**
     * Event fired after ContentObject.activate(...)
     * The Event source object is the calling ContentObject, the event object is
     * the ObjectKey instance of the content object.
     *
     * @param theEvent ContentActivationEvent
     */
    public void contentActivation (ContentActivationEvent theEvent);
    public void aggregatedContentActivation( JahiaEvent je );

    public void contentWorkflowStatusChanged (ContentActivationEvent theEvent);
    public void aggregatedContentWorkflowStatusChanged( JahiaEvent je );

    /**
     * Event fired once a content object has been first created ( stored in persistence )
     * The Event source object is the JahiaUser
     * The Object is the ContentObject created.
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectCreated (JahiaEvent theEvent);
    public void aggregatedContentObjectCreated (JahiaEvent theEvent);

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectUpdated (JahiaEvent theEvent);

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent);

    /**
     * Event fired on content object delete
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectDelete (ContentObjectDeleteEvent theEvent);

    /**
     * Event fired on content object restore version
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent theEvent);

    /**
     * Event fired when ACLs on a Slide resource are modified.
     *
     * @param theEvent contains the modified FileNode object with the
     * new ACLs set.
     */
    public void fileManagerAclChanged(JahiaEvent theEvent);

    /**
     * Event fired when a template is updated
     * @param theEvent contains the currently edited template
     */
    public void templateUpdated(JahiaEvent theEvent);

    /**
     * Event fired when a category is updated
     * @param theEvent contains the currently edited category
     */
    public void categoryUpdated(JahiaEvent theEvent);

    /**
     * Event fired to notifying a retention rule' state change
     *
     * @param theEvent
     */
    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent );

    /**
     * Event fired to notify all aggregated events will be processed.
     * The event's object is the list of all aggregated events.
     *
     * @param theEvent
     */
    public void aggregatedEventsFlush(JahiaEvent theEvent);

    /**
     * Flush the ESI cache
     *
     * @param theEvent
     */
    public void flushEsiCacheEvent(JahiaEvent theEvent);

    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
    public void pageDeleted( JahiaEvent je);
    public void pageAccepted( JahiaEvent je);
    public void pageRejected( JahiaEvent je);
    public void templateAdded( JahiaEvent je );
    public void templateDeleted( JahiaEvent je );
    public void userAdded( JahiaEvent je );
    public void userDeleted( JahiaEvent je );
    public void userUpdated( JahiaEvent je );
    public void groupAdded( JahiaEvent je );
    public void groupDeleted( JahiaEvent je );
    public void groupUpdated( JahiaEvent je );
    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - End

    /**
     * Called when an exception occurs in the system.
     * 
     * @param je
     *            the jahia event object
     */
    public void errorOccurred(JahiaErrorEvent je);
    
    /**
     * Called when a notification event is fired.
     * 
     * @param evt
     *            the notification event
     */
    void notification(NotificationEvent evt);

    /**
     * Called when an aggregated notification event is fired.
     * 
     * @param evt
     *            the aggregated notification event
     */
    void aggregatedNotification(JahiaEvent evt);
}