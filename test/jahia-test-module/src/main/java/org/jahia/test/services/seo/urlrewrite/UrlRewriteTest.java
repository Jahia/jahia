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
package org.jahia.test.services.seo.urlrewrite;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;

/**
 * Test case for URL rewriting.
 *
 * @author Sergiy Shyrkov
 */
public class UrlRewriteTest extends JahiaTestCase {

    private static final String DEFAULT_LANG = "en";

    private static final String SECOND_LANG = "fr";

    private static UrlRewriteService engine;

    private static boolean seoRulesEnabled = false;
    private static boolean seoRemoveCmsPrefix = false;

    private static final String SERVER_NAME = "urlrewrite.jahia.org";

    private static final String SERVER_NAME_ALIAS = "urlrewrite-alias.jahia.org";

    private static final String SERVLET = "/cms";

    private static final String FILES_SERVLET = "/files";

    private static final String SITE_KEY = "urlRewriteSite";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        engine = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");
        if (!engine.isSeoRulesEnabled()) {
            engine.setSeoRulesEnabled(true);
            seoRulesEnabled = true;
        }
        if (!engine.isSeoRemoveCmsPrefix()) {
            engine.setSeoRemoveCmsPrefix(true);
            seoRemoveCmsPrefix = true;
        }
        if (seoRulesEnabled || seoRemoveCmsPrefix) {
            engine.afterPropertiesSet();
        }
        JahiaSite site = TestHelper.createSite(SiteCreationInfo.builder().siteKey(SITE_KEY).serverName(SERVER_NAME)
                .serverNameAliases(SERVER_NAME_ALIAS).templateSet(TestHelper.WEB_TEMPLATES).build());
        site = (JahiaSite) JCRSessionFactory.getInstance().getCurrentUserSession().getNode(site.getJCRLocalPath());
        Set<String> languages = new HashSet<String>();
        languages.add(DEFAULT_LANG);
        languages.add(SECOND_LANG);
        site.setLanguages(languages);
        site.setDefaultLanguage(DEFAULT_LANG);
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        service.updateSystemSitePermissions(site);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(SITE_KEY);
        JCRSessionFactory.getInstance().closeAllSessions();

