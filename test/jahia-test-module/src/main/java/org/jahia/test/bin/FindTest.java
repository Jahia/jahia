/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Locale;

import javax.jcr.RepositoryException;

import org.apache.commons.httpclient.HttpStatus;
import org.jahia.api.Constants;
import org.jahia.bin.Find;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

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

    private static JahiaSite site;
    private static final String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private static final String COMPLEX_QUERY_VALUE = "b:+-*\"&()[]{}$/\\%\'";
    
    private static final String FIND_DISABLED = "jahia.find.disabled";
    private static String isFindDisabled;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
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
            
            isFindDisabled = SettingsBean.getInstance().getPropertiesFile().getProperty(FIND_DISABLED);
            setFindServletDisabled("false");
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
    public void setUp() throws Exception {
        loginRoot();
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }

    @Test
    public void testFindEscapingWithXPath() throws IOException, JSONException, JahiaException {

        @SuppressWarnings("deprecation")
        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {"query", "/jcr:root" + SITECONTENT_ROOT_NODE
                    + "//element(*, nt:base)[jcr:contains(.,'{$q}')]"},
            new String[] {"q", COMPLEX_QUERY_VALUE}, // to test if the reserved characters work correctly.
            new String[] {"language", javax.jcr.query.Query.XPATH},
            new String[] {"propertyMatchRegexp", "{$q}.*"},
            new String[] {"removeDuplicatePropValues", "true"},
            new String[] {"depthLimit", "1"});
 
        assertEquals("Method failed: " + post.statusLine, HttpStatus.SC_OK, post.statusCode);

        // Read the response body.
        String responseBody = post.responseBody;
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.responseBody).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug("Status code={} JSON response={}", post.statusCode, post.responseBody);

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, COMPLEX_QUERY_VALUE);
    }

    @Test
    public void testSimpleFindWithSQL2() throws IOException, JSONException {

        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {"query",
                    "select * from [nt:base] as base where isdescendantnode(["
                            + SITECONTENT_ROOT_NODE
                            + "/]) and contains(base.*,'{$q}*')"},
            new String[] {"q", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE},
            new String[] {"language", javax.jcr.query.Query.JCR_SQL2},
            new String[] {"propertyMatchRegexp", "{$q}.*"},
            new String[] {"removeDuplicatePropValues", "true"},
            new String[] {"depthLimit", "1"},
            new String[] {"getNodes", "true"});

        assertEquals("Method failed: " + post.statusLine, HttpStatus.SC_OK, post.statusCode);

        // Read the response body.
        String responseBody = post.responseBody;
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.responseBody).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug("Status code={} JSON response={}", post.statusCode, post.responseBody);

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

        validateFindJSONResults(jsonResults, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
    }

    @Test
    public void testFindEscapingWithSQL2() throws IOException, JSONException {

        PostResult post = post(getFindServletURL()+ "/"+Constants.EDIT_WORKSPACE+"/en",
            new String[] {"query",
                    "select * from [nt:base] as base where isdescendantnode(["
                            + SITECONTENT_ROOT_NODE
                            + "/]) and contains(base.*,'{$q}')"},
            new String[] {"q", COMPLEX_QUERY_VALUE}, // to test if the reserved characters work correctly.
            new String[] {"language", javax.jcr.query.Query.JCR_SQL2},
            new String[] {"propertyMatchRegexp", "{$q}.*"},
            new String[] {"removeDuplicatePropValues", "true"},
            new String[] {"depthLimit", "1"},
            new String[] {"getNodes", "true"});

        assertEquals("Method failed: " + post.statusLine, HttpStatus.SC_OK, post.statusCode);

        // Read the response body.
        String responseBody = post.responseBody;
        if (!responseBody.startsWith("[")) {
            StringBuilder responseBodyBuilder = new StringBuilder();
            responseBodyBuilder.append("[").append(post.responseBody).append("]");
            responseBody = responseBodyBuilder.toString();
        }

        logger.debug("Status code={} JSON response={}", post.statusCode, post.responseBody);

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        assertTrue("Result should not be empty !", (jsonResults.length() > 0));

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
    public void testFiltering() throws IOException, JSONException {

        PostResult post = post(getFindServletURL() + "/" + Constants.LIVE_WORKSPACE + "/en", new String[] { "query",
                "select * from [jnt:user] where ischildnode('/users/')" }, new String[] { "depthLimit", "10" });

        logger.debug("Status code={} JSON response=[]", post.statusCode, post.responseBody);

        assertFalse("Root user is not filtered out from the results", post.responseBody.contains("/users/root"));
        assertFalse("Password policy nodes are not filtered out from the results",
                post.responseBody.contains("jnt:passwordHistory"));

        post = post(getFindServletURL() + "/" + Constants.LIVE_WORKSPACE + "/en", new String[] { "query",
                "select * from [jnt:user]" }, new String[] { "depthLimit", "10" }, new String[] { "limit", "10" });

        assertFalse("j:password property is not filtered out from the results",
                post.responseBody.contains("j:password"));
        assertFalse("Password policy nodes are not filtered out from the results",
                post.responseBody.contains("jnt:passwordHistory"));
    }
}
