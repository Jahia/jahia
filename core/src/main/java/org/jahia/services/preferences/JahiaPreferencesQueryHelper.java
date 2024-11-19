/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.preferences;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 *
 * User: jahia
 * Date: 11 mars 2009
 * Time: 15:06:47
 */
public class JahiaPreferencesQueryHelper {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPreferencesQueryHelper.class);

    public static String getSimpleSQL(String prefName) {
        Map<String, Object> properties = new HashMap<String, Object>();
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

    /**
     * Convert a map of String Object to an sqlConstraint
     *
     * @param propertiesMap Map of property key value
     * @return a string containing sql constraint to be used with JahiaPreferencesProvider
     */
    public static String convertToSQL(Map<String, Object> propertiesMap) {
        StringBuilder prefPath = new StringBuilder();
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
        StringBuilder prefPath = new StringBuilder();
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
