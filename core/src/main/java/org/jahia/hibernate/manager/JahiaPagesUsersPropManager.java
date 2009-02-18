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

package org.jahia.hibernate.manager;

import org.jahia.hibernate.dao.JahiaPagesUsersPropDAO;
import org.jahia.hibernate.model.JahiaPagesUsersProp;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.Cache;
import org.jahia.services.pagesusers.PageUserProperty;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.ajax.gwt.client.data.GWTJahiaPageUserProperty;
import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.List;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 11:45:16
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPagesUsersPropManager {
    
    private static final transient Logger logger = Logger
            .getLogger(JahiaPagesUsersPropManager.class);
    
    private JahiaPagesUsersPropDAO jahiaPagesUsersPropDAO = null;
    private CacheService cacheService = null;
    private static final String PAGESUSERSMANAGER_CACHENAME = "PagesUsersManagerCache";
    private static final String PAGESUSERSPROPERTY_KEYPREFIX = "PagesUsersPropertyPrefix";


    public JahiaPagesUsersPropDAO getJahiaPagesUsersPropDAO() {
        return jahiaPagesUsersPropDAO;
    }

    public void setJahiaPagesUsersPropDAO(JahiaPagesUsersPropDAO jahiaPagesUsersPropDAO) {
        this.jahiaPagesUsersPropDAO = jahiaPagesUsersPropDAO;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void savePageUserProperty(PageUserProperty targetProperty) {
        Cache cache = cacheService.getCache(PAGESUSERSMANAGER_CACHENAME);
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(PAGESUSERSMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (cache != null) {
            cache.remove(buildCacheKey(targetProperty.getPageId(), targetProperty.getPrincipalKey(), targetProperty.getPrincipalType()));
        }
        Integer pageID = new Integer(targetProperty.getPageId());
        String principalKey = targetProperty.getPrincipalKey();
        String principalType = targetProperty.getPrincipalType();
        String propType = targetProperty.getPropType();
        String name = targetProperty.getName();
        String value = targetProperty.getValue();
        jahiaPagesUsersPropDAO.savePageUserProperty(pageID, principalKey, principalType, propType, name, value);
    }

    public void deletePageUserProperties(PageUserProperty targetProperty) {
        Cache cache = getCache();
        if (cache != null) {
            cache.remove(buildCacheKey(targetProperty.getPageId(), targetProperty.getPrincipalKey(), targetProperty.getPrincipalType()));
        }
        Integer pageID = new Integer(targetProperty.getPageId());
        String principalKey = targetProperty.getPrincipalKey();
        String principalType = targetProperty.getPrincipalType();
        jahiaPagesUsersPropDAO.deleteProperties(pageID, principalKey, principalType);
    }

    public void deletePageUserPropertiesByPropType(String propType) {
        Cache cache = getCache();
        if (cache != null) {
            // flush all cash. TO DO remove only entries with "propType"
            cache.flush();
        }
        jahiaPagesUsersPropDAO.deleteProperties(propType);
    }

    public PageUserProperty getPageUserProperty(Integer pageId, String principalKey, String principalType, String propType, String propName) {
        Map properties = null;
        Cache cache = getCache();
        if (cache != null) {
            properties = (Map) cache.get(buildCacheKey(pageId, principalKey, principalType));
        }
        JahiaPagesUsersProp objects = jahiaPagesUsersPropDAO.getPageUserProperty(new Integer(pageId), principalKey, principalType, propType, propName);
        return updatePageUserProp(properties, objects);
    }

    public Map<String, GWTJahiaPageUserProperty> getPageUsersProperties(int pageId, String principalKey, String principalType, String propType) {
        Map properties = null;
        Cache cache = getCache();
        if (cache != null) {
            properties = (Map) cache.get(buildCacheKey(pageId, principalKey, principalType));
        }

        if (properties == null) {
            final List pageUserProperties = jahiaPagesUsersPropDAO.getPageUserProperties(new Integer(pageId), principalKey, principalType, propType);
            properties = new FastHashMap(53);
            for (Object pageUserProperty : pageUserProperties) {
                JahiaPagesUsersProp objects = (JahiaPagesUsersProp) pageUserProperty;
                updatePageUserProp(properties, objects);
            }
            if (cache != null) {
                cache.put(buildCacheKey(pageId, principalKey, principalType), properties);
            }
        }
        return properties;
    }

    private PageUserProperty updatePageUserProp(Map properties, JahiaPagesUsersProp objects) {
        int foundPageId = objects.getPageId();
        String foundPrincipalKey = objects.getPrincipalKey();
        String foundPrincipalType = objects.getPrincipalType();
        String foundPropType = objects.getPropType();
        String foundName = objects.getName();
        String foundValue = objects.getValue();
        if (!properties.containsKey(foundName)) {
            properties.put(foundName, new PageUserProperty(foundPageId, foundPrincipalKey, foundPrincipalType, foundPropType, foundName));
        }
        PageUserProperty pageUserProperty = (PageUserProperty) properties.get(foundName);
        pageUserProperty.setValue(foundValue);
        return pageUserProperty;
    }


    private Cache getCache() {
        Cache cache = cacheService.getCache(PAGESUSERSMANAGER_CACHENAME);
        if (cache == null) {
            try {
                cache = cacheService.createCacheInstance(PAGESUSERSMANAGER_CACHENAME);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return cache;
    }

    private String buildCacheKey(int pageId, String principalKey, String principalType) {
        return PAGESUSERSPROPERTY_KEYPREFIX + pageId + "|" + principalKey + "|" + principalType;
    }
}
