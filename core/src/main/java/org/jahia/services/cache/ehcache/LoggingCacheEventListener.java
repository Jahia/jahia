/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

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
