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
//  EV  10.01.2001
//  NK  05.02.2002 Added Multiple fields (JahiaSimpleField) edit at once support
//  NK	05.02.2002 Version 2.0 Multiple field edit + customisable field ( read/write permission check )
//
package org.jahia.engines.restorelivecontainer;

import org.jahia.content.ContentObject;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.UndoStagingContentTreeVisitor;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.fields.ContentField;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * @author Xavier Lawrence
 */
public class RestoreLiveContainer_Engine implements JahiaEngine {
    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(RestoreLiveContainer_Engine.class);

    public static final String ENGINE_NAME = "restorecontainer";
    public static final String TEMPLATE_JSP = "restore_container";

    private static RestoreLiveContainer_Engine instance = null;
    private final EngineToolBox toolBox;

    /**
     * Default constructor, creates a new <code>RestoreLiveContainer_Engine</code> instance.
     */
    private RestoreLiveContainer_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    /**
     * Returns the unique instance of this class
     *
     * @return the unique instance of this class
     */
    public static RestoreLiveContainer_Engine getInstance() {
        if (instance == null) {
            instance = new RestoreLiveContainer_Engine();
        }
        return instance;
    }

    public String getName() {
        return ENGINE_NAME;
    }

    public boolean authoriseRender(final ProcessingContext processingContext) {
        return toolBox.authoriseRender(processingContext);
    }

    public String renderLink(final ProcessingContext processingContext,
                             final Object theObj)
            throws JahiaException {
        final ContentContainer contentContainer = (ContentContainer) theObj;
        final StringBuffer buff = new StringBuffer();
        buff.append("?cid=");
        buff.append(contentContainer.getID());
        return processingContext.composeEngineUrl(ENGINE_NAME, buff.toString());
    }

    public boolean needsJahiaData(final ProcessingContext processingContext) {
        return true;
    }

    public EngineValidationHelper handleActions(final ProcessingContext processingContext,
                                                final JahiaData jData)
            throws JahiaException {
        // initalizes the hashmap
        final Map engineMap = initEngineMap(processingContext);
        // checks if the user has the right to display the engine
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
        final JahiaUser user = processingContext.getUser();
        if (theContainer.checkWriteAccess(user)) {
            // #ifdef LOCK
            engineMap.put("writeAccess", Boolean.TRUE);
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            if (processingContext.settings().areLocksActivated()) {
                final LockKey lockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE, theContainer.getID());
                if (lockRegistry.acquire(lockKey, user, user.getUserKey(),
                        processingContext.getSessionState().getMaxInactiveInterval())) {
                    // #endif
                    engineMap.put("lock", lockKey);
                    processScreen(processingContext, engineMap);
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
                    processScreen(processingContext, engineMap);
                }
            }

        } else {
            throw new JahiaForbiddenAccessException();
        }

