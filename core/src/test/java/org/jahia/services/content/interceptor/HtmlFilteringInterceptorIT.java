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
package org.jahia.services.content.interceptor;

import org.apache.commons.io.IOUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Test case for the {@link HtmlFilteringInterceptor}.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlFilteringInterceptorIT extends AbstractJUnitTest {
    private static JCRNodeWrapper node;
    private static JCRSessionWrapper session;

    private static String loadContent(String resource) throws IOException {
        String content = null;

        InputStream is = HtmlFilteringInterceptorIT.class.getResourceAsStream(resource);
        try {
            content = IOUtils.toString(is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        assertNotNull("Cannot read content from resource '" + resource + "'", content);

        return content;
    }

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        TestHelper.createSite("html-filtering", null);
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite("html-filtering");
    }

    @Before
    public void setUp() throws RepositoryException {

        JCRNodeWrapper shared = session.getNode("/sites/html-filtering/contents");
        if (!shared.isCheckedOut()) {
            session.checkout(shared);
        }
        if (shared.hasNode("html-filtering")) {
            shared.getNode("html-filtering").remove();
        }

        node = shared.addNode("html-filtering", "jnt:mainContent");
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        node.remove();
        session.save();
    }

    @Test
    public void testFilteringDisabled() throws Exception {
        String source = "abc";
        assertEquals("Filtering should nor be done as the tag set is empty", source,
                HtmlFilteringInterceptor.filterTags(source, Collections.<String> emptySet(), false));
    }

    @Test
    public void testHr() throws Exception {
        String source = loadContent("hr.txt");

        String out = HtmlFilteringInterceptor.filterTags(source,
                new HashSet<String>(Arrays.asList("script", "object", "hr")), false);
        assertFalse("<hr/> tag was not removed", out.contains("<hr"));
        assertTrue("other elements were incorrectly removed", out.contains("My separated text"));
        assertTrue("other elements were incorrectly removed", out.contains("My separated text 2"));
    }

    @Test
    public void testFormatting() throws Exception {
        String source = loadContent("formatting.txt");

        String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("b", "i", "strong")),
                false);
        assertFalse("<strong/> tag was not removed", out.contains("<strong"));
        assertFalse("<i/> tag was not removed", out.contains("<i"));
        assertFalse("<b/> tag was not removed", out.contains("<b"));
        assertTrue("other elements were incorrectly removed", out.contains("video") && out.contains("here:")
                && out.contains("require") && out.contains("market") && out.contains("Jahia Solutions"));
    }

}
