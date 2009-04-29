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
//

/*
 * ----- BEGIN LICENSE BLOCK -----
 * Version: JCSL 1.0
 *
 * The contents of this file are subject to the Jahia Community Source License
 * 1.0 or later (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.jahia.org/license
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the rights, obligations and limitations governing use of the contents
 * of the file. The Original and Upgraded Code is the Jahia CMS and Portal
 * Server. The developer of the Original and Upgraded Code is JAHIA Ltd. JAHIA
 * Ltd. owns the copyrights in the portions it created. All Rights Reserved.
 *
 * The Shared Modifications are Jahia Lock Engine.
 *
 * The Developer of the Shared Modifications is Jahia Solution S�rl.
 * Portions created by the Initial Developer are Copyright (C) 2002 by the
 * Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Apr 28 2002 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
 */

package org.jahia.engines.lock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * <p>Title: Jahia Lock Engine</p> <p>Description: This engine displays all necessary forms to
 * permit the user (who has write and/or admin access) to change a Jahia page workflow state.
 * </p> <p>Copyright: Copyright (c) 2003</p> <p>Company: Jahia Solutions SaRL</p>
 *
 * @author MAP
 * @version 1.0
 */
public class LockEngine implements JahiaEngine {

    public static final String ENGINE_NAME = "lock";
    public static final String LOCK_ENGINE_JSP = "lock";
    private static final String TEMPLATE_JSP = "lock";

    private EngineToolBox toolBox;

    /** Logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (LockEngine.class);


    public LockEngine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * @param jParams ;)
     *
     * @return Always true
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        // Always allowed to render workflow. In other hand the button is displayed
        // only if the logged user has write or admin access.
        return true;
    }

    /**
     * @param jParams ;)
     *
     * @return Always false
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }

    /**
     * Compose a valid workflow engine URL.
     *
     * @param jParams ;)
     * @param theObj  not used
     *
     * @return The composed URL; on s'en serait doute...
     *
     * @throws JahiaException
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        final LockKey lockKey = (LockKey) theObj;
        return jParams.composeEngineUrl ("lock", "?lockKey=" + lockKey);
    }

    /**
     * Process the engine action triggered on the displayed form. The actions can be decomposed
     * as follow : screen ::= "display" | "apply" | "save" | "cancel"
     *
     * @param jParams ;)
     * @param jData   not used
     *
     * @throws JahiaException
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        Map engineMap = new HashMap();
        processAction (jParams, engineMap);

        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }

    public void redirect (ProcessingContext jParams, Map engineMap, LockKey lockKey)
            throws JahiaException {
        String redirectedToLockEngine = jParams.getParameter ("redirectedToLockEngine");
        if (redirectedToLockEngine == null) {
            initLockEngine (jParams, engineMap, lockKey);
            toolBox.displayScreen (jParams, engineMap);
        } else {
            processAction (jParams, engineMap);
        }
    }

    private void processAction (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        String actionScreen = jParams.getParameter ("screen");
        logger.debug("processAction: " + actionScreen);
        if(actionScreen==null) {
            actionScreen = "display";
        }
        engineMap.put("screen", actionScreen);
        // screen = display
        if ("display".equals (actionScreen)) {
            final String lockKeyStr = jParams.getParameter ("lockKey");
            if (lockKeyStr != null) {
                final LockKey lockKey = LockKey.composeLockKey (lockKeyStr);
                final LockPrerequisitesResult lpr = LockPrerequisites.getInstance ().getLockPrerequisitesResult(lockKey);
                if (lpr != null) {
                    lpr.setShowDetails(true);
                }
                initLockEngine(jParams, engineMap, lockKey);

                logger.debug("engineMap: " + engineMap);

                LockService lockRegistry = ServicesRegistry.getInstance ().getLockService ();
                Long timeRemaining = lockRegistry.getTimeRemaining (lockKey, jParams.getUser().getUserKey());
                if (timeRemaining != null && timeRemaining.longValue () < 0) {
                    engineMap.put ("jspSource", "close");
                }
                toolBox.displayScreen (jParams, engineMap);
            }
        }
        // apply modifications
        else if ("save".equals (actionScreen) || "apply".equals (actionScreen)) {
            final Iterator paramNames = jParams.getParameterNames ();
            final LockService lockRegistry = ServicesRegistry.getInstance ().getLockService ();
            while (paramNames.hasNext ()) {
                String paramName = (String) paramNames.next ();
                LockKey lockKey = LockKey.composeLockKey (paramName);
                logger.debug("lockKey: " + lockKey + ", from: " + paramName);
                if (lockKey != null) {
                    final JahiaUser user = jParams.getUser();
                    if (lockRegistry.isAlreadyAcquired (lockKey) &&
                            !lockRegistry.isAlreadyAcquiredInContext (lockKey, user, user.getUserKey())) {
                        logger.debug("steal: " + user.getUsername());
                        lockRegistry.steal (lockKey, user, user.getUserKey());
                    } else {
                        logger.debug("nuke: " + user.getUsername());
                        lockRegistry.nuke (lockKey, user, user.getUserKey());
                    }  
                    lockRegistry.acquire(lockKey, user, user.getUserKey(),
                                         jParams.getSessionState().getMaxInactiveInterval());
                    jParams.getSessionState().removeAttribute("showNavigationInLockEngine");
                }
            }

            logger.debug("ENGINE_NAME_PARAM: " + jParams.getParameter(ENGINE_NAME_PARAM));

            if (!"lock".equals (jParams.getParameter(ENGINE_NAME_PARAM))) {
                engineMap.put("screen","edit");
                engineMap.put ("jspSource", "apply");
            } else {
                engineMap.put ("jspSource", "close");
                engineMap.put ("screen", "close");
            }
            engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
            toolBox.displayScreen (jParams, engineMap);
        }/* else if (actionScreen.equals ("cancel")) {
            engineMap.put ("jspSource", "close");
            engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
            toolBox.displayScreen (jParams, engineMap);
        }*/
    }

    private void initLockEngine (ProcessingContext jParams, Map engineMap, LockKey lockKey) {
        engineMap.put ("lockKey", lockKey);
        LockPrerequisitesResult lockPrerequisitesResult =
                LockPrerequisites.getInstance ().getLockPrerequisitesResult (lockKey);
        engineMap.put ("lockPrerequisitesResult", lockPrerequisitesResult);
        engineMap.put ("jspSource", TEMPLATE_JSP);
        engineMap.put (ENGINE_NAME_PARAM, "Jahia Locks");
        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        jParams.setAttribute ("engineTitle", "Lock"); // Displayed in 'engine.jsp'
    }

}
