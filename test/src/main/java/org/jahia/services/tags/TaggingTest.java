/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.tags;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;

import junit.framework.TestCase;

import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

/**
 * Unit test for the Tagging feature: creating tags, assigning tags to nodes
 * etc.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingTest extends TestCase {

	private static final int TAGS_TO_CREATE = 1000;

    private final static String TESTSITE_NAME = "taggingTest";

	private int counter = 0;

	private TaggingService service;

    private JahiaSite site;

	private String tagPrefix;

	private String generateTagName() {
		return tagPrefix + counter++;
	}

	@Override
	protected void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
		tagPrefix = "test-" + System.currentTimeMillis() + "-";
		service = (TaggingService) SpringContextSingleton.getBean("org.jahia.services.tags.TaggingService");
		// create the first tag
		testCreateTag();
	}

	@Override
	protected void tearDown() throws Exception {
		JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
			public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
				session.checkout("/sites/" + TESTSITE_NAME + "/tags");
				NodeIterator nodeIterator = session.getWorkspace().getQueryManager().createQuery(
				        "select * from [jnt:tag] " + "where ischildnode([/sites/" + TESTSITE_NAME
				                + "/tags]) and name() like '" + tagPrefix + "%'", Query.JCR_SQL2).execute().getNodes();
				while (nodeIterator.hasNext()) {
					Node node = nodeIterator.nextNode();
					try {
						session.checkout(node);
						node.remove();
					} catch (PathNotFoundException e) {
						// ignore -> it is a bug in Jackrabbit that produces
						// duplicate results
					}
				}
				session.checkout("/sites/" + TESTSITE_NAME);
				try {
					session.getNode("/sites/" + TESTSITE_NAME + "/tags-content").remove();
				} catch (PathNotFoundException e) {
					// ignore it
				}
				session.save();
				return null;
			}
		});
        TestHelper.deleteSite(TESTSITE_NAME);

		tagPrefix = null;
		counter = 0;
		service = null;
	}

	public void testCreateMultipleTags() throws RepositoryException {
		String tag = null;
		for (int i = 0; i < TAGS_TO_CREATE; i++) {
			tag = generateTagName();
			service.createTag(tag, TESTSITE_NAME);
		}
	}

	public void testCreateTag() throws RepositoryException {
		String tag = generateTagName();
		service.createTag(tag, TESTSITE_NAME);
		assertTrue("Tag node is not created", service.exists(tag, TESTSITE_NAME));
	}

	public void testDeleteTag() throws RepositoryException {
		String tag = generateTagName();
		service.createTag(tag, TESTSITE_NAME);
		assertTrue(service.deleteTag(tag, TESTSITE_NAME));

		assertFalse(service.deleteTag(tagPrefix + "-1", TESTSITE_NAME));
	}

	public void testTagContentObject() throws RepositoryException {
		JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
			public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
				Node siteNode = session.getNode("/sites/" + TESTSITE_NAME);
				session.checkout(siteNode);
				Node contentFolder = siteNode.addNode("tags-content", "jnt:folder");
				Node contentNode = contentFolder.addNode("content-1", "jnt:content");
				contentNode.addMixin("jmix:tagged");
				session.save();
				return null;
			}
		});

		String tag = null;
		List<String> tags = new LinkedList<String>();
		for (int i = 0; i < 10; i++) {
			tag = generateTagName();
			tags.add(tag);
			service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-1", tag, TESTSITE_NAME, true);
		}

		List<String> assignedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(
		        new JCRCallback<List<String>>() {
			        public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
				        Node node = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content/content-1");
				        Value[] values = node.getProperty("j:tags").getValues();
				        List<String> nodeTags = new LinkedList<String>();
				        for (Value val : values) {
					        nodeTags.add(session.getNodeByIdentifier(val.getString()).getName());
				        }
				        return nodeTags;
			        }
		        });
		assertTrue("Tags were not correctly applied to the node", tags.size() == assignedTags.size()
		        && assignedTags.containsAll(tags));
	}

	public void testTagCount() throws RepositoryException {
		// create 10 content nodes
		JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
			public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
				Node siteNode = session.getNode("/sites/" + TESTSITE_NAME);
				session.checkout(siteNode);
				Node contentFolder = siteNode.addNode("tags-content", "jnt:folder");
				for (int i = 1; i <= 10; i++) {
					Node contentNode = contentFolder.addNode("content-" + i, "jnt:content");
					contentNode.addMixin("jmix:tagged");
				}

				session.save();
				return null;
			}
		});

		// create 10 tags
		final List<String> tags = new LinkedList<String>();
		for (int i = 0; i < 10; i++) {
			String tag = generateTagName();
			tags.add(tag);
			service.createTag(tag, TESTSITE_NAME);
		}

		// tag content using those tags
		for (int i = 1; i <= 10; i++) {
			for (int j = i; j <= 10; j++) {
				service
				        .tag("/sites/" + TESTSITE_NAME + "/tags-content/content-" + i, tags.get(j - 1), TESTSITE_NAME,
				                false);
			}
		}

		// assert the tag count
		JCRTemplate.getInstance().doExecuteWithSystemSession(
		        new JCRCallback<Object>() {
			        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
				        for (int i = 1; i <= 10; i++) {
				        	Node tagNode = session.getNode("/sites/" + TESTSITE_NAME + "/tags/" + tags.get(i-1));
				        	assertEquals("Wrong count for the tag '" + tagNode.getName() + "'", tagNode.getReferences().getSize(), i);
                        }
				        return null;
			        }
		        });
	}

}
