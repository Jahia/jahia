package org.jahia.services.cache.ehcache;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheException;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * A very simple EHCache logging cache listeners that logs all the
 * cache events to Jahia's logs.
 *
 * @author loom
 *         Date: Sep 14, 2009
 *         Time: 3:54:36 PM
 */
public class LoggingCacheEventListener implements CacheEventListener {
    private static Category logger = Logger.getLogger(LoggingCacheEventListener.class);
    public LoggingCacheEventListener(Properties properties) {
    }

    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug(ehcache.getName() + ": Element " + element.getKey() + " removed.");
        }
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug(ehcache.getName() +": Element " + element.getKey() + " put.");
        }
    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        if (logger.isDebugEnabled()) {
            logger.debug(ehcache.getName() +": Element " + element.getKey() + " updated.");
        }
    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        if (logger.isInfoEnabled()) {
            logger.info(ehcache.getName() +": Element " + element.getKey() + " expired.");
        }
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        if (logger.isDebugEnabled()) {
            logger.debug(ehcache.getName() +": Element " + element.getKey() + " evicted.");
        }
    }

    public void notifyRemoveAll(Ehcache ehcache) {
        if (logger.isDebugEnabled()) {
            logger.debug(ehcache.getName() +": All elements removed!");
        }
    }

    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug(" Event listener disposed of.");
        }
    }

    public Object clone() throws CloneNotSupportedException {
        if (logger.isDebugEnabled()) {
            logger.debug("Event listener cloned.");
        }
        return this.clone();
    }
}
