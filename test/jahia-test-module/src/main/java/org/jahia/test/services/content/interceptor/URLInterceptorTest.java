/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content.interceptor;

import static org.junit.Assert.*;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.interceptor.URLInterceptor;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.jahia.bin.Jahia;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Locale;

/**
 * Test case for the {@link URLInterceptor}.
 * User: toto
 * Date: Nov 30, 2009
 * Time: 5:30:59 PM
 */
public class URLInterceptorTest extends JahiaTestCase {
    private static final String SITEKEY = "test";
    private static final String SITEPATH = "/sites/" + SITEKEY;
    private JCRSessionWrapper localizedSession;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        TestHelper.createSite(SITEKEY);
    }

    @Before
    public void setUp() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper shared = session.getNode(SITEPATH + "/contents");
        if (!shared.isCheckedOut()) {
            session.checkout(shared);
        }
        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        if (shared.hasNode("refContent")) {
            shared.getNode("refContent").remove();
        }
        session.save();

        shared.addNode("testContent", "jnt:mainContent");
        shared.addNode("refContent", "jnt:mainContent");

        session.save();
        localizedSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        localizedSession.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite("test");
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testBadReferenceEncoding() throws Exception {
        JCRNodeWrapper n = localizedSession.getNode(SITEPATH + "/contents/testContent");
        try {
            String value = "<img src=\"" + Jahia.getContextPath() + "/files" + SITEPATH + "/contents/noNode\">";
            n.setProperty("body", value);
            fail("Did not throw exception " + value);
        } catch (ConstraintViolationException e) {
        }

        try {
            String value = "<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}" + SITEPATH
                    + "/contents/noNode.html\">test</a>";
            n.setProperty("body", value);
            fail("Did not throw exception " + value);
        } catch (ConstraintViolationException e) {
        }
    }

    @Test
    public void testEncodeAndDecode() throws Exception {
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/render/default/en" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/edit/default/en" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/render/live/fr" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/en" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH + "/contents/refContent.html\">test</a>"
                        + "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH + "/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH
                + "/contents/refContent.html\">test</a>" + "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH
                + "/contents/testContent.html\">test</a>");
        validateEncodeAndDecode("<img src=\"" + Jahia.getContextPath() + "/files/default" + SITEPATH + "/contents/refContent\">");
        validateEncodeAndDecode(
                "<a href=\"" + Jahia.getContextPath() + "/cms/%7bmode%7d/%7Blang%7D" + SITEPATH + "/contents/refContent.html\">test</a>",
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}" + SITEPATH + "/contents/refContent.html\">test</a>"
        );
    }

    private void validateEncodeAndDecode(String value) throws RepositoryException {
        validateEncodeAndDecode(value, value);
    }

    /** Allows specifying different input/output values when encoded braces are normalized */
    private void validateEncodeAndDecode(String value, String expected) throws RepositoryException {
        JCRNodeWrapper n = localizedSession.getNode(SITEPATH + "/contents/testContent");
        n.setProperty("body", value);
        assertEquals("Not the same value after get", expected, n.getProperty("body").getString());
    }
}
