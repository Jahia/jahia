package org.jahia.bin.filters;

import org.apache.commons.lang.ArrayUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevan on 28/02/14.
 */
public class CompositeFilter implements Filter {
    private List<AbstractServletFilter> filters = new ArrayList<AbstractServletFilter>();
    private FilterConfig filterConfig;

    public void destroy() {
        for (int i = filters.size(); i-- > 0;) {
            AbstractServletFilter filter = filters.get(i);
            filter.destroy();
        }
    }

    public void registerFilter(AbstractServletFilter filter) throws ServletException {
        if(filterConfig != null){
            filter.init(filterConfig);
        }
        this.filters.add(filter);
    }

    public void unregisterFilter(AbstractServletFilter filter) {
        filter.destroy();
        filters.remove(filter);
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
        new VirtualFilterChain(chain, filters).doFilter(request, response);
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

        String requestPath = request.getRequestURI().substring(request.getContextPath().length());

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

    private static boolean matchFiltersURL(String testPath, String requestPath) {

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
