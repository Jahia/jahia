/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility for provisioning script
 */
public final class ProvisioningScriptUtil {
    private static final Logger logger = LoggerFactory.getLogger(ProvisioningScriptUtil.class);

    private ProvisioningScriptUtil() {
    }

    private static final Pattern PATTERN = Pattern.compile("^[a-z][a-z0-9+.-]+:.*");
    /**
     * Get resource
     * @param key key
     * @param executionContext executioncontext
     * @return resource
     * @throws IOException exception
     */
    public static Resource getResource(String key, ExecutionContext executionContext) throws IOException {
        if (PATTERN.matcher(key).matches()) {
            return new UrlResource(key);
        } else {
            Map<String, Resource> resources = (Map<String, Resource>) executionContext.getContext().get("resources");
            if (resources != null) {
                return resources.get(key);
            }
        }
        throw new IOException(MessageFormat.format("Resource not found, {0}", key));
    }

    /**
     * Convert an entry with a list of string values on one main field, to a list of entries, one entry per value.
     * Generated entries will contain all other fields from the main entry.
     * If a value is itself an object, every fields here will be copied to the generated entry.
     *
     * Example :
     * { key: [ 'a', 'b', 'c' ], option: 'v1' } -> [ { key: 'a', option: 'v1' }, { key: 'b', option: 'v1' }, { key: 'c', option: 'v1' }]
     * { key: [ 'a', { subkey: 'b', option: 'v2' } ], option: v1 } -> [ { key: 'a', option: 'v1' }, { key: 'b', option: 'v2' } ]
     *
     * @param entry The full entry
     * @param key The main key that can contain a list
     * @param subkey Key used as main key in sub entries ( xx: [
     * @return list of entries
     */
    public static List<Map<String, Object>> convertToList(Map<String, Object> entry, String key, String subkey) {
        Object value = entry.get(key);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof String) {
            // Simple case - value is a single-value, just return it as singleton entry
            return Collections.singletonList(entry);
        } else if (value instanceof List) {
            // Value is a list - items can be string, or sub-entries with different options
            List<Map<String, Object>> result = new ArrayList<>();
            List<?> l = (List<?>) value;
            for (Object item : l) {
                Map<String,Object> m = new HashMap<>(entry);
                if (item instanceof String) {
                    // This item is a string - build an entry based on root entry, with this value as the main key
                    m.put(key, item);
                } else if (item instanceof Map) {
                    // This item is a sub-entry - build an entry based on root entry, override with sub entry, and use the sub-key field for main key
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    m.put(key, itemMap.remove(subkey));
                    m.putAll(itemMap);
                }
                result.add(m);
            }
            return result;
        }
        throw new IllegalArgumentException(key + " parameter must be a list or a single-value");
    }

    /**
     * Evaluate a groovy condition
     *
     * @param condition groovy expression
     * @return true or false
     */
    public static boolean evalCondition(String condition) {
        try {
            return (boolean) ScriptEngineUtils.getInstance().scriptEngine("groovy").eval(condition);
        } catch (Exception e) {
            logger.error("Cannot eval {} to boolean", condition, e);
        }
        return false;
    }
}

