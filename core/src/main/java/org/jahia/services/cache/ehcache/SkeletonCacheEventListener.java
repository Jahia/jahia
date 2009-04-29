/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheException;

import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: rincevent Date: 7 févr. 2008 Time: 11:08:47 To change this template use File |
 * Settings | File Templates.
 */
public class SkeletonCacheEventListener implements CacheEventListener {
    private static Category logger = Logger.getLogger(SkeletonCacheEventListener.class);
    public SkeletonCacheEventListener(Properties properties) {
    }

    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyElementExpired(Ehcache ehcache, Element element) {
        logger.info("Element "+element.getKey()+" has expired");
    }

    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyRemoveAll(Ehcache ehcache) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object clone() throws CloneNotSupportedException {
        return null;//To change body of implemented methods use File | Settings | File Templates.
    }
}
