/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.usermanager.JahiaUserManagerService;
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
                HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
                // the following check is done to make sure that the user hasn't been logged out between the reading
                // from the session and the writing back to it. In the case of the logout, the isAuthRetrievedFromSession
                // will return true, but the session will not contain a user because the old session was invalidated
                // and a new one was created. In that case we will not insert the user back into the session.
                // In all other cases we set the user in the session, even if we read from it because this is an expected
                // behavior in the case where the user was updated in the backend (JCR) but not yet in the session.
                // For more information see : https://jira.jahia.org/browse/BACKLOG-5166 and https://jira.jahia.org/browse/BACKLOG-5207
                if (authValveContext == null || !httpSession.isNew() || !authValveContext.isAuthRetrievedFromSession()) {
                    httpSession.setAttribute(Constants.SESSION_USER, sessionFactory.getCurrentUser());
                    // an IllegalStateException might be raised by the setAttribute call if the session was
                    // invalidated, which is the expected behavior because we do want to interrupt the
                    // processing in that case.
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
