package org.jahia.services.feedimporter;

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

    private static Logger logger = Logger.getLogger(GetFeedActionTest.class);
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
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/home");
        JCRNodeWrapper source = node.addNode("source", "jnt:page");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");

        session.save();

        JCRPublicationService.getInstance().publish("/sites/"+TESTSITE_NAME+"/home", Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, true);

        testFeed("testSDAFeed", "res:feedimporter/newsml/newsml_1_2_sda", "2_textwithphotoreference.xml");
        testFeed("testKoreanPicturesFeed", "res:feedimporter/newsml/koreanpictures_iptc", "2002-09-23T000051Z_01_BER04D_RTRIDSP_0_GERMANY.XML");
        testFeed("testAFPBasicFeed", "res:feedimporter/newsml/newsml_1_2_afp_basicsample", "NewsML-AFP-mmd-sample.xml");
        testFeed("testAFPLongFeed", "res:feedimporter/newsml/newsml_1_2_afp_longsample", "index.xml");
        testFeed("testReutersFeed", "res:feedimporter/newsml/reuters_246_samples", "2002-09-23T100332Z_01_LA391519_RTRIDST_0_SPORT-CRICKET-CHAMPIONS-UPDATE-2.XML");

        session.save();
    }

    private void testFeed(String nodeName, String feedURL, String testNodeName) throws RepositoryException, IOException, JSONException {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));
        JCRNodeWrapper node = session.getNode("/sites/"+TESTSITE_NAME+ "/home");

        session.checkout(node);

        JCRNodeWrapper target;
        JCRNodeWrapper sdaFeedNode = node.addNode(nodeName, "jnt:feed");

        sdaFeedNode.setProperty("url", feedURL);

        session.save();
        JCRPublicationService.getInstance().publish("/sites/"+TESTSITE_NAME+"/home/" + nodeName, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, true);

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
        target = liveSession.getNode("/sites/"+TESTSITE_NAME+ "/home/"+nodeName+"/" + testNodeName);
        // assertNotNull("Feed should have some childs", target); deactivated because we load content in a single language.
    }


}
