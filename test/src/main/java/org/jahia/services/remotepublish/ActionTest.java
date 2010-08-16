/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.remotepublish;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONObject;

import java.net.URL;

/**
 * Unit test for remote publishing
 *
 */
public class ActionTest extends TestCase {
    private static Logger logger = Logger.getLogger(ActionTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrRPTest";

    @Override
    protected void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    public void testRemotePublishAction() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper node = session.getNode("/sites/jcrRPTest/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        JCRNodeWrapper target = node.addNode("target", "jnt:page");

        JCRNodeWrapper rp = node.addNode("rp", "jnt:remotePublication");
        String baseurl = "http://localhost:8080" + Jahia.getContextPath() + "/cms";

        rp.setProperty("remoteUrl", baseurl);
        rp.setProperty("node", source);
        rp.setProperty("remotePath", target.getPath());
        rp.setProperty("remoteUser", "root");
        rp.setProperty("remotePassword", "root1234");

        session.save();

        JCRPublicationService.getInstance().publish("/sites/jcrRPTest/home", Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                true);

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        final URL url = new URL(baseurl + "/render/default/en" + rp.getPath() + ".remotepublish.do");

        Credentials defaultcreds = new UsernamePasswordCredentials("root", "root1234");
        client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);

        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        PostMethod remotePublish = new PostMethod(url.toExternalForm());
        remotePublish.addRequestHeader(new Header("accept", "application/json"));

        client.executeMethod(remotePublish);
        assertEquals("Bad result code", 200, remotePublish.getStatusCode());

        JSONObject response = new JSONObject(remotePublish.getResponseBodyAsString());
        remotePublish.releaseConnection();

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.LIVE_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        target = liveSession.getNodeByUUID(target.getIdentifier());
        assertTrue("Node not added", target.hasNode("page1"));

    }


}