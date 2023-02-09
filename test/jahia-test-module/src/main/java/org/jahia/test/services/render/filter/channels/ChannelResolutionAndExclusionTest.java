/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render.filter.channels;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.ImportUUIDBehavior;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class ChannelResolutionAndExclusionTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelResolutionAndExclusionTest.class);
    private static final String TESTSITE_NAME = "channelsTestSite";
    private static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web-space");
            assertNotNull(site);

            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory()
                    .getCurrentUserSession();
            InputStream importStream = ChannelResolutionAndExclusionTest.class.getClassLoader().getResourceAsStream("imports/importChannelsTest.xml");
            session.importXML(SITECONTENT_ROOT_NODE, importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();
            JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
            JCRPublicationService.getInstance().publishByMainId(siteNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                    true, null);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            Assert.fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.warn("Exception during test tearDown", e);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testGenericChannelResolution() throws Exception, IOException {
        CloseableHttpClient client = getHttpClient();
        HttpGet nodeGet = new HttpGet(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try (CloseableHttpResponse responseCode = client.execute(nodeGet)) {
            assertEquals("Response code " + responseCode, HttpServletResponse.SC_OK, responseCode.getCode());
            response = EntityUtils.toString(responseCode.getEntity());
            assertTrue("This text should be displayed on generic channel", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        }
    }

    @Test
    public void testSupportedChannelResolution() throws Exception, IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3")
                .build();
        HttpGet nodeGet = new HttpGet(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try (CloseableHttpResponse responseCode = client.execute(nodeGet)) {
            assertEquals("Response code " + responseCode.getCode(), HttpServletResponse.SC_OK, responseCode.getCode());
            response = EntityUtils.toString(responseCode.getEntity());
            assertFalse("This text shouldn't be displayed on iPhone channel", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        }
    }

    @Test
    public void testUnsupportedChannelResolution() throws Exception, IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/12.0.024; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.12344")
                .build();
        HttpGet nodeGet = new HttpGet(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try (CloseableHttpResponse responseCode = client.execute(nodeGet)) {
            assertEquals("Response code " + responseCode.getCode(), HttpServletResponse.SC_OK, responseCode.getCode());
            response = EntityUtils.toString(responseCode.getEntity());
            assertTrue("Non supported channel should fall back on generic", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        }
    }

    @Test
    public void testNonExcludedChannel() throws Exception, IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3")
                .build();
        HttpGet nodeGet = new HttpGet(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try (CloseableHttpResponse responseCode = client.execute(nodeGet)) {
            assertEquals("Response code " + responseCode.getCode(), HttpServletResponse.SC_OK, responseCode.getCode());
            response = EntityUtils.toString(responseCode.getEntity());
            assertTrue("This text should be displayed when channel is not iPad", response.contains("This text shouldn&#39;t appear on an iPad."));
        }
    }

    @Test
    public void testExcludedChannel() throws Exception, IOException {
        CloseableHttpClient client = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3")
                .build();
        HttpGet nodeGet = new HttpGet(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try (CloseableHttpResponse responseCode = client.execute(nodeGet)) {
            assertEquals("Response code " + responseCode.getCode(), HttpServletResponse.SC_OK, responseCode.getCode());
            response = EntityUtils.toString(responseCode.getEntity());
            assertFalse("This text should be hidden when channel is iPad", response.contains("This text shouldn&#39;t appear on an iPad."));
        }
    }

}
