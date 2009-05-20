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
