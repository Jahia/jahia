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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the {@link HttpClientService}.
 * 
 * @author Sergiy Shyrkov
 */
public class HttpClientServiceTest {

    private HttpClientService httpClientService;

    private void ensureProxy(String url, String expectedHost, int expectedPort) {
        ensureProxy(url, expectedHost, expectedPort, null, null);
    }

    private void ensureProxy(String url, String expectedHost, int expectedPort, String user, String pwd) {
        HttpClient httpClient = httpClientService.getHttpClient(url);
        assertEquals(expectedHost, httpClient.getHostConfiguration().getProxyHost());
        assertEquals(expectedPort, httpClient.getHostConfiguration().getProxyPort());

        Credentials credentials = httpClient.getState().getProxyCredentials(AuthScope.ANY);
        if (null == user) {
            assertNull(credentials);
        } else {
            assertNotNull(credentials);
            assertTrue(credentials instanceof UsernamePasswordCredentials);
            assertEquals(user, ((UsernamePasswordCredentials) credentials).getUserName());
            assertEquals(pwd, ((UsernamePasswordCredentials) credentials).getPassword());
        }
    }

    private void initClient() {
        // instantiate HttpClient
        HttpClientParams params = new HttpClientParams();
        params.setAuthenticationPreemptive(true);
        params.setCookiePolicy("ignoreCookies");

        HttpConnectionManagerParams cmParams = new HttpConnectionManagerParams();
        cmParams.setConnectionTimeout(15000);
        cmParams.setSoTimeout(60000);

        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.setParams(cmParams);

        httpClientService = new HttpClientService();
        httpClientService.setHttpClient(new HttpClient(params, httpConnectionManager));
    }

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
        if (httpClientService != null) {
            httpClientService.shutdown();
            httpClientService = null;
        }
    }

    @Test
    public void testBothProxiesDefined() throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        System.setProperty("http.proxyPort", "9090");
        System.setProperty("http.proxyUser", "httpProxyUser");
        System.setProperty("http.proxyPassword", "httpProxyPassword");
        System.setProperty("https.proxyHost", "httpsProxy");
        System.setProperty("https.proxyPort", "9443");
        System.setProperty("https.proxyUser", "httpsProxyUser");
        System.setProperty("https.proxyPassword", "httpsProxyPassword");

        initClient();
        ensureProxy(null, "httpsProxy", 9443, "httpsProxyUser", "httpsProxyPassword");

        ensureProxy("https://www.test.com", "httpsProxy", 9443, "httpsProxyUser", "httpsProxyPassword");

        ensureProxy("http://www.test.com", "httpProxy", 9090, "httpProxyUser", "httpProxyPassword");
    }

    private void testHttpProxyDefined(int definedPort, int expectedPort) throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        if (definedPort > 0) {
            System.setProperty("http.proxyPort", String.valueOf(definedPort));
        }

        initClient();
        HttpClient httpClient = httpClientService.getHttpClient(null);
        assertNull(httpClient.getState().getProxyCredentials(AuthScope.ANY));

        ensureProxy(null, "httpProxy", expectedPort);

        ensureProxy("http://www.jahia.com", "httpProxy", expectedPort);
        ensureProxy("http://localhost:8080", "httpProxy", expectedPort);
        ensureProxy("www.jahia.com:9090", "httpProxy", expectedPort);
        ensureProxy("localhost:9090", "httpProxy", expectedPort);
        ensureProxy("http://localhost:8080", "httpProxy", expectedPort);
        
        ensureProxy("https://www.jahia.com", null, -1);
        ensureProxy("https://localhost:8080", null, -1);

        ensureProxy("/aaa.html", null, -1);
    }

    @Test
    public void testHttpProxyDefinedWithoutPort() throws HttpException {
        testHttpProxyDefined(0, 80);
    }

    @Test
    public void testHttpProxyDefinedWithPort() throws HttpException {
        testHttpProxyDefined(9090, 9090);
    }

    private void testHttpsProxyDefined(int definedPort, int expectedPort) throws HttpException {
        System.setProperty("https.proxyHost", "httpsProxy");
        if (definedPort > 0) {
            System.setProperty("https.proxyPort", String.valueOf(definedPort));
        }

        initClient();
        HttpClient httpClient = httpClientService.getHttpClient(null);
        assertNull(httpClient.getState().getProxyCredentials(AuthScope.ANY));

        ensureProxy(null, "httpsProxy", expectedPort);

        ensureProxy("http://www.jahia.com", null, -1);
        ensureProxy("http://localhost:8080", null, -1);
        ensureProxy("www.jahia.com:9090", null, -1);
        ensureProxy("localhost:9090", null, -1);
        ensureProxy("http://localhost:8080", null, -1);

        ensureProxy("https://www.jahia.com", "httpsProxy", expectedPort);
        ensureProxy("https://localhost:8080", "httpsProxy", expectedPort);

        ensureProxy("/aaa.html", null, -1);
    }

    @Test
    public void testHttpsProxyDefinedWithoutPort() throws HttpException {
        testHttpsProxyDefined(0, 443);
    }

    @Test
    public void testHttpsProxyDefinedWithPort() throws HttpException {
        testHttpsProxyDefined(9443, 9443);
    }

    /**
     * This test is disabled as it is used only for local testing and requires specific Apache HTTPD setup.
     * 
     * @throws HttpException
     *             in case of an error
     */
    // @Test
    public void testLocalWithApache() throws HttpException {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8282");
        System.setProperty("http.proxyUser", "httpProxyUser");
        System.setProperty("http.proxyPassword", "httpProxyPassword");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "8383");
        System.setProperty("https.proxyUser", "httpsProxyUser");
        System.setProperty("https.proxyPassword", "httpsProxyPassword");
        System.setProperty("http.nonProxyHosts", "localhost|*.jahia.com|*.google.de");

        initClient();

        assertTrue(httpClientService.executeGet("http://www.dw.com/en/legal-notice/a-15718492").contains("Legal"));

        assertTrue(httpClientService.executeGet("https://www.oracle.com/legal/privacy/index.html").contains("Privacy"));

        assertTrue(httpClientService.executeGet("https://www.google.de").contains("Deutschland"));
        assertTrue(httpClientService.executeGet("http://localhost:8080/ping.jsp").contains("PONG"));
    }

    @Test
    public void testNonProxyHostsDefined() throws HttpException {
        System.setProperty("http.proxyHost", "httpProxy");
        System.setProperty("http.proxyPort", "9090");
        System.setProperty("http.proxyUser", "httpProxyUser");
        System.setProperty("http.proxyPassword", "httpProxyPassword");
        System.setProperty("https.proxyHost", "httpsProxy");
        System.setProperty("https.proxyPort", "9443");
        System.setProperty("https.proxyUser", "httpsProxyUser");
        System.setProperty("https.proxyPassword", "httpsProxyPassword");

        System.setProperty("http.nonProxyHosts", "localhost|*.jahia.com");

        initClient();

        ensureProxy(null, "httpsProxy", 9443, "httpsProxyUser", "httpsProxyPassword");
        ensureProxy("https://www.test.com", "httpsProxy", 9443, "httpsProxyUser", "httpsProxyPassword");
        ensureProxy("http://www.test.com", "httpProxy", 9090, "httpProxyUser", "httpProxyPassword");

        ensureProxy("www.test.com:9090", "httpProxy", 9090, "httpProxyUser", "httpProxyPassword");

        ensureProxy("https://www.jahia.com", null, -1, null, null);
        ensureProxy("http://www.jahia.com", null, -1, null, null);
        ensureProxy("www.jahia.com", null, -1, null, null);
        ensureProxy("https://localhost:8080", null, -1, null, null);
        ensureProxy("http://localhost:8080", null, -1, null, null);
        ensureProxy("localhost:8080", null, -1, null, null);

        ensureProxy("/aaa.html", null, -1, null, null);
    }

    @Test
    public void testNoProxySettingsDefined() throws HttpException {
        initClient();
        HttpClient httpClient = httpClientService.getHttpClient(null);
        assertNull(httpClient.getState().getProxyCredentials(AuthScope.ANY));
        assertNull(httpClient.getHostConfiguration().getProxyHost());

        assertTrue(httpClient == httpClientService.getHttpClient("http://www.jahia.com"));
        assertTrue(httpClient == httpClientService.getHttpClient("https://www.jahia.com"));
        assertTrue(httpClient == httpClientService.getHttpClient("http://localhost:8080"));
        assertTrue(httpClient == httpClientService.getHttpClient("https://localhost:8080"));
        assertTrue(httpClient == httpClientService.getHttpClient("www.jahia.com:9090"));
        assertTrue(httpClient == httpClientService.getHttpClient("localhost:9090"));
        assertTrue(httpClient == httpClientService.getHttpClient("/aaa.html"));
    }
}
