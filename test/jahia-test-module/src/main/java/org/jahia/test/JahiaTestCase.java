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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test;

import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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

    private CloseableHttpClient client;
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
        return getAsText(relativeUrl, requestHeaders, expectedResponseCode, collectedResponseHeaders, new HttpClientContext());
    }

    protected String getAsText(String relativeUrl, Map<String, String> requestHeaders, int expectedResponseCode,
            Map<String, List<String>> collectedResponseHeaders, HttpClientContext context) {
        String body = StringUtils.EMPTY;
        HttpGet getMethod = createHttpGet(relativeUrl);
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                getMethod.addHeader(header.getKey(), header.getValue());
            }
        }
        try (CloseableHttpResponse response = getHttpClient().execute(getMethod, context)) {
            assertEquals("Response code for URL " + relativeUrl + " is incorrect", expectedResponseCode, response.getCode());
            body = response.getEntity() == null ? null : EntityUtils.toString(response.getEntity());
            if (collectedResponseHeaders != null) {
                for (Header header : response.getHeaders()) {
                    String headerName = header.getName();
                    if (!collectedResponseHeaders.containsKey(headerName)) {
                        collectedResponseHeaders.put(headerName, new LinkedList<>());
                    }
                    collectedResponseHeaders.get(headerName).add(header.getValue());
                }
            }
        } catch (IOException | ParseException e) {
            logger.error(e.getMessage(), e);
        }

        return body;
    }

    protected HttpGet createHttpGet(String relativeUrl) {
        HttpGet getMethod = new HttpGet(getBaseServerURL() + Jahia.getContextPath() + relativeUrl);
        getMethod.addHeader("Origin", getBaseServerURL());
        return getMethod;
    }

    protected String getBaseServerURLPort() {
        HttpServletRequest req = getRequest();
        return req != null ? String.valueOf(getRequest().getServerPort()) : PORT;
    }

    protected CloseableHttpClient getHttpClient() {
        if (client == null) {
            client = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException {
                    URI uri = super.getLocationURI(request, response, context);
                    try {
                        return new URI(uri.toString().replace("%3Bjsessionid%3D", ";jsessionid="));
                    } catch (URISyntaxException e) {
                        return uri;
                    }
                }
            }).build();
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
        HttpPost method = new HttpPost(url);
        method.addHeader("Origin", getBaseServerURL());

        List <NameValuePair> nvps = new ArrayList<>();
        for (String[] param : params) {
            nvps.add(new BasicNameValuePair(param[0], param[1]));
        }
        method.setEntity(new UrlEncodedFormEntity(nvps));

        int statusCode;
        String statusLine;
        String responseBody;

        try (CloseableHttpResponse response = getHttpClient().execute(method)) {
            statusCode = response.getCode();
            statusLine = response.getReasonPhrase();
            if (response.getCode() != HttpStatus.SC_OK) {
                logger.warn("Method failed: {} {}", response.getCode(), response.getReasonPhrase());
            }

            // Read the response body.
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new IOException(e);
        }

        return new PostResult(statusCode, statusLine, responseBody);
    }

}
