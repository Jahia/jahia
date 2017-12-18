/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
