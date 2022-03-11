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
