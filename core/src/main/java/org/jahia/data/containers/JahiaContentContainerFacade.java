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
package org.jahia.data.containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.data.fields.ExpressionMarker;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.PublicContentFieldEntryState;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntryStateable;

/**
 * Used to hold a set of JahiaContainer instance in multiple language.
 *
 * @author Khue Nguyen
 */
public class JahiaContentContainerFacade implements Serializable, ContainerFacadeInterface {

    private static final long serialVersionUID = 9005601678103023710L;

    private static org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getLogger(JahiaContentContainerFacade.class);

    private int ctnID = -1;

    private int aclID = -1;

    private boolean containerStructureChecked = false;

    // this is a List of EntryState listing ALL different Entry States
    // that are ACTIVE or STAGED.
    private List<EntryStateable> activeAndStagingEntryStates;

    // Map of JahiaContentFieldFacade instances [fieldID,JahiaContentFieldFacade obj]
    private Map<Integer, JahiaContentFieldFacade> fields;

    private List<JahiaContentFieldFacade> orderedFields = null;

    // Map of JahiaContainer instances , key = ContentFieldEntryState
    private Map<EntryStateable, JahiaContainer> containers;
    private boolean createMissingFieldsInDB = true;
    private Map<Integer, JahiaContentFieldFacade> existingFields;

