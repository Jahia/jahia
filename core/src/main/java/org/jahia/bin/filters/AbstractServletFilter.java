package org.jahia.bin.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by kevan on 11/03/14.
 */
public abstract class AbstractServletFilter implements javax.servlet.Filter{

    private String[] urlPatterns;
    private boolean matchAllUrls = false;

    @Override
    public abstract void init(FilterConfig filterConfig) throws ServletException;

    @Override
    public abstract void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException;

    @Override
    public abstract void destroy();

    public String[] getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(String[] urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    public boolean isMatchAllUrls() {
        return matchAllUrls;
    }

    public void setMatchAllUrls(boolean matchAllUrls) {
        this.matchAllUrls = matchAllUrls;
    }
}
