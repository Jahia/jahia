/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.preferences;

import org.jahia.registries.ServicesRegistry;
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
public class JahiaPreferencesQueryHelper {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesQueryHelper.class);

    public static String getSimpleSQL(String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:prefName", prefName);
        return convertToSQL(properties);
    }

    public static String getToolbarSQL(String name, String type) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:toolbarName", name);
        properties.put("j:type", type);
        return convertToSQL(properties);
    }

    public static String getPageSQL(int pid, String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", getPageUUID(pid));
        properties.put("j:prefName", prefName);
        return convertToSQL(properties);
    }


    public static String getPortletSQL(String portletName, String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:portletName", portletName);
        properties.put("j:prefName", prefName);
        return convertToSQL(properties);
    }

    public static String getPortletSQL(String portletName) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:portletName", portletName);
        return convertToSQL(properties);
    }

    public static String getBookmarkSQL(int pid) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("j:page", getPageUUID(pid));
        return convertToSQL(properties);
    }

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
     * Convert a map of String Object to an sqlConstraint
     *
     * @param propertiesMap Map of property key value
     * @return a string containing sql constraint to be used with JahiaPreferencesProvider
     */
    public static String convertToSQL(Map<String, Object> propertiesMap) {
        StringBuffer prefPath = new StringBuffer();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Iterator<?> propertiesIterator = propertiesMap.keySet().iterator();
            boolean isFirstProperty = true;
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                Object propertyValue = propertiesMap.get(propertyName);
                // add only if value is not null
                if (propertyValue != null) {
                    if (isFirstProperty) {
                        isFirstProperty = false;
                    } else {
                        prefPath.append(" and ");
                    }
                    prefPath.append("p.[").append(propertyName).append("]='").append(propertyValue.toString()).append("'");
                }
            }
        }
        return prefPath.toString();
    }

    /**
     * Convert a map of String String to an sqlConstraint
     *
     * @param propertiesMap Map of property key value
     * @return a string containing sql constraint to be used with JahiaPreferencesProvider
     */
    public static String convertToSQLPureStringProperties(Map<String, String> propertiesMap) {
        StringBuffer prefPath = new StringBuffer();
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            Iterator<?> propertiesIterator = propertiesMap.keySet().iterator();
            boolean isFirstProperty = true;
            while (propertiesIterator.hasNext()) {
                String propertyName = (String) propertiesIterator.next();
                String propertyvalue = propertiesMap.get(propertyName);
                // add only if value is not null
                if (propertyvalue != null) {
                    if (isFirstProperty) {
                        isFirstProperty = false;
                    } else {
                        prefPath.append(" and ");
                    }
                    prefPath.append("p.[").append(propertyName).append("]='").append(propertyvalue).append("'");
                }
            }
        }
        return prefPath.toString();
    }

}
