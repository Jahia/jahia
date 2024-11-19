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
package org.jahia.test.services.content.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.jahia.services.content.interceptor.HtmlFilteringInterceptor;
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
public class HtmlFilteringInterceptorTest extends HtmlFilteringInterceptor {
	private JCRNodeWrapper node;
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
	public void testFilteringDisabled() throws Exception {
		String source = "abc";
		assertEquals("Filtering should nor be done as the tag set is empty", source,
		        HtmlFilteringInterceptor.filterTags(source, Collections.emptySet(), false));
	}

	@Test
	public void testHr() throws Exception {
		String source = loadContent("hr.txt");

		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("script", "object", "hr")), false);
		assertFalse("<hr/> tag was not removed", out.contains("<hr"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text"));
		assertTrue("other elements were incorrectly removed", out.contains("My separated text 2"));
	}

	@Test
	public void testFormatting() throws Exception {
		String source = loadContent("formatting.txt");

		String out = HtmlFilteringInterceptor.filterTags(source, new HashSet<String>(Arrays.asList("b", "i", "strong")), false);
		assertFalse("<strong/> tag was not removed", out.contains("<strong"));
		assertFalse("<i/> tag was not removed", out.contains("<i"));
		assertFalse("<b/> tag was not removed", out.contains("<b"));
		assertTrue("other elements were incorrectly removed", out.contains("video") && out.contains("here:") && out.contains("require") && out.contains("market") && out.contains("Jahia Solutions"));
	}

}
