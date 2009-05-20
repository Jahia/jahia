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
