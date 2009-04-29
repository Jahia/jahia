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
package org.jahia.services.containers;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.ContainersChangeEventListener;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaContainerListManager;
import org.jahia.hibernate.manager.JahiaContainerManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.version.*;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
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

public class ContentContainer extends ContentObject
    implements PageReferenceableInterface, TimeBasedPublishingJahiaObject, Cloneable, Serializable {

    private static final long serialVersionUID = -3972455571793209458L;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentContainer.class);

    private List<ContentObjectEntryState> activeAndStagedEntryStates;
    private List<ContentObjectEntryState> versionedEntryStates = null;
    private int parentContainerListID;
    private int aclID;
    private Map<Object, Object> properties;

    private int jahiaID;
    private int pageID;
    private int ctnDefID;
    private transient JahiaContainerListManager containerListManager;
    private ContentObject parent = null;
    private transient JahiaContainerManager containerManager;

    static {
        JahiaObject.registerType (ContentContainerKey.CONTAINER_TYPE,
                                  ContentContainer.class.getName ());
    }

    /**
     * Return the pageID
     *
     * @return  the page identification number
     */
    public int getPageID () {
        return this.pageID;
    }


    public int getSiteID() {
        return this.jahiaID;
    }

    /**
     * Returns the ContentPage ancestor.
     *
     * @return Return the ContentPage ancestor.
     */
    public ContentPage getPage () throws JahiaException {
        return ContentPage.getPage (this.getPageID ());
    }

    /**
     * Returns the Container definition id for this Content object.
     *
     * @param loadRequest not used, can be null
     *
     * @return the Container definition id for this Content object
     */
    public int getDefinitionID (EntryLoadRequest loadRequest) {
        return this.ctnDefID;
    }

    /**
     * Returns the Container definition id for this Content object.
     *
     * @return the Container definition id for this Content object
     */
    public int getDefinitionID() {
        return this.ctnDefID;
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
        return new ContainerDefinitionKey(getDefinitionID (loadRequest));
    }

    /**
     * Returns the Definition Key of the Content Definition for this Content object.
     * This is a ContainerDefinition
     *
     * @return the Definition Key of the Content Definition for this Content object. This is a ContainerDefinition
     */
    public ContainerDefinitionKey getDefinitionKey() {
        return new ContainerDefinitionKey(getDefinitionID());
    }

    public static ContentObject getChildInstance (String IDInType) {
        try {
            return getContainer (Integer.parseInt (IDInType));
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container instance for id : " + IDInType, je);
        }
        return null;
    }

    public static ContentObject getChildInstance (String IDInType, boolean forceLoadFromDB) {
        try {
            return getContainer (Integer.parseInt (IDInType), forceLoadFromDB);
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container instance for id : " + IDInType, je);
        }
        return null;
    }    
    
    public ContentContainer (int ID, int jahiaID, int pageID, int ctnDefID, int listID,
                                int aclID, List<ContentObjectEntryState> activeAndStagedEntryStates) {
        super (new ContentContainerKey (ID));
        this.jahiaID = jahiaID;
        this.pageID = pageID;
        this.ctnDefID = ctnDefID;
        this.parentContainerListID = listID;
        this.activeAndStagedEntryStates = activeAndStagedEntryStates;
        this.aclID = aclID;
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
        containerManager = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
    }

    public ContentContainer(ObjectKey objectKey) {
        super(objectKey);
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
        containerManager = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
    }

    /**
     * No arg constructor to support serialization
     */
    public ContentContainer() {
        ApplicationContext context = SpringContextSingleton.getInstance().getContext();
        containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
        containerManager = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
    }

    public SortedSet<ContentObjectEntryState> getEntryStates ()
            throws JahiaException {
        SortedSet<ContentObjectEntryState> resultSet = new TreeSet<ContentObjectEntryState> ();
        if (versionedEntryStates == null) {
            versionedEntryStates =
                    ContentContainerTools.getInstance ().getVersionedEntryStates (getID (), false);
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
    
    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest)
            throws JahiaException {

        return getChilds(user, loadRequest,
                JahiaContainerStructure.ALL_TYPES);
    }
    
    public List<? extends ContentObject> getChilds(JahiaUser user,
                               EntryLoadRequest loadRequest, int loadFlag)    
            throws JahiaException {
        List<ContentObject> resultList = new FastArrayList ();
        
        if ((loadFlag & JahiaContainerStructure.JAHIA_FIELD) != 0) {
            List<Integer> fieldIDs = ContentContainerTools.getInstance()
                    .getFieldIDsByContainer(getID(), loadRequest);

            Iterator<Integer> fieldIDEnum = fieldIDs.iterator();

            if (fieldIDEnum.hasNext()) {
                int fieldID = fieldIDEnum.next().intValue();
                ContentField currentField = ContentFieldTools.getInstance()
                        .getFieldFromCacheOnly(fieldID);
                if (currentField == null) {
                    ContentField
                            .preloadActiveOrStagedFieldsByContainerID(getID());
                    currentField = ContentField.getField(fieldID);
                }
                resultList.add(currentField);

                while (fieldIDEnum.hasNext()) {
                    fieldID = fieldIDEnum.next().intValue();

                    currentField = ContentField.getField(fieldID);
                    if (currentField != null) {
                        resultList.add(currentField);
                    }
                }
            }
        }
        if ((loadFlag & JahiaContainerStructure.JAHIA_CONTAINER) != 0) {

            // now let's check that the case of subcontainer lists. If they cannot
            // be marked for deletion, neither can this container.
            for (Integer curContainerListID : containerListManager
                    .getSubContainerListIDs(getID(), loadRequest)) {
                ContentContainerList curContainerList = ContentContainerList
                        .getContainerList(curContainerListID.intValue());
                if (curContainerList != null) {
                    resultList.add(curContainerList);
                }
            }
        }
        ((FastArrayList)resultList).setFast(true);
        return resultList;
    }

    public ContentObject getParent (EntryLoadRequest loadRequest)
            throws JahiaException {
        if(parent ==null)
            parent = ContentContainerList.getContainerList (parentContainerListID);
        return parent;
    }

    public ContentObject getParent (JahiaUser user,
                                    EntryLoadRequest loadRequest,
                                    String operationMode)
            throws JahiaException {
        return getParent(loadRequest);
    }

    public static ContentContainer getContainer (int containerID)
            throws JahiaException {
        return ContentContainerTools.getInstance()
            .getContainer(containerID);
    }

    /**
     * If loadFromDB is true, force reload from db
     * @param containerID
     * @param loadFromDB
     * @return
     * @throws JahiaException
     */
    public static ContentContainer getContainer (int containerID, boolean loadFromDB)
            throws JahiaException {
        long start = System.currentTimeMillis();
        ContentContainer container = ContentContainerTools.getInstance().getContainer(containerID,loadFromDB);
        if(logger.isDebugEnabled()) {
            Throwable throwable = (new Throwable());
            throwable.fillInStackTrace();
            String calledBy = throwable.getStackTrace()[1].getClassName()+"."+throwable.getStackTrace()[1].getMethodName()+ "at line "+throwable.getStackTrace()[1].getLineNumber();
            String calledBy2 = throwable.getStackTrace()[2].getClassName()+"."+throwable.getStackTrace()[2].getMethodName()+ "at line "+throwable.getStackTrace()[2].getLineNumber();
            logger.debug("Retrieving containerListId "+containerID+ " took "+(System.currentTimeMillis()-start)+" ms called by "+calledBy+ "\n called by "+calledBy2);
        }
        return container;
    }

    public RestoreVersionTestResults isValidForRestore (JahiaUser user,
                                                        String operationMode,
                                                        ContentObjectEntryState entryState,
                                                        boolean removeMoreRecentActive,
                                                        StateModificationContext stateModificationContext)
            throws JahiaException {
        // first let's check if we have entries that correspond for this
        // container
        RestoreVersionTestResults opResult = new RestoreVersionTestResults ();
        opResult.merge (
                super.isValidForRestore (user, operationMode, entryState,
                        removeMoreRecentActive, stateModificationContext));
        if (opResult.getStatus () == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
            return opResult;
        }

        // now let's check for the children of this container. If only
        // one of them fails, we fail the whole container.
        List<Locale> locales = new ArrayList<Locale> ();
        locales.add (EntryLoadRequest.SHARED_LANG_LOCALE);
        locales.add (LanguageCodeConverters.languageCodeToLocale (entryState.getLanguageCode ()));
        EntryLoadRequest loadRequest = new EntryLoadRequest (entryState.getWorkflowState (),
                entryState.getVersionID (), locales);
        List<? extends ContentObject> children = getChilds (user, loadRequest);
        for (ContentObject curChild : children) {
            opResult.merge (
                    curChild.isValidForRestore (user, operationMode, entryState,
                            removeMoreRecentActive, stateModificationContext));
        }
        return opResult;
    }

    public String getDisplayName(ProcessingContext jParams) {
        JahiaContainerDefinition def = (JahiaContainerDefinition)
                JahiaContainerDefinition.getChildInstance(
                String.valueOf(this.getDefinitionID(jParams.getEntryLoadRequest())));
        String primaryItemName = def.getNodeType().getPrimaryItemName();
        String primaryDisplayName = null;
        try {
            if (primaryItemName != null) {
                List<? extends ContentObject> obj = getChilds(jParams.getUser(), jParams.getEntryLoadRequest());
                for (ContentObject contentObject : obj) {
                    ContentDefinition primDef = (ContentDefinition) ContentDefinition.getInstance(contentObject.getDefinitionKey(jParams.getEntryLoadRequest()));
                    if (primDef.getName().equals(def.getName()+"_"+primaryItemName)) {
                        primaryDisplayName = contentObject.getDisplayName(jParams);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        String title ;
        try {
            title = def.getTitle(
                    jParams.getEntryLoadRequest().getFirstLocale(true));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            title = getObjectKey().getKey() ;
        }
        if (primaryDisplayName != null) {
            primaryDisplayName = StringUtils.abbreviate(primaryDisplayName,15);
            return title + " ( "+primaryDisplayName + " )";
        } else {
            return title;
        }
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
                List<? extends ContentObject> childs = getChilds(user, null, JahiaContainerStructure.JAHIA_CONTAINER);
                for (Iterator<? extends ContentObject> it = childs.iterator(); it.hasNext() && !result;) {
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

        ContentContainerTools.getInstance ().copyEntry (getID (), fromE, toE);

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
        ContentContainerTools.getInstance().deleteEntry(getID (), deleteEntryState);
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
                .markContainerLanguageForDeletion (getID (), user, languageCode,
                        stateModificationContext);

        notifyContainerUpdate(ContainersChangeEventListener.CONTAINER_DELETED);
    }

    public int getAclID () {
        return aclID;
    }

    public void setAclID(int aclID) {
        this.aclID = aclID;
        containerManager.updateContainerAclId(getID(), aclID);
    }

    public RestoreVersionTestResults restoreVersion (JahiaUser user,
                                                     String operationMode,
                                                     ContentObjectEntryState entryState,
                                                     boolean removeMoreRecentActive,
                                                     RestoreVersionStateModificationContext
                                                     stateModificationContext)
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
       boolean isDeleted = this.isDeletedOrDoesNotExist(entryState.getVersionID());

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(entryState.getLanguageCode()));
        EntryLoadRequest loadRequest = null;

        List<? extends ContentObject> children = null;
        if ( stateModificationContext.isUndelete() || !isDeleted ){
            // 1. First restore archive
            // load archive to restore
            loadRequest = new EntryLoadRequest(entryState.
               getWorkflowState(), entryState.getVersionID(), locales);
            children = getChilds(user, loadRequest);

            // For performance issue, we don't want to restore twice objects.
            List<String> processedChilds = new ArrayList<String>();

            for (ContentObject curChild : children) {
               // We don't allow restoring direct page field, they have to be restored
               // outside here.
               /*
               if ( curChild instanceof ContentPageField ){
                   ContentPageField pageField = (ContentPageField)curChild;
                   try {
                       ContentObjectEntryState es = pageField.getClosestVersionedEntryState(entryState);
                       String val = pageField.getValue(es);
                       if ( !"-1".equals(val) ){
                           ContentPage cPage = ContentPage.getPage(Integer.
                               parseInt(val));
                           if (cPage != null &&
                               (cPage.getPageType(new EntryLoadRequest(es)) ==
                                ContentPage.TYPE_DIRECT)) {
                               continue;
                           }
                       }
                   } catch ( Exception t ){
                       logger.debug("Skip restoring direct page field inside JahiaContainer.restore, fieldID="
                                    + curChild.getID(),t );
                   }
               }*/
               opResult.merge(curChild.restoreVersion(user,
                   operationMode, entryState, (removeMoreRecentActive),
                   stateModificationContext));
               processedChilds.add(curChild.getObjectKey().toString());
            }

            //2.Second, remove more recent data
            // We don't need to apply "remove more recent data here, because the overriden
            // super.restoreVersion call will automatically mark this container for delete
            // if it is more recent and therefore mark its fields too.
            if ( !stateModificationContext.isUndelete() && removeMoreRecentActive ){
               // we need to check if this container doesn't contain a page that is
               // only in staging --> we won't re

               // load staging or active to perform a restore which will mark them for delete
               loadRequest =
                       new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                       0, locales);
               children = getChilds(user, loadRequest);

               for (ContentObject curChild : children) {
                   if ( !processedChilds.contains(curChild.getObjectKey().toString()) ){

                       // We don't allow restoring direct page field, they have to be restored
                       // outside here.
                       /*
                       if (curChild instanceof ContentPageField) {
                           ContentPageField pageField = (ContentPageField)
                               curChild;
                           try {
                               es = pageField.getClosestVersionedEntryState(
                                   entryState);
                               if ( es != null ){
                                   String val = pageField.getValue(es);
                                   ContentPage cPage = ContentPage.getPage(Integer.
                                       parseInt(val));
                                   lr = new EntryLoadRequest(es);
                                   lr.setWithDeleted(true);
                                   lr.setWithMarkedForDeletion(true);
                                   if (cPage != null &&
                                       (cPage.getPageType(lr)
                                        == ContentPage.TYPE_DIRECT)) {
                                       continue;
                                   }
                               }
                           }
                           catch (Exception t) {
                               logger.debug(
                                   "Skip restoring direct page field inside JahiaContainer.restore, fieldID="
                                   + curChild.getID(), t);
                           }
                       }*/
                       opResult.merge(curChild.restoreVersion(user,
                           operationMode, entryState, removeMoreRecentActive,
                           stateModificationContext));
                   }
               }
            }
        }

       // we need to ensure that synchronize this JahiaContainer states here
       // because childs ContentPageField could change it ( restore and page move
       // issue ).
       ContentContainer contentContainer
               = ContentContainer.getContainer(this.getID(),true);
       this.activeAndStagedEntryStates =
               new ArrayList<ContentObjectEntryState>(contentContainer.getActiveAndStagingEntryStates());
        if (  contentContainer.versionedEntryStates != null ){
            this.versionedEntryStates = new ArrayList<ContentObjectEntryState>(contentContainer.versionedEntryStates);
        }

       opResult.merge(super.restoreVersion(user, operationMode, entryState,
               removeMoreRecentActive,
               stateModificationContext));

        notifyContainerUpdate(ContainersChangeEventListener.CONTAINER_UPDATED);

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
        //ServicesRegistry.getInstance().getJahiaSearchService()
        //        .indexContainer(getID(), user);

        // check to avoid link container without page
        if (stateModificationContext.getContainerPageChildId() == -1){
            Set<String> stagedStates = this.getStagingLanguages(false);
            if ( !stagedStates.isEmpty() ){
                loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, locales);
                loadRequest.setWithMarkedForDeletion(true);
                children = this.getChilds(user,loadRequest);
                boolean processLive = true;
                RestoreVersionStateModificationContext smc = new RestoreVersionStateModificationContext(stateModificationContext.getStartObject(),stateModificationContext.getLanguageCodes(),stateModificationContext.getEntryState());
                smc.pushAllLanguages(true);
                for ( ContentObject curChild : children){
                    if ( curChild instanceof ContentPageField ){
                        JahiaPageField jahiaPageField = (JahiaPageField)((ContentPageField)curChild).getJahiaField(loadRequest);
                        try {
                            int pageId = Integer.parseInt(jahiaPageField.getValue());
                            if ( pageId != -1 ){
                                ContentPage contentPage = ContentPage.getPage(pageId);
                                if ( contentPage != null ){
                                    if ( !contentPage.hasActiveOrStagingEntries() || contentPage.isMarkedForDelete() ) {
                                        this.markLanguageForDeletion(user,ContentObject.SHARED_LANGUAGE,stateModificationContext);
                                    } else {
                                        // check for moved page
                                        ContentPageField pageField = (ContentPageField)contentPage.getParent(EntryLoadRequest.STAGED);
                                        if ( pageField != null && pageField.getContainerID() != this.getID() ){
                                            this.markLanguageForDeletion(user,ContentObject.SHARED_LANGUAGE,stateModificationContext);
                                        }
                                    }

                                }
                            }
                        } catch ( Exception t ){
                        }
                        processLive = false;
                        break;
                    }
                }
                if ( processLive ){
                    loadRequest = new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                            ServicesRegistry.getInstance()
                                .getJahiaVersionService().getCurrentVersionID(), locales);
                    loadRequest.setWithDeleted(true);
                    children = this.getChilds(user,loadRequest);

                    for ( ContentObject curChild : children){
                        if ( curChild instanceof ContentPageField ){
                            JahiaPageField jahiaPageField = (JahiaPageField)((ContentPageField)curChild).getJahiaField(loadRequest);
                            try {
                                int pageId = Integer.parseInt(jahiaPageField.getValue());
                                if ( pageId != -1 ){
                                    ContentPage contentPage = ContentPage.getPage(pageId);
                                    if ( contentPage != null ){
                                        if ( !contentPage.hasActiveOrStagingEntries() || contentPage.isMarkedForDelete() ) {
                                            this.markLanguageForDeletion(user,ContentObject.SHARED_LANGUAGE,stateModificationContext);
                                        } else {
                                            // check for moved page
                                            ContentPageField pageField = (ContentPageField)contentPage.getParent(EntryLoadRequest.STAGED);
                                            if ( pageField != null && pageField.getContainerID() != this.getID() ){
                                                this.markLanguageForDeletion(user,ContentObject.SHARED_LANGUAGE,stateModificationContext);
                                            }
                                        }
                                    }
                                }
                            } catch ( Exception t ){
                            }
                            break;
                        }
                    }
                }
            }
        }

        return opResult;
    }

    /**
     * Is this kind of object shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

    /**
     * Return the parent container list ID.
     *
     * @return This parent container list ID.
     */
    public int getParentContainerListID () {
        return this.parentContainerListID;
    }

    public JahiaContainer getJahiaContainer (ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {
        return ServicesRegistry.getInstance ().getJahiaContainersService ().loadContainer (
                getID (), LoadFlags.ALL, jParams, loadRequest);
    }

    /**
     * This method removes all the data related to the staging mode of this
     * container, effectively "undoing" all the changes and returning to the
     * active values. Doesn't descent in childs.
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

        notifyContainerUpdate(ContainersChangeEventListener.CONTAINER_UPDATED);

        ContentUndoStagingEvent jahiaEvent = new ContentUndoStagingEvent(this, this.getSiteID(), jParams);
        ServicesRegistry.getInstance().getJahiaEventService()
                .fireContentObjectUndoStaging(jahiaEvent);

        // handled by previous event
        /*
        if ( this.activeAndStagedEntryStates.size() == 0 ){
            ServicesRegistry.getInstance().getJahiaSearchService()
                    .removeContentObject(this.getObjectKey());
        } else {
            ServicesRegistry.getInstance().getJahiaSearchService()
                .indexContainer(getID(), jParams.getUser());
        }*/
    }

    /**
     * Returns the status of this container, indicating if the restore will
     * remove it, restore it or keep unchanged.
     * oldVersion and newVersion can be : 0  -> staged entry,
     * 1  -> active entry,
     * x  -> archive entry
     * -1 -> staging marked for delete
     *
     * @param oldVersion
     * @param newVersion
     * @param exactRestore  if <code>true</code>, remove more recent data and apply
     *                       archive deleted status when restoring
     *
     * @return
     */
    public int getRestoreStatus (int oldVersion, int newVersion, boolean exactRestore)
            throws JahiaException {

        int status = VersioningDifferenceStatus.UNCHANGED;
        oldVersion = resolveVersion(this,oldVersion);
        if ( newVersion != ContentObjectEntryState.WORKFLOW_STATE_START_STAGING ){
            newVersion = resolveVersion(this,newVersion);
        }

        // resolve old entry state
        ContentObjectEntryState oldEntryState =
                ContentObjectEntryState.getEntryState (oldVersion,
                        ContentObject.SHARED_LANGUAGE);
        oldEntryState = this.getEntryState (oldEntryState, false, false);

        ContentObjectEntryState newEntryState = null;
        if ( newVersion == ContentObjectEntryState.WORKFLOW_STATE_START_STAGING ){
            newEntryState =
                ContentObjectEntryState.getEntryState (0,
                        ContentObject.SHARED_LANGUAGE);
            newEntryState = this.getEntryState (newEntryState, false, true);
        } else if ( newVersion != 0 ){
            newEntryState =
                ContentObjectEntryState.getEntryState (newVersion,
                        ContentObject.SHARED_LANGUAGE);
            newEntryState = this.getEntryState (newEntryState, false, true);
        } else {
            // staged does not exist, so get live
            SortedSet<ContentObjectEntryState> entryStates = this.getActiveAndStagingEntryStates();
            for (ContentObjectEntryState curEntryState : entryStates){
                if ( curEntryState.getWorkflowState()
                     == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                    newEntryState = curEntryState;
                    break;
                }
            }
        }

        status = getRestoreStatus (oldEntryState, newEntryState, exactRestore);
        return status;
    }

    /**
     * Returns the status of this container, indicating if the restore will
     * remove it, restore it or keep unchanged.
     *
     * @param oldEntryState
     * @param newEntryState
     * @param exactRestore
     *
     * @return
     */
    public int getRestoreStatus (ContentObjectEntryState oldEntryState,
                                 ContentObjectEntryState newEntryState,
                                 boolean exactRestore) {

        int status = VersioningDifferenceStatus.UNCHANGED;
        if (oldEntryState == null && newEntryState == null) {
            return status;
        }

        if (oldEntryState == null) {
            //doesn't exist at old entry version
            if (exactRestore) {
                if (newEntryState != null) {
                    if ((newEntryState.getWorkflowState () == -1)
                            || (newEntryState.isStaging () && newEntryState.getVersionID ()
                            == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                        status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                    } else {
                        // to be removed because archive doesn't exist at all.
                        if ( newEntryState.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                            status = VersioningDifferenceStatus.TO_BE_REMOVED;
                        } else {
                            status = VersioningDifferenceStatus.ADDED;
                        }
                    }
                }
            } else {
                if (newEntryState != null) {
                    if ((newEntryState.getWorkflowState () == -1)
                            || (newEntryState.isStaging () && newEntryState.getVersionID ()
                            == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                        status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                    } else {
                        status = VersioningDifferenceStatus.ADDED;
                    }
                } else {
                    status = VersioningDifferenceStatus.ADDED;
                }
            }
        } else {
            if (newEntryState == null) {
                if (oldEntryState.getWorkflowState ()
                        == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                    status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                } else {
                    /*
                    if ( newEntryState.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                        status = VersioningDifferenceStatus.TO_BE_RESTORED;
                    } else {
                        status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                    }*/
                    status = VersioningDifferenceStatus.TO_BE_RESTORED;
                }
            } else {
                if (exactRestore) {
                    if (oldEntryState.getWorkflowState ()
                            == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                        if (newEntryState.getWorkflowState ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                                || (newEntryState.isStaging () && newEntryState.getVersionID ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                            status =
                                    VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                        } else {
                            // to be removed because archive status == delete
                            if ( newEntryState.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                                status = VersioningDifferenceStatus.TO_BE_RESTORED;
                            } else {
                                status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                            }
                        }
                    } else {
                        if (newEntryState.getWorkflowState ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                                || (newEntryState.isStaging ()
                                && newEntryState.getVersionID ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                            // actually deleted , but will be restored
                            if ( newEntryState.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                                status = VersioningDifferenceStatus.TO_BE_RESTORED;
                            } else {
                                status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                            }
                        } else {
                            status = VersioningDifferenceStatus.TO_BE_UPDATED;
                        }
                    }
                } else {
                    if (oldEntryState.getWorkflowState ()
                            == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                        if (newEntryState.getWorkflowState ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                                || (newEntryState.isStaging () && newEntryState.getVersionID ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                            status =
                                    VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                        }
                    } else {
                        if (newEntryState.getWorkflowState ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                                || (newEntryState.isStaging ()
                                && newEntryState.getVersionID ()
                                == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED)) {
                            // actually deleted , but will be restored
                            if ( newEntryState.getWorkflowState() == EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                                status = VersioningDifferenceStatus.TO_BE_RESTORED;
                            } else {
                                status = VersioningDifferenceStatus.CURRENTLY_DELETED_AND_NOT_RESTORED;
                            }
                        } else {
                            status = VersioningDifferenceStatus.TO_BE_UPDATED;
                        }
                    }
                }
            }
        }
        return status;
    }

    /**
     * Returns the status of this container, indicating if an activation of staging content will
     * remove it, update it or keep unchanged.
     *
     * @return
     */
    public int getStagingStatus ()
            throws JahiaException {

        int status = VersioningDifferenceStatus.UNCHANGED;

        // resolve active entry state
        ContentObjectEntryState activeEntryState =
                ContentObjectEntryState.getEntryState (1,
                        ContentObject.SHARED_LANGUAGE);
        activeEntryState = this.getEntryState (activeEntryState, false, false);

        // resolve staging entry state
        ContentObjectEntryState stagingEntryState =
                ContentObjectEntryState.getEntryState (0,
                        ContentObject.SHARED_LANGUAGE);
        stagingEntryState = this.getEntryState (stagingEntryState, false, false);

        if (stagingEntryState != null) {
            if (stagingEntryState.getVersionID ()
                    == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                status = VersioningDifferenceStatus.TO_BE_REMOVED;
            } else {
                if (activeEntryState == null) {
                    status = VersioningDifferenceStatus.ADDED;
                } else {
                    status = VersioningDifferenceStatus.TO_BE_UPDATED;
                }
            }
        }
        return status;
    }

    /**
     * Returns the workflow state of all the languages contained in this container.
     * This returns the state of both the active and staged languages, the
     * staging version taking priority over the active for a given language.
     * This method does NOT go down into the fields and sub-containers lists
     * to retrieve their state !
     *
     * @return an Map that contains the language code String as the key,
     *         and the current workflow state of the language is the value
     */
    public Map<String, Integer> getLanguagesStates () {

        Map<String, Integer> languageStates = new HashMap<String, Integer>();

        // first let's get all the active languages in the map.
        for (ContentObjectEntryState entryState : activeAndStagedEntryStates) {
            // ok huston, we have an active entry
            if (!entryState.isStaging () && !languageStates.containsKey(entryState.getLanguageCode())) {
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

    /**
     * Returns true if the field has staging entry of given language.
     *
     * @return true if the field has staging entry of given language.
     */
    public boolean hasStagingEntryIgnoreLanguageCase (String languageCode) {
        for (ContentObjectEntryState thisEntryState : activeAndStagedEntryStates) {
            if (thisEntryState.isStaging ()
                && (ContentObject.SHARED_LANGUAGE.equalsIgnoreCase(thisEntryState.getLanguageCode ())
                || thisEntryState.getLanguageCode ().equalsIgnoreCase(languageCode))) {
                return true;
            }
        }
        return false;
    }

    public ActivationTestResults isValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = super.isValidForActivation(languageCodes, jParams, stateModifContext);
        activationTestResults.merge(ServicesRegistry.getInstance().getJahiaContainersService ().isContainerValidForActivation(
                languageCodes, getID (), jParams.getUser(), null, jParams, stateModifContext));
        return activationTestResults;
    }


    public synchronized ActivationTestResults activate(
            Set<String> languageCodes,
            boolean versioningActive, JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {

        ActivationTestResults result = null;
        try {
            result = ServicesRegistry.getInstance().
                                           getJahiaContainersService().
                                           activateStagedContainer(
                languageCodes, getID(), user, saveVersion, jParams,
                stateModifContext);

            fireContentActivationEvent(languageCodes,
                                       versioningActive,
                                       saveVersion,
                                       jParams,
                                       stateModifContext,
                                       result);
            notifyContainerUpdate(this.getID(),
                                       ContainersChangeEventListener.CONTAINER_ACTIVATED);
            syncClusterOnValidation();
            return result;
        } catch ( Exception t ){
            notifyContainerUpdate(this.getID(),
                                       ContainersChangeEventListener.CONTAINER_ACTIVATED);
            syncClusterOnValidation();
            throw new JahiaException("Container Activation exception",
                                     "Container Activation exception",
                                     JahiaException.DATA_ERROR,
                                     JahiaException.ERROR_SEVERITY,t);
        }
    }

    /**
     * update ContainersChangeEventListener listener
     *
     * @param operation @see ContainersChangeEventListener
     */
    public static void notifyContainerUpdate(int containerID, String operation){

        // remove from cache
        ContentContainerTools.getInstance().invalidateContainerFromCache(containerID);
        try {
            // load from db
            ContentContainer contentContainer = getContainer(containerID);
            if ( contentContainer != null ){
                contentContainer.notifyContainerUpdate(operation,false);
            }
        } catch ( JahiaException je ){
            logger.debug("Exception occured on JahiaContainer id=" + containerID,
                         je);
        }
    }

    /**
     * update ContainersChangeEventListener listener
     *
     * @param operation @see ContainersChangeEventListener
     */
    private void notifyContainerUpdate(String operation){
        notifyContainerUpdate(operation,true);
    }

    /**
     * update ContainersChangeEventListener listener
     *
     * @param operation @see ContainersChangeEventListener
     */
    private void notifyContainerUpdate(String operation, boolean updateCache){

        if ( updateCache ){
            // remove from cache
            ContentContainerTools.getInstance().invalidateContainerFromCache(this.getID());
            ServicesRegistry.getInstance ().getJahiaContainersService ()
                .invalidateContainerFromCache(this.getID());
//            JahiaContainerUtilsDB.getInstance().invalidateCtnIdsByCtnListCache(this.getParentContainerListID());
        }
        ContainersChangeEventListener listener = (
                ContainersChangeEventListener)JahiaListenersRegistry.
                getInstance ()
                .getListenerByClassName (ContainersChangeEventListener.class.getName ());
        if ( listener != null ){
            listener.notifyChange(this,operation);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Clone
     */
    public Object clone () {
        List<ContentObjectEntryState> activeAndStagedEntryStates = new ArrayList<ContentObjectEntryState>(this.activeAndStagedEntryStates.size());
        if ( this.activeAndStagedEntryStates != null ){
            for (ContentObjectEntryState entryState : activeAndStagedEntryStates) {
                activeAndStagedEntryStates.add(entryState);
            }
        }
        ContentContainer clone =
            new ContentContainer (this.getID(),this.jahiaID,this.getPageID(),
                                  this.ctnDefID, this.parentContainerListID,
                                  this.aclID, activeAndStagedEntryStates);
//        clone.setProperties(properties);
        if ( this.versionedEntryStates != null ){
            clone.versionedEntryStates = new ArrayList<ContentObjectEntryState>(this.versionedEntryStates.size());
            for (ContentObjectEntryState entryState : versionedEntryStates) {
                clone.versionedEntryStates.add(entryState);
            }
        }
        return clone;
    }

    public static void invalidateContainerCache(int containerID) {
        ContentContainerTools.getInstance().invalidateContainerFromCache(containerID);
    }
    
    public String toString() {
        return "ContentContainer: " + getID();
    }

    /**
     * This method is called to notify that time based publishing state has changed
     */
    public void notifyStateChanged(){
        notifyContainerUpdate(ContainersChangeEventListener.CONTAINER_UPDATED);
    }

    public String getProperty(String name) throws JahiaException {
        return (String) getProperties().get(name);
    }

    public void setProperty(Object name, Object val) throws JahiaException {
        getProperties().put(name,val);
        ServicesRegistry.getInstance().getJahiaContainersService().setContainerProperties(getID(), getSiteID(), getProperties());
    }

    public void removeProperty(String name) throws JahiaException {
        getProperties().remove(name);
        containerManager.removeContainerProperty(getID(), name);
    }

    public Map<Object, Object> getProperties() {
        if (properties == null) {
            try {
                properties = ServicesRegistry.getInstance().getJahiaContainersService().getContainerProperties(getID());
            } catch (JahiaException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
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

                containerManager.putToWaitingStateContainer(this.getID(),newWorkflowState,entryState.getVersionID());

                newActiveAndStagingEntryStates.add (newEntryState);

            } else {
                newActiveAndStagingEntryStates.add (entryState);
            }

        }
        activeAndStagedEntryStates = newActiveAndStagingEntryStates;
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

        RuleEvaluationContext ctx = new RuleEvaluationContext(this.getObjectKey(),this,context,
                context.getUser());
        ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getID(),
                JahiaAdminUser.getAdminUser(this.getSiteID()), false, true,
                ctx);

        // update content childs
        List<? extends ContentObject> contentChilds = this.getChilds(JahiaAdminUser.getAdminUser(this.getSiteID()),
                EntryLoadRequest.STAGED);

        for (ContentObject child : contentChilds) {
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

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         ApplicationContext context = SpringContextSingleton.getInstance().getContext();
         containerListManager = (JahiaContainerListManager) context.getBean(JahiaContainerListManager.class.getName());
         containerManager = (JahiaContainerManager) context.getBean(JahiaContainerManager.class.getName());
     }

}

