/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.importexport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.ItemExistsException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.jdom.JDOMException;
import org.junit.*;

import com.google.common.collect.Lists;

/**
 * Unit test for import/export functionality
 * 
 * For a better overview, the test has a 3 levels deep structure of always 3 sub-pages 
 * (named child0, child1, child2) and each of them has 3 content lists having 3 mainContent 
 * nodes. And on some of them we are doing operations. 
 * 
 * Here is the tree to see, what is done:
 * 
 * home
 *   child0
 *     child0 - is copied
 *       child0
 *       child1
 *       child2
 *     child1 -> mark-for-deletion
 *       child0
 *       child1
 *       child2
 *     child2
 *       child0
 *       child1
 *       child2        
 *   child1 <-> renamed-child
 *     child0
 *       child0
 *         contentList2
 *               contentList2_text2 <-> renamed text-node
 *       child1
 *       child2        
 *       moved-page
 *       moved-ugc-page
 *     child1
 *       contentList0
 *         contentList0_text2 <-> updated title
 *       child0
 *       child1
 *       child2        
 *     child2 -> removed
 *       child0
 *       child1
 *       child2        
 *     added-page-to-renamed-page
 *     added-ugc-page-to-renamed-page (live)       
 *       contentListUGC
 *         contentListUGC0_text2 <-> updated title (live)
 *         contentListUGC0_text3 -> removed
 *         contentListUGC0_text4 -> moved before contentListUGC0_text0
 *   child2
 *     child0
 *       child0
 *       child1
 *       child2
 *     child1
 *       child0
 *         copied-node
 *           child0
 *           child1
 *           child2
 *         copied-ugc-node
 *           child0
 *           child1
 *           child2                  
 *       child1
 *       child2        
 *     child2  
 *       child0
 *       child1
 *       child2 -> ordered before child0       
 *     added-child-to-existing-subpage -> moved 
 *     added-ugc-child-to-existing-subpage (live)  -> moved 
 *   added-child
 *   added-child-with-subpage
 *     subpage
 *   added-ugc-child (live)
 *   added-ugc-child-with-subpage (live)
 *     ugc-subpage (live)
 *     
 * @author Benjamin Papez
 * 
 */
public class ImportExportTest {
    private static Logger logger = org.slf4j.LoggerFactory
            .getLogger(ImportExportTest.class);

    private final static String DEFAULT_LANGUAGE = "en";

