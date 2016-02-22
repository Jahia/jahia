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
package org.jahia.test.services.tags;

import java.util.*;

import javax.jcr.*;

import org.jahia.services.content.*;
import org.jahia.services.tags.*;
import org.junit.*;
import org.slf4j.Logger;
import org.jahia.services.SpringContextSingleton;
import org.jahia.test.TestHelper;

import static org.junit.Assert.*;

/**
 * Unit test for the Tagging feature: creating tags, assigning tags to nodes etc.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TaggingTest.class);

    private final static String TESTSITE_NAME = "taggingTest";

    private TaggingService service = (TaggingService) SpringContextSingleton.getBean("org.jahia.services.tags.TaggingService");

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
    }

    private class ChesseTagDeleteCallback implements TagActionCallback<Void> {
        TagsSuggester tagsSuggester;
        JCRSessionWrapper session;

        private ChesseTagDeleteCallback(TagsSuggester tagsSuggester, JCRSessionWrapper session) {
            this.tagsSuggester = tagsSuggester;
            this.session = session;
        }

        @Override
        public void afterTagAction(JCRNodeWrapper node) throws RepositoryException {
            session.save();
            Assert.assertFalse(node.getPropertyAsString("j:tagList").contains("cheese"));
        }

        @Override
        public void onError(JCRNodeWrapper node, RepositoryException e) throws RepositoryException {
            fail("Fail on taging action on node: " + node.getPath());
            throw e;
        }

        @Override
        public Void end() throws RepositoryException {
            Map<String, Long> suggested = tagsSuggester.suggest("cheese", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
            Assert.assertTrue(suggested.size() == 0);
            return null;
        }
    }

    private class BalckOliveTagRenamedCallback implements TagActionCallback<Void> {
        TagsSuggester tagsSuggester;
        JCRSessionWrapper session;

        private BalckOliveTagRenamedCallback(TagsSuggester tagsSuggester, JCRSessionWrapper session) {
            this.tagsSuggester = tagsSuggester;
            this.session = session;
        }

        @Override
        public void afterTagAction(JCRNodeWrapper node) throws RepositoryException {
            session.save();
            Assert.assertFalse(node.getPropertyAsString("j:tagList").contains("black olives"));
            Assert.assertTrue(node.getPropertyAsString("j:tagList").contains("camembert"));
        }

        @Override
        public void onError(JCRNodeWrapper node, RepositoryException e) throws RepositoryException {
            fail("Fail on taging action on node: " + node.getPath());
            throw e;
        }

        @Override
        public Void end() throws RepositoryException {
            Map<String, Long> suggested = tagsSuggester.suggest("black olives", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
            Assert.assertTrue(suggested.size() == 0);

            suggested = tagsSuggester.suggest("camembert", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
            Assert.assertTrue(suggested.size() == 1);
            Assert.assertTrue(suggested.containsKey("camembert"));
            Assert.assertTrue(suggested.get("camembert") == 4);
            return null;
        }
    }

    @Test    
    public void testTagTaggingService() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Void>() {
            public Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper contentFolder = session.getNode("/sites/" + TESTSITE_NAME + "/tags-content");
                for (int i = 0; i < 5; i++) {
                    contentFolder.addNode("content-" + i, "jnt:text");
                }
                service.tag(contentFolder.getNode("content-0"), Arrays.asList(new String[]{"cheese", "Chipmunk", "pepperoni", "black Olives", "aircheck"}));
                service.tag(contentFolder.getNode("content-1"), Arrays.asList(new String[]{"cheese", "chiPmunk", "black Olives", "aircheck"}));
                service.tag(contentFolder.getNode("content-2"), Arrays.asList(new String[]{"cheese"}));
                service.tag(contentFolder.getNode("content-3"), Arrays.asList(new String[]{"cheese", "black Olives"}));
                service.tag(contentFolder.getNode("content-4"), Arrays.asList(new String[]{"pepperoni", "black Olives"}));
                session.save();

                TagsSuggesterImpl tagsSuggester = new TagsSuggesterImpl();
                tagsSuggester.setFaceted(true);
                // faceted: sugest on "ch"
                Map<String, Long> suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 2);
                Assert.assertTrue(suggested.get("cheese") != null);
                Assert.assertTrue(suggested.get("cheese") == 4);
                Assert.assertTrue(suggested.get("chipmunk") != null);
                Assert.assertTrue(suggested.get("chipmunk") == 2);

                // faceted: order by count
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, true, session);
                Assert.assertTrue(suggested.size() == 2);
                Assert.assertTrue(suggested.get("cheese") != null);
                Assert.assertTrue(suggested.get("cheese") == 4);
                Assert.assertTrue(suggested.get("chipmunk") != null);
                Assert.assertTrue(suggested.get("chipmunk") == 2);
                Assert.assertTrue(new TreeMap<>(suggested).firstKey().equals("cheese"));

                // faceted: test minimum count
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 3l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("cheese") != null);
                Assert.assertTrue(suggested.get("cheese") == 4);

                // faceted: test offset and limit
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, 1l, 1l, false, session);
                Assert.assertTrue(suggested.size() == 1);

                // faceted: test limit
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, 1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);

                // faceted: sugest on "chip"
                suggested = tagsSuggester.suggest("chip", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("chipmunk") != null);
                Assert.assertTrue(suggested.get("chipmunk") == 2);

                tagsSuggester.setFaceted(false);
                // simple: suggest on "ch"
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 2);
                Assert.assertTrue(suggested.containsKey("cheese"));
                Assert.assertTrue(suggested.containsKey("chipmunk"));

                // simple: suggest on "chip"
                suggested = tagsSuggester.suggest("chip", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.containsKey("chipmunk"));

                // simple: test offset and limit
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, 1l, 1l, false, session);
                Assert.assertTrue(suggested.size() == 1);

                // simple: test limit
                suggested = tagsSuggester.suggest("ch", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, 1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);

                //untag
                service.untag(contentFolder.getNode("content-2"), Arrays.asList(new String[]{"cheese"}));
                session.save();

                // test property and mixin
                Assert.assertFalse(contentFolder.getNode("content-2").isNodeType("jmix:tagged"));
                Assert.assertFalse(contentFolder.getNode("content-2").hasProperty("j:tagList"));

                // test faceted result:
                tagsSuggester.setFaceted(true);
                suggested = tagsSuggester.suggest("cheese", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("cheese") != null);
                Assert.assertTrue(suggested.get("cheese") == 3);

                // test simple result:
                suggested = tagsSuggester.suggest("cheese", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("cheese") != null);
                Assert.assertTrue(suggested.get("cheese") == 3);

                // rename
                service.renameTag(contentFolder.getNode("content-4"), "pepperoni", "babybel");
                session.save();

                // test with faceted
                suggested = tagsSuggester.suggest("babybel", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("babybel") != null);
                Assert.assertTrue(suggested.get("babybel") == 1);
                suggested = tagsSuggester.suggest("pepperoni", "/sites/" + TESTSITE_NAME + "/tags-content", 1l, -1l, 0l, false, session);
                Assert.assertTrue(suggested.size() == 1);
                Assert.assertTrue(suggested.get("pepperoni") != null);
                Assert.assertTrue(suggested.get("pepperoni") == 1);

                // bench delete tag
                service.deleteTagUnderPath("/sites/" + TESTSITE_NAME + "/tags-content", session, "cheese", new ChesseTagDeleteCallback(tagsSuggester, session));

                // bench rename
                service.renameTagUnderPath("/sites/" + TESTSITE_NAME + "/tags-content", session, "black olives", "camembert", new BalckOliveTagRenamedCallback(tagsSuggester, session));
                return null;
            }
        });
    }
}
