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
package org.jahia.services.workflow;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.test.TestHelper;

import java.io.InputStream;
import java.util.*;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

/**
 * Unit test for publish / unpublish using JCR 
 * - tests publish / unpublish of pages, container lists, containers
 * - with different language settings (all, none, one, two languages)
 * - with using user not having rights
 * - publication with automatically publishing parent
 *
 * @author Benjamin Papez
 *
 */
public class JcrWorkflowTest extends TestCase {
    private static Logger logger = Logger.getLogger(JcrWorkflowTest.class);
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "jcrWorkflowTest";
    private final static String SITECONTENT_ROOT_NODE = "/" + Constants.CONTENT
            + "/sites/" + TESTSITE_NAME;

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
            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getThreadSession(ctx
                    .getUser());
            JCRSessionWrapper liveSession = jcrService.getThreadSession(ctx
                    .getUser(), Constants.LIVE_WORKSPACE);

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
            JahiaUser guestUser = userMgr
                    .lookupUser(JahiaUserManagerService.GUEST_USERNAME);
            boolean accessWasDenied = false;
            try {
                jcrService.publish(stageNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, guestUser,
                        false, false);
            } catch (AccessDeniedException e) {
                accessWasDenied = true;
            }
            assertTrue(
                    "Guest user was able to publish a node although he has no access "
                            + stageNode.getPath(), accessWasDenied);

            jcrService.publish(stageNode.getPath(),Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, session
                    .getUser(), false, false);

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

            accessWasDenied = false;
            try {
                jcrService.unpublish(stageNode.getPath(), languages, guestUser);
            } catch (AccessDeniedException e) {
                accessWasDenied = true;
            }
            assertTrue(
                    "Guest user was able to unpublish a node although he has no access "
                            + stageNode.getPath(), accessWasDenied);
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    public void testPublishUnpublishPageWithContent() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getThreadSession(ctx.getUser());
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
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getThreadSession(ctx.getUser());
        JCRSessionWrapper liveSession = jcrService.getThreadSession(ctx
                .getUser(), Constants.LIVE_WORKSPACE);
        Map<String, Long> publishedDateForObjects = new HashMap<String, Long>();
        addNodeAndDependands(pageNodeToPublish, languages,
                publishedDateForObjects);

        jcrService.publish(pageNodeToPublish.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, session
                .getUser(), publishParent, false);

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
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getThreadSession(ctx.getUser());        
        Map<String, Long> publishedDateForObjects = new HashMap<String, Long>();
        addNodeAndDependands(pageNodeToPublish, languages,
                publishedDateForObjects);

        jcrService.unpublish(pageNodeToPublish.getPath(), languages, session
                .getUser());

        JCRSessionWrapper liveSession = jcrService.getThreadSession(ctx
                .getUser(), Constants.LIVE_WORKSPACE);
        
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

    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}
