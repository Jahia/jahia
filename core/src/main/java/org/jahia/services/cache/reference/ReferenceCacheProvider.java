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

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.settings.SettingsBean;

import java.util.HashMap;
import java.util.Map;

public class ReferenceCacheProvider implements CacheProvider {

    private int cacheMaxGroups = 10000;
    private Map cacheMaxGroupsTable = new HashMap();

    public void enableClusterSync() throws JahiaInitializationException {
	}

	public void init(SettingsBean settingsBean, CacheService cacheService)
			throws JahiaInitializationException {
        this.cacheMaxGroups = settingsBean.getCacheMaxGroups();
    }

	public boolean isClusterCache() {
		return false;
	}

	public CacheImplementation newCacheImplementation(String name) {
		return new ReferenceCacheImpl(name, cacheMaxGroups);
	}

	public void shutdown() {
	}

	public void stopClusterSync() {
	}

	public void syncClusterNow() {
	}

}
