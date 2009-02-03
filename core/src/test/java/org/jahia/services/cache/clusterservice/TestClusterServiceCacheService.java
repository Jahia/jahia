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

package org.jahia.services.cache.clusterservice;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.services.cache.AbstractTestCacheService;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.cluster.JGroupsClusterService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FilePathResolver;
import org.jahia.utils.PathResolver;

/**
 * User: Serge Huber
 * Date: Jul 27, 2005
 * Time: 2:43:58 PM
 * Copyright (C) Jahia Inc.
 */
public class TestClusterServiceCacheService extends AbstractTestCacheService {

    private static final String PROP_FILE = "src"+File.separator+
                    "test"+File.separator+
                    "etc"+File.separator+
                    "caches" + File.separator +
                    "jahia_clusterservicecachetest.properties";

    private ClusterService clusterService;

    protected void setUp() throws Exception {
        super.setUp();
        // DOMConfigurator.configureAndWatch("log4j.xml");

        clusterService = new JGroupsClusterService();

        cacheService = CacheFactory.getInstance();
        PathResolver pathResolver = new FilePathResolver();
        File jahiaCacheProperties = new File(PROP_FILE);
        System.out.println("Loading Jahia configuration from " + jahiaCacheProperties.getAbsoluteFile().toString());
        SettingsBean settingsBean = new SettingsBean(pathResolver, jahiaCacheProperties.getAbsoluteFile().toString(), "", Jahia.getBuildNumber());
        settingsBean.load();

        clusterService.setSettingsBean(settingsBean);
        clusterService.start();

        ClusterServiceCacheProvider cacheProvider = new ClusterServiceCacheProvider();
        cacheProvider.setClusterService(clusterService);
        Map providers = new HashMap(1);
        providers.put(CacheFactory.DEFAULT_CACHE,cacheProvider);
        cacheService.setCacheProviders(providers);
        cacheService.setSettingsBean(settingsBean);
        cacheService.start();
        cacheService.enableClusterSync();
    }

    protected synchronized void tearDown() throws Exception {
        super.tearDown();
        cacheService.syncClusterNow();
        wait(5000);
        cacheService.stopClusterSync();
        cacheService.stop();
        clusterService.stop();
    }
}
