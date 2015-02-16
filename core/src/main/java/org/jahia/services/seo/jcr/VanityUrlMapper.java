package org.jahia.services.seo.jcr;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.urlrewrite.ServerNameToSiteMapper;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

    public static final String VANITY_KEY = "org.jahia.services.seo.jcr.VanityUrl";

    /**
     * checks if a vanity exists and put the result as an attribute of the request
     * this is used in urlRewriting rules
     * @param hsRequest
     *              the request used during urlRewriting operations
     * @param outboundUrl
     *              the url received to be checked
     */
    public void checkVanityUrl(HttpServletRequest hsRequest,String outboundContext, String outboundUrl) {
        hsRequest.removeAttribute(VANITY_KEY);

        URLResolverFactory urlResolverFactory = (URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory");
        VanityUrlService vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean("org.jahia.services.seo.jcr.VanityUrlService");
        UrlRewriteService urlRewriteService = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");

        String ctx = StringUtils.defaultIfEmpty(hsRequest.getContextPath(), "");
        String fullUrl = ctx + Render.getRenderServletPath() + outboundUrl;
        hsRequest.setAttribute(VANITY_KEY, fullUrl);
        
        if (StringUtils.isNotEmpty(outboundUrl) && !Url.isLocalhost(hsRequest.getServerName())) {
            String serverName = null;
            String siteKey = null;
            String contextToCheck = outboundContext;            
            int schemaDelimiterIndex = outboundContext.indexOf("://");
            if (schemaDelimiterIndex != -1) {
                try {
                    URI uri = new URI(outboundContext);
                    siteKey = lookupSiteKeyByServerName(uri.getHost());
                    if (siteKey != null) {
                        contextToCheck = uri.getPath();
                        StringBuffer sb = new StringBuffer()
                                .append(uri.getScheme()).append("://")
                                .append(uri.getHost());
                        if (uri.getPort() != -1) {
                            sb.append(':').append(uri.getPort());
                        }
                        serverName = sb.toString();
                    }
                } catch (URISyntaxException e) {
                    // simply continue as siteKey will be determined later
                }
            } else if (StringUtils.isNotEmpty((String) hsRequest.getAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK))) {
                String host = (String) hsRequest.getAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK);
                host = StringUtils.substringBefore(StringUtils.substringAfter(host, "://"), ":");
                siteKey = lookupSiteKeyByServerName(host);
            }
            if (StringUtils.equals(ctx, contextToCheck)) {
                String url = "/render" + outboundUrl;
                try {
                    url = URLDecoder.decode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                URLResolver urlResolver = urlResolverFactory.createURLResolver(url, hsRequest.getServerName(), hsRequest);
                try {
                    JCRNodeWrapper node = urlResolver.getNode();
                    boolean vanityUrlFound = false;
                    if (urlResolver.isMapped()) {
                        RenderContext context = (RenderContext) hsRequest.getAttribute("renderContext");
                        if (siteKey == null) {
                            siteKey = context != null ? context.getSite().getSiteKey() : node.getResolveSite().getSiteKey();
                        }                        
                        VanityUrl vanityUrl = vanityUrlService
                                .getVanityUrlForWorkspaceAndLocale(
                                        node,
                                        urlResolver.getWorkspace(),
                                        urlResolver.getLocale(), siteKey);
                        if (vanityUrl != null && vanityUrl.isActive()) {
                            vanityUrlFound = true;
                            hsRequest.setAttribute(VANITY_KEY, ctx + Render.getRenderServletPath() + "/" + urlResolver.getWorkspace() + vanityUrl.getUrl());
                            if (serverName != null) {
                                hsRequest.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK, serverName);
                            }                            
                        }
                    }
                } catch (RepositoryException e) {
                    logger.debug("Error when trying to obtain vanity url", e);
                    if (serverName != null) {
                        hsRequest.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK, serverName);
                    }
                }
            }
        }
    }
    
    private static String lookupSiteKeyByServerName(String host) {
        JahiaSite site = null;
        if (SpringContextSingleton.getInstance().isInitialized()) {
            try {
                site = JahiaSitesService.getInstance().getSiteByServerName(host);
            } catch (JahiaException e) {
                logger.error("Error resolving site by server name '" + host + "'", e);
            }
        }
        return site != null ? site.getSiteKey() : "";
    }
    
}


