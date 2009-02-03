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
//  Template_Engine
//  NK  27.01.2002
//

package org.jahia.engines.template;

import java.util.HashMap;
import java.util.Map;

import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.audit.ManageLogs_Engine;
import org.jahia.engines.rights.ManageRights;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.acl.ACLResource;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageDefinitionTemp;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaObjectTool;

/**
 * Engine for editing and setting rights on template
 *
 * @author Khue Nguyen
 */
public class Template_Engine implements JahiaEngine {

    private static final String TEMPLATE_JSP = "manage_template";
    public static final String TEMPLATE_SESSION_NAME = "theTemplate";

    // the temporary template to hold change until they have to be saved.
    private static final String TEMPORARY_TEMPLATE_SESSION_NAME = "theTemporaryTemplate";

    public static final String ENGINE_NAME = "template";
    private EngineToolBox toolBox;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (Template_Engine.class);

    private final String WRITE_ACCESS_STR = "writeAccess";
    private final String ADMIN_ACCESS_STR = "adminAccess";

    private final String SCREEN_STR = "screen";

    private final String EDIT_STR = "edit";
    private final String SAVE_STR = "save";
    private final String CANCEL_STR = "cancel";
    private final String LOGS_STR = "logs";
    private final String APPLY_STR = "apply";
    private final String CLOSE_STR = "close";
    private final String LASTSCREEN_STR = "lastscreen";
    private final String JSPSOURCE_STR = "jspSource";


