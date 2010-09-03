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

package org.jahia.services.versioning;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Unit test to test version listing created during publication
 */
public class VersioningTest extends TestCase {
    private static Logger logger = Logger.getLogger(VersioningTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrVersioningTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static final String MAIN_CONTENT_TITLE = "Main content title update ";
    private static final String MAIN_CONTENT_BODY = "Main content body update ";
    private static int NUMBER_OF_VERSIONS = 20;

    protected void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME, "localhost"+System.currentTimeMillis(), TestHelper.INTRANET_TEMPLATES);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }


    /**
     * Test number of version after publication
     *
     * @throws Exception
     */
    public void testVersions() throws Exception {
        try {
            JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

            JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
            JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);

            JCRNodeWrapper stagedSubPage = createNodes(jcrService, editSession);

            // let's do some validation checks, first for the live workspace...

            // check number of versions

            List<VersionInfo> editVersionInfos = ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(editSession, stagedSubPage);
            int index = 0;
            for (VersionInfo curVersionInfo : editVersionInfos) {
                String versionName = curVersionInfo.getVersion().getName();
                JCRNodeWrapper versionNode = null;
                if (curVersionInfo.getCheckinDate() != null) {
                    versionNode = stagedSubPage.getFrozenVersionAsRegular(curVersionInfo.getCheckinDate().getTime());
                } else {
                    versionNode = stagedSubPage.getFrozenVersionAsRegular(curVersionInfo.getVersion().getCreated().getTime());
                }
                validateVersionedNode(index, curVersionInfo, versionName, versionNode);
                index++;
            }
            logger.debug("number of version: " + index);
            assertEquals(NUMBER_OF_VERSIONS, index);


            JCRNodeWrapper subPagePublishedNode = liveSession.getNode(stagedSubPage.getPath());

            List<VersionInfo> liveVersionInfos = ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(liveSession, subPagePublishedNode);
            index = 0;
            for (VersionInfo curVersionInfo : liveVersionInfos) {
                String versionName = curVersionInfo.getVersion().getName();
                JCRNodeWrapper versionNode = null;
                if (curVersionInfo.getComment() != null && !"".equals(curVersionInfo.getComment()) && !curVersionInfo.getComment().contains(",")) {
                    versionNode = subPagePublishedNode.getFrozenVersionAsRegular(curVersionInfo.getComment());
                    validateVersionedNode(index, curVersionInfo, versionName, versionNode);
                    index++;
                }
            }
            logger.debug("number of version: " + index);                                                        
            assertEquals(NUMBER_OF_VERSIONS, index);


        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
            throw ex;
        }
    }

    private JCRNodeWrapper createNodes(JCRPublicationService jcrService, JCRSessionWrapper editSession)
            throws RepositoryException {
        JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
        JCRSessionWrapper liveSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);
        Set<String> languagesStringSet = new LinkedHashSet<String>();
        languagesStringSet.add(Locale.ENGLISH.toString());
        Node versioningTestActivity = editSession.getWorkspace().getVersionManager().createActivity("versioningTest");
        Node previousActivity = editSession.getWorkspace().getVersionManager().setActivity(versioningTestActivity);
        if (previousActivity != null) {
            logger.debug("Previous activity=" + previousActivity.getName() + " new activity=" + versioningTestActivity.getName());
        } else {
            logger.debug("New activity=" + versioningTestActivity.getName());
        }

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("jcr:title", "title0");

        JCRNodeWrapper stagedPageContent = stagedSubPage.addNode("pagecontent", "jnt:contentList");
        JCRNodeWrapper stagedRow1 = stagedPageContent.addNode("row1", "jnt:row");
        stagedRow1.setProperty("column", "2col106");
        JCRNodeWrapper stagedCol1 = stagedRow1.addNode("col1", "jnt:contentList");
        JCRNodeWrapper mainContent = stagedCol1.addNode("mainContent", "jnt:mainContent");
        mainContent.setProperty("jcr:title", MAIN_CONTENT_TITLE + "0");
        mainContent.setProperty("body", MAIN_CONTENT_BODY + "0");

        JCRNodeWrapper stagedSubSubPage = stagedSubPage.addNode("home_subsubpage1", "jnt:page");
        stagedSubSubPage.setProperty("jcr:title", "subtitle0");
        editSession.save();

        // publish it
        jcrService.publish(stageNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languagesStringSet, true);
        String label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
        jcrVersionService.addVersionLabel(liveSession.getNodeByUUID(stageNode.getIdentifier()),label);
        for (int i = 1; i < NUMBER_OF_VERSIONS; i++) {

            for (int j = 0; j < 2; j++) {
                editSession.checkout(mainContent);
                int updateNumber = (i-1)*2 + j+1;
                mainContent.setProperty("jcr:title", MAIN_CONTENT_TITLE + updateNumber);
                mainContent.setProperty("body", MAIN_CONTENT_BODY + updateNumber);
                editSession.save();
                jcrService.publish(mainContent.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languagesStringSet,
                        true);
            }

            editSession.checkout(stagedSubPage);
            stagedSubPage.setProperty("jcr:title", "title" + i);
            editSession.save();

            // each time the node i published, a new version should be created
            jcrService.publish(stagedSubPage.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languagesStringSet,
                    false);
            label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
            jcrVersionService.addVersionLabel(liveSession.getNodeByUUID(stagedSubPage.getIdentifier()),label);
            editSession.checkout(stagedSubSubPage);
            stagedSubSubPage.setProperty("jcr:title", "subtitle" + i);
            editSession.save();
            jcrService.publish(stagedSubSubPage.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languagesStringSet, 
                    false);
            label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
            jcrVersionService.addVersionLabel(liveSession.getNodeByUUID(stagedSubSubPage.getIdentifier()),label);
        }

        // now let's do a little system versioning ourselves...

        editSession.getWorkspace().getVersionManager().checkpoint(stagedSubPage.getPath());
        return stagedSubPage;
    }

    private void validateVersionedNode(int index, VersionInfo curVersionInfo, String versionName, JCRNodeWrapper versionNode) throws RepositoryException {
        assertNotNull("Version node is null !!", versionNode);
        JCRPropertyWrapper property = versionNode.getProperty("jcr:title");
        assertNotNull("Title property should not be null on versioned node",property);
        String versionTitle = property.getString();
        String title = "title" + index;

        if (logger.isDebugEnabled() && curVersionInfo!=null && versionName!=null) {
            Calendar checkinCalendar = curVersionInfo.getCheckinDate();
            Date checkinDate = null;
            if (checkinCalendar != null) {
                checkinDate = checkinCalendar.getTime();
            }
            logger.debug(
                    "version number:" + versionName + ", jcr:title: " + versionTitle + " created=" + curVersionInfo.getVersion().getCreated().getTime() + " checkinDate=" + checkinDate);
        }

        assertEquals("Title does not match !", title, versionTitle);
        // let's check the version node's path
        assertEquals("Versioned node path is invalid !", SITECONTENT_ROOT_NODE + "/home/home_subpage1", versionNode.getPath());
        // let's check the node type
        assertEquals("Versioned node should be viewed as a node type jnt:page", "jnt:page", versionNode.getPrimaryNodeTypeName());
        // let's check the mixin types
        assertTrue("Versioned node should be viewed as a mixin node type jmix:basemetadata", versionNode.isNodeType("jmix:basemetadata"));
        assertTrue("Versioned node should be viewed as a mixin node type jmix:nodenameInfo", versionNode.isNodeType("jmix:nodenameInfo"));

        // getNode check
        assertEquals("Versioned node getNode() returns invalid node name", "home_subsubpage1", versionNode.getNode("home_subsubpage1").getName());
        assertEquals("Versioned node getNode() returns invalid nodetype", "jnt:page", versionNode.getNode("home_subsubpage1").getPrimaryNodeType().getName());
        assertEquals("Versioned node getNode(..) returns invalid node name", "home", versionNode.getNode("..").getName());

        // now let's check the parent
        JCRNodeWrapper parentVersionNode = versionNode.getParent();
        assertEquals("Parent node name is not correct", "home", parentVersionNode.getName());
        assertEquals("Parent node type is not of type jnt:page", "jnt:page", parentVersionNode.getPrimaryNodeTypeName());
        assertEquals("Parent node path invalid", SITECONTENT_ROOT_NODE + "/home", parentVersionNode.getPath());

        // now let's check the child objects.
        JCRNodeWrapper mainContentNode = versionNode.getNode("pagecontent/row1/col1/mainContent");
        assertEquals("Child node has incorrect value", MAIN_CONTENT_TITLE + (index*2), mainContentNode.getProperty("jcr:title").getString());
        assertEquals("Child node has incorrect value", MAIN_CONTENT_BODY + (index*2), mainContentNode.getProperty("body").getString());

    }

    public void testVersionsLabels() throws Exception {
        try {
            JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
            JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
            JCRSessionWrapper editSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);

            JCRNodeWrapper stagedSubPage = createNodes(jcrService, editSession);
            String labeledTitle = stagedSubPage.getProperty("jcr:title").getString();
            String label = "my_version";
            jcrVersionService.addVersionLabel(stagedSubPage, label);

            VersionHistory history = editSession.getWorkspace().getVersionManager().getVersionHistory(
                    stagedSubPage.getPath());
            assertTrue(stagedSubPage.getPath()+" should have a version labeled my_version",history.hasVersionLabel(
                    label));

            Version versionByLabel = JCRVersionService.findVersionByLabel(history, label);
            assertNotNull("We should have found a version associated to label "+label,versionByLabel);
            Node node = versionByLabel.getFrozenNode();
            assertTrue("Labeled version should have a j:checkinDate property", node.hasProperty("j:checkinDate"));

            JCRNodeWrapper frozenVersionAsRegular = stagedSubPage.getFrozenVersionAsRegular(node.getProperty(
                    "j:checkinDate").getDate().getTime());
            Date createdDate = frozenVersionAsRegular.getProperty("jcr:created").getDate().getTime();
            assertEquals("Label Version node title should be equal to current node title",labeledTitle,frozenVersionAsRegular.getProperty("jcr:title").getString());
            editSession.checkout(stagedSubPage);
            stagedSubPage.setProperty("jcr:title","my title");
            editSession.save();
            jcrService.publish(stagedSubPage.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                    false);
            Version baseVersion = editSession.getWorkspace().getVersionManager().getBaseVersion(stagedSubPage.getPath());
            assertEquals("Base version frozen node title and current node title should be equals",
                         stagedSubPage.getProperty("jcr:title").getString(),
                         stagedSubPage.getFrozenVersionAsRegular(baseVersion.getFrozenNode().getProperty("j:checkinDate").getDate().getTime()).getProperty("jcr:title").getString());
            jcrVersionService.restoreVersionLabel(stagedSubPage,label);            
            stagedSubPage = editSession.getNodeByUUID(stagedSubPage.getIdentifier());
            assertEquals("Restored node created must match the one from labeled version",createdDate,stagedSubPage.getProperty("jcr:created").getDate().getTime());
            assertEquals("Restored node label must match the one from labeled version",labeledTitle,stagedSubPage.getProperty("jcr:title").getString());
            validateVersionedNode(NUMBER_OF_VERSIONS-1,null, null, stagedSubPage);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),e);
            throw e;
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
