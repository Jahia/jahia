/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.params.valves;

import org.jahia.api.Constants;
import org.jahia.bin.Login;
import org.jahia.osgi.FrameworkService;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.security.spi.LicenseCheckUtil;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.observation.JahiaEventService;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Thomas Draier
 */
public class LoginEngineAuthValveImpl extends BaseAuthValve {

    public static final String ACCOUNT_LOCKED = "account_locked";
    public static final String BAD_PASSWORD = "bad_password";
    public static final String LOGGED_IN_USERS_LIMIT_REACHED = "logged_in_users_limit_reached";

    public static final String LOGIN_TAG_PARAMETER = "doLogin";
    public static final String OK = "ok";
    public static final String UNKNOWN_USER = "unknown_user";
    public static final String USE_COOKIE = "useCookie";
    public static final String VALVE_RESULT = "login_valve_result";

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LoginEngineAuthValveImpl.class);

    private CookieAuthConfig cookieAuthConfig;
    private boolean fireLoginEvent = false;
    private String preserveSessionAttributes = null;
    private JahiaUserManagerService userManagerService;

    public void setFireLoginEvent(boolean fireLoginEvent) {
        this.fireLoginEvent = fireLoginEvent;
    }

    public void setPreserveSessionAttributes(String preserveSessionAttributes) {
        this.preserveSessionAttributes = preserveSessionAttributes;
    }

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authContext.getRequest();

        JCRUserNode theUser = getJcrUserNode(httpServletRequest);

        if (theUser != null) {

            logger.debug("User {} logged in.", theUser);

            // if there are any attributes to conserve between session, let's copy them into a map first
            Map<String, Object> savedSessionAttributes = preserveSessionAttributes(httpServletRequest);

            JahiaUser jahiaUser = theUser.getJahiaUser();

            if (httpServletRequest.getSession(false) != null) {
                httpServletRequest.getSession().invalidate();
            }

            // if there were saved session attributes, we restore them here.
            restoreSessionAttributes(httpServletRequest, savedSessionAttributes);

            httpServletRequest.setAttribute(VALVE_RESULT, OK);
            authContext.getSessionFactory().setCurrentUser(jahiaUser);

            // set UI locale to the user's preferred locale after login
            Locale preferredUserLocale = UserPreferencesHelper.getPreferredLocale(theUser, LanguageCodeConverters.resolveLocaleForGuest(httpServletRequest));
            httpServletRequest.getSession().setAttribute(Constants.SESSION_UI_LOCALE, preferredUserLocale);

            // do a switch to the user's preferred language
            if (SettingsBean.getInstance().isConsiderPreferredLanguageAfterLogin()) {
                httpServletRequest.getSession().setAttribute(Constants.SESSION_LOCALE, preferredUserLocale);
            }

            String useCookie = httpServletRequest.getParameter(USE_COOKIE);
            if (!SettingsBean.getInstance().isFullReadOnlyMode() && "on".equals(useCookie)) {
                // the user has indicated he wants to use cookie authentication
                CookieAuthValveImpl.createAndSendCookie(authContext, theUser, cookieAuthConfig);
            }

            if (fireLoginEvent) {
                LoginEvent event = new LoginEvent(this, jahiaUser, authContext);
                SpringContextSingleton.getInstance().publishEvent(event);
                ((JahiaEventService) SpringContextSingleton.getBean("jahiaEventService")).publishEvent(event);

                Map<String, Object> m = new HashMap<>();
                m.put("user", jahiaUser);
                m.put("authContext", authContext);
                m.put("source", this);
                FrameworkService.sendEvent("org/jahia/usersgroups/login/LOGIN", m, false);
            }

        } else {
            valveContext.invokeNext(context);
        }
    }

    private JCRUserNode getJcrUserNode(HttpServletRequest httpServletRequest) {
        if (isLoginRequested(httpServletRequest)) {
            final String username = httpServletRequest.getParameter("username");
            final String password = httpServletRequest.getParameter("password");
            final String site = httpServletRequest.getParameter("site");

            if ((username != null) && (password != null)) {
                // Check if the user has site access ( even though it is not a user of this site )
                return getJcrUserNode(httpServletRequest, username, password, site);
            }
        }
        return null;
    }

    private JCRUserNode getJcrUserNode(HttpServletRequest httpServletRequest, String username, String password, String site) {
        JCRUserNode theUser = userManagerService.lookupUser(username, site);
        if (theUser != null) {
            if (theUser.verifyPassword(password)) {
                if (!theUser.isAccountLocked()) {
                    if (!theUser.isRoot() && LicenseCheckUtil.isLoggedInUsersLimitReached()) {
                        logger.warn("The number of logged in users has reached the authorized limit.");
                        httpServletRequest.setAttribute(VALVE_RESULT, LOGGED_IN_USERS_LIMIT_REACHED);
                    } else {
                        return theUser;
                    }
                } else {
                    logger.warn("Login failed: account for user {} is locked.", theUser.getName());
                    httpServletRequest.setAttribute(VALVE_RESULT, ACCOUNT_LOCKED);
                }
            } else {
                logger.warn("Login failed: password verification failed for user {}", theUser.getName());
                httpServletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
            }
        } else {
            logger.debug("Login failed. Unknown username {}", username);
            httpServletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
        }
        return null;
    }

    private Map<String, Object> preserveSessionAttributes(HttpServletRequest httpServletRequest) {
        Map<String, Object> savedSessionAttributes = new HashMap<String, Object>();
        if ((preserveSessionAttributes != null) && (httpServletRequest.getSession(false) != null) && (preserveSessionAttributes.length() > 0)) {
            String[] sessionAttributeNames = Patterns.TRIPLE_HASH.split(preserveSessionAttributes);
            HttpSession session = httpServletRequest.getSession(false);
            for (String sessionAttributeName : sessionAttributeNames) {
                Object attributeValue = session.getAttribute(sessionAttributeName);
                if (attributeValue != null) {
                    savedSessionAttributes.put(sessionAttributeName, attributeValue);
                }
            }
        }
        return savedSessionAttributes;
    }

    private void restoreSessionAttributes(HttpServletRequest httpServletRequest, Map<String, Object> savedSessionAttributes) {
        if (savedSessionAttributes.size() > 0) {
            HttpSession session = httpServletRequest.getSession();
            for (Map.Entry<String, Object> savedSessionAttribute : savedSessionAttributes.entrySet()) {
                session.setAttribute(savedSessionAttribute.getKey(), savedSessionAttribute.getValue());
            }
        }
    }

    protected boolean isLoginRequested(HttpServletRequest request) {
        String doLogin = request.getParameter(LOGIN_TAG_PARAMETER);
        if (doLogin != null) {
            return Boolean.valueOf(doLogin) || "1".equals(doLogin);
        } else if ("/cms".equals(request.getServletPath())) {
            return Login.getMapping().equals(request.getPathInfo());
        }

        return false;
    }

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public class LoginEvent extends BaseLoginEvent {
        private static final long serialVersionUID = -7356560804745397662L;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source, jahiaUser, authValveContext);
        }
    }

}
