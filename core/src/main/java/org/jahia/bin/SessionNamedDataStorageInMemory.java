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
package org.jahia.bin;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage implementation that keeps session data in memory.
 */
public class SessionNamedDataStorageInMemory<T> extends SessionNamedDataStorageSupport<T> implements SessionNamedDataStorage<T> {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, T>> sessionByID = new ConcurrentHashMap<String, ConcurrentHashMap<String, T>>();

    @Override
    public void put(String sessionID, String name, T data) {
        ConcurrentHashMap<String, T> newDataByName = new ConcurrentHashMap<String, T>();
        ConcurrentHashMap<String, T> dataByName = sessionByID.putIfAbsent(sessionID, newDataByName);
        if (dataByName == null) {
            // There was no existing entry corresponding to this session.
            dataByName = newDataByName;
        }
        dataByName.put(name, data);
    }

    @Override
    public T get(String sessionID, String name) {
        ConcurrentHashMap<String, T> dataByName = sessionByID.get(sessionID);
        if (dataByName == null) {
            return null;
        }
        return dataByName.get(name);
    }

    @Override
    public T getRequired(String sessionID, String name) {
        T data = get(sessionID, name);
        if (data == null) {
            throw new IllegalArgumentException(MessageFormat.format("No session data found, session ID: {0}, name: {1}", sessionID, name));
        }
        return data;
    }

    @Override
    public void remove(String sessionID, String name) {
        ConcurrentHashMap<String, T> dataByName = sessionByID.get(sessionID);
        if (dataByName == null || dataByName.remove(name) == null) {
            throw new IllegalArgumentException(MessageFormat.format("No session data found, session ID: {0}, name: {1}", sessionID, name));
        }
    }

    @Override
    public void removeIfExists(String sessionID) {
        sessionByID.remove(sessionID);
    }
}
