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
//  EV  10.01.20001
//  NK  06.02.2002 Added Multiple fields edit at once support
//  NK	05.02.2002 Version 2.0 Multiple field edit + customisable field ( read/write permission check )
//

package org.jahia.engines.updatecontainer;

import org.apache.commons.lang.StringUtils;
import org.jahia.content.*;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.*;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.*;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.engines.addcontainer.AddContainer_Engine;
import org.jahia.engines.categories.ManageCategories;
import org.jahia.engines.importexport.ManageImportExport;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine;
import org.jahia.engines.validation.EngineValidationHelper;
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
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;


/**
 * Display the popup that let the user update a container.
 *
 * @author EV
 * @author NK
 * @author XL
 * @version 2.0
 */
public class UpdateContainer_Engine implements JahiaEngine {

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(UpdateContainer_Engine.class);

    public static final String TEMPLATE_JSP = "update_container";
    public static final String ENGINE_NAME = "updatecontainer";

    private TransactionTemplate transactionTemplate = null;

    /**
     * Default constructor, creates a new <code>UpdateContainer_Engine</code> instance.
     */
    public UpdateContainer_Engine() {
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender(final ProcessingContext jParams) {
        return EngineToolBox.getInstance().authoriseRender(jParams);
    } // end authoriseRender

    /**
     * renderLink
     */
    public String renderLink(final ProcessingContext jParams, final Object theObj)
            throws JahiaException {
        final ContentContainer contentContainer = (ContentContainer) theObj;
        final String params = "?mode=display&cid=" + contentContainer.getID();
        String useOriginPage = jParams.getParameter("use_container_origin_page");
        if (useOriginPage != null && (Boolean.valueOf(useOriginPage)).booleanValue()) {
            Properties extraParams = new Properties();
            extraParams.put(ParamBean.PAGE_ID_PARAMETER, Integer.toString(contentContainer.getPageID()));
            return jParams.composeEngineUrl(ENGINE_NAME, extraParams, params);
        } else {
            return jParams.composeEngineUrl(ENGINE_NAME, params);
        }
    } // end renderLink

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData(final ProcessingContext jParams) {
        return false;
    } // end needsJahiaData

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException,
            JahiaForbiddenAccessException {
        jParams.getSessionState().setAttribute("showNavigationInLockEngine", "true");
        return handleActions(jParams, jData, null, null);
    }

    protected EngineValidationHelper handleActions(final ProcessingContext jParams,
                                                   final JahiaData jData,
                                                   final String cid,
                                                   final String screen)
            throws JahiaException, JahiaForbiddenAccessException {
        // initalizes the hashmap
        jParams.getSessionState().removeAttribute("FireContainerUpdated");
        final Map engineMap = initEngineMap(jParams, cid, screen);

        Integer contextualContainerListId = (Integer)engineMap.get("contextualContainerListId");
        if (contextualContainerListId == null){
            contextualContainerListId = new Integer(0);
        }

        // get the screen
        final String theScreen = (String) engineMap.get("screen");

        // checks if the user has the right to display the engine
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
        final JahiaUser user = jParams.getUser();

        // does the current user have permission for the current engine ?
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.update",
                        jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) <= 0) {
            throw new JahiaForbiddenAccessException();
        }

        // if the user has admin rights give him automaticaly also the write
        // rights in order to avoid two recursions in the rights checking.
        if (theContainer.checkAdminAccess(user)) {
            if (theContainer.getContentContainer().getPickedObject() == null) {
                engineMap.put("enableAuthoring", Boolean.TRUE);
                engineMap.put("enableMetadata", Boolean.TRUE);
                engineMap.put("enableImport", Boolean.TRUE);
                engineMap.put("enableExport", Boolean.TRUE);
                engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
                // temporary disable versioning for containers with field of type 'Page'
                engineMap.put("enableVersioning", Boolean.valueOf(!hasPageField(theContainer)));
            }
            engineMap.put("adminAccess", Boolean.TRUE);
            engineMap.put("enableRightView", Boolean.TRUE);
            engineMap.put("writeAccess", Boolean.TRUE);
            engineMap.put("enableAdvancedWorkflow", Boolean.TRUE);
        } else if (theContainer.checkWriteAccess(user)) {
            if (theContainer.getContentContainer().getPickedObject() == null) {
                engineMap.put("enableAuthoring", Boolean.TRUE);
                engineMap.put("enableMetadata", Boolean.TRUE);
                engineMap.put("enableImport", Boolean.TRUE);
                engineMap.put("enableExport", Boolean.TRUE);
                engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
                // temporary disable versioning for containers with field of type 'Page'
                engineMap.put("enableVersioning", Boolean.valueOf(!hasPageField(theContainer)));
            }
            engineMap.put("writeAccess", Boolean.TRUE);
        }

