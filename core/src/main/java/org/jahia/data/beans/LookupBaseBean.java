/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.data.beans;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Acts as a proxy for calling single argument methods from the JSP scope.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class LookupBaseBean<K,V> implements Map<K,V> {

    public void clear() {
        throw new UnsupportedOperationException();

    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public abstract V get(Object key);

    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    public V put(K arg0, V arg1) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> arg0) {
        throw new UnsupportedOperationException();

    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }
}
