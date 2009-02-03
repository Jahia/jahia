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
//  JahiaEventListener
//  EV      12.01.2001
//

package org.jahia.data.events;

import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;


public class JahiaEventListener implements JahiaEventListenerInterface
{
    private static transient Category logger = Logger.getLogger(JahiaEventListener.class);
    public void beforeServicesLoad( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : beforeServicesLoad"+je);
        return;
    }
    public void afterServicesLoad( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : afterServicesLoad"+je);
        return; }

    public void siteAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : siteAdded"+je);
        return; }
    public void siteDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : siteDeleted"+je);
        return; }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,ContentField)
     * @param je
     */
    public void beforeFieldActivation( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : beforeFieldActivation"+je);
        return; }

    public void fieldAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : fieldAdded"+je);
        return; }
    public void fieldUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : fieldUpdated"+je);
        return; }
    public void fieldDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : fieldDeleted"+je);
        return; }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,JahiaContainer)
     * @param je
     */
    public void beforeContainerActivation( JahiaEvent je ){
        if(logger.isDebugEnabled()) logger.debug("Receive event : beforeContainerActivation"+je);
        return;}

    public void containerValidation(JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : containerValidation"+je);
        return; }
    public void addContainerEngineAfterSave( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : addContainerEngineAfterSave"+je);
        return; }
    public void addContainerEngineBeforeSave( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : addContainerEngineBeforeSave"+je);
        return; }
    public void addContainerEngineAfterInit( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : addContainerEngineAfterInit"+je);
        return; }

    public void updateContainerEngineBeforeSave( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : updateContainerEngineBeforeSave"+je);
        return; }
    public void updateContainerEngineAfterInit( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : updateContainerEngineAfterInit"+je);
        return; }

    public void containerAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : containerAdded"+je);
        return; }
    public void containerUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : containerUpdated"+je);
        return; }
    public void containerDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : containerDeleted"+je);
        return; }

    public void pageAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pageAdded"+je);
        return; }
    public void pageLoaded( JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pageLoaded"+je);
        return; }

    public void pagePropertiesSet( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pagePropertiesSet"+je);
        return; }
    public void containerListPropertiesSet( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : containerListPropertiesSet"+je);
        return; }
    /**
     * Event fired when a user property is set  
     */
    public void userPropertiesSet( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userPropertiesSet"+je);
        return; }

    public void templateUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : templateUpdated"+je);
        return; }
    public void categoryUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : categoryUpdated"+je);
        return; }

    public void rightsSet( JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : rightsSet"+je);
        return; }

    public void userLoggedIn( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userLoggedIn"+je);
        return; }
    public void userLoggedOut( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userLoggedOut"+je);
        return; }

    public void objectChanged( WorkflowEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : objectChanged"+je);
        return; }
    public void aggregatedObjectChanged( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedObjectChanged"+je);
        return; }
    public void beforeStagingContentIsDeleted(JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : beforeStagingContentIsDeleted"+je);
        return; }

    public void metadataEngineAfterInit (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : metadataEngineAfterInit"+theEvent);
        return; }
    public void aggregatedMetadataEngineAfterInit (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedMetadataEngineAfterInit"+theEvent);
        return; }

    public void metadataEngineBeforeSave (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : metadataEngineBeforeSave"+theEvent);
        return; }
    public void aggregatedMetadataEngineBeforeSave (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedMetadataEngineBeforeSave"+theEvent);
        return; }

    public void metadataEngineAfterSave (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : metadataEngineAfterSave"+theEvent);
        return; }
    public void aggregatedMetadataEngineAfterSave (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedMetadataEngineAfterSave"+theEvent);
        return; }

    public void afterGroupActivation (ContentActivationEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : afterGroupActivation"+theEvent);
        return; }

    public void contentActivation (ContentActivationEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentActivation"+theEvent);
        return; }
    public void aggregatedContentActivation( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedContentActivation"+je);
        return; }

    public void contentObjectCreated (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentObjectCreated"+theEvent);
        return; }
    public void aggregatedContentObjectCreated (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedContentObjectCreated"+theEvent);
        return; }

    public void contentObjectUpdated (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentObjectUpdated"+theEvent);
        return; }

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentObjectUndoStaging"+theEvent);
        return; }

    /**
     * Event fired after a call to contentObjectonce a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectDelete (ContentObjectDeleteEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentObjectDelete"+theEvent);
        return; }

    /**
     * Event fired on content object restore version
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : contentObjectRestoreVersion"+theEvent);
        return; }

    public void fileManagerAclChanged (JahiaEvent theEvent) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : fileManagerAclChanged"+theEvent);
        return; }

    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : timeBasedPublishingEvent"+theEvent);
        return; }

    /**
     * Event fired to notify all aggregated events will be processed.
     * The event's object is the list of all aggregated events.
     *
     * @param theEvent
     */
    public void aggregatedEventsFlush(JahiaEvent theEvent){
        if(logger.isDebugEnabled()) logger.debug("Receive event : aggregatedEventsFlush"+theEvent);
        return; }

    public void flushEsiCacheEvent(JahiaEvent theEvent){
        if(logger.isDebugEnabled()) logger.debug("Receive event : flushEsiCacheEvent"+theEvent);
        return; }

    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - Begin
    public void pageDeleted( JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pageDeleted"+je);
        return; }
    public void pageAccepted( JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pageAccepted"+je);
        return; }
    public void pageRejected( JahiaEvent je) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : pageRejected"+je);
        return; }
    public void templateAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : templateAdded"+je);
        return; }
    public void templateDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : templateDeleted"+je);
        return; }
    public void userAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userAdded"+je);
        return; }
    public void userDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userDeleted"+je);
        return; }
    public void userUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : userUpdated"+je);
        return; }
    public void groupAdded( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : groupAdded"+je);
        return; }
    public void groupDeleted( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : groupDeleted"+je);
        return; }
    public void groupUpdated( JahiaEvent je ) {
        if(logger.isDebugEnabled()) logger.debug("Receive event : groupUpdated"+je);

        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        servicesRegistry.getJahiaACLManagerService().flushCache();
        
    }
    // Nicol�s Charczewski - Neoris Argentina - added 28/03/2006 - End

} // end JahiaEventListener
