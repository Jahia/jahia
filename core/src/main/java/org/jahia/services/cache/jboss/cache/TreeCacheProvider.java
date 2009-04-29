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
package org.jahia.services.cache.jboss.cache;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.settings.SettingsBean;

public class TreeCacheProvider implements CacheProvider {
    
    public void enableClusterSync() throws JahiaInitializationException {
        
    }

    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        // do nothing
    }

    public boolean isClusterCache() {
        return true;
    }

    public CacheImplementation newCacheImplementation(String name) {
        return new TreeCacheImpl(name);
    }

    public void shutdown() {
        // TODO Auto-generated method stub

    }

    public void stopClusterSync() {
        // TODO Auto-generated method stub
        
    }

    public void syncClusterNow() {
        // TODO Auto-generated method stub
        
    }

}
