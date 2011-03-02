/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
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
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Servlet filter for setting proper cache control response headers, based on
 * the configured resource patterns.
 * 
 * @author Sergiy Shyrkov
 */
public class ResponseCacheControlFilter implements Filter, InitializingBean {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ResponseCacheControlFilter.class);

    private Pattern cachedResources;

    private long expires;

    private Pattern foreverCachedResources;

    private long neverExpires;

    private Pattern noCacheResources;

    private boolean enabled;

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (enabled) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            String uri = request.getRequestURI();
            if (logger.isDebugEnabled()) {
                 logger.debug("Resource requested: " + uri);
            }
            if (noCacheResources.matcher(uri).matches()) {
                WebUtils.setNoCacheHeaders(response);
                if (logger.isDebugEnabled()) {
                    logger.debug("Disabling cache for '" + uri + "'");
                }
            } else if (neverExpires > 0 && foreverCachedResources.matcher(uri).matches()) {
            	WebUtils.setCacheHeaders(neverExpires, response);
                if (logger.isDebugEnabled()) {
                    logger.debug("Long-term caching enabled for '" + uri + "'");
                }
            } else if (expires > 0 && cachedResources.matcher(uri).matches()) {
            	WebUtils.setCacheHeaders(expires, response);
                if (logger.isDebugEnabled()) {
                    logger.debug("Caching enabled for '" + uri + "'");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("URI '" + uri + "' does not match any pattern or expires values are set to '0'");
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void init(FilterConfig config) throws ServletException {
        // do nothing
    }

    public void setCachedResources(String cachedResources) {
        this.cachedResources = StringUtils.isNotBlank(cachedResources) ? Pattern.compile(cachedResources.trim()) : null;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public void setForeverCachedResources(String foreverCachedResources) {
        this.foreverCachedResources = StringUtils.isNotBlank(foreverCachedResources) ? Pattern
                .compile(foreverCachedResources.trim()) : null;
    }

    public void setNeverExpires(long neverExpires) {
        this.neverExpires = neverExpires;
    }

    public void setNoCacheResources(String noCacheResources) {
        this.noCacheResources = StringUtils.isNotBlank(noCacheResources) ? Pattern.compile(noCacheResources.trim())
                : null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void afterPropertiesSet() throws Exception {
        if (enabled) {
            logger.info("ResponseCacheControlFilter started using following configuration: " + "expires=" + expires
                    + " ms, neverExpires=" + neverExpires + " ms, cachedResources=" + cachedResources.pattern()
                    + ", foreverCachedResources=" + foreverCachedResources.pattern() + ", noCacheResources="
                    + noCacheResources.pattern());
        } else {
            logger.info("ResponseCacheControlFilter is disabled");
        }
    }

}