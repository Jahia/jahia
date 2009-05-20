/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache.jboss.cache;

import java.util.*;

import org.apache.log4j.Logger;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheListener;
import org.jboss.cache.CacheException;
import org.jboss.cache.ConfigureException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;

public class TreeCacheImpl implements CacheImplementation {
    /** logging. */
    private static final transient Logger logger = Logger.getLogger(TreeCacheImpl.class);

    private static final String[] nonTxCaches = {  };

    private static TreeCache globalNonTxTreeCache = null;
    private static TreeCache globalTxTreeCache = null;
    
    private TreeCache treeCache = null;

    private Fqn fqn;
    private Fqn groupsFqn;

    /**
     * <p>
     * Creates a new <code>Cache</code> instance.
     * </p>
     *
     * @param aName
     *            the cache name
     */
    protected TreeCacheImpl(final String aName) {
            init(aName);
    }

    /**
     * <p>
     * Initialize the cache.
     * </p>
     *
     * @param aName
     *            the cache name
     */
    private void init(String aName) {
        initTreeCache(aName);
        fqn = new Fqn(aName);
        groupsFqn = new Fqn(fqn, "groups");
        try {
            getTreeCache().put(fqn, new HashMap());
        } catch (Exception e) {
            logger.warn(getName(), e);
        }
    }

    /**
     * @return Returns the treeCache.
     */
    private TreeCache getTreeCache() {
        return treeCache;
    }

    /**
     * @return Returns the txTreeCache.
     */
    private TreeCache getNonTxTreeCache() {
        return globalNonTxTreeCache;
    }

    /**
     * @return Returns the txTreeCache.
     */
    private TreeCache getTxTreeCache() {
        return globalTxTreeCache;
    }

    /**
     * @param aTreeCache
     *            The treeCache to set.
     */

//    private void setTreeCache(TreeCache aTreeCache) {
//        treeCache = aTreeCache;
//    }

    public boolean containsKey(Object entryKey) {
        boolean containsKey = false;
        Node node = getTreeNode();
        if (node != null) {
            containsKey = node.childExists(entryKey);
        }
        return containsKey;
    }

    public void flushAll(boolean propagate) {
        try {
            if (!propagate) {
                getTreeCache().evict(getFqn());
            } else {
                getTreeCache().remove(getFqn());
            }

            getTreeCache().put(getName(), new HashMap());
        } catch (Exception e) {
            logger.warn(getName(), e);
        }
    }

    public void flushGroup(String groupName) {
        try {
            Fqn currentGroup = new Fqn(groupsFqn, groupName);
            Node group = getNonTxTreeCache().get(currentGroup);
            if (group != null){
                for (Iterator it = group.getDataKeys().iterator(); it.hasNext();){
                    Fqn itemFqn = (Fqn)it.next();
                    if (getTreeCache().exists(itemFqn)){
                        getTreeCache().remove(itemFqn);
                    }
                }
                getNonTxTreeCache().remove(currentGroup);
            }
        } catch (Exception e) {
            logger.warn(getName(), e);
        }
    }

    public Object get(Object entryKey) {
        Object result = null;

        CacheEntry entry = getCacheEntry(entryKey);
        if (entry != null) {
            result = entry.getObject();
        }

        return result;
    }

    private Fqn getFqn() {
        return fqn;
    }

    public String getName() {
        return (String) fqn.get(0);
    }

    public Object[] keys() {
        Object[] keys = null;
        try {
            Set treeKeys = getTreeCache().getChildrenNames(getFqn());

            if (treeKeys != null) {
                keys = treeKeys.toArray();
            }
        } catch (CacheException e) {
            logger.warn(getName(), e);
        }
        if (keys == null) {
            keys = new Object[] {};
        }

        return keys;
    }

    public void put(Object entryKey, Object entryObj) {
        CacheEntry entry = new CacheEntry(entryObj);
        putCacheEntry(entryKey, entry, true);
    }

    public void put(Object entryKey, String groups[], Object entryObj) {
        try {
            put(entryKey, entryObj);

            if (groups != null){
            	Fqn entryFqn = new Fqn(getFqn(), entryKey);
                for (int i = 0; i < groups.length; i++){
                    getNonTxTreeCache().put(new Fqn(groupsFqn, groups[i]), entryFqn, null);
                }
            }
        } catch (Exception e) {
            logger.warn(getName() + entryKey, e);
        }
    }

    /**
     * <p>
     * Register a new cache listener.
     * </p>
     * <p>
     * When the specified <code>listener</code> is <code>null</code> or
     * already present within the listeners list, the registration process is
     * ignored.
     * </p>
     *
     * @param listener
     *            the reference of the cache listener to register.
     * @since Jahia 4.0.2
     */
    public synchronized void addListener(CacheListener listener) {
        getTreeCache().addTreeCacheListener(new TreeCacheListenerWrapper(listener));
    }

    /**
     * <p>
     * Unregister a cache listener.
     * </p>
     * <p>
     * When there is not cache listener registered, or the specified
     * <code>listener</code> is <code>null</code>, the unregistration
     * process is ignored.
     * </p>
     *
     * @param listener
     *            the reference of the cache listener to register
     * @since Jahia 4.0.2
     */
    public synchronized void removeListener(CacheListener listener) {
        getTreeCache().removeTreeCacheListener(new TreeCacheListenerWrapper(listener));
    }

    public void remove(Object entryKey) {
        try {
            Fqn localFqn = new Fqn(getFqn(),
                    entryKey instanceof List ? (List) entryKey : entryKey);

            getTreeCache().remove(localFqn);
        } catch (CacheException e) {
            logger.warn(getName() + entryKey, e);
        }
    }

