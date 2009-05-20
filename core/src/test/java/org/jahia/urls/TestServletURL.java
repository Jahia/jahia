/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.urls;

import junit.framework.*;

public class TestServletURL extends TestCase {
    private ServletURL servletURL = null;

    protected void setUp() throws Exception {
        super.setUp();
        /**@todo verify the constructors*/
        servletURL = new ServletURL();
    }

    protected void tearDown() throws Exception {
        servletURL = null;
        super.tearDown();
    }

    public void testMatchesServletPattern() {
        String pattern = null;
        String servletPath = null;
        boolean expectedReturn = false;
        boolean actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/

        // now testing the regular cases

        pattern = "/Jahia/*";
        servletPath = "/Jahia";
        expectedReturn = true;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        pattern = "*.do";
        servletPath = "/Jahia.do";
        expectedReturn = true;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        pattern = "/exact/Mapping";
        servletPath = "/exact/Mapping";
        expectedReturn = true;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        pattern = "/";
        servletPath = "/any/mapping";
        expectedReturn = true;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        // now let's test cases that shouldn't match.
        pattern = "/Jahia/*";
        servletPath = "Jahia";
        expectedReturn = false;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        pattern = "*.do";
        servletPath = ".do.test";
        expectedReturn = false;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

        pattern = "/exact/Mapping";
        servletPath = "/exact/mapping";
        expectedReturn = false;
        actualReturn = servletURL.matchesServletPattern(pattern, servletPath);
        assertEquals("return value", expectedReturn, actualReturn);

    }

}
