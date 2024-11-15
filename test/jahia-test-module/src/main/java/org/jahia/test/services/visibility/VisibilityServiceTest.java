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
package org.jahia.test.services.visibility;

import com.google.common.collect.ImmutableSet;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * User: loom
 * Date: Mar 14, 2010
 * Time: 1:40:10 PM
 */
public class VisibilityServiceTest extends JahiaTestCase {

    private static final String INVISIBLE_TEXT = "This is an invisible text";
    private static Logger logger = LoggerFactory.getLogger(VisibilityServiceTest.class);
    private final static String PASSWORD = "password";
    private static JCRPublicationService publicationService;
    private static JahiaSite site;
    private final static String TESTSITE_NAME = "visibilityServiceTest";

    private final static String USERNAME = "visibilityServiceTestUser";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, TestHelper.WEB_TEMPLATES);
        assertNotNull(site);
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();

        Properties properties = new Properties();
        properties.setProperty("j:firstName", "John");
        properties.setProperty("j:lastName", "Doe");
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRUserNode john = ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(USERNAME, PASSWORD, properties, session);
        session.getNode("/sites/" + site.getSiteKey()).grantRoles("u:" + john.getName(), ImmutableSet.of("editor-in-chief"));
        session.save();

        session = publicationService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        publicationService.publishByMainId(session.getNode("/sites/" + TESTSITE_NAME + "/search-results").getIdentifier());
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test oneTimeTearDown", ex);
        }
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance()
                .getJahiaUserManagerService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        userManagerService.deleteUser(userManagerService.lookupUser(USERNAME).getPath(), session);
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private JCRNodeWrapper home;

    private JCRNodeWrapper invisible;

    private JCRSessionWrapper session;

    private boolean isPresent(String relativeUrl) throws IOException {
        return getAsText(relativeUrl).contains(INVISIBLE_TEXT);
    }

    @Before
    public void setUp() throws Exception {

        session = publicationService.getSessionFactory().getCurrentUserSession(
                Constants.EDIT_WORKSPACE, Locale.ENGLISH);

        home = session.getNode("/sites/" + TESTSITE_NAME + "/home");

        publicationService.publishByMainId(home.getIdentifier());

        session.checkout(home);

        invisible = home.getNode("listA").addNode("text", "jnt:text");
        invisible.setProperty("text", INVISIBLE_TEXT);

        session.save();

        login(USERNAME, PASSWORD);
    }

    @After
    public void tearDown() throws Exception {
        invisible.remove();
        session.save();
        publicationService.publishByMainId(home.getIdentifier());

        JCRSessionFactory.getInstance().closeAllSessions();

        logout();
    }

    @Test
    public void testVisibilityRenderMatchesAllConditions() throws RepositoryException,
            ParseException, IOException {

        invisible.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = invisible.addNode("j:conditionalVisibility",
                "jnt:conditionalVisibility");
        condVis.setProperty("j:forceMatchAllConditions", true);
        JCRNodeWrapper firstCondition = condVis.addNode("firstCondition",
                "jnt:startEndDateCondition");
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 20);
        firstCondition.setProperty("start", instance);

        JCRNodeWrapper secondCondition = condVis.addNode("secondCondition",
                "jnt:startEndDateCondition");
        instance.add(Calendar.DAY_OF_MONTH, 5);
        secondCondition.setProperty("end", instance);
        session.save();

        // Validate that content is not visible in preview
        assertFalse("Found unexpected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/default/en" + home.getPath() + ".html"));

        // publish it
        publicationService.publishByMainId(home.getIdentifier());

        // Validate that content is not visible in live
        assertFalse("Found unexpected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/live/en" + home.getPath() + ".html"));

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        // Validate that content is visible in live
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/live/en" + home.getPath() + ".html"));

        Map<JCRNodeWrapper, Boolean> conditionMatchesDetails = VisibilityService.getInstance()
                .getConditionMatchesDetails(invisible);

        assertTrue(conditionMatchesDetails.size() == 2);

        Set<Map.Entry<JCRNodeWrapper, Boolean>> entries = conditionMatchesDetails.entrySet();
        for (Map.Entry<JCRNodeWrapper, Boolean> entry : entries) {
            assertTrue(entry.getValue());
        }
    }

    @Test
    public void testVisibilityRenderMatchesAllEmptyConditions() throws RepositoryException,
            ParseException, IOException {
        invisible.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = invisible.addNode("j:conditionalVisibility",
                "jnt:conditionalVisibility");
        condVis.setProperty("j:forceMatchAllConditions", true);
        session.save();

        // Validate that content is visible in preview with no conditions
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/default/en" + home.getPath() + ".html"));

        // publish it
        publicationService.publishByMainId(home.getIdentifier());

        // Validate that content is visible in live with no conditions
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/live/en" + home.getPath() + ".html"));

        condVis.setProperty("j:forceMatchAllConditions", false);
        session.save();

        // Validate that content is visible in preview with no conditions
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/default/en" + home.getPath() + ".html"));

        // publish it
        publicationService.publishByMainId(home.getIdentifier());

        // Validate that content is visible in live with no conditions
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/default/en" + home.getPath() + ".html"));
    }

    @Test
    public void testVisibilityRenderMatchesOneCondition() throws RepositoryException,
            ParseException, IOException {
        // Test GWT display template
        String gwtDisplayTemplate = VisibilityService.getInstance().getConditions()
                .get("jnt:startEndDateCondition").getGWTDisplayTemplate(Locale.ENGLISH);
        assertNotNull(gwtDisplayTemplate);

        invisible.addMixin("jmix:conditionalVisibility");
        JCRNodeWrapper condVis = invisible.addNode("j:conditionalVisibility",
                "jnt:conditionalVisibility");
        JCRNodeWrapper firstCondition = condVis.addNode("firstCondition",
                "jnt:startEndDateCondition");
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, 20);
        firstCondition.setProperty("start", instance);
        session.save();

        // Validate that content is not visible in preview
        assertFalse("Found unexpected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/default/en" + home.getPath() + ".html"));

        // publish it
        publicationService.publishByMainId(home.getIdentifier());

        // Validate that content is not visible in live
        assertFalse("Found unexpected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/live/en" + home.getPath() + ".html"));

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        // Validate that content is visible in live
        assertTrue("Could not find expected value (" + INVISIBLE_TEXT + ") in response body",
                isPresent("/cms/render/live/en" + home.getPath() + ".html"));
    }
}
