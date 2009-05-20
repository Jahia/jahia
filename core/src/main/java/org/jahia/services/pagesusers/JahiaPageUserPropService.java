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
package org.jahia.services.pagesusers;

import org.jahia.services.JahiaService;
import org.jahia.services.cache.CacheService;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaPagesUsersPropManager;

import java.util.Map;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 13:59:46
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPageUserPropService extends JahiaService {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPageUserPropService.class);
    private JahiaPagesUsersPropManager pagesUsersPropManager;
    private CacheService cacheService;
    private static JahiaPageUserPropService instance;
    public static synchronized JahiaPageUserPropService getInstance() {
        if (instance == null) {
            instance = new JahiaPageUserPropService();
        }
        return instance;
    }


    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JahiaPagesUsersPropManager getPagesUsersPropManager() {
        return pagesUsersPropManager;
    }

    public Map getPageUserProperties(int pageId, String principalKey,String principalType,String propType) {
        return pagesUsersPropManager.getPageUsersProperties(pageId,principalKey,principalType,propType);
    }

    public PageUserProperty getPageUserProperty(int pageId, String principalKey,String principalType,String propType,String propName) {
        return pagesUsersPropManager.getPageUserProperty(pageId,principalKey,principalType,propType,propName);
    }

    public void savePageUserProperty(int pageId, String principalKey,String principalType,String propType,String name, String value) {
        PageUserProperty pageUserProp = new PageUserProperty(pageId,principalKey,principalType,propType,name);
        pageUserProp.setValue(value);
        pagesUsersPropManager.savePageUserProperty(pageUserProp);
    }

    public void deletePageUserPropertiesByPropType(String propType) {
        pagesUsersPropManager.deletePageUserPropertiesByPropType(propType);
    }

    public void setPagesUsersPropManager(JahiaPagesUsersPropManager pagesUsersPropManager) {
        this.pagesUsersPropManager = pagesUsersPropManager;
    }

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the PageUserProperty Service ...");

    }

    public synchronized void stop() {
        logger.debug("** Stop the PageUserProperty Service ...");

    }
}
