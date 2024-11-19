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
package org.jahia.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic collection utilities.
 *
 * @author Sergiy Shyrkov
 */
public final class CollectionUtils {

    /**
     * Returns a bounded in size LRU map.
     *
     * @param maxSize the maximum map size
     * @return a bounded in size LRU map
     */
    public static <K, V> Map<K, V> lruCache(final int maxSize) {
        return lruCache(16, maxSize);
    }

    /**
     * Returns a bounded in size LRU map.
     *
     * @param initialCapacity the initial capacity of the map
     * @param maxSize the maximum map size
     * @return a bounded in size LRU map
     */
    public static <K, V> Map<K, V> lruCache(final int initialCapacity, final int maxSize) {
        return new LinkedHashMap<K, V>(initialCapacity, 0.75f, true) {

            private static final long serialVersionUID = 6773642631901363425L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

    private CollectionUtils() {
        super();
    }
}
