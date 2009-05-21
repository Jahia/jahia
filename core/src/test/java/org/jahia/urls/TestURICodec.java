/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.urls;

import junit.framework.*;
import java.io.*;

public class TestURICodec extends TestCase {
    private URICodec uRICodec = null;

    public TestURICodec(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        /**@todo verify the constructors*/
        uRICodec = new URICodec();
    }

    protected void tearDown() throws Exception {
        uRICodec = null;
        super.tearDown();
    }

    public void testEncode() throws UnsupportedEncodingException {
        String input = null;
        String encoding = null;
        String authorizedChars = null;
        String expectedReturn = null;
        String actualReturn = uRICodec.encode(input, encoding, authorizedChars);
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
        input = "abcdef ������+_&%~�;//_$*";
        encoding = "ISO-8859-1";
        actualReturn = uRICodec.decode(uRICodec.encode(input, encoding, URICodec.DEFAULT_AUTHORIZEDCHARS), encoding);
        assertEquals("return value", input, actualReturn);
        input = "abcdef ������+_&%~�;//_$*";
        encoding = "UTF-8";
        actualReturn = uRICodec.decode(uRICodec.encode(input, encoding, URICodec.DEFAULT_AUTHORIZEDCHARS), encoding);
        assertEquals("return value", input, actualReturn);
    }

}
