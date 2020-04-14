/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.versioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.JCRVersionService;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.content.VersionInfo;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Unit test to test version listing created during publication
 */
public class VersioningTest extends JahiaTestCase {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VersioningTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrVersioningTest_" + System.currentTimeMillis();
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static final String MAIN_CONTENT_TITLE = "Main content title update ";
    private static final String MAIN_CONTENT_BODY = "Main content body update ";
    private static int NUMBER_OF_VERSIONS = 2;
    JCRSessionWrapper editSession;
    JCRSessionWrapper liveSession;
    private SimpleDateFormat yyyy_mm_dd_hh_mm_ss;
    private Set<String> languagesStringSet;
    private String lastLabelForPublication = null;


    @Before
    public void setUp() throws Exception {
        try {
            site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                    TestHelper.INTRANET_TEMPLATES);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }

        loginRoot();
        
        yyyy_mm_dd_hh_mm_ss = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        languagesStringSet = new LinkedHashSet<String>();
        languagesStringSet.add(Locale.ENGLISH.toString());
    }


    /**
     * Test number of version after publication
     *
     * @throws Exception
     */
    @Test
    public void testVersions() throws Exception {
        try {
            JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

            reopenSession();

            final JCRNodeWrapper richText = createNodes(jcrService);

            // let's do some validation checks, first for the live workspace...

            // check number of versions

            reopenSession();
            final JCRNodeWrapper richTextLiveNode = liveSession.getNode(richText.getPath());

            List<VersionInfo> liveVersionInfos = ServicesRegistry.getInstance().getJCRVersionService().getVersionInfos(
                    liveSession, richTextLiveNode);
            final int[] index = {0};
            for (final VersionInfo curVersionInfo : liveVersionInfos) {
                final String versionName = curVersionInfo.getVersion().getName();
                if (curVersionInfo.getLabel() != null && !"".equals(curVersionInfo.getLabel()) &&
                    !curVersionInfo.getLabel().contains(",")) {
                    JCRTemplate.getInstance().doExecute("root", null, Constants.LIVE_WORKSPACE, Locale.ENGLISH,
                            new JCRCallback<Object>() {
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    JCRNodeWrapper versionNode = null;
                                    String versionLabel = curVersionInfo.getLabel();
                                    session.setVersionLabel(versionLabel);
                                    try {
                                        session.setVersionDate(yyyy_mm_dd_hh_mm_ss.parse(versionLabel.split(
                                                "_at_")[1]));
                                    } catch (ParseException e) {
                                        throw new RepositoryException(e);
                                    }
                                    versionNode = session.getNodeByUUID(richTextLiveNode.getIdentifier());
                                    validateVersionedNode(index[0], curVersionInfo, versionName, versionNode);
                                    index[0]++;
                                    return null;
                                }
                            });
                }
            }
            logger.debug("number of version: " + index[0]);
            assertEquals(NUMBER_OF_VERSIONS, index[0]);


        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
            throw ex;
        }
    }

    private void reopenSession() throws RepositoryException {
        JCRSessionFactory.getInstance().closeAllSessions();
        editSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
    }

    private JCRNodeWrapper createNodes(JCRPublicationService jcrService)
            throws RepositoryException, InterruptedException {
        JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
        JCRPublicationService jcrPublicationService = ServicesRegistry.getInstance().getJCRPublicationService();
        JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);
        Node versioningTestActivity = editSession.getWorkspace().getVersionManager().createActivity("versioningTest");
        Node previousActivity = editSession.getWorkspace().getVersionManager().setActivity(versioningTestActivity);
        if (previousActivity != null) {
            logger.debug("Previous activity=" + previousActivity.getName() + " new activity=" +
                         versioningTestActivity.getName());
        } else {
            logger.debug("New activity=" + versioningTestActivity.getName());
        }

        // get home page
        JCRNodeWrapper stageNode = stageRootNode.getNode("home");

        editSession.checkout(stageNode);
        JCRNodeWrapper stagedSubPage = stageNode.addNode("home_subpage1", "jnt:page");
        stagedSubPage.setProperty("jcr:title", "title0");
        stagedSubPage.setProperty("j:templateName", "simple");

        JCRNodeWrapper stagedPageContent = stagedSubPage.addNode("pagecontent", "jnt:contentList");
        JCRNodeWrapper stagedRow1 = stagedPageContent.addNode("row1", "jnt:row");
        stagedRow1.setProperty("column", "2col106");
        JCRNodeWrapper stagedCol1 = stagedRow1.addNode("col1", "jnt:contentList");
        JCRNodeWrapper mainContent = stagedCol1.addNode("mainContent", "jnt:mainContent");
        mainContent.setProperty("jcr:title", MAIN_CONTENT_TITLE + "0");
        mainContent.setProperty("body", MAIN_CONTENT_BODY + "0");

        JCRNodeWrapper richText1 = stagedCol1.addNode("richText1", "jnt:bigText");
        richText1.setProperty("text", "richText0");

        JCRNodeWrapper stagedSubSubPage = stagedSubPage.addNode("home_subsubpage1", "jnt:page");
        stagedSubSubPage.setProperty("jcr:title", "subtitle0");
        stagedSubSubPage.setProperty("j:templateName", "simple");
        editSession.save();

        // publish it
        publishAndLabelizedVersion(jcrPublicationService, jcrVersionService, stageNode.getIdentifier());

        richText1.setProperty("text", "richText1");
        editSession.save();
        publishAndLabelizedVersion(jcrPublicationService, jcrVersionService, stageNode.getIdentifier());

        // now let's do a little system versioning ourselves...

        editSession.getWorkspace().getVersionManager().checkpoint(stagedSubPage.getPath());
        return richText1;
    }

    private List<String> getUuids(List<PublicationInfo> publicationInfo) {
        List<String> uuids = new LinkedList<String>();
        for (PublicationInfo info : publicationInfo) {
            uuids.addAll(info.getAllUuids());
        }
        return uuids;
    }

    private void validateVersionedNode(int index, VersionInfo curVersionInfo, String versionName,
                                       JCRNodeWrapper versionNode) throws RepositoryException {
        assertNotNull("Version node is null !!", versionNode);
        JCRPropertyWrapper property = versionNode.getProperty("text");
        assertNotNull("Title property should not be null on versioned node", property);
        String versionTitle = property.getString();
        String title = "richText" + index;

        if (logger.isDebugEnabled() && curVersionInfo != null && versionName != null) {
            logger.debug("version number:" + versionName + ", text: " + versionTitle + " created=" +
                         curVersionInfo.getVersion().getCreated().getTime());
        }

        assertEquals("Title does not match !", title, versionTitle);
        // let's check the version node's path
        assertEquals("Versioned node path is invalid !", SITECONTENT_ROOT_NODE + "/home/home_subpage1/pagecontent/row1/col1/richText1",
                versionNode.getPath());
        // let's check the node type
        assertEquals("Versioned node should be viewed as a node type jnt:bigText", "jnt:bigText",
                versionNode.getPrimaryNodeTypeName());
        // let's check the mixin types
        assertTrue("Versioned node should be viewed as a mixin node type jmix:editorialContent", versionNode.isNodeType(
                "jmix:editorialContent"));
        // now let's check the parent
        JCRNodeWrapper parentVersionNode = versionNode.getParent();
        assertEquals("Parent node name is not correct", "col1", parentVersionNode.getName());
        assertEquals("Parent node type is not of type jnt:page", "jnt:contentList",
                parentVersionNode.getPrimaryNodeTypeName());
        assertEquals("Parent node path invalid", SITECONTENT_ROOT_NODE + "/home/home_subpage1/pagecontent/row1/col1",
                parentVersionNode.getPath());

    }

    @After
    public void tearDown() throws Exception {
        logout();
        
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testRestoreVersions() throws Exception {
        try {
            JCRPublicationService jcrPublicationService = ServicesRegistry.getInstance().getJCRPublicationService();
            JCRVersionService jcrVersionService = ServicesRegistry.getInstance().getJCRVersionService();
            reopenSession();
            JCRNodeWrapper stageRootNode = editSession.getNode(SITECONTENT_ROOT_NODE);
            // get home page
            JCRNodeWrapper stageNode = stageRootNode.getNode("home/listA");
            String listIdentifier = stageNode.getIdentifier();
            editSession.checkout(stageNode);

            JCRNodeWrapper richText = stageNode.addNode("richText", "jnt:bigText");
            String richTextIdentifier = richText.getIdentifier();
            richText.setProperty("text", "text0");
            editSession.save();
            
            // First publication
            String labelForFirstPublication = publishAndLabelizedVersion(jcrPublicationService, jcrVersionService, listIdentifier);
            JCRNodeWrapper richTextLiveNode = liveSession.getNodeByUUID(richText.getIdentifier());
            assertEquals("rich text should be texgt0", "text0", richTextLiveNode.getProperty("text").getString());
            logger.info("Versions after first publication (listA & rich text)");
            displayVersions(editSession, stageNode, liveSession);

            reopenSession();

            // Change a property of rich text
            stageNode = editSession.getNodeByUUID(listIdentifier);
            richText = editSession.getNodeByUUID(richTextIdentifier);
            editSession.checkout(richText);
            editSession.checkout(stageNode);
            richText.setProperty("text", "text1");
            editSession.save();

            //Second publication
            logger.info("Versions before second publication (listA & rich text)");
            displayVersions(editSession, stageNode, liveSession);
            reopenSession();
            publishAndLabelizedVersion(jcrPublicationService, jcrVersionService, listIdentifier);
            reopenSession();

            // Restore node
            jcrVersionService.restoreVersionLabel(editSession.getNodeByUUID(richTextIdentifier), yyyy_mm_dd_hh_mm_ss.parse(labelForFirstPublication.split("_at_")[1]), labelForFirstPublication, true);
            richText = editSession.getNodeByUUID(richTextIdentifier);
            assertEquals("text0", richText.getProperty("text").getString());

            // Third publication
            logger.info("Versions before publication of restore of listA");
            displayVersions(editSession, stageNode, liveSession);
            publishAndLabelizedVersion(jcrPublicationService, jcrVersionService, listIdentifier);
            richTextLiveNode = liveSession.getNodeByUUID(richText.getIdentifier());
            assertEquals("richTextLiveNode title should be text0", "text0", richTextLiveNode.getProperty("text").getString());

            // Close sessions
            logger.info("Versions after third publication (listA & rich text)");
            displayVersions(editSession, stageNode, liveSession);

            reopenSession();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    private void displayVersions(JCRSessionWrapper editSession, JCRNodeWrapper subPageEditNode,
                                 JCRSessionWrapper liveSession) throws RepositoryException {
        // Display versions
        VersionManager versionManager;
        VersionHistory versionHistory;
        versionManager = editSession.getWorkspace().getVersionManager();
        logger.info("Versions of " + subPageEditNode.getPath() + " in edit ws :");
        try {
            logger.info("Base version in edit ws is : " + versionManager.getBaseVersion(
                    subPageEditNode.getPath()).getName());
            logger.info("Base version in live ws is : " + liveSession.getWorkspace().getVersionManager().getBaseVersion(
                    subPageEditNode.getPath()).getName());
        } catch (RepositoryException e) {
            logger.debug(e.getMessage(), e);
        }
        try {
            versionHistory = versionManager.getVersionHistory(subPageEditNode.getPath());
            VersionIterator allVersions = versionHistory.getAllVersions();
            while (allVersions.hasNext()) {
                Version version = allVersions.nextVersion();
                StringBuilder builder = new StringBuilder();

                builder.append(version.getName());
                String[] strings = versionHistory.getVersionLabels(version);
                if (strings != null && strings.length > 0) {
                    builder.append(" ").append(Arrays.deepToString(strings));
                }
                Version[] versions = version.getPredecessors();
                for (Version version1 : versions) {
                    builder.append(" <- ").append(version1.getName());
                    strings = versionHistory.getVersionLabels(version1);
                    if (strings != null && strings.length > 0) {
                        builder.append(" ").append(Arrays.deepToString(strings));
                    }
                }
                logger.info(builder.toString());
            }
        } catch (RepositoryException e) {
            logger.debug(e.getMessage(), e);
        }
    }

    private String publishAndLabelizedVersion(JCRPublicationService jcrPublicationService,
                                            JCRVersionService jcrVersionService, String identifier)
            throws RepositoryException {
        Set<String> languagesStringSet = new LinkedHashSet<String>();
        languagesStringSet.add(Locale.ENGLISH.toString());
        List<PublicationInfo> infoList = jcrPublicationService.getPublicationInfo(identifier, languagesStringSet, true,
                true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
        jcrPublicationService.publishByInfoList(infoList, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE,
                Collections.<String>emptyList());
        String labelForPublication = getPublicationLabel();
        for (PublicationInfo info : infoList) {
            jcrVersionService.addVersionLabel(info.getAllUuids(), labelForPublication,
                    Constants.LIVE_WORKSPACE);
        }
        return Constants.LIVE_WORKSPACE+"_"+labelForPublication;
    }
    
    private String getPublicationLabel() {
        String labelForPublication = null;
        do {
            labelForPublication = "published_at_"
                    + yyyy_mm_dd_hh_mm_ss.format(GregorianCalendar
                            .getInstance().getTime());

        } while (labelForPublication.equals(lastLabelForPublication));
        
        lastLabelForPublication = labelForPublication;
        return labelForPublication;
    }

}
