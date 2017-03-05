/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.test.services.render;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.test.services.visibility.VisibilityServiceTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP-based test for the login page.
 * 
 * @author Sergiy Shyrkov
 */
public class LoginPageHttpTest extends JahiaTestCase {

    private static String aboutUsPageUrl;

    private static Logger logger = LoggerFactory.getLogger(VisibilityServiceTest.class);

    private final static String PASSWORD = "password";

    private static JCRPublicationService publicationService;

    private static JahiaSite site;

    private final static String SITE_NAME = "loginPageHttpTest";

    private static final String SITE_PATH = "/sites/" + SITE_NAME;

    private final static String USERNAME = "loginPageHttpTestUser";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        // create site
        site = TestHelper.createSite(SITE_NAME);
        assertNotNull(site);

        publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        // create user
        Properties properties = new Properties();
        properties.setProperty("j:firstName", "John");
        properties.setProperty("j:lastName", "Doe");
        ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(USERNAME, PASSWORD, properties, session);

        // revoke "reader" role from guest
        session.getNode("/sites/" + site.getSiteKey()).denyRoles("u:guest", Collections.singleton("reader"));
        session.save();

        // publish site
        publicationService.publishByMainId(session.getNode(SITE_PATH).getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, null, true, null);

        // create sub-page
        JCRSessionWrapper enSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper page = enSession.getNode(SITE_PATH + "/home").addNode("about-us", "jnt:page");
        page.setProperty("j:templateName", "simple");
        page.setProperty("jcr:title", "About Us");
        enSession.save();

        // publish site
        publicationService.publishByMainId(enSession.getNode(SITE_PATH).getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, null, true, null);

        aboutUsPageUrl = "/cms/render/live/en/sites/" + SITE_NAME + "/home/about-us.html";
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(SITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test oneTimeTearDown", ex);
        }

        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        userManagerService.deleteUser(userManagerService.lookupUser(USERNAME).getPath(), session);
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }

    @Test
    public void testInvalidPassword() throws Exception {
        String content = getAsText("/cms/login?username=" + USERNAME + "&password=" + PASSWORD + "_invalid"
                + "&redirect=" + Jahia.getContextPath() + aboutUsPageUrl);
        assertTrue("Should see a login page with invalid password error",
                content.contains("name=\"loginForm\"") && content.contains("Invalid username/password"));
    }

    @Test
    public void testNoGuestAccess() throws Exception {
        String content = getAsText(aboutUsPageUrl, 401);
        assertTrue("Guest can access the home page, which should not be the case",
                content.contains("name=\"loginForm\""));
    }

    @Test
    public void testNormalLogin() throws Exception {
        String content = getAsText("/cms/login?username=" + USERNAME + "&password=" + PASSWORD + "&redirect="
                + Jahia.getContextPath() + aboutUsPageUrl);
        assertTrue("After normal login the user should see the About Us page",
                content.contains("<title>About Us</title>"));
    }

    @Test
    public void testRootLogin() throws Exception {
        String content = getAsText("/cms/login?username=root&password=" + ROOT_PASSWORD + "&redirect="
                + Jahia.getContextPath() + "/cms/admin/default/en/settings.aboutJahia.html");
        assertTrue("After login the root user should see the about page in the administration",
                content.contains("<title>Edit</title>") && content.contains("template=\"aboutJahia\""));
    }

    @Test
    public void testXssOnRedirect() throws Exception {
        String content = getAsText("/cms/login?redirect=%2fsites%2fwhatever%22%3C%2Fscript%3E%3Cscript%3Ealert(%27xss%27)%3C%2Fscript%3E");
        assertFalse("<script> element should not be in the page output",
                content.contains("<script>alert('xss')</script>"));
    }

}
