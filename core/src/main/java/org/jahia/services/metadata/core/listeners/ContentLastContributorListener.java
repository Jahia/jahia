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

 package org.jahia.services.metadata.core.listeners;

import java.util.HashMap;
import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowEvent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class ContentLastContributorListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContentLastContributorListener.class);

    private String metadataName;
    private static final Map lastUpdateTimeMap = new HashMap();

    private Long updateTimeMinInterval;

    public void setMetadataName(String metadataName){
        this.metadataName = metadataName;
    }

    public String getMetadataName(){
        return this.metadataName;
    }

    public Long getLastUpdateTime(ObjectKey key) {
//        Map map = (Map)lastUpdateTimeMap.get();
//        if ( map == null ){
//            map = new HashMap();
//            lastUpdateTimeMap.set(map);
//        }
        return (Long)lastUpdateTimeMap.get(key);
    }

    public void putLastUpdateTime(ObjectKey key, Long time) {
//        if ( lastUpdateTimeMap == null ){
//            lastUpdateTimeMap = new ThreadLocal();
//            lastUpdateTimeMap.set(new HashMap());
//        }
//        Map map = (Map)lastUpdateTimeMap.get();
        lastUpdateTimeMap.put(key,time);
    }

    public void resetLastUpdateTime(ObjectKey key) {
//        if ( lastUpdateTimeMap == null ){
//            return;
//        }
//        Map map = (Map)lastUpdateTimeMap.get();
//        if ( map != null ){
            lastUpdateTimeMap.remove(key);
//        }
    }

    public Long getUpdateTimeMinInterval() {
        if ( updateTimeMinInterval == null ){
            updateTimeMinInterval = new Long(300);
        }
        return updateTimeMinInterval;
    }

    public void setUpdateTimeMinInterval(Long updateTimeMinInterval) {
        this.updateTimeMinInterval = updateTimeMinInterval;
    }

    public void metadataEngineAfterInit (JahiaEvent theEvent) {
        processEvent("metadataEngineAfterInit",theEvent);
    }

    public void metadataEngineBeforeSave (JahiaEvent theEvent) {
        processEvent("metadataEngineBeforeSave",theEvent);
    }

    public void containerUpdated(JahiaEvent theEvent) {
        handleObjectChanged(theEvent);
    }

    public void objectChanged (WorkflowEvent theEvent) {
        handleObjectChanged(theEvent);
    }

    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent) {
        handleObjectChanged(theEvent);
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

    public void contentObjectCreated(JahiaEvent theEvent){
        ContentObject contentObject = (ContentObject)theEvent.getObject();
        if ( contentObject == null ){
            return;
        }
        handleObjectChanged(theEvent,true);
    }

    public void contentObjectUpdated(JahiaEvent theEvent){
        handleObjectChanged(theEvent);
    }

    protected void handleObjectChanged(JahiaEvent theEvent) {
        handleObjectChanged(theEvent,false);
    }

    protected void handleObjectChanged(JahiaEvent theEvent, boolean contentCreated) {
        Object eventObject = theEvent.getObject();
        ContentObject contentObject = null;
        if ( eventObject instanceof JahiaContainer ){
            JahiaContainer jahiaContainer = (JahiaContainer)theEvent.getObject();
            try {
                contentObject = ContentContainer.getContainer(jahiaContainer.getID());
            } catch ( Exception t ){
                logger.debug("Error retrieving ContentContainer from JahiaContainer",t);
            }
        } else {
            contentObject = (ContentObject) theEvent.getObject();
        }
        if (contentObject == null) {
            return;
        }
        if (contentObject instanceof ContentField && ((ContentField) contentObject).getContainerID() != 0) {
            return;
        }

        if (Jahia.getThreadParamBean().getAttributeSafeNoNullPointer("importMode") != null){
            try {
                JahiaField jahiaField = contentObject.getMetadataAsJahiaField(this.getMetadataName(), Jahia.getThreadParamBean());
                if ( jahiaField == null ){
                    return;
                }
                if (jahiaField.getValue() != null && !jahiaField.getValue().equals("")) {
                    return;
                }
            } catch (JahiaException e) {
            }
        }

        Long lastUpdateTime;
        synchronized (lastUpdateTimeMap) {
            lastUpdateTime = getLastUpdateTime(contentObject.getObjectKey());
            logger.debug("last update time for " + contentObject.getObjectKey() + " : " + lastUpdateTime + " the event time = " + theEvent.getEventTime() + " update Time Interval = " + updateTimeMinInterval);
            if ( !contentCreated ){
            if (lastUpdateTime != null) {
                if (theEvent.getEventTime() < lastUpdateTime.longValue() ||
                    (theEvent.getEventTime() - lastUpdateTime.longValue())  < this.getUpdateTimeMinInterval().longValue())
                {
                    logger.debug("Skip updating last contributor metadata for content " + contentObject.getObjectKey());
                    return;
                }
            }
            putLastUpdateTime(contentObject.getObjectKey(), new Long(theEvent.getEventTime()));
        }
        }
        try {
            updateMetadata(contentObject, theEvent.getProcessingContext(), theEvent);

            // propagate to parent page
            if (contentObject instanceof PageReferenceableInterface) {
                PageReferenceableInterface pageRefObj = (PageReferenceableInterface) contentObject;
                try {
                    ContentPage parentPage = pageRefObj.getPage();
                    boolean update = false;
                    if (parentPage != null) {
                        synchronized (lastUpdateTimeMap) {
                            lastUpdateTime = getLastUpdateTime(parentPage.getObjectKey());
                            if (lastUpdateTime != null) {
                                if ((theEvent.getEventTime() - lastUpdateTime.longValue())
                                    > this.getUpdateTimeMinInterval().longValue()) {
                                    update = true;
                                }
                            } else {
                                update = true;
                            }
                            if (update) {
                                putLastUpdateTime(parentPage.getObjectKey(), new Long(theEvent.getEventTime()));
                            }
                        }
                        if (update) {
                            updateMetadata(parentPage, theEvent.getProcessingContext(), theEvent);
                        }
                    }
                } catch (Exception t) {
                    logger.debug("exception occured updating last modif date metadata", t);
                }
            }

            // propagate to parent container list if need
            if (contentObject instanceof ContentContainer) {
                ContentContainer container = (ContentContainer) contentObject;
                ContentContainerList ctnList = ContentContainerList
                        .getContainerList(container.getParentContainerListID());
                boolean update = false;
                synchronized (lastUpdateTimeMap) {
                    lastUpdateTime = getLastUpdateTime(ctnList.getObjectKey());
                    if (lastUpdateTime != null) {
                        if ((theEvent.getEventTime() - lastUpdateTime.longValue())
                            > this.getUpdateTimeMinInterval().longValue()) {
                            update = true;
                        }
                    } else {
                        update = true;
                    }
                    if (update) {
                        putLastUpdateTime(ctnList.getObjectKey(), new Long(theEvent.getEventTime()));
                    }
                }
                if (update) {
                    updateMetadata(ctnList, theEvent.getProcessingContext(), theEvent);
                }

            }

        } catch (Exception t) {
            logger.debug(t);
        }
    }

    protected void updateMetadata(ContentObject contentObject,
                                  ProcessingContext jParams,
                                  JahiaEvent theEvent)
    throws Exception {
        if(logger.isDebugEnabled())logger.debug("Updating metadata for object "+contentObject);
        if (jParams == null){
            jParams = Jahia.getThreadParamBean();
        }
        if (jParams ==null){
            return;
        }
        EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
        boolean resetStagingLoadRequest = MetadataTools.switchToStagingEntryLoadRequest(jParams);
        try {
            JahiaField jahiaField = contentObject.getMetadataAsJahiaField(this.getMetadataName(),jParams, true);
            if ( jahiaField == null ){
                return;
            }
            JahiaUser user = jParams.getUser();
            String userName = "unknown";
            if ( user != null ){
                userName = user.getUsername();
            }
            synchronized(jahiaField) {
                jahiaField.setValue(userName);
                jahiaField.save(jParams);
            }
        } finally {
            if (resetStagingLoadRequest) {
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
            }
        }
    }

    public void processEvent (String eventName, JahiaEvent theEvent) {
        if ( metadataName == null || theEvent == null ){
            return;
        }
        try {
            String attribPrefix = Metadata_Engine.ENGINE_NAME + ".";
            ProcessingContext jParams = theEvent.getProcessingContext();
            Map engineMap = (Map) jParams.getSessionState().getAttribute(
                "jahia_session_engineMap");

            FieldsEditHelper feh = (FieldsEditHelper)engineMap.get(attribPrefix
                                                                   +FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

            String lastContributor = null;
            JahiaField theField = feh.getField(this.getMetadataName());
            if (theField == null) {
                logger.info("Requested metadata field ["+this.getMetadataName()+"] not found!");
                return;
            }
            if ( "metadataEngineAfterInit".equals(eventName) ){
                String fieldValue = "";
                if (theField.getValue() != null) {
                    fieldValue = ( theField.getValue()).trim();
                    if (!"".equals(fieldValue)) {
                        return;
                    }
                }
                lastContributor = jParams.getUser().getUsername();

                // we want to init the creation date field
                if (lastContributor != null) {
                    theField.setValue(lastContributor);
                    feh.addUpdatedField(theField.getID(),theField.getLanguageCode());
                }
            } else if ( "metadataEngineBeforeSave".equals(eventName) ){
                // we want set the last contributor field
//                theField.setValue(jParams.getUser().getUsername());
//                feh.addUpdatedField(theField.getID(),theField.getLanguageCode());
            }
        } catch ( Exception t ){
            logger.debug("Exception processing event " + eventName, t);
        }
    }

}
