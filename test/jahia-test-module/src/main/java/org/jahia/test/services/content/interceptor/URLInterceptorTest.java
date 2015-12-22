/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content.interceptor;

import static junit.framework.Assert.*;

import org.jahia.services.sites.JahiaSite;
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
    private static JahiaSite site;
    private static JCRSessionWrapper session;
    private static JCRSessionWrapper localizedSession;
    private static JCRNodeWrapper node;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite("test");
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        site = (JahiaSite) session.getNode("/sites/test");
        setSessionSite(site);
        JCRNodeWrapper shared = session.getNode("/sites/"+site.getSiteKey()+"/contents");
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

        node = shared.addNode("testContent", "jnt:mainContent");
        node = shared.addNode("refContent", "jnt:mainContent");

        session.save();
        localizedSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
    }
    
    @After
    public void tearDown() throws Exception {
        session.save();
        localizedSession.save();
        JCRSessionFactory.getInstance().closeAllSessions();
        site = null;
    }
    
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite("test");
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testBadReferenceEncoding() throws Exception {
        JCRNodeWrapper n = localizedSession.getNode("/sites/"+site.getSiteKey()+"/contents/testContent");
        try {
            String value = "<img src=\"" + Jahia.getContextPath() + "/files/sites/"+site.getSiteKey()+"/contents/noNode\">";
            n.setProperty("body", value);
            fail("Did not throw exception " +value);
        } catch (ConstraintViolationException e) {
        }

        try {
            String value = "<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}/sites/"+site.getSiteKey()+"/contents/noNode.html\">test</a>";
            n.setProperty("body", value);
            fail("Did not throw exception " +value);
        } catch (ConstraintViolationException e) {
        }
    }

    @Test
    public void testEncodeAndDecode() throws Exception {
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/default/en/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/edit/default/en/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/fr/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/en/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/sites/"+site.getSiteKey()+"/contents/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/sites/"+site.getSiteKey()+"/contents/testContent.html\">test</a>");
        validateEncodeAndDecode("<img src=\"" + Jahia.getContextPath() + "/files/default/sites/"+site.getSiteKey()+"/contents/refContent\">");
    }

    private void validateEncodeAndDecode(String value) throws RepositoryException {
        JCRNodeWrapper n = localizedSession.getNode("/sites/"+site.getSiteKey()+"/contents/testContent");
        n.setProperty("body", value);
        assertEquals("Not the same value after get",value, n.getProperty("body").getString());
    }





}
