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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.sites.JahiaSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes the site key for the current server name as a request attribute if
 * the corresponding server name is mapped to a site.
 * 
 * @author Sergiy Shyrkov
 */
public class ServerNameToSiteMapper {

	private static final String ATTR_NAME = "jahiaServerNameSiteKey";
	private static final Logger logger = LoggerFactory.getLogger(ServerNameToSiteMapper.class);
	private static final String MAPPING_ATTR_NAME = ServerNameToSiteMapper.class.getName()
	        + ".mapping";

	private String lookupSiteKeyByServerName(String host) {
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

	public void map(HttpServletRequest request, HttpServletResponse response) {
		String host = request.getServerName();
		if (StringUtils.isEmpty(host)) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> mapping = (Map<String, String>) request.getAttribute(MAPPING_ATTR_NAME);
		if (mapping == null || !mapping.containsKey(host)) {
			if (mapping == null) {
				mapping = new HashMap<String, String>();
				request.setAttribute(ATTR_NAME, mapping);
			}
			mapping.put(host, lookupSiteKeyByServerName(host));
		}
		String targetSiteKey = mapping.get(host);
		if (targetSiteKey != null) {
			request.setAttribute(ATTR_NAME, targetSiteKey);
			logger.info("Mapping server name {} to site key {}", host, targetSiteKey);
		} else {
			logger.info("No site mapping found for server name {}", host);
		}

	}

}
