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
import org.apache.commons.lang.StringUtils;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.springframework.web.util.HtmlUtils;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Cache render filter, in charge of providing the html for a given fragment (from the cache or by generating it)
 * Then cache the result if necessary
 *
 * Created by jkevan on 12/04/2016.
 */
public class CacheFilter extends AbstractFilter {
    protected transient static final Logger logger = org.slf4j.LoggerFactory.getLogger(CacheFilter.class);

    public static final String CACHE_PER_USER = "cache.perUser";
    public static final String CACHE_PER_USER_PROPERTY = "j:perUser";
    public static final String CACHE_EXPIRATION = "cache.expiration";
    public static final String V = "v";
    public static final String EC = "ec";
    public static final String ALL = "ALL";
    public static final Set<String> ALL_SET = Collections.singleton(ALL);

    protected ModuleCacheProvider cacheProvider;
    protected ModuleGeneratorQueue generatorQueue;
    protected boolean cascadeFragmentErrors = false;
    protected int errorCacheExpiration = 5;
    protected int dependenciesLimit = 1000;

    static protected ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    static protected ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    static protected ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final boolean debugEnabled = logger.isDebugEnabled();
        final String key = (String) renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering");

        // Replace the placeholders to have the final key that is used in the cache.
        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        /* TODO reimplemt Keeps a list of keys being generated to avoid infinite loops.
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<>();
            userKeys.set(userKeysLinkedList);
        }
        if (userKeysLinkedList.contains(finalKey)) {
            return null;
        }
        userKeysLinkedList.add(0, finalKey);
        */

        Element element = null;
        final Cache cache = cacheProvider.getCache();

        try {
            if (debugEnabled) {
                logger.debug("Try to get content from cache for node with final key: {}", finalKey);
            }
            element = cache.get(finalKey);
        } catch (LockTimeoutException e) {
            logger.warn("Error while rendering " + renderContext.getMainResource() + e.getMessage(), e);
        }

        if (element != null && element.getObjectValue() != null) {
            // The element is found in the cache. Need to
            return returnFromCache(renderContext, resource, key, finalKey, element, cache);
        } else {
            // resource is lazy in aggregation so call .getNode(), will load the node from jcr and store it in the resource
            if (resource.safeLoadNode() == null) {
                // Node is not available anymore, return empty content for this fragment
                // TODO throw NodeNotFoundException ?
                return StringUtils.EMPTY;
            }

            // TODO reimplemt latch
            // The element is not found in the cache with that key. Use CountLatch to avoid parallel processing of the
            // module - if somebody else is generating this fragment, wait for the entry to be generated and
            // return the content from the cache. Otherwise, return null to continue the render chain.
            // Note that the fragment MIGHT be in cache, but the key may not be correct - some parameters impacting the
            // key like dependencies can only be calculated when the fragment has been generated.
//            CountDownLatch countDownLatch = avoidParallelProcessingOfSameModule(finalKey, renderContext.getRequest(), resource, properties);
//            if (countDownLatch == null) {
//                element = cache.get(finalKey);
//                if (element != null && element.getObjectValue() != null) {
//                    return returnFromCache(renderContext, resource, key, finalKey, element, cache);
//                }
//            } else {
//                Set<CountDownLatch> latches = processingLatches.get();
//                if (latches == null) {
//                    latches = new HashSet<>();
//                    processingLatches.set(latches);
//                }
//                latches.add(countDownLatch);
//            }
            return null;
        }
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return execute(previousOut, renderContext, resource, chain, false);
    }

    private String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain, boolean bypassDependencies) throws Exception {
        final Cache cache = cacheProvider.getCache();
        String key = (String) renderContext.getRequest().getAttribute("aggregateCacheFilter.rendering");

        // Generates the cache key - check
        /* TODO do we still need this check ?
        String generatedKey = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);
        if (!generatedKey.equals(key)) {
            logger.warn("Key generation does not give the same result after execution , was" + key + " , now is " + generatedKey);
        } */

        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        // If this content has been served from cache, no need to cache it again
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null || !servedFromCache.contains(finalKey)) {
            Properties fragmentProperties = cacheProvider.getKeyGenerator().getAttributesForKey(renderContext, resource);
            logger.debug("Caching content {} , key = {}", resource.getPath(), finalKey);

            // Check if the fragment is still cacheable, based on the key and cache properties
            boolean cacheable = isCacheable(renderContext, key, resource, fragmentProperties);
            boolean debugEnabled = logger.isDebugEnabled();

            if (cacheable) {
                if (!bypassDependencies) {
                    // Add self path as dependency for this fragment (for cache flush - will not impact the key)
                    resource.getDependencies().add(resource.getNode().getCanonicalPath());

                    // Add main resource if cache.mainResource is set
                    if ("true".equals(fragmentProperties.getProperty("cache.mainResource"))) {
                        resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
                    }
                }

                if (debugEnabled) {
                    logger.debug("Caching content for final key : {}", finalKey);
                }
                // if cacheFilter.fragmentExpiration not specify it's mean we are on the template fragment, first fragment of the page
                doCache(previousOut, renderContext, resource, Long.parseLong(fragmentProperties.getProperty(CACHE_EXPIRATION)), cache, finalKey, bypassDependencies);
            } else {
                cacheProvider.setFragmentKeyAsNotCacheable(key);
            }
        }

        // Append debug information
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, previousOut);
        }

        return previousOut;
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {
        // TODO reimplemt latch
//        releaseLatch(resource);
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.getContentForError(renderContext, resource, chain, e);
        if (cascadeFragmentErrors || Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration())) {
            return null;
        }
        try {
            renderContext.getRequest().setAttribute("expiration", Integer.toString(errorCacheExpiration));
            logger.error(e.getMessage(), e);

            final String key = cacheProvider.getKeyGenerator().generate(resource, renderContext,
                    cacheProvider.getKeyGenerator().getAttributesForKey(renderContext, resource));
            final String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

            Element element = null;
            final Cache cache = cacheProvider.getCache();
            try {
                element = cache.get(finalKey);
            } catch (LockTimeoutException ex) {
                logger.warn("Error while rendering " + renderContext.getMainResource() + ex.getMessage(), ex);
            }

            if (element != null && element.getObjectValue() != null) {
                return returnFromCache(renderContext, resource, key, finalKey, element, cache);
            }

            // Returns a fragment with an error comment
            return execute("<!-- Module error : " + HtmlUtils.htmlEscape(e.getMessage()) + "-->", renderContext, resource, chain, true);
        } catch (Exception e1) {
            return null;
        }
