/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache render filter, in charge of providing the html for a given fragment (from the cache or by generating it)
 * Then cache the result if necessary
 *
 * Created by jkevan on 12/04/2016.
 */
public class CacheFilter extends AbstractFilter {

    private static final String FLAG_VERSION = "v";
    private static final String FLAG_RANDOM = "ec";
    private static final String FLAG_ALL = "ALL";
    private static final Set<String> FLAGS_ALL_SET = Collections.singleton(FLAG_ALL);

    // Flags used to know if we have to cache the fragment or not,
    // - used when the fragment is return from the cache in the prepare() to avoid cache it again in execute()
    // - used when the fragment is not cacheable (this is calculate in the prepare)
    public static final String FRAGMENT_SERVED_FROM_CACHE = "cacheFilter.fragment.servedFromCache";
    public static final String FRAGMENT_NOT_CACHEABLE = "cacheFilter.fragment.notCacheable";

    // Used to measure the rendering time of fragments
    private static final String RENDERING_TIMER = "cacheFilter.rendering.time";

    private static final Logger logger = LoggerFactory.getLogger(CacheFilter.class);

    protected ModuleCacheProvider cacheProvider;
    protected boolean cascadeFragmentErrors = false;
    protected int errorCacheExpiration = 5;
    protected int dependenciesLimit = 1000;

    private ModuleGeneratorQueue generatorQueue;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {

        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) renderContext.getRequest().getAttribute("moduleMap");
        if (!isCacheFilterEnabled(moduleMap)) {
            return null;
        }

