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

package org.jahia.test.services.publication;

import java.util.*;

import javax.jcr.*;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.PublicationInfoNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.*;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

/**
 * Unit test for publish / unpublish using JCR
 * - tests publish / unpublish of pages, container lists, containers
 * - with different language settings (all, none, one, two languages)
 * - with using user not having rights
 * - publication with automatically publishing parent
 *
 * @author Benjamin Papez
 *         <p/>
 *         <p/>
 *         TestA - standard nodes :
 *         <p/>
 *         1/ create new node "nodeA" - publish it - check in live if nodeA is here
 *         2/ modify nodeA - publish it - check in live if nodeA is modified
 *         3/ unpublish nodeA - check in live if nodeA has disappeared
 *         4/ publish nodeA - check in live if nodeA is here
 *         5/ rename nodeA, in the same location - publish it - check that nodeA was properly renamed.
 *         6/ move nodeA in same list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 *         7/ move nodeA in another list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 *         8/ move and modify nodeA in another list, before a node - publish it - check if nodeA is correctly modified and moved (and removed from original place)
 *         9/ move nodeA in another list, before a node twice - publish it - check if nodeA is correctly moved (and removed from original place)
 *         10/ delete nodeA - publish parent - check in live the node is deleted
 *         11/ Mark for deletion the node, publish it and check that it has disappeared in live mode.
 *         12/ Delete a published node and recreate a new one with the same name. Republish and check.
 *         <p/>
 */
