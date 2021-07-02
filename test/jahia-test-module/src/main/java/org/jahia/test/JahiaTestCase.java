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
package org.jahia.test;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.bin.BaseTestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Super class for Jahia tests
 * 
 * @author Guillaume Lucazeau
 */
public class JahiaTestCase {

    protected class PostResult {
        private int statusCode;
        private String statusLine;
        private String responseBody;
        
        PostResult(int statusCode, String statusLine, String responseBody) {
            super();
            this.statusCode = statusCode;
            this.statusLine = statusLine;
            this.responseBody = responseBody;
        }
        
        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusLine() {
            return statusLine;
        }

        public String getResponseBody() {
            return responseBody;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(JahiaTestCase.class);
    
    private static final String PORT = "9090";
    
    private static final String BASE_URL = "http://localhost:" + PORT;

    private static boolean baseUrlForTestsLogged;
    
    private static SimpleCredentials rootUserCredentials;

    private HttpClient client;
    /**
     * Returns the <code>HttpServletRequest</code> object for the current call.
     * 
     * @return current {@link HttpServletRequest} object
     */
    protected static final HttpServletRequest getRequest() {
        return BaseTestController.getThreadLocalRequest();
    }

    /**
     * Returns the <code>HttpServletResponse</code> object for the current call.
     * 
     * @return current {@link HttpServletResponse} object
     */
    protected static final HttpServletResponse getResponse() {
        return BaseTestController.getThreadLocalResponse();
    }

    protected static final JahiaUser getUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    protected static final JCRUserNode getUserNode(JahiaUser admin) {
        return ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByPath(admin.getLocalPath());
    }
    
    protected static final SimpleCredentials getRootUserCredentials() {
        if (rootUserCredentials == null) {
            rootUserCredentials = new SimpleCredentials("root", "root1234".toCharArray());
        }
        return rootUserCredentials;
    }

    protected static void publishAll(String nodeIdentifier) throws RepositoryException {
        JCRPublicationService.getInstance().publishByMainId(nodeIdentifier);
    }

    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.warn("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    
    @SuppressWarnings("java:S2696")
    protected String getBaseServerURL() {
        HttpServletRequest req = getRequest();
        String url = req != null ? req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() : BASE_URL;
        if (!baseUrlForTestsLogged) {
            logger.info("Base URL for tests is: {}", url);
            baseUrlForTestsLogged = true;
        }
        return url;
    }

    protected String getAsText(String relativeUrl) {
        return getAsText(relativeUrl, 200);
    }

    protected String getAsText(String relativeUrl, int expectedResponseCode) {
        return getAsText(relativeUrl, null, expectedResponseCode, null);
    }

    protected String getAsText(String relativeUrl, Map<String, String> requestHeaders, int expectedResponseCode,
            Map<String, List<String>> collectedResponseHeaders) {
        String body = StringUtils.EMPTY;
        GetMethod getMethod = createGetMethod(relativeUrl);
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                getMethod.addRequestHeader(header.getKey(), header.getValue());
            }
        }
        try {
            int responseCode = getHttpClient().executeMethod(getMethod);
            assertEquals("Response code for URL " + relativeUrl + " is incorrect", expectedResponseCode, responseCode);
            body = getMethod.getResponseBodyAsString();
            if (collectedResponseHeaders != null) {
                for (Header header : getMethod.getResponseHeaders()) {
                    String headerName = header.getName();
                    if (!collectedResponseHeaders.containsKey(headerName)) {
                        collectedResponseHeaders.put(headerName, new LinkedList<>());
                    }
                    collectedResponseHeaders.get(headerName).add(header.getValue());
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            getMethod.releaseConnection();
        }

        return body;
    }

    protected GetMethod createGetMethod(String relativeUrl) {
        GetMethod getMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath() + relativeUrl);
        getMethod.addRequestHeader("Origin", getBaseServerURL());
        return getMethod;
    }

    protected String getBaseServerURLPort() {
        HttpServletRequest req = getRequest();
        return req != null ? String.valueOf(getRequest().getServerPort()) : PORT;
    }

    protected HttpClient getHttpClient() {
        if (client == null) {
            client = new HttpClient();
        }
        return client;
    }

    protected void login(String username, String password) throws IOException {
        int statusCode = post(getBaseServerURL() + Jahia.getContextPath() + "/cms/login", new String[] { "username", username },
                new String[] { "password", password }, new String[] { "restMode", "true" }).getStatusCode();

        assertEquals("Login failed for user", HttpStatus.SC_OK, statusCode);
    }

    protected void loginRoot() throws IOException {
        login(getRootUserCredentials().getUserID(), new String(getRootUserCredentials().getPassword()));
    }

    protected void logout() throws IOException {
        PostResult post = post(getBaseServerURL() + Jahia.getContextPath() + "/cms/logout", (new String[] { "redirectActive", "false" }));

        if (post.getStatusCode() != HttpStatus.SC_OK) {
            logger.error("Method failed: {}", post.getStatusLine());
        }
    }

    protected PostResult post(String url, String[]... params) throws IOException {
        PostMethod method = new PostMethod(url);
        method.addRequestHeader("Origin", getBaseServerURL());
        for (String[] param : params) {
            method.addParameter(param[0], param[1]);
        }

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        int statusCode = 0;
        String statusLine = null;
        String responseBody = null;
        try {
            // Execute the method.
            statusCode = getHttpClient().executeMethod(method);

            statusLine = method.getStatusLine().toString();
            if (statusCode != HttpStatus.SC_OK) {
                logger.warn("Method failed: {}", statusLine);
            }

            // Read the response body.
            responseBody = method.getResponseBodyAsString();
        } finally {
            method.releaseConnection();
        }

        return new PostResult(statusCode, statusLine, responseBody);
    }

}