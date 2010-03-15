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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;

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

    HttpClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.

        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            ctx = Jahia.getThreadParamBean();
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }

        // Create an instance of HttpClient.
        client = new HttpClient();

        // todo we should really insert content to test the find.

        PostMethod loginMethod = new PostMethod("http://localhost:8080/cms/login");
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

        PostMethod logoutMethod = new PostMethod("http://localhost:8080/cms/logout");
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

        for (int i = 1; i < 11; i++) {
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
            GetMethod versionGet = new GetMethod("http://localhost:8080/cms/render/live/en" + subPagePublishedNode.getPath() + ".html?v=" + curVersionInfo.getVersion().getName());
            try {
                int responseCode = client.executeMethod(versionGet);
                assertEquals("Response code " + responseCode, 200, responseCode);
                String responseBody = versionGet.getResponseBodyAsString();
                assertFalse("Couldn't find expected value in response body", responseBody.indexOf("title" + Integer.toString(index)) < 0);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            index++;
        }
        logger.debug("number of version: " + index);
        assertEquals(11, index);

    }

}
