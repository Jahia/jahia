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

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;

/**
 * Error logging filter that is called before an error page, configured in the
 * Web application deployment descriptor.
 * 
 * @author Sergiy Shyrkov
 */
public class ErrorLoggingFilter implements Filter {

    private static Logger logger = Logger.getLogger(ErrorLoggingFilter.class);

    private static Throwable lastMailedException = null;
    private static int lastMailedExceptionOccurences = 0;

    private static Throwable lastFileDumpedException = null;
    private static int lastFileDumpedExceptionOccurences = 0;
    private static long exceptionCount = 0L;
    
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

            if (lastFileDumpedException != null && t != null
                    && t.toString().equals(lastFileDumpedException.toString())) {
                lastFileDumpedExceptionOccurences++;
                if (lastFileDumpedExceptionOccurences < SettingsBean.getInstance().getFileDumpMaxRegroupingOfPreviousException()) {
                    logger.debug("Same exception, not logging since below fileDumpMaxRegroupingOfPreviousException");
                    return;
                }
            }

            final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

            File errorDir = new File(sysTempDir, "jahia-errors");
            if (!errorDir.exists()) {
                errorDir.mkdir();
            }
            Date now = new Date();
            SimpleDateFormat directoryDateFormat = new SimpleDateFormat("yyyy_MM_dd");
            File todaysDirectory = new File(errorDir, directoryDateFormat.format(now));
            if (!todaysDirectory.exists()) {
                todaysDirectory.mkdir();
            }
            SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss_SSS");
            File errorFile = new File(todaysDirectory, "error-" + fileDateFormat.format(now) + "-" + Long.toString(exceptionCount) + ".txt");
            StringWriter errorWriter = generateErrorReport(request, t);
            FileWriter fileWriter = new FileWriter(errorFile);
            fileWriter.write(errorWriter.toString());
            fileWriter.close();
            logger.error("Error logged to file " + errorFile.getAbsolutePath());
            lastFileDumpedException = t;
            lastFileDumpedExceptionOccurences = 1;

        } catch (Throwable t) {
            logger.warn("Error creating error file", t);
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

            StringWriter msgBodyWriter = generateErrorReport(request, t);

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

    private StringWriter generateErrorReport(HttpServletRequest request, Throwable t) {
        StringWriter msgBodyWriter = new StringWriter();
        PrintWriter strOut = new PrintWriter(msgBodyWriter);
        if (lastMailedExceptionOccurences > 1) {
            strOut.println("");
            strOut.println("The previous error: " + lastMailedException.getMessage() + " occured " + Integer.toString(lastMailedExceptionOccurences) + " times.");
            strOut.println("");
        }
        strOut.println("");
        strOut.println("Your Server has generated an error. Please review the details below for additional information: ");
        strOut.println("");
        if (t instanceof JahiaException) {
            JahiaException nje = (JahiaException) t;
            String severityMsg = "Undefined";
            switch (nje.getSeverity()) {
                case JahiaException.WARNING_SEVERITY :
                    severityMsg = "WARNING";
                    break;
                case JahiaException.ERROR_SEVERITY :
                    severityMsg = "ERROR";
                    break;
                case JahiaException.CRITICAL_SEVERITY :
                    severityMsg = "CRITICAL";
                    break;
                case JahiaException.FATAL_SEVERITY :
                    severityMsg = "FATAL";
                    break;
            }
            strOut.println("Severity: " + severityMsg);
        }
        strOut.println("");
        strOut.println("Error: " + t.getMessage());
        strOut.println("");
        strOut.println("URL: " + request.getRequestURL());
        if (request.getQueryString() != null) {
            strOut.println("?" + request.getQueryString());
        }
        strOut.println("   Method: " + request.getMethod());
        strOut.println("");
        strOut.println("Remote host: " + request.getRemoteHost() + "     Remote Address: " + request.getRemoteAddr());
        strOut.println("");
        strOut.println("Request headers:");
        strOut.println("-----------------");
        Iterator headerNames = new EnumerationIterator(request.getHeaderNames());
        while (headerNames.hasNext()) {
            String headerName = (String) headerNames.next();
            String headerValue = request.getHeader(headerName);
            strOut.println("   " + headerName + " : " + headerValue);
        }

        strOut.println("");
        strOut.println("Stack trace:");
        strOut.println("-------------");
        String stackTraceStr = stackTraceToString(t);
        strOut.println(stackTraceStr);

        // now let's output the system properties.
        strOut.println();
        strOut.println("System properties:");
        strOut.println("-------------------");
        Map orderedProperties = new TreeMap(System.getProperties());
        Iterator entrySetIter = orderedProperties.entrySet().iterator();
        while (entrySetIter.hasNext()) {
            Map.Entry curEntry = (Map.Entry) entrySetIter.next();
            String curPropertyName = (String) curEntry.getKey();
            String curPropertyValue = (String) curEntry.getValue();
            strOut.println("   " + curPropertyName + " : " + curPropertyValue);
        }
        strOut.println("");

        if (SettingsBean.getInstance() != null) {
            strOut.println("Server configuration:");
            strOut.println("---------------------");
            SettingsBean settings = SettingsBean.getInstance();
            Map jahiaOrderedProperties = new TreeMap(settings.getPropertiesFile());
            Iterator jahiaEntrySetIter = jahiaOrderedProperties.entrySet().iterator();
            while (jahiaEntrySetIter.hasNext()) {
                Map.Entry curEntry = (Map.Entry) jahiaEntrySetIter.next();
                String curPropertyName = (String) curEntry.getKey();
                String curPropertyValue = null;
                if (curEntry.getValue() == null) {
                    curPropertyValue = null;
                } else if (curEntry.getValue() instanceof String) {
                    curPropertyValue = (String) curEntry.getValue();
                } else {
                    curPropertyValue = curEntry.getValue().toString();
                }
                if (curPropertyName.toLowerCase().indexOf("password") == -1) {
                    strOut.println("   " + curPropertyName + " = " + curPropertyValue);
                }
            }
        }

        strOut.println("");
        strOut.println("Memory status:");
        strOut.println("---------------");
        strOut.println("Max memory   : " + Runtime.getRuntime().maxMemory() + " bytes");
        strOut.println("Free memory  : " + Runtime.getRuntime().freeMemory() + " bytes");
        strOut.println("Total memory : " + Runtime.getRuntime().totalMemory() + " bytes");

        strOut.println("");
        strOut.println("Cache status:");
        strOut.println("--------------");

        SortedSet sortedCacheNames = new TreeSet(ServicesRegistry.getInstance().getCacheService().getNames());
        Iterator cacheNameIte = sortedCacheNames.iterator();
        DecimalFormat percentFormat = new DecimalFormat("###.##");
        while (cacheNameIte.hasNext()) {
            String curCacheName = (String) cacheNameIte.next();
            Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache(curCacheName);
            if (objectCache instanceof Cache) {
                Cache curCache = (Cache) objectCache;
                long cacheLimit = curCache.getCacheLimit();
                String efficiencyStr = "0";
                if (!Double.isNaN(curCache.getCacheEfficiency())) {
                    efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                }
                strOut.println("name=" + curCacheName +
                        " size=" + curCache.size() +
                        " limit=" + cacheLimit / (1024 * 1024) + "MB" +
                        " successful hits=" + curCache.getSuccessHits() +
                        " total hits=" + curCache.getTotalHits() +
                        " efficiency=" + efficiencyStr + "%");
            }
        }

        strOut.println("");
        strOut.println("Thread status:");
        strOut.println("--------------");
        ThreadMonitor threadMonitor = new ThreadMonitor();
        threadMonitor.generateThreadInfo(strOut);
        strOut.println("");
        strOut.println("Deadlock status :");
        threadMonitor.findDeadlock(strOut);

        strOut.println("");
        strOut.println("Depending on the severity of this error, server may still be operational or not. Please check your");
        strOut.println("installation as soon as possible.");
        strOut.println("");
        strOut.println("Yours Faithfully, ");
        strOut.println("    Server Notification Service");
        return msgBodyWriter;
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

        exceptionCount++;

        // add request information
        request.setAttribute("org.jahia.exception.requestInfo",
                getRequestInfo(request));

        logDebugInfo(request, response);

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
                if (ex != null && SC_BAD_REQUEST == code) {
                    logger.warn(message, ex);
                }
                if (SC_UNAUTHORIZED == code) {
                    logger.info(message);
                } else {
                    logger.warn(message != null && message.length() > 0 ? message : "Error code: " + code);
                }
            }
        }
    }

    /**
     * Returns the request information for logging purposes.
     * 
     * @param request
     *            the http request object
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

            Iterator headerNames = new EnumerationIterator(request.getHeaderNames());
            while (headerNames.hasNext()) {
                String headerName = (String) headerNames.next();
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

    /**
     * Converts an exception stack trace to a string, going doing into all
     * the embedded exceptions too to detail as much as possible the real
     * causes of the error.
     *
     * @param t the exception (eventually that contains other exceptions) for
     *          which we want to convert the stack trace into a string.
     * @return a string containing all the stack traces of all the exceptions
     *         contained inside this exception, or an empty string if passed an
     *         empty string.
     */
    protected static String stackTraceToString(Throwable t) {
        int nestingDepth = getNestedExceptionDepth(t, 0);
        return recursiveStackTraceToString(t, nestingDepth);
    }

    protected static String recursiveStackTraceToString(Throwable t, int curDepth) {
        if (t == null) {
            return "";
        }
        StringWriter msgBodyWriter = new StringWriter();
        PrintWriter strOut = new PrintWriter(msgBodyWriter);
        Throwable innerThrowable = t.getCause();
        if (innerThrowable != null) {
            String innerExceptionTrace = recursiveStackTraceToString(innerThrowable, curDepth - 1);
            msgBodyWriter.write(innerExceptionTrace);
        }
        if (curDepth == 0) {
            strOut.println("Cause level : " + curDepth + " (level 0 is the most precise exception)");

        } else {
            strOut.println("Cause level : " + curDepth);
        }
        t.printStackTrace(strOut);
        return msgBodyWriter.toString();
    }

    protected static int getNestedExceptionDepth(Throwable t, int curDepth) {
        if (t == null) {
            return curDepth;
        }
        int newDepth = curDepth;
        Throwable innerThrowable = t.getCause();
        if (innerThrowable != null) {
            newDepth = getNestedExceptionDepth(innerThrowable, curDepth + 1);
        }
        return newDepth;
    }

}
