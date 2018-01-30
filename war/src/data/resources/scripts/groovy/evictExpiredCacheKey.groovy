import java.util.Collection
import net.sf.ehcache.Element
import net.sf.ehcache.store.Store
import org.jahia.services.SpringContextSingleton
import org.jahia.services.cache.ehcache.EhCacheProvider
import org.slf4j.LoggerFactory

def long startTime = System.currentTimeMillis();

def logger = LoggerFactory.getLogger("evictExpiredCacheKeyJob");

// 1. On this step we check the entries in the HTMLDependenciesCache and HTMLREGEXPDependenciesCache
// and update the lastAccessTime of entries to the greatest lastAccessTime of the corresponding entry in HTMLCache
// to avoid their expiration while they are idling, but corresponding HTMLCache entries are accessed

def outputCacheManager = ((EhCacheProvider) SpringContextSingleton.getBean("bigEhCacheProvider")).getCacheManager()
def outCache = outputCacheManager.getCache("HTMLCache");
// if we do not have any entry in HTMLCache, we skip the whole step
if (outCache != null && outCache.getSize() > 0) {
    // we iterate on two caches
    [outputCacheManager.getCache("HTMLDependenciesCache"), outputCacheManager.getCache("HTMLREGEXPDependenciesCache")].each { depCache ->
        def Store outStore = outCache.getStore();
        if (depCache != null && !depCache.getCacheConfiguration().isEternal()) {
            def Store depStore = depCache.getStore();
            int updatedCount = 0;
            for (String key : depCache.getKeys()) {
                // this call bypasses the expiry check of the element
                Element e = depStore.getQuiet(key);
                // we are interested only in non-null and non-eternal elements with idle timeout set
                Object value = e != null && !e.isEternal() && e.getTimeToIdle() > 0 ? e.getObjectValue() : null;
                if (value instanceof Collection) {
                    long currentAccessTime = Math.max(e.getCreationTime(), e.getLastAccessTime());
                    for (String depKey : (Collection) value) {
                        Element htmlCacheElement = outStore.getQuiet(depKey);
                        if (htmlCacheElement != null) {
                            long otherLastAccessTime = htmlCacheElement.getLastAccessTime();
                            if (otherLastAccessTime > currentAccessTime) {
                                // corresponding lastAccessTime of the entry in HTMLCache is greater: will take it
                                currentAccessTime = otherLastAccessTime;
                            }
                        }
                    }
                    if (currentAccessTime > e.getLastAccessTime()) {
                        // corresponding lastAccessTime of the entries in HTMLCache is greater
                        // -> will update the lastAccessTime of the entry in dependencies cache with that value
                        e.@lastAccessTime = currentAccessTime;
                        updatedCount++;
                    }
                }
            }
            if (updatedCount > 0) {
                logger.info("Updated lastAccessTime for " + updatedCount + " entries in " + depCache.getName());
            }
        }
    }
}

// 2. On this step we iterate over all caches and trigger the eviction of expired entries
def totalKeys = 0
def cleanedKeys = 0
def mapCleanedKeysPerCache = [:]
def managerList = ["ehCacheProvider", "bigEhCacheProvider"]
managerList.each { beanName ->
    def cacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(beanName)).getCacheManager()
    cacheManager.getCacheNames().each { cacheName ->
        def cache = cacheManager.getCache(cacheName)
        if (cache != null) {
            def originalSize = cache.getSize()
            totalKeys += originalSize
            cache.evictExpiredElements()
            def newSize = cache.getSize()
            cleanedKeys += newSize
            if (newSize != originalSize) {
                mapCleanedKeysPerCache[cache.name] = originalSize - newSize
            }
        }
    }
}

def cleanedKeysTotal = totalKeys - cleanedKeys
if (cleanedKeysTotal > 0) {
    logger.info("Cleaned ${cleanedKeysTotal} keys from the caches")

    mapCleanedKeysPerCache.each() { key, val -> logger.info("\t- ${val} from cache ${key}") }
}

logger.info("Done check for expired cache entries in " + (System.currentTimeMillis() - startTime) + " ms");