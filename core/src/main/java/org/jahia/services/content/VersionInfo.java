/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

/**
 * Contains version info for end-users.
 *
 * @author loom
 *         Date: Mar 10, 2010
 *         Time: 10:08:08 AM
 */
public class VersionInfo implements Comparable {

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(VersionInfo.class);

    private Version version;
    private String label;
    private int depth;

    public VersionInfo(Version version, String label, int depth) {
        this.version = version;
        this.label = label;
        this.depth = depth;
    }

    public Version getVersion() {
        return version;
    }

    public String getLabel() {
        return label;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        VersionInfo that = (VersionInfo) o;
        if(label !=null) {
            return label.equals(that.label);
        }

        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        if(label !=null) {
            return label.hashCode();
        }
        return 31*version.hashCode();
    }

    public int compareTo(Object o) {
        if (this == o) return 0;

        VersionInfo that = (VersionInfo) o;
        if (this.equals(that)) {
            return 0;
        }
        if (label != null && that.label != null) {
            return this.label.compareTo(that.label);
        }
        try {
            return this.version.getCreated().compareTo(that.version.getCreated());
        } catch (RepositoryException re) {
            logger.error("Error while comparing versions ", re);
            return 0;
        }
    }
}
