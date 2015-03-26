package org.jahia.services.cache.ehcache;

import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.SizeOfEngineFactory;
import net.sf.ehcache.pool.impl.DefaultSizeOfEngine;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.slf4j.Logger;

public class JahiaSizeOfEngineFactory implements SizeOfEngineFactory {
    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaSizeOfEngineFactory.class);
    @Override
    public SizeOfEngine createSizeOfEngine(int maxObjectCount, boolean abort, boolean silent) {
        logger.info("Using JahiaSizeOfEngineFactory");
        System.setProperty(DefaultSizeOfEngine.USER_FILTER_RESOURCE,Thread.currentThread().getContextClassLoader().getResource("jahiaSizeOfEngine.filter").toString());
        return new DefaultSizeOfEngine(maxObjectCount, abort, silent);
    }
}
