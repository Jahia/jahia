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
package org.jahia.services.templates;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * These tests documents current behavior: Maven-equivalent raw syntaxes are kept distinct as SortedMap keys.
 *
 * In ModuleVersion, equals/hashCode are raw-string based. compareTo intentionally keeps non-zero ordering
 * between non-equal raw strings, even when Maven semantic comparison says they are equivalent.
 *
 * Future normalization to semantic keys could change this behavior, but that would be a separate and explicit
 * design decision.
 */
public class ModuleVersionTest {

    private void assertCompareToEqualsContract(String left, String right) {
        ModuleVersion leftVersion = new ModuleVersion(left);
        ModuleVersion rightVersion = new ModuleVersion(right);

        int cmp = leftVersion.compareTo(rightVersion);
        int reverseCmp = rightVersion.compareTo(leftVersion);

        assertEquals("compareTo must be anti-symmetric", Integer.signum(cmp), -Integer.signum(reverseCmp));

        if (leftVersion.equals(rightVersion)) {
            assertEquals("compareTo must return 0 for equal objects", 0, cmp);
        } else {
            assertTrue("compareTo must not return 0 for non-equal objects", cmp != 0);
        }
    }

    private void assertMavenEquivalent(String left, String right) {
        int mavenCmp = new ComparableVersion(left).compareTo(new ComparableVersion(right));
        assertEquals("Pair should be Maven-equivalent", 0, mavenCmp);
    }

    private void assertMavenNonEquivalent(String left, String right) {
        int mavenCmp = new ComparableVersion(left).compareTo(new ComparableVersion(right));
        assertTrue("Pair should not be Maven-equivalent", mavenCmp != 0);
    }

    @Test
    public void testCompareToIsConsistentWithEqualsForEquivalentMavenSyntaxes() {
        ModuleVersion dotSyntax = new ModuleVersion("1.alpha");
        ModuleVersion hyphenSyntax = new ModuleVersion("1-alpha");

        assertNotEquals("Different raw version strings must not be equal", dotSyntax, hyphenSyntax);
        assertTrue("compareTo must not return equality for non-equal objects", dotSyntax.compareTo(hyphenSyntax) != 0);
    }

    @Test
    public void testTreeMapKeepsDistinctEquivalentMavenSyntaxes() {
        TreeMap<ModuleVersion, String> versions = new TreeMap<>();
        versions.put(new ModuleVersion("1.alpha"), "dot");
        versions.put(new ModuleVersion("1-alpha"), "hyphen");

        assertEquals("Equivalent Maven syntaxes should not overwrite each other in sorted maps", 2, versions.size());
    }

    @Test
    public void testCompareToEqualsContractForMavenEquivalentPairs() {
        String[][] mavenEquivalentPairs = {
                {"1.0.0", "1.0.0"},
                {"1", "1.0.0"},
                {"1.alpha", "1-alpha"},
                {"1.0-SNAPSHOT", "1.0.0-SNAPSHOT"},
                {"1.0.0-qualifier", "1.0.0.qualifier"},
                {"1.0.0.SNAPSHOT", "1.0.0-SNAPSHOT"}
        };

        for (String[] pair : mavenEquivalentPairs) {
            assertMavenEquivalent(pair[0], pair[1]);
            assertCompareToEqualsContract(pair[0], pair[1]);
        }
    }

    @Test
    public void testCompareToEqualsContractForMavenNonEquivalentPairs() {
        String[][] mavenNonEquivalentPairs = {
                {"2-alpha", "2-alpha-SNAPSHOT"},
                {"1.0.0.RC1", "1.0.0-RC2"},
                {"2.13.5.SP1-redhat-00002", "2.13.5.Final-redhat-00002"}
        };

        for (String[] pair : mavenNonEquivalentPairs) {
            assertMavenNonEquivalent(pair[0], pair[1]);
            assertCompareToEqualsContract(pair[0], pair[1]);
        }
    }
}
