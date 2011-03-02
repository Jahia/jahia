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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.sites.JahiaSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes the site key for the current server name as a request attribute if the corresponding server name is mapped to a site.
 * 
 * @author Sergiy Shyrkov
 */
public class ServerNameToSiteMapper {

    public static final String ATTR_NAME_SITE_KEY = "jahiaSiteKeyForCurrentServerName";
    public static final String ATTR_NAME_SITE_KEY_MATCHES = "jahiaSiteKeyMatchesCurrentServerName";
    private static final Logger logger = LoggerFactory.getLogger(ServerNameToSiteMapper.class);

    public static String getSiteKeyByServerName(HttpServletRequest request) {

        String targetSiteKey = (String) request.getAttribute(ATTR_NAME_SITE_KEY);
        if (targetSiteKey != null) {
            return targetSiteKey;
        }

        String host = request.getServerName();
        if (StringUtils.isEmpty(host) || URLGenerator.isLocalhost(host)) {
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
        request.setAttribute(ATTR_NAME_SITE_KEY, targetSiteKey);

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
        return site != null ? site.getSiteKey() : null;
    }

    public void canResolveSiteByServerName(HttpServletRequest request, String ctx, String language,
            String siteKey) {
        String currentSiteKey = getSiteKeyByServerName(request);
        boolean matches = currentSiteKey.equals(siteKey);
        request.setAttribute(ATTR_NAME_SITE_KEY_MATCHES, Boolean.valueOf(matches));
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "canResolveSiteByServerName({}, {}, {}) | currentSiteKey={} targetSiteKey={} matches {}",
                    new Object[] { ctx, language, siteKey, currentSiteKey, siteKey, matches });
        }
    }

    public void map(HttpServletRequest request) {
        getSiteKeyByServerName(request);
    }
}
