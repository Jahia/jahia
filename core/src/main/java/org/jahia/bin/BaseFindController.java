/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for controllers, exposing repository nodes via JCR Query API.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseFindController extends JahiaController {

    public static class PropertyFilter {
        
        public static final PropertyFilter EMPTY = new PropertyFilter();

        private Set<String> excludes;

        private Set<String> includes;

        public Set<String> getExcludes() {
            return excludes;
        }

        public Set<String> getIncludes() {
            return includes;
        }

        public void setExcludes(Set<String> excludes) {
            this.excludes = excludes != null && excludes.size() == 0 ? null : excludes;
        }

        public void setIncludes(Set<String> includes) {
            this.includes = includes != null && includes.size() == 0 ? null : includes;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(BaseFindController.class);

    protected int defaultLimit = 20;
    
    protected boolean disabled;

    protected int hardLimit = 100;

    /**
     * The default number of results this controller will return if no limit was specified as a request parameter.
     * 
     * @param defaultLimit
     *            default number of results this controller will return if no limit was specified as a request parameter
     */
    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    /**
     * Sets the "hard" limit, i.e. the maximum number of results this controller will return no matter what limit was specified as a request
     * parameter. This is done to prevent denial of service attacks or just limit the number of results to some "reasonable" amount.
     * 
     * @param hardLimit
     *            the maximum number of results this controller will return
     */
    public void setHardLimit(int hardLimit) {
        this.hardLimit = hardLimit;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (disabled) {
            response.sendError(SC_NOT_FOUND);
            return null;
        }
        long startTime = System.currentTimeMillis();
        String sessionId = null;
        try {
            if (logger.isInfoEnabled()) {
                sessionId = request.getSession().getId();
            }
            if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
                handle(request, response);
            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS, POST");
            } else {
                response.sendError(SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(request.getRequestURI());
                JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
                if (user != null) {
                    sb.append("] user=[").append(user.getUsername());
                }
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[").append(
                        sessionId).append("] in [").append(
                        System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }
        return null;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // implemented in subclasses
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

}