public class PublicationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationTest.class);

    private static JahiaSite site;
    private static JCRPublicationService jcrService;

    private final static String TESTSITE_NAME = "jcrPublicationTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";

    private JCRSessionWrapper englishEditSession;
    private JCRSessionWrapper englishLiveSession;

    private JCRNodeWrapper testHomeEdit;

    @BeforeClass
    public static void setUpClass() {
        try {
            site = TestHelper.createSite(TESTSITE_NAME, new HashSet<String>(Arrays.asList("en","fr")),Collections.singleton("en"), false);
            assertNotNull(site);

            jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();

        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() {
        try {
            getCleanSession();
            JCRNodeWrapper englishEditSiteHomeNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home");
            testHomeEdit = englishEditSiteHomeNode.addNode("test"+System.currentTimeMillis(), "jnt:page");
            testHomeEdit.setProperty("jcr:title", "Test page");
            testHomeEdit.setProperty("j:templateName", "simple");
            englishEditSession.save();
            jcrService.publishByMainId(testHomeEdit.getIdentifier());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            fail("Cannot setUp test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            getCleanSession();
            englishEditSession.getNodeByIdentifier(testHomeEdit.getIdentifier()).remove();
            englishEditSession.save();
            englishLiveSession.getNodeByIdentifier(testHomeEdit.getIdentifier()).remove();
            englishLiveSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            fail("Cannot remove nodes: " + e.getMessage());
        }
    }

    @Test
    public void testNodePublish() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 1 : let's check the existence of the node property value in the live workspace.
        testPropertyInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + "1", "Property value for text node 1 was not found or invalid in english live workspace");

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");
        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");
    }

    @Test
    public void testNodeUpdate() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 2 : now let's modify the node, republish and check.
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");
        editTextNode1.setProperty("body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1");
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        testPropertyInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1", "Property value for text node 1 was not found or invalid in english live workspace");
    }

    @Test
    public void testNodeUnpublish() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 3 : not let's unpublish the node and test it's presence in the live workspace.
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");
        jcrService.unpublish(Lists.newArrayList(editTextNode1.getIdentifier()));

        // Need to add this, as otherwise the unpublished node will still be served from cache
        JCRSessionFactory.getInstance().closeAllSessions();
        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

        // englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        testNodeNotInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was unpublished, should not be available in the live workspace anymore !");

        // Case 4 : now let's publish the parent node once again, and check if it is published properly.
        jcrService.publishByMainId(editTextNode1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);
        testNodeInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was re-published, it should have been present in the live workspace");
    }

    @Test
    public void testNodeRename() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 5 : rename node, publish it and check that it was properly renamed.
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");
        editTextNode1.rename("contentList1_text1_renamed");
        englishEditSession.save();
        getCleanSession();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        testNodeInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");

        JCRNodeWrapper liveContentList1 = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");
        testChildOrdering(liveContentList1, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1_renamed", "contentList1_text2");
        // now let's move it back to continue the tests.
        editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1_renamed");
        editTextNode1.rename("contentList1_text1");
        englishEditSession.save();

        // Need to add this, as otherwise the node renaming back to the previous name will still be in live session cache with the intermediate name 
        getCleanSession();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);
        testNodeInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        liveContentList1 = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");
        testChildOrdering(liveContentList1, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");
    }

    @Test
    public void testNodeReorder() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 6 : now we must move the text node inside the list, and check that the move is properly propagated in live mode
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        editContentList1.orderBefore("contentList1_text1", "contentList1_text0");
        englishEditSession.save();
        testChildOrdering(editContentList1, Constants.EDIT_WORKSPACE, "contentList1_text1", "contentList1_text0", "contentList1_text2");

        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");

        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList1_text0", "contentList1_text2");
    }

    @Test
    public void testNodeMove() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(testHomeEdit, "contentList2", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 7 : now let's move the node to another container list.
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper editContentList2 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList2");

        JCRNodeWrapper editTextNode1 = editContentList1.getNode("contentList1_text1");
        englishEditSession.move(editTextNode1.getPath(), testHomeEdit.getPath() + "/contentList2/contentList1_text1");
        editContentList2.orderBefore("contentList1_text1", "contentList2_text0");
        englishEditSession.save();

        getCleanSession();

        // jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publishByMainId(editContentList2.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        getCleanSession();

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList2");
        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text2", "contentList1_text3");
        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList2_text0", "contentList2_text1");
    }

    @Test
    public void testNodeMoveAndUpdate() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(testHomeEdit, "contentList2", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper editContentList2 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList2");

        englishEditSession.move(testHomeEdit.getPath() + "/contentList1/contentList1_text1", testHomeEdit.getPath() + "/contentList2/contentList1_text1");
        englishEditSession.save();

        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList2/contentList1_text1");
        editTextNode1.setProperty("body", "English text update 2");
        englishEditSession.save();
        editContentList2.orderBefore("contentList1_text1", "contentList2_text0");
        englishEditSession.save();

        getCleanSession();
        jcrService.publishByMainId(editContentList2.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList2");
        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text2", "contentList1_text3");
        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList2_text0", "contentList2_text1", "contentList2_text2", "contentList2_text3");
    }

    @Test
    public void testNodeMoveTwice() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList3", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(testHomeEdit, "contentList4", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.
        englishEditSession.getNode(testHomeEdit.getPath() + "/contentList3");
        JCRNodeWrapper editContentList4 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList4");

        englishEditSession.move(testHomeEdit.getPath() + "/contentList3/contentList3_text1", testHomeEdit.getPath() + "/contentList4/contentList3_text1");
        editContentList4.orderBefore("contentList3_text1", "contentList4_text1");
        editContentList4.orderBefore("contentList3_text1", "contentList4_text0");
        englishEditSession.save();
        englishEditSession.getNode(testHomeEdit.getPath() + "/contentList4/contentList3_text1");

        getCleanSession();
        jcrService.publishByMainId(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        JCRNodeWrapper liveContentList3Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList3");
        JCRNodeWrapper liveContentList4Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList4");
        testChildOrdering(liveContentList3Node, Constants.LIVE_WORKSPACE, "contentList3_text0", "contentList3_text2", "contentList3_text3");
        testChildOrdering(liveContentList4Node, Constants.LIVE_WORKSPACE, "contentList3_text1", "contentList4_text0", "contentList4_text1", "contentList4_text2", "contentList4_text3");
    }

    @Test
    public void testNodeDelete() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");
        editTextNode1.remove();
        englishEditSession.save();

        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        testNodeNotInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was deleted, should not be available in the live workspace anymore !");
    }

    @Test
    public void testNodeMarkForDelete() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        // Case 11 : Mark for deletion the node, publish it and check that it has disappeared in live mode.
        JCRNodeWrapper editContentList4 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");
        editTextNode1.markForDeletion("Deleted by unit test");
        englishEditSession.save();

        jcrService.publishByMainId(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        testNodeNotInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "Text node 1 was deleted, should not be available in the live workspace anymore !");
    }

    @Test
    public void testNodeAddAndReorder() throws Exception {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        // Case 12 : Reorder nodes, add a new node and order it - check everything is correctly ordered in the end

        JCRNodeWrapper editContentList1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1");
        editContentList1.orderBefore("contentList1_text2", "contentList1_text3");
        editContentList1.orderBefore("contentList1_text1", "contentList1_text2");
        editContentList1.orderBefore("contentList1_text0", "contentList1_text1");
        englishEditSession.save();
        testChildOrdering(editContentList1, Constants.EDIT_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");

        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(testHomeEdit.getPath() + "/contentList1");

        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");

        for (int i = 4; i < 8; i++) {
            JCRNodeWrapper textNode = editContentList1.addNode(editContentList1.getName() + "_text" + Integer.toString(i), "jnt:mainContent");
            textNode.setProperty("jcr:title", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + Integer.toString(i));
            textNode.setProperty("body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + Integer.toString(i));
        }

        englishEditSession.save();
        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2", "contentList1_text3", "contentList1_text4", "contentList1_text5", "contentList1_text6", "contentList1_text7");

        JCRNodeWrapper textNode = editContentList1.addNode(editContentList1.getName() + "_text8", "jnt:mainContent");
        textNode.setProperty("jcr:title", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + 8);
        textNode.setProperty("body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + 8);

        editContentList1.orderBefore("contentList1_text8", "contentList1_text1");

        englishEditSession.save();
        testChildOrdering(editContentList1, Constants.EDIT_WORKSPACE, "contentList1_text0", "contentList1_text8", "contentList1_text1");

        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);
        testChildOrdering(liveContentList1Node, Constants.EDIT_WORKSPACE, "contentList1_text0", "contentList1_text8", "contentList1_text1");
    }


    @Test
    public void testNodeRemoveAndAdd() throws Exception {
        JCRNodeWrapper list = TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        // Case 12 : Delete a published node and recreate a new one with the same name. Republish and check.

        jcrService.publishByMainId(testHomeEdit.getIdentifier());
        String firstId = list.getIdentifier();
        list.remove();
        list = TestHelper.createList(testHomeEdit, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        String secondId = list.getIdentifier();
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier());

        try {
            englishLiveSession.getNodeByUUID(firstId);
            fail("Node should have been deleted");
        } catch (ItemNotFoundException e) {
        }

        JCRNodeWrapper liveList = englishLiveSession.getNode(list.getPath());
        assertEquals("Invalid uuid", secondId, liveList.getIdentifier());
    }

    @Test
    public void testAddMixinAndPublish() throws RepositoryException {
        //  Add mixin to a node already published once, publish it, and verify that the new mixin was properly added.
        testHomeEdit.addMixin("jmix:sitemap");
        englishEditSession.save();

        assertTrue("Page should now have the sitemap mixin type in edit workspace", testHomeEdit.isNodeType(
                "jmix:sitemap"));

        testPropertyInWorkspace(englishEditSession, testHomeEdit.getPath(), "changefreq", "monthly",
                "Propery changefreq should have default value of 'monthly'");

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                null, false, null);

        JCRNodeWrapper englishLivePage1 = englishLiveSession.getNode(testHomeEdit.getPath());
        assertTrue("Page should now have the sitemap mixin type in live workspace", englishLivePage1.isNodeType(
                "jmix:sitemap"));
        testPropertyInWorkspace(englishLiveSession, testHomeEdit.getPath(), "changefreq", "monthly",
                "Propery changefreq should have default value of 'monthly'");

        testHomeEdit.removeMixin("jmix:sitemap");
        englishEditSession.save();

        assertFalse("pageAddMixin should now no longer have the sitemap mixin in the edit workspace!",
                testHomeEdit.isNodeType("jmix:sitemap"));

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                null, false, null);
        englishLivePage1 = englishLiveSession.getNode(testHomeEdit.getPath());
        assertFalse("Page should now no longer have the sitemap mixin in the live workspace!",
                englishLivePage1.isNodeType("jmix:sitemap"));
    }

    @Test
    public void testPublicationMixin() throws RepositoryException {
        TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        String listPath = testHomeEdit.getPath() + "/contentList1/contentList1_text2";
        englishEditSession.getNode(listPath).addMixin("jmix:publication");
        englishEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        // Let's check the existence of the node property value in the live workspace.
        testPropertyInWorkspace(englishLiveSession, testHomeEdit.getPath() + "/contentList1/contentList1_text1", "body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + "1", "Property value for text node 1 was not found or invalid in english live workspace");

        // Let's check the non existence of the subpage in the live workspace
        testNodeNotInWorkspace(englishLiveSession, listPath, "Sub page should have not been published");

        // Publish with all sub tree
        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
        testNodeInWorkspace(englishLiveSession, listPath, "Sub page should have been published");
    }

    @Test
    public void testMultiLanguagePublication() throws RepositoryException {
        // Set french title
        JCRSessionWrapper frenchEditSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LanguageCodeConverters.languageCodeToLocale("fr"));
        JCRNodeWrapper page = frenchEditSession.getNode(testHomeEdit.getPath());
        page.setProperty("jcr:title", "French title");
        frenchEditSession.save();

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Collections.singleton("en"), false, null);

        // Let's check the existence of the node property value in the live workspace.
        testNodeInWorkspace(englishLiveSession, testHomeEdit.getPath(), "Page should be published in english");

        JCRSessionWrapper frenchLiveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, LanguageCodeConverters.languageCodeToLocale("fr"));

        // Let's check the non existence of the subpage in the live workspace
        testNodeNotInWorkspace(frenchLiveSession, testHomeEdit.getPath(), "Page should not be published in english");

        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, Collections.singleton("fr"), false, null);
        testNodeInWorkspace(frenchLiveSession, testHomeEdit.getPath(), "Page should be published in french");
    }

    @Test
    public void testPublicationStatus() throws RepositoryException {
        JCRNodeWrapper list = TestHelper.createList(testHomeEdit, "contentList1", 4, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(testHomeEdit.getPath() + "/contentList1/contentList1_text1");

        englishEditSession.save();
        List<PublicationInfo> infos = jcrService.getPublicationInfo(testHomeEdit.getIdentifier(), null, false, true, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);

        assertEquals("Invalid status for page", PublicationInfo.PUBLISHED,getStatusFor(infos, testHomeEdit.getIdentifier()));
        assertEquals("Invalid status for list", PublicationInfo.NOT_PUBLISHED,getStatusFor(infos, list.getIdentifier()));
        assertEquals("Invalid status for content", PublicationInfo.NOT_PUBLISHED,getStatusFor(infos, editTextNode1.getIdentifier()));

        // Publish content
        jcrService.publishByMainId(testHomeEdit.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, false, null);

        infos = jcrService.getPublicationInfo(testHomeEdit.getIdentifier(), null, false, true, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);

        assertEquals("Invalid status for page", PublicationInfo.PUBLISHED,getStatusFor(infos, testHomeEdit.getIdentifier()));
        assertEquals("Invalid status for list", PublicationInfo.PUBLISHED,getStatusFor(infos, list.getIdentifier()));
        assertEquals("Invalid status for content", PublicationInfo.PUBLISHED,getStatusFor(infos, editTextNode1.getIdentifier()));

        // Change un-internationalized property
        editTextNode1.setProperty("align", "right");
        englishEditSession.save();

        infos = jcrService.getPublicationInfo(testHomeEdit.getIdentifier(), null, false, true, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);

        assertEquals("Invalid status for page", PublicationInfo.PUBLISHED,getStatusFor(infos, testHomeEdit.getIdentifier()));
        assertEquals("Invalid status for list", PublicationInfo.PUBLISHED,getStatusFor(infos, list.getIdentifier()));
        assertEquals("Invalid status for content", PublicationInfo.MODIFIED,getStatusFor(infos, editTextNode1.getIdentifier()));
    }

    private int getStatusFor(List<PublicationInfo> infos, String uuid) {
        for (PublicationInfo info : infos) {
            int i = getStatusFor(info.getRoot(), uuid);
            if (i > 0) {
                return i;
            }
        }
        return 0;
    }

    private int getStatusFor(PublicationInfoNode info, String uuid) {
        if (info.getUuid().equals(uuid)) {
            return info.getStatus();
        }
        for (PublicationInfoNode node : info.getChildren()) {
            int i = getStatusFor(node, uuid);
            if (i > 0) {
                return i;
            }
        }
        return 0;
    }



//    @Test
//    public void testCPagesWithSubPages() throws RepositoryException {
//        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
//
//        getCleanSession();
//        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
//        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");
//        String englishEditSiteHomeNodeIdentifier = englishEditSiteHomeNode.getIdentifier();
//
//        // now let's setup the pages we will use.
//
//        JCRNodeWrapper englishEditPage1Node = englishEditSiteHomeNode.addNode("page1", "jnt:page");
//        String englishEditPage1NodeIdentifier = englishEditPage1Node.getIdentifier();
//        englishEditPage1Node.setProperty("jcr:title", "Page1");
//        TestHelper.createList(englishEditPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        JCRNodeWrapper englishEditPage2Node = englishEditSiteHomeNode.addNode("page2", "jnt:page");
//        englishEditPage2Node.setProperty("jcr:title", "Page2");
//        TestHelper.createList(englishEditPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        JCRNodeWrapper englishEditPage3Node = englishEditSiteHomeNode.addNode("page3", "jnt:page");
//        englishEditPage3Node.setProperty("jcr:title", "Page3");
//        TestHelper.createList(englishEditPage3Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditPage3Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//
//        JCRNodeWrapper englishEditSubPage1Node = englishEditPage1Node.addNode("page1_subpage1", "jnt:page");
//        englishEditSubPage1Node.setProperty("jcr:title", "SubPage1");
//        TestHelper.createList(englishEditSubPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSubPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        JCRNodeWrapper englishEditSubPage2Node = englishEditPage1Node.addNode("page1_subpage2", "jnt:page");
//        englishEditSubPage2Node.setProperty("jcr:title", "SubPage2");
//        TestHelper.createList(englishEditSubPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSubPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//
//        englishEditSession.save();
//
//        Set<String> languages = null;
//        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        jcrService.publishByMainId(englishEditPage2Node.getIdentifier(), Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        jcrService.publishByMainId(englishEditSubPage1Node.getIdentifier(), Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//
//        // Case 1 : let's check the existence of the node property value in the live workspace.
//        getCleanSession();
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
//                "Page 1 should have been published");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
//                "ContentList0 on Page 1 should have been published");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0/contentList0_text0",
//                "Text0 in ContentList0 on Page 1 should have been published");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
//                "Sub Page 1 should have been published");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1",
//                "ContentList1 on Sub Page 1 should have been published");
//        testNodeInWorkspace(englishLiveSession,
//                SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1/contentList1_text1",
//                "Text1 in ContentList1 on Sub Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
//                "Sub Page 2 should not have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1",
//                "ContentList1 on Sub Page 2 should not have been published");
//        testNodeNotInWorkspace(englishLiveSession,
//                SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1/contentList1_text1",
//                "Text1 in ContentList1 on Sub Page 2 should not have been published");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2",
//                "Page 2 should have been published");
//
//        // Case 2 : now let's modify the node, republish and check.
//        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditPage1NodeIdentifier));
//        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
//                SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed");
//        englishEditSession.save();
//
//        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//
//        getCleanSession();
//
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
//                "Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
//                "ContentList0 on Page 1 not be present anymore since we renamed it.");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed",
//                "ContentList0_renamed on Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
//                "Sub Page 2 should not have been published");
//
//        // Case 3 : now let's unpublish the node and test it's presence in the live workspace.
//        getCleanSession();
//        jcrService.unpublish(Collections.singletonList(englishEditPage1NodeIdentifier), languages);
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
//                "Page 1 should have been unpublished !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
//                "Sub Page 1 should not have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
//                "Sub Page 2 should not have been published");
//
//        // Case 4 : now let's republish the node and test it's presence in the live workspace.
//        getCleanSession();
//        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
//                "Page 1 should have be published !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
//                "Sub Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
//                "Sub Page 2 should not have been published");
//
//        // Case 5 : let's rename the page and check it's been properly renamed in the live workspace.
//        getCleanSession();
//        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
//        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1", SITECONTENT_ROOT_NODE + "/home/page1_renamed");
//        englishEditSession.save();
//        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed",
//                "Page 1 should have be published !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage1",
//                "Sub Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage2",
//                "Sub Page 2 should not have been published");
//
//        // now let's move it back to continue the tests.
//        getCleanSession();
//        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
//        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1_renamed", SITECONTENT_ROOT_NODE + "/home/page1");
//        englishEditSession.save();
//        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, false, null);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
//                "Page 1 should have be published !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
//                "Sub Page 1 should have been published");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
//                "Sub Page 2 should not have been published");
//
//        // Case 6 : now we must move the page inside the list of the parent page, and check that the move is properly propagated in live mode
//        getCleanSession();
//        englishEditSiteHomeNode = englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier);
//        englishEditSession.checkout(englishEditSiteHomeNode);
//        englishEditSiteHomeNode.orderBefore("page1", null); // this should put it at the end of the list.
//        englishEditSession.save();
//        testChildOrdering(englishEditSiteHomeNode, Constants.EDIT_WORKSPACE, "page2", "page3", "page1");
//        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, true, null);
//        JCRNodeWrapper englishLiveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
//        testChildOrdering(englishLiveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3", "page1");
//
//        // Case 7 : now let's move the page to another page
//        getCleanSession();
//        JCRNodeWrapper editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
//        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
//        JCRNodeWrapper editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
//        englishEditSession.checkout(editPage2);
//        englishEditSession.checkout(
//                editPage1); // we have to check it out because of a property being changed during move !
//        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page2/page1");
//        editPage2.orderBefore("page1", "contentList1");
//        englishEditSession.save();
//        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, true, null);
//        JCRNodeWrapper liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
//        JCRNodeWrapper livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
//        testChildOrdering(liveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3");
//        testChildOrdering(livePage2, Constants.LIVE_WORKSPACE, "contentList0", "page1", "contentList1");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2/page1",
//                "Page 1 was not properly moved below page 2 in the live workspace.");
//
//        // Case 8 : now let's move it to yet another list, modify it, then publish it.
//        getCleanSession();
//        editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
//        englishEditSession.checkout(editPage2);
//        JCRNodeWrapper editPage3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3");
//        englishEditSession.checkout(editPage3);
//        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2/page1");
//        englishEditSession.checkout(editPage1);
//        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page3/page1");
//        editPage3.orderBefore("page1", "contentList1");
//        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3/page1");
//        editPage1.addNode("anotherList", "jnt:contentList");
//        englishEditSession.save();
//        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
//                Constants.LIVE_WORKSPACE, languages, true, null);
//
//        liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
//        livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
//        JCRNodeWrapper livePage3 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3");
//        testChildOrdering(liveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3");
//        testChildOrdering(livePage2, Constants.LIVE_WORKSPACE, "contentList0", "contentList1");
//        testChildOrdering(livePage3, Constants.LIVE_WORKSPACE, "contentList0", "page1", "contentList1");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1",
//                "Page 1 was not properly moved below page 3 in the live workspace.");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1/anotherList",
//                "New list on Page 1 was not found in the live workspace.");
//
//        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.
//
//        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
//
//    }

    private void testNodeInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath, String failureMessage)
            throws RepositoryException {
        boolean nodeWasFoundInLive;
        nodeWasFoundInLive = false;
        try {
            sessionWrapper.getNode(absoluteNodePath);
            nodeWasFoundInLive = true;
        } catch (PathNotFoundException pnfe) {
            // this is what we expect, so let's just signal it.
            nodeWasFoundInLive = false;
        }

        String treeString = "";
        if (nodeWasFoundInLive == false) {
            // if it was not found when it should, let's dump the tree.
            StringBuilder stringBuilder = TestHelper.dumpTree(new StringBuilder(), testHomeEdit, 0, true);
            treeString = "\nTree dump:\n" + stringBuilder.toString();
        }

        assertTrue(failureMessage + treeString, nodeWasFoundInLive);
    }

    private void testNodeNotInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath,
                                        String failureMessage) throws RepositoryException {
        boolean nodeWasFoundInLive;
        nodeWasFoundInLive = true;
        try {
            sessionWrapper.getNode(absoluteNodePath);
        } catch (PathNotFoundException pnfe) {
            // this is what we expect, so let's just signal it.
            nodeWasFoundInLive = false;
        }

        String treeString = "";
        if (nodeWasFoundInLive == true) {
            // if it was found when it shouldn't, let's dump the tree.
            StringBuilder stringBuilder = TestHelper.dumpTree(new StringBuilder(), testHomeEdit, 0, true);
            treeString = "\nTree dump:\n" + stringBuilder.toString();
        }

        assertFalse(failureMessage + treeString, nodeWasFoundInLive);
    }

    private void testPropertyInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath, String propertyName,
                                         String propertyValue, String failureMessage) throws RepositoryException {
        boolean nodeWasFoundInLive;
        JCRNodeWrapper liveTextNode1;
        nodeWasFoundInLive = false;
        try {
            liveTextNode1 = sessionWrapper.getNode(absoluteNodePath);
            nodeWasFoundInLive = true;
            String readPropertyValue = liveTextNode1.getPropertyAsString(propertyName);
            if (propertyValue == null) {
                if (readPropertyValue == null) {
                    return; // This means we test for the non existence of the property, which is fine too.
                } else {
                    // there is a property that shouldn't be there !
                    assertNotNull(failureMessage, readPropertyValue);
                }
            } else {
                // we test for the existence of a property, let's test.
                assertEquals(failureMessage, propertyValue, readPropertyValue);
            }
        } catch (PathNotFoundException pnfe) {
            // this is what we expect, so let's just signal it.
            nodeWasFoundInLive = false;
        }
        assertTrue(failureMessage, nodeWasFoundInLive);
    }

    private void testChildOrdering(JCRNodeWrapper contentList1Node, String workspace, String... child) throws RepositoryException {
        String expectedValueString = StringUtils.join(child, " ");
        NodeIterator childNodeIterator = contentList1Node.getNodes();
        List<String> foundValues = new ArrayList<String>();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            if (!Arrays.asList(child).contains(currentChildNode.getName())) {
                continue;
            }
            foundValues.add(currentChildNode.getName());
        }
        String foundValueString = StringUtils.join(foundValues.toArray(), " ");

        assertEquals("Order is invalid, " + (workspace.equals(
                Constants.EDIT_WORKSPACE) ? "done in staging mode" : "propagated to live mode"), expectedValueString, foundValueString);
    }

    private void getCleanSession() throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        englishEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        englishLiveSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
    }


}