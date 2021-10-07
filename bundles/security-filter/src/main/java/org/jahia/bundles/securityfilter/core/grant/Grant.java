package org.jahia.bundles.securityfilter.core.grant;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public interface Grant {

    boolean matches(Map<String, Object> query);

    static <T> boolean anyMatch(Collection<T> values, Predicate<T> predicate) {
        if (values.isEmpty()) {
            return true;
        }
        for (T value : values) {
            if (predicate.test(value)) {
                return true;
            }
        }
        return false;
    }

    static <T> boolean noneMatch(Collection<T> values, Predicate<T> predicate) {
        if (values.isEmpty()) {
            return true;
        }
        for (T value : values) {
            if (predicate.test(value)) {
                return false;
            }
        }
        return true;
    }
}
