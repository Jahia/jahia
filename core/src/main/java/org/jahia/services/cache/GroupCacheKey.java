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
 package org.jahia.services.cache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * Composite cache key, used when we want to create a key that
 * can be flushed using groups.
 * User: Serge Huber
 * Date: Jul 14, 2005
 * Time: 2:35:29 PM
 * Copyright (C) Jahia Inc.
 */
public class GroupCacheKey implements Serializable, Comparable<GroupCacheKey> {

    private static final long serialVersionUID = 9006071384859530347L;

    private static String groupSeparator = "#$#G_";
    private static String keyGroupSeparator = "$$$";

    private final Object key;
    private final Set<String> groups;

    /**
     * This contructor will generate a key based on the combination
     * of the group names.
     * In order to avoid name conflict between key and groups names,
     * it is however HIGHLY recommended that you use one of the
     * constructors that seperatly specify the key and group names.
     * @param groups
     */
    public GroupCacheKey(Set<String> groups) {
        this.key = groups.toString();
        this.groups = groups;
    }

    public GroupCacheKey(Object key, Set<String> groups) {
        this.key = key;
        this.groups = groups;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public String[] getGroupArray() {
        return (String[]) groups.toArray(new String[groups.size()]);
    }

    public boolean containsGroup(Object group) {
        return groups.contains(group);
    }

    /*
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object.getClass() == GroupCacheKey.class) {
            GroupCacheKey otherKey = (GroupCacheKey) object;
            return otherKey.equals(key) && otherKey.getGroups().equals(groups);
        } else {
            return false;
        }
    }
    */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final GroupCacheKey groupCacheKey = (GroupCacheKey) o;

        if (!key.equals(groupCacheKey.key)) return false;

        return true;
    }

    public int hashCode() {
        return key.hashCode();
    }

    public String toString() {
        StringBuilder result = new StringBuilder(groups.size()*5);
        result.append(key);
        result.append(keyGroupSeparator);
        result.append(groupSeparator);
        Iterator<String> keyIter = groups.iterator();
        while (keyIter.hasNext()) {
            result.append((String) keyIter.next());
            if (keyIter.hasNext()) {
                result.append(groupSeparator);
            }
        }
        return result.toString();
    }

    public int compareTo(GroupCacheKey otherKey) {
        return toString().compareTo(otherKey.toString());
    }

    public static String getKeyGroupSeparator() {
        return keyGroupSeparator;
    }

    public static void setKeyGroupSeparator(String keyGroupSeparator) {
        GroupCacheKey.keyGroupSeparator = keyGroupSeparator;
    }

    public static String getGroupSeparator() {
        return groupSeparator;
    }

    public static void setGroupSeparator(String groupSeparator) {
        GroupCacheKey.groupSeparator = groupSeparator;
    }

    public Object getKey() {
        return key;
    }
}
