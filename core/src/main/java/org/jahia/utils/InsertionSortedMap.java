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
 package org.jahia.utils;

import java.util.*;
import java.io.Serializable;


/**
 * <p>Title: Map implementation that respects the insertion order.</p>
 * <p>Description: This map implementation actually uses an List to
 * store the entry pairs.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 *
 */

public class InsertionSortedMap<K,V> extends AbstractMap<K,V> implements Serializable {

    private List<Map.Entry<K, V>> internalList = new ArrayList<Map.Entry<K, V>>();

    private class Entry<K,V> implements Map.Entry<K,V>, Serializable {

        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals (Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Map.Entry))
                return false;        
            if (obj != null && this.getClass() == obj.getClass()) {
                final Map.Entry<K,V> e2 = (Map.Entry<K,V>) obj;
                if ( (this.getKey() == null ?
                    e2.getKey() == null : this.getKey().equals(e2.getKey())) &&
                    (this.getValue() == null ? 
                    e2.getValue() == null : this.getValue().equals(e2.getValue()))) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
           return  ((this.getKey()==null   ? 0 : this.getKey().hashCode()) ^
                    (this.getValue()==null ? 0 : this.getValue().hashCode()));
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }

    public InsertionSortedMap() {
    }

    public InsertionSortedMap(Map<K, V> t) {
        // we must now build the key order based on the map we were passed.
        Iterator<? extends Map.Entry<K, V>> sourceEntryIter = t.entrySet().iterator();
        while (sourceEntryIter.hasNext()) {
            Map.Entry<K, V> curEntry = sourceEntryIter.next();
            internalList.add(curEntry);
        }
    }

    public Set<Map.Entry<K,V>> entrySet() {
        InsertionSortedSet<Map.Entry<K, V>> insertionSortedSet = new InsertionSortedSet<Map.Entry<K, V>>();
        insertionSortedSet.setInternalList(internalList);
        return insertionSortedSet;
    }

    public V put(K key,
                      V value)
        throws     UnsupportedOperationException ,
        ClassCastException ,
        IllegalArgumentException ,
        NullPointerException {
        int pos = findKey(key);
        if (pos == -1) {
            Entry<K, V> newEntry = new Entry<K, V>(key, value);
            internalList.add(newEntry);
            return null;
        } else {
            Map.Entry<K,V> existingEntry = internalList.get(pos);
            V oldValue = existingEntry.getValue();
            existingEntry.setValue(value);
            return oldValue;
        }
    }

    private int findKey(K key) {
        int pos = -1;
        Iterator<Map.Entry<K,V>> listIter = internalList.iterator();
        while (listIter.hasNext()) {
            Map.Entry<K,V> curEntry = listIter.next();
            pos++;
            if (curEntry.getKey().equals(key)) {
                return pos;
            }
        }
        return -1;
    }

}