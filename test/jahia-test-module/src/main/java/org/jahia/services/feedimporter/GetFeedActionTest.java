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

package org.jahia.services.feedimporter;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URL;

/**
 * Unit test for feed importer action.
 *
 * @author loom
 *         Date: May 21, 2010
 *         Time: 3:22:09 PM
 */
public class GetFeedActionTest extends TestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(GetFeedActionTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrFeedImportTest";

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

    public void testGetFeedAction() throws Exception {
        /*JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/contents");
        session.checkout(node);
        node.addNode("feeds","jnt:contentList");
        session.save();

        JCRPublicationService.getInstance().publishByMainId(node.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                true, null);

        testFeed("testSDAFeed", "res:feedimporter/newsml/newsml_1_2_sda", "2_textwithphotoreference.xml");
        testFeed("testKoreanPicturesFeed", "res:feedimporter/newsml/koreanpictures_iptc", "2002-09-23T000051Z_01_BER04D_RTRIDSP_0_GERMANY.XML");
        testFeed("testAFPBasicFeed", "res:feedimporter/newsml/newsml_1_2_afp_basicsample", "NewsML-AFP-mmd-sample.xml");
        testFeed("testAFPLongFeed", "res:feedimporter/newsml/newsml_1_2_afp_longsample", "index.xml");
        // testFeed("testReutersFeed", "res:feedimporter/newsml/reuters_246_samples", "2002-09-23T100332Z_01_LA391519_RTRIDST_0_SPORT-CRICKET-CHAMPIONS-UPDATE-2.XML");

        session.save();*/
    }

    private void testFeed(String nodeName, String feedURL, String testNodeName) throws RepositoryException, IOException, JSONException {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/contents/feeds");

        session.checkout(node);

        JCRNodeWrapper sdaFeedNode = node.addNode(nodeName, "jnt:feed");

        sdaFeedNode.setProperty("url", feedURL);

        session.save();
        JCRPublicationService.getInstance().publishByMainId(sdaFeedNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                true, null);

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        String baseurl = "http://localhost:8080" + Jahia.getContextPath() + "/cms";
        final URL url = new URL(baseurl + "/render/default/en" + sdaFeedNode.getPath() + ".getfeed.do");

        Credentials defaultcreds = new UsernamePasswordCredentials("root", "root1234");
        client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);

        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        PostMethod getFeedAction = new PostMethod(url.toExternalForm());
        getFeedAction.addRequestHeader(new Header("accept", "application/json"));

        client.executeMethod(getFeedAction);
        assertEquals("Bad result code", 200, getFeedAction.getStatusCode());

        JSONObject response = new JSONObject(getFeedAction.getResponseBodyAsString());
        getFeedAction.releaseConnection();

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.LIVE_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper target = liveSession.getNode("/sites/"+TESTSITE_NAME+ "/contents/feeds/"+nodeName+"/" + testNodeName);
        // assertNotNull("Feed should have some childs", target); deactivated because we load content in a single language.
    }


}
