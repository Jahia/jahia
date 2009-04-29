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
package org.jahia.services.cache.dummy;

import org.jahia.services.cache.CacheProvider;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.CacheImplementation;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.settings.SettingsBean;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 27 août 2008
 * Time: 14:21:22
 * To change this template use File | Settings | File Templates.
 */
public class DummyCacheProvider implements CacheProvider {
    public void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void enableClusterSync() throws JahiaInitializationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stopClusterSync() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void syncClusterNow() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isClusterCache() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public CacheImplementation newCacheImplementation(String name) {
        return new DummyCacheImpl(name);  //To change body of implemented methods use File | Settings | File Templates.
    }
}
