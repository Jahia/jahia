/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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