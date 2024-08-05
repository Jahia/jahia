/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A mocked (fake) implementation of a Map.
 * That implementation can be used internally to replace an in memory cache map.
 * Use with caution as putting some content in that map have no effect at all.
 *
 * @author Jerome Blanchard
 */
public class FakeMap<K, V> implements Map<K, V> {

    public FakeMap() {
    }

    @Override public void forEach(BiConsumer<? super K, ? super V> action) {
        //
    }

    @Override public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        //
    }

    @Override public boolean containsValue(Object value) {
        return false;
    }

    @Override public V get(Object key) {
        return null;
    }

    @Override public V getOrDefault(Object key, V defaultValue) {
        return defaultValue;
    }

    @Override public void clear() {
        //
    }

    @Override public Set<K> keySet() {
        return Set.of();
    }

    @Override public Collection<V> values() {
        return List.of();
    }

    @Override public Set<Map.Entry<K, V>> entrySet() {
        return Set.of();
    }

    @Override public V put(K key, V value) {
        return null;
    }

    @Override public void putAll(Map<? extends K, ? extends V> m) {
        //
    }

    @Override public boolean containsKey(Object key) {
        return false;
    }

    @Override public boolean isEmpty() {
        return true;
    }

    @Override public int size() {
        return 0;
    }

    @Override public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override public V remove(Object key) {
        return null;
    }

    @Override public boolean remove(Object key, Object value) {
        return false;
    }

    @Override public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override public V replace(K key, V value) {
        return null;
    }

    @Override public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return null;
    }
}
