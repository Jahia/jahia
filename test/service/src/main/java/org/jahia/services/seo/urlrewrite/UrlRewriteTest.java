/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.seo.urlrewrite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.jahia.services.SpringContextSingleton;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.tuckey.web.filters.urlrewrite.RewrittenUrl;
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * Test case for URL rewriting.
 * 
 * @author Sergiy Shyrkov
 */
public class UrlRewriteTest {

    private static UrlRewriteEngine engine;

    private static final String SERVLET = "/cms";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        if (SpringContextSingleton.getInstance().isInitialized()) {
            engine = ((UrlRewriteService) SpringContextSingleton.getBean("UrlRewriteService"))
                    .getEngine();
        } else {
            String prop = System.getProperty("urlrewrite.resource");
            if (prop == null) {
                throw new IllegalArgumentException("Unable to find urlrewrite.xml configuration.");
            }
            Log.setLevel("SYSOUT:DEBUG");
            FileInputStream is = null;
            try {
                is = new FileInputStream(prop);
                engine = new UrlRewriteEngine(is, prop);
            } finally {
                IOUtils.closeQuietly(is);
            }
            assertTrue("Configuration is not valid", engine.getConf().isOk());
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        engine = null;
    }

    private String context = "";

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    protected void rewrite(String in, String expectedOut) throws IOException, ServletException,
            InvocationTargetException {
        if (context.length() > 0) {
            in = context + in;
            expectedOut = context + expectedOut;
        }

        System.out.println(in);
        request.setRequestURI(in);
        String out = engine.rewriteOutbound(in, request, response);
        System.out.println(" -> " + out);
        assertEquals("Rewritte outbound URL is wrong", expectedOut, out);

        request.setRequestURI(out);
        RewrittenUrl restored = engine.rewriteInbound(request, response);
        assertEquals("Restored (inbound) URL is wrong", in, restored != null ? restored.getTarget()
                : null);
    }

    @Before
    public void setUp() throws RepositoryException {
        request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        context = "";
        request.setContextPath(context);

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
        rewrite("/cms/render/live/en/sites/ACME/home.html", SERVLET + "/en/sites/ACME/home.html");
        rewrite("/cms/render/live/en/sites/ACME/home.html?test=aaa", SERVLET
                + "/en/sites/ACME/home.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home.html?param1=aaa&param2=bbb", SERVLET
                + "/en/sites/ACME/home.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html", SERVLET
                + "/en/sites/ACME/home/activities.html");
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html?test=aaa", SERVLET
                + "/en/sites/ACME/home/activities.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html?param1=aaa&param2=bbb",
                SERVLET + "/en/sites/ACME/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html", SERVLET
                + "/en/sites/ACME/home/activities/last.html");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html?test=aaa", SERVLET
                + "/en/sites/ACME/home/activities/last.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html?param1=aaa&param2=bbb",
                SERVLET + "/en/sites/ACME/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite("/cms/render/live/en/sites/ACME/search-results.html", SERVLET
                + "/en/sites/ACME/search-results.html");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html?test=aaa", SERVLET
                + "/en/sites/ACME/search-results.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html?param1=aaa&param2=bbb", SERVLET
                + "/en/sites/ACME/search-results.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html", SERVLET
                + "/en/sites/ACME/contents/aaa/my-text.viewContent.html");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?test=aaa",
                SERVLET + "/en/sites/ACME/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteLocalhostWithContext() throws Exception {
        context = "/jahia";
        request.setContextPath(context);
        testLiveSiteLocalhost();
    }

    @Test
    public void testLiveSiteServername() throws Exception {
        // test with the "mapped" server name
        request.setServerName("servername");
        request.setAttribute(ServerNameToSiteMapper.ATTR_NAME_SITE_KEY, "ACME");

        // home page
        rewrite("/cms/render/live/en/sites/ACME/home.html", SERVLET + "/en/home.html");
        rewrite("/cms/render/live/en/sites/ACME/home.html?test=aaa", SERVLET
                + "/en/home.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home.html?param1=aaa&param2=bbb", SERVLET
                + "/en/home.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET + "/en/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // page under home - 1st level
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html", SERVLET
                + "/en/home/activities.html");
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html?test=aaa", SERVLET
                + "/en/home/activities.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home/activities.html?param1=aaa&param2=bbb",
                SERVLET + "/en/home/activities.html?param1=aaa&param2=bbb");

        // page under home - 2st level
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html", SERVLET
                + "/en/home/activities/last.html");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html?test=aaa", SERVLET
                + "/en/home/activities/last.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html?param1=aaa&param2=bbb",
                SERVLET + "/en/home/activities/last.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/home/activities/last.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite("/cms/render/live/en/sites/ACME/search-results.html", SERVLET
                + "/en/search-results.html");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html?test=aaa", SERVLET
                + "/en/search-results.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html?param1=aaa&param2=bbb", SERVLET
                + "/en/search-results.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/search-results.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html", SERVLET
                + "/en/contents/aaa/my-text.viewContent.html");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?test=aaa",
                SERVLET + "/en/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                SERVLET + "/en/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteServernameNotMapped() throws Exception {
        // test first with the server name which has no mapping to a site
        request.setServerName("servername");
        rewrite("/cms/render/live/en/sites/ACME/home.html", SERVLET + "/en/sites/ACME/home.html");
        rewrite("/cms/render/live/en/sites/ACME/home.html?test=aaa", SERVLET
                + "/en/sites/ACME/home.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/home.html?param1=aaa&param2=bbb", SERVLET
                + "/en/sites/ACME/home.html?param1=aaa&param2=bbb");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4",
                SERVLET
                        + "/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4");
        rewrite("/cms/render/live/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/home.html;jsessionid=3731EB090078DDBFF24CC12F69AD2422.qa-j4?param1=aaa&param2=bbb");

        // non-home page
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html", SERVLET
                + "/en/sites/ACME/contents/aaa/my-text.viewContent.html");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?test=aaa",
                SERVLET + "/en/sites/ACME/contents/aaa/my-text.viewContent.html?test=aaa");
        rewrite("/cms/render/live/en/sites/ACME/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb",
                SERVLET
                        + "/en/sites/ACME/contents/aaa/my-text.viewContent.html?param1=aaa&param2=bbb");
    }

    @Test
    public void testLiveSiteServernameNotMappedWithContext() throws Exception {
        context = "/jahia";
        request.setContextPath(context);
        testLiveSiteServernameNotMapped();
    }

    @Test
    public void testLiveSiteServernameWithContext() throws Exception {
        context = "/jahia";
        request.setContextPath(context);
        testLiveSiteServername();
    }
}
