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
//  EV  23.01.20001

package org.jahia.engines.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.core.Core_Engine;
import org.jahia.engines.mysettings.MySettingsEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

public class Login_Engine implements JahiaEngine {

    /**
     * Engine's name.
     */
    public static final String ENGINE_NAME = "login";

    public static final String REQUEST_URI = "org.jahia.login.request_uri";
    
    /** Logger instance */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Login_Engine.class);

    private static final String LOGIN_JSP = "login";
    private static final String BADLOGIN_JSP = "bad_login";
    private static final String CLOSE_JSP = "login_close";

    private EngineToolBox toolBox;


    /**
     * constructor
     */
    public Login_Engine() {
        toolBox = EngineToolBox.getInstance();
    }


    /**
     * authorises engine render
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true;                    // always allowed to render login
    }


    /**
     * renders link to pop-up window
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        return jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING);
    }


    /**
     * specifies if the engine needs the JahiaData object
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
            JahiaSessionExpirationException {
        // initalizes the hashmap
        Map<String, Object> engineMap = new HashMap<String, Object>();
        initEngineMap(jParams, engineMap);

        processScreen(jParams, engineMap);

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
        return ENGINE_NAME;
    }


    /**
     * prepares the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void processScreen(ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException,
            JahiaSessionExpirationException {

        logger.debug("started");

        JahiaUser theUser = jParams.getUser();
        if (logger.isDebugEnabled()) {
            logger.debug("user is : " + theUser.getUserKey()) ;
        }

        String res = (String) jParams.getAttribute("login_valve_result");
        if (res == null) {
            return;
        }

        if ("ok".equals(res)) {
            String loginChoice = jParams.getParameter ("loginChoice");
            boolean stayAtCurrentPage = (loginChoice != null && loginChoice.equals (LoginEngineAuthValveImpl.STAY_AT_CURRENT_PAGE) || theUser.isRoot());
            JahiaPage loginPage = null;
            if (stayAtCurrentPage) {
                logger.debug("Staying at current page...") ;
                JahiaPageBaseService pageService = JahiaPageBaseService.getInstance ();
                try {
                    loginPage = pageService.lookupPage(jParams.getPageID(), jParams);

                    if (loginPage != null && !loginPage.checkReadAccess(theUser)) {
                        String username = theUser.getUsername();

                        logger.debug(
                                "The user do not have read access to the requested page ( other than GUEST ) !");

                        logger.error("User " + username + " cannot log in from this page");
                        engineMap.put("notAllowedToLoginFromThisPage", Boolean.TRUE);
                        engineMap.put("screen", "edit");                        
                        
                        if (jParams.getParameter("logtag") != null) {
                            engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl("", EMPTY_STRING));
                            jParams.setAttribute("NotAllowedLogin", Boolean.TRUE); 

                        } else {
                            engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING));
                            engineMap.put("jspSource", BADLOGIN_JSP);
                        }

                        return;
                    }
                } catch (Exception t) {
                    logger.error("Exception looking at page : " + jParams.getPageID(), t);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug ("Page to log to is null (2) ! :" + jParams.getPageID ());
            }

            if (loginPage == null) {
                loginPage = getHomepage(jParams.getSite(), theUser, jParams);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("User homepage is " + loginPage != null ? loginPage.getTitle() : "null");
            }
            int loginPageId = loginPage == null ? 0 : loginPage.getID();            
            if (loginPageId == 0) {
                loginPageId = jParams.getSite().getHomePageID();
            }

            String engineUrl = null;

            String oldRequestUri = (String) jParams.getSessionState()
                    .getAttribute(REQUEST_URI);
            // check if the my settings page was requested before
            if (oldRequestUri != null && oldRequestUri.indexOf("/" + ProcessingContext.ENGINE_NAME_PARAMETER+"/" + MySettingsEngine.ENGINE_NAME) != -1) {
                jParams.getSessionState().removeAttribute(REQUEST_URI);
                engineUrl = jParams.composePageUrl (loginPageId, jParams.getLocale().toString());
                String basePath = Jahia.getContextPath() + Jahia.getServletPath();
                int pos = engineUrl.indexOf(basePath);
                if (pos != -1) {
                    engineUrl = engineUrl.substring(0, pos + basePath.length())
                            + "/" + ProcessingContext.ENGINE_NAME_PARAMETER
                            + "/" + MySettingsEngine.ENGINE_NAME
                            + engineUrl.substring(pos + basePath.length());
                }
            } else {
                engineUrl = jParams.composePageUrl (loginPageId, jParams.getLocale().toString());
            }
            engineMap.put(ENGINE_URL_PARAM, engineUrl);
            logger.debug("engineUrl=" + engineUrl);

            JahiaEvent theEvent = new JahiaEvent(this, jParams, theUser);
            ServicesRegistry.getInstance().getJahiaEventService().fireLogin(theEvent);

        } else {
            if (jParams.getParameter("logtag") != null) {
                engineMap.put("notAllowedToLoginFromThisPage", Boolean.FALSE);
                engineMap.put("screen", "edit");
                engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl("", EMPTY_STRING));
                jParams.setAttribute("wrongLogin", Boolean.FALSE);
            } else {
                engineMap.put("notAllowedToLoginFromThisPage", Boolean.FALSE);
                engineMap.put("screen", "edit");
                engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING));
                engineMap.put("jspSource", BADLOGIN_JSP);
            }
        }

        logger.debug("end");

    }


    /**
     * inits the engine map
     *
     * @param jParams a ProcessingContext object (with request and response)
     */
    private void initEngineMap(ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        // init map
        String theScreen = jParams.getParameter("screen");
        if (theScreen == null) {
            theScreen = "edit";
        }

        engineMap.put("screen", theScreen);
        if (!theScreen.equals("save")) {
            engineMap.put("jspSource", LOGIN_JSP);
            engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(ENGINE_NAME, EMPTY_STRING));
            engineMap.put(ENGINE_NAME_PARAM, ENGINE_NAME);
        } else {
            engineMap.put(ENGINE_NAME_PARAM, Core_Engine.ENGINE_NAME);
            engineMap.put(ENGINE_URL_PARAM, jParams.composePageUrl(jParams.getPageID()));
            engineMap.put("jspSource", CLOSE_JSP);
        }
        engineMap.put(RENDER_TYPE_PARAM, new Integer(JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put("jahiaBuild", new Integer(jParams.settings().getBuildNumber()));
        engineMap.put("javascriptUrl", jParams.settings().getJsHttpPath());

        // JSP Attribute
        jParams.setAttribute("cookieAuthActivated", Boolean.valueOf(jParams.settings().isCookieAuthActivated()));
        jParams.setAttribute("engineTitle", "Login");
        //jParams.getRequest().setAttribute( "engineMap", engineMap );
    }
    
    /**
     * Return the first available home page for this user. Personal homepage has precedence over
     * groups' homepage.
     *
     * @param site current site.
     * @param user the user.
     * @return JahiaPage the first available home page, null if none.
     */
    public static JahiaPage getHomepage(JahiaSite site, JahiaUser user, ProcessingContext jParams) {

        if (logger.isDebugEnabled()) {
            logger.debug("started homepage retrieval for user '" + user.getUserKey() + "'");
        }

        JahiaPage page = null;
        try {

            // get user home page
            if (user.getHomepageID() >= 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("homepage id = " + user.getHomepageID());
                }
                try {
                    ContentPage contentPage =
                            ContentPage.getPage(user.getHomepageID());
                    if (contentPage != null) {
                        page = contentPage.getPage((jParams != null) ? jParams.getEntryLoadRequest() : null, (jParams != null) ? jParams.getOperationMode() : null, (jParams != null) ? jParams.getUser() : null);
                    }
                    if (page != null) {
                        //logger.debug("found user homepage " + page.getTitle());
                        return page;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // get group homepages
            logger.debug("Searching groups for homepage") ;
            JahiaGroupManagerService grpServ = ServicesRegistry.getInstance ().getJahiaGroupManagerService ();

            List<String> v = grpServ.getUserMembership(user);
            v.add(grpServ.lookupGroup(site.getID(),"users").getName());
            int size = v.size();
            String grpKey;
            JahiaGroup grp;
            for (int i = 0; i < size; i++) {
                grpKey = (String) v.get(i);
                grp = grpServ.lookupGroup(grpKey);
                if (grp != null
                        && (grp.getSiteID() == site.getID() || grp.getSiteID() == 0)
                        && grp.getHomepageID() >= 0) {
                    //logger.debug("found a group with homepage " + grp.getGroupname() + ", page=" + grp.getHomepageID() );
                    try {
                        ContentPage contentPage =
                                ContentPage.getPage(grp.getHomepageID());
                        if (contentPage != null) {
                            page = contentPage.getPage((jParams != null) ? jParams.getEntryLoadRequest() : null, (jParams != null) ? jParams.getOperationMode() : null, (jParams != null) ? jParams.getUser() : null);
                        }
                        if (page != null) {
                            //logger.debug("found group homepage =" + page.getTitle() );
                            return page;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
