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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.params.valves.LoginConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;

/**
 * Error pages dispatcher servlet.
 * 
 * @author Sergiy Shyrkov
 */
public class ErrorServlet extends HttpServlet {

    public static final String LICENSE_TERMS_VIOLATION_MODE = "Jahia License Violation";

    private static final Logger logger = LoggerFactory.getLogger(ErrorServlet.class);

    public static final String MAINTENANCE_MODE = "Jahia in under maintenance";

    private static final long serialVersionUID = -6990851339777685000L;
    
    protected boolean siteLevelErrorPagesEnabled;

    protected JahiaSitesService sitesService;

    protected JahiaTemplateManagerService templateService;

    protected URLResolverFactory urlResolverFactory;
    
    protected void forwardToErrorPage(String errorPagePath, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (null == errorPagePath) {
            logger.info("No appropriate error page found for error code '" + getErrorCode(request)
                    + "'. Using server's default page.");
            throw new JahiaRuntimeException(
                    "No appropriate error page found. Server's default page will be used.");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Forwarding request to the following error page: " + errorPagePath);
            }

            request.setAttribute("org.jahia.exception.forwarded", Boolean.TRUE);
            getServletContext().getRequestDispatcher(errorPagePath).forward(request, response);
        }
    }

    protected int getErrorCode(HttpServletRequest request) {
        int errorCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        return errorCode != 0 ? errorCode : SC_INTERNAL_SERVER_ERROR;

    }

    protected String getErrorPagePath(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
                logger.warn("No resource found at the specified path '" + page
                        + "'. Fallback to standard error page heuristic algorithm.");
            }
        }

        if (null == path) {
            if (siteLevelErrorPagesEnabled) {
                String siteKey = resolveSiteKey(request);

                // site information available?
                if (siteKey != null) {
                    // check site-specific page
                    String pathToCheck = "/errors/sites/" + siteKey + "/" + page;
                    if (getServletContext().getResource(pathToCheck) != null) {
                        path = pathToCheck;
                    } else {
                        pathToCheck = "/errors/sites/" + siteKey + "/error.jsp";
                        path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                                : null;
                    }
                    if (null == path) {
                        try {
                            JahiaSite site = sitesService.getSiteByKey(siteKey);
                            if (site != null) {
                                // try template set error page considering inheritance
                                JahiaTemplatesPackage pkg = templateService
                                        .getTemplatePackage(site
                                                .getTemplatePackageName());
                                if (pkg != null) {
                                    pathToCheck = pkg.getRootFolderPath()
                                            + "/errors/" + page;
                                    path = getServletContext().getResource(
                                            pathToCheck) != null ? pathToCheck
                                            : null;
                                    if (null == path) {
                                        pathToCheck = pkg.getRootFolderPath()
                                                + "/errors/error.jsp";
                                        path = getServletContext().getResource(
                                                pathToCheck) != null ? pathToCheck
                                                : null;
                                    }
                                }
                            }
                        } catch (JahiaException e) {
                            logger.debug("Cannot find site",e);
                        }
                    }
                }
            }

            if (null == path) {
                String pathToCheck = "/errors/" + page;
                if (getServletContext().getResource(pathToCheck) != null) {
                    path = pathToCheck;
                } else {
                    pathToCheck = "/errors/error.jsp";
                    path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                            : null;
                }
            }
        }

        return path;
    }

    protected String resolveSiteKey(HttpServletRequest request) {
        String siteKey = null;
        // site information available?
        try {
            URLResolver urlResolver = ((URLResolver) request.getAttribute("urlResolver"));
            if (urlResolver == null) {
                urlResolver = urlResolverFactory.createURLResolver(request.getPathInfo(),
                        request.getServerName(), request);
            }
            siteKey = urlResolver.getSiteKey();
        } catch (Exception e) {
            // ignore
        }
        return siteKey;
    }

    protected Throwable getException(HttpServletRequest request) {
        Throwable ex = (Throwable) request.getAttribute("javax.servlet.error.exception");
        ex = ex != null ? ex : (Throwable) request.getAttribute("org.jahia.exception");

        return ex;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        siteLevelErrorPagesEnabled = SettingsBean.getInstance().getSiteErrorEnabled();
        if (siteLevelErrorPagesEnabled) {
            sitesService = ServicesRegistry.getInstance().getJahiaSitesService();
            templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            urlResolverFactory = (URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory");
        }
    }

    protected void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (response.isCommitted()) {
            logger.warn("Response is already committed. Skipping error processing.");
            return;
        }

        int errorCode = getErrorCode(request);

        if (errorCode == HttpServletResponse.SC_UNAUTHORIZED) {
            String redirectUrl = LoginConfig.getInstance().getCustomLoginUrl(request);
            if (redirectUrl != null) {
                response.sendRedirect(response.encodeRedirectURL(redirectUrl));
                return;
            }
        }

        response.setStatus(errorCode);
        response.setContentType("text/html; charset="
                + SettingsBean.getInstance().getCharacterEncoding());
        response.resetBuffer();

        String errorPagePath = getErrorPagePath(request, response);
        forwardToErrorPage(errorPagePath, request, response);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        WebUtils.setNoCacheHeaders(response);

        // check if the Basic Authentication is required
        Integer errorCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        if (errorCode == HttpServletResponse.SC_UNAUTHORIZED && getException(request) == null) {
            if (!response.containsHeader("WWW-Authenticate")) {
                response.setHeader("WWW-Authenticate", "BASIC realm=\"Secured Jahia tools\"");
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            if (errorCode == HttpServletResponse.SC_SERVICE_UNAVAILABLE
                    && StringUtils.equals(ErrorServlet.MAINTENANCE_MODE,
                            (String) request.getAttribute("javax.servlet.error.message"))) {
                forwardToErrorPage("/errors/maintenance.jsp", request, response);
            } else if (errorCode == HttpServletResponse.SC_SERVICE_UNAVAILABLE
                    && StringUtils.equals(ErrorServlet.LICENSE_TERMS_VIOLATION_MODE,
                            (String) request.getAttribute("javax.servlet.error.message"))) {
                forwardToErrorPage("/errors/license.jsp", request, response);
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
}