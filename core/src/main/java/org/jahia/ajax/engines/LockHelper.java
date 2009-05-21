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
package org.jahia.ajax.engines;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.content.ContentContainerListsXRefManager;
import org.jahia.content.ContentFieldXRefManager;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.deletecontainer.DeleteContainer_Engine;
import org.jahia.engines.workflow.ManageWorkflow;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Helper service for releasing locks.
 * 
 * @author Sergiy Shyrkov
 */
public final class LockHelper {

    protected static final LockService lockRegistry = ServicesRegistry
            .getInstance().getLockService();

    private static final Logger logger = Logger.getLogger(LockHelper.class);

    private static void flushPageCacheThatDisplayContainer(
            final ProcessingContext jParams, final JahiaContainer theContainer)
            throws JahiaException {

        EntryLoadRequest loadVersion = EntryLoadRequest.CURRENT;
        if (ServicesRegistry.getInstance().getJahiaVersionService()
                .isStagingEnabled(theContainer.getJahiaID()))
            loadVersion = EntryLoadRequest.STAGED;

        final JahiaContainerList theList = ServicesRegistry.getInstance()
                .getJahiaContainersService().loadContainerListInfo(
                        theContainer.getListID(), loadVersion);
        // since we have made modifications concerning this page, let's flush
        // the content cache for all the users and browsers as well as all
        // pages that display this containerList...
        final Set containerPageRefs = ContentContainerListsXRefManager
                .getInstance().getAbsoluteContainerListPageIDs(theList.getID());

        if (containerPageRefs != null) {
            Iterator pageRefIDs = containerPageRefs.iterator();
            while (pageRefIDs.hasNext()) {
                Integer curPageID = (Integer) pageRefIDs.next();
            }
        } else {
            logger.debug("Why is cross ref list empty ?");
        }
    }

    private static void flushPageCacheThatDisplayContainerList(
            ProcessingContext jParams, JahiaContainerList theContainerList)
            throws JahiaException {

        // since we have made modifications concerning this page, let's flush
        // the content cache for all the users and browsers as well as all
        // pages that display this containerList...
        if (theContainerList != null) {
            Set containerPageRefs = ContentContainerListsXRefManager
                    .getInstance().getAbsoluteContainerListPageIDs(
                            theContainerList.getID());
            if (containerPageRefs != null) {
                Iterator pageRefIDs = containerPageRefs.iterator();
                while (pageRefIDs.hasNext()) {
                    Integer curPageID = (Integer) pageRefIDs.next();
                }
            } else {
                logger.debug("Why is cross ref list empty ?");
            }
        } else {
            logger
                    .debug("Couldn't retrieve parent containerList, why is that ?");
        }
    }

    private static void flushPageCacheThatDisplayField(ProcessingContext ctx,
            JahiaField theField) throws JahiaException {
        // since we have made modifications concerning this page, let's flush
        // the content cache for all the users and browsers as well as all
        // pages that display this containerList...
        Set fieldPageRefs = ContentFieldXRefManager.getInstance()
                .getAbsoluteFieldPageIDs(theField.getID());

        if (fieldPageRefs != null) {
            Iterator pageRefIDs = fieldPageRefs.iterator();
            while (pageRefIDs.hasNext()) {
                Integer curPageID = (Integer) pageRefIDs.next();
            }
        } else {
            logger.debug("Why is cross ref list empty ?");
        }
    }

    public static boolean release(String lockType, ProcessingContext ctx)
            throws JahiaException {
        if ("Update_ContentContainer".equals(lockType)
                || "Add_ContentContainerList".equals(lockType)
                || "Update_ContentContainer".equals(lockType)
                || "Delete_ContentContainer".equals(lockType)
                || "RestoreLiveContent_ContentContainer".equals(lockType)) {
            return releaseLockContainer(lockType, ctx);
        } else if ("Update_ContentContainerList".equals(lockType)) {
            return releaseLockContainerListProperties(lockType, ctx);
        } else if ("Update_ContentField".equals(lockType)) {
            return releaseLockField(lockType, ctx);
        } else if ("Update_ContentPage".equals(lockType)) {
            return releaseLockPageProperties(lockType, ctx);
        } else if (lockType.startsWith("Workflow_")) {
            return releaseLockWorkflow(lockType, ctx);
        } else {
            return false;
        }
    }

