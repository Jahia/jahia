package net.sf.ehcache.hibernate;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import org.hibernate.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * A sharing Hibernate EHCache provider that can re-use a CacheManager provided by Jahia. This is useful for using
 * Hibernate inside modules.
 */
public class SharingEhcacheProvider extends AbstractEhcacheProvider {

    public static final String ORG_JAHIA_HIBERNATE_EHCACHE_EXISTING_CACHE_MANAGER_NAME = "org.jahia.hibernate.ehcache.existingCacheManagerName";

    private static final Logger LOG = LoggerFactory.getLogger(SharingEhcacheProvider.class.getName());

    private final ProviderMBeanRegistrationHelper mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();

    @Override
    public void start(Properties properties) throws CacheException {
        if (manager != null) {
            LOG.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() " +
                    " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider." +
                    " If this behaviour is required, consider using SingletonEhCacheProvider.");
            return;
        }
        try {
            String existingCacheManagerName = null;
            if (properties != null) {
                existingCacheManagerName = (String) properties.get(ORG_JAHIA_HIBERNATE_EHCACHE_EXISTING_CACHE_MANAGER_NAME);
            }
            if (existingCacheManagerName == null || existingCacheManagerName.length() == 0) {
                throw new CacheException("No existing cache manager name specified in configuration, property " + ORG_JAHIA_HIBERNATE_EHCACHE_EXISTING_CACHE_MANAGER_NAME + " was not set properly.");
            } else {
                manager = CacheManager.getCacheManager(existingCacheManagerName);
            }
            mbeanRegistrationHelper.registerMBean(manager, properties);
        } catch (net.sf.ehcache.CacheException e) {
            if (e.getMessage().startsWith("Cannot parseConfiguration CacheManager. Attempt to create a new instance of " +
                    "CacheManager using the diskStorePath")) {
                throw new CacheException("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() " +
                        " between repeated calls to buildSessionFactory. Consider using SingletonEhCacheProvider. Error from " +
                        " ehcache was: " + e.getMessage());
            } else {
                throw e;
            }
        }

    }

    @Override
    public void stop() {
        // Do nothing we let the real creator of the cache manager to the shutdown.
    }
}
