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

import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;

import java.net.URL;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 29 mars 2007
 * Time: 17:25:33
 * To change this template use File | Settings | File Templates.
 */
public class EhCacheProvider implements CacheProvider {

    final private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (EhCacheProvider.class);

    CacheManager cacheManager = null;
    private int groupsSizeLimit = 100;
    public EhCacheProvider() {
    }

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        URL url = getClass().getResource("/"+settingsBean.getEhCacheJahiaFile());
        cacheManager = CacheManager.create(url);
        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer("SimpleAgent");
        ManagementService.registerMBeans(cacheManager, mBeanServer, false, false, false, true);
    }

    public void shutdown() {
        logger.info("Shutting down cache provider, serializing to disk if active. Please wait...");
        long startTime = System.currentTimeMillis();
        cacheManager.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Cache provider shutdown completed in " + totalTime + "[ms]");
    }

    public void enableClusterSync() throws JahiaInitializationException {
    }

    public void stopClusterSync() {
    }

    public void syncClusterNow() {
    }

    public boolean isClusterCache() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CacheImplementation newCacheImplementation(String name) {
        return new EhCacheImpl(name, cacheManager, this);
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public int getGroupsSizeLimit() {
        return groupsSizeLimit;
    }

    public void setGroupsSizeLimit(int groupsSizeLimit) {
        this.groupsSizeLimit = groupsSizeLimit;
    }
}
