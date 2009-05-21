/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.errors;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaErrorEvent;
import org.jahia.services.events.JahiaEventGeneratorBaseService;

/**
 * Error event filter that fires <code>errorOccurred</code> Jahia event.
 * 
 * @author Sergiy Shyrkov
 */
public class ErrorEventFilter implements Filter {

    private static Logger logger = Logger.getLogger(ErrorEventFilter.class);

    private int minimalErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        Boolean alreadyForwarded = (Boolean) request
                .getAttribute("org.jahia.exception.forwarded");
        if (alreadyForwarded == null || !alreadyForwarded.booleanValue()) {
            handle((HttpServletRequest) request, (HttpServletResponse) response);
        }

        filterChain.doFilter(request, response);
    }

    protected Throwable getException(HttpServletRequest request) {
        Throwable ex = (Throwable) request
                .getAttribute("javax.servlet.error.exception");
        ex = ex != null ? ex : (Throwable) request
                .getAttribute("org.jahia.exception");

        return ex;
    }

    protected void handle(HttpServletRequest request,
            HttpServletResponse response) {

        int code = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        code = code != 0 ? code : SC_INTERNAL_SERVER_ERROR;
        if (code >= minimalErrorCode) {
            Throwable ex = getException(request);

            if (logger.isDebugEnabled()) {
                logger.debug("firing errorOccurred event with error code '"
                        + code + "'", ex);
            }

            JahiaEventGeneratorBaseService.getInstance().fireErrorOccurred(
                    new JahiaErrorEvent(this, ex, code, request, response));

        } else {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("The error code '"
                                + code
                                + "' is less than the minimal required to fire an event ("
                                + minimalErrorCode
                                + ")."
                                + " Adjust the 'minimalErrorCode' filter init parameter to change this handling.");
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig cfg) throws ServletException {
        // do nothing
        minimalErrorCode = cfg.getInitParameter("minimalErrorCode") != null ? Integer
                .parseInt(cfg.getInitParameter("minimalErrorCode"))
                : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

}
