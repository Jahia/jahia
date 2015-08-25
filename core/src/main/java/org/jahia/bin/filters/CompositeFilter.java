/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.bin.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide a servlet filter to handle servlet filters provided by the modules.
 * An inner filter chain is created in this composite filter to execute the additional filters.
 *
 * @author kevan
 */
public class CompositeFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(CompositeFilter.class);
    
    private List<AbstractServletFilter> filters = new ArrayList<AbstractServletFilter>();
    private FilterConfig filterConfig;

    public void destroy() {
        for (int i = filters.size(); i-- > 0;) {
            AbstractServletFilter filter = filters.get(i);
            filter.destroy();
        }
    }

    /**
     * Tests if a filter is already registered in the composite filter
     * @param filter the filter we want to test for presence in the composite
     * @return true if the filter is already present, false otherwise
     */
    public boolean containsFilter(AbstractServletFilter filter) {
        return filters.contains(filter);
    }

    public void registerFilter(AbstractServletFilter filter) throws ServletException {
        if(filterConfig != null){
            filter.init(filterConfig);
        }
        logger.info("Registering servlet filter {}", filter);
        this.filters.add(filter);
    }

    public void unregisterFilter(AbstractServletFilter filter) {
        logger.info("Unregistering servlet filter {}", filter);
        try {
            filter.destroy();
        } finally {
            filters.remove(filter);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        for (AbstractServletFilter filter : filters){
            filter.init(filterConfig);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        if (filters.isEmpty()) {
            chain.doFilter(request, response);
        } else {
            new VirtualFilterChain(chain, filters).doFilter(request, response);
        }
    }

    private static class VirtualFilterChain implements FilterChain {
        private final FilterChain originalChain;
        private final List<? extends AbstractServletFilter> additionalFilters;
        private int currentPosition = 0;

        private VirtualFilterChain(FilterChain chain, List<? extends AbstractServletFilter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
        }

        public void doFilter(final ServletRequest request, final ServletResponse response) throws IOException,
                ServletException {

            AbstractServletFilter nextFilter = getNext(request);
            if(nextFilter == null){
                originalChain.doFilter(request, response);
            }else {
                nextFilter.doFilter(request, response, this);
            }

        }

        private AbstractServletFilter getNext(final ServletRequest request){
            if (currentPosition < additionalFilters.size()) {
                AbstractServletFilter next = additionalFilters.get(currentPosition);
                currentPosition ++;
                if(request instanceof HttpServletRequest && isFilterMatchPath(next, (HttpServletRequest) request)) {
                    return next;
                }else {
                    return getNext(request);
                }
            }
            return null;
        }
    }

    private static boolean isFilterMatchPath(AbstractServletFilter filter, HttpServletRequest request){
        // Check the specific "*" special URL pattern, which also matches
        // named dispatches
        if (filter.isMatchAllUrls())
            return (true);

        int length = request.getContextPath().length();
        String requestPath = length > 0 ? request.getRequestURI().substring(length) : request.getRequestURI();

        // Match on context relative request path
        String[] testPaths = filter.getUrlPatterns();

        for (String testPath : testPaths) {
            if (matchFiltersURL(testPath, requestPath)) {
                return (true);
            }
        }

        // No match
        return (false);
    }

    public static boolean matchFiltersURL(String testPath, String requestPath) {

        if (testPath == null)
            return (false);

        // Case 1 - Exact Match
        if (testPath.equals(requestPath))
            return (true);

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*"))
            return (true);
        if (testPath.endsWith("/*")) {
            if (testPath.regionMatches(0, requestPath, 0,
                    testPath.length() - 2)) {
                if (requestPath.length() == (testPath.length() - 2)) {
                    return (true);
                } else if ('/' == requestPath.charAt(testPath.length() - 2)) {
                    return (true);
                }
            }
            return (false);
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period)
                    == (testPath.length() - 1))) {
                return (testPath.regionMatches(2, requestPath, period + 1,
                        testPath.length() - 2));
            }
        }

        // Case 4 - "Default" Match
        return (false); // NOTE - Not relevant for selecting filters

    }
}