    /**
     * <p>
     * Return true if cache is empty.
     * </p>
     *
     * @return true if cache is empty
     */
    final public boolean isEmpty() {
        return !getTreeCache().hasChild(getFqn());
    }

    public CacheEntry getCacheEntry(Object entryKey) {
        CacheEntry entry = null;
        try {
            Fqn entryFqnKey = new Fqn(getFqn(),
                    entryKey instanceof List ? (List) entryKey : entryKey);

            entry = (CacheEntry) getTreeCache().get(
                    entryFqnKey, "entry");
            // check if the entry is expired
            if (entry != null
                    && entry.getExpirationDate() != null
                    && entry.getExpirationDate().getTime() <= System
                            .currentTimeMillis()) {
                // entry has expired, we must remove it and then exit.
                logger.debug("Cache entry has expired, ignoring entry and removing...");
                getTreeCache().remove(entryFqnKey);
                entry = null;
            }
        } catch (CacheException e) {
            logger.warn(getName() + entryKey, e);
        }
        return entry;
    }

    public void putCacheEntry(Object entryKey, CacheEntry entry,
            boolean propagate) {
        try {
            if (entryKey instanceof List) {
                List entryList = (List) entryKey;
                getTreeCache()
                        .put(new Fqn(getFqn(), entryList), "entry", entry);
            } else {
                getTreeCache().put(new Fqn(getFqn(), entryKey), "entry", entry);
            }
        } catch (Exception e) {
            logger.warn(getName() + entryKey, e);
        }
    }

    public int size() {
        int size = 0;
        try {
            Set treeKeys = getTreeCache().getChildrenNames(getFqn());
            if (treeKeys != null) {
                size = treeKeys.size();
            }
        } catch (CacheException e) {
            logger.warn(getName(), e);
        }
        return size;
    }

    public long getGroupsSize() {
        int size = 0;
        try {
            Set treeKeys = getTreeCache().getChildrenNames(groupsFqn);
            if (treeKeys != null) {
                size = treeKeys.size();
            }
        } catch (CacheException e) {
            logger.warn(getName(), e);
        }
        return size;
    }

    /**
     * Not yet implemented for JBossCache.
     * @return
     */
    public long getGroupsKeysTotal() {
        return -1;
    }

    public long getCacheLimit() {
        int cacheLimit = -1;
        /*
         * Element evictionPolicyConfig = tree.getEvictionPolicyConfig(); if
         * (evictionPolicyConfig != null) { String maxNodes =
         * evictionPolicyConfig.getAttribute("maxNodes"); if (maxNodes != null &&
         * maxNodes.length() > 0) cacheLimit = Integer.parseInt(maxNodes); }
         */
        return cacheLimit;
    }

    public void setCacheLimit(long limit) {
        // tree.setEvictionPolicyConfig(new DOMElementImpl());

    }

    /**
     * Not supported
     * @return
     */
    public long getCacheGroupsLimit() {
        return -1;
    }

    /**
     * Not supported
     * @param groupsLimit
     */
    public void setCacheGroupsLimit(long groupsLimit) {
    }

    private TreeCache initTreeCache(String aName) {
        boolean isTxCache = isTxCache(aName);

/*
        try {
            TreeCacheMBean cache = (TreeCacheMBean) MBeanProxyExt.create(TreeCacheMBean.class,
                    isTxCache ? "jboss.cache:service=jahiaTxTreeCache" : "jboss.cache:service=jahiaTreeCache",
                    MBeanServerLocator.locateJBoss());

            cacheInstance = (TreeCache)cache.getInstance();
        } catch (MalformedObjectNameException e) {
            logger.error("Treecache must be named: jboss.cache:service=jahiaTreeCache. Check XML file!", e);
        }
*/
        if (getNonTxTreeCache() == null) {
            try {
                globalNonTxTreeCache = new TreeCache();
                PropertyConfigurator config = new PropertyConfigurator();
                config.configure(globalNonTxTreeCache, "jahia-cache-service.xml");
                globalNonTxTreeCache.start();
            } catch (ConfigureException e) {
                logger.error("Jahia Tree-Cache could not be started due to configuration errors", e);
            } catch (Exception e) {
                logger.error("Jahia Tree-Cache could not be started", e);
            }
        }

        if (getTxTreeCache() == null) {
            try {
                globalTxTreeCache = new TreeCache();
                PropertyConfigurator config = new PropertyConfigurator();
                config.configure(globalTxTreeCache, "jahia-tx-cache-service.xml");
                globalTxTreeCache.start();
            } catch (ConfigureException e) {
                logger.error("Jahia Tree-Cache could not be started due to configuration errors", e);
            } catch (Exception e) {
                logger.error("Jahia Tree-Cache could not be started", e);
            }
        }

        treeCache = isTxCache ? globalTxTreeCache : globalNonTxTreeCache;

        return treeCache;
    }

    private Node getTreeNode() {
        Node treeNode = null;
        try {
            treeNode = getTreeCache().get(getFqn());
        } catch (CacheException e) {
            logger.warn(getName(), e);
        }
        return treeNode;
    }

    private boolean isTxCache (String aName) {
        boolean isTxCache = true;

        for (int i = 0; i < nonTxCaches.length && !isTxCache; i++) {
            if (nonTxCaches[i].equals(aName))
                isTxCache = false;
        }
        
        return isTxCache;
    }
    
	public void setName(String name) {
		// not supported.
	}
    public Set getGroupKeys(String groupName) {
        return null;//To change body of implemented methods use File | Settings | File Templates.
    }
    

    public Collection<Object> getKeys() {
        Set<Object> treeKeys = Collections.emptySet(); 
        try {
            treeKeys = getTreeCache().getChildrenNames(getFqn());
        } catch (CacheException e) {
            logger.warn(getName(), e);
        }

        return treeKeys;
    }
}
