package org.jahia.bin;

import junit.framework.TestCase;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Test case for find servlet.
 * User: loom
 * Date: Jan 29, 2010
 * Time: 7:18:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class FindTest extends TestCase {

    HttpClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
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
    }

    public void testFindEscapingWithXPath() throws IOException, JSONException, JahiaException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "/jcr:root/sites/mySite//element(*, nt:base)[jcr:contains(.,'{$q}*')]");
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

    public void testSimpleFindWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode([/jcr:root/sites/mySite/]) and contains(base.*,'{$q}*')");
        method.addParameter("q", "a"); 
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

    public void testFindEscapingWithSQL2() throws IOException, JSONException {

        PostMethod method = new PostMethod("http://localhost:8080/cms/find/default/en");
        method.addParameter("query", "select * from [nt:base] as base where isdescendantnode([/jcr:root/sites/mySite/]) and contains(base.*,'{$q}*')");
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
