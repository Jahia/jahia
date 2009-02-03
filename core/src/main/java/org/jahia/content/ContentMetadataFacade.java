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

package org.jahia.content;

import org.jahia.data.containers.ContainerFacadeInterface;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.PublicContentFieldEntryState;
import org.jahia.services.metadata.MetadataBaseService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;
import org.jahia.services.version.JahiaSaveVersion;

import java.util.*;
import java.util.Map.Entry;

/**
 * Used to hold the metadata fields in different set of language.
 *
 * @author Khue Nguyen
 */
public class ContentMetadataFacade implements ContainerFacadeInterface{

    private static org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getLogger(ContentMetadataFacade.class);

    private ObjectKey objectKey;

    private boolean checkStructure = false;

    private boolean containerStructureChecked = false;

    // this is a List of EntryState listing ALL different Entry States
    // that are ACTIVE or STAGED.
    private List<EntryStateable> activeAndStagingEntryStates;

    // Map of JahiaContentFieldFacade instances [fieldID,JahiaContentFieldFacade obj]
    private Map<Integer, JahiaContentFieldFacade> fields;

    // Map of JahiaContainer instances , key = ContentFieldEntryState
    private Map<EntryStateable, JahiaContainer> containers;

    private Map fieldsOrder = new HashMap();

    private boolean requiredOnly = false;

    //--------------------------------------------------------------------------
    /**
     * Constructor for existing ContentObject only.
     * Load instance for all site's Languages.
     *
     * @param objectKey, the contentObject.
     * @param loadFlag for fields
     * @param jParams
     * @param createMissingLanguages,
     *              if true, create missing instance for locales not found in db.
     * @param checkStructure boolean ( create missing metadata or not )
     * @throws JahiaException
     */
    public ContentMetadataFacade( ObjectKey objectKey,
                                  int loadFlag,
                                  ProcessingContext jParams,
                                  boolean createMissingLanguages,
                                  boolean checkStructure,
                                  boolean requiredOnly)
    throws JahiaException
    {
        this.objectKey = objectKey;
        this.checkStructure = checkStructure;
        this.containers = new HashMap();
        this.fields     = new HashMap();
        this.activeAndStagingEntryStates = new ArrayList();
        this.requiredOnly = requiredOnly;
        instanceContainers(loadFlag, jParams, jParams.getSite().getLanguageSettingsAsLocales(false),
                           createMissingLanguages);
    }


