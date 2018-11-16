/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;

/**
 * Represents the bundle identifier.
 *
 * @author bdjiba
 */
public class BundleInfo {

    private BundleBucketInfo bucketInfo;
    private String version;

    /**
     * Creates the {@link BundleInfo} instance using provided bundle.
     *
     * @param bundle the bundle to compose info for
     * @return the {@link BundleInfo} instance using provided bundle
     */
    public static BundleInfo fromBundle(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle cannot be null");
        }
        return new BundleInfo(BundleUtils.getModuleGroupId(bundle), bundle.getSymbolicName(), bundle.getVersion().toString());
    }

    /**
     * Creates the {@link BundleInfo} instance using provided bundle key.
     *
     * @param key The bundle key
     * @return the {@link BundleInfo} instance using provided bundle key
     */
    public static BundleInfo fromKey(String key) {
        if (key == null || key.indexOf('/') == -1) {
            throw new IllegalArgumentException("Illegal bundle key: " + key);
        }
        String bucketKey = StringUtils.substringBeforeLast(key, "/");
        String version = StringUtils.substringAfterLast(key, "/");
        BundleBucketInfo bucketInfo = BundleBucketInfo.fromKey(bucketKey);
        return new BundleInfo(bucketInfo, version);
    }

    /**
     * Create a {@link BundleInfo} instance using provided module ID and version.
     *
     * @param moduleId The ID of the module
     * @param moduleVersion The module version
     * @return the {@link BundleInfo} instance using provided module ID and version
     */
    public static BundleInfo fromModuleInfo(String moduleId, String moduleVersion) {
        if (moduleId == null || moduleVersion == null) {
            throw new IllegalArgumentException("Illegal module info (id/version): " + moduleId + '/' + moduleVersion);
        }
        Bundle bundle = BundleUtils.getBundle(moduleId, moduleVersion);
        return bundle != null ? fromBundle(bundle) : null;
    }

    /**
     * Create an instance of this class.
     *
     * @param symbolicName The symbolic name of this bundle
     * @param version The version of this bundle
     */
    public BundleInfo(String symbolicName, String version) {
        this(null, symbolicName, version);
    }

    /**
     * Create an instance of this class.
     *
     * @param groupId The ID of the group for this bundle
     * @param symbolicName The symbolic name of this bundle
     * @param version The version of this bundle
     */
    public BundleInfo(String groupId, String symbolicName, String version) {
        this(new BundleBucketInfo(groupId, symbolicName), version);
    }

    /**
     * Create an instance of this class.
     *
     * @param bucketInfo The bucket this bundle belongs to
     * @param version The version of this bundle
     */
    public BundleInfo(BundleBucketInfo bucketInfo, String version) {
        if (bucketInfo == null || version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Bundle bucket info and bundle version are required");
        }
        this.bucketInfo = bucketInfo;
        this.version = version;
    }

    /**
     * @return The ID of the group for this bundle; can be null
     */
    public String getGroupId() {
        return bucketInfo.getGroupId();
    }

    /**
     * @return The key of this bundle, which is composed of the group ID, symbolic name and version
     */
    public String getKey() {
        return bucketInfo.getKey() + '/' + version;
    }

    /**
     * @return The symbolic name for this bundle
     */
    public String getSymbolicName() {
        return bucketInfo.getSymbolicName();
    }

    /**
     * @return The version of this bundle
     */
    public String getVersion() {
        return version;
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
        return obj != null && obj.getClass() == this.getClass() && ((BundleInfo) obj).getKey().equals(getKey());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
