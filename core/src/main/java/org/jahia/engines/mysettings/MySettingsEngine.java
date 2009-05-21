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
package org.jahia.engines.mysettings;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts.Globals;
import org.jahia.data.JahiaData;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserProperty;
import org.jahia.utils.i18n.JahiaResourceBundle;


/**
 * @author Fulco Houkes
 * @version 1.0
 * @since Jahia 4.0.2
 */
public class MySettingsEngine implements JahiaEngine {

    public static final String REQUEST_KEY_PREFIX = "mysettings-user-";
    public static final String REQUEST_PASSWORD_KEY = REQUEST_KEY_PREFIX + "password";
    public static final String REQUEST_PASSWORD_CONFIRMATION_KEY = REQUEST_KEY_PREFIX + "passwordConfirmation";

    public static final String SEPARATOR = "#";
    public static final String USER_PROPERTY_PREFIX = REQUEST_KEY_PREFIX + "property" + SEPARATOR;
    public static final String USER_NEWPROPERTY_PREFIX = REQUEST_KEY_PREFIX + "newpropkey";
    public static final String USER_NEWPROPERTYVALUE_PREFIX = REQUEST_KEY_PREFIX + "newpropvalue";
    public static final String EDIT_TOKEN = "edit";
    public static final String SAVE_TOKEN = "save";

    /**
     * The engine's name
     */
    public static final String ENGINE_NAME = "mysettings";
    private static final String EDIT_JSP = "mysettings.jsp";
    private static final String SUCCESS_JSP = "mysettingschanged.jsp";

    /**
     * logging
     */
    private static final transient Logger logger = Logger
            .getLogger(MySettingsEngine.class);

    private EngineToolBox toolbox;
    private boolean personalremoved=false;

    /**
     * Default constructor, creates a new <code>MySettingsEngine</code> object instance.
     */
    public MySettingsEngine () {
        toolbox = EngineToolBox.getInstance ();
    }

    public boolean authoriseRender (ProcessingContext jParams) {
        return true;
    }


