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
package org.jahia.test.services.feedimporter;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.xerces.impl.dv.util.Base64;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit test for feed importer action.
 *
 * @author loom
 *         Date: May 21, 2010
 *         Time: 3:22:09 PM
 */
public class GetFeedActionTest extends JahiaTestCase {

    private final static String TESTSITE_NAME = "jcrFeedImportTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        TestHelper.createSite(TESTSITE_NAME);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Test
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
        JCRSessionWrapper baseSession = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRSiteNode site = (JCRSiteNode) baseSession.getNode(SITECONTENT_ROOT_NODE);

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

        CloseableHttpClient client = getHttpClient();

        String baseurl = getBaseServerURL() + Jahia.getContextPath() + "/cms";
        final URL url = new URL(baseurl + "/render/default/en" + sdaFeedNode.getPath() + ".getfeed.do");

        HttpPost getFeedAction = new HttpPost(url.toExternalForm());
            getFeedAction.addHeader("Authorization", "Basic " + Base64.encode((JahiaTestCase.getRootUserCredentials().getUserID() + ":" + String.valueOf(JahiaTestCase.getRootUserCredentials().getPassword())).getBytes()));

            getFeedAction.addHeader("accept", "application/json");

        try (CloseableHttpResponse httpResponse = client.execute(getFeedAction)) {
            assertEquals("Bad result code", HttpServletResponse.SC_OK, httpResponse.getCode());
        }

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.LIVE_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper target = liveSession.getNode("/sites/"+TESTSITE_NAME+ "/contents/feeds/"+nodeName+"/" + testNodeName);
        // assertNotNull("Feed should have some childs", target); deactivated because we load content in a single language.
    }


}