//        LinkedList<String> userKeysLinkedList = userKeys.get();
//        if (userKeysLinkedList != null && userKeysLinkedList.size() > 0) {
//            String finalKey = userKeysLinkedList.get(0);
//
//            final Cache cache = cacheProvider.getCache();
//            CacheEntry<String> entry = createCacheEntry("##moduleerrorcache##", renderContext, resource, finalKey);
//            Element cachedElement = new Element(finalKey, entry);
//            cachedElement.setTimeToLive(5);
//            cache.put(cachedElement);
//        }
    }

    /*
     * Create the fragment and store it into the cache.
     *
     * @param previousOut
     * @param renderContext
     * @param resource
     * @param properties
     * @param cache
     * @param key
     * @param finalKey
     * @throws RepositoryException
     * @throws ParseException

    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Properties properties,
                           Cache cache, String key, String finalKey) throws RepositoryException, ParseException {
        doCache(previousOut, renderContext, resource, properties, cache, key, finalKey, false);
    }
     */

    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Long expiration,
                           Cache cache, String finalKey, boolean bypassDependencies) throws RepositoryException, ParseException {
        Set<String> depNodeWrappers = Collections.emptySet();

        // Create the fragment entry based on the rendered content
        CacheEntry<String> cacheEntry = new CacheEntry<>(previousOut);

        // Store some properties that may have been set during fragment execution (todo : handle this another way)
        addPropertiesToCacheEntry(resource, cacheEntry, renderContext);

        Element cachedElement = new Element(finalKey, cacheEntry);

        if (expiration > 0) {
            addExpirationToCacheElements(cache, finalKey, expiration, cachedElement);
        }
        if (!bypassDependencies) {
            storeDependencies(renderContext, resource, finalKey, depNodeWrappers);
        }
        cache.put(cachedElement);

        if (logger.isDebugEnabled()) {
            logger.debug("Store in cache content of node with key: {}", finalKey);
            StringBuilder stringBuilder = new StringBuilder();
            for (String path : depNodeWrappers) {
                stringBuilder.append(path).append("\n");
            }
            logger.debug("Dependencies of {}:\n", finalKey, stringBuilder.toString());
        }
    }

    /**
     * Add the specified expiration time to the cache entry
     *
     * @param cache
     * @param finalKey
     * @param expiration
     * @param cachedElement
     * @throws ParseException
     */
    private void addExpirationToCacheElements(Cache cache, String finalKey, Long expiration, Element cachedElement)
            throws ParseException {
        cachedElement.setTimeToLive(expiration.intValue());
    }

    /**
     * Store the dependencies in the dependency cache. For each dependency, an entry in the dependencies cache is
     * created, containing the list of keys which depends on it. The current fragment key is added to that list.
     *
     * @param renderContext
     * @param resource        The current resource
     * @param finalKey
     * @param depNodeWrappers The list of dependencies
     * @return
     */
    private void storeDependencies(RenderContext renderContext, Resource resource, String finalKey,
                                   Set<String> depNodeWrappers) {
        if (useDependencies()) {
            final Cache dependenciesCache = cacheProvider.getDependenciesCache();
            depNodeWrappers = resource.getDependencies();
            for (String path : depNodeWrappers) {
                if(!path.startsWith("/modules")) {
                    Element element1 = dependenciesCache.get(path);
                    Set<String> dependencies = element1 != null ? (Set<String>) element1.getObjectValue() : Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                    if (!dependencies.contains(ALL)) {
                        if ((dependencies.size() + 1) > dependenciesLimit) {
                            Element element = new Element(path, ALL_SET);
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
     *
     * @param renderContext
     * @param finalKey
     * @param cache
     * @param value
     * @param newDependencies
     */
    protected void addDependencies(RenderContext renderContext, String finalKey, Cache cache, String value, Set<String> newDependencies) {
        if (newDependencies.add(finalKey)) {
            cache.put(new Element(value, newDependencies));
        }
    }

    /**
     * Store some properties that may have been set during fragment execution
     *
     * @param resource
     * @param cacheEntry
     * @param renderContext
     * @throws RepositoryException
     */
    private void addPropertiesToCacheEntry(Resource resource, CacheEntry<String> cacheEntry,
                                           RenderContext renderContext) throws RepositoryException {
        Map<String,Object> m = (Map<String, Object>) renderContext.getRequest().getAttribute("moduleMap");
        if (m != null && m.containsKey("requestAttributesToCache")){
            HashMap<String,Serializable> attributes = new HashMap<>();
            Collection<String> requestAttributesToCache = (Collection<String>) m.get("requestAttributesToCache");
            for (String attributesToCache : requestAttributesToCache) {
                if (renderContext.getRequest().getAttribute(attributesToCache) instanceof Serializable) {
                    attributes.put(attributesToCache, (Serializable) renderContext.getRequest().getAttribute(attributesToCache));
                }
            }
            cacheEntry.setProperty("requestAttributes", attributes);
        }
    }

    private void releaseLatch(Resource resource) {
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null && userKeysLinkedList.size() > 0) {

            String finalKey = userKeysLinkedList.remove(0);
            if (finalKey.equals(acquiredSemaphore.get())) {
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }

            Set<CountDownLatch> latches = processingLatches.get();
            Map<String, CountDownLatch> countDownLatchMap = generatorQueue.getGeneratingModules();
            CountDownLatch latch = countDownLatchMap.get(finalKey);
            if (latches != null && latches.contains(latch)) {
                latch.countDown();
                synchronized (countDownLatchMap) {
                    latches.remove(countDownLatchMap.remove(finalKey));
                }
            }
        }
    }

    /**
     * Replace all placeholders in the cache key to get a final key.
     *
     * @param renderContext RenderContext
     * @param key           Key with placeholders
     * @return The final key with placeholders replaced
     */
    protected String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        return cacheProvider.getKeyGenerator().replacePlaceholdersInCacheKey(renderContext, key);
    }

    /**
     * Is the current fragment cacheable or not. Based on the notCacheableFragment list and the ec or v parameter.
     *
     * @param renderContext render context
     * @param resource      current resource
     * @param key           calculated cache key
     * @param properties    fragment properties
     * @return true if fragments is cacheable, false if not
     * @throws RepositoryException
     */
    protected boolean isCacheable(RenderContext renderContext, String key, Resource resource, Properties properties) throws RepositoryException {
        // first check if the key is not part of the non cacheable fragments
        if (cacheProvider.isNotCacheableFragment(key)) {
            return false;
        }

        // check v parameter
        if (renderContext.getRequest().getParameter(V) != null && renderContext.isLoggedIn()) {
            return false;
        }

        // check ec parameter
        final String ecParameter = renderContext.getRequest().getParameter(EC);
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
        final String cacheExpiration = properties.getProperty(CACHE_EXPIRATION);
        Long expiration = cacheExpiration != null ? Long.parseLong(cacheExpiration) : -1;
        return expiration != 0L;
    }

    /**
     * This method is called if the entry is found in the cache. It will get the fragment entry, and regenerates the
     * full content by aggregating all included fragments.
     *
     * @param renderContext The render context
     * @param resource      The resource being rendered
     * @param key           The key with placeholders
     * @param finalKey      The final key with placeholders replaced
     * @param element       The cached element
     * @param cache         The cache
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String returnFromCache(RenderContext renderContext, Resource resource,
                                     String key, String finalKey, Element element,
                                     Cache cache) throws RenderException {
        if (logger.isDebugEnabled()) {
            logger.debug("Content retrieved from cache for node with key: {}", finalKey);
        }
        CacheEntry<?> cacheEntry = (CacheEntry<?>) element.getObjectValue();
        String cachedContent = (String) cacheEntry.getObject();

        // Add this key to the list of fragments already served from the cache to avoid to re-store it.
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }
        servedFromCache.add(finalKey);

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
        String key1 = replacePlaceholdersInCacheKey(renderContext, key);
        if (!cacheProvider.isNotCacheableFragment(key)) {
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

    public ModuleCacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public boolean isCascadeFragmentErrors() {
        return cascadeFragmentErrors;
    }

    public void setCascadeFragmentErrors(boolean cascadeFragmentErrors) {
        this.cascadeFragmentErrors = cascadeFragmentErrors;
    }

    public int getErrorCacheExpiration() {
        return errorCacheExpiration;
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
