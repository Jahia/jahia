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
package org.jahia.services.modulemanager.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * DTO for handling bundle information
 */
public class BundleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Checksum
     */
    private String checksum;

    /**
     * Display Name
     */
    private String displayName;

    /**
     * File Name
     */
    private String fileName;

    /**
     * Symbolic Name
     */
    private String symbolicName;

    /**
     * Version
     */
    private String version;

    /**
     * Group Id
     */
    private String groupId;

    /**
     * Jar File
     */
    private Resource jarFile;

    private String bundleKey;

    private String path;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            BundleDTO other = (BundleDTO) obj;
            return StringUtils.equals(getBundleKey(), other.getBundleKey())
                    && StringUtils.equals(getChecksum(), other.getChecksum());
        }

        return false;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getFileName() {
        return fileName;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getBundleKey()).append(getChecksum()).toHashCode();
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Resource getJarFile() {
        return jarFile;
    }

    public void setJarFile(Resource jarFile) {
        this.jarFile = jarFile;
    }

    public String getBundleKey() {
        return bundleKey;
    }

    public String getPath() {
        if (path == null) {
            path = groupId.replace('.', '/') + "/" + symbolicName + "/" + version + "/" + symbolicName + "-" + version + ".jar";
        }

        return path;
    }

    public void setBundleKey(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
