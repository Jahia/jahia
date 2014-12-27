package org.jahia.services.seo.jcr;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p/>
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p/>
 * 1/ GPL
 * ======================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * "This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * <p/>
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 * <p/>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p/>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 * <p/>
 * <p/>
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 * <p/>
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 * <p/>
 * For more information, please visit http://www.jahia.com
 */

/**
 * This class is used in UrlRewriting rules to map the current url to the vanity if exists
 */
public class VanityUrlMapper {


    private static final Logger logger = LoggerFactory.getLogger(VanityUrlMapper.class);

    /**
     * checks if a vanity exists and put the result as an attribute of the request
     * this is used in urlRewriting rules
     * @param hsRequest
     *              the request used during urlRewriting operations
     * @param outboundUrl
     *              the url received to be checked
     */
    public void checkVanityUrl(HttpServletRequest hsRequest, String outboundUrl) {
        URLResolverFactory urlResolverFactory = (URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory");
        VanityUrlService vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlService");
        UrlRewriteService urlRewriteService = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");

        hsRequest.setAttribute("vanityUrl",outboundUrl);
        String ctx = StringUtils.defaultIfEmpty(hsRequest.getContextPath(), null);
        if (StringUtils.isNotEmpty(outboundUrl) && !Url.isLocalhost(hsRequest.getServerName()) && !StringUtils.endsWith(outboundUrl,".flow")) {
            String url = StringUtils.substringAfter(outboundUrl, ctx != null ? (ctx + "/cms") : "/cms");
            url = StringUtils.substringBefore(url, "?");
            url = StringUtils.substringBefore(url, "#");
            url = StringUtils.substringBefore(url, ";");
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            URLResolver urlResolver = urlResolverFactory.createURLResolver(url, hsRequest.getServerName(), hsRequest);
            try {
                JCRNodeWrapper node = urlResolver.getNode();
                if (urlResolver.isMapped()) {
                    RenderContext context = (RenderContext) hsRequest.getAttribute("renderContext");
                    VanityUrl vanityUrl = vanityUrlService
                            .getVanityUrlForWorkspaceAndLocale(
                                    node,
                                    urlResolver.getWorkspace(),
                                    urlResolver.getLocale(), context != null ? context.getSite().getSiteKey() : node.getResolveSite().getSiteKey());
                    if (vanityUrl != null && vanityUrl.isActive()) {
                        outboundUrl = outboundUrl.replace("/" + urlResolver.getLocale()
                                + urlResolver.getPath(), vanityUrl.getUrl());
                    }
                }
                if (!urlRewriteService.isSeoRulesEnabled()) {
                    // Just in case the SEO is not activated, switch the servername anyway to avoid crosscontext pages
                    try {
                        // Switch to correct site for links
                        outboundUrl = Url.appendServerNameIfNeeded(node, outboundUrl, hsRequest);
                    } catch (PathNotFoundException e) {
                        // Cannot find node
                    } catch (MalformedURLException e) {
                        // Cannot find node
                    }
                }
                hsRequest.setAttribute("vanityUrl",outboundUrl);
            } catch (RepositoryException e) {
                logger.debug("Error when trying to obtain vanity url", e);
            }
        }
    }
}


