/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.FrameworkService;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.jahia.params.valves.LoginEngineAuthValveImpl.*;

/**
 * <p>Title: Generic SSO auth valve</p>
 * <p>Description: authenticate users with a SSO server.</p>
 * <p>Copyright: Copyright (c) 2005 - Pascal Aubry</p>
 * <p>Company: University of Rennes 1</p>
 *
 * @author Pascal Aubry
 * @version 1.0
 */

public abstract class SsoValve extends BaseAuthValve {

    private boolean fireLoginEvent = false;

    public void setFireLoginEvent(boolean fireLoginEvent) {
        this.fireLoginEvent = fireLoginEvent;
    }

    public class LoginEvent extends BaseLoginEvent {

        public LoginEvent(Object source, JahiaUser jahiaUser, AuthValveContext authValveContext) {
            super(source, jahiaUser, authValveContext);
        }
    }

    /**
     * Logger instance
     */
    private static final Logger logger = LoggerFactory.getLogger(SsoValve.class);
    
    private boolean skipAuthentication = false;
    
    /**
     * Retrieve the credentials from the request.
     *
     * @param request current request
     * @return an object.
     * @throws Exception any exception
     */
    public abstract Object retrieveCredentials(HttpServletRequest request) throws Exception;

    /**
     * Validate the credentials.
     *
     * @param credentials the crendentials.
     * @param request current request
     * @return the id of user that was authenticated, or null if none.
     * @throws Exception any exception
     */
    public abstract String validateCredentials(Object credentials, HttpServletRequest request) throws JahiaException;
    
    /**
     * @see org.jahia.pipelines.valves.Valve#invoke(java.lang.Object, org.jahia.pipelines.valves.ValveContext)
     */
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("starting " + this.getClass().getName() + ".invoke()...");
        }
        AuthValveContext authContext = (AuthValveContext) context;

        // at first look if the user was previously authenticated
        JahiaUser sessionUser = null;
        final HttpServletRequest servletRequest = authContext.getRequest();
        HttpSession session = servletRequest.getSession();
        if (session != null) {
            sessionUser = (JahiaUser) session.getAttribute(Constants.SESSION_USER);
        }
        if (sessionUser != null && !JahiaUserManagerService.isGuest(sessionUser)) {
            if (logger.isDebugEnabled()) {
                logger.debug("user '{}' was already authenticated!", sessionUser.getUsername());
            }
            authContext.getSessionFactory().setCurrentUser(sessionUser);
            servletRequest.setAttribute(VALVE_RESULT, OK);
            return;
        }

        logger.debug("retrieving credentials...");
        Object credentials;
        try {
            credentials = retrieveCredentials(servletRequest);
        } catch (Exception e) {
            throw new PipelineException("exception was thrown while retrieving credentials!", e);
        }
        if (credentials == null) {
            logger.debug("no credentials found!");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("credentials = {}", credentials);
            }

            logger.debug("validating credentials...");
            String uid;
            try {
                uid = validateCredentials(credentials, servletRequest);
            } catch (Exception e) {
                throw new PipelineException("exception was thrown while validating credentials!", e);
            }
            if (uid == null) {
                logger.warn("credentials were not validated!");
                servletRequest.setAttribute(VALVE_RESULT, BAD_PASSWORD);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("uid = {}", uid);
            }

            logger.debug("checking user existence in Jahia database...");
            JCRUserNode user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(uid);
            if (user == null) {
                servletRequest.setAttribute(VALVE_RESULT, UNKNOWN_USER);
                throw new PipelineException("user '" + uid + "' was authenticated but not found in database!");
            }

            if (user.isAccountLocked()) {
                logger.warn("Login failed. Account is locked for user " + uid);
                servletRequest.setAttribute(VALVE_RESULT, ACCOUNT_LOCKED);
                return;
            }

            JahiaUser jahiaUser = user.getJahiaUser();
            if (session != null) {
                servletRequest.getSession().invalidate();
                // user has been successfully authenticated, note this in the current session.
                servletRequest.getSession().setAttribute(Constants.SESSION_USER, jahiaUser);
            }
            // eventually set the Jahia user
            authContext.getSessionFactory().setCurrentUser(jahiaUser);
            servletRequest.setAttribute(VALVE_RESULT, OK);

            if (fireLoginEvent) {
                SpringContextSingleton.getInstance().publishEvent(new LoginEvent(this, jahiaUser, authContext));

                Map<String, Object> m = new HashMap<>();
                m.put("user", jahiaUser);
                m.put("authContext", authContext);
                m.put("source", this);
                FrameworkService.sendEvent("org/jahia/usersgroups/login/LOGIN", m, false);
            }

            return;
        }
        if (!isSkipAuthentication()) {
            valveContext.invokeNext(context);
        }
    }

    /**
     * Return the URL to redirect to for authentication.
     *
     * @param request current request
     * @return a URL.
     * @throws JahiaInitializationException
     */
    public abstract String getRedirectUrl(HttpServletRequest request) throws JahiaException;

    public boolean isSkipAuthentication() {
        return skipAuthentication;
    }

    public void setSkipAuthentication(boolean skipAuthentication) {
        this.skipAuthentication = skipAuthentication;
    }

}
