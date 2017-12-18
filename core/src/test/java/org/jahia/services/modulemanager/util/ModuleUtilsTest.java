/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_NAME;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_SYMBOLIC_NAME;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_JAHIA_DEPENDS;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_PROVIDE_CAPABILITY;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_REQUIRE_CAPABILITY;
import static org.jahia.services.modulemanager.Constants.OSGI_CAPABILITY_MODULE_DEPENDENCIES;
import static org.jahia.services.modulemanager.Constants.OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY;
import static org.jahia.services.modulemanager.util.ModuleUtils.addCapabilities;
import static org.jahia.services.modulemanager.util.ModuleUtils.buildClauseProvideCapability;
import static org.jahia.services.modulemanager.util.ModuleUtils.buildClauseRequireCapability;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.jar.Attributes;

import org.jahia.services.modulemanager.util.ModuleUtils;
import org.junit.Test;

/**
 * Unit test for the {@link ModuleUtils}.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleUtilsTest {

    private static final String BUNDLE_ID = "my-bundle";
    private static final String BUNDLE_NAME = "My Bundle";

    private static final Attributes ATTS_TEMPLATE;
    static {
        ATTS_TEMPLATE = new Attributes();
        ATTS_TEMPLATE.put(ATTR_NAME_BUNDLE_SYMBOLIC_NAME, BUNDLE_ID);
    }

    private void assertProvides(Attributes atts, String... provides) {
        String value = atts.getValue(ATTR_NAME_PROVIDE_CAPABILITY);
        if (provides.length == 1) {
            assertEquals(buildClauseProvideCapability(provides[0]), value);
        } else {
            StringBuilder b = new StringBuilder();
            for (String p : provides) {
                if (b.length() > 0) {
                    b.append(',');
                }
                b.append(buildClauseProvideCapability(p));
            }
            assertEquals(b.toString(), value);
        }
    }

    private void assertRequires(Attributes atts, String... requires) {
        String value = atts.getValue(ATTR_NAME_REQUIRE_CAPABILITY);
        StringBuilder b = new StringBuilder();
        for (String p : requires) {
            if (b.length() > 0) {
                b.append(',');
            }
            b.append(buildClauseRequireCapability(p));
        }
        assertEquals(b.toString(), value);
    }

    @Test
    public void testBuildClauses() {
        assertEquals(OSGI_CAPABILITY_MODULE_DEPENDENCIES + ';' + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY + "=\""
                + BUNDLE_ID + "\"", buildClauseProvideCapability(BUNDLE_ID));
        assertEquals(OSGI_CAPABILITY_MODULE_DEPENDENCIES + ';' + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY + "=\""
                + BUNDLE_NAME + "\"", buildClauseProvideCapability(BUNDLE_NAME));

        assertEquals(OSGI_CAPABILITY_MODULE_DEPENDENCIES + ";filter:=\"(" + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY
                + "=" + BUNDLE_ID + ")\"", buildClauseRequireCapability(BUNDLE_ID));
        assertEquals(OSGI_CAPABILITY_MODULE_DEPENDENCIES + ";filter:=\"(" + OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY
                + "=" + BUNDLE_NAME + ")\"", buildClauseRequireCapability(BUNDLE_NAME));
    }

    @Test
    public void testProvideCapability() {
        // no change if the manifest attribute is already there
        Attributes atts = new Attributes(ATTS_TEMPLATE);
        String testCapability = buildClauseProvideCapability("aaa");
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
        assertEquals(existingValue + "," + buildClauseProvideCapability(BUNDLE_ID),
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

}
