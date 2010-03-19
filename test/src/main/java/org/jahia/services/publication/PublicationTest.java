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
package org.jahia.services.publication;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.InputStream;
import java.util.*;

/**
 * Unit test for publish / unpublish using JCR
 * - tests publish / unpublish of pages, container lists, containers
 * - with different language settings (all, none, one, two languages)
 * - with using user not having rights
 * - publication with automatically publishing parent
 *
 * @author Benjamin Papez
 *
 *
 * TestA - standard nodes :
 *
 * 1/ create new node "nodeA" - publish it - check in live if nodeA is here
 * 2/ modify nodeA - publish it - check in live if nodeA is modified
 * 3/ unpublish nodeA - check in live if nodeA has disappeared
 * 4/ publish nodeA - check in live if nodeA is here
 * 5/ rename nodeA, in the same location - publish it - check that nodeA was properly renamed.
 * 6/ move nodeA in same list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 * 7/ move nodeA in another list, before a node - publish it - check if nodeA is correctly moved (and removed from original place)
 * 8/ move and modify nodeA in another list, before a node - publish it - check if nodeA is correctly modified and moved (and removed from original place)
 * 9/ move nodeA in another list, before a node twice - publish it - check if nodeA is correctly moved (and removed from original place)
 * 10/ delete nodeA - publish parent - check in live the node is deleted
 *
 * TestB - shareable nodes - same scenarios
 * TestC - pages node with sub pages - same scenarios. sub pages should not be published.
 * TestD - modify content in live AND edit and merge with edit workspace.
 * TestE - test with shareable nodes published in different locations in different languages.
 * TestF - concurrent modifications (especially moves) in both workspaces.
 *
 */
public class PublicationTest extends TestCase {
    private static Logger logger = Logger.getLogger(PublicationTest.class);
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "jcrPublicationTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private static final String INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE = "English shared text";

