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

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.sites.JahiaSite;
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

    public static final String ATTR_NAME_DEFAULT_LANG = "siteDefaultLanguage";
    public static final String ATTR_NAME_DEFAULT_LANG_MATCHES = "jahiaSiteKeyMatchesDefaultLanguage";
    public static final String ATTR_NAME_SITE_KEY = "jahiaSiteKeyForCurrentServerName";
    public static final String ATTR_NAME_SITE_KEY_FOR_LINK = "jahiaSiteKeyForLink";
    public static final String ATTR_NAME_SITE_KEY_MATCHES = "jahiaSiteKeyMatchesCurrentServerName";
    public static final String ATTR_NAME_SKIP_INBOUND_SEO_RULES = "jahiaSkipInboundSeoRules";
    public static final String ATTR_NAME_VANITY_LANG = "vanityUrlTargetLang";
    public static final String ATTR_NAME_VANITY_PATH = "vanityUrlTargetPath";
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
                if (targetSiteKey != null) {
                    logger.debug("Mapping server name {} to site key {}", host, targetSiteKey);
                } else {
                    logger.debug("No site mapping found for server name {}", host);
                }
            }
        }

        return targetSiteKey;
    }

    private static String lookupSiteKeyByServerName(String host) {
        JahiaSite site = null;
        if (SpringContextSingleton.getInstance().isInitialized()) {
            try {
                site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSiteByServerName(host);
            } catch (JahiaException e) {
                logger.error("Error resolving site by server name '" + host + "'", e);
            }
        }
        return site != null ? site.getSiteKey() : "";
    }

    private static String lookupSiteServerNameByKey(String key) {
        JahiaSite site = null;
        if (SpringContextSingleton.getInstance().isInitialized()) {
            try {
                site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSiteByKey(key);
            } catch (JahiaException e) {
                logger.error("Error resolving site by key '" + key + "'", e);
            }
        }
        return site != null && !Url.isLocalhost(site.getServerName()) ? site.getServerName() : null;
    }

    public void canResolveSiteByServerName(HttpServletRequest request, String ctx, String language,
            String siteKey) {
        String currentSiteKey = getSiteKeyByServerName(request);
        boolean matches = currentSiteKey.equals(siteKey);
        request.setAttribute(ATTR_NAME_SITE_KEY_MATCHES, Boolean.valueOf(matches));

        try {
            JahiaSite siteByKey = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(siteKey);
            request.setAttribute(ATTR_NAME_DEFAULT_LANG_MATCHES, Boolean.valueOf(siteByKey.getDefaultLanguage().equals(language)));
        } catch (JahiaException e) {
            logger.error("Error resolving site by key '" + siteKey + "'", e);
        }

        if (!matches) {
            String serverName = lookupSiteServerNameByKey(siteKey);
            if (!StringUtils.isEmpty(serverName)) {
                if (!(("http".equals(request.getScheme()) && (request.getServerPort() == 80)) ||
                      ("https".equals(request.getScheme()) && (request.getServerPort() == 443)))) {
                    serverName += ":"+request.getServerPort();
                }
            }
            request.setAttribute(ATTR_NAME_SITE_KEY_FOR_LINK, serverName);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "canResolveSiteByServerName({}, {}, {}) | currentSiteKey={} targetSiteKey={} matches {}",
                    new Object[] { ctx, language, siteKey, currentSiteKey, siteKey, matches });
        }
    }
}
