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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaString;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
    public static final String LOGIN_TAG_PARAMETER = "doLogin";
    public static final String LOGIN_CHOICE_PARAMETER = "loginChoice";
    public static final String DO_REDIRECT = "loginDoRedirect";

    public static final String STAY_AT_CURRENT_PAGE = "1";
    public static final String GO_TO_HOMEPAGE = "2";

    private CookieAuthConfig cookieAuthConfig;

    public void initialize() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authContext.getRequest();

        JahiaUser theUser = null;
        boolean ok = false;

        String doLogin = httpServletRequest.getParameter(LOGIN_TAG_PARAMETER);
        if (Boolean.valueOf(doLogin) || "1".equals(doLogin)) {

            final String username = httpServletRequest.getParameter("username");
            final String password = httpServletRequest.getParameter("password");

            if ((username != null) && (password != null)) {
                final ServicesRegistry theRegistry = ServicesRegistry.getInstance();
                if (theRegistry != null) {
                    JahiaUserManagerService theService = theRegistry.getJahiaUserManagerService();
                    if (theService != null) {
                        // Check if the user has site access ( even though it is not a user of this site )
                        theUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(username);
                        if (theUser != null) {
                            if (theUser.verifyPassword(password)) {
                                ok = true;
                            } else {
                                logger.warn("Couldn't validate password for user " + theUser.getUserKey() + "!");
                                logger.debug("User " + username + " entered bad password");
                                httpServletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                            }
                        } else {
                            httpServletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                        }
                    }
                }
            }
        }
        if (ok) {
            if (logger.isDebugEnabled()) {
                logger.debug("User " + theUser + " logged in.");
            }
            if (httpServletRequest.getSession(false) != null) {
                httpServletRequest.getSession().invalidate();
            }
            httpServletRequest.setAttribute(VALVE_RESULT, OK);
            authContext.getSessionFactory().setCurrentUser(theUser);

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(theUser);
                JahiaSite site = (JahiaSite) authContext.getRequest().getSession().getAttribute(ProcessingContext.SESSION_SITE);
                if (site != null) {
                    List<Locale> siteLocales = site.getLanguagesAsLocales();
                    if (siteLocales.contains(preferredUserLocale)) {
                        httpServletRequest.getSession()
                                .setAttribute(ProcessingContext.SESSION_LOCALE, preferredUserLocale);
                    }
                }
            }

            String useCookie = httpServletRequest.getParameter(USE_COOKIE);
            if ((useCookie != null) && ("on".equals(useCookie))) {
                // the user has indicated he wants to use cookie authentication
                // now let's create a random identifier to store in the cookie.
                String cookieUserKey = null;
                // now let's look for a free random cookie value key.
                while (cookieUserKey == null) {
                    cookieUserKey = JahiaString.generateRandomString(cookieAuthConfig.getIdLength());
                    Properties searchCriterias = new Properties();
                    searchCriterias.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                    Set<Principal> foundUsers =
                            ServicesRegistry.getInstance().getJahiaUserManagerService().searchUsers(searchCriterias);
                    if (foundUsers.size() > 0) {
                        cookieUserKey = null;
                    }
                }
                // let's save the identifier for the user in the database
                theUser.setProperty(cookieAuthConfig.getUserPropertyName(), cookieUserKey);
                // now let's save the same identifier in the cookie.
                Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieUserKey);
                authCookie.setPath(StringUtils.isNotEmpty(httpServletRequest.getContextPath()) ?
                        httpServletRequest.getContextPath() : "/");
                authCookie.setMaxAge(cookieAuthConfig.getMaxAgeInSeconds());
            }

            enforcePasswordPolicy(theUser);
            theUser.setProperty(JahiaUserManagerService.PROP_LAST_LOGIN_DATE,
                    String.valueOf(System.currentTimeMillis()));
        } else {
            valveContext.invokeNext(context);
        }
    }

    private void enforcePasswordPolicy(JahiaUser theUser) {
        PolicyEnforcementResult evalResult = ServicesRegistry.getInstance().getJahiaPasswordPolicyService().
                enforcePolicyOnLogin(theUser);
        if (!evalResult.isSuccess()) {
            EngineMessages policyMsgs = evalResult.getEngineMessages();
            EngineMessages resultMessages = new EngineMessages();
            for (Object o : policyMsgs.getMessages()) {
                resultMessages.add((EngineMessage) o);
            }
        }
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

}
