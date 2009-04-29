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
package org.jahia.bin.filters;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

/**
 * Servlet filter for setting proper cache control response headers, based on
 * the configured resource patterns.
 * 
 * @author Sergiy Shyrkov
 */
public class ResponseCacheControlFilter implements Filter {

    private static final Logger logger = Logger
            .getLogger(ResponseCacheControlFilter.class);

    private Pattern cachedResources;

    private long expires;

    private Pattern foreverCachedResources;

    private long neverExpires;

    private Pattern noCacheResources;

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest servletRequest,
            ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        // logger.info("Resource requested: " + uri);
        if (noCacheResources.matcher(uri).matches()) {
            setNoCacheHeaders(response);
            if (logger.isDebugEnabled()) {
                logger.debug("Disabling cache for '" + uri + "'");
            }
        } else if (neverExpires > 0
                && foreverCachedResources.matcher(uri).matches()) {
            setCacheHeaders(neverExpires, response);
            if (logger.isDebugEnabled()) {
                logger.debug("Long-term caching enabled for '" + uri + "'");
            }
        } else if (expires > 0 && cachedResources.matcher(uri).matches()) {
            setCacheHeaders(expires, response);
            if (logger.isDebugEnabled()) {
                logger.debug("Caching enabled for '" + uri + "'");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("URI '"
                                + uri
                                + "' does not match any pattern or expires valueas are set to '0'");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void init(FilterConfig config) throws ServletException {
        expires = NumberUtils.toLong(config.getInitParameter("Expires"), 0);
        neverExpires = NumberUtils.toLong(config
                .getInitParameter("Never-Expires"), 0);

        String pattern = config.getInitParameter("cachedResources");
        cachedResources = StringUtils.isNotBlank(pattern) ? Pattern
                .compile(pattern.trim()) : null;

        pattern = config.getInitParameter("foreverCachedResources");
        foreverCachedResources = StringUtils.isNotBlank(pattern) ? Pattern
                .compile(pattern.trim()) : null;

        pattern = config.getInitParameter("noCacheResources");
        noCacheResources = StringUtils.isNotBlank(pattern) ? Pattern
                .compile(pattern.trim()) : null;

        logger.info("Filter started using following configuration: "
                + "expires=" + expires + " ms, neverExpires=" + neverExpires
                + " ms, cachedResources=" + cachedResources.pattern()
                + ", foreverCachedResources="
                + foreverCachedResources.pattern() + ", noCacheResources="
                + noCacheResources.pattern());
    }

    private void setCacheHeaders(long expires, HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=" + expires);
        response.setDateHeader("Expires", System.currentTimeMillis() + expires
                * 1000L);
    }

    public static void setNoCacheHeaders(HttpServletResponse response) {
        /*response
                .setHeader("Cache-Control",
                        "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");*/
        response.setHeader("Pragma", "no-cache"); 
        //response.setDateHeader("Expires", 295075800000L);
    }

}