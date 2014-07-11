/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.bin.BaseTestController;

/**
 * Super class for Jahia tests
 * 
 * @author Guillaume Lucazeau
 */
public class JahiaTestCase {

    private final static String PORT = "9090";
    
    private final static String BASE_URL = "http://localhost:" + PORT;

    private static Logger logger = Logger.getLogger(JahiaTestCase.class);

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

    protected static void publishAll(String nodeIdentifier) throws RepositoryException {
        JCRPublicationService.getInstance().publishByMainId(nodeIdentifier);
    }

    /**
     * @deprecated
     */
    protected static void setSessionSite(JahiaSite site) {
    }

    private HttpClient client;

    protected String getAsText(String relativeUrl) {
        String body = StringUtils.EMPTY;
        GetMethod getMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath() + relativeUrl);
        try {
            int responseCode = getHttpClient().executeMethod(getMethod);
            assertEquals("Response code is not OK: " + responseCode, 200, responseCode);
            body = getMethod.getResponseBodyAsString();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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

    protected void login(String username, String password) {
        PostMethod loginMethod = new PostMethod(getBaseServerURL() + Jahia.getContextPath() + "/cms/login");
        loginMethod.addParameter("username", username);
        loginMethod.addParameter("password", password);
        loginMethod.addParameter("restMode", "true");

        try {
            int statusCode = getHttpClient().executeMethod(loginMethod);
            assertEquals("Login failed for user", HttpStatus.SC_OK, statusCode);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            loginMethod.releaseConnection();
        }
    }

    protected void loginRoot() {
        login("root", "root1234");
    }

    protected void logout() {
        PostMethod logoutMethod = new PostMethod(getBaseServerURL() + Jahia.getContextPath() + "/cms/logout");
        logoutMethod.addParameter("redirectActive", "false");

        try {
            int statusCode = getHttpClient().executeMethod(logoutMethod);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + logoutMethod.getStatusLine());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            logoutMethod.releaseConnection();
        }
    }

}