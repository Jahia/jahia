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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Represents the bundle bucket identifier.
 */
public class BundleBucketInfo implements Serializable {

    private static final long serialVersionUID = 9116182373302703262L;

    private String groupId;
    private String symbolicName;

    /**
     * Create a {@link BundleBucketInfo} instance using provided bundle bucket key.
     *
     * @param key The bundle bucket key
     * @return a {@link BundleBucketInfo} instance
     */
    public static BundleBucketInfo fromKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Illegal bundle bucket key: " + key);
        }
        String groupId;
        String symbolicName;
        if (key.indexOf('/') == -1) {
            groupId = null;
            symbolicName = key;
        } else {
            groupId = StringUtils.substringBefore(key, "/");
            symbolicName = StringUtils.substringAfter(key, "/");
        }
        return new BundleBucketInfo(groupId, symbolicName);
    }

    /**
     * Create an instance of this class.
     *
     * @param groupId The ID of the group for this bundle bucket
     * @param symbolicName The symbolic name of this bundle bucket
     */
    public BundleBucketInfo(String groupId, String symbolicName) {
        if (symbolicName == null || symbolicName.isEmpty()) {
            throw new IllegalArgumentException("Symbolic name is required for the bundle bucket");
        }
        this.groupId = groupId;
        this.symbolicName = symbolicName;
    }

    /**
     * @return The ID of the group for this bundle bucket; can be null
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return The key of this bundle bucket, which is composed of the group ID and symbolic name
     */
    public String getKey() {
        return (groupId == null ? symbolicName : groupId + '/' + symbolicName);
    }

    /**
     * @return The symbolic name of this bundle bucket
     */
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj != null && obj.getClass() == this.getClass() && ((BundleBucketInfo) obj).getKey().equals(getKey());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
