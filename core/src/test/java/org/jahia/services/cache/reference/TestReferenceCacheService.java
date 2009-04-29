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
package org.jahia.services.cache.reference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.services.cache.AbstractTestCacheService;
import org.jahia.services.cache.CacheFactory;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.FilePathResolver;
import org.jahia.utils.PathResolver;

public class TestReferenceCacheService extends AbstractTestCacheService {
	private static final String PROP_FILE = "src"+File.separator+
	"test"+File.separator+
	"etc"+File.separator+
	"caches" + File.separator +
	"jahia_referencecachetest.properties";

	protected void setUp() throws Exception {
		super.setUp();
//		DOMConfigurator.configureAndWatch("log4j.xml");

		cacheService = CacheFactory.getInstance();
		PathResolver pathResolver = new FilePathResolver();
		File jahiaCacheProperties = new File(PROP_FILE);
		System.out.println("Loading Jahia configuration from " + jahiaCacheProperties.getAbsoluteFile().toString());
		SettingsBean settingsBean = new SettingsBean(pathResolver, jahiaCacheProperties.getAbsoluteFile().toString(), "", Jahia.getBuildNumber());
		settingsBean.load();

		ReferenceCacheProvider cacheProvider = new ReferenceCacheProvider();
		Map providers = new HashMap(1);
        providers.put(CacheFactory.DEFAULT_CACHE,cacheProvider);
        cacheService.setCacheProviders(providers);
		cacheService.setSettingsBean(settingsBean);
		cacheService.start();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		cacheService.stop();
	}

}
