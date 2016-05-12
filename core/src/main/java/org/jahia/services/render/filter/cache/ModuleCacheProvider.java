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
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.jackrabbit.core.JahiaRepositoryImpl;
import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.ehcache.DependenciesCacheEvictionPolicy;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instantiates and provides access to the module output and dependency caches.
 *
 * @author Cedric Mailleux
 * @author Sergiy Shyrkov
 */
public class ModuleCacheProvider implements InitializingBean, ApplicationListener<TemplatePackageRedeployedEvent> {

    private static final String CACHE_NAME = "HTMLCache";
    private static final String CACHE_SYNC_NAME = "HTMLCacheEventSync";
    private static final String DEPS_CACHE_NAME = "HTMLDependenciesCache";
    private static final String REGEXPDEPS_CACHE_NAME = "HTMLREGEXPDependenciesCache";

    private static Logger logger = LoggerFactory.getLogger(ModuleCacheProvider.class);

    private Cache regexpDependenciesCache;
    private Cache htmlCache;
    private EhCacheProvider cacheProvider;
    private Cache dependenciesCache;
    private Cache syncCache;
    private Set<String> nonCacheableFragments = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());;
    private CacheKeyGenerator keyGenerator;
    private JCRSessionFactory jcrSessionFactory;

    /**
     * @return an instance of this class
     */
    public static ModuleCacheProvider getInstance() {
        return (ModuleCacheProvider) SpringContextSingleton.getBean("ModuleCacheProvider");
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p/>
     * This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an exception
     * in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such as failure to
     *                   set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {

        CacheManager cacheManager = cacheProvider.getCacheManager();
        htmlCache = cacheManager.getCache(CACHE_NAME);
        if (htmlCache == null) {
            cacheManager.addCache(CACHE_NAME);
            htmlCache = cacheManager.getCache(CACHE_NAME);
        }
        dependenciesCache = cacheManager.getCache(DEPS_CACHE_NAME);
        if (dependenciesCache == null) {
            cacheManager.addCache(DEPS_CACHE_NAME);
            dependenciesCache = cacheManager.getCache(DEPS_CACHE_NAME);
        }
        dependenciesCache.setMemoryStoreEvictionPolicy(new DependenciesCacheEvictionPolicy());
        regexpDependenciesCache = cacheManager.getCache(REGEXPDEPS_CACHE_NAME);
        if (regexpDependenciesCache == null) {
            cacheManager.addCache(REGEXPDEPS_CACHE_NAME);
            regexpDependenciesCache = cacheManager.getCache(REGEXPDEPS_CACHE_NAME);
        }

        if (SettingsBean.getInstance().isClusterActivated()) {
            // only create syncCache in cluster
            syncCache = cacheManager.getCache(CACHE_SYNC_NAME);
            if (syncCache == null) {
                cacheManager.addCache(CACHE_SYNC_NAME);
                syncCache = cacheManager.getCache(CACHE_SYNC_NAME);
            }
        }
    }

    /**
     * Flushes all the cache entries, related to the specified node.
     *
     * @param nodePathOrIdentifier the node path or node uuid to be invalidated.
     * @throws ParseException in case of a malformed key
     */
    public void invalidate(String nodePathOrIdentifier) {
        invalidate(nodePathOrIdentifier, true);
    }

    /**
     * Flushes all the cache entries, related to the specified node.
     * @param nodePathOrIdentifier the node path or node uuid to be invalidated.
     * @param propagateToOtherClusterNodes do notify replicators of this event
     * @throws ParseException in case of a malformed key
     */
    @SuppressWarnings("unchecked")
    public void invalidate(String nodePathOrIdentifier, boolean propagateToOtherClusterNodes) {
        Element element = dependenciesCache.get(nodePathOrIdentifier);
        if (element != null) {
            Set<String> deps = (Set<String>) element.getObjectValue();
            if (deps.contains("ALL")) {
                // do not propagate
                htmlCache.removeAll(true);
            } else {
                htmlCache.removeAll(deps);
            }
        }
        if(propagateToOtherClusterNodes) {
            propagatePathFlushToCluster(nodePathOrIdentifier);
        }
    }

    public void invalidate(Collection<String> nodePathOrIdentifiers, boolean propagateToOtherClusterNodes) {

        Set<String> all = new HashSet<>();
        for (String nodePathOrIdentifier : nodePathOrIdentifiers) {
            Element element = dependenciesCache.get(nodePathOrIdentifier);
            if (element != null) {
                @SuppressWarnings("unchecked")
                Set<String> deps = (Set<String>) element.getObjectValue();
                if (deps.contains("ALL")) {
                    // do not propagate
                    htmlCache.removeAll(true);
                    all.clear();
                    break;
                } else {
                    all.addAll(deps);
                }
            }
        }
        htmlCache.removeAll(all);

        if(propagateToOtherClusterNodes) {
            for (String nodePathOrIdentifier : nodePathOrIdentifiers) {
                propagatePathFlushToCluster(nodePathOrIdentifier);
            }
        }
    }


    public Cache getCache() {
        return htmlCache;
    }

    public Cache getDependenciesCache() {
        return dependenciesCache;
    }

    public CacheKeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * Injects the cache key generator implementation.
     *
     * @param keyGenerator the cache key generator implementation to use
     */
    public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void flushCaches() {
        htmlCache.removeAll();
        htmlCache.flush();
        dependenciesCache.removeAll();
        dependenciesCache.flush();
        regexpDependenciesCache.removeAll();
        regexpDependenciesCache.flush();
    }

    public Cache getRegexpDependenciesCache() {
        return regexpDependenciesCache;
    }

    public void invalidateRegexp(String key) {
        invalidateRegexp(key, true);
    }

    public void invalidateRegexp(String key, boolean propagateToOtherClusterNodes) {
        Element element = regexpDependenciesCache.get(key);
        if (element != null) {
            @SuppressWarnings("unchecked")
            Set<String> deps = (Set<String>) element.getObjectValue();
            htmlCache.removeAll(deps);
        }
        if(propagateToOtherClusterNodes && syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of regexp {} across cluster", key);
            }
            syncCache.put(new Element("FLUSH_REGEXP-" + UUID.randomUUID(), new CacheClusterEvent(key,getClusterRevision())));
        }
    }

    public void propagateFlushRegexpDependenciesOfPath(String key, boolean propagateToOtherClusterNodes) {
        if(propagateToOtherClusterNodes && syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of regexp dependencies {} across cluster", key);
            }
            syncCache.put(new Element("FLUSH_REGEXPDEP-" + UUID.randomUUID(), new CacheClusterEvent(key,getClusterRevision())));
        }
    }

    public void propagateChildrenDependenciesFlushToCluster(String path, boolean propagateToOtherClusterNodes) {
        if(propagateToOtherClusterNodes && syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of children of {} across cluster", path);
            }
            syncCache.put(new Element("FLUSH_CHILDS-" + UUID.randomUUID(), new CacheClusterEvent(path,getClusterRevision())));
        }
    }

    /**
     * Flush Children dependencies of a specific path
     * @param path path to flush all its children cache
     * @param propagateToOtherClusterNodes true if it should propagate to other cluster nodes
     */
    public void flushChildrenDependenciesOfPath(String path, boolean propagateToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path: {}", path);
        }
        @SuppressWarnings("unchecked")
        List<String> keys = dependenciesCache.getKeys();
        String pathWithTrailingSlash = null;
        if (!keys.isEmpty()) {
            pathWithTrailingSlash = path + '/';
        }
        for (String key : keys) {
            if (key.equals(path) || key.startsWith(pathWithTrailingSlash)) {
                invalidate(key, propagateToOtherClusterNodes);
            }
        }
        if (SettingsBean.getInstance().isClusterActivated()) {
            propagateChildrenDependenciesFlushToCluster(path, propagateToOtherClusterNodes);
        }
    }

    /**
     * Flushes dependencies if the provided node path matches the corresponding key in the {@link #REGEXPDEPS_CACHE_NAME}} cache.
     *
     * @param path
     *            the concerned node path
     * @param propagateToOtherClusterNodes
     *            <code>true</code> in case the flush event should be propagated to other cluster nodes
     */
    public void flushRegexpDependenciesOfPath(String path, boolean propagateToOtherClusterNodes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Flushing dependencies for path: {}", path);
        }
        @SuppressWarnings("unchecked")
        List<String> keys = getRegexpDependenciesCache().getKeys();
        for (String key : keys) {
            if (path.matches(key)) {
                invalidateRegexp(key, propagateToOtherClusterNodes);
            }
        }
        if (propagateToOtherClusterNodes && SettingsBean.getInstance().isClusterActivated()) {
            propagateFlushRegexpDependenciesOfPath(path, propagateToOtherClusterNodes);
        }
    }

    /**
     * Set the given fragment key as non-cacheable
     * @param key fragment key
     */
    public void addNonCacheableFragment(String key) {
        nonCacheableFragments.add(key);
    }

    /**
     * Remove keys from the non-cacheable fragments map looking for path in the cache key given as parameter
     * @param key fragment key of the fragment
     */
    public void removeNonCacheableFragment(String key) {
        Map<String, String> keyAttrs = keyGenerator.parse(key);
        String path = keyAttrs.get("path");
        List<String> removableKeys = new ArrayList<String>();
        for (String nonCacheableKey : nonCacheableFragments) {
            if (nonCacheableKey.contains(path)) {
                removableKeys.add(nonCacheableKey);
            }
        }
        for (String removableKey : removableKeys) {
            nonCacheableFragments.remove(removableKey);
        }
    }

    /**
     * Flush the non-cacheable fragments map
     */
    public void flushNonCacheableFragments() {
        nonCacheableFragments.clear();
    }

    /**
     * Check if fragment key is known as non-cacheable
     * @param key fragment key
     * @return whether the fragment is known as non-cacheable
     */
    public boolean isNonCacheableFragment(String key) {
        return nonCacheableFragments.contains(key);
    }

    public void propagatePathFlushToCluster(String nodePathOrIdentifier) {
        if (syncCache != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending flush of {} across cluster", nodePathOrIdentifier);
            }
            syncCache.put(new Element("FLUSH_PATH-" + UUID.randomUUID(), new CacheClusterEvent(nodePathOrIdentifier,getClusterRevision())));
        }
    }

    private long getClusterRevision() {
        final ClusterNode clusterNode = ((JahiaRepositoryImpl) ((SpringJackrabbitRepository) jcrSessionFactory.getDefaultProvider().getRepository()).getRepository()).getContext().getClusterNode();
        return clusterNode.getRevision();
    }

    public void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }

    public Cache getSyncCache() {
        return syncCache;
    }

    @Override
    public void onApplicationEvent(TemplatePackageRedeployedEvent templatePackageRedeployedEvent) {
        flushNonCacheableFragments();
    }
}
