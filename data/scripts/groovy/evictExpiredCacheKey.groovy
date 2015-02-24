import org.jahia.services.SpringContextSingleton
import org.jahia.services.cache.ehcache.EhCacheProvider

def cacheManager = ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider")).getCacheManager()
def totalKeys = 0
def cleanedKeys = 0
def mapCleanedKeysPerCache = [:]
cacheManager.getCacheNames().each {cacheName ->
    def cache = cacheManager.getCache(cacheName)
    if(cache!=null) {
        def originalSize = cache.getKeys().size()
        totalKeys += originalSize
        def newSize = cache.getKeysWithExpiryCheck().size()
        cleanedKeys += newSize
        if(newSize!=originalSize){
            mapCleanedKeysPerCache[cache.name] = originalSize - newSize
        }
    }
}
println "Cleaned ${(totalKeys - cleanedKeys)} keys from the caches"

mapCleanedKeysPerCache.each() {key,val -> println "\t- ${val} from cache ${key}"}