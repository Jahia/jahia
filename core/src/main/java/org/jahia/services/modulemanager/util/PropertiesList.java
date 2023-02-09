/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * List of values
 */
public interface PropertiesList {
    /**
     * Gets the size if this list
     * @return
     */
    int getSize();

    /**
     * Gets the string value at the specified index
     * @param index property index
     * @return value
     */
    String getProperty(int index);

    /**
     * Modifies the property at the specified index
     * @param index property index
     * @param value the value
     */
    void setProperty(int index, String value);

    /**
     * Adds a new string property
     * @param value the value
     */
    void addProperty(String value) ;

    /**
     * Gets the boolean value at the specified index
     * @param index property index
     * @return value
     */
    Boolean getBooleanProperty(int index);

    /**
     * Modifies the property at the specified index
     * @param index property index
     * @param value the value
     */
    void setBooleanProperty(int index, boolean value);

    /**
     * Adds a new boolean property
     * @param value the value
     */
    void addBooleanProperty(boolean value) ;

    /**
     * Gets the integer value at the specified index
     * @param index property index
     * @return value
     */
    Integer getIntegerProperty(int index);

    /**
     * Modifies the property at the specified index
     * @param index property index
     * @param value the value
     */
    void setIntegerProperty(int index, int value);

    /**
     * Adds a new integer property
     * @param value the value
     */
    void addIntegerProperty(int value) ;

    /**
     * Gets the byte array value at the specified index
     * @param index property index
     * @return value
     */
    byte[] getBinaryProperty(int index);

    /**
     * Modifies the property at the specified index
     * @param index property index
     * @param value the value
     */
    void setBinaryProperty(int index, byte[] value);

    /**
     * Adds a new byte array property
     * @param value the value
     */
    void addBinaryProperty(byte[] value);

    /**
     * Get structured values at specified index
     * @param index property index
     * @return properties values
     */
    PropertiesValues getValues(int index);

    /**
     * Adds a new structured values in the list
     * @return properties values
     */
    PropertiesValues addValues();

    /**
     * Get sub list at specified index
     * @param index property index
     * @return properties list
     */
    PropertiesList getList(int index);

    /**
     * Adds a new sub list in the list
     * @return properties list
     */
    PropertiesList addList();

    List<Object> getStructuredList();

    /**
     * Convert to JSON array
     * @return json
     * @throws JSONException parsing exception
     */
    JSONArray toJSON() throws JSONException;

    /**
     * Import from JSON array
     * @param array json value
     * @throws JSONException parsing exception
     */
    void updateFromJSON(JSONArray array) throws JSONException;
}
