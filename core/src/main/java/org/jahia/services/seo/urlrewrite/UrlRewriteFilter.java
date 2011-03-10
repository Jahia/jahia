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

package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.tuckey.web.filters.urlrewrite.Status;
import org.tuckey.web.filters.urlrewrite.UrlRewriteWrappedResponse;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;
import org.tuckey.web.filters.urlrewrite.utils.ServerNameMatcher;

/**
 * Jahia specific implementation of the {@link org.tuckey.web.filters.urlrewrite.UrlRewriteFilter} that adds multiple configuration
 * resources lookup support and other features.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlRewriteFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteFilter.class);
    public static final String CMS_APPLICATION_CONTEXT = "org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet";

    private boolean enabled = true;

    private boolean outboundRulesEnabled = true;

    private UrlRewriteService urlRewriteService;
    private VanityUrlService vanityUrlService;

    private boolean statusEnabled = true;

    private String statusPath = "/tools/rewrite-status";

    private ServerNameMatcher statusServerNameMatcher;

    private FilterConfig config;

    public void init(FilterConfig cfg) throws ServletException {
        this.config = cfg;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!enabled) {
            chain.doFilter(request, response);
            return;
        }

        UrlRewriter urlRewriter = urlRewriteService.getEngine();

        final HttpServletRequest hsRequest = (HttpServletRequest) request;
        final HttpServletResponse hsResponse = outboundRulesEnabled ? new UrlRewriteWrappedResponse(
                (HttpServletResponse) response, hsRequest, urlRewriter)
                : (HttpServletResponse) response;

        // check for status request
        String uri = hsRequest.getRequestURI();

        logger.debug("Handling {}", uri);

        if (statusEnabled
                && uri != null
                && uri.startsWith(hsRequest.getContextPath() + statusPath)
                && (statusServerNameMatcher == null || statusServerNameMatcher.isMatch(request
                        .getServerName()))) {
            showStatus(hsRequest, hsResponse);
            return;
        }

        if ("/cms".equals(hsRequest.getServletPath())) {
            String path = uri.substring(hsRequest.getContextPath().length() + hsRequest.getServletPath().length());
            ApplicationContext cmsContext = (ApplicationContext) config.getServletContext().getAttribute(CMS_APPLICATION_CONTEXT);
            SimpleUrlHandlerMapping mapping = (SimpleUrlHandlerMapping) cmsContext.getBean("rendererMapping");
            for (String registeredPattern : mapping.getUrlMap().keySet()) {
                if (mapping.getPathMatcher().match(registeredPattern, path)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
            String targetSiteKey = ServerNameToSiteMapper.getSiteKeyByServerName(hsRequest);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY, targetSiteKey);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG, StringUtils.EMPTY);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, StringUtils.EMPTY);
            if (!StringUtils.isEmpty(targetSiteKey)) {
                try {
                    List<VanityUrl> vanityUrls = vanityUrlService.findExistingVanityUrls(path, targetSiteKey, "live");
                    if (!vanityUrls.isEmpty()) {
                        vanityUrls.get(0).getLanguage();
                        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG, vanityUrls.get(0).getLanguage());
                        path = StringUtils.substringBefore(vanityUrls.get(0)
                                .getPath(), "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE + "/")
                                + ".html";
                        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, path);
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get vanity Url",e);
                }
                try {
                    String defaultLanguage = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(targetSiteKey).getDefaultLanguage();
                    request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG, defaultLanguage);
                } catch (JahiaException e) {
                    logger.error("Cannot get site", e);
                }
            }

        }


        // if no rewrite has taken place continue as normal
        if (!urlRewriter.processRequest(hsRequest, hsResponse, chain)) {
            chain.doFilter(hsRequest, hsResponse);
        } else if (hsRequest.getAttribute("UrlRewriteFilter.sendError") != null) {
            // workaround: call sendError with the specified error code 
            hsResponse.sendError(Integer.valueOf((String) hsRequest.getAttribute("UrlRewriteFilter.sendError")));
        }
    }

    /**
     * Show the status of the conf and the filter to the user.
     * 
     * @param request
     *            to get status info from
     * @param response
     *            response to show the status on.
     * @throws java.io.IOException
     *             if the output cannot be written
     */
    protected void showStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Status status = new Status(urlRewriteService.getEngine().getConf());
        status.displayStatusInContainer(request);
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(status.getBuffer().length());
        final PrintWriter out = response.getWriter();
        out.write(status.getBuffer().toString());
        out.close();
    }

    public void destroy() {
        // do nothing
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }

    public void setOutboundRulesEnabled(boolean outboundRulesEnabled) {
        this.outboundRulesEnabled = outboundRulesEnabled;
    }

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }
}