    /**
     * constructor
     */
    public Template_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * authoriseRender
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return true; // we do not check if we are in Edit mode
    }

    /**
     * renderLink
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        JahiaPageDefinition theTemplate = (JahiaPageDefinition) theObj;
        String params = "?mode=display&templateid=" + theTemplate.getID ();

        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }

    /**
     * needsJahiaData
     */
    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }

    /**
     * handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException,
            JahiaSessionExpirationException,
            JahiaForbiddenAccessException {
        // initalizes the hashmap
        Map engineMap = initEngineMap (jParams);

        // checks if the user has the right to display the engine
        JahiaPageDefinition theTemplate =
                (JahiaPageDefinition) engineMap.get (TEMPLATE_SESSION_NAME);
        JahiaUser theUser = jParams.getUser ();

        if (ACLResource.checkAdminAccess (null, theTemplate, theUser) &&
            LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.admin.templates.ManageTemplateRights", 0)) {
            engineMap.put (ADMIN_ACCESS_STR, Boolean.TRUE);
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put ("enableRightView", Boolean.TRUE);
            engineMap.put (WRITE_ACCESS_STR, Boolean.TRUE);

        } else if (ACLResource.checkWriteAccess (null, theTemplate, theUser)) {
            engineMap.put("enableAuthoring", Boolean.TRUE);
            engineMap.put (WRITE_ACCESS_STR, Boolean.TRUE);
        }

        if (engineMap.get (WRITE_ACCESS_STR) != null) {
            processLastScreen (jParams, engineMap);
            processCurrentScreen (jParams, engineMap);
        } else {
            throw new JahiaForbiddenAccessException ();
        }

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
     * @param engineMap the engine map
     */
    public void processLastScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        JahiaPageDefinition theTemplate =
                (JahiaPageDefinition) engineMap.get (TEMPLATE_SESSION_NAME);

        // gets the last screen
        // lastscreen   = edit, rights, logs
        String lastScreen = jParams.getParameter (LASTSCREEN_STR);
        if (lastScreen == null) {
            lastScreen = EDIT_STR;
        }

        int mode = JahiaEngine.UPDATE_MODE;

        if (lastScreen.equals (EDIT_STR)) {
            if (!processTemplateEdit (jParams, mode, engineMap)) {
                // if there was an error, come back to last screen
                engineMap.put (SCREEN_STR, lastScreen);
                engineMap.put (JSPSOURCE_STR, TEMPLATE_JSP);
            }
        } else if (lastScreen.equals ("rightsMgmt")) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance ()
                        .handleActions (jParams, mode, engineMap, theTemplate.getAclID (), null, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (lastScreen.equals (LOGS_STR)) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageLogs_Engine.getInstance ().handleActions (jParams, mode, engineMap, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        }
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processCurrentScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException,
            JahiaForbiddenAccessException {
        // gets the current screen
        // screen   = edit, rights, logs
        String theScreen = (String) engineMap.get (SCREEN_STR);
        JahiaPageDefinition theTemplate =
                (JahiaPageDefinition) engineMap.get (TEMPLATE_SESSION_NAME);

        // indicates to sub engines that we are processing last screen
        int mode = JahiaEngine.LOAD_MODE;

        // dispatches to the appropriate sub engine
        if (theScreen.equals (EDIT_STR)) {
            processTemplateEdit (jParams, mode, engineMap);
        } else if (theScreen.equals (LOGS_STR)) {
            toolBox.loadLogData (jParams, JahiaObjectTool.TEMPLATE_TYPE, engineMap);
        } else if (theScreen.equals ("rightsMgmt")) {
            if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                ManageRights.getInstance ()
                        .handleActions (jParams, mode, engineMap, theTemplate.getAclID (), null, null);
            } else {
                throw new JahiaForbiddenAccessException ();
            }
        } else if (theScreen.equals (SAVE_STR) || theScreen.equals (APPLY_STR)) {
            mode = JahiaEngine.SAVE_MODE;
            if (processTemplateSave (jParams, engineMap)) {
                if (engineMap.get (ADMIN_ACCESS_STR) != null) {
                    engineMap.put ("logObjectType",
                            Integer.toString (JahiaObjectTool.TEMPLATE_TYPE));
                    engineMap.put ("logObject", theTemplate);
                    ManageRights.getInstance ()
                            .handleActions (jParams, mode, engineMap, theTemplate.getAclID (), null, null);
                }

                JahiaEvent theEvent = new JahiaEvent (this, jParams, theTemplate);
                ServicesRegistry.getInstance ().getJahiaEventService ().fireUpdateTemplate (
                        theEvent);
            } else {
                // if there was an error, come back to last screen
                engineMap.put (SCREEN_STR, EDIT_STR);
                engineMap.put (JSPSOURCE_STR, TEMPLATE_JSP);
            }
            if (theScreen.equals (APPLY_STR)) {

                JahiaPageDefinitionTemp theTemporaryTemplate = (JahiaPageDefinitionTemp)
                        engineMap.get (TEMPORARY_TEMPLATE_SESSION_NAME);

                // remove template context and the sitekey from the source path
                String templateContext = jParams.settings ().getTemplatesContext () +
                        jParams.getSite ().getSiteKey () +
                        "/";
                if ((templateContext != null)
                        &&
                        (theTemporaryTemplate.getSourcePath ().indexOf (templateContext) != -1)) {
                    theTemporaryTemplate.setSourcePath (
                            theTemporaryTemplate.getSourcePath ().substring (
                                    templateContext.length (),
                                    theTemporaryTemplate.getSourcePath ().length ()));
                }

                engineMap.put (SCREEN_STR, jParams.getParameter (LASTSCREEN_STR));
            }
            logger.debug ("Saving !!");
        } else if (theScreen.equals (CANCEL_STR)) {
            engineMap.put (ENGINE_OUTPUT_FILE_PARAM, JahiaEngine.CANCEL_JSP);
        }
    }


    /**
     * inits the engine map
     *
     *
     * @return HashMap, a Map object containing all the basic values needed by an engine
     */
    private Map initEngineMap (ProcessingContext jParams)
            throws JahiaException,
            JahiaSessionExpirationException {

        Map engineMap = new HashMap();
        JahiaPageDefinition theTemplate;

        // gets session values
        SessionState theSession = jParams.getSessionState ();

        // tries to find if this is the first screen generated by the engine
        String theScreen = jParams.getParameter (SCREEN_STR);
        if (theScreen != null) {
            // if no, load the engine map value from the session
            engineMap = (Map) theSession.getAttribute (ProcessingContext.SESSION_JAHIA_ENGINEMAP);
            if (engineMap == null) {
                throw new JahiaSessionExpirationException ();
            }
            theTemplate = (JahiaPageDefinition) engineMap.get (TEMPLATE_SESSION_NAME);
        } else {
            // first screen generated by engine -> init sessions
            int templateID = -1;
            String value = jParams.getParameter ("templateid");
            if (value != null)
                templateID = Integer.parseInt (value);

            theTemplate = ServicesRegistry.getInstance ()
                    .getJahiaPageTemplateService ()
                    .lookupPageTemplate (templateID);

            //theScreen = EDIT_STR;
            theScreen = "rightsMgmt";

            // init the temporary template bean
            JahiaPageDefinitionTemp theTemporaryTemplate =
                    new JahiaPageDefinitionTemp (theTemplate,
                            (jParams.getSite ().getDefaultTemplateID () ==
                    theTemplate.getID ()));

            // remove template context and the sitekey from the source path
            String templateContext = jParams.settings ().getTemplatesContext () +
                    jParams.getSite ().getSiteKey () +
                    "/";
            if ((templateContext != null)
                    && theTemporaryTemplate.getSourcePath () != null
                    && (theTemporaryTemplate.getSourcePath ().indexOf (templateContext) != -1)) {
                theTemporaryTemplate.setSourcePath (
                        theTemporaryTemplate.getSourcePath ().substring (
                                templateContext.length (),
                                theTemporaryTemplate.getSourcePath ().length ()));
            }

            // init session
            engineMap.put (TEMPLATE_SESSION_NAME, theTemplate);
            engineMap.put (TEMPORARY_TEMPLATE_SESSION_NAME, theTemporaryTemplate);
        }

        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM,
                jParams.composeEngineUrl (ENGINE_NAME, "?templateid=" + theTemplate.getID ()));

        theSession.setAttribute (ProcessingContext.SESSION_JAHIA_ENGINEMAP, engineMap);

        // sets screen
        engineMap.put (SCREEN_STR, theScreen);
        if (theScreen.equals (SAVE_STR)) {
            engineMap.put (JSPSOURCE_STR, CLOSE_STR);
        } else if (theScreen.equals (APPLY_STR)) {
            engineMap.put (JSPSOURCE_STR, APPLY_STR);
        } else if (theScreen.equals (CANCEL_STR)) {
            engineMap.put (JSPSOURCE_STR, CLOSE_STR);
        } else {
            engineMap.put (JSPSOURCE_STR, TEMPLATE_JSP);
        }

        // sets engineMap for JSPs
        jParams.setAttribute (ENGINE_NAME_PARAM, "Manage Templates");
        jParams.setAttribute ("org.jahia.engines.EngineHashMap", engineMap);
        jParams.setAttribute ("Template_Engine.warningMsg", EMPTY_STRING);
        return engineMap;
    }


    /**
     * Prepare data to display or retrieves submitted values and store them in session
     */
    private boolean processTemplateEdit (ProcessingContext jParams, int mode, Map engineMap) {

        if (mode == JahiaEngine.LOAD_MODE) {
            // everything is in the session , so do nothing
            return true;
        } else if (mode == JahiaEngine.UPDATE_MODE) {
            // check the last screen
            String lastScreen = jParams.getParameter (LASTSCREEN_STR);
            if (lastScreen == null)
                lastScreen = EMPTY_STRING;

            if (lastScreen.equals (EDIT_STR)) {
                // retrieve submitted data
                JahiaPageDefinitionTemp theTemporaryTemplate = (JahiaPageDefinitionTemp)
                        engineMap.get (TEMPORARY_TEMPLATE_SESSION_NAME);
                if (theTemporaryTemplate == null)
                    return false; // should not

                // get the name
                String value = jParams.getParameter ("templateName");
                if (value != null)
                    theTemporaryTemplate.setName (value);

                // get the source path
                value = jParams.getParameter ("sourcePath");
                if (value != null)
                    theTemporaryTemplate.setSourcePath (value);

                // get the available option
                value = jParams.getParameter ("templateAvailable");
                theTemporaryTemplate.setAvailable (value != null);

                // get the default option
                value = jParams.getParameter ("templateDefault");
                theTemporaryTemplate.setDefault (value != null);
            }
            return true;
        }
        return false;
    }


    /**
     * Save data
     */
    private boolean processTemplateSave (ProcessingContext jParams, Map engineMap)
            throws JahiaException {

        StringBuffer warningMsg = new StringBuffer (EMPTY_STRING);

        JahiaPageDefinitionTemp theTemporaryTemplate = (JahiaPageDefinitionTemp)
                engineMap.get (TEMPORARY_TEMPLATE_SESSION_NAME);

        JahiaPageDefinition theTemplate = (JahiaPageDefinition)
                engineMap.get (TEMPLATE_SESSION_NAME);

        // check data integrity
        if (theTemporaryTemplate.getName () == null
                || theTemporaryTemplate.getName ().trim ().equals (EMPTY_STRING))
            warningMsg.append ("<li>The name is required.</li>");

        // check source path
        warningMsg.append (checkSourcePath (warningMsg.toString (), jParams,
                theTemporaryTemplate));

        if (warningMsg.length() > 0) {
            warningMsg.insert(0, "<ul>").append ("</ul>");
            jParams.setAttribute ("Template_Engine.warningMsg",
                    warningMsg.toString ());
            return false;
        }

        // If everything is ok save new values
        theTemplate.setName (theTemporaryTemplate.getName ());
        theTemplate.setSourcePath (theTemporaryTemplate.getSourcePath ());
        logger.debug ("Source path :" + theTemporaryTemplate.getSourcePath ());
        theTemplate.setAvailable (theTemporaryTemplate.isAvailable ());
        theTemplate.commitChanges ();

        // save site's default template if needed
        boolean doUpdateSite = true;
        if ((jParams.getSite ().getDefaultTemplateID () == theTemplate.getID ())
                && !theTemporaryTemplate.isDefault ()) {
            jParams.getSite ().setDefaultTemplateID (-1);
        } else if (theTemporaryTemplate.isDefault ()) {
            jParams.getSite ().setDefaultTemplateID (theTemplate.getID ());
        } else {
            doUpdateSite = false;
        }

        if (doUpdateSite)
            ServicesRegistry.getInstance ()
                    .getJahiaSitesService ().updateSite (jParams.getSite ());

        return true;

    }

    /**
     * Returns a warning msg in case of not valid source path
     */
    private String checkSourcePath (String warningMsg, ProcessingContext jParams,
                                    JahiaPageDefinitionTemp tempoPageDef) {

        return EMPTY_STRING;
        
//        if (tempoPageDef.getSourcePath () == null
//                || tempoPageDef.getSourcePath ().trim ().equals (EMPTY_STRING)) {
//            return "<li>The source path is required.</li>";
//        }
//
//        // replace all "\" by "/"
//        String sourcePath = JahiaTools.replacePattern (tempoPageDef.getSourcePath (), "\\",
//                "/");
//
//        while (sourcePath.startsWith ("/") || sourcePath.startsWith (".")) {
//            sourcePath = sourcePath.substring (1, sourcePath.length ());
//        }
//        sourcePath = JahiaTools.replacePattern (sourcePath, "..", EMPTY_STRING);
//        sourcePath = JahiaTools.replacePattern (sourcePath, "./", "/");
//        sourcePath = JahiaTools.replacePattern (sourcePath, "/.", "/");
//        sourcePath = JahiaTools.replacePattern (sourcePath, "//", "/");
//
//
//
//        // check if the file exists
//        String path = jParams.settings ().getJahiaTemplatesDiskPath ();
//        path = JahiaTools.replacePattern (path, "\\", "/");
//        if (!path.endsWith ("/"))
//            path += "/";
//        File f = new File (
//                path + jParams.getSite ().getSiteKey () + File.separator + sourcePath);
//        logger.debug (" Template Path = " + f.getAbsolutePath ());
//
//        if (!f.isFile ())
//            return "<li>The source file does not exist.</li>";
//
//        if (!f.canRead ())
//            return "<li>No access allowed to the source file.</li>";
//
//        // if everything is ok , save :
//        if (warningMsg.equals (EMPTY_STRING)) {
//            String templateContext = jParams.settings ().getTemplatesContext ()
//                    + jParams.getSite ().getSiteKey () + "/";
//            tempoPageDef.setSourcePath (templateContext + sourcePath);
//        }
//        return EMPTY_STRING;
    }

}
