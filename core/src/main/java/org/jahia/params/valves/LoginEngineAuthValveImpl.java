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
package org.jahia.params.valves;

import org.apache.log4j.Logger;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.login.Login_Engine;
import org.jahia.engines.mysettings.MySettingsEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaString;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * @author Thomas Draier
 */
public class LoginEngineAuthValveImpl implements Valve {
    private static final transient Logger logger = Logger.getLogger(LoginEngineAuthValveImpl.class);
    public static final String VALVE_RESULT = "login_valve_result";
    public static final String BAD_PASSWORD = "bad_password";
    public static final String UNKNOWN_USER = "unknown_user";
    public static final String OK = "ok";
    public static final String USE_COOKIE = "useCookie";
    public static final String LOGIN_TAG_PARAMETER = "loginFromTag";
    public static final String LOGIN_CHOICE_PARAMETER = "loginChoice";
    public static final String DO_REDIRECT = "loginDoRedirect";
    
    public static final String STAY_AT_CURRENT_PAGE = "1";
    public static final String GO_TO_HOMEPAGE = "2";    

    private CookieAuthConfig cookieAuthConfig;
    
    public void initialize() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        try {
            final ProcessingContext jParams = (ProcessingContext) context;
            final String theScreen = jParams.getParameter("screen");

            JahiaUser theUser = null;
            boolean ok = false;

            if ("1".equals(jParams.getParameter(LOGIN_TAG_PARAMETER)) ||
                    (Login_Engine.ENGINE_NAME.equals(jParams.getEngine()) && "save".equals(theScreen))) {

                final String username = jParams.getParameter("username");
                final String password = jParams.getParameter("password");

                if ((username != null) && (password != null)) {
                    final ServicesRegistry theRegistry = ServicesRegistry.getInstance();
                    if (theRegistry != null) {
                        JahiaUserManagerService theService = theRegistry.getJahiaUserManagerService();
                        if (theService != null) {
                            // Check if the user has site access ( even though it is not a user of this site )
                            theUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().
                                    getMember(jParams.getSiteID(), username);
                            if (theUser != null) {
                                if (theUser.verifyPassword(password)) {
                                    ok = true;
                                } else {
                                    logger.warn("Couldn't validate password for user " + theUser.getUserKey() + "!");
                                    logger.debug("User " + username + " entered bad password");
                                    jParams.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                                }
                            } else {
                                jParams.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                            }
                        }
                    }
                }
            }
            if (ok) {
                if (logger.isDebugEnabled()) {
                    logger.debug("User " + theUser + " logged in.");
                }
                ParamBean paramBean = null;
                if (jParams instanceof ParamBean) {
                    paramBean = (ParamBean) jParams;
                    paramBean.invalidateSession();
                }
                jParams.setAttribute(VALVE_RESULT, OK);
                jParams.setUser(theUser);

                // do a switch to the user's preferred language
                if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                    Locale preferredUserLocale = UserPreferencesHelper
                            .getPreferredLocale(theUser, jParams
                                    .getSite());
                    if (jParams.getSite() != null) {
                        List<Locale> siteLocales;
                        try {
                            siteLocales = jParams.getSite()
                                    .getLanguageSettingsAsLocales(true);
                            if (siteLocales.contains(preferredUserLocale)) {
                                jParams.getSessionState().setAttribute(
                                        ProcessingContext.SESSION_LOCALE,
                                        preferredUserLocale);
                                jParams
                                        .setCurrentLocale(preferredUserLocale);
                                jParams.setLocaleList(null);
                            }
                        } catch (JahiaException e) {
                            logger.warn(
                                    "Unable to switch to the user's preferred language. Cause: "
                                            + e.getMessage(), e);
                        }
                    }
                }
                
                ServicesRegistry.getInstance().getLockService().purgeLockForContext(theUser.getUserKey());

                String useCookie = jParams.getParameter(USE_COOKIE);
                if ((useCookie != null) && ("on".equals(useCookie))) {
                    // the user has indicated he wants to use cookie authentication
                    // now let's create a random identifier to store in the cookie.
                    String cookieUserKey = null;
                    // now let's look for a free random cookie value key.
                    while (cookieUserKey == null) {
                        cookieUserKey = JahiaString.generateRandomString(cookieAuthConfig.getIdLength());
                        Properties searchCriterias = new Properties();
                        searchCriterias.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                        Set<Principal> foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(
                                jParams.getSiteID(), searchCriterias);
                        if (foundUsers.size() > 0) {
                            cookieUserKey = null;
                        }
                    }
                    // let's save the identifier for the user in the database
                    theUser.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                    // now let's save the same identifier in the cookie.
                    Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey);
                    authCookie.setPath(jParams.getContextPath());
                    authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
                    if (paramBean != null) {
                        HttpServletResponse realResponse = paramBean.getRealResponse();
                        realResponse.addCookie(authCookie);
                    }
                }

