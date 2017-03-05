/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.files;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * File entry data.
 *
 * @author Sergiy Shyrkov
 */
class FileKey {

    private String cacheKey;

    private String path;
    private String thumbnail;
    private String versionDate;
    private String versionLabel;
    private String workspace;

    /**
     * Initializes an instance of this class.
     *
     * @param workspace
     * @param path
     */
    FileKey(String workspace, String path) {
        this(workspace, path, null, null, null);
    }

    /**
     * Initializes an instance of this class.
     *
     * @param workspace
     * @param path
     * @param versionDate
     * @param versionLabel
     * @param thumbnail
     */
    FileKey(String workspace, String path, String versionDate, String versionLabel, String thumbnail) {
        super();
        this.workspace = workspace;
        this.path = path;
        this.versionDate = versionDate;
        this.versionLabel = versionLabel;
        this.thumbnail = thumbnail;
    }

    public String getCacheKey() {
        if (cacheKey == null) {
            StringBuilder key = new StringBuilder(64);
            key.append(workspace).append(":").append(path).append(":")
                    .append(versionDate == null ? "0" : versionDate).append(":");
            if (versionLabel != null) {
                key.append(versionLabel);
            }
            cacheKey = key.toString();
        }

        return cacheKey;
    }

    public String getPath() {
        return path;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getVersionDate() {
        return versionDate;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public String getWorkspace() {
        return workspace;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}