    private final static String TESTSITE_NAME = "jcrImportExportTest";
    private final static String SITECONTENT_ROOT_NODE = "sites/"
            + TESTSITE_NAME;
    private final static String TARGET_TESTSITE_NAME = "jcrTargetImportExportTest";
    private final static String TARGET_SITECONTENT_ROOT_NODE = "sites/"
            + TARGET_TESTSITE_NAME;

    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(
                        Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters
                                .languageCodeToLocale(DEFAULT_LANGUAGE));
        initContent(session, site);
        
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession();
            if (session.nodeExists("/" + SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimpleExportImport() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {

                        exportImportAndCheck();

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    @Test
    public void testSimpleExportImportWithLive() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        final JCRPublicationService jcrService = ServicesRegistry
                                .getInstance().getJCRPublicationService();

                        jcrService.publishByMainId(session.getRootNode()
                                .getNode(SITECONTENT_ROOT_NODE)
                                .getIdentifier(), Constants.EDIT_WORKSPACE,
                                Constants.LIVE_WORKSPACE, null, true, null);
                        session.save();

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        exportImportAndCheck();

                        return null;
                    }
                });
        sf.closeAllSessions();        
    }

    @Test
    public void testExportImportWithComplexChanges() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {

                        JCRNodeWrapper englishEditSiteRootNode = session
                                .getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode
                                .getNode("home");

                        // additions
                        if (!englishEditSiteHomeNode.isCheckedOut()) {
                            session.getWorkspace()
                                    .getVersionManager()
                                    .checkout(englishEditSiteHomeNode.getPath());
                        }
                        JCRNodeWrapper newPage = englishEditSiteHomeNode
                                .addNode("added-child", "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added child");
                        
                        newPage = englishEditSiteHomeNode.addNode(
                                "added-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added child with subpage");
                        newPage = newPage.addNode("subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Subpage");
                        
                        JCRNodeWrapper childPage = englishEditSiteHomeNode
                                .getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-child-to-existing-subpage",
                                "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added child to existing subpage");
                        session.save();

                        // updates
                        childPage = englishEditSiteHomeNode.getNode("child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage.rename("renamed-child");
                        newPage = childPage.addNode("added-page-to-renamed-page",
                                "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added page to renamed page");

                        JCRNodeWrapper textNode = childPage
                                .getNode("child1/contentList0/contentList0_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(textNode.getPath());
                        }
                        textNode.setProperty("jcr:title", "updated title");
                        textNode = childPage
                                .getNode("child0/contentList2/contentList2_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(textNode.getPath());
                        }
                        textNode.rename("renamed-text-node");
                        session.save();
                        
                        // marked for deletion
                        childPage = englishEditSiteHomeNode
                                .getNode("child0/child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage
                                .markForDeletion("marked for deletion in unit test");

                        // remove
                        childPage = englishEditSiteHomeNode
                                .getNode("renamed-child");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage = englishEditSiteHomeNode
                                .getNode("renamed-child/child2");
                        childPage.remove();

                        session.save();

                        // reordering
                        childPage = englishEditSiteHomeNode
                                .getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage.orderBefore("child2", "child0");

                        // move
                        session.move(
                                englishEditSiteHomeNode
                                        .getNode(
                                                "child2/added-child-to-existing-subpage")
                                        .getPath(), englishEditSiteHomeNode
                                        .getNode("renamed-child/child0")
                                        .getPath()
                                        + "/moved-page");

                        // copy
                        childPage = englishEditSiteHomeNode
                                .getNode("child0/child0");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage.copy(englishEditSiteHomeNode
                                .getNode("child2/child1/child0"),
                                "copied-node", false);
                        session.save();                        

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        exportImportAndCheck();

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        // Now publish everything and check again
                        final JCRPublicationService jcrService = ServicesRegistry
                                .getInstance().getJCRPublicationService();                        

                        jcrService.publishByMainId(session.getRootNode()
                                .getNode(SITECONTENT_ROOT_NODE)
                                .getIdentifier(), Constants.EDIT_WORKSPACE,
                                Constants.LIVE_WORKSPACE, null, true, null);
                        session.save();
                        
                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        exportImportAndCheck();

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    @Test
    public void testExportImportWithUGCComplexChanges() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {

                        JCRNodeWrapper englishLiveSiteRootNode = session
                                .getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRNodeWrapper englishLiveSiteHomeNode = (JCRNodeWrapper) englishLiveSiteRootNode
                                .getNode("home");

                        // additions
                        if (!englishLiveSiteHomeNode.isCheckedOut()) {
                            session.getWorkspace()
                                    .getVersionManager()
                                    .checkout(englishLiveSiteHomeNode.getPath());
                        }
                        JCRNodeWrapper newPage = englishLiveSiteHomeNode.addNode("added-ugc-child",
                                "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added UGC child");
                        
                        newPage = englishLiveSiteHomeNode.addNode(
                                "added-ugc-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added UGC child with subpage");
                        newPage = newPage.addNode("ugc-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "UGC subpage");
                        
                        JCRNodeWrapper childPage = englishLiveSiteHomeNode
                                .getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-ugc-child-to-existing-subpage",
                                "jnt:page");
                        newPage.setProperty("jcr:title",
                                "Added UGC child to existing subpage");
                        session.save();

                        // updates
                        childPage = englishLiveSiteHomeNode.getNode("renamed-child");
                        JCRNodeWrapper addedNode = childPage.addNode("added-ugc-page-to-renamed-page",
                                "jnt:page");
                        addedNode.setProperty("jcr:title",
                                "Added UGC pageto renamed page");
                        
                        TestHelper.createList(addedNode, "contentListUGC", 5,
                                INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
                        
                        JCRNodeWrapper textNode = addedNode
                                .getNode("contentListUGC/contentListUGC_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(textNode.getPath());
                        }
                        textNode.setProperty("jcr:title", "updated title");
                        
                        session.save();
                        
                        // remove
                        childPage = englishLiveSiteHomeNode
                                .getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage = englishLiveSiteHomeNode
                                .getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC/contentListUGC_text3");
                        childPage.remove();

                        session.save();

                        // reordering
                        childPage = englishLiveSiteHomeNode
                                .getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage.orderBefore("contentListUGC_text4", "contentListUGC_text0");

                        // move
                        session.move(
                                englishLiveSiteHomeNode
                                        .getNode(
                                                "child2/added-ugc-child-to-existing-subpage")
                                        .getPath(), englishLiveSiteHomeNode
                                        .getNode("renamed-child/child0")
                                        .getPath()
                                        + "/moved-ugc-page");

                        // copy
                        childPage = englishLiveSiteHomeNode
                                .getNode("child0/child0");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(childPage.getPath());
                        }
                        childPage.copy(englishLiveSiteHomeNode
                                .getNode("child2/child1/child0"),
                                "copied-ugc-node", false);
                        session.save();                        

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(
                sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE),
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        exportImportAndCheck();

                        return null;
                    }
                });
        sf.closeAllSessions();
    }
    
    
    private void exportImportAndCheck()
            throws RepositoryException {
        File createdZip = exportSite();
        assertNotNull("Export failed - see console log for detailed exception",
                createdZip);
        String targetSiteName = TARGET_TESTSITE_NAME;
        JCRSessionWrapper editSession = JCRSessionFactory.getInstance()
                .getCurrentUserSession(
                        Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters
                                .languageCodeToLocale(DEFAULT_LANGUAGE));            
        try {
            importSite(createdZip, targetSiteName);
            
            boolean stagingCheck = compareNodes(
                    editSession.getNode("/" + SITECONTENT_ROOT_NODE),
                    editSession.getNode("/" + TARGET_SITECONTENT_ROOT_NODE));

            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(
                            Constants.LIVE_WORKSPACE,
                            LanguageCodeConverters
                                    .languageCodeToLocale(DEFAULT_LANGUAGE));
            boolean liveCheck = compareNodes(
                    liveSession.getNode("/" + SITECONTENT_ROOT_NODE),
                    liveSession.getNode("/" + TARGET_SITECONTENT_ROOT_NODE));

            assertTrue(
                    "Importing export to a new site does not lead to mirrored site",
                    stagingCheck);
            assertTrue(
                    "Importing export to a new site does not lead to mirrored site",
                    liveCheck);
        } finally {
            try {
                if (editSession.nodeExists("/" + TARGET_SITECONTENT_ROOT_NODE)) {
                    TestHelper.deleteSite(TARGET_TESTSITE_NAME);
                }
                editSession.save();
            } catch (Exception ex) {
                logger.warn("Exception during test tearDown", ex);
            }
        }
    }

    private File exportSite() throws RepositoryException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ImportExportService.VIEW_ACL, Boolean.TRUE);
        params.put(ImportExportService.VIEW_CONTENT, Boolean.TRUE);
        params.put(ImportExportService.VIEW_JAHIALINKS, Boolean.TRUE);
        params.put(ImportExportService.VIEW_METADATA, Boolean.TRUE);
        params.put(ImportExportService.VIEW_VERSION, Boolean.FALSE);
        params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
        params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
        params.put(
                ImportExportService.XSL_PATH,
                ((ParamBean) Jahia.getThreadParamBean()).getContext()
                        .getRealPath(
                                "/WEB-INF/etc/repository/export/cleanup.xsl"));
        ImportExportService importExportService = ServicesRegistry
                .getInstance().getImportExportService();
        File zipFile = null;

        try {
            zipFile = File.createTempFile("simpleimportexporttest", ".zip");
            OutputStream outputStream = new FileOutputStream(zipFile);
            List<JahiaSite> sites = Lists.newArrayList(ServicesRegistry
                    .getInstance().getJahiaSitesService()
                    .getSiteByKey(TESTSITE_NAME));
            importExportService.exportSites(outputStream, params, sites);
            outputStream.close();
        } catch (FileNotFoundException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (IOException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (JahiaException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (SAXException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (JDOMException e) {
            logger.error("Exception during ImportExportTest", e);
        }
        return zipFile;
    }

    private void importSite(final File zipFile, final String targetSiteName)
            throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        try {
                            TestHelper.createSite(targetSiteName, "localhost"
                                    + System.currentTimeMillis(),
                                    TestHelper.WEB_TEMPLATES,
                                    zipFile.getAbsolutePath(), TESTSITE_NAME
                                            + ".zip");
                            session.save();
                        } catch (Exception e) {
                            logger.error("Cannot create or publish site", e);
                        }
                        return null;
                    }
                });
    }

    private static void initContent(JCRSessionWrapper session, JahiaSite site) {
        try {
            JCRPublicationService jcrService = ServicesRegistry.getInstance()
                    .getJCRPublicationService();

            String defaultLanguage = site.getDefaultLanguage();

            Locale englishLocale = Locale.ENGLISH;

            JCRSessionWrapper englishEditSession = jcrService
                    .getSessionFactory().getCurrentUserSession(
                            Constants.EDIT_WORKSPACE,
                            englishLocale,
                            LanguageCodeConverters
                                    .languageCodeToLocale(defaultLanguage));
            JCRNodeWrapper englishEditSiteRootNode = englishEditSession
                    .getNode("/" + SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode
                    .getNode("home");

            TestHelper.createSubPages(englishEditSiteHomeNode, 3, 3, "Page title");
            englishEditSession.save();

            fillPagesWithLists(englishEditSiteHomeNode);
            englishEditSession.save();
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    private static void addLists(JCRNodeWrapper node) throws LockException,
            ConstraintViolationException, NoSuchNodeTypeException,
            ItemExistsException, VersionException, RepositoryException {
        for (int i = 0; i < 3; i++) {
            TestHelper.createList(node, "contentList" + i, 3,
                    INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        }
    }

    private static void fillPagesWithLists(JCRNodeWrapper node)
            throws RepositoryException {
        NodeIterator it = node.getNodes("child*");
        while (it.hasNext()) {
            fillPagesWithLists((JCRNodeWrapper) it.next());
        }
        addLists(node);
    }

    private boolean compareNodes(JCRNodeWrapper sourceSiteNode,
            JCRNodeWrapper targetSiteNode) throws RepositoryException {
        boolean matches = true;

        if (!sourceSiteNode
                .toString()
                .replace(SITECONTENT_ROOT_NODE, "")
                .equals(targetSiteNode.toString().replace(
                        TARGET_SITECONTENT_ROOT_NODE, ""))) {
            logger.error("Nodes do not match: " + sourceSiteNode.toString()
                    + " vs. " + targetSiteNode.toString());
            // assertTrue(
            // "Nodes do not match: " + sourceSiteNode.toString() + " vs. "
            // + targetSiteNode.toString(),
            // sourceSiteNode
            // .toString()
            // .replace(SITECONTENT_ROOT_NODE, "")
            // .equals(targetSiteNode.toString().replace(
            // TARGET_SITECONTENT_ROOT_NODE, "")));
            matches = false;
        }

        NodeIterator sourceSiteIt = sourceSiteNode.getNodes();
        NodeIterator targetSiteIt = targetSiteNode.getNodes();
        if (sourceSiteNode.getPath().endsWith(
                "groups/site-privileged/j:members")) {
            // TODO: here we could add a check that source has site-administrators__<source-site-id>
            // and target has site-administrators__<source-site-id> AND site-administrators__<target-site-id>

        } else if (sourceSiteNode.getPath().endsWith(
                "/groups/site-administrators/j:members")) {
            // TODO: also a special case
        } else {
            if (sourceSiteNode.isNodeType("orderable")) {
                while (sourceSiteIt.hasNext() && targetSiteIt.hasNext()) {
                    matches = matches
                            && compareNodes(
                                    (JCRNodeWrapper) sourceSiteIt.next(),
                                    (JCRNodeWrapper) targetSiteIt.next());
                }
                if (sourceSiteIt.hasNext() != targetSiteIt.hasNext()) {
                    logger.error("Number of childnodes do not match for parent nodes: "
                            + sourceSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode
                                    .getNodes()))
                            + ")"
                            + " vs. "
                            + targetSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(targetSiteNode
                                    .getNodes())) + ")");

                    // assertTrue("Number of childnodes do not match for parent nodes: "
                    // + sourceSiteNode.toString() + "("
                    // + Arrays.toString(getChildNodesArray(sourceSiteNode.getNodes())) + ")"
                    // + " vs. " + targetSiteNode.toString() + "("
                    // + Arrays.toString(getChildNodesArray(targetSiteNode.getNodes())) + ")",
                    // sourceSiteIt.hasNext() == targetSiteIt.hasNext());
                    matches = false;
                }
            } else {
                Map<String, JCRNodeWrapper> sourceChildeNodes = new TreeMap<String, JCRNodeWrapper>();
                Map<String, JCRNodeWrapper> targetChildeNodes = new TreeMap<String, JCRNodeWrapper>();

                while (sourceSiteIt.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) sourceSiteIt.next();
                    sourceChildeNodes.put(node.getName(), node);
                }
                while (targetSiteIt.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) targetSiteIt.next();
                    targetChildeNodes.put(node.getName(), node);
                }
                if (sourceChildeNodes.size() != targetChildeNodes.size()) {
                    logger.error("Number of childnodes do not match for parent nodes: "
                            + sourceSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode
                                    .getNodes()))
                            + ")"
                            + " vs. "
                            + targetSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(targetSiteNode
                                    .getNodes())) + ")");
                    matches = false;
                } else if (!sourceChildeNodes.keySet().equals(
                        targetChildeNodes.keySet())) {
                    logger.error("Childnodes do not match for parent nodes: "
                            + sourceSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode
                                    .getNodes()))
                            + ")"
                            + " vs. "
                            + targetSiteNode.toString()
                            + "("
                            + Arrays.toString(getChildNodesArray(targetSiteNode
                                    .getNodes())) + ")");
                    matches = false;
                } else {
                    for (Map.Entry<String, JCRNodeWrapper> sourceEntry : sourceChildeNodes
                            .entrySet()) {
                        matches = matches
                                && compareNodes(sourceEntry.getValue(),
                                        targetChildeNodes.get(sourceEntry
                                                .getKey()));
                    }
                }
            }

        }

        return matches;
    }

    private String[] getChildNodesArray(NodeIterator nodeIt) {
        List<String> nodeList = new ArrayList<String>();
        while (nodeIt.hasNext()) {
            nodeList.add(((JCRNodeWrapper) nodeIt.next()).getName());
        }
        return nodeList.toArray(new String[nodeList.size()]);
    }
}