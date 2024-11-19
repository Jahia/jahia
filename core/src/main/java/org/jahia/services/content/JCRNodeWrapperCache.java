/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * <p>
 * Cache for {@code JCRNodeWrapper} instances.
 * </p>
 * <p>
 * Thread safety is not a concern here, as this cache is only used within the context of a single session.
 * The cache is designed to prevent the creation of multiple instances of the same node wrapper.
 * </p>
 * <p>
 * Two internal caches are used:
 * <ul>
 *   <li>
 *     <strong>{@code wrappersPerPath}</strong>: A cache for unmodified node wrappers. This cache can be size-limited
 *     using the {@code maxWrappedNodes} parameter. If a size limit is set, an LRU (Least Recently Used)
 *     eviction policy is applied to remove the oldest entries when the cache exceeds the {@code maxWrappedNodes} limit.
 *     This cache can be disabled with the {@code disable()} method and re-enabled with the {@code enable()} method.
 *   </li>
 *   <li>
 *     <strong>{@code changedWrappersPerPath}</strong>: A cache for all node wrappers that have been modified (e.g., nodes
 *     with added or changed properties). This cache is not limited in size and must be manually flushed when
 *     {@code session.save()} is called. It is primarily used to track all modified nodes before a save operation
 *     in order to perform validation. This cache is not subject to eviction and is unaffected by the {@code disable()} method.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * A given wrapper cannot reside in both caches simultaneously, due to the potential eviction rules applied to the main cache.
 * A wrapper will either be retrieved from the main cache or the changed cache, but not both.
 * </p>
 * <p>
 * This cache also provides convenience methods for moving or removing nodes from the cache, allowing it to handle most JCR operation updates.
 * Additionally, it offers a method to retrieve a node by its UUID by maintaining an internal map of UUIDs to paths.
 * Finally, it includes methods for flushing the cache—either by clearing all caches or by clearing only the main cache while retaining the current changes.
 * </p>
 * <p>
 * Modified node wrappers are always cached and never evicted; they are removed only when the session is saved
 * and a manual reset of the changes is performed. It is crucial to keep the changes cache synchronized with the session state.
 * When working with large batches of modified or created nodes, it is important to regularly save the session and flush the changes cache.
 * Since the changes cache is not subject to eviction, it can grow indefinitely if not properly managed.
 * </p>
 */
