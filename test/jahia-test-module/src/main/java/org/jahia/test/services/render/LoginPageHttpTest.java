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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.logging.log4j.Level;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.LoggingConfigListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.params.valves.CookieAuthValveImpl;
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

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
    private static final String LOGIN_URL_FORMAT = "/cms/login?username=%s&password=%s&redirect=%s";
    private static final String ABOUT_US_TITLE = "<title>About Us</title>";
    private static final String LOGIN_FORM_NAME_LOCATOR = "name=\"loginForm\"";

    private static String aboutUsPageUrl;

    @BeforeClass
    public static void oneTimeSetUp() throws RepositoryException, IOException, JahiaException {
        // create site
        JahiaSite site = TestHelper.createSite(SITE_NAME);
        assertThat(site).isNotNull();

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

    protected Cookie getCookie(HttpClientContext context, String cookieName) {
        for (Cookie c : context.getCookieStore().getCookies()) {
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
        assertThat(content).contains(LOGIN_FORM_NAME_LOCATOR, "login-error")
                .withFailMessage("Should see a login page with invalid password error");
    }

    @Test
    public void testNoGuestAccess() {
        String content = getAsText(aboutUsPageUrl, HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testNormalLogin() {
        String content = getAsText(String.format(LOGIN_URL_FORMAT, USERNAME, PASSWORD, Jahia.getContextPath() + aboutUsPageUrl));
        assertThat(content).contains(ABOUT_US_TITLE).withFailMessage("After normal login the user should see the About Us page");
    }

    @Test
    public void testRememberMe() {
        String loggerName = LoggerFactory.getLogger(CookieAuthValveImpl.class).getName();
        try {
            // enable debug logging
            LoggingConfigListener.setLoggerLevel(loggerName, Level.DEBUG.toString());

            HttpClientContext context = new HttpClientContext();
            CookieAuthConfig cookieAuthConfig = (CookieAuthConfig) SpringContextSingleton.getBean("cookieAuthConfig");
            Map<String, List<String>> responseHeaders = new HashMap<>();
            getAsText("/cms/login?username=" + USERNAME + "&password=" + PASSWORD + "&restMode=true" + "&" + LoginEngineAuthValveImpl.USE_COOKIE
                    + "=on", null, HttpServletResponse.SC_OK, responseHeaders, context);

            final String cookieName = cookieAuthConfig.getCookieName();
            List<String> setCookie = responseHeaders.get("Set-Cookie");
            Iterator<String> cookieValueIteraror = setCookie != null
                    ? Iterables.filter(setCookie, Predicates.containsPattern(cookieName + "=")).iterator()
                    : null;
            assertThat(cookieValueIteraror).isNotNull()
                    .withFailMessage("The response header should contain the corresponding remember me cookie %s", cookieName);
            final String cookieValue = StringUtils.substringBetween(cookieValueIteraror.next(), cookieName + "=", ";");

            Cookie authCookie = getCookie(context, cookieName);
            assertThat(authCookie).isNotNull().withFailMessage("Remember me cookie is not present in HTTP client state");
            assertThat(authCookie.getValue()).isEqualTo(cookieValue).withFailMessage("Remember me cookie has wrong value in HTTP client state");

            String content = getAsText(aboutUsPageUrl, null, HttpServletResponse.SC_OK, null, context);

            assertThat(content).contains(ABOUT_US_TITLE).withFailMessage("After normal login the user should see the About Us page");

            // we clear the cookies to remove current session cookie from HTTP state
            context.getCookieStore().clear();

            content = getAsText(aboutUsPageUrl, null, HttpServletResponse.SC_NOT_FOUND, null, context);

            // we put the remember me cookie into HTTP state
            context.getCookieStore().clear();
            context.getCookieStore().addCookie(authCookie);

            // validate reuse of remember me cookie is correctly added in cookie store
            Cookie authCookieClone = getCookie(context, cookieName);
            assertThat(authCookieClone.getValue()).isEqualTo(cookieValue)
                    .withFailMessage("Reuse of Remember me cookie has wrong value in HTTP client state");

            content = getAsText(aboutUsPageUrl, null, HttpServletResponse.SC_OK, null, context);
            assertThat(content).contains(ABOUT_US_TITLE).withFailMessage(
                    "With a remember me cookie the login should be done automatically and the user should see the About Us page");
        } finally {
            // restore default logging level
            LoggingConfigListener.setLoggerLevel(loggerName, LoggingConfigListener.getRootLoggerLevel());
        }
    }

    @Test
    public void testRootLogin() {
        String content = getAsText(String.format(LOGIN_URL_FORMAT, JahiaTestCase.getRootUserCredentials().getUserID(),
                new String(JahiaTestCase.getRootUserCredentials().getPassword()),
                Jahia.getContextPath() + "/cms/adminframe/default/en/settings.aboutJahia.html"));
        assertThat(content).contains("<title>settings</title>", "template=\"aboutJahia\"")
                .withFailMessage("After login the root user should see the about page in the administration");
    }

    @Test
    public void testXssOnRedirect() {
        String content = getAsText("/cms/login?redirect=%2fsites%2fwhatever%22%3C%2Fscript%3E%3Cscript%3Ealert(%27xss%27)%3C%2Fscript%3E");
        assertThat(content).doesNotContain("<script>alert('xss')</script>")
                .withFailMessage("<script> element should not be in the page output");
    }

}
