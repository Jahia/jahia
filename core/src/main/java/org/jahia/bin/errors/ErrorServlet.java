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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.params.valves.LoginConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.urlrewrite.SessionidRemovalResponseWrapper;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static javax.servlet.http.HttpServletResponse.*;

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
            logger.debug("Forwarding request to the following error page: {}", errorPagePath);
            request.setAttribute("org.jahia.exception.forwarded", Boolean.TRUE);
            try {
                getServletContext().getRequestDispatcher(errorPagePath).forward(request, response);
            } catch (Throwable e) {
                // In case of an error in the process of the resource, fallback to Jahia error page.
                logger.error("Unable to process error page {}", errorPagePath, e);
                request.setAttribute("org.jahia.exception", e);
                getServletContext().getRequestDispatcher(getErrorPagePath("error.jsp", null)).forward(request, response);
            }
        }
    }

    public static int getErrorCode(HttpServletRequest request) {
        try {
            return (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Full stack", e);
                logger.debug("Unable to process error code {} from request", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
            }
            return SC_INTERNAL_SERVER_ERROR;
        }
    }

    protected String getErrorPagePath(HttpServletRequest request) throws IOException {
        String path = null;
        int errorCode = getErrorCode(request);
        String page = getDedicateErrorPage(errorCode);
        URLResolver urlResolver = (URLResolver) request.getAttribute("urlResolver");
        boolean isEdit = urlResolver != null && !urlResolver.getServletPart().equals("render");
        if (siteLevelErrorPagesEnabled && !isEdit) {
            String siteKey = resolveSiteKey(request);

            // site information available?
            if (siteKey != null) {
                // check site-specific page
                path = getSiteSpecificErrorPagePath(page, siteKey);
                if (null == path) {
                    path = getModuleErrorPagePath(page, siteKey);
                }
            }
        }

        if (isEdit && !urlResolver.getServletPart().equals("studio")) {
            path = getErrorPagePath(page, "react");
        }

        if (null == path) {
            path = getErrorPagePath(page, null);
            if (null == path) {
                path = getErrorPagePath("error.jsp", null);
            }
        }

        return path;
    }

    private String getModuleErrorPagePath(String page, String siteKey) {
        try {
            JahiaSite site = sitesService.getSiteByKey(siteKey);
            if (site != null) {
                // try template set error page considering inheritance
                JahiaTemplatesPackage pkg = templateService
                        .getTemplatePackage(site
                                .getTemplatePackageName());
                if (pkg != null) {
                    if (pkg.getResource("/errors/" + page) != null) {
                        return "/modules/" + pkg.getId() + "/errors/" + page;
                    } else if (pkg.getResource("/errors/error.jsp") != null) {
                        return "/modules/" + pkg.getId() + "/errors/error.jsp";
                    }
                }
            }
        } catch (JahiaException e) {
            logger.debug("Cannot find site", e);
        }
        return null;
    }

    private String getSiteSpecificErrorPagePath(String page, String siteKey) throws MalformedURLException {
        String path;
        String pathToCheck = "/errors/sites/" + siteKey + "/" + page;
        if (getServletContext().getResource(pathToCheck) != null) {
            path = pathToCheck;
        } else {
            pathToCheck = "/errors/sites/" + siteKey + "/error.jsp";
            path = getServletContext().getResource(pathToCheck) != null ? pathToCheck
                    : null;
        }
        return path;
    }

    private String getDedicateErrorPage(int errorCode) {
        switch (errorCode) {
            case SC_FORBIDDEN:
            case SC_NOT_FOUND:
            case SC_UNAUTHORIZED:
            case SC_BAD_REQUEST:
            case SC_SERVICE_UNAVAILABLE: return "error_" + errorCode + ".jsp";
            default: return "error.jsp";
        }
    }

    private String getErrorPagePath(String page, String theme) throws MalformedURLException {
        boolean defaultTheme = (theme == null || "default".equals(theme));
        String path;
        if (defaultTheme) {
            path = "/errors/" + page;
        } else {
            path = "/errors/" + theme + "/" + page;
        }
        if (getServletContext().getResource(path) != null) {
            return path;
        }
        if (defaultTheme) {
            return null;
        } else {
            return getErrorPagePath(page, null);
        }
    }


    protected String resolveSiteKey(HttpServletRequest request) {
        String siteKey = (String) request.getAttribute("siteKey");
        if (siteKey != null) {
            return siteKey;
        }
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
            ErrorLoggingFilter.handle(request, response);
            return;
        }

        int errorCode = getErrorCode(request);

        if (errorCode == HttpServletResponse.SC_UNAUTHORIZED) {
            String redirectUrl = LoginConfig.getInstance().getCustomLoginUrl(request);
            if (redirectUrl != null) {
                try {
                    // Add referer as query parameter if present
                    // If the error servlet has been called from the Render servlet (/cms/**), the urlResolver is present in the request
                    URLResolver urlResolver = (URLResolver) request.getAttribute("urlResolver");
                    if (urlResolver != null) {
                        String returnUrl = urlResolver.getUrlPathInfo();
                        if (StringUtils.isNotBlank(returnUrl)) {
                            String queryString = request.getQueryString();
                            UrlRewriteService urlRewriteService = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");
                            // Switch back to "/cms" servlet
                            String fullReturnUrl = urlRewriteService.rewriteOutbound(request.getContextPath() + "/cms" + returnUrl, request, response);
                            if (StringUtils.isNotBlank(queryString)) {
                                fullReturnUrl += "?" + queryString;
                            }

                            // Limit URL length to prevent potential DoS attacks
                            if (fullReturnUrl.length() <= 2048) {
                                try {
                                    String encodedReturnUrl = URLEncoder.encode(fullReturnUrl, StandardCharsets.UTF_8);
                                    String separator = redirectUrl.contains("?") ? "&" : "?";
                                    redirectUrl = redirectUrl + separator + "redirect=" + encodedReturnUrl;
                                } catch (Exception e) {
                                    logger.warn("Failed to encode return URL: {}", fullReturnUrl);
                                    logger.debug("Full error", e);
                                }
                            } else {
                                logger.warn("Return URL too long, skipping redirect parameter: {}", fullReturnUrl.length());
                            }
                        }
                    }
                } catch (Throwable t) {
                    // Ignore any issue in resolving redirect url
                    logger.debug("An error occurred while computing the redirect URL", t);
                }
                redirectUrl = response.encodeRedirectURL(redirectUrl);
                if (SettingsBean.getInstance().isDisableJsessionIdParameter()) {
                    redirectUrl = SessionidRemovalResponseWrapper.removeJsessionId(redirectUrl);
                }
                response.sendRedirect(redirectUrl);
                return;
            }
        }

        response.setStatus(errorCode);
        response.setContentType("text/html; charset="
                + SettingsBean.getInstance().getCharacterEncoding());
        response.resetBuffer();

        String errorPagePath = getErrorPagePath(request);
        forwardToErrorPage(errorPagePath, request, response);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        WebUtils.setNoCacheHeaders(response);

        // check if the Basic Authentication is required
        Integer errorCode = getErrorCode(request);

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
