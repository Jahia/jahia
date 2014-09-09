/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.test.services.tags;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.jahia.services.content.*;
import org.jahia.services.tags.TagHandlerImpl;
import org.jahia.services.tags.TagsSuggesterImpl;
import org.slf4j.Logger;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.tags.TaggingService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for the Tagging feature: creating tags, assigning tags to nodes etc.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TaggingTest.class);

    private final static String TESTSITE_NAME = "taggingTest";

    private static int counter = 0;

    private TaggingService service = (TaggingService) SpringContextSingleton
    .getBean("org.jahia.services.tags.TaggingService");

    private static String tagPrefix = "test-" + System.currentTimeMillis() + "-";

    private String generateTagName() {
        return tagPrefix + counter++;
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            TestHelper.createSite(TESTSITE_NAME);
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Node siteNode = session.getNode("/sites/" + TESTSITE_NAME);
                    siteNode.addNode("tags-content", "jnt:contentList");
                    session.save();
                    return null;
                }
            });
        } catch (Exception e) {
            logger.error("Error setting up TaggingTest environment", e);
        }
    }
    
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
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
    }

    @Test    
    public void testTagContentObject() throws RepositoryException {
        List<String> addedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content");
                contentFolder.addNode("content-0", "jnt:text");

                String tag = null;
                List<String> tags = new LinkedList<String>();
                for (int i = 0; i < 10; i++) {
                    tag = generateTagName();
                    tags.add(tag);
                    service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-0", tag, session);
                }
                session.save();

                return tags;
            }
        });


        List<String> assignedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        Node node = session.getNode("/sites/" + TESTSITE_NAME
                                + "/tags-content/content-0");
                        Value[] values = node.getProperty("j:tagList").getValues();
                        List<String> tags = new LinkedList<String>();
                        for (Value val : values) {
                            tags.add(val.getString());
                        }
                        return tags;
                    }
                });
        assertTrue("Tags were not correctly applied to the node",
                addedTags.size() == assignedTags.size() && assignedTags.containsAll(addedTags) );
    }

    @Test
    public void testUnTagContentObject() throws RepositoryException {
        // add tags
        final List<String> addedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Node contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content");
                contentFolder.addNode("content-15", "jnt:text");

                String tag = null;
                List<String> tags = new LinkedList<String>();
                for (int i = 0; i < 10; i++) {
                    tag = generateTagName();
                    tags.add(tag);
                    service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-15", tag, session);
                }
                session.save();

                return tags;
            }
        });

        // untag some tags
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content/content-15");
                for (int i = 0; i < 10; i++) {
                    if(i % 2 == 0){
                        service.untag(contentFolder, addedTags.get(i));
                    }
                }
                session.save();
                return null;
            }
        });


        List<String> assignedTags = JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        Node node = session.getNode("/sites/" + TESTSITE_NAME
                                + "/tags-content/content-15");
                        Value[] values = node.getProperty("j:tagList").getValues();
                        List<String> tags = new LinkedList<String>();
                        for (Value val : values) {
                            tags.add(val.getString());
                        }
                        return tags;
                    }
                });
        assertTrue("Tags were not correctly applied to the node",
                addedTags.size() / 2 == assignedTags.size());
    }

    @Test    
    public void testFacetedSuggester() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // assert the tag count
                Node contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content");
                for (int i = 1; i <= 10; i++) {
                    contentFolder.addNode("content-45" + i, "jnt:text");
                }
                session.save();

                // create 10 tags
                List<String> tags = new LinkedList<String>();
                for (int i = 0; i < 10; i++) {
                    String tag = generateTagName();
                    tags.add(tag);
                }

                // tag content using those tags
                for (int i = 1; i <= 10; i++) {
                    for (int j = i; j <= 10; j++) {
                        service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-45" + i,
                                tags.get(j - 1), session);
                    }
                }
                session.save();

                for (int i = 1; i <= 10; i++) {
                    String tag = tags.get(i - 1);
                    TagsSuggesterImpl tagsSuggester = new TagsSuggesterImpl();
                    tagsSuggester.setFaceted(true);
                    Map<String, Long> tagsMap =tagsSuggester.suggest(tag, "/sites/" + TESTSITE_NAME, 1l, -1l, 0l, false, session);
                    assertEquals("Wrong count for the tag '" + tag + "'",
                            tagsMap.get(tag).longValue(), new Integer(i).longValue());
                }
                return null;
            }
        });
    }

    @Test
    public void testSimpleSuggester() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // assert the tag count
                Node contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content");
                for (int i = 1; i <= 10; i++) {
                    contentFolder.addNode("content-95" + i, "jnt:text");
                }
                session.save();

                // create 10 tags
                List<String> tags = new LinkedList<String>();
                for (int i = 0; i < 10; i++) {
                    String tag = "test-" + System.currentTimeMillis() + "-" + i;
                    tags.add(tag);
                }

                // tag content using those tags
                for (int i = 1; i <= 10; i++) {
                    for (int j = i; j <= 10; j++) {
                        service.tag("/sites/" + TESTSITE_NAME + "/tags-content/content-95" + i,
                                tags.get(j - 1), session);
                    }
                }
                session.save();

                TagsSuggesterImpl tagsSuggester = new TagsSuggesterImpl();
                tagsSuggester.setFaceted(false);
                Map<String, Long> tagsMap = tagsSuggester.suggest("test-", "/sites/" + TESTSITE_NAME, 1l, 40l, 0l, false, session);
                assertEquals("Wrong number of tags suggested",
                        tagsMap.size(), 10);
                return null;
            }
        });
    }

    @Test
    public void testTagHandler() throws RepositoryException {
        TagHandlerImpl tagHandler = new TagHandlerImpl();
        assertEquals("Tag handler should lower case and trim the tag",
                tagHandler.execute(" TEST "), "test");
    }
}
