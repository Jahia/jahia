/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

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
        site = TestHelper.createSite(TESTSITE_NAME, TestHelper.INTRANET_TEMPLATES);
        assertNotNull(site);
        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        Properties properties = new Properties();
        properties.setProperty("j:firstName", "John");
        properties.setProperty("j:lastName", "Doe");
        JahiaUser john = ServicesRegistry.getInstance().getJahiaUserManagerService()
                .createUser(USERNAME, PASSWORD, properties);
        JCRSessionFactory.getInstance().getCurrentUserSession()
                .getNode("/sites/" + site.getSiteKey())
                .grantRoles("u:" + john.getUsername(), ImmutableSet.of("editor-in-chief"));
        JCRSessionFactory.getInstance().getCurrentUserSession().save();
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
        userManagerService.deleteUser(userManagerService.lookupUser(USERNAME));
        JCRSessionFactory.getInstance().getCurrentUserSession().save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private JCRNodeWrapper home;

    private JCRNodeWrapper invisible;

    private JCRSessionWrapper session;

    private boolean isPresent(String relativeUrl) {
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
            ParseException {

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
            ParseException {
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
            ParseException {
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
