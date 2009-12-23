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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jahia.bin.filters.ResponseCacheControlFilter;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.engines.login.Login_Engine;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.exceptions.JahiaServerOverloadedException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;

/**
 * Error pages dispatcher servlet.
 * 
 * @author Sergiy Shyrkov
 */
public class ErrorServlet extends HttpServlet {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat
            .getInstance(CalendarHandler.DEFAULT_DATE_FORMAT);

    private static final Logger logger = Logger.getLogger(ErrorServlet.class);

    protected int getErrorCode(HttpServletRequest request) {
        Throwable error = getException(request);
        
        // trick for 503 error
        if (error instanceof JahiaServerOverloadedException) {
            return SC_SERVICE_UNAVAILABLE;
        }
        
        int errorCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");

        return errorCode != 0 ? errorCode : SC_INTERNAL_SERVER_ERROR;

    }

    protected Throwable getException(HttpServletRequest request) {
        Throwable ex = (Throwable) request
                .getAttribute("javax.servlet.error.exception");
        ex = ex != null ? ex : (Throwable) request
                .getAttribute("org.jahia.exception");

        return ex;
    }

    protected String getErrorPagePath(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String path = null;
        String page = request.getParameter("page");
        int errorCode = getErrorCode(request);

        if (null == page) {
            page = "error_" + errorCode + ".jsp";
        }

        // use specified page
        if (page.startsWith("/")) {
            // absolute path specified
            if (getServletContext().getResource(page) != null) {
                path = page;
            } else {
                logger
                        .warn("No resource found at the specified path '"
                                + page
                                + "'. Fallback to standard error page heuristic algorithm.");
            }
        }

        if (null == path) {

            JahiaSite site = null;
            SettingsBean settings = SettingsBean.getInstance();
            String jspPath = settings.getJspContext()
                    + (!settings.getJspContext().endsWith("/") ? "/" : "");

            if (settings.getSiteErrorEnabled()) {
                // site information available?
                try {
                    site = (JahiaSite) request.getSession().getAttribute(
                            "org.jahia.services.sites.jahiasite");
                } catch (Exception e) {
                    // ignore
                }
            }

            // relative page path specified
            // site information available?
            if (site != null) {
                // check site-specific page
                String pathToCheck = jspPath + "errors/sites/"
                        + site.getSiteKey() + "/" + page;
                if (getServletContext().getResource(pathToCheck) != null) {
                    path = pathToCheck;
                } else {
                    pathToCheck = jspPath + "errors/sites/" + site.getSiteKey()
                            + "/error.jsp";
                    path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                            : null;
                }

                if (null == path && site.getTemplatePackageName() != null) {
                    // try template set error page considering inheritance
                    JahiaTemplateManagerService templateService = ServicesRegistry
                            .getInstance().getJahiaTemplateManagerService();
                    JahiaTemplatesPackage pkg = templateService.getTemplatePackage(site.getTemplatePackageName());
                    pathToCheck = pkg.getRootFolderPath() + "errors/" + page;
                    path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                            : null;
                    if (null == path) {
                        pathToCheck = pkg.getRootFolderPath() + "/errors/error.jsp";
                        path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                                : null;
                    }
                }
            }

            if (null == path) {
                String pathToCheck = jspPath + "errors/" + page;
                if (getServletContext().getResource(pathToCheck) != null) {
                    path = pathToCheck;
                } else {
                    pathToCheck = jspPath + "errors/error.jsp";
                    path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                            : null;
                }
            }
        }

        return path;
    }

    protected void process(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        if (response.isCommitted()) {
            logger
                    .warn("Response is already committed. Skipping error processing.");
            return;
        }

        int errorCode = getErrorCode(request);

        try {
            request.getSession(true).removeAttribute(Login_Engine.REQUEST_URI);
        } catch (IllegalStateException ex) {
            // ignore it
        }

        switch (errorCode) {
        case SC_SERVICE_UNAVAILABLE: {
            // special handling for server overloaded exception --> set
            // retry-after header
            Throwable rootCause = (Throwable) request
                    .getAttribute("javax.servlet.error.exception");
            rootCause = rootCause != null ? rootCause : (Throwable) request
                    .getAttribute("org.jahia.exception");

            while (rootCause != null && rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            if (rootCause != null
                    && rootCause instanceof JahiaServerOverloadedException) {
                Calendar cal = Calendar
                        .getInstance(TimeZone.getTimeZone("GMT"));
                cal.add(Calendar.SECOND,
                        ((JahiaServerOverloadedException) rootCause)
                                .getSuggestedRetryTime());
                response.setHeader("Retry-After", DATE_FORMAT.format(cal
                        .getTime())
                        + " GMT");
            }
            break;
        }

        case SC_FORBIDDEN: {
            try {
                request
                        .getSession()
                        .setAttribute(
                                Login_Engine.REQUEST_URI,
                                request
                                        .getAttribute("javax.servlet.error.request_uri"));
            } catch (IllegalStateException e) {
                // do nothing
            }

            break;
        }
        }

        response.setStatus(errorCode);
        response.setContentType("text/html; charset="
                + SettingsBean.getInstance().getDefaultResponseBodyEncoding());
        response.resetBuffer();

        String errorPagePath = getErrorPagePath(request, response);
        if (null == errorPagePath) {
            logger.info("No appropriate error page found for error code '"
                    + errorCode + "'. Using server's default page.");
            throw new JahiaRuntimeException(
                    "No appropriate error page found. Server's default page will be used.");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Forwarding request to the following error page: "
                        + errorPagePath);
            }

            request.setAttribute("org.jahia.exception.forwarded", Boolean.TRUE);
            getServletContext().getRequestDispatcher(errorPagePath).forward(
                    request, response);
        }
    }

    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        ResponseCacheControlFilter.setNoCacheHeaders(response);
        
        // check if the Basic Authentication is required
        Integer errorCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");

        if (errorCode == HttpServletResponse.SC_UNAUTHORIZED
                && getException(request) == null) {
            if (!response.containsHeader("WWW-Authenticate")) {
                response.setHeader("WWW-Authenticate",
                        "BASIC realm=\"Secured Jahia tools\"");
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // otherwise continue with processing of the error
            String method = request.getMethod();
            if (method.equals("GET") || method.equals("POST")) {
                process(request, response);
            } else {
                response.sendError(errorCode != null ? errorCode.intValue()
                        : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

}
