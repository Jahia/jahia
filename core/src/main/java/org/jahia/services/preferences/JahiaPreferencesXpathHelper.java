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
package org.jahia.services.preferences;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.ContentPage;
import org.jahia.exceptions.JahiaException;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 11 mars 2009
 * Time: 15:06:47
 */
public class JahiaPreferencesXpathHelper {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesXpathHelper.class);

    public static String getSimpleXpath(String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:prefName", prefName);
        return convetToXpath(properties);
    }

    public static String getToolbarXpath(String name, String type) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:toolbarName", name);
        properties.put("j:type", type);
        return convetToXpath(properties);
    }

    public static String getPageXpath(int pid, String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", getPageUUID(pid));
        properties.put("j:prefName", prefName);
        return convetToXpath(properties);
    }

    public static String getLayoutmanagerXpath(int pid, String windowId) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", pid);
        properties.put("j:windowId", windowId);
        return convetToXpath(properties);
    }

    public static String getLayoutmanagerXpath(int pid) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", getPageUUID(pid));
        return convetToXpath(properties);
    }

    public static String getPortletXpath(String portletName, String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:portletName", portletName);
        properties.put("j:prefName", prefName);
        return convetToXpath(properties);
    }

    public static String getPortletXpath(String portletName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:portletName", portletName);
        return convetToXpath(properties);
    }

    public static String getBookmarkXpath(int pid) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", getPageUUID(pid));
        return convetToXpath(properties);
    }

    /**
     * Get page uuid
     *
     * @param pid
     * @return
     */
    private static String getPageUUID(int pid) {
        try {
            ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pid, false);
            return page.getUUID();
        } catch (JahiaException e) {
            logger.error(e, e);
            return null;
        }
    }

    /**
     * Convert a map to an xpath
     *
     * @param propertiesMap
     * @return
     */
    public static String convetToXpath(Map<String, Object> propertiesMap) {
        StringBuffer prefPath = new StringBuffer();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Iterator<?> propertiesIterator = propertiesMap.keySet().iterator();
            boolean isFirstProperty = true;
            boolean hasProperties = false;
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                String propertyvalue = propertiesMap.get(propertyName).toString();
                // add only if value is not null
                if (propertyvalue != null) {
                    if (isFirstProperty) {
                        prefPath.append("[");
                        isFirstProperty = false;
                    } else {
                        prefPath.append(" and ");
                    }
                    hasProperties = true;
                    prefPath.append("@" + propertyName + "='" + propertyvalue + "'");
                }
            }

            //close
            if (hasProperties) {
                prefPath.append("]");
            }
        }
        return prefPath.toString();
    }

    /**
     * @param propertiesMap
     * @return
     */
    public static String convetToXpath2(Map<String, String> propertiesMap) {
        StringBuffer prefPath = new StringBuffer();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Iterator<?> propertiesIterator = propertiesMap.keySet().iterator();
            boolean isFirstProperty = true;
            boolean hasProperties = false;
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                String propertyvalue = propertiesMap.get(propertyName).toString();
                // add only if value is not null
                if (propertyvalue != null) {
                    if (isFirstProperty) {
                        prefPath.append("[");
                        isFirstProperty = false;
                    } else {
                        prefPath.append(" and ");
                    }
                    hasProperties = true;
                    prefPath.append("@" + propertyName + "='" + propertyvalue + "'");
                }
            }

            //close
            if (hasProperties) {
                prefPath.append("]");
            }
        }
        return prefPath.toString();
    }

}
