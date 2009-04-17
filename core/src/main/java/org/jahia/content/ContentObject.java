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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.jahia.bin.Jahia;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.*;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.services.workflow.ExternalWorkflow;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.workflow.WorkflowService;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

/**
 * This class is the main content model class, from which all the other
 * content classes derive. It contains information about version,
 * locking, language, state (for workflows), references, etc...
 * @author Serge Huber
 */
public abstract class ContentObject extends JahiaObject {

    private static final long serialVersionUID = -4112230169014398471L;

    public static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContentObject.class);


    public static final String SHARED_LANGUAGE = "shared";
    public static final String PAGEPATH_PAGEID_PREFIX = "jcpid";


    private static transient JahiaLinkManager linkManager;
    private static transient JahiaObjectManager jahiaObjectManager;
    private static transient JahiaFieldsDataManager jahiaFieldsDataManager;
    private ContentObject pickedObject = null;
    private String pickedObjectType = null;

    protected static long lastClusterSyncTime = 0;

    protected ContentObject(ObjectKey objectKey) {
        super(objectKey);
        linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
        jahiaFieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());
    }

    /**
     * No arg constructor to support serialization
     */
    protected ContentObject() {
        linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
        jahiaFieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());
    }

    /**
     * Retrieves all the ContentObjectEntryState for this content object.
     * @return a SortedSet containing ContentObjectEntryState objects.
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states
     */
    public abstract SortedSet<ContentObjectEntryState> getEntryStates ()
        throws JahiaException ;

    public SortedSet<ContentObjectEntryState> getMetadataEntryStates() throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState>();
        List<ContentField> metadatas = this.getMetadatas();
        if (metadatas == null || metadatas.isEmpty()){
            return entryStates;
        }
        for (ContentField metadata : metadatas){
            JahiaFieldDefinition def = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(metadata.getFieldDefID());
            if (!CoreMetadataConstant.notRestorableMetadatas.contains(def.getName())){
                Set<ContentObjectEntryState> metadataEntryStates = metadata.getEntryStates();
                if (metadataEntryStates != null){
                    entryStates.addAll(metadataEntryStates);
                }
            }
        }
        return entryStates;
    }

    /**
     * Retrieves all the active or staging ContentObjectEntryState for this content object.
     * Should be optimized by subclasses
     *
     * @return a SortedSet containing ContentObjectEntryState objects.
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states
     */
    public abstract SortedSet<ContentObjectEntryState> getActiveAndStagingEntryStates()
        throws JahiaException;

    /**
     * Returns all the childrens of this object, given the current context of
     * the current user (permissions are checked), the current EntryLoadRequest
     * (used to determine if we are loading active, staging or versioned
     * entries), and the current operation mode, to check for invalid state
     * mixtures (such as loading a versioned entry in active mode !)
     *
     * @param user the JahiaUser for whom to retrieve the children objects
     * @param loadRequest the EntryLoadRequest specifying for which state
     * to retrive the child objects
     * @return an List of ContentObject objects that are the children of
     * this object. If the case of a JahiaContainer, the children might be
     * ContentFields and ContentContainerLists !
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the children of this object.
     */
    public abstract List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest) throws JahiaException;

    /**
     * Returns all the childrens of this object, given the current context of
     * the current user (permissions are checked), the current EntryLoadRequest
     * (used to determine if we are loading active, staging or versioned
     * entries), and the current operation mode, to check for invalid state
     * mixtures (such as loading a versioned entry in active mode !)
     *
     * @param user the JahiaUser for whom to retrieve the children objects
     * @param loadRequest the EntryLoadRequest specifying for which state
     * to retrive the child objects
     * @param loadFlag JahiaContainerStructure.ALL_TYPES, 
     * JahiaContainerStructure.JAHIA_FIELD or 
     * JahiaContainerStructure.JAHIA_CONTAINER 
     * @return an List of ContentObject objects that are the children of
     * this object. If the case of a JahiaContainer, the children might be
     * ContentFields and ContentContainerLists !
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the children of this object.
     */
    public abstract List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest, int loadFlag) throws JahiaException;
    
    /**
     * Backward compatibility
     * @deprecated
     */
    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest, String operationMode) throws JahiaException {
        return getChilds(user,loadRequest);
    }


    /**
     * Returns the parent ContentObject if there is one, according to the
     * context passed in the parameters
     *
     * @param user the JahiaUser for whom to retrieve the parent object
     * @param loadRequest the EntryLoadRequest specifying for which state
     * to retrive the parent object
     * @param operationMode the operation mode, this is a value of ProcessingContext.NORMAL,
     * ProcessingContext.EDIT or ProcessingContext.PREVIEW
     *
     * @return a ContentObject that is the parent object of this object.
     * Returns null if the object has no parent.
     *
     * @throws JahiaException thrown if there was an error retrieving the parent
     * of this object.
     */
    public abstract ContentObject getParent(JahiaUser user,
                                            EntryLoadRequest loadRequest,
                                            String operationMode)
        throws JahiaException;

    /**
     * Returns the parent ContentObject if there is one, according to the
     * context passed in the parameters
     *
     * @param loadRequest the EntryLoadRequest specifying for which state
     * to retrive the parent object
     * @return a ContentObject that is the parent object of this object.
     * Returns null if the object has no parent.
     *
     * @throws JahiaException thrown if there was an error retrieving the parent
     * of this object.
     */
    public abstract ContentObject getParent(EntryLoadRequest loadRequest)
        throws JahiaException;

    /**
     * Mark a content object's language for deletion. Does nothing if the
     * language doesn't exist, but if it exists only in staging, the staging
     * entry is removed. Make sure you include as many languages as you can
     * when calling this method instead of calling this method multiple times.
     * Since we are doing delete of entries in certain cases (such as content
     * that only exists in staging mode), the content object tree might not be
     * navigateable once this method has been called. Also this method WILL
     * implicetely delete sub content if a content object is to be completely
     * removed by the marking operation
     *
     * @param user the user performing the operation, in order to perform
     * rights checks
     * @param languageCode the language to mark for deletion. Make sure you
     * include as many languages as you can when calling this method instead
     * of calling this method multiple times. Since we are doing delete of
     * entries in certain cases (such as content that only exists in staging
     * mode), the content object tree might not be navigateable once this
     * method has been called.
     * @param stateModificationContext contains the start object of the
     * operation, as well as settings such as recursive descending in sub pages,
     * and content object stack trace.
     *
     * @throws JahiaException raised if there was a problem while marking the
     * content object for deletion (mostly related to communication with the
     * database)
     */
    public abstract void markLanguageForDeletion (JahiaUser user,
                                                  String languageCode,
                                                  StateModificationContext
                                                  stateModificationContext)
        throws JahiaException;

    /**
     * This method is called when a entry should be copied into a new entry
     * it is called when an    old version -> active version   move occurs
     * This method should not write/change the DBValue, the service handles that.
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState the entry state that will be written to the database
     */
    protected abstract void copyEntry (EntryStateable fromEntryState,
                                       EntryStateable toEntryState)
        throws JahiaException;

    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a object is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext content fields for instance, this method should delete
     * the text file corresponding to the field entry
     * @param deleteEntryState the entry state to delete
     * fields, sub pages, as well as generated JahiaEvents.
     */
    protected abstract void deleteEntry (EntryStateable deleteEntryState)
        throws JahiaException;

    /**
     * This method must be implemented by content objects to specify whether
     * it can be restored from this entry state. The entry state must be a
     * versioned entry state or the result will not be predictable.
     *
     * This method should be redefined by content objects that need to add their
     * own checks, calling this method first to make sure the entry states
     * do exist.
     *
     * @param user the JahiaUser performing the request. Can be used for rights
     * checks, especially if doing checks on child objects.
     * @param operationMode the operation mode we are working in, used mostly
     * for accessing child objects.
     * @param entryState a VERSIONED entry state that contains the version ID
     * and language for which to restore.
     * @param removeMoreRecentActive specifies whether content that didn't exist at
     * the time of the versionID but was created and activated after this
     * versionID should be kept or remove (ie marked for deletion).
     * @param stateModificationContext a StateModificationContext class that
     * contains the start object, the path through which we went, as well
     * as whether we are descending recursively through sub pages or not.
     *
     * @return a RestoreVersionTestResults object that contains the status as
     * well as messages resulting of the test for restoration of the request
     * version.
     *
     * @throws JahiaException in case there was a problem retrieving the
     * entry states from the database.
     */
    public RestoreVersionTestResults isValidForRestore(JahiaUser user,
                                                       String operationMode,
                                                       ContentObjectEntryState entryState,
                                                       boolean removeMoreRecentActive,
                                                       StateModificationContext stateModificationContext)
        throws JahiaException {
        RestoreVersionTestResults opResult = new RestoreVersionTestResults();
        if (!removeMoreRecentActive) {
            ContentObjectEntryState resultEntryState =
                getClosestVersionedEntryState(entryState);
            if (resultEntryState == null) {
                opResult.setStatus(RestoreVersionTestResults.FAILED_OPERATION_STATUS);
                opResult.appendError(new RestoreVersionNodeTestResult(
                    getObjectKey(),
                    entryState.getLanguageCode(),
                    "No entry found found for this version"));
            }
        }
        return opResult;
    }

    /**
     * Restores a version of this content object. Basically a restore is a
     * copy operation of a previous version into the staging version, overwriting
     * the existing staging entry if it exists. If the object did not exist
     * at the time and has been created (and activated) since then, an option
     * is used to specify whether it should be kept or removed (ie marked for
     * deletion).
     *
     * @param user the user performing the restore operation, used to do rights
     * checks
     * @param operationMode the operation mode used to do the restore, used to
     * load more or less of the object children depending on the mode. (should
     * mostly always be EDIT)
     * @param entryState the entry state containing the version ID and the
     * language code for which to restore the content object.
     * @param removeMoreRecentActive specifies whether content that didn't exist at
     * the time of the versionID but was created and activated after this
     * versionID should be kept or removed (ie marked for deletion).
     * @param stateModificationContext the state modification context used to
     * specify the start object of the restore operation, whether it should
     * be recursive into the subpages, and a stack trace object that may be
     * use to do loop detection (currently not implemented).
     *
     * @return a RestoreVersionTestResults object that contains the status (failed,
     * partial or complete) and messages resulting of the restore operation on
     * the content object tree.
     *
     * @throws JahiaException in case there was a problem retrieving content
     * object data from the database
     *
     */
    public RestoreVersionTestResults restoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext stateModificationContext)
        throws JahiaException {

        RestoreVersionTestResults opResult = new RestoreVersionTestResults();
        /**
         * This test has no meaning because some content object could miss in some language
         * and this case should not abort the restore process
         *
        opResult.merge(isValidForRestore(user, operationMode, entryState, removeMoreRecentActive, stateModificationContext));
        if (opResult.getStatus() == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
            return opResult;
        }*/

        ContentObjectEntryState resultEntryState = getClosestVersionedEntryState(entryState);

        // now let's find the active and staging entries if they exist for this
        // language code.
        ContentObjectEntryState stagingEntryState = null;
        Set<ContentObjectEntryState> allEntryStates = getEntryStates();
        for (ContentObjectEntryState curEntryState : allEntryStates) {
            if (curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                if (curEntryState.getLanguageCode().equals(entryState.getLanguageCode()) ||
                    ContentObject.SHARED_LANGUAGE.equals(curEntryState.getLanguageCode())) {
                }
            } else if (curEntryState.getWorkflowState() >= ContentObjectEntryState.WORKFLOW_STATE_START_STAGING) {
                if (curEntryState.getLanguageCode().equals(entryState.getLanguageCode()) ||
                    ContentObject.SHARED_LANGUAGE.equals(curEntryState.getLanguageCode())) {
                    stagingEntryState = curEntryState;
                }
            }
        }

        boolean markedForDelete = false;
        boolean isDeleted = this.isDeletedOrDoesNotExist(entryState.getVersionID());
        if (!stateModificationContext.isUndelete() && removeMoreRecentActive) {
            // we're not keeping active versions that date after the requested
            // version, so let's mark it for deletion if it exists.
            if (isDeleted){
                // deleted in all lang
                markLanguageForDeletion(user, ContentObject.SHARED_LANGUAGE,
                                        stateModificationContext);
                markedForDelete = true;
            } else if ( resultEntryState == null || (resultEntryState != null &&
                                                     (resultEntryState.getWorkflowState()
                                                      == EntryLoadRequest.DELETED_WORKFLOW_STATE)) ) {
                // delete only in requested lang
                markLanguageForDeletion(user, entryState.getLanguageCode(),
                                        stateModificationContext);
                markedForDelete = true;
           }
        }

        if ( markedForDelete || resultEntryState == null ) {
            return opResult;
        }
        // if there is a staged version currently, let's remove it first
        if (stagingEntryState != null) {
            deleteEntry(stagingEntryState);
        }

        if ( stagingEntryState == null ||
             stagingEntryState.getVersionID()
             == EntryLoadRequest.DELETED_WORKFLOW_STATE ){
            // mark for deletion, so recreate a new entry
            stagingEntryState = new ContentObjectEntryState(
                ContentObjectEntryState.WORKFLOW_STATE_START_STAGING, 0,
                entryState.getLanguageCode());
        }

        // FIXME : NK
        // At this point, the resultEntryState could be of SHARED language code
        // ( case of shared language field, like Page Field ).
        // This situation actually cause troubles in further DB operations calls.
        // Typically, with content page field, the copyEntry(...) will apply the
        // same entry changes to its Content Object ( ContentPage ) with
        // fromEntryState lang = SHARED while the content page itself is not
        // SHARED !!!!!!!! , which is a wrong situation.
        // that is why we take care to have the correct lang code.
        // This issue was bring out when undeleting page of link types
        resultEntryState = new ContentObjectEntryState(resultEntryState.getWorkflowState(),
                                                       resultEntryState.getVersionID(), entryState.getLanguageCode());
        // same concern with stagingEntryState
        stagingEntryState = new ContentObjectEntryState(stagingEntryState.getWorkflowState(),
                                                        stagingEntryState.getVersionID(),entryState.getLanguageCode());

        // now let's copy from the versioned entry to a staging entry.
        copyEntry(resultEntryState, stagingEntryState);

        // flag that we restored this content object
         opResult.addRestoredContent(this.getObjectKey());

        WorkflowEvent theEvent = new WorkflowEvent (this, this, user, entryState.getLanguageCode(), false);
        ServicesRegistry.getInstance ().getJahiaEventService ().fireObjectChanged(theEvent);

        if ( this.getPageID() > 0 ){
            try {
                ContentObjectKey contentPageKey = new ContentPageKey(this.getPageID());
                if ( this instanceof ContentPage ){
                    int parentId = 0;
                    ContentPage contentPage = (ContentPage)this;
                    EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGED);
                    loadRequest.setWithDeleted(true);
                    loadRequest.setWithMarkedForDeletion(true);
                    parentId = contentPage.getParentID(loadRequest);
                    if ( parentId > 0 ){
                        contentPageKey = new ContentPageKey(parentId);
                        ServicesRegistry.getInstance().getWorkflowService().flushCacheForObjectChanged(contentPageKey);
                    }
                    loadRequest = new EntryLoadRequest(EntryLoadRequest.CURRENT);
                    loadRequest.setWithDeleted(true);
                    parentId = contentPage.getParentID(loadRequest);
                    if ( parentId > 0 ){
                        contentPageKey = new ContentPageKey(parentId);
                        ServicesRegistry.getInstance().getWorkflowService().flushCacheForObjectChanged(contentPageKey);
                    }
                } else {
                    ServicesRegistry.getInstance().getWorkflowService().flushCacheForObjectChanged(contentPageKey);
                }
            } catch ( Exception t ){
                logger.debug("Exeption flushing linked object cache",t);
            }
        }
        return opResult;
    }

    /**
     * Restores a version of this content object. Basically a restore is a
     * copy operation of a previous version into the staging version, overwriting
     * the existing staging entry if it exists. If the object did not exist
     * at the time and has been created (and activated) since then, an option
     * is used to specify whether it should be kept or removed (ie marked for
     * deletion).
     *
     * @param user the user performing the restore operation, used to do rights
     * checks
     * @param operationMode the operation mode used to do the restore, used to
     * load more or less of the object children depending on the mode. (should
     * mostly always be EDIT)
     * @param entryState the entry state containing the version ID and the
     * language code for which to restore the content object.
     * @param removeMoreRecentActive specifies whether content that didn't exist at
     * the time of the versionID but was created and activated after this
     * versionID should be kept or removed (ie marked for deletion).
     * @param stateModificationContext the state modification context used to
     * specify the start object of the restore operation, whether it should
     * be recursive into the subpages, and a stack trace object that may be
     * use to do loop detection (currently not implemented).
     *
     * @return a RestoreVersionTestResults object that contains the status (failed,
     * partial or complete) and messages resulting of the restore operation on
     * the content object tree.
     *
     * @throws JahiaException in case there was a problem retrieving content
     * object data from the database
     *
     */
    public RestoreVersionTestResults metadataRestoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext stateModificationContext)
        throws JahiaException {
        RestoreVersionTestResults result = new RestoreVersionTestResults();
        List<ContentField> metadatas = this.getMetadatas();
        if (metadatas == null){
            return result;
        }
        for (ContentField field : metadatas){
            try {
                JahiaFieldDefinition def = JahiaFieldDefinitionsRegistry.getInstance().getDefinition(field.getFieldDefID());
                if (!CoreMetadataConstant.notRestorableMetadatas.contains(def.getName())){
                    result.merge(field.restoreVersion(user,operationMode,entryState,removeMoreRecentActive,
                        stateModificationContext));
                }
            } catch (Exception t){
                logger.debug("Exception occured restoring metadata field " + field.getID(),t);
            }
        }
        return result;
    }

    /**
     * Returns an integer representing the identifier of the ContentObject.
     * This is SPECIFIC to the ContentObject class, so it's not unique for
     * ALL ContentObjects, just within a specific class.
     * Note: this uses the underlying ObjectKey class, which uses String for
     * the IDInType identifier. So this implementation parses the String and
     * expects a int within. An exception will be raised if no integer is
     * found.
     * @todo solve this IDInType (String) to ID (int) conversion problem.
     * @return an integer representing the identifier of the Object's instance.
     * within the type
     */
    public int getID() {
        return getObjectKey().getIdInType();
    }

    /**
     * Returns the identifier of the Content Definition for this Content object.
     *
     * @param loadRequest
     * @return
     */
    public abstract int getDefinitionID(EntryLoadRequest loadRequest);

    /**
     * Returns the Definition Key of the Content Definition for this Content object.
     *
     * @param loadRequest
     * @return
     */
    public abstract ObjectKey getDefinitionKey(EntryLoadRequest loadRequest);


    /**
     * Find and returns the closest versioned entry state for this content
     * object if it is available. Otherwise returns null.
     *
     * @param entryState a VERSIONED entry state (null returned if invalid),
     * that contains the versionID and language for which to find the entry
     * state.
     *
     * @return a entry state resolved for the object. This can then be used for
     * operations that need to work with real entry states.
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states from the database.
     */
    public ContentObjectEntryState getClosestVersionedEntryState(ContentObjectEntryState entryState)
        throws JahiaException {
        return getClosestVersionedEntryState(entryState,false);
    }

    /**
     * Find and returns the closest versioned entry state for this content
     * object if it is available. Otherwise returns null.
     *
     * @param entryState a VERSIONED entry state (null returned if invalid),
     * that contains the versionID and language for which to find the entry
     * state.
     *
     * @return a entry state resolved for the object. This can then be used for
     * operations that need to work with real entry states.
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states from the database.
     */
    public ContentObjectEntryState getClosestVersionedEntryState(ContentObjectEntryState entryState, boolean smallerVersionOnly)
        throws JahiaException {

        if (entryState.getWorkflowState() >
            ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            logger.debug(
                "Invalid workflow state requested when trying to find versioned entry state: " +
                entryState.getWorkflowState());
            return null;
        }
        // let's test if we have a version id that exists prior to or equal
        // to the one we've been asked for in this language.
        Set<ContentObjectEntryState> entryStates = getEntryStates();
        ContentObjectEntryState resultEntryState = null;

        for (ContentObjectEntryState curEntryState : entryStates) {
            if (curEntryState.getWorkflowState() <= ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                if ( smallerVersionOnly && (curEntryState.getVersionID() < entryState.getVersionID())
                     || !smallerVersionOnly && (curEntryState.getVersionID() <= entryState.getVersionID()) ){
                    // we must now still test if the language corresponds.
                    if (curEntryState.getLanguageCode().equals(entryState.getLanguageCode()) ||
                        curEntryState.getLanguageCode().equals(ContentObject.SHARED_LANGUAGE) ||
                        ContentObject.SHARED_LANGUAGE.equals(entryState.getLanguageCode())) {
                        if (resultEntryState != null) {
                            // now let's test if it's closer to our previous result.
                            if (resultEntryState.getVersionID() < curEntryState.getVersionID()) {
                                resultEntryState = curEntryState;
                            }
                        } else {
                            resultEntryState = curEntryState;
                        }
                    }
                }
            }

        }
        return resultEntryState;
    }

    /**
     * Should be implemented by subobject for optimization
     *
     * Returns true if the content object has active entries.
     * @return
     */
    public boolean hasActiveEntries() throws JahiaException {
        Set<ContentObjectEntryState> entryStates = getActiveAndStagingEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( curEntryState.getWorkflowState()
                 ==ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                return true;
            }
        }
        return false;
    }

    /**
     * Should be implemented by subobject for optimization
     *
     * Returns true if the content object has active entries.
     * @return
     */
    public boolean hasActiveOrStagingEntries() throws JahiaException {
        Set<ContentObjectEntryState> entryStates = getActiveAndStagingEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( curEntryState.getWorkflowState()
                 >=ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this Content Object has an archive entryState
     * before or at a given versionID
     *
     * @param versionID
     * @return
     * @throws JahiaException
     */
    public boolean hasArchiveEntryState(int versionID) throws JahiaException {
        return hasArchiveEntryState(versionID,false);
    }

    /**
     * Returns true if this Content Object has an archive entryState
     * before or at a given versionID
     *
     * @param versionID int
     * @param notDeletedOnly boolean, allow deleted entry or not
     * @throws JahiaException
     * @return boolean
     */
    public boolean hasArchiveEntryState(int versionID,
                                        boolean notDeletedOnly) throws JahiaException {

        Set<ContentObjectEntryState> entryStates = getEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( curEntryState.getWorkflowState()<=ContentObjectEntryState.WORKFLOW_STATE_ACTIVE
                 && curEntryState.getVersionID()<=versionID ){
                if ( !notDeletedOnly ||
                     (curEntryState.getWorkflowState() != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) ){
                return true;
            }
        }
        }
        return false;
    }

    /**
     * Returns true if this Content Object has an archived entryState
     * before a given versionID in the specified locale
     *
     * @param versionID int
     * @param notDeletedOnly boolean, allow deleted entry or not
     * @param locale
     * @throws JahiaException
     * @return boolean
     */
    public boolean hasArchivedEntryStateInLocale(int versionID,
                                                boolean notDeletedOnly, String locale) throws JahiaException {

        Set<ContentObjectEntryState> entryStates = getEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( curEntryState.getLanguageCode().equals(locale) && curEntryState.getWorkflowState()<ContentObjectEntryState.WORKFLOW_STATE_ACTIVE
                 && curEntryState.getVersionID()<versionID ){
                if ( !notDeletedOnly ||
                     (curEntryState.getWorkflowState() != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) ){
                return true;
            }
        }
        }
        return false;
    }

    /**
     * Find and returns the closest versioned entry state for the given
     * version ID, in ALL languages that weren't deleted at the time.
     *
     * @param versionID the identifier of the version we want to retrieve
     * the list of languages that were actually active at the time (deleted
     * languages are only taken into account if the version ID matches the
     * entry state version ID).
     *
     * @return an List containing ContentObjectEntryState objects that
     * are the various language entry states that correspond to the closest
     * versions for each language.
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states from the database.
     */
    public List<ContentObjectEntryState> getClosestVersionedEntryStates(int versionID)
        throws JahiaException {
        Map<String, ContentObjectEntryState> closestInLanguage = new HashMap<String, ContentObjectEntryState>();

        // let's test if we have a version id that exists prior to or equal
        // to the one we've been asked for
        Set<ContentObjectEntryState> entryStates = getEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( ((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) &&
                  (curEntryState.getVersionID() <= versionID))
                 ||
                 ((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONED) &&
                  (curEntryState.getVersionID() <= versionID))
                 ||
                 ((curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) &&
                  (curEntryState.getVersionID() <= versionID))
                 ) {

                // we found an acceptable versioned entry state, let's check
                // if it's closer in the language.
                // first we retrieve the current closest version ID for the
                // language if it exists...
                ContentObjectEntryState resultEntryState = closestInLanguage.get(curEntryState.getLanguageCode());
                if (resultEntryState != null) {
                    // now let's test if it's closer to our previous result.
                    if (resultEntryState.getVersionID() <
                        curEntryState.getVersionID()) {
                        closestInLanguage.put(curEntryState.getLanguageCode(), curEntryState);
                    }
                } else {
                    // no version found for this language, let's add it.
                    closestInLanguage.put(curEntryState.getLanguageCode(), curEntryState);
                }
            }

        }
        List<ContentObjectEntryState> resultEntryStates = new ArrayList<ContentObjectEntryState>(closestInLanguage.values());
        return resultEntryStates;
    }


    /**
     * Check if the user has administration access on the specified content object.
     * Admininistration access means having the ability to add pages, containers
     * and fields, but also giving rights to users to the different objects/
     * applications in the specified page.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has read access for the specified object,
     *           or false in any other case.
     */
    public final boolean checkAdminAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.ADMIN_RIGHTS,false);
    }

    public final boolean checkAdminAccess(JahiaUser user,boolean checkChilds) {
        return checkAccess(user, JahiaBaseACL.ADMIN_RIGHTS,checkChilds);
    }

    /**
     * Check if the user has read access on the specified content object. Read
     * access means having the rights to display and read the content object.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has read access for the specified object,
     *           or false in any other case.
     */
    public final boolean checkReadAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.READ_RIGHTS,false);
    }

    /**
     * Check if the user has write access on the specified content object. Write
     * access means adding new pages, containers and fields in the specified
     * content object.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has read access for the specified object,
     *           or false in any other case.
     */
    public final boolean checkWriteAccess(JahiaUser user) {
        return checkAccess(user, JahiaBaseACL.WRITE_RIGHTS,false);
    }

    public final boolean checkWriteAccess(JahiaUser user,boolean checkChilds) {
        return checkAccess(user, JahiaBaseACL.WRITE_RIGHTS,checkChilds);
    }

    public final boolean checkWriteAccess(JahiaUser user,boolean checkChilds,boolean forceChildsRights) {
        return checkAccess(user, JahiaBaseACL.WRITE_RIGHTS,checkChilds,forceChildsRights);
    }

    /**
     * Check if the user has a specified access to the specified content object.
     * @param user Reference to the user.
     * @param permission One of READ_RIGHTS, WRITE_RIGHTS or ADMIN_RIGHTS permission
     * flag.
     * @return Return true if the user has the specified access to the specified
     * object, or false in any other case.
     */
    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds) {
        boolean result = false;
        try {
            JahiaBaseACL localAcl = getACL();
            result = localAcl.getPermission (user, permission);
            if (!result && checkChilds) {
                for (Iterator<? extends ContentObject> it = getChilds(user, null).iterator(); it.hasNext() && !result;) {
                    ContentObject contentObject = it.next();
                    if(!(contentObject instanceof ContentPage))
                        result = contentObject.checkAccess(user, permission, checkChilds);
                }
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return result;
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
            JahiaBaseACL acl = getACL();
            result = acl.getPermission (user, permission);
            if (result && forceChildRights) {
                List<? extends ContentObject> childs = getChilds(user, Jahia.getThreadParamBean().getEntryLoadRequest());
                for (Iterator<? extends ContentObject> it = childs.iterator(); it.hasNext() && result;) {
                    ContentObject contentObject = it.next();
                    result = contentObject.checkAccess(user, permission, checkChilds, forceChildRights);
                }
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return result;
    }
    
    /**
     * This method is used to determine if all the active entries of this
     * field will be deleted once this object is activated.
     *
     * @param markDeletedLanguageCode an extra language to be removed, to add
     * testing before actually marking for deletion. This may be null in the
     * case we just want to test the current state.
     * @param activationLanguageCodes a set of language for which we are
     * currently activating. May be null if we want to test for all the
     * languages, but if specified will test if the object will be completly
     * deleted when activated with only those languages.
     *
     * @return true if in the next activation there will be no active entries
     * left.
     *
     * @throws JahiaException in case there was a problem retrieving the
     * entry states from the database
     */
    public boolean willBeCompletelyDeleted(String markDeletedLanguageCode,
                                           Set<String> activationLanguageCodes)
        throws JahiaException {

        if (ContentObject.SHARED_LANGUAGE.equals(markDeletedLanguageCode) ){
            return true;
        }

        Set<String> deactivatedLanguageCodes = new HashSet<String>();
        JahiaSite jahiaSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(getSiteID());
        if (jahiaSite != null) {
            List<SiteLanguageSettings> siteLanguageSettings = jahiaSite.getLanguageSettings();
            for (SiteLanguageSettings curSettings : siteLanguageSettings ) {
                if (!curSettings.isActivated()) {
                    deactivatedLanguageCodes.add(curSettings.getCode());
                }
            }
        }

        Set<ContentObjectEntryState> entryStates = getActiveAndStagingEntryStates();
        List<ContentObjectEntryState> stagedEntryStates = new ArrayList<ContentObjectEntryState>();
        List<ContentObjectEntryState> activeEntryStates = new ArrayList<ContentObjectEntryState>();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if (curEntryState.isActive()) {
                activeEntryStates.add(curEntryState);
            } else if (curEntryState.isStaging()) {
                stagedEntryStates.add(curEntryState);
            }
        }

        if ( this.isShared() && markDeletedLanguageCode == null ){
            if ( activeEntryStates.isEmpty() && stagedEntryStates.isEmpty()){
                return true;
            }
            if ( stagedEntryStates.isEmpty() ){
                return false;
            }
            ContentObjectEntryState entry =
                    (ContentObjectEntryState)stagedEntryStates.get(0);
            return ( entry.getVersionID() ==
                     ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED );
        }

        Set<String> languageCodes = new HashSet<String>();

        // first let's get all the active languages into the set.
        for (ContentObjectEntryState entryState : activeEntryStates) {        
            languageCodes.add(entryState.getLanguageCode());
        }

        // now let's remove all the languages that are flagged for deletion.
        for (ContentObjectEntryState entryState : stagedEntryStates) {        
            if (entryState.getVersionID() == -1) {
                if (activationLanguageCodes == null ) {
                    languageCodes.remove(entryState.getLanguageCode());
                } else {
                    // we are only testing for a subset of languages, let's
                    // remove them only if within the subset. We also handle the
                    // case of deactivated language codes here, meaning that deactivated
                    // languages will be included in the removal automatically.
                    if (activationLanguageCodes.contains(entryState.getLanguageCode()) ||
                        deactivatedLanguageCodes.contains(entryState.getLanguageCode()) ) {
                        languageCodes.remove(entryState.getLanguageCode());
                    }
                }
            } else if ( !languageCodes.contains(entryState.getLanguageCode()) ){
                // exist only in staging ( not marked for delete ) , not in active
                languageCodes.add(entryState.getLanguageCode());
            }
        }

        // now let's remove the parameter if added.
        if (markDeletedLanguageCode != null) {
            languageCodes.remove(markDeletedLanguageCode);
        }

        return languageCodes.isEmpty() ? true : false;
    }

    /**
     * This method is used to determine if all the active entries of this
     * content's childs will be deleted once this object is activated.
     *
     * @param markDeletedLanguageCode an extra language to be removed, to add
     * testing before actually marking for deletion. This may be null in the
     * case we just want to test the current state.
     * @param activationLanguageCodes a set of language for which we are
     * currently activating. May be null if we want to test for all the
     * languages, but if specified will test if the object will be completly
     * deleted when activated with only those languages.
     *
     * @return true if in the next activation there will be no active entries
     * left.
     *
     * @throws JahiaException in case there was a problem retrieving the
     * entry states from the database
     */
    public boolean willAllChildsBeCompletelyDeleted(JahiaUser user,
                                                    String markDeletedLanguageCode, Set<String> activationLanguageCodes)
        throws JahiaException {

        List<? extends ContentObject> childs = getChilds(user,null);
        for (ContentObject contentObject : childs) {
            if (!contentObject.willBeCompletelyDeleted(markDeletedLanguageCode, activationLanguageCodes)) {
                return false;
            }
            if (!contentObject.willAllChildsBeCompletelyDeleted(user, markDeletedLanguageCode, activationLanguageCodes)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a set of languages that represents all the languages in active or
     * staging mode, including or not the languages marked for deletion.
     * 
     * @param includingMarkedForDeletion
     *            set this boolean to true if you want the result to include the
     *            languages marked for deletion in staging mode.
     * @return a Set of String objects that contain the language codes.
     * @throws JahiaException
     *             thrown in case there was a problem loading the entry states
     *             from the database.
     */
    public Set<String> getStagingLanguages(boolean includingMarkedForDeletion)
        throws JahiaException {
        return getStagingLanguages(true, includingMarkedForDeletion);
    }

    /**
     * Returns a set of languages that represents all the languages in active
     * or staging mode, including or not the languages marked for deletion.
     * @param includingMarkedForDeletion set this boolean to true if you want
     * the result to include the languages marked for deletion in staging mode.
     * @return a Set of String objects that contain the language codes.
     * @throws JahiaException thrown in case there was a problem loading the
     * entry states from the database.
     */
    public Set<String> getStagingLanguages(boolean withActive,
                                   boolean includingMarkedForDeletion)
        throws JahiaException {
        Set<String> languageCodes = new HashSet<String>();
        Set<ContentObjectEntryState> entryStates = getEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if ( (withActive && curEntryState.isActive()) || curEntryState.isStaging()) {
                languageCodes.add(curEntryState.getLanguageCode());
            }
        }

        if (!includingMarkedForDeletion) {
            // now let's remove the languages marked for deletion.
            for (ContentObjectEntryState curEntryState : entryStates) {
                if (curEntryState.isStaging() &&
                    (curEntryState.getVersionID() == -1)) {
                    languageCodes.remove(curEntryState.getLanguageCode());
                }
            }
        }

        return languageCodes;
    }

    /**
     * Resolve to a matching entryState, can be null
     *
     *
     * @param entryState
     * @param smallerVersionOnly, only if versioned
     * @param activeOrStaging, when requesting a staging and not exists, return or not the staging
     * @return
     * @throws JahiaException
     */
    public ContentObjectEntryState getEntryState(ContentObjectEntryState entryState,
                                                 boolean smallerVersionOnly, boolean activeOrStaging) throws JahiaException {
        if ( entryState == null ){
            return null;
        }
        if (entryState.getWorkflowState() < ContentObjectEntryState.WORKFLOW_STATE_ACTIVE){
            return getClosestVersionedEntryState(entryState,smallerVersionOnly);
        } else {
            Set<ContentObjectEntryState> entryStates = this.getActiveAndStagingEntryStates();
            ContentObjectEntryState activeEntryState = null;
            ContentObjectEntryState stagingEntryState = null;
            for (ContentObjectEntryState es :  entryStates){
                if ( es.isActive() &&
                     es.getLanguageCode().equals(entryState.getLanguageCode()) ){
                    activeEntryState = es;
                }
                if ( es.isStaging() &&
                     es.getLanguageCode().equals(entryState.getLanguageCode()) ){
                    stagingEntryState = es;
                }
            }
            if ( entryState.isActive() && activeEntryState != null ){
                return activeEntryState;
            }
            if ( entryState.isStaging() && stagingEntryState != null ){
                return stagingEntryState;
            }
            if ( entryState.isStaging() && activeOrStaging ){
                return activeEntryState;
            }
        }
        return null;
    }

    /**
     * Returns the versionID at which the content was the last time deleted.
     *
     * @return -1 if the content is not actually deleted otherwise the versionID of the
     * last delete operation.
     * @throws JahiaException
     */
    public int getDeleteVersionID() throws JahiaException {
        int versionID = -1;
        ContentObjectEntryState resultEntryState = null;
        
        Set<ContentObjectEntryState> entryStates = this.getActiveAndStagingEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates){
            if ( curEntryState.getWorkflowState()
                 == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                return -1;
            }
        }

        entryStates = this.getEntryStates();
        for (ContentObjectEntryState curEntryState : entryStates) {
            if (curEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {

                if (resultEntryState == null) {
                    resultEntryState = curEntryState;
                } else if (resultEntryState.getVersionID() < curEntryState.getVersionID()) {
                    resultEntryState = curEntryState;
                }
            }
        }
        if ( resultEntryState != null ){
            versionID = resultEntryState.getVersionID();
        }
        return versionID;
    }

    /**
     * Return true if the content object is deleted in all language at a given
     * date or doesn't exist
     *
     * @param versionID
     * @return
     */
    public boolean isDeletedOrDoesNotExist(int versionID) throws JahiaException{

        List<ContentObjectEntryState> entryStates =
                this.getClosestVersionedEntryStates(versionID);

        for (ContentObjectEntryState entryState : entryStates) {        
            if ( entryState.getWorkflowState() !=
                 ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the content object is deleted in all language at a given
     * date.
     * @param versionID
     * @return
     */
    public boolean isDeleted(int versionID) throws JahiaException{
        List<ContentObjectEntryState> entryStates =
                this.getClosestVersionedEntryStates(versionID);
        if (entryStates.isEmpty()) {
            return false;
        }
        for (ContentObjectEntryState entryState : entryStates) {        
            if ( entryState.getWorkflowState() !=
                 ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the current content object is marked for delete
     * in all languages
     * @return true if true
     */
    public boolean isMarkedForDelete() throws JahiaException{
        Set<ContentObjectEntryState> entryStates = getActiveAndStagingEntryStates();
        if ( entryStates.isEmpty() ){
            return false;
        }
        Map<String, Boolean> states = new HashMap<String, Boolean>();
        for (ContentObjectEntryState entryState : entryStates){
            Boolean bool = (Boolean)states.get(entryState.getLanguageCode());
            if ( bool == null || !bool.booleanValue() ){
                if (entryState.getVersionID() !=
                    EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                    states.put(entryState.getLanguageCode(), Boolean.FALSE);
                } else {
                    states.put(entryState.getLanguageCode(), Boolean.TRUE);
                }
            }
        }

        for ( Boolean bool : states.values() ){
            if ( !bool.booleanValue() ){
                return false;
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Object marked for delete:" + this + " entryStates: " + entryStates);
        }
        return true;
    }

    /**
     * Return true if the current content object is marked for delete for the given languageCode
     *
     * @return
     */
    public boolean isMarkedForDelete(String languageCode) throws JahiaException {
        if ( languageCode == null ){
            return false;
        }

        Set<ContentObjectEntryState> entryStates = getActiveAndStagingEntryStates();
        for (ContentObjectEntryState entryState : entryStates) {
            if (entryState.isStaging() && (this.isShared()
                                           || languageCode.equals(entryState.getLanguageCode()))
                && entryState.getVersionID() ==
                   EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method removes all the data related to the staging mode of this
     * object, effectively "undoing" all the changes and returning to the
     * active values.
     * @throws JahiaException in the case there are errors accessing the
     * persistant storage system.
     */
    public abstract void undoStaging(ProcessingContext jParams)
    throws JahiaException;

    /**
     * Is this kind of object shared (i.e. not one version for each language, but one version for every language)
     */
    public abstract boolean isShared ();

    /**
     * Returns the corresponding ACL id for a content object. This is currently
     * an abstract method because the persistence model is different for each
     * content object implementation, but this might change in the future.
     *
     * @return an integer corresponding to the ACL ID of the content object.
     */
    public abstract int getAclID();

    public abstract void setAclID(int acl);

    /**
     * Return the objects's ACL object. Based on getAclID() implementation.
     *
     * @return Return the object's ACL.
     */
    public JahiaBaseACL getACL () {
        JahiaBaseACL acl = null;
        try {
            acl = new JahiaBaseACL(getAclID());
        } catch (Exception t) {
            logger.error("Error getting ACL", t);
        }
        return acl;
    }

    /**
     * Should return a human readable title for this object
     * Only implemented for pages right now
     *
     * @param lastUpdatedTitles
     * @return
     */
    public Map<String , String> getTitles (boolean lastUpdatedTitles) {
        return new HashMap<String , String>();
    }


    /**
     * Return the site ID in which the object is located.
     *
     * @return Return the page site ID.
     */
    public abstract int getSiteID();

    public ActivationTestResults isValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults t = new ActivationTestResults();
        return t;
    }

    public ActivationTestResults isPickedValidForActivation(
            Set<String> languageCodes,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = new ActivationTestResults();
        ContentObject pickedObject = getPickedObject(StructuralRelationship.CHANGE_PICKER_LINK);
        if (pickedObject != null) {
            for (Iterator<String> iterator = languageCodes.iterator(); iterator.hasNext();) {
                String s = iterator.next();
                if (pickedObject.getStagingStatus(s) != VersioningDifferenceStatus.UNCHANGED) {
                    try {
                        activationTestResults.setStatus (ActivationTestResults.FAILED_OPERATION_STATUS);
                        ContentObjectKey mainKey = ServicesRegistry.getInstance().getWorkflowService().getMainLinkObject((ContentObjectKey) getObjectKey());
                        final EngineMessage msg = new EngineMessage(
                                "org.jahia.content.ContentObject.validateSourceObjectError");
                        activationTestResults.appendError(new IsValidForActivationResults(mainKey, s, msg));
                    } catch (ClassNotFoundException e) {
                        //...
                    }
                }
            }
        }
        return activationTestResults;
    }

    public void setWorkflowState (Set<String> languageCodes,
                                  int newWorkflowState,
                                  ProcessingContext jParams,
                                  StateModificationContext stateModifContext)
            throws JahiaException {
    }

    public Map<String, Integer> getLanguagesStates () {
        return new HashMap<String, Integer>();
    }

    /**
     * Overriden class must fire contentObjectActivate event after activation
     *
     * @param languageCodes Set
     * @param versioningActive boolean
     * @param saveVersion JahiaSaveVersion
     * @param user JahiaUser
     * @param jParams ProcessingContext
     * @param stateModifContext StateModificationContext
     * @throws JahiaException
     * @return ActivationTestResults
     */
    public synchronized ActivationTestResults activate (
            Set<String> languageCodes,
            boolean versioningActive, JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        ActivationTestResults result = new ActivationTestResults();
        fireContentActivationEvent(languageCodes,versioningActive,saveVersion,jParams,stateModifContext,result);
        syncClusterOnValidation();
        return result;
    }

    /**
     * sub classes must call this method at the end of their activate method.
     * It's necessary for the indexation server to synchronize live content.
     *
     */
    public void syncClusterOnValidation(){

        long now = System.currentTimeMillis();
        if ( now-lastClusterSyncTime>3000 ){
            lastClusterSyncTime = now;
            ServicesRegistry.getInstance().getCacheService().syncClusterNow();
            JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
        }
    }

    /**
     * Activate content object's metadatas
     *
     * @param languageCodes Set
     * @param versioningActive boolean
     * @param saveVersion JahiaSaveVersion
     * @param user JahiaUser
     * @param jParams ProcessingContext
     * @param stateModifContext StateModificationContext
     * @throws JahiaException
     * @return ActivationTestResults
     */
    public synchronized ActivationTestResults activateMetadatas(Set<String> languageCodes,
                                                                boolean versioningActive,
                                                                JahiaSaveVersion saveVersion,
                                                                JahiaUser user,
                                                                ProcessingContext jParams,
                                                                StateModificationContext stateModifContext) throws JahiaException {

        ActivationTestResults result = new ActivationTestResults();
        try {
            for (ContentField contentField : getStagedMetadatas()) {
                try {
                    contentField.activate(languageCodes,
                                          versioningActive,
                                          saveVersion,
                                          user,
                                          jParams,
                                          stateModifContext);
                } catch (Exception t) {
                    logger.debug("Exception activating content object's metadata", t);
                }
            }
        } catch (Exception t) {
            logger.error("Exception activating content object's metadatas ", t);
        }
        return result;
    }

    private List<ContentField> getStagedMetadatas() throws JahiaException {
        return getMetadatas(getObjectKey(), true, true);
    }

    /**
     * Must be called by Subclasses activate method.
     *
     * @param languageCodes Set
     * @param versioningActive boolean
     * @param saveVersion JahiaSaveVersion
     * @param jParams ProcessingContext
     * @param stateModifContext StateModificationContext
     * @throws JahiaException
     */
    public void fireContentActivationEvent (Set<String> languageCodes,
                                            boolean versioningActive,
                                            JahiaSaveVersion saveVersion,
                                            ProcessingContext jParams,
                                            StateModificationContext stateModifContext,
                                            ActivationTestResults result)
    throws JahiaException {

        ContentActivationEvent event = new ContentActivationEvent(this, this.getObjectKey(), jParams.getUser(),
                languageCodes, versioningActive, saveVersion, jParams, stateModifContext, result);

        ServicesRegistry.getInstance().getJahiaEventService().fireContentActivation(event);

    }

    /**
     * Return an Array of ContentField that have a core "metadata" strutural
     * relationship with this one.
     * 
     * @see StructuralRelationship.METADATA_LINK
     * 
     * @return List of JahiaObject
     */
    public List<ContentField> getMetadatas() throws JahiaException {
        return getMetadatas(this.getObjectKey());
    }

    /**
     * Return an Array of ContentField that have a core "metadata" strutural relationship
     * with this one. @see StructuralRelationship.METADATA_LINK
     *
     * @param withOldEntryState if true, batch load field's old entrystates for better performance issue
     * @return List of JahiaObject
     */
    public List<ContentField> getMetadatas(boolean withOldEntryState) throws JahiaException {
        return getMetadatas(this.getObjectKey(),withOldEntryState);
    }

    /**
     * Return the ContentField metadata with this one and having the given name.
     *
     * @return a JahiaObject
     */
    public ContentField getMetadata(String name) throws JahiaException {
        return getMetadata(name, false);
    }

    /**
     * Return the ContentField metadata with this one and having the given name.
     *
     * @return a JahiaObject
     */
    public ContentField getMetadata(String name, boolean forceLoadFromDB) throws JahiaException {
        return jahiaFieldsDataManager.loadMetadataByOwnerAndName(name,this.getObjectKey(), forceLoadFromDB);
    }
    
    /**
     * Return the ContentField metadatas with this one and having the given names.
     *
     * @return a JahiaObject
     */
    public Map<String, ContentField> getMetadatas(String[] names,
            EntryLoadRequest loadVersion, boolean forceLoadFromDB)
            throws JahiaException {
        return jahiaFieldsDataManager.loadMetadataByOwnerAndNames(names, this
                .getObjectKey(), loadVersion, forceLoadFromDB);
    }    

    /**
     * Returns the metadata as a JahiaField instance
     *
     * @param name the metadata name
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public JahiaField getMetadataAsJahiaField(String name,
                                              ProcessingContext jParams)
    throws JahiaException {
        return getMetadataAsJahiaField(name, jParams, false);
    }

    /**
     * Returns the metadata as a JahiaField instance
     *
     * @param name the metadata name
     * @param jParams
     * @param forceLoadFromDB
     * @return
     * @throws JahiaException
     */
    public JahiaField getMetadataAsJahiaField(String name,
                                              ProcessingContext jParams,
                                              boolean forceLoadFromDB)
    throws JahiaException {

        ContentField contentField = this.getMetadata(name, forceLoadFromDB);
        if ( contentField == null ){
            return null;
        }
        return ServicesRegistry.getInstance().getJahiaFieldService()
                .loadField(contentField.getID(),LoadFlags.ALL,jParams);
    }
    
    /**
     * Returns the metadata value
     *
     * @param name the metadata name
     * @param jParams
     * @param defaultValue
     * @return
     * @throws JahiaException
     */
    public String getMetadataValue(String name,
                                   ProcessingContext jParams,
                                   String defaultValue) throws JahiaException {

        JahiaField jahiaField = this.getMetadataAsJahiaField(name,jParams);
        if ( jahiaField == null || jahiaField.getValue() == null){
            return defaultValue;
        } else {
            return jahiaField.getValue();
        }
    }
    
    /**
     * Returns the metadata value
     *
     * @param name the metadata name
     * @param jParams
     * @param defaultValue
     * @return
     * @throws JahiaException
     */
    public Map<String, String> getMetadatasValue(String[] names,
            ProcessingContext jParams, String[] defaultValues)
            throws JahiaException {
        Map<String, ContentField> contentFields = this.getMetadatas(names,
                jParams.getEntryLoadRequest(), false);
        Map<String, String> result = new HashMap<String, String>(names
                .length);
        int i = 0;
        for (String name : names) {
            ContentField contentField = contentFields.get(name);
            String value = contentField != null ? contentField
                    .getValue(jParams) : null;
            result.put(name, value != null ? value : defaultValues[i]);
            i++;
        }
        return result;
    }    

    /**
     * For use with Date metadata.
     *
     * @param name
     * @param jParams
     * @param defaultValue
     * @param dateFormat an optional date formatter
     * @return
     * @throws JahiaException
     */
    public String getMetadataDateValue(String name,
                                       ProcessingContext jParams,
                                       String defaultValue,
                                       DateFormat dateFormat ) throws JahiaException {
        if ( dateFormat == null ){
            return getMetadataValue(name,jParams,defaultValue);
        }
        String value = defaultValue;
        JahiaField f = this.getMetadataAsJahiaField(name, jParams);
        if ( f != null ){
            value = (String)f.getObject();
            if ( value != null && !"".equals(value) ) {
                try {
                    value = dateFormat.format(new Date(Long.parseLong(value)));
                } catch ( Exception t ){
                }
            }
        }
        return value;
    }

    /**
     * For use with Date metadata.
     *
     * @param name
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public Date getMetadataAsDate(  String name,
                                    ProcessingContext jParams) throws JahiaException {
        Date value = null;
        JahiaField f = this.getMetadataAsJahiaField(name, jParams);
        if ( f != null && f.getObject() != null
                && StringUtils.isNumeric(f.getObject().toString()) ){
            try {
                value = new Date(Long.parseLong(f.getObject().toString()));
            } catch ( Exception t ){
            }
        }
        return value;
    }

    /**
     * Returns the metadata values
     *
     * @param name the metadata name
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public String[] getMetadataValues(  String name,
                                        ProcessingContext jParams,
                                        String[] defaultValues) throws JahiaException {

        JahiaField jahiaField = this.getMetadataAsJahiaField(name,jParams);
        if ( jahiaField == null ){
            return defaultValues;
        }
        try {
            return jahiaField.getValues();
        } catch ( Exception t){
            logger.debug("Exception getting values from metadata :"+ name,t);
        }
        return new String[]{};
    }

    /**
     * Change a metadata value
     *
     * @param name
     * @param value
     * @param jParams
     * @return true if success, false if the metadata does not exist or value is null
     * @throws JahiaException
     */
    public boolean setMetadataValue(String name, String value,  ProcessingContext jParams)
    throws JahiaException {

        if ( value == null ){
            return false;
        }
        JahiaField jahiaField = getMetadataAsJahiaField(name,jParams, true);
        if ( jahiaField == null ){
            return false;
        }
        jahiaField.setValue(value);
        jahiaField.save(jParams);
        return true;
    }

    /**
     * Return an Array of ContentField that have a core "metadata" strutural relationship
     * with the given objectKey. @see StructuralRelationship.METADATA_LINK
     *
     * @param objectKey ObjectKey
     * @return List of JahiaObject
     */
    public static List<ContentField> getMetadatas(ObjectKey objectKey) {
        return getMetadatas(objectKey,false);
    }

    public static List<ContentField> getMetadatas(ObjectKey objectKey, boolean withOldEntryStates) {
        return getMetadatas(objectKey, withOldEntryStates, false);
    }

    /**
     * Return an Array of ContentField that have a core "metadata" strutural relationship
     * with the given objectKey. @see StructuralRelationship.METADATA_LINK
     *
     * @param objectKey ObjectKey
     * @param withOldEntryStates if true, batch load field's old entrystates for better performance issue
     * @return List of JahiaObject
     */
    public static List<ContentField> getMetadatas(ObjectKey objectKey, boolean withOldEntryStates, boolean stagedOnly) {
        List<ContentField> objects = new ArrayList<ContentField>();
        List<Integer> ids;
        if (stagedOnly) {
            ids = jahiaFieldsDataManager.findStagedFieldsByMetadataOwner(objectKey);
        } else {
            ids = jahiaFieldsDataManager.findMetadatasByOwner(objectKey);
        }
        if ( ids.isEmpty() ){
            return objects;
        }

        Map<Integer, List<ContentObjectEntryState>> oldEntryStates = new HashMap<Integer, List<ContentObjectEntryState>>();
        if ( withOldEntryStates ){
            oldEntryStates = jahiaFieldsDataManager.findOldEntryStateForMetadatas(objectKey);
        }
        Integer id = ids.get(0);
        if ( ContentFieldTools.getInstance().getFieldFromCacheOnly(id.intValue()) == null ){
            try {
                if (stagedOnly) {
                    ContentFieldTools.getInstance().preloadStagedFieldsByMetadataOwner(objectKey);
                } else {
                    ContentFieldTools.getInstance().preloadActiveOrStagedFieldsByMetadataOwner(objectKey);
                }
            } catch ( Exception t ){
                logger.debug("Error preloading metadatas for object " + objectKey,t);
            }
        }
        for (Integer fieldId : ids) {
            try {
                ContentField contentField = ContentField.getField(fieldId.intValue());
                if ( contentField != null ){
                    if ( withOldEntryStates ){
                        List<ContentObjectEntryState> entryStates = oldEntryStates.get(new Integer(contentField.getID()));
                        if (entryStates == null ){
                            entryStates = new ArrayList<ContentObjectEntryState>();
                        }
                        contentField.setVersioningEntryStates(entryStates);
                    }
                    objects.add(contentField);
                }
            } catch ( Exception t ){
                logger.warn("metadata not found fieldId=" + fieldId.intValue());
            }
        }
        return objects;
    }

    /**
     * Returns the contentObject owner of the given metadata
     *
     * @param metadataObjectKey
     * @return the contentObject owner of the metadata
     */
    public static ContentObject getContentObjectFromMetadata(ObjectKey metadataObjectKey) {
        if (!ContentFieldKey.FIELD_TYPE.equals(metadataObjectKey.getType())) {
            return null;
        }
        return jahiaFieldsDataManager.findJahiaObjectByMetadata(
                new Integer(metadataObjectKey.getIdInType()));
    }

    public void addPickerObject(ProcessingContext jParams, ContentObject object, String type) {
        if (this.getClass() == object.getClass()) {
            try {
                ObjectLink.createLink(this.getObjectKey(), object.getObjectKey(), type, new HashMap<String, String>());
                object.resetPicked();
                ServicesRegistry.getInstance().getWorkflowService().flushCacheForPageCreatedOrDeleted((ContentObjectKey) object.getObjectKey());
            } catch (Exception e) {
                logger.debug(e);
            }
        }
    }

    public Set<ContentObject> getPickerObjects() throws JahiaException {
        Set<ContentObject> s = getPickerObjects(StructuralRelationship.ACTIVATION_PICKER_LINK);
        s.addAll(getPickerObjects(StructuralRelationship.CHANGE_PICKER_LINK));
        return s;
    }

    public Set<ContentObject> getPickerObjects(String type) throws JahiaException {
        Set<ContentObject> set = new HashSet<ContentObject>();
        List<ObjectLink> links = linkManager.findByTypeAndLeftObjectKey(type,this.getObjectKey());
        for (ObjectLink link : links) {        
            try {
                ContentObject picker = (ContentObject) ContentObject.getInstance(link.getRightObjectKey());
                if (picker != null && !picker.isMarkedForDelete() && !picker.isDeleted(new Long(System.currentTimeMillis()/1000).intValue())) {
                    set.add(picker);
                }
            } catch (Exception t) {
                logger.debug(t);
            }
        }
        return set;
    }

    void resetPicked() {
        pickedObject = null;
        pickedObjectType = null;
    }

    public ContentObject getPickedObject() throws JahiaException {
        ContentObject o = getPickedObject(StructuralRelationship.ACTIVATION_PICKER_LINK);
        if (o != null) {
            return o;
        }

        // unused for now
        // o = getPickedObject(StructuralRelationship.CHANGE_PICKER_LINK);

        if (o == null) {
            pickedObjectType = "none";
        }

        return o;
    }

    public ContentObject getPickedObject(String type) throws JahiaException {
        if (pickedObjectType == null) {
            List<ObjectLink> links = linkManager.findByTypeAndRightObjectKey(type, this.getObjectKey());
            for (ObjectLink link : links) {
                try {
                    pickedObject = (ContentObject) ContentObject.getInstance(link.getLeftObjectKey());
                    pickedObjectType = type;
                    return pickedObject;
                } catch (Exception t) {
                    logger.debug(t);
                }
            }
        } else {
            if (!type.equals(pickedObjectType)) {
                return null;
            }
        }
        return pickedObject;
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     * an instance of.
     * @returns a JahiaObject sub class instance that corresponds to the given
     * object key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static ContentObject getContentObjectInstance (ObjectKey objectKey)
        throws ClassNotFoundException {
        return (ContentObject) getInstanceAsObject(objectKey) ;
    }



    public int getStagingStatus (String languageCode)
            throws JahiaException {

        int status = VersioningDifferenceStatus.UNCHANGED;

        // resolve active entry state
        ContentObjectEntryState activeEntryState =
                ContentObjectEntryState.getEntryState (1,
                                                       languageCode);
        activeEntryState = this.getEntryState (activeEntryState, false, false);

        // resolve staging entry state
        ContentObjectEntryState stagingEntryState =
                ContentObjectEntryState.getEntryState (0,
                                                       languageCode);
        stagingEntryState = this.getEntryState (stagingEntryState, false, false);

        if (stagingEntryState != null) {
            if (stagingEntryState.getVersionID ()
                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                status = VersioningDifferenceStatus.TO_BE_REMOVED;
            } else if (activeEntryState == null) {
                status = VersioningDifferenceStatus.ADDED;
            } else {
                status = VersioningDifferenceStatus.TO_BE_UPDATED;
            }
        }
        return status;
    }

    /**
     * Return true if this object is available ( not EXPIRED_STATE or not in NOT_VALID_STATE )
     *
     * @return
     * @see TimeBasedPublishingState
     */
    public boolean isAvailable() {
        final JahiaObjectDelegate jahiaObject =
                jahiaObjectManager.getJahiaObjectDelegate(this.getObjectKey());
        return jahiaObject != null ? jahiaObject.isValid() : true;
    }

    public int getTimeBasedPublishingState() {
        JahiaObjectDelegate jahiaObject =
                jahiaObjectManager.getJahiaObjectDelegate(this.getObjectKey());
        return (jahiaObject == null ? TimeBasedPublishingState.IS_VALID_STATE : jahiaObject.getTimeBPState().intValue());
    }

    /**
     * Returns true if the contentObject has staging entry of given language.
     *
     * @return true if the field has staging entry of given language.
     */
    public boolean hasStagingEntryIgnoreLanguageCase (String languageCode) {
        try {
            Set<ContentObjectEntryState> entryStates = this.getEntryStates();
            for (ContentObjectEntryState thisEntryState : entryStates){
                if (thisEntryState.isStaging ()
                    && (ContentObject.SHARED_LANGUAGE.equalsIgnoreCase(thisEntryState.getLanguageCode ())
                        || thisEntryState.getLanguageCode ().equalsIgnoreCase(languageCode))) {
                    return true;
                }
            }
        } catch ( Exception t) {
            logger.debug("Exception occured checking staging entry ignore lang and no case sensitive",t);
        }
        return false;
    }

    /**
     * Returns the page path of the current content object.
     * The page path is of the form : /pid8/pid10/pid111 where pid8 is the site's home page and the pid111 is
     *  the current page or the current content object's parent page
     *
     * Default implementation for Content implementing PageReferenceableInterface.
     * Other ContentObject must override this method if they do not implement PageReferenceableInterface
     *
     * @param context
     * @return
     */
    public String getPagePathString(ProcessingContext context) {
        return getPagePathString(context,false);
    }

    /**
     * Returns the page path of the current content object.
     * The page path is of the form : /pid8/pid10/pid111 where pid8 is the site's home page and the pid111 is
     *  the current page or the current content object's parent page
     *
     * Default implementation for Content implementing PageReferenceableInterface.
     * Other ContentObject must override this method if they do not implement PageReferenceableInterface
     *
     * @param context
     * @param ignoreMetadata if true , the path is resolved dynamically,
     *          not the one stored in metadata
     * @return
     */
    public String getPagePathString(ProcessingContext context, boolean ignoreMetadata) {
        String pagePath = "";
        ContentPage contentPage = null;
        try {
            if ( this instanceof PageReferenceableInterface ){
                contentPage = ((PageReferenceableInterface)this).getPage();
            } else if ( this instanceof ContentPage ){
                contentPage = (ContentPage)this;
            }
            if ( contentPage != null ){
                //pagePath = contentPage.getPagePathString(context,ignoreMetadata);
                Iterator<ContentPage> pageEnum = contentPage.getContentPagePath(context
                        .getEntryLoadRequest(),context.getOperationMode(), JahiaAdminUser.getAdminUser(this.getSiteID()), 
                        JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED);
                ContentPage page = null;
                StringBuffer buff = new StringBuffer(512);
                while ( pageEnum.hasNext() ){
                    page = (ContentPage)pageEnum.next();
                    buff.append(ContentObject.PAGEPATH_PAGEID_PREFIX).append(page.getID());
                }
                pagePath = buff.toString();
            }
        } catch ( Exception t ){
            logger.debug("Exception occured getting pagePath for contentObject " + this.getObjectKey(),t);
        }
        return pagePath;
    }

    /*
    * Update all childs content's metadata PAGE_PATH bellow this node
    *
    * Should be implemented by subClasses
    *
    * @param startNode the starting node
    */
    public void updateContentPagePath(ProcessingContext context) throws JahiaException {
        // do nothing by default
    }


    public abstract String getProperty(String name) throws JahiaException;

    public abstract void setProperty(Object name, Object val) throws JahiaException;

    public abstract void removeProperty(String name) throws JahiaException;

    public void setUnversionedChanged() throws JahiaException {
        setProperty("unversionedChange", Long.toString(System.currentTimeMillis()/1000));    
    }

    public abstract int getPageID();

    public static int resolveVersion(ContentObject contentObject, int versionId)
    throws JahiaException {
        int v = versionId;
        if ( contentObject == null ){
            return v;
        }
        if ( versionId == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
            v = 0;
        } else if ( versionId == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            Set<ContentObjectEntryState> entryStates = contentObject.getActiveAndStagingEntryStates();
            for (ContentObjectEntryState entryState : entryStates){
                if ( entryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    v = entryState.getVersionID();
                    break;
                }
            }
        }
        return v;
    }

    public void createStaging(String lang) throws JahiaException {
        Set<ContentObjectEntryState> activeAndStagingEntryStates = getActiveAndStagingEntryStates();
        for (ContentObjectEntryState fromEntryState : activeAndStagingEntryStates) {
            if (fromEntryState.getLanguageCode().equals(lang) &&
                    fromEntryState.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                ContentObjectEntryState toEntryState = ContentObjectEntryState.getEntryState (0, lang);
                copyEntry(fromEntryState, toEntryState);
            }
        }
    }
    
    public boolean checkValidationAccess(JahiaUser user) {
        boolean result = true;
        try {
            List<? extends ContentObject> childs = getChilds(user, Jahia.getThreadParamBean().getEntryLoadRequest());
            WorkflowService service = ServicesRegistry.getInstance().getWorkflowService();
            for (Iterator<? extends ContentObject> it = childs.iterator(); it.hasNext() && result;) {            
                ContentObject contentObject = it.next();
                if (service.getWorkflowMode(contentObject) == WorkflowService.EXTERNAL) {
                    final String name = service.getInheritedExternalWorkflowName(contentObject);
                    if(name == null) {
                        result = contentObject.checkValidationAccess(user);
                    } else {
                        ExternalWorkflow workflow = service.getExternalWorkflow(name);
                        final String processId = service.getInheritedExternalWorkflowProcessId(contentObject);
                        result = workflow.isUserAuthorizedForWorkflow(processId, contentObject, user);
                    }
                } else {
                    result = contentObject.checkValidationAccess(user);
                }
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return result;
    }

    public boolean isAclSameAsParent() {
        try {
            if (getAclID() == -1 ) {
                return true;
            }
            ContentObject co = getParent(null);
//            return co != null && co.getAclID() == getAclID();
            return co != null && (co.getAclID() == getAclID() || co.getAclID() != getACL().getParentID());
        } catch (JahiaException e) {
            return false;
        }
    }

    public void updateAclForChildren(int aclid) {
        updateAclForChildren(aclid, true);
    }

    /**
     * Return jcr path "/siteKey/pageKey"
     *
     * @param context
     * @return
     * @throws JahiaException
     */
    public String getJCRPath(ProcessingContext context) throws JahiaException {
        try {
            return ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(getUUID(), context.getUser()).getPath();
        } catch (RepositoryException e) {
            throw new JahiaException("","",0,0,e);
        }
    }

    public Node getJCRNode(ProcessingContext context) throws JahiaException {
        try {
            return ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(getUUID(), context.getUser());
        } catch (RepositoryException e) {
            throw new JahiaException("","",0,0,e);
        }
    }

    public void updateAclForChildren(int aclid, boolean head) {
        int old = getAclID();
        if (old == -1) {
            return;
        }
        setAclID(aclid);
        try {
            Set<ContentObject> picks = getPickerObjects();
            for (ContentObject picker : picks) {
                if (head && picker.isAclSameAsParent()) {
                    JahiaBaseACL newacl = new JahiaBaseACL();
                    newacl.create(picker.getAclID(),aclid);
                    picker.updateAclForChildren(newacl.getID());
                }
                JahiaAcl pickerAcl = picker.getACL().getACL();
                if (pickerAcl.getPickedAclId().intValue()  == old) {
                    pickerAcl.setPickedAclId(new Integer(aclid));
                    ServicesRegistry.getInstance().getJahiaACLManagerService().updateCache(pickerAcl);
                }
            }            
            List<? extends ContentObject> l = getChilds(null,null);
            for (ContentObject contentObject : l) {
                if (contentObject instanceof ContentPageField) {
                    List<? extends ContentObject> c = contentObject.getChilds(null,null);
                    if (!c.isEmpty()) {
                        contentObject = c.iterator().next();
                    } else {
                        continue;
                    }
                }
                if (contentObject instanceof ContentField) {
                    continue;
                }

                if (contentObject.getAclID() == old) {
                    contentObject.updateAclForChildren(aclid, false);
                } else {
                    contentObject.getACL().setParentID(aclid);
                }
            }
//            if (!(this instanceof ContentField)) {
//                List<ContentField> mds = getMetadatas();
//                for (ContentField md : mds) {
//                    md.setAclID(aclid);
//                }
//            }
        } catch (JahiaException e) {
            logger.warn("Problem when updating ACLs", e);  
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {

         in.defaultReadObject();

         linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
         jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
         jahiaFieldsDataManager = (JahiaFieldsDataManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldsDataManager.class.getName());
     }

    public String getUUID() throws JahiaException {
        String uuid  = getProperty("uuid");
        if (uuid == null) {
            uuid = idGen.nextIdentifier().toString();
            setProperty("uuid", uuid);
        }
        return uuid;
    }

}
