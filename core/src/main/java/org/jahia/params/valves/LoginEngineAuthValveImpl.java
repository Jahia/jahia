/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.bin.Login;
import org.jahia.osgi.BundleUtils;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.security.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

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

    private boolean fireLoginEvent = false;

    public void setFireLoginEvent(boolean fireLoginEvent) {
        this.fireLoginEvent = fireLoginEvent;
    }

    /**
     *
     * @deprecated not used anymore
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public void setPreserveSessionAttributes(String preserveSessionAttributes) {
        // ignored
    }

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        final AuthValveContext authContext = (AuthValveContext) context;
        final HttpServletRequest httpServletRequest = authContext.getRequest();

        if (isLoginRequested(httpServletRequest)) {
            String username = httpServletRequest.getParameter("username");
            String password = httpServletRequest.getParameter("password");
            String site = httpServletRequest.getParameter("site");
            boolean rememberMe = "on".equals(httpServletRequest.getParameter(USE_COOKIE));

            if ((username != null) && (password != null)) {
                AuthenticationService authenticationService = BundleUtils.getOsgiService(AuthenticationService.class, null);
                AuthenticationRequest authenticationRequest = new AuthenticationRequest(username, password, site, true);
                AuthenticationOptions authenticationOptions = AuthenticationOptions.Builder.withDefaults()
                        // pass whether the login event should be fired
                        .triggerLoginEventEnabled(fireLoginEvent)
                        // the check is performed later in the SessionAuthValveImpl
                        .sessionValidityCheckEnabled(false)
                        // pass the "remember me" flag
                        .shouldRememberMe(rememberMe).build();
                try {
                    authenticationService.authenticate(authenticationRequest, authenticationOptions, httpServletRequest,
                            authContext.getResponse());
                    httpServletRequest.setAttribute(VALVE_RESULT, OK);
                    return;
                } catch (AccountNotFoundException e) {
                    httpServletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                } catch (AccountLockedException e) {
                    httpServletRequest.setAttribute(VALVE_RESULT, ACCOUNT_LOCKED);
                } catch (FailedLoginException e) {
                    httpServletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                } catch (ConcurrentLoggedInUsersLimitExceededLoginException e) {
                    httpServletRequest.setAttribute(VALVE_RESULT, LOGGED_IN_USERS_LIMIT_REACHED);
                } catch (InvalidSessionLoginException e) {
                    // should not happen as the check is disabled
                    throw new IllegalStateException("Unexpected InvalidSessionLoginException", e);
                }
            }
        }
        // at this point, the user is not authenticated. We continue to the next valve
        valveContext.invokeNext(context);
    }

    protected boolean isLoginRequested(HttpServletRequest request) {
        String doLogin = request.getParameter(LOGIN_TAG_PARAMETER);
        if (doLogin != null) {
            return Boolean.parseBoolean(doLogin) || "1".equals(doLogin);
        } else if ("/cms".equals(request.getServletPath())) {
            return Login.getMapping().equals(request.getPathInfo());
        }

        return false;
    }

    /**
     *
     * @deprecated not used anymore
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        // ignored
    }

    /**
     *
     * @deprecated not used anymore
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        // ignored
    }

    /**
     * @deprecated to be replaced by an implementation of {@link BaseLoginEvent} in the "core-services" bundle, see
     * <a href="https://github.com/Jahia/jahia-private/issues/4453">#4453</a>
     *
     */
    @Deprecated(since = "8.2.3.0", forRemoval = true)
    public static class LoginEvent extends BaseLoginEvent {
        private static final long serialVersionUID = -7356560804745397662L;

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source, jahiaUser, authValveContext);
        }
    }

}
