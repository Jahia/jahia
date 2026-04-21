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
package org.jahia.bundles.config;

import java.util.*;

public class ConfigUtil {
    private ConfigUtil() {
    }

    /**
     * Converts an OSGi configuration {@link Dictionary} into a flat {@code Map<String, String>}.
     *
     * <p>Internal OSGi/Felix keys (those starting with {@code felix.} or {@code service.}) are excluded.</p>
     *
     * <p>Multi-value properties (stored as arrays or {@link java.util.Collection}s by the OSGi runtime or
     * the Felix web console) are expanded into indexed entries using bracket notation, e.g.:
     * <pre>
     *   "myProp" = ["a", "b", "c"]  →  "myProp[0]"="a", "myProp[1]"="b", "myProp[2]"="c"
     * </pre>
     * This matches the flat representation used by {@link org.jahia.services.modulemanager.util.PropertiesManager}.
     * </p>
     *
     * @param dictionary the OSGi configuration dictionary, may be {@code null}
     * @return a flat string map of the configuration properties, never {@code null}
     */
    public static Map<String, String> getMap(Dictionary<String, ?> dictionary) {
        Map<String, String> m = new HashMap<>();
        if (dictionary != null) {
            Enumeration<String> en = dictionary.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                if (!key.startsWith("felix.") && !key.startsWith("service.")) {
                    Object value = dictionary.get(key);
                    if (value instanceof Object[]) {
                        putIndexedEntries(m, key, Arrays.asList((Object[]) value));
                    } else if (value instanceof Collection) {
                        putIndexedEntries(m, key, (Collection<?>) value);
                    } else if (value != null) {
                        m.put(key, value.toString());
                    }
                }
            }
        }
        return m;
    }

    private static void putIndexedEntries(Map<String, String> m, String key, Collection<?> items) {
        int i = 0;
        for (Object item : items) {
            if (item != null) {
                m.put(key + "[" + i + "]", item.toString());
            }
            i++;
        }
    }

    public static void flatten(Map<String, String> builder, String key, Map<String, ?> m) {
        for (Map.Entry<String, ?> entry : m.entrySet()) {
            flatten(builder, (key.isEmpty() ? key : (key + '.')) + entry.getKey(), entry.getValue());
        }
    }

    private static void flatten(Map<String, String> builder, String key, List<?> m) {
        int i = 0;
        for (Object value : m) {
            flatten(builder, key + '[' + (i++) + ']', value);
        }
    }

    private static void flatten(Map<String, String> builder, String key, Object value) {
        if (value instanceof Map) {
            flatten(builder, key, (Map<String, ?>) value);
        } else if (value instanceof List) {
            flatten(builder, key, (List<?>) value);
        } else if (value != null) {
            builder.put(key, value.toString());
        }
    }
}
