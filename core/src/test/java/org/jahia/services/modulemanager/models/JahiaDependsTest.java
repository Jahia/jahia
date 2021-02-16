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
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
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
package org.jahia.services.modulemanager.models;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link JahiaDepends}.
 */
public class JahiaDependsTest {

    public void testParser(String dependsStr, String moduleName, String min, String max, String filterString) {
        JahiaDepends depends = new JahiaDepends(dependsStr);
        assertEquals(moduleName, depends.getModuleName());
        assertEquals(min, depends.getMinVersion());
        assertEquals(max, depends.getMaxVersion());
        assertEquals(StringUtils.isNotEmpty(min) || StringUtils.isNotEmpty(max), depends.hasVersion());
        assertEquals(filterString, depends.toFilterString());
    }

    @Test
    public void testParser() {
        testParser("module-name1", "module-name1", "", "", "(moduleIdentifier=module-name1)");
        testParser("module-name1=[1.4, 2.0)", "module-name1", "1.4.0", "2.0.0",
                "(&(moduleIdentifier=module-name1)(moduleVersion>=1.4.0)(!(moduleVersion>=2.0.0)))");
        testParser("module-name1=1.4", "module-name1", "1.4.0", "",
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
    }

    @Test
    public void toOsgiVersion() {
        assertEquals("1.0.0", JahiaDepends.toOsgiVersion("1.0"));
        assertEquals("1.0.0.SNAPSHOT", JahiaDepends.toOsgiVersion("1.0.0-SNAPSHOT"));
    }

}