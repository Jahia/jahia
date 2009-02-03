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

 package org.jahia.services.metadata;

import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.LoadFlags;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.ContentMetadataFacade;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.services.fields.ContentField;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.params.ProcessingContext;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 nov. 2005
 * Time: 10:00:48
 * To change this template use File | Settings | File Templates.
 */
public class MetadataEventListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(MetadataEventListener.class);

    private boolean isMetadata = false;

    public MetadataEventListener(){
    }

    public boolean isMetadata() {
        return isMetadata;
    }

    public void setMetadata(boolean metadata) {
        isMetadata = metadata;
    }

    public void beforeServicesLoad(JahiaEvent je) {
        return;
    }

    public void afterServicesLoad(JahiaEvent je) {
        return;
    }

    /**
     * Event fired before WorkflowService.activate(...)
     *
     * @param theEvent ContentActivationEvent
     */
    public void afterGroupActivation (ContentActivationEvent theEvent){
        return;
    }

    public void beforeStagingContentIsDeleted(JahiaEvent je ){
        return;
    }

    /**
     * Event fired when a template is updated
     * @param theEvent contains the currently edited template
     */
    public void templateUpdated(JahiaEvent theEvent){
        return;
    }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,ContentField)
     *
     * @param je
     */
    public void beforeFieldActivation(JahiaEvent je) {
        ContentField cf = (ContentField) je.getObject();
        if (cf != null) {
            try {
                if ( cf.isMetadata() ){
                    setMetadata(true);
                    return;
                }
                // take care to create all metadatas
                try {
                    // try to create all missing metadata
                    new ContentMetadataFacade(cf.getObjectKey(),LoadFlags.ALL,je.getProcessingContext(),true, false, true);
                } catch (Exception t) {
                    logger.debug(
                            "Exception loading metadata for content object " +
                                    cf.getObjectKey(), t);
                }
            } catch (Exception t) {
                logger.debug(t);
            }
        }
    }

    public void fieldAdded(JahiaEvent je) {
        return;
    }

    public void fieldUpdated(JahiaEvent je) {
        return;
    }

    public void fieldDeleted(JahiaEvent je) {
        return;
    }

    /**
     * JahiaEvent(JahiaSaveVersion,ProcessingContext,JahiaContainer)
     *
     * @param je
     */
    public void beforeContainerActivation(JahiaEvent je) {
        return;
    }

    public void addContainerEngineBeforeSave(JahiaEvent je) {
        return;
    }

    public void addContainerEngineAfterInit(JahiaEvent je) {
        return;
    }

    public void updateContainerEngineBeforeSave(JahiaEvent je) {
        return;
    }

    public void updateContainerEngineAfterInit(JahiaEvent je) {
        return;
    }

    public void containerAdded(JahiaEvent je) {
        return;
    }

    public void containerUpdated(JahiaEvent je) {
        return;
    }

    public void containerDeleted(JahiaEvent je) {
        return;
    }

    public void pageAdded(JahiaEvent je) {
        return;
    }

    public void pageLoaded(JahiaEvent je) {
        return;
    }

    public void pagePropertiesSet(JahiaEvent je) {
        return;
    }

    public void containerListPropertiesSet(JahiaEvent je) {
        return;
    }

    public void rightsSet(JahiaEvent je) {
        return;
    }

    public void userLoggedIn(JahiaEvent je) {
        return;
    }

    public void userLoggedOut(JahiaEvent je) {
        return;
    }

    /**
     * Event fired after the Metadata_Engine has been initialized ( engineMap init )
     * and before processing last and current engine request.
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineAfterInit(JahiaEvent theEvent) {
        return;
    }

    /**
     * Event fired before the Metadata_Engine start to save the metadata fields for
     * the current content metadata facade
     * The Event source object is the calling Metadata_Engine, the event object is a
     * ContentMetadataFacade instance.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineBeforeSave(JahiaEvent theEvent) {
        return;
    }

    /**
     * Event fired after the Metadata_Engine has saved the metadata fields for
     * the current content object
     * The Event source object is the calling Metadata_Engine, the event object is
     * the ObjectKey instance of the content object.
     *
     * @param theEvent JahiaEvent
     */
    public void metadataEngineAfterSave(JahiaEvent theEvent) {
        return;
    }

    /**
     * Event fired once a content object has been first created ( stored in persistence )
     * The Event source object is the JahiaUser
     * The Object is the ContentObject created.
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectCreated(JahiaEvent theEvent) {
        try {

            ContentObject co = (ContentObject) theEvent.getObject();
            if (co == null) {
                return;
            }
            if ( co instanceof ContentField && ((ContentField)co).isMetadata() ){
                setMetadata(true);
                return;
            }

            ProcessingContext jParams = theEvent.getProcessingContext();
            if (jParams != null) {
                // ensure to create metadata for this content object
                new ContentMetadataFacade(co.getObjectKey(),LoadFlags.ALL,jParams,true,true, true);
            }

        } catch (Exception t) {
            logger.debug("Exception processing event contentActivation ", t);
        }

    }

    /**
     * Event fired once a content object has been updated ( changes stored in persistence )
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectUpdated(JahiaEvent theEvent) {
        try {

            ContentObject co = (ContentObject) theEvent.getObject();
            if (co == null) {
                return;
            }
            if ( co instanceof ContentField && ((ContentField)co).isMetadata() ){
                setMetadata(true);
                return;
            }
        } catch (Exception t) {
            logger.debug("Exception processing event contentActivation ", t);
        }
    }

    public void fileManagerAclChanged(JahiaEvent theEvent) {
        return;
    }

    public void objectChanged(WorkflowEvent theEvent) {
        handleObjectChanged(theEvent);
    }

    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent) {
        handleObjectChanged(theEvent);
    }

    protected void handleObjectChanged(JahiaEvent theEvent){
        try {
            ContentObject co = (ContentObject) theEvent.getObject();
            if (co != null &&
                    co instanceof ContentField && ((ContentField)co).isMetadata() ){
                setMetadata(true);
                return;
            }

            JahiaUser user = null;
            if ( theEvent instanceof WorkflowEvent ){
                user = ((WorkflowEvent)theEvent).getUser();
            } else {
                user = theEvent.getProcessingContext().getUser();
            }

            // check if the content object still exist
            try {
                ContentObject sourceObject = (ContentObject) theEvent.getObject();
                if (sourceObject.getEntryStates().size() == 0) {
                    // doesn't exist anymore, so remove all metadata
                    Iterator<ContentField> iterator = sourceObject.getMetadatas().iterator();
                    ContentField contentField = null;
                    Set<String> languageCodes = new HashSet<String>();
                    languageCodes.add(ContentObject.SHARED_LANGUAGE);
                    while (iterator.hasNext()) {
                        contentField = (ContentField) iterator.next();
                        try {
                            StateModificationContext smc
                                    = new StateModificationContext(contentField.getObjectKey(),
                                    languageCodes);
                            contentField.markLanguageForDeletion(user,
                                    ContentObject.SHARED_LANGUAGE,
                                    smc);
                        } catch (Exception t) {
                            logger.debug(t);
                        }
                    }
                    return;
                }
            } catch (Exception t) {
                logger.debug(t);
            }
        } catch (Exception t) {
            logger.debug("Exception processing event contentActivation ", t);
        }
    }

    public void contentActivation(ContentActivationEvent theEvent) {
        try {
            ObjectKey objectKey = (ObjectKey) theEvent.getObject();
            if (objectKey != null) {
                // check is this content object is not a metadata

                ContentObject co = ContentObject.getContentObjectInstance(objectKey);
                if (co instanceof ContentField && ((ContentField)co).isMetadata() ) {
                    setMetadata(true);
                    return;
                }
            }
        } catch (Exception t) {
            logger.debug("Exception processing event contentActivation ", t);
        }
    }

    /**
     * Event fired to notifying a retention rule' state change
     *
     * @param theEvent
     */
    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent ){
        return;
    }

    /**
     * Event fired on content object delete
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectDelete (ContentObjectDeleteEvent theEvent){
        handleObjectChanged(theEvent);
    }

    /**
     * Event fired on content object restore version
     *
     * @param theEvent JahiaEvent
     */
    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent theEvent){
        handleObjectChanged(theEvent);
    }

}
