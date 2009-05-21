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
//
//  EV  28.01.20001
//

package org.jahia.engines.deletecontainer;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.files.JahiaFileField;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

public class DeleteContainer_Engine implements JahiaEngine {

    /**
     * The engine's name
     */
    public static final String ENGINE_NAME = "deletecontainer";
    
    public static final int PAGE_LIST_SIZE_LIMIT = 30;    

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DeleteContainer_Engine.class);

    private static final String TEMPLATE_JSP = "delete_container";
    private EngineToolBox toolBox;

    private TransactionTemplate transactionTemplate = null;
    private JahiaPageService pageService;
    
    /**
     * Default constructor, creates a new <code>DeleteContainer_Engine</code> instance.
     */
    public DeleteContainer_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    public JahiaPageService getPageService() {
        return pageService;
    }

    public void setPageService(JahiaPageService pageService) {
        this.pageService = pageService;
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return toolBox.authoriseRender(jParams);
    } // end authoriseRender

    /**
     * renderLink
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        ContentContainer contentContainer = (ContentContainer) theObj;
        String params = EMPTY_STRING;
        params += "?mode=display";
        params += "&cid=" + contentContainer.getID();
        return jParams.composeEngineUrl(ENGINE_NAME, params);
    } // end renderLink

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;
    } // end needsJahiaData

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException,
            JahiaForbiddenAccessException {
        // initalizes the hashmap
        Map engineMap = initEngineMap(jParams);

        // checks if the user has the right to display the engine
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(
                "theContainer");
        final JahiaUser user = jParams.getUser();

        // does the current user have permission for the current engine ?
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.delete",
                        jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) <= 0) {
            throw new JahiaForbiddenAccessException();
        }

        if (theContainer.checkWriteAccess(user)) {
            // #ifdef LOCK
            engineMap.put("writeAccess", Boolean.TRUE);
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            if (jParams.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, theContainer.getID());
                if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                        jParams.getSessionState().getMaxInactiveInterval())) {
                    // #endif
                    engineMap.put("lock", lockKey);
                    processScreen(jParams, engineMap);
                    // #ifdef LOCK
                } else {
                    final Map m = lockRegistry.getLocksOnObject(lockKey);
                    if (! m.isEmpty()) {
                        final String action = (String) m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().
                                getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
                    }
                    processScreen(jParams, engineMap);
                }
            }
            // #endif
        } else {
            throw new JahiaForbiddenAccessException();
        }

        // displays the screen
        toolBox.displayScreen(jParams, engineMap);

        return null;

    } // end handleActions


    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return ENGINE_NAME;
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processScreen(final ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get("screen");
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, theContainer.getID());
        final LockService lockRegistry = ServicesRegistry.getInstance().
                getLockService();
        // #endif

        // dispatches to the appropriate sub engine
        final JahiaUser user = jParams.getUser();
        if (theScreen.equals("edit")) {
            composeWarningMessages(jParams, engineMap);
        } else if (theScreen.equals("save") || theScreen.equals("apply")) {
            if (engineMap.get("writeAccess") != null) {
                final LockKey futureStolenkey = (LockKey) engineMap.get("LockKey");
                if (LockPrerequisites.getInstance().getLockPrerequisitesResult(futureStolenkey) != null) {
                    final String param = jParams.getParameter("whichKeyToSteal");
                    if (param != null && param.length() > 0) {
                        if (lockRegistry.isAlreadyAcquired(futureStolenkey) &&
                                !lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user.getUserKey())) {
                            logger.debug("steal: " + user.getUsername());
                            lockRegistry.steal(futureStolenkey, user, user.getUserKey());
                        } else {
                            logger.debug("nuke: " + user.getUsername());
                            lockRegistry.nuke(futureStolenkey, user, user.getUserKey());
                        }
                        if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                                jParams.getSessionState().getMaxInactiveInterval())) {
                            engineMap.remove("LockKey");
                            jParams.getSessionState().setAttribute("jahia_session_engineMap", engineMap);
                            logger.debug("We were able to acquire the lock after stealing");
                        } else {
                            logger.debug("We were unable to acquire the lock after stealing");
                        }
                        jParams.getSessionState().removeAttribute("showNavigationInLockEngine");
                    }
                    engineMap.put("screen", jParams.getParameter("lastscreen"));
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    processScreen(jParams, engineMap);
                    return;
                }

                // #ifdef LOCK
                // Did somebody steal the lock ? Panpan cucul !
                if (jParams.settings().areLocksActivated() &&
                        lockRegistry.isStealedInContext(lockKey, user, user.getUserKey())) {
                    engineMap.put("screen", jParams.getParameter("lastscreen"));
                    engineMap.put("jspSource", "apply");
                    return;
                }
                // #endif
                if (transactionTemplate == null) {
                    SpringContextSingleton instance = SpringContextSingleton.getInstance();
                    if (instance.isInitialized()) {
                        PlatformTransactionManager manager = (PlatformTransactionManager) instance.getContext().getBean("transactionManager");
                        transactionTemplate = new TransactionTemplate(manager);
                    }
                }
                try {
//                    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                        protected void doInTransactionWithoutResult(TransactionStatus status) {
//                            try {
                    JahiaEvent theEvent = new JahiaEvent(this, jParams, theContainer);
                    ServicesRegistry.getInstance().getJahiaEventService(). fireDeleteContainer(theEvent);

                    // ServicesRegistry.getInstance().getJahiaContainersService().deleteContainer( theContainer.getID(), jParams );
                    ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());

                    // we only need to remove the shared language since this will
                    // automatically mark all sub languages for deletion too...
                    Set<String> curLanguageCodes = new HashSet<String>();
                    curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);

                    StateModificationContext stateModifContext =
                            new StateModificationContext(new
                                    ContentContainerKey(
                                    theContainer.getID()), curLanguageCodes, true);
                    stateModifContext.pushAllLanguages(true);

                    contentContainer.markLanguageForDeletion(
                            user,
                            ContentObject.SHARED_LANGUAGE,
                            stateModifContext);

//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    });
                    
                    //Check File deletion
                    String deleteFile = jParams.getParameter("deleteFile");
                    if(deleteFile != null)  
                    {
                    	//delete File
                    	int pageDefID = jParams.getPage().getPageTemplateID();
                    	Iterator fieldList = theContainer.getFields();
                    	 while (fieldList.hasNext()) {
                         final JahiaField aField = (JahiaField) fieldList.next();
                         int fieldType = aField.getDefinition().getType();
                         if(fieldType == org.jahia.data.fields.FieldTypes.FILE)
                         {
                        	 JahiaFileField fileField = (JahiaFileField)aField.getObject();
                        	 int fid = aField.getContentField().getID();
                        	 String name = fileField.getRealName();
                        	 JCRNodeWrapper nodeToDelete = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(name, jParams.getUser());
                        	 List<UsageEntry> useList = ServicesRegistry.getInstance().getJCRStoreService().findUsages(fileField.getStorageName(), jParams, false);
                        	 if(useList == null || useList.size() == 0 || useList.size() == 1)
                        	 {
                        		 if(useList.size()== 1)
                        		 {
                        			 UsageEntry usage = (UsageEntry)useList.get(0);
                        			 if(usage.getId() == fid)
                        			 {
                                 int status = nodeToDelete.deleteFile() ;
                                 switch (status) {
                                     case JCRNodeWrapper.OK:
                                         nodeToDelete.saveSession();
                                         break ;
                                     case JCRNodeWrapper.INVALID_FILE:
                                    	   logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - INVALID FILE").toString()) ;
                                         break ;
                                     case JCRNodeWrapper.ACCESS_DENIED:
                                    	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString()) ;
                                         break ;
                                     case JCRNodeWrapper.UNSUPPORTED_ERROR:
                                    	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString()) ;
                                         break ;
                                     default:
                                    	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - UNKNOWN ERROR").toString()) ;
                                 }
                        			 }
                        		 }
                        		 else{
                               int status = nodeToDelete.deleteFile() ;
                               switch (status) {
                                   case JCRNodeWrapper.OK:
                                       nodeToDelete.saveSession();
                                       break ;
                                   case JCRNodeWrapper.INVALID_FILE:
                                  	   logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - INVALID FILE").toString()) ;
                                       break ;
                                   case JCRNodeWrapper.ACCESS_DENIED:
                                  	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString()) ;
                                       break ;
                                   case JCRNodeWrapper.UNSUPPORTED_ERROR:
                                  	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString()) ;
                                       break ;
                                   default:
                                  	 logger.error(new StringBuilder(nodeToDelete.getPath()).append(" - UNKNOWN ERROR").toString()) ;
                               }
                        		 }
                        	 }
                         }
                    	 }
                    }
                    
                } catch (Exception e) {
                    logger.error("Error during delete operation of an element we must flush all caches to ensure integrity between database and viewing");
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    throw new JahiaException(e.getMessage(), e.getMessage(),
                            JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
                } finally {
                    // #ifdef LOCK
                    if (jParams.settings().areLocksActivated()) {
                        lockRegistry.release(lockKey, user, user.getUserKey());
                    }
                    // #endif
                }
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (theScreen.equals("cancel")) {
            // #ifdef LOCK
            if (jParams.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
            // #endif
            engineMap.put("jspSource", "close");
        }
    } // end processScreen

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     *
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap(ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {
        Map engineMap;
        JahiaContainer theContainer;

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession( true );
        SessionState theSession = jParams.getSessionState();

        // gets container id
        String ctnidStr = jParams.getParameter("cid");
        int ctnid;
        try {
            ctnid = Integer.parseInt(ctnidStr);
        } catch (NumberFormatException nfe) {
            throw new JahiaException("Error in parameters", "Error in parameters : cid (" + ctnidStr +
                    ") cannot be converted in int",
                    JahiaException.DATA_ERROR,
                    JahiaException.CRITICAL_SEVERITY, nfe);
        }

        // Resolve language code
        String languageCode = jParams.getParameter("engine_lang");
        if (languageCode == null) {
            languageCode = jParams.settings().getDefaultLanguageCode();
        }
        String prevLanguageCode = jParams.getParameter("prev_engine_lang");
        if (prevLanguageCode == null) {
            prevLanguageCode = languageCode;
        }

        EntryLoadRequest entryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0, new ArrayList());
        entryLoadRequest.getLocales()
                .add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        EntryLoadRequest prevEntryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0, new ArrayList());
        prevEntryLoadRequest.getLocales()
                .add(LanguageCodeConverters.languageCodeToLocale(prevLanguageCode));

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter("screen");
        if (theScreen != null) {
            // if no, load the field value from the session
            logger.debug("engine map loaded from the session") ;
            engineMap = (Map) theSession.getAttribute("jahia_session_engineMap");

            // engine language helper
            EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
            if (elh == null) {
                elh = new EngineLanguageHelper(jParams.getLocale());
                engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER, elh) ;
            }
            elh.update(jParams);

            // change the container language
            JahiaContentContainerFacade jahiaContentContainerFacade = new JahiaContentContainerFacade(ctnid,
                    LoadFlags.ALL,
                    jParams,
                    jParams.getSite().getLanguageSettingsAsLocales(true),
                    false);
            theContainer = jahiaContentContainerFacade.getContainer(entryLoadRequest, true);
            if (theContainer == null &&
                    jahiaContentContainerFacade.getContainers().hasNext()) {
                // requested language not found, return the first available language
                theContainer = (JahiaContainer) jahiaContentContainerFacade.getContainers().next();
            }
            engineMap.put("theContainer", theContainer);

            ///////////////////////////////////////////////////////////////////////////////////////
            // FIXME -Fulco-
            //
            //      This is a quick hack, engineMap should not be null if the session didn't
            //      expired. Maybe there are other cases where the engineMap can be null, I didn't
            //      checked them at all.
            ///////////////////////////////////////////////////////////////////////////////////////
            if (engineMap == null) {
                throw new JahiaSessionExpirationException();
            }

            //theContainer = (JahiaContainer) engineMap.get("theContainer");
        } else {
            logger.debug("engine map will be rebuilt") ;
            JahiaContentContainerFacade jahiaContentContainerFacade = new JahiaContentContainerFacade(ctnid,
                    LoadFlags.ALL,
                    jParams,
                    jParams.getSite().getLanguageSettingsAsLocales(true),
                    false);
            theContainer = jahiaContentContainerFacade.getContainer(entryLoadRequest, true);
            if (theContainer == null &&
                    jahiaContentContainerFacade.getContainers().hasNext()) {
                // requested language not found, return the first available language
                theContainer = (JahiaContainer) jahiaContentContainerFacade.getContainers().next();
            }

            theScreen = "edit";
            // init engine map
            engineMap = new HashMap();
            engineMap.put("theContainer", theContainer);
            engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
            engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
            engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, "?cid=" + ctnid));
            theSession.setAttribute("jahia_session_engineMap", engineMap);
        }

        // sets screen
        engineMap.put("screen", theScreen);
        if (!theScreen.equals("save")) {
            engineMap.put("jspSource", TEMPLATE_JSP);
        } else {
            engineMap.put("jspSource", "close");
        }

        // sets engineMap for JSPs
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
        jParams.setAttribute("engineTitle", "Delete Container");

        return engineMap;
    } // end initEngineMap

    /**
     * composes warning messages
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap the engine parameters
     */
    private void composeWarningMessages(ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        JahiaContainer theContainer = (JahiaContainer) engineMap.get(
                "theContainer");

        engineMap.put("deletedPages", new ArrayList());
        engineMap.put("deletedLinks", new ArrayList());
        engineMap.put("futureBrokenLinkObjects", new ArrayList());
        engineMap.put("warning", Boolean.FALSE);
        engineMap.put("errorMessage", Boolean.FALSE);

        JahiaPage page = pageService.lookupPage(theContainer.getPageID(), jParams);
        int pageDefID = page.getPageTemplateID();

        Set objectKeysPointingToDeletedContent = new HashSet();

        if (theContainer.getContentContainer().hasStagingEntryIgnoreLanguageCase(jParams.getCurrentLocale().getLanguage())) {
            engineMap.put("stagingWarning", Boolean.TRUE) ;
            engineMap.put("warning", Boolean.TRUE);
        } else {
            engineMap.put("stagingWarning", Boolean.FALSE) ;
        }

        checkContainerAccessRights(theContainer, pageDefID, objectKeysPointingToDeletedContent, jParams, engineMap);

        /**
         * todo we should also be checking in sub container lists here for
         * all containers and all fields that contain pages.
         */

    } // end composeWarningMessages
    
    private boolean checkContainerAccessRights(JahiaContainer theContainer, int pageDefID,
            Set objectKeysPointingToDeletedContent, ProcessingContext jParams, Map engineMap) throws JahiaException {
        boolean allowed = theContainer.getContentContainer().checkWriteAccess(jParams.getUser(), true, true);
        if (!allowed) {
            engineMap.put("errorMessage", Boolean.TRUE);
        } else {
            Iterator theFields = theContainer.getFields();

            engineMap.remove("ctnPickers") ;
            Set<ContentObject> pickers = theContainer.getContentContainer().getPickerObjects() ;
            if (pickers.size() > 0) {
                engineMap.put("ctnPickers", pickers);
            }

            while (allowed && theFields.hasNext()) {
                JahiaField theField = (JahiaField) theFields.next();
                int fieldType = theField.getDefinition().getType();
                fieldType = (fieldType != 0) ? fieldType : theField.getType();

                // checks if deleting the container means deleting pages
                if (fieldType == FieldTypes.PAGE) {
                    JahiaPage thePage = (JahiaPage) theField.getObject();
                    if (thePage != null) {
                        objectKeysPointingToDeletedContent.addAll(contentObjectPointingOnPage(thePage.getID()));

                        Set deletedPageIDs = pageService.getUncheckedPageSubTreeIDs(thePage.getID(), true,
                                PAGE_LIST_SIZE_LIMIT + 1);
                        List deletedPages = new ArrayList(deletedPageIDs.size());
                        List deletedLinks = new ArrayList();

                        if (deletedPageIDs.size() <= PAGE_LIST_SIZE_LIMIT) {
                            deletedLinks = new ArrayList(ServicesRegistry.getInstance().getJahiaPageService()
                                    .getPagesPointingOnPage(thePage.getID(), jParams));

                            for (Iterator it = deletedPageIDs.iterator(); it.hasNext();) {
                                int aPageID = ((Integer) it.next()).intValue();
                                objectKeysPointingToDeletedContent.addAll(contentObjectPointingOnPage(aPageID));
                                deletedLinks.addAll(pageService.getPagesPointingOnPage(aPageID, jParams));
                                deletedPages.add(pageService.lookupPage(aPageID, jParams));
                            }
                        } else {
                            for (Iterator it = deletedPageIDs.iterator(); it.hasNext();) {
                                deletedPages.add(pageService.lookupPage(((Integer) it.next()).intValue(), jParams));
                            }
                        }

                        engineMap.put("deletedPages", deletedPages);
                        engineMap.put("deletedLinks", deletedLinks);

                        List hardcodedLinkSourceObject = buildHardcodedSourceInfo(
                                objectKeysPointingToDeletedContent, jParams);
                        engineMap.put("futureBrokenLinkObjects", hardcodedLinkSourceObject);
                        if (!deletedPages.isEmpty() || !deletedLinks.isEmpty()
                                || !objectKeysPointingToDeletedContent.isEmpty()) {
                            engineMap.put("warning", Boolean.TRUE);
                        }
                    }
                }
            }
            if (pickers != null && pickers.size() > 0) {
                engineMap.put("warning", Boolean.TRUE);
            }
        }
        return allowed;
    }

    private Set contentObjectPointingOnPage (int pageID)
            throws JahiaException {
        Set objectKeysPointingToPage = new HashSet();
        JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.PAGE + pageID);
        for (JahiaFieldXRef fieldXRef : c) {
            ContentField contentField = ContentField.getField(fieldXRef.getComp_id().getFieldId());
            if (contentField != null) {
                if (!contentField.getActiveAndStagingEntryStates().isEmpty()) {
                    objectKeysPointingToPage.add(contentField.getObjectKey());
                } else {
                    logger.debug(
                            "No active or staging entries found for objectKey=" +
                            contentField.getObjectKey() +
                            ", not displaying deleted or archived references");
                }
            } else {
                logger.warn(
                        "Hardcoded URL link found for object that no longer exists:" +
                        fieldXRef.getComp_id().getFieldId() + ", removing reference");
            }
        }
        return objectKeysPointingToPage;
    }

    private List buildHardcodedSourceInfo(final Set objectKeys,
                                               final ProcessingContext processingContext){
        List sourceObjectList = new ArrayList();
        Iterator objectKeyIter = objectKeys.iterator();
        ContentObjectEntryState entryState = new ContentObjectEntryState(
                ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                0, processingContext.getLocale().toString());
        while (objectKeyIter.hasNext()) {
            ObjectKey curObjectKey = (ObjectKey) objectKeyIter.next();
            HardcodedLinkSourceInfo curLinkInfo = new HardcodedLinkSourceInfo();
            curLinkInfo.setID(curObjectKey.getIDInType());
            curLinkInfo.setObjectType(curObjectKey.getType());
            try {
                JahiaObject jahiaObject = JahiaObject.getInstance(curObjectKey);
                if (jahiaObject instanceof ContentObject) {
                    ContentObject curContentObject = (ContentObject) jahiaObject;
                    ObjectKey definitionKey = curContentObject.getDefinitionKey(processingContext.getEntryLoadRequest());
                    ContentDefinition definition = ContentDefinition.getContentDefinitionInstance(definitionKey);
                    if (definition instanceof JahiaFieldDefinition) {
                        curLinkInfo.setName(((JahiaFieldDefinition)definition).getItemDefinition().getLabel(processingContext.getLocale()));
                    } else {
                        curLinkInfo.setName(definition.getName());
                    }
                    curLinkInfo.setTitle(definition.getTitle(curContentObject, entryState));
                    if (curContentObject instanceof ContentField) {
                        ContentField curContentField = (ContentField) curContentObject;
                        curLinkInfo.setObjectSubType(FieldTypes.typeName[curContentField.getType()]);
                    }
                }
                if (jahiaObject instanceof PageReferenceableInterface) {
                    PageReferenceableInterface pageObject = (PageReferenceableInterface) jahiaObject;
                    curLinkInfo.setPageID(pageObject.getPageID());
                }
                sourceObjectList.add(curLinkInfo);
            } catch (ClassNotFoundException cnfe) {
                logger.error(
                        "Error while loading content object, will not be added to hardcoded source objects",
                        cnfe);
            }
        }
        return sourceObjectList;
    }

} // end DeleteContainer_Engine
