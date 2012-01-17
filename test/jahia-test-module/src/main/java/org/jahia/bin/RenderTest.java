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

package org.jahia.bin;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jahia.api.Constants;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.comparator.NumericStringComparator;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * 
 * User: loom
 * Date: Mar 14, 2010
 * Time: 1:40:10 PM
 */
public class RenderTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RenderTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "renderTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static final String MAIN_CONTENT_TITLE = "Main content title update ";
    private static final String MAIN_CONTENT_BODY = "Main content body update ";
    private static int NUMBER_OF_VERSIONS = 5;

    HttpClient client;
    private SimpleDateFormat yyyy_mm_dd_hh_mm_ss = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    @Before
    public void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                    TestHelper.INTRANET_TEMPLATES);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }

        // Create an instance of HttpClient.
        client = new HttpClient();

        // todo we should really insert content to test the find.

        PostMethod loginMethod = new PostMethod("http://localhost:8080" + Jahia.getContextPath() + "/cms/login");
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

    @After
    public void tearDown() throws Exception {

        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();

        PostMethod logoutMethod = new PostMethod("http://localhost:8080" + Jahia.getContextPath() + "/cms/logout");
        logoutMethod.addParameter("redirectActive", "false");

        int statusCode = client.executeMethod(logoutMethod);
        if (statusCode != HttpStatus.SC_OK) {
            System.err.println("Method failed: " + logoutMethod.getStatusLine());
        }

        logoutMethod.releaseConnection();
    }

    private List<String> getUuids(List<PublicationInfo> publicationInfo) {
        List<String> uuids = new LinkedList<String>();
        for (PublicationInfo info : publicationInfo) {
            uuids.addAll(info.getAllUuids());
        }
        return uuids;
    }

    @Test
    public void testVersionRender() throws RepositoryException, ParseException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                Locale.ENGLISH);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        Node versioningTestActivity = editSession.getWorkspace().getVersionManager().createActivity("versioningTest");
        Node previousActivity = editSession.getWorkspace().getVersionManager().setActivity(versioningTestActivity);
        if (previousActivity != null) {
            logger.debug("Previous activity=" + previousActivity.getName() + " new activity=" +
                         versioningTestActivity.getName());
        } else {
            logger.debug("New activity=" + versioningTestActivity.getName());
        }

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("j:templateNode", editSession.getNode(
                    SITECONTENT_ROOT_NODE + "/templates/base/simple"));
        stagedSubPage.setProperty("jcr:title", "title0");
        editSession.save();
        Set<String> languagesStringSet = new LinkedHashSet<String>();
        languagesStringSet.add(Locale.ENGLISH.toString());
        // publish it
        List<PublicationInfo> publicationInfo = jcrService.getPublicationInfo(stageNode.getIdentifier(),
                languagesStringSet, true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
        jcrService.publishByInfoList(publicationInfo, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                Collections.<String>emptyList());
        String label = "published_at_" + yyyy_mm_dd_hh_mm_ss.format(GregorianCalendar.getInstance().getTime());
        List<String> uuids = getUuids(publicationInfo);
        jcrVersionService.addVersionLabel(uuids, label, Constants.LIVE_WORKSPACE);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        for (int i = 1; i < NUMBER_OF_VERSIONS; i++) {
            editSession.checkout(stagedSubPage);
            stagedSubPage.setProperty("jcr:title", "title" + i);
            editSession.save();

            // each time the node i published, a new version should be created

            publicationInfo = jcrService.getPublicationInfo(stagedSubPage.getIdentifier(), languagesStringSet, true,
                    true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
            jcrService.publishByInfoList(publicationInfo, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                    Collections.<String>emptyList());
            label = "published_at_" + yyyy_mm_dd_hh_mm_ss.format(GregorianCalendar.getInstance().getTime());
            uuids = getUuids(publicationInfo);
            jcrVersionService.addVersionLabel(uuids, label, Constants.LIVE_WORKSPACE);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // now let's do a little system versioning ourselves...

        editSession.getWorkspace().getVersionManager().checkpoint(stagedSubPage.getPath());

        // let's do some validation checks, first for the live workspace...

        // check number of versions
        JCRNodeWrapper subPagePublishedNode = liveSession.getNode(stagedSubPage.getPath());

        List<VersionInfo> liveVersionInfos = jcrVersionService.getVersionInfos(liveSession, subPagePublishedNode);
        Collections.sort(liveVersionInfos,new Comparator<VersionInfo>() {
            public int compare(VersionInfo o1, VersionInfo o2) {
                NumericStringComparator<String> numericStringComparator = new NumericStringComparator<String>();
                return numericStringComparator.compare(o1.getLabel(), o2.getLabel());
            }
        });
        int index = 0;
        for (VersionInfo curVersionInfo : liveVersionInfos) {
            Version version = curVersionInfo.getVersion();
            if (version.getCreated() != null && curVersionInfo.getLabel()!=null) {
                GetMethod versionGet = new GetMethod(
                        "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/live/en" +
                        subPagePublishedNode.getPath() + ".html?v=" +
                        ((yyyy_mm_dd_hh_mm_ss.parse(curVersionInfo.getLabel().split("_at_")[1]).getTime()+5000l)));
                try {
                    int responseCode = client.executeMethod(versionGet);
                    assertEquals("Response code " + responseCode, 200, responseCode);
                    String responseBody = versionGet.getResponseBodyAsString();
                    logger.debug("Response body=[" + responseBody + "]");
                    assertFalse("Couldn't find expected value (title" + Integer.toString(index) + ") in response body",
                            responseBody.indexOf("title" + Integer.toString(index)) < 0);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                index++;
            }
        }
        logger.debug("number of version: " + index);
        assertEquals(NUMBER_OF_VERSIONS, index);
    }

    @Test
    public void testRestAPI() throws RepositoryException, IOException, JSONException {

        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");

        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                englishLocale);
        jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        JCRNodeWrapper stagedPageContent = stageNode.getNode("listA");
        JCRNodeWrapper mainContent = stagedPageContent.addNode("mainContent", "jnt:mainContent");
        mainContent.setProperty("jcr:title", MAIN_CONTENT_TITLE + "0");
        mainContent.setProperty("body", MAIN_CONTENT_BODY + "0");
        editSession.save();

        PostMethod createPost = new PostMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en" + SITECONTENT_ROOT_NODE +
                "/home/listA/*");
        createPost.addRequestHeader("x-requested-with", "XMLHttpRequest");
        createPost.addRequestHeader("accept", "application/json");
        // here we voluntarily don't set the node name to test automatic name creation.
        createPost.addParameter("jcrNodeType", "jnt:mainContent");
        createPost.addParameter("jcr:title", MAIN_CONTENT_TITLE + "1");
        createPost.addParameter("body", MAIN_CONTENT_BODY + "1");

        int responseCode = client.executeMethod(createPost);
        assertEquals("Error in response, code=" + responseCode, 201, responseCode);
        String responseBody = createPost.getResponseBodyAsString();

        JSONObject jsonResults = new JSONObject(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);
        assertTrue("body property should be "+MAIN_CONTENT_BODY + "1",jsonResults.get("body").equals(MAIN_CONTENT_BODY + "1"));

    }
}
