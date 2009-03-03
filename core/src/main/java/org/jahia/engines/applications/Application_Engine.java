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

package org.jahia.engines.applications;

import org.jahia.data.JahiaData;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.audit.ManageLogs_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaObjectTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Engine for editing and setting rights on template
 *
 * @author Khue Nguyen
 */
public class Application_Engine implements JahiaEngine {

    private static final String APPLICATION_JSP = "manage_application";
    public static final String APPLICATION_SESSION_NAME = "theApplication";

    // the temporary template to hold change until they have to be saved.
    public static final String TEMPORARY_APPLICATION_SESSION_NAME = "theTemporaryApplication";
    public static final String PORTLETS_CATEGORIES_MAP_ATTR = "portletCategoriesMap";

    public static final String ENGINE_NAME = "application";
    private EngineToolBox toolBox;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Application_Engine.class);

    private final String WRITE_ACCESS_STR = "writeAccess";
    private final String ADMIN_ACCESS_STR = "adminAccess";

    private final String SCREEN_STR = "screen";

    private final String EDIT_STR = "edit";
    private final String SAVE_STR = "save";
    private final String CANCEL_STR = "cancel";
    private final String OPEN_CATEGORIES = "open_categories";
    private final String LOGS_STR = "logs";
    private final String APPLY_STR = "apply";
    private final String CLOSE_STR = "close";
    private final String LASTSCREEN_STR = "lastscreen";
    private final String JSPSOURCE_STR = "jspSource";

    /**
     * constructor
     */
    public Application_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true; // we do not check if we are in Edit mode
    }

    /**
     * renderLink
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        ApplicationBean application = (ApplicationBean) theObj;
        String params = "?mode=display&appid=" + application.getID();
        if (jParams instanceof ParamBean) {
            if (((ParamBean) jParams).getRequest().getAttribute("showAllComponents") != null) {
                params += "&showAllSites=true";
            }
        }
        return jParams.composeEngineUrl(Application_Engine.ENGINE_NAME, params);
    }

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;
    }

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
        ApplicationBean theApplication = (ApplicationBean) engineMap.get(Application_Engine.APPLICATION_SESSION_NAME);
        JahiaUser theUser = jParams.getUser();

        if (theApplication.getACL().getPermission(null, null, theUser, JahiaBaseACL.ADMIN_RIGHTS, false, jParams.getSiteID()) &&
                LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.server.admin.components.ManageComponentRights", 0)) {
            engineMap.put(ADMIN_ACCESS_STR, Boolean.TRUE);
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put("enableRightView", Boolean.TRUE);
            engineMap.put(WRITE_ACCESS_STR, Boolean.TRUE);

        } else
        if (theApplication.getACL().getPermission(null, null, theUser, JahiaBaseACL.WRITE_RIGHTS, false, jParams.getSiteID())) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put(WRITE_ACCESS_STR, Boolean.TRUE);
        }

        if (engineMap.get(WRITE_ACCESS_STR) != null) {
            processLastScreen(jParams, engineMap);
            processCurrentScreen(jParams, engineMap);
        } else {
            throw new JahiaForbiddenAccessException();
        }

        // displays the screen
        toolBox.displayScreen(jParams, engineMap);

        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return Application_Engine.ENGINE_NAME;
    }


    /**
     * processes the last screen sent by the user
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine map
     */
    public void processLastScreen(ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        ApplicationBean theApplication = (ApplicationBean) engineMap.get(Application_Engine.APPLICATION_SESSION_NAME);

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter(LASTSCREEN_STR);
        if (lastScreen == null) {
            lastScreen = EDIT_STR;
        }

        int mode = JahiaEngine.UPDATE_MODE;

        if (lastScreen.equals(EDIT_STR)) {
            if (!processApplicationEdit(jParams, mode, engineMap)) {
                // if there was an error, come back to last screen
                engineMap.put(SCREEN_STR, lastScreen);
                engineMap.put(JSPSOURCE_STR, Application_Engine.APPLICATION_JSP);
            }
        } else if (lastScreen.equals("rightsMgmt")) {
            if (engineMap.get(ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance()
                        .handleActions(jParams, mode, engineMap, theApplication.getAclID(), null, null);
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (lastScreen.equals(LOGS_STR)) {
            if (engineMap.get(ADMIN_ACCESS_STR) != null) {
                ManageLogs_Engine.getInstance().handleActions(jParams, mode, engineMap, null);
            } else {
                throw new JahiaForbiddenAccessException();
            }
        }
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen(ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get(SCREEN_STR);
        ApplicationBean theApplication = (ApplicationBean) engineMap.get(Application_Engine.APPLICATION_SESSION_NAME);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // dispatches to the appropriate sub engine
        if (theScreen.equals(EDIT_STR)) {
            processApplicationEdit(jParams, mode, engineMap);
        } else if (theScreen.equals(LOGS_STR)) {
            toolBox.loadLogData(jParams, JahiaObjectTool.APPLICATION_TYPE, engineMap);
        } else if (theScreen.equals("rightsMgmt")) {
            if (engineMap.get(ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance().handleActions(jParams, mode, engineMap, theApplication.getAclID(), null, null);
            } else {
                throw new JahiaForbiddenAccessException();
            }
        } else if (theScreen.equals(SAVE_STR) || theScreen.equals(APPLY_STR)) {
            mode = JahiaEngine.SAVE_MODE;
            if (processApplicationSave(jParams, engineMap)) {
                if (engineMap.get(ADMIN_ACCESS_STR) != null) {
                    engineMap.put("logObjectType", Integer.toString(JahiaObjectTool.APPLICATION_TYPE));
                    engineMap.put("logObject", theApplication);
                    ManageRights.getInstance().handleActions(jParams, mode, engineMap, theApplication.getAclID(), null, null);
                }
            } else {
                // if there was an error, come back to last screen
                engineMap.put(SCREEN_STR, EDIT_STR);
                engineMap.put(JSPSOURCE_STR, Application_Engine.APPLICATION_JSP);
            }
            if (theScreen.equals(APPLY_STR)) {
                engineMap.put(SCREEN_STR, jParams.getParameter(LASTSCREEN_STR));
            }
            Application_Engine.logger.debug("Saving !!");
        } else if (theScreen.equals(CANCEL_STR)) {
            engineMap.put(ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.CANCEL_JSP);
        } else if (theScreen.equals(OPEN_CATEGORIES)) {
            engineMap.put(SCREEN_STR, jParams.getParameter(LASTSCREEN_STR));
            String[] objectIDs = jParams.getParameterValues("objectIDs");
            if (objectIDs != null && objectIDs.length > -1) {
                engineMap.put("openCategoriesManager", Boolean.TRUE);
                jParams.getSessionState().setAttribute("objectIDs", objectIDs);
            }
        }
    }


    /**
     * inits the engine map
     *
     * @return HashMap, a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap(ProcessingContext jParams) throws JahiaException  {

        Map engineMap = new HashMap();
        ApplicationBean theApplication;
        boolean showAllsites = false;
        if (jParams instanceof ParamBean) {
            if (((ParamBean) jParams).getRequest().getParameter("showAllSites") != null) {
                showAllsites = true;
            }
        }
        // gets session values
        SessionState theSession = jParams.getSessionState();

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter(SCREEN_STR);
        if (theScreen != null) {
            // if no, load the engine map value from the session
            engineMap = (Map) theSession.getAttribute(ProcessingContext.SESSION_JAHIA_ENGINEMAP);
            if (engineMap == null) {
                throw new JahiaSessionExpirationException();
            }
            theApplication = (ApplicationBean) engineMap.get(Application_Engine.APPLICATION_SESSION_NAME);
        } else {
            // first screen generated by engine -> init sessions
            int appID = -1;
            String value = jParams.getParameter("appid");
            if (value != null)
                appID = Integer.parseInt(value);

            theApplication = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(appID);

            theScreen = EDIT_STR;

            // init the temporary template bean
            ApplicationBean theTemporaryApplication =
                    new ApplicationBean(theApplication.getID(), theApplication.getName(),
                            theApplication.getContext(), theApplication.getVisibleStatus(),
                            theApplication.isShared(), theApplication.getRights(),
                            theApplication.getFilename(), theApplication.getdesc(), theApplication.getType());

            // category of portlets
            Map portletsCategoriesMap = computePortletCategoriesMap(theTemporaryApplication);

            // init session
            engineMap.put(Application_Engine.APPLICATION_SESSION_NAME, theApplication);
            engineMap.put(Application_Engine.TEMPORARY_APPLICATION_SESSION_NAME, theTemporaryApplication);
            engineMap.put(Application_Engine.PORTLETS_CATEGORIES_MAP_ATTR, portletsCategoriesMap);
        }

         // Init Engine Language Helper
        EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        if (elh == null) {
            elh = new EngineLanguageHelper();
            engineMap.put(JahiaEngine.ENGINE_LANGUAGE_HELPER, elh);
        }
        elh.update(jParams);

        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM, Application_Engine.ENGINE_NAME);
        String value = jParams.composeEngineUrl(Application_Engine.ENGINE_NAME, "?appid=" + theApplication.getID());
        if (showAllsites) {
            value += "&showAllSites=true";
        }
        engineMap.put(ENGINE_URL_PARAM, value);

        theSession.setAttribute(ProcessingContext.SESSION_JAHIA_ENGINEMAP, engineMap);

        // sets screen
        engineMap.put(SCREEN_STR, theScreen);
        if (theScreen.equals(SAVE_STR)) {
            engineMap.put(JSPSOURCE_STR, CLOSE_STR);
        } else if (theScreen.equals(APPLY_STR)) {
            engineMap.put(JSPSOURCE_STR, APPLY_STR);
        } else if (theScreen.equals(CANCEL_STR)) {
            engineMap.put(JSPSOURCE_STR, CLOSE_STR);
        } else {
            engineMap.put(JSPSOURCE_STR, Application_Engine.APPLICATION_JSP);
        }
        if (!showAllsites)
            engineMap.put("restrictRightsToSite", new Integer(jParams.getSiteID()));
        else {
            engineMap.put("showSiteKey", Boolean.TRUE);
            engineMap.put("selectSiteInSelectUsrGrp", Boolean.TRUE);
            engineMap.put("showAdminGroups", Boolean.TRUE);
        }
        // sets engineMap for JSPs
        String engineTitle = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.application.applicationSettings.label", jParams.getLocale());
        jParams.setAttribute(ENGINE_NAME_PARAM, "Manage Templates");
        jParams.setAttribute("engineTitle", engineTitle);
        jParams.setAttribute("org.jahia.engines.EngineHashMap", engineMap);
        jParams.setAttribute("Template_Engine.warningMsg", EMPTY_STRING);
        return engineMap;
    }


    /**
     * Prepare data to display or retrieves submitted values and store them in session
     */
    private boolean processApplicationEdit(ProcessingContext jParams, int mode, Map engineMap) {

        if (mode == JahiaEngine.LOAD_MODE) {
            // everything is in the session , so do nothing
            return true;
        } else if (mode == JahiaEngine.UPDATE_MODE) {
            // check the last screen
            String lastScreen = jParams.getParameter(LASTSCREEN_STR);
            if (lastScreen == null)
                lastScreen = EMPTY_STRING;

            if (lastScreen.equals(EDIT_STR)) {
                // retrieve submitted data
                ApplicationBean theTemporaryApplication = (ApplicationBean)
                        engineMap.get(Application_Engine.TEMPORARY_APPLICATION_SESSION_NAME);
                if (theTemporaryApplication == null)
                    return false; // should not

                // get the name
                String value = jParams.getParameter("applicationName");
                if (value != null)
                    theTemporaryApplication.setName(value);

                // get the available option
                value = jParams.getParameter("applicationDescription");
                theTemporaryApplication.setdesc(value);

            }
            return true;
        }
        return false;
    }


    /**
     * Save data
     */
    private boolean processApplicationSave(ProcessingContext jParams, Map engineMap)
            throws JahiaException {

        StringBuffer warningMsg = new StringBuffer(EMPTY_STRING);

        ApplicationBean theTemporaryApplication = (ApplicationBean) engineMap.get(Application_Engine.TEMPORARY_APPLICATION_SESSION_NAME);

        ApplicationBean theApplication = (ApplicationBean) engineMap.get(Application_Engine.APPLICATION_SESSION_NAME);

        // check data integrity
        if (theTemporaryApplication.getName() == null
                || theTemporaryApplication.getName().trim().equals(EMPTY_STRING))
            warningMsg.append("<lu><li>The name is required.<br>");

        if (!warningMsg.toString().equals(EMPTY_STRING)) {
            warningMsg.append("</lu>");
            jParams.setAttribute("Template_Engine.warningMsg",
                    warningMsg.toString());
            return false;
        }

        // If everything is ok save new values
        theApplication.setName(theTemporaryApplication.getName());
        theApplication.setdesc(theTemporaryApplication.getdesc());
        theApplication.setVisible(theTemporaryApplication.getVisibleStatus());
        ServicesRegistry.getInstance().getApplicationsManagerService().saveDefinition(theApplication);

        return true;

    }

    private Map computePortletCategoriesMap(ApplicationBean theTemporaryApplication) throws JahiaException {
        // build portletUniqueId string
        List definitions = theTemporaryApplication.getEntryPointDefinitions();
        String[] portletIDs = new String[definitions.size()];
        for (int i = 0; i < definitions.size(); i++) {
            EntryPointDefinition definition = (EntryPointDefinition) definitions.get(i);
            String appID;
            // get name and id of the current portlet Definition
            String definitionName = "";
            String uniqueID = "";
            if (definition != null) {
                definitionName = definition.getName();
                appID = "" + definition.getApplicationID();
                uniqueID = appID + "::" + definitionName;
            }
            portletIDs[i] = uniqueID;
        }
        // build category Map. User methods of ManaApplicationCategorieEngine
        Object[] results = ((ManageApplicationCategoriesEngine) EnginesRegistry.getInstance().getEngineByBeanName("manageApplicationCategoriesEngine")).loadSelectedEntryPointDefinitons(portletIDs);
        Map portletsCategoriesMap = (Map) results[2];
        return portletsCategoriesMap;
    }

}
