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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * URL rewriter service.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlRewriteService implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(UrlRewriteService.class);

	private Resource configurationResource;

	/**
	 * A user defined setting that says how often to check the configuration has
	 * changed.
	 */
	private int confReloadCheckInterval = 0;

	private long lastModified;

	private UrlRewriteEngine urlRewriteEngine;

	public void destroy() throws Exception {
		if (urlRewriteEngine != null) {
			urlRewriteEngine.destroy();
		}
	}

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param configs
	 *            the URL rewriter configuration resource location
	 */
	public UrlRewriteEngine getEngine() {
		try {
			if (urlRewriteEngine == null || needsReloading()) {
				if (urlRewriteEngine != null) {
					urlRewriteEngine.destroy();
				}
				urlRewriteEngine = new UrlRewriteEngine(configurationResource);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return urlRewriteEngine;
	}

	protected boolean needsReloading() throws IOException {
		if (confReloadCheckInterval > 0
		        && configurationResource != null
		        && (lastModified <= 0 || lastModified + confReloadCheckInterval < System
		                .currentTimeMillis())) {

			URL resourceUrl = configurationResource.getURL();
			long newLastModified = ResourceUtils.isJarURL(resourceUrl) ? ResourceUtils.getFile(
			        ResourceUtils.extractJarFileURL(resourceUrl)).lastModified()
			        : configurationResource.getFile().lastModified();

			boolean doReload = lastModified <= 0 || newLastModified > lastModified;

			lastModified = newLastModified;

			return doReload;
		}
		return false;
	}

	public RewrittenUrl rewriteInbound(HttpServletRequest request, HttpServletResponse response)
	        throws IOException, ServletException, InvocationTargetException {
		return getEngine().processRequest(request, response);
	}

	public String rewriteOutbound(String url, HttpServletRequest request,
	        HttpServletResponse response) throws IOException, ServletException,
	        InvocationTargetException {
		return getEngine().rewriteOutbound(url, request, response);
	}

	public void setConfigurationResource(Resource configurationResource) {
		this.configurationResource = configurationResource;
	}

	public void setConfReloadCheckInterval(int confReloadCheckInterval) {
		this.confReloadCheckInterval = confReloadCheckInterval;
	}

	public void afterPropertiesSet() throws Exception {
		Log.setLevel("SLF4J");
		if (configurationResource != null && SettingsBean.getInstance().isDevelopmentMode()
		        && (confReloadCheckInterval == 0 || confReloadCheckInterval < 5000)) {
			confReloadCheckInterval = 5000;
			logger.info("Development mode is activated."
			        + " Setting URL rewriter configuration check interval to 5 seconds.");
		}
	}

}
