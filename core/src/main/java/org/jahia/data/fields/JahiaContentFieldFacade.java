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
package org.jahia.data.fields;

import java.util.*;

import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.PublicContentFieldEntryState;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.utils.LanguageCodeConverters;
import java.io.Serializable;

/**
 * Used to hold a set of JahiaField instance in multiple language for a given
 * field id.
 *
 * @author Khue Nguyen
 */
public class JahiaContentFieldFacade implements Serializable {

    private static final long serialVersionUID = 2903039268539880868L;

    private static org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getLogger(JahiaContentFieldFacade.class);

    private int fieldID = -1;

    // this is a List of EntryState listing ALL different Entry States
    // for a field, that are ACTIVE or STAGED (including all languages)
    private List<EntryStateable> activeAndStagingEntryStates;

    private Map<EntryStateable, JahiaField> fields;

    private List<Locale> locales;

    //--------------------------------------------------------------------------
    /**
     * Constructor for existing Field only
     *
     * @param fieldID the unique field identifier
     * @param loadFlag
     * @param jParams
     * @param locales the list of locales
     * @param createFieldForMissingLanguage
     *              if true, create missing instance for locales not found in db.
     */
    public JahiaContentFieldFacade( int fieldID,
                                    int loadFlag,
                                    ProcessingContext jParams,
                                    List<Locale> locales,
                                    boolean createFieldForMissingLanguage )
    throws JahiaException
    {
        this.fieldID = fieldID;
        this.fields = new HashMap<EntryStateable, JahiaField>();
        this.activeAndStagingEntryStates = new ArrayList<EntryStateable>();
        this.locales = locales;
        instanceFields(loadFlag, jParams, locales, createFieldForMissingLanguage);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor for a new Field. The field is only stored in memory,
     * nothing stored in persistance.
     *
     */
    public JahiaContentFieldFacade ( int fieldID,
                                     int jahiaID,
                                     int pageID,
                                     int ctnID,
                                     int fieldDefID,
                                     int fieldType,
                                     int connectType,
                                     String fieldValue,
                                     int aclID,
                                     ProcessingContext jParams,
                                     List<Locale> locales )
    throws JahiaException
    {

        this.fieldID = fieldID;
        this.fields = new HashMap<EntryStateable, JahiaField>();
        this.activeAndStagingEntryStates = new ArrayList<EntryStateable>();
        this.locales = locales;
        createFieldForMissingLanguage( fieldID,
                                       jahiaID,
                                       pageID,
                                       ctnID,
                                       fieldDefID,
                                       fieldType,
                                       connectType,
                                       fieldValue,
                                       aclID,
                                       jParams,
                                       locales );
    }

    //--------------------------------------------------------------------------
    public Iterator<JahiaField> getFields(){
        return fields.values().iterator();
    }


    //--------------------------------------------------------------------------
    /**
     * Return a field for a entryLoadRequest using resolve entry state mechanism.
     *
     * @param entryLoadRequest
     * @param activeIfStagingNotFound
     */
    public JahiaField getField( EntryLoadRequest entryLoadRequest,
                                boolean activeIfStagingNotFound ){

        logger.debug("EntryLoadRequest :" + entryLoadRequest.toString());

        Locale locale = entryLoadRequest.getFirstLocale(true);
        if ( locale != null ){
            logger.debug("EntryLoadRequest locale :" + locale.toString());
        } else {
            logger.debug("EntryLoadRequest locale is null !?");
        }

        ContentObjectEntryState entryState =
                            (ContentObjectEntryState)ServicesRegistry.getInstance()
                                     .getJahiaVersionService()
                                     .resolveEntry(activeAndStagingEntryStates,
                                                    entryLoadRequest);

        if ( entryState != null ){
            logger.debug("Resolved entryState :" + entryState.toString());
        }
        if ( entryLoadRequest.isStaging() && entryState == null
             && activeIfStagingNotFound ){

            EntryLoadRequest newEntryLoadRequest = new EntryLoadRequest(
                            ContentObjectEntryState.WORKFLOW_STATE_ACTIVE,
                            0,
                            entryLoadRequest.getLocales());

            entryState = (ContentObjectEntryState)ServicesRegistry.getInstance()
                                         .getJahiaVersionService()
                                         .resolveEntry(activeAndStagingEntryStates,
                                                        newEntryLoadRequest);
        } else if ( entryLoadRequest.isStaging() && entryState != null
                    && entryState.isActive() && !activeIfStagingNotFound ){
            // we only want the staging entry
            return null;
        }
        JahiaField field = null;
        if ( entryState != null ){
            field =
            fields.get(new PublicContentFieldEntryState(entryState));
        }

        if ( field != null ){
            logger.debug("Returned entryState :" + entryState.toString());
            logger.debug("Field Value :" + field.getValue()
                         + ", langCode=" + field.getLanguageCode());
        } else {
            logger.debug("Returned entryState is null ");
        }
        return field;
    }

    //--------------------------------------------------------------------------
    private void instanceFields( int loadFlag,
                                 ProcessingContext jParams,
                                 List<Locale> locales,
                                 boolean createFieldForMissingLanguage )
    throws JahiaException
    {

        ContentField contentField = ContentField.getField(fieldID);
        EntryLoadRequest elr = null;
        List<ContentObjectEntryState> v = new ArrayList<ContentObjectEntryState>();
        v.addAll(contentField.getActiveAndStagingEntryStates());
        for (ContentObjectEntryState entryState : v)
        {
            PublicContentFieldEntryState entryStateKey =
                                new PublicContentFieldEntryState(entryState);

            List<Locale> entryLocales = new ArrayList<Locale>();
            entryLocales.add(LanguageCodeConverters
                             .languageCodeToLocale(entryState.getLanguageCode()));
            elr = new EntryLoadRequest(entryState.getWorkflowState(),
                                       entryState.getVersionID(),
                                       entryLocales);
            elr.setWithMarkedForDeletion(true);

            try {
                EntryLoadRequest savedEntryLoadRequest = 
                    jParams.getSubstituteEntryLoadRequest();
                jParams.setSubstituteEntryLoadRequest(elr);
                JahiaField field = ServicesRegistry.getInstance()
                                 .getJahiaFieldService()
                                 .loadField(fieldID,
                                 loadFlag,
                                 jParams,
                                 elr);
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
                if ( field != null ){
                    String rawValue = field.getRawValue();
                    if ( rawValue == null ){
                        rawValue = "";
                    }
                    field.setValue( rawValue );
                    fields.put(entryStateKey,field);
                    activeAndStagingEntryStates.add(entryStateKey);
                }
            } catch ( Exception t ){
                logger.error(t.getMessage(), t);
            }
        }

        if ( createFieldForMissingLanguage )
        {

            JahiaFieldDefinition fieldDef =
                JahiaFieldDefinitionsRegistry
                                    .getInstance()
                                    .getDefinition( contentField.getFieldDefID() );

            String fieldValue = "";
            // in case of metadata field definition, the definition is global for all site
            if ( jParams != null && jParams.getPage() != null ){
                fieldValue = fieldDef.getDefaultValue();
            }

            createFieldForMissingLanguage( contentField.getID(),
                                           contentField.getSiteID(),
                                           contentField.getPageID(),
                                           contentField.getContainerID(),
                                           contentField.getFieldDefID(),
                                           contentField.getType(),
                                           contentField.getConnectType(),
                                           FormDataManager.htmlEncode(fieldValue),
                                           contentField.getAclID(),
                                           jParams,
                                           locales );

        }
    }

    //--------------------------------------------------------------------------
    private void createFieldForMissingLanguage( int fieldID,
                                                int jahiaID,
                                                int pageID,
                                                int ctnID,
                                                int fieldDefID,
                                                int fieldType,
                                                int connectType,
                                                String fieldValue,
                                                int aclID,
                                                ProcessingContext jParams,
                                                List<Locale> locales )
    throws JahiaException
    {
         EntryLoadRequest entryLoadRequest = null;
         ContentObjectEntryState entryState = null;

         // create entry state for all languages
         for ( Locale locale : locales )
         {
             List<Locale> entryLocales = new ArrayList<Locale>();
             entryLocales.add(locale);

             entryLoadRequest = new EntryLoadRequest(
                                 ContentObjectEntryState.WORKFLOW_STATE_ACTIVE,
                                 0, entryLocales);

             entryState = (ContentObjectEntryState)ServicesRegistry.getInstance()
                                          .getJahiaVersionService()
                                          .resolveEntry(activeAndStagingEntryStates,
                                                         entryLoadRequest);
             if ( ( entryState == null ) ||
                     ((!entryState.getLanguageCode().equals("shared")) &&
                      (!entryState.getLanguageCode().equals(locale.toString()))) ) {
                    // second case can happen because we might have resolved to "simpler" language
                    // in case of "composed" language code. For exemple we resolved "en" from "en_US".
                    // in the case of resolving to a shared language, we ignore the check of a simpler
                    // language.

                 entryLoadRequest = new EntryLoadRequest(
                                 ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                                 0, entryLocales);
                 ContentObjectEntryState entryStateStaging = (ContentObjectEntryState) ServicesRegistry
                        .getInstance().getJahiaVersionService().resolveEntry(
                                activeAndStagingEntryStates, entryLoadRequest);
                if (entryStateStaging != null)
                    entryState = entryStateStaging;
            }
            if ((entryState == null)
                    || ((!entryState.getLanguageCode().equals("shared")) && (!entryState
                            .getLanguageCode().equals(locale.toString())))) {
                 // have to create a staged
                 JahiaField field =
                     ServicesRegistry.getInstance()
                             .getJahiaFieldService()
                             .createJahiaField(
                                 fieldID,
                                 jahiaID,
                                 pageID,
                                 ctnID,
                                 fieldDefID,
                                 fieldType,
                                 connectType,
                                 fieldValue,
                                 0, // ranking
                                 aclID,
                                 0, // versionID
                                 ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                                 locale.toString());

                 PublicContentFieldEntryState entryStateKey =
                     new PublicContentFieldEntryState(
                                 ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                                 0,
                                 field.getLanguageCode());

                 if ( fields.get(entryStateKey) == null ){
                     activeAndStagingEntryStates.add(entryStateKey);
                 }
                 field.setRawValue(field.getValue());
                 fields.put(entryStateKey,field);

                 logger.debug( "Created new Field for entryState :"
                              + entryStateKey.toString() + " langCode="
                              + locale.toString());
             }
         }
    }

    //--------------------------------------------------------------------------
    /**
     * @return the field ID
     */
    public int getFieldID(){
        return this.fieldID;
    }

    //--------------------------------------------------------------------------
    /**
     * Change the field ID for all field.
     * It can be used to change temporary instance of fields
     *
     * @param fieldID
     */
    public void setFieldID(int fieldID){
        Iterator<JahiaField> fieldEnum = getFields();
        JahiaField field = null;
        while ( fieldEnum.hasNext() ){
            field = fieldEnum.next();
            field.setID(fieldID);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Change the Acl for all field.
     * It can be used to change temporary instance of fields
     *
     * @param aclID
     */
    public void setAclID(int aclID){
        Iterator<JahiaField> fieldEnum = getFields();
        JahiaField field = null;
        while ( fieldEnum.hasNext() ){
            field = fieldEnum.next();
            field.setAclID(aclID);
        }
    }

    /**
     * Change the field type ( from undefined to another type )
     *
     * @param type
     */
    public JahiaContentFieldFacade changeType(int type,
            ProcessingContext jParams) throws JahiaException {
        JahiaField aField = this.fields.values().iterator().next();
        JahiaContentFieldFacade facade =
               new JahiaContentFieldFacade(this.getFieldID(),aField.getJahiaID(),
               aField.getPageID(),aField.getctnid(),aField.getFieldDefID(),
               type,aField.getConnectType(),"",aField.getAclID(),jParams,
               this.locales);
        return facade;
    }

    public boolean existsEntry(int workflowState, String languageCode){
        int size = activeAndStagingEntryStates.size();
        PublicContentFieldEntryState entryState = null;
        for ( int i=0; i<size; i++ ){
            entryState = (PublicContentFieldEntryState)activeAndStagingEntryStates.get(i);
            if ( entryState.getLanguageCode().equals(languageCode) ){
                if ( workflowState == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                    && entryState.getWorkflowState() == workflowState ){
                    return true;
                } else if ( workflowState > EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                    && entryState.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    return true;
                }
            }
        }
        return false;
    }

}