    //--------------------------------------------------------------------------
    /**
     * Constructor for existing Container only
     *
     * @param ctnID, the container unique field identifier.
     * @param loadFlag for fields
     * @param jParams
     * @param locales, the list of locales for which to load content
     * @param createMissingLanguages,
     *              if true, create missing instance for locales not found in db.
     */
    public JahiaContentContainerFacade( int ctnID,
                                        int loadFlag,
                                        ProcessingContext jParams,
                                        List<Locale> locales,
                                        boolean createMissingLanguages )
    throws JahiaException
    {
        this(ctnID,loadFlag,jParams,locales,createMissingLanguages,true);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor for existing Container only
     *
     * @param ctnID, the container unique field identifier.
     * @param loadFlag for fields
     * @param jParams
     * @param locales, the list of locales for which to load content
     * @param createMissingLanguages,
     *              if true, create missing instance for locales not found in db.
     * @param createMissingFields if true, perform field structure check ( if a field is declared but does not exist, create it in persistence )
     *
     * @throws JahiaException
     */
    public JahiaContentContainerFacade( int ctnID,
                                        int loadFlag,
                                        ProcessingContext jParams,
                                        List<Locale> locales,
                                        boolean createMissingLanguages,
                                        boolean createMissingFields )
    throws JahiaException
    {
        this.ctnID      = ctnID;
        this.containers = new HashMap<EntryStateable, JahiaContainer>();
        this.fields     = new HashMap<Integer, JahiaContentFieldFacade>();
        this.activeAndStagingEntryStates = new ArrayList<EntryStateable>();
        this.containerStructureChecked = !createMissingFields;
        instanceContainers(loadFlag, jParams, locales, createMissingLanguages);
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor for a new Container. The container is only stored in memory,
     * nothing stored in persistance.
     *
     */
    public JahiaContentContainerFacade (int ctnID,
                                        int jahiaID,
                                        int pageID,
                                        int ctnListID,
                                        int ctnDefID,
                                        int aclID,
                                        ProcessingContext jParams,
                                        List<Locale> locales )
    throws JahiaException
    {

        this.ctnID      = ctnID;
        this.aclID      = aclID;
        this.containers = new HashMap<EntryStateable, JahiaContainer>();
        this.fields     = new HashMap<Integer, JahiaContentFieldFacade>();
        this.activeAndStagingEntryStates = new ArrayList<EntryStateable>();
        createTempContainers( ctnID, jahiaID, pageID, ctnListID,
                              ctnDefID, aclID, jParams, locales );
    }

    public JahiaContentContainerFacade(JahiaContainer container,JahiaPage page, int loadFlag, ProcessingContext jParams, List<Locale> localeList,
                                       boolean createMissingLanguages, boolean checkStructure, boolean createMissingFieldsInDB) throws JahiaException{
        this.ctnID      = container.getID();
        this.containers = new HashMap<EntryStateable, JahiaContainer>();
        this.fields     = new HashMap<Integer, JahiaContentFieldFacade>();
        this.activeAndStagingEntryStates = new ArrayList<EntryStateable>();
        this.containerStructureChecked = !checkStructure;
        this.createMissingFieldsInDB = createMissingFieldsInDB;
        fillExistingFields(loadFlag, jParams, localeList, createMissingLanguages, page, container);
        instanceContainers(loadFlag, jParams, localeList, createMissingLanguages);
    }

    private void fillExistingFields(int loadFlag, ProcessingContext jParams, List<Locale> localeList, boolean createMissingLanguages, JahiaPage page, JahiaContainer container) throws JahiaException {
        // load all fields no mather they are active, staged or versioned
        // we just want all field ID.
        // create JahiaContentFieldFacade instance for each fieldID.
        existingFields = new HashMap<Integer, JahiaContentFieldFacade>(5);
        for ( Integer I : ServicesRegistry.getInstance()
                .getJahiaContainersService()
                .getFieldIDsInContainer(this.ctnID,null))
        {
            if ( this.fields.get(I) == null ){
                JahiaContentFieldFacade contentFieldFacade =
                        new JahiaContentFieldFacade(I.intValue(), loadFlag,
                                                    jParams,localeList, createMissingLanguages);
                final JahiaField jahiaField = contentFieldFacade.getField(jParams.getEntryLoadRequest(), true);
                if ( jahiaField != null && jahiaField.getDefinition().getItemDefinition() != null){
                    // testing for not null field is required in case of versioning context
                final Integer fieldDefId = new Integer(jahiaField.getDefinition().getID());
                existingFields.put(fieldDefId,contentFieldFacade);
                this.fields.put(I,contentFieldFacade);
            }
        }
        }
        int fakeID = -1; // new single fields start at -1.

        int pageDefID = page.getPageTemplateID();
        Iterator<JahiaContainerStructure> structure = container.getDefinition().getStructure(
                JahiaContainerStructure.JAHIA_FIELD );
        while (structure.hasNext()) {
            JahiaContainerStructure theStruct =
                        (JahiaContainerStructure) structure.next();
            JahiaFieldDefinition theDef =
                        (JahiaFieldDefinition) theStruct.getObjectDef();
            JahiaContentFieldFacade contentFieldFacade
            = createContentFieldFacade( theDef, container, page.getContentPage(), fakeID, jParams, localeList);
            Integer fieldDefId = new Integer(theDef.getID());
            if(!existingFields.containsKey(fieldDefId)) {
                this.fields.put(new Integer(fakeID),contentFieldFacade);
                existingFields.put(fieldDefId,contentFieldFacade);
                fakeID--;
            }
        }
    }

    //--------------------------------------------------------------------------
    public int getAclID(){
        return this.aclID;
    }

    //--------------------------------------------------------------------------
    public int getContainerID(){
        return this.ctnID;
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

        for (EntryStateable entryState : this.activeAndStagingEntryStates){
            JahiaContainer container =
                    (JahiaContainer)this.containers.get(entryState);
            EntryLoadRequest loadRequest = new EntryLoadRequest(entryState);
            JahiaField field = facade.getField(loadRequest,true);
            container.setField(field);
        }
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

    //--------------------------------------------------------------------------
    private void instanceContainers( int loadFlag,
                                     ProcessingContext jParams,
                                     List<Locale> locales,
                                     boolean createMissingLanguages )
    throws JahiaException
    {

        // load all fields no mather they are active, staged or versioned
        // we just want all field ID.
        // create JahiaContentFieldFacade instance for each fieldID.
        for ( Integer I : ServicesRegistry.getInstance()
                .getJahiaContainersService()
                .getFieldIDsInContainer(this.ctnID,null) )
        {
            if ( this.fields.get(I) == null ){
                JahiaContentFieldFacade contentFieldFacade =
                        new JahiaContentFieldFacade(I.intValue(), loadFlag,
                            jParams,locales,createMissingLanguages);
                this.fields.put(I,contentFieldFacade);
            }
        }

        List<Locale> loadLocales = null;
        EntryLoadRequest loadVersion = null;
        for ( Locale locale : locales )
        {
            loadLocales = new ArrayList<Locale>();
            loadLocales.add(locale);

            // load the active container first.
            loadVersion =
                new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE,
                0, loadLocales);

            JahiaContainer container = loadContainer(loadFlag,loadVersion,jParams,locales,createMissingLanguages);
            if ( container != null ){
                container.setLanguageCode(locale.toString());
                if ( !this.containerStructureChecked )
                    container.fieldsStructureCheck(jParams,createMissingFieldsInDB);
                PublicContentFieldEntryState entryStateKey =
                    new PublicContentFieldEntryState(
                                container.getWorkflowState(),
                                container.getVersionID(), locale.toString());
                if ( this.containers.get(entryStateKey)==null ){
                    this.containers.put(entryStateKey,container);
                    this.activeAndStagingEntryStates.add(entryStateKey);
                }
            }

            // load the staging container.
            loadVersion =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                0, loadLocales);

            JahiaContainer stagingContainer = loadContainer(loadFlag,loadVersion,jParams,locales,createMissingLanguages);
            if ( (stagingContainer != null) && (stagingContainer.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE) ){
                stagingContainer.setLanguageCode(locale.toString());
                if ( !this.containerStructureChecked )
                    stagingContainer.fieldsStructureCheck(jParams,createMissingFieldsInDB);
                PublicContentFieldEntryState entryStateKey =
                    new PublicContentFieldEntryState(
                                stagingContainer.getWorkflowState(),
                                stagingContainer.getVersionID(), locale.toString());
                if ( this.containers.get(entryStateKey)==null ){
                    this.containers.put(entryStateKey,stagingContainer);
                    this.activeAndStagingEntryStates.add(entryStateKey);
                }
            } else {
                // we can have fields in staging but container only in active ( due to container update mechanism change !! )
                // so we store the active as staging to ensure we have loaded all staged fields as well
                if ( stagingContainer != null) {
                    stagingContainer.setLanguageCode(locale.toString());
                    if ( !this.containerStructureChecked )
                        stagingContainer.fieldsStructureCheck(jParams,createMissingFieldsInDB);
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
                                       int aclID,
                                       ProcessingContext jParams,
                                       List<Locale> locales )
    throws JahiaException
    {

         EntryLoadRequest loadVersion = null;

         // create tempFields

         // create stating entry state for all languages
         for ( Locale locale : locales )
         {
             List<Locale> entryLocales = new ArrayList<Locale>();
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
                     aclID,ctnDefID,0,ContentObjectEntryState.WORKFLOW_STATE_START_STAGING);

             container.setLanguageCode(locale.toString());
             if ( fields.size()==0 ){

                 // source page
                 ContentPage page = ContentPage.getPage(pageID);
                 if (page == null) {
                     throw new JahiaException( "Page missing",
                         "Trying to add a container on a non-existing page (" + pageID + ")",
                         JahiaException.PAGE_ERROR, JahiaException.ERROR_SEVERITY );
                 }
                 createContentFieldFacades(page,container,jParams,locales);
             }

             // set fields
             for (JahiaContentFieldFacade contentFieldFacade : orderedFields == null ? this.fields.values() : orderedFields){
                 JahiaField field = contentFieldFacade.getField(loadVersion,true);
                 container.addField(field);
             }

             if ( this.containers.get(entryStateKey) == null ){
                 if ( !this.containerStructureChecked )
                     container.fieldsStructureCheck(jParams);
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
        JahiaContainer containerInfo = null;
        try {
            containerInfo = ServicesRegistry.getInstance()
                          .getJahiaContainersService()
                          .loadContainerInfo(this.ctnID,loadVersion);
        } catch ( JahiaException je ) {
        }
        if ( containerInfo == null ){
            return null;
        }

        int effectiveWorkflowState = containerInfo.getWorkflowState();
        int effectiveVersionId = containerInfo.getVersionID();
        if ( containerInfo.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                && loadVersion.getWorkflowState()> EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            effectiveWorkflowState = loadVersion.getWorkflowState();
            effectiveVersionId = loadVersion.getVersionID();
        }

        JahiaContainer container =
            new JahiaContainer (this.ctnID,
            containerInfo.getJahiaID(),
            containerInfo.getPageID(),
            containerInfo.getListID(),
            containerInfo.getRank(),
            containerInfo.getAclID(),
            containerInfo.getctndefid(),
            effectiveVersionId,
            effectiveWorkflowState);
        container.setChanged(false);
        container.setLanguageCode(loadVersion.getFirstLocale(true).toString());

        // set fields
        for ( JahiaContentFieldFacade contentFieldFacade : orderedFields == null ? this.fields.values() : orderedFields){
            JahiaField field = contentFieldFacade.getField(loadVersion,true);

            // the active version doesn't exists, so we request the staging
            // @Fixme : this behavior is quite strange. We're requesting live content, not staging
            //          so I comment this
            /*
            if ( loadVersion.isCurrent() && field == null ){
                EntryLoadRequest stagingVersion
                    = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                                           loadVersion.getVersionID(),
                                           loadVersion.getLocales());
                field = contentFieldFacade.getField(stagingVersion,true);
            }
            */
            if ( field != null ){
                container.addField(field);
            }
        }

        if ( !this.containerStructureChecked ){
            // this is the first time we perform a container structure check
            container.fieldsStructureCheck(jParams,createMissingFieldsInDB);
            Iterator<JahiaField> en = container.getFields();
            orderedFields = new ArrayList<JahiaContentFieldFacade>();
            while (en.hasNext()) {
                JahiaField jahiaField = en.next();
                JahiaContentFieldFacade o;
                if(existingFields != null)
                    o = this.existingFields.get(new Integer(jahiaField.getDefinition().getID()));
                else
                    o = this.fields.get(new Integer(jahiaField.getID()));
                if (o != null) {
                    orderedFields.add(o);
                }
            }
            // we must ensure that all fields are loaded correctly
            Iterator<JahiaField> fields = container.getFields();
            JahiaField field = null;
            boolean hasChanged = false;
            while ( fields.hasNext() ){
                field = fields.next();
                Integer I = new Integer(field.getID());
                if (I.intValue() > 0) {
                    if ((this.fields.get(I) == null && existingFields==null) || (existingFields != null && !existingFields.containsKey(new Integer(field.getDefinition().getID())))) {
                        hasChanged = true;
                        JahiaContentFieldFacade contentFieldFacade =
                                new JahiaContentFieldFacade(I.intValue(), loadFlag,
                                        jParams,locales,createMissingLanguages);
                        this.fields.put(I,contentFieldFacade);
                    }
                }
            }
            if ( hasChanged ){
                // return a clean container instance
                container = this.loadContainer(loadFlag,loadVersion,
                        jParams,locales,createMissingLanguages);
            }
            this.containerStructureChecked = true;
        }

        return container;
    }

    //--------------------------------------------------------------------------
    private void createContentFieldFacades( ContentPage page,
                                            JahiaContainer container,
                                            ProcessingContext jParams, List<Locale> locales )
    throws JahiaException
    {

        // creates fake fields
        int fakeID = -1; // new single fields start at -1.

        int pageDefID = page.getPageTemplateID(jParams);
        Iterator<JahiaContainerStructure> structure = container.getDefinition().getStructure(
                JahiaContainerStructure.JAHIA_FIELD );
        while (structure.hasNext()) {
            JahiaContainerStructure theStruct =
                        (JahiaContainerStructure) structure.next();
            JahiaFieldDefinition theDef =
                        (JahiaFieldDefinition) theStruct.getObjectDef();
            JahiaContentFieldFacade contentFieldFacade
            = createContentFieldFacade( theDef, container, page, fakeID, jParams, locales);
            this.fields.put(new Integer(fakeID),contentFieldFacade);
            fakeID--;
        }
    }

    //--------------------------------------------------------------------------
    private JahiaContentFieldFacade createContentFieldFacade( JahiaFieldDefinition fieldDef,
                                                      JahiaContainer container,
                                                      ContentPage thePage,
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

        if ( (fieldDef.getType() == FieldTypes.SMALLTEXT || fieldDef.getType()== FieldTypes.SMALLTEXT_SHARED_LANG) &&
                fieldValue.startsWith("<jahia-expression")) {
            fieldValue = ExpressionMarker.getValue(fieldValue, jParams);
        }

        int aclID = 0;
        JahiaContentFieldFacade contentFieldFacade =
                new JahiaContentFieldFacade( fieldID,
                                             thePage.getJahiaID(),
                                             thePage.getID(),
                                             container.getID(),
                                             fieldDef.getID(),
                                             fieldDef.getType(),
                                             connectType,
                                             fieldValue,
                                             aclID,
                                             jParams,
                                             locales );
        return contentFieldFacade;
    }
}
