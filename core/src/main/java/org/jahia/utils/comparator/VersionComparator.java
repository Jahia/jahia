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
     * Compares two version strings using Maven ordering rules.
      *
      * <p>Works around a regression introduced by the MNG-7644 fix in maven-artifact 3.9.x,
      * where versions combining a dot-separated alphabetic qualifier with -SNAPSHOT are
      * incorrectly ordered by {@link ComparableVersion}. For example, without this fix:
      * {@code 2.0.0.alpha-SNAPSHOT > 2.0.0.alpha} — which is wrong.
      *
      * <p>The workaround strips -SNAPSHOT, compares base versions, and enforces
      * SNAPSHOT &lt; release when base versions are equal.
      *
      * <p>Verified broken on maven-artifact 3.9.15. Re-evaluate if maven-artifact is upgraded.
     */
    public static int compare(String v1, String v2) {
        boolean v1Snapshot = v1.endsWith("-SNAPSHOT");
        boolean v2Snapshot = v2.endsWith("-SNAPSHOT");

        if (v1Snapshot != v2Snapshot) {
            // Compare the base versions (without the -SNAPSHOT suffix on whichever has it)
            String v1Base = v1Snapshot ? v1.substring(0, v1.length() - "-SNAPSHOT".length()) : v1;
            String v2Base = v2Snapshot ? v2.substring(0, v2.length() - "-SNAPSHOT".length()) : v2;
            int cmp = new ComparableVersion(v1Base).compareTo(new ComparableVersion(v2Base));
            if (cmp == 0) {
                // Same base version: SNAPSHOT < release
                return v1Snapshot ? -1 : 1;
            }
            return cmp;
        }

        return new ComparableVersion(v1).compareTo(new ComparableVersion(v2));
    }
}
