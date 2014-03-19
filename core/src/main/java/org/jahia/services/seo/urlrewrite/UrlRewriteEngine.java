/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.seo.urlrewrite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
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
    
    private boolean urlRewriteSeoRulesEnabled;

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
            String ctx = StringUtils.defaultIfEmpty(hsRequest.getContextPath(), null);
            if (outboundUrl.startsWith(ctx != null ? (ctx + Render.getRenderServletPath()) : Render.getRenderServletPath())) {
                if (StringUtils.isNotEmpty(outboundUrl) && !Url.isLocalhost(hsRequest.getServerName())) {
                    String url = StringUtils.substringAfter(outboundUrl, ctx != null ? (ctx + "/cms") : "/cms");
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
                            if (vanityUrl != null && vanityUrl.isActive()) {
                                outboundUrl = outboundUrl.replace("/" + urlResolver.getLocale()
                                        + urlResolver.getPath(), vanityUrl.getUrl());
                            }
                        } catch (RepositoryException e) {
                            logger.debug("Error when trying to obtain vanity url", e);
                        }
                    }
                    if (!isUrlRewriteSeoRulesEnabled()) {
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

    public boolean isUrlRewriteSeoRulesEnabled() {
        return urlRewriteSeoRulesEnabled;
    }

    public void setUrlRewriteSeoRulesEnabled(boolean urlRewriteSeoRulesEnabled) {
        this.urlRewriteSeoRulesEnabled = urlRewriteSeoRulesEnabled;
    }
}
