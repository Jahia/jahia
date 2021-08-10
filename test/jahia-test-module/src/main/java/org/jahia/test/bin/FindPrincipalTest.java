/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.bin;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jahia.bin.FindPrincipal;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test of the find principal servlet.
 *
 * @author loom
 * Date: Jun 16, 2010
 * Time: 12:03:19 PM
 */
public class FindPrincipalTest extends JahiaTestCase {

    private final static String TESTSITE_NAME = "findPrincipalTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FindPrincipalTest.class);
    private CloseableHttpClient client;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.WEB_TEMPLATES);
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    session.save();
                    return null;
                }
            });

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Before
    public void setUp() throws Exception {

        // Create an instance of HttpClient.
        client = getHttpClient();

        // todo we should really insert content to test the find.

        HttpPost loginMethod = new HttpPost(getLoginServletURL());
            SimpleCredentials rootUserCredentials = JahiaTestCase.getRootUserCredentials();
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("username", rootUserCredentials.getUserID()));
            nvps.add(new BasicNameValuePair("password", new String(rootUserCredentials.getPassword())));
            nvps.add(new BasicNameValuePair("redirectActive", "false"));
            // the next parameter is required to properly activate the valve check.
            nvps.add(new BasicNameValuePair(LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1"));
            loginMethod.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = client.execute(loginMethod)) {
            assertEquals("Method failed: " + response.getCode(), HttpStatus.SC_OK, response.getCode());
        }
    }

    @After
    public void tearDown() throws Exception {
        HttpPost logoutMethod = new HttpPost(getLogoutServletURL());
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("redirectActive", "false"));
        logoutMethod.setEntity(new UrlEncodedFormEntity(nvps));

        try (CloseableHttpResponse response = client.execute(logoutMethod)) {
            assertEquals("Method failed: " + response.getCode(), HttpStatus.SC_OK, response.getCode());
        }
    }

    @Test
    public void testFindUsers() throws IOException, JSONException, JahiaException {

        HttpPost method = new HttpPost(getFindPrincipalServletURL());
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("principalType", "users"));
            nvps.add(new BasicNameValuePair("wildcardTerm", "*root*"));
            method.setEntity(new UrlEncodedFormEntity(nvps));

            // Execute the method.
        try (CloseableHttpResponse response = client.execute(method)) {
            assertEquals("Method failed: " + response.getCode(), HttpStatus.SC_OK, response.getCode());

            // Read the response body.
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[")
                    .append(EntityUtils.toString(response.getEntity())).append("]");
            String responseBody = responseBodyBuilder.toString();

            JSONArray jsonResults = new JSONArray(responseBody);

            assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);
        } catch (ParseException e) {
            throw new IOException(e);
        }

        // @todo we need to add more tests to validate results.

    }

    @Test
    public void testFindGroups() throws IOException, JSONException {

        HttpPost method = new HttpPost(getFindPrincipalServletURL());
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("principalType", "groups"));
        nvps.add(new BasicNameValuePair("siteKey", TESTSITE_NAME));
        nvps.add(new BasicNameValuePair("wildcardTerm", "*administrators*"));
        method.setEntity(new UrlEncodedFormEntity(nvps));

        // Execute the method.
        try (CloseableHttpResponse response = client.execute(method)) {

            assertEquals("Method failed: " + response.getCode(),
                    HttpStatus.SC_OK, response.getCode());

            // Read the response body.
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[")
                    .append(EntityUtils.toString(response.getEntity())).append("]");
            String responseBody = responseBodyBuilder.toString();

            JSONArray jsonResults = new JSONArray(responseBody);

            assertNotNull(
                    "A proper JSONObject instance was expected, got null instead",
                    jsonResults);
        } catch (ParseException e) {
            throw new IOException(e);
        }

        // @todo we need to add more tests to validate results.

    }

    private String getLoginServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + "/cms/login";
    }

    private String getLogoutServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + "/cms/logout";
    }

    private String getFindPrincipalServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + FindPrincipal.getFindPrincipalServletPath();
    }

}
