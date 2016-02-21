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
package org.jahia.bin.filters;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.errors.ErrorServlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Allows temporary disabling request serving and switching to a maintenance mode.
 * 
 * @author Sergiy Shyrkov
 */
public class MaintenanceFilter implements Filter {

    private Set<Pattern> allowedResources = new HashSet<Pattern>();

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean block = Jahia.isMaintenance();
        if (block) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = StringUtils.substringAfter(httpRequest.getRequestURI(),
                    httpRequest.getContextPath());
            for (Pattern resourcePattern : allowedResources) {
                if (resourcePattern.matcher(uri).matches()) {
                    block = false;
                    break;
                }
            }
        }
        if (block) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    ErrorServlet.MAINTENANCE_MODE);
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void setAllowedResources(Set<String> allowedResources) {
        for (String regex : allowedResources) {
            this.allowedResources.add(Pattern.compile(regex));
        }
    }

}
