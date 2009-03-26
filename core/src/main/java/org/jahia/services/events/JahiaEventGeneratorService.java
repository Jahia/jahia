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


public abstract class JahiaEventGeneratorService extends JahiaService {

    public abstract void fireAggregatedEvents();

    public abstract void fireAggregatedEventsFlush();

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

} // end JahiaEventGeneratorService
