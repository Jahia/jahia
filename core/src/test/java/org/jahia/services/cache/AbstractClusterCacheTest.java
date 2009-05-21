/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.cache;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.xml.DOMConfigurator;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FilePathResolver;
import org.jahia.utils.PathResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * User: Serge Huber
 * Date: Jul 18, 2005
 * Time: 9:47:16 AM
 * Copyright (C) Jahia Inc.
 */
public abstract class AbstractClusterCacheTest extends TestCase {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (AbstractClusterCacheTest.class);

    private final static String springRelativePath =  "src"+File.separator+
                    "test"+File.separator+
                    "etc"+File.separator+
                    "caches";
    private static final String CACHE_NAME = "clusterCache";
    protected ApplicationContext context;

    protected CacheService cacheService;
    protected Cache cache;

    protected long wakeupTime = 2000;
    protected long shutdownTime = 2000;

    private static final String PROP_FILE = "src"+File.separator+
                    "test"+File.separator+
                    "etc"+File.separator+
                    "caches" + File.separator +
                    "jahia_clustercachetest.properties";

    private static final String LOG4J_FILE = "src"+File.separator+
                    "test"+File.separator+
                    "etc"+File.separator+
                    "caches" + File.separator +
                    "log4j_clustercachetest.xml";

    protected synchronized void setUp() throws Exception {
        super.setUp();
        DOMConfigurator.configureAndWatch(LOG4J_FILE);
        PathResolver pathResolver = new FilePathResolver();
        File jahiaCacheProperties = new File(PROP_FILE);
        logger.info("Loading Jahia configuration from " + jahiaCacheProperties.getAbsoluteFile().toString());
        SettingsBean settingsBean = new SettingsBean(pathResolver, jahiaCacheProperties.getAbsoluteFile().toString(), "", Jahia.getBuildNumber());
        settingsBean.load();

        try {
            final String springPath = pathResolver.resolvePath(springRelativePath);
            String[] paths = {"file:" + springPath + File.separator + "applicationContext-*.xml"};
            context = new FileSystemXmlApplicationContext(paths);
        } catch (Exception e) {
            logger.error("Cannot load spring initialisation file ",e);
            String[] paths = {"spring/applicationContext*.xml"};
            context = new ClassPathXmlApplicationContext(paths);
        }

        cacheService = (CacheService) context.getBean(CacheService.class.getName());
        cacheService.setSettingsBean(settingsBean);
        cacheService.start();

        cache = cacheService.createCacheInstance(CACHE_NAME);

        cacheService.enableClusterSync();
        logger.info("Waiting for wake-up of all nodes...");
        wait(wakeupTime); // give some time for other node to wake-up
    }

    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        logger.info("Waiting for shutdown of all nodes...");
        wait(shutdownTime); // give some time for other node to finish processing.
        cacheService.stopClusterSync();
        cacheService.stop();
    }

    protected synchronized boolean waitForValue(Object value, int maxLoops, long checkInterval) throws InterruptedException {
        String cacheKey = null;
        int loops = 0;
        while (cacheKey == null) {
            cacheKey = (String) cache.get(value);
            wait(checkInterval);
            loops++;
            if (loops == maxLoops) {
                return false;
            }
        }
        return true;
    }

}
