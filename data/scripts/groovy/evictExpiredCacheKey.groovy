import org.jahia.services.SpringContextSingleton
import org.jahia.services.cache.ehcache.EhCacheProvider

def cacheManager = ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider")).getCacheManager()
def totalKeys = 0
def cleanedKeys = 0
cacheManager.getCacheNames().each {cacheName ->
    def cache = cacheManager.getCache(cacheName)
    if(cache!=null) {
        totalKeys += cache.getKeys().size()
        cleanedKeys += cache.getKeysWithExpiryCheck().size()
    }
}
println "Cleaned " + (totalKeys - cleanedKeys) + " keys from the caches"