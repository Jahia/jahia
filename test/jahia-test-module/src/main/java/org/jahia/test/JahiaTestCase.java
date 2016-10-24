/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
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
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.bin.BaseTestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Super class for Jahia tests
 * 
 * @author Guillaume Lucazeau
 */
public class JahiaTestCase {

    protected class PostResult {
        public int statusCode;
        public String statusLine;
        public String responseBody;
        
        PostResult(int statusCode, String statusLine, String responseBody) {
            super();
            this.statusCode = statusCode;
            this.statusLine = statusLine;
            this.responseBody = responseBody;
        }
    }

    private final static String PORT = "9090";
    
    private final static String BASE_URL = "http://localhost:" + PORT;

    private static Logger logger = LoggerFactory.getLogger(JahiaTestCase.class);

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

    protected static void publishAll(String nodeIdentifier) throws RepositoryException {
        JCRPublicationService.getInstance().publishByMainId(nodeIdentifier);
    }

    /**
     * @deprecated
     */
    protected static void setSessionSite(JahiaSite site) {
    }

    private HttpClient client;

    protected String getAsText(String relativeUrl) throws IOException {
        String body = StringUtils.EMPTY;
        GetMethod getMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath() + relativeUrl);
        try {
            int responseCode = getHttpClient().executeMethod(getMethod);
            assertEquals("Response code is not OK: " + responseCode, 200, responseCode);
            body = getMethod.getResponseBodyAsString();
        } finally {
            getMethod.releaseConnection();
        }

        return body;
    }

    protected String getBaseServerURL() {
        HttpServletRequest req = getRequest();
        String url = req != null ? req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() : BASE_URL;
        logger.info("Base URL for tests is: " + url);
        return url;
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
                new String[] { "password", password }, new String[] { "restMode", "true" }).statusCode;

        assertEquals("Login failed for user", HttpStatus.SC_OK, statusCode);
    }

    protected void loginRoot() throws IOException {
        login("root", "root1234");
    }

    protected void logout() throws IOException {
        PostResult post = post(getBaseServerURL() + Jahia.getContextPath() + "/cms/logout", new String[] { "redirectActive", "false" });

        if (post.statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + post.statusLine);
        }
    }

    protected PostResult post(String url, String[]... params) throws IOException {
        PostMethod method = new PostMethod(url);
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