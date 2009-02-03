/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
        URL url = getClass().getResource("/ehcache-jahia.xml");
        cacheManager = CacheManager.create(url);        
        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer("SimpleAgent");
        ManagementService.registerMBeans(cacheManager, mBeanServer, false, false, false, true);
    }

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
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
