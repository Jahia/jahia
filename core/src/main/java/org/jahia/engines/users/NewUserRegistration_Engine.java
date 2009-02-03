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

 package org.jahia.engines.users;

import org.jahia.data.JahiaData;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

import java.util.*;

public class NewUserRegistration_Engine implements JahiaEngine {

    private static final String EDIT_JSP = "newuserregistration.jsp";
    private static final String SUCCESS_JSP = "userregistrationok.jsp";
    private static final String CLOSE_JSP = "close";
    public static final String ENGINE_NAME = "newuserregistration";
    private EngineToolBox toolBox;

    public NewUserRegistration_Engine () {
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
        return JahiaUserManagerService.isGuest(jParams.getUser());
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
        return jParams.composeEngineUrl (ENGINE_NAME, "?mode=display&screen=edit");
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
        return true;
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
            boolean allValuesValid = true;

            // first let's retrieve all the values from the form.
            String userName = jParams.getParameter("newUser_username");
            String password1 = jParams.getParameter("newUser_password1");
            String password2 = jParams.getParameter("newUser_password2");
            String[] groupList = jParams.getParameterValues("newUser_groupList");
            Properties userProperties = new Properties();
            Iterator paramNames = jParams.getParameterNames();
            while (paramNames.hasNext()) {
                String curParamName = (String) paramNames.next();
                String[] curParamValues = jParams.getParameterValues(curParamName);
                if (curParamName.startsWith("newUserProp_")) {
                    // found a user property, let's store it.
                    String propName = curParamName.substring("newUserProp_".length());
                    StringBuffer propValue = new StringBuffer();
                    for (int i=0; i < curParamValues.length; i++) {
                        propValue.append(curParamValues[i]);
                        if (i < (curParamValues.length -1)) {
                            propValue.append(";");
                        }
                    }
                    userProperties.setProperty(propName, propValue.toString());
                }
                jParams.setAttribute(curParamName, curParamValues);
            }

            EngineMessages resultMessages = new EngineMessages();
            if ((userName == null) || "".equals(userName)) {
                allValuesValid = false;
                EngineMessage errorMessage = new EngineMessage(
                    "org.jahia.engines.users.newuserregistration.missingUserName");
                resultMessages.add("newUserRegistration", errorMessage);
            }
            if ((password1 == null) || "".equals(password1)) {
                allValuesValid = false;
                EngineMessage errorMessage = new EngineMessage(
                    "org.jahia.engines.users.newuserregistration.missingPassword1");
                resultMessages.add("newUserRegistration", errorMessage);
            }
            if ((password2 == null) || "".equals(password2)) {
                allValuesValid = false;
                EngineMessage errorMessage = new EngineMessage(
                    "org.jahia.engines.users.newuserregistration.missingPassword2");
                resultMessages.add("newUserRegistration", errorMessage);
            }
            if (allValuesValid) {
                // now let's check password integrity.
                if (!password1.equals(password2)) {
                    allValuesValid = false;
                    EngineMessage errorMessage = new EngineMessage(
                        "org.jahia.engines.users.newuserregistration.passwordsDontMatch");
                    resultMessages.add("newUserRegistration", errorMessage);
                }
            }

            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry
                    .getInstance().getJahiaPasswordPolicyService();
            boolean pwdPolicyEnabled = pwdPolicyService
                    .isPolicyEnabled(jParams.getSiteID());

            if (allValuesValid && !pwdPolicyEnabled && password1.length() < 6) {
                allValuesValid = false;
                EngineMessage errorMessage = new EngineMessage("org.jahia.engines.users.newuserregistration.passwordTooShort");
                resultMessages.add("newUserRegistration", errorMessage);
            }

            JahiaUserManagerService uMgr = ServicesRegistry.getInstance().
                    getJahiaUserManagerService();
            if (allValuesValid) {
                // now let's check if username already exists.
                JahiaUser existingUser = uMgr.lookupUser(userName);
                if (existingUser != null) {
                    allValuesValid = false;
                    EngineMessage errorMessage = new EngineMessage(
                        "org.jahia.engines.users.newuserregistration.userNameAlreadyExists");
                    resultMessages.add("newUserRegistration", errorMessage);
                }
            }

            if (allValuesValid) {
                // now let's check that the group list doesn't contain
                // invalid groups
                if (groupList != null) {
                    for (int i = 0; i < groupList.length; i++) {
                        String curGroupName = groupList[i];
                        if (JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME.
                            equals(curGroupName) ||
                            JahiaGroupManagerService.GUEST_GROUPNAME.equals(
                            curGroupName) ||
                            JahiaGroupManagerService.USERS_GROUPNAME.equals(
                            curGroupName)) {
                            allValuesValid = false;
                            EngineMessage errorMessage = new EngineMessage(
                                "org.jahia.engines.users.newuserregistration.unauthorizedGroup",
                                curGroupName);
                            resultMessages.add("newUserRegistration",
                                               errorMessage);
                        }
                    }
                }
            }

            if (allValuesValid && pwdPolicyEnabled) {
                PolicyEnforcementResult evalResult = pwdPolicyService
                        .enforcePolicyOnUserCreate(new JahiaDBUser(-1,
                                userName, password1, null,
                                null), password1, jParams.getSiteID());
                if (!evalResult.isSuccess()) {
                    allValuesValid = false;
                    EngineMessages policyMsgs = evalResult.getEngineMessages();
                    for (Iterator iterator = policyMsgs.getMessages()
                            .iterator(); iterator.hasNext();) {
                        resultMessages.add("newUserRegistration",
                                (EngineMessage) iterator.next());
                    }
                }
            }
            if (allValuesValid) {
                JahiaUser newUser = null;
                newUser = uMgr.createUser(userName, password1, userProperties);
                if (newUser != null) {
                    ServicesRegistry.getInstance().
                        getJahiaSiteUserManagerService().addMember(jParams.
                        getSiteID(), newUser);

                    // make user part of selected groups.
                    if ((groupList != null) && (groupList.length > 0)) {
                        for (int i = 0; i < groupList.length; i++) {
                            JahiaGroup curGroup = ServicesRegistry.getInstance().
                                                  getJahiaGroupManagerService().
                                                  lookupGroup(
                                jParams.getSiteID(), groupList[i]);
                            if (curGroup != null) {
                                curGroup.addMember(newUser);
                            }
                        }
                    }
                } else {
                    allValuesValid = false;
                    EngineMessage errorMessage = new EngineMessage(
                        "org.jahia.engines.users.newuserregistration.errorWhileCreatingUser");
                    resultMessages.add("newUserRegistration", errorMessage);
                }
            }
            if (!allValuesValid) {
                // let's stay on the edit screen.
                engineMap.put("screen", "edit");
                resultMessages.saveMessages(((ParamBean)jParams).getRequest());
            }
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
        String theScreen = (String) engineMap.get ("screen");

        // let's prepare the group list

        Map groupMap = ServicesRegistry.getInstance().getJahiaSiteGroupManagerService().getGroups(jParams.getSiteID());
        groupMap.remove(JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
        groupMap.remove(JahiaGroupManagerService.GUEST_GROUPNAME);
        groupMap.remove(JahiaGroupManagerService.USERS_GROUPNAME);
        Set groupNameSet = groupMap.keySet();
        engineMap.put("groupList", groupNameSet);

        String targetJSP = EDIT_JSP;
        if ("save".equals(theScreen)) {
            targetJSP = SUCCESS_JSP;
        }

        String jspSiteMapFileName = jParams.getPage ().getPageTemplate ().getSourcePath ();
        jspSiteMapFileName = jspSiteMapFileName.substring (0,
                jspSiteMapFileName.lastIndexOf ("/") + 1) +
                targetJSP;
        engineMap.put (ENGINE_OUTPUT_FILE_PARAM, jspSiteMapFileName);

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
            engineMap.put ("jspSource", EDIT_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute ("engineTitle", "New User Registration");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap",
                engineMap);

        return engineMap;
    }
}
