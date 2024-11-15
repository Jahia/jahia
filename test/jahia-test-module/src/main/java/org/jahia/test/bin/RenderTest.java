/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.bin;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * User: loom
 * Date: Mar 14, 2010
 * Time: 1:40:10 PM
 */
public class RenderTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RenderTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "renderTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static final String MAIN_CONTENT_TITLE = "Main content title update ";
    private static final String MAIN_CONTENT_BODY = "Main content body update ";
    private static int NUMBER_OF_VERSIONS = 0;

    private SimpleDateFormat yyyy_mm_dd_hh_mm_ss = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                TestHelper.WEB_TEMPLATES);
        assertNotNull(site);

        loginRoot();
    }

    @After
    public void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();

        logout();
    }

    private List<String> getUuids(List<PublicationInfo> publicationInfo) {
        List<String> uuids = new LinkedList<String>();
        for (PublicationInfo info : publicationInfo) {
            uuids.addAll(info.getAllUuids());
        }
        return uuids;
    }

    @Test
    public void testVersionRender() throws RepositoryException, java.text.ParseException {
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

        Set<String> languagesStringSet = new LinkedHashSet<String>();
        languagesStringSet.add(Locale.ENGLISH.toString());

        // publish search-results page
        jcrService.publishByInfoList(jcrService.getPublicationInfo(stageRootNode.getNode("search-results").getIdentifier(),
                languagesStringSet, true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                Collections.<String>emptyList());

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");
        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("j:templateName", "simple");
        stagedSubPage.setProperty(Constants.JCR_TITLE, "title0");
        editSession.save();

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
            stagedSubPage.setProperty(Constants.JCR_TITLE, "title" + i);
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
                NumericStringComparator<String> numericStringComparator = new NumericStringComparator<String>(Locale.ENGLISH);
                return numericStringComparator.compare(o1.getLabel(), o2.getLabel());
            }
        });
        int index = 0;
        for (VersionInfo curVersionInfo : liveVersionInfos) {
            Version version = curVersionInfo.getVersion();
            if (version.getCreated() != null && curVersionInfo.getLabel()!=null) {
                    String responseBody = getAsText("/cms/render/live/en" +
                            subPagePublishedNode.getPath() + ".html?v=" +
                            ((yyyy_mm_dd_hh_mm_ss.parse(curVersionInfo.getLabel().split("_at_")[1]).getTime()+5000l)));
                    logger.debug("Response body=[" + responseBody + "]");
                    assertFalse("Couldn't find expected value (title" + Integer.toString(index) + ") in response body",
                            responseBody.indexOf("title" + Integer.toString(index)) < 0);
                index++;
            }
        }
        logger.debug("number of version: {}", index);

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
        mainContent.setProperty(Constants.JCR_TITLE, MAIN_CONTENT_TITLE + "0");
        mainContent.setProperty("body", MAIN_CONTENT_BODY + "0");
        editSession.save();

        HttpPost createPost = new HttpPost(
        		getBaseServerURL() + Jahia.getContextPath() + "/cms/render/default/en" + SITECONTENT_ROOT_NODE +
                "/home/listA/*");
        createPost.addHeader("x-requested-with", "XMLHttpRequest");
        createPost.addHeader("accept", "application/json");
        // here we voluntarily don't set the node name to test automatic name creation.
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("jcrNodeType",  "jnt:mainContent"));
        nvps.add(new BasicNameValuePair(Constants.JCR_TITLE, MAIN_CONTENT_TITLE + "1"));
        nvps.add(new BasicNameValuePair("body", MAIN_CONTENT_BODY + "1"));
        createPost.setEntity(new UrlEncodedFormEntity(nvps));


        String responseBody = "";
        try (CloseableHttpResponse response = getHttpClient().execute(createPost)) {
            assertEquals("Error in response, code=" + response.getCode(), 201, response.getCode());
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (ParseException e) {
            throw new IOException(e);
        }

        JSONObject jsonResults = new JSONObject(responseBody);

        assertNotNull("A proper JSONObject instance was expected, got null instead", jsonResults);
        assertTrue("body property should be "+MAIN_CONTENT_BODY + "1",jsonResults.get("body").equals(MAIN_CONTENT_BODY + "1"));

    }
}
