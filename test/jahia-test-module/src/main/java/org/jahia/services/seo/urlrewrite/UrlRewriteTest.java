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

package org.jahia.services.seo.urlrewrite;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
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
public class UrlRewriteTest {

    private static UrlRewriteService engine;

    private static final String SERVER_NAME = "urlrewrite.jahia.org";

    private static final String SERVLET = "/cms";

    private static final String SITE_KEY = "urlRewriteSite";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        engine = (UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService");
        engine.setSeoRulesEnabled(true);
        engine.afterPropertiesSet();
        JahiaSite site = TestHelper.createSite(SITE_KEY, SERVER_NAME, TestHelper.WEB_TEMPLATES, null, null);

        ((ParamBean) Jahia.getThreadParamBean()).getSession(true).setAttribute(ParamBean.SESSION_SITE, site);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(SITE_KEY);
        JCRSessionFactory.getInstance().closeAllSessions();
        engine = null;
    }

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

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
        assertEquals("Restored (inbound) URL is wrong", expectedIn, restored != null ? request.getContextPath() + restored.getTarget()
                : null);
    }

    protected void rewrite(String in, String expectedOut) throws IOException, ServletException,
            InvocationTargetException {
        rewrite(in, expectedOut, in);
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
        // home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html", "/en/sites/urlRewriteSite/home.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html?test=aaa",
                "/en/sites/urlRewriteSite/home.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html",
                "/en/sites/urlRewriteSite/home/activities.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html?test=aaa",
                "/en/sites/urlRewriteSite/home/activities.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html",
                "/en/sites/urlRewriteSite/home/activities/last.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html?test=aaa",
                "/en/sites/urlRewriteSite/home/activities/last.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
               "/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html",
                "/en/sites/urlRewriteSite/search-results.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html?test=aaa",
                "/en/sites/urlRewriteSite/search-results.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html",
                "/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa",
                "/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                "/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteLocalhostWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteLocalhost();
    }

    @Test
    public void testLiveSiteServername() throws Exception {
        request.setServerName(SERVER_NAME);

        // home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html", "/home.html",
                SERVLET + "/render/live/sites/urlRewriteSite/home.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html?test=aaa", "/home.html?test=aaa",
                SERVLET + "/render/live/sites/urlRewriteSite/home.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb",
                "/home.html?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET + "/render/live/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html", "/home/activities.html",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html?test=aaa",
                "/home/activities.html?test=aaa",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb",
                "/home/activities.html?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html",
                "/home/activities/last.html",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities/last.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html?test=aaa",
                "/home/activities/last.html?test=aaa",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities/last.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb",
                "/home/activities/last.html?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html", "/search-results.html",
                SERVLET + "/render/live/sites/urlRewriteSite/search-results.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html?test=aaa",
                "/search-results.html?test=aaa",
                SERVLET + "/render/live/sites/urlRewriteSite/search-results.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb",
                "/search-results.html?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET + "/render/live/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html",
                "/contents/aaa/my-text.viewContent.html",
                SERVLET + "/render/live/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa",
                "/contents/aaa/my-text.viewContent.html?test=aaa",
                SERVLET + "/render/live/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite(SERVLET + "/render/live/en/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                "/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                SERVLET + "/render/live/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteServernameWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteServername();
    }

    @Test
    public void testLiveSiteServernameNonDefaultLanguage() throws Exception {
        request.setServerName(SERVER_NAME);

        // home page
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home.html", "/fr/home.html");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home.html?test=aaa", "/fr/home.html?test=aaa");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home.html?param1=aaa&param2=bbb",
                "/fr/home.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/fr/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/fr/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities.html", "/fr/home/activities.html");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities.html?test=aaa",
                "/fr/home/activities.html?test=aaa");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities.html?param1=aaa&param2=bbb",
                "/fr/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities/last.html",
                "/fr/home/activities/last.html");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities/last.html?test=aaa",
                "/fr/home/activities/last.html?test=aaa");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities/last.html?param1=aaa&param2=bbb",
                "/fr/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/fr/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/fr/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/search-results.html", "/fr/search-results.html");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/search-results.html?test=aaa",
                "/fr/search-results.html?test=aaa");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/search-results.html?param1=aaa&param2=bbb",
                "/fr/search-results.html?param1=aaa&param2=bbb");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                "/fr/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                "/fr/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html",
                "/fr/contents/aaa/my-text.viewContent.html");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?test=aaa",
                "/fr/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite(SERVLET + "/render/live/fr/sites/urlRewriteSite/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                "/fr/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteServernameNonDefaultLanguageWithContext() throws Exception {
        request.setContextPath("/jahia");
        testLiveSiteServernameNonDefaultLanguage();
    }
}
