/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.models;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link JahiaDepends}.
 */
public class JahiaDependsTest {

    public void testParser(String dependsStr, String moduleName, String min, String max, boolean isOptional, String filterString) {
        JahiaDepends depends = new JahiaDepends(dependsStr);
        assertEquals(moduleName, depends.getModuleName());
        assertEquals(min, depends.getMinVersion());
        assertEquals(max, depends.getMaxVersion());
        assertEquals(StringUtils.isNotEmpty(min) || StringUtils.isNotEmpty(max), depends.hasVersion());
        assertEquals(filterString, depends.toFilterString());
        assertEquals(isOptional, depends.isOptional());
    }

    @Test
    public void testModuleOnly() {
        testParser("module-name1", "module-name1", "", "", false,
                "(moduleIdentifier=module-name1)");
    }

    @Test
    public void testRangedFilter() {
        testParser("module-name1=[1.4, 2.0)", "module-name1", "1.4.0", "2.0.0", false,
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0)(!(moduleVersion>=2.0.0)))");
    }

    @Test
    public void testMinFilter() {
        testParser("module-name1=1.4", "module-name1", "1.4.0", "", false,
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0))");
    }

    @Test
    public void testModuleOptional() {
        testParser("module-name1=optional", "module-name1", "", "", true,
                "(moduleIdentifier=module-name1)");
    }

    @Test
    public void testRangedFilterOptional() {
        testParser("module-name1=[1.4, 2.0);optional", "module-name1", "1.4.0", "2.0.0", true,
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0)(!(moduleVersion>=2.0.0)))");

    }

    /** optional keyword needs to be after version */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRangeFilterOptional() {
        testParser("module-name1=optional;[1.4, 2.0)", "module-name1", "1.4.0", "2.0.0", true,
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0)(!(moduleVersion>=2.0.0)))");
    }

    @Test
    public void testMinFilterOptional() {
        testParser("module-name1=1.4;optional", "module-name1", "1.4.0", "", true,
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0))");
    }

    @Test
    public void inRange() {
        JahiaDepends depends = new JahiaDepends("module-name1=[1,5]");
        assertTrue(depends.inRange("1.0.0-SNAPSHOT"));
        assertTrue(depends.inRange("5.0.0"));
        assertFalse(depends.inRange("5.0.0-SNAPSHOT"));

        // no range specified
        depends = new JahiaDepends("module-name2");
        assertTrue(depends.inRange("1.0"));

        // test min
        depends = new JahiaDepends("module-name1=1.2.4");
        assertTrue(depends.inRange("1.2.4"));
        assertTrue(depends.inRange("5.0.0-SNAPSHOT"));
        assertFalse(depends.inRange("1.2"));
        assertTrue(depends.inRange("1.2.4-SNAPSHOT"));
    }

    @Test
    public void toOsgiVersion() {
        assertEquals("1.0.0", JahiaDepends.toOsgiVersion("1.0"));
        assertEquals("1.0.0.SNAPSHOT", JahiaDepends.toOsgiVersion("1.0.0-SNAPSHOT"));
    }

}
