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

package org.jahia.services.visibility;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

/**
 * User: loom
 * Date: Mar 14, 2010
 * Time: 1:40:10 PM
 */
public class VisibilityServiceTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VisibilityServiceTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "visibilityServiceTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

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
    public void testVisibilityRenderMatchesOneCondition() throws RepositoryException, ParseException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        // Test GWT display template
        String gwtDisplayTemplate = VisibilityService.getInstance().getConditions().get(
                "jnt:startEndDateCondition").getGWTDisplayTemplate(Locale.ENGLISH);
        assertNotNull(gwtDisplayTemplate);

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("j:templateNode", editSession.getNode(
                SITECONTENT_ROOT_NODE + "/templates/base/simple"));
        stagedSubPage.setProperty("jcr:title", "Page not visible");
        stagedSubPage.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = stagedSubPage.addNode("j:conditionalVisibility", "jnt:conditionalVisibility");
        JCRNodeWrapper firstCondition = condVis.addNode("firstCondition", "jnt:startEndDateCondition");
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, 1);
        firstCondition.setProperty("start", instance);
        editSession.save();
        // Validate that content is not visible in preview
        GetMethod visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertFalse("Could find non expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }


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


        // Validate that content is not visible in live
        visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/live/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertFalse("Could find non expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        // Validate that content is visible in live
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertTrue("Could not find expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void testVisibilityRenderMatchesAllConditions() throws RepositoryException, ParseException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        // Test GWT display template
        String gwtDisplayTemplate = VisibilityService.getInstance().getConditions().get(
                "jnt:startEndDateCondition").getGWTDisplayTemplate(Locale.ENGLISH);
        assertNotNull(gwtDisplayTemplate);

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("j:templateNode", editSession.getNode(
                SITECONTENT_ROOT_NODE + "/templates/base/simple"));
        stagedSubPage.setProperty("jcr:title", "Page not visible");
        stagedSubPage.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = stagedSubPage.addNode("j:conditionalVisibility", "jnt:conditionalVisibility");
        condVis.setProperty("j:forceMatchAllConditions", true);
        JCRNodeWrapper firstCondition = condVis.addNode("firstCondition", "jnt:startEndDateCondition");
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE, 1);
        firstCondition.setProperty("start", instance);
        JCRNodeWrapper secondCondition = condVis.addNode("secondCondition", "jnt:startEndDateCondition");
        instance.add(Calendar.MINUTE, 5);
        secondCondition.setProperty("end", instance);
        editSession.save();
        // Validate that content is not visible in preview
        GetMethod visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertFalse("Could find non expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }


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


        // Validate that content is not visible in live
        visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/live/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertFalse("Could find non expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        // Validate that content is visible in live
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertTrue("Could not find expected value (Page not visible) in response body", responseBody.indexOf(
                    "Page not visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        Map<JCRNodeWrapper, Boolean> conditionMatchesDetails = VisibilityService.getInstance().getConditionMatchesDetails(
                stagedSubPage);
        assertTrue(conditionMatchesDetails.size() == 2);
        Set<Map.Entry<JCRNodeWrapper, Boolean>> entries = conditionMatchesDetails.entrySet();
        for (Map.Entry<JCRNodeWrapper, Boolean> entry : entries) {
            assertTrue(entry.getValue());
        }
    }

    @Test
    public void testVisibilityRenderMatchesAllEmptyConditions() throws RepositoryException, ParseException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);

        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);

        // Test GWT display template
        String gwtDisplayTemplate = VisibilityService.getInstance().getConditions().get(
                "jnt:startEndDateCondition").getGWTDisplayTemplate(Locale.ENGLISH);
        assertNotNull(gwtDisplayTemplate);

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("j:templateNode", editSession.getNode(
                SITECONTENT_ROOT_NODE + "/templates/base/simple"));
        stagedSubPage.setProperty("jcr:title", "Page visible");
        stagedSubPage.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = stagedSubPage.addNode("j:conditionalVisibility", "jnt:conditionalVisibility");
        condVis.setProperty("j:forceMatchAllConditions", true);
        editSession.save();
        // Validate that content is not visible in preview
        GetMethod visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertTrue("Could not find expected value (Page visible) in response body", responseBody.indexOf(
                    "Page visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        condVis.setProperty("j:forceMatchAllConditions", false);
        editSession.save();
        // Validate that content is not visible in preview
        visibilityGet = new GetMethod(
                "http://localhost:8080" + Jahia.getContextPath() + "/cms/render/default/en" + stageNode.getPath() +
                ".html");
        try {
            int responseCode = client.executeMethod(visibilityGet);
            assertEquals("Response code " + responseCode, 200, responseCode);
            String responseBody = visibilityGet.getResponseBodyAsString();
            logger.debug("Response body=[" + responseBody + "]");
            assertTrue("Could not find expected value (Page visible) in response body", responseBody.indexOf(
                    "Page visible") > 0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
