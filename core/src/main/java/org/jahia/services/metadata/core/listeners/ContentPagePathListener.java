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
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class ContentPagePathListener extends JahiaEventListener {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContentPagePathListener.class);

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

            String pagePath = "";
            JahiaField theField = feh.getField(this.getMetadataName());
            if (theField == null) {
                logger.info("Requested metadata field ["+this.getMetadataName()+"] not found!");
                return;
            }
            String fieldValue = "";
            if (theField != null && theField.getValue() != null) {
                fieldValue = theField.getValue().trim();
                if (fieldValue.startsWith(ContentObject.PAGEPATH_PAGEID_PREFIX)) {
                    pagePath = fieldValue;
                } else {
                    if ( theField.getID()>0 ){
                        ContentField contentField = ContentField.getField(theField.getID());
                        pagePath = contentField.getPagePathString(jParams,true);
                    } else {
                        pagePath = jParams.getContentPage().getPagePathString(jParams);
                    }
                }
            }

            // we want to init the page Path
            if ( pagePath != null && pagePath.startsWith(ContentObject.PAGEPATH_PAGEID_PREFIX) ){
                theField.setValue(pagePath);
                feh.addUpdatedField(theField.getID(),theField.getLanguageCode());
            }
        } catch ( Exception t ){
            logger.debug("Exception processing event metadataEngineAfterInit",t);
        }
    }

    public void contentObjectCreated(JahiaEvent theEvent){
        ProcessingContext jParams = theEvent.getProcessingContext();
        ContentObject contentObject = (ContentObject)theEvent.getObject();

        if ( contentObject == null || jParams == null
                || contentObject instanceof ContentField ){
            return;
        }
        String pagePath = "";
        EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
        boolean resetStagingLoadRequest = MetadataTools.switchToStagingEntryLoadRequest(jParams);
        try {
            pagePath = contentObject.getPagePathString(jParams);

            JahiaField jahiaField = contentObject.getMetadataAsJahiaField(this.getMetadataName(),jParams);
            if ( jahiaField == null ){
                return;
            }
            if ( jahiaField == null ){
                return;
            }
            jahiaField.setValue(pagePath);
            jahiaField.save(jParams);
        } catch ( Exception t ){
            logger.debug(t);
        } finally {
            if (resetStagingLoadRequest){
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
            }
        }
    }
}