    public String renderLink (ProcessingContext jParams, Object theObj) throws JahiaException {
        String theUrl = jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING);
        if (theObj != null)
            theUrl += theObj;
        return jParams.encodeURL (theUrl);
    }


    public boolean needsJahiaData (ProcessingContext jParams) {
        return true;
    }


    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {

        if (JahiaUserManagerService.isGuest(jParams.getUser())) {
            throw new JahiaUnauthorizedException();
        }
        
        // initalizes the hashmap
        Map engineMap = new HashMap();
        initEngineMap (jParams, engineMap);

        processScreen (jParams, engineMap);

        // displays the screen
        toolbox.displayScreen (jParams, engineMap);
        
        return null;
    }


    public final String getName () {
        return ENGINE_NAME;
    }


    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     */
    private void initEngineMap (ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        // init map
        String theScreen = jParams.getParameter ("screen");
        if (theScreen == null) {
            theScreen = EDIT_TOKEN;
        }

        engineMap.put ("screen", theScreen);
        if (!theScreen.equals (SAVE_TOKEN)) {
//            engineMap.put ("jspSource", getEditJspPath(jParams));
            engineMap.put (ENGINE_URL_PARAM, jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING));
            engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);

        } else {
            engineMap.put (ENGINE_NAME_PARAM, Core_Engine.ENGINE_NAME);
            engineMap.put (ENGINE_URL_PARAM, jParams.composePageUrl (jParams.getPageID ()));
            engineMap.put ("jspSource", "close");
        }
        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put ("jahiaBuild", new Integer (jParams.settings ().getBuildNumber ()));
        engineMap.put ("javascriptUrl", jParams.settings ().getJsHttpPath ());


        if (logger.isDebugEnabled ())
            logger.debug ("fetch user properties");

        // get the user properties
        JahiaUser user = jParams.getUser ();
        UserProperties userProps = user.getUserProperties ();

        // get the properties names, which will be used to store them into the request
        Iterator propNameIter = userProps.propertyNameIterator();
        while (propNameIter.hasNext()) {
            String key = (String) propNameIter.next();

            // get the property value
            UserProperty value = userProps.getUserProperty(key);

            // store the property into the request
            engineMap.put (USER_PROPERTY_PREFIX + key, value);
            logger.debug ("Adding property ["+ key +"] = ["+ value +"]");
        }

        if (logger.isDebugEnabled ())
            logger.debug ("done with properties");


        jParams.setAttribute ("engineTitle", "MySettings");
        jParams.setAttribute( "engineMap", engineMap );
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processScreen (ProcessingContext jParams, Map engineMap)
            throws JahiaException {

        if (logger.isDebugEnabled ())
            logger.debug ("started process screen");
        personalremoved=false;
        // gets the actual screen
        // screen can be "edit" or "save"
        String theScreen = jParams.getParameter ("screen");
        if (theScreen == null) {
            theScreen = EDIT_TOKEN;
            logger.debug ("screen=edit");
        }

        JahiaUser user = jParams.getUser ();
        if (user == null)
            return;

        if (jParams.getSessionState().getAttribute(EngineMessages.CONTEXT_KEY) != null) {
            EngineMessages msgs = (EngineMessages) jParams.getSessionState()
                    .getAttribute(EngineMessages.CONTEXT_KEY);
            // store our engine messages
            msgs.saveMessages(((ParamBean) jParams).getRequest());
            // store Struts action messages
            ((ParamBean) jParams).getRequest().setAttribute(Globals.ERROR_KEY, msgs.toActionMessages());
            // store localized messages
            List<String> localizedMessages = new LinkedList<String>();
            {
                for (EngineMessage engineMessage : (List<EngineMessage>) msgs
                        .getMessages()) {
                    localizedMessages.add(MessageFormat.format(
                            JahiaResourceBundle
                                    .getMessageResource(engineMessage.getKey(),
                                            jParams.getLocale()), engineMessage
                                    .getValues()));
                }
            }
            ((ParamBean) jParams).getRequest().setAttribute("passwordPolicyMessages", localizedMessages);
            
            jParams.getSessionState().removeAttribute(
                    EngineMessages.CONTEXT_KEY);
        }
            
        // by default, consider the request as successful, and change it to *false* when
        // any error occurs.
        boolean ok = true;

        // When the user clicked on the "save" button
        if (theScreen.equals (SAVE_TOKEN)) {
            logger.debug("saving");
            ok = processSave (jParams, user);
        }

        if (!ok) {
            //TODO: The process did not succeeded, process the error here.
        }

        if (logger.isDebugEnabled ())
            logger.debug ("end");

        String targetJSP = ("save".equals(theScreen) && ok && !personalremoved) ? getSuccessJspPath(jParams)
                : getEditJspPath(jParams);

        engineMap.put (ENGINE_OUTPUT_FILE_PARAM, targetJSP);
    }

    private boolean processSave (ProcessingContext jParams, JahiaUser user) {
        if (logger.isDebugEnabled ())
            logger.debug ("save button clicked");

        EngineMessages resultMessages = new EngineMessages();
        ServicesRegistry registry = ServicesRegistry.getInstance ();
        if (registry != null) {
            JahiaUserManagerService service = registry.getJahiaUserManagerService ();
            if (service != null) {

                // retrieve the user password and set it if it is not null
                String password = jParams.getParameter (REQUEST_PASSWORD_KEY);
                if ((password != null) && (!"".equals(password))) {
                    if (logger.isDebugEnabled ())
                        logger.debug ("password changed, check for password confirmation");

                    JahiaPasswordPolicyService pwdPolicyService = registry
                            .getJahiaPasswordPolicyService();
                    boolean pwdPolicyEnabled = pwdPolicyService
                            .isPolicyEnabled(user);
            
                    if (!pwdPolicyEnabled && password.length() < 6) {
                        EngineMessage errorMessage = new EngineMessage("org.jahia.engines.mysettings.passwordTooShort");
                        resultMessages.add(errorMessage);
                        resultMessages.saveMessages(((ParamBean)jParams).getRequest());
                        return false;
                    }
                    
                    // the password is not null, retrieve the password confirmation
                    String passwordConfirmation = jParams.getParameter (REQUEST_PASSWORD_CONFIRMATION_KEY);
                    if (password.equals (passwordConfirmation)) {
                        if (logger.isDebugEnabled ())
                            logger.debug ("password and password confirmation match!");
                        
                        if (pwdPolicyEnabled) {
                            PolicyEnforcementResult evalResult = pwdPolicyService
                                    .enforcePolicyOnPasswordChange(user,
                                            password, true);
                            if (!evalResult.isSuccess()) {
                                EngineMessages policyMsgs = evalResult
                                        .getEngineMessages();
                                for (Iterator iterator = policyMsgs
                                        .getMessages().iterator(); iterator
                                        .hasNext();) {
                                    resultMessages.add((EngineMessage) iterator
                                            .next());
                                }
                                resultMessages
                                        .saveMessages(((ParamBean) jParams)
                                                .getRequest());
                                return false;
                            }
                        }
                        user.setPassword (password);

                    } else {
                        if (logger.isDebugEnabled ())
                            logger.debug ("password and password confirmation do not match!");
                        EngineMessage errorMessage = new EngineMessage(
                            "org.jahia.engines.mysettings.passwordsDontMatch");
                        resultMessages.add("mySettings", errorMessage);
                        resultMessages.saveMessages(((ParamBean)jParams).getRequest());
                        return false;
                    }
                }
                //remove property
                String removekey=jParams.getParameter("keytoremove");
                if(removekey!=null && !"".equals(removekey)){
                    logger.debug("to remove="+removekey);
                    UserProperty proptodelete= user.getUserProperty (removekey);
                    if(proptodelete!=null){
                        user.removeProperty(removekey);
                        personalremoved=true;
                        return true;
                    }
                }

                // pick out all the user properties parameters, and set it into the
                // user properties
                Iterator names = jParams.getParameterNames ();
                if (names != null) {
                    while (names.hasNext ()) {
                        String name = (String) names.next ();

                        if (name != null && name.startsWith (USER_PROPERTY_PREFIX)) {
                            String newValue = jParams.getParameter (name);
                            logger.debug(name+" "+newValue);
                            int index = name.indexOf (SEPARATOR);
                            String key = name.substring (index + 1);

                            if(key.equals("email")
                                    ||key.equals("lastname")
                                    ||key.equals("firstname")
                                    ||key.equals("organization")
                                    ||key.equals("emailNotificationsDisabled")
                                    ||key.equals("preferredLanguage")){
                            key = name.substring (index + 1);
                            } else {
                            key = name;
                            }
                            UserProperty currentProp= user.getUserProperty (key);
                            if (newValue == null) {
                                continue;
                            }
                            if (currentProp == null) {
                                user.setProperty (key, newValue);
                            } else if (!currentProp.getValue().equals (newValue)) {
                                //TODO: The new data should be validated here!!
                                if (!currentProp.isReadOnly()) {
                                    user.setProperty(key, newValue);
                                } else {
                                 logger.debug("this property is readonly");   
                                }
                            }
                        }
                    }
                }
                String newPropertyKey= jParams.getParameter(USER_NEWPROPERTY_PREFIX);
                String newPropertyValue= jParams.getParameter(USER_NEWPROPERTYVALUE_PREFIX);

                if(newPropertyKey!=null
                        && !newPropertyKey.equals(""))
                {
                    logger.debug("adding new value"+newPropertyValue+" for property key:"+newPropertyKey);
                        user.setProperty(USER_PROPERTY_PREFIX+newPropertyKey, newPropertyValue);
                }

            } else {
                return false;
            }
        } else {
            return false;
        }

        if (logger.isDebugEnabled ())
            logger.debug ("save process ended");
        return true;
    }

    private String getEditJspPath(ProcessingContext ctx) {
        String path = EDIT_JSP;

        JahiaTemplateManagerService templateMgr = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();

        JahiaTemplatesPackage templatePackage = templateMgr
                .getTemplatePackage(ctx.getSite().getTemplatePackageName());

        if (templatePackage.getMySettingsPageName() != null) {
            path = templateMgr.resolveResourcePath(templatePackage
                    .getMySettingsPageName(), templatePackage.getName());
        } else {
            String jspSiteMapFileName = ctx.getPage().getPageTemplate()
                    .getSourcePath();
            path = jspSiteMapFileName.substring(0,
                    jspSiteMapFileName.lastIndexOf("/") + 1)
                    + path;
        }

        return path;
    }
    
    private String getSuccessJspPath(ProcessingContext ctx) {
        String path = SUCCESS_JSP;

        JahiaTemplateManagerService templateMgr = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();

        JahiaTemplatesPackage templatePackage = templateMgr
                .getTemplatePackage(ctx.getSite().getTemplatePackageName());

        if (templatePackage.getMySettingsSuccessPageName() != null) {
            path = templateMgr.resolveResourcePath(templatePackage
                    .getMySettingsSuccessPageName(), templatePackage.getName());
        } else {
            String jspSiteMapFileName = ctx.getPage().getPageTemplate()
                    .getSourcePath();
            path = jspSiteMapFileName.substring(0,
                    jspSiteMapFileName.lastIndexOf("/") + 1)
                    + path;
        }

        return path;
    }
}
