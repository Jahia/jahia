/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Test case for find servlet.
 * User: loom
 * Date: Jan 29, 2010
 * Time: 7:18:29 AM
 * 
 */
public class FindTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FindTest.class);
    
    private HttpClient client;
    private final static String TESTSITE_NAME = "findTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private static JahiaSite site;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private static final String COMPLEX_QUERY_VALUE = "b:+-*\"&()[]{}$/\\%\'";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        site = TestHelper.createSite(TESTSITE_NAME);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }

                    session.save();
                    return null;
                }
            });

            JCRPublicationService jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();

            String defaultLanguage = site.getDefaultLanguage();

            Locale englishLocale = Locale.ENGLISH;

            JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
            JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

            JCRNodeWrapper contentList0 = TestHelper.createList(englishEditSiteHomeNode, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            JCRNodeWrapper complexValueNode = contentList0.addNode("complex-value", "jnt:mainContent");
            complexValueNode.setProperty("jcr:title", COMPLEX_QUERY_VALUE);
            complexValueNode.setProperty("body", COMPLEX_QUERY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

            englishEditSession.save();

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
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

            session.logout();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }


    @Before
    public void setUp() throws Exception {
        // Create an instance of HttpClient.
        client = new HttpClient();

        PostMethod loginMethod = new PostMethod(getLoginServletURL());
        loginMethod.addParameter("username", "root");
        loginMethod.addParameter("password", "root1234");
        loginMethod.addParameter("redirectActive", "false");
        // the next parameter is required to properly activate the valve check.
        loginMethod.addParameter(LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1");

        int statusCode = client.executeMethod(loginMethod);
        if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + loginMethod.getStatusLine());
        }
    }

    @After
    public void tearDown() throws Exception {

        PostMethod logoutMethod = new PostMethod(getLogoutServletURL());
        logoutMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(logoutMethod);
        if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + logoutMethod.getStatusLine());
        }

        logoutMethod.releaseConnection();
    }

    @Test
    public void testFindEscapingWithXPath() throws IOException, JSONException, JahiaException {

        PostMethod method = new PostMethod(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en");
        method.addParameter("query", "/jcr:root"+SITECONTENT_ROOT_NODE+"//element(*, nt:base)[jcr:contains(.,'{$q}')]");
        method.addParameter("q", COMPLEX_QUERY_VALUE); // to test if the reserved characters work correctly.
        method.addParameter("language", javax.jcr.query.Query.XPATH);
        method.addParameter("propertyMatchRegexp", "{$q}.*");
        method.addParameter("removeDuplicatePropValues", "true");
        method.addParameter("depthLimit", "1");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        logger.debug("Status code=" + statusCode +" JSON response=[" + responseBody + "]");

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, COMPLEX_QUERY_VALUE);

    }

    @Test
    public void testSimpleFindWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode(["+SITECONTENT_ROOT_NODE+"/]) and contains(base.*,'{$q}*')");
        method.addParameter("q", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        method.addParameter("language", javax.jcr.query.Query.JCR_SQL2);
        method.addParameter("propertyMatchRegexp", "{$q}.*");
        method.addParameter("removeDuplicatePropValues", "true");
        method.addParameter("depthLimit", "1");
        method.addParameter("getNodes", "true");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        logger.debug("Status code=" + statusCode +" JSON response=[" + responseBody + "]");

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

    }

    @Test
    public void testFindEscapingWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode(["+SITECONTENT_ROOT_NODE+"/]) and contains(base.*,'{$q}')");
        method.addParameter("q", COMPLEX_QUERY_VALUE); // to test if the reserved characters work correctly.
        method.addParameter("language", javax.jcr.query.Query.JCR_SQL2);
        method.addParameter("propertyMatchRegexp", "{$q}.*");
        method.addParameter("removeDuplicatePropValues", "true");
        method.addParameter("depthLimit", "1");
        method.addParameter("getNodes", "true");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            logger.error("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        logger.debug("Status code=" + statusCode + " JSON response=[" + responseBody + "]");
        
        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, COMPLEX_QUERY_VALUE);

    }

    private String getBaseServerURL() {
        return "http://localhost:8080";
    }


    private String getLoginServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/cms/login";
    }

    private String getLogoutServletURL() {
        return getBaseServerURL()+ Jahia.getContextPath() + "/cms/logout";
    }

    private String getFindServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + Find.getFindServletPath();
    }

    private void validateFindJSONResults(JSONArray jsonResults, String textToValidate) throws JSONException {
        for (int i=0; i < jsonResults.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonResults.get(i);
            if (jsonObject.has("jcr:score")) {
                // we are handling a row, let's extract the node from it.
                jsonObject = jsonObject.getJSONObject("node");
            }
            JSONArray matchingPropertiesJSONArray = jsonObject.getJSONArray("matchingProperties");
            assertEquals("Expected two matching properties : jcr:title and body", 2, matchingPropertiesJSONArray.length());
            for (int j=0; j < matchingPropertiesJSONArray.length(); j++) {
                String matchingPropertyName = (String) matchingPropertiesJSONArray.get(j);
                String propertyValue = jsonObject.getString(matchingPropertyName);
                assertNotNull("Property " + matchingPropertyName + " not found or null !", propertyValue);
                assertTrue("Expected matching property " + matchingPropertyName + " to start with value " + textToValidate, propertyValue.startsWith(textToValidate));
            }
        }
    }
    
}
