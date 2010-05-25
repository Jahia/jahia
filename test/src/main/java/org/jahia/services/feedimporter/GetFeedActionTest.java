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
import org.json.JSONObject;

import java.net.URL;

/**
 * TODO Comment me
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
        JCRNodeWrapper target = node.addNode("target", "jnt:page");

        JCRNodeWrapper feedNode = node.addNode("testFeed", "jnt:feed");

        feedNode.setProperty("url", "res:feedimporter/newsml/koreanpictures_iptc");
        //feedNode.setProperty("user", "root");
        //feedNode.setProperty("password", "root1234");

        session.save();

        JCRPublicationService.getInstance().publish("/sites/"+TESTSITE_NAME+"/home", Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, true);

        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        String baseurl = "http://localhost:8080" + Jahia.getContextPath() + "/cms";
        final URL url = new URL(baseurl + "/render/default/en" + feedNode.getPath() + ".getfeed.do");

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
        target = liveSession.getNodeByUUID(target.getIdentifier());
        assertTrue("Node not added", target.hasNode("page1"));

    }


}
