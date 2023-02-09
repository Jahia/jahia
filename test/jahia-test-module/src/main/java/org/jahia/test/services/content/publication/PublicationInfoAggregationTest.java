/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content.publication;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.ComplexPublicationService;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PublicationInfoAggregationTest extends JahiaTestCase {

    private static final String SUBNODE_TITLE_EN1 = "text EN - subList1";
    private static final String SUBNODE_TITLE_FR1 = "text FR - subList1";
    private static final String SUBNODE_TITLE_EN2 = "text EN - subList2";
    private static final String SUBNODE_TITLE_FR2 = "text FR - subList2";

    private static ComplexPublicationService publicationService;

    private static String nodeUuid;
    private static String subNodeUuid1;
    private static String subNodeUuid1En;
    private static String subNodeUuid2;
    private static String subNodeUuid3;
    private static String subNodeUuid4;
    private static String subNodeUuid41;
    private static String subNodeUuid42;
    private static String subNodeUuid5;
    private static String subNodeUuid51;
    private static String subNodeUuid52;
    private static String ref1Uuid;

    private static JahiaUser editor;

    @BeforeClass
    public static void oneTimeSetup() throws Exception {

        publicationService = BundleUtils.getOsgiService(ComplexPublicationService.class, null);

        JCRPublicationService publicationService = BundleUtils.getOsgiService(JCRPublicationService.class, null);

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, session -> {

            JCRNodeWrapper node = session.getNode("/").addNode("testList", "jnt:contentList");
            nodeUuid = node.getIdentifier();

            JCRNodeWrapper subNode1 = node.addNode("testSubList1", "jnt:contentList");
            subNode1.setProperty("jcr:title", SUBNODE_TITLE_EN1);
            subNodeUuid1 = subNode1.getIdentifier();
            subNodeUuid1En = subNode1.getNode("j:translation_en").getIdentifier();

            JCRNodeWrapper subNode2 = node.addNode("testSubList2", "jnt:contentList");
            subNode2.setProperty("jcr:title", SUBNODE_TITLE_EN2);
            subNode2.addMixin("jmix:lastPublished");
            subNode2.setProperty(Constants.WORKINPROGRESS_STATUS, Constants.WORKINPROGRESS_STATUS_LANG);
            subNode2.setProperty(Constants.WORKINPROGRESS_LANGUAGES, new String[]{"en"});
            JCRNodeWrapper subNode2En = subNode2.getNode("j:translation_en");
            subNodeUuid2 = subNode2.getIdentifier();

            JCRNodeWrapper subNode3 = node.addNode("testSubList3", "jnt:contentList");
            subNodeUuid3 = subNode3.getIdentifier();

            JCRNodeWrapper subNode4 = node.addNode("testSubList4", "jnt:contentList");
            JCRNodeWrapper subNode41 = subNode4.addNode("testSubList4_1", "jnt:contentList");
            JCRNodeWrapper subNode42 = subNode4.addNode("testSubList4_2", "jnt:contentList");
            subNodeUuid4 = subNode4.getIdentifier();
            subNodeUuid41 = subNode41.getIdentifier();
            subNodeUuid42 = subNode42.getIdentifier();

            JCRNodeWrapper subNode5 = node.addNode("testSubList5", "jnt:contentList");
            JCRNodeWrapper subNode51 = subNode5.addNode("testSubList5_1", "jnt:contentList");
            JCRNodeWrapper subNode52 = subNode5.addNode("testSubList5_2", "jnt:contentList");
            subNodeUuid5 = subNode5.getIdentifier();
            subNodeUuid51 = subNode51.getIdentifier();
            subNodeUuid52 = subNode52.getIdentifier();

            JCRNodeWrapper ref1 = node.addNode("reference1", "jnt:contentReference");
            ref1.setProperty("j:node", subNode1);
            ref1Uuid = ref1.getIdentifier();

            editor = JahiaUserManagerService.getInstance().createUser("testUser", null, "testPassword", new Properties(), session).getJahiaUser();
            subNode1.grantRoles("u:" + editor.getUsername(), Collections.singleton("editor"));

            session.save();

            subNode2En.lockAndStoreToken("validation");

            return null;
        });

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.FRENCH, session -> {
            JCRNodeWrapper node = session.getNode("/testList");
            node.getNode("testSubList1").setProperty("jcr:title", SUBNODE_TITLE_FR1);
            node.getNode("testSubList2").setProperty("jcr:title", SUBNODE_TITLE_FR2);
            session.save();
            return null;
        });

        publicationService.publish(Arrays.asList(
            nodeUuid,
                subNodeUuid1,
                    subNodeUuid1En,
                    // subNodeUuid1Fr,
                subNodeUuid2, // Unpublished later
                    // subNodeUuid2En,
                    // subNodeUuid2Fr,
               // subNodeUuid3,
               subNodeUuid4,
                    subNodeUuid41,
                    subNodeUuid42,
               subNodeUuid5, // Marked for deletion later
                    subNodeUuid51, // Marked for deletion later
                    subNodeUuid52, // Marked for deletion later
               ref1Uuid
        ), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null);

        publicationService.unpublish(Collections.singletonList(subNodeUuid2), false);

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, session -> {
            JCRNodeWrapper subNode5 = session.getNodeByIdentifier(subNodeUuid5);
            subNode5.markForDeletion(null);
            session.save();
            return null;
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, session -> {
            JahiaUserManagerService.getInstance().deleteUser(editor.getLocalPath(), session);
            session.save();
            return null;
        });

        removeTestNodes(Constants.EDIT_WORKSPACE);
        removeTestNodes(Constants.LIVE_WORKSPACE);
    }

    @Test
    public void publishedNode_WithoutSubNodes_SeenPublished() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(nodeUuid, "en", false, false, getSession());
        Assert.assertEquals(PublicationInfo.PUBLISHED, info.getPublicationStatus());
    }

    @Test
    public void notPublishedNode_WithoutSubNodes_SeenNotPublished() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid3, "en", false, false, getSession());
        Assert.assertEquals(PublicationInfo.NOT_PUBLISHED, info.getPublicationStatus());
    }

    @Test
    public void publishedNode_WithPublishedTranslationSubNode_SeenPublished() throws Exception {
        testPublicationStatus(subNodeUuid1, "en", PublicationInfo.PUBLISHED);
    }

    @Test
    public void publishedNode_WithNotPublishedTranslationSubNode_SeenNotPublished() throws Exception {
        testPublicationStatus(subNodeUuid1, "fr", PublicationInfo.MODIFIED);
    }

    @Test
    public void unpublishedNode_WithNotPublishedTranslationSubNode_SeenNotPublished() throws Exception {
        testPublicationStatus(subNodeUuid2, "en", PublicationInfo.UNPUBLISHED);
    }

    @Test
    public void publishedNode_WithPublishedSubNodes_SeenPublished() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid4, "en", true, false, getSession());
        Assert.assertEquals(PublicationInfo.PUBLISHED, info.getPublicationStatus());
    }

    @Test
    public void publishedNode_WithDifferentSubNodes_SeenModified() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(nodeUuid, "en", true, false, getSession());
        Assert.assertEquals(PublicationInfo.MODIFIED, info.getPublicationStatus());
    }

    @Test
    public void publishedNode_WithPublishedReferences_SeenPublished() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(ref1Uuid, "en", false, true, getSession());
        Assert.assertEquals(PublicationInfo.PUBLISHED, info.getPublicationStatus());
    }

    @Test
    public void publishedNode_WithNotPublishedReferences_SeenModified() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(ref1Uuid, "fr", false, true, getSession());
        Assert.assertEquals(PublicationInfo.MODIFIED, info.getPublicationStatus());
    }

    @Test
    public void node_WithLockedWorkInProgressTranslationSubNode_SeenLockedWorkInProgress() throws Exception {
        testLockedWorkInProgress(subNodeUuid2, "en", true);
    }

    @Test
    public void node_WithoutLockedWorkInProgressTranslationSubNode_NotSeenLockedWorkInProgress() throws Exception {
        testLockedWorkInProgress(subNodeUuid1, "en", false);
    }

    @Test
    public void nonRootMarkedForDeletion_Recognized() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid51, "en", false, false, getSession());
        Assert.assertEquals(PublicationInfo.MARKED_FOR_DELETION, info.getPublicationStatus());
        Assert.assertTrue(info.isNonRootMarkedForDeletion());
    }

    @Test
    public void rootMarkedForDeletion_Recognized() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid5, "en", false, false, getSession());
        Assert.assertEquals(PublicationInfo.MARKED_FOR_DELETION, info.getPublicationStatus());
        Assert.assertFalse(info.isNonRootMarkedForDeletion());
    }

    @Test
    public void allowedToPublishWithoutWorkflow_Recognized() throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid1, "en", false, false, getSession());
        Assert.assertTrue(info.isAllowedToPublishWithoutWorkflow());
    }

    @Test
    public void notAllowedToPublishWithoutWorkflow_Recognized() throws Exception {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JahiaUser currentUserBackup = sessionFactory.getCurrentUser();
        sessionFactory.setCurrentUser(editor);
        try {
            ComplexPublicationService.AggregatedPublicationInfo info = publicationService.getAggregatedPublicationInfo(subNodeUuid1, "en", false, false, getSession());
            Assert.assertFalse(info.isAllowedToPublishWithoutWorkflow());
        } finally {
            sessionFactory.setCurrentUser(currentUserBackup);
        }
    }

    private static void testPublicationStatus(String nodeUuid, String language, int expectedPublicationStatus) throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo infoNotUsingSubNodes = publicationService.getAggregatedPublicationInfo(nodeUuid, language, false, false, getSession());
        ComplexPublicationService.AggregatedPublicationInfo infoUsingSubNodes = publicationService.getAggregatedPublicationInfo(nodeUuid, language, true, false, getSession());
        Assert.assertEquals(expectedPublicationStatus, infoNotUsingSubNodes.getPublicationStatus());
        Assert.assertEquals(expectedPublicationStatus, infoUsingSubNodes.getPublicationStatus());
    }

    private static void testLockedWorkInProgress(String nodeUuid, String language, boolean expectedLockedWorkInProgress) throws Exception {
        ComplexPublicationService.AggregatedPublicationInfo infoUsingSubNodes = publicationService.getAggregatedPublicationInfo(nodeUuid, language, true, false, getSession());
        Assert.assertEquals(expectedLockedWorkInProgress, infoUsingSubNodes.isLocked());
        Assert.assertEquals(expectedLockedWorkInProgress, infoUsingSubNodes.isWorkInProgress());
    }

    private static void removeTestNodes(String workspace) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, session -> {
            if (session.itemExists("/testList")) {
                session.getNode("/testList").remove();
                session.save();
            }
            return null;
        });
    }

    private static JCRSessionWrapper getSession() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentUserSession();
    }
}
