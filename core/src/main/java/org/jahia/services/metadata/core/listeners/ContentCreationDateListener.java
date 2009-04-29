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
 package org.jahia.services.metadata.core.listeners;

import java.util.Map;

import org.jahia.content.ContentObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class ContentCreationDateListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContentCreationDateListener.class);

    private String metadataName;

    public void setMetadataName(String metadataName){
        this.metadataName = metadataName;
    }

    public String getMetadataName(){
        return this.metadataName;
    }

    public void metadataEngineAfterInit (JahiaEvent theEvent) {
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

            String creationDate = null;
            JahiaField theField = feh.getField(this.getMetadataName());
            if (theField == null) {
                logger.info("Requested metadata field ["+this.getMetadataName()+"] not found!");
                return;
            }
            String fieldValue = "";
            if (theField != null && theField.getObject() != null) {
                fieldValue = ( (String) theField.getObject()).trim();
                if (!"".equals(fieldValue)) {
                    creationDate = fieldValue;
                }
            }
            if ( creationDate == null ){
                creationDate = String.valueOf(theEvent.getEventTime());
            }

            // we want to init the creation date field
            theField.setObject(creationDate);
            feh.addUpdatedField(theField.getID(),theField.getLanguageCode());

        } catch ( Exception t ){
            logger.debug("Exception processing event metadataEngineAfterInit",t);
        }
    }

    public void contentObjectCreated(JahiaEvent theEvent){
        ProcessingContext jParams = theEvent.getProcessingContext();
        ContentObject contentObject = (ContentObject)theEvent.getObject();

        if ( contentObject == null || jParams==null ){
            return;
        }
        EntryLoadRequest savedEntryLoadRequest =
            jParams.getSubstituteEntryLoadRequest();
        boolean resetStagingLoadRequest = MetadataTools.switchToStagingEntryLoadRequest(jParams);
        try {
            JahiaField jahiaField = contentObject.getMetadataAsJahiaField(this.getMetadataName(),jParams);
            if ( jahiaField == null ){
                return;
            }
            if (jahiaField.getValue() == null || jahiaField.getValue().equals("")) {
                jahiaField.setObject(String.valueOf(theEvent.getEventTime()));
                jahiaField.save(jParams);
            }
        } catch ( Exception t ){
            logger.debug(t);
        } finally {
            if (resetStagingLoadRequest){
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
            }
        }
    }

}
