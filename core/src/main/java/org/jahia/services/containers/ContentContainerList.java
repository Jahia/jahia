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

package org.jahia.services.containers;

import org.apache.jackrabbit.spi.Name;
import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.manager.JahiaContainerListManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ContentContainerList extends ContentObject
        implements PageReferenceableInterface, TimeBasedPublishingJahiaObject {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContentContainerList.class);

    private List<ContentObjectEntryState> activeAndStagedEntryStates;
    private List<ContentObjectEntryState> versionedEntryStates = null;
    private int parentContainerID;
    private int pageID;
    private int aclID;
    private int containerListDefinitionID;
    private Map<String, String> properties;
    private transient JahiaContainerListManager containerListManager;

    static {
        JahiaObject.registerType (ContentContainerListKey.CONTAINERLIST_TYPE,
                ContentContainerList.class.getName ());
    }

    public static ContentObject getChildInstance (String IDInType) {
        try {
            return getContainerList (Integer.parseInt (IDInType));
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container list instance for id : " + IDInType, je);
        }
        return null;
    }

    public static ContentObject getChildInstance (String IDInType, boolean forceLoadFromDB) {
        try {
            return getContainerList (Integer.parseInt (IDInType));
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container list instance for id : " + IDInType, je);
        }
        return null;
    }    
    
    public ContentContainerList (int ID, int parentContainerID, int pageID, int ctnDefID,
                                    int aclID, List<ContentObjectEntryState> activeAndStagedEntries) {
        super (new ContentContainerListKey (ID));
        this.parentContainerID = parentContainerID;
        this.pageID = pageID;
        this.aclID = aclID;
        this.containerListDefinitionID = ctnDefID;
        this.activeAndStagedEntryStates = activeAndStagedEntries;
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
    }

    /**
     * No arg constructor to support serialization
     */
    public ContentContainerList() {
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
    }

    /**
     * Return the pageID
     *
     * @return
     */
    public int getPageID () {
        return this.pageID;
    }


    public int getSiteID() {
        int siteId = -1;
        try {
            try {
                ContentObject contentObject = getParent(null, null, null);
                if (contentObject != null) {
                    siteId = contentObject.getSiteID();
                }
            } catch (JahiaPageNotFoundException pnfe) {
                // parent page can already be deleted --> try via definition
            }
            if (siteId == -1) {
                // try via definition
                JahiaContainerDefinition ctnDef = JahiaContainerDefinitionsRegistry
                        .getInstance().getDefinition(
                                getContainerListDefinitionID());
                if (ctnDef != null) {
                    siteId = ctnDef.getJahiaID();
                }

            }
        } catch (JahiaException e) {
            logger.error(
                    "Unable to obtain site ID for the content container list object with ID "
                            + getID(), e);
        }
        return siteId;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the ContentPage ancestor.
     *
     * @return Return the ContentPage ancestor.
     */
    public ContentPage getPage () throws JahiaException {
        return ContentPage.getPage (this.getPageID ());
    }

    /**
     * Returns the identifier of the Content Definition for Content object.
     *
     * @return the identifier of the Content Definition for Content object
     */
    public int getDefinitionID () {
        return this.containerListDefinitionID;
    }

    /**
     * Returns the identifier of the Content Definition for Content object.
     *
     * @param loadRequest
     *
     * @return the identifier of the Content Definition for Content object
     */
    public int getDefinitionID (EntryLoadRequest loadRequest) {
        return this.containerListDefinitionID;
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
        return new ContainerDefinitionKey(getDefinitionID(loadRequest));
    }

    public SortedSet<ContentObjectEntryState> getEntryStates ()
            throws JahiaException {
        SortedSet<ContentObjectEntryState> resultSet = new TreeSet<ContentObjectEntryState> ();
        if (versionedEntryStates == null) {
            versionedEntryStates = containerListManager.getVersionedEntryStates (getID (), false);
        }
        resultSet.addAll (activeAndStagedEntryStates);
        resultSet.addAll (versionedEntryStates);
        return resultSet;
    }

    /**
     * Get an Iterator of active and staged entry state.
     */
    public SortedSet<ContentObjectEntryState> getActiveAndStagingEntryStates () {
        SortedSet<ContentObjectEntryState> entries = new TreeSet<ContentObjectEntryState> ();
        entries.addAll (activeAndStagedEntryStates);
        return entries;
    }

    public List<ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest,
            int loadFlag) throws JahiaException {
        return getChilds(user, loadRequest);
    }
    
    public List<ContentObject> getChilds(JahiaUser user,
                               EntryLoadRequest loadRequest
    )
            throws JahiaException {
        /**
         * @todo FIXME Filters are currently not supported in this method.
         */
        List<ContentObject> resultList = new ArrayList<ContentObject> ();
        Set<Integer> there = new HashSet<Integer>();
        List<Integer> containerIDs = ServicesRegistry.getInstance ().getJahiaContainersService ()
                .getctnidsInList (getID (), loadRequest);
        for (Integer curContainerID : containerIDs) {
            if (!there.contains(curContainerID)) {
                there.add(curContainerID);
                ContentContainer curContainer = ContentContainer.getContainer (
                        curContainerID.intValue ());
                if (curContainer != null) {
                    resultList.add (curContainer);
                }
            }
        }
        return resultList;
    }

    public ContentObject getParent (EntryLoadRequest loadRequest)
            throws JahiaException {
        ContentObject parent;
        if (parentContainerID > 0) {
            parent = ContentContainer.getContainer(parentContainerID);
        } else {
            parent = ContentPage.getPage(pageID);
        }
        return parent;
    }

    public ContentObject getParent (JahiaUser user,
                                    EntryLoadRequest loadRequest,
                                    String operationMode)
            throws JahiaException {
        return getParent(loadRequest);
    }

    public static ContentContainerList getContainerList (int containerListID)
            throws JahiaException {
        long start = System.currentTimeMillis();
        ContentContainerList containerList = ((JahiaContainerListManager) (SpringContextSingleton.getInstance().getContext().getBean(JahiaContainerListManager.class.getName()))).getContainerList (containerListID);
        if(logger.isDebugEnabled()) {
            Throwable throwable = (new Throwable());
            throwable.fillInStackTrace();
            String calledBy = throwable.getStackTrace()[1].getClassName()+"."+throwable.getStackTrace()[1].getMethodName()+ "at line "+throwable.getStackTrace()[1].getLineNumber();
            String calledBy2 = throwable.getStackTrace()[2].getClassName()+"."+throwable.getStackTrace()[2].getMethodName()+ "at line "+throwable.getStackTrace()[2].getLineNumber();
            logger.debug("Retrieving containerListId "+containerListID+ " took "+(System.currentTimeMillis()-start)+" ms called by "+calledBy+ "\n called by "+calledBy2);
        }
        return containerList;
    }

    public JahiaContainerList getJahiaContainerList (ProcessingContext jParams,
                                                     EntryLoadRequest loadRequest)
            throws JahiaException {
        return ServicesRegistry.getInstance ().getJahiaContainersService ().loadContainerList (
                getID (), LoadFlags.ALL, jParams, loadRequest, new HashMap<Integer, List<Integer>>(), new HashMap<Integer, List<Integer>>(), new HashMap<Integer, List<Integer>>());
    }

    public int getAclID () {
        return aclID;
    }

    public void setAclID(int aclID) {
        this.aclID = aclID;
        containerListManager.updateContainerListAclId(getID(), aclID);
    }

    public RestoreVersionTestResults isValidForRestore (JahiaUser user,
                                                        String operationMode,
                                                        ContentObjectEntryState entryState,
                                                        boolean removeMoreRecentActive,
                                                        StateModificationContext stateModificationContext)
            throws JahiaException {
        // first let's check if we have entries that correspond for this
        // container list
        RestoreVersionTestResults opResult = new RestoreVersionTestResults ();
        opResult.merge (
                super.isValidForRestore (user, operationMode, entryState,
                        removeMoreRecentActive, stateModificationContext));
        if (opResult.getStatus () == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
            return opResult;
        }

        // now let's check for the children of this container list. If only
        // one of them fails, we fail the whole container.
        List<Locale> locales = new ArrayList<Locale> ();
        locales.add (EntryLoadRequest.SHARED_LANG_LOCALE);
        locales.add (LanguageCodeConverters.languageCodeToLocale (entryState.getLanguageCode ()));
        EntryLoadRequest loadRequest = new EntryLoadRequest (entryState.getWorkflowState (),
                entryState.getVersionID (), locales);
        List<ContentObject> children = getChilds (user, loadRequest);
        for (ContentObject curChild : children) {
            RestoreVersionTestResults childResult = curChild.isValidForRestore (user,
                    operationMode, entryState, removeMoreRecentActive,
                    stateModificationContext);
            // if a child fails this is ok, we simply indicate this by changing
            // the status to partial, and keeping the messages
            if (childResult.getStatus () == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
                childResult.setStatus (RestoreVersionTestResults.PARTIAL_OPERATION_STATUS);
                childResult.moveErrorsToWarnings ();
            }
            opResult.merge (childResult);
        }
        return opResult;
    }

    private boolean hasEntry (EntryStateable entryState)
            throws JahiaException {
        getEntryStates (); // this insures we have the loaded the versioned
        // entry states
        ContentObjectEntryState entryStateObject = new ContentObjectEntryState (entryState);
        if (entryStateObject.getWorkflowState () >= ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            int objectPos = activeAndStagedEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                return true;
            }
        } else {
            int objectPos = versionedEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                return true;
            }

        }
        return false;
    }

    private void removeEntryFromCaches (EntryStateable entryState)
            throws JahiaException {
        getEntryStates (); // this insures we have the loaded the versioned
        // entry states
        ContentObjectEntryState entryStateObject = new ContentObjectEntryState (entryState);
        if (entryStateObject.getWorkflowState () >= ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            int objectPos = activeAndStagedEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                activeAndStagedEntryStates.remove (objectPos);
            }
        } else {
            int objectPos = versionedEntryStates.indexOf (entryStateObject);
            if (objectPos != -1) {
                versionedEntryStates.remove (objectPos);
            }

        }
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

        containerListManager.copyEntry (getID (), fromE, toE);
        if (toE.getWorkflowState () >= ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            activeAndStagedEntryStates.add (toE);
        } else {
            versionedEntryStates.add (toE);
        }
    }


    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a object is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext content fields for instance, this method should delete
     * the text file corresponding to the field entry
     *
     * @param deleteEntryState the entry state to delete
     */
    protected void deleteEntry (EntryStateable deleteEntryState)
            throws JahiaException {
        removeEntryFromCaches (deleteEntryState);
        containerListManager.deleteEntry (getID (), deleteEntryState);
    }

    /**
     * Mark a content object's language for deletion. Does nothing if the
     * language doesn't exist, but if it exists only in staging, the staging
     * entry is removed.
     *
     * @param user                     the user performing the operation, in order to perform
     *                                 rights checks
     * @param languageCode             the language to mark for deletion
     * @param stateModificationContext contains the start object of the
     *                                 operation, as well as settings such as recursive descending in sub pages,
     *                                 and content object stack trace.
     *
     * @throws JahiaException raised if there was a problem while marking the
     *                        content object for deletion (mostly related to communication with the
     *                        database)
     */
    public void markLanguageForDeletion (JahiaUser user,
                                         String languageCode,
                                         StateModificationContext
            stateModificationContext)
            throws JahiaException {
        ServicesRegistry.getInstance ().getJahiaContainersService ()
                .markContainerListLanguageForDeletion (getID (), user, languageCode,
                        stateModificationContext);
    }

    public RestoreVersionTestResults restoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext stateModificationContext)
            throws JahiaException {

        RestoreVersionTestResults opResult = new RestoreVersionTestResults ();

        /**
         * This check has no meaning because some field could not exists in the current restore language
         *
         opResult.merge(isValidForRestore(user, operationMode, entryState,
         removeMoreRecentActive,
         stateModificationContext));
         if (opResult.getStatus() ==
         RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
         return opResult;
         }
         */

       // retrieve the exact archive entry state
//       ContentObjectEntryState closestVersion = getClosestVersionedEntryState(entryState);
//       boolean markedForDelete = false;

       List<Locale> locales = new ArrayList<Locale>();
       locales.add(LanguageCodeConverters.languageCodeToLocale(entryState.getLanguageCode()));
       EntryLoadRequest loadRequest = null;

       // 1. First restore archive
       // load archive to restore
       loadRequest = new EntryLoadRequest(entryState.
           getWorkflowState(), entryState.getVersionID(), locales);
       List<ContentObject> children = getChilds(user, loadRequest);

       // For performance issue, we don't want to restore twice objects.
       List<String> processedChilds = new ArrayList<String>();
       for (ContentObject curChild : children) {
           opResult.merge(curChild.restoreVersion(user,
                   operationMode, entryState, removeMoreRecentActive, stateModificationContext));
           processedChilds.add(curChild.getObjectKey().toString());
       }

       //2.Second, remove more recent data
       if ( removeMoreRecentActive ){
           // load staging or active to perform a restore which will mark them for delete
           loadRequest =
                   new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                   0, locales);
           children = getChilds(user, loadRequest);
           for (ContentObject curChild : children) {
               if ( !processedChilds.contains(curChild.getObjectKey().toString()) ){
                   opResult.merge(curChild.restoreVersion(user,
                       operationMode, entryState, removeMoreRecentActive,
                       stateModificationContext));
               }
           }
       }

       opResult.merge(super.restoreVersion(user, operationMode, entryState,
                                    removeMoreRecentActive,
                                    stateModificationContext));

