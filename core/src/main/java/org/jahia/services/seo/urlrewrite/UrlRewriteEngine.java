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

package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.OutSupport;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.tuckey.web.filters.urlrewrite.RewrittenOutboundUrl;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

/**
 * URL rewriter engine.
 * 
 * @author Sergiy Shyrkov
 */
class UrlRewriteEngine extends UrlRewriter {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteEngine.class);

    private URLResolverFactory urlResolverFactory;

    private VanityUrlService vanityUrlService;

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }

    private static Configuration getConfiguration(ServletContext context, Resource[] confLocations) {
        Configuration cfg = null;
        if (confLocations == null || confLocations.length == 0) {
            logger.warn("No configuration resource location specified for"
                    + " the URL rewrite engine. Using empty one.");
            return new Configuration();
        }
        try {
            cfg = new Configuration(context, confLocations);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return cfg;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param conf
     *            the URL rewriter configuration
     */
    UrlRewriteEngine(InputStream is, String fileName) {
        super(new Configuration(is, fileName));
        logger.info("Loaded URL rewrite rules from {}", fileName);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param context
     *            current servlet context
     * @param confLocation
     *            the URL rewriter configuration resource location
     */
    public UrlRewriteEngine(ServletContext context, Resource[] confLocations) {
        super(getConfiguration(context, confLocations));
        if (confLocations != null) {
            logger.info("Loaded URL rewrite rules from {}",
                    confLocations != null ? java.util.Arrays.asList(confLocations) : null);
        }
    }

    public RewrittenUrl rewriteInbound(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, InvocationTargetException {
        return processRequest(request, response);
    }

    public String rewriteOutbound(String url, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException,
            InvocationTargetException {

        RewrittenOutboundUrl rou = processEncodeURL(response, request, false, url);
        if (rou == null) {
            return response.encodeURL(url);
        }
        if (rou.isEncode()) {
            rou.setTarget(response.encodeURL(rou.getTarget()));
        }
        return processEncodeURL(response, request, true, rou.getTarget()).getTarget();

    }

    @Override
    protected RewrittenOutboundUrl processEncodeURL(HttpServletResponse hsResponse, HttpServletRequest hsRequest, boolean encodeUrlHasBeenRun, String outboundUrl) {
        try {
            if (outboundUrl.startsWith(hsRequest.getContextPath()+ Render.getRenderServletPath())) {
                if (StringUtils.isNotEmpty(outboundUrl) && !Url.isLocalhost(hsRequest.getServerName())) {
                    String url = StringUtils.substringAfter(outboundUrl,hsRequest.getContextPath()+"/cms");
                    url = StringUtils.substringBefore(url,"?");
                    url = StringUtils.substringBefore(url,"#");
                    url = StringUtils.substringBefore(url,";");
                    url = URLDecoder.decode(url,"UTF-8");
                    URLResolver urlResolver = urlResolverFactory.createURLResolver(url, hsRequest.getServerName() ,hsRequest);
                    JCRNodeWrapper node = urlResolver.getNode();
                    if (urlResolver.isMapped()) {
                        try {
                            RenderContext context = (RenderContext) hsRequest.getAttribute("renderContext");
                            VanityUrl vanityUrl = vanityUrlService
                                    .getVanityUrlForWorkspaceAndLocale(
                                            node,
                                            urlResolver.getWorkspace(),
                                            urlResolver.getLocale(), context != null ? context.getSite().getSiteKey() : null);
                            if (vanityUrl != null) {
                                outboundUrl = outboundUrl.replace("/" + urlResolver.getLocale()
                                        + urlResolver.getPath(), vanityUrl.getUrl());
                            }
                        } catch (RepositoryException e) {
                            logger.debug("Error when trying to obtain vanity url", e);
                        }
                    }
                    if (!SettingsBean.getInstance().isUrlRewriteSeoRulesEnabled()) {
                        // Just in case the SEO is not activated, switch the servername anyway to avoid crosscontext pages
                        try {
                            // Switch to correct site for links
                            outboundUrl = Url.appendServerNameIfNeeded(node, outboundUrl, hsRequest);
                        } catch (PathNotFoundException e) {
                            // Cannot find node
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Cannot parse url for rewriting : "+outboundUrl , e);
        }

        return super.processEncodeURL(hsResponse, hsRequest, encodeUrlHasBeenRun, outboundUrl);
    }
}
