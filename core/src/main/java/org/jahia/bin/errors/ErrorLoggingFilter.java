/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin.errors;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Error logging filter that is called before an error page, configured in the
 * Web application deployment descriptor.
 *
 * @author Sergiy Shyrkov
 */
public class ErrorLoggingFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(ErrorLoggingFilter.class);

    private static Throwable lastMailedException = null;
    private static int lastMailedExceptionOccurences = 0;

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */

    public void destroy() {
        // nothing to do
        ErrorFileDumper.shutdown();
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


    protected void dumpToFile(HttpServletRequest request) {
        try {
            Throwable t = getException(request);

            int code = (Integer) request
                    .getAttribute("javax.servlet.error.status_code");

            code = code != 0 ? code : SC_INTERNAL_SERVER_ERROR;

            if (code < 500) {
                logger.debug("Status code below 500, will not dump error to file");
                return;
            }

            if (!ErrorFileDumper.isShutdown()) {
                ErrorFileDumper.dumpToFile(t, request);
            }
        } catch (Throwable throwable) {
            logger.warn("Error creating error file", throwable);
        }

    }

    protected void emailAlert(HttpServletRequest request,
                              HttpServletResponse response) {

        Throwable t = getException(request);
        try {
            if (lastMailedException != null && t != null
                    && t.toString().equals(lastMailedException.toString())) {
                lastMailedExceptionOccurences++;
                if (lastMailedExceptionOccurences < SettingsBean.getInstance().getMail_maxRegroupingOfPreviousException()) {
                    return;
                }
            }

            StringWriter msgBodyWriter = ErrorFileDumper.generateErrorReport(new ErrorFileDumper.HttpRequestData(request), t, lastMailedExceptionOccurences, lastMailedException);

            ServicesRegistry.getInstance().getMailService().sendMessage(null, null, null, null,
                    "Server Error: " + t.getMessage(), msgBodyWriter
                            .toString());

            logger.debug("Mail was sent successfully.");

            lastMailedException = t;
            lastMailedExceptionOccurences = 1;

        } catch (Exception ex) {
            logger.warn("Error sending an e-mail alert: " + ex.getMessage(), ex);
        }
    }


    protected Throwable getException(HttpServletRequest request) {
        Throwable ex = (Throwable) request
                .getAttribute("javax.servlet.error.exception");
        ex = ex != null ? ex : (Throwable) request
                .getAttribute("org.jahia.exception");

        return ex;
    }

    protected String getLogMessage(HttpServletRequest request) {

        Throwable ex = getException(request);

        String message = (String) request
                .getAttribute("javax.servlet.error.message");

        Integer code = (Integer) request
                .getAttribute("javax.servlet.error.status_code");

        switch (code.intValue()) {
            case SC_NOT_FOUND:
                message = "Requested resource is not available: "
                        + request.getAttribute("javax.servlet.error.request_uri");
                break;

            case SC_UNAUTHORIZED:
                message = "Authorization required for resource: "
                        + request.getAttribute("javax.servlet.error.request_uri");
                break;

            case SC_FORBIDDEN:
                message = "Access denied for resource: "
                        + request.getAttribute("javax.servlet.error.request_uri");
                break;

            default:
                if (message != null) {
                    if (ex != null && StringUtils.isNotEmpty(ex.getMessage())
                            && !message.equals(ex.getMessage())) {
                        message = message + ". Error message: " + ex.getMessage();
                    }
                } else {
                    if (ex != null && StringUtils.isNotEmpty(ex.getMessage())) {
                        message = ex.getMessage();
                    } else {
                        message = "Unexpected exception occurred";
                    }
                }
                break;
        }

        if (logger.isInfoEnabled()) {
            String requestInfo = (String) request
                    .getAttribute("org.jahia.exception.requestInfo");
            if (requestInfo != null) {
                message = message
                        + "\n"
                        + request
                        .getAttribute("org.jahia.exception.requestInfo");
            }
        }

        return message;
    }

    protected void handle(HttpServletRequest request,
                          HttpServletResponse response) {

        // add request information
        request.setAttribute("org.jahia.exception.requestInfo",
                getRequestInfo(request));

        logDebugInfo(request, response);

        if (HttpServletResponse.SC_SERVICE_UNAVAILABLE == (Integer) request
                .getAttribute("javax.servlet.error.status_code")
                && (StringUtils.equals(ErrorServlet.MAINTENANCE_MODE,
                        (String) request.getAttribute("javax.servlet.error.message")) || StringUtils
                        .equals(ErrorServlet.LICENSE_TERMS_VIOLATION_MODE,
                                (String) request.getAttribute("javax.servlet.error.message")))) {
            return;
        }

        logException(request, response);

        if (SettingsBean.getInstance().isDumpErrorsToFiles()) {
            dumpToFile(request);
        }

        if (isMailServiceEnabled() && isEmailAlertRequired(request, response)) {
            emailAlert(request, response);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */

    public void init(FilterConfig cfg) throws ServletException {
        // do nothing
        if (ErrorFileDumper.isShutdown()) {
            ErrorFileDumper.start();
        }
    }

    protected boolean isEmailAlertRequired(HttpServletRequest request,
                                           HttpServletResponse response) {

        Throwable error = getException(request);

        return error != null
                && (error instanceof JahiaException)
                && ServicesRegistry.getInstance().getMailService()
                .getSettings().getNotificationSeverity() != 0
                && ServicesRegistry.getInstance().getMailService()
                .getSettings().getNotificationSeverity() <= ((JahiaException) error)
                .getSeverity();
    }

    private boolean isMailServiceEnabled() {
        return ServicesRegistry.getInstance().getMailService().isEnabled();
    }

    protected void logDebugInfo(HttpServletRequest request,
                                HttpServletResponse response) {

        if (logger.isDebugEnabled()) {
            logger.debug("Handling exception for request ["
                    + request.getAttribute("javax.servlet.error.request_uri")
                    + "]:"
                    + "\n"
                    + "Status code: "
                    + request.getAttribute("javax.servlet.error.status_code")
                    + "\n"
                    + "Error message: "
                    + request.getAttribute("javax.servlet.error.message")
                    + "\n"
                    + "Exception type: "
                    + request
                    .getAttribute("javax.servlet.error.exception_type")
                    + "\n" + "Exception: "
                    + request.getAttribute("javax.servlet.error.exception")
                    + "\n" + "Servlet name: "
                    + request.getAttribute("javax.servlet.error.servlet_name"));
        }
    }

    protected void logException(HttpServletRequest request,
                                HttpServletResponse response) {

        Throwable ex = getException(request);
        
        int code = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        code = code != 0 ? code : SC_INTERNAL_SERVER_ERROR;

        String message = getLogMessage(request);

        if (code >= 500) {
            if (ex != null) {
                logger.error(message, ex);
            } else {
                logger.error(message);
            }
        } else {
            if (ex != null && logger.isDebugEnabled()) {
                logger.debug(message, ex);
            } else {
                if (SC_UNAUTHORIZED == code) {
                    logger.info(message);
                } else {
                    logger.warn("[Error code: " + code + "]"+ (message != null && message.length() > 0 ? ": " + message : ""));
                }
            }
        }
    }

    /**
     * Returns the request information for logging purposes.
     *
     * @param request the http request object
     * @return the request information for logging purposes
     */
    private static String getRequestInfo(HttpServletRequest request) {
        StringBuilder info = new StringBuilder(512);
        if (request != null) {
            String uri = (String) request
                    .getAttribute("javax.servlet.error.request_uri");
            String queryString = (String) request
                    .getAttribute("javax.servlet.forward.query_string");
            if (StringUtils.isNotEmpty(queryString)) {
                uri = uri + "?" + queryString;
            }
            info.append("Request information:").append("\nURL: ").append(uri)
                    .append("\nMethod: ").append(request.getMethod()).append(
                    "\nProtocol: ").append(request.getProtocol())
                    .append("\nRemote host: ").append(request.getRemoteHost())
                    .append("\nRemote address: ").append(
                    request.getRemoteAddr()).append("\nRemote port: ")
                    .append(request.getRemotePort()).append("\nRemote user: ")
                    .append(request.getRemoteUser()).append("\nSession ID: ")
                    .append(request.getRequestedSessionId()).append("\nSession user: ")
                    .append(getUserInfo(request)).append("\nRequest headers: ");

            @SuppressWarnings("unchecked")
            Iterator<String> headerNames = new EnumerationIterator(request.getHeaderNames());
            while (headerNames.hasNext()) {
                String headerName = headerNames.next();
                String headerValue = request.getHeader(headerName);
                info.append("\n  ").append(headerName).append(": ").append(
                        headerValue);
            }
        }
        return info.toString();
    }

    protected static String getUserInfo(HttpServletRequest request) {
        String info = null;

        // processing context available?
        ProcessingContext ctx = (ProcessingContext) request
                .getAttribute("org.jahia.params.ParamBean");
        info = ctx != null && ctx.getUser() != null ? ctx.getUser()
                .getUsername() : null;

        if (null == info) {
            // try out session user
            JahiaUser user = null;
            try {
                user = (JahiaUser) request.getSession(true).getAttribute(
                        ProcessingContext.SESSION_USER);
                info = user != null ? user.getUsername() : null;
            } catch (IllegalStateException ex) {
                // ignore it
            }
        }

        // last chance: request's user principal
        info = info != null ? info : String.valueOf(request.getUserPrincipal());

        return info;
    }


}
