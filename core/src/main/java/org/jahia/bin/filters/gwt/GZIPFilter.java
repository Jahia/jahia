/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin.filters.gwt;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * User: ktlili
 * Date: 21 oct. 2008
 * Time: 12:33:32
 */
public class GZIPFilter implements Filter {
    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpRequest.getRequestURI();
        if (!requestURI.endsWith(".gz") && requestURI.contains(".cache.") && false) {
            // check for gzip support
            String acceptEncoding = httpRequest.getHeader("accept-encoding");
            if (acceptEncoding != null && acceptEncoding.indexOf("gzip") != -1) {
                // foward to .gz file
                try {
                    RequestDispatcher rd = filterConfig.getServletContext().getRequestDispatcher(requestURI + ".gz");
                    rd.forward(servletRequest, servletResponse);
                } catch (ServletException e) {
                    /*continue  */
                }
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);

    }

    public void destroy() {
        filterConfig = null;
    }
}
