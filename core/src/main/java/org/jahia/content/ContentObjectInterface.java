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
package org.jahia.content;

import java.util.*;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.RestoreVersionTestResults;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.JahiaSaveVersion;

/**
 * This class is the main content model class, from which all the other
 * content classes derive. It contains information about version,
 * locking, language, state (for workflows), references, etc...
 * @author Serge Huber
 */
public interface ContentObjectInterface {

    /**
     * Retrieves all the ContentObjectEntryState for this content object.
     * @return a SortedSet containing ContentObjectEntryState objects.
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states
     */
    public abstract SortedSet getEntryStates ()
        throws JahiaException ;

    /**
     * Retrieves all the active or staging ContentObjectEntryState for this content object.
     * Should be optimized by subclasses
     *
     * @return a SortedSet containing ContentObjectEntryState objects.
     * @throws JahiaException thrown in case there was a problem retrieving
     * the entry states
     */
    public abstract SortedSet getActiveAndStagingEntryStates()
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
     * @param operationMode the operation mode, this is a value of ProcessingContext.NORMAL,
     * ProcessingContext.EDIT or ProcessingContext.PREVIEW
     *
     * @return an List of ContentObject objects that are the children of
     * this object. If the case of a JahiaContainer, the children might be
     * ContentFields and ContentContainerLists !
     *
     * @throws JahiaException thrown in case there was a problem retrieving
     * the children of this object.
     */
    public abstract List getChilds (JahiaUser user,
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
    public abstract RestoreVersionTestResults isValidForRestore(JahiaUser user,
                                     String operationMode,
                                     ContentObjectEntryState entryState,
                                     boolean removeMoreRecentActive,
                                     StateModificationContext stateModificationContext)
        throws JahiaException;

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
    public abstract RestoreVersionTestResults restoreVersion (JahiaUser user,
        String operationMode,
        ContentObjectEntryState entryState,
        boolean removeMoreRecentActive,
        StateModificationContext stateModificationContext)
        throws JahiaException;

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
    public abstract int getID();

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
    public abstract ContentObjectEntryState getClosestVersionedEntryState(ContentObjectEntryState entryState)
        throws JahiaException;

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
    public abstract ContentObjectEntryState getClosestVersionedEntryState(ContentObjectEntryState entryState, boolean smallerVersionOnly)
        throws JahiaException;
    /**
     * Should be implemented by subobject for optimization
     *
     * Returns true if the content object has active entries.
     * @return
     */
    public abstract boolean hasActiveEntries() throws JahiaException;

    /**
     * Returns true if this Content Object has an archive entryState
     * before or at a given versionID
     *
     * @param VersionID
     * @return
     * @throws JahiaException
     */
    public abstract boolean hasArchiveEntryState(int versionID) throws JahiaException;

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
    public abstract List getClosestVersionedEntryStates(int versionID)
        throws JahiaException;

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
    public abstract boolean checkAdminAccess(JahiaUser user);

    /**
     * Check if the user has read access on the specified content object. Read
     * access means having the rights to display and read the content object.
     *
     * @param    user    Reference to the user.
     *
     * @return   Return true if the user has read access for the specified object,
     *           or false in any other case.
     */
    public abstract boolean checkReadAccess(JahiaUser user);

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
    public abstract boolean checkWriteAccess(JahiaUser user);

    /**
     * Check if the user has a specified access to the specified content object.
     * @param user Reference to the user.
     * @param permission One of READ_RIGHTS, WRITE_RIGHTS or ADMIN_RIGHTS permission
     * flag.
     * @return Return true if the user has the specified access to the specified
     * object, or false in any other case.
     */
    public abstract boolean checkAccess(JahiaUser user, int permission);

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
    public abstract boolean willBeCompletelyDeleted(String markDeletedLanguageCode,
                                           Set activationLanguageCodes)
        throws JahiaException;

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
    public abstract boolean willAllChildsBeCompletelyDeleted(JahiaUser user,
            String markDeletedLanguageCode, Set activationLanguageCodes)
        throws JahiaException;

    /**
     * Returns a set of languages that represents all the languages in active
     * or staging mode, including or not the languages marked for deletion.
     * @param includingMarkedForDeletion set this boolean to true if you want
     * the result to include the languages marked for deletion in staging mode.
     * @return a Set of String objects that contain the language codes.
     * @throws JahiaException thrown in case there was a problem loading the
     * entry states from the database.
     */
    public abstract Set getStagingLanguages(boolean includingMarkedForDeletion)
        throws JahiaException;

    /**
     * Returns a set of languages that represents all the languages in active
     * or staging mode, including or not the languages marked for deletion.
     * @param includingMarkedForDeletion set this boolean to true if you want
     * the result to include the languages marked for deletion in staging mode.
     * @return a Set of String objects that contain the language codes.
     * @throws JahiaException thrown in case there was a problem loading the
     * entry states from the database.
     */
    public abstract Set getStagingLanguages(boolean withActive,
                                   boolean includingMarkedForDeletion)
        throws JahiaException;

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
    public abstract ContentObjectEntryState getEntryState(ContentObjectEntryState entryState,
            boolean smallerVersionOnly, boolean activeOrStaging) throws JahiaException;

    /**
     * Returns the versionID at which the content was the last time deleted.
     *
     * @return -1 if the content is not actually deleted otherwise the versionID of the
     * last delete operation.
     * @throws JahiaException
     */
    public abstract int getDeleteVersionID() throws JahiaException;
    /**
     * Return true if the content object is deleted in all language at a given
     * date.
     * @param versionID
     * @return
     */
    public abstract boolean isDeleted(int versionID) throws JahiaException;

    /**
     * Return true if the current content object is marked for delete
     * @return
     */
    public abstract boolean isMarkedForDelete() throws JahiaException;

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

    /**
     * Return the objects's ACL object. Based on getAclID() implementation.
     *
     * @return Return the object's ACL.
     */
    public abstract JahiaBaseACL getACL ();

    /**
     * Should return a human readable title for this object
     * Only implemented for pages right now
     *
     * @param lastUpdatedTitles
     * @return
     */
    public abstract Map getTitles (boolean lastUpdatedTitles);

    /**
     * Return the site ID in which the object is located.
     *
     * @return Return the page site ID.
     */
    public abstract int getSiteID();

    public abstract ActivationTestResults isValidForActivation (
            Set languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException;

    public abstract void setWorkflowState (Set languageCodes,
                                  int newWorkflowState,
                                  ProcessingContext jParams,
                                  StateModificationContext stateModifContext)
            throws JahiaException;

    public abstract ActivationTestResults activate (
            Set languageCodes,
            boolean versioningActive, JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException;

    /**
     * Return an Array of JahiaObject that have a core "metadata" strutural relationship
     * with this one. @see StructuralRelationship.METADATA_LINK
     *
     * @return List of JahiaObject
     */
    public abstract List getMetadatas() throws JahiaException;

    /**
     * Return the given JahiaObject that has a core "metadata" strutural relationship
     * with this one and having the given name.
     * @see StructuralRelationship.METADATA_LINK
     *
     * @return a JahiaObject
     */
    public abstract JahiaObject getMetadata(String name) throws JahiaException;

    /**
     * Create a "metadata" strutural relationship with this one
     *
     * @param jahiaObject JahiaObject
     * @throws JahiaException
     */
    public abstract void addMetadata(JahiaObject metadata, String userKey)
    throws JahiaException;

}
