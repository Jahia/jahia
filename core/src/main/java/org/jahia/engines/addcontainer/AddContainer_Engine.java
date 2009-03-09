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

//
//  EV  10.01.2001
//  NK  05.02.2002 Added Multiple fields (JahiaSimpleField) edit at once support
//  NK	05.02.2002 Version 2.0 Multiple field edit + customisable field ( read/write permission check )
//

package org.jahia.engines.addcontainer;

import net.sf.cglib.proxy.Proxy;

import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorResult;
import org.apache.commons.validator.ValidatorResults;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.Resources;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.*;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.*;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.engines.categories.ManageCategories;
import org.jahia.engines.importexport.ManageContentPicker;
import org.jahia.engines.importexport.ManageImportExport;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.shared.JahiaPageEngineTempBean;
import org.jahia.engines.shared.Page_Field;
import org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine;
import org.jahia.engines.updatecontainer.UpdateContainer_Engine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.engines.validation.ValidationErrorSorter;
import org.jahia.engines.workflow.ManageWorkflow;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.events.JahiaEventGeneratorService;
import org.jahia.services.lock.Lock;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.fields.ContentField;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.Map.Entry;


/**
 * Display the popup that let the user add a new container.
 *
 * @author EV
 * @author NK
 * @version 2.0
 */
public class AddContainer_Engine implements JahiaEngine {

    /**
     * The engine name
     */
    public static final String ENGINE_NAME = "addcontainer";
    public static final String TEMPLATE_JSP = "add_container";

    /**
     * logging
     */
    private static final transient Logger logger = Logger.getLogger(AddContainer_Engine.class);

    private TransactionTemplate transactionTemplate = null;
    private LockService lockRegistry;

    /**
     * Default constructor, creates a new <code>AddContainer_Engine</code> instance.
     */
    public AddContainer_Engine() {
    }

    public LockService getLockRegistry() {
        return lockRegistry;
    }

    public void setLockRegistry(LockService lockRegistry) {
        this.lockRegistry = lockRegistry;
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender(final ProcessingContext jParams) {
        return EngineToolBox.getInstance().authoriseRender(jParams);
    }

    /**
     * @param jParams
     * @param theObj
     * @return
     * @throws JahiaException todo add some javadoc description here
     */
    public String renderLink(final ProcessingContext jParams, final Object theObj)
            throws JahiaException {
        final JahiaContainerList jahiaContainerList = (JahiaContainerList) theObj;
        final StringBuffer params = new StringBuffer("?mode=display&clistid=");
        params.append(jahiaContainerList.getID());
        params.append("&cdefid=");
        params.append(jahiaContainerList.getctndefid());
        params.append("&cpid=");
        params.append(jahiaContainerList.getPageID());
        params.append("&cparentid=");
        params.append(jahiaContainerList.getParentEntryID());

        final String opEditMode = jParams.getOperationMode();
        jParams.setOperationMode(ProcessingContext.EDIT);
        final String result = jParams.composeEngineUrl(ENGINE_NAME, params.toString());
        jParams.setOperationMode(opEditMode);
        return result;
    }

    public boolean needsJahiaData(final ProcessingContext jParams) {
        return true;
    } // end needsJahiaData

    protected boolean hasWriteAccess(JahiaContainer theContainer,
            JahiaUser theUser, Map<String, Object> engineMap, ProcessingContext jParams)
            throws JahiaException {
        boolean hasAdmin = false, hasWrite = false;
        if (theContainer.getListID() != 0) {
            JahiaContainerList theContainerList = ServicesRegistry.getInstance().getJahiaContainersService()
                    .loadContainerListInfo(theContainer.getListID());
            hasAdmin = theContainerList.checkAdminAccess(theUser);
            hasWrite = hasAdmin || theContainerList.checkWriteAccess(theUser);
        } else {
            JahiaPage thePage = jParams.getPage();
            hasAdmin = thePage.checkAdminAccess(theUser);
            hasWrite = hasAdmin || thePage.checkWriteAccess(theUser);
        }
        if (hasAdmin) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put("enableMetadata", Boolean.TRUE);
            engineMap.put("adminAccess", Boolean.TRUE);
            //engineMap.put( "enableRightView", Boolean.TRUE );
            engineMap.put("writeAccess", Boolean.TRUE);
            engineMap.put("enableImport", Boolean.TRUE);
            engineMap.put("enableContentPick", Boolean.TRUE);
            engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
            engineMap.put("enableAdvancedWorkflow", Boolean.TRUE);
        }
        if (hasWrite) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put("enableMetadata", Boolean.TRUE);
            engineMap.put("writeAccess", Boolean.TRUE);
            engineMap.put("enableImport", Boolean.TRUE);
            engineMap.put("enableContentPick", Boolean.TRUE);
            engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
        }
        engineMap.put("enableLogs", Boolean.FALSE);

