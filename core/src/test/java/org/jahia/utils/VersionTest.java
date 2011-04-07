package org.jahia.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for version parsing class.
 */
public class VersionTest {

    @Test
    public void testVersionParsing() {
        Version version = new Version("1.0");
        Assert.assertEquals("Major version should be 1", 1, version.getMajorVersion());
        Assert.assertEquals("Minor version should be 0", 0, version.getMinorVersion());
        Assert.assertEquals("Service pack version should be 0", 0, version.getServicePackVersion());
        Assert.assertEquals("Patch version should be 0", 0, version.getPatchVersion());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        Assert.assertEquals("Version " + version + " toString not equal", "1.0", version.toString());
        boolean exception = false;
        try {
            version = new Version(null);
        } catch (NumberFormatException nfe) {
            exception = true;
        }
        Assert.assertTrue("Version " + version + " is invalid but not detected as such", exception);
        version = new Version("1.1.1.1.1.1.1.1");
        Assert.assertEquals("Version " + version + " toString not equal", "1.1.1.1.1.1.1.1", version.toString());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        version = new Version("1.1.rc1");
        Assert.assertTrue("Version " + version + " should be identified as release candidate", version.isReleaseCandidate());
        version = new Version("1.1b1");
        Assert.assertTrue("Version " + version + " should be identified as beta", version.isBeta());
        version = new Version("6.5-SNAPSHOT");
        Assert.assertEquals("Version " + version + " toString not equal", "6.5-SNAPSHOT", version.toString());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        version = new Version("6.5b1-B1");
        Assert.assertEquals("Version " + version + " toString not equal", "6.5b1-B1", version.toString());
        Assert.assertTrue("Version " + version + " should be identified as beta", version.isBeta());
        List<String> qualifiers = new ArrayList<String>();
        qualifiers.add("b07");
        qualifiers.add("334");
        qualifiers.add("10M3326");
        version = new Version("1.6.0_24-b07-334-10M3326");
        Assert.assertEquals("Version " + version + " toString not equal", "1.6.0_24-b07-334-10M3326", version.toString());
        Assert.assertEquals("Major version should be 1", 1, version.getMajorVersion());
        Assert.assertEquals("Minor version should be 6", 6, version.getOrderedVersionNumbers().get(1).intValue());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        Assert.assertEquals("Version " + version + " qualifiers are invalid", version.getQualifiers(), qualifiers);
        version = new Version("  1.6.0_24-b07-334-10M3326   ");
        Assert.assertEquals("Version " + version + " toString not equal", "1.6.0_24-b07-334-10M3326", version.toString());
        Assert.assertEquals("Major version should be 1", 1, version.getMajorVersion());
        Assert.assertEquals("Minor version should be 6", 6, version.getOrderedVersionNumbers().get(1).intValue());
        Assert.assertEquals("Update version should be 24", "24", version.getUpdateMarker());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        Assert.assertEquals("Version " + version + " qualifiers are invalid", version.getQualifiers(), qualifiers);
        version = new Version(" 1.6.0_u24-b07-334-10M3326");
        Assert.assertEquals("Version " + version + " toString not equal", "1.6.0_u24-b07-334-10M3326", version.toString());
        Assert.assertEquals("Major version should be 1", 1, version.getMajorVersion());
        Assert.assertEquals("Minor version should be 6", 6, version.getOrderedVersionNumbers().get(1).intValue());
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        Assert.assertEquals("Version " + version + " qualifiers are invalid", version.getQualifiers(), qualifiers);
        exception = false;
        try {
            version = new Version("1.6.0u24-b07-334-10M3326");
        } catch (NumberFormatException nfe) {
            exception = true;
        }
        Assert.assertTrue("Version " + version + " is invalid but not detected as such", exception);
        version = new Version("6.5-BETA2");
        Assert.assertTrue("Version " + version + " should be identified as final", version.isFinal());
        qualifiers.clear();
        qualifiers.add("BETA2");
        Assert.assertEquals("Version " + version + " qualifier should be BETA2", version.getQualifiers(), qualifiers);
    }

    @Test
    public void testVersionComparison() {
        Version lowerVersion = new Version("1.5");
        Version higherVersion = new Version("1.7");

        Version testVersion = new Version("1.6.0_24-b07-334-10M3326");
        Assert.assertEquals("Test version should be higher than lower version", -1, lowerVersion.compareTo(testVersion));
        Assert.assertEquals("Test version should be lower than higher version", 1, higherVersion.compareTo(testVersion));

        testVersion = new Version("1.6-SNAPSHOT");
        Assert.assertEquals("Test version should be higher than lower version", -1, lowerVersion.compareTo(testVersion));
        Assert.assertEquals("Test version should be lower than higher version", 1, higherVersion.compareTo(testVersion));

        testVersion = new Version("1.5-SNAPSHOT");
        Assert.assertEquals("Test version should be equal to lower version", 0, lowerVersion.compareTo(testVersion));
        Assert.assertEquals("Test version should be lower than higher version", 1, higherVersion.compareTo(testVersion));

    }
}
