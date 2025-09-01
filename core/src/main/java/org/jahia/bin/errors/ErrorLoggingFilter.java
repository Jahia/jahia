/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin.errors;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.SessionIdHashingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    private static final Logger logger = LoggerFactory.getLogger(ErrorLoggingFilter.class);

    private static Throwable previousException = null;
    private static int previousExceptionOccurrences = 0;

    @Override
    public void destroy() {
        ErrorFileDumper.shutdown();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        Boolean alreadyForwarded = (Boolean) request.getAttribute("org.jahia.exception.forwarded");
        if (alreadyForwarded == null || !alreadyForwarded.booleanValue()) {
            handle((HttpServletRequest) request, (HttpServletResponse) response);
        }
        filterChain.doFilter(request, response);
    }


    protected static void dumpToFile(HttpServletRequest request) {
        if (ErrorFileDumper.isShutdown()) {
            return;
        }
        try {
            Throwable t = getException(request);
            int code = (Integer) request.getAttribute("javax.servlet.error.status_code");
            code = code != 0 ? code : SC_INTERNAL_SERVER_ERROR;
            if (code < 500) {
                logger.debug("Status code below 500, will not dump error to file");
                return;
            }
            ErrorFileDumper.dumpToFile(t, request);
        } catch (Exception throwable) {
            logger.warn("Error creating error file", throwable);
        }
    }

    protected static void emailAlert(HttpServletRequest request, HttpServletResponse response) {

        Throwable exception = getException(request);
        try {

            Throwable previousExceptionToMail;
            int previousExceptionOccurrencesToMail;

            synchronized (ErrorLoggingFilter.class) {
                if (previousException != null && exception != null && exception.toString().equals(previousException.toString())) {
                    previousExceptionOccurrences++;
                    if (previousExceptionOccurrences < SettingsBean.getInstance().getMail_maxRegroupingOfPreviousException()) {
                        return;
                    }
                }
                previousExceptionToMail = previousException;
                previousExceptionOccurrencesToMail = previousExceptionOccurrences;
                previousException = exception;
                previousExceptionOccurrences = 1;
            }

            StringWriter msgBodyWriter = ErrorFileDumper.generateErrorReport(new ErrorFileDumper.HttpRequestData(request), exception, previousExceptionOccurrencesToMail, previousExceptionToMail);
            ServicesRegistry.getInstance().getMailService().sendMessage(null, null, null, null, "Server Error: " + (exception != null ? exception.getMessage() : ""), msgBodyWriter.toString());

            logger.debug("Mail was sent successfully.");
        } catch (Exception ex) {
            logger.warn("Error sending an e-mail alert: " + ex.getMessage(), ex);
        }
    }


    protected static Throwable getException(HttpServletRequest request) {
        Throwable ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
        ex = ex != null ? ex : (Throwable) request.getAttribute("org.jahia.exception");
        return ex;
    }

    protected static String getLogMessage(HttpServletRequest request) {

        Throwable ex = getException(request);
        String message = (String) request.getAttribute("javax.servlet.error.message");
        Integer code = (Integer) request.getAttribute("javax.servlet.error.status_code");

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

            case SC_SERVICE_UNAVAILABLE:
                message = "Service unavailable";
                break;

            default:
                if (StringUtils.isNotEmpty(message)) {
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

    protected static void handle(HttpServletRequest request, HttpServletResponse response) {

        // add request information
        request.setAttribute("org.jahia.exception.requestInfo", getRequestInfo(request));

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

        dumpToFile(request);

        if (isMailServiceEnabled() && isEmailAlertRequired(request, response)) {
            emailAlert(request, response);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig cfg) throws ServletException {
        // do nothing
    }

    protected static boolean isEmailAlertRequired(HttpServletRequest request, HttpServletResponse response) {

        Throwable error = getException(request);

        return error != null
                && (error instanceof JahiaException)
                && ServicesRegistry.getInstance().getMailService().getSettings().getNotificationSeverity() != 0
                && ServicesRegistry.getInstance().getMailService().getSettings().getNotificationSeverity() <= ((JahiaException) error).getSeverity();
    }

    private static boolean isMailServiceEnabled() {
        return ServicesRegistry.getInstance().getMailService().isEnabled();
    }

    protected static void logDebugInfo(HttpServletRequest request, HttpServletResponse response) {

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

    protected static void logException(HttpServletRequest request, HttpServletResponse response) {

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
                    .append(SessionIdHashingUtils.getHashedSessionId(request, true)).append("\nSession user: ")
                    .append(getUserInfo(request)).append("\nRequest headers: ");

            @SuppressWarnings("unchecked")
            Iterator<String> headerNames = new EnumerationIterator(request.getHeaderNames());
            while (headerNames.hasNext()) {
                String headerName = headerNames.next();
                if (!headerName.equalsIgnoreCase("authorization")) {
                    String headerValue = request.getHeader(headerName);
                    info.append("\n  ").append(headerName).append(": ").append(
                            headerValue);
                }
            }
        }
        return info.toString();
    }

    protected static String getUserInfo(HttpServletRequest request) {

        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user == null) {
            try {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    user = (JahiaUser) session.getAttribute(Constants.SESSION_USER);
                }
            } catch (IllegalStateException ex) {
                // ignore it
            }
        }
        String info = user != null ? user.getUsername() : null;

        // last chance: request's user principal
        info = info != null ? info : String.valueOf(request.getUserPrincipal());

        return info;
    }
}