        return hasWrite;
    }

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException,
            JahiaForbiddenAccessException {
        logger.debug("Processing engine action...");
        jParams.getSessionState().setAttribute("showNavigationInLockEngine", "true");

        // initalizes the hashmap
        final Map<String, Object> engineMap = initEngineMap(jParams);

        Integer contextualContainerListId = (Integer)engineMap.get("contextualContainerListId");
        if (contextualContainerListId == null){
            contextualContainerListId = new Integer(0);
        }

        // checks if the user has the right to display the engine
        final JahiaUser user = jParams.getUser();
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");

        // does the current user have permission for the current engine ?
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.add",
                        jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) <= 0) {
            throw new JahiaForbiddenAccessException();
        }

        if (hasWriteAccess(theContainer, user, engineMap, jParams)) {
        boolean allowedToProcess = true;

            if (jParams.settings().areLocksActivated()) {
                // We must ensure the presence of container list to put lock for the first time user trying to add
                // some content in this list
                ensureContainerList(theContainer, engineMap, jParams);
                final LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE,
                        theContainer.getListID());
                if (!lockRegistry.acquire(lockKey, user, user.getUserKey(),
                        jParams.getSessionState().getMaxInactiveInterval())) {
                    // Prerequisites are NOT completed ! Damned ! Redirect the JSP
                    // output to lock informations.
                    final Map<String, Set<Lock>> m = lockRegistry.getLocksOnObject(lockKey);
                    if (! m.isEmpty()) {
                        final String action = (String) m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().
                                getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
                    }

                    allowedToProcess = false;
                    processCurrentScreen(jParams, engineMap);
                } else {
                    engineMap.put("lock",lockKey);
                }
            }
            // #endif
            if (allowedToProcess) {

                // fire event
                final JahiaEvent theEvent = new JahiaEvent(this, jParams, theContainer);
                ServicesRegistry.getInstance().getJahiaEventService().fireAddContainerEngineAfterInit(theEvent);

                final EngineValidationHelper evh = processLastScreen(jParams, engineMap);
                if (evh != null && evh.hasErrors()) {
                    engineMap.put("screen", evh.getNextScreen());
                    engineMap.put("jspSource", TEMPLATE_JSP);
                } else {
                    processCurrentScreen(jParams, engineMap);
                }
            }
        } else {
            throw new JahiaForbiddenAccessException();
        }

        final String navigation = jParams.getParameter("navigation");
        if (navigation != null && navigation.length() > 0) {
            jParams.getSessionState().setAttribute("needToRefreshParentPage", "true");
        } else jParams.getSessionState().removeAttribute("needToRefreshParentPage");
        jParams.setAttribute("addnew",jParams.getParameter("addnew"));
        if (navigation != null && navigation.length() > 0 && !"new".equals(navigation)) {
            final SessionState theSession = jParams.getSessionState();
            theSession.setAttribute("Navigation", "Navigation");
            if (jParams.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, theContainer.getListID());
                final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
            final String cid;
            if ("first".equals(navigation)) {
                cid = (jParams.getParameter("first"));
            } else if ("previous".equals(navigation)) {
                cid = (jParams.getParameter("previous"));
            } else {
                throw new IllegalArgumentException("Validation param is unknow: " + navigation);
            }

            final UpdateContainer_Engine theEngine = (UpdateContainer_Engine) EnginesRegistry.getInstance().getEngineByBeanName("updateContainerEngine");
            jParams.getParameterMap().clear();
            jParams.setParameter("cid", cid);
            jParams.setParameter("contextualContainerListId",String.valueOf(contextualContainerListId));
            logger.debug("SWITCHING ENGINES: Navigating to container cid=" + cid);

            theEngine.handleActions(jParams, jData);
            return null;
        }

        // do not forward to engine.jsp - only the data should be saved (for e.g. partnerregistration)
        if (jParams.getParameter("noEngineForward") == null)
        // displays the screen
        EngineToolBox.getInstance().displayScreen(jParams, engineMap);

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
     * processes the last screen sent by the user
     *
     * @param jParams a ProcessingContext object
     */
    public EngineValidationHelper processLastScreen(final ProcessingContext jParams, final Map<String, Object> engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        EngineValidationHelper evh = null;

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter("lastscreen");

        if (lastScreen == null) {
            lastScreen = "edit";
            // reset session var
            jParams.getSessionState().removeAttribute("UpdateContainer");
        }
        logger.debug("Processing last screen..." + lastScreen);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.UPDATE_MODE;

        if (lastScreen.equals("edit")) {

            final StringBuffer buff = new StringBuffer();
            final ContainerFieldsEditHelper feh = (ContainerFieldsEditHelper) engineMap.get(
                    buff.append(ENGINE_NAME).append(".").append(
                            FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID).toString());

            if (feh.getLastFieldId() == 0) {
                // stop processing because of undefined field
                return null;
            }

            final JahiaContentContainerFacade jahiaContentContainerFacade = (JahiaContentContainerFacade) engineMap.get(
                    "UpdateContainer_Engine.JahiaContentContainerFacade");

            engineMap.put(JahiaEngine.PROCESSING_LOCALE, elh.getPreviousLocale());

            final JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(
                    elh.getPreviousEntryLoadRequest(), true);
            engineMap.put("theContainer", theContainer);

            if (!feh.processLastFields(ENGINE_NAME, jahiaContentContainerFacade,
                    elh, jParams, engineMap, mode)) {
                // if there was an error, come back to last screen
                engineMap.put("screen", lastScreen);
                engineMap.put("jspSource", TEMPLATE_JSP);
            }
        } else if (lastScreen.equals("timeBasedPublishing")) {
            if (engineMap.get("writeAccess") != null
                    || engineMap.get("adminAccess") != null) {
                final ObjectKey objectKey;
                final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");

                if (theContainer.getID() > 0) {
                    final ContentContainer contentContainer = ContentContainer
                            .getContainer(theContainer.getID());
                    objectKey = contentContainer.getObjectKey();
                } else {
                    objectKey = new ContentContainerKey(-1);
                }
                boolean result = TimeBasedPublishingEngine.getInstance().handleActions(jParams, mode, engineMap, objectKey);
                if ( !result ){
                    evh = (EngineValidationHelper)engineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".EngineValidationError");
                }
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("metadata")) {
            final ObjectKey objectKey;
            final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");

            if (theContainer.getID() > 0) {
                final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey = contentContainer.getObjectKey();
            } else {
                final JahiaContainerDefinition def = theContainer.getDefinition();
                objectKey = def.getObjectKey();
            }
            evh = Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);
        } else if (lastScreen.equals("categories")) {
            final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
            ManageCategories.getInstance().handleActions(jParams, mode,
                    engineMap, new ContentContainerKey(0),
                    theContainer.getDefinition(), true);
        } else if (lastScreen.equals("workflow")) {
            if (engineMap.get("adminAccess") != null) {
                ManageWorkflow.getInstance().handleActions(jParams, mode,
                        engineMap, null, null);
            } else {
                throw new JahiaForbiddenAccessException();
            }
        }

        return evh;
    } // end processLastScreen

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public EngineValidationHelper processCurrentScreen(final ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {

        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get("screen");
        logger.debug("Processing current screen... " + theScreen);

        final EngineLanguageHelper elh = (EngineLanguageHelper)
                engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        final JahiaContentContainerFacade jahiaContentContainerFacade
                = (JahiaContentContainerFacade) engineMap.get(
                "UpdateContainer_Engine.JahiaContentContainerFacade");

        final StringBuffer buff = new StringBuffer();
        final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(
                buff.append(ENGINE_NAME).append(".").append(
                        FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID).toString());

        final JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(
                elh.getCurrentEntryLoadRequest(), true);

        engineMap.put("theContainer", theContainer);

        setDefaultWorkflowMode(theContainer, jParams, engineMap);

        // indicates to sub engines that we are processing last screen
        final int[] mode = new int[]{JahiaEngine.LOAD_MODE};

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, theContainer.getListID());
        // #endif
        final JahiaUser user = jParams.getUser();
        if (theScreen.equals("edit")) {

            if (feh.getStayOnSameField()) {
                feh.setSelectedFieldId(feh.getLastFieldId());
            }
            feh.processCurrentFields(ENGINE_NAME, jahiaContentContainerFacade, elh, jParams, engineMap, mode[0]);
        } else if (theScreen.equals("timeBasedPublishing")) {
            if (engineMap.get("writeAccess") != null
                    || engineMap.get("adminAccess") != null) {
                final ObjectKey objectKey;
                if (theContainer.getID() > 0) {
                    final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                    objectKey = contentContainer.getObjectKey();
                } else {
                    objectKey = new ContentContainerKey(-1);
                }
                TimeBasedPublishingEngine.getInstance().
                        handleActions(jParams, mode[0], engineMap, objectKey);
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (theScreen.equals("categories")) {
            ManageCategories.getInstance().handleActions(jParams, mode[0],
                    engineMap,
                    new ContentContainerKey(jahiaContentContainerFacade.getContainerID()),
                    theContainer.getDefinition(), true);
        } else if (theScreen.equals("metadata")) {
            final ObjectKey objectKey;
            if (theContainer.getID() > 0) {
                final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey = contentContainer.getObjectKey();
            } else {
                final JahiaContainerDefinition def = theContainer.getDefinition();
                objectKey = def.getObjectKey();
            }
            Metadata_Engine.getInstance().handleActions(jParams, mode[0], objectKey);

        } else if (theScreen.equals("workflow")) {
            if (engineMap.get("adminAccess") != null) {
                ManageWorkflow.getInstance().handleActions(jParams, mode[0],
                        engineMap, null, ContentContainerList.getContainerList(theContainer.getListID()));
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (theScreen.equals("import")) {
            final JahiaContainerList list = ensureContainerList(theContainer, engineMap, jParams);
            ManageImportExport.getInstance().handleActions(jParams, mode[0],
                    engineMap, list.getContentContainerList());
        } else if (theScreen.equals("contentPick")) {
            final JahiaContainerList list = ensureContainerList(theContainer, engineMap, jParams);
            ManageContentPicker.getInstance().handleActions(jParams, mode[0],
                    engineMap, list.getContentContainerList());
        } else if (theScreen.equals("apply")) {
            final String lastScreen = jParams.getParameter("lastscreen");
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
                engineMap.put("screen", lastScreen);
                engineMap.put("jspSource", TEMPLATE_JSP);
                processCurrentScreen(jParams, engineMap);
                return null;
            }
        } else if (theScreen.equals("save")) { //|| theScreen.equals("apply")) {

            logger.debug("processCurrentScreen > we are in save Mode, navigation: " + jParams.getParameter("navigation"));

            if (engineMap.get("LockKey") != null) {
                logger.debug("We are in Read-Only mode... Do not save anything");
                return null;
            }

            if (transactionTemplate == null) {
                final SpringContextSingleton contextInstance = SpringContextSingleton.getInstance();
                if (contextInstance.isInitialized()) {
                    PlatformTransactionManager manager = (PlatformTransactionManager) contextInstance.getContext().getBean("transactionManager");
                    transactionTemplate = new TransactionTemplate(manager);
                }
            }

            String lastScreen = jParams.getParameter("lastscreen");
            if ("contentPick".equals(lastScreen)) {
                final JahiaContainerList list = ensureContainerList(theContainer, engineMap, jParams);
                if (ManageContentPicker.getInstance().handleActions(jParams, JahiaEngine.SAVE_MODE,
                        engineMap, list.getContentContainerList())) {
                    // #ifdef LOCK
                    if (jParams.settings().areLocksActivated()) {
                        lockRegistry.release(lockKey, user, user.getUserKey());
                    }
                    // #endif
                    return null;
                }
            } else if (ProcessingContext.isMultipartRequest(((ParamBean) jParams).getRequest())) {
                final JahiaContainerList list = ensureContainerList(theContainer, engineMap, jParams);
                if (ManageImportExport.getInstance().handleActions(jParams, JahiaEngine.SAVE_MODE,
                        engineMap, list.getContentContainerList())) {
                    // #ifdef LOCK
                    if (jParams.settings().areLocksActivated()) {
                        lockRegistry.release(lockKey, user, user.getUserKey());
                    }
                    // #endif
                    return null;
                }
            }
            // try to save but there are some error when processing previous field
            if (feh.getStayOnSameField()) {
                feh.setSelectedFieldId(feh.getLastFieldId());
                feh.processCurrentFields(AddContainer_Engine.ENGINE_NAME,
                        jahiaContentContainerFacade, elh,
                        jParams, engineMap, JahiaEngine.LOAD_MODE);
                return null;
            }

            mode[0] = JahiaEngine.VALIDATE_MODE;

            EngineValidationHelper evh = validate(jahiaContentContainerFacade, jParams, engineMap, feh, elh);

            //engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
            //JahiaEvent validationEvent = new JahiaEvent (this, jParams, theContainer);
            JahiaEventGeneratorService eventService = ServicesRegistry.getInstance ().getJahiaEventService ();
            //eventService.fireContainerValidation(validationEvent);

            if (evh != null && evh.hasErrors()) {

                final ContainerEditViewFieldGroup currentFieldGroup = getFieldGroup(
                        feh.getSelectedFieldId(),
                        theContainer,
                        feh.getContainerEditView());

                Collections.sort(evh.getErrors(), new ValidationErrorSorter(currentFieldGroup));

                if (!feh.getStayOnSameField()) {
                    feh.setSelectedFieldId(((JahiaField)(evh.getFirstError()).getSource()).getID());
                }

                for (Iterator<ValidationError> iterator = evh.getErrors().iterator(); iterator.hasNext();) {
                    ValidationError validationError = iterator.next();
                    if (validationError.getLanguageCode() != null && !validationError.getLanguageCode().equals(ContentField.SHARED_LANGUAGE)) {
                        jParams.setParameter(EngineLanguageHelper.ENGINE_LANG_PARAM, validationError.getLanguageCode()); 
                        elh.update(jParams);
                        break;
                    }
                }

                feh.processCurrentFields(AddContainer_Engine.ENGINE_NAME,
                        jahiaContentContainerFacade, elh,
                        jParams, engineMap, JahiaEngine.LOAD_MODE);

                // prepare view
                engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                engineMap.put("screen", "edit");
                engineMap.put("jspSource", TEMPLATE_JSP);
                return evh;
            }

            // metadata validation before save
            final ObjectKey[] objectKey = new ObjectKey[]{null};
            if (theContainer.getID() > 0) {
                final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey[0] = contentContainer.getObjectKey();
                evh = Metadata_Engine.getInstance().handleActions(jParams, mode[0], objectKey[0]);
                if (evh != null && evh.hasErrors()) {
                    engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                    engineMap.put("screen", evh.getNextScreen());
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    return null;
                }
            }

            engineMap.remove(JahiaEngine.ENGINE_VALIDATION_HELPER);
            mode[0] = JahiaEngine.SAVE_MODE;

            // #ifdef LOCK
            // Did somebody steal the lock ? Panpan cucul !
            if (jParams.settings().areLocksActivated() &&
                    lockRegistry.isStealedInContext(lockKey, user, user.getUserKey())) {
                engineMap.put("screen",
                        jParams.getParameter("lastscreen"));
                engineMap.put("jspSource", "apply");
                return null;
            }
            // #endif

            // fire event
            JahiaEvent theEvent = new JahiaEvent(this, jParams, theContainer);
            eventService.fireAddContainerEngineBeforeSave(theEvent);
//            try {
//                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//                    protected void doInTransactionWithoutResult(TransactionStatus status) {
//                        try {
            // save the container info
            int containerParentID = 0;
            try {
                containerParentID = ((Integer) engineMap.get("containerParentID")).intValue();
            } catch (Exception e) {
                try {
                    containerParentID = Integer.parseInt(jParams.getParameter("cparentid"));
                    engineMap.put("containerParentID", Integer.valueOf(containerParentID));
                } catch (Exception ee) {
                    logger.warn("Unable to set EngineMap value 'containerParentID'");
                }
            }
            final int parentAclID;
            if (containerParentID != 0) {
                parentAclID = ServicesRegistry.getInstance().getJahiaContainersService().
                        loadContainerInfo(containerParentID, jParams.getEntryLoadRequest()).getAclID();
            } else {
                parentAclID = jParams.getPage().getAclID();
            }

            // todo we need to set the ranking here.
            ServicesRegistry.getInstance().getJahiaContainersService().
                    saveContainerInfo(theContainer, containerParentID, parentAclID, jParams);

            /** This contextual container list Id attribute is used to position container list pagination to the last edited
             * container
             */
            Integer contextualContainerListId = (Integer)engineMap.get("contextualContainerListId");
            if (theContainer.getID()>0 && contextualContainerListId != null && contextualContainerListId.intValue() != 0){
                jParams.getSessionState().setAttribute("ContextualContainerList_" + String.valueOf(contextualContainerListId),
                        new Integer(theContainer.getID()));
            }

            //theEvent = new JahiaEvent (this, jParams, theContainer);
            //eventService.fireAddContainerEngineAfterSave(theEvent);

            saveFields(theContainer, jahiaContentContainerFacade, feh, jParams, mode[0], engineMap);

            // save rights and workflow
            if (engineMap.get("adminAccess") != null) {
                ManageRights.getInstance().handleActions(jParams, mode[0],
                        engineMap, theContainer.getAclID(), null, null, true,new ContentContainerKey(theContainer.getID()).toString());
                JahiaBaseACL acl = (JahiaBaseACL) engineMap.get(ManageRights.NEW_ACL+"_"+theContainer.getAclID());
                if (acl != null) {
                    theContainer.getContentContainer().updateAclForChildren(acl.getID());
                }                                 
                ManageWorkflow.getInstance().handleActions(jParams, mode[0],
                        engineMap, theContainer.getContentContainer());
            }

            if (engineMap.get("writeAccess") != null
                    || engineMap.get("adminAccess") != null) {
                if (theContainer.getID() > 0) {
                    final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                    objectKey[0] = contentContainer.getObjectKey();
                    TimeBasedPublishingEngine.getInstance().
                            handleActions(jParams, mode[0], engineMap, objectKey[0]);
                }
            }

            // save categories
            ManageCategories.getInstance().handleActions(jParams, mode[0],
                    engineMap,
                    new ContentContainerKey(theContainer.getID()),
                    theContainer.getDefinition(), true);

            objectKey[0] = null;
            if (theContainer.getID() > 0) {
                final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey[0] = contentContainer.getObjectKey();
            } else {
                final JahiaContainerDefinition def = theContainer.getDefinition();
                objectKey[0] = def.getObjectKey();
            }
            Metadata_Engine.getInstance().handleActions(jParams, mode[0], objectKey[0]);

            // fire event
            theEvent = new JahiaEvent(this, jParams, theContainer);
            ServicesRegistry.getInstance().getJahiaEventService().fireAddContainer(theEvent);

            // handled by previous event
            // ServicesRegistry.getInstance().getJahiaSearchService()
            //	.indexContainer(theContainer.getID(), jParams.getUser());

            // flag for subEngine: means that is a call from  AddContainer, reset the flag
            jParams.getSessionState().setAttribute("AddContainer", "false");
            logger.debug("Saving container !!");
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                });
//            } catch (Exception e) {
//                logger.error("Error during add operation of a new element we must flush all caches to ensure integrity between database and viewing");
//                ServicesRegistry.getInstance().getCacheService().flushAllCaches();
//                throw new JahiaException(e.getMessage(),e.getMessage(),
//                                         JahiaException.DATABASE_ERROR,JahiaException.CRITICAL_SEVERITY,e);
//            } finally{
            // #ifdef LOCK
            if (jParams.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
            // #endif
//            }

            if ("true".equals(jParams.getParameter("addnew"))) {
                List<Integer> ctnIds = (List<Integer>) jParams.getSessionState().getAttribute("getSorteredAndFilteredCtnIds" +
                        theContainer.getListID());
                if (ctnIds == null){
                    ctnIds = new ArrayList<Integer>();
                    jParams.getSessionState().setAttribute("getSorteredAndFilteredCtnIds" +
                        theContainer.getListID(),ctnIds);
                }
                ctnIds.add(new Integer(theContainer.getID()));
                logger.debug("ADDED TO CTNIDS :::::::::::::::::: " + theContainer.getID());
            }

        } else if (theScreen.equals("cancel")) {
            // #ifdef LOCK
            if (jParams.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
            // #endif
            // flag for subEngine: means that is a call from  AddContainer, reset the flag
            jParams.getSessionState().setAttribute("AddContainer", "false");
            jParams.getSessionState().removeAttribute("showNavigationInLockEngine");
            jParams.getSessionState().removeAttribute("needToRefreshParentPage");
        }
        return null;

    } // end processCurrentScreen

    private void setDefaultWorkflowMode(final JahiaContainer theContainer,
                                        final ProcessingContext jParams,
                                        final Map<String, Object> engineMap) throws JahiaException {
        int defaultMode = WorkflowService.LINKED;
        final Iterator<JahiaField> en = theContainer.getFields();
        while (en.hasNext()) {
            final JahiaField jahiaField = (JahiaField) en.next();
            if (jahiaField.getType() == FieldTypes.PAGE) {
                final Map<String, JahiaPageEngineTempBean> pageBeans = (Map<String, JahiaPageEngineTempBean>) jParams.getSessionState().getAttribute("Page_Field.PageBeans");
                if (pageBeans != null) {
                    final JahiaPageEngineTempBean pageBean = (JahiaPageEngineTempBean) pageBeans.get(jahiaField.getDefinition().getName());
                    if (pageBean != null && Page_Field.CREATE_PAGE.equals(pageBean.getOperation())) {
                        defaultMode = WorkflowService.INHERITED;
                    }
                }
            }
        }
        engineMap.put("defaultMode", Integer.valueOf(defaultMode));
    }

    private JahiaContainerList ensureContainerList(final JahiaContainer theContainer,
                                                   final Map<String, Object> engineMap,
                                                   final ProcessingContext jParams) throws JahiaException {
        final JahiaContainersService jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
        JahiaContainerList list = jahiaContainersService.loadContainerListInfo(theContainer.getListID());
        int containerParentID = 0;
        try {
            containerParentID = ((Integer) engineMap.get("containerParentID")).intValue();
        } catch (Exception e) {
            try {
                containerParentID = Integer.parseInt(jParams.getParameter("cparentid"));
                engineMap.put("containerParentID", Integer.valueOf(containerParentID));
            } catch (Exception ee) {
                logger.warn("Unable to set EngineMap value 'containerParentID'");
            }
        }
        final int id = jahiaContainersService.getContainerListID(theContainer.getDefinition().getName(), theContainer.getPageID(), containerParentID);
        if (list == null && id > 0) {
            list = jahiaContainersService.loadContainerListInfo(id);
            theContainer.setListID(list.getID());
        }
        if (list == null) {
            // list should already be created, keeps this just in case
            final int parentAclID;
            if (containerParentID != 0) {
                parentAclID = jahiaContainersService.loadContainerInfo(containerParentID,
                        jParams.getEntryLoadRequest()).getAclID();
            } else {
                parentAclID = jParams.getPage().getAclID();
            }
            list = new JahiaContainerList(0, containerParentID, theContainer.getPageID(), theContainer.getctndefid(), 0);
            jahiaContainersService.saveContainerListInfo(list, parentAclID, jParams);
            theContainer.setListID(list.getID());
        }
        return list;
    }

    private void saveFields(final JahiaContainer theContainer,
                            final JahiaContentContainerFacade jahiaContainerFacade,
                            final FieldsEditHelper feh,
                            final ProcessingContext jParams,
                            final int mode,
                            final Map<String, Object> engineMap) throws JahiaException {
        // Save fields
        final Iterator<JahiaContentFieldFacade> enu = jahiaContainerFacade.getFields();
        Set<String> editedLanguages = new HashSet<String>();
        for (Iterator<List<String>> iterator = feh.getUpdatedFields().values().iterator(); iterator.hasNext();) {
            List<String> list = iterator.next();
            editedLanguages.addAll(list);
        }
        while (enu.hasNext()) {
            final JahiaContentFieldFacade contentFieldFacade = (JahiaContentFieldFacade) enu.next();
            final Iterator<JahiaField> fields = contentFieldFacade.getFields();
            int newFieldID = 0;
            int newAclID = theContainer.getAclID();
            while (fields.hasNext()) {
                final JahiaField field = (JahiaField) fields.next();
                if (editedLanguages.contains(field.getLanguageCode()) || field.isShared()) {
                field.setID(newFieldID);
                field.setAclID(newAclID);
                field.setctnid(theContainer.getID());

                final EntryLoadRequest processingEntryLoadRequest =
                        new EntryLoadRequest(field.getWorkflowState(), field.getVersionID(), new ArrayList<Locale>());
                processingEntryLoadRequest.getLocales().add(LanguageCodeConverters.languageCodeToLocale(
                        field.getLanguageCode()));
                EntryLoadRequest savedEntryLoadRequest =
                    jParams.getSubstituteEntryLoadRequest();
                jParams.setSubstituteEntryLoadRequest(processingEntryLoadRequest);
                if (field.getID() == 0) {
                    // create the field only once
//                    ServicesRegistry.getInstance().getJahiaFieldService().
//                            saveField(field, theContainer.getAclID(), jParams);
                }
                EngineToolBox.getInstance().processFieldTypes(field, theContainer, ENGINE_NAME, jParams, mode, engineMap);
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
                if (newFieldID == 0) {
                    newFieldID = field.getID();
                }
                if (newAclID == 0) {
                    newAclID = field.getAclID();
                }
                }
            }
        }
    }

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map<String, Object> initEngineMap(final ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {

        EngineLanguageHelper elh;
        JahiaContainer theContainer;
        ContainerFieldsEditHelper feh = null;
        int lastFieldId = 0;
        String theScreen = jParams.getParameter("screen");

        logger.debug("Initializing engine map for screen [" + theScreen + "]");

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession( true );
        final SessionState theSession = jParams.getSessionState();
        final boolean isNavigation = theSession.getAttribute("Navigation") != null;

        // flag for subEngine: means that is a call from  AddContainer
        theSession.setAttribute("AddContainer", "true");

        // gets parent container id
        String parentIDStr = jParams.getParameter("cparentid");

        int parentID = 0;
        if (parentIDStr != null && parentIDStr.length() > 0) {
            try {
                parentID = Integer.parseInt(parentIDStr);
            } catch (NumberFormatException nfe) {
                StringBuffer buff = new StringBuffer();
                String errorMsg = buff.append("Error in parameters : parentid (").
                        append(parentIDStr).append(") cannot be converted in int").toString();
                throw new JahiaException("Error in parameters", errorMsg,
                        JahiaException.DATA_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }
        }

        final JahiaContentContainerFacade jahiaContentContainerFacade;
        final Map<String, Object> engineMap;
        if (theScreen != null && !isNavigation) {
            logger.debug("The Screen is not null, load it from session: " + theScreen);
            // if no, load the container value from the session
            engineMap = (Map<String, Object>) theSession.getAttribute("jahia_session_engineMap");

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
            jahiaContentContainerFacade = (JahiaContentContainerFacade)
                    engineMap.get("UpdateContainer_Engine.JahiaContentContainerFacade");
            final StringBuffer buff = new StringBuffer();
            feh = (ContainerFieldsEditHelper) engineMap.get(buff.append(ENGINE_NAME)
                    .append(".").append(FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID)
                    .toString());
        } else {
             logger.debug("the Screen is null load from storage, Navigation: " + isNavigation);
            // init engine map
            engineMap = new HashMap<String, Object>();
            org.jahia.engines.shared.Page_Field.resetPageBeanSession(jParams);
            // retrieve the choice ID if the screen is reloaded. Otherwise it should be set
            // to a default value, to avoid having it set to 3 (tree operation) if no source
            // page has been selected
            final String reload = jParams.getParameter("reload");
            logger.debug(" reload = " + reload);
            if (reload != null) {
                if (reload.equals("true")) {
                    final Map<String, Object> previousEngineMap = (Map<String, Object>) theSession.getAttribute("jahia_session_engineMap");
                    if (previousEngineMap != null) {
                        logger.debug(" previous session is not null");
                        final StringBuffer buff = new StringBuffer();
                        feh = (ContainerFieldsEditHelper) engineMap.get(
                                buff.append(ENGINE_NAME).append(".").append(
                                        FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID).toString());
                        if (feh != null) {
                            lastFieldId = feh.getSelectedFieldId();
                        }
                        feh = null;
                        theSession.removeAttribute("jahia_session_engineMap");
                    }
                }
            }

            jahiaContentContainerFacade = createJahiaContentContainerFacade(jParams);

            engineMap.put("UpdateContainer_Engine.JahiaContentContainerFacade", jahiaContentContainerFacade);

            theScreen = "edit";
            engineMap.put("containerParentID", Integer.valueOf(parentID));
            theSession.removeAttribute("Navigation");

            int contextualContainerListId = 0;
            try {
                final String contextualContainerListIDStr = jParams.getParameter("clistid");
                if (contextualContainerListIDStr != null){
                    contextualContainerListId = Integer.parseInt(contextualContainerListIDStr);
                }
            } catch ( Exception t ) {
            }
            engineMap.put("contextualContainerListId",new Integer(contextualContainerListId));

        }

        // Init Engine Language Helper
        elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if (elh == null) {
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER, elh);
        }
        elh.update(jParams);

        theContainer = jahiaContentContainerFacade.getContainer(elh.getCurrentEntryLoadRequest(), true);

        ensureContainerList(theContainer, engineMap, jParams);

        if (feh == null || isNavigation) {
            feh = new ContainerFieldsEditHelper(theContainer);
            // create the edit view
            final Map<Integer, Integer> ctnListFieldAcls = JahiaEngineTools.getCtnListFieldAclMap(theContainer, jParams);
            final Set<Integer> visibleFields = JahiaEngineTools.getCtnListVisibleFields(theContainer, jParams.getUser(), ctnListFieldAcls);
            final ContainerEditView editView = ContainerEditView.getInstance(theContainer, jParams, visibleFields);
            feh.setContainerEditView(editView);
            feh.setCtnListFieldAcls(ctnListFieldAcls);
            feh.setVisibleFields(visibleFields);
            feh.setSelectedFieldId(-1);
            final StringBuffer buff = new StringBuffer();
            engineMap.put(buff.append(ENGINE_NAME).append(".").append(FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID).toString(),
                    feh);
        }
        // Update FieldsEditHelper
        feh.setFieldForms(new HashMap<Integer, String>());
        feh.setStayOnSameField(false);
        final String lastScreen = jParams.getParameter("lastscreen");
        if ("edit".equals(lastScreen)) {
            // come from same engine
            feh.processRequest(jParams, lastFieldId);
        } else {
            // reset field
            feh.setSelectedFieldId(-1);
            feh.setLastFieldId(0);
        }

        final StringBuffer buff = new StringBuffer();
        engineMap.put(buff.append(AddContainer_Engine.ENGINE_NAME).append(".fieldForms").toString(), new HashMap());
        buff.delete(0, buff.length());

        engineMap.put("theContainer", theContainer);

        engineMap.put("noApply", EMPTY_STRING);
        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME,
                buff.append("?clistid=").append(theContainer.getListID()).
                        append("&cdefid=").append(theContainer.getctndefid()).
                        append("&cpid=").append(theContainer.getPageID()).
                        append("&cparentid=").append(parentID).toString()));
        theSession.setAttribute("jahia_session_engineMap", engineMap);

        // init map
        engineMap.put("screen", theScreen);
        if (!theScreen.equals("save") && !theScreen.equals("cancel")) {
            engineMap.put("jspSource", TEMPLATE_JSP);
        } else {
            engineMap.put("jspSource", "close");
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("Select_Page_Entry");
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("selectedPageOperation");
        }

        engineMap.put("enableCategories", Boolean.TRUE);
        engineMap.remove(JahiaEngine.ENGINE_VALIDATION_HELPER);

        // sets engineMap for JSPs
        jParams.setAttribute("engineTitle", JahiaResourceBundle
                .getJahiaInternalResource(
                        "org.jahia.engines.addcontainer.AddContainer.label",
                        elh.getCurrentLocale()));
        jParams.setAttribute("org.jahia.engines.EngineHashMap",
                engineMap);

        return engineMap;
    } // end initEngineMap

    private JahiaContentContainerFacade createJahiaContentContainerFacade(final ProcessingContext jParams)
            throws JahiaException {
        JahiaContentContainerFacade contentContainerFacade;

        final String listIDStr = jParams.getParameter("clistid");
        int listID = 0;
        final String defIDStr = jParams.getParameter("cdefid");
        int defID = 0;
        final String pageIDStr = jParams.getParameter("cpid");
        int pageID = 0;

        try {
            listID = Integer.parseInt(listIDStr);
            defID = Integer.parseInt(defIDStr);
            pageID = Integer.parseInt(pageIDStr);
        } catch (NumberFormatException nfe) {
            final StringBuffer buff = new StringBuffer();
            final String errorMsg = buff.append("Error in parameters : clistid (")
                    .append(listIDStr).append(") or cdefid (").append(defIDStr).
                    append(") or cpid (" + pageIDStr + ") cannot be converted in int").toString();
            throw new JahiaException("Error in parameters", errorMsg,
                    JahiaException.DATA_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }

        // source page
        final JahiaPage sourcePage = ServicesRegistry.getInstance().getJahiaPageService().lookupPage(pageID, jParams);
        if (sourcePage == null) {
            throw new JahiaException("Page missing",
                    "Trying to add a container on a non-existing page (" + pageID +
                            ")",
                    JahiaException.PAGE_ERROR, JahiaException.ERROR_SEVERITY);
        }

        contentContainerFacade = new JahiaContentContainerFacade(0,
                        sourcePage.getJahiaID(),
                        sourcePage.getID(),
                        listID,
                        defID,
                        //sourcePage.getAclID(), why the page acl, it's the acl, not the parent acl
                        0,
                        jParams,
                        jParams.getSite().getLanguageSettingsAsLocales(false));

        return contentContainerFacade;
    }

    /**
     * Check Fields validation
     *
     * @param jahiaContentContainerFacade ContainerFacadeInterface
     * @param jParams                     ProcessingContext
     * @param engineMap                   HashMap
     * @param elh
     * @return EngineValidationHelper
     * @throws JahiaException
     */
    public static EngineValidationHelper validate(
            final ContainerFacadeInterface jahiaContentContainerFacade,
            final ProcessingContext jParams,
            final Map<String, Object> engineMap,
            final FieldsEditHelper feh, EngineLanguageHelper elh)
            throws JahiaException {
        Locale currentLocale = null;
        EngineValidationHelper evh = feh.validate(ENGINE_NAME, jahiaContentContainerFacade, elh, jParams, engineMap);

        final Iterator<JahiaContentFieldFacade> enu = jahiaContentContainerFacade.getFields();
        final JahiaContainer theContainer =
                (JahiaContainer) engineMap.get("theContainer");
        final ContainerEditView editView = feh.getContainerEditView();
        JahiaContentFieldFacade cff;
        JahiaField field;
        final JahiaContainerDefinition definition = theContainer.getDefinition();
        final String containerBeanName = definition.getProperty("containerBeanName");
        final String validatorKey = definition.getProperty("validatorKey");

        if (validatorKey != null
                && validatorKey.length() > 0) {

            try {
                Class<?> theClass = null;
                if (containerBeanName != null && containerBeanName.length() > 0) {
                    try {
                        theClass = Class.forName(containerBeanName);
                    } catch (Exception e) {
                        logger.warn("Class file for specified containerBeanName (" + containerBeanName + ") is not found");
                    }
                }
                // define the types of the parameters
                final Class<?>[] theParams = {
                        org.jahia.data.containers.ContainerFacadeInterface.class,
                        org.jahia.params.ParamBean.class};

                final Object containerBean;
                if (theClass == null) {
                    containerBean = new DynaContainerValidatorBase(jahiaContentContainerFacade, jParams);
                } else if (!theClass.isInterface()) {
                    containerBean = theClass.getConstructor(theParams).newInstance(new Object[]{
                            jahiaContentContainerFacade,
                            jParams});
                } else {
                    containerBean = Proxy.newProxyInstance(theClass.getClassLoader(),
                            new Class[]{theClass}, new ContainerValidatorBase(jahiaContentContainerFacade, jParams));
                }    
                final ActionMessages errors = new ActionMessages();

                // Create a validator with the ValidateBean actions for the bean
                // we're interested in.
                final Validator validator = Resources.initValidator(validatorKey, containerBean,
                                            ((ParamBean) jParams).getContext(),
                                            ((ParamBean) jParams).getRequest(),
                                            errors, 1);

                // Run the validation actions against the bean.
                final ValidatorResults results = validator.validate();

                while (!results.isEmpty() && enu.hasNext()) {
                    cff = (JahiaContentFieldFacade) enu.next();
                    field = (JahiaField) cff.getFields().next();
                    final String name = field.getDefinition().getName();
                    if (editView.getFieldGroupByFieldName(name) != null) {

                        // Get the result of validating the property.
                        ValidatorResult vr = results.getValidatorResult(name);
                        if(vr==null) {
                            // maybe this field name has an id so we can not find it directly in the map
                            Iterator<String> iterator = results.getPropertyNames().iterator();
                            while (iterator.hasNext() && vr == null) {
                                String o = iterator.next();
                                String[] aliasNames = field.getDefinition().getAliasNames();
                                if (aliasNames == null || aliasNames.length == 0) {
                                    aliasNames = new String[]{name};
                                }
                                for (int i = 0; i < aliasNames.length; i++) {
                                    String aliasName = aliasNames[i];
                                    if(o.equals(aliasName)) {
                                        vr=results.getValidatorResult(o);
                                    }
                                }
                            }
                        }
                        // Get all the actions run against the property, and iterate over their names.
                        if (vr != null) {
                            final Map actionMap = vr.getActionMap();
                            final Iterator keys = actionMap.keySet().iterator();
                            while (keys.hasNext()) {
                                final String actName = (String) keys.next();

                                // Get the Action for that name.
                                final ValidatorAction action = Resources.
                                        getValidatorResources(((ParamBean) jParams).getContext(),
                                                ((ParamBean) jParams).getRequest()).
                                        getValidatorAction(actName);

                                //If the result failed, format the Action's message against the formatted field name
                                if (!vr.isValid(actName)) {
                                    if (currentLocale == null) {
                                        currentLocale = elh.getCurrentLocale() != null ? elh.getCurrentLocale() : jParams.getLocale();
                                    }
                                    final String msg = Resources.getMessage(Resources.
                                            getMessageResources(((ParamBean) jParams).getRequest()),
                                            currentLocale,
                                            action,
                                            vr.getField());


                                    final String displayName = field.getDefinition().getTitle(
                                            currentLocale);

                                    final String[] tokens = msg.split(" ");
                                    final StringBuffer buff = new StringBuffer();
                                    for (int i = 0; i < tokens.length; i++) {
                                        if (i == 0) {
                                            buff.append(displayName);
                                        } else {
                                            buff.append(tokens[i]);
                                        }
                                        buff.append(" ");
                                    }

                                    final ValidationError ve = new ValidationError(field, msg);
                                    evh.addError(ve);
                                }
                            }
                        }
                    }
                }
            } catch (Exception t) {
                logger.error("Error in validating :", t);
            }
        }
        return evh;
    }

    public static ContainerEditViewFieldGroup getFieldGroup(
            int fieldID,
            JahiaContainer theContainer,
            ContainerEditView editView)
            throws JahiaException {

        ContainerEditViewFieldGroup fieldGroup = null;

        JahiaField theField = theContainer.getField(fieldID);
        if (theField != null) {
            fieldGroup =
                    editView.getFieldGroupByFieldName(
                            theField.getDefinition().getName());
        }
        if (fieldGroup == null) {
            List<ContainerEditViewFieldGroup> views = new ArrayList<ContainerEditViewFieldGroup>(editView.getViews().values());
            if (views.size() > 0) {
                fieldGroup = views.get(0);
            }
        }
        return fieldGroup;
    }
}

