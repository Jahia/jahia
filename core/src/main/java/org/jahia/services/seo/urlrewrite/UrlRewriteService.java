/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.api.io.IOResource;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.io.adapter.SpringResourceAdapter;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.util.UriUtils;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL rewriter service.
 *
 * @author Sergiy Shyrkov
 */
public class UrlRewriteService implements InitializingBean, DisposableBean, ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(UrlRewriteService.class);

    private List<IOResource> configurationResources;

    private List<IOResource> lastConfigurationResources;

    private List<IOResource> seoConfigurationResources;

    /**
     * A user defined setting that says how often to check the configuration has changed. <b>0</b> means check on each call.
     */
    private int confReloadCheckIntervalSeconds = 0;

    private long lastChecked;

    private Map<Integer, Long> lastModified = new ConcurrentHashMap<>(1);

    private List<HandlerMapping> renderMapping;

    private boolean seoRulesEnabled;

    private boolean seoRemoveCmsPrefix;

    private ServletContext servletContext;

    private UrlRewriteEngine urlRewriteEngine;

    private VanityUrlService vanityUrlService;

    private URLResolverFactory urlResolverFactory;

    private Set<String> reservedUrlPrefixSet;

    private JahiaSitesService siteService;

    private SettingsBean settingsBean;

    /**
     * Flag to indicate that a resource has been added or removed from one of the resource lists, needed to bypass reload check interval
     */
    private transient boolean modified;

    public void afterPropertiesSet() throws Exception {
        long timer = System.currentTimeMillis();
        Log.setLevel("SLF4J");

//        mergeConfigurationResources();

        if (hasConfigurationResources() && settingsBean.isDevelopmentMode() && (confReloadCheckIntervalSeconds < 0 || confReloadCheckIntervalSeconds > 5)) {
            confReloadCheckIntervalSeconds = 5;
            logger.info("Development mode is activated. Setting URL rewriter configuration check interval to 5 seconds.");
        }
        // do first call to load the configuration and initialize the engine
        getEngine();

        logger.info("URL rewriting service started in {} ms using configurations [{}]. Configuration check interval set to {} seconds.", System.currentTimeMillis() - timer, getMergedConfigurationResources(), confReloadCheckIntervalSeconds);
    }

    private boolean hasConfigurationResources() {
        return (configurationResources != null && !configurationResources.isEmpty()) || (seoConfigurationResources != null && !seoConfigurationResources.isEmpty()) || (lastConfigurationResources != null && !lastConfigurationResources.isEmpty());
    }

    private List<IOResource> getMergedConfigurationResources() {
        final int seoSize = seoConfigurationResources != null ? seoConfigurationResources.size() : 0;
        final int lastSize = lastConfigurationResources != null ? lastConfigurationResources.size() : 0;

        final List<IOResource> merged = configurationResources == null ? new ArrayList<>(seoSize + lastSize) : new ArrayList<>(configurationResources);

        // add SEO rules if provided and SEO URL rewriting is enabled
        if (seoRulesEnabled && seoSize > 0) {
            addAllWithoutDuplicates(merged, seoConfigurationResources);
        }

        // add rules which are executed as last, if provided
        if (lastSize > 0) {
            addAllWithoutDuplicates(merged, lastConfigurationResources);
        }

        return merged;
    }

    private void addAllWithoutDuplicates(List<IOResource> merged, List<IOResource> resourcesToAdd) {
        for (IOResource resource : resourcesToAdd) {
            // make sure we don't add duplicates
            if (!merged.contains(resource)) {
                merged.add(resource);
            }
        }
    }

    public void destroy() throws Exception {
        if (urlRewriteEngine != null) {
            urlRewriteEngine.destroy();
        }
    }

    /**
     * Initializes an instance of this class.
     */
    public UrlRewriteEngine getEngine() {
        try {
            // get merged resources if they were changed, null otherwise
            final List<IOResource> merged = getMergedResourcesIfReloadNeeded();
            if (urlRewriteEngine == null || merged != null) {
                if (urlRewriteEngine != null) {
                    urlRewriteEngine.destroy();
                }
                urlRewriteEngine = new UrlRewriteEngine(servletContext, merged.toArray(new IOResource[merged.size()]));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return urlRewriteEngine;
    }

    protected List<HandlerMapping> getRenderMapping() {
        if (renderMapping == null) {
            LinkedList<HandlerMapping> mapping = new LinkedList<HandlerMapping>();
            ApplicationContext ctx = (ApplicationContext) servletContext.getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet");
            if (ctx != null) {
                mapping.addAll(ctx.getBeansOfType(HandlerMapping.class).values());
                mapping.addAll(ctx.getParent().getBeansOfType(HandlerMapping.class).values());
                renderMapping = mapping;
            }
        }
        if (renderMapping != null) {
            List<HandlerMapping> l = new LinkedList<HandlerMapping>(renderMapping);
            l.addAll(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getSpringHandlerMappings());
            return l;
        }
        return null;
    }

    public boolean isSeoRulesEnabled() {
        return seoRulesEnabled;
    }

    public boolean isResrvedPrefix(String prefix) {
        return reservedUrlPrefixSet.contains(prefix);
    }

    /**
     * @return <code>null</code> if resources were unchanged, the list of updated resources otherwise
     * @throws IOException
     */
    private List<IOResource> getMergedResourcesIfReloadNeeded() throws IOException {
        if (confReloadCheckIntervalSeconds > -1 && hasConfigurationResources()) {

            boolean doReload = false;
            List<IOResource> result = null;
            if (modified || confReloadCheckIntervalSeconds == 0 || lastChecked == 0 || lastChecked + confReloadCheckIntervalSeconds * 1000L < System.currentTimeMillis()) {
                logger.debug("Checking for modifications in URL rewriter configuration resources.");
                List<IOResource> mergedConfigurationResources = getMergedConfigurationResources();
                if (modified) {
                    result = mergedConfigurationResources;
                } else {
                    // look at last modified time for resources
                    for (IOResource resource : mergedConfigurationResources) {
                        long resourceLastModified = resource.lastModified();
                        int hash = resource.hashCode();
                        Long previous = lastModified.get(hash);

                        // if we detected that a resource was modified since last time we checked, we don't need to look further
                        doReload = previous == null || resourceLastModified > previous;
                        if (doReload) {
                            lastModified.put(hash, resourceLastModified);
                            result = mergedConfigurationResources;
                            break;
                        }
                    }
                    logger.debug(doReload ? "Changes detected" : "No changes detected");
                }
            }

            lastChecked = System.currentTimeMillis();
            modified = false; // we've checked if there were changes, so reset modified flag

            return result;
        }
        return null;
    }

    public boolean prepareInbound(final HttpServletRequest request, HttpServletResponse response) {
        resetState(request);
        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_CONTEXT_PATH, request.getContextPath());
        String input = request.getRequestURI();
        if (request.getContextPath().length() > 0) {
            input = StringUtils.substringAfter(input, request.getContextPath());
        }
        if (input.contains(";")) {
            input = StringUtils.substringBefore(input, ";");
        }

        String prefix = StringUtils.EMPTY;
        if (isSeoRemoveCmsPrefix() && input.length() > 1 && input.indexOf('/') == 0) {
            int end = input.indexOf('/', 1);
            prefix = end != -1 ? input.substring(1, end) : input.substring(1);
        }
        if (prefix.length() > 1) {
            boolean contains = reservedUrlPrefixSet.contains(prefix);
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_ADD_CMS_PREFIX, !contains);
            if (contains && !"cms".equals(prefix) && !"files".equals(prefix)) {
                return false;
            }
        } else {
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_ADD_CMS_PREFIX, false);
        }

        String path = request.getPathInfo() != null ? request.getPathInfo() : input;
        try {
            List<HandlerMapping> mappings = getRenderMapping();
            if (mappings != null) {
                for (HandlerMapping mapping : mappings) {
                    if (mapping instanceof SimpleUrlHandlerMapping) {
                        SimpleUrlHandlerMapping simpleUrlHandlerMapping = (SimpleUrlHandlerMapping) mapping;
                        for (String registeredPattern : simpleUrlHandlerMapping.getUrlMap().keySet()) {
                            if (simpleUrlHandlerMapping.getPathMatcher().match(registeredPattern, path)) {
                                request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SKIP_INBOUND_SEO_RULES, Boolean.TRUE);
                                return false;
                            }
                        }
                    } else {
                        HandlerExecutionChain handlerExecutionChain = mapping.getHandler(request);
                        if (handlerExecutionChain != null) {
                            // we found an execution chain for this handler, we deactivate SEO rules for this request.
                            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SKIP_INBOUND_SEO_RULES, Boolean.TRUE);
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Unable to load the handler mappings", ex);
        }
        String targetSiteKey = ServerNameToSiteMapper.getSiteKeyByServerName(request);
        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG, StringUtils.EMPTY);
        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, StringUtils.EMPTY);
        if (StringUtils.isNotEmpty(targetSiteKey) && !path.startsWith("/sites")) {
            try {
                List<VanityUrl> vanityUrls = vanityUrlService.findExistingVanityUrls(UriUtils.decode(path, "UTF-8"), targetSiteKey, "live");
                if (!vanityUrls.isEmpty()) {
                    VanityUrl vanityUrl = vanityUrls.get(0);
                    request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG, vanityUrl.getLanguage());
                    path = StringUtils.substringBefore(vanityUrl.getPath(), "/" + VanityUrlManager.VANITYURLMAPPINGS_NODE + "/");
                    request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH, path);
                    request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_ISFILE, vanityUrl.isFile());
                }
            } catch (RepositoryException | UnsupportedEncodingException e) {
                logger.error("Cannot get vanity Url", e);
            }
        } else if (path.startsWith("/sites/")) {
            targetSiteKey = StringUtils.substringAfter(path, "/sites/");
            if (targetSiteKey.contains("/")) {
                targetSiteKey = StringUtils.substringBefore(targetSiteKey, "/");
            } else if (targetSiteKey.contains(".")) {
                // remove templateType from the url
                targetSiteKey = StringUtils.substringBeforeLast(targetSiteKey, ".");
            }
        }

        try {
            String language = siteService.getSiteDefaultLanguage(targetSiteKey);
            if (language == null) {
                // remove template from the url
                language = siteService.getSiteDefaultLanguage(StringUtils.substringBeforeLast(targetSiteKey, "."));
            }
            request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG, language);
        } catch (JahiaException e) {
            logger.error("Cannot get site for key " + targetSiteKey, e);
        }

        return true;
    }

    private void resetState(HttpServletRequest request) {
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_CMS_TOKEN);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_DEFAULT_LANG_MATCHES);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_LANG_TOKEN);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_ADD_CMS_PREFIX);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY_FOR_LINK);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SERVERNAME_FOR_LINK);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY_MATCHES);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_SKIP_INBOUND_SEO_RULES);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_LANG);
        request.removeAttribute(ServerNameToSiteMapper.ATTR_NAME_VANITY_PATH);
    }

    public RewrittenUrl rewriteInbound(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InvocationTargetException {
        return getEngine().processRequest(request, response);
    }

    public String rewriteOutbound(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, InvocationTargetException {
        return getEngine().rewriteOutbound(url, request, response);
    }

    // not deprecated, used as Spring bean setters, so valid usage of Resource for now.
    public void setConfigurationResources(Resource[] configurationResources) {
        setConfigurationIOResources(SpringResourceAdapter.fromSpring(configurationResources));
    }

    public void setConfigurationIOResources(IOResource[] configurationResources) {
        this.configurationResources = createIfNeededAndAddAll(configurationResources, this.configurationResources);
    }

    /**
     * @deprecated use {@link #addConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void addConfigurationResource(Resource resource) {
        addConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void addConfigurationIOResource(IOResource resource) {
        configurationResources = addTo(resource, configurationResources);
    }

    /**
     * @deprecated use {@link #removeConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void removeConfigurationResource(Resource resource) {
        removeConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void removeConfigurationIOResource(IOResource resource) {
        removeFrom(resource, configurationResources);
    }

    public void setConfReloadCheckIntervalSeconds(int confReloadCheckIntervalSeconds) {
        this.confReloadCheckIntervalSeconds = confReloadCheckIntervalSeconds;
    }

    // not deprecated, used as Spring bean setters, so valid usage of Resource for now.
    public void setSeoConfigurationResources(Resource[] seoConfigurationResources) {
        setSeoConfigurationIOResources(SpringResourceAdapter.fromSpring(seoConfigurationResources));
    }

    public void setSeoConfigurationIOResources(IOResource[] seoConfigurationResources) {
        this.seoConfigurationResources = createIfNeededAndAddAll(seoConfigurationResources, this.seoConfigurationResources);
    }

    /**
     * @deprecated use {@link #addSeoConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void addSeoConfigurationResource(Resource resource) {
        addSeoConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void addSeoConfigurationIOResource(IOResource resource) {
        seoConfigurationResources = addTo(resource, seoConfigurationResources);
    }

    /**
     * @deprecated use {@link #removeSeoConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0")
    public void removeSeoConfigurationResource(Resource resource) {
        removeSeoConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void removeSeoConfigurationIOResource(IOResource resource) {
        removeFrom(resource, seoConfigurationResources);
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

    public void setReservedUrlPrefixes(String reservedUrlPrefixes) {
        Set<String> prefixes = new HashSet<String>();
        if (StringUtils.isBlank(reservedUrlPrefixes)) {
            prefixes = Collections.emptySet();
        } else {
            for (String prefix : StringUtils.split(reservedUrlPrefixes, ",")) {
                if (StringUtils.isNotBlank(prefix)) {
                    prefixes.add(StringUtils.trim(prefix));
                }
            }
        }
        this.reservedUrlPrefixSet = prefixes;
    }

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    // not deprecated, used as Spring bean setters, so valid usage of Resource for now.
    public void setLastConfigurationResources(Resource[] postSeoConfigurationResources) {
        setLastConfigurationIOResources(SpringResourceAdapter.fromSpring(postSeoConfigurationResources));
    }

    public void setLastConfigurationIOResources(IOResource[] postSeoConfigurationResources) {
        this.lastConfigurationResources = createIfNeededAndAddAll(postSeoConfigurationResources, this.lastConfigurationResources);
    }

    /**
     * @deprecated use {@link #addLastConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void addLastConfigurationResource(Resource resource) {
        addLastConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void addLastConfigurationIOResource(IOResource resource) {
        lastConfigurationResources = addTo(resource, lastConfigurationResources);
    }

    /**
     * @deprecated use {@link #removeLastConfigurationIOResource(IOResource)} instead, this method is not used anymore and should be removed in future versions
     */
    @Deprecated(since = "8.2.4.0", forRemoval = true)
    public void removeLastConfigurationResource(Resource resource) {
        removeLastConfigurationIOResource(SpringResourceAdapter.fromSpring(resource));
    }

    public void removeLastConfigurationIOResource(IOResource resource) {
        removeFrom(resource, lastConfigurationResources);
    }

    public boolean isSeoRemoveCmsPrefix() {
        return seoRemoveCmsPrefix;
    }

    public void setSeoRemoveCmsPrefix(boolean seoRemoveCmsPrefix) {
        this.seoRemoveCmsPrefix = seoRemoveCmsPrefix;
    }

    private List<IOResource> addTo(IOResource resource, List<IOResource> resources) {
        if (resource != null) {
            if (resources == null) {
                resources = new ArrayList<>();
            }
            if (!resources.contains(resource)) {
                resources.add(resource);
                modified = true; // set modified flag
            }
        }

        return resources;
    }

    private void removeFrom(IOResource resource, List<IOResource> resources) {
        if (resources != null && resource != null) {
            resources.remove(resource);
            modified = true; // set modified flag
        }
    }

    private List<IOResource> createIfNeededAndAddAll(IOResource[] newResources, List<IOResource> resources) {
        if (newResources != null) {
            resources = new ArrayList<>(newResources.length);
            Collections.addAll(resources, newResources);
        }

        return resources;
    }
}
