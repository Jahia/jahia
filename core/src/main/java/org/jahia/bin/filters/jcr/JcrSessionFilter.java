/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.jahia.registries.ServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.params.valves.AuthValveContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUserManagerService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet filter that performs user authentication and the initialization of
 * the JCR session for current user.
 * 
 * @author Thomas Draier
 */
public class JcrSessionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(JcrSessionFilter.class);
    
    private Pipeline authPipeline;

    private JCRSessionFactory sessionFactory;

    private JahiaUserManagerService userManagerService;
    
    public void destroy() {
    	// do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        boolean initialized = SpringContextSingleton.getInstance().isInitialized();
        try {
            if (initialized) {
                try {
                    sessionFactory.setCurrentUser(null);
                    authPipeline.invoke(new AuthValveContext((HttpServletRequest) servletRequest,
                            (HttpServletResponse) servletResponse, sessionFactory));
                } catch (PipelineException pe) {
                    logger.error("Error while authorizing user", pe);
                }
            }
            if (sessionFactory.getCurrentUser() != null && sessionFactory.getCurrentUser().isAccountLocked()) {
                sessionFactory.setCurrentUser(null);
            }

            if (sessionFactory.getCurrentUser() == null) {
                sessionFactory
                        .setCurrentUser(userManagerService.lookupUser(JahiaUserManagerService.GUEST_USERNAME));
            } else {
                ((HttpServletRequest)servletRequest).getSession().setAttribute(Constants.SESSION_USER, sessionFactory.getCurrentUser());
            }

            filterChain.doFilter (servletRequest, servletResponse );
        } finally {
            if (initialized) {
                endRequest();
            }
        }
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
        sessionFactory.setOnlyCheckLiveRoles(false);
        sessionFactory.setCurrentServletPath(null);
        sessionFactory.setCurrentPreviewDate(null);
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

	public void setSessionFactory(JCRSessionFactory sessionFactory) {
    	this.sessionFactory = sessionFactory;
    }

	public void setUserManagerService(JahiaUserManagerService userManagerService) {
    	this.userManagerService = userManagerService;
    }
}