        final String navigation = jParams.getParameter("navigation");
        final boolean isNavigation = navigation != null && navigation.length() > 0;
        if (isNavigation) {
            jParams.getSessionState().setAttribute("needToRefreshParentPage", "true");
        } else jParams.getSessionState().removeAttribute("needToRefreshParentPage");
        if (engineMap.get("writeAccess") != null) {
            // #ifdef LOCK
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            if (jParams.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, theContainer.getID());
                EngineValidationHelper evh = null;
                if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                        jParams.getSessionState().getMaxInactiveInterval())) {

                    engineMap.put("lock", lockKey);

                    // fire event
                    final JahiaEvent theEvent = new JahiaEvent(this, jParams, theContainer);
                    ServicesRegistry.getInstance().getJahiaEventService().fireUpdateContainerEngineAfterInit(theEvent);
                    // end fire event

                    if (!theScreen.equals("cancel")) {
                        evh = processLastScreen(jParams, engineMap);
                    }
                    if (evh != null && evh.hasErrors()) {
                        engineMap.put("screen", evh.getNextScreen());
                        engineMap.put("jspSource", TEMPLATE_JSP);
                    } else {
                        processCurrentScreen(jParams, engineMap);
                    }
                    // #ifdef LOCK
                } else {
                    // Prerequisites are NOT completed ! Damned ! Redirect the JSP
                    // output to lock informations.
                    final Map m = lockRegistry.getLocksOnObject(lockKey);
                    if (! m.isEmpty()) {
                        final String action = (String) m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
                    }

                    if (theScreen.equals("apply")) {
                        if (isNavigation) {
                            if ("new".equals(navigation)) {
                                //return null;
                            } else {
                                final String newCID;
                                if ("first".equals(navigation)) {
                                    newCID = jParams.getParameter("first");
                                } else if ("previous".equals(navigation)) {
                                    newCID = jParams.getParameter("previous");
                                } else if ("next".equals(navigation)) {
                                    newCID = jParams.getParameter("next");
                                } else if ("last".equals(navigation)) {
                                    newCID = jParams.getParameter("last");
                                } else {
                                    throw new IllegalArgumentException("Validation param is unknow: " + navigation);
                                }
                                String newEngineURL = jParams.composeEngineUrl(ENGINE_NAME, "?cid=" + newCID);
                                newEngineURL += "&contextualContainerListId="
                                        + String.valueOf(contextualContainerListId);
                                engineMap.put(ENGINE_URL_PARAM, newEngineURL);

                                final SessionState theSession = jParams.getSessionState();
                                theSession.setAttribute("Navigation", "Navigation");

                                logger.debug("reloading engineMap and handleActions for navigation");
                                handleActions(jParams, jData, newCID, "edit");
                                return null;
                            }
                        }
                    }

                    if (! "new".equals(navigation)) {
                        processCurrentScreen(jParams, engineMap);
                    }
                }
            }
            // #endif
        } else {
            throw new JahiaForbiddenAccessException();
        }

        // displays the screen
        if (theScreen.equals("apply")) {
            if (isNavigation) {
                final SessionState theSession = jParams.getSessionState();
                theSession.setAttribute("Navigation", "Navigation");
                if (jParams.settings().areLocksActivated()) {
                    final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, theContainer.getID());
                    final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
                    lockRegistry.release(lockKey, user, user.getUserKey());
                }
                final String newEngineURL;
                if ("new".equals(navigation)) {
                    final AddContainer_Engine theEngine = (AddContainer_Engine) EnginesRegistry.getInstance().getEngineByBeanName("addContainerEngine");
                    EntryLoadRequest loadVersion = EntryLoadRequest.CURRENT;
                    if (ServicesRegistry.getInstance().getJahiaVersionService().isStagingEnabled(theContainer.getJahiaID())) {
                        loadVersion = EntryLoadRequest.STAGED;
                    }
                    final JahiaContainerList theList = ServicesRegistry.getInstance().getJahiaContainersService().
                            loadContainerListInfo(theContainer.getListID(), loadVersion);
                    final String link = theEngine.renderLink(jParams, theList);
                    final String params = link.substring(link.indexOf("?") + 1);
                    final StringTokenizer tokenizer = new StringTokenizer(params, "&");
                    jParams.getParameterMap().clear();
                    while (tokenizer.hasMoreTokens()) {
                        final String token = tokenizer.nextToken();
                        final int index = token.indexOf("=");
                        jParams.setParameter(token.substring(0, index), token.substring(index + 1, token.length()));
                    }
                    jParams.setParameter("contextUtlaContainerListId",String.valueOf(contextualContainerListId));
                    theEngine.handleActions(jParams, jData);
                    return null;
                } else {
                    final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                    final StringBuffer buff = new StringBuffer();
                    if ("first".equals(navigation)) {
                        buff.append("?cid=").append(jParams.getParameter("first")).append("&" +
                                EngineLanguageHelper.ENGINE_LANG_PARAM + "=").append(elh.getCurrentLanguageCode())
                                .append("&contextualContainerListId=").append(String.valueOf(contextualContainerListId));
                        newEngineURL = jParams.composeEngineUrl(ENGINE_NAME, buff.toString());
                    } else if ("previous".equals(navigation)) {
                        buff.append("?cid=").append(jParams.getParameter("previous")).append("&" +
                                EngineLanguageHelper.ENGINE_LANG_PARAM + "=").append(elh.getCurrentLanguageCode())
                                .append("&contextualContainerListId=").append(String.valueOf(contextualContainerListId));
                        newEngineURL = jParams.composeEngineUrl(ENGINE_NAME, buff.toString());
                    } else if ("next".equals(navigation)) {
                        buff.append("?cid=").append(jParams.getParameter("next")).append("&" +
                                EngineLanguageHelper.ENGINE_LANG_PARAM + "=").append(elh.getCurrentLanguageCode())
                                .append("&contextualContainerListId=").append(String.valueOf(contextualContainerListId));
                        newEngineURL = jParams.composeEngineUrl(ENGINE_NAME, buff.toString());
                    } else if ("last".equals(navigation)) {
                        buff.append("?cid=").append(jParams.getParameter("last")).append("&" +
                                EngineLanguageHelper.ENGINE_LANG_PARAM + "=").append(elh.getCurrentLanguageCode())
                                .append("&contextualContainerListId=").append(String.valueOf(contextualContainerListId));
                        newEngineURL = jParams.composeEngineUrl(ENGINE_NAME, buff.toString());
                    } else {
                        throw new IllegalArgumentException("Validation param is unknow: " + navigation);
                    }
                    engineMap.put(ENGINE_URL_PARAM, newEngineURL);
                }
            }
        }

        EngineToolBox.getInstance().displayScreen(jParams, engineMap);
        return null;
    }

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
    public EngineValidationHelper processLastScreen(final ProcessingContext jParams, Map engineMap)
            throws JahiaException, JahiaForbiddenAccessException {

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter("lastscreen");
        logger.debug("processLastScreen: " + lastScreen + " (UPDATE_MODE)");

        if (lastScreen == null) {
            //lastScreen = "edit";
            lastScreen = EMPTY_STRING;
        }
        // indicates to sub engines that we are processing last screen
        final int mode = JahiaEngine.UPDATE_MODE;

        final JahiaContentContainerFacade jahiaContentContainerFacade = (JahiaContentContainerFacade)
                engineMap.get("UpdateContainer_Engine.JahiaContentContainerFacade");
        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        engineMap.put(JahiaEngine.PROCESSING_LOCALE, elh.getPreviousLocale());

        EngineValidationHelper evh = null;

        JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(elh.getPreviousEntryLoadRequest(), true);
        engineMap.put("theContainer", theContainer);

        Set allProcessedScreen = (Set) engineMap.get("allProcessedScreen");
        if (allProcessedScreen == null) {
            allProcessedScreen = new HashSet();
            engineMap.put("allProcessedScreen", allProcessedScreen);
        }
        allProcessedScreen.add(lastScreen);
        
        if (lastScreen.equals("edit")) {

            final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(
                    AddContainer_Engine.ENGINE_NAME + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

            if (feh.getLastFieldId() == 0) {
                // stop processing because of undefined field
                return null;
            }
            try {
                if (!feh.processLastFields(AddContainer_Engine.ENGINE_NAME,
                        jahiaContentContainerFacade,
                        elh, jParams, engineMap, mode)) {
                    // if there was an error, come back to last screen
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    logger.debug("processLastFields returned false, setting the screen to: " + lastScreen);
                }
            } catch (Exception e) {
                logger.debug(e, e);
            }
        } else if (lastScreen.equals("versioning")) {
            engineMap.put(RENDER_TYPE_PARAM,
                    new Integer(JahiaEngine.RENDERTYPE_FORWARD));
            // reset engine map to default value
            engineMap.remove(ENGINE_OUTPUT_FILE_PARAM);
        } else if (lastScreen.equals("rightsMgmt")) {
            if (engineMap.get("adminAccess") != null) {
                evh = ManageRights.getInstance().
                        handleActions(jParams, mode, engineMap, theContainer.getAclID(), null, null, theContainer.getContentContainer().isAclSameAsParent(),theContainer.getContentContainer().getObjectKey().toString());

                if (evh != null && evh.hasErrors()) {
                    // if there was an error, come back to last screen
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    logger.debug("handleActions returned false, setting the screen to: " + lastScreen);
                }
                evh = null;

            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("timeBasedPublishing")) {
            if (engineMap.get("writeAccess") != null || engineMap.get("adminAccess") != null) {
                ObjectKey objectKey;
                theContainer = (JahiaContainer) engineMap.get("theContainer");
                final ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey = contentContainer.getObjectKey();
                boolean result = TimeBasedPublishingEngine.getInstance().handleActions(jParams, mode, engineMap, objectKey);
                if ( !result ){
                    evh = (EngineValidationHelper)engineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".EngineValidationError");
                }
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("metadata")) {
            final ObjectKey objectKey;
            theContainer = (JahiaContainer) engineMap.get("theContainer");

            if (theContainer.getID() > 0) {
                ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                objectKey = contentContainer.getObjectKey();
            } else {
                JahiaContainerDefinition def = theContainer.getDefinition();
                objectKey = def.getObjectKey();
            }
            evh = Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);

        } else if (lastScreen.equals("categories")) {
            theContainer = (JahiaContainer) engineMap.get("theContainer");
            ManageCategories.getInstance().handleActions(jParams, mode,
                    engineMap,
                    new ContentContainerKey(jahiaContentContainerFacade.getContainerID()),
                    theContainer.getDefinition(), false);
        } else if (lastScreen.equals("workflow")) {
            if (engineMap.get("adminAccess") != null) {
                theContainer = (JahiaContainer) engineMap.get("theContainer");
                ManageWorkflow.getInstance().handleActions(jParams, mode, engineMap, theContainer.getContentContainer());
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
    public EngineValidationHelper processCurrentScreen(final ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {

        final JahiaContentContainerFacade jahiaContentContainerFacade = (JahiaContentContainerFacade)
                engineMap.get("UpdateContainer_Engine.JahiaContentContainerFacade");
        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(elh.getCurrentEntryLoadRequest(), true);
        engineMap.put("theContainer", theContainer);

        // gets the current screen
        // screen   = edit, rights, logs
        final String theScreen = (String) engineMap.get("screen");
        logger.debug("processCurrentScreen: " + theScreen + " LOAD_MODE");

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, theContainer.getID());
        final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
        // #endif

        final JahiaUser user = jParams.getUser();
        if (theScreen.equals("edit")) {
            FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(
                    AddContainer_Engine.ENGINE_NAME + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

            if (feh.getStayOnSameField()) {
                feh.setSelectedFieldId(feh.getLastFieldId());
            }
            try {
                feh.processCurrentFields(AddContainer_Engine.ENGINE_NAME, jahiaContentContainerFacade, elh,
                        jParams, engineMap, mode);
            } catch (Exception e) {
                logger.debug(e, e);
            }

        } else if (theScreen.equals("logs")) {
            EngineToolBox.getInstance().loadLogData(jParams,
                    LoggingEventListener.CONTAINER_TYPE, engineMap);

        } else {
            boolean sameAcl = theContainer.getContentContainer().isAclSameAsParent();
            if (theScreen.equals("rightsMgmt")) {
                if (engineMap.get("adminAccess") != null) {
                    ManageRights.getInstance().handleActions(jParams, mode,
                            engineMap, theContainer.getAclID(), null, null, sameAcl,theContainer.getContentContainer().getObjectKey().toString());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("timeBasedPublishing")) {
                if (engineMap.get("writeAccess") != null
                        || engineMap.get("adminAccess") != null) {
                    ObjectKey objectKey;
                    theContainer = (JahiaContainer) engineMap.get("theContainer");
                    ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                    objectKey = contentContainer.getObjectKey();
                    TimeBasedPublishingEngine.getInstance().
                            handleActions(jParams, mode, engineMap, objectKey);
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("metadata")) {
                final ObjectKey objectKey;
                if (theContainer.getID() > 0) {
                    ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                    objectKey = contentContainer.getObjectKey();
                } else {
                    JahiaContainerDefinition def = theContainer.getDefinition();
                    objectKey = def.getObjectKey();
                }
                Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);

            } else if (theScreen.equals("versioning")) {

                String goTo = jParams.getParameter("method");
                if (goTo == null || goTo.length() == 0) {
                    goTo = "showRevisionsList";
                }
                logger.debug("Going to: " + goTo);

                final Properties params = new Properties();
                params.put("method", goTo);
                params.put("objectKey", new ContentContainerKey(theContainer.getID()).toString());
                final String versioningURL = jParams.composeStrutsUrl(
                        "ContainerVersioning", params, null);
                engineMap.put(RENDER_TYPE_PARAM,
                        new Integer(JahiaEngine.RENDERTYPE_FORWARD));
                engineMap.put(JahiaEngine.ENGINE_REDIRECT_URL, versioningURL);
                engineMap.put(ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.REDIRECT_JSP);
            } else if (theScreen.equals("categories")) {
                ManageCategories.getInstance().handleActions(jParams, mode,
                        engineMap,
                        new ContentContainerKey(jahiaContentContainerFacade.getContainerID()),
                        theContainer.getDefinition(), false);
            } else if (theScreen.equals("workflow")) {
                final boolean isReadOnly = LockPrerequisites.getInstance().
                        getLockPrerequisitesResult((LockKey) engineMap.get("LockKey")) != null;
                if (engineMap.get("adminAccess") != null || isReadOnly) {
                    ManageWorkflow.getInstance().handleActions(jParams, mode,
                            engineMap, theContainer.getContentContainer());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("import") || theScreen.equals("export")) {
                ManageImportExport.getInstance().handleActions(jParams, mode,
                        engineMap, theContainer.getContentContainer());
            } else if (theScreen.equals("save") || theScreen.equals("apply")) {
                Set allProcessedScreen = (Set) engineMap.get("allProcessedScreen");
                final String navigation = jParams.getParameter("navigation");
                final boolean isNavigation = navigation != null && navigation.length() > 0;
                final String lastScreen = jParams.getParameter("lastscreen");
                final LockKey futureStolenkey = (LockKey) engineMap.get("LockKey");
                if ((!isNavigation) &&
                        LockPrerequisites.getInstance().getLockPrerequisitesResult(futureStolenkey) != null) {
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

                // #ifdef LOCK
                // Did somebody steal the lock ? Panpan cucul !
                if (jParams.settings().areLocksActivated() &&
                        lockRegistry.isStealedInContext(lockKey, user, user.getUserKey())) {
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", "apply");
                    return null;
                }
                // #endif

                if (allProcessedScreen.contains("import") && ProcessingContext.isMultipartRequest(((ParamBean) jParams).getRequest())) {
                    if (ManageImportExport.getInstance().handleActions(jParams, JahiaEngine.SAVE_MODE,
                            engineMap, theContainer.getContentContainer())) {
                        if (theScreen.equals("apply")) {
                            ManageImportExport.getInstance().handleActions(jParams, JahiaEngine.LOAD_MODE,
                                    engineMap, theContainer.getContentContainer());
                        }
                    }
                }

                final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(AddContainer_Engine.ENGINE_NAME + "."
                        + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

                // try to save but there are some error when processing previous field
                if (feh.getStayOnSameField()) {
                    feh.setSelectedFieldId(feh.getLastFieldId());
                    feh.processCurrentFields(AddContainer_Engine.ENGINE_NAME,
                            jahiaContentContainerFacade, elh,
                            jParams, engineMap, JahiaEngine.LOAD_MODE);
                    return null;
                }

                mode = JahiaEngine.VALIDATE_MODE;

                EngineValidationHelper evh = null;

                if (allProcessedScreen.contains("rightsMgmt")) {
                    evh = ManageRights.getInstance().handleActions(jParams, mode, engineMap,
                            theContainer.getAclID(), null, null, sameAcl,theContainer.getContentContainer().getObjectKey().toString());

                    if (evh != null && evh.hasErrors()) {
                        engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                        engineMap.put("screen", "rightsMgmt");
                        engineMap.put("jspSource", TEMPLATE_JSP);
                        return evh;
                    }
                }

                evh = AddContainer_Engine.validate(
                        jahiaContentContainerFacade,
                        jParams,
                        engineMap, feh, elh);

                engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                //JahiaEvent validationEvent = new JahiaEvent (this, jParams, theContainer);
                //ServicesRegistry.getInstance ().getJahiaEventService ().fireContainerValidation(validationEvent);

                if (evh != null && evh.hasErrors()) {

                    final ContainerEditViewFieldGroup currentFieldGroup =
                            AddContainer_Engine.getFieldGroup(
                                    feh.getSelectedFieldId(),
                                    theContainer,
                                    feh.getContainerEditView());

                    Collections.sort(evh.getErrors(), new ValidationErrorSorter(currentFieldGroup));
                    if (!feh.getStayOnSameField()) {
                        feh.setSelectedFieldId(((JahiaField) (evh.getFirstError()).getSource()).getID());
                    }
                    feh.processCurrentFields(AddContainer_Engine.ENGINE_NAME,
                            jahiaContentContainerFacade, elh, jParams, engineMap,
                            JahiaEngine.LOAD_MODE);

                    // prepare view
                    engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                    engineMap.put("screen", "edit");
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    return evh;
                }

                // metadata validation before save
                final ObjectKey[] objectKey = new ObjectKey[]{null};
                if (theContainer.getID() > 0) {
                    ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                    objectKey[0] = contentContainer.getObjectKey();
                } else {
                    JahiaContainerDefinition def = theContainer.getDefinition();
                    objectKey[0] = def.getObjectKey();
                }

                if (allProcessedScreen.contains("metadata")) {
                    evh = Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey[0]);
                    if (evh != null && evh.hasErrors()) {
                        engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                        engineMap.put("screen", evh.getNextScreen());
                        engineMap.put("jspSource", TEMPLATE_JSP);
                        return evh;
                    }
                }

                engineMap.remove(JahiaEngine.ENGINE_VALIDATION_HELPER);
                mode = JahiaEngine.SAVE_MODE;

                // fire event
                final JahiaEvent[] theEvent = new JahiaEvent[]{new JahiaEvent(this, jParams, theContainer)};
                ServicesRegistry.getInstance().getJahiaEventService().
                        fireUpdateContainerEngineBeforeSave(theEvent[0]);
                // end fire event

                // save the container info
                if (transactionTemplate == null) {
                    SpringContextSingleton contextInstance = SpringContextSingleton.getInstance();
                    if (contextInstance.isInitialized()) {
                        PlatformTransactionManager manager = (PlatformTransactionManager) contextInstance.getContext().getBean("transactionManager");
                        transactionTemplate = new TransactionTemplate(manager);
                    }
                }
                try {
    //                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
    //                    protected void doInTransactionWithoutResult(TransactionStatus status) {
    //                        try {
                    final EntryLoadRequest loadVersion;
                    if (ServicesRegistry.getInstance().getJahiaVersionService().
                            isStagingEnabled(theContainer.getJahiaID())) {
                        loadVersion = EntryLoadRequest.STAGED;

                    } else {
                        loadVersion = EntryLoadRequest.CURRENT;
                    }

                    final JahiaContainerList theList = ServicesRegistry.getInstance().getJahiaContainersService().
                            loadContainerListInfo(theContainer.getListID(), loadVersion);

                    if (allProcessedScreen.contains("workflow")) {
                        // save workflow
                        if (!ManageWorkflow.getInstance().handleActions(jParams, mode,
                                engineMap, theContainer.getContentContainer())) {
                            engineMap.put("screen", "workflow");
                            engineMap.put("jspSource", TEMPLATE_JSP);
                            return null;
                        }
                    }

                    // 0 for parentAclID in saveContainerInfo, because container already exists
                    //  -> container already has an aclID
                    //  -> no need to create a new one
                    ServicesRegistry.getInstance().getJahiaContainersService().saveContainerInfo(theContainer,
                            theList.getParentEntryID(), 0, jParams);

                    saveFields(theContainer, jahiaContentContainerFacade, feh, jParams, mode, engineMap);

                    // save rights
                    if (allProcessedScreen.contains("rightsMgmt") && engineMap.get("adminAccess") != null) {
                        engineMap.put("logObjectType", Integer.toString(LoggingEventListener.CONTAINER_TYPE));
                        engineMap.put("logObject", theContainer);
                        //ViewRights.getInstance().handleActions( jParams, mode, engineMap, theContainer.getAclID() );
                        ManageRights.getInstance().handleActions(jParams, mode,
                                engineMap, theContainer.getAclID(), null, null, sameAcl,theContainer.getContentContainer().getObjectKey().toString());
                        if (sameAcl) {
                            JahiaBaseACL acl = (JahiaBaseACL) engineMap.get(ManageRights.NEW_ACL+"_"+theContainer.getContentContainer().getObjectKey());
                            if (acl != null) {
                                theContainer.getContentContainer().updateAclForChildren(acl.getID());
                            }
                        }

                        if (Boolean.TRUE.equals(engineMap.get("rightsUpdated"))) {
                            theContainer.getContentContainer().setUnversionedChanged();
                        }

                        // todo FIXME we need to add code here that flushes the cache
                        // of sub-pages if this container contains one or more page
                        // fields. The entire page sub-structure must be flushed for
                        // the users/groups that were added since rights are
                        // inherited. If rights have been "cut", we can stop at the
                        // cut points though.
                    }

                    if (allProcessedScreen.contains("timeBasedPublishing")) {
                        if ((engineMap.get("writeAccess") != null || engineMap.get("adminAccess") != null)) {
                            objectKey[0] = null;
                            ContentContainer contentContainer = ContentContainer.getContainer(theContainer.getID());
                            objectKey[0] = contentContainer.getObjectKey();
                            TimeBasedPublishingEngine.getInstance().handleActions(jParams, mode, engineMap, objectKey[0]);

                            if (Boolean.TRUE.equals(engineMap.get("tbpUpdated"))) {
                                theContainer.getContentContainer().setUnversionedChanged();
                            }
                        } else {
                            throw new JahiaForbiddenAccessException();
                        }
                    }

                    if (allProcessedScreen.contains("categories")) {
                        // save categories
                        ManageCategories.getInstance().handleActions(jParams, mode, engineMap,
                                new ContentContainerKey(jahiaContentContainerFacade.getContainerID()),
                                theContainer.getDefinition(), false);
                    }

                    // save metadata
                    if (allProcessedScreen.contains("metadata")) {
                        Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey[0]);
                    }

                    // Only fire the event if at least 1 field in the container has been updated
                    if (jParams.getSessionState().getAttribute("FireContainerUpdated") != null) {
                        // fire event
                        theEvent[0] = new JahiaEvent(this, jParams, theContainer);
                        ServicesRegistry.getInstance().getJahiaEventService().fireUpdateContainer(theEvent[0]);
                        logger.debug("Changes applied and saved !");
                    } else {
                        logger.debug("The Container has not changed !");
                    }

                    //handled by previous event
                    //ServicesRegistry.getInstance().getJahiaSearchService()
                    //	.indexContainer(theContainer1.getID(), jParams.getUser());

                    // flag for subEngine: means that is a call from  updateContainer, reset the flag
                    jParams.getSessionState().setAttribute("UpdateContainer", "false");
    //                        } catch (Exception e) {
    //                            throw new RuntimeException(e);
    //                        }
    //                    }
    //                });
                } catch (Exception e) {
                    logger.error("Error during update operation of an element we must flush all caches to ensure integrity between database and viewing");
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    throw new JahiaException(e.getMessage(), e.getMessage(),
                            JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
                } finally {
                    if (theScreen.equals("apply")) {
                        engineMap.put("prevScreenIsApply", Boolean.TRUE);
                        engineMap.put("screen", lastScreen);
                    }
                    // #ifdef LOCK
                    else {
                        if (jParams.settings().areLocksActivated()) {
                            lockRegistry.release(lockKey, user, user.getUserKey());
                        }
                    }
                    // #endif
                }
            } else if (theScreen.equals("cancel")) {

                mode = JahiaEngine.CANCEL_MODE;
                ManageRights.getInstance().handleActions(jParams, mode, engineMap, theContainer.getAclID(), null, null, sameAcl,theContainer.getContentContainer().getObjectKey().toString());
                ManageWorkflow.getInstance().handleActions(jParams, mode, engineMap, theContainer.getContentContainer());

                // #ifdef LOCK
                if (jParams.settings().areLocksActivated()) {
                    lockRegistry.release(lockKey, user, user.getUserKey());
                }
                // #endif
                // flag for subEngine: means that is a call from  updateContainer, reset the flag
                jParams.getSessionState().setAttribute("UpdateContainer", "false");
                jParams.getSessionState().removeAttribute("showNavigationInLockEngine");
                jParams.getSessionState().removeAttribute("needToRefreshParentPage");
            }
        }
        return null;
    } // end processCurrentScreen

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap(final ProcessingContext jParams, final String forcedCid, final String screen)
            throws JahiaException,
            JahiaSessionExpirationException {

        logger.debug("Start initEngineMap");
        EngineLanguageHelper elh;
        Locale previousLocale = null;
        ContainerFieldsEditHelper feh = null;
        int lastFieldId = 0;

        // flag for subEngine: means that is a call from  updateContainer
        final SessionState theSession = jParams.getSessionState();
        theSession.setAttribute("UpdateContainer", "true");
        final boolean isNavigation = theSession.getAttribute("Navigation") != null;

        String theScreen;
        if (screen != null && screen.length() > 0) {
            theScreen = screen;
        } else {
            theScreen = jParams.getParameter("screen");
        }

        final String ctnidStr;
        if (forcedCid != null && forcedCid.length() > 0) {
            ctnidStr = forcedCid;
        } else {
            ctnidStr = jParams.getParameter("cid");
        }
        int ctnid;

        try {
            ctnid = Integer.parseInt(ctnidStr);
        } catch (NumberFormatException nfe) {
            throw new JahiaException("Error in parameters", "Error in parameters : cid (" + ctnidStr +
                    ") cannot be converted in int",
                    JahiaException.DATA_ERROR,
                    JahiaException.CRITICAL_SEVERITY, nfe);
        }

        // gets session values
        Map engineMap = (Map) theSession.getAttribute("jahia_session_engineMap");
        boolean prevScreenIsApply = false;

        if (isNavigation || (engineMap != null && theScreen != null)) {
            Boolean prevScreenIsApplyBool = (Boolean) engineMap.get("prevScreenIsApply");
            prevScreenIsApply = (prevScreenIsApplyBool != null && prevScreenIsApplyBool.booleanValue());
            if (prevScreenIsApply || isNavigation) {
                elh = (EngineLanguageHelper) engineMap
                        .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if (elh != null) {
                    previousLocale = elh.getPreviousLocale();
                }
                feh = (ContainerFieldsEditHelper) engineMap.get(AddContainer_Engine.ENGINE_NAME + "."
                        + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
                if (feh != null) {
                    lastFieldId = feh.getSelectedFieldId();
                }
                feh = null;
            }
        }

        JahiaContentContainerFacade jahiaContentContainerFacade;
        final ContentContainer container = ContentContainer.getContainer(ctnid);
        if (theScreen != null && !isNavigation) {

            logger.debug("The Screen is not null, load it from session: " + theScreen);

            // if no, load the container value from the session
            engineMap = (Map) theSession.getAttribute("jahia_session_engineMap");

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

            feh = (ContainerFieldsEditHelper) engineMap.get(AddContainer_Engine.
                    ENGINE_NAME + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);

            if (prevScreenIsApply && theScreen.equals("edit")) {
                // reinit jahiaContentContainerFacade
                List<Locale> localeList = jParams.getSite(). getLanguageSettingsAsLocales(false);

                jahiaContentContainerFacade = new JahiaContentContainerFacade(ctnid,
                        LoadFlags.ALL,
                        jParams,
                        localeList,
                        true);

                engineMap.put("UpdateContainer_Engine.JahiaContentContainerFacade",
                        jahiaContentContainerFacade);

                // reset session
                engineMap.remove(AddContainer_Engine.ENGINE_NAME + "." + "updated.fields");
                engineMap.remove("prevScreenIsApply");

                org.jahia.engines.shared.Page_Field.resetPageBeanSession(jParams);
                // reset Fields Edit Helper
                feh = null;
            }

        } else {
            logger.debug("the Screen is null load from storage, Navigation: " + isNavigation);

            // init engine map
            engineMap = new HashMap();

            // reset session
            org.jahia.engines.shared.Page_Field.resetPageBeanSession(jParams);

            // init the JahiaContentFieldFacade
            final List<Locale> localeList = jParams.getSite().getLanguageSettingsAsLocales(false);

            jahiaContentContainerFacade = new JahiaContentContainerFacade(container.getJahiaContainer(jParams,
                    jParams.getEntryLoadRequest()),
                    jParams.getPage(),
                    LoadFlags.ALL,
                    jParams,
                    localeList,
                    true, true, false);

            engineMap.put("UpdateContainer_Engine.JahiaContentContainerFacade", jahiaContentContainerFacade);
            theSession.removeAttribute("Navigation");

            int contextualContainerListId = 0;
            try {
                final String contextualContainerListIDStr = jParams.getParameter("contextualContainerListId");
                if (contextualContainerListIDStr != null){
                    contextualContainerListId = Integer.parseInt(contextualContainerListIDStr);
                }
            } catch ( Exception t ) {
            }
            engineMap.put("contextualContainerListId",new Integer(contextualContainerListId));
        }
        if (theScreen == null || theScreen.equals(EMPTY_STRING)) {
            final String gotoscreen = jParams.getParameter("gotoscreen");
            if (gotoscreen != null && gotoscreen.length() > 0) {
                theScreen = gotoscreen;
            } else {
                theScreen = "edit";
            }
        }

        if (container.getPickedObject() != null && "edit".equals(theScreen)) {
            theScreen = "rightsMgmt";
        }

        elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if (elh == null) {
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER, elh);
        }
        elh.update(jParams);
        if (previousLocale != null) {
            elh = new EngineLanguageHelper(previousLocale);
        }

        final JahiaContainer theContainer = jahiaContentContainerFacade.getContainer(elh.getCurrentEntryLoadRequest(), true);
        engineMap.put("theContainer", theContainer);

        if (feh == null) {
            feh = new ContainerFieldsEditHelper(theContainer);
            // create the edit view
            final Map ctnListFieldAcls = JahiaEngineTools.getCtnListFieldAclMap(theContainer, jParams);
            Set visibleFields = JahiaEngineTools.getCtnListVisibleFields(theContainer, jParams.getUser(), ctnListFieldAcls);
            final ContainerEditView editView = ContainerEditView.getInstance(theContainer, jParams, visibleFields);
            feh.setContainerEditView(editView);
            feh.setCtnListFieldAcls(ctnListFieldAcls);
            feh.setVisibleFields(visibleFields);
            // check, if we should focus a particular field
            int fieldId = 0;
            String fieldIdStr = jParams.getParameter("fid");
            if (StringUtils.isNotEmpty(fieldIdStr)) {
                try {
                    fieldId = Integer.parseInt(fieldIdStr);
                } catch (NumberFormatException e) {
                    // ignore it and show the first field in the engine
                }
            }
            feh.setSelectedFieldId(fieldId);
            engineMap.put(AddContainer_Engine.ENGINE_NAME + "." + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID, feh);
        }
        // Update FieldsEditHelper
        feh.setFieldForms(new HashMap());
        feh.setStayOnSameField(false);
        if (theScreen.equals(engineMap.get("screen"))) {
            feh.processRequest(jParams, lastFieldId);
        } else {
            try {
                int fieldId = Integer.parseInt(jParams.getParameter("lastfid"));
                feh.setLastFieldId(fieldId);
            } catch (final NumberFormatException e) {
                logger.debug(e);
                logger.debug("NumberFormatException: Parameter lastfid cannot be converted to int... Not setting lastFiledID in the Helper");
            }
        }
        // remember the updated fields ( Apply Change to all lang options )
        Set updatedFields = (Set) engineMap.get(AddContainer_Engine.ENGINE_NAME + "." + "updated.fields");
        if (updatedFields == null) {
            updatedFields = new HashSet();
        }
        engineMap.put(AddContainer_Engine.ENGINE_NAME + "." + "updated.fields", updatedFields);

        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, "?cid=" + theContainer.getID()));
        theSession.setAttribute("jahia_session_engineMap", engineMap);

        // sets screen
        engineMap.put("screen", theScreen);

        if (theScreen.equals("save")) {
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("Select_Page_Entry");
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("selectedPageOperation");
            engineMap.put("jspSource", "close");

        } else if (theScreen.equals("apply")) {
            engineMap.put("jspSource", "apply");

        } else if (theScreen.equals("cancel")) {
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("Select_Page_Entry");
            ((ParamBean) jParams).getRequest().getSession().removeAttribute("selectedPageOperation");
            engineMap.put("jspSource", "close");

        } else {
            engineMap.put("jspSource", TEMPLATE_JSP);
        }

        if (container.getPickedObject() == null) {
            engineMap.put("enableCategories", Boolean.TRUE);
        }

        /** This contextual container list Id attribute is used to position container list pagination to the last edited
         * container
         */
        Integer contextualContainerListId = (Integer)engineMap.get("contextualContainerListId");
        if (contextualContainerListId != null && contextualContainerListId.intValue() != 0){
            theSession.setAttribute("ContextualContainerList_" + String.valueOf(contextualContainerListId), 
                    new Integer(ctnid));
        }

        // sets engineMap for JSPs
        engineMap.put(AddContainer_Engine.ENGINE_NAME + "." + "fieldForms", new HashMap());
        jParams.setAttribute("engineTitle", JahiaResourceBundle
                .getEngineResource(
                        "org.jahia.engines.updatecontainer.UpdateContainer_Engine.updateContainer.label",
                        jParams, elh.getCurrentLocale()));
        jParams.setAttribute("org.jahia.engines.EngineHashMap",
                engineMap);

        return engineMap;
    }


    private boolean saveFields(
            final JahiaContainer theContainer,
            final JahiaContentContainerFacade jahiaContainerFacade,
            final FieldsEditHelper feh,
            final ProcessingContext jParams,
            final int mode,
            final Map engineMap) throws JahiaException {
        final Iterator contentFieldFacadeEnum = jahiaContainerFacade.getFields();
        boolean changed = false;
        Set editedLanguages = new HashSet();
        for (Iterator iterator = feh.getUpdatedFields().values().iterator(); iterator.hasNext();) {
            List list = (List) iterator.next();
            editedLanguages.addAll(list);
        }
        while (contentFieldFacadeEnum.hasNext()) {
            final JahiaContentFieldFacade contentFieldFacade = (JahiaContentFieldFacade) contentFieldFacadeEnum.next();
            final Iterator fields = contentFieldFacade.getFields();
            EntryLoadRequest processingEntryLoadRequest;
            int newFieldID = 0;
            int newAclID = 0;
            while (fields.hasNext()) {
                JahiaField field = (JahiaField) fields.next();
                if ((field.hasChanged() && feh.containsUpdatedField(field.getID(), field.getLanguageCode())) || editedLanguages.contains(field.getLanguageCode())) {

                    changed = true;

                    if (field.getID() < 0) {
                        field.setID(newFieldID);
                        if (newAclID != 0) {
                            field.setAclID(newAclID);
                        }
                    }

                    // save the active entry only if the staging doesn't exists.
                    boolean processField = true;
                    processingEntryLoadRequest =
                            new EntryLoadRequest(field.getWorkflowState(),
                                    field.getVersionID(),
                                    new ArrayList());
                    processingEntryLoadRequest.getLocales().
                            add(LanguageCodeConverters.languageCodeToLocale(field.getLanguageCode()));

                    if (field.getWorkflowState() ==
                            EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        final List entryLocales = new ArrayList();
                        entryLocales.add(LanguageCodeConverters.languageCodeToLocale(field.
                                getLanguageCode()));
                        final EntryLoadRequest stagingLoadRequest =
                                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, entryLocales);

                        processField = (contentFieldFacade.getField(
                                stagingLoadRequest, false) == null);
                        if (processField) {
                            processingEntryLoadRequest = stagingLoadRequest;
                        }
                    }
                    if (processField) {
                        if (field.getID() == 0) {
                            JahiaSaveVersion saveVersion = ServicesRegistry.getInstance().
                                    getJahiaVersionService().getSiteSaveVersion(jParams.getJahiaID());
                            Object o = field.getObject();
                            field = ServicesRegistry.getInstance().
                                    getJahiaFieldService().
                                    createJahiaField(0, field.getJahiaID(), field.getPageID(),
                                            field.getctnid(), field.getFieldDefID(),
                                            field.getType(), field.getConnectType(),
                                            field.getValue(), field.getRank(), field.getAclID(),
                                            saveVersion.getVersionID(),
                                            saveVersion.getWorkflowState(),
                                            field.getLanguageCode());
                        }
                        engineMap.put("theField", field);
                        EntryLoadRequest savedEntryLoadRequest =
                            jParams.getSubstituteEntryLoadRequest();
                        jParams.setSubstituteEntryLoadRequest(
                                processingEntryLoadRequest);
                        EngineToolBox.getInstance().processFieldTypes(field,
                                theContainer, AddContainer_Engine.ENGINE_NAME, jParams, mode, engineMap);
                        jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
                    }
                    if (newFieldID == 0 && field.getID() > 0) {
                        newFieldID = field.getID();
                    }
                    if (newAclID == 0 && field.getAclID() > 0) {
                        newAclID = field.getAclID();
                    }
                }
            }
        }
        return changed;
    }
    
    private static boolean hasPageField(JahiaContainer container) throws JahiaException {
        for (Iterator<JahiaContainerStructure> structureIterator = container.getDefinition().getStructure(); structureIterator.hasNext();) {
            JahiaContainerStructure field = (JahiaContainerStructure) ((Iterator) structureIterator).next();
            final Object def = field.getObjectDef();
            if (def instanceof JahiaFieldDefinition) {
                JahiaFieldDefinition fieldDefinition = (JahiaFieldDefinition) def;
                if (fieldDefinition.getType() == FieldTypes.PAGE) {
                    return true;
                }
            }
        }
        return false;
    }
}
