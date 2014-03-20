/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

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
    public void testGenericChannelResolution() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            response = nodeGet.getResponseBodyAsString();
            assertTrue("This text should be displayed on generic channel", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testSupportedChannelResolution() throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            response = nodeGet.getResponseBodyAsString();
            assertFalse("This text shouldn't be displayed on iPhone channel", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testUnsupportedChannelResolution() throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (SymbianOS/9.4; Series60/5.0 NokiaN97-1/12.0.024; Profile/MIDP-2.1 Configuration/CLDC-1.1; en-us) AppleWebKit/525 (KHTML, like Gecko) BrowserNG/7.1.12344");
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            response = nodeGet.getResponseBodyAsString();
            assertTrue("Non supported channel should fall back on generic", response.contains("This banner shouldn&#39;t appear on an iPhone."));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testNonExcludedChannel() throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            response = nodeGet.getResponseBodyAsString();
            assertTrue("This text should be displayed when channel is not iPad", response.contains("This text shouldn&#39;t appear on an iPad."));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testExcludedChannel() throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/5.0 (iPad; CPU OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3");
        GetMethod nodeGet = new GetMethod(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/live/en" +
                        SITECONTENT_ROOT_NODE + "/home.html");
        String response = null;
        try {
            int responseCode = client.executeMethod(nodeGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            response = nodeGet.getResponseBodyAsString();
            assertFalse("This text should be hidden when channel is iPad", response.contains("This text shouldn&#39;t appear on an iPad."));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
