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

package org.jahia.services.content.interceptor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test case for the {@link HtmlFilteringInterceptor}.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlFilteringInterceptorTest {
	private static JCRNodeWrapper node;
	private static JCRSessionWrapper session;

	private static String loadContent(String resource) throws IOException {
		String content = null;

		InputStream is = HtmlFilteringInterceptorTest.class.getResourceAsStream(resource);
		try {
			content = IOUtils.toString(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
		
		assertNotNull("Cannot read content from resource '" + resource+ "'", content);

		return content;
	}

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		TestHelper.createSite("html-filtering");
		session = JCRSessionFactory.getInstance().getCurrentUserSession();
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
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
	@SuppressWarnings("unchecked")
	public void testFilteringDisabled() throws Exception {
		String source = "abc";
		assertEquals("Filtering should nor be done as the tag set is empty", source,
		        HtmlFilteringInterceptor.filterTags(source, Collections.EMPTY_SET));
	}

	@Test
	public void testHr() throws Exception {
		String source = loadContent("hr.txt");
		
		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("script", "object", "hr")));
		assertFalse("<hr/> tag was not removed", out.contains("<hr"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text 2"));
	}

	@Test
	public void testFormatting() throws Exception {
		String source = loadContent("formatting.txt");
		
		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("b", "i", "strong")));
		assertFalse("<strong/> tag was not removed", out.contains("<strong"));
		assertFalse("<i/> tag was not removed", out.contains("<i"));
		assertFalse("<b/> tag was not removed", out.contains("<b"));
		assertTrue("other elements were incorrectly removed", out.contains("video") && out.contains("here:") && out.contains("require") && out.contains("market") && out.contains("Jahia Solutions"));
	}

}
