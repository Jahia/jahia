/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.seo.jcr;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.urlrewrite.ServerNameToSiteMapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

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
                        serverName = Url.getServer(uri.getScheme(), uri.getHost(), uri.getPort());
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
                String path = "/render";
                int queryIndex = outboundUrl.indexOf('?');
                if (queryIndex < 0) {
                    path += outboundUrl;
                } else {
                    path += outboundUrl.substring(0, queryIndex);
                }
                try {
                    path = URLDecoder.decode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // shouldn't happen
                    throw new JahiaRuntimeException(e);
                }
                URLResolver urlResolver = urlResolverFactory.createURLResolver(path, hsRequest.getServerName(), hsRequest);
                try {
                    JCRNodeWrapper node = urlResolver.getNode();
                    if (urlResolver.isMapped()) {
                        RenderContext context = (RenderContext) hsRequest.getAttribute("renderContext");
                        if (siteKey == null) {
                            siteKey = context != null ? context.getSite().getSiteKey() : node.getResolveSite().getSiteKey();
                        }
                        // not necessary to generate vanity url if the current server name is bound to a site that is not the same as the current site key ( it could lead to 400 Error page )
                        // because the vanity url won't be found on site resolved by the server name or worst it could lead to the wrong page
                        if (!(StringUtils.isNotEmpty(urlResolver.getSiteKeyByServerName()) && !urlResolver.getSiteKeyByServerName().equals(siteKey))) {
                            VanityUrl vanityUrl = vanityUrlService
                                    .getVanityUrlForWorkspaceAndLocale(
                                            node,
                                            urlResolver.getWorkspace(),
                                            urlResolver.getLocale(), siteKey);
                            if (vanityUrl != null && vanityUrl.isActive() && checkForVanityUrlAmbiguousMapping(vanityUrl, urlResolver, hsRequest, vanityUrlService)) {
                                //for macros some parameters added (like ##requestParameters## for languageswitcher)
                                String extension = "";
                                if (fullUrl.matches("(.?)*##[a-zA-Z]*##$")) {
                                    extension = "##" + StringUtils.substringBetween(fullUrl, "##") + "##";
                                } else if (queryIndex >= 0) {
                                    extension = outboundUrl.substring(queryIndex);
                                }
                                hsRequest.setAttribute(VANITY_KEY, ctx + Render.getRenderServletPath() + "/" + urlResolver.getWorkspace() + vanityUrl.getUrl() + extension);
                                if (serverName != null) {
                                    hsRequest.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK, serverName);
                                }
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

    /**
     * if servername does not allow site resolution, we need to check if the vanity url used is unique between sites
     *
     * @return true if vanity url is safe to be use
     */
    private boolean checkForVanityUrlAmbiguousMapping(VanityUrl vanityUrlToTest, URLResolver urlResolver, HttpServletRequest hsRequest, VanityUrlService vanityUrlService) throws RepositoryException {

        if (StringUtils.isEmpty(urlResolver.getSiteKeyByServerName())) {
            // check for vanity uniqueness
            List<VanityUrl> foundVanityUrls = vanityUrlService.findExistingVanityUrls(vanityUrlToTest.getUrl(), StringUtils.EMPTY, urlResolver.getWorkspace());
            for (VanityUrl foundVanityUrl : foundVanityUrls) {
                if (!foundVanityUrl.getSite().equals(vanityUrlToTest.getSite())) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Ambiguous vanity URL resolution: current server name '{}' " +
                                "does not allow to resolve the appropriate site, and multiple vanity urls exist " +
                                "with the exact same url mapping '{}' in different sites {}",
                                new Object[]{hsRequest.getServerName(),
                                        vanityUrlToTest.getUrl(),
                                        Arrays.toString(new String[]{vanityUrlToTest.getSite(), foundVanityUrl.getSite()})});
                    }

                    return false;
                }
            }
        }
        return true;
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


