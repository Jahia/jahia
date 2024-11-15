/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provide a servlet filter to handle servlet filters provided by the modules.
 * An inner filter chain is created in this composite filter to execute the additional filters.
 *
 * @author kevan
 */
public class CompositeFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(CompositeFilter.class);

    private List<AbstractServletFilter> filters = new ArrayList<>();
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
            filter.init(filterConfig.getServletContext());
        }
        logger.info("Registering servlet filter {}", filter);
        this.filters.add(filter);
        Collections.sort(this.filters);
    }

    public void unregisterFilter(AbstractServletFilter filter) {
        logger.info("Unregistering servlet filter {}", filter);
        try {
            filter.destroy();
        } finally {
            filters.remove(filter);
            Collections.sort(this.filters);
        }
    }

    public void setFilters(List<AbstractServletFilter> filters) {
        this.filters = filters;
        Collections.sort(this.filters);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        for (AbstractServletFilter filter : filters){
            try {
                filter.init(filterConfig.getServletContext());
            } catch (Exception e) {
                logger.error("Error when executing filter",e);
            }
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

        private boolean isFilterMatchPath(AbstractServletFilter filter, HttpServletRequest request){
            try {
                if (filter.getDispatcherTypes() != null && !filter.getDispatcherTypes().contains(request.getDispatcherType().name())) {
                    return false;
                }

                // Check the specific "*" special URL pattern, which also matches
                // named dispatches
                if (filter.isMatchAllUrls())
                    return true;

                int length = request.getContextPath().length();
                String requestPath = length > 0 ? request.getRequestURI().substring(length) : request.getRequestURI();

                // Match on context relative request path
                String[] testPaths = filter.getUrlPatterns();
                if (testPaths != null) {
                    for (String testPath : testPaths) {
                        if (matchFiltersURL(testPath, requestPath)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot check condition for filter {}",filter.getFilterName(), e);
            }

            // No match
            return false;
        }
    }

    public static boolean matchFiltersURL(String testPath, String requestPath) {

        if (testPath == null) {
            return false;
        }

        // Case 1 - Exact Match
        if (testPath.equals(requestPath)) {
            return true;
        }

        // Case 2 - Path Match ("/.../*")
        if (testPath.equals("/*")) {
            return true;
        }
        if (testPath.endsWith("/*")) {
            return testPath.regionMatches(0, requestPath, 0, testPath.length() - 2) && (requestPath.length() == (testPath.length() - 2) || '/' == requestPath.charAt(testPath.length() - 2));
        }

        // Case 3 - Extension Match
        if (testPath.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) && (period != requestPath.length() - 1) && ((requestPath.length() - period) == (testPath.length() - 1))) {
                return (testPath.regionMatches(2, requestPath, period + 1, testPath.length() - 2));
            }
        }

        // Case 4 - "Default" Match
        return false; // NOTE - Not relevant for selecting filters

    }
}
