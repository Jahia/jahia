import org.jahia.services.SpringContextSingleton
import org.jahia.services.cache.ehcache.EhCacheProvider
import org.slf4j.LoggerFactory

def totalKeys = 0
def cleanedKeys = 0
def mapCleanedKeysPerCache = [:]
def managerList = ["ehCacheProvider", "bigEhCacheProvider"]
def logger = LoggerFactory.getLogger("evictExpiredCacheKeyJob");
managerList.each { beanName ->
    def cacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(beanName)).getCacheManager()
    cacheManager.getCacheNames().each { cacheName ->
        def cache = cacheManager.getCache(cacheName)
        if (cache != null) {
            def originalSize = cache.getKeys().size()
            totalKeys += originalSize
            def newSize = cache.getKeysWithExpiryCheck().size()
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