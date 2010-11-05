/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import org.slf4j.Logger;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the Tagging feature: creating tags, assigning tags to nodes etc.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TaggingTest.class);
    
    private static final int TAGS_TO_CREATE = 1000;

    private final static String TESTSITE_NAME = "taggingTest";

    private int counter = 0;

    private TaggingService service;

    private String tagPrefix;

    private String generateTagName() {
        return tagPrefix + counter++;
    }

    @Before
    public void setUp() {
        try {
            TestHelper.createSite(TESTSITE_NAME);
            tagPrefix = "test-" + System.currentTimeMillis() + "-";
            service = (TaggingService) SpringContextSingleton
                    .getBean("org.jahia.services.tags.TaggingService");
            // create the first tag
            testCreateTag();
        } catch (Exception e) {
            logger.error("Error setting up TaggingTest environment", e);
        }
    }

    @After
    public void tearDown() {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getWorkspace().getVersionManager()
                            .checkout("/sites/" + TESTSITE_NAME + "/tags");
                    NodeIterator nodeIterator = session
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery(
                                    "select * from [jnt:tag] " + "where ischildnode([/sites/"
                                            + TESTSITE_NAME + "/tags]) and name() like '" + tagPrefix
                                            + "%'", Query.JCR_SQL2).execute().getNodes();
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
                    session.getWorkspace().getVersionManager().checkout("/sites/" + TESTSITE_NAME);
                    try {
                        session.getNode("/sites/" + TESTSITE_NAME + "/tags-content").remove();
                    } catch (PathNotFoundException e) {
                        // ignore it
                    }
                    session.save();
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error tearing down TaggingTest environment", e);
        }
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.error("Error tearing down TaggingTest environment", e);
        }   
        tagPrefix = null;
        counter = 0;
        service = null;
    }

    @Test    
    public void testCreateMultipleTags() throws RepositoryException {
        String tag = null;
        for (int i = 0; i < TAGS_TO_CREATE; i++) {
            tag = generateTagName();
            service.createTag(tag, TESTSITE_NAME);
        }
    }

    @Test    
    public void testCreateTag() throws RepositoryException {
        String tag = generateTagName();
        service.createTag(tag, TESTSITE_NAME);
        assertTrue("Tag node is not created", service.exists(tag, TESTSITE_NAME));
    }

    @Test    
    public void testDeleteTag() throws RepositoryException {
        String tag = generateTagName();
        service.createTag(tag, TESTSITE_NAME);
        assertTrue(service.deleteTag(tag, TESTSITE_NAME));

        assertFalse(service.deleteTag(tagPrefix + "-1", TESTSITE_NAME));
    }

    @Test    
    public void testTagContentObject() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node siteNode = session.getNode("/sites/" + TESTSITE_NAME);
                session.checkout(siteNode);
                Node contentFolder = siteNode.addNode("tags-content", "jnt:contentList");
                Node contentNode = contentFolder.addNode("content-1", "jnt:text");
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
            service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-1", tag, TESTSITE_NAME,
                    true);
        }

        List<String> assignedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        Node node = session.getNode("/sites/" + TESTSITE_NAME
                                + "/tags-content/content-1");
                        Value[] values = node.getProperty("j:tags").getValues();
                        List<String> nodeTags = new LinkedList<String>();
                        for (Value val : values) {
                            nodeTags.add(session.getNodeByIdentifier(val.getString()).getName());
                        }
                        return nodeTags;
                    }
                });
        assertTrue("Tags were not correctly applied to the node",
                tags.size() == assignedTags.size() && assignedTags.containsAll(tags));
    }

    @Test    
    public void testTagCount() throws RepositoryException {
        // create 10 content nodes
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node siteNode = session.getNode("/sites/" + TESTSITE_NAME);
                session.checkout(siteNode);
                Node contentFolder = siteNode.addNode("tags-content", "jnt:contentList");
                for (int i = 1; i <= 10; i++) {
                    Node contentNode = contentFolder.addNode("content-" + i, "jnt:text");
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
                service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-" + i,
                        tags.get(j - 1), TESTSITE_NAME, false);
            }
        }

        // assert the tag count
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (int i = 1; i <= 10; i++) {
                    Node tagNode = session.getNode("/sites/" + TESTSITE_NAME + "/tags/"
                            + tags.get(i - 1));
                    assertEquals("Wrong count for the tag '" + tagNode.getName() + "'", tagNode
                            .getWeakReferences().getSize(), i);
                }
                return null;
            }
        });
    }

}
