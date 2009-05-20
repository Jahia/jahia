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
//
//  JahiaEventGeneratorService
//  EV      12.01.2001
//
//
//  fireUpdateField( theEvent )
//  fireAddContainer( theEvent )
//  fireUpdateContainer( theEvent )
//  fireDeleteContainer( theEvent )
//  fireAddPage( theEvent )
//  fireLogin( theEvent )
//  fireLogout( theEvent )
// fireUpdateTemplate ( theEvent )
//

package org.jahia.services.events;

import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.notification.NotificationEvent;


public abstract class JahiaEventGeneratorService extends JahiaService {

    public abstract void fireAggregatedEvents();

    public abstract void fireAggregatedEventsFlush(JahiaEvent theEvent);

    public abstract void fireSiteAdded (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireSiteDeleted (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireBeforeFieldActivation (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireAddField (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireUpdateField (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireDeleteField (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireBeforeContainerActivation (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireContainerValidation (JahiaEvent theEvent)
            throws JahiaException;    
    
    public abstract void fireAddContainerEngineBeforeSave (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireAddContainerEngineAfterSave (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireAddContainerEngineAfterInit (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireAddContainer (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireUpdateContainerEngineBeforeSave (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireUpdateContainerEngineAfterInit (JahiaEvent theEvent)
            throws JahiaException;

    public abstract void fireUpdateContainer (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireDeleteContainer (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireSetPageProperties (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireSetContainerListProperties (JahiaEvent theEvent)
            throws JahiaException;

    /**
     * Event fired when a user property is set
    */
    public abstract void fireUserPropertiesSet (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireAddPage (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireLoadPage (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireSetRights (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireLogin (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireLogout (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireUpdateTemplate (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireUpdateCategory (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireObjectChanged (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireBeforeStagingContentIsDeleted (JahiaEvent theEvent) throws JahiaException;

    /**
     * Event fired after the Metadata_Engine has been initialized ( engineMap init )
     * and before processing last and current engine request.
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireMetadataEngineAfterInit (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Event fired before the Metadata_Engine start to save the metadata fields for
     * the current content metadata facade
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireMetadataEngineBeforeSave (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Event fired after the Metadata_Engine has saved the metadata fields for
     * the current content object
     * The Event source object is the calling Metadata_Engine, the event object is
     * the ObjectKey instance of the content object.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireMetadataEngineAfterSave (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Event fired before WorkflowService.activate(...)
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireAfterGroupActivation (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Event fired after ContentObject.activate(...)
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentActivation (JahiaEvent theEvent)
    throws JahiaException;

    public abstract void fireContentWorkflowStatusChanged (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Should be fired once a content object has been first created ( stored in persistence )
     * The Event source object is the JahiaUser
     * The Object is the ContentObject created.
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentObjectCreated (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Should be fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentObjectUpdated (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * fired by ContentObject.undoStaging()
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentObjectUndoStaging (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * fire on content object delete
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentObjectDelete (JahiaEvent theEvent)
    throws JahiaException;

    /**
     * Should be fired whenever ACLs are changed in the file manager GUI.
     * 
       * @param theEvent contains the modified FileNode object with the
       * new ACLs set.
     */
    public abstract void fireFileManagerAclChanged(JahiaEvent theEvent)
    throws JahiaException;


    public abstract void fireTimeBasedPublishingStateChange(JahiaEvent theEvent)
    throws JahiaException;

    /**
     * fire on content object restore
     *
     * @param theEvent JahiaEvent
     * @throws JahiaException
     */
    public abstract void fireContentObjectRestoreVersion (JahiaEvent theEvent)
    throws JahiaException;

    // ==================================================================
    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - begin
    public abstract void fireDeletePage (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireAcceptPage (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireRejectPage (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireAddUser(JahiaEvent theEvent) throws JahiaException;

    public abstract void fireDeleteUser(JahiaEvent theEvent) throws JahiaException;
    
    public abstract void fireUpdateUser(JahiaEvent theEvent) throws JahiaException;
    
    public abstract void fireAddGroup(JahiaEvent theEvent) throws JahiaException;

    public abstract void fireDeleteGroup(JahiaEvent theEvent) throws JahiaException;

    public abstract void fireUpdateGroup(JahiaEvent theEvent) throws JahiaException;

    public abstract void fireAddTemplate (JahiaEvent theEvent) throws JahiaException;

    public abstract void fireDeleteTemplate (JahiaEvent theEvent) throws JahiaException;
    
    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - end
    // ==================================================================

    /**
     * Propagates the specified notification event to the listeners.
     * 
     * @param theEvent
     *            the fired notification event
     */
    public abstract void fireNotification(NotificationEvent theEvent);
    
    public abstract void fireBeforeContentCopy(JahiaEvent theEvent) throws JahiaException;

    public abstract void fireBeforeFormHandling(JahiaEvent theEvent) throws JahiaException;    
}