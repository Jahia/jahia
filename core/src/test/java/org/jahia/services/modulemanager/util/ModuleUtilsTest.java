/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.util;

import static java.util.stream.Collectors.joining;
import static org.jahia.services.modulemanager.Constants.*;
import static org.jahia.services.modulemanager.util.ModuleUtils.addCapabilities;
import static org.jahia.services.modulemanager.util.ModuleUtils.buildClauseProvideCapability;
import static org.jahia.services.modulemanager.util.ModuleUtils.buildClauseRequireCapability;
import static org.jahia.services.modulemanager.util.ModuleUtils.replaceDependsDelimiter;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.jar.Attributes;

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
        String minVersion = "1.0";
        String maxVersion = "2.3";
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(%s>=%s)(%s<=%s))\"",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, minVersion, OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, maxVersion);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=[%s,%s]", BUNDLE_ID, minVersion, maxVersion)));
    }

    @Test
    public void testBuildClauseRequireWithMinVersionOnly() {
        String minVersion = "1.0";
        String maxVersion = "";
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(%s>=%s))\"",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, minVersion);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=[%s,%s]", BUNDLE_ID, minVersion, maxVersion)));
    }

    @Test
    public void testBuildClauseRequireWithMaxVersionOnly() {
        String minVersion = "";
        String maxVersion = "2.3";
        String reqClausePrefix = String.format("%s;filter:=\"(&(%s=",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES, OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY);
        String reqVersionClause = String.format(")(%s<=%s))\"",
                OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY, maxVersion);
        assertEquals(reqClausePrefix + BUNDLE_ID + reqVersionClause,
                buildClauseRequireCapability(String.format("%s=[%s,%s]", BUNDLE_ID, minVersion, maxVersion)));
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
        assertRequires(atts, "default");

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
        assertRequires(atts, "default", "my1", "my2");

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
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "depend-module=[,1.5],depend-module2,depend-module3=[4,5]");
        addCapabilities(atts);
        assertRequires(atts, "depend-module=[,1.5]", "depend-module2", "depend-module3=[4,5]");

        // existing value, multiple depends
        atts = new Attributes();
        String existingValue = "com.jahia.services.content;filter:=\"(nodetypes=jmix:my)\"";
        atts.put(ATTR_NAME_REQUIRE_CAPABILITY, existingValue);
        atts.put(ATTR_NAME_JAHIA_DEPENDS, "my1, my2");
        addCapabilities(atts);
        assertEquals(
                existingValue + ',' + buildClauseRequireCapability("default") + ','
                        + buildClauseRequireCapability("my1") + ',' + buildClauseRequireCapability("my2"),
                atts.get(ATTR_NAME_REQUIRE_CAPABILITY));
    }

    @Test
    public void testParseJahiaDepends() {
        try {
            String jahiaDepends;
            jahiaDepends = "module1";
            assertEquals(jahiaDepends,  replaceDependsDelimiter(jahiaDepends));
            jahiaDepends = "module1=[,]";
            assertEquals(jahiaDepends,  replaceDependsDelimiter(jahiaDepends));
            jahiaDepends = "module1=[1.4,]";
            assertEquals(jahiaDepends,  replaceDependsDelimiter(jahiaDepends));
            jahiaDepends = "module1 = [ 1.4,  2]  ";
            assertEquals("module1=[ 1.4,2]",  replaceDependsDelimiter(jahiaDepends));
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseJahiaDependsMixed() {
        try {
            String jahiaDepends = "module-name1=[,],module-name2=[2.5.4, ], module-name3=[,1.4], module with a space ,module-name5=[2,3.4"
                    + ".23]";
            String actual = replaceDependsDelimiter(jahiaDepends);
            assertEquals("module-name1=[,];module-name2=[2.5.4,];module-name3=[,1.4];module with a space;module-name5=[2,3.4.23]", actual);
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
