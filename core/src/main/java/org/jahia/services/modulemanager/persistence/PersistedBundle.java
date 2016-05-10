/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.persistence;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.services.modulemanager.BundleInfo;
import org.springframework.core.io.Resource;

/**
 * DTO for handling persisted bundle information.
 * 
 * @author Ahmed Chaabni
 */
public class PersistedBundle extends BundleInfo {

    private static final long serialVersionUID = -1719708168071531703L;

    /**
     * Checksum.
     */
    private String checksum;

    /**
     * Display name.
     */
    private String displayName;

    /**
     * Basic bundle information
     */
    private transient BundleInfo info;

    /**
     * Resource.
     */
    private Resource resource;

    /**
     * Initializes an instance of this class.
     * 
     * @param groupId
     *            the ID of the group for this bundle
     * @param symbolicName
     *            the symbolic name of this bundle
     * @param version
     *            the version of this bundle
     */
    public PersistedBundle(String groupId, String symbolicName, String version) {
        super(groupId, symbolicName, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        return (obj instanceof PersistedBundle)
                && StringUtils.equals(getChecksum(), ((PersistedBundle) obj).getChecksum());
    }

    /**
     * Returns basic information about this bundle.
     * 
     * @return basic information about this bundle
     */
    public BundleInfo getBundleInfo() {
        if (info == null) {
            info = new BundleInfo(getGroupId(), getSymbolicName(), getVersion());
        }
        return info;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the bundle location.
     * 
     * @return the bundle location
     */
    public String getLocation() {
        return "jcr:" + getKey();
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getKey()).append(getChecksum()).toHashCode();
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setResource(Resource jarFile) {
        this.resource = jarFile;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
