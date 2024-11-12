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
package org.jahia.services.modulemanager.util;

import static java.util.stream.Collectors.joining;
import static org.jahia.services.modulemanager.Constants.*;
import static org.jahia.services.modulemanager.util.ModuleUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.jar.Attributes;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * Unit test for the {@link ModuleUtils}.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleUtilsTest {

    private static final String BUNDLE_ID = "my-bundle";
    private static final String BUNDLE_NAME = "My Bundle";

    private static final String BUNDLE_VERSION = "1.0.4";

    private static final Attributes ATTS_TEMPLATE;
    static {
        ATTS_TEMPLATE = new Attributes();
        ATTS_TEMPLATE.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, BUNDLE_ID);
        ATTS_TEMPLATE.put(ATTR_NAME_BUNDLE_VERSION, BUNDLE_VERSION);
    }

    private void assertProvides(Attributes atts, String... provides) {
        String value = atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY);
        String b = Arrays.stream(provides)
                .map(p -> buildClauseProvideCapability(p, BUNDLE_VERSION))
                .collect(joining(","));
        assertEquals(b, value);
    }

    private void assertRequires(Attributes atts, String... requires) {
        String value = atts.getValue(ATTR_NAME_REQUIRE_CAPABILITY);
        String b = Arrays.stream(requires)
                .map(p -> buildClauseRequireCapability(p))
                .collect(joining(","));
        assertEquals(b, value);
    }

    @Test
    public void testBuildClauseProvide() {
        String clausePrefix = OSGI_CAPABILITY_MODULE_DEPENDENCIES + ';' + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY + "=\"";
        String versionClause = OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY + ":Version=" + BUNDLE_VERSION;

        assertEquals(clausePrefix + BUNDLE_ID + "\";" + versionClause,
                buildClauseProvideCapability(BUNDLE_ID, BUNDLE_VERSION));
        assertEquals(clausePrefix + BUNDLE_NAME + "\";" + versionClause,
                buildClauseProvideCapability(BUNDLE_NAME, BUNDLE_VERSION));
    }

    @Test
    public void testBuildClauseRequire() {
        String clausePrefix = String.format("%s;filter:=\"(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        assertEquals(clausePrefix + BUNDLE_ID + ")\"", buildClauseRequireCapability(BUNDLE_ID));
        assertEquals(clausePrefix + BUNDLE_NAME + ")\"", buildClauseRequireCapability(BUNDLE_NAME));
    }

    @Test
    public void testBuildClauseRequireWithVersion() {
        String minVersion = "1";
        String minVersionExpected = "1.0.0";
        String maxVersion = "2.3";
        String maxVersionExpected = "2.3.0";
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(%s>=%s)(%s<=%s))\"", OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY,
                minVersionExpected, OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, maxVersionExpected);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=[%s,%s]", BUNDLE_ID, minVersion, maxVersion)));
    }

    @Test
    public void testBuildClauseRequireWithMinVersionOnly() {
        String minVersion = "1.0";
        String minVersionExpected = "1.0.0"; // padded with 0
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(%s>=%s))\"",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, minVersionExpected);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=%s", BUNDLE_ID, minVersion)));
    }

    @Test
    public void testBuildClauseRequireWithMaxVersionOnly() {
        String minVersion = "0";
        String minVersionExpected = "0.0.0"; // padded with 0
        String maxVersion = "2.3";
        String maxVersionExpected = "2.3.0"; // padded with 0
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(moduleVersion>=%s)(!(%s>=%s)))\"",
                minVersionExpected, OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, maxVersionExpected);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=[%s,%s)", BUNDLE_ID, minVersion, maxVersion)));
    }

    @Test
    public void testProvideCapability() {
        // no change if the manifest attribute is already there
        Attributes atts = new Attributes(ATTS_TEMPLATE);
        String testCapability = buildClauseProvideCapability("aaa", BUNDLE_VERSION) +
                "," + OSGI_CAPABILITY_SERVER;
        atts.put(ATTR_NAME_PROVIDE_CAPABILITY, testCapability);
        assertFalse(addCapabilities(atts));
        assertEquals(testCapability, atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY));

        // no bundle name
        atts = new Attributes(ATTS_TEMPLATE);
        assertTrue(addCapabilities(atts));
        assertProvides(atts, BUNDLE_ID);

        // with bundle name
        atts = new Attributes(ATTS_TEMPLATE);
        atts.put(ATTR_NAME_BUNDLE_NAME, BUNDLE_NAME);
        assertTrue(addCapabilities(atts));
        assertProvides(atts, BUNDLE_ID, BUNDLE_NAME);

        // provides contains existing value
        atts = new Attributes(ATTS_TEMPLATE);
        String existingValue = "my;aaa=bbb";
        atts.put(ATTR_NAME_PROVIDE_CAPABILITY, existingValue);
        assertTrue(addCapabilities(atts));
        assertEquals(existingValue + "," + buildClauseProvideCapability(BUNDLE_ID, BUNDLE_VERSION),
                atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY));

    }

    @Test
    public void testRequireCapability() {
        // only default dependency (implicit)
        Attributes atts = new Attributes(ATTS_TEMPLATE);
        addCapabilities(atts);
        assertNull(atts.getValue(ATTR_NAME_REQUIRE_CAPABILITY));

        // only default dependency (explicit)
        atts = new Attributes(ATTS_TEMPLATE);
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "default");
        addCapabilities(atts);
        assertRequires(atts, "default");

        // only default dependency (explicit by name)
        atts = new Attributes(ATTS_TEMPLATE);
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "Default Jahia Templates");
        addCapabilities(atts);
        assertRequires(atts, "Default Jahia Templates");

        // no default dependency
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        addCapabilities(atts);
        assertNull(atts.getValue(ATTR_NAME_REQUIRE_CAPABILITY));

        // no default dependency, explicit depends
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "ckeditor");
        addCapabilities(atts);
        assertRequires(atts, "ckeditor");

        // no default dependency, multiple depends
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "ckeditor, jquery");
        addCapabilities(atts);
        assertRequires(atts, "ckeditor", "jquery");

        // multiple depends, implicit default
        atts = new Attributes();
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "my1, my2");
        addCapabilities(atts);
        assertRequires(atts, "my1", "my2");

        // multiple depends, explicit default
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "my1, my2");
        addCapabilities(atts);
        assertRequires(atts, "my1", "my2");

        // depends with version
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "depend-module=[1.5,2]");
        addCapabilities(atts);
        assertRequires(atts, "depend-module=[1.5,2]");

        // depends with version, mixed multiple
        atts = new Attributes();
        atts.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, "assets");
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "depend-module=1.5,depend-module2,depend-module3=[4,5]");
        addCapabilities(atts);
        assertRequires(atts, "depend-module=1.5", "depend-module2", "depend-module3=[4,5]");

        // existing value, multiple depends
        atts = new Attributes();
        String existingValue = "com.jahia.services.content;filter:=\"(nodetypes=jmix:my)\"";
        atts.put(ATTR_NAME_REQUIRE_CAPABILITY, existingValue);
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "my1, my2");
        addCapabilities(atts);
        assertEquals(existingValue + ',' + buildClauseRequireCapability("my1") + ',' + buildClauseRequireCapability("my2"),
                atts.get(ATTR_NAME_REQUIRE_CAPABILITY));
    }

    @Test
    public void testParseJahiaDepends() {
        try {
            String jahiaDepends;
            jahiaDepends = "module1";
            assertEquals(jahiaDepends, replaceDependsDelimiter(jahiaDepends));
            jahiaDepends = "module1=1.4";
            assertEquals(jahiaDepends,  replaceDependsDelimiter(jahiaDepends));
            jahiaDepends = "module1 = [ 1.4,  2]  ";
            assertEquals("module1 = [ 1.4,  2]  ",  replaceDependsDelimiter(jahiaDepends));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseJahiaDependsMixed() {
        try {
            String jahiaDepends = "module-name1,module-name2=2.5.4, module-name3=[ ,1.4], "
                    + "module with a space = 2.0,module-name5=[2,3.4.23]";
            String actual = replaceDependsDelimiter(jahiaDepends);

            String expected = StringUtils.join(new String[] {
                    "module-name1",
                    "module-name2=2.5.4",
                    " module-name3=[ ,1.4]",
                    " module with a space = 2.0",
                    "module-name5=[2,3.4.23]",
            }, DEPENDENCY_DELIMITER);
            assertEquals(expected, actual);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseJahiaDependsFail() {
        try {
            String jahiaDepends = "module-name1=(,],module-name2=[2.5.4, ], module-name3=[,1.4], module with a space =,module-name5=[2,3.4"
                    + ".23]";
            String actual = replaceDependsDelimiter(jahiaDepends);
            fail("Should fail for 'module with a space = ' and 'module-name1=(,]'");
        } catch (Exception e) {
            // expected exception
        }
    }

}
