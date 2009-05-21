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
 package org.jahia.engines.users;

import java.util.HashMap;
import java.util.Map;

import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;

/**
 * <p>Title: User preference edition engine</p> <p>Description: This engine allows the edition
 * of the user preferences such as the user's personal settings, password and also language
 * preferences </p> <p>Copyright: Copyright (c) 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class EditUserPreferences_Engine implements JahiaEngine {

    private static final String TEMPLATE_JSP = "edituserpreferences";
    private static final String CLOSE_JSP = "selectusers_close";
    private static EditUserPreferences_Engine instance = null;
    public static final String ENGINE_NAME = "edituserpreferences";
    private EngineToolBox toolBox;

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (EditUserPreferences_Engine.class);

    /**
     * Private constructor for singleton pattern
     */
    public EditUserPreferences_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * Check if we have the rights to view this engine
     *
     * @param jParams ProcessingContext object
     *
     * @return boolean if we are allowed to render this engine, false otherwise
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return toolBox.authoriseRender (jParams);
    }

    /**
     * Renders a link to this engine.
     *
     * @param jParams ProcessingContext object to be used to generate URL.
     * @param theObj  the target object on which we want to process this engine
     *
     * @return a String containing an URL to this engine
     *
     * @throws JahiaException
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        String rightParams = (String) theObj;
        String params = EMPTY_STRING;
        params += "?mode=display&screen=edit";
        params += rightParams;
        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }

    /**
     * needsJahiaData
     *
     * @param jParams the current ProcessingContext
     *
     * @return true if the engine requires a JahiaData object to be constructed before
     *         dispatching to it.
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     *
     * @throws JahiaException if there is an error processing input parameters
     * @throws JahiaSessionExpirationException
     *                        if the session has expired while processing input actions
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException {
        // initalizes the hashmap
        Map engineMap = initEngineMap (jParams);

        processLastScreen (jParams, engineMap);
        processCurrentScreen (jParams, engineMap);

        // displays the screen
        toolBox.displayScreen (jParams, engineMap);

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

    /**
     * processes the last screen sent by the user
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engineMap object containing the current engine state
     *
     * @throws JahiaException if there is an error processing input parameters
     */
    public void processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        // gets engineMap values
        String theScreen = (String) engineMap.get ("screen");
        if (theScreen == null) {
            throw new JahiaException ("EditUserPreferences_Engine.processLastScreen",
                    "Error in parameters",
                    JahiaException.PARAMETER_ERROR,
                    JahiaException.CRITICAL_SEVERITY);
        }
        if (theScreen.equals ("edit")) {
        } else if (theScreen.equals ("save")) {
        }
    }

    /**
     * prepares the screen requested by the user
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engineMap object containing the current engine state
     *
     * @throws JahiaException if there is an error processing input parameters
     */
    public void processCurrentScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        jParams.setAttribute ("jahia_session_engineMap", engineMap);

    }

    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     *
     * @return a Map object containing all the basic values needed by an engine
     *
     * @throws JahiaException if there is an error building the current engineMap
     * @throws JahiaSessionExpirationException
     *                        if the session has expired while processing input actions
     */
    private Map initEngineMap (ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {
        String theScreen = jParams.getParameter ("screen");

        // gets session values
        //HttpSession theSession = jParams.getRequest().getSession (true);
        SessionState theSession = jParams.getSessionState ();

        Map engineMap = (Map) theSession.getAttribute (
                "jahia_session_engineMap");

        if (engineMap == null) {
            theScreen = "edit";
            // init engine map
            engineMap = new HashMap();
        }
        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM, jParams.composeEngineUrl (ENGINE_NAME));
        engineMap.put ("selectUGEngine", "selectGroups");
        theSession.setAttribute ("jahia_session_engineMap", engineMap);

        if (theScreen == null) {
            theScreen = "edit";
        }

        // sets screen
        engineMap.put ("screen", theScreen);
        if (theScreen.equals ("cancel")) {
            engineMap.put ("jspSource", CLOSE_JSP);
        } else if (theScreen.equals ("save")) {
            engineMap.put ("jspSource", CLOSE_JSP);
        } else {
            engineMap.put ("jspSource", TEMPLATE_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute ("engineTitle", "Edit user preferences");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap",
                engineMap);

        return engineMap;
    }

}
