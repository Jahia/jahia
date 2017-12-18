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
package org.jahia.services.seo.urlrewrite;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.seo.jcr.VanityUrlMapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Exposes the site key for the current server name as a request attribute if the corresponding server name is mapped to a site.
 * 
 * @author Sergiy Shyrkov
 */
public class ServerNameToSiteMapper {

    public static final String ATTR_NAME_CMS_TOKEN = "jahiaSeoCmsToken";
    public static final String ATTR_NAME_DEFAULT_LANG = "siteDefaultLanguage";
    public static final String ATTR_NAME_DEFAULT_LANG_MATCHES = "jahiaSiteKeyMatchesDefaultLanguage";
    public static final String ATTR_NAME_LANG_TOKEN = "jahiaSeoLanguageToken";
    public static final String ATTR_NAME_ADD_CMS_PREFIX = "jahiaSeoAddCmsPrefix";
    public static final String ATTR_NAME_SITE_KEY = "jahiaSiteKeyForCurrentServerName";
    /**
     * @deprecated use {@link #ATTR_NAME_SERVERNAME_FOR_LINK} instead
     */
    @Deprecated
    public static final String ATTR_NAME_SITE_KEY_FOR_LINK = "jahiaSiteKeyForLink";
    public static final String ATTR_NAME_SERVERNAME_FOR_LINK = "jahiaSeoServernameForLink";
    public static final String ATTR_NAME_SITE_KEY_MATCHES = "jahiaSiteKeyMatchesCurrentServerName";
    public static final String ATTR_NAME_SKIP_INBOUND_SEO_RULES = "jahiaSkipInboundSeoRules";
    public static final String ATTR_NAME_VANITY_LANG = "vanityUrlTargetLang";
    public static final String ATTR_NAME_VANITY_PATH = "vanityUrlTargetPath";
    public static final String ATTR_NAME_CONTEXT_PATH = "contextPath";


    private static final Logger logger = LoggerFactory.getLogger(ServerNameToSiteMapper.class);

    public static String getSiteKeyByServerName(HttpServletRequest request) {

        String targetSiteKey = (String) request.getAttribute(ATTR_NAME_SITE_KEY);
        if (targetSiteKey != null) {
            return targetSiteKey;
        }

        String host = request.getServerName();
        if (StringUtils.isEmpty(host) || Url.isLocalhost(host)) {
            targetSiteKey = StringUtils.EMPTY;
        } else {
            targetSiteKey = StringUtils.defaultString(lookupSiteKeyByServerName(host));
            if (logger.isDebugEnabled()) {
                if (targetSiteKey.length() > 0) {
                    logger.debug("Mapping server name {} to site key {}", host, targetSiteKey);
                } else {
                    logger.debug("No site mapping found for server name {}", host);
                }
            }
        }
        
        request.setAttribute(ATTR_NAME_SITE_KEY, targetSiteKey);

        return targetSiteKey;
    }

    private static String lookupSiteKeyByServerName(String host) {
        String site = null;
        if (SpringContextSingleton.getInstance().isInitialized()) {
            try {
                site = JahiaSitesService.getInstance().getSitenameByServerName(host);
            } catch (JahiaException e) {
                logger.error("Error resolving site by server name '" + host + "'", e);
            }
        }
        return site != null ? site : "";
    }

    private UrlRewriteService urlRewriteService;
    
    private UrlRewriteService getUrlRewriteService() {
        if (urlRewriteService == null) {
            urlRewriteService = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService"); 
        }
        
        return urlRewriteService;
    }
    
    public void canResolveSiteByServerName(HttpServletRequest request, String ctx, String language,
            String siteKey) {
        analyzeLink(request, ctx, language, siteKey, null);
    }

