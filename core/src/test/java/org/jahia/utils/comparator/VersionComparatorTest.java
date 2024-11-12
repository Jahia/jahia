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

import org.apache.felix.utils.version.VersionCleaner;
import org.junit.Test;
import org.osgi.framework.Version;

import static org.junit.Assert.*;

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
 *     Copyright (C) 2002-2023 Jahia Solutions Group. All rights reserved.
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
public class VersionComparatorTest {

    /* Helper methods */

    private void testEqual(String version1, String version2, String assertMsg) {
        Version v1 = new Version(VersionCleaner.clean(version1));
        Version v2 = new Version(VersionCleaner.clean(version2));
        int result = VersionComparator.compare(v1, v2);
        assertEquals(assertMsg, 0, result);
    }

    private void testGreater(String version1, String version2, String assertMsg) {
        Version v1 = new Version(VersionCleaner.clean(version1));
        Version v2 = new Version(VersionCleaner.clean(version2));
        assertTrue(VersionComparator.compare(v1, v2) > 0);
        assertTrue(VersionComparator.compare(v2, v1) < 0);
    }

    /* Tests */

    @Test
    public void testEqual() {
        testEqual("1.0.0", "1.0.0", "Test equal");
        testEqual("1", "1.0.0", "Test semantic equals");
    }

    @Test
    public void testEqualQualifierSnapshot() {
        testEqual("1.0.0-qualifier", "1.0.0.qualifier", "Test qualifier with different separator");
        testEqual("1.0.0.SNAPSHOT", "1.0.0-SNAPSHOT", "Test SNAPSHOT with different separator");
        testEqual("1.0.0-qualifier-SNAPSHOT", "1.0.0-qualifier-SNAPSHOT", "Test equals qualifier with SNAPSHOT");
    }

    @Test
    public void testReleaseComparison() {
        testGreater("1.0.0", "1.0.0-SNAPSHOT", "Test release > SNAPSHOT");
        testGreater("1.0.1-SNAPSHOT", "1.0", "Test next release with SNAPSHOT > previous release");
    }

    @Test
    public void testQualifierComparison() {
        testGreater("1.0.0-qualifier", "1.0", "Test qualifier > release");
        testGreater("2.4.0-qualifier2", "2.4-qualifier1", "Test qualifier comparison");
        testGreater("2-alpha", "2-alpha-SNAPSHOT", "Test qualifier with snapshot comparison");
        testGreater("1.4-qualifier2-SNAPSHOT", "1.4-qualifier1",
                "Test next qualifier with snapshot with previous qualifier");
    }
}
