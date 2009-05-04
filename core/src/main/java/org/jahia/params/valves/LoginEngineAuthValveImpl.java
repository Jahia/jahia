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
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaString;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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
    public static final String DO_REDIRECT = "loginDoRedirect";

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

                SettingsBean settingsBean = org.jahia.settings.SettingsBean.getInstance();
                // do a switch to the user's preferred language
                if (settingsBean.isConsiderPreferredLanguageAfterLogin()) {
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
                        cookieUserKey = JahiaString.generateRandomString(settingsBean.getCookieAuthIDLength());
                        Properties searchCriterias = new Properties();
                        searchCriterias.setProperty(settingsBean.getCookieAuthUserPropertyName(), cookieUserKey);
                        Set foundUsers = ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(
                                jParams.getSiteID(), searchCriterias);
                        if (foundUsers.size() > 0) {
                            cookieUserKey = null;
                        }
                    }
                    // let's save the identifier for the user in the database
                    theUser.setProperty(settingsBean.getCookieAuthUserPropertyName(), cookieUserKey);
                    // now let's save the same identifier in the cookie.
                    Cookie authCookie = new Cookie(settingsBean.getCookieAuthCookieName(), cookieUserKey);
                    authCookie.setPath(jParams.getContextPath());
                    authCookie.setMaxAge(settingsBean.getCookieAuthMaxAgeInSeconds());
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
        if ("POST".equals(ctx.getRequest().getMethod())) {
            String doRedirect = ctx.getRequest().getParameter(DO_REDIRECT);
            if (doRedirect != null
                    && (Boolean.valueOf(doRedirect) || "1".equals(doRedirect))) {
                try {
                    ctx.getResponse().sendRedirect(ctx.composePageUrl());
                } catch (Exception ex) {
                    logger.error(
                            "Unable to perform client-side redirect after login. Cause: "
                                    + ex.getMessage(), ex);
                }

            }
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
}
