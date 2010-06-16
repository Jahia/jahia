package org.jahia.bin;

import junit.framework.TestCase;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.json.JSONArray;
import org.json.JSONException;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Test of the find principal servlet.
 *
 * @author loom
 *         Date: Jun 16, 2010
 *         Time: 12:03:19 PM
 */
public class FindPrincipalTest extends TestCase {

    private static Logger logger = Logger.getLogger(FindPrincipalTest.class);

    private HttpClient client;
    private final static String TESTSITE_NAME = "findPrincipalTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    private JahiaSite site;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.ACME_TEMPLATES,
                                null);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }
                    session.save();
                    return null;
                }
            });

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }

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

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.

        PostMethod logoutMethod = new PostMethod("http://localhost:8080/cms/logout");
        logoutMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(logoutMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + logoutMethod.getStatusLine());
        }

        logoutMethod.releaseConnection();

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

    public void testFindUsers() throws IOException, JSONException, JahiaException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/findPrincipal");
        method.addParameter("principalType", "users");
        method.addParameter("siteKey", TESTSITE_NAME);
        method.addParameter("wildcardTerm", "*root*");

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

    public void testFindGroups() throws IOException, JSONException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/findPrincipal");
        method.addParameter("principalType", "groups");
        method.addParameter("wildcardTerm", "*administrators*");

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
