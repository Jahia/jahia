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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.settings.SettingsBean;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * URL rewriter service.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlRewriteService implements InitializingBean, DisposableBean, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteService.class);

    private Resource[] configurationResources;

    /**
     * A user defined setting that says how often to check the configuration has changed. <b>0</b> means check on each call.
     */
    private int confReloadCheckIntervalSeconds = 0;

    private long lastChecked;

    private Map<Integer, Long> lastModified = new HashMap<Integer, Long>(1);

    private ServletContext servletContext;

    private UrlRewriteEngine urlRewriteEngine;

    public void afterPropertiesSet() throws Exception {
        long timer = System.currentTimeMillis();
        Log.setLevel("SLF4J");
        if (configurationResources != null && configurationResources.length > 0
                && SettingsBean.getInstance().isDevelopmentMode()
                && (confReloadCheckIntervalSeconds < 0 || confReloadCheckIntervalSeconds > 5)) {
            confReloadCheckIntervalSeconds = 5;
            logger.info("Development mode is activated."
                    + " Setting URL rewriter configuration check interval to 5 seconds.");
        }

        // do first call to load the configuration and initialize the engine
        getEngine();

        logger.info("URL rewriting service started in {} ms using configurations {}."
                + " Configuration check interval set to {} seconds.",
                new Object[] { System.currentTimeMillis() - timer, configurationResources,
                        confReloadCheckIntervalSeconds });

    }

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
                urlRewriteEngine = new UrlRewriteEngine(servletContext, configurationResources);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return urlRewriteEngine;
    }

    protected boolean needsReloading() throws IOException {
        if (confReloadCheckIntervalSeconds > -1 && configurationResources != null
                && configurationResources.length > 0) {

            boolean doReload = false;

            if (confReloadCheckIntervalSeconds == 0
                    || lastChecked == 0
                    || lastChecked + confReloadCheckIntervalSeconds * 1000L < System
                            .currentTimeMillis()) {
                logger.debug("Checking for modifications in URL rewriter configuration resources.");
                for (Resource resource : configurationResources) {
                    long resourceLastModified = FileUtils.getLastModified(resource);
                    int hash = resource.hashCode();
                    Long previous = lastModified.get(hash);
                    doReload = doReload || previous == null || resourceLastModified > previous;
                    lastModified.put(hash, Long.valueOf(resourceLastModified));
                }
                logger.debug(doReload ? "Changes detected" : "No changes detected");
            }

            lastChecked = System.currentTimeMillis();

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

    public void setConfigurationResources(Resource[] configurationResources) {
        this.configurationResources = configurationResources;
    }

    public void setConfReloadCheckIntervalSeconds(int confReloadCheckIntervalSeconds) {
        this.confReloadCheckIntervalSeconds = confReloadCheckIntervalSeconds;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}
