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
package org.jahia.bundles.config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Structured object used to manipulate properties. Allows to create list and sub-structures
 */
public interface PropertiesValues {

    /**
     * Get the list of all keys at this level
     *
     * @return
     */
    Set<String> getKeys();

    /**
     * Gets a property as a string
     * @param name
     * @return
     */
    String getProperty(String name);

    /**
     * Gets a property as a boolean
     * @param name
     * @return
     */
    Boolean getBooleanProperty(String name);

    /**
     * Gets a property as an integer
     * @param name
     * @return
     */
    Integer getIntegerProperty(String name);

    /**
     * Gets a property as a byte array
     * @param name
     * @return
     */
    byte[] getBinaryProperty(String name);

    /**
     * Sets a string property
     * @param name
     * @param value
     */
    void setProperty(String name, String value);

    /**
     * Sets a boolean property
     * @param name
     * @param value
     */
    void setBooleanProperty(String name, boolean value);

    /**
     * Sets an integer property
     * @param name
     * @param value
     */
    void setIntegerProperty(String name, int value);

    /**
     * Sets a byte array property
     * @param name
     * @param data
     */
    void setBinaryProperty(String name, byte[] data);

    /**
     * Removes and return a single property
     * @param name
     */
    String removeProperty(String name);

    /**
     * Gets a list structure for the specified name
     * It will create properties list xxx.name[0],xxx.name[1],...
     * @param name
     * @return
     */
    PropertiesList getList(String name);

    /**
     * Gets a sub-structure for the specified name
     * It will create properties list xxx.name.prop1,xxx.name.prop2,...
     * @param name
     * @return
     */
    PropertiesValues getValues(String name);

    /**
     * Remove the property, lists and sub-structure with the specified name
     * @param name
     */
    void remove(String name);

    /**
     * Convert to JSON object
     * @return
     * @throws JSONException
     */
    JSONObject toJSON() throws JSONException;

    /**
     * Import from JSON array
     * @return
     * @throws JSONException
     */
    void updateFromJSON(JSONObject array) throws JSONException;

}
