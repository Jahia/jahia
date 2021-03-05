/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 * HTTP-based test for the login page.
 * 
 * @author Sergiy Shyrkov
 */
public class LoginPageHttpTest extends JahiaTestCase {
    private static Logger logger = LoggerFactory.getLogger(LoginPageHttpTest.class);

    private static final String PASSWORD = "password";
    private static final String SITE_NAME = "loginPageHttpTest";
    private static final String SITE_PATH = "/sites/" + SITE_NAME;
    private static final String USERNAME = "loginPageHttpTestUser";
    private static final String LOGIN_URL_FORMAT = "/cms/login?sername=%s&password=%s&redirect=%s";
    private static final String ABOUT_US_TITLE = "<title>About Us</title>";
    private static final String LOGIN_FORM_NAME_LOCATOR = "name=\"loginForm\"";
    
    private static String aboutUsPageUrl;

    @BeforeClass
    public static void oneTimeSetUp() throws RepositoryException, IOException, JahiaException {
        // create site
        JahiaSite site = TestHelper.createSite(SITE_NAME);
        assertNotNull(site);

        JCRPublicationService publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
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
    public static void oneTimeTearDown() throws RepositoryException {
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

    protected Cookie getCookie(String cookieName) {
        for (Cookie c : getHttpClient().getState().getCookies()) {
            if (cookieName.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    @After
    public void tearDown() throws IOException {
        logout();
    }

    @Test
    public void testInvalidPassword() {
        String content = getAsText(
                String.format(LOGIN_URL_FORMAT, USERNAME, PASSWORD + "_invalid", Jahia.getContextPath() + aboutUsPageUrl));
        assertTrue("Should see a login page with invalid password error",
                content.contains(LOGIN_FORM_NAME_LOCATOR) && content.contains("Invalid username/password"));
    }

    @Test
    public void testNoGuestAccess() {
        String content = getAsText(aboutUsPageUrl, HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue("Guest can access the home page, which should not be the case",
                content.contains(LOGIN_FORM_NAME_LOCATOR));
    }

    @Test
    public void testNormalLogin() {
        String content = getAsText(String.format(LOGIN_URL_FORMAT, USERNAME, PASSWORD, Jahia.getContextPath() + aboutUsPageUrl));
        assertTrue("After normal login the user should see the About Us page", content.contains(ABOUT_US_TITLE));
    }

    @Test
    public void testRememberMe() {
        CookieAuthConfig cookieAuthConfig = (CookieAuthConfig) SpringContextSingleton.getBean("cookieAuthConfig");
        Map<String, List<String>> responseHeaders = new HashMap<>();
        getAsText("/cms/login?username=" + USERNAME + "&password=" + PASSWORD + "&restMode=true" + "&" + LoginEngineAuthValveImpl.USE_COOKIE
                + "=on", null, HttpServletResponse.SC_OK, responseHeaders);

        String cookieName = cookieAuthConfig.getCookieName();
        List<String> setCookie = responseHeaders.get("Set-Cookie");
        Iterator<String> cookieValueIteraror = setCookie != null
                ? Iterables.filter(setCookie, Predicates.containsPattern(cookieName + "=")).iterator()
                : null;
        assertTrue("The response header should contain the corresponding remember me cookie " + cookieName,
                cookieValueIteraror != null && cookieValueIteraror.hasNext());
        String cookieValue = StringUtils.substringBetween(cookieValueIteraror.next(), cookieName + "=", ";");

        Cookie authCookie = getCookie(cookieName);
        assertNotNull("Remember me cookie is not present in HTTP client state", authCookie);
        assertEquals("Remember me cookie has wrong value in HTTP client state", cookieValue, authCookie.getValue());

        String content = getAsText(aboutUsPageUrl);

        assertTrue("After normal login the user should see the About Us page",
                content.contains(ABOUT_US_TITLE));

        // we clear the cookies to remove current session cookie from HTTP state
        getHttpClient().getState().clearCookies();

        content = getAsText(aboutUsPageUrl, HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue("Guest can access the home page, which should not be the case",
                content.contains(LOGIN_FORM_NAME_LOCATOR));

        // we put the remember me cookie into HTTP state
        getHttpClient().getState().addCookie(authCookie);

        content = getAsText(aboutUsPageUrl);
        assertTrue(
                "With a remember me cookie the login should be done automatically and the user should see the About Us page",
                content.contains(ABOUT_US_TITLE));
    }

    @Test
    public void testRootLogin() {
        String content = getAsText(String.format(LOGIN_URL_FORMAT, JahiaTestCase.getRootUserCredentials().getUserID(),
                new String(JahiaTestCase.getRootUserCredentials().getPassword()),
                Jahia.getContextPath() + "/cms/admin/default/en/settings.aboutJahia.html"));
        assertTrue("After login the root user should see the about page in the administration",
                content.contains("<title>settings</title>") && content.contains("template=\"aboutJahia\""));
    }

    @Test
    public void testXssOnRedirect() {
        String content = getAsText("/cms/login?redirect=%2fsites%2fwhatever%22%3C%2Fscript%3E%3Cscript%3Ealert(%27xss%27)%3C%2Fscript%3E");
        assertFalse("<script> element should not be in the page output", content.contains("<script>alert('xss')</script>"));
    }

}