        if (seoRulesEnabled || seoRemoveCmsPrefix) {
            engine.setSeoRulesEnabled(!seoRulesEnabled);
            engine.setSeoRemoveCmsPrefix(!seoRemoveCmsPrefix);
            engine.afterPropertiesSet();
            seoRulesEnabled = false;
            seoRemoveCmsPrefix = false;
        }
        engine = null;
    }

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    public void doLiveSiteLocalhostTest(String lang) throws Exception {
        String rewrittenLang = DEFAULT_LANG.equals(lang) ? "" : "/" + lang;

        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html", rewrittenLang + "/sites/urlRewriteSite/home.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html?test=aaa",
                rewrittenLang + "/sites/urlRewriteSite/home.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html",
                rewrittenLang + "/sites/urlRewriteSite/home/activities.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html?test=aaa",
                rewrittenLang + "/sites/urlRewriteSite/home/activities.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html",
                rewrittenLang + "/sites/urlRewriteSite/home/activities/last.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html?test=aaa",
                rewrittenLang + "/sites/urlRewriteSite/home/activities/last.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
               rewrittenLang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html",
                rewrittenLang + "/sites/urlRewriteSite/search-results.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html?test=aaa",
                rewrittenLang + "/sites/urlRewriteSite/search-results.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html",
                rewrittenLang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa",
                rewrittenLang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                rewrittenLang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");

        // files
        rewrite(FILES_SERVLET + "/live/sites/urlRewriteSite/files/path/to/my/file.txt",
                FILES_SERVLET + "/live/sites/urlRewriteSite/files/path/to/my/file.txt");

        doSiteUsersTest(lang, rewrittenLang);
    }

    public void doLiveSiteServernameTest(String serverName, String lang) throws Exception {
        String rewrittenLang = DEFAULT_LANG.equals(lang) ? "" : "/" + lang;
        request.setServerName(serverName);

        // home page
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html", rewrittenLang + "/home.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html?test=aaa", rewrittenLang + "/home.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb",
                rewrittenLang + "/home.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                rewrittenLang + "/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html", rewrittenLang + "/home/activities.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html?test=aaa",
                rewrittenLang + "/home/activities.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb",
                rewrittenLang + "/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html",
                rewrittenLang + "/home/activities/last.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html?test=aaa",
                rewrittenLang + "/home/activities/last.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb",
                rewrittenLang + "/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                rewrittenLang + "/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html", rewrittenLang + "/search-results.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html?test=aaa",
                rewrittenLang + "/search-results.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb",
                rewrittenLang + "/search-results.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                rewrittenLang + "/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                rewrittenLang + "/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html",
                rewrittenLang + "/contents/aaa/my-text.viewContent.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa",
                rewrittenLang + "/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                rewrittenLang + "/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");

        // files
        rewrite(FILES_SERVLET + "/live/sites/urlRewriteSite/files/path/to/my/file.txt",
                FILES_SERVLET + "/live/sites/urlRewriteSite/files/path/to/my/file.txt");

        doSiteUsersTest(lang, rewrittenLang);
    }

    private void doSiteUsersTest(String lang, String rewrittenLang)
            throws IOException, ServletException, InvocationTargetException {
        // site users (also for user registration: QA-9629) -> no rewrite of the user path should take place; also the language is preserved
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/users/hh/ai/ie/sergiy.html",
                '/' + lang + "/sites/urlRewriteSite/users/hh/ai/ie/sergiy.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/users/hh/ai/ie/sergiy.unauthenticatedChangePassword.do",
                '/' + lang + "/sites/urlRewriteSite/users/hh/ai/ie/sergiy.unauthenticatedChangePassword.do");

        // with localhost the /sites/<sitekey> part should not be removed from the URL
        String prefix = "localhost".equals(request.getServerName()) ? "/sites/urlRewriteSite" : "";
        // check similar page URLs
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/userssearch",
                rewrittenLang + prefix + "/userssearch");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/userssearch.html",
                rewrittenLang + prefix + "/userssearch.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/userssearch/test.html",
                rewrittenLang + prefix + "/userssearch/test.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/searchusers",
                rewrittenLang + prefix + "/searchusers");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/searchusers.html",
                rewrittenLang + prefix + "/searchusers.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/searchusers/test.html",
                rewrittenLang + prefix + "/searchusers/test.html");
        rewrite(SERVLET + "/render/live/" + lang + "/sites/urlRewriteSite/home/users/test.html",
                rewrittenLang + prefix + "/home/users/test.html");
    }

    protected void rewrite(String in, String expectedOut) throws IOException, ServletException,
            InvocationTargetException {
        rewrite(in, expectedOut, in);
    }

    protected void rewrite(String in, String expectedOut, String expectedIn) throws IOException, ServletException,
            InvocationTargetException {
        if (request.getContextPath().length() > 0) {
            in = request.getContextPath() + in;
            expectedIn = request.getContextPath() + expectedIn;
            expectedOut = request.getContextPath() + expectedOut;
        }

        System.out.println(in);
        request.setRequestURI(in);
        String out = engine.rewriteOutbound(in, request, response);
        System.out.println(" -> " + out);
        assertEquals("Rewritten outbound URL is wrong", expectedOut, out);

        request.setRequestURI(out);
        RewrittenUrl restored = null;
        if (engine.prepareInbound(request, response)) {
            restored = engine.rewriteInbound(request, response);
        }
        assertEquals("Restored (inbound) URL is wrong", StringUtils.substringBefore(expectedIn,"?"), restored != null ? request.getContextPath() + restored.getTarget()
                : out);
    }

    @Before
    public void setUp() throws RepositoryException {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContextPath("");
        request.setServletPath(SERVLET);

        response = new MockHttpServletResponse();
        response.setCharacterEncoding("UTF-8");
    }

    @After
    public void tearDown() throws RepositoryException {
        request = null;
        response = null;
    }

    @Test
    public void testLiveSiteLocalhost() throws Exception {
        doLiveSiteLocalhostTest(DEFAULT_LANG);
    }

    @Test
    public void testLiveSiteLocalhostNonDefaultLanguage() throws Exception {
        doLiveSiteLocalhostTest(SECOND_LANG);
    }

    @Test
    public void testLiveSiteLocalhostWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteLocalhost();
    }

    @Test
    public void testLiveSiteServernameAlias() throws Exception {
        doLiveSiteServernameTest(SERVER_NAME_ALIAS, DEFAULT_LANG);
    }

    @Test
    public void testLiveSiteServernameAliasNonDefaultLanguage() throws Exception {
        doLiveSiteServernameTest(SERVER_NAME_ALIAS, SECOND_LANG);
    }

    @Test
    public void testLiveSiteServername() throws Exception {
        doLiveSiteServernameTest(SERVER_NAME, DEFAULT_LANG);
    }

    public void testLiveSiteServernameNonDefaultLanguage() throws Exception {
        doLiveSiteServernameTest(SERVER_NAME, SECOND_LANG);
    }

    @Test
    public void testLiveSiteServernameNonDefaultLanguageWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteServernameNonDefaultLanguage();
    }

    @Test
    public void testLiveSiteServernameWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteServername();
    }
}
