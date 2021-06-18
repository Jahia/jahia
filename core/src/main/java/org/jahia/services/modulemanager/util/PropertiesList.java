/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