public class JCRNodeWrapperCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCRNodeWrapperCache.class);

    private final Map<String, JCRNodeWrapper> wrappersPerPath;
    private final Map<String, JCRNodeWrapper> changedWrappersPerPath = new HashMap<>();
    private final Map<String, String> uuidPath;
    private boolean disableCache = false;
    private int maxWrappedNodes;

    protected JCRNodeWrapperCache(int maxWrappedNodes) {
        this.maxWrappedNodes = maxWrappedNodes;
        if (this.maxWrappedNodes > 0) {
            // LRU cache will evict the eldest entry when the size exceeds the maxWrappedNodes
            this.wrappersPerPath = new InternalLRUMap();
        } else {
            // Not limited cache
            this.wrappersPerPath = new HashMap<>();
        }
        this.uuidPath = new HashMap<>();
    }

    /**
     * Get the size of the cache
     * @param includeChangedNodes if true will include the size of the changed nodes cache
     * @return the size of the cache
     */
    protected int getCacheSize(boolean includeChangedNodes) {
        return includeChangedNodes ? wrappersPerPath.size() + changedWrappersPerPath.size() : wrappersPerPath.size();
    }

    /**
     * Cache a node wrapper as not changed
     * @param path the path of the node, usually the node.getPath(), but could be different in case of dereferenced nodes
     * @param node the node to cache
     */
    protected void putNode(String path, JCRNodeWrapper node) {
        putNode(path, node, false);
    }

    /**
     * Cache a node wrapper
     * @param path the path of the node
     * @param node the node to cache
     * @param changed if true will cache the node as changed node in the changed cache not subject to eviction
     */
    protected void putNode(String path, JCRNodeWrapper node, boolean changed) {
        if (changed) {
            changedWrappersPerPath.put(path, node);
            try {
                uuidPath.put(node.getIdentifier(), path);
            } catch (RepositoryException e) {
                // ignore
            }
            // remove from main cache if present
            wrappersPerPath.remove(path);
        } else if (!disableCache && !changedWrappersPerPath.containsKey(path)) {
            wrappersPerPath.put(path, node);
            try {
                uuidPath.put(node.getIdentifier(), path);
            } catch (RepositoryException e) {
                // ignore
            }
        }
    }

    /**
     * Remove a node wrapper from the cache
     * @param path the path of the node to remove
     */
    protected void removeNode(String path) throws RepositoryException {
        removePathInCache(path, wrappersPerPath);
        removePathInCache(path, changedWrappersPerPath);
    }

    /**
     * Move a node wrapper
     * @param source the source path
     * @param dest the destination path
     */
    protected void moveNode(String source, String dest) throws RepositoryException {
        updatePathInCache(source, dest, wrappersPerPath);
        updatePathInCache(source, dest, changedWrappersPerPath);
    }

    /**
     * Get a node wrapper by path
     * @param path the path of the node
     * @return the node wrapper or null if not found in caches
     */
    protected JCRNodeWrapper getByPath(String path) {
        return changedWrappersPerPath.getOrDefault(path, wrappersPerPath.get(path));
    }

    /**
     * Get a node wrapper by UUID
     * @param uuid the UUID of the node
     * @return the node wrapper or null if not found in caches
     */
    protected JCRNodeWrapper getByUUID(String uuid) {
        String cachedPath = uuidPath.get(uuid);
        if (cachedPath != null) {
            JCRNodeWrapper cached = getByPath(cachedPath);
            if (cached != null) {
                return cached;
            } else {
                // not in cache anymore ? remove uuid/path mapping
                uuidPath.remove(uuid);
            }
        }
        return null;
    }

    /**
     * Get all changed node wrappers
     * @return a collection of all changed node wrappers
     */
    protected Collection<JCRNodeWrapper> getAllChangedNodes() {
        return new ArrayList<>(changedWrappersPerPath.values());
    }

    /**
     * Flush the cache
     * @param keepChanges if true will keep the changed nodes cache intact
     */
    protected void flush(boolean keepChanges) {
        if (!keepChanges) {
            clearAllCaches();
        } else {
            clearSingleCache(wrappersPerPath);
        }
    }

    /**
     * Reset the changes cache, will flush the changed nodes cache
     * Useful after a session.save()
     */
    protected void resetChanges() {
        clearSingleCache(changedWrappersPerPath);
    }

    /**
     * Allow to disable the main cache, no wrappers will be cached anymore
     * Only the changed nodes cache will be maintained
     */
    protected void disable() {
        if (!disableCache) {
            disableCache = true;
            flush(true);
        }
    }

    /**
     * Allow to enable the main cache
     */
    protected void enable() {
        if (disableCache) {
            disableCache = false;
        }
    }

    /**
     * Check if the main cache is enabled
     * @return true if the main cache is enabled
     */
    protected boolean isEnabled() {
        return !disableCache;
    }

    private void updatePathInCache(String source, String dest, Map<String, JCRNodeWrapper> cacheByPath) throws RepositoryException {
        String sourcePrefix = source + "/";
        Set<String> paths = new HashSet<>(cacheByPath.keySet());
        for (String s : paths) {
            if (s.equals(source) || s.startsWith(sourcePrefix)) {
                JCRNodeWrapper n = cacheByPath.remove(s);
                if (n instanceof JCRNodeDecorator) {
                    n = ((JCRNodeDecorator) n).getDecoratedNode();
                }
                String newPath = dest;
                if (source.length() < n.getPath().length()) {
                    newPath += n.getPath().substring(source.length());
                }
                String localPath = newPath;
                if (n.getProvider().getMountPoint().length() > 1) {
                    localPath = newPath.substring(n.getProvider().getMountPoint().length());
                }
                ((JCRNodeWrapperImpl) n).localPath = localPath;
                ((JCRNodeWrapperImpl) n).localPathInProvider = localPath;
                cacheByPath.put(newPath, n);
                uuidPath.put(n.getIdentifier(), newPath);
            }
        }
    }

    private void removePathInCache(String path, Map<String, JCRNodeWrapper> cacheByPath) throws RepositoryException {
        String pathPrefix = path + "/";
        Set<String> paths = new HashSet<>(cacheByPath.keySet());
        for (String s : paths) {
            if (s.equals(path) || s.startsWith(pathPrefix)) {
                JCRNodeWrapper n = cacheByPath.remove(s);
                uuidPath.remove(n.getIdentifier());
            }
        }
    }

    private void clearSingleCache(Map<String, JCRNodeWrapper> cacheByPath) {
        for (JCRNodeWrapper value : cacheByPath.values()) {
            try {
                uuidPath.remove(value.getIdentifier());
            } catch (RepositoryException ignored) { //
            }
        }
        cacheByPath.clear();
    }

    private void clearAllCaches() {
        wrappersPerPath.clear();
        changedWrappersPerPath.clear();
        uuidPath.clear();
    }

    class InternalLRUMap extends LinkedHashMap<String, JCRNodeWrapper> {
        InternalLRUMap() {
            super(16, 0.75f, true);
        }
        protected boolean removeEldestEntry(Map.Entry<String, JCRNodeWrapper> eldest) {
            boolean remove = this.size() > maxWrappedNodes;
            if (remove) {
                // sync uuidPath
                try {
                    uuidPath.remove(eldest.getValue().getIdentifier());
                } catch (RepositoryException e) {
                    // ignore
                }
            }
            return remove;
        }
    }
}
