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
import java.util.HashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
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

    private static final String DEFAULT_LANG = "en";

    private static final String SECOND_LANG = "fr";

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
        Set<String> languages = new HashSet<String>();
        languages.add(DEFAULT_LANG);
        languages.add(SECOND_LANG);
        site.setLanguages(languages);
        site.setDefaultLanguage(DEFAULT_LANG);
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        service.updateSite(site);

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
    }

    public void doLiveSiteServernameTest(String lang) throws Exception {
        String rewrittenLang = DEFAULT_LANG.equals(lang) ? "" : "/" + lang;
        request.setServerName(SERVER_NAME);

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
        assertEquals("Restored (inbound) URL is wrong", expectedIn, restored != null ? request.getContextPath() + restored.getTarget()
                : null);
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
    public void testLiveSiteServername() throws Exception {
        doLiveSiteServernameTest(DEFAULT_LANG);
    }

    public void testLiveSiteServernameNonDefaultLanguage() throws Exception {
        doLiveSiteServernameTest(SECOND_LANG);
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
