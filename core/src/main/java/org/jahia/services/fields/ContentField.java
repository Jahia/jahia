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
package org.jahia.services.fields;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.events.JahiaEventGeneratorService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.utils.LanguageCodeConverters;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * This class represents a Field in Jahia (it should replace JahiaFields in the
 * future). There can't be two ContentField objects with the same ID in memory.
 */
public abstract class ContentField extends ContentObject
        implements ACLResourceInterface, PageReferenceableInterface, Serializable {

    private static final long serialVersionUID = -8431751682771890809L;

    private static final Logger logger = Logger.getLogger(ContentField.class);

    private static final String NO_VALUE = "<no_value>";

    private int fieldType;
    private int jahiaID;
    private int pageID;
    private int containerID;
    private int fieldDefID;
    private int connectType;
    // this is a List of EntryState listing ALL different Entry States
    // for a field, that are ACTIVE or STAGED (including all languages)
    private List<ContentObjectEntryState> activeAndStagingEntryStates;
    // this is a List of EntryState listing ALL different Entry States
    // for VERSIONING (inactive) versions of the field. This List can
    // be NULL if such information is not loaded, else every versioning
    // entry state must be in.
    private List<ContentObjectEntryState> versioningEntryStates;
    // key = the EntryState, value = the Value from the database (DBValue class)
    // actually the key is the EntryState as stored in the DB. if a field
    // is modified here, the key will remain the same (with old versionID) until
    // it has been synched with the DB with the CFS.setField method.
    private Map<ContentObjectEntryState, String> loadedDBValues;


    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private transient JahiaFieldsDataManager fieldsDataManager;
    private ContentObject parent;

    private static transient JahiaVersionService jahiaVersionService;
    private static transient JahiaSearchService jahiaSearchService;
    private static transient JahiaEventGeneratorService jahiaEventService;
    private static transient JahiaFieldService jahiaFieldService;
    private static transient JahiaContainersService jahiaContainersService;

    protected ObjectKey metadataOwnerObjectKey;

    static {
        JahiaObject.registerType (ContentFieldKey.FIELD_TYPE,
                ContentField.class.getName ());
    }

    public static ContentObject getChildInstance (String IDInType) {
        try {
            return getField (Integer.parseInt (IDInType));
        } catch (JahiaException je) {
            logger.debug ("Error retrieving field instance for id : " + IDInType, je);
        }
        return null;
    }

    public static ContentObject getChildInstance (String IDInType, boolean forceLoadFromDB) {
        try {
            return getField (Integer.parseInt (IDInType), forceLoadFromDB);
        } catch (JahiaException je) {
            logger.debug ("Error retrieving field instance for id : " + IDInType, je);
        }
        return null;
    }

    public int getAclID () {
        if (isMetadata()) {
            try {
                ContentObject co = (ContentObject) ContentObject.getInstance(getMetadataOwnerObjectKey());
                return co.getAclID();
            } catch (ClassNotFoundException e) {
            }
        } else if (containerID > 0) {
            try {
                ContentContainer container = ContentContainer.getContainer(containerID);
                return container.getAclID();
            } catch (JahiaException e) {
            }
        }
        return -1;
    }

    public void setAclID(int aclID) {
//        if (containerID <= 0 && !isMetadata()) {
//        if (!isMetadata()) {
//            this.aclID = aclID;
//            fieldsDataManager.updateFieldAclId(getID(), aclID);
//        }
    }

    public int getType () {
        return fieldType;
    }

    public int getSiteID () {
        return jahiaID;
    }

    public int getPageID () {
        return pageID;
    }

    public int getContainerID () {
        return containerID;
    }

    public int getFieldDefID () {
        return fieldDefID;
    }

    public int getConnectType () {
        return connectType;
    }

    public ObjectKey getMetadataOwnerObjectKey() {
        return metadataOwnerObjectKey;
    }

    public void setMetadataOwnerObjectKey(ObjectKey metadataOwnerObjectKey) {
        this.metadataOwnerObjectKey = metadataOwnerObjectKey;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the ContentPage ancestor.
     *
     * @return Return the ContentPage ancestor.
     */
    public ContentPage getPage () throws JahiaException {
        if ( this.getPageID()<=0 ){
            return null;
        }
        return ContentPage.getPage (this.getPageID ());
    }

    /**
     * Returns the identifier of the Content Definition for Content object.
     *
     * @param loadRequest
     *
     * @return
     */
    public int getDefinitionID (EntryLoadRequest loadRequest) {
        return this.getFieldDefID ();
    }

    /**
     * Returns the Definition Key of the Content Definition for this Content object.
     * This is a ContainerDefinition
     *
     * @param loadRequest
     *
     * @return
     */
    public ObjectKey getDefinitionKey (EntryLoadRequest loadRequest) {
        return new FieldDefinitionKey(getDefinitionID (loadRequest));
    }


    /**
     * This constructor can only be called by extensions of this class
     * such as ContentBigTextField, etc.
     * Warning : all constructors signatures must be equal since they are called
     * via class reflection in ContentFieldTools.createFieldInstance !
     */
    protected ContentField (int fieldID, int jahiaID, int pageID, int ctnid, int fieldDefID,
                            int typeField, int connectType, int rights, List<ContentObjectEntryState> activeAndStagingEntryStates,
                            Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (new ContentFieldKey (fieldID));
        this.jahiaID = jahiaID;
        this.pageID = pageID;
        this.containerID = ctnid;
        this.fieldDefID = fieldDefID;
        this.fieldType = typeField;
        this.connectType = connectType;
        this.activeAndStagingEntryStates = activeAndStagingEntryStates;
        this.versioningEntryStates = null;

        this.loadedDBValues = new HashMap<ContentObjectEntryState, String>(activeAndStagedDBValues);

        fieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());

    }

    /**
     * No arg constructor used to comply with Serialization requirements.
     */
    protected ContentField() {

    }

    /**
     * Returns an entry state for the field corresponding to the EntryLoadRequest
     * parameter. This is used notably to create JahiaField facades from
     * ContentFields.
     *
     * @param loadRequest an Entry load request for which to resolve an entry
     *                    state
     *
     * @return an ContentFieldEntryState object that contains the resolved
     *         entry for the content field.
     *
     * @throws JahiaException
     */
    public ContentObjectEntryState getEntryState (EntryLoadRequest loadRequest)
            throws JahiaException {
        List<EntryStateable> entryStateables = new ArrayList<EntryStateable> (activeAndStagingEntryStates);
        if (loadRequest.getWorkflowState () < 1)  // we want a backuped version
        {
            loadVersioningEntryStates ();
            // so we must add the backuped field entry state to the list aswell
            entryStateables.addAll (versioningEntryStates);
        }
        // faire le resolving STAGING/ACTIVE/MULTILANGUE a partir de loadedDBValues
        return (ContentObjectEntryState) getJahiaVersionService ().resolveEntry (
                entryStateables, loadRequest);
    }

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc.
     */
    public String getValue (ProcessingContext jParams) throws JahiaException {
        return getValue(jParams, (jParams!=null)?jParams.getEntryLoadRequest():null);
    }

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc.
     */
    public String getValue (ProcessingContext jParams, EntryLoadRequest loadRequest) throws JahiaException {
        ContentObjectEntryState entryState = getEntryState (loadRequest);
        if (entryState == null) {
            return null;
        }
        return this.getValue (jParams, entryState);
    }

    public String getValue (ContentObjectEntryState entryState)
            throws JahiaException {
        return this.getValue (null, entryState);
    }

    /**
     * Mark a language for deletion, or removes all the content definitively
     * if there is no active version.
     *
     * @param user                     the reference to the user
     * @param languageCode             the language code
     * @param stateModificationContext
     *
     * @throws JahiaException when an internal error occured.
     */
    public void markLanguageForDeletion (JahiaUser user,
                                         String languageCode,
                                         StateModificationContext stateModificationContext)
            throws JahiaException {
        /**
         * NK:
         * Testing if the field will be completely deleted has no meaning,
         * because some fields are SHARED, but their content can be MULTILANGUAL (i.e page fields).
         * Instead, we should state that :
         *       a field will be completely deleted only if its parent ( page or container )
         *       is going to be completely deleted
         */
        boolean stateModified = false;
        if (ContentObject.SHARED_LANGUAGE.equals (languageCode)
                || (!this.isShared () && this.willBeCompletelyDeleted (languageCode, null))) {
            stateModified = true;
            stateModificationContext.pushAllLanguages (true);
        }

        Set<String> languageCodes = new HashSet<String> ();
        if (ContentObject.SHARED_LANGUAGE.equals (languageCode)
                && !this.isShared ()) {
            languageCodes.addAll (getStagingLanguages (true, false));

            for (String curLanguageCode : languageCodes) {
                markContentLanguageForDeletion (user, curLanguageCode,
                        stateModificationContext);
            }
        } else {
            markContentLanguageForDeletion (user, languageCode, stateModificationContext);
        }

        languageCodes = new HashSet<String> ();
        languageCodes.add (languageCode);
        if (stateModified && stateModificationContext.isAllLanguages ()) {
            languageCodes.addAll (getStagingLanguages (false));
        }
        if ((this.isShared () &&
                !this.willAllChildsBeCompletelyDeleted (user, languageCode, null))
                || (this.isShared () && !ContentObject.SHARED_LANGUAGE.equals (languageCode))) {

            // we are not going to delete this content field if
            // it is shared and there are still some undeleted content
            // or the requested lang is not SHARED

            // But we have to create a staged entry to be able to activate this field's staged content
            // we have to create a staged entry to be able to active mark for deleted on this container's fields
            if (this.getStagingLanguages (false, true).isEmpty ()) {
                // if no staging or nor marked for delete exists
                Iterator<ContentObjectEntryState> iterator = this.getActiveAndStagingEntryStates ().iterator ();
                if (iterator.hasNext ()) {
                    ContentObjectEntryState fromEntryState =
                            iterator.next ();
                    ContentObjectEntryState toEntryState =
                            new ContentObjectEntryState (ContentObjectEntryState
                            .WORKFLOW_STATE_START_STAGING, 0,
                                    languageCode);
                    if ( ContentObject.SHARED_LANGUAGE.equals(fromEntryState.getLanguageCode()) ){
                        fromEntryState =
                            new ContentObjectEntryState (fromEntryState.getWorkflowState(),
                                    fromEntryState.getVersionID(),languageCode);
                    }
                    this.copyEntry (fromEntryState, toEntryState);
                }
            }

            if (stateModified) {
                stateModificationContext.popAllLanguages ();
            }

            // update the search index
            /*
            getJahiaSearchService ()
                    .removeFieldFromSearchEngine (this.getSiteID (), this.getID (),
                            ContentObjectEntryState.WORKFLOW_STATE_START_STAGING, languageCode);
            */

            notifyFieldUpdate();

            if (!isMetadata()) {
                WorkflowEvent theEvent = new WorkflowEvent (this, this, user, languageCode, true);
                getJahiaEventService ().fireObjectChanged(theEvent);
            } else {
                RuleEvaluationContext ctx = new RuleEvaluationContext(this.getObjectKey(),this,
                        Jahia.getThreadParamBean(),user);
                getJahiaSearchService ()
                        .indexContentObject(this, user, ctx);
            }

            return;
        }

        for (String curLanguageCode : languageCodes) {
            boolean foundInActive = false;
            for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
                if (thisEntryState.isActive ()) {
                    if (this.isShared () || thisEntryState.getLanguageCode ().equals (
                            curLanguageCode)) {
                        foundInActive = true;
                        break;
                    }
                }
            }
            if (this.isShared ()) {
                // switch lang to shared
                curLanguageCode = ContentObject.SHARED_LANGUAGE;
            }
            EntrySaveRequest saveRequest = new EntrySaveRequest (user,
                    curLanguageCode);
            if (foundInActive) {
                // FIXME : Site language deletion issue :
                // The only case in which we can mark for delete a SHARED lang entry ,
                // is when the languageCode is SHARED, in other situation, we can't
                // mark it for delete
                if ( (this.isShared() && ContentField.SHARED_LANGUAGE.equals(languageCode))
                     || !this.isShared() ){
                    preSet (NO_VALUE, saveRequest);
                    postSet (saveRequest);
                }
            } else {
                JahiaEvent theEvent = new JahiaEvent(this, null, this);
                getJahiaEventService ().fireBeforeStagingContentIsDeleted(theEvent);

                Set<String> tempLanguageCodes = new HashSet<String> ();
                tempLanguageCodes.add (saveRequest.getLanguageCode ());
                deleteStagingEntries (tempLanguageCodes);
            }

            if (!isMetadata()) {
                WorkflowEvent theEvent = new WorkflowEvent (this, this, user, curLanguageCode, true);
                getJahiaEventService ().fireObjectChanged(theEvent);
            } else {
                RuleEvaluationContext ctx = new RuleEvaluationContext(this.getObjectKey(),this,
                        Jahia.getThreadParamBean(),user);
                getJahiaSearchService ()
                        .indexContentObject(this, user, ctx);
            }
        }

        if (stateModified) {
            stateModificationContext.popAllLanguages ();
        }

        notifyFieldUpdate();

        // handled by previous objectChanged event
        // getJahiaSearchService ().indexContentObject(this, user);

    }


    public synchronized ActivationTestResults activate(Set<String> languageCodes,
                                                       boolean versioningActive,
                                                       JahiaSaveVersion saveVersion,
                                                       JahiaUser user,
                                                       ProcessingContext jParams,
                                                       StateModificationContext stateModifContext) throws JahiaException {
        return activate(languageCodes, saveVersion.getVersionID(), jParams, stateModifContext);
    }

    /**
     * This method should be called when the staged version has to be activated.
     *
     * @param JahiaSaveVersion containing the new VersionID
     * @param jParams          ProcessingContext needed to destroy page related data such as
     *                         fields, sub pages, as well as generated JahiaEvents.
     */
    public synchronized ActivationTestResults activate (
            Set<String> languageCodes,
            int newVersionID,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        return activate(languageCodes, newVersionID, jParams, stateModifContext, true);
    }

    /**
     * This method should be called when the staged version has to be activated.
     *
     * @param JahiaSaveVersion containing the new VersionID
     * @param jParams          ProcessingContext needed to destroy page related data such as
     *                         fields, sub pages, as well as generated JahiaEvents.
     */
    protected synchronized ActivationTestResults activate (
            Set<String> languageCodes,
            int newVersionID,
            ProcessingContext jParams,
            StateModificationContext stateModifContext, boolean needBackup) throws JahiaException {
        ActivationTestResults activationResults = new ActivationTestResults ();

        // check that we are not going to create a new version entry that already exist!

        int deletedVersionId = this.getDeleteVersionID();
        if ( deletedVersionId == newVersionID ){
            // has been already deleted! Do not activate deletion again
            activationResults.appendWarning(new NodeOperationResult(getObjectKey(), null, "Field " + getID() +
                    " is already activated with newVersionID=" + newVersionID));
            return activationResults;
        }


        boolean versioningEnabled = getJahiaVersionService ().isVersioningEnabled (jahiaID);
        List<ContentObjectEntryState> stagedEntries = new ArrayList<ContentObjectEntryState> ();
                          
        boolean stateModified = false;
        if (isMarkedForDelete()) {
            stateModified = true;
            stateModifContext.pushAllLanguages (true);
        }

        Set<String> activateLanguageCodes = new HashSet<String> (languageCodes);
        if (stateModifContext.isAllLanguages ()) {
            activateLanguageCodes = new HashSet<String>(LanguageCodeConverters.localesToLanguageCodes(jParams.getSite().getLanguageSettingsAsLocales(true)));
        }

        if (isShared())  {
            if (getStagingLanguages(false,true).isEmpty()) {
                return activationResults;
            } else {
                activateLanguageCodes = new HashSet<String>(LanguageCodeConverters.localesToLanguageCodes(jParams.getSite().getLanguageSettingsAsLocales(true)));
            }
        } else {
            activateLanguageCodes.retainAll(getStagingLanguages(false,true));
            if (activateLanguageCodes.isEmpty()) {
                return activationResults;
            }
        }



        activationResults.merge (
                isValidForActivation (activateLanguageCodes, jParams, stateModifContext));

        if ( !this.isMetadata() ){
            activationResults.merge (
                isPickedValidForActivation(activateLanguageCodes, stateModifContext));
        }

        if (activationResults.getStatus () == ActivationTestResults.FAILED_OPERATION_STATUS) {
            return activationResults;
        }

        // build the List of only staged entries for the languages we want
        // to activate.
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            if (entryState.isStaging ()) {
                if (entryState.getLanguageCode ().equals (ContentField.SHARED_LANGUAGE)
                        || activateLanguageCodes.contains (entryState.getLanguageCode ())) {
                    stagedEntries.add (entryState);
                }
            }
        }

        /** @todo Remove all active entries that will be replaced in the search engine */

        // FIXME NK :
        // We first backup the active ( create an archive entry , then delete the active) before switching the staging to active,
        // but we never guaranteed to switch the staging to active (done by calling changeEntryState )
        // even thought we effectively deleted the active in db.
        //

        // we backup all active entries that will be replaced
        for (int i = 0; i < activeAndStagingEntryStates.size (); i++) {
            ContentObjectEntryState actEntryState = (ContentObjectEntryState) activeAndStagingEntryStates.get (
                    i);
            if (actEntryState.isActive ()) {
                for (ContentObjectEntryState staEntryState : stagedEntries) {
                    if (actEntryState.getLanguageCode ().equals (
                            staEntryState.getLanguageCode ())) {
                        // ok appollo, we catched an active entry here! we handle this..
                        if (needBackup && versioningEnabled && newVersionID != actEntryState.getVersionID()) {
                            try {
                                ContentObjectEntryState backupEntryState = fieldsDataManager.backupField(this,
                                                                                                        actEntryState); // we backup the active version
                                // ok we backuped an entry of the field
                                if (backupEntryState != null) {
                                    // if the entry state list is loaded, let's update it
                                    if (versioningEntryStates != null) {
                                        versioningEntryStates.add (backupEntryState);
                                    }
                                    // also we must call the changeEntryState because, like, we changed the version or something hehe..
                                    this.changeEntryState (actEntryState, backupEntryState,
                                            jParams, stateModifContext);
                                }

                            } catch (JahiaException e) {
                            }
                        } else {
                            // if versioning is not enabled, we delete the old value content!
//                            this.deleteEntry (actEntryState);
                        }
                        // we then delete the active entry
                        this.deleteEntry (actEntryState);
//                        ContentFieldDB.getInstance ().deleteDBValue (this, actEntryState);
//                        activeAndStagingEntryStates.remove (i);
                        i -= 1;
                    }
                }
            }
        }

        // we activate every staged value
        // we must take care of doing the "special trick" here, that is, if
        // versionID = -1 in staged mode, it means we want to delete this entry of
        // the field. So instead of activate it we must create a BACKUPED entry
        // of this field with current version ID and versionStatus = -1
        for (ContentObjectEntryState staEntryState : stagedEntries) {
            ContentObjectEntryState newEntryState = null;
            // if it's -1 -> delete flag
            if (staEntryState.getVersionID () == -1) {
                // create a new entry state (deleted)
                newEntryState = new ContentObjectEntryState (
                        ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED,
                        newVersionID,
                        staEntryState.getLanguageCode ());
            } else {
                // create a new entry state
                newEntryState = new ContentObjectEntryState (
                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE,
                        newVersionID,
                        staEntryState.getLanguageCode ());
            }

            // check if we have to delete this field for REAL
            if ((!versioningEnabled) && (newEntryState.getWorkflowState () <= 1)) {
                // yes, we must delete this field for real!
                this.deleteEntry (staEntryState);
            } else {
                // no, we must just change it/version it, etc...
                // we call the field-type-specific changeEntryState
                activationResults.merge (
                        this.changeEntryState (staEntryState, newEntryState, jParams,
                                stateModifContext));

                /* FIXME NK : should we really abort the activation if there are only warning ?
                 * In case of page field (LINK) created initially without any link, the page field will fail activate
                 * because there is a warning stating that the Content Page is not valid for activation
                 */
                /*
                if (activationResults.getStatus() == ActivationTestResults.COMPLETED_OPERATION_STATUS) {
                */
                if (activationResults.getStatus () != ActivationTestResults.FAILED_OPERATION_STATUS) {
                    // update staged entry in DB into active or deleted entry state
                    fieldsDataManager.changeEntryState (this, staEntryState, newEntryState);
                } else {
                    if (stateModified) {
                        stateModifContext.popAllLanguages ();
                    }
                    syncClusterOnValidation();
                    return activationResults;
                }
            }

            // we remove the staged version from the tables
//            String thisValue = getDBValue(staVerInfo); // let's not do that, we never know...
            activeAndStagingEntryStates.remove (staEntryState); // remove it from the List
            loadedDBValues.remove (staEntryState);              // remove it from the Map

            // we add the new active version to the tables
            // if active-staged version
            if (newEntryState.getWorkflowState () >= 1) {
                activeAndStagingEntryStates.add (newEntryState);    // add it to the List
            } else {
                // versioning version
                if ((versioningEnabled) && (versioningEntryStates != null)) {
                    versioningEntryStates.add (newEntryState);
                }
            }
//            loadedDBValues.put(newVerInfo, thisValue);      // add it to the Map
        }


        // check if an active or staged version still exist, and if not, and
        // if versioning is not enabled, it means the field is REALLY deleted !
        // so we remove it's ACL etc...
        if (activeAndStagingEntryStates.isEmpty() && !versioningEnabled) {
            // true delete...
            ContentFieldTools.getInstance ().purgeFieldData (this);
        }


        /** Remove field from search engine */
        /*
        getJahiaSearchService ().removeFieldFromSearchEngine (this);

        if (!this.willBeCompletelyDeleted (null, null)) {
            getJahiaSearchService().indexContainer(this.getContainerID(), false, jParams);
        }*/

        if (stateModified) {
            stateModifContext.popAllLanguages ();
        }

        JahiaSaveVersion saveVersion = new JahiaSaveVersion (true,
                versioningEnabled, newVersionID);

        if (!isMetadata()) {
            fireContentActivationEvent(activateLanguageCodes,
                    versioningEnabled,
                    saveVersion,
                    jParams,
                    stateModifContext,
                    activationResults);
        } else {
            RuleEvaluationContext ctx = new RuleEvaluationContext(this.getObjectKey(),this,
                    jParams,jParams.getUser());
            getJahiaSearchService().indexContentObject(this, jParams.getUser(), ctx);
        }

        // handled by contentActivationEvent
        //        getJahiaSearchService().indexContentObject(this, jParams.getUser());

        syncClusterOnValidation();
        return activationResults;
    }

    /**
     * This method implements tests for activation validity. Basically what
     * must be done here is test if the field is ready to be activated. This
     * include checking if it has dependencies on content that hasn't been
     * activated yet, or missing languages for mandatory languages.
     * This method calls the isContentValidForActivation method that is
     * polymorphic and implemented for each class of ContentField.
     *
     * @return an ActivationTestResults object that indicates if the activation
     *         would fail, only partially succeed (for example because a page field
     *         depends on a pages that hasn't been validated), or successfully completes.
     *         There are two types of messages returned by the test results : errors,
     *         and warnings. Errors are associated with a test failure, while warnings
     *         are associated with partial completion of the activation.
     */
    public ActivationTestResults isValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = super.isValidForActivation(languageCodes, jParams, stateModifContext);

        int containerId = this.getContainerID();
        ContentContainer contentContainer = null;
        if ( containerId > 0 ){
            contentContainer = ContentContainer.getContainer(containerId);
        }

        // skip test mandatory language if we're going to delete it
        int now = new Long(System.currentTimeMillis()/1000).intValue();

        boolean deleted = this.isDeleted(now);
        boolean markedForDelete = this.isMarkedForDelete();
        if ( ((contentContainer == null) && !(deleted || markedForDelete)) ||
             ((contentContainer != null) && !((contentContainer.isMarkedForDelete() || contentContainer.isDeleted(now)))
              && !(deleted || markedForDelete) ) ){

            // first we must test if we have all the mandatory languages in our
            // field
            JahiaSite theSite = jParams.getSite();

            // first let's check if all the languages have unitialized values,
            // in which case we do not need to do mandatory language checks.
            boolean allEntriesUninitialized = true;
            Map<String, Boolean> languagesInitialized = new HashMap<String, Boolean>(32);
            for (ContentObjectEntryState curEntryState : activeAndStagingEntryStates) {
                if (isEntryInitialized(curEntryState)) {
                    allEntriesUninitialized = false;
                    languagesInitialized.put(curEntryState.getLanguageCode(),Boolean.TRUE);
                }
            }

            if (!markedForDelete && (!allEntriesUninitialized)) {
                List<SiteLanguageSettings> languageSettings = theSite.getLanguageSettings(true);
                for (SiteLanguageSettings curSettings : languageSettings) {
                    if (curSettings.isMandatory()) {
                        // we found a mandatory language, let's check that there at
                        // least an active or a staged entry for this field.
                        boolean foundLanguage = false;
                        if(languagesInitialized.containsKey(curSettings.getCode()) ||
                           languagesInitialized.containsKey(ContentField.SHARED_LANGUAGE))
                        foundLanguage = true;
                        ContentObjectKey mainKey = ServicesRegistry.getInstance().getWorkflowService().getMainLinkObject((ContentObjectKey) getObjectKey());

                        if (!foundLanguage) {
                            final boolean isAdminMember = jParams.getUser().isAdminMember(jParams.getSiteID());
                            activationTestResults.mergeStatus(!isAdminMember ? ActivationTestResults.FAILED_OPERATION_STATUS : ActivationTestResults.PARTIAL_OPERATION_STATUS);
                            try {
                                String containerName = JahiaContainerDefinitionsRegistry.getInstance().getDefinition(getParent(null).getDefinitionID(null)).getTitle(jParams.getLocale());

                                final EngineMessage msg = new EngineMessage(
                                        "org.jahia.services.fields.ContentField.mandatoryLangMissingError",
                                        getFieldTitle(jParams), curSettings.getCode(), containerName, getContainerID());
                                for (String code : languageCodes) {
                                    if (!code.equals(curSettings.getCode())) {
                                        IsValidForActivationResults activationResults = new IsValidForActivationResults(
                                                mainKey, code, msg);
                                        if (!isAdminMember) {
                                            activationResults.setBlocker(true);
                                        }
                                        activationTestResults.appendError(activationResults);
                                    }
                                }
                            } catch (ClassNotFoundException cnfe) {
                                logger.error("Error while creating activation test node result", cnfe);
                            }
                        } else if (!languagesInitialized.containsKey(ContentField.SHARED_LANGUAGE)
                                && !languageCodes.contains(curSettings.getCode())
                                && !hasActiveEntry(curSettings.getCode())) {
                            final boolean isAdminMember = jParams.getUser().isAdminMember(jParams.getSiteID());
                            activationTestResults.mergeStatus(!isAdminMember ? ActivationTestResults.FAILED_OPERATION_STATUS : ActivationTestResults.PARTIAL_OPERATION_STATUS);
                            try {
                                for (String code : languageCodes) {
                                    IsValidForActivationResults activationResults = new IsValidForActivationResults(
                                            mainKey, code, new EngineMessage(
                                                    "org.jahia.services.fields.ContentField.mandatoryLangNotPublished",
                                                    getFieldTitle(jParams), curSettings.getCode()));
                                    if (!isAdminMember) {
                                        activationResults.setBlocker(true);
                                    }
                                    activationTestResults.appendError(activationResults);
                                }
                            } catch (ClassNotFoundException e) {
                                logger.error("Error while creating activation test node result", e);
                            }
                        }
                    }
                }
            }
        }

        activationTestResults.merge(
                isContentValidForActivation(languageCodes, jParams, stateModifContext));

        return activationTestResults;
    }

    private String getFieldTitle(ProcessingContext ctx) throws JahiaException {

        String fieldTitle = null;
        JahiaFieldDefinition fieldDefinition = JahiaFieldDefinitionsRegistry
                .getInstance().getDefinition(getFieldDefID());
        if (fieldDefinition != null) {
            try {
                fieldTitle = fieldDefinition.getTitle(
                        ctx.getLocale());
            } catch (NullPointerException npe) {
                // if we can't solve the title, let's
                // just do nothing.
            }
        }

        return fieldTitle;
    }

    /**
     * This method is used to check if the field has been initialized for
     * a specific entry state. This is mostly used to figure out whether we
     * must perform mandatory language check. If all entry states are un-
     * initialized, we must not perform mandatory language checks.
     *
     * You will want to redefine this method in subclasses in order to check
     * for other empty value markers.
     *
     * @param curEntryState ContentObjectEntryState the entry state for which
     * to check if the entry's value has been initialized.
     * @throws JahiaException
     * @return boolean true if the field has been initialized with a value,
     * false otherwise.
     */
    protected boolean isEntryInitialized (ContentObjectEntryState curEntryState)
        throws JahiaException {
        String entryValue = getValue(curEntryState);
        if (entryValue == null) {
            return false;
        }
        if (!entryValue.equals("") &&
            !entryValue.equals("<empty>")) {
            return true;
        }
        return false;
    }

    /**
     * This method removes all the data related to the staging mode of this
     * field, effectively "undoing" all the changes and returning to the
     * active values.
     *
     * @throws JahiaException in the case there are errors accessing the
     *                        persistant storage system.
     */
    public synchronized void undoStaging (ProcessingContext jParams)
            throws JahiaException {

        // first we construct a List of all the staging versions
        List<ContentObjectEntryState> stagedEntryStates = new ArrayList<ContentObjectEntryState> ();
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            // ok huston, we have a staged version here, let's advise...
            if (entryState.isStaging ()) {
                stagedEntryStates.add (entryState);
            }
        }

        // now let's use that List to destroy all staged versions
        for (ContentObjectEntryState curEntryState : stagedEntryStates) {
            // first we call the field to destroy it's related data
            this.deleteEntry (curEntryState);


            // finally let's remove it from the internal tables.
            activeAndStagingEntryStates.remove (curEntryState); // remove it from the List
            loadedDBValues.remove (curEntryState);              // remove it from the Map

            // now let's delete the database value
            fieldsDataManager.deleteJahiaFields (this, curEntryState);
        }

        notifyFieldUpdate();

        if ( this.hasStagingEntries() || this.hasActiveEntries() ){

            /** Remove field from search engine */
            /*
               getJahiaSearchService().removeContentObject(this);*/

        }

        ContentUndoStagingEvent jahiaEvent = new ContentUndoStagingEvent(this, this.getSiteID(), jParams);
        getJahiaEventService()
                .fireContentObjectUndoStaging(jahiaEvent);
        // handled by previous event
        // update the search index
        //getJahiaSearchService()
        //        .indexContentObject(this, jParams.getUser());

    }

    /**
     * This method purges the fields from the database, removing all versions,
     * all workflow states, everything !
     *
     * @throws JahiaException in case an error occurs while executing the
     *                        purging operations.
     */
    public synchronized void purge ()
            throws JahiaException {
        purgeContent ();

        //deleting the parent will automatically delete the acl
        if ( this.getContainerID() == 0 && !this.isMetadata() && !isAclSameAsParent()){
            JahiaBaseACL acl = getACL ();
            acl.delete ();
            acl = null;
        }

        this.activeAndStagingEntryStates.clear ();
        this.loadedDBValues.clear ();
        if (this.versioningEntryStates != null) {
            this.versioningEntryStates.clear ();
        }

        fieldsDataManager.purgeField (this);
    }

    /**
     * This is the getValue method that should be implemented by the different
     * ContentFields. It is called by the public getValue() method defined in this
     * class, which handles entry resolving
     * This method should call getDBValue to get the DBValue
     */
    public abstract String getValue (ProcessingContext jParams,
                                        ContentObjectEntryState entryState)
            throws JahiaException;

    /**
     * This is the getValue method that should be implemented by the different
     * ContentFields. It is called by the public getValue() method defined in this
     * class, which handles entry resolving
     * This method should call getDBValue to get the DBValue
     *
     * @param jParams the jParam containing the loadVersion and locales
     */
    public abstract String getValueForSearch (ProcessingContext jParams,
                                              ContentObjectEntryState entryState)
            throws JahiaException;

    /**
     * This method is called when there is a workflow state change
     * Such as  staged mode -> active mode (validation), active -> inactive (for versioning)
     * and also staged mode -> other staged mode (workflow)
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     * @param jParams        ProcessingContext object used to get information about the user
     *                       doing the request, the current locale, etc...
     *
     * @return null if the entry state change wasn't an activation, otherwise it
     *         returns an object that contains the status of the activation (whether
     *         successfull, partial or failed, as well as messages describing the
     *         warnings during the activation process)
     */
    protected abstract ActivationTestResults changeEntryState (
            ContentObjectEntryState fromEntryState,
            ContentObjectEntryState toEntryState,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException;

    /**
     * This method implements tests for content activation validity. Basically
     * what must be done here is test if the field content is ready to be
     * activated. This include checking if it has dependencies on content that
     * hasn't been activated yet, or missing languages for mandatory languages.
     *
     * @return an ActivationTestResults object that indicates if the activation
     *         would fail, only partially succeed (for example because a page field
     *         depends on a pages that hasn't been validated), or successfully completes.
     *         There are two types of messages returned by the test results : errors,
     *         and warnings. Errors are associated with a test failure, while warnings
     *         are associated with partial completion of the activation.
     */
    protected abstract ActivationTestResults isContentValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams, StateModificationContext stateModifContext)
            throws JahiaException;


    /**
     * Called at the beginning of a purge operation and must remove all the
     * field related data, recursively if necessary.
     * throws JahiaException if an exception is thrown during the purging of
     * the fields content.
     */
    protected void purgeContent () throws JahiaException {
        // default implementation does nothing.
    }

    /**
     * Called when marking a language for deletion on a field. This is done
     * first to allow field implementation to provide a custom behavior when
     * marking fields for deletion. It isn't abstract because most fields will
     * not need to redefine this method.
     *
     * @param user              the user performing the operation
     * @param languageCode      the language to mark for deletion.
     * @param stateModifContext used to detect loops in deletion marking.
     *
     * @throws JahiaException in the case there was an error processing the
     *                        marking of the content.
     */
    protected void markContentLanguageForDeletion (JahiaUser user,
                                                   String languageCode,
                                                   StateModificationContext stateModifContext)
            throws JahiaException {
        return;
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public abstract boolean isShared ();

    /**
     * Creates a ContentField value in RAM, with no content
     * This method should be called to create a field, and the field content should be set right
     * after that! This method allocates a new AclID and fieldID so don't play with it if you
     * don't have a firm intention to create a field ;)
     *
     * @param siteID      the site identifier to which this new field belongs
     * @param pageID      the page Id to which this new fields belongs
     * @param containerID the containerID to which this new fields belongs
     * @param fieldDefID  the identifier for the field definition
     * @param typeField   an integer specifying the field type (see ContentFieldTypes)
     * @param connectType the type of connection for the field, legacy method
     *                    for storing big text editor type and previously to specify if the field
     *                    was local or remote (datasourcing)
     * @param parentAclid the acl ID of the parent object
     * @param aclID       the ACL for this new field if we already have one, or 0 to
     *                    create a new ACL for the field
     *
     * @return the created content field. Do a set on it right after, if no set is done, the
     *         field will never be written to the DB ! (but its acl won't be deleted)
     */
    protected static ContentField createField (int siteID,
                                                            int pageID, int containerID,
                                                            int fieldDefID,
                                                            int typeField, int connectType,
                                                            int parentAclID, int aclID)
            throws JahiaException {
        return ContentFieldTools.getInstance ().createField (siteID, pageID,
                containerID, fieldDefID, typeField, connectType, parentAclID,
                aclID);
    }

    /**
     * Get a ContentField from its ID
     *
     * @throws JahiaException If the field doesn't exist, or there's a DB error
     */
    public static  ContentField getField (int fieldID)
            throws JahiaException {
       return getField(fieldID, false);
    }

    /**
     * Get a ContentField from its ID
     *
     * @param fieldID
     * @param forceloadFromDB
     *
     * @return
     *
     * @throws JahiaException
     */
    public static ContentField getField (int fieldID, boolean forceLoadFromDB)
            throws JahiaException {
        if (fieldID == 0) {
            return null;
        }
        long start = System.currentTimeMillis();
        ContentField field = ContentFieldTools.getInstance ().getField (fieldID,forceLoadFromDB);
        if (logger.isDebugEnabled()) {
            Throwable throwable = (new Throwable());
            throwable.fillInStackTrace();
            String calledBy = throwable.getStackTrace()[1].getClassName()+"."+throwable.getStackTrace()[1].getMethodName()+ "at line "+throwable.getStackTrace()[1].getLineNumber();
            String calledBy2 = throwable.getStackTrace()[2].getClassName()+"."+throwable.getStackTrace()[2].getMethodName()+ "at line "+throwable.getStackTrace()[2].getLineNumber();
            logger.debug("Retrieving containerListId "+fieldID+ " took "+(System.currentTimeMillis()-start)+" ms called by "+calledBy+ "\n called by "+calledBy2);
        }
        return field;
    }

    /**
     * Get a ContentField from its ID
     *
     * @param fieldID
     * @param forceloadFromDB
     *
     * @return
     *
     * @throws JahiaException
     */
    public static List<ContentField> getFields (List<Integer> fieldIDs, EntryLoadRequest loadVersion, boolean forceLoadFromDB)
            throws JahiaException {
        if (fieldIDs == null || fieldIDs.isEmpty()) {
            return null;
        }
        long start = System.currentTimeMillis();
        List<ContentField> fields = ContentFieldTools.getInstance ().getFields (fieldIDs, loadVersion, forceLoadFromDB);
        if (logger.isDebugEnabled()) {
            Throwable throwable = (new Throwable());
            throwable.fillInStackTrace();
            String calledBy = throwable.getStackTrace()[1].getClassName()+"."+throwable.getStackTrace()[1].getMethodName()+ "at line "+throwable.getStackTrace()[1].getLineNumber();
            String calledBy2 = throwable.getStackTrace()[2].getClassName()+"."+throwable.getStackTrace()[2].getMethodName()+ "at line "+throwable.getStackTrace()[2].getLineNumber();
            logger.debug("Retrieving containerListIds "+fieldIDs+ " took "+(System.currentTimeMillis()-start)+" ms called by "+calledBy+ "\n called by "+calledBy2);
        }
        return fields;
    }

    /**
     * Preloads all the active or staged fields for a given page into the
     * fields cache.
     *
     * @param pageID the page ID for which to preload all the content fields
     *
     * @throws JahiaException thrown if there was an error while loading the
     *                        fields from the database.
     */
    public static void preloadActiveOrStagedFieldsByPageID (int pageID)
            throws JahiaException {
        ContentFieldTools.getInstance ().preloadActiveOrStagedFieldsByPageID (pageID);
    }

    /**
     * Removes a field from the cache if it was present in the cache. If not,
     * this does nothing.
     *
     * @param fieldID the identifier for the field to try to remove from the
     *                cache.
     */
    public static synchronized void removeFromCache (int fieldID) {
    }


    /**
     * Preloads all the active or staged fields for a given container into the
     * field cache.
     *
     * @param containerID the container ID for which to preload all the content fields
     *
     * @throws JahiaException thrown if there was an error while loading the
     *                        fields from the database.
     */
    public static void preloadActiveOrStagedFieldsByContainerID (int containerID)
            throws JahiaException {
        ContentFieldTools.getInstance ().preloadActiveOrStagedFieldsByContainerID (containerID);
    }

    /**
     * Delete a ContentField from its ID
     * (This is a user request. In fact, it just puts a "delete flag" on every language of
     * The field, and the field will really be delete when the page is staging-validated
     * and versioning is disabled)
     *
     * @param user              the user performing the operation
     * @param stateModifContext used to detect loops in deletion marking.
     *
     * @throws JahiaException If the field doesn't exist, or there's a DB error
     */
    public static synchronized void markFieldForDeletion (int fieldID,
                                                          JahiaUser user,
                                                          StateModificationContext stateModifContext)
            throws JahiaException {
        ContentField theField = ContentField.getField (fieldID); // exception is thrown if the field stateModifContextdoesn't exist
        synchronized (theField) {

            ContentFieldTools.getInstance ().markFieldForDeletion (theField, user, theField.activeAndStagingEntryStates,
                                                                   stateModifContext);

        }
    }

    /**
     * Method that HAVE to be called at the beginning of every field setter.
     * It updates the DBValue into the DB
     *
     * @param saveRequest only used to know which language we have to store.
     *
     * @return EntryState of the value stored into the DB, or null if we
     *         couldn't delete the field because it doesn't have an active entry.
     */
    protected ContentObjectEntryState preSet (String newDBValue, EntrySaveRequest saveRequest)
            throws JahiaException {
        int currentStatus = ContentObjectEntryState.WORKFLOW_STATE_START_STAGING;
        // let's see if a currentVerInfo exist
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isStaging ()) {
                currentStatus = thisEntryState.getWorkflowState ();
                break;
            }
        }

        int currentVersionID = 0; // always 0 in staging
        if (newDBValue.equals (NO_VALUE)) {
            currentVersionID = -1;
            // let's lookup the previous value.
            ContentObjectEntryState oldEntryState = null;
            // let's first look in the active values.
            for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
                if ((!thisEntryState.isStaging ()) &&
                        (thisEntryState.getLanguageCode ().equals (
                                saveRequest.getLanguageCode ()))) {
                    oldEntryState = thisEntryState;
                    break;
                }
            }

            if (oldEntryState == null) {
                for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
                    if ((thisEntryState.isStaging ()) &&
                            (thisEntryState.getLanguageCode ().equals (
                                    saveRequest.getLanguageCode ()))) {
                        oldEntryState = thisEntryState;
                        break;
                    }
                }
            }
            // if found we assign the previous value.
            if (oldEntryState != null) {
                newDBValue = getDBValue (oldEntryState);
            }
        }
        String currentLanguage = saveRequest.getLanguageCode ();
        ContentObjectEntryState newEntryState = new ContentObjectEntryState (currentStatus,
                currentVersionID, currentLanguage);
        // end get new version info

        ContentObjectEntryState currentEntryState = null; // it will contain the current versionInfo if it exists in DB
        // let's see if a current entry exists in staging that corresponds to our new value
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if ((thisEntryState.isStaging ()) &&
                    (thisEntryState.getLanguageCode ().equals (saveRequest.getLanguageCode ()))) {
                    currentEntryState = thisEntryState;
                    break;
            }
        }

        // remove old value and version info from the memory tables if it existed
        if (currentEntryState != null) {
            // remove the version from the activeAndStagingVersionInfo
            activeAndStagingEntryStates.remove (currentEntryState);
            // we must remove it from the Map to change the version ID
            loadedDBValues.remove (currentEntryState);
        }

        // we put it back in the Map
        loadedDBValues.put (newEntryState, newDBValue);
        // add the new version from the activeAndStagingVersionInfo
        activeAndStagingEntryStates.add (newEntryState);

        // if the version is new one we create a new EMPTY field version
        if (currentEntryState == null) {
            int id = fieldsDataManager.createValue (this, newEntryState, newDBValue);
            if(id != objectKey.getIdInType())
                this.objectKey = new ContentFieldKey(id);
        }
        else {
            // we update it with the new value
            fieldsDataManager.updateValue (this, newEntryState, newDBValue, true);
        }
        return newEntryState;
    }

    private static Set<String> systemMetadata = new HashSet<String>();

    static {
        systemMetadata.add("created");
        systemMetadata.add("createdBy");
        systemMetadata.add("lastModifiedBy");
        systemMetadata.add("lastModified");
        systemMetadata.add("lastPublisher");
        systemMetadata.add("lastPublishingDate");
        systemMetadata.add("pagePath");
    }

    public void postSet(EntrySaveRequest saveRequest) throws JahiaException{

        if (!isMetadata()) {
            WorkflowEvent theEvent = new WorkflowEvent(this, this, saveRequest.getUser(), saveRequest.getLanguageCode(), false);
            theEvent.setNew(saveRequest.isNew());
            getJahiaEventService ().fireObjectChanged(theEvent);
        } else {
            JahiaFieldDefinition d = (JahiaFieldDefinition) JahiaFieldDefinition.getChildInstance(""+ getDefinitionID(null));
            if (!systemMetadata.contains(d.getName())) {
                try {

                    if (hasActiveEntries() || !"".equals(getValue(null,new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,new ArrayList<Locale>(Arrays.asList(LanguageCodeConverters.languageCodeToLocale(saveRequest.getLanguageCode()))))))) {
                        ContentObject c = (ContentObject) ContentObject.getInstance(getMetadataOwnerObjectKey());
                        Map<String, Integer> ls = c.getLanguagesStates();
                        for (Map.Entry<String, Integer> languageState : ls.entrySet()) {
                            if (languageState.getValue() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                                c.createStaging(languageState.getKey());
                                WorkflowEvent theEvent = new WorkflowEvent(this, c, saveRequest.getUser(), saveRequest.getLanguageCode(), false);
                                theEvent.setNew(saveRequest.isNew());
                                getJahiaEventService ().fireObjectChanged(theEvent);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // !$@$!$
                }

            }
//            stopWatch.start("index postSet");
//            getJahiaSearchService()
//                    .indexContentObject(this,saveRequest.getUser());
//            stopWatch.stop();
        }

    }

    /**
     * @return the language
     */
    protected String getSetLanguageCode (ProcessingContext jParams)
            throws JahiaException {
        if (isShared ()) {
            return (SHARED_LANGUAGE);
        } else {
            return jParams.getLocale ().toString (); // can crash if there is no language in the session (!?!)
        }
    }

    /**
     * Gets the value of the field that is contained in the value_jahia_fields_data
     * column of the database, for a given VersionInfo. Loads it if not already
     * loaded.
     *
     * @throws JahiaException If field/version doesn't exist or there is a database error
     */
    protected String getDBValue (EntryStateable entryState) throws JahiaException {
        // We ensure to check if this content object is Shared or not even though
        // the entryState is provided with a specific language ( en, fr, ... )
        if (entryState == null) {
            return "";
        }

        String languageCode = entryState.getLanguageCode ();
        if (this.isShared ()) {
            languageCode = ContentObject.SHARED_LANGUAGE;
        }
        ContentObjectEntryState objectEntryState = new ContentObjectEntryState (
                entryState.getWorkflowState (), entryState.getVersionID (),
                languageCode);
        String theResult = (String) loadedDBValues.get (objectEntryState);
        // not already in hashmap, let's put it in !
        if (theResult == null) {
            theResult = fieldsDataManager.loadValue (this, objectEntryState);
            if(theResult!=null) {
                loadedDBValues.put (objectEntryState, theResult);
            } else {
                loadedDBValues.put (objectEntryState, org.jahia.data.fields.JahiaField.NULL_STRING_MARKER);
                return org.jahia.data.fields.JahiaField.NULL_STRING_MARKER;
            }
        }
        return theResult;
    }

    /**
     * Gets all the values of the field that are contained in the
     * value_jahia_fields_data column of the database. This does not load up
     * into the caches since the data might be quite large, and will probably
     * not be reused often.
     *
     * @return a Map containing as the key a ContentFieldEntryState object, and
     *         as value the String containing the value for the field.
     *
     * @throws JahiaException If field/version doesn't exist or there is a
     *                        database error
     */
    protected Map<ContentObjectEntryState, String> getAllDBValues () throws JahiaException {
        Map<ContentObjectEntryState, String> theResult = fieldsDataManager.loadAllValues (this.getID());
        return theResult;
    }


    /**
     * Returns true if the field has active entries.
     *
     * @return true if the field has active entries.
     */
    public boolean hasActiveEntries () {
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isActive ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the field has active entry of given language.
     *
     * @return true if the field has active entry of given language.
     */
    public boolean hasActiveEntry(String languageCode) {
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isActive()
                    && (thisEntryState.getLanguageCode().equals(languageCode) || thisEntryState
                            .getLanguageCode().equals(
                                    ContentObject.SHARED_LANGUAGE))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the field has staging entries.
     */
    public boolean hasStagingEntries () {
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isStaging ()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the field has staging entry of given language.
     *
     * @return true if the field has staging entry of given language.
     */
    public boolean hasStagingEntry (String languageCode) {
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isStaging ()
                    && (thisEntryState.getLanguageCode ().equals (
                    languageCode) || thisEntryState.getLanguageCode().equals(ContentObject.SHARED_LANGUAGE))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the field has staging entry of given language.
     *
     * @return true if the field has staging entry of given language.
     */
    public boolean hasStagingEntryIgnoreLanguageCase (String languageCode) {
        for (ContentObjectEntryState thisEntryState : activeAndStagingEntryStates) {
            if (thisEntryState.isStaging ()
                && (ContentObject.SHARED_LANGUAGE.equalsIgnoreCase(thisEntryState.getLanguageCode ())
                || thisEntryState.getLanguageCode ().equalsIgnoreCase(languageCode))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the workflow state of all the languages contained in this field.
     * This returns the state of both the active and staged languages, the
     * staging version taking priority over the active for a given language.
     *
     * @return an Map that contains the language code String as the key,
     *         and the current workflow state of the language is the value
     */
    public Map<String, Integer> getLanguagesStates () {

        Map<String, Integer> languageStates = new HashMap<String, Integer>();

        // first let's get all the active languages in the map.
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            // we only return the highest valued workflow stage between active and staging.
            Integer curWorkflowState = (Integer) languageStates.get(entryState.getLanguageCode ());
            if (curWorkflowState == null || curWorkflowState.intValue() < entryState.getWorkflowState ()) {
                languageStates.put (entryState.getLanguageCode (),
                        new Integer (entryState.getWorkflowState ()));
            }

        }

        return languageStates;
    }


    /**
     * Get an Iterator of active and staged entry state.
     */
    public SortedSet<ContentObjectEntryState> getActiveAndStagingEntryStates () {
        SortedSet<ContentObjectEntryState> entries = new TreeSet<ContentObjectEntryState> ();
        entries.addAll (activeAndStagingEntryStates);
        return entries;
    }

    /**
     * Get an Iterator of all the entrystates. Loads the versioning entry
     * states from the database if they weren't available.
     *
     * @return a SortedSet of ContentFieldEntryState objects.
     */
    public SortedSet<ContentObjectEntryState> getEntryStates ()
            throws JahiaException {
        if (versioningEntryStates == null) {
            versioningEntryStates = fieldsDataManager.loadOldEntryStates (this.getID());
        }
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState> ();
        entryStates.addAll (activeAndStagingEntryStates);
        entryStates.addAll (versioningEntryStates);
        return entryStates;
    }

    /**
     * Allows to set the workflow state of a set of languages in this content
     * object.
     *
     * @param languageCodes    a set of language codes for which to set the new
     *                         workflow state
     * @param newWorkflowState the integer value of the new workflow state
     * @param jParams          a ProcessingContext object useful for some treatments, notably
     *                         when we address ContentPageFields
     * @param withSubPages     if set to true, sub pages inside content page fields
     *                         will also be asked to change their state.
     *
     * @throws JahiaException raised if we have trouble storing the new
     *                        workflow state in persistent storage.
     */
    public void setWorkflowState (Set<String> languageCodes,
                                  int newWorkflowState,
                                  ProcessingContext jParams,
                                  StateModificationContext stateModifContext)
            throws JahiaException {
        List<ContentObjectEntryState> newActiveAndStagingEntryStates = new ArrayList<ContentObjectEntryState> ();
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            // ok huston, we have a staged entry here, let's advise...
            if (entryState.isStaging () &&
                    (ContentObject.SHARED_LANGUAGE.equals (entryState.getLanguageCode ())
                    || languageCodes.contains (entryState.getLanguageCode ())) &&
                    (newWorkflowState >= ContentObjectEntryState.WORKFLOW_STATE_START_STAGING)) {
                // now we must updates both the internal data and the
                // database to reflect these changes.
                ContentObjectEntryState newEntryState =
                        new ContentObjectEntryState (newWorkflowState,
                                entryState.getVersionID (),
                                entryState.getLanguageCode ());

                this.changeEntryState (entryState, newEntryState, jParams, stateModifContext);

                fieldsDataManager.changeEntryState(this, entryState, newEntryState);

                newActiveAndStagingEntryStates.add (newEntryState);

            } else {
                newActiveAndStagingEntryStates.add (entryState);
            }

        }
        activeAndStagingEntryStates = newActiveAndStagingEntryStates;


    }

    /**
     * Change the Field Type. i.e from undefined to another type.
     *
     * @param fieldType        the new field type.
     * @param fieldValue       the field value needed to create a new Staging entry of the new type.
     * @param ParamBean        the param Bean.
     * @param entrySaveRequest
     *
     * @return a new instance of ContentField of new Field Type.
     *
     * @author NK
     */
    public synchronized ContentField changeType (int fieldType,
                                                 String fieldValue,
                                                 ProcessingContext jParams,
                                                 EntrySaveRequest entrySaveRequest)
            throws JahiaException {
        // 1. versioning active.
        // 2. delete staged.
        // 3. create a staged entry with the new type.
        // 4. return a new Instance of ContentField of new Type from DB.

        boolean versioningEnabled = getJahiaVersionService ().isVersioningEnabled (jahiaID);
        Set<String> languageCodes = new HashSet<String> ();

        // 1. we backup all active entries that will be replaced
        for (int i = 0; i < activeAndStagingEntryStates.size (); i++) {
            ContentObjectEntryState actEntryState =
                    (ContentObjectEntryState) activeAndStagingEntryStates.get (i);
            if (actEntryState.isStaging ()) {
                languageCodes.add (actEntryState.getLanguageCode ());
            }
            if (actEntryState.isActive ()) {
                // ok appollo, we caught an active entry here! we handle this..
                if (versioningEnabled) {
                    // we backup the active version
                    ContentObjectEntryState backupEntryState = fieldsDataManager.backupField (this, actEntryState);
                    // ok we backuped an entry of the field
                    if (backupEntryState != null) {
                        // if the entry state list is loaded, let's update it
                        if (versioningEntryStates != null) {
                            versioningEntryStates.add (backupEntryState);
                        }
                        // also we must call the changeEntryState because, like, we changed the version or something hehe..
                        StateModificationContext stateModifContext = new StateModificationContext (
                                new ContentFieldKey (getID ()), languageCodes);
                        stateModifContext.setDescendingInSubPages (false);
                        this.changeEntryState (actEntryState, backupEntryState, jParams,
                                stateModifContext);
                    }
                } else {
                    // if versioning is not enabled, we delete the old value content!
                    this.deleteEntry (actEntryState);
                }
                activeAndStagingEntryStates.remove (i);
                // we then delete the active entry
                fieldsDataManager.deleteJahiaFields (this, actEntryState);
                i -= 1;
            }
        }

        //2. Remove all staging entries
        this.deleteStagingEntries (languageCodes);

        //3. Create a new staging entry of new field type
        this.fieldType = fieldType;
        this.preSet (fieldValue, entrySaveRequest);
        this.postSet (entrySaveRequest);

        // 4. Return a new instance of correct type.
        return ContentFieldTools.getInstance ().getField (this.getID (), true);


    }

    /**
     *  It's used for batch loading of old entry states
     *
     * @throws JahiaException
     */
    public synchronized void setVersioningEntryStates (List<ContentObjectEntryState> entryStates) throws JahiaException {
        if (entryStates != null) {
            this.versioningEntryStates = entryStates;
        }
    }

    /**
     * Delete permanently all the Staging entries
     */
    private void deleteStagingEntries (Set<String> languageCodes)
            throws JahiaException {
        // for data integrity reasons we need to do this in two steps...
        List<ContentObjectEntryState> toBeRemoved = new ArrayList<ContentObjectEntryState> ();
        for (ContentObjectEntryState entryState : activeAndStagingEntryStates) {
            if (languageCodes.contains (entryState.getLanguageCode ())) {
                if (entryState.isStaging ()) {
                    toBeRemoved.add (entryState);
                }
            }
        }

        for (ContentObjectEntryState curEntryState : toBeRemoved) {
            deleteEntry (curEntryState);
        }
    }

    /**
     * @throws JahiaException
     */
    private synchronized void loadVersioningEntryStates () throws JahiaException {
        if (this.versioningEntryStates == null) {
            this.versioningEntryStates = fieldsDataManager.loadOldEntryStates (this.getID());
        }
    }

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest)
            throws JahiaException {
        // default implementation returns no children.
        return Collections.emptyList();
    }

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest,
            int loadFlag) throws JahiaException {
        // default implementation returns no children.
        return Collections.emptyList();
    }

    public ContentObject getParent (EntryLoadRequest loadRequest)
            throws JahiaException {
//        if(parent==null) {
        if ( this.getPageID() == 0 ){
            // this is the case for field used as Metadata
            return null;
        } else if (getContainerID () > 0) {
            parent = ContentContainer.getContainer (getContainerID ());
        } else {
            parent = ContentPage.getPage (getPageID ());
        }
//        }
        return parent;
    }

    public ContentObject getParent (JahiaUser user,
                                    EntryLoadRequest loadRequest,
                                    String operationMode)
            throws JahiaException {
        return getParent(loadRequest);
    }

    private boolean hasEntry (EntryStateable entryState) {
        ContentObjectEntryState entryStateObject = new ContentObjectEntryState (entryState);
        if (entryStateObject.getWorkflowState () >= ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            int objectPos = activeAndStagingEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                return true;
            }
        } else {
            int objectPos = versioningEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                return true;
            }

        }
        return false;
    }

    /**
     * This method is called when a entry should be copied into a new entry
     * it is called when an    old version -> active version   move occurs
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     */
    protected void copyEntry (EntryStateable fromEntryState,
                              EntryStateable toEntryState)
            throws JahiaException {

        ContentObjectEntryState fromE = new ContentObjectEntryState (fromEntryState);
        ContentObjectEntryState toE = new ContentObjectEntryState (toEntryState);
        if (this.isShared ()) {
            // swith to Shared lang
            fromE = new ContentObjectEntryState (fromEntryState.getWorkflowState (),
                    fromEntryState.getVersionID (), ContentObject.SHARED_LANGUAGE);

            toE = new ContentObjectEntryState (toEntryState.getWorkflowState (),
                    toEntryState.getVersionID (), ContentObject.SHARED_LANGUAGE);
        }

        if (hasEntry (toE)) {
            deleteEntry (toE);
        }

        if (toE.getWorkflowState () < ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            versioningEntryStates.add (toE);
        } else {
            activeAndStagingEntryStates.add (toE);
        }
        fieldsDataManager.copyEntry(this, fromE, toE);
        loadedDBValues.put (toE, getDBValue (toE));
    }

    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a object is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext content fields for instance, this method should delete
     * the text file corresponding to the field entry
     *
     * @param deleteEntryState the entry state to delete
     * @param jParams          ProcessingContext needed to destroy page related data such as
     *                         fields, sub pages, as well as generated JahiaEvents.
     */
    protected void deleteEntry (EntryStateable deleteEntryState)
            throws JahiaException {
        if (deleteEntryState.getWorkflowState () < ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            versioningEntryStates.remove (deleteEntryState);
        } else {
            activeAndStagingEntryStates.remove (deleteEntryState);
        }
        loadedDBValues.remove (deleteEntryState);
        fieldsDataManager.deleteEntry (this, deleteEntryState);
    }

    /**
     * Call overridden restoreVersion then, reindex the field in search index
     * if it is not actually marked for delete.
     *
     * @param user
     * @param operationMode
     * @param entryState
     * @param removeMoreRecentActive
     * @param stateModificationContext
     * @return
     * @throws JahiaException
     */
    public RestoreVersionTestResults restoreVersion (JahiaUser user,
        String operationMode,
        ContentObjectEntryState entryState,
        boolean removeMoreRecentActive,
        RestoreVersionStateModificationContext stateModificationContext)
        throws JahiaException {

        RestoreVersionTestResults result =
            super.restoreVersion(user,operationMode,entryState,
                             removeMoreRecentActive,stateModificationContext);

        notifyFieldUpdate();
        // let's inform the cache server that we have updated this object,
        // so that other nodes in the cluster can update their values.

        // handled by previous super.restoreVersion()
        /*
        if (result.getStatus() !=
            RestoreVersionTestResults.FAILED_OPERATION_STATUS ){
            try {
                //getJahiaSearchService()
                //    .removeFieldFromSearchEngine(this);
                getJahiaSearchService().indexContentObject(this, user);
            } catch ( Exception t ){
                logger.debug("Error re-indexing the field " + this.getID()
                + " after restore version operation.",t);
            }
        }*/
        return result;
    }

    /**
     * Returns a JahiaField object corresponding to the current content field
     * and the specified entry load request.
     *
     * @param entryLoadRequest the entry load request for which to creating the
     *                         JahiaField instance
     *
     * @return a JahiaField of the correct instance corresponding to the
     *         content field and the EntryLoadRequest.
     *
     * @throws JahiaException if there was an error while creating the
     *                        JahiaField instance.
     */
    public JahiaField getJahiaField (EntryLoadRequest entryLoadRequest)
            throws JahiaException {
        return getJahiaFieldService ().
                contentFieldToJahiaField (this, entryLoadRequest);
    }

    /**
     * update ContainersChangeEventListener listener
     *
     * @param operation @see ContainersChangeEventListener
     */
    private void notifyFieldUpdate(){
        // do nothing
    }

    public Map<Object, Object> getProperties(){
        return this.properties;
    }

    /**
     * You need to call storeProperties to really store them in Persistence
     */
    public void setProperties(Map<Object, Object> properties){
        this.properties = properties;
    }

    public void storeProperties() throws JahiaException {
        fieldsDataManager.saveProperties(this,this.properties);
    }

  /**
     * Returns the biggest versionID of the active entry, 0 if doesn't exist
     *
     * @return
     *
     * @throws JahiaException
     */
    public int getActiveVersionID () throws JahiaException {
        int versionID = 0;
        for (ContentObjectEntryState entryState : this.getActiveAndStagingEntryStates()) {
            if ( entryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                 && entryState.getVersionID()  > versionID ){
                     versionID = entryState.getVersionID();
            }
        }
        return versionID;
    }

    public boolean isMetadata() {
        try {
            return getJahiaFieldService()
                    .loadFieldDefinition(getFieldDefID()).getJahiaID() == 0;
        } catch (Exception e) {
        }
        return false;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("ContentField: ID = ").
                append(getID()).append(", Type: ").
                append(getObjectKey().getType());
        return buff.toString();
    }

    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds) {
        if (containerID > 0) {
            // We must get the acl associated with the container list
            try {
                ContentContainer container = ContentContainer.getContainer(containerID);
                if (container.getParentContainerListID() > 0) {
                    Map<Object, Object> properties = getJahiaContainersService().getContainerListProperties(container.getParentContainerListID());
                    String aclID = (String)properties.get("view_field_acl_" + getJahiaFieldService().loadFieldDefinition(fieldDefID).getName());
                    if (aclID != null && !"".equals(aclID)) {
                        boolean result = false;
                        try {
                            JahiaBaseACL acl = new JahiaBaseACL(Integer.parseInt(aclID));
                            result = acl.getPermission(user, permission);
                        } catch (JahiaException ex) {
                            logger.debug("Cannot load ACL ID " + getAclID(), ex);
                        }
                        return result;
                    }
                }
            } catch (JahiaException e) {
                logger.error("Cannot check acl on field"+ this.getID(), e);
            }
        }
        return super.checkAccess(user, permission,false);
    }

    /**
     * Check if the user has a specified access to the specified content object.
     * @param user Reference to the user.
     * @param permission One of READ_RIGHTS, WRITE_RIGHTS or ADMIN_RIGHTS permission
     * flag.
     * @return Return true if the user has the specified access to the specified
     * object, or false in any other case.
     */
    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds,boolean forceChildRights) {
        boolean result = false;
        try {
            result = checkAccess(user, permission, false);

            if (result && forceChildRights) {
                for (ContentObject contentObject : getChilds(user, Jahia.getThreadParamBean().getEntryLoadRequest())) {
                    result = contentObject.checkAccess(user, permission,checkChilds,forceChildRights);
                    if (!result) {
                        break;
                    }
                }
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return result;
    }


    public String getPagePathString(ProcessingContext context,
                                    boolean ignoreMetadata) {
        return super.getPagePathString(context,ignoreMetadata);
        /*
        String pagePath = "";
        ContentPage contentPage = null;
        try {
            if ( this.isMetadata() ){
                ContentObject contentObject = getContentObjectFromMetadata(this.getObjectKey());
                if ( contentObject != null ){
                    pagePath = contentObject.getPagePathString(context,ignoreMetadata);
                }
            } else {
                contentPage = this.getPage();
                if ( contentPage != null ){
                    pagePath = contentPage.getPagePathString(context,ignoreMetadata);
                }
            }
        } catch ( Exception t ){
            logger.debug("Exception occured getting pagePath for contentObject " + this.getObjectKey(),t);
        }
        return pagePath;
        */
    }

    public String getDisplayName(ProcessingContext jParams) {
        try {
            return getValue(jParams);
        } catch (JahiaException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getProperty(String name) throws JahiaException {
        return (String) getProperties().get(name);
    }

    public void setProperty(Object name, Object val) throws JahiaException {
        getProperties().put(name, val);
        storeProperties();
    }

    public void removeProperty(String name) throws JahiaException {
        getProperties().remove(name);
        fieldsDataManager.removeFieldProperty(getID(), name);
    }


    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         fieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());
     }

    public static JahiaVersionService getJahiaVersionService() {
        if (jahiaVersionService == null) {
            jahiaVersionService = ServicesRegistry.getInstance().getJahiaVersionService();
        }
        return jahiaVersionService;
    }

    public static JahiaSearchService getJahiaSearchService() {
        if (jahiaSearchService == null) {
            jahiaSearchService = ServicesRegistry.getInstance().getJahiaSearchService();
        }
        return jahiaSearchService;
    }

    public static JahiaEventGeneratorService getJahiaEventService() {
        if (jahiaEventService == null) {
            jahiaEventService = ServicesRegistry.getInstance().getJahiaEventService();
        }
        return jahiaEventService;
    }

    public static JahiaFieldService getJahiaFieldService() {
        if (jahiaFieldService == null) {
            jahiaFieldService = ServicesRegistry.getInstance().getJahiaFieldService();
        }
        return jahiaFieldService;
    }

    public static JahiaContainersService getJahiaContainersService() {
        if (jahiaContainersService == null) {
            jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
        }
        return jahiaContainersService;
    }
}
