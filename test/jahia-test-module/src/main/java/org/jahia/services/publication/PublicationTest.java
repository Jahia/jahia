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

package org.jahia.services.publication;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.*;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
 *         <p/>
 *         TestB - shareable nodes - same scenarios
 *         TestC - pages node with sub pages - same scenarios. sub pages should not be published.
 *         TestD - modify content in live AND edit and merge with edit workspace.
 *         TestE - test with shareable nodes published in different locations in different languages.
 *         TestF - concurrent modifications (especially moves) in both workspaces.
 */
public class PublicationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrPublicationTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
//    private static final String INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE = "English shared text";

    @Before
    public void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

//    public void testPublishUnpublishHomePageWithAccessCheck() throws Exception {
//        try {
//            JCRPublicationService jcrService = ServicesRegistry.getInstance()
//                    .getJCRPublicationService();
//            JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
//            JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);
//
//            Set<String> languages = null;
//            JCRNodeWrapper stageRootNode = session
//                    .getNode(SITECONTENT_ROOT_NODE);
//            JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode
//                    .getNode("home");
//            long s = stageNode.hasProperty(Constants.LASTPUBLISHED) ? stageNode
//                    .getProperty(Constants.LASTPUBLISHED).getValue().getLong()
//                    : 0;
//
//            final JahiaUserManagerService userMgr = ServicesRegistry
//                    .getInstance().getJahiaUserManagerService();
//
//// todo : cannot use a different user than current user, use the current user. plan a switch user in test framework
////            JahiaUser guestUser = userMgr
////                    .lookupUser(JahiaUserManagerService.GUEST_USERNAME);
////            boolean accessWasDenied = false;
////            try {
////                jcrService.publish(stageNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
////                        false, false);
////            } catch (AccessDeniedException e) {
////                accessWasDenied = true;
////            }
////            assertTrue(
////                    "Guest user was able to publish a node although he has no access "
////                            + stageNode.getPath(), accessWasDenied);
//
//            jcrService.publish(stageNode.getIdentifier(),Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
//
//            JCRNodeWrapper publishedNode = liveSession.getNode(stageNode
//                    .getPath());
//            long p = publishedNode.getProperty(Constants.LASTPUBLISHED)
//                    .getValue().getLong();
//
//            assertTrue(
//                    "Publication date after publishing a page is not updated for "
//                            + stageNode.getPath(), p > s);
//            assertTrue(
//                    "Publisher name was not updated correctly in live workspace for "
//                            + stageNode.getPath(), session.getUser().getName()
//                            .equals(
//                                    publishedNode.getProperty(
//                                            Constants.LASTPUBLISHEDBY)
//                                            .getValue().getString()));
//            assertTrue(
//                    "Publisher name was not updated correctly in default workspace for "
//                            + stageNode.getPath(), session.getUser().getName()
//                            .equals(
//                                    publishedNode.getProperty(
//                                            Constants.LASTPUBLISHEDBY)
//                                            .getValue().getString()));
//            assertTrue(
//                    "Publication date was not updated correctly in default workspace for "
//                            + stageNode.getPath(), p == stageNode.getProperty(
//                            Constants.LASTPUBLISHED).getValue().getLong());
//
//// todo : cannot use a different user than current user, use the current user. plan a switch user in test framework
////            accessWasDenied = false;
////            try {
////                jcrService.unpublish(stageNode.getIdentifier(), languages);
////            } catch (AccessDeniedException e) {
////                accessWasDenied = true;
////            }
////            assertTrue(
////                    "Guest user was able to unpublish a node although he has no access "
////                            + stageNode.getPath(), accessWasDenied);
//        } catch (Exception ex) {
//            logger.warn("Exception during test", ex);
//        }
//    }
//
    @Test
    public void testPublishUnpublishPageWithContent() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();
        try {
            InputStream importStream = getClass().getClassLoader()
                    .getResourceAsStream("jahia-test-module-war/src/main/resources/imports/importJCR.xml");
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
                    "jahia-test-module-war/src/main/resources/imports/importJCRContainerList.xml");
            session.importXML(SITECONTENT_ROOT_NODE
                    + "/content-def/workflow-test", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
            importStream.close();
            session.save();

            testPublishNodeWithContentInLanguages((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"),
                    null, false);

            NodeIterator childNodeIt = ((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"))
                    .getNodes();
            List<JCRNodeWrapper> childNodes = new ArrayList<JCRNodeWrapper>();
            while (childNodeIt.hasNext()) {
                childNodes.add((JCRNodeWrapper) childNodeIt.next());
            }
            
            importStream = getClass().getClassLoader().getResourceAsStream(
                    "jahia-test-module-war/src/main/resources/imports/importJCRContainer.xml");
            session.importXML(SITECONTENT_ROOT_NODE
                    + "/content-def/workflow-test/allFieldsWithList", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            importStream.close();
            session.save();
            NodeIterator newChildNodeIt = ((JCRNodeWrapper) session
                    .getNode(SITECONTENT_ROOT_NODE
                            + "/content-def/workflow-test/allFieldsWithList"))
                    .getNodes();
            List<JCRNodeWrapper> newChildNodes = new ArrayList<JCRNodeWrapper>();
            while (newChildNodeIt.hasNext()) {
                newChildNodes.add((JCRNodeWrapper) newChildNodeIt.next());
            } 
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

        jcrService.publishByMainId(pageNodeToPublish.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);

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
        Map<String, Long> publishedDateForObjects = new HashMap<String, Long>();
        addNodeAndDependands(pageNodeToPublish, languages,
                publishedDateForObjects);

        jcrService.unpublish(Lists.newArrayList(pageNodeToPublish.getIdentifier()), languages);

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
        for (NodeIterator it = node.getNodes(); it.hasNext();) {
            JCRNodeWrapper childNode = (JCRNodeWrapper)it.next();
            if (!childNode.isNodeType(Constants.JAHIANT_PAGE)) {
                addNodeAndDependands(childNode, languages,
                        publishedDateForObjects);
            }
        }
    }
    
    @Test
    public void testASimpleNodePublish() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishLiveSiteRootNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        TestHelper.createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        englishEditSession.save();

        Set<String> languages = null;

        jcrService.publishByMainId(englishEditSiteHomeNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);

        // Case 1 : let's check the existence of the node property value in the live workspace.

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + "1", "Property value for text node 1 was not found or invalid in english live workspace");
        JCRNodeWrapper liveNode = englishLiveSiteRootNode.getNode("home");
        JCRNodeWrapper liveContentList1Node = liveNode.getNode("contentList1");

        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2" );

        // Case 2 : now let's modify the node, republish and check.

        //englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");

        englishEditSession.checkout(editTextNode1);

        editTextNode1.setProperty("body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1");
        englishEditSession.save();

        jcrService.publishByMainId(englishEditSiteHomeNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);

        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "body", INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE + " update 1", "Property value for text node 1 was not found or invalid in english live workspace");

        // Case 3 : not let's unpublish the node and test it's presence in the live workspace.

        jcrService.unpublish(Lists.newArrayList(editTextNode1.getIdentifier()), Collections.singleton(englishLiveSession.getLocale().toString()));

        // Need to add this, as otherwise the unpublished node will still be served from cache
        JCRSessionFactory.getInstance().closeAllSessions();
        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        
        // englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was unpublished, should not be available in the live workspace anymore !");

        // Case 4 : now let's publish the parent node once again, and check if it is published properly.
        jcrService.publishByMainId(editTextNode1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was re-published, it should have been present in the live workspace");

        // Need to add this, as otherwise the node will be in live session cache with the old name even after renaming and publishing it
        JCRSessionFactory.getInstance().closeAllSessions();
        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));        
        
        // Case 5 : rename node, publish it and check that it was properly renamed.
        JCRNodeWrapper editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");
        editTextNode1.rename("contentList1_text1_renamed");
        englishEditSession.save();
        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        JCRNodeWrapper liveContentList1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        testChildOrdering(liveContentList1,Constants. LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1_renamed", "contentList1_text2");
        // now let's move it back to continue the tests.
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed");
        editTextNode1.rename("contentList1_text1");
        englishEditSession.save();
        
        // Need to add this, as otherwise the node renaming back to the previous name will still be in live session cache with the intermediate name 
        JCRSessionFactory.getInstance().closeAllSessions();
        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));        
        
        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1_renamed", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
        liveContentList1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        testChildOrdering(liveContentList1, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");

        // Case 6 : now we must move the text node inside the list, and check that the move is properly propagated in live mode

        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        englishEditSession.checkout(editContentList1);
        editContentList1.orderBefore("contentList1_text1", "contentList1_text0");
        englishEditSession.save();
        testChildOrdering(editContentList1, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList1_text0", "contentList1_text2" );

        jcrService.publishByMainId(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, null);

        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");

        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList1_text0", "contentList1_text2" );

        // Case 7 : now let's move the node to another container list.
        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        englishEditSession.checkout(editContentList1);
        JCRNodeWrapper editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/contentList1_text1");
        englishEditSession.move(editTextNode1.getPath(), SITECONTENT_ROOT_NODE + "/home/contentList2/contentList1_text1");
        editContentList2.orderBefore("contentList1_text1", "contentList2_text0");
        englishEditSession.save();

        // jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
        jcrService.publishByMainId(editContentList2.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);

        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text2", "contentList1_text3");
        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList2_text0", "contentList2_text1");

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        englishEditSession.checkout(editContentList2);
        JCRNodeWrapper editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        englishEditSession.checkout(editContentList3);
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList2/contentList1_text1", SITECONTENT_ROOT_NODE + "/home/contentList3/contentList1_text1");
        englishEditSession.save();
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3/contentList1_text1");
        englishEditSession.checkout(editTextNode1);
        editTextNode1.setProperty("body", "English text update 2");
        englishEditSession.save();
        editContentList3.orderBefore("contentList1_text1", "contentList3_text0");
        englishEditSession.save();

        jcrService.publishByMainId(editContentList3.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);

        liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
        JCRNodeWrapper liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "contentList2_text0", "contentList2_text1", "contentList2_text2");
        testChildOrdering(liveContentList3Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList3_text0", "contentList3_text1");

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

        jcrService.publishByMainId(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);

        liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
        JCRNodeWrapper liveContentList4Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        testChildOrdering(liveContentList3Node, Constants.LIVE_WORKSPACE, "contentList3_text0", "contentList3_text1", "contentList3_text2");
        testChildOrdering(liveContentList4Node, Constants.LIVE_WORKSPACE, "contentList1_text1", "contentList4_text0", "contentList4_text1");

        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
        editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
        englishEditSession.checkout(editContentList4);
        editTextNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1");
        englishEditSession.checkout(editTextNode1);
        editTextNode1.markForDeletion("Deleted by unit test");
        englishEditSession.save();

        jcrService.publishByMainId(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, true, null);

        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList4/contentList1_text1", "Text node 1 was deleted, should not be available in the live workspace anymore !");


    }

    //    public void testBSharedNodePublish() throws RepositoryException {
//        JCRPublicationService jcrService = ServicesRegistry.getInstance()
//                .getJCRPublicationService();
//
//        String defaultLanguage = site.getDefaultLanguage();
//
//        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
//        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");
//
//        TestHelper.createList(englishEditSiteHomeNode, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSiteHomeNode, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSiteHomeNode, "contentList2", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSiteHomeNode, "contentList3", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//        TestHelper.createList(englishEditSiteHomeNode, "contentList4", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
//
//        englishEditSession.save();
//
//        Set<String> languages = null;
//
//        jcrService.publish(englishEditSiteHomeNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                true);
//
//        englishEditSession.logout();
//        englishLiveSession.logout();
//        englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//        englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//
//        // now let's create a shared node and share it in two lists.
//        JCRNodeWrapper editContentList0 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList0");
//        englishEditSession.checkout(editContentList0);
//        JCRNodeWrapper editSharedNode0 = editContentList0.addNode("shared_node_list0", "jnt:mainContent");
//        editSharedNode0.setProperty("body", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE);
//        JCRNodeWrapper editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
//        englishEditSession.checkout(editContentList1);
//        JCRNodeWrapper editSharedNode1 = editContentList1.clone(editSharedNode0, "shared_node_list1");
//        englishEditSession.save();
//        jcrService.publish(editSharedNode0.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        // Case 1 : let's check the existence of the node property value in the live workspace.
//
//        JCRNodeWrapper liveContentList0Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList0");
//
//        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "body", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE, "Shared text node 0 property invalid or missing in live workspace !");
//
//        testChildOrdering(liveContentList0Node, Constants.LIVE_WORKSPACE, "contentList0_text0", "contentList0_text1", "contentList0_text2" );
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared text node 1 was not published should not be available in the live workspace !");
//
//        // now let's publish the second location of the node, and check that it was made available in live...
//        jcrService.publish(editSharedNode1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        JCRNodeWrapper liveSharedTextNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
//        assertNotNull("Shared text node 1 was published but it is not available in the live workspace.", liveSharedTextNode);
//
//        // Case 2 : now let's modify the node, republish and check.
//
//        //englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
//        englishEditSession.checkout(editSharedNode1);
//        editSharedNode1.setProperty("body", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1");
//        englishEditSession.save();
//
//        jcrService.publish(englishEditSiteHomeNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                true);
//
//        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "body", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1", "Shared Text node 0 value is not correct !");
//
//        // now let's check the second location has the same value.
//        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "body", INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE + " update 1", "Shared Text node 1 value is not correct !");
//
//        // Case 3 : now let's unpublish the node and test it's presence in the live workspace.
//
//        jcrService.unpublish(editSharedNode1.getIdentifier(), Collections.singleton(englishLiveSession.getLocale().toString()));
//
//        // englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared Text node 1 was unpublished, should not be available in the live workspace anymore !");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was unpublished, should not be available in the live workspace anymore !");
//
//        // Case 4 : now let's publish the parent node once again, and check if it is published properly.
//        jcrService.publish(editSharedNode1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Shared Text node 1 was re-published, it should have been present in the live workspace");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was re-published, it should have been present in the live workspace");
//
//        // Case 5 : rename node, publish it and check that it was properly renamed.
//
//        // for shared nodes, we cannot rename using a move operation, so we must remove the shared and re-create it under another name.
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1");
//        englishEditSession.checkout(editSharedNode1);
//        editSharedNode1.rename("shared_node_list1_renamed");
//        englishEditSession.save();
//        jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Text node 1 was renamed but not text node 0, should have still been available in the live workspace in the original location !");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
//        // now let's move it back to continue the tests.
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed");
//        englishEditSession.checkout(editSharedNode1);
//        editSharedNode1.rename("shared_node_list1");
//        englishEditSession.save();
//        jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1", "Text node 1 was renamed, should have been available under the new name in the live workspace !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Text node 1 was renamed but not text node 0, should have still been available in the live workspace in the original location !");
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList1/shared_node_list1_renamed", "Text node 1 was renamed, should not have been available under the old name in the live workspace !");
//
//        // Case 6 : now we must move the text node inside the list, and check that the move is properly propagated in live mode
//        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
//
//        englishEditSession.checkout(editContentList1);
//        editContentList1.orderBefore("shared_node_list1", "contentList1_text0");
//        englishEditSession.save();
//        testChildOrdering(editContentList1, Constants.LIVE_WORKSPACE, "shared_node_list1", "contentList1_text0", "contentList1_text1" );
//
//        jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                true);
//
//        JCRNodeWrapper liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
//
//        testChildOrdering(liveContentList0Node, Constants.LIVE_WORKSPACE, "contentList0_text0", "contentList0_text1", "contentList0_text2" );
//        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "shared_node_list1", "contentList1_text0", "contentList1_text1" );
//
//        // Case 7 : now let's move the node to another container list.
//        editContentList1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
//        englishEditSession.checkout(editContentList1);
//        JCRNodeWrapper editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
//        englishEditSession.checkout(editContentList2);
//        englishEditSession.checkout(editSharedNode1); // we have to check it out because of a property being changed during move !
//        englishEditSession.move(editSharedNode1.getPath(), SITECONTENT_ROOT_NODE + "/home/contentList2/shared_node_list2");
//        editContentList2.orderBefore("shared_node_list2", "contentList2_text0");
//        englishEditSession.save();
//
//        // jcrService.publish(editContentList1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, false, false);
//        jcrService.publish(editContentList2.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        liveContentList1Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList1");
//        JCRNodeWrapper liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
//        testChildOrdering(liveContentList1Node, Constants.LIVE_WORKSPACE, "contentList1_text0", "contentList1_text1", "contentList1_text2");
//        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "shared_node_list2", "contentList2_text0", "contentList2_text1");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");
//
//        // Case 8 : now let's move it to yet another list, modify it, then publish it.
//        editContentList2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
//        englishEditSession.checkout(editContentList2);
//        JCRNodeWrapper editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
//        englishEditSession.checkout(editContentList3);
//        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList2/shared_node_list2", SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3");
//        englishEditSession.save();
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3");
//        englishEditSession.checkout(editSharedNode1);
//        editSharedNode1.setProperty("body", "English text update 2");
//        englishEditSession.save();
//        editContentList3.orderBefore("shared_node_list3", "contentList3_text0");
//        englishEditSession.save();
//
//        jcrService.publish(editContentList3.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        liveContentList2Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList2");
//        JCRNodeWrapper liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
//        testChildOrdering(liveContentList2Node, Constants.LIVE_WORKSPACE, "contentList2_text0", "contentList2_text1", "contentList2_text2");
//        testChildOrdering(liveContentList3Node, Constants.LIVE_WORKSPACE, "shared_node_list3", "contentList3_text0", "contentList3_text1");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");
//
//        // Case 9 : Let's move to yet another list, and then modify it's location in the list twice.
//        editContentList3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
//        englishEditSession.checkout(editContentList3);
//        JCRNodeWrapper editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
//        englishEditSession.checkout(editContentList4);
//        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/contentList3/shared_node_list3", SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");
//        editContentList4.orderBefore("shared_node_list4", "contentList4_text1");
//        editContentList4.orderBefore("shared_node_list4", "contentList4_text0");
//        englishEditSession.save();
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");
//
//        jcrService.publish(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        liveContentList3Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList3");
//        JCRNodeWrapper liveContentList4Node = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
//        testChildOrdering(liveContentList3Node, Constants.LIVE_WORKSPACE, "contentList3_text0", "contentList3_text1", "contentList3_text2");
//        testChildOrdering(liveContentList4Node, Constants.LIVE_WORKSPACE, "shared_node_list4", "contentList4_text0", "contentList4_text1");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was not touched, it should be available in live work space.");
//
//        // Case 10 : Delete the node, publish it and check that it has disappeared in live mode.
//        editContentList4 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4");
//        englishEditSession.checkout(editContentList4);
//        editSharedNode1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4");
//        englishEditSession.checkout(editSharedNode1);
//        editSharedNode1.remove();
//        englishEditSession.save();
//
//        jcrService.publish(editContentList4.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages,
//                false);
//
//        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList4/shared_node_list4", "Shared Text node 4 was deleted, it should not be present in the live workspace anymore !");
//        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/contentList0/shared_node_list0", "Shared Text node 0 was deleted, it should not be present in the live workspace anymore !");
//
//    }
    JCRSessionWrapper englishEditSession;
    JCRSessionWrapper englishLiveSession;

    private void getCleanSession() throws Exception {
        String defaultLanguage = site.getDefaultLanguage();
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        englishEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        englishLiveSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
    }

    @Test
    public void testCPagesWithSubPages() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        getCleanSession();
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");
        String englishEditSiteHomeNodeIdentifier = englishEditSiteHomeNode.getIdentifier();

        // now let's setup the pages we will use.

        JCRNodeWrapper englishEditPage1Node = englishEditSiteHomeNode.addNode("page1", "jnt:page");
        String englishEditPage1NodeIdentifier = englishEditPage1Node.getIdentifier();
        englishEditPage1Node.setProperty("jcr:title", "Page1");
        TestHelper.createList(englishEditPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditPage2Node = englishEditSiteHomeNode.addNode("page2", "jnt:page");
        englishEditPage2Node.setProperty("jcr:title", "Page2");
        TestHelper.createList(englishEditPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditPage3Node = englishEditSiteHomeNode.addNode("page3", "jnt:page");
        englishEditPage3Node.setProperty("jcr:title", "Page3");
        TestHelper.createList(englishEditPage3Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditPage3Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        JCRNodeWrapper englishEditSubPage1Node = englishEditPage1Node.addNode("page1_subpage1", "jnt:page");
        englishEditSubPage1Node.setProperty("jcr:title", "SubPage1");
        TestHelper.createList(englishEditSubPage1Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditSubPage1Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        JCRNodeWrapper englishEditSubPage2Node = englishEditPage1Node.addNode("page1_subpage2", "jnt:page");
        englishEditSubPage2Node.setProperty("jcr:title", "SubPage2");
        TestHelper.createList(englishEditSubPage2Node, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        TestHelper.createList(englishEditSubPage2Node, "contentList1", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

        englishEditSession.save();

        Set<String> languages = null;
        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(englishEditPage2Node.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        jcrService.publishByMainId(englishEditSubPage1Node.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);

        getCleanSession();

        // Case 1 : let's check the existence of the node property value in the live workspace.
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
                "ContentList0 on Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0/contentList0_text0",
                "Text0 in ContentList0 on Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
                "Sub Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1",
                "ContentList1 on Sub Page 1 should have been published");
        testNodeInWorkspace(englishLiveSession,
                SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1/contentList1/contentList1_text1",
                "Text1 in ContentList1 on Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
                "Sub Page 2 should not have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1",
                "ContentList1 on Sub Page 2 should not have been published");
        testNodeNotInWorkspace(englishLiveSession,
                SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2/contentList1/contentList1_text1",
                "Text1 in ContentList1 on Sub Page 2 should not have been published");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2",
                "Page 2 should have been published");

        // Case 2 : now let's modify the node, republish and check.
        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditPage1NodeIdentifier));
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
                SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed");
        englishEditSession.save();

        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);

        getCleanSession();

        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0",
                "ContentList0 on Page 1 not be present anymore since we renamed it.");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/contentList0_renamed",
                "ContentList0_renamed on Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
                "Sub Page 2 should not have been published");

        // Case 3 : now let's unpublish the node and test it's presence in the live workspace.
        getCleanSession();
        jcrService.unpublish(Collections.singletonList(englishEditPage1NodeIdentifier), languages);
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have been unpublished !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
                "Sub Page 1 should not have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
                "Sub Page 2 should not have been published");
        getCleanSession();
        // Case 4 : now let's republish the node and test it's presence in the live workspace.
        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
                "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
                "Sub Page 2 should not have been published");

        getCleanSession();
        // Case 5 : let's rename the page and check it's been properly renamed in the live workspace.
        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1", SITECONTENT_ROOT_NODE + "/home/page1_renamed");
        englishEditSession.save();
        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed",
                "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage1",
                "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1_renamed/page1_subpage2",
                "Sub Page 2 should not have been published");
        // now let's move it back to continue the tests.
        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
        englishEditSession.move(SITECONTENT_ROOT_NODE + "/home/page1_renamed", SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.save();
        jcrService.publishByMainId(englishEditPage1NodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, false, null);
        getCleanSession();
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have be published !");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage1",
                "Sub Page 1 should have been published");
        testNodeNotInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1/page1_subpage2",
                "Sub Page 2 should not have been published");

        // Case 6 : now we must move the page inside the list of the parent page, and check that the move is properly propagated in live mode
        englishEditSiteHomeNode = englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier);
        englishEditSession.checkout(englishEditSiteHomeNode);
        englishEditSiteHomeNode.orderBefore("page1", null); // this should put it at the end of the list.
        englishEditSession.save();
        testChildOrdering(englishEditSiteHomeNode, Constants.EDIT_WORKSPACE, "page2", "page3", "page1");
        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);
        JCRNodeWrapper englishLiveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        testChildOrdering(englishLiveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3", "page1");
        getCleanSession();
        // Case 7 : now let's move the page to another page
        JCRNodeWrapper editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.checkout(englishEditSession.getNodeByIdentifier(englishEditSiteHomeNodeIdentifier));
        JCRNodeWrapper editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        englishEditSession.checkout(editPage2);
        englishEditSession.checkout(
                editPage1); // we have to check it out because of a property being changed during move !
        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page2/page1");
        editPage2.orderBefore("page1", "contentList1");
        englishEditSession.save();
        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);
        JCRNodeWrapper liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        JCRNodeWrapper livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        testChildOrdering(liveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3");
        testChildOrdering(livePage2, Constants.LIVE_WORKSPACE, "contentList0", "page1", "contentList1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page2/page1",
                "Page 1 was not properly moved below page 2 in the live workspace.");

        getCleanSession();

        // Case 8 : now let's move it to yet another list, modify it, then publish it.
        editPage2 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        englishEditSession.checkout(editPage2);
        JCRNodeWrapper editPage3 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3");
        englishEditSession.checkout(editPage3);
        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2/page1");
        englishEditSession.checkout(editPage1);
        englishEditSession.move(editPage1.getPath(), SITECONTENT_ROOT_NODE + "/home/page3/page1");
        editPage3.orderBefore("page1", "contentList1");
        editPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3/page1");
        editPage1.addNode("anotherList", "jnt:contentList");
        englishEditSession.save();
        jcrService.publishByMainId(englishEditSiteHomeNodeIdentifier, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);

        liveSiteHomeNode = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        livePage2 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page2");
        JCRNodeWrapper livePage3 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page3");
        testChildOrdering(liveSiteHomeNode, Constants.LIVE_WORKSPACE, "page2", "page3");
        testChildOrdering(livePage2, Constants.LIVE_WORKSPACE, "contentList0", "contentList1");
        testChildOrdering(livePage3, Constants.LIVE_WORKSPACE, "contentList0", "page1", "contentList1");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1",
                "Page 1 was not properly moved below page 3 in the live workspace.");
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page3/page1/anotherList",
                "New list on Page 1 was not found in the live workspace.");

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
    @Test
    public void testAddMixinAndPublish() throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(
                Constants.EDIT_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRSessionWrapper englishLiveSession = jcrService.getSessionFactory().getCurrentUserSession(
                Constants.LIVE_WORKSPACE, Locale.ENGLISH, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        Set<String> languages = null;

        JCRNodeWrapper englishEditPage1 = englishEditSiteHomeNode.addNode("page1", "jnt:page");
        englishEditPage1.setProperty("jcr:title", "Page1");

        englishEditSession.save();

        jcrService.publishByMainId(englishEditSiteHomeNode.getIdentifier(), Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, languages, true, null);
        testNodeInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1",
                "Page 1 should have been published to live workspace !");

        //  Add mixin to a node already published once, publish it, and verify that the new mixin was properly added.
        englishEditSession.checkout(englishEditPage1);
        englishEditPage1.addMixin("jmix:sitemap");
        englishEditSession.save();
        assertTrue("Page 1 should now have the sitemap mixin type in edit workspace", englishEditPage1.isNodeType(
                "jmix:sitemap"));
        testPropertyInWorkspace(englishEditSession, SITECONTENT_ROOT_NODE + "/home/page1", "changefreq", "monthly",
                "Propery changefreq should have default value of 'monthly'");
        jcrService.publishByMainId(englishEditPage1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                languages, false, null);
        JCRNodeWrapper englishLivePage1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        assertTrue("Page 1 should now have the sitemap mixin type in live workspace", englishLivePage1.isNodeType(
                "jmix:sitemap"));
        testPropertyInWorkspace(englishLiveSession, SITECONTENT_ROOT_NODE + "/home/page1", "changefreq", "monthly",
                "Propery changefreq should have default value of 'monthly'");

        englishEditPage1 = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        englishEditSession.checkout(englishEditPage1);
        englishEditPage1.removeMixin("jmix:sitemap");
        englishEditSession.save();
        assertFalse("Page 1 should now no longer have the sitemap mixin in the edit workspace!",
                englishEditPage1.isNodeType("jmix:sitemap"));
        jcrService.publishByMainId(englishEditPage1.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                languages, false, null);
        englishLivePage1 = englishLiveSession.getNode(SITECONTENT_ROOT_NODE + "/home/page1");
        assertFalse("Page 1 should now no longer have the sitemap mixin in the live workspace!",
                englishLivePage1.isNodeType("jmix:sitemap"));
    }

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
            StringBuilder stringBuilder = TestHelper.dumpTree(new StringBuilder(), sessionWrapper.getNode(
                    SITECONTENT_ROOT_NODE + "/home"), 0, true);
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
            StringBuilder stringBuilder = TestHelper.dumpTree(new StringBuilder(), sessionWrapper.getNode(
                    SITECONTENT_ROOT_NODE + "/home"), 0, true);
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

    private void testChildOrdering(JCRNodeWrapper contentList1Node, String workspace, String child0NodeName,
                                   String child1NodeName, String child2NodeName) throws RepositoryException {
        int index = 0;
        boolean orderIsValid = true;
        String expectedValue = null;
        String foundValue = null;
        NodeIterator childNodeIterator = contentList1Node.getNodes();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            if (!child0NodeName.equals(currentChildNode.getName()) && !child1NodeName.equals(
                    currentChildNode.getName()) && !child2NodeName.equals(currentChildNode.getName())) {
                continue;
            }
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
        assertTrue("Move inside the same list has not been properly " + (workspace.equals(
                Constants.EDIT_WORKSPACE) ? "done in staging mode" : "propagated to live mode") + "! Expected value=" +
                   expectedValue + " but found value=" + foundValue, orderIsValid && index == 3);
    }

    private void testChildOrdering(JCRNodeWrapper liveContentList1Node, String workspace, String child0NodeName,
                                   String child1NodeName) throws RepositoryException {
        int index = 0;
        boolean orderIsValid = true;
        String expectedValue = null;
        String foundValue = null;
        NodeIterator childNodeIterator = liveContentList1Node.getNodes();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            if (!child0NodeName.equals(currentChildNode.getName()) && !child1NodeName.equals(
                    currentChildNode.getName())) {
                continue;
            }
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
        assertTrue("Move inside the same list has not been properly " + (workspace.equals(
                Constants.EDIT_WORKSPACE) ? "done in staging mode" : "propagated to live mode") + " ! Expected value=" +
                   expectedValue + " but found value=" + foundValue, orderIsValid && index == 2);
    }

    @After
    public void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testNodeReorder() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        getCleanSession();
        JCRNodeWrapper home = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        
        JCRNodeWrapper source = home.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper page2 = source.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper page3 = source.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "Page3");
        JCRNodeWrapper page4 = source.addNode("page4", "jnt:page");
        page4.setProperty("jcr:title", "Page4");
        JCRNodeWrapper page5 = source.addNode("page5", "jnt:page");
        page5.setProperty("jcr:title", "Page5");
        JCRNodeWrapper page6 = source.addNode("page6", "jnt:page");
        page6.setProperty("jcr:title", "Page6");
        englishEditSession.save();

        jcrService.publishByMainId(home.getIdentifier());

        JCRNodeWrapper liveSource = englishLiveSession.getNodeByIdentifier(source.getIdentifier());

        List<String> pageNames = Arrays.asList("page1", "page2", "page3", "page4", "page5", "page6");
        List<String> pageFound = new ArrayList<String>();
        NodeIterator nodeIterator = liveSource.getNodes();
        int i = 0;
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testNodeReorder: [" + i + "] " + nodeWrapper.getName());
            assertEquals("Order of published nodes is wrong."
                    + " Node name should be: " + s, pageNames.get(i),
                    nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
        assertTrue("Number of pages should be " + pageNames.size() + " but found " + pageFound.size(),
                pageNames.size() == pageFound.size());

        englishEditSession.checkout(source);
        englishEditSession.checkout(page1);
        englishEditSession.checkout(page2);
        englishEditSession.checkout(page3);
        englishEditSession.checkout(page4);
        englishEditSession.checkout(page5);
        englishEditSession.checkout(page6);
        source.orderBefore("page4", "page2");
        source.orderBefore("page6", "page5");
        englishEditSession.save();

        jcrService.publishByMainId(home.getIdentifier());
        
        pageNames = Arrays.asList("page1", "page4", "page2", "page3", "page6", "page5");
        pageFound = new ArrayList<String>();
        i = 0;
        nodeIterator = englishLiveSession.getNodeByIdentifier(source.getIdentifier()).getNodes();
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testNodeReorder source: [" + i + "] " + nodeWrapper.getName());
            assertEquals("Publishing of the node reodering failed."
                    + " Node name should be: " + s, pageNames.get(i),
                    nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
   }
    
    @Test
    public void testNodeAddAndReorder() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        getCleanSession();
        JCRNodeWrapper home = englishEditSession.getNode(SITECONTENT_ROOT_NODE);
        
        JCRNodeWrapper source = home.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper page1 = source.addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page1");
        JCRNodeWrapper page2 = source.addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page2");
        JCRNodeWrapper page3 = source.addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "Page3");
        
        englishEditSession.save();
        jcrService.publishByMainId(home.getIdentifier());
        
        
        JCRNodeWrapper page4 = source.addNode("page4", "jnt:page");
        page4.setProperty("jcr:title", "Page4");
        JCRNodeWrapper page5 = source.addNode("page5", "jnt:page");
        page5.setProperty("jcr:title", "Page5");
        JCRNodeWrapper page6 = source.addNode("page6", "jnt:page");
        page6.setProperty("jcr:title", "Page6");
        
        englishEditSession.save();
        jcrService.publishByMainId(home.getIdentifier());

        JCRNodeWrapper liveSource = englishLiveSession.getNodeByIdentifier(source.getIdentifier());

        List<String> pageNames = Arrays.asList("page1", "page2", "page3", "page4", "page5", "page6");
        List<String> pageFound = new ArrayList<String>();
        NodeIterator nodeIterator = liveSource.getNodes();
        int i = 0;
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testNodeReorder: [" + i + "] " + nodeWrapper.getName());
            assertEquals("Order of published nodes is wrong."
                    + " Node name should be: " + s, pageNames.get(i),
                    nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
        assertTrue("Number of pages should be " + pageNames.size() + " but found " + pageFound.size(),
                pageNames.size() == pageFound.size());

        englishEditSession.checkout(source);
        englishEditSession.checkout(page1);
        englishEditSession.checkout(page2);
        englishEditSession.checkout(page3);
        englishEditSession.checkout(page4);
        englishEditSession.checkout(page5);
        englishEditSession.checkout(page6);
        source.orderBefore("page2", "page5");

        englishEditSession.save();
        jcrService.publishByMainId(home.getIdentifier());
        
        pageNames = Arrays.asList("page1", "page3", "page4", "page2", "page5", "page6");
        pageFound = new ArrayList<String>();
        i = 0;
        nodeIterator = englishLiveSession.getNodeByIdentifier(source.getIdentifier()).getNodes();
        while (nodeIterator.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
            String s = pageNames.get(i);
            logger.info("testNodeReorder source: [" + i + "] " + nodeWrapper.getName());
            assertEquals("Publishing of the node reodering failed."
                    + " Node name should be: " + s, pageNames.get(i),
                    nodeWrapper.getName());
            pageFound.add(s);
            i++;
        }
   }


    @Test
    public void testNodeRemoveAndAdd() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        getCleanSession();
        JCRNodeWrapper home = englishEditSession.getNode(SITECONTENT_ROOT_NODE);

        JCRNodeWrapper source = home.addNode("source", "jnt:page");
        source.setProperty("jcr:title", "Source");
        JCRNodeWrapper list = TestHelper.createList(source, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        englishEditSession.save();

        jcrService.publishByMainId(home.getIdentifier());
        String firstId = list.getIdentifier();
        list.remove();
        list = TestHelper.createList(source, "contentList0", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        String secondId = list.getIdentifier();
        englishEditSession.save();

        jcrService.publishByMainId(home.getIdentifier());

        try {
            englishLiveSession.getNodeByUUID(firstId);
            fail("Node should have been deleted");
        } catch (ItemNotFoundException e) {
        }

        JCRNodeWrapper liveList = englishLiveSession.getNode(list.getPath());
        assertEquals("Invalid uuid", secondId, liveList.getIdentifier());
   }
}