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
import java.net.*;
import javax.servlet.http.*;
import java.io.UnsupportedEncodingException;

public class TestURI extends TestCase {
    private URI uRI = null;
    private final String uriTest1 = "http://login:password@host/path1/path2/filename?name1=value2&name2=value2#fragment";
    private final String uriTest2 = "http://login:password@host/path1_����/path2_���/file name.txt.txt�����?name1_����=value2_�����&name2_�����=value2_������#fragment_������!";

    protected void setUp() throws Exception {
        super.setUp();
        /**@todo verify the constructors*/
        uRI = new URI();
    }

    protected void tearDown() throws Exception {
        uRI = null;
        super.tearDown();
    }

    public void testURI() {
        uRI = new URI();
        /**@todo fill in the test code*/
    }

    public void testURI1() {
        String uri = null;
        uRI = new URI(uriTest1);
        String expectedReturn = uriTest1;
        String actualReturn = uRI.toString();
        assertEquals("return value", expectedReturn, actualReturn);
        uRI = new URI(uriTest2);
        expectedReturn = uriTest2;
        try {
            System.out.println(uriTest2);
            System.out.println("becomes with .toString(\"UTF-8\")");
            System.out.println(uRI.toString("UTF-8"));
            actualReturn = URICodec.decode(uRI.toString("UTF-8"), "UTF-8");
            assertEquals("return value", expectedReturn, actualReturn);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        try {
            System.out.println(uriTest2);
            System.out.println("becomes with .toString(\"ISO-8859-1\")");
            System.out.println(uRI.toString("ISO-8859-1"));
            actualReturn = URICodec.decode(uRI.toString("ISO-8859-1"), "ISO-8859-1");
            assertEquals("return value", expectedReturn, actualReturn);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        uri = null;
        uRI = new URI(uri);
    }

    public void testGetAuthority() {
        String expectedReturn = null;
        String actualReturn = uRI.getAuthority();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetFragmentString() {
        String expectedReturn = null;
        String actualReturn = uRI.getFragmentString();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetHostName() {
        String expectedReturn = null;
        String actualReturn = uRI.getHostName();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetPath() {
        String expectedReturn = null;
        String actualReturn = uRI.getPath();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetPort() {
        int expectedReturn = -1;
        int actualReturn = uRI.getPort();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetQueryString() {
        String expectedReturn = null;
        String actualReturn = uRI.getQueryString();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetScheme() {
        String expectedReturn = null;
        String actualReturn = uRI.getScheme();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testGetUserInfo() {
        String expectedReturn = null;
        String actualReturn = uRI.getUserInfo();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testIsURIStartingAtPath() {
        boolean expectedReturn = false;
        boolean actualReturn = uRI.isURIStartingAtPath();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testSetAuthority() {
        String authority = null;
        uRI.setAuthority(authority);
        /**@todo fill in the test code*/
    }

    public void testSetFragmentString() {
        String fragmentString = null;
        uRI.setFragmentString(fragmentString);
        /**@todo fill in the test code*/
    }

    public void testSetHostName() {
        String hostName = null;
        uRI.setHostName(hostName);
        /**@todo fill in the test code*/
    }

    public void testSetPath() {
        String path = null;
        uRI.setPath(path);
        /**@todo fill in the test code*/
    }

    public void testSetPort() {
        int port = 0;
        uRI.setPort(port);
        /**@todo fill in the test code*/
    }

    public void testSetQueryString() {
        String queryString = null;
        uRI.setQueryString(queryString);
        /**@todo fill in the test code*/
    }

    public void testSetScheme() {
        String scheme = null;
        uRI.setScheme(scheme);
        /**@todo fill in the test code*/
    }

    public void testSetURI() {
        String uri = null;
        uRI.setURI(uri);
        /**@todo fill in the test code*/
    }

    public void testSetURI1() {
        URL javaURL = null;
        uRI.setURI(javaURL);
        /**@todo fill in the test code*/
    }

    public void testSetURIStartingAtPath() {
        boolean uriStartingAtPath = false;
        uRI.setURIStartingAtPath(uriStartingAtPath);
        /**@todo fill in the test code*/
    }

    public void testSetUserInfo() {
        String userInfo = null;
        uRI.setUserInfo(userInfo);
        /**@todo fill in the test code*/
    }

    public void testToString() {
        String expectedReturn = null;
        String actualReturn = uRI.toString();
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testToString1() {
        HttpServletResponse response = null;
        String expectedReturn = null;
        String actualReturn = uRI.toString(response);
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testToString2() {
        HttpServletResponse response = null;
        String encoding = null;
        String expectedReturn = null;
        String actualReturn = uRI.toString(response, encoding);
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

    public void testToString3() {
        String encoding = null;
        String expectedReturn = null;
        String actualReturn = uRI.toString(encoding);
        assertEquals("return value", expectedReturn, actualReturn);
        /**@todo fill in the test code*/
    }

}