    private static boolean releaseLockContainer(String lockType,
            ProcessingContext ctx) throws JahiaException {

        final JahiaUser user = ctx.getUser();
        JahiaContainer theContainer = null;
        final SessionState session = ctx.getSessionState();
        final Map engineMap = (Map) session
                .getAttribute("jahia_session_engineMap");
        final JahiaContentContainerFacade jahiaContentContainerFacade = (JahiaContentContainerFacade) engineMap
                .get("UpdateContainer_Engine.JahiaContentContainerFacade");
        if (jahiaContentContainerFacade != null) {
            final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap
                    .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
            theContainer = jahiaContentContainerFacade.getContainer(elh
                    .getCurrentEntryLoadRequest(), true);

        } else if (DeleteContainer_Engine.ENGINE_NAME.equals(engineMap
                .get(JahiaEngine.ENGINE_NAME_PARAM))) {
            theContainer = (JahiaContainer) engineMap.get("theContainer");
        }

        if (theContainer != null) {
            final LockKey lockKey;
            if (theContainer.getID() == 0) {
                lockKey = LockKey.composeLockKey(lockType, theContainer
                        .getListID());
            } else {
                lockKey = LockKey
                        .composeLockKey(lockType, theContainer.getID());
            }

            if (!lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user
                    .getUserKey())) {
                logger
                        .info("Cannot release the lock, since it has not previously been acquired in the same context: "
                                + lockKey);
                ctx.getSessionState()
                        .removeAttribute("needToRefreshParentPage");
                return true;
            }
            if (theContainer.getID() > 0) {
                final int mode = JahiaEngine.CANCEL_MODE;
                ManageWorkflow.getInstance().handleActions(ctx, mode,
                        engineMap, theContainer.getContentContainer());
            }
            if (ctx.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
                if (logger.isDebugEnabled()) {
                    logger.debug(lockKey + " was released");
                }
                flushPageCacheThatDisplayContainer(ctx, theContainer);
            }
        }
        ctx.getSessionState().removeAttribute("needToRefreshParentPage");
        return true;

    }

    private static boolean releaseLockContainerListProperties(String lockType,
            ProcessingContext ctx) throws JahiaException {
        final JahiaUser user = ctx.getUser();
        final SessionState session = ctx.getSessionState();
        final Map engineMap = (Map) session
                .getAttribute("jahia_session_engineMap");
        final JahiaContainerList theContainerList = (JahiaContainerList) engineMap
                .get("theContainerList");
        final LockKey lockKey = LockKey.composeLockKey(
                LockKey.UPDATE_CONTAINERLIST_TYPE, theContainerList.getID());

        if (!lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user
                .getUserKey())) {
            logger
                    .info("Cannot release the lock, since it has not previously been acquired in the same context: "
                            + lockKey);
            throw new JahiaForbiddenAccessException(
                    "Cannot release the lock, since it has not previously been acquired in the same context: "
                            + lockKey);
        }

        final int mode = JahiaEngine.CANCEL_MODE;
        ManageWorkflow.getInstance().handleActions(ctx, mode, engineMap,
                theContainerList.getContentContainerList());
        if (ctx.settings().areLocksActivated()) {
            lockRegistry.release(lockKey, user, user.getUserKey());
            if (logger.isDebugEnabled()) {
                logger.debug(lockKey + " was released");
            }
            flushPageCacheThatDisplayContainerList(ctx, theContainerList);
        }

        return true;
    }

    private static boolean releaseLockField(String lockType,
            ProcessingContext ctx) throws JahiaException {
        final JahiaUser user = ctx.getUser();
        final SessionState session = ctx.getSessionState();
        final Map engineMap = (Map) session
                .getAttribute("jahia_session_engineMap");
        EngineLanguageHelper elh = (EngineLanguageHelper) engineMap
                .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        final JahiaContentFieldFacade jahiaContentFieldFacade = (JahiaContentFieldFacade) engineMap
                .get("jahiaContentFieldFacade");
        final JahiaField theField = jahiaContentFieldFacade.getField(elh
                .getCurrentEntryLoadRequest(), true);
        final LockKey lockKey = LockKey.composeLockKey(
                LockKey.UPDATE_FIELD_TYPE, theField.getID());

        if (!lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user
                .getUserKey())) {
            logger
                    .info("Cannot release the lock, since it has not previously been acquired in the same context: "
                            + lockKey);
            throw new JahiaForbiddenAccessException(
                    "Cannot release the lock, since it has not previously been acquired in the same context: "
                            + lockKey);
        }

        final int mode = JahiaEngine.CANCEL_MODE;
        ManageWorkflow.getInstance().handleActions(ctx, mode, engineMap,
                theField.getContentField());
        if (ctx.settings().areLocksActivated()) {
            lockRegistry.release(lockKey, user, user.getUserKey());
            if (logger.isDebugEnabled()) {
                logger.debug(lockKey + " was released");
            }
            flushPageCacheThatDisplayField(ctx, theField);
        }
        return true;
    }

    private static boolean releaseLockPageProperties(String lockType,
            ProcessingContext ctx) throws JahiaException {
        final JahiaUser user = ctx.getUser();
        final SessionState session = ctx.getSessionState();
        final Map engineMap = (Map) session
                .getAttribute("jahia_session_engineMap");
        final JahiaPage thePage = (JahiaPage) engineMap.get("thePage");
        if (thePage != null) {
            final LockKey lockKey = LockKey.composeLockKey(
                    LockKey.UPDATE_PAGE_TYPE, thePage.getID());

            if (!lockRegistry.isAlreadyAcquiredInContext(lockKey, user, user
                    .getUserKey())) {
                logger
                        .info("Cannot release the lock, since it has not previously been acquired in the same context: "
                                + lockKey);
                throw new JahiaForbiddenAccessException(
                        "Cannot release the lock, since it has not previously been acquired in the same context: "
                                + lockKey);
            }

            final int mode = JahiaEngine.CANCEL_MODE;
            ManageWorkflow.getInstance().handleActions(ctx, mode, engineMap,
                    thePage.getContentPage());
            if (ctx.settings().areLocksActivated()) {
                lockRegistry.release(lockKey, user, user.getUserKey());
                if (logger.isDebugEnabled()) {
                    logger.debug(lockKey + " was released");
                }
            }
        }
        return true;
    }

    private static boolean releaseLockWorkflow(String lockType,
            ProcessingContext ctx) throws JahiaForbiddenAccessException {
        final JahiaUser user = ctx.getUser();
        if (user == null || !ctx.getPage().checkWriteAccess(user)) {
            logger
                    .warn("Cannot release the lock, since it has not previously been acquired in the same context");
            throw new JahiaForbiddenAccessException(
                    "Cannot release the lock, since it has not previously been acquired in the same context");
        }
        if (ctx.settings().areLocksActivated()) {
            final Set wl = (Set) ctx.getSessionState().getAttribute(
                    "workflowLocks");
            if (wl != null && wl.size() > 0) {
                final LockService lockRegistry = ServicesRegistry.getInstance()
                        .getLockService();
                final Iterator iterator = wl.iterator();
                while (iterator.hasNext()) {
                    final LockKey lockKey = (LockKey) iterator.next();
                    lockRegistry.release(lockKey, user, user.getUserKey());
                    if (logger.isDebugEnabled()) {
                        logger.debug(lockKey + " was released");
                    }
                    iterator.remove();
                }
            }
            ctx.getSessionState().removeAttribute("initialObj");
        }
        return true;
    }

    /**
     * Initializes an instance of this class.
     */
    private LockHelper() {
        super();
        // TODO Auto-generated constructor stub
    }
}
