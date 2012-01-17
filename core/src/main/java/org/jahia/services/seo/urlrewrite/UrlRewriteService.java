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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
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

    private List<SimpleUrlHandlerMapping> renderMapping;

    private Resource[] seoConfigurationResources;

    private boolean seoRulesEnabled;

    private ServletContext servletContext;

    private UrlRewriteEngine urlRewriteEngine;

    private VanityUrlService vanityUrlService;

    private URLResolverFactory urlResolverFactory;

    public void afterPropertiesSet() throws Exception {
        long timer = System.currentTimeMillis();
        Log.setLevel("SLF4J");
        if (seoRulesEnabled && seoConfigurationResources != null
                && seoConfigurationResources.length > 0) {
            if (configurationResources != null) {
                Resource[] cfg = new Resource[configurationResources.length
                        + seoConfigurationResources.length];
                System.arraycopy(configurationResources, 0, cfg, 0, configurationResources.length);
                System.arraycopy(seoConfigurationResources, 0, cfg, configurationResources.length,
                        seoConfigurationResources.length);
                configurationResources = cfg;
            } else {
                configurationResources = seoConfigurationResources;
            }
        }

        if (configurationResources != null && configurationResources.length > 0
                && SettingsBean.getInstance().isDevelopmentMode()
                && (confReloadCheckIntervalSeconds < 0 || confReloadCheckIntervalSeconds > 5)) {
            confReloadCheckIntervalSeconds = 5;
            logger.info("Development mode is activated."
                    + " Setting URL rewriter configuration check interval to 5 seconds.");
        }
        // do first call to load the configuration and initialize the engine
        getEngine();

        logger.info("URL rewriting service started in {} ms using configurations [{}]."
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
                urlRewriteEngine.setUrlResolverFactory(urlResolverFactory);
                urlRewriteEngine.setVanityUrlService(vanityUrlService);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return urlRewriteEngine;
    }

    protected List<SimpleUrlHandlerMapping> getRenderMapping() {
        if (renderMapping == null) {
            LinkedList<SimpleUrlHandlerMapping> mapping = new LinkedList<SimpleUrlHandlerMapping>();
            ApplicationContext ctx = (ApplicationContext) servletContext.getAttribute(
                    "org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet");
            mapping.addAll(ctx.getBeansOfType(SimpleUrlHandlerMapping.class).values());
            mapping.addAll(ctx.getParent().getBeansOfType(SimpleUrlHandlerMapping.class)
                    .values());
            renderMapping = mapping;
        }

        return renderMapping;
    }

    public boolean isSeoRulesEnabled() {
        return seoRulesEnabled;
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

    public boolean prepareInbound(HttpServletRequest request, HttpServletResponse response) {
        if ("/cms".equals(request.getServletPath())) {
            String path = request.getPathInfo() != null ? request.getPathInfo() : "";
            try{
                List<SimpleUrlHandlerMapping> mappings = getRenderMapping();
                for (SimpleUrlHandlerMapping mapping : mappings) {
                    for (String registeredPattern : mapping.getUrlMap().keySet()) {
                        if (mapping.getPathMatcher().match(registeredPattern, path)) {
                            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SKIP_INBOUND_SEO_RULES, Boolean.TRUE);
                            return false;
                        }
                    }
                }
            }catch(Exception ex) {
                logger.warn("Unable to load the SimpleUrlHandlerMapping", ex);
            }
            String targetSiteKey = ServerNameToSiteMapper.getSiteKeyByServerName(request);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY, targetSiteKey);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG, StringUtils.EMPTY);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, StringUtils.EMPTY);
            if (!StringUtils.isEmpty(targetSiteKey)) {
                try {
                    List<VanityUrl> vanityUrls = vanityUrlService.findExistingVanityUrls(path,
                            targetSiteKey, "live");
                    if (!vanityUrls.isEmpty()) {
                        vanityUrls.get(0).getLanguage();
                        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG,
                                vanityUrls.get(0).getLanguage());
                        path = StringUtils.substringBefore(vanityUrls.get(0).getPath(), "/"
                                + VanityUrlManager.VANITYURLMAPPINGS_NODE + "/")
                                + ".html";
                        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, path);
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get vanity Url", e);
                }
                try {
                    String defaultLanguage = ServicesRegistry.getInstance().getJahiaSitesService()
                            .getSiteByKey(targetSiteKey).getDefaultLanguage();
                    request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG,
                            defaultLanguage);
                } catch (JahiaException e) {
                    logger.error("Cannot get site", e);
                }
            }

        }

        return true;
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

    public void setSeoConfigurationResources(Resource[] seoConfigurationResources) {
        this.seoConfigurationResources = seoConfigurationResources;
    }

    public void setSeoRulesEnabled(boolean seoRulesEnabled) {
        this.seoRulesEnabled = seoRulesEnabled;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setVanityUrlService(VanityUrlService vanityUrlService) {
        this.vanityUrlService = vanityUrlService;
    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

}
