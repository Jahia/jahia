package org.jahia.bundles.config;

import java.util.*;

public class ConfigUtil {
    private ConfigUtil() {
    }

    public static Map<String, String> getMap(Dictionary<String, ?> d) {
        Map<String, String> m = new HashMap<>();
        if (d != null) {
            Enumeration<String> en = d.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement();
                if (!key.startsWith("felix.") && !key.startsWith("service.")) {
                    m.put(key, d.get(key).toString());
                }
            }
        }
        return m;
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
