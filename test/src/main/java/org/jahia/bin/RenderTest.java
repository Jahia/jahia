package org.jahia.bin;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.VersionInfo;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 14, 2010
 * Time: 1:40:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class RenderTest extends TestCase {

    private static Logger logger = Logger.getLogger(RenderTest.class);
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "renderTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static final String MAIN_CONTENT_TITLE = "Main content title update ";
    private static final String MAIN_CONTENT_BODY = "Main content body update ";
    private static int NUMBER_OF_VERSIONS = 5;

    HttpClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.

        try {
            site = TestHelper.createSite(TESTSITE_NAME, "localhost"+System.currentTimeMillis(), TestHelper.INTRANET_TEMPLATES, null);
            ctx = Jahia.getThreadParamBean();
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }

        // Create an instance of HttpClient.
        client = new HttpClient();

        // todo we should really insert content to test the find.

        PostMethod loginMethod = new PostMethod("http://localhost:8080"+Jahia.getContextPath()+"/cms/login");
        loginMethod.addParameter("username", "root");
        loginMethod.addParameter("password", "root1234");
        loginMethod.addParameter("redirectActive", "false");
        // the next parameter is required to properly activate the valve check.
        loginMethod.addParameter(LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER, "1");

        int statusCode = client.executeMethod(loginMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + loginMethod.getStatusLine());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.

        PostMethod logoutMethod = new PostMethod("http://localhost:8080"+Jahia.getContextPath()+"/cms/logout");
        logoutMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(logoutMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + logoutMethod.getStatusLine());
        }

        logoutMethod.releaseConnection();

        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    public void testVersionRender() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession();
        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        Node versioningTestActivity = editSession.getWorkspace().getVersionManager().createActivity("versioningTest");
        Node previousActivity = editSession.getWorkspace().getVersionManager().setActivity(versioningTestActivity);
        if (previousActivity != null) {
            logger.debug("Previous activity=" + previousActivity.getName() + " new activity=" + versioningTestActivity.getName());
        } else {
            logger.debug("New activity=" + versioningTestActivity.getName());
        }

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("jcr:title", "title0");
        editSession.save();

        // publish it
        jcrService.publish(stageNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, true);

        for (int i = 1; i < NUMBER_OF_VERSIONS; i++) {
            editSession.checkout(stagedSubPage);
            stagedSubPage.setProperty("jcr:title", "title" + i);
            editSession.save();

            // each time the node i published, a new version should be created
            jcrService.publish(stagedSubPage.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, false);
        }

        // now let's do a little system versioning ourselves...

        editSession.getWorkspace().getVersionManager().checkpoint(stagedSubPage.getPath());

        // let's do some validation checks, first for the live workspace...

        // check number of versions
        JCRNodeWrapper subPagePublishedNode = liveSession.getNode(stagedSubPage.getPath());

        List<VersionInfo> liveVersionInfos = ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(liveSession, subPagePublishedNode);
        int index = 0;
        for (VersionInfo curVersionInfo : liveVersionInfos) {
            GetMethod versionGet = new GetMethod("http://localhost:8080"+Jahia.getContextPath()+"/cms/render/live/en" + subPagePublishedNode.getPath() + ".html?v=" + curVersionInfo.getCheckinDate().getTime().getTime());
            try {
                int responseCode = client.executeMethod(versionGet);
                assertEquals("Response code " + responseCode, 200, responseCode);
                String responseBody = versionGet.getResponseBodyAsString();
                // logger.debug("Response body=[" + responseBody + "]");
                assertFalse("Couldn't find expected value (title" + Integer.toString(index)+") in response body", responseBody.indexOf("title" + Integer.toString(index)) < 0);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            index++;
        }
        logger.debug("number of version: " + index);
        assertEquals(NUMBER_OF_VERSIONS, index);

    }

    public void testRestAPI() throws RepositoryException, IOException, JSONException {

        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");

        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale);
        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("jcr:title", "title0");

        JCRNodeWrapper stagedPageContent = stagedSubPage.addNode("pagecontent", "jnt:contentList");
        JCRNodeWrapper stagedRow1 = stagedPageContent.addNode("row1", "jnt:row");
        stagedRow1.setProperty("column", "2col106");
        JCRNodeWrapper stagedCol1 = stagedRow1.addNode("col1", "jnt:contentList");
        JCRNodeWrapper mainContent = stagedCol1.addNode("mainContent", "jnt:mainContent");
        mainContent.setProperty("jcr:title", MAIN_CONTENT_TITLE + "0");
        mainContent.setProperty("body", MAIN_CONTENT_BODY + "0");

        PostMethod createPost = new PostMethod("http://localhost:8080"+Jahia.getContextPath()+"/cms/render/default/en" + SITECONTENT_ROOT_NODE + "/home/pagecontent/row1/col1/*");
        createPost.addRequestHeader("x-requested-with", "XMLHttpRequest");
        createPost.addRequestHeader("accept", "application/json");
        // here we voluntarily don't set the node name to test automatic name creation.
        createPost.addParameter("nodeType", "jnt:mainContent");
        createPost.addParameter("jcr:title", MAIN_CONTENT_TITLE + "1");
        createPost.addParameter("body", MAIN_CONTENT_BODY + "1");

        int responseCode = client.executeMethod(createPost);
        assertEquals("Error in response, code=" + responseCode, 201, responseCode);
        String responseBody = createPost.getResponseBodyAsString();

        JSONObject jsonResults = new JSONObject(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);

    }

}
