/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin.errors;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.params.valves.LoginConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

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

    private transient boolean siteLevelErrorPagesEnabled;

    private transient JahiaSitesService sitesService;

    private transient JahiaTemplateManagerService templateService;

    private transient URLResolverFactory urlResolverFactory;

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

        int errorCode = getErrorCode(request);
        String page = "error_" + errorCode + ".jsp";

        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestUri != null && requestUri.matches(getServletContext().getContextPath() + "/cms/[^/]+frame/.*")) {
            page = "gwt_error.jsp";
        } else if (siteLevelErrorPagesEnabled) {
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
                                if (pkg.getResource("/errors/" + page) != null) {
                                    path = "/modules/" + pkg.getId() + "/errors/" + page;
                                } else if (pkg.getResource("/errors/error.jsp") != null) {
                                    path = "/modules/" + pkg.getId() + "/errors/error.jsp";
                                }
                            }
                        }
                    } catch (JahiaException e) {
                        logger.debug("Cannot find site", e);
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