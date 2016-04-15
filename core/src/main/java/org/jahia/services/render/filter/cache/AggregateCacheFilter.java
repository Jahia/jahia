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
import org.jahia.api.Constants;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.web.util.HtmlUtils;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Module content caching filter.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter implements ApplicationListener<TemplatePackageRedeployedEvent> {
    protected transient static final Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateCacheFilter.class);

    public static final String CACHE_PER_USER = "cache.perUser";
    public static final String PER_USER = "j:perUser";
    public static final String CACHE_EXPIRATION = "cache.expiration";
    public static final String ALL = "ALL";
    public static final Set<String> ALL_SET = Collections.singleton(ALL);

    private static final String CACHE_TAG_START_1_NOSRC = "<!-- cache:include";
    private static final String CACHE_TAG_START_1 = CACHE_TAG_START_1_NOSRC + " src=\"";
    private static final String CACHE_TAG_START_2 = "\" -->\n";
    private static final String CACHE_TAG_END = "\n<!-- /cache:include -->";
    private static final String CACHE_ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String CACHE_ESI_TAG_END = "\"></jahia_esi:include>";
    private static final int CACHE_ESI_TAG_END_LENGTH = CACHE_ESI_TAG_END.length();
    private static final int CACHE_TAG_LENGTH = CACHE_TAG_START_1.length() + CACHE_TAG_START_2.length() + CACHE_TAG_END.length();

    private static final String V = "v";
    private static final String EC = "ec";

    public static final TextUtils.ReplacementGenerator GENERATOR = new TextUtils.ReplacementGenerator() {
        @Override
        public void appendReplacementForMatch(int matchStart, int matchEnd, char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix) {
            // expects match to start with: src="<what we want to extract>"
            int firstQuoteIndex = matchStart;
            while (initialStringAsCharArray[firstQuoteIndex++] != '"') ;

            int secondQuoteIndex = firstQuoteIndex + 1;
            while (initialStringAsCharArray[secondQuoteIndex++] != '"') ;

            builder.append(CACHE_ESI_TAG_START)
                    .append(initialStringAsCharArray, firstQuoteIndex, secondQuoteIndex - firstQuoteIndex - 1)
                    .append(CACHE_ESI_TAG_END);
        }
    };
    protected ModuleCacheProvider cacheProvider;
    protected ModuleGeneratorQueue generatorQueue;

    protected static final Pattern CLEANUP_REGEXP = Pattern.compile(CACHE_TAG_START_1 + "(.*)" + CACHE_TAG_START_2 + "|" + CACHE_TAG_END);

    // We use ConcurrentHashMap instead of Set since we absolutely need the thread safety of this implementation but we don't want reads to lock.
    // @todo when migrating to JDK 1.6 we can replacing this with Collections.newSetFromMap(Map m) calls.
    protected static final Map<String, Boolean> notCacheableFragment = new ConcurrentHashMap<String, Boolean>(512);
    static protected ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    static protected ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    static protected ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();
    protected static long lastThreadDumpTime = 0L;
    protected Byte[] threadDumpCheckLock = new Byte[0];
    protected int dependenciesLimit = 1000;
    protected boolean cascadeFragmentErrors = false;
    protected int errorCacheExpiration = 5;

    private Set<String> skipLatchForConfigurations;
    private Set<String> skipLatchForPaths;
    private Set<String> skipLatchForNodeTypes;

    public void setDependenciesLimit(int dependenciesLimit) {
        this.dependenciesLimit = dependenciesLimit;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    public void setCascadeFragmentErrors(boolean cascadeFragmentErrors) {
        this.cascadeFragmentErrors = cascadeFragmentErrors;
    }

    public void setErrorCacheExpiration(int errorCacheExpiration) {
        this.errorCacheExpiration = errorCacheExpiration;
    }

    public void setSkipLatchForConfigurations(Set<String> skipLatchForConfigurations) {
        this.skipLatchForConfigurations = skipLatchForConfigurations;
    }

    public void setSkipLatchForPaths(Set<String> skipLatchForPaths) {
        this.skipLatchForPaths = skipLatchForPaths;
    }

    public void setSkipLatchForNodeTypes(Set<String> skipLatchForNodeTypes) {
        this.skipLatchForNodeTypes = skipLatchForNodeTypes;
    }

    /**
     * The prepare method is the first entry point in the cache. Its purpose is to check if a content is inside the
     * cache, and returns cached content for the fragment matching the requested resource if it is found. If other
     * fragment (module tags) are embedded inside this fragment, it will get them from the cache or by calling the
     * render service and aggregate them.
     *
     * @param renderContext The render context
     * @param resource      The resource to render
     * @param chain         The render chain
     * @return The content (with sub content aggregated) if found in the cache, null otherwise.
     * @throws Exception
     */
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final boolean debugEnabled = logger.isDebugEnabled();
        // Generates the key of the requested fragment. The KeyGenerator will create a key based on the request
        // (resource and context) and the cache properties. The generated key will contains temporary placeholders
        // that will be replaced to have the final key.
        Properties properties = getAttributesForKey(renderContext, resource);
        final Map<String, Serializable> moduleParams = resource.getModuleParams();
        final Boolean forceGeneration = (Boolean) moduleParams.remove("cache.forceGeneration");

        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);

        // Store the cache key in module params for usage in execute
        moduleParams.put("cacheKey", key);

        if (debugEnabled) {
            logger.debug("Cache filter for key with placeholders : {}", key);
        }

        // If we force the generation, return null
        if (Boolean.TRUE.equals(forceGeneration)) {
            return null;
        }

        // First check if the key is in the list of non-cacheable keys. The cache can also be skipped by specifying the
        // ec parameter with the uuid of the current node.
        if (!isCacheable(renderContext, resource, key, properties)) {
            return null;
        }

        // Replace the placeholders to have the final key that is used in the cache.
        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        // Keeps a list of keys being generated to avoid infinite loops.
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<>();
            userKeys.set(userKeysLinkedList);
        }
        if (userKeysLinkedList.contains(finalKey)) {
            return null;
        }
        userKeysLinkedList.add(0, finalKey);


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
            // The element is not found in the cache with that key. Use CountLatch to avoid parallel processing of the
            // module - if somebody else is generating this fragment, wait for the entry to be generated and
            // return the content from the cache. Otherwise, return null to continue the render chain.
            // Note that the fragment MIGHT be in cache, but the key may not be correct - some parameters impacting the
            // key like dependencies can only be calculated when the fragment has been generated.
            CountDownLatch countDownLatch = avoidParallelProcessingOfSameModule(finalKey, renderContext.getRequest(), resource, properties);
            if (countDownLatch == null) {
                element = cache.get(finalKey);
                if (element != null && element.getObjectValue() != null) {
                    return returnFromCache(renderContext, resource, key, finalKey, element, cache);
                }
            } else {
                Set<CountDownLatch> latches = processingLatches.get();
                if (latches == null) {
                    latches = new HashSet<>();
                    processingLatches.set(latches);
                }
                latches.add(countDownLatch);
            }
            return null;
        }
    }

    /**
     * Is the current fragment cacheable or not. Based on the notCacheableFragment list and the ec or v parameter.
     *
     * @param renderContext render context
     * @param resource      current resource
     * @param key           calculated cache key
     * @param properties    cache properties
     * @return true if fragments is cacheable, false if not
     * @throws RepositoryException
     */
    protected boolean isCacheable(RenderContext renderContext, Resource resource, String key, Properties properties) throws RepositoryException {
        // first check if the key is not part of the non cacheable fragments
        if (notCacheableFragment.containsKey(key)) {
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
     * Sets whether dependencies should be stored per cache object for this filter, which is useful for dependent
     * flushes.
     *
     * @return true if filter uses dependencies, false if not
     */
    protected boolean useDependencies() {
        return true;
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

        // Calls aggregation on the fragment content
        cachedContent = aggregateContent(cache, cachedContent, renderContext,
                (String) cacheEntry.getProperty("areaResource"), new Stack<String>(),
                (Set<String>) cacheEntry.getProperty("allPaths"));

        if (renderContext.getMainResource() == resource) {
            cachedContent = removeCacheTags(cachedContent);
        }

        // Add this key to the list of fragments already served from the cache to avoid to re-store it.
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }
        servedFromCache.add(finalKey);

        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));

        if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, cachedContent, element);
        } else {
            return cachedContent;
        }
    }

    /**
     * If the entry was not found in the cache, the execute method is called once all normal render processing has been
     * done. The fragment key is computed again, taking all dependencies into account. The content is cleared from all
     * sub-content to get only the fragment content, which is finally stored in the cache.
     *
     * @param previousOut   Result from the previous filter
     * @param renderContext The render context
     * @param resource      The resource to render
     * @param chain         The render chain
     * @return
     * @throws Exception
     */
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        return execute(previousOut, renderContext, resource, chain, false);
    }

    private String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain, boolean bypassDependencies) throws Exception {
        Properties properties = getAttributesForKey(renderContext, resource);

        if (!bypassDependencies) {
            // Add self path as dependency for this fragment (for cache flush - will not impact the key)
            resource.getDependencies().add(resource.getNode().getCanonicalPath());

            // Add main resource if cache.mainResource is set
            if ("true".equals(properties.getProperty("cache.mainResource"))) {
                resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
            }
        }

        // Get the key generated in prepare
        String key = (String) resource.getModuleParams().remove("cacheKey");


        // Generates the cache key - check
        String generatedKey = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);
        if (!generatedKey.equals(key)) {
            logger.warn("Key generation does not give the same result after execution , was" + key + " , now is " + generatedKey);
        }

        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        // If this content has been served from cache, no need to cache it again
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache != null && servedFromCache.contains(finalKey)) {
            return previousOut;
        }

        // Check if the fragment is still cacheable, based on the key and cache properties
        boolean cacheable = isCacheable(renderContext, resource, key, properties);
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));

        if (cacheable) {
            final Cache cache = cacheProvider.getCache();
            if (debugEnabled) {
                logger.debug("Caching content for final key : {}", finalKey);
            }
            doCache(previousOut, renderContext, resource, properties, cache, key, finalKey, bypassDependencies);
        } else {
            notCacheableFragment.put(key, Boolean.TRUE);
        }

        // Append debug information
        if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, previousOut), null);
        }

        if (renderContext.getMainResource() == resource) {
            return removeCacheTags(previousOut);
        } else {
            return surroundWithCacheTag(key, previousOut);
        }
    }


    /**
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
     */
    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Properties properties,
                           Cache cache, String key, String finalKey) throws RepositoryException, ParseException {
        doCache(previousOut, renderContext, resource, properties, cache, key, finalKey, false);
    }

    protected void doCache(String previousOut, RenderContext renderContext, Resource resource, Properties properties,
                           Cache cache, String key, String finalKey, boolean bypassDependencies) throws RepositoryException, ParseException {
        Long expiration = Long.parseLong(properties.getProperty(CACHE_EXPIRATION));
        Set<String> depNodeWrappers = Collections.emptySet();

        // Create the fragment entry based on the rendered content
        CacheEntry<String> cacheEntry = createCacheEntry(previousOut, renderContext, resource, key);

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
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation. The
     * cache properties may come from the script properties file, or from the jmix:cache mixin (for cache.perUser
     * only).
     * <p/>
     * If the component is a list, the properties can also come from its hidden.load script properties.
     * <p/>
     * cache.perUser : is the cache entry specific for each user. Is set by j:perUser node property or cache.perUser
     * property in script properties
     * <p/>
     * cache.mainResource : is the cache entry dependant on the main resource. Is set by cache.mainResource property
     * in script properties, or automatically set if the component is bound.
     * <p/>
     * cache.requestParameters : list of request parameter that will impact the rendering of the resource. Is set
     * by cache.requestParameters property in script properties. ec,v,cacheinfo and moduleinfo are automatically added.
     * <p/>
     * cache.expiration : the expiration time of the cache entry. Can be set by the "expiration" request attribute,
     * j:expiration node property or the cache.expiration property in script properties.
     *
     * @param renderContext
     * @param resource
     * @return
     * @throws RepositoryException
     */
    protected Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        final JCRNodeWrapper node = resource.getNode();
        boolean isBound = node.isNodeType(Constants.JAHIAMIX_BOUND_COMPONENT);
        boolean isList = node.isNodeType(Constants.JAHIAMIX_LIST);

        Properties properties = new Properties();

        if (script != null) {
            properties.putAll(script.getView().getDefaultProperties());
            properties.putAll(script.getView().getProperties());
        }

        if (isList) {
            Resource listLoader = new Resource(node, resource.getTemplateType(), "hidden.load", Resource.CONFIGURATION_INCLUDE);
            try {
                Script s = service.resolveScript(listLoader, renderContext);
                properties.putAll(s.getView().getProperties());
            } catch (TemplateNotFoundException e) {
                logger.error("Cannot find loader script for list " + node.getPath(), e);
            }
        }

        if (node.hasProperty(PER_USER)) {
            properties.put(CACHE_PER_USER, node.getProperty(PER_USER).getString());
        }
        if (isBound) {
            properties.put("cache.mainResource", "true");
        }

        // update requestParameters if needed
        final StringBuilder updatedRequestParameters;
        final String requestParameters = properties.getProperty("cache.requestParameters");
        if (!StringUtils.isEmpty(requestParameters)) {
            updatedRequestParameters = new StringBuilder(requestParameters + ",ec,v");
        } else {
            updatedRequestParameters = new StringBuilder("ec,v");
        }
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            updatedRequestParameters.append(",cacheinfo,moduleinfo");
        }
        properties.put("cache.requestParameters", updatedRequestParameters.toString());

        // cache expiration lookup by order : request attribute -> node -> view -> -1 (forever in cache realm, 4 hours)
        String viewExpiration = properties.getProperty(CACHE_EXPIRATION);
        final Object requestExpiration = renderContext.getRequest().getAttribute("expiration");
        if (requestExpiration != null) {
            properties.put(CACHE_EXPIRATION, requestExpiration);
        } else if (node.hasProperty("j:expiration")) {
            properties.put(CACHE_EXPIRATION, node.getProperty("j:expiration").getString());
        } else if (viewExpiration != null) {
            properties.put(CACHE_EXPIRATION, viewExpiration);
        } else {
            properties.put(CACHE_EXPIRATION, "-1");
        }

        String propertiesScript = properties.getProperty("cache.propertiesScript");
        if (propertiesScript != null) {
            Resource props = new Resource(node, resource.getTemplateType(), propertiesScript, Resource.CONFIGURATION_INCLUDE);
            try {
                Script s = service.resolveScript(props, renderContext);
                try {
                    renderContext.getRequest().setAttribute("cacheProperties", properties);
                    s.execute(props, renderContext);
                } catch (RenderException e) {
                    logger.error("Cannot execute script",e);
                } finally {
                    renderContext.getRequest().removeAttribute("cacheProperties");
                }
            } catch (TemplateNotFoundException e) {
                logger.error(
                        "Cannot find cache properties script " + propertiesScript + " for the node " + node.getPath(),
                        e);
            }
        }

        return properties;
    }

    /**
     * Create the cache entry based on the rendered content. All sub-content (which should be surrounded by
     * &lt;!-- cache:include --&gt; tags) are removed and replaced by &lt;jahia_esi:include&gt; tags, keeping only the
     * cache key (without placeholder replacements).
     *
     * @param previousOut   The full rendered content, coming from the render chain
     * @param renderContext The render context
     * @param resource      The resource that is being rendered
     * @param key           The key of the fragment
     * @return An entry that can be stored in the cache
     * @throws RepositoryException
     */
    protected CacheEntry<String> createCacheEntry(String previousOut, RenderContext renderContext, Resource resource, String key) {
        String out = TextUtils.replaceBoundedString(previousOut, "<!-- cache:include", "<!-- /cache:include -->", GENERATOR);
        StringBuilder sb = new StringBuilder(out.length() + key.length() + CACHE_TAG_LENGTH);
        sb.append(out);
        // Finally, add the  <!-- cache:include --> around the content and create cache entry.
        surroundWithCacheTag(key, sb);
        return new CacheEntry<>(sb.toString());
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
        if (resource.getNode().isNodeType("jnt:area") || resource.getNode().isNodeType(
                "jnt:mainResourceDisplay")) {
            cacheEntry.setProperty("areaResource", resource.getNode().getIdentifier());
        }
        TreeSet<String> allPaths = new TreeSet<String>();
        allPaths.addAll(renderContext.getRenderedPaths());
        //Add current resource too as is as been removed by the TemplatesScriptFilter already
//        if (renderContext.getRequest().getAttribute("lastResourceRenderedByScript") != null && renderContext.getRequest().getAttribute("lastResourceRenderedByScript").equals(resource)) {
//            allPaths.add(resource.getNode().getPath());
//        }
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

        cacheEntry.setProperty("allPaths", allPaths);
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
     * Replace all placeholders in the cache key to get a final key.
     *
     * @param renderContext RenderContext
     * @param key           Key with placeholders
     * @return The final key with placeholders replaced
     */
    protected String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        return cacheProvider.getKeyGenerator().replacePlaceholdersInCacheKey(renderContext, key);
    }

    private int replaceInContent(StringBuilder sb, int start, int end, String replacement) {
        if (replacement == null) {
            replacement = "";
        }
        sb.replace(start, end, replacement);
        return sb.indexOf(CACHE_ESI_TAG_START, start + replacement.length());
    }

    /**
     * Aggregate the content that are inside the cached fragment to get a full HTML content with all sub modules
     * embedded.
     *
     * @param cache          The cache
     * @param cachedContent  The fragment, as it is stored in the cache
     * @param renderContext  The render context
     * @param areaIdentifier
     * @param cacheKeyStack
     * @param allPaths
     * @return
     */
    protected String aggregateContent(Cache cache, String cachedContent, RenderContext renderContext, String areaIdentifier,
                                      Stack<String> cacheKeyStack, Set<String> allPaths) throws RenderException {

        int esiTagStartIndex = cachedContent.indexOf(CACHE_ESI_TAG_START);
        if(esiTagStartIndex == -1){
            return cachedContent;
        } else {
            StringBuilder sb = new StringBuilder(cachedContent);
            while (esiTagStartIndex != -1){
                int esiTagEndIndex = sb.indexOf(CACHE_ESI_TAG_END, esiTagStartIndex);
                if (esiTagEndIndex != -1) {
                    String cacheKey = sb.substring(esiTagStartIndex + CACHE_ESI_TAG_START.length(), esiTagEndIndex);
                    String replacedCacheKey = replacePlaceholdersInCacheKey(renderContext, cacheKey);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Check if {} is in cache", replacedCacheKey);
                    }

                    boolean cacheable = true;
                    CacheKeyGenerator keyGenerator = cacheProvider.getKeyGenerator();
                    Map<String, String> keyAttrbs = keyGenerator.parse(cacheKey);
                    final String ecParameter = renderContext.getRequest().getParameter(EC);
                    if ((ecParameter != null && ecParameter.equals(keyAttrbs.get("resourceID"))) ||
                            (renderContext.getRequest().getParameter(V) != null && renderContext.isLoggedIn())) {
                        cacheable = false;
                    }

                    if (cacheable && cache.isKeyInCache(replacedCacheKey)) {
                        // If fragment is in cache, get it from there and aggregate recursively
                        final Element element = cache.get(replacedCacheKey);
                        if (element != null && element.getObjectValue() != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("It has been found in cache");
                            }
                            @SuppressWarnings("unchecked")
                            final CacheEntry<String> cacheEntry = (CacheEntry<String>) element.getObjectValue();
                            String content = cacheEntry.getObject();

                            // Avoid loops
                            if (cacheKeyStack.contains(cacheKey)) {
                                // replace by empty
                                esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH, StringUtils.EMPTY);
                                continue;
                            }
                            cacheKeyStack.push(cacheKey);

                            try {
                                if (!cachedContent.equals(content)) {
                                    try {
                                        if (cacheEntry.getProperty("requestAttributes") != null) {
                                            Map<String,Serializable> requestAttributesToCache = (Map<String, Serializable>) cacheEntry.getProperty("requestAttributes");
                                            for (Map.Entry<String, Serializable> entry : requestAttributesToCache.entrySet()) {
                                                renderContext.getRequest().setAttribute(entry.getKey(), entry.getValue());
                                            }
                                        }

                                        esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH,
                                                aggregateContent(cache, content, renderContext, (String) cacheEntry.getProperty("areaResource"), cacheKeyStack, (Set<String>) cacheEntry.getProperty("allPaths")));
                                    } catch (RenderException e) {
                                        throw new RuntimeException(e.getMessage(), e);
                                    }
                                } else {
                                    // TODO: need to investigate here, seem's that the if condition is always true.
                                    esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH, content);
                                }
                            } finally {
                                cacheKeyStack.pop();
                            }
                        } else {
                            cache.put(new Element(replacedCacheKey, null));
                            if (logger.isDebugEnabled()) {
                                logger.debug("Content is expired");
                            }
                            // The fragment is not in the cache, generate it
                            try {
                                esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH,
                                        generateContent(renderContext, cacheKey, areaIdentifier, allPaths));
                            } catch (RenderException e) {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Content is missing from cache");
                        }
                        // The fragment is not in the cache, generate it
                        try {
                            esiTagStartIndex = replaceInContent(sb, esiTagStartIndex, esiTagEndIndex + CACHE_ESI_TAG_END_LENGTH,
                                    generateContent(renderContext, cacheKey, areaIdentifier, allPaths));
                        } catch (RenderException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                } else {
                    // no closed esi end tag found
                    return sb.toString();
                }
            }
            return sb.toString();
        }
    }

    /**
     * Generates content for a sub fragment.
     *
     * @param renderContext  The render context
     * @param cacheKey       The cache key of the fragment to generate
     * @param areaIdentifier
     * @param allPaths
     */
    protected String generateContent(RenderContext renderContext,
                                     String cacheKey, String areaIdentifier,
                                     Set<String> allPaths) throws RenderException {
        final CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
        try {
            // Parse the key to get all separate key attributes like node path and template
            Map<String, String> keyAttrbs = cacheKeyGenerator.parse(cacheKey);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getWorkspace(), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            JCRNodeWrapper node = null;
            try {
                // Get the node associated to the fragment to generate
                node = currentUserSession.getNode(StringUtils.replace(keyAttrbs.get("path"), PathCacheKeyPartGenerator.MAIN_RESOURCE_KEY, StringUtils.EMPTY));
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node {} is no longer available." + " Replacing output with empty content.",
                            keyAttrbs.get("path"));
                }
                // Node is not found, return empty content
                return StringUtils.EMPTY;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Calling render service for generating content for key " + cacheKey + " with attributes : " +
                        " areaIdentifier " + areaIdentifier);
            }

            // Prepare to dispatch to the render service - restore all area/templates atributes
            renderContext.getRequest().removeAttribute(
                    "areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));
            Template oldOne = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            String context = keyAttrbs.get("context");
            if (!context.equals("page")) {
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
            }
            if (!StringUtils.isEmpty(keyAttrbs.get("templateNodes"))) {
                Template templateNodes = new Template(keyAttrbs.get("templateNodes"));
                renderContext.getRequest().setAttribute("previousTemplate", templateNodes);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }

            Set<String> addedPath = new HashSet<String>();
            if (null != allPaths && !allPaths.isEmpty()) {
                for (String path : allPaths) {
                    if (!renderContext.getRenderedPaths().contains(path)) {
                        renderContext.getRenderedPaths().add(path);
                        addedPath.add(path);
                    }
                }
            }

            renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            Object oldInArea = (Object) renderContext.getRequest().getAttribute("inArea");
            String inArea = keyAttrbs.get("inArea");
            if (StringUtils.isEmpty(inArea)) {
                renderContext.getRequest().removeAttribute("inArea");
            } else {
                renderContext.getRequest().setAttribute("inArea", Boolean.valueOf(inArea));
            }
            if (areaIdentifier != null) {
                renderContext.getRequest().setAttribute("areaListResource", currentUserSession.getNodeByIdentifier(areaIdentifier));
            }
            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), context);

            String params = keyAttrbs.get("moduleParams");
            if (StringUtils.isNotEmpty(params)) {
                try {
                    JSONObject map = new JSONObject(keyAttrbs.get("moduleParams"));
                    Iterator keys = map.keys();
                    while (keys.hasNext()) {
                        String next = (String) keys.next();
                        resource.getModuleParams().put(next, (Serializable) map.get(next));
                    }
                } catch (JSONException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // Fragment with full final key is not in the cache, set cache.forceGeneration parameter to avoid returning
            // a cache entry based on incomplete dependencies.
            resource.getModuleParams().put("cache.forceGeneration", true);

            // Dispatch to the render service to generate the content
            String content = RenderService.getInstance().render(resource, renderContext);
            if (StringUtils.isBlank(content) && renderContext.getRedirect() == null) {
                logger.error("Empty generated content for key " + cacheKey + " with attributes : " +
                        " areaIdentifier " + areaIdentifier);
            }

            for (String s : addedPath) {
                renderContext.getRenderedPaths().remove(s);
            }

            if (oldInArea != null) {
                renderContext.getRequest().setAttribute("inArea", oldInArea);
            } else {
                renderContext.getRequest().removeAttribute("inArea");
            }
            if (oldOne != null) {
                renderContext.getRequest().setAttribute("previousTemplate", oldOne);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }
            return content;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Add &lt;!-- cache include --&gt; tags around the content, with the cache key as an attribute.
     *
     * @param key    The key of the fragment
     * @param output The content
     * @return The content surrounded by the tag
     */
    protected String surroundWithCacheTag(String key, String output) {
        StringBuilder builder = new StringBuilder(output.length() + key.length() + CACHE_TAG_LENGTH);
        builder.append(output);
        surroundWithCacheTag(key, builder);
        return builder.toString();
    }

    protected void surroundWithCacheTag(String key, StringBuilder output) {
        output.insert(0, CACHE_TAG_START_2).insert(0, key).insert(0, CACHE_TAG_START_1).append(CACHE_TAG_END);
    }

    protected String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                            Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Expiration: </span><span>");
        String key1 = replacePlaceholdersInCacheKey(renderContext, key);
        if (!notCacheableFragment.containsKey(key)) {
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

    /**
     * @deprecated
     */
    public static String removeEsiTags(String content) {
        return removeCacheTags(content);
    }

    /**
     * Remove the surrounding cache:include tags - only used for the main resource, as the content won't be included
     * into another fragment but returned to the user
     *
     * @param content
     * @return
     */
    public static String removeCacheTags(String content) {
        if (StringUtils.isNotEmpty(content)) {
            return CLEANUP_REGEXP.matcher(content).replaceAll(StringUtils.EMPTY);
        } else {
            return content;
        }
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

            final Properties properties = getAttributesForKey(renderContext, resource);
            final String key = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);
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

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {
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

    protected CountDownLatch avoidParallelProcessingOfSameModule(String key, HttpServletRequest request,
                                                                 Resource resource, Properties properties) throws Exception {
        CountDownLatch latch = null;
        boolean mustWait = true;

        Map<String, CountDownLatch> generatingModules = generatorQueue.getGeneratingModules();
        if (generatingModules.get(key) == null && acquiredSemaphore.get() == null) {
            if (!generatorQueue.getAvailableProcessings().tryAcquire(generatorQueue.getModuleGenerationWaitTime(),
                    TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processing reached (" +
                        generatorQueue.getMaxModulesToGenerateInParallel() + ") - " + key + " - " +
                        request.getRequestURI());
            } else {
                acquiredSemaphore.set(key);
            }
        }
        if (shouldUseLatch(resource, properties)) {
            synchronized (generatingModules) {
                latch = generatingModules.get(key);
                if (latch == null) {
                    latch = new CountDownLatch(1);
                    generatingModules.put(key, latch);
                    mustWait = false;
                }
            }
        } else {
            mustWait = false;
        }
        if (mustWait) {
            if (acquiredSemaphore.get() != null) {
                // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }
            try {
                if (!latch.await(generatorQueue.getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    throw new Exception("Module generation takes too long due to module not generated fast enough (>" +
                            generatorQueue.getModuleGenerationWaitTime() + " ms)- " + key + " - " +
                            request.getRequestURI());
                }
                latch = null;
            } catch (InterruptedException ie) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The waiting thread has been interrupted :", ie);
                }
                throw new Exception(ie);
            }
        }
        return latch;
    }

    private boolean shouldUseLatch(Resource resource, Properties properties) throws RepositoryException {
        if (!StringUtils.isEmpty(properties.getProperty("cache.latch"))) {
            return Boolean.valueOf(properties.getProperty("cache.latch"));
        }
        if (skipLatchForConfigurations != null && skipLatchForConfigurations.contains(resource.getContextConfiguration())) {
            return false;
        }
        if (skipLatchForPaths != null) {
            for (String skipLatchForPath : skipLatchForPaths) {
                if (skipLatchForPath.contains("$currentSite")) {
                    skipLatchForPath = skipLatchForPath.replace("$currentSite", resource.getNode().getResolveSite().getPath());
                }
                if (resource.getNode().getPath().startsWith(skipLatchForPath)) {
                    return false;
                }
            }
        }
        if (skipLatchForNodeTypes != null) {
            for (String nodeType : skipLatchForNodeTypes) {
                if (resource.getNode().isNodeType(nodeType)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void manageThreadDump() {
        boolean createDump = false;
        long minInterval = generatorQueue.getMinimumIntervalAfterLastAutoThreadDump();
        if (minInterval > -1 && (generatorQueue.isThreadDumpToSystemOut() || generatorQueue.isThreadDumpToFile())) {
            long now = System.currentTimeMillis();
            synchronized (threadDumpCheckLock) {
                if (now > (lastThreadDumpTime + minInterval)) {
                    createDump = true;
                    lastThreadDumpTime = now;
                }
            }
        }
        if (createDump) {
            ThreadMonitor tm = ThreadMonitor.getInstance();
            tm.dumpThreadInfo(generatorQueue.isThreadDumpToSystemOut(), generatorQueue.isThreadDumpToFile());
            tm = null;
        }
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        if(!this.isDisabled()) {
            flushNotCacheableFragment();
        }
    }

    /**
     * @deprecated As of release 7.2, replaced by {@link org.jahia.services.render.filter.cache.ModuleCacheProvider#removeNotCacheableFragment(String)}
     * @param key
     */
    public void removeNotCacheableFragment(String key) {
        CacheKeyGenerator keyGenerator = cacheProvider.getKeyGenerator();
        if (keyGenerator instanceof DefaultCacheKeyGenerator) {
            DefaultCacheKeyGenerator defaultCacheKeyGenerator = (DefaultCacheKeyGenerator) keyGenerator;
            Map<String, String> keyAttrbs = defaultCacheKeyGenerator.parse(key);
            String path = keyAttrbs.get("path");
            List<String> removableKeys = new ArrayList<String>();
            for (String notCacheableKey : notCacheableFragment.keySet()) {
                if (notCacheableKey.contains(path)) {
                    removableKeys.add(notCacheableKey);
                }
            }
            for (String removableKey : removableKeys) {
                notCacheableFragment.remove(removableKey);
            }
        }
    }

    /**
     * @deprecated As of release 7.2, replaced by {@link org.jahia.services.render.filter.cache.ModuleCacheProvider#flushNotCacheableFragment()}
     */
    public static void flushNotCacheableFragment() {
        notCacheableFragment.clear();
    }
}
