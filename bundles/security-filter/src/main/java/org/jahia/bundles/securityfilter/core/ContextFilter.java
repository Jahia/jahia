package org.jahia.bundles.securityfilter.core;

import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.services.securityfilter.PermissionService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ContextFilter extends AbstractServletFilter {

    private PermissionService permissionService;

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        try {
            permissionService.initScopes(httpServletRequest);
            chain.doFilter(request, response);
        } finally {
            permissionService.resetScopes();
        }
    }

    @Override
    public void destroy() {

    }
}
