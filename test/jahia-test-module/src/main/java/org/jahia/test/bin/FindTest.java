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

import org.apache.hc.core5.http.HttpStatus;
import org.jahia.api.Constants;
import org.jahia.bin.Find;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

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
public class FindTest extends JahiaTestCase {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FindTest.class);
    
    private static final String TESTSITE_NAME = "findTestSite";
    private static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private static final String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private static final String COMPLEX_QUERY_VALUE = "b:+-*\"&()[]{}$/\\%\'";
    private static final String METHOD_FAILED = "Method failed: ";
    private static final String FIND_DISABLED = "jahia.find.disabled";
    private static final String JSON_RESPONSE_STATUS = "Status code={} JSON response={}";
    private static final String QUERY_PARAM = "query";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PROPERTY_MATCH_REGEXP_PARAM = "propertyMatchRegexp";
    private static final String PROPERTY_MATCH_REGEXP = "{$q}.*";
    private static final String REMOVE_DUPLICATE_PARAM = "removeDuplicatePropValues";
    private static final String DEPTH_LIMIT_PARAM = "depthLimit";
    private static final String RESULT_NOT_EMPTY_MSG = "Result should not be empty !";
    private static final String JSONOBJECT_NULL_MSG = "A proper JSONObject instance was expected, got null instead";
    
    private static String isFindDisabled;
    private static JahiaSite site;

    @BeforeClass
    public static void oneTimeSetUp() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        site = TestHelper.createSite(TESTSITE_NAME);
                        session.save();
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    return null;
                }
            });

            JCRPublicationService jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();

            String defaultLanguage = site.getDefaultLanguage();

            Locale englishLocale = Locale.ENGLISH;

            JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
            JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishEditSiteHomeNode = englishEditSiteRootNode.getNode("home");

            JCRNodeWrapper contentList0 = TestHelper.createList(englishEditSiteHomeNode, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            JCRNodeWrapper complexValueNode = contentList0.addNode("complex-value", "jnt:mainContent");
            complexValueNode.setProperty("jcr:title", COMPLEX_QUERY_VALUE);
            complexValueNode.setProperty("body", COMPLEX_QUERY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
            TestHelper.createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

            englishEditSession.save();
            
            isFindDisabled = SettingsBean.getInstance().getPropertiesFile().getProperty(FIND_DISABLED);
            setFindServletDisabled("false");
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        } finally {
            setFindServletDisabled(isFindDisabled);
        }
    }

    
    private static void setFindServletDisabled(String disabled) {
        SettingsBean.getInstance().getPropertiesFile().setProperty(FIND_DISABLED, disabled);
        ApplicationContext ctx = (ApplicationContext) JahiaContextLoaderListener.getServletContext().getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet");
        Find findServlet = (Find)ctx.getBean("org.jahia.bin.Find");
        findServlet.setDisabled(Boolean.parseBoolean(disabled));
    }

    @Before
    public void setUp() throws IOException {
        loginRoot();
    }

    @After
    public void tearDown() throws IOException {
        logout();
    }

    @Test
    public void testFindEscapingWithXPath() throws IOException, JSONException {

        @SuppressWarnings("deprecation")
        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {QUERY_PARAM, "/jcr:root" + SITECONTENT_ROOT_NODE
                    + "//element(*, nt:base)[jcr:contains(.,'{$q}')]"},
            new String[] {"q", COMPLEX_QUERY_VALUE}, // to test if the reserved characters work correctly.
            new String[] {LANGUAGE_PARAM, javax.jcr.query.Query.XPATH},
            new String[] {PROPERTY_MATCH_REGEXP_PARAM, PROPERTY_MATCH_REGEXP},
            new String[] {REMOVE_DUPLICATE_PARAM, "true"},
            new String[] {DEPTH_LIMIT_PARAM, "1"});
 
        assertEquals(METHOD_FAILED + post.getStatusLine(), HttpStatus.SC_OK, post.getStatusCode());

        // Read the response body.
        String responseBody = post.getResponseBody();
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.getResponseBody()).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug(JSON_RESPONSE_STATUS, post.getStatusCode(), post.getResponseBody());

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull(JSONOBJECT_NULL_MSG, jsonResults);

        assertTrue(RESULT_NOT_EMPTY_MSG, (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, COMPLEX_QUERY_VALUE);
    }

    @Test
    public void testSimpleFindWithSQL2() throws IOException, JSONException {

        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {QUERY_PARAM,
                    "select * from [nt:base] as base where isdescendantnode(["
                            + SITECONTENT_ROOT_NODE
                            + "/]) and contains(base.*,'{$q}*')"},
            new String[] {"q", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE},
            new String[] {LANGUAGE_PARAM, javax.jcr.query.Query.JCR_SQL2},
            new String[] {PROPERTY_MATCH_REGEXP_PARAM, PROPERTY_MATCH_REGEXP},
            new String[] {REMOVE_DUPLICATE_PARAM, "true"},
            new String[] {DEPTH_LIMIT_PARAM, "1"},
            new String[] {"getNodes", "true"});

        assertEquals(METHOD_FAILED + post.getStatusLine(), HttpStatus.SC_OK, post.getStatusCode());

        // Read the response body.
        String responseBody = post.getResponseBody();
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.getResponseBody()).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug(JSON_RESPONSE_STATUS, post.getStatusCode(), post.getResponseBody());

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull(JSONOBJECT_NULL_MSG, jsonResults);

        assertTrue(RESULT_NOT_EMPTY_MSG, (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
    }

    @Test
    public void testFindEscapingWithSQL2() throws IOException, JSONException {

        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {QUERY_PARAM,
                    "select * from [nt:base] as base where isdescendantnode(["
                            + SITECONTENT_ROOT_NODE
                            + "/]) and contains(base.*,'{$q}')"},
            new String[] {"q", COMPLEX_QUERY_VALUE}, // to test if the reserved characters work correctly.
            new String[] {LANGUAGE_PARAM, javax.jcr.query.Query.JCR_SQL2},
            new String[] {PROPERTY_MATCH_REGEXP_PARAM, PROPERTY_MATCH_REGEXP},
            new String[] {REMOVE_DUPLICATE_PARAM, "true"},
            new String[] {DEPTH_LIMIT_PARAM, "1"},
            new String[] {"getNodes", "true"});

        assertEquals(METHOD_FAILED + post.getStatusLine(), HttpStatus.SC_OK, post.getStatusCode());

        // Read the response body.
        String responseBody = post.getResponseBody();
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.getResponseBody()).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug(JSON_RESPONSE_STATUS, post.getStatusCode(), post.getResponseBody());

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull(JSONOBJECT_NULL_MSG, jsonResults);

        assertTrue(RESULT_NOT_EMPTY_MSG, (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, COMPLEX_QUERY_VALUE);
    }
    
    private String getFindServletURL() {
        return getBaseServerURL() + Jahia.getContextPath() + Find.getFindServletPath();
    }

    private void validateFindJSONResults(JSONArray jsonResults, String textToValidate) throws JSONException {
        for (int i = 0; i < jsonResults.length(); i++) {
            if (jsonResults.get(i) instanceof JSONArray) { 
                JSONArray jsonArray = (JSONArray) jsonResults.get(i);
                for (int j = 0; j < jsonArray.length(); j++) {
                    validateFindJSONResults((JSONObject)jsonResults.get(j), textToValidate);
                }
            } else {
                validateFindJSONResults((JSONObject)jsonResults.get(i), textToValidate);                
            }
        }
    }

    private void validateFindJSONResults(JSONObject jsonObject,
            String textToValidate) throws JSONException {
        if (jsonObject.has("jcr:score")) {
            // we are handling a row, let's extract the node from it.
            jsonObject = jsonObject.getJSONObject("node");
        }
        JSONArray matchingPropertiesJSONArray = jsonObject
                .getJSONArray("matchingProperties");
        assertEquals("Expected two matching properties : jcr:title and body",
                2, matchingPropertiesJSONArray.length());
        for (int k = 0; k < matchingPropertiesJSONArray.length(); k++) {
            String matchingPropertyName = (String) matchingPropertiesJSONArray
                    .get(k);
            String propertyValue = jsonObject.getString(matchingPropertyName);
            assertNotNull("Property " + matchingPropertyName
                    + " not found or null !", propertyValue);
            assertTrue("Expected matching property " + matchingPropertyName
                    + " to start with value " + textToValidate,
                    propertyValue.startsWith(textToValidate));
        }
    }

    @Test
    public void testFiltering() throws IOException {

        PostResult post = post(getFindServletURL() + "/" + Constants.LIVE_WORKSPACE + "/en", new String[] { QUERY_PARAM,
                "select * from [jnt:user] where ischildnode('/users/')" }, new String[] { DEPTH_LIMIT_PARAM, "10" });

        logger.debug(JSON_RESPONSE_STATUS, post.getStatusCode(), post.getResponseBody());

        assertFalse("Root user is not filtered out from the results", post.getResponseBody().contains("/users/root"));
        assertFalse("Password policy nodes are not filtered out from the results",
                post.getResponseBody().contains("jnt:passwordHistory"));

        post = post(getFindServletURL() + "/" + Constants.LIVE_WORKSPACE + "/en", new String[] { QUERY_PARAM,
                "select * from [jnt:user]" }, new String[] { DEPTH_LIMIT_PARAM, "10" }, new String[] { "limit", "10" });

        assertFalse("j:password property is not filtered out from the results",
                post.getResponseBody().contains("j:password"));
        assertFalse("Password policy nodes are not filtered out from the results",
                post.getResponseBody().contains("jnt:passwordHistory"));
    }
}
