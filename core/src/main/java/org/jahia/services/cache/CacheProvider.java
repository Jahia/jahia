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

import org.jahia.settings.SettingsBean;
import org.jahia.exceptions.JahiaInitializationException;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 2:33:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CacheProvider {

    void init(SettingsBean settingsBean, CacheService cacheService) throws JahiaInitializationException;

    void shutdown();

    void enableClusterSync() throws JahiaInitializationException;

    void stopClusterSync();

    void syncClusterNow();

    boolean isClusterCache();

    CacheImplementation newCacheImplementation(String name);
}
