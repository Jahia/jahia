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
