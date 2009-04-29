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
