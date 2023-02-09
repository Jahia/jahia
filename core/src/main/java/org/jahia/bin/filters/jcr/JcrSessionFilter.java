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
package org.jahia.bin.filters.jcr;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.filters.CompositeFilter;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.i18n.JahiaLocaleContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that performs user authentication and the initialization of
 * the JCR session for current user.
 *
 * @author Thomas Draier
 */
public class JcrSessionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(JcrSessionFilter.class);

    private Pipeline authPipeline;

    private List<String> bypassForPatterns;

    private JCRSessionFactory sessionFactory;

    private JahiaUserManagerService userManagerService;

    public void destroy() {
    	// do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (bypass(servletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        boolean initialized = SpringContextSingleton.getInstance().isInitialized();
        try {
            AuthValveContext authValveContext = null;
            if (initialized) {
                try {
                    sessionFactory.setCurrentUser(null);
                    authValveContext = new AuthValveContext((HttpServletRequest) servletRequest,
                                                (HttpServletResponse) servletResponse, sessionFactory);
                    authPipeline.invoke(authValveContext);
                } catch (PipelineException pe) {
                    logger.error("Error while authorizing user", pe);
                }
            }

            if (sessionFactory.getCurrentUser() == null) {
                sessionFactory
                        .setCurrentUser(userManagerService.lookupUserByPath(JahiaUserManagerService.GUEST_USERPATH).getJahiaUser());
            } else {
                JCRUserNode userNode = userManagerService.lookupUserByPath(sessionFactory.getCurrentUser().getLocalPath());
                if (userNode == null || userNode.isAccountLocked()) {
                    sessionFactory.setCurrentUser(null);
                }

                // Only creates a session if we wants to store the user in a new session. Otherwise, just get the
                // session to update the user if needed
                boolean storeInSession = authValveContext != null && authValveContext.isShouldStoreAuthInSession();

                HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession(storeInSession);
                // the following check is done to make sure that the user hasn't been logged out between the reading
                // from the session and the writing back to it. In the case of the logout, the isAuthRetrievedFromSession
                // will return true, but the session will not contain a user because the old session was invalidated
                // and a new one was created. In that case we will not insert the user back into the session.
                // In all other cases we set the user in the session, even if we read from it because this is an expected
                // behavior in the case where the user was updated in the backend (JCR) but not yet in the session.
                // For more information see : https://jira.jahia.org/browse/BACKLOG-5166 and https://jira.jahia.org/browse/BACKLOG-5207
                if (httpSession != null && (!httpSession.isNew() || !authValveContext.isAuthRetrievedFromSession())) {
                    httpSession.setAttribute(Constants.SESSION_USER, sessionFactory.getCurrentUser());
                    // an IllegalStateException might be raised by the setAttribute call if the session was
                    // invalidated, which is the expected behavior because we do want to interrupt the
                    // processing in that case.

                    // Save locale to use it to translate correctly the error messages
                    JahiaLocaleContextHolder.setLocale(UserPreferencesHelper.getPreferredLocale(userNode));
                }
            }

            filterChain.doFilter (servletRequest, servletResponse );
        } finally {
            if (initialized) {
                endRequest();
            }
        }
    }

    private boolean bypass(ServletRequest servletRequest) {
        if (bypassForPatterns == null || bypassForPatterns.isEmpty() || !(servletRequest instanceof HttpServletRequest)) {
            return false;
        }
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String path = req.getRequestURI();
        if (req.getContextPath().length() > 0) {
            path = StringUtils.substringAfter(path, req.getContextPath());
        }
        for (String pattern : bypassForPatterns) {
            if (CompositeFilter.matchFiltersURL(pattern, path)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs finalization task for ending request processing: resetting thread locals and closing all active JCR sessions.
     */
    public static void endRequest() {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.setCurrentUser(null);
        sessionFactory.setCurrentLocale(null);
        sessionFactory.setFallbackLocale(null);
        sessionFactory.setCurrentAliasedUser(null);
        sessionFactory.setCurrentServletPath(null);
        sessionFactory.setCurrentPreviewDate(null);
        sessionFactory.setReadOnlyCacheEnabled(null);
        /*sessionFactory.setVersionDate(null);
        sessionFactory.setVersionLabel(null);*/
        sessionFactory.closeAllSessions();

        ServicesRegistry.getInstance().getSchedulerService().triggerEndOfRequest();
        JahiaLocaleContextHolder.resetLocale();
    }



    public void init(FilterConfig filterConfig) throws ServletException {
    	// do nothing;
    }

	public void setAuthPipeline(Pipeline authPipeline) {
    	this.authPipeline = authPipeline;
    }

    public void setBypassForPatterns(List<String> bypassForPatterns) {
        this.bypassForPatterns = bypassForPatterns;
    }

	public void setSessionFactory(JCRSessionFactory sessionFactory) {
    	this.sessionFactory = sessionFactory;
    }

	public void setUserManagerService(JahiaUserManagerService userManagerService) {
    	this.userManagerService = userManagerService;
    }
}