        toolBox.displayScreen(processingContext, engineMap);
        return null;
    }

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     * @return a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap(ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {
        final Map engineMap;
        JahiaContainer theContainer;

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession( true );
        final SessionState theSession = jParams.getSessionState();

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
            languageCode = jParams.getLocale().toString();
        }

        final EntryLoadRequest entryLoadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                new ArrayList());
        entryLoadRequest.getLocales().add(LanguageCodeConverters.languageCodeToLocale(languageCode));

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter("screen");
        if (theScreen != null) {
            // if no, load the field value from the session
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

            //theContainer = (JahiaContainer) engineMap.get("theContainer");
        } else {
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
        if (!theScreen.equals("save") && ! theScreen.equals("cancel")) {
            engineMap.put("jspSource", TEMPLATE_JSP);
        } else {
            engineMap.put("jspSource", "close");
        }
        engineMap.put("noApply", "");

        // sets engineMap for JSPs
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
        jParams.setAttribute("engineTitle", "Restore Container");

        return engineMap;
    } // end initEngineMap

    /**
     * prepares the screen requested by the user
     *
     * @param processingContext a ProcessingContext object
     */
    public void processScreen(final ProcessingContext processingContext,
                              Map engineMap)
            throws JahiaException {
        String theScreen = (String) engineMap.get("screen");
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");

        // #ifdef LOCK
        final LockKey lockKey = LockKey.composeLockKey(LockKey.RESTORE_LIVE_CONTAINER_TYPE, theContainer.getID());
        final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
        final JahiaUser user = processingContext.getUser();

        if (theScreen.equals("save") || theScreen.equals("apply")) {
            final LockKey futureStolenkey = (LockKey) engineMap.get("LockKey");
            if (LockPrerequisites.getInstance().getLockPrerequisitesResult(futureStolenkey) != null) {
                final String param = processingContext.getParameter("whichKeyToSteal");
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
                            processingContext.getSessionState().getMaxInactiveInterval())) {
                        engineMap.remove("LockKey");
                        processingContext.getSessionState().setAttribute("jahia_session_engineMap", engineMap);
                        logger.debug("We were able to acquire the lock after stealing");
                    } else {
                        logger.debug("We were unable to acquire the lock after stealing");
                    }
                    processingContext.getSessionState().removeAttribute("showNavigationInLockEngine");
                }
                engineMap.put("screen", processingContext.getParameter("lastscreen"));
                engineMap.put("jspSource", TEMPLATE_JSP);
                processScreen(processingContext, engineMap);
                return;
            }
            restoreAllParents(processingContext, theContainer.getContentContainer());
            lockRegistry.release(lockKey, user, user.getUserKey());

        } else if (theScreen.equals("cancel")) {
            if (processingContext.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
        }
    }

    /**
     * Implements the restore algorithm
     *
     * @param object Starting object to restore
     */
    private synchronized void restoreAllParents(final ProcessingContext processingContext,
                                                final ContentObject object)
            throws JahiaException {
        final List nonPageParents = new ArrayList();
        final List pagesToRestore = new ArrayList();
        final List pagesToRestoreIds = new ArrayList();
        if (object.getClass() == ContentPage.class) {
            pagesToRestore.add(object);
            pagesToRestoreIds.add(new Integer(object.getPageID()));
        } else {
            nonPageParents.add(object);
        }
        ContentObject parentObject = object.getParent(processingContext.getEntryLoadRequest());

        while (parentObject != null && parentObject.isMarkedForDelete()) {
            nonPageParents.add(parentObject);
            if (parentObject.getClass() == ContentPage.class) {
                pagesToRestore.add(parentObject);
                pagesToRestoreIds.add(new Integer(object.getPageID()));
            } else {
                nonPageParents.add(object);
            }
            parentObject = parentObject.getParent(processingContext.getEntryLoadRequest());

            if (parentObject.getClass() == ContentContainerList.class && ! parentObject.isMarkedForDelete()) {
                // Deal with navigation container lists (absolute)
                if (parentObject.getPageID() > 0) {
                    final ContentPage parentPage = ContentPage.getPage(parentObject.getPageID());
                    if (parentPage == null || parentPage.isMarkedForDelete()) {
                        parentObject = parentPage;
                    }
                }
            }
        }

        for (int i = 0; i < pagesToRestore.size(); i++) {
            final ContentPage page = (ContentPage) pagesToRestore.get(i);
            logger.debug("Restoring page: " + page.getID());
            final UndoStagingContentTreeVisitor visitor = new UndoStagingContentTreeVisitor(page,
                    processingContext.getUser(),
                    processingContext.getEntryLoadRequest(),
                    processingContext.getOperationMode(),
                    processingContext);
            visitor.undoStaging();
            page.invalidateHtmlCache();
        }

        for (int i = 0; i < nonPageParents.size(); i++) {
            final ContentObject o = (ContentObject) nonPageParents.get(i);
            if (pagesToRestoreIds.contains(new Integer(o.getPageID()))) {
                continue;
            }
            // Only restore an object if its parent page has not yet been restored by the visitor
            logger.debug("Restoring object: " + o.getID());
            o.undoStaging(processingContext);
            if (o.getClass() == ContentContainer.class) {
                // Restore all the fields
                final List childs = o.getChilds(processingContext.getUser(), processingContext.getEntryLoadRequest());
                for (int j = 0; j < childs.size(); j++) {
                    final ContentField field = (ContentField) childs.get(j);
                    logger.debug("Restoring field: " + field.getID());
                    field.undoStaging(processingContext);
                }
            }
        }
    }
}
