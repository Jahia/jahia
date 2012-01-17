/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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