//       JahiaContainerUtilsDB.getInstance().invalidateSubCtnListIDsByCtnCache(this.getParentContainerID());
//       JahiaContainerUtilsDB.getInstance().invalidateCtnIdsByCtnListCache(this.getID());

        try {
            ProcessingContext jParams = Jahia.getThreadParamBean();
            ContentObjectRestoreVersionEvent jahiaEvent =
                new ContentObjectRestoreVersionEvent(this,this.getSiteID(),jParams);
            ServicesRegistry.getInstance().getJahiaEventService()
                    .fireContentObjectRestoreVersion(jahiaEvent);
        } catch ( Exception t){
            logger.debug("Exception firing ContentObjectRestoreVersionEvent",t);
        }

        // handled by previous event
        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainerList(this.getID(), user);

       ServicesRegistry.getInstance ().getJahiaContainersService ()
           .invalidateContainerListFromCache(this.getID());

        return opResult;
    }

    public int getParentContainerID () {
        return parentContainerID;
    }

    public int getContainerListDefinitionID () {
        return containerListDefinitionID;
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
        for (ContentObjectEntryState entryState : activeAndStagedEntryStates) {
            // ok huston, we have a staged version here, let's advise...
            if (entryState.isStaging ()) {
                stagedEntryStates.add (entryState);
            }
        }

        // now let's use that List to destroy all staged versions
        for (ContentObjectEntryState curEntryState : stagedEntryStates) {
            // first we call the field to destroy it's related data
            this.deleteEntry (curEntryState);
        }

//        JahiaContainerUtilsDB.getInstance().invalidateSubCtnListIDsByCtnCache(this.getParentContainerID());
//        JahiaContainerUtilsDB.getInstance().invalidateCtnIdsByCtnListCache(this.getID());

        ContentUndoStagingEvent jahiaEvent = new ContentUndoStagingEvent(this, this.getSiteID(), jParams);
        ServicesRegistry.getInstance().getJahiaEventService()
                .fireContentObjectUndoStaging(jahiaEvent);
        // handled by previous event
        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainerList(this.getID(), jParams.getUser());

        ServicesRegistry.getInstance ().getJahiaContainersService ()
            .invalidateContainerListFromCache(this.getID());

    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

    public ActivationTestResults isValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = super.isValidForActivation(languageCodes, jParams, stateModifContext);
        activationTestResults.merge(ServicesRegistry.getInstance().getJahiaContainersService ().isContainerListValidForActivation(
                languageCodes, getID (), jParams.getUser(), null, stateModifContext));
        return activationTestResults;
    }


    public synchronized ActivationTestResults activate(
            Set<String> languageCodes,
            boolean versioningActive, JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        ActivationTestResults result = ServicesRegistry.getInstance().getJahiaContainersService ().activateStagedContainerList (
                languageCodes, getID (), user, saveVersion, jParams, stateModifContext);

        fireContentActivationEvent(languageCodes,
                                   versioningActive,
                                   saveVersion,
                                   jParams,
                                   stateModifContext,
                                   result);
        syncClusterOnValidation();
        return result;
    }

    /**
     * This method is called to notify that time based publishing state has changed
     */
    public void notifyStateChanged(){
        // container list doesn't expire
    }

    /**
     * Returns true if the containerlist has staging entry of given language.
     *
     * @return true if the field has staging entry of given language.
     */
    public boolean hasStagingEntryIgnoreLanguageCase (String languageCode) {
        for (int i = 0; i < activeAndStagedEntryStates.size (); i++) {
            ContentObjectEntryState thisEntryState = (ContentObjectEntryState) activeAndStagedEntryStates.get (
                    i);
            if (thisEntryState.isStaging ()) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayName(ProcessingContext jParams) {
        JahiaContainerDefinition def = (JahiaContainerDefinition)
                JahiaContainerDefinition.getChildInstance(
                String.valueOf(this.getDefinitionID(jParams.getEntryLoadRequest())));
        return def.getTitle(
                jParams.getEntryLoadRequest().getFirstLocale(true));
    }

    /*
    * Update all childs content's metadata PAGE_PATH bellow this node
    *
    * Default implementation for Content implementing PageReferenceableInterface.
    * Other ContentObject must override this method if they do not implement PageReferenceableInterface
    *
    * @param startNode the starting node
    */
    public void updateContentPagePath(ProcessingContext context) throws JahiaException {
        // update content childs
        List<ContentObject> contentChilds = this.getChilds(JahiaAdminUser.getAdminUser(this.getSiteID()),
                EntryLoadRequest.STAGED);
        for ( ContentObject child : contentChilds){
            child.updateContentPagePath(context);
        }
        /*
        ContentPage parentPage = null;
        try {
            parentPage = this.getPage();
        } catch ( Exception t ){
            logger.debug("Exception occured updating pagePath for contentObject " + this.getObjectKey(),t);
            return;
        }

        String newPagePath = "";
        if ( parentPage != null ){
            newPagePath = parentPage.getPagePathString(context);
        }
        setMetadataValue(CoreMetadataConstant.PAGE_PATH, newPagePath, context);

        // update content childs
        List contentChilds = this.getChilds(JahiaAdminUser.getAdminUser(this.getSiteID()),
                EntryLoadRequest.STAGED);
        int size = contentChilds.size();
        ContentObject child = null;
        for ( int i=0; i<size; i++ ){
            child = (ContentObject)contentChilds.get(i);
            child.updateContentPagePath(context);
        }*/
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            try {
                properties = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListProperties(getID());
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public String getProperty(String name) throws JahiaException {
        return (String) getProperties().get(name);
    }

    public void setProperty(String name, String val) throws JahiaException {
        final Map<String, String> p = getProperties();
        if (p.get(name) !=null && p.get(name).equals(val)) {
            return;
        }
        p.put(name,val);
        ServicesRegistry.getInstance().getJahiaContainersService().setContainerListProperties(getID(), getSiteID(), getProperties());
    }

    public void removeProperty(String name) throws JahiaException {
        getProperties().remove(name);
        containerListManager.removeContainerListProperty(getID(), name);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public Map<String, Integer> getLanguagesStates() {
        int size = activeAndStagedEntryStates.size ();
        Map<String, Integer> languageStates = new HashMap<String, Integer>();

        // first let's get all the active languages in the map.
        for (int i = 0; i < size; i++) {
            ContentObjectEntryState entryState = (ContentObjectEntryState) activeAndStagedEntryStates.get (
                    i);
            // ok huston, we have an active entry
            if (!entryState.isStaging ()) {
                languageStates.put (entryState.getLanguageCode (),
                        new Integer (entryState.getWorkflowState ()));
            }
            if (entryState.isStaging ()) {
                languageStates.put (entryState.getLanguageCode (),
                        new Integer (entryState.getWorkflowState ()));
            }
        }

        return languageStates;
    }

    public void setWorkflowState(Set<String> languageCodes, int newWorkflowState, ProcessingContext jParams, StateModificationContext stateModifContext) throws JahiaException {
        List<ContentObjectEntryState> newActiveAndStagingEntryStates = new ArrayList<ContentObjectEntryState> ();
        for (ContentObjectEntryState entryState : activeAndStagedEntryStates) {
            // ok huston, we have a staged entry here, let's advise...
            if (entryState.isStaging () && entryState.getWorkflowState() != newWorkflowState &&
                    (ContentObject.SHARED_LANGUAGE.equals (entryState.getLanguageCode ())
                    || languageCodes.contains (entryState.getLanguageCode ())) &&
                    (newWorkflowState >= ContentObjectEntryState.WORKFLOW_STATE_START_STAGING)) {
                // now we must updates both the internal data and the
                // database to reflect these changes.
                ContentObjectEntryState newEntryState =
                        new ContentObjectEntryState (newWorkflowState,
                                entryState.getVersionID (),
                                entryState.getLanguageCode ());

                containerListManager.changeEntryState(this, entryState, newEntryState);

                newActiveAndStagingEntryStates.add (newEntryState);

            } else {
                newActiveAndStagingEntryStates.add (entryState);
            }

        }
        activeAndStagedEntryStates = newActiveAndStagingEntryStates;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns true if the acl set at container list level allow a field of a given
     * field def name to be editable or not
     *
     * @return boolean true if the acl return true , false else
     * @author Khue Nguyen
     */
    public final boolean isFieldEditable(String fieldDefName, JahiaUser user){
        if ( fieldDefName == null || user == null )
            return false;

        try {
            String val = this.getProperty("view_field_acl_"+fieldDefName);
            if ( val != null ){
                try {
                    int aclID = Integer.parseInt(val);
                    JahiaBaseACL theACL = null;
                    try {
                        theACL = new JahiaBaseACL (aclID);
                        return theACL.getPermission(user,JahiaBaseACL.WRITE_RIGHTS);
                    }
                    catch (ACLNotFoundException ex) {
                    }
                    catch (JahiaException ex) {
                    }
                } catch ( Exception t ){
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot read propety",e);
        }
        return false;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns true if the acl set at container list level allow a field of a given
     * field def name to be visible ( READ permission )
     *
     * @return boolean true if the acl return true , false else
     * @author Khue Nguyen
     */
    public final boolean isFieldReadable(String fieldDefName, JahiaUser user){
        if ( fieldDefName == null || user == null )
            return false;

        try {
            String val = this.getProperty("view_field_acl_"+fieldDefName);
            if ( val != null ){
                try {
                    int aclID = Integer.parseInt(val);
                    JahiaBaseACL theACL = new JahiaBaseACL (aclID);
                    return theACL.getPermission(user,JahiaBaseACL.READ_RIGHTS);
                } catch ( Exception t ){
                    // One acl defined but there's an error assume false for security reason
                    logger.error("error dureing guessing of readable field ",t);
                    return false;
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot read property",e);
        }
        // NO Acl defined so assume true
        return true;
    }

    public void updateAclForChildren(int aclid, boolean head) {
        int old = getAclID();
        super.updateAclForChildren(aclid, false);
        try {
            Map<String, String> properties = getProperties();
            for (Map.Entry<String, String> property : new HashSet<Map.Entry<String, String>>(properties.entrySet())) {
                if (property.getKey().startsWith("view_field_acl_")) {
                    int acl = Integer.parseInt(property.getValue());
                    if (acl == old) {
                        setProperty(property.getKey(), Integer.toString(aclid));
                    } else {
                        JahiaBaseACL.getACL(acl).setParentID(aclid);
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot set field acl property",e);
        }
    }

    /**
     * Return jcr path "/siteKey/pageKey"
     *
     * @param context
     * @return
     * @throws JahiaException
     */

//    public String getJCRPath(ProcessingContext context) throws JahiaException {
//        String path = this.getObjectKey().toString();
//        ContentPage contentPage = ContentPage.getPage(this.getPageID());
//        String parentPath = contentPage.getJCRPath(context);
//        return parentPath + "/" + ServicesRegistry.getInstance().getQueryService()
//                .getNameFactory().create(Name.NS_DEFAULT_URI,path);
//    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         ApplicationContext context = SpringContextSingleton.getInstance().getContext();
         containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
     }
}

