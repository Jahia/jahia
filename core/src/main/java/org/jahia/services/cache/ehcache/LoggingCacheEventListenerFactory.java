package org.jahia.services.cache.ehcache;

import net.sf.ehcache.event.CacheEventListenerFactory;
import net.sf.ehcache.event.CacheEventListener;

import java.util.Properties;

/**
 * A very simpler cache event listener factory that creates simple debugging
 * listeners that log all events to the Jahia logs.
 *
 * @author loom
 *         Date: Sep 14, 2009
 *         Time: 3:53:25 PM
 */
public class LoggingCacheEventListenerFactory extends CacheEventListenerFactory {
    public CacheEventListener createCacheEventListener(Properties properties) {
        return new LoggingCacheEventListener(properties);
    }
}
