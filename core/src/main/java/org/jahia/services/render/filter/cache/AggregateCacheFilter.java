/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import net.htmlparser.jericho.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.*;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

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
 *        Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent>, InitializingBean {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateCacheFilter.class);
    protected ModuleCacheProvider cacheProvider;
    protected ModuleGeneratorQueue generatorQueue;

    public static final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->");
    public static final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");
    protected static final Pattern CLEANUP_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->\n|\n<!-- /cache:include -->");

    // We use ConcurrentHashMap instead of Set since we absolutely need the thread safety of this implementation but we don't want reads to lock.
    // @todo when migrating to JDK 1.6 we can replacing this with Collections.newSetFromMap(Map m) calls.
    protected static final Map<String,Boolean> notCacheableFragment = new ConcurrentHashMap<String,Boolean>(512);
    static protected ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    static protected ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    static protected ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();
    protected static long lastThreadDumpTime = 0L;
    protected Byte[] threadDumpCheckLock = new Byte[0];
    protected Map<String, String> moduleParamsProperties;
    protected int dependenciesLimit = 1000;

    protected int errorCacheExpiration = 5;


    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        Config.LoggerProvider = LoggerProvider.DISABLED;
    }

    public void setDependenciesLimit(int dependenciesLimit) {
        this.dependenciesLimit = dependenciesLimit;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    public void setModuleParamsProperties(Map<String, String> moduleParamsProperties) {
        this.moduleParamsProperties = moduleParamsProperties;
    }

    public void setErrorCacheExpiration(int errorCacheExpiration) {
        this.errorCacheExpiration = errorCacheExpiration;
    }

    /**
     * The prepare method is the first entry point in the cache. Its purpose is to check if a content is inside the
     * cache, and returns cached content for the fragment matching the requested resource if it is found. If other
     * fragment (module tags) are embedded inside this fragment, it will get them from the cache or by calling the
     * render service and aggregate them.
     *
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return The content (with sub content aggregated) if found in the cache, null otherwise.
     * @throws Exception
     */
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        boolean debugEnabled = logger.isDebugEnabled();
        @SuppressWarnings("unchecked")

        // Generates the key of the requested fragment. The KeyGenerator will create a key based on the request
        // (resource and context) and the cache properties. The generated key will contains temporary placeholders
        // that will be replaced to have the final key.
        Properties properties = getAttributesForKey(renderContext, resource);
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);

        if (debugEnabled) {
            logger.debug("Cache filter for key with placeholders : {}", key);
        }

        // First check if the key is in the list of non-cacheable keys. The cache can also be skipped by specifying the
        // ec parameter with the uuid of the current node.
        boolean cacheable = isCacheable(renderContext, resource, key, properties, true);
        if(!cacheable) {
            return null;
        }

        // Replace the placeholders to have the final key that is used in the cache.
        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        // Keeps a list of keys being generated to avoid infinite loops.
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<String>();
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

        if (element != null && element.getValue() != null) {
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
                if (element != null && element.getValue() != null) {
                    return returnFromCache(renderContext, resource, key, finalKey, element, cache);
                }
            } else {
                Set<CountDownLatch> latches = processingLatches.get();
                if (latches == null) {
                    latches = new HashSet<CountDownLatch>();
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
     * @param resource current resource
     * @param key calculated cache key
     * @param properties cache properties
     * @param isInPrepare true if we are in filter prepare mode, and false if we are in filter execute mode
     * @return true if fragments is cacheable, false if not
     * @throws RepositoryException
     */
    protected boolean isCacheable(RenderContext renderContext, Resource resource, String key, Properties properties, boolean isInPrepare) throws RepositoryException {
        boolean cacheable = !notCacheableFragment.containsKey(key);
        if (renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v") != null) {
            cacheable = false;
        }
        if (renderContext.getRequest().getParameter("ec") != null && renderContext.getRequest().getParameter(
                "ec").equals(resource.getNode().getIdentifier())) {
            cacheable = false;
        }
        Long expiration = properties.getProperty("cache.expiration") != null ? Long.parseLong(properties.getProperty("cache.expiration")) : -1;
        if(expiration==0L) {
            cacheable = false;
        }
        return cacheable;
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
     * @param resource The resource being rendered
     * @param key The key with placeholders
     * @param finalKey The final key with placeholders replaced
     * @param element The cached element
     * @param cache The cache
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String returnFromCache(RenderContext renderContext, Resource resource,
                                     String key, String finalKey, Element element,
                                     Cache cache) {
        if (logger.isDebugEnabled()) {
            logger.debug("Content retrieved from cache for node with key: {}", finalKey);
        }
        CacheEntry<?> cacheEntry = (CacheEntry<?>) element.getValue();
        String cachedContent = (String) cacheEntry.getObject();

        // Calls aggregation on the fragment content
        cachedContent = aggregateContent(cache, cachedContent, renderContext,
                (Map<String, Serializable>) cacheEntry.getProperty("moduleParams"), (String) cacheEntry.getProperty("areaResource"), new Stack<String>(),
                (Set<String>) cacheEntry.getProperty("allPaths"));

        if (renderContext.getMainResource() == resource) {
            cachedContent = removeCacheTags(cachedContent);
        }

        // Add this key to the list of fragments already served from the cache to avoid to re-store it.
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<String>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }
        servedFromCache.add(key);

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
     * @param previousOut Result from the previous filter
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return
     * @throws Exception
     */
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {

        Properties properties = getAttributesForKey(renderContext, resource);

        // Add self path as dependency for this fragment (for cache flush - will not impact the key)
        resource.getDependencies().add(resource.getNode().getCanonicalPath());

        // Add main resource if cache.mainResource is set
        if ("true".equals(properties.getProperty("cache.mainResource"))) {
            resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
            // todo: may be done directly in navmenu .. ?
            if (Boolean.valueOf(properties.getProperty("cache.mainResource.flushParent", "false"))) {

            }
        }

        // Generates the cache key
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext, properties);

        // If this content has been served from cache, no need to cache it again
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache != null && servedFromCache.contains(key)) {
            return previousOut;
        }

        // Check if the fragment is still cacheable, based on the key and cache properties
        boolean cacheable = isCacheable(renderContext, resource, key, properties, false);
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));

        String finalKey = replacePlaceholdersInCacheKey(renderContext, key);

        try {
            if (cacheable) {
                final Cache cache = cacheProvider.getCache();
                if (debugEnabled) {
                    logger.debug("Caching content for final key : {}", finalKey);
                }
                doCache(previousOut, renderContext, resource, properties, cache, key, finalKey);
            }
            else {
                notCacheableFragment.put(key,Boolean.TRUE);
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
        } catch (Exception e) {
            throw e;
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
        Long expiration = Long.parseLong(properties.getProperty("cache.expiration"));
        Set<String> depNodeWrappers = Collections.emptySet();

        // Create the fragment entry based on the rendered content
        CacheEntry<String> cacheEntry = createCacheEntry(previousOut, renderContext, resource, key);

        // Store some properties that may have been set during fragment execution (todo : handle this another way)
        addPropertiesToCacheEntry(resource, cacheEntry, renderContext);

        Element cachedElement = new Element(finalKey, cacheEntry);

        if (expiration > 0) {
            addExpirationToCacheElements(cache, finalKey, expiration, cachedElement);
        }
        storeDependencies(renderContext, resource, finalKey, depNodeWrappers);
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
     * @param resource The current resource
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
                Element element1 = dependenciesCache.get(path);
                Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String>emptySet();
                if (!dependencies.contains("ALL")) {
                    Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                    newDependencies.addAll(dependencies);
                    if ((newDependencies.size() + 1) > dependenciesLimit) {
                        newDependencies.clear();
                        newDependencies.add("ALL");
                        dependenciesCache.put(new Element(path, newDependencies));
                    } else {
                        addDependencies(renderContext, finalKey, dependenciesCache, path, newDependencies);
                    }
                }
            }
            final Cache regexpDependenciesCache = cacheProvider.getRegexpDependenciesCache();
            Set<String> regexpDepNodeWrappers = resource.getRegexpDependencies();
            for (String regexp : regexpDepNodeWrappers) {
                Element element1 = regexpDependenciesCache.get(regexp);
                Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String>emptySet();
                Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                newDependencies.addAll(dependencies);
                addDependencies(renderContext, finalKey, regexpDependenciesCache, regexp, newDependencies);
            }
        }
        resource.getDependencies().clear();
        resource.getRegexpDependencies().clear();
    }

    /**
     * Get all cache attributes that need to be applied on this fragment and that will impact key generation. The
     * cache properties may come from the script properties file, or from the jmix:cache mixin (for the cache.perUser
     * only).
     *
     * cache.perUser : is the cache entry specific for each user. Is set by j:perUser node property or cache.perUser
     * property in script properties
     *
     * cache.mainResource : is the cache entry dependant on the main resource. Is set by cache.mainResource property
     * in script properties, or automatically set if the component is bound.
     *
     * cache.requestParameters : list of request parameter that will impact the rendering of the resource. Is set
     * by cache.requestParameters property in script properties. ec,v,cacheinfo and moduleinfo are automatically added.
     *
     * cache.expiration : the expiration time of the cache entry. Can be set by the "expiration" request attribute,
     * j:expiration node property or the cache.expiration property in script properties.
     *
     *
     * @param renderContext
     * @param resource
     * @return
     * @throws RepositoryException
     */
    protected Properties getAttributesForKey(RenderContext renderContext, Resource resource) throws RepositoryException {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        boolean isBound = resource.getNode().isNodeType("jmix:bindedComponent");

        Properties properties = new Properties();

        if (script != null) {
            properties.putAll(script.getView().getDefaultProperties());
            properties.putAll(script.getView().getProperties());
        }

        if (resource.getNode().hasProperty("j:perUser")) {
            properties.put("cache.perUser",resource.getNode().getProperty("j:perUser").getString());
        }
        if (isBound) {
            properties.put("cache.mainResource", "true");
        }
        String requestParameters = properties.getProperty("cache.requestParameters");

        StringBuilder stringBuilder = new StringBuilder(requestParameters != null ? requestParameters : "");
        if (stringBuilder.length() == 0) {
            stringBuilder.append("ec,v");
        } else {
            stringBuilder.append(",ec,v");
        }
        if (SettingsBean.getInstance().isDevelopmentMode()) {
            stringBuilder.append(",cacheinfo,moduleinfo");
        }
        requestParameters = stringBuilder.toString();
        properties.put("cache.requestParameters", requestParameters);

        if (renderContext.getRequest().getAttribute("expiration") != null) {
            properties.put("cache.expiration", renderContext.getRequest().getAttribute("expiration"));
        } else if (resource.getNode().hasProperty("j:expiration")) {
            properties.put("cache.expiration", resource.getNode().getProperty("j:expiration").getString());
        } else {
            properties.put("cache.expiration", "-1");
        }
        return properties;
    }

    /**
     * Create the cache entry based on the rendered content. All sub-content (which should be surrounded by
     * &lt;!-- cache:include --&gt; tags) are removed and replaced by &lt;esi:include&gt; tags, keeping only the
     * cache key (without placeholder replacements).
     *
     * @param previousOut The full rendered content, coming from the render chain
     * @param renderContext The render context
     * @param resource The resource that is being rendered
     * @param key The key of the fragment
     * @return An entry that can be stored in the cache
     * @throws RepositoryException
     */
    protected CacheEntry<String> createCacheEntry(String previousOut, RenderContext renderContext, Resource resource, String key) {
        // Replace <!-- cache:include --> tags of sub fragments by HTML tags that can be parsed by jericho
        String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(previousOut).replaceAll("</esi:include>");
        cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll("<esi:include src=\"$1\">");

        Source source = new Source(cachedRenderContent);

        //// This will remove all blank line and drastically reduce data in memory
        // source = new Source((new SourceFormatter(source)).toString());

        // We will remove module:tag content here has we do not want to store them twice in memory
        List<StartTag> esiIncludeTags = source.getAllStartTags("esi:include");
        OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);

        // Finally, add the  <!-- cache:include --> around the content and create cache entry.
        String output = outputDocument.toString();
        cachedRenderContent = surroundWithCacheTag(key, output);
        CacheEntry<String> cacheEntry = new CacheEntry<String>(cachedRenderContent);

        return cacheEntry;
    }

    /**
     * Store some properties that may have been set during fragment execution
     *
     *
     * @param resource
     * @param cacheEntry
     * @param renderContext
     * @throws RepositoryException
     */
    private void addPropertiesToCacheEntry(Resource resource, CacheEntry<String> cacheEntry,
                                           RenderContext renderContext) throws RepositoryException {
        LinkedHashMap<String, Object> moduleParams = null;
        for (String property : moduleParamsProperties.keySet()) {
            if (resource.getNode().hasProperty(property)) {
                if (moduleParams == null) {
                    moduleParams = new LinkedHashMap<String, Object>();
                }
                moduleParams.put(moduleParamsProperties.get(property),
                        resource.getNode().getPropertyAsString(property));
            }
        }
        if (moduleParams != null && moduleParams.size() > 0) {
            cacheEntry.setProperty("moduleParams", moduleParams);
        }
        if (resource.getNode().isNodeType("jnt:area") || resource.getNode().isNodeType(
                "jnt:mainResourceDisplay")) {
            cacheEntry.setProperty("areaResource", resource.getNode().getIdentifier());
        }
        HashSet<String> allPaths = new HashSet<String>();
        for (Resource resource1 : renderContext.getResourcesStack()) {
            allPaths.add(resource1.getNode().getPath());
        }
        //Add current resource too as is as been removed by the TemplatesScriptFilter already
        allPaths.add(resource.getNode().getPath());
        cacheEntry.setProperty("allPaths",allPaths);
    }

    /**
     * Add key to the list of dependencies
     *
     * @param renderContext
     * @param finalKey
     * @param regexpDependenciesCache
     * @param regexp
     * @param newDependencies
     */
    protected void addDependencies(RenderContext renderContext, String finalKey, Cache regexpDependenciesCache, String regexp, Set<String> newDependencies) {
        if (newDependencies.add(finalKey)) {
            regexpDependenciesCache.put(new Element(regexp, newDependencies));
        }
    }

    /**
     * Replace all placeholders in the cache key to get a final key.
     * @param renderContext RenderContext
     * @param key Key with placeholders
     * @return The final key with placehodlers replaced
     */
    protected String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        return cacheProvider.getKeyGenerator().replacePlaceholdersInCacheKey(renderContext, key);
    }

    /**
     * Aggregate the content that are inside the cached fragment to get a full HTML content with all sub modules
     * embedded.
     *
     *
     * @param cache The cache
     * @param cachedContent The fragment, as it is stored in the cache
     * @param renderContext The render context
     * @param moduleParams Module params the key/value of the params that where on when this fragment was generated in case we need to regenerate it
     * @param areaIdentifier
     * @param cacheKeyStack
     * @param allPaths
     * @return
     */
    protected String aggregateContent(Cache cache, String cachedContent, RenderContext renderContext, Map<String, Serializable> moduleParams, String areaIdentifier,
                                      Stack<String> cacheKeyStack, Set<String> allPaths) {
        // aggregate content
        Source htmlContent = new Source(cachedContent);

        // Get all embedded esi:include tags that should be replaced with proper content
        List<? extends Tag> esiIncludeTags = htmlContent.getAllStartTags("esi:include");
        if (esiIncludeTags.size() > 0) {
            OutputDocument outputDocument = new OutputDocument(htmlContent);
            for (Tag esiIncludeTag : esiIncludeTags) {
                StartTag segment = (StartTag) esiIncludeTag;
                /*if (logger.isDebugEnabled()) {
                    logger.debug(segment.toString());
                }*/
                // Get the sub-fragment cache key
                String cacheKey = segment.getAttributeValue("src");

                // Replace placeholder to have a full contextual cache key
                String replacedCacheKey = replacePlaceholdersInCacheKey(renderContext, cacheKey);

                if (logger.isDebugEnabled()) {
                    logger.debug("Check if {} is in cache", replacedCacheKey);
                }

                if (cache.isKeyInCache(replacedCacheKey)) {
                    // If fragment is in cache, get it from there and aggregate recursively
                    final Element element = cache.get(replacedCacheKey);
                    if (element != null && element.getValue() != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("It has been found in cache", replacedCacheKey);
                        }
                        @SuppressWarnings("unchecked")
                        final CacheEntry<String> cacheEntry = (CacheEntry<String>) element.getValue();
                        String content = cacheEntry.getObject();
                        /*if (logger.isDebugEnabled()) {
                            logger.debug("Document replace from : " + segment.getStartTagType() + " to " +
                                         segment.getElement().getEndTag().getEndTagType() + " with " + content);
                        }*/

                        // Avoid loops
                        if (cacheKeyStack.contains(cacheKey)) {
                            continue;
                        }
                        cacheKeyStack.push(cacheKey);

                        if (!cachedContent.equals(content)) {
                            String aggregatedContent = aggregateContent(cache, content, renderContext, (Map<String, Serializable>) cacheEntry.getProperty("moduleParams"), (String) cacheEntry.getProperty("areaResource"), cacheKeyStack, (Set<String>) cacheEntry.getProperty("allPaths"));
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    aggregatedContent);
                        } else {
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    content);
                        }

                        cacheKeyStack.pop();
                    } else {
                        cache.put(new Element(replacedCacheKey, null));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Content is expired", replacedCacheKey);
                        }
                        // The fragment is not in the cache, generate it
                        generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams, areaIdentifier, allPaths);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Content is missing from cache", replacedCacheKey);
                    }
                    // The fragment is not in the cache, generate it
                    generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams, areaIdentifier,allPaths);
                }
            }
            return outputDocument.toString();
        }
        return cachedContent;
    }


    /**
     * Generates content for a sub fragment.
     * @param renderContext The render context
     * @param outputDocument The full output document
     * @param segment The segment to replace in the output document
     * @param cacheKey The cache key of the fragment to generate
     * @param moduleParams The module params of that fragment
     * @param areaIdentifier
     * @param allPaths
     */
    protected void generateContent(RenderContext renderContext, OutputDocument outputDocument, StartTag segment,
                                   String cacheKey, Map<String, Serializable> moduleParams, String areaIdentifier,
                                   Set<String> allPaths) {
        final CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
        try {
            // Parse the key to get all separate key attributes like node path and template
            Map<String, String> keyAttrbs = cacheKeyGenerator.parse(cacheKey);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getWorkspace(), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            JCRNodeWrapper node = null;
            try {
                // Get the node associated to the fragment to generate
                node = currentUserSession.getNode(StringUtils.replace(keyAttrbs.get("path"), PathCacheKeyPartGenerator.MAIN_RESOURCE_KEY,""));
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node {} is no longer available." + " Replacing output with empty content.",
                            keyAttrbs.get("path"));
                }
                // Node is not found, return empty content
                outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                        StringUtils.EMPTY);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Calling render service for generating content for key " + cacheKey + " with attributes : " +
                        new ToStringBuilder(keyAttrbs, ToStringStyle.MULTI_LINE_STYLE) + "\nmodule params : " +
                        ToStringBuilder.reflectionToString(moduleParams, ToStringStyle.MULTI_LINE_STYLE) +
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
            if(null==allPaths || allPaths.isEmpty()) {
                renderContext.getRequest().removeAttribute("org.jahia.area.stack.cache");
            } else {
                renderContext.getRequest().setAttribute("org.jahia.area.stack.cache",allPaths);
            }
            renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            Object oldInArea = (Object) renderContext.getRequest().getAttribute("inArea");
            String inArea = keyAttrbs.get("inArea");
            if (inArea == null || "".equals(inArea)) {
                renderContext.getRequest().removeAttribute("inArea");
            } else {
                renderContext.getRequest().setAttribute("inArea", Boolean.valueOf(inArea));
            }
            if (areaIdentifier != null) {
                renderContext.getRequest().setAttribute("areaListResource", currentUserSession.getNodeByIdentifier(areaIdentifier));
            }
            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), context);
            if (moduleParams != null) {
                for (Map.Entry<String, Serializable> entry : moduleParams.entrySet()) {
                    resource.getModuleParams().put(entry.getKey(), entry.getValue());
                }
            }

            // Dispatch to the render service to generate the content
            String content = RenderService.getInstance().render(resource, renderContext);
            if (content == null || "".equals(content.trim())) {
                logger.error("Empty generated content for key " + cacheKey + " with attributes : " +
                        new ToStringBuilder(keyAttrbs, ToStringStyle.MULTI_LINE_STYLE) + "\nmodule params : " +
                        ToStringBuilder.reflectionToString(moduleParams, ToStringStyle.MULTI_LINE_STYLE) +
                        " areaIdentifier " + areaIdentifier);
            }

            // And replace the content in the document
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), content);
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
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Add &lt;!-- cache include --&gt; tags around the content, with the cache key as an attribute.
     * @param key The key of the fragment
     * @param output The content
     * @return The content surrounded by the tag
     */
    protected String surroundWithCacheTag(String key, String output) {
        String cachedRenderContent;
        StringBuilder builder = new StringBuilder();
        builder.append("<!-- cache:include src=\"");
        builder.append(key);
        builder.append("\" -->\n");
        builder.append(output);
        builder.append("\n<!-- /cache:include -->");
        cachedRenderContent = builder.toString();
        return cachedRenderContent;
    }

    protected String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                            Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Expiration: </span><span>");
        String key1 = replacePlaceholdersInCacheKey(renderContext, key);
        if(!notCacheableFragment.containsKey(key)){
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(cacheProvider.getCache().get(
                    key1).getExpirationTime())));
        } else{
            stringBuilder.append("Not cached fragment ").append(SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        }
        stringBuilder.append("</span><br/>");
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }

    protected static OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : segments) {
            outputDocument.replace(segment.getElement().getContent(), "");
        }
        return outputDocument;
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
            String s = CLEANUP_REGEXP.matcher(content).replaceAll("");
            return s;
        } else {
            return content;
        }
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.getContentForError(renderContext, resource, chain, e);
        try {
            renderContext.getRequest().setAttribute("expiration", ""+errorCacheExpiration);
            // Returns a fragment with an error comment
            return execute("<!-- Module error : "+e.getMessage()+"-->", renderContext, resource, chain);
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
        boolean semaphoreAcquired = false;
        Map<String, CountDownLatch> generatingModules = generatorQueue.getGeneratingModules();
        if (generatingModules.get(key) == null && acquiredSemaphore.get() == null) {
            if (!generatorQueue.getAvailableProcessings().tryAcquire(generatorQueue.getModuleGenerationWaitTime(),
                    TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processings reached (" +
                        generatorQueue.getMaxModulesToGenerateInParallel() + ") - " + key + " - " +
                        request.getRequestURI());
            } else {
                acquiredSemaphore.set(key);
                semaphoreAcquired = true;
            }
        }
        synchronized (generatingModules) {
            if ( !Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration()) && Boolean.valueOf(StringUtils.defaultIfEmpty(properties.getProperty("cache.latch"),"true") )) {
                latch = generatingModules.get(key);
                if (latch == null) {
                    latch = new CountDownLatch(1);
                    generatingModules.put(key, latch);
                    mustWait = false;
                }
            } else {
                mustWait = false;
            }
        }
        if (mustWait) {
            if (semaphoreAcquired) {
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

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            notCacheableFragment.clear();
        }
    }

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

    public static void flushNotCacheableFragment() {
        notCacheableFragment.clear();
    }
}
