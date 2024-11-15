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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification;

import org.apache.hc.core5.http.HttpException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