        moduleMap.put(RENDERING_TIMER, System.currentTimeMillis());
        final String path = resource.getNodePath();
        final String key = (String) moduleMap.get(AggregateFilter.RENDERING_KEY);
        final String finalKey = (String) moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY);

        Element element = null;
        final Cache cache = cacheProvider.getCache();

        try {
            logger.debug("Try to get fragment from cache with final key: {}", finalKey);
            element = cache.get(finalKey);
        } catch (LockTimeoutException e) {
            logger.warn("Error while rendering " + renderContext.getMainResource() + e.getMessage(), e);
        }

        if (element != null && element.getObjectValue() != null) {
            logger.debug("Fragment found in cache: {} ", path);
            return returnFromCache(renderContext, key, finalKey, element, moduleMap);
        } else {

            logger.debug("Fragment not found in cache: {} ", path);

            // Fragment need to be generate, load the node in the resource to avoid performance warning messages
            // all the code execute after this allow to read the node from JCR.
            resource.safeLoadNode();

            // test if fragment is not cacheable, no need for a latch if the fragment is not cacheable
            if (isCacheable(renderContext, key, resource)) {

                // The element is not found in the cache with that key. use latch to allow only one thread to generate the fragment
                // All other threads will wait until the first thread finish the generation, then the LatchReleasedCallback will be executed
                // for all the waiting threads.
                logger.debug("Use latch to decide between generate or waiting fragment: ", path);

                if (generatorQueue.getLatch(renderContext, finalKey)) {
                    element = cache.get(finalKey);
                    if (element != null && element.getObjectValue() != null) {
                        logger.debug("Latch released for fragment: {} and fragment found in cache", path);
                        return returnFromCache(renderContext, key, finalKey, element, moduleMap);
                    }
                    logger.debug("Latch released for fragment: {} but fragment not found in cache, generate it", path);
                }
                return null;
            } else {
                // store this fragment as a known non cacheable fragment
                cacheProvider.addNonCacheableFragment(key);

                // Add flag to say that this fragment is not cacheable
                moduleMap.put(FRAGMENT_NOT_CACHEABLE, Boolean.TRUE);
                return null;
            }
        }
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return execute(previousOut, renderContext, resource, false);
    }

    private String execute(String previousOut, RenderContext renderContext, Resource resource, boolean bypassDependencies) throws RepositoryException {

        HttpServletRequest request = renderContext.getRequest();
        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");
        if (!isCacheFilterEnabled(moduleMap)) {
            return previousOut;
        }

        String key = (String) moduleMap.get(AggregateFilter.RENDERING_KEY);

        // Check if we have to put the fragment in cache
        if (moduleMap.get(FRAGMENT_NOT_CACHEABLE) == null && moduleMap.get(FRAGMENT_SERVED_FROM_CACHE) == null) {

            Properties fragmentProperties = cacheProvider.getKeyGenerator().getAttributesForKey(renderContext, resource);

            // because fragment is not served from the cache, but the all render chain, we check that the key is still
            // the same after prepare() and execute() of all the render filters after the cache.
            String generatedKey = cacheProvider.getKeyGenerator().generate(resource, renderContext, fragmentProperties);
            if (!generatedKey.equals(key)) {
                logger.warn("Key generation does not give the same result after execution , was" + key + " , now is " + generatedKey);
            }

            String finalKey = (String) moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY);

            if (!bypassDependencies) {
                // Add self path as dependency for this fragment (for cache flush - will not impact the key)
                // necessary even if resource is already storing self node in dep, because of referenced nodes, for exemple:
                // resource: /sites/ACMESPACE/home/main/content-reference@/news_36-3
                // already have this path as dependency
                // but it need the referenced node also as dependency, and the referenced node path is retrieve using getCanonicalPath() that will return this:
                // /sites/ACMESPACE/contents/projects-news/news_36-3
                resource.getDependencies().add(resource.getNode().getCanonicalPath());

                // Add main resource if cache.mainResource is set
                if ("true".equals(fragmentProperties.getProperty("cache.mainResource"))) {
                    resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
                }
            }

            logger.debug("Caching fragment {} for final key: {}", resource.getPath(), finalKey);

            doCache(previousOut, renderContext, resource, Long.parseLong(fragmentProperties.getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION)), cacheProvider.getCache(), finalKey, bypassDependencies);
            // content is in cache and available, release latch for other threads waiting for this fragment
            generatorQueue.releaseLatch(finalKey);
        }

        String result = previousOut;

        // Append debug information
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(request.getParameter("cacheinfo"));
        if (displayCacheInfo && !result.contains("<body") && result.trim().length() > 0) {
            result = appendDebugInformation(renderContext, key, result);
        }

        logCacheFilterRenderingTime(resource, moduleMap);
        return result;
    }

    private void logCacheFilterRenderingTime(Resource resource, Map<String ,Object> moduleMap) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        Object servedFromCacheAttribute = moduleMap.get(FRAGMENT_SERVED_FROM_CACHE);
        Boolean isServerFromCache = servedFromCacheAttribute != null && (Boolean) servedFromCacheAttribute;
        String cacheLogMsg = isServerFromCache ? "served fragment {} from cache in {} ms" : "generated fragment {} in {} ms";
        long start = (Long) moduleMap.get(RENDERING_TIMER);
        logger.debug(cacheLogMsg, resource.getPath(), System.currentTimeMillis() - start);
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {

        // If an error occured during render and the latch is not release during the execute() it's important that we release it
        // in any case to avoid threads waiting for nothing

        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) renderContext.getRequest().getAttribute("moduleMap");
        if (isCacheFilterEnabled(moduleMap)) {
            generatorQueue.releaseLatch((String) moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        }
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {

        super.getContentForError(renderContext, resource, chain, e);
        HttpServletRequest request = renderContext.getRequest();
        Map<String, Object> moduleMap = (Map<String, Object>) request.getAttribute("moduleMap");
        if (!isCacheFilterEnabled(moduleMap) || cascadeFragmentErrors || Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration())) {
            return null;
        }

        try {

            request.setAttribute("expiration", Integer.toString(errorCacheExpiration));
            logger.error(e.getMessage(), e);

            final String key = (String) moduleMap.get(AggregateFilter.RENDERING_KEY);
            final String finalKey = (String) moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY);

            Element element = null;
            try {
                Cache cache = cacheProvider.getCache();
                element = cache.get(finalKey);
            } catch (LockTimeoutException ex) {
                logger.warn("Error while rendering " + renderContext.getMainResource() + ex.getMessage(), ex);
            }

            if (element != null && element.getObjectValue() != null) {
                return returnFromCache(renderContext, key, finalKey, element, (Map<String, Object>) request.getAttribute("moduleMap"));
            }

            // Returns a fragment with an error comment
            return execute("<!-- Module error : " + HtmlUtils.htmlEscape(e.getMessage()) + "-->", renderContext, resource, true);
        } catch (Exception e1) {
            return null;
        }
    }

    private boolean isCacheFilterEnabled(Map<String, Object> moduleMap) {
        return moduleMap.get(AggregateFilter.RENDERING_KEY) != null && moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY) != null;
    }

    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Long expiration, Cache cache, String finalKey, boolean bypassDependencies) {

        Set<String> depNodeWrappers = Collections.emptySet();

        // Create the fragment entry based on the rendered content
        CacheEntry<String> cacheEntry = new CacheEntry<String>(previousOut);

        // Store some properties that may have been set during fragment execution (todo : handle this another way)
        addPropertiesToCacheEntry(cacheEntry, renderContext);

        Element cachedElement = new Element(finalKey, cacheEntry);

        if (expiration > 0) {
            cachedElement.setTimeToLive(expiration.intValue());
        }
        if (!bypassDependencies) {
            storeDependencies(renderContext, resource, finalKey, depNodeWrappers);
        }
        cache.put(cachedElement);

        if (logger.isDebugEnabled()) {
            logger.debug("Store in cache content of fragment with key: {}", finalKey);
            StringBuilder stringBuilder = new StringBuilder();
            for (String path : depNodeWrappers) {
                stringBuilder.append(path).append("\n");
            }
            logger.debug("Dependencies of {}:\n", finalKey, stringBuilder.toString());
        }
    }

    /**
     * Store the dependencies in the dependency cache. For each dependency, an entry in the dependencies cache is
     * created, containing the list of keys which depends on it. The current fragment key is added to that list.
     *
     * @param resource        The current resource
     * @param depNodeWrappers The list of dependencies
     */
    @SuppressWarnings("unchecked")
    private void storeDependencies(RenderContext renderContext, Resource resource, String finalKey, Set<String> depNodeWrappers) {
        if (useDependencies()) {
            final Cache dependenciesCache = cacheProvider.getDependenciesCache();
            depNodeWrappers = resource.getDependencies();
            for (String path : depNodeWrappers) {
                if(!path.startsWith("/modules")) {
                    Element element1 = dependenciesCache.get(path);
                    Set<String> dependencies = element1 != null ? (Set<String>) element1.getObjectValue() : Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                    if (!dependencies.contains(FLAG_ALL)) {
                        if ((dependencies.size() + 1) > dependenciesLimit) {
                            Element element = new Element(path, FLAGS_ALL_SET);
                            element.setEternal(true);
                            dependenciesCache.put(element);
                        } else {
                            addDependencies(renderContext, finalKey, dependenciesCache, path, dependencies);
                        }
                    }
                }
            }
            final Cache regexpDependenciesCache = cacheProvider.getRegexpDependenciesCache();
            Set<String> regexpDepNodeWrappers = resource.getRegexpDependencies();
            for (String regexp : regexpDepNodeWrappers) {
                Element element1 = regexpDependenciesCache.get(regexp);
                Set<String> dependencies = element1 != null ? (Set<String>) element1.getObjectValue() : Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                addDependencies(renderContext, finalKey, regexpDependenciesCache, regexp, dependencies);
            }
        }
        resource.getDependencies().clear();
        resource.getRegexpDependencies().clear();
    }

    /**
     * Sets whether dependencies should be stored per cache object for this filter, which is useful for dependent
     * flushes.
     *
     * @return true if filter uses dependencies, false if not
     */
    protected boolean useDependencies() {
        return true;
    }

    /**
     * Add key to the list of dependencies
     */
    protected void addDependencies(RenderContext renderContext, String finalKey, Cache cache, String value, Set<String> newDependencies) {
        if (newDependencies.add(finalKey)) {
            cache.put(new Element(value, newDependencies));
        }
    }

    /**
     * Store some properties that may have been set during fragment execution
     */
    @SuppressWarnings("unchecked")
    private void addPropertiesToCacheEntry(CacheEntry<String> cacheEntry, RenderContext renderContext) {
        Map<String,Object> moduleMap = (Map<String, Object>) renderContext.getRequest().getAttribute("moduleMap");
        if (moduleMap != null && moduleMap.containsKey("requestAttributesToCache")){
            HashMap<String,Serializable> attributes = new HashMap<>();
            Collection<String> requestAttributesToCache = (Collection<String>) moduleMap.get("requestAttributesToCache");
            for (String attributesToCache : requestAttributesToCache) {
                if (renderContext.getRequest().getAttribute(attributesToCache) instanceof Serializable) {
                    attributes.put(attributesToCache, (Serializable) renderContext.getRequest().getAttribute(attributesToCache));
                }
            }
            cacheEntry.setProperty("requestAttributes", attributes);
        }
    }

    /**
     * Restore properties that have been stored in cache entry
     */
    @SuppressWarnings("unchecked")
    private void restorePropertiesFromCacheEntry(CacheEntry cacheEntry, RenderContext renderContext) {
        if (cacheEntry.getProperty("requestAttributes") != null) {
            Map<String,Serializable> requestAttributesToCache = (Map<String, Serializable>) cacheEntry.getProperty("requestAttributes");
            for (Map.Entry<String, Serializable> entry : requestAttributesToCache.entrySet()) {
                renderContext.getRequest().setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Is the current fragment cacheable or not. Based on the nonCacheableFragments list and the ec or v parameter.
     *
     * @param renderContext render context
     * @param resource      current resource
     * @param key           calculated cache key
     * @return true if fragments is cacheable, false if not
     */
    protected boolean isCacheable(RenderContext renderContext, String key, Resource resource) throws RepositoryException {

        // first check if the key is not part of the non cacheable fragments
        if (cacheProvider.isNonCacheableFragment(key)) {
            return false;
        }

        // check v parameter
        if (renderContext.getRequest().getParameter(FLAG_VERSION) != null && renderContext.isLoggedIn()) {
            return false;
        }

        // check ec parameter
        final String ecParameter = renderContext.getRequest().getParameter(FLAG_RANDOM);
        if (ecParameter != null) {
            if (ecParameter.equals(resource.getNode().getIdentifier())) {
                return false;
            }
            for (Resource parent : renderContext.getResourcesStack()) {
                if (ecParameter.equals(parent.getNode().getIdentifier())) {
                    return false;
                }
            }
        }

        // check if we have a valid cache expiration
        final String cacheExpiration = cacheProvider.getKeyGenerator().getAttributesForKey(renderContext, resource).getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_EXPIRATION);
        Long expiration = cacheExpiration != null ? Long.parseLong(cacheExpiration) : -1;
        return expiration != 0L;
    }

    /**
     * This method is called if the entry is found in the cache. It will get the fragment entry, and regenerates the
     * full content by aggregating all included fragments.
     *
     * @param renderContext The render context
     * @param key           The key with placeholders
     * @param finalKey      The final key with placeholders replaced
     * @param element       The cached element
     * @param moduleMap     The current module map
     */
    protected String returnFromCache(RenderContext renderContext, String key, String finalKey, Element element, Map<String, Object> moduleMap) {

        logger.debug("Content retrieved from cache for node with key: {}", finalKey);
        CacheEntry<?> cacheEntry = (CacheEntry<?>) element.getObjectValue();
        String cachedContent = (String) cacheEntry.getObject();

        // restore properties from cache entry
        // (todo : handle this another way), kept for now because fix pager issue
        restorePropertiesFromCacheEntry(cacheEntry, renderContext);

        // Add attr to say that this fragment have been served by the cache, to avoid cache it again
        moduleMap.put(FRAGMENT_SERVED_FROM_CACHE, Boolean.TRUE);

        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, cachedContent);
        } else {
            return cachedContent;
        }
    }

    protected String appendDebugInformation(RenderContext renderContext, String key, String renderContent) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Expiration: </span><span>");
        String key1 = cacheProvider.getKeyGenerator().replacePlaceholdersInCacheKey(renderContext, key);
        if (!cacheProvider.isNonCacheableFragment(key)) {
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(cacheProvider.getCache().get(
                    key1).getExpirationTime())));
        } else {
            stringBuilder.append("Not cached fragment ").append(SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        }
        stringBuilder.append("</span><br/>");
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setCascadeFragmentErrors(boolean cascadeFragmentErrors) {
        this.cascadeFragmentErrors = cascadeFragmentErrors;
    }

    public void setErrorCacheExpiration(int errorCacheExpiration) {
        this.errorCacheExpiration = errorCacheExpiration;
    }

    public void setDependenciesLimit(int dependenciesLimit) {
        this.dependenciesLimit = dependenciesLimit;
    }

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }
}
