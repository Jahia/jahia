package org.jahia.bin.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;

/**
 * A simple servlet filter to redirect call to /modules resource to OSGi /osgi resources.
 */
public class ModuleToOSGiResourceFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.filterConfig = config;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/modules/")) {
            URL resourceURL = filterConfig.getServletContext().getResource(requestURI);
            if (resourceURL == null) {
                // resource was not found, we will try under the /osgi location.
                String modulePart = requestURI.substring("/modules/".length());
                String newURI = "/osgi/" + modulePart;
                req.getRequestDispatcher(newURI).forward(req, res);
            } else {
                // resource exists, simply continue normally.
                chain.doFilter(req,res);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        //
    }
}
