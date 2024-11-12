/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * Structured object used to manipulate properties. Allows to create list and sub-structures
 */
public interface PropertiesValues {

    /**
     * Get the list of all keys at this level
     *
     * @return list of keys
     */
    Set<String> getKeys();

    /**
     * Gets a property as a string
     * @param name property name
     * @return value
     */
    String getProperty(String name);

    /**
     * Gets a property as a boolean
     * @param name property name
     * @return value
     */
    Boolean getBooleanProperty(String name);

    /**
     * Gets a property as an integer
     * @param name property name
     * @return value
     */
    Integer getIntegerProperty(String name);

    /**
     * Gets a property as a byte array
     * @param name property name
     * @return value
     */
    byte[] getBinaryProperty(String name);

    /**
     * Sets a string property
     * @param name property name
     * @param value the value
     */
    void setProperty(String name, String value);

    /**
     * Sets a boolean property
     * @param name property name
     * @param value the value
     */
    void setBooleanProperty(String name, boolean value);

    /**
     * Sets an integer property
     * @param name property name
     * @param value the value
     */
    void setIntegerProperty(String name, int value);

    /**
     * Sets a byte array property
     * @param name property name
     * @param data the value
     */
    void setBinaryProperty(String name, byte[] data);

    /**
     * Removes and return a single property
     * @param name property name
     * @return value
     */
    String removeProperty(String name);

    /**
     * Gets a list structure for the specified name
     * It will create properties list xxx.name[0],xxx.name[1],...
     * @param name property name
     * @return value
     */
    PropertiesList getList(String name);

    /**
     * Gets a sub-structure for the specified name
     * It will create properties list xxx.name.prop1,xxx.name.prop2,...
     * @param name property name
     * @return value
     */
    PropertiesValues getValues(String name);

    /**
     * Remove the property, lists and sub-structure with the specified name
     * @param name property name
     */
    void remove(String name);

    Map<String, Object> getStructuredMap();

    /**
     * Convert to JSON object
     * @return json
     * @throws JSONException parsing exception
     */
    JSONObject toJSON() throws JSONException;

    /**
     * Import from JSON array
     * @param array json value
     * @throws JSONException parsing exception
     */
    void updateFromJSON(JSONObject array) throws JSONException;

}
