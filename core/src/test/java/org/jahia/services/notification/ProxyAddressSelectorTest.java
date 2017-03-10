/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.httpclient.HttpException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the {@link ProxyAddressSelector}.
 * 
 * @author Sergiy Shyrkov
 */
public class ProxyAddressSelectorTest {

    @Before
    public void setUp() throws Exception {
        // cleanup the properties
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        System.setProperty("http.proxyUser", "");
        System.setProperty("http.proxyPassword", "");

        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");
        System.setProperty("https.proxyUser", "");
        System.setProperty("https.proxyPassword", "");

        System.setProperty("http.nonProxyHosts", "");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBothProxiesDefined() throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        System.setProperty("http.proxyPort", "9090");
        System.setProperty("https.proxyHost", "httpsProxy");
        System.setProperty("https.proxyPort", "9443");

        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
        assertEquals("httpsProxy:9443", ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("http://localhost:8080"));
        assertEquals("httpsProxy:9443", ProxyAddressSelector.getProxyForUrl("https://localhost:8080"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("www.jahia.com:9090"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("localhost:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("/aaa.html"));
    }

    @Test
    public void testHttpProxyDefined() throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        System.setProperty("http.proxyPort", "9090");

        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("http://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://localhost:8080"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("www.jahia.com:9090"));
        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("localhost:9090"));
        assertEquals("httpProxy:9090",
                ProxyAddressSelector.getProxyForUrl("localhost:9090/aaa/bb/test.jsp?a=aaa&b=bbb"));
        assertNull(ProxyAddressSelector.getProxyForUrl("/aaa.html"));

        System.setProperty("http.proxyPort", "");
        assertEquals("httpProxy:80", ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
    }

    @Test
    public void testHttpsProxyDefined() throws HttpException {
        System.setProperty("https.proxyHost", "httpsProxy");
        System.setProperty("https.proxyPort", "9443");

        assertNull(ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
        assertEquals("httpsProxy:9443", ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("http://localhost:8080"));
        assertEquals("httpsProxy:9443", ProxyAddressSelector.getProxyForUrl("https://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("www.jahia.com:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("localhost:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("/aaa.html"));

        System.setProperty("https.proxyPort", "");
        assertEquals("httpsProxy:443", ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
    }

    @Test
    public void testNonProxyHostsDefined() throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        System.setProperty("http.proxyPort", "9090");
        System.setProperty("https.proxyHost", "httpsProxy");
        System.setProperty("https.proxyPort", "9443");
        System.setProperty("http.nonProxyHosts", "localhost|*.jahia.com");

        assertNull(ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("http://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("www.jahia.com:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("localhost:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("/aaa.html"));

        assertEquals("httpProxy:9090", ProxyAddressSelector.getProxyForUrl("http://www.google.com"));
        assertEquals("httpsProxy:9443", ProxyAddressSelector.getProxyForUrl("https://www.google.com"));
    }

    @Test
    public void testNoProxySettingsDefined() throws HttpException {
        assertNull(ProxyAddressSelector.getProxyForUrl("http://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://www.jahia.com"));
        assertNull(ProxyAddressSelector.getProxyForUrl("http://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("https://localhost:8080"));
        assertNull(ProxyAddressSelector.getProxyForUrl("www.jahia.com:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("localhost:9090"));
        assertNull(ProxyAddressSelector.getProxyForUrl("/aaa.html"));
    }
}