    public void analyzeLink(HttpServletRequest request, String ctx, String language,
            String siteKey, String path) {
        
        String currentSiteKey = getSiteKeyByServerName(request);
        boolean matches = currentSiteKey.equals(siteKey);
        request.setAttribute(ATTR_NAME_SITE_KEY_MATCHES, Boolean.valueOf(matches));

        try {
            boolean languageMatches = JahiaSitesService.getInstance().getSiteDefaultLanguage(siteKey).equals(language);
            request.setAttribute(ATTR_NAME_DEFAULT_LANG_MATCHES, languageMatches);
            request.setAttribute(ATTR_NAME_LANG_TOKEN, languageMatches ? "" : "/" + language);
        } catch (JahiaException e) {
            logger.error("Error resolving language " + language + " for siteKey '" + siteKey + "'", e);
        }

        if (!matches && currentSiteKey.length() > 0 && SettingsBean.getInstance().isUrlRewriteUseAbsoluteUrls()) {
            JahiaSite siteByKey = null;
            try {
                siteByKey = JahiaSitesService.getInstance().getSiteByKey(siteKey);
            } catch (JahiaException e) {
                logger.error("Error resolving site for site key " + siteKey,e);
            }
            String serverName = siteByKey != null && !Url.isLocalhost(siteByKey.getServerName()) ? siteByKey.getServerName() : null;
            if (StringUtils.isNotEmpty(serverName)) {
                int port = SettingsBean.getInstance().getSiteURLPortOverride();
                if (port == 0) {
                    port = request.getServerPort();
                }
                if (!(port == 80 && "http".equals(request.getScheme()) || port == 443
                        && "https".equals(request.getScheme()))) {
                    serverName = new StringBuilder().append(serverName).append(":").append(port)
                            .toString();
                }
            }
            request.setAttribute(ATTR_NAME_SITE_KEY_FOR_LINK, serverName);
            request.setAttribute(ATTR_NAME_SERVERNAME_FOR_LINK, serverName != null ? (request.getScheme() + "://" + serverName) : null);
        }
        
        checkCmsPrefix(request, ctx, path);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "analyzeLink({}, {}, {}, {}) | currentSiteKey={} targetSiteKey={} matches={}",
                    new Object[] { ctx, language, siteKey, path, currentSiteKey, siteKey, matches });
        }
    }

    public void getLinkLocale(HttpServletRequest request, String ctx, String path) {
        String linkLocale = request.getAttribute("currentLocale").toString();
        if (path.startsWith("/sites/")) {
            String siteKey = StringUtils.substringBetween(path,"/sites/", "/");
            try {
                JahiaSite site = JahiaSitesService.getInstance().getSiteByKey(siteKey);
                if (site != null && !site.getLanguages().contains(linkLocale)) {
                    linkLocale = site.getDefaultLanguage();
                }
            } catch (JahiaException e) {
                // cannot get site, don't change locale
            }
        }
        request.setAttribute("currentLinkLocale",linkLocale);
    }


    public void analyzeLink(HttpServletRequest request, String ctx, String siteKey, String path) {
        analyzeLink(request, ctx, null, siteKey, path);
    }

    public void checkCmsPrefix(HttpServletRequest request, String ctx, String input) {
        boolean doRemove = getUrlRewriteService().isSeoRemoveCmsPrefix();
        if (doRemove && input.length() > 0) {
            int end = input.indexOf('/');
            end = end == -1 ? input.indexOf('?') : end;
            doRemove = !getUrlRewriteService().isResrvedPrefix(
                    end != -1 ? input.substring(0, end) : input);
            if (logger.isDebugEnabled()) {
                logger.info("checkCmsPrefix({}): {}", input, doRemove);
            }
        }
        request.setAttribute(ATTR_NAME_CMS_TOKEN, doRemove ? "" : "/cms");
    }

    public void resetStateForOutboundUrl(HttpServletRequest request) {
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_CMS_TOKEN);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG_MATCHES);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_LANG_TOKEN);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY_FOR_LINK);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY_MATCHES);
        request.removeAttribute(VanityUrlMapper.VANITY_KEY);
    }
}
