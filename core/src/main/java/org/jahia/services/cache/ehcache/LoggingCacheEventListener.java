/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheException;
import org.slf4j.Logger;

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
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LoggingCacheEventListener.class);
    private final Properties properties;

    public LoggingCacheEventListener(Properties properties) {
        this.properties = properties;
    }

    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        if (testLogging("removed")) {
            logger.debug(ehcache.getName() + ": Element " + element.getObjectKey() + " removed.");
        }
    }

    private boolean testLogging(String type) {
        return logger.isDebugEnabled() && (properties==null || properties.size()==0 || properties.containsKey(type));
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        if (testLogging("put")) {
            logger.debug(ehcache.getName() +": Element " + element.getObjectKey() + " put.");
        }
    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        if (testLogging("updated")) {
            logger.debug(ehcache.getName() +": Element " + element.getObjectKey() + " updated.");
        }
    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        if (testLogging("expired")) {
            logger.info(ehcache.getName() +": Element " + element.getObjectKey() + " expired.");
        }
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        if (testLogging("evicted")) {
            logger.debug(ehcache.getName() +": Element " + element.getObjectKey() + " evicted.");
        }
    }

    public void notifyRemoveAll(Ehcache ehcache) {
        if (testLogging("removeAll")) {
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
        throw new CloneNotSupportedException();
    }
}