                enforcePasswordPolicy(theUser, paramBean);
                theUser.setProperty(JahiaDBUser.PROP_LAST_LOGIN_DATE, String.valueOf(System.currentTimeMillis()));
                checkRedirect(paramBean);
            } else {
                valveContext.invokeNext(context);
            }
        } catch (JahiaException e) {
            throw new PipelineException(e);
        }
    }

    private void checkRedirect(ParamBean ctx) {
        String redirectUrl = null;
        try {
            if (GO_TO_HOMEPAGE.equals(ctx.getParameter(LOGIN_CHOICE_PARAMETER))) {
                JahiaPage loginPage = Login_Engine.getHomepage(ctx.getSite(), ctx.getUser(), ctx);
                if (logger.isDebugEnabled()) {
                    logger.debug("User homepage is " + loginPage != null ? loginPage.getTitle() : "null");
                }
                int loginPageId = loginPage == null ? 0 : loginPage.getID();            
                if (loginPageId == 0) {
                    loginPageId = ctx.getSite().getHomePageID();
                }
                redirectUrl = ctx.composePageUrl (loginPageId, ctx.getLocale().toString());
            } else if ("POST".equals(ctx.getRequest().getMethod())) {
                String doRedirect = ctx.getRequest().getParameter(DO_REDIRECT);
                if (doRedirect != null && (Boolean.valueOf(doRedirect) || "1".equals(doRedirect))) {
                    redirectUrl = ctx.composePageUrl();
                }
            }
            if (redirectUrl != null) {
                ctx.getResponse().sendRedirect(redirectUrl);
            }
        } catch (Exception ex) {
            logger.error("Unable to perform client-side redirect after login. Cause: " + ex.getMessage(), ex);
        }
    }

    private void enforcePasswordPolicy(JahiaUser theUser, ParamBean paramBean) {
        PolicyEnforcementResult evalResult = ServicesRegistry.getInstance().getJahiaPasswordPolicyService().
                enforcePolicyOnLogin(theUser);
        if (!evalResult.isSuccess()) {
            EngineMessages policyMsgs = evalResult.getEngineMessages();
            EngineMessages resultMessages = new EngineMessages();
            for (Object o : policyMsgs.getMessages()) {
                resultMessages.add((EngineMessage) o);
            }
            if (paramBean != null) {
                paramBean.getRequest().getSession().setAttribute(EngineMessages.CONTEXT_KEY, resultMessages);
                try {
                    String urlToForward = paramBean.composeEngineUrl(MySettingsEngine.ENGINE_NAME) + "?screen=" +
                            MySettingsEngine.EDIT_TOKEN;
                    paramBean.getResponse().sendRedirect(urlToForward);
                } catch (Exception ex) {
                    logger.error("Unable to forward to the mysettings engine page", ex);
                }
            }
        }
    }

	public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
    	this.cookieAuthConfig = cookieAuthConfig;
    }
    
}
