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
// $Id$
//

//
//  EV  10.02.20001
//

package org.jahia.engines.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.categories.ManageCategories;
import org.jahia.engines.importexport.ManageImportExport;
import org.jahia.engines.lock.LockEngine;
import org.jahia.engines.metadata.Metadata_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.shared.JahiaPageEngineTempBean;
import org.jahia.engines.timebasedpublishing.TimeBasedPublishingEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.workflow.ManageWorkflow;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.JahiaFieldDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.services.cache.CacheService;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.views.engines.versioning.actions.ContentVersioningAction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2002</p> <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class PageProperties_Engine implements JahiaEngine {

    /**
     * logging
     */
    private static final transient Logger logger = Logger.getLogger(PageProperties_Engine.class);

    private static final String TEMPLATE_JSP = "page_properties";
    public static final String ENGINE_NAME = "pageproperties";
    private EngineToolBox toolBox;

    // private static final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private LockService lockService;
    private CacheService cacheService;
    private TransactionTemplate transactionTemplate;

    /**
     * Default constructor, creates a new <code>PageProperties_Engine</code> instance.
     */
    public PageProperties_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    public LockService getLockService() {
        return lockService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender(final ProcessingContext jParams) {
        return toolBox.authoriseRender(jParams);
    }

    /**
     * renderLink
     */
    public String renderLink(final ProcessingContext jParams, final Object theObj)
            throws JahiaException {
        return jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING + "?mode=display");
    }

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData(final ProcessingContext jParams) {
        return true;
    }

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException, JahiaForbiddenAccessException {
        final JahiaUser user = jParams.getUser();
        EngineValidationHelper evh = null;

        // initalizes the hashmap
        final Map engineMap = initEngineMap(jParams, jData);

        // checks if the user has the right to display the engine
        final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
        // get the screen
        final String theScreen = (String) engineMap.get("screen");

        // does the current user have permission for the current engine ?
        if (ServicesRegistry.getInstance().getJahiaACLManagerService().
                getSiteActionPermission("engines.actions.update",
                        jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) <= 0) {
            throw new JahiaForbiddenAccessException();
        }

        if (thePage.checkAdminAccess(user)) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put("enableMetadata", Boolean.TRUE);
            engineMap.put("adminAccess", Boolean.TRUE);
            engineMap.put("enableRightView", Boolean.TRUE);
            engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
            engineMap.put("writeAccess", Boolean.TRUE);
            // temporary disable versioning in Page Settings
            engineMap.put("enableVersioning", Boolean.FALSE);
            engineMap.put("enableImport", Boolean.TRUE);
            engineMap.put("enableExport", Boolean.TRUE);
        } else if (thePage.checkWriteAccess(user)) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put("enableMetadata", Boolean.TRUE);
            engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
            engineMap.put("enableImport", Boolean.TRUE);
            engineMap.put("enableExport", Boolean.TRUE);
            engineMap.put("writeAccess", Boolean.TRUE);
            // temporary disable versioning in Page Settings
            engineMap.put("enableVersioning", Boolean.FALSE);
        }

        if (engineMap.get("writeAccess") != null) {

            // #ifdef LOCK
            if (jParams.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, thePage.getID());
                if (lockService.acquire(lockKey, user,
                        user.getUserKey(),
                        jParams.getSessionState().getMaxInactiveInterval())) {

                    engineMap.put("lock", lockKey);
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
                    //LockEngine.getInstance().redirect(jParams, engineMap, lockKey);
                    final Map m = LockRegistry.getInstance().getLocksOnObject(lockKey);
                    if (!m.isEmpty()) {
                        final String action = (String) m.keySet().iterator().next();
                        engineMap.put("LockKey", LockKey.composeLockKey(lockKey.getObjectKey(), action));
                    } else {
                        final LockPrerequisitesResult results = LockPrerequisites.getInstance().
                                getLockPrerequisitesResult(lockKey);
                        engineMap.put("LockKey", results.getFirstLockKey());
                    }
                    processCurrentScreen(jParams, engineMap);
                }
            }
            // #endif

        } else {
            throw new JahiaForbiddenAccessException();
        }

        // displays the screen
        toolBox.displayScreen(jData.getProcessingContext(), engineMap);

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
    public EngineValidationHelper processLastScreen(final ProcessingContext jParams, final Map engineMap)
            throws JahiaException, JahiaForbiddenAccessException {

        // Sets the page
        final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
        // gets the last screen
        // lastscreen   = edit, rights, logs
        final String lastScreen = jParams.getParameter("lastscreen");
        logger.debug("processLastScreen: " + lastScreen + " UPDATE_MODE");

        if (lastScreen == null) {
            return null;
            //lastScreen = "edit";
        }

        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.
                get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        // indicates to sub engines that we are processing last screen
        final int mode = JahiaEngine.UPDATE_MODE;

        // dispatches to the appropriate sub engine
        if (lastScreen.equals("edit")) {
            final String screen = (String) engineMap.get("screen");
            if (!"cancel".equals(screen)) {
                if (!updatePageData(jParams, engineMap, elh.getPreviousLanguageCode())) {
                    // if there was an error, come back to last screen, unless we clicked on cancel
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", TEMPLATE_JSP);
                }
            }
        } else if (lastScreen.equals("versioning")) {
            engineMap.put(RENDER_TYPE_PARAM,
                    new Integer(JahiaEngine.RENDERTYPE_FORWARD));
            // reset engine map to default value
            engineMap.remove(ENGINE_OUTPUT_FILE_PARAM);

            releaseTreeVersioningLocks(jParams);

            // now let's reacquire the page lock for this page, as we
            // have cleared it as part of the page subtree.
            if (jParams.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, thePage.getID());
                final JahiaUser user = jParams.getUser();
                if (lockService.acquire(lockKey, user,
                        user.getUserKey(),
                        jParams.getSessionState().
                                getMaxInactiveInterval())) {
                    engineMap.put("lock", lockKey);
                } else {
                    // Prerequisites are NOT completed ! Damned ! Redirect the JSP
                    // output to lock informations.
                    ((LockEngine)EnginesRegistry.getInstance().getEngine("lock")).redirect(jParams, engineMap,
                            lockKey);
                }
            }
            // #endif
        } else if (lastScreen.equals("rightsMgmt")) {
            ContentPage theContentPage = ContentPage.getPage(thePage.getID());
            if (engineMap.get("adminAccess") != null) {
                final EngineValidationHelper evh = ManageRights.getInstance().
                        handleActions(jParams, mode, engineMap, theContentPage.getAclID(), null, null, theContentPage.isAclSameAsParent(), thePage.getContentPage().getObjectKey().toString());

                if (evh != null && evh.hasErrors()) {
                    // if there was an error, come back to last screen
                    engineMap.put("screen", lastScreen);
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    logger.debug("handleActions returned false, setting the screen to: " + lastScreen);
                }

            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("timeBasedPublishing")) {
            if (engineMap.get("writeAccess") != null || engineMap.get("adminAccess") != null) {
                boolean result = TimeBasedPublishingEngine.getInstance().
                        handleActions(jParams, mode, engineMap, ContentPage.getPage(thePage.getID()).getObjectKey());
                if (!result) {
                    final EngineValidationHelper evh = (EngineValidationHelper) engineMap.get(TimeBasedPublishingEngine.ENGINE_NAME + ".EngineValidationError");
                    if (evh != null && evh.hasErrors()) {
                        //if there was an error, come back to last screen
                        engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                        engineMap.put("screen", "timeBasedPublishing");
                        engineMap.put("jspSource", TEMPLATE_JSP);
                        logger.debug("handleActions returned false, setting the screen to: " + lastScreen);
                        return evh;
                    }
                }
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("categories")) {
            ManageCategories.getInstance().handleActions(jParams, mode,
                    engineMap, new ContentPageKey(thePage.getID()),
                    thePage.getPageTemplate(), false);
        } else if (lastScreen.equals("workflow")) {
            if (engineMap.get("adminAccess") != null) {
                ManageWorkflow.getInstance().handleActions(jParams, mode,
                        engineMap, jParams.getContentPage());
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals("metadata")) {
            final ObjectKey objectKey = ContentPage.getPage(thePage.getID()).getObjectKey();
            Metadata_Engine.getInstance().handleActions(jParams, mode, objectKey);
        }


        if ("save".equals(jParams.getParameter("screen"))) {
            ContentPage theContentPage = ContentPage.getPage(thePage.getID());
            final EngineValidationHelper evh = ManageRights.getInstance().handleActions(
                    jParams, JahiaEngine.VALIDATE_MODE, engineMap, theContentPage.getAclID(), null, null, theContentPage.isAclSameAsParent(), thePage.getContentPage().getObjectKey().toString());

            if (evh != null && evh.hasErrors()) {
                // if there was an error, come back to last screen
                engineMap.put("screen", "rightsMgmt");
                engineMap.put("jspSource", TEMPLATE_JSP);
                logger.debug("handleActions returned false, setting the screen to: " + lastScreen);
            }
        }
        return null;
    }

    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public EngineValidationHelper processCurrentScreen(final ProcessingContext jParams, final Map engineMap)
            throws JahiaException, JahiaForbiddenAccessException {

        // Sets the actual field
        final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");

        final JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");

//        EntryLoadRequest entryLoadRequest
//                = (EntryLoadRequest) engineMap.get ("entryLoadRequest");

        // gets the current screen
        // screen   = edit, rights, logs
        final String theScreen = (String) engineMap.get("screen");
        logger.debug("processCurrentScreen: " + theScreen + " LOAD_MODE");

        // indicates to sub enginesthat we are processing last screen
        final int[] mode = new int[]{JahiaEngine.LOAD_MODE};

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, thePage.getID());
        // #endif

        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);

        // dispatches to the appropriate sub engine
        final JahiaUser user = jParams.getUser();
        if (theScreen.equals("edit")) {
            loadPageData(jParams, engineMap);
        } else if (theScreen.equals("logs")) {
            toolBox.loadLogData(jParams, LoggingEventListener.PAGE_TYPE, engineMap);
        } else {
            ContentPage theContentPage = ContentPage.getPage(thePage.getID());
            boolean sameAcl = theContentPage.isAclSameAsParent();
            if (theScreen.equals("rightsMgmt")) {
                if (engineMap.get("adminAccess") != null) {
                    ManageRights.getInstance().handleActions(jParams, mode[0], engineMap, theContentPage.getAclID(), null, null,
                            sameAcl, thePage.getContentPage().getObjectKey().toString());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("timeBasedPublishing")) {
                if (engineMap.get("writeAccess") != null || engineMap.get("adminAccess") != null) {
                    TimeBasedPublishingEngine.getInstance().handleActions(
                            jParams, mode[0], engineMap, ContentPage.getPage(thePage.getID()).getObjectKey());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("versioning")) {
                // we don't need to free the lock when going to the versioning
                // engine because we can re-acquire a lock we already have.

                String goTo = jParams.getParameter("method");
                if (goTo == null || goTo.length() == 0) {
                    goTo = "showOperationChoices";
                }
                logger.debug("Going to: " + goTo);

                final Properties params = new Properties();
                params.put("method", goTo);
                params.put("objectKey", new ContentPageKey(thePage.getID()).toString());
                final String versioningURL = jParams.composeStrutsUrl(
                        "PagesVersioning", params, null);
                engineMap.put(RENDER_TYPE_PARAM,
                        new Integer(JahiaEngine.RENDERTYPE_FORWARD));
                engineMap.put(JahiaEngine.ENGINE_REDIRECT_URL, versioningURL);
                engineMap.put(ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.REDIRECT_JSP);
            } else if (theScreen.equals("categories")) {
                ManageCategories.getInstance().handleActions(jParams, mode[0],
                        engineMap, new ContentPageKey(thePage.getID()),
                        thePage.getPageTemplate(), false);
            } else if (theScreen.equals("workflow")) {
                if (engineMap.get("adminAccess") != null) {
                    ManageWorkflow.getInstance().handleActions(jParams, mode[0],
                            engineMap, jParams.getContentPage());
                } else {
                    throw new JahiaForbiddenAccessException();
                }
            } else if (theScreen.equals("import") || theScreen.equals("export")) {
                ManageImportExport.getInstance().handleActions(jParams, mode[0],
                        engineMap, jParams.getContentPage());
            } else if (theScreen.equals("metadata")) {
                final ObjectKey objectKey = ContentPage.getPage(thePage.getID()).getObjectKey();
                Metadata_Engine.getInstance().handleActions(jParams, mode[0], objectKey);
            } else if (theScreen.equals("save") || theScreen.equals("apply")) {
                if (transactionTemplate == null) {
                    SpringContextSingleton contextInstance = SpringContextSingleton.getInstance();
                    if (contextInstance.isInitialized()) {
                        PlatformTransactionManager manager = (PlatformTransactionManager) contextInstance.getContext().getBean("transactionManager");
                        transactionTemplate = new TransactionTemplate(manager);
                    }
                }

                final LockKey futureStolenkey = (LockKey) engineMap.get("LockKey");
                if (LockPrerequisites.getInstance().getLockPrerequisitesResult(futureStolenkey) != null) {
                    final String param = jParams.getParameter("whichKeyToSteal");
                    if (param != null && param.length() > 0) {
                        final LockService lockRegistry = lockService;
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
                    processCurrentScreen(jParams, engineMap);
                    return null;
                }

                // #ifdef LOCK
                // Did somebody steal the lock ? Panpan cucul !
                if (jParams.settings().areLocksActivated() &&
                        lockService.isStealedInContext(lockKey, user, user.getUserKey())) {
                    engineMap.put("screen", jParams.getParameter("lastscreen"));
                    engineMap.put("jspSource", "apply");
                    return null;
                }
                // #endif

                if (ProcessingContext.isMultipartRequest(((ParamBean) jParams).getRequest())) {
                    if (ManageImportExport.getInstance().handleActions(jParams, JahiaEngine.SAVE_MODE,
                            engineMap, jParams.getContentPage())) {
                        // #ifdef LOCK
                        if (jParams.settings().areLocksActivated()) {
                            lockService.release(lockKey, user, user.getUserKey());
                        }
                        // #endif
                        return null;
                    }
                }

                mode[0] = JahiaEngine.VALIDATE_MODE;
                final EngineValidationHelper evh = ManageRights.getInstance().
                        handleActions(jParams, mode[0], engineMap, theContentPage.getAclID(), null, null,
                                sameAcl, thePage.getContentPage().getObjectKey().toString());

                if (evh != null && evh.hasErrors()) {
                    toEngineMessages(evh).saveMessages("manageRights.warning.", ((ParamBean) jParams).getRequest());
                    logger.debug("messages saved to request");
                    engineMap.put(JahiaEngine.ENGINE_VALIDATION_HELPER, evh);
                    engineMap.put("screen", "rightsMgmt");
                    engineMap.put("jspSource", TEMPLATE_JSP);
                    return evh;
                }
                try {
                    //                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    //                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                    //                        try {
                    mode[0] = JahiaEngine.SAVE_MODE;

                    // create one entry for each language
                    final ContentPage contentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(
                            thePage.getID(), false);

                    // save workflow
                    if (!ManageWorkflow.getInstance().handleActions(jParams, mode[0],
                            engineMap, contentPage)) {
                        engineMap.put("screen", "workflow");
                        engineMap.put("jspSource", TEMPLATE_JSP);
                        return null;
                    }

                    //Map languageStates = contentPage.getLanguagesStates (false);

                    Set updatedLanguageEntries = (Set) engineMap.get(
                            "updatedLanguageEntries");
                    if (updatedLanguageEntries == null) {
                        updatedLanguageEntries = new HashSet();
                    }
                    if (!updatePageData(jParams, engineMap, elh.getCurrentLanguageCode())) {
                        // if there was an error, come back to last screen
                        String lastScreen = jParams.getParameter("lastscreen");
                        if (lastScreen == null) {
                            lastScreen = "edit";
                        }
                        engineMap.put("screen", lastScreen);
                        // already set
                        //engineMap.put( "jspSource", TEMPLATE_JSP );
                    }

                    boolean changed = thePage.setTitles(updatedLanguageEntries, pageTempBean.getTitles());

                    //String  pageTitle       = (String)engineMap.get ("dataPageTitle");
                    //Boolean validate = (Boolean) engineMap.get ("validate");

                    //thePage.setTitle (pageTitle);
                    changed |= thePage.setPageTemplateID(pageTempBean.getPageTemplateID());

                    final String pageKey = (String) engineMap.get("dataPageURLKey");

                    String oldPageKey = thePage.getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                    changed |= (oldPageKey == null && (pageKey != null && pageKey.length() > 0)) || (oldPageKey != null && (pageKey == null
                            || !pageKey.equals(oldPageKey)));
                    if (pageKey != null && pageKey.length() > 0) {
                        thePage.getContentPage().setPageKey(pageKey);
                        engineMap.put(PageProperty.PAGE_URL_KEY_PROPNAME, pageKey);
                        engineMap.put("dataPageURLKey", pageKey);
                    } else if (oldPageKey != null) {
                        thePage.getContentPage().setPageKey(null);
                        engineMap.put(PageProperty.PAGE_URL_KEY_PROPNAME, pageKey);
                    }
                    if (changed) {
                        thePage.getContentPage().setUnversionedChanged();
                    }

                    logger.debug("changed: " + changed);

                    if (changed) {
                        thePage.commitChanges(true, user);

                        // let's flush the sitemap to make sure the changes are updated
                        // everywhere.
                        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();

                        final JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                        ServicesRegistry.getInstance().getJahiaEventService()
                                .fireContentObjectUpdated(objectCreatedEvent);

                        /* handled by previous event
                        // index page
                        ServicesRegistry.getInstance().getJahiaSearchService()
                                .indexPage(thePage.getID(), jParams.getUser());

                        int pageFieldID = ServicesRegistry.getInstance()
                        .getJahiaPageService().getPageFieldID(contentPage.
                                getID());
                        if (pageFieldID != -1) {
                            try {
                                ContentField field = ContentField.getField(pageFieldID);
                                if ( field.getContainerID() > 0 ){
                                    ServicesRegistry.getInstance()
                                    .getJahiaSearchService().indexContainer(field.getContainerID(), jParams.getUser());
                                }
                            } catch ( Exception t ){
                            }
                        }*/

                        engineMap.put("dataPageTitle", pageTempBean.getTitle(elh.getCurrentLanguageCode()));
                    }

                    // save rights
                    if (engineMap.get("adminAccess") != null) {
                        engineMap.put("logObjectType",
                                Integer.toString(LoggingEventListener.PAGE_TYPE));
                        engineMap.put("logObject", thePage);
                        //ViewRights.getInstance().handleActions (jParams, mode, engineMap, thePage.getAclID());
                        ManageRights.getInstance().handleActions(jParams, mode[0],
                                engineMap, theContentPage.getAclID(), null, null, sameAcl, thePage.getContentPage().getObjectKey().toString());
                        if (sameAcl) {
                            JahiaBaseACL acl = (JahiaBaseACL) engineMap.get(ManageRights.NEW_ACL + "_" + theContentPage.getObjectKey());
                            if (acl != null) {
                                theContentPage.updateAclForChildren(acl.getID());
                            }
                        }

                        if (Boolean.TRUE.equals(engineMap.get("rightsUpdated"))) {
                            thePage.getContentPage().setUnversionedChanged();
                        }
                    }

                    // save timebasedpublishing engine
                    if (engineMap.get("writeAccess") != null
                            || engineMap.get("adminAccess") != null) {
                        TimeBasedPublishingEngine.getInstance().
                                handleActions(jParams, mode[0], engineMap, ContentPage.getPage(thePage.getID()).getObjectKey());
                        if (Boolean.TRUE.equals(engineMap.get("tbpUpdated"))) {
                            thePage.getContentPage().setUnversionedChanged();
                        }
                    }

                    // save categories
                    changed |= ManageCategories.getInstance().handleActions(jParams, mode[0],
                            engineMap,
                            new ContentPageKey(thePage.getID()), thePage.getPageTemplate(), false);

                    // save metadata
                    if (changed) {
                        final ObjectKey objectKey = ContentPage.getPage(thePage.getID()).getObjectKey();
                        Metadata_Engine.getInstance().handleActions(jParams, mode[0], objectKey);
                    }

                    if ("true".equals(jParams.getSessionState()
                            .getAttribute("FireContainerUpdated"))) {
                        final JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                        ServicesRegistry.getInstance().getJahiaEventService()
                                .fireContentObjectUpdated(objectCreatedEvent);
                    }
                    //                        } catch (Exception e) {
                    //                            throw new RuntimeException(e);
                    //                        }
                    //                    }
                    //                });
                } catch (Exception e) {
                    logger.error("Error during add operation of a new element we must flush all caches to ensure integrity between database and viewing");
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                    throw new JahiaException(e.getMessage(), e.getMessage(),
                            JahiaException.DATABASE_ERROR, JahiaException.CRITICAL_SEVERITY, e);
                } finally {
                    final String lastScreen = jParams.getParameter("lastscreen");
                    if (lastScreen != null) {
                        logger.debug("lastScreen=" + lastScreen);
                        if (lastScreen.equals("edit")) {
                            // fire event
                            final JahiaEvent theEvent = new JahiaEvent(this, jParams, thePage);
                            ServicesRegistry.getInstance().getJahiaEventService().fireSetPageProperties(theEvent);
                        }
                    }
                    if (theScreen.equals("apply")) {
                        engineMap.put("screen", lastScreen);
                        // if urlkey has been changed than engine url has to be updated too
                        String engineUrl = jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING);
                        logger.debug("Engine url: " + engineUrl);
                        engineMap.put(ENGINE_URL_PARAM, engineUrl);
                    }
                    // #ifdef LOCK
                    else {
                        if (jParams.settings().areLocksActivated()) {
                            lockService.release(lockKey, user, user.getUserKey());
                        }
                    }
                }
                // #endif
            } else if (theScreen.equals("cancel")) {
                // #ifdef LOCK

                mode[0] = JahiaEngine.CANCEL_MODE;
                ManageRights.getInstance().
                        handleActions(jParams, mode[0], engineMap, theContentPage.getAclID(), null, null);
                ManageWorkflow.getInstance().handleActions(jParams, mode[0], engineMap, thePage.getContentPage());

                if (jParams.settings().areLocksActivated()) {
                    lockService.release(lockKey, user, user.getUserKey());
                }
                // #endif
            }
        }
        return null;
    }


    /**
     * inits the engine map
     *
     * @param jData
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap(final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException, JahiaSessionExpirationException {

        String theScreen = jParams.getParameter("screen");

        // gets session values
        final SessionState theSession = jParams.getSessionState();
        if (theSession == null)
            throw new JahiaSessionExpirationException();

        final ContentPage contentPage = ServicesRegistry.getInstance().getJahiaPageService().
                lookupContentPage(jData.getProcessingContext().getPage().getID(), false);

        JahiaPageEngineTempBean pageTempBean;

        Map engineMap;
        if (theScreen != null) {
            // if no, load the container value from the session
            engineMap = (Map) theSession.getAttribute("jahia_session_engineMap");
            //thePage     = (JahiaPage) engineMap.get( "thePage" );

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

        } else {
            // initalizes the hashmap
            jParams.getSessionState().removeAttribute("FireContainerUpdated");

            JahiaPage thePage = jParams.getPage();
            final String gotoscreen = jParams.getParameter("gotoscreen");
            if (gotoscreen != null && gotoscreen.length() > 0) {
                theScreen = gotoscreen;
            } else {
                theScreen = "edit";
            }

            // init engine map
            engineMap = new HashMap();

            engineMap.put("thePage", thePage);
            pageTempBean = loadPageBeanFromRealPage(thePage);
            engineMap.put("pageTempBean", pageTempBean);
            engineMap.put("dataPageTemplateID", new Integer(thePage.getPageTemplateID()));
            final PageProperty urlKeyProperty = thePage.getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);

            logger.debug("initEngineMap - urlKeyProperty: " + urlKeyProperty);

            if (urlKeyProperty != null) {
                engineMap.put("dataPageURLKey", urlKeyProperty.getValue());
            } else {
                engineMap.put("dataPageURLKey", EMPTY_STRING);
            }
            engineMap.put("validate", Boolean.FALSE);

        }
        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
        String engineUrl = jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING);
        logger.debug("Engine url: " + engineUrl);
        engineMap.put(ENGINE_URL_PARAM, engineUrl);

        // reset engine map to default value
        engineMap.remove(ENGINE_OUTPUT_FILE_PARAM);

        // Init Engine Language Helper
        EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if (elh == null) {
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER, elh);
        }
        elh.update(jParams);

        engineMap.put("contentPage", contentPage);

        pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");

        // load page title for the processing language Code
        EntryLoadRequest savedEntryLoadRequest =
                jParams.getSubstituteEntryLoadRequest();
        jParams.setSubstituteEntryLoadRequest(elh.getCurrentEntryLoadRequest());
        final String pageTitle = contentPage.getTitle(jParams.getEntryLoadRequest());
        if (pageTempBean.getTitle(elh.getCurrentLanguageCode()) == null && (pageTitle != null)) {
            pageTempBean.setTitle(elh.getCurrentLanguageCode(), pageTitle);
        }
        jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
        if (pageTempBean.getTitle(elh.getCurrentLanguageCode()) != null) {
            engineMap.put("dataPageTitle", pageTempBean.getTitle(elh.getCurrentLanguageCode()));
        } else {
            engineMap.put("dataPageTitle", EMPTY_STRING);
        }

        // remember the requested language codes :
        Set updatedLanguageEntries = (Set) engineMap.get(
                "updatedLanguageEntries");
        if (updatedLanguageEntries == null) {
            updatedLanguageEntries = new HashSet();
        }
        if (!updatedLanguageEntries.contains(elh.getCurrentLanguageCode())) {
            updatedLanguageEntries.add(elh.getCurrentLanguageCode());
        }
        engineMap.put("updatedLanguageEntries", updatedLanguageEntries);

        theSession.setAttribute("jahia_session_engineMap", engineMap);

        // sets screen
        engineMap.put("screen", theScreen);

        if (theScreen.equals("save")) {
            engineMap.put("jspSource", "close");

        } else if (theScreen.equals("cancel")) {
            engineMap.put("jspSource", "close");

        } else if (theScreen.equals("apply")) {
            engineMap.put("jspSource", "apply");

        } else {
            engineMap.put("jspSource", TEMPLATE_JSP);
        }

        // engineMap.put("enableVersioning", new Boolean(true));
        engineMap.put("enableCategories", Boolean.TRUE);
        engineMap.put("enableTimeBasedPublishing", Boolean.TRUE);
        engineMap.put("enableAdvancedWorkflow", Boolean.TRUE);
//        engineMap.put("enableImportExport", Boolean.TRUE);

        // sets engineMap for JSPs
        jParams.setAttribute("engineTitle", "Page properties");
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);

        return engineMap;
    }

    /**
     * loads data for the JSP file
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap then engine map, to be forwarded to the JSP file
     */
    private void loadPageData(final ProcessingContext jParams, final Map engineMap)
            throws JahiaException {
        // check the arguments are valid
        if (jParams == null)
            throw new JahiaException("Invalid parameter", "jParam argument is null!!",
                    JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY);

        final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
        final JahiaUser theUser = jParams.getUser();

        // get all templates
        final Iterator templateEnum = ServicesRegistry.getInstance().
                getJahiaPageTemplateService().
                getPageTemplates(theUser, thePage.getJahiaID(), false);

        // get current page's template too even though it is desactivated
        final ContentPage contentPage = ContentPage.getPage(thePage.getID());
        final List vec = filterTemplates(jParams, contentPage, templateEnum);
        engineMap.put("templateList", vec);

        // existing pages, for move... not used yet
        /*
                 Iterator pageTree = ServicesRegistry.getInstance().
                getJahiaPageService().
                getAllPages (thePage.getJahiaID(), PageLoadFlags.DIRECT,
                        jParams, theUser);
                 engineMap.put ("pageTree", pageTree);
         */
    }

    private List filterTemplates(final ProcessingContext jParams, final ContentPage contentPage,
                                   final Iterator tlist) throws JahiaException {
        Set constraintTemplates = new HashSet();// List of templates constraint
        final ContentObject parentField = contentPage.getParent(jParams.getUser(),
                jParams.getEntryLoadRequest(), jParams.getOperationMode());
        if (parentField != null) {
            final JahiaFieldDefinition fieldDef = JahiaFieldDefinitionsRegistry
                    .getInstance().getDefinition(
                    parentField.getDefinitionID(jParams
                            .getEntryLoadRequest()));
            final ContentPage parentPage = ((ContentPageField) parentField).getPage();
            final int pageDefID = parentPage != null ? parentPage
                    .getPageTemplateID(jParams) : contentPage
                    .getPageTemplateID(jParams);
            String defaultValue = fieldDef.getDefaultValue();

            // requested
            if (defaultValue.indexOf("[") != -1) {
                final int p1 = defaultValue.indexOf("[");
                final int p2 = defaultValue.lastIndexOf("]");
                String tList = defaultValue.substring(p1, p2 + 1);
                //   defaultValue = StringUtils.replace(defaultValue, tList, "");

                tList = tList.substring(1, tList.length() - 1);
                constraintTemplates = getStringToSet(tList);
                logger.debug("tlist: " + constraintTemplates);
            }
        }

        final List all = new ArrayList();
        final List constraint = new ArrayList();
        int checkcount = constraintTemplates.size();
        while (tlist.hasNext()) {
            final JahiaPageDefinition t = (JahiaPageDefinition) tlist.next();
            if (t.isAvailable()) {
                all.add(t);
            }
            final String templatename = t.getName();
            if (constraintTemplates.contains(templatename)) {
                logger.debug("found requested templates:" + templatename);
                checkcount--;
                constraint.add(t);
            }
        }

        final List vec = (constraintTemplates.size() > 0 && checkcount == 0 ? constraint
                : all);

        final EntryLoadRequest loadRequest = new EntryLoadRequest(
                EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, jParams
                .getEntryLoadRequest().getLocales());
        // active page def
        final JahiaPageDefinition activePageDef = contentPage
                .getPageTemplate(loadRequest);
        final JahiaPageDefinition currentPageDef = contentPage
                .getPageTemplate(jParams);

        if (activePageDef != null && !vec.contains(activePageDef))
            vec.add(activePageDef);
        if (currentPageDef != null && !vec.contains(currentPageDef))
            vec.add(currentPageDef);

        // sort it
        if (vec.size() > 1) {
            Collections.sort(vec, (Comparator) vec.get(0));
        }
        return vec;
    }

    /**
     * utility method
     *
     * @param s the long string
     * @return a Set of string
     */
    private Set getStringToSet(final String s) {
        final Set vlist = new HashSet();
        final StringTokenizer tok = new StringTokenizer(s, ",");
        while (tok.hasMoreTokens()) {
            final String v = tok.nextToken().trim();
            vlist.add(v);
        }
        return vlist;
    }


    /**
     * gets POST form data from the JSP file
     *
     * @param jParams   a ProcessingContext object (with request and response)
     * @param engineMap then engine map, to be forwarded to the JSP file
     * @return true if everything went okay, false if not
     */
    private boolean updatePageData(final ProcessingContext jParams, final Map engineMap,
                                   final String languageCode) throws JahiaException {
        final EngineMessages engineMessages = new EngineMessages();
        boolean setPageTitleSuccessfull = setPageTitleIfNecessary(jParams, languageCode, engineMessages, engineMap);
        boolean setPageURLKeySuccessfull = setPageURLKeyIfValidAndNotEmpty(jParams, engineMessages, engineMap);
        setPageTemplateIDInEngineMapIfNecessary(jParams, engineMap);
        saveMessagesIfNotEmpty(engineMessages, jParams);

        return setPageTitleSuccessfull && setPageURLKeySuccessfull;
    }

//    private void DisplayEngineMap(Map engineMap) {
//        StringBuffer output = new StringBuffer("Detail of engineMap :\n");
//
//        Set keys = engineMap.keySet();
//        Iterator iter = keys.iterator();
//
//        while (iter.hasNext()) {
//            String name = (String) iter.next();
//            Object object = engineMap.get(name);
//            output.append("-").append(name).append(" = [").append(object.toString()).append("]\n");
//        }
//    }

    /**
     * loads the page cache bean with info from an existing JahiaPage
     *
     * @return a PageBean to use as cache for the page info
     */
    private JahiaPageEngineTempBean loadPageBeanFromRealPage(final JahiaPage theRealPage) {

        logger.debug("Making PageBean from REAL Page...");
        final StringBuffer pageAttribute = new StringBuffer("        jahiaID       : [");
        pageAttribute.append(theRealPage.getJahiaID());
        pageAttribute.append("]\n");
        pageAttribute.append("        parentID      : [");
        pageAttribute.append(theRealPage.getParentID());
        pageAttribute.append("]\n");
        pageAttribute.append("        pageType      : [");
        pageAttribute.append(theRealPage.getPageType());
        pageAttribute.append("]\n");
        pageAttribute.append("        title         : [");
        pageAttribute.append(theRealPage.getTitle());
        pageAttribute.append("]\n");
        pageAttribute.append("        pageTemplateID: [");
        pageAttribute.append(theRealPage.getPageTemplateID());
        pageAttribute.append("]\n");
        pageAttribute.append("        remoteURL     : [");
        pageAttribute.append(theRealPage.getRemoteURL());
        pageAttribute.append("]\n");
        pageAttribute.append("        pageLinkID    : [");
        pageAttribute.append(theRealPage.getPageLinkID());
        pageAttribute.append("]\n");
        pageAttribute.append("        creator       : [");
        pageAttribute.append(theRealPage.getCreator());
        pageAttribute.append("]\n");

        logger.debug(pageAttribute.toString());

        return new JahiaPageEngineTempBean(
                theRealPage.getID(),
                theRealPage.getJahiaID(),
                theRealPage.getParentID(),
                theRealPage.getPageType(),
                theRealPage.getPageTemplateID(),
                theRealPage.getPageLinkID(),
                theRealPage.getCreator(),
                -1 // linked field not used
        );
    }

    private void releaseTreeVersioningLocks(final ProcessingContext jParams)
            throws JahiaException {
        if (jParams.settings().areLocksActivated()) {
            final List acquiredPageLocks = (List) jParams.getSessionState().getAttribute(ContentVersioningAction.SESSION_VERSIONING_LOCK_LIST);
            if (acquiredPageLocks == null) {
                return;
            }

            // Lock all page site if possible.
            final Iterator acquiredPageIter = acquiredPageLocks.iterator();
            while (acquiredPageIter.hasNext()) {
                int curPageID = ((Integer) acquiredPageIter.next()).intValue();
                final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, curPageID);
                final JahiaUser user = jParams.getUser();
                lockService.release(lockKey, user, user.getUserKey());
            }
        }
    }

    /**
     * Validate and set page URL key if its present.
     * To be valid, the key must :
     * - not be a jahia reserved word
     * - be composed of characters in 0 to 9, a to z or A to Z, length min 2 max 250
     * - not be used by another page on current jahia site
     * <p/>
     * The validation stops as soon as one error condition is encountered. Higher cost validation is performed last in the chain.
     *
     * @param jParams
     * @param engineMessages
     * @param engineMap
     * @return true if the key is valid and was set, false if empty or not valid.
     */
    private boolean setPageURLKeyIfValidAndNotEmpty(ProcessingContext jParams, EngineMessages engineMessages, Map engineMap) throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission(LockPrerequisites.URLKEY, jParams.getUser(),
                JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0) {
            return true;
        }

        String pageURLKey = jParams.getParameter("pageURLKey");
        if (pageURLKey == null) return false;
        JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
        engineMap.put("dataPageURLKey", pageURLKey);

        if (pageURLKey.length() > 250) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeytooLong.label", pageURLKey.substring(0, 50) + "..."));
            return false;
        } else if (ParamBean.isReservedKeyword(pageURLKey)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyIsReservedWord.label", pageURLKey));
            return false;
        } else if (!isValidURLKey(pageURLKey)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyHasInvalidChars.label", pageURLKey));
            return false;
        } else if (isURLKeyAlreadyUsed(pageURLKey, pageTempBean)) {
            engineMessages.add("pageProperties", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.urlKeyIsDuplicate.label", pageURLKey));
            return false;
        }

        return true;
    }

    /**
     * Set page title if it was specified in request.
     *
     * @param jParams
     * @param languageCode
     * @param engineMessages
     * @param engineMap
     * @return true if page title was successfully set, false if empty
     */
    private boolean setPageTitleIfNecessary(ProcessingContext jParams, String languageCode, EngineMessages engineMessages, Map engineMap) {
        String pageTitle = jParams.getParameter("pageTitle");
        JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
        pageTempBean.setTitle(languageCode, pageTitle);
        engineMap.put("dataPageTitle", pageTitle);
        engineMap.put("validate", Boolean.valueOf(jParams.getParameter("validate") != null));
        if (StringUtils.isBlank(pageTitle)) {
            engineMessages.add("pageTitle", new EngineMessage("org.jahia.engines.pages.PageProperties_Engine.pageTitle.required.label"));
            return false;
        }
        return true;
    }

    /**
     * Set page template ID if it was specified in request.
     *
     * @param jParams
     * @param engineMap
     */
    private void setPageTemplateIDInEngineMapIfNecessary(ProcessingContext jParams, Map engineMap) {
        String pageDef = jParams.getParameter("pageTemplate");

        if (pageDef == null) return;
        JahiaPageEngineTempBean pageTempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
        int pageTemplateID = Integer.parseInt(pageDef);
        pageTempBean.setPageTemplateID(pageTemplateID);
        engineMap.put("dataPageTemplateID", new Integer(pageTemplateID));
        engineMap.put("validate", Boolean.valueOf(jParams.getParameter("validate") != null));
    }

    private void saveMessagesIfNotEmpty(EngineMessages engineMessages, ProcessingContext jParams) {
        if (engineMessages.isEmpty()) return;
        engineMessages.saveMessages(((ParamBean) jParams).getRequest());
    }

    public static boolean isURLKeyAlreadyUsed(String urlKey,
                                              JahiaPageEngineTempBean pageTempBean) throws JahiaException {
        boolean isUsed = false;
        final List pageProperties = ServicesRegistry.getInstance().getJahiaPageService()
                .getPagePropertiesByValueAndSiteID(urlKey,
                        pageTempBean.getSiteID());
        final Iterator propIter = pageProperties.iterator();
        while (propIter.hasNext()) {
            final PageProperty curProperty = (PageProperty) propIter.next();
            if (PageProperty.PAGE_URL_KEY_PROPNAME.equals(curProperty.getName())
                    && curProperty.getPageID() != pageTempBean.getID()) {
                isUsed = true;
            }
        }
        return isUsed;
    }

    public static boolean isValidURLKey(String urlKey) {
        return Pattern.compile("^[a-zA-Z_0-9\\-\\.]{0,250}$").matcher(urlKey).matches();
    }

    protected EngineMessages toEngineMessages(EngineValidationHelper evh) {
        return evh != null ? evh.getEngineMessages("manageRights.future403")
                : new EngineMessages();
    }
}
