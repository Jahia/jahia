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

package org.jahia.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
<<<<<<< .working
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRPublicationService;
=======
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
>>>>>>> .merge-right.r44465

/**
 * Super class for Jahia tests
 * 
 * @author Guillaume Lucazeau
 */
public class JahiaTestCase {

    private static Logger logger = Logger.getLogger(JahiaTestCase.class);

<<<<<<< .working
    private final static String PORT = "9090";
    private final static String BASE_URL = "http://localhost:" + PORT;
=======
    private final static String PORT = "8080";
    private final static String BASE_URL = "http://localhost:" + PORT;
>>>>>>> .merge-right.r44465

    private HttpClient client;
    
    protected static void publishAll(String nodeIdentifier) throws RepositoryException {
        JCRPublicationService.getInstance().publishByMainId(nodeIdentifier);
    }
    
    protected String getAsText(String relativeUrl) {
        String body = StringUtils.EMPTY;
        GetMethod getMethod = new GetMethod(getBaseServerURL() + Jahia.getContextPath()
                + relativeUrl);
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
        ProcessingContext ctx = Jahia.getThreadParamBean();
        String url = ctx != null ? ctx.getScheme() + "://" + ctx.getServerName() + ":" + ctx.getServerPort() : BASE_URL;
        logger.info("Base URL for tests is: " + url);
        return url;
    }

    protected String getBaseServerURLPort() {
        ProcessingContext ctx = Jahia.getThreadParamBean();
        return ctx !=  null ? String.valueOf(ctx.getServerPort()) : PORT;
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