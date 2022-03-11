/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.utils.comparator;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.osgi.framework.Version;

public class VersionComparator {

    private VersionComparator() {}

    /**
     * Compare OSGI versions using maven ordering rules
     */
    public static int compare(Version v1, Version v2) {
        return compare(v1.toString(), v2.toString());
    }

    /**
     * Compare versions using maven ordering rules
     */
    public static int compare(String v1, String v2) {
        ComparableVersion cv1 = new ComparableVersion(v1);
        ComparableVersion cv2 = new ComparableVersion(v2);
        return cv1.compareTo(cv2);
    }
}
