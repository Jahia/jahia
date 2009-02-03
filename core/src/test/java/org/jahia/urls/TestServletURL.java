/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