    protected void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            ctx = Jahia.getThreadParamBean();
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    public void testPublishUnpublishHomePageWithAccessCheck() throws Exception {
        try {
            JCRPublicationService jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();
            JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
            JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);

            Set<String> languages = null;
            JCRNodeWrapper stageRootNode = session
                    .getNode(SITECONTENT_ROOT_NODE);
            JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode
                    .getNode("home");
            long s = stageNode.hasProperty(Constants.LASTPUBLISHED) ? stageNode
                    .getProperty(Constants.LASTPUBLISHED).getValue().getLong()
                    : 0;

            final JahiaUserManagerService userMgr = ServicesRegistry
                    .getInstance().getJahiaUserManagerService();

// todo : cannot use a different user than current user, use the current user. plan a switch user in test framework
//            JahiaUser guestUser = userMgr
//                    .lookupUser(JahiaUserManagerService.GUEST_USERNAME);
//            boolean accessWasDenied = false;
//            try {
//                jcrService.publish(stageNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                        false, false);
//            } catch (AccessDeniedException e) {
//                accessWasDenied = true;
//            }
//            assertTrue(
//                    "Guest user was able to publish a node although he has no access "
//                            + stageNode.getPath(), accessWasDenied);

            jcrService.publish(stageNode.getPath(),Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

            JCRNodeWrapper publishedNode = liveSession.getNode(stageNode
                    .getPath());
            long p = publishedNode.getProperty(Constants.LASTPUBLISHED)
                    .getValue().getLong();

            assertTrue(
                    "Publication date after publishing a page is not updated for "
                            + stageNode.getPath(), p > s);
            assertTrue(
                    "Publisher name was not updated correctly in live workspace for "
                            + stageNode.getPath(), session.getUser().getName()
                            .equals(
                                    publishedNode.getProperty(
                                            Constants.LASTPUBLISHEDBY)
                                            .getValue().getString()));
            assertTrue(
                    "Publisher name was not updated correctly in default workspace for "
                            + stageNode.getPath(), session.getUser().getName()
                            .equals(
                                    publishedNode.getProperty(
                                            Constants.LASTPUBLISHEDBY)
                                            .getValue().getString()));
            assertTrue(
                    "Publication date was not updated correctly in default workspace for "
                            + stageNode.getPath(), p == stageNode.getProperty(
                            Constants.LASTPUBLISHED).getValue().getLong());

// todo : cannot use a different user than current user, use the current user. plan a switch user in test framework
//            accessWasDenied = false;
//            try {
//                jcrService.unpublish(stageNode.getPath(), languages);
//            } catch (AccessDeniedException e) {
//                accessWasDenied = true;
//            }
//            assertTrue(
//                    "Guest user was able to unpublish a node although he has no access "
//                            + stageNode.getPath(), accessWasDenied);
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    public void testPublishUnpublishPageWithContent() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
        try {
            InputStream importStream = getClass().getClassLoader()
                    .getResourceAsStream("imports/importJCR.xml");
            session.importXML(SITECONTENT_ROOT_NODE, importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
            importStream.close();
            session.save();

            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE + "/content-def"), null,
                    false);
            Set<String> languages = new HashSet<String>();
            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/all-selectors"), languages, false);
            languages.add("en");
            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/container-types"), languages, false);
            languages.add("de");
            testPublishNodeWithContentInLanguages(
                    (JCRNodeWrapper) session.getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/all-fields"), languages, false);
            languages.add("fr");
            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test"), languages, false);

            importStream = getClass().getClassLoader().getResourceAsStream(
                    "imports/importJCRContainerList.xml");
            session.importXML(SITECONTENT_ROOT_NODE
                    + "/content-def/workflow-test", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
            importStream.close();
            session.save();

            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"),
                    null, false);

            List<JCRNodeWrapper> childNodes = ((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"))
                    .getChildren();

            importStream = getClass().getClassLoader().getResourceAsStream(
                    "imports/importJCRContainer.xml");
            session.importXML(SITECONTENT_ROOT_NODE
                    + "/content-def/workflow-test/allFieldsWithList", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            importStream.close();
            session.save();
            List<JCRNodeWrapper> newChildNodes = ((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"))
                    .getChildren();
            newChildNodes.removeAll(childNodes);
            testPublishNodeWithContentInLanguages(newChildNodes.get(0), null,
                    false);

            testUnpublishNodeWithContentInLanguages(newChildNodes.get(0), null);
            testUnpublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"),
                    null);

            languages = new HashSet<String>();
            testUnpublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/all-selectors"), languages);
            languages.add("en");
            testUnpublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/container-types"), languages);
            languages.add("de");
            testUnpublishNodeWithContentInLanguages(
                    (JCRNodeWrapper) session.getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/all-fields"), languages);
            languages.add("fr");
            testUnpublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test"), languages);

            testUnpublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE + "/content-def"), null);

            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test"), languages, true);
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    private void testPublishNodeWithContentInLanguages(
            JCRNodeWrapper pageNodeToPublish, Set<String> languages,
            boolean publishParent) throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);
        Map<String, Long> publishedDateForObjects = new HashMap<String, Long>();
        addNodeAndDependands(pageNodeToPublish, languages,
                publishedDateForObjects);

        jcrService.publish(pageNodeToPublish.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        for (Map.Entry<String, Long> publishedDateForObject : publishedDateForObjects
                .entrySet()) {
            JCRNodeWrapper publishedNode = null;
            String nodePath = publishedDateForObject.getKey();
            try {
                if (nodePath.endsWith("]")) {
                    JCRNodeWrapper parentNode = liveSession.getNode(nodePath
                            .substring(0, nodePath.lastIndexOf('/')));
                    int index = Integer.parseInt(nodePath.substring(nodePath
                            .lastIndexOf('[') + 1, nodePath.lastIndexOf(']')));
                    int counter = 1;
                    for (NodeIterator it = parentNode.getNodes(nodePath
                            .substring(nodePath.lastIndexOf('/') + 1, nodePath
                                    .lastIndexOf('['))); it.hasNext(); counter++) {
                        if (counter == index) {
                            publishedNode = (JCRNodeWrapper) it.next();
                            break;
                        } else {
                            it.next();
                        }
                    }
                } else {
                    publishedNode = liveSession.getNode(nodePath);
                }
            } catch (PathNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            assertTrue(
                    "Node has not been published as it is not found in live workspace: "
                            + nodePath, publishedNode != null);
            if (publishedNode != null) {
                if (publishedNode.isNodeType(Constants.JAHIAMIX_LASTPUBLISHED)) {
                    long p = publishedNode.getProperty(Constants.LASTPUBLISHED)
                            .getValue().getLong();
                    assertTrue(
                            "Publication date after publishing a page is not updated for "
                                    + nodePath, p > publishedDateForObject
                                    .getValue());
                    assertTrue(
                            "Publication date was not updated correctly in default workspace for "
                                    + nodePath, p == session.getNode(
                                    publishedNode.getPath()).getProperty(
                                    Constants.LASTPUBLISHED).getValue()
                                    .getLong());
                }
                if (languages != null
                        && publishedNode.isNodeType(Constants.MIX_LANGUAGE)) {
                    assertTrue("Wrong language published for "
                            + nodePath
                            + " language: "
                            + publishedNode.getProperty(Constants.JCR_LANGUAGE)
                                    .getValue().getString(), languages
                            .contains(publishedNode.getProperty(
                                    Constants.JCR_LANGUAGE).getValue()
                                    .getString()));
                }
            }
        }
    }

    private void testUnpublishNodeWithContentInLanguages(
            JCRNodeWrapper pageNodeToPublish, Set<String> languages)
            throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
        Map<String, Long> publishedDateForObjects = new HashMap<String, Long>();
        addNodeAndDependands(pageNodeToPublish, languages,
                publishedDateForObjects);

        jcrService.unpublish(pageNodeToPublish.getPath(), languages);

        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);

        for (Map.Entry<String, Long> publishedDateForObject : publishedDateForObjects
                .entrySet()) {
            JCRNodeWrapper publishedNode = null;
            String nodePath = publishedDateForObject.getKey();
            try {
                if (nodePath.endsWith("]")) {
                    JCRNodeWrapper parentNode = liveSession.getNode(nodePath
                            .substring(0, nodePath.lastIndexOf('/')));
                    int index = Integer.parseInt(nodePath.substring(nodePath
                            .lastIndexOf('[') + 1, nodePath.lastIndexOf(']')));
                    int counter = 1;
                    for (NodeIterator it = parentNode.getNodes(nodePath
                            .substring(nodePath.lastIndexOf('/') + 1, nodePath
                                    .lastIndexOf('['))); it.hasNext(); counter++) {
                        if (counter == index) {
                            publishedNode = (JCRNodeWrapper) it.next();
                            break;
                        } else {
                            it.next();
                        }
                    }
                } else {
                    publishedNode = liveSession.getNode(nodePath);
                }
            } catch (PathNotFoundException e) {
            }
            assertTrue(
                    "Node has not been unpublished as it is found in live workspace: "
                            + nodePath, publishedNode == null);
        }
    }

    private void addNodeAndDependands(JCRNodeWrapper node,
            Set<String> languages, Map<String, Long> publishedDateForObjects)
            throws RepositoryException {
        if (languages != null
                && node.isNodeType(Constants.MIX_LANGUAGE)
                && !languages.contains(node.getProperty(Constants.JCR_LANGUAGE)
                        .getValue().getString())) {
            return;
        }
        publishedDateForObjects.put(node.getPath(), node
                .hasProperty(Constants.LASTPUBLISHED) ? node.getProperty(
                Constants.LASTPUBLISHED).getValue().getLong() : 0);
        for (JCRNodeWrapper childNode : node.getChildren()) {
            if (!childNode.isNodeType(Constants.JAHIANT_PAGE)) {
                addNodeAndDependands(childNode, languages,
                        publishedDateForObjects);
            }
        }
    }

    public void testASimpleNodePublish() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
        Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        englishEditSession.save();

        Set<String> languages = null;

        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        // Case 1 : let's check the existence of the node property value in the live workspace.

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + "1", "Property value for text node 1 was not found or invalid in english live workspace");
        JCRNodeWrapper liveNode = englishLiveSiteRootNode.getNode("home");
        JCRNodeWrapper liveContentList1Node = liveNode.getNode("contentList1");

        testChildOrdering(liveContentList1Node, "contentList1_text0", "contentList1_text1", "contentList1_text2" );

        // Case 2 : now let's modify the node, republish and check.

        //englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");

        englishEditSession.checkout(editTextNode1);

        editTextNode1.setProperty("text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1");
        englishEditSession.save();

        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "text", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1", "Property value for text node 1 was not found or invalid in english live workspace");

        // Case 3 : not let's unpublish the node and test it's presence in the live workspace.

        jcrService.unpublish(editTextNode1.getPath(), Collections.singleton(englishLiveSession.getLocale().toString()));

        // englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was unpublished, should not be available in the live workspace anymore !");

        // Case 4 : now let's publish the parent node once again, and check if it is published properly.
        jcrService.publish(editTextNode1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was re-published, it should have been present in the live workspace");

        // Case 5 : rename node, publish it and check that it was properly renamed.
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");
        editTextNode1.rename("contentList1_text1_renamed");
        englishEditSession.save();
        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        JCRNodeWrapper liveContentList1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        testChildOrdering(liveContentList1, "contentList1_text0", "contentList1_text1_renamed", "contentList1_text2");
        // now let's move it back to continue the tests.
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed");
        editTextNode1.rename("contentList1_text1");
        englishEditSession.save();
        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        liveContentList1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        testChildOrdering(liveContentList1, "contentList1_text0", "contentList1_text1", "contentList1_text2");

        // Case 6 : now we must move the text node inside the list, and check that the move is properly propagated in live mode

        englishEditSession.checkout(editContentList1);
        editContentList1.orderBefore("contentList1_text1", "contentList1_text0");
        englishEditSession.save();
        testChildOrdering(editContentList1, "contentList1_text1", "contentList1_text0", "contentList1_text2" );

        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");

        testChildOrdering(liveContentList1Node, "contentList1_text1", "contentList1_text0", "contentList1_text2" );

        // Case 7 : now let's move the node to another container list.
        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        englishEditSession.checkout(editContentList1);
        JCRNodeWrapper editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");
        englishEditSession.move(editTextNode1.getPath(), SITECONTENT_ROOT_NODE + "/home/contentList2/contentList1_text1");
        editContentList2.orderBefore("contentList1_text1", "contentList2_text0");
        englishEditSession.save();

        // jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publish(editContentList2.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        testChildOrdering(liveContentList1Node, "contentList1_text0", "contentList1_text2", "contentList1_text3");
        testChildOrdering(liveContentList2Node, "contentList1_text1", "contentList2_text0", "contentList2_text1");

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        JCRNodeWrapper editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        englishEditSession.checkout(editContentList3);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList2/contentList1_text1", SITECONTENT_ROOT_NODE + "/home/contentList3/contentList1_text1");
        englishEditSession.save();
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3/contentList1_text1");
        englishEditSession.checkout(editTextNode1);
        editTextNode1.setProperty("text", "English text update 2");
        englishEditSession.save();
        editContentList3.orderBefore("contentList1_text1", "contentList3_text0");
        englishEditSession.save();

        jcrService.publish(editContentList3.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        JCRNodeWrapper liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        testChildOrdering(liveContentList2Node, "contentList2_text0", "contentList2_text1", "contentList2_text2");
        testChildOrdering(liveContentList3Node, "contentList1_text1", "contentList3_text0", "contentList3_text1");

        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.
        editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        englishEditSession.checkout(editContentList3);
        JCRNodeWrapper editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        englishEditSession.checkout(editContentList4);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList3/contentList1_text1", SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1");
        editContentList4.orderBefore("contentList1_text1", "contentList4_text1");
        editContentList4.orderBefore("contentList1_text1", "contentList4_text0");
        englishEditSession.save();
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1");

        jcrService.publish(editContentList4.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        JCRNodeWrapper liveContentList4Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        testChildOrdering(liveContentList3Node, "contentList3_text0", "contentList3_text1", "contentList3_text2");
        testChildOrdering(liveContentList4Node, "contentList1_text1", "contentList4_text0", "contentList4_text1");

        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
        editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        englishEditSession.checkout(editContentList4);
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1");
        englishEditSession.checkout(editTextNode1);
        editTextNode1.remove();
        englishEditSession.save();

        jcrService.publish(editContentList4.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1", "Text node 1 was deleted, should not be available in the live workspace anymore !");


    }

    public void testBSharedNodePublish() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
        Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        createList(englishEditSiteHomeNode, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        englishEditSession.save();

        Set<String> languages = null;

        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        englishEditSession.logout();
        englishLiveSession.logout();
        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));

        // now let's create a shared node and share it in two lists.
        JCRNodeWrapper editContentList0 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList0");
        englishEditSession.checkout(editContentList0);
        JCRNodeWrapper editSharedNode0 = editContentList0.addNode("shared_node_list0", "jnt:text");
        editSharedNode0.setProperty("text", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        englishEditSession.checkout(editContentList1);
        JCRNodeWrapper editSharedNode1 = editContentList1.clone(editSharedNode0, "shared_node_list1");
        englishEditSession.save();
        jcrService.publish(editSharedNode0.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        // Case 1 : let's check the existence of the node property value in the live workspace.

        JCRNodeWrapper liveContentList0Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList0");

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "text", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE, "Shared text node 0 property invalid or missing in live workspace !");

        testChildOrdering(liveContentList0Node, "contentList0_text0", "contentList0_text1", "contentList0_text2" );
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared text node 1 was not published should not be available in the live workspace !");

        // now let's publish the second location of the node, and check that it was made available in live...
        jcrService.publish(editSharedNode1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        JCRNodeWrapper liveSharedTextNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
        assertNotNull("Shared text node 1 was published but it is not available in the live workspace.", liveSharedTextNode);

        // Case 2 : now let's modify the node, republish and check.

        //englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
        englishEditSession.checkout(editSharedNode1);
        editSharedNode1.setProperty("text", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1");
        englishEditSession.save();

        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "text", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1", "Shared Text node 0 value is not correct !");

        // now let's check the second location has the same value.
        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "text", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1", "Shared Text node 1 value is not correct !");

        // Case 3 : now let's unpublish the node and test it's presence in the live workspace.

        jcrService.unpublish(editSharedNode1.getPath(), Collections.singleton(englishLiveSession.getLocale().toString()));

        // englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared Text node 1 was unpublished, should not be available in the live workspace anymore !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was unpublished, should not be available in the live workspace anymore !");

        // Case 4 : now let's publish the parent node once again, and check if it is published properly.
        jcrService.publish(editSharedNode1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared Text node 1 was re-published, it should have been present in the live workspace");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was re-published, it should have been present in the live workspace");

        // Case 5 : rename node, publish it and check that it was properly renamed.

        // for shared nodes, we cannot rename using a move operation, so we must remove the shared and re-create it under another name.
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
        englishEditSession.checkout(editSharedNode1);
        editSharedNode1.rename("shared_node_list1_renamed");
        englishEditSession.save();
        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Text node 1 was renamed but not text node 0, should have still been available in the live workspace in the original location !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        // now let's move it back to continue the tests.
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed");
        englishEditSession.checkout(editSharedNode1);
        editSharedNode1.rename("shared_node_list1");
        englishEditSession.save();
        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Text node 1 was renamed but not text node 0, should have still been available in the live workspace in the original location !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");

        // Case 6 : now we must move the text node inside the list, and check that the move is properly propagated in live mode
        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");

        englishEditSession.checkout(editContentList1);
        editContentList1.orderBefore("shared_node_list1", "contentList1_text0");
        englishEditSession.save();
        testChildOrdering(editContentList1, "shared_node_list1", "contentList1_text0", "contentList1_text1" );

        jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);

        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");

        testChildOrdering(liveContentList0Node, "contentList0_text0", "contentList0_text1", "contentList0_text2" );
        testChildOrdering(liveContentList1Node, "shared_node_list1", "contentList1_text0", "contentList1_text1" );

        // Case 7 : now let's move the node to another container list.
        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        englishEditSession.checkout(editContentList1);
        JCRNodeWrapper editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        englishEditSession.checkout(editSharedNode1); // we have to check it out because of a property being changed during move !
        englishEditSession.move(editSharedNode1.getPath(), SITECONTENT_ROOT_NODE + "/home/contentList2/shared_node_list2");
        editContentList2.orderBefore("shared_node_list2", "contentList2_text0");
        englishEditSession.save();

        // jcrService.publish(editContentList1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publish(editContentList2.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        testChildOrdering(liveContentList1Node, "contentList1_text0", "contentList1_text1", "contentList1_text2");
        testChildOrdering(liveContentList2Node, "shared_node_list2", "contentList2_text0", "contentList2_text1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        JCRNodeWrapper editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        englishEditSession.checkout(editContentList3);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList2/shared_node_list2", SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3");
        englishEditSession.save();
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3");
        englishEditSession.checkout(editSharedNode1);
        editSharedNode1.setProperty("text", "English text update 2");
        englishEditSession.save();
        editContentList3.orderBefore("shared_node_list3", "contentList3_text0");
        englishEditSession.save();

        jcrService.publish(editContentList3.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        JCRNodeWrapper liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        testChildOrdering(liveContentList2Node, "contentList2_text0", "contentList2_text1", "contentList2_text2");
        testChildOrdering(liveContentList3Node, "shared_node_list3", "contentList3_text0", "contentList3_text1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");

        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.
        editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        englishEditSession.checkout(editContentList3);
        JCRNodeWrapper editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        englishEditSession.checkout(editContentList4);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3", SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");
        editContentList4.orderBefore("shared_node_list4", "contentList4_text1");
        editContentList4.orderBefore("shared_node_list4", "contentList4_text0");
        englishEditSession.save();
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");

        jcrService.publish(editContentList4.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        JCRNodeWrapper liveContentList4Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        testChildOrdering(liveContentList3Node, "contentList3_text0", "contentList3_text1", "contentList3_text2");
        testChildOrdering(liveContentList4Node, "shared_node_list4", "contentList4_text0", "contentList4_text1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");

        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
        editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        englishEditSession.checkout(editContentList4);
        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");
        englishEditSession.checkout(editSharedNode1);
        editSharedNode1.remove();
        englishEditSession.save();

        jcrService.publish(editContentList4.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4", "Shared Text node 4 was deleted, it should not be present in the live workspace anymore !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was deleted, it should not be present in the live workspace anymore !");

    }

    public void testCPagesWithSubPages() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();
        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
        Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        // now let's setup the pages we will use.

        JCRNodeWrapper englishEditPage1Node = englishEditSiteHomeNode.addNode("page1", "jnt:page");
        createList(englishEditPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditPage2Node = englishEditSiteHomeNode.addNode("page2", "jnt:page");
        createList(englishEditPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditPage3Node = englishEditSiteHomeNode.addNode("page3", "jnt:page");
        createList(englishEditPage3Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditPage3Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        JCRNodeWrapper englishEditSubPage1Node = englishEditPage1Node.addNode("page1_subpage1", "jnt:page");
        createList(englishEditSubPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSubPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditSubPage2Node = englishEditPage1Node.addNode("page1_subpage2", "jnt:page");
        createList(englishEditSubPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        createList(englishEditSubPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        englishEditSession.save();

        Set<String> languages = null;
        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publish(englishEditPage1Node.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publish(englishEditPage2Node.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publish(englishEditSubPage1Node.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        // Case 1 : let's check the existence of the node property value in the live workspace.
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0", "ContentList0 on Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0/contentList0_text0", "Text0 in ContentList0 on Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1", "Sub Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1", "ContentList1 on Sub Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1/contentList1_text1", "Text1 in ContentList1 on Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2", "Sub Page 2 should not have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1", "ContentList1 on Sub Page 2 should not have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1/contentList1_text1", "Text1 in ContentList1 on Sub Page 2 should not have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2", "Page 2 should have been published");

        // Case 2 : now let's modify the node, republish and check.
        englishEditSession.checkout(englishEditPage1Node);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1/contentList0", SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed");
        englishEditSession.save();

        jcrService.publish(englishEditPage1Node.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0", "ContentList0 on Page 1 not be present anymore since we renamed it.");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed", "ContentList0_renamed on Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2", "Sub Page 2 should not have been published");

        // Case 3 : now let's unpublish the node and test it's presence in the live workspace.

        jcrService.unpublish(englishEditPage1Node.getPath(), languages);
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have be unpublished !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1", "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2", "Sub Page 2 should not have been published");

        // Case 4 : now let's republish the node and test it's presence in the live workspace.
        jcrService.publish(englishEditPage1Node.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1", "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2", "Sub Page 2 should not have been published");

        // Case 5 : let's rename the page and check it's been properly renamed in the live workspace.
        englishEditSession.checkout(englishEditSiteHomeNode);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1", SITECONTENT_ROOT_NODE + "/home/page1_renamed");
        englishEditSession.save();
        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed", "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage1", "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage2", "Sub Page 2 should not have been published");
        // now let's move it back to continue the tests.
        englishEditSession.checkout(englishEditSiteHomeNode);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1_renamed", SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.save();
        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1", "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2", "Sub Page 2 should not have been published");

        // Case 6 : now we must move the page inside the list of the parent page, and check that the move is properly propagated in live mode
        englishEditSession.checkout(englishEditSiteHomeNode);
        englishEditSiteHomeNode.orderBefore("page1", null); // this should put it at the end of the list.
        englishEditSession.save();
        testChildOrdering(englishEditSiteHomeNode, "page2", "page3", "page1");
        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        JCRNodeWrapper englishLiveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        testChildOrdering(englishLiveSiteHomeNode, "page2", "page3", "page1");

        // Case 7 : now let's move the page to another page
        JCRNodeWrapper editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.checkout(englishEditSiteHomeNode);
        JCRNodeWrapper editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        englishEditSession.checkout(editPage2);
        englishEditSession.checkout(editPage1); // we have to check it out because of a property being changed during move !
        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page2/page1");
        editPage2.orderBefore("page1", "contentList1");
        englishEditSession.save();
        jcrService.publish(editPage2.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        JCRNodeWrapper liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        JCRNodeWrapper livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        testChildOrdering(liveSiteHomeNode, "page2", "page3");
        testChildOrdering(livePage2, "contentList0", "page1", "contentList1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2/page1", "Page 1 was not properly moved below page 2 in the live workspace.");

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        englishEditSession.checkout(editPage2);
        JCRNodeWrapper editPage3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        englishEditSession.checkout(editPage3);
        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2/page1");
        englishEditSession.checkout(editPage1);
        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page3/page1");
        editPage3.orderBefore("page1", "contentList1");
        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3/page1");
        editPage1.addNode("anotherList", "jnt:contentList");
        englishEditSession.save();
        jcrService.publish(editPage3.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);

        liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        JCRNodeWrapper livePage3 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3");
        testChildOrdering(liveSiteHomeNode, "page2", "page3");
        testChildOrdering(livePage2, "contentList0", "contentList1");
        testChildOrdering(livePage3, "contentList0", "page1", "contentList1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1", "Page 1 was not properly moved below page 3 in the live workspace.");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1/anotherList", "New list on Page 1 was not found in the live workspace.");

        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.

        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.

    }

/* @todo Still to be implemented... 
    public void testDModificationInTwoWorkspaces() {

    }

    public void testESharedNodesInMultipleLanguages() {

    }

    public void testFConcurrentModificationsInTwoWorkspaces() {
        
    }
*/

    public void testAddMixinAndPublish() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = LanguageCodeConverters.languageCodeToLocale("en");
        Locale frenchLocale = LanguageCodeConverters.languageCodeToLocale("fr");

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        Set<String> languages = null;

        JCRNodeWrapper englishEditPage1 = englishEditSiteHomeNode.addNode("page1", "jnt:page");

        englishEditSession.save();

        jcrService.publish(englishEditSiteHomeNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, true);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "Page 1 should have been published to live workspace !");

        //  Add mixin to a node already published once, publish it, and verify that the new mixin was properly added.
        englishEditSession.checkout(englishEditPage1);
        englishEditPage1.addMixin("jmix:sitemap");
        englishEditSession.save();
        assertTrue("Page 1 should now have the sitemap mixin type in edit workspace", englishEditPage1.isNodeType("jmix:sitemap"));
        testPropertyInWorkspace(englishEditSession, SITECONTENT_ROOT_NODE + "/home/page1", "changefreq", "monthly", "Propery changefreq should have default value of 'monthly'");
        jcrService.publish(englishEditPage1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        JCRNodeWrapper englishLivePage1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        assertTrue("Page 1 should now have the sitemap mixin type in live workspace", englishLivePage1.isNodeType("jmix:sitemap"));
        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "changefreq", "monthly", "Propery changefreq should have default value of 'monthly'");

        englishEditPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.checkout(englishEditPage1);
        englishEditPage1.removeMixin("jmix:sitemap");
        englishEditSession.save();
        assertFalse("Page 1 should now no longer have the sitemap mixin in the edit workspace!", englishEditPage1.isNodeType("jmix:sitemap"));
        jcrService.publish(englishEditPage1.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        englishLivePage1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        assertFalse("Page 1 should now no longer have the sitemap mixin in the live workspace!", englishLivePage1.isNodeType("jmix:sitemap"));
    }

    private void testNodeInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath, String failureMessage) throws RepositoryException {
        boolean nodeWasFoundInLive;
        JCRNodeWrapper liveTextNode1;
        nodeWasFoundInLive = false;
        try {
            liveTextNode1 = sessionWrapper.getNode(absoluteNodePath);
            nodeWasFoundInLive = true;
        } catch (PathNotFoundException pnfe) {
            // this is what we expect, so let's just signal it.
            nodeWasFoundInLive = false;
        }

        String treeString = "";
        if (nodeWasFoundInLive == false) {
            // if it was not found when it should, let's dump the tree.
            StringBuilder stringBuilder = dumpTree(new StringBuilder(), sessionWrapper.getNode(SITECONTENT_ROOT_NODE + "/home"), 0, true);
            treeString = "\nTree dump:\n" + stringBuilder.toString();
        }

        assertTrue(failureMessage + treeString, nodeWasFoundInLive);
    }

    private void testNodeNotInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath, String failureMessage) throws RepositoryException {
        boolean nodeWasFoundInLive;
        nodeWasFoundInLive = true;
        try {
            JCRNodeWrapper liveTextNode1 = sessionWrapper.getNode(absoluteNodePath);
        } catch (PathNotFoundException pnfe) {
            // this is what we expect, so let's just signal it.
            nodeWasFoundInLive = false;
        }

        String treeString = "";
        if (nodeWasFoundInLive == true) {
            // if it was found when it shouldn't, let's dump the tree.
            StringBuilder stringBuilder = dumpTree(new StringBuilder(), sessionWrapper.getNode(SITECONTENT_ROOT_NODE + "/home"), 0, true);
            treeString = "\nTree dump:\n" + stringBuilder.toString();
        }

        assertFalse(failureMessage + treeString, nodeWasFoundInLive);
    }

    private void testPropertyInWorkspace(JCRSessionWrapper sessionWrapper, String absoluteNodePath, String propertyName, String propertyValue, String failureMessage) throws RepositoryException {
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

    private void testChildOrdering(JCRNodeWrapper liveContentList1Node, String child0NodeName, String child1NodeName, String child2NodeName) throws RepositoryException {
        int index = 0;
        boolean orderIsValid = true;
        String expectedValue = null;
        String foundValue = null;
        NodeIterator childNodeIterator = liveContentList1Node.getNodes();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            if (index == 0) {
                if (!child0NodeName.equals(currentChildNode.getName())) {
                    orderIsValid = false;
                    expectedValue = child0NodeName;
                    foundValue = currentChildNode.getName();
                    break;
                }
            } else if (index == 1) {
                if (!child1NodeName.equals(currentChildNode.getName())) {
                    orderIsValid = false;
                    expectedValue = child1NodeName;
                    foundValue = currentChildNode.getName();
                    break;
                }
            } else if (index == 2) {
                if (!child2NodeName.equals(currentChildNode.getName())) {
                    orderIsValid = false;
                    expectedValue = child2NodeName;
                    foundValue = currentChildNode.getName();
                    break;
                }
            } else {
                break;
            }
            index++;
        }
        assertTrue("Move inside the same list has not been properly propagated to live mode ! Expected value=" + expectedValue + " but found value=" + foundValue, orderIsValid);
    }

    private void testChildOrdering(JCRNodeWrapper liveContentList1Node, String child0NodeName, String child1NodeName) throws RepositoryException {
        int index = 0;
        boolean orderIsValid = true;
        String expectedValue = null;
        String foundValue = null;
        NodeIterator childNodeIterator = liveContentList1Node.getNodes();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            if (index == 0) {
                if (!child0NodeName.equals(currentChildNode.getName())) {
                    orderIsValid = false;
                    expectedValue = child0NodeName;
                    foundValue = currentChildNode.getName();
                    break;
                }
            } else if (index == 1) {
                if (!child1NodeName.equals(currentChildNode.getName())) {
                    orderIsValid = false;
                    expectedValue = child1NodeName;
                    foundValue = currentChildNode.getName();
                    break;
                }
            } else {
                break;
            }
            index++;
        }
        assertTrue("Move inside the same list has not been properly propagated to live mode ! Expected value=" + expectedValue + " but found value=" + foundValue, orderIsValid);
    }

    private void createList(JCRNodeWrapper parentNode, String listName, int elementCount, String textPrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        JCRNodeWrapper contentList1 = parentNode.addNode(listName, "jnt:contentList");

        for (int i=0; i < elementCount; i++) {
            JCRNodeWrapper textNode1 = contentList1.addNode(listName + "_text" + Integer.toString(i), "jnt:text");
            textNode1.setProperty("text", textPrefix + Integer.toString(i));
        }        
    }

    private StringBuilder dumpTree(StringBuilder stringBuilder, Node startNode, int depth, boolean logAsError) throws RepositoryException {
        for (int i=0; i < depth; i++) {
            if (i == 0) {
                stringBuilder.append("+-");
            } else {
                stringBuilder.append("--");
            }
        }
        stringBuilder.append(startNode.getName());
        stringBuilder.append(" = ");
        stringBuilder.append(startNode.getIdentifier());
        stringBuilder.append("\n");
        NodeIterator childNodeIter = startNode.getNodes();
        while (childNodeIter.hasNext()) {
            Node currentChild = childNodeIter.nextNode();
            stringBuilder = dumpTree(stringBuilder, currentChild, depth + 1, logAsError);
        }
        return stringBuilder;
    }
        
    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}