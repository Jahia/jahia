/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

/**
 * Storage of named data objects belonging to a single HTTP session.
 */
public interface SessionNamedDataStorage<T> {

    /**
     * Put data belonging to specific HTTP session; overwrite in case an equally named data already exists in the storage
     * @param sessionID HTTP session ID
     * @param name Data name
     * @param data Data object
     */
    void put(String sessionID, String name, T data);

    /**
     * Get data belonging to specific HTTP session, by name
     * @param sessionID HTTP session ID
     * @param name Data name
     * @return Data object corresponding to the name, null if does not exist
     */
    T get(String sessionID, String name);

    /**
     * Get data belonging to specific HTTP session, by name; throw an exception if does not exist
     * @param sessionID HTTP session ID
     * @param name Data name
     * @return Data object corresponding to the name
     */
    T getRequired(String sessionID, String name);

    /**
     * Remove data belonging to specific HTTP session, by name; throw an exception if does not exist
     * @param sessionID HTTP session ID
     * @param name Data name
     */
    void remove(String sessionID, String name);

    /**
     * Remove all data belonging to specific HTTP session, if any.
     * @param sessionID HTTP session ID
     */
    void removeIfExists(String sessionID);
}
