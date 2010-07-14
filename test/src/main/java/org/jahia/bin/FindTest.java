package org.jahia.bin;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
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
import org.junit.*;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

/**
 * Test case for find servlet.
 * User: loom
 * Date: Jan 29, 2010
 * Time: 7:18:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class FindTest {

    private static Logger logger = Logger.getLogger(FindPrincipalTest.class);
    
    private HttpClient client;
    private final static String TESTSITE_NAME = "findTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private static JahiaSite site;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";

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

            Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
            Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

            JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
            JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
            JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

            TestHelper.createList(englishEditSiteHomeNode, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
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

        // todo we should really insert content to test the find.

        PostMethod loginMethod = new PostMethod("http://localhost:8080/cms/login");
        loginMethod.addParameter("username", "root");
        loginMethod.addParameter("password", "root1234");
        loginMethod.addParameter("redirectActive", "false");
        // the next parameter is required to properly activate the valve check.
        loginMethod.addParameter(LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1");

        int statusCode = client.executeMethod(loginMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + loginMethod.getStatusLine());
        }
    }

    @After
    public void tearDown() throws Exception {

        PostMethod logoutMethod = new PostMethod("http://localhost:8080/cms/logout");
        logoutMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(logoutMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + logoutMethod.getStatusLine());
        }

        logoutMethod.releaseConnection();
    }

    @Test
    public void testFindEscapingWithXPath() throws IOException, JSONException, JahiaException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "/jcr:root"+SITECONTENT_ROOT_NODE+"//element(*, nt:base)[jcr:contains(.,'{$q}*')]");
        method.addParameter("q", "a:+-*\"&()[]{}$/\\%\'"); // to test if the reserved characters work correctly.
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
            System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        // @todo we need to add more tests to validate results.

    }

    @Test
    public void testSimpleFindWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode([/jcr:root"+SITECONTENT_ROOT_NODE+"/]) and contains(base.*,'{$q}*')");
        method.addParameter("q", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        method.addParameter("language", javax.jcr.query.Query.JCR_SQL2);
        method.addParameter("propertyMatchRegexp", "{$q}.*");
        method.addParameter("removeDuplicatePropValues", "true");
        method.addParameter("depthLimit", "1");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        // @todo we need to add more tests to validate results.

    }

    @Test
    public void testFindEscapingWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode([/jcr:root"+SITECONTENT_ROOT_NODE+"/]) and contains(base.*,'{$q}*')");
        method.addParameter("q", "a:+-*\"&()[]{}$/\\%\'"); // to test if the reserved characters work correctly.
        method.addParameter("language", javax.jcr.query.Query.JCR_SQL2);
        method.addParameter("propertyMatchRegexp", "{$q}.*");
        method.addParameter("removeDuplicatePropValues", "true");
        method.addParameter("depthLimit", "1");

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        String responseBody = method.getResponseBodyAsString();

        JSONArray jsonResults = new JSONArray(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

        // @todo we need to add more tests to validate results.

    }

}