    //--------------------------------------------------------------------------
    /**
     * Constructor for existing ContentObject only
     *
     * @param objectKey, the contentObject.
     * @param loadFlag for fields
     * @param jParams
     * @param locales, the list of locales for which to load content
     * @param createMissingLanguages,
     *              if true, create missing instance for locales not found in db.
     * @param checkStructure boolean ( create missing metadata or not )
     */
    public ContentMetadataFacade( ObjectKey objectKey,
                                  int loadFlag,
                                  ProcessingContext jParams,
                                  List<Locale> locales,
                                  boolean createMissingLanguages,
                                  boolean checkStructure)
    throws JahiaException
    {
        this.objectKey = objectKey;
        this.checkStructure = checkStructure;
        this.containers = new HashMap();
        this.fields     = new HashMap();
        this.activeAndStagingEntryStates = new ArrayList();
        instanceContainers(loadFlag, jParams, locales, createMissingLanguages);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor for not existing ContentObject.
     *
     * @param objectKey ObjectKey, the objectKey here is the ContentDefinition key!!!
     * @param jParams ProcessingContext
     * @param locales ArrayList
     * @throws JahiaException
     */
    public ContentMetadataFacade (ObjectKey objectKey,
                                  ProcessingContext jParams,
                                  List<Locale> locales )
    throws JahiaException
    {
        this.objectKey = objectKey;
        this.containers = new HashMap();
        this.fields     = new HashMap();
        this.activeAndStagingEntryStates = new ArrayList();
        createTempContainers( 0, jParams.getSiteID(), jParams.getPageID(), 0,
                              0, jParams, locales );
    }

    /**
     *
     */
    public ObjectKey getObjectKey(){
        return this.objectKey;
    }

    /**
     *
     * @param objectKey
     */
    public void setObjectKey(ObjectKey objectKey){
        this.objectKey = objectKey;
    }

    //--------------------------------------------------------------------------
    public Iterator<JahiaContainer> getContainers(){
        return containers.values().iterator();
    }

    //--------------------------------------------------------------------------
    public Iterator<JahiaContentFieldFacade> getFields(){
        return fields.values().iterator();
    }

    //--------------------------------------------------------------------------
    public JahiaContentFieldFacade getContentFieldFacade(int fieldID){
        return (JahiaContentFieldFacade)this.fields.get(new Integer(fieldID));
    }

    //--------------------------------------------------------------------------
    /**
     * Return a container for a entryLoadRequest using resolve entry state mechanism.
     *
     * @param entryLoadRequest
     * @param activeIfStagingNotFound
     */
    public JahiaContainer getContainer( EntryLoadRequest entryLoadRequest,
                                        boolean activeIfStagingNotFound ){

        logger.debug("EntryLoadRequest :" + entryLoadRequest.toString());

        Locale locale = entryLoadRequest.getFirstLocale(true);
        logger.debug("EntryLoadRequest locale :" + locale.toString());

        ContentObjectEntryState entryState =
                            (ContentObjectEntryState)ServicesRegistry.getInstance()
                                     .getJahiaVersionService()
                                     .resolveEntry(activeAndStagingEntryStates,
                                                    entryLoadRequest);

        if ( entryState != null ){
            logger.debug("Resolved entryState :" + entryState.toString());
        }
        if ( entryState == null && activeIfStagingNotFound ){

            EntryLoadRequest newEntryLoadRequest = new EntryLoadRequest(
                            ContentObjectEntryState.WORKFLOW_STATE_ACTIVE,
                            0,
                            entryLoadRequest.getLocales());

            entryState = (ContentObjectEntryState)ServicesRegistry.getInstance()
                                         .getJahiaVersionService()
                                         .resolveEntry(activeAndStagingEntryStates,
                                                        newEntryLoadRequest);
        } else if ( entryState != null && entryState.isActive() && !activeIfStagingNotFound ){
            // we only want the staging entry
            return null;
        }
        JahiaContainer container = null;
        if ( entryState != null ){
            container =
            (JahiaContainer)containers.get(new PublicContentFieldEntryState(entryState));
            logger.debug("Returned entryState :" + entryState.toString());
        } else {
            logger.debug("Entry state is null");
        }

        return container;
    }

    //--------------------------------------------------------------------------
    /**
     * Change the field type ( from undefined to another type )
     *
     * @param type
     */
    public void changeType(int fieldId, int type,
            ProcessingContext jParams) throws JahiaException {

        JahiaContentFieldFacade facade = this.getContentFieldFacade(fieldId);
        facade = facade.changeType(type,jParams);
        this.fields.put(new Integer(fieldId),facade);

        for ( EntryStateable entryState : this.activeAndStagingEntryStates ){
            JahiaContainer container =
                    (JahiaContainer)this.containers.get(entryState);
            EntryLoadRequest loadRequest = new EntryLoadRequest(entryState);
            JahiaField field = (JahiaField)facade.getField(loadRequest,true);
            container.setField(field);
        }
    }

    //--------------------------------------------------------------------------
    private void instanceContainers( int loadFlag,
                                     ProcessingContext jParams,
                                     List<Locale> locales,
                                     boolean createMissingLanguages )
    throws JahiaException
    {
        EntryLoadRequest loadVersion =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                     0, locales);

        // load all metadatas fields
        ContentObject contentObject = null;
        List<ContentField> metadatas = new ArrayList<ContentField>();
        try {
            contentObject = (ContentObject)ContentObject.getInstance(this.
                objectKey);
            metadatas = contentObject.getMetadatas();

        } catch ( Exception t ){
            throw new JahiaException("Invalid ContentObject", "Invalid ContentObject",
                                     JahiaException.DATA_ERROR, JahiaException.ERROR_SEVERITY,
                                     t );
        }
        ContentDefinition objectDef = null;
        try {
            objectDef = JahiaContainerDefinition.getContentDefinitionInstance(
            contentObject.getDefinitionKey(null));
        } catch ( ClassNotFoundException cnfe ){
            throw new JahiaException("Cannot retrieve ContentDefinition",
                                     "Cannot retrieve ContentDefinition",
                                     JahiaException.APPLICATION_ERROR,
                                     JahiaException.ERROR_SEVERITY, cnfe);
        }
        int size = metadatas.size();
        List l = ContentDefinition.getMetadataDefinitions(objectDef);
        Map orders = new HashMap();
        int j = 0;
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            JahiaFieldDefinition o = (JahiaFieldDefinition) iterator.next();
            orders.put(new Integer(o.getID()), new Integer(j++));
        }
        for (int i = 0; i < size; i++) {
            ContentField contentField = (ContentField) metadatas.get(i);
            this.fieldsOrder.put(new Integer(contentField.getID()),orders.get(new Integer(contentField.getDefinitionID(null))));
            if (this.fields.get(new Integer(contentField.getID())) == null) {
                JahiaContentFieldFacade contentFieldFacade =
                    new JahiaContentFieldFacade(contentField.getID(),
                    loadFlag,
                    jParams, locales, createMissingLanguages);
                this.fields.put(new Integer(contentField.getID()),
                                contentFieldFacade);
            }
        }
        Iterator iterator = locales.iterator();
        Locale locale = null;
        List<Locale> loadLocales = null;
        while ( iterator.hasNext() )
        {
            locale = (Locale)iterator.next();

            loadLocales = new ArrayList<Locale>();
            loadLocales.add(locale);

            // load the staging container.
            loadVersion =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                0, loadLocales);
            JahiaContainer stagingContainer = loadContainer(loadFlag,loadVersion,jParams,locales,createMissingLanguages);
            if ( (stagingContainer != null) && (stagingContainer.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE) ){
                stagingContainer.setLanguageCode(locale.toString());
                PublicContentFieldEntryState entryStateKey =
                    new PublicContentFieldEntryState(
                                loadVersion.getWorkflowState(),
                                loadVersion.getVersionID(), locale.toString());
                if ( this.containers.get(entryStateKey)==null ){
                    this.containers.put(entryStateKey,stagingContainer);
                    this.activeAndStagingEntryStates.add(entryStateKey);
                }
            } else {
                // we can have fields in staging but container only in active ( due to container update mechanism change !! )
                // so we store the active as staging to ensure we have loaded all staged fields as well
                if ( stagingContainer != null) {
                    stagingContainer.setLanguageCode(locale.toString());
                    stagingContainer.fieldsStructureCheck(jParams);
                    PublicContentFieldEntryState entryStateKey =
                        new PublicContentFieldEntryState(
                                    EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                    0, locale.toString());
                    if ( this.containers.get(entryStateKey)==null ){
                        this.containers.put(entryStateKey,stagingContainer);
                        this.activeAndStagingEntryStates.add(entryStateKey);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    private void createTempContainers( int ctnID,
                                       int jahiaID,
                                       int pageID,
                                       int ctnListID,
                                       int ctnDefID,
                                       ProcessingContext jParams,
                                       List<Locale> locales )
    throws JahiaException
    {

         EntryLoadRequest loadVersion = null;

         // create tempFields

         // create stating entry state for all languages
         Iterator iterator = locales.iterator();
         Locale locale = null;
         while ( iterator.hasNext() )
         {
             locale = (Locale)iterator.next();
             List entryLocales = new ArrayList();
             entryLocales.add(locale);

             loadVersion =  new EntryLoadRequest(ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                                  0, entryLocales);

             PublicContentFieldEntryState entryStateKey =
                 new PublicContentFieldEntryState(
                             ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                             0,
                             locale.toString());

             JahiaContainer container =
                     new JahiaContainer(ctnID, jahiaID, pageID, ctnListID,0,
                     0,ctnDefID,0,ContentObjectEntryState.WORKFLOW_STATE_START_STAGING);

             container.setLanguageCode(locale.toString());
             if ( fields.size()==0 ){

                 // source page
                 JahiaPage page = ServicesRegistry.getInstance()
                                .getJahiaPageService().lookupPage (pageID, jParams);
                 if (page == null) {
                     throw new JahiaException( "Page missing",
                         "Trying to add a container on a non-existing page (" + pageID + ")",
                         JahiaException.PAGE_ERROR, JahiaException.ERROR_SEVERITY );
                 }
                 createContentFieldFacades(page,container.getID(),jParams,locales);
             }

             // set fields
             for ( JahiaContentFieldFacade contentFieldFacade : this.fields.values() ){
                 JahiaField field = contentFieldFacade.getField(loadVersion,true);
                 Integer fieldOrder = (Integer)this.fieldsOrder.get(new Integer(field.getID()));
                 if ( fieldOrder != null ){
                     field.setOrder(fieldOrder.intValue());
                 }
                 container.addField(field);
             }
             if ( container != null ){
                 container.sortFieldByOrderAttribute();
             }

             if ( this.containers.get(entryStateKey) == null ){
                 this.containers.put(entryStateKey,container);
                 this.activeAndStagingEntryStates.add(entryStateKey);
             }

             logger.debug( "Created new Container for entryState :"
                          + entryStateKey.toString() + " langCode="
                          + locale.toString());
         }
    }

    //--------------------------------------------------------------------------
    private JahiaContainer loadContainer(int loadFlag,
            EntryLoadRequest loadVersion,
            ProcessingContext jParams, List<Locale> locales, boolean createMissingLanguages)
    throws JahiaException
    {
        JahiaContainer container = new JahiaContainer ();
        container.setFieldsLoaded(true);

        container.setLanguageCode(loadVersion.getFirstLocale(true).toString());
        // set fields
        for ( JahiaContentFieldFacade contentFieldFacade : this.fields.values() ){
            JahiaField field = contentFieldFacade.getField(loadVersion,true);
            if ( loadVersion.isCurrent() && field == null ){
                // the active version doesn't exists, so we request the staging
                EntryLoadRequest stagingVersion
                    = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                           loadVersion.getVersionID(),
                                           loadVersion.getLocales());
                field = contentFieldFacade.getField(stagingVersion,true);
            }
            if ( field != null ){
                Integer fieldOrder = (Integer)this.fieldsOrder.get(new Integer(field.getID()));
                if ( fieldOrder != null ){
                    field.setOrder(fieldOrder.intValue());
                }
                container.addField(field);
            }
        }
        if ( this.checkStructure ){
            /** WARNING : Very costly in term of PERFORMANCE if we have to check
                       the structure again and again !!!
                       We should provide Synchronization tools that perform on demand
                       these structure check !! */
           if ( !this.containerStructureChecked ){
                // this is the first time we perform a container structure check
                ContentObject contentObject = null;
                try {
                    contentObject = (ContentObject) ContentObject.getInstance(this.
                        objectKey);
                } catch (Exception t) {
                    throw new JahiaException("Invalid ContentObject",
                                             "Invalid ContentObject",
                                             JahiaException.DATA_ERROR,
                                             JahiaException.ERROR_SEVERITY,
                                             t);
                }
                this.checkMetadataStructure(contentObject ,container,jParams);
                boolean hasChanged = false;
                for ( Iterator<JahiaField> it = container.getFields(); it.hasNext();){
                    JahiaField field = it.next();
                    Integer I = new Integer(field.getID());
                    if ( this.fields.get(I) == null ){
                        hasChanged = true;
                        JahiaContentFieldFacade contentFieldFacade = new JahiaContentFieldFacade(I.intValue(), loadFlag,
                                    jParams,locales,createMissingLanguages);
                        this.fields.put(I,contentFieldFacade);
                    }
                }
                this.containerStructureChecked = true;
                if ( hasChanged ){
                    // return a clean container instance
                    container = this.loadContainer(loadFlag,loadVersion,
                            jParams,locales,createMissingLanguages);
                }
            }
        }
        if ( container != null ){
            container.sortFieldByOrderAttribute();
        }
        container.setChanged(false);
        return container;
    }

    //--------------------------------------------------------------------------
    private void createContentFieldFacades( JahiaPage page,
                                            int containerId,
                                            ProcessingContext jParams, List<Locale> locales )
    throws JahiaException
    {

        // creates fake fields
        int fakeID = -1; // new single fields start at -1.
        ContentDefinition contentDefinition = null;
        try {
            contentDefinition = ContentDefinition
            .getContentDefinitionInstance(this.objectKey);
        } catch ( ClassNotFoundException cnfe ){
            throw new JahiaException(
                "Cannot retrieve content definition",
                "Cannot retrieve content definition",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);
        }

        List assignedAttributes = contentDefinition.getMetadataDefinitions();
        JahiaFieldDefinition fieldDef = null;
        for ( int i=0; i<assignedAttributes.size(); i++ ){
            try {
            fieldDef = (JahiaFieldDefinition)ContentDefinition
                       .getContentDefinitionInstance(((JahiaObject)
                       assignedAttributes.get(i)).getObjectKey());
            } catch (ClassNotFoundException cnfe) {
                throw new JahiaException(
                "Cannot retrieve content definition",
                "Cannot retrieve content definition",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);
            }
//            int fieldType = fieldDef.getType(0);
//            String fieldValue = fieldDef.getDefaultValue(0);
//            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
//                                           getJahiaVersionService().
//                                           getSiteSaveVersion(0);
            JahiaContentFieldFacade contentFieldFacade
                     = createContentFieldFacade(fieldDef, containerId, page,
                                                fakeID, jParams, locales);
            this.fields.put(new Integer(fakeID), contentFieldFacade);
            this.fieldsOrder.put(new Integer(fakeID),new Integer(i));
            fakeID--;
         }
    }

    //--------------------------------------------------------------------------
    private JahiaContentFieldFacade createContentFieldFacade( JahiaFieldDefinition fieldDef,
                                                      int containerId,
                                                      JahiaPage thePage,
                                                      int fieldID, ProcessingContext jParams,
                                                      List<Locale> locales )
    throws JahiaException
    {
        int     connectType = 0;
        String  fieldValue  = fieldDef.getDefaultValue();

        // to avoid to display the undefined field default value
        if (fieldValue != null
            && fieldValue.toUpperCase().indexOf("JAHIA_MASKTYPE") != -1) {
            fieldValue = "";
        }

        int aclID = 0;
        JahiaContentFieldFacade contentFieldFacade =
                new JahiaContentFieldFacade( fieldID,
                                             thePage.getJahiaID(),
                                             0,
                                             containerId,
                                             fieldDef.getID(),
                                             fieldDef.getType(),
                                             connectType,
                                             fieldValue,
                                             aclID,
                                             jParams,
                                             locales );
        return contentFieldFacade;
    }

    public void checkMetadataStructure (ContentObject contentObject,
                                                     JahiaContainer container,
                                                     ProcessingContext jParams)
        throws JahiaException {
        if (container.getLanguageCode() == null ||
            container.getLanguageCode().trim().equals("")) {
            throw new JahiaException(
                "Not a valid Language Code ( empty Str or null )",
                "Not a valid Language Code ( empty Str or null )",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        // let's order fields as they appear in declaration!
        List fieldOrder = new ArrayList();
        List orderedFields = new ArrayList();
        Map fieldDefs = new HashMap();
        ContentDefinition contentDefinition = null;
        try {
            contentDefinition = ContentDefinition
            .getContentDefinitionInstance(contentObject.getDefinitionKey(null));
        } catch ( ClassNotFoundException cnfe ){
            throw new JahiaException(
                "Cannot retrieve content definition",
                "Cannot retrieve content definition",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);
        }
        List metadataDefs = contentDefinition.getMetadataDefinitions();
        JahiaFieldDefinition fieldDef = null;
        JahiaObject jahiaObject = null;
        for (int i = 0; i < metadataDefs.size(); i++) {
            try {
                jahiaObject = (JahiaObject)metadataDefs.get(i);
                fieldDef = (JahiaFieldDefinition)JahiaFieldDefinition
                      .getContentDefinitionInstance(jahiaObject.getObjectKey());
            } catch (ClassNotFoundException cnfe) {
                throw new JahiaException(
                "Cannot retrieve content definition",
                "Cannot retrieve content definition",
                JahiaException.DATA_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);
            }
            String isDeleted = fieldDef.getProperty("isDeleted");
            String required = fieldDef.getProperty("required");

            if ( !"true".equals(isDeleted) && (!requiredOnly || "true".equals(required)) ) {
                fieldOrder.add(fieldDef.getName());
                orderedFields.add(null); //fake element
                fieldDefs.put(fieldDef.getName(),fieldDef);
            }
        }
        for ( Iterator<JahiaField> it = container.getFields() ; it.hasNext(); ){
            JahiaField field = it.next();
            int fieldPos = fieldOrder.indexOf(field.getDefinition().getName());
            if (fieldPos != -1) {
                logger.debug("JahiaContainer.fieldsStructureCheck : Field " +
                             field.getDefinition().getName() +
                             " has pos : " + fieldPos);
                orderedFields.set(fieldPos, field);
                // why need to put it again ?
                //children.put(field.getDefinition().getName(), field);
                fieldDefs.remove(field.getDefinition().getName());
            } else {
                // seems that we encountered a field for which the definition has benn removed from the container declaration
                // so we ignore this field
            }
        }
        // We check here if the container declaration has changed and if we have to create new
        // declared field for this container.
        Iterator enumeration = fieldDefs.values().iterator();
        fieldDef = null;
        int pageDefID = 0; // global metadata are not stuck on pageDef
        while (enumeration.hasNext()) {
            fieldDef = (JahiaFieldDefinition) enumeration.next();
            logger.debug("JahiaContainer.fieldsStructureCheck : Field " +
                         fieldDef.getName() +
                         " is missing, we have to create a new one ");
            int fieldType = fieldDef.getType();

            int connectType = 0;
            int rank = 0;
            int aclID = 0;
            String fieldValue = fieldDef.getDefaultValue();
            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                                           getJahiaVersionService().
                                           getSiteSaveVersion(0);
            JahiaField field = ServicesRegistry.getInstance().
                    getJahiaFieldService().
                    createJahiaField(0, contentObject.getSiteID(), 0,
                                     0, fieldDef.getID(),
                                     fieldType,
                                     connectType, fieldValue, rank, aclID,
                                     saveVersion.getVersionID(),
                                     saveVersion.getWorkflowState(),
                                     container.getLanguageCode());
            if (field != null) {
                field.setIsMetadata(true);
                field.setMetadataOwnerObjectKey(contentObject.getObjectKey());
                field.setValue(null);
                // save the field
                ServicesRegistry.getInstance().getJahiaFieldService().
                    saveField(field, contentObject.getAclID(), jParams);
                int fieldPos = fieldOrder.indexOf(fieldDef.getName());
                logger.debug("JahiaContainer.fieldsStructureCheck : Field " +
                             field.getDefinition().getName() +
                             " has pos : " + fieldPos);
                if (field.getID()>0) {
                    orderedFields.set(fieldPos, field);
                }

                int order = MetadataBaseService.getInstance().getMatchingMetadatas(contentDefinition).indexOf(fieldDef.getObjectKey());
                this.fieldsOrder.put(new Integer(field.getID()),new Integer(order));
            }
        }

        container.clearFields();
        int size = orderedFields.size();
        for ( int i=0 ; i<size; i++ ){
            JahiaField jahiaField = ((JahiaField)orderedFields.get(i));
            container.addField(jahiaField);
        }
    }

    public boolean isContentObject(){
        return ( this.getObjectKey() instanceof ContentObjectKey );
    }

    public ContentObject getContentObject(){
        try {
            return ContentObject.getContentObjectInstance(this.getObjectKey());
        } catch ( Exception t){
            logger.debug("Cannot get content object", t);
        }
        return null;
    }

}
