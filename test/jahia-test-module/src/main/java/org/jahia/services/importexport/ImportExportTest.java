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

package org.jahia.services.importexport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.ItemExistsException;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
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
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.validation.MissingNodetypesValidationResult;
import org.jahia.services.importexport.validation.ValidationResult;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.jdom.JDOMException;
import org.junit.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Unit test for import/export functionality
 * 
 * For a better overview, the test has a 3 levels deep structure of always 3 sub-pages (named child0, child1, child2) and each of them has 3
 * content lists having 3 mainContent nodes. And on some of them we are doing operations.
 * 
 * Here is the tree to see, what is done:
 * 
 * home
 *   child0
 *     child0 - is copied
 *       child0 -> receives node-import (home/child2)
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
 *   child2 -> node export (imported to home/child0/child0/child0)
 *     child0
 *       child0
 *       child1 -> change ACL in default
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
 *       child1 -> add tag in default
 *       child2 -> add tag in live       
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
 *   added-ugc-child-with-subpage (live)  -> change ACL in live
 *     ugc-subpage (live)
 * 
 * @author Benjamin Papez
 * 
 */
public class ImportExportTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ImportExportTest.class);

    private final static String DEFAULT_LANGUAGE = "en";

    private final static String TESTSITE_NAME = "jcrImportExportTest";
    private final static String SITECONTENT_ROOT_NODE = "sites/" + TESTSITE_NAME;
    private final static String TARGET_TESTSITE_NAME = "jcrTargetImportExportTest";
    private final static String TARGET_SITECONTENT_ROOT_NODE = "sites/" + TARGET_TESTSITE_NAME;

    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
    private final static String ILLEGAL_STATE = "illegal state";

    private final static Set<String> ignoredProperties = Sets.newHashSet("jcr:baseVersion", "jcr:created", "j:deletedChildren",
            "j:installedModules", "jcr:lastModified", "jcr:lastModifiedBy", "j:lastPublished", "jcr:predecessors", "j:serverName",
            "j:siteId", "j:templateNode", "jcr:uuid", "jcr:versionHistory", "result", "j:fullpath");

    private final static Set<String> notExportedProperties = Sets.newHashSet("jcr:lockIsDeep", "j:lockTypes", "jcr:lockOwner",
            "j:locktoken");

    private final static Set<String> optionalProperties = Sets.newHashSet("jcr:mixinTypes", "j:published", "j:lastPublished",
            "j:lastPublishedBy", "j:deletedChildren", "j:fullpath");
    
    private final static Set<String> optionalMixins = Sets.newHashSet("jmix:deletedChildren");
    
    private static boolean readyForUGCTest = false; 

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
        initContent(session, site);

        JCRSessionFactory.getInstance().closeAllSessions();
        
        readyForUGCTest = false; 
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
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
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    @Test
    public void testSimpleExportImportWithLive() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

                        jcrService.publishByMainId(session.getRootNode().getNode(SITECONTENT_ROOT_NODE).getIdentifier(),
                                Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                        session.save();

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    @Test
    public void testExportImportWithComplexChanges() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                        JCRNodeWrapper englishEditSiteRootNode = session.getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

                        // additions
                        if (!englishEditSiteHomeNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(englishEditSiteHomeNode.getPath());
                        }
                        JCRNodeWrapper newPage = englishEditSiteHomeNode.addNode("added-child", "jnt:page");
                        newPage.setProperty("jcr:title", "Added child");

                        newPage = englishEditSiteHomeNode.addNode("added-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added child with subpage");
                        newPage = newPage.addNode("subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Subpage");

                        JCRNodeWrapper childPage = englishEditSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-child-to-existing-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added child to existing subpage");
                        session.save();

                        // updates
                        childPage = englishEditSiteHomeNode.getNode("child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.rename("renamed-child");
                        newPage = childPage.addNode("added-page-to-renamed-page", "jnt:page");
                        newPage.setProperty("jcr:title", "Added page to renamed page");

                        JCRNodeWrapper textNode = childPage.getNode("child1/contentList0/contentList0_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(textNode.getPath());
                        }
                        textNode.setProperty("jcr:title", "updated title");
                        textNode = childPage.getNode("child0/contentList2/contentList2_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(textNode.getPath());
                        }
                        textNode.rename("renamed-text-node");
                        session.save();

                        // marked for deletion
                        childPage = englishEditSiteHomeNode.getNode("child0/child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.markForDeletion("marked for deletion in unit test");

                        // remove
                        childPage = englishEditSiteHomeNode.getNode("renamed-child");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage = englishEditSiteHomeNode.getNode("renamed-child/child2");
                        childPage.remove();

                        session.save();

                        // reordering
                        childPage = englishEditSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.orderBefore("child2", "child0");

                        // move
                        session.move(englishEditSiteHomeNode.getNode("child2/added-child-to-existing-subpage").getPath(),
                                englishEditSiteHomeNode.getNode("renamed-child/child0").getPath() + "/moved-page");

                        // copy
                        childPage = englishEditSiteHomeNode.getNode("child0/child0");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.copy(englishEditSiteHomeNode.getNode("child2/child1/child0"), "copied-node", false);
                        session.save();

                        // set ACL
                        childPage = englishEditSiteHomeNode.getNode("child2/child0/child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
                        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

                        JahiaUser user1 = userManager.createUser("impexpuser", "password", new Properties());
                        user1 = userManager.createUser("user1", "password", new Properties());
                        childPage.setAclInheritanceBreak(true);
                        childPage.grantRoles("u:" + user1.getUsername(), Sets.newHashSet("owner"));

                        // set tag
                        TaggingService tagService = (TaggingService) SpringContextSingleton
                                .getBean("org.jahia.services.tags.TaggingService");

                        childPage = englishEditSiteHomeNode.getNode("child2/child1/child1");
                        childPage.addMixin("jmix:tagged");
                        tagService.tag(childPage.getPath(), "impexptag", TESTSITE_NAME, true);
                        session.save();
                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        // Now publish everything and check again
                        final JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

                        jcrService.publishByMainId(session.getRootNode().getNode(SITECONTENT_ROOT_NODE).getIdentifier(),
                                Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                        session.save();

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
        
        readyForUGCTest = true;
    }

    @Test
    public void testExportImportWithUGCComplexChanges() throws Exception {
        assertTrue("UGC tests cannot be executed, because a dependend test case failed before", readyForUGCTest);
        
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                        JCRNodeWrapper englishLiveSiteRootNode = session.getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRNodeWrapper englishLiveSiteHomeNode = (JCRNodeWrapper) englishLiveSiteRootNode.getNode("home");

                        // additions
                        if (!englishLiveSiteHomeNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(englishLiveSiteHomeNode.getPath());
                        }
                        JCRNodeWrapper newPage = englishLiveSiteHomeNode.addNode("added-ugc-child", "jnt:page");
                        newPage.setProperty("jcr:title", "Added UGC child");

                        newPage = englishLiveSiteHomeNode.addNode("added-ugc-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added UGC child with subpage");
                        newPage = newPage.addNode("ugc-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "UGC subpage");

                        JCRNodeWrapper childPage = englishLiveSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-ugc-child-to-existing-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added UGC child to existing subpage");
                        session.save();

                        // updates
                        childPage = englishLiveSiteHomeNode.getNode("renamed-child");
                        JCRNodeWrapper addedNode = childPage.addNode("added-ugc-page-to-renamed-page", "jnt:page");
                        addedNode.setProperty("jcr:title", "Added UGC pageto renamed page");

                        TestHelper.createList(addedNode, "contentListUGC", 5, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);

                        JCRNodeWrapper textNode = addedNode.getNode("contentListUGC/contentListUGC_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(textNode.getPath());
                        }
                        textNode.setProperty("jcr:title", "updated title");

                        session.save();

                        // remove
                        childPage = englishLiveSiteHomeNode.getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage = englishLiveSiteHomeNode
                                .getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC/contentListUGC_text3");
                        childPage.remove();

                        session.save();

                        // reordering
                        childPage = englishLiveSiteHomeNode.getNode("renamed-child/added-ugc-page-to-renamed-page/contentListUGC");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.orderBefore("contentListUGC_text4", "contentListUGC_text0");

                        // move
                        session.move(englishLiveSiteHomeNode.getNode("child2/added-ugc-child-to-existing-subpage").getPath(),
                                englishLiveSiteHomeNode.getNode("renamed-child/child0").getPath() + "/moved-ugc-page");

                        // copy
                        childPage = englishLiveSiteHomeNode.getNode("child0/child0");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.copy(englishLiveSiteHomeNode.getNode("child2/child1/child0"), "copied-ugc-node", false);
                        session.save();

                        // set ACL
                        childPage = englishLiveSiteHomeNode.getNode("added-ugc-child-with-subpage");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
                        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

                        JahiaUser user2 = userManager.createUser("impexpuserugc", "password", new Properties());
                        childPage.setAclInheritanceBreak(true);
                        childPage.grantRoles("u:" + user2.getUsername(), Sets.newHashSet("owner"));

                        // set tag
                        TaggingService tagService = (TaggingService) SpringContextSingleton
                                .getBean("org.jahia.services.tags.TaggingService");

                        childPage = englishLiveSiteHomeNode.getNode("child2/child1/child2");
                        childPage.addMixin("jmix:tagged");
                        tagService.tag(childPage.getPath(), "impexptagugc", TESTSITE_NAME, true);
                        session.save();
                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    private void exportImportAndCheck(JCRSessionWrapper editSession) throws RepositoryException {
        File createdZip = exportSite();
        assertNotNull("Export failed - see console log for detailed exception", createdZip);
        String targetSiteName = TARGET_TESTSITE_NAME;

        try {
            importSite(createdZip, targetSiteName);

            boolean stagingCheck = compareNodes(editSession.getNode("/" + SITECONTENT_ROOT_NODE),
                    editSession.getNode("/" + TARGET_SITECONTENT_ROOT_NODE), "/" + SITECONTENT_ROOT_NODE, "/"
                            + TARGET_SITECONTENT_ROOT_NODE);

            JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
            boolean liveCheck = compareNodes(liveSession.getNode("/" + SITECONTENT_ROOT_NODE),
                    liveSession.getNode("/" + TARGET_SITECONTENT_ROOT_NODE), "/" + SITECONTENT_ROOT_NODE, "/"
                            + TARGET_SITECONTENT_ROOT_NODE);

            assertTrue("Importing export to a new site does not lead to mirrored site", stagingCheck);
            assertTrue("Importing export to a new site does not lead to mirrored site", liveCheck);
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
        params.put(ImportExportService.XSL_PATH,
                ((ParamBean) Jahia.getThreadParamBean()).getContext().getRealPath("/WEB-INF/etc/repository/export/cleanup.xsl"));
        ImportExportService importExportService = ServicesRegistry.getInstance().getImportExportService();
        File zipFile = null;

        try {
            zipFile = File.createTempFile("simpleimportexporttest", ".zip");
            OutputStream outputStream = new FileOutputStream(zipFile);
            List<JahiaSite> sites = Lists.newArrayList(ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(TESTSITE_NAME));
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

    private void importSite(final File zipFile, final String targetSiteName) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    TestHelper.createSite(targetSiteName, "localhost" + System.currentTimeMillis(), TestHelper.WEB_TEMPLATES,
                            zipFile.getAbsolutePath(), TESTSITE_NAME + ".zip");
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
            JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

            String defaultLanguage = site.getDefaultLanguage();

            Locale englishLocale = Locale.ENGLISH;

            JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                    englishLocale, LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
            JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode("/" + SITECONTENT_ROOT_NODE);
            JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

            TestHelper.createSubPages(englishEditSiteHomeNode, 3, 3, "Page title");
            englishEditSession.save();

            fillPagesWithLists(englishEditSiteHomeNode);
            englishEditSession.save();
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        }
    }

    private static void addLists(JCRNodeWrapper node) throws LockException, ConstraintViolationException, NoSuchNodeTypeException,
            ItemExistsException, VersionException, RepositoryException {
        for (int i = 0; i < 3; i++) {
            TestHelper.createList(node, "contentList" + i, 3, INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE);
        }
    }

    private static void fillPagesWithLists(JCRNodeWrapper node) throws RepositoryException {
        NodeIterator it = node.getNodes("child*");
        while (it.hasNext()) {
            fillPagesWithLists((JCRNodeWrapper) it.next());
        }
        addLists(node);
    }

    private boolean compareNodes(JCRNodeWrapper sourceSiteNode, JCRNodeWrapper targetSiteNode, String sourceRootPath, String targetRootPath)
            throws RepositoryException {
        boolean matches = true;

        if (!sourceSiteNode.toString().replace(sourceRootPath, "").equals(targetSiteNode.toString().replace(targetRootPath, ""))) {
            logger.error("Nodes do not match: " + sourceSiteNode.toString() + " vs. " + targetSiteNode.toString());
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

        matches = matches && compareProperties(sourceSiteNode, targetSiteNode, sourceRootPath, targetRootPath);

        NodeIterator sourceSiteIt = sourceSiteNode.getNodes();
        NodeIterator targetSiteIt = targetSiteNode.getNodes();
        if (sourceSiteNode.getPath().endsWith("groups/site-privileged/j:members")) {
            // TODO: here we could add a check that source has site-administrators__<source-site-id>
            // and target has site-administrators__<source-site-id> AND site-administrators__<target-site-id>

        } else if (sourceSiteNode.getPath().endsWith("/groups/site-administrators/j:members")) {
            // TODO: also a special case
        } else {
            if (sourceSiteNode.isNodeType("orderable")) {
                while (sourceSiteIt.hasNext() && targetSiteIt.hasNext()) {
                    matches = matches
                            && compareNodes((JCRNodeWrapper) sourceSiteIt.next(), (JCRNodeWrapper) targetSiteIt.next(), sourceRootPath,
                                    targetRootPath);
                }
                if (sourceSiteIt.hasNext() != targetSiteIt.hasNext()) {
                    logger.error("Number of childnodes do not match for parent nodes: " + sourceSiteNode.toString() + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode.getNodes())) + ")" + " vs. " + targetSiteNode.toString()
                            + "(" + Arrays.toString(getChildNodesArray(targetSiteNode.getNodes())) + ")");

                    // assertTrue("Number of childnodes do not match for parent nodes: "
                    // + sourceSiteNode.toString() + "("
                    // + Arrays.toString(getChildNodesArray(sourceSiteNode.getNodes())) + ")"
                    // + " vs. " + targetSiteNode.toString() + "("
                    // + Arrays.toString(getChildNodesArray(targetSiteNode.getNodes())) + ")",
                    // sourceSiteIt.hasNext() == targetSiteIt.hasNext());
                    matches = false;
                }
            } else {
                Map<String, JCRNodeWrapper> sourceChildNodes = new TreeMap<String, JCRNodeWrapper>();
                Map<String, JCRNodeWrapper> targetChildNodes = new TreeMap<String, JCRNodeWrapper>();

                while (sourceSiteIt.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) sourceSiteIt.next();
                    sourceChildNodes.put(node.getName(), node);
                }
                while (targetSiteIt.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) targetSiteIt.next();
                    targetChildNodes.put(node.getName(), node);
                }
                if (sourceChildNodes.size() != targetChildNodes.size()) {
                    logger.error("Number of childnodes do not match for parent nodes: " + sourceSiteNode.toString() + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode.getNodes())) + ")" + " vs. " + targetSiteNode.toString()
                            + "(" + Arrays.toString(getChildNodesArray(targetSiteNode.getNodes())) + ")");
                    matches = false;
                } else if (!sourceChildNodes.keySet().equals(targetChildNodes.keySet())) {
                    logger.error("Childnodes do not match for parent nodes: " + sourceSiteNode.toString() + "("
                            + Arrays.toString(getChildNodesArray(sourceSiteNode.getNodes())) + ")" + " vs. " + targetSiteNode.toString()
                            + "(" + Arrays.toString(getChildNodesArray(targetSiteNode.getNodes())) + ")");
                    matches = false;
                } else {
                    for (Map.Entry<String, JCRNodeWrapper> sourceEntry : sourceChildNodes.entrySet()) {
                        matches = matches
                                && compareNodes(sourceEntry.getValue(), targetChildNodes.get(sourceEntry.getKey()), sourceRootPath,
                                        targetRootPath);
                    }
                }
            }

        }

        return matches;
    }

    private boolean compareProperties(JCRNodeWrapper sourceSiteNode, JCRNodeWrapper targetSiteNode, String sourceRootPath,
            String targetRootPath) throws RepositoryException {
        boolean matches = true;
        PropertyIterator sourceSiteIt = sourceSiteNode.getProperties();
        PropertyIterator targetSiteIt = targetSiteNode.getProperties();

        Map<String, JCRPropertyWrapper> sourceProperties = new TreeMap<String, JCRPropertyWrapper>();
        Map<String, JCRPropertyWrapper> targetProperties = new TreeMap<String, JCRPropertyWrapper>();

        while (sourceSiteIt.hasNext()) {
            JCRPropertyWrapper property = (JCRPropertyWrapper) sourceSiteIt.next();
            sourceProperties.put(property.getName(), property);
        }
        while (targetSiteIt.hasNext()) {
            JCRPropertyWrapper property = (JCRPropertyWrapper) targetSiteIt.next();
            targetProperties.put(property.getName(), property);
        }

        if (sourceProperties.size() != targetProperties.size()) {
            for (String optionalProperty : optionalProperties) {
                if (sourceProperties.containsKey(optionalProperty) && !targetProperties.containsKey(optionalProperty)) {
                    sourceProperties.remove(optionalProperty);
                } else if (!sourceProperties.containsKey(optionalProperty) && targetProperties.containsKey(optionalProperty)) {
                    targetProperties.remove(optionalProperty);
                }
            }
            for (String propertyToRemove : notExportedProperties) {
                sourceProperties.remove(propertyToRemove);
            }
        }
        if (sourceProperties.size() != targetProperties.size()) {
            logger.error("Number of properties do not match for nodes: " + sourceSiteNode.toString() + "("
                    + Arrays.toString(getPropertiesArray(sourceSiteNode.getProperties())) + ")" + " vs. " + targetSiteNode.toString() + "("
                    + Arrays.toString(getPropertiesArray(targetSiteNode.getProperties())) + ")");
            matches = false;
        } else if (!sourceProperties.keySet().equals(targetProperties.keySet())) {
            logger.error("Properties do not match for nodes: " + sourceSiteNode.toString() + "("
                    + Arrays.toString(getPropertiesArray(sourceSiteNode.getProperties())) + ")" + " vs. " + targetSiteNode.toString() + "("
                    + Arrays.toString(getPropertiesArray(targetSiteNode.getProperties())) + ")");
            matches = false;
        } else {
            for (Map.Entry<String, JCRPropertyWrapper> sourceEntry : sourceProperties.entrySet()) {
                // ignored properties
                if (ignoredProperties.contains(sourceEntry.getKey())) {
                    continue;
                }

                JCRPropertyWrapper sourceProperty = sourceEntry.getValue();
                JCRPropertyWrapper targetProperty = targetProperties.get(sourceEntry.getKey());
                boolean isReference = sourceProperty.getDefinition().getRequiredType() == PropertyType.REFERENCE || sourceProperty
                        .getDefinition().getRequiredType() == PropertyType.WEAKREFERENCE;
                if (sourceProperty.isMultiple() && targetProperty.isMultiple()) {
                    Set<Value> sourceValues = Sets.newHashSet(sourceProperty.getValues());
                    Set<Value> targetValues = Sets.newHashSet(targetProperty.getValues());
                    if (sourceValues.size() != targetValues.size() && "jcr:mixinTypes".equals(sourceProperty.getDefinition().getName())) {
                        Iterator<Value> it = sourceValues.iterator();
                        while (it.hasNext()) {
                            if (optionalMixins.contains(it.next().getString())) {
                                it.remove();
                            }
                        }
                        it = targetValues.iterator();
                        while (it.hasNext()) {
                            if (optionalMixins.contains(it.next().getString())) {
                                it.remove();
                            }
                        }
                    }
                    if (sourceValues.size() != targetValues.size() 
                            || (isReference && !compareReferenceValues(
                                    sourceValues, targetValues, sourceSiteNode.getSession(), targetSiteNode.getSession()))
                            || (!isReference && !compareArrayValues(sourceValues, targetValues))) {
                        logger.error("Property values do not match for property "
                                + sourceProperty.getName()
                                + " of nodes: "
                                + sourceSiteNode.toString()
                                + "("
                                + Arrays.toString(isReference ? getReferenceArray(
                                        sourceProperty.getValues(), sourceSiteNode.getSession())
                                        : getValueArray(sourceProperty.getValues()))
                                + ")"
                                + " vs. "
                                + targetSiteNode.toString()
                                + "("
                                + Arrays.toString(isReference ? getReferenceArray(
                                        targetProperty.getValues(), targetSiteNode.getSession())
                                        : getValueArray(targetProperty.getValues())) + ")");
                        matches = false;
                    }
                } else if (sourceProperty.isMultiple() != targetProperty.isMultiple()) {
                    logger.error("Property value sizes do not match for property "
                            + sourceProperty.getName()
                            + " of nodes: "
                            + sourceSiteNode.toString()
                            + "("
                            + ((isReference ? (sourceProperty.isMultiple() ? Arrays
                                    .toString(getReferenceArray(sourceProperty.getValues(), sourceSiteNode.getSession())) : sourceSiteNode
                                    .getSession().getNodeByUUID(sourceProperty.getValue().getString()).getPath())
                                    : (sourceProperty.isMultiple() ? Arrays.toString(getValueArray(sourceProperty.getValues()))
                                            : sourceProperty.getValue().getString())))
                            + ")"
                            + " vs. "
                            + targetSiteNode.toString()
                            + "("
                            + ((isReference ? (targetProperty.isMultiple() ? Arrays
                                    .toString(getReferenceArray(targetProperty.getValues(), targetSiteNode.getSession())) : targetSiteNode
                                    .getSession().getNodeByUUID(targetProperty.getValue().getString()).getPath())
                                    : (targetProperty.isMultiple() ? Arrays.toString(getValueArray(targetProperty.getValues()))
                                            : targetProperty.getValue().getString()))) + ")");
                    matches = false;
                } else {
                    Object sourceValue = sourceProperty.getValue();
                    Object targetValue = targetProperty.getValue();
                    String sourceReferencePath = "";
                    String targetReferencePath = "";
                    if ("j:fullpath".equals(sourceEntry.getKey()) || "j:nodename".equals(sourceEntry.getKey())
                            || "j:title".equals(sourceEntry.getKey())) {
                        sourceValue = ((Value)sourceValue).getString().replace(sourceRootPath, "");
                        targetValue = ((Value)targetValue).getString().replace(targetRootPath, "");
                        sourceValue = ((String)sourceValue).replace(TESTSITE_NAME, "");
                        targetValue = ((String)targetValue).replace(TARGET_TESTSITE_NAME, "");
                    } else if (isReference) {
                        try {
                            sourceReferencePath = sourceSiteNode.getSession().getNodeByUUID(sourceProperty.getValue().getString())
                                    .getPath();
                            sourceValue = sourceReferencePath;
                            sourceValue = ((String)sourceValue).replace(sourceRootPath, "");
                            sourceValue = ((String)sourceValue).replace(TESTSITE_NAME, "");
                        } catch (Exception e) {
                            logger.warn(sourceProperty.getPath() + "'s value leads to an exception");
                            sourceReferencePath = ILLEGAL_STATE;
                            sourceValue = ILLEGAL_STATE;
                        }

                        try {
                            targetReferencePath = targetSiteNode.getSession().getNodeByUUID(targetProperty.getValue().getString())
                                    .getPath();
                            targetValue = targetReferencePath;
                            targetValue = ((String)targetValue).replace(targetRootPath, "");
                            targetValue = ((String)targetValue).replace(TARGET_TESTSITE_NAME, "");
                        } catch (Exception e) {
                            logger.warn(targetProperty.getPath() + "'s value leads to an exception");
                            targetReferencePath = ILLEGAL_STATE;
                            targetValue = ILLEGAL_STATE;
                        }
                    }
                    if (!sourceValue.equals(targetValue)) {
                        logger.error("Property value does not match for property "
                                + sourceProperty.getName()
                                + " of nodes: "
                                + sourceSiteNode.toString()
                                + "("
                                + (isReference ? sourceReferencePath
                                        : sourceProperty.getValue().getString())
                                + ")"
                                + " vs. "
                                + targetSiteNode.toString()
                                + "("
                                + (isReference ? targetReferencePath
                                        : targetProperty.getValue().getString()) + ")");
                        matches = false;
                    }
                }
            }
        }

        return matches;
    }

    private boolean compareArrayValues(Set<Value> sourceValues, Set<Value> targetValues) {
        boolean match = true;

        for (Iterator<Value> it = sourceValues.iterator(); match && it.hasNext(); ) {
            match = targetValues.contains(it.next());
        }
        return match;
    }

    private boolean compareReferenceValues(Set<Value> sourceValues, Set<Value> targetValues, JCRSessionWrapper sourceSession,
            JCRSessionWrapper targetSession) {
        boolean match = true;
        Set<String> sourceReferences = new HashSet<String>();
        Set<String> targetReferences = new HashSet<String>();
        for (Value sourceValue : sourceValues) {
            String sourceReference = ILLEGAL_STATE;
            try {
                sourceReference = sourceSession.getNodeByUUID(sourceValue.getString()).getPath().replace(TESTSITE_NAME, "").replace(TARGET_TESTSITE_NAME, "");
            } catch (Exception e) {
            }
            sourceReferences.add(sourceReference);
        }
        for (Value targetValue : targetValues) {
            String targetReference = ILLEGAL_STATE;
            try {
                targetReference = targetSession.getNodeByUUID(targetValue.getString()).getPath().replace(TESTSITE_NAME, "").replace(TARGET_TESTSITE_NAME, "");
            } catch (Exception e) {
            }
            targetReferences.add(targetReference);
        }
        for (Iterator<String> it = sourceReferences.iterator(); match && it.hasNext();) {
            match = targetReferences.contains(it.next());
        }

        return match;
    }

    private String[] getValueArray(Value[] values) {
        String[] valueStrings = new String[values.length];
        int i = 0;
        for (Value value : values) {
            try {
                valueStrings[i++] = value.getString();
            } catch (Exception e) {
                valueStrings[i++] = ILLEGAL_STATE;
            }
        }
        return valueStrings;
    }

    private String[] getReferenceArray(Value[] values, JCRSessionWrapper session) {
        String[] valueStrings = new String[values.length];
        int i = 0;
        for (Value value : values) {
            try {
                valueStrings[i] = session.getNodeByUUID(value.getString()).getPath();
            } catch (Exception e) {
                valueStrings[i] = ILLEGAL_STATE;
            } finally {
                i++;
            }
        }
        return valueStrings;
    }

    private String[] getChildNodesArray(NodeIterator nodeIt) {
        List<String> nodeList = new ArrayList<String>();
        while (nodeIt.hasNext()) {
            nodeList.add(((JCRNodeWrapper) nodeIt.next()).getName());
        }
        return nodeList.toArray(new String[nodeList.size()]);
    }

    private String[] getPropertiesArray(PropertyIterator propsIt) {
        List<String> propsList = new ArrayList<String>();
        while (propsIt.hasNext()) {
            try {
                propsList.add(((JCRPropertyWrapper) propsIt.next()).getName());
            } catch (RepositoryException e) {
            }
        }
        return propsList.toArray(new String[propsList.size()]);
    }

    @Test
    public void testNodeExportImportWithLive() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRSessionWrapper sessionNoLocale = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE);
                        JCRNodeWrapper englishLiveSiteRootNode = sessionNoLocale.getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRNodeWrapper englishLiveSiteHomeNode = (JCRNodeWrapper) englishLiveSiteRootNode.getNode("home");

                        nodeExportImportAndCheck(session, englishLiveSiteHomeNode.getNode("child2"), "/" + SITECONTENT_ROOT_NODE
                                + "/home/child0/child0/child0");

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    private void nodeExportImportAndCheck(JCRSessionWrapper editSession, JCRNodeWrapper node, String parentPath) throws RepositoryException {
        File createdZip = exportNode(node);
        assertNotNull("Export failed - see console log for detailed exception", createdZip);

        performNodeImport(createdZip, parentPath);

        boolean stagingCheck = compareNodes(editSession.getNode(node.getPath()), editSession.getNode(parentPath + "/" + node.getName()),
                node.getPath(), parentPath + "/" + node.getName());

        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
        boolean liveCheck = compareNodes(liveSession.getNode(node.getPath()), liveSession.getNode(parentPath + "/" + node.getName()),
                node.getPath(), parentPath + "/" + node.getName());

        assertTrue("Importing export to a new node does not lead to exact mirror", stagingCheck);
        assertTrue("Importing export to a new node does not lead to exact mirror", liveCheck);

    }

    private File exportNode(JCRNodeWrapper node) throws RepositoryException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
        params.put(ImportExportService.VIEW_CONTENT, Boolean.TRUE);
        params.put(ImportExportService.VIEW_JAHIALINKS, Boolean.TRUE);
        params.put(ImportExportService.VIEW_ACL, Boolean.TRUE);
        params.put(ImportExportService.VIEW_METADATA, Boolean.TRUE);
        params.put(ImportExportService.VIEW_VERSION, Boolean.FALSE);
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.FALSE);
        params.put(ImportExportService.XSL_PATH,
                ((ParamBean) Jahia.getThreadParamBean()).getContext().getRealPath("/WEB-INF/etc/repository/export/cleanup.xsl"));
        ImportExportService importExportService = ServicesRegistry.getInstance().getImportExportService();
        File zipFile = null;

        try {
            zipFile = File.createTempFile("nodeimportexporttest", ".zip");
            OutputStream outputStream = new FileOutputStream(zipFile);
            importExportService.exportZip(node, null, outputStream, params);
            outputStream.close();

        } catch (FileNotFoundException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (IOException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (SAXException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (JDOMException e) {
            logger.error("Exception during ImportExportTest", e);
        }
        return zipFile;
    }

    private void performNodeImport(final File zipFile, final String parentPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
                    importExport.importZip(parentPath, zipFile, DocumentViewImportHandler.ROOT_BEHAVIOUR_RENAME);
                    session.save();
                } catch (Exception e) {
                    logger.error("Cannot create or publish site", e);
                }
                return null;
            }
        });
    }
    
    @Test
    public void testImportValidation() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithUserSession(sf.getCurrentUser().getName(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper englishSiteRootNode = session.getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRSiteNode site = englishSiteRootNode.getResolveSite();
                        ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
                        String prepackedZIPFile = SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites/webtemplates.zip";
                        String siteZIPName = "ACME.zip";
                        File siteZIPFile = null;
                        try {
                            ZipInputStream zis = null;
                            OutputStream os = null;
                            try {
                                zis = new ZipInputStream(new FileInputStream(new File(prepackedZIPFile)));
                                ZipEntry z = null;
                                while ((z = zis.getNextEntry()) != null) {
                                    if (siteZIPName.equalsIgnoreCase(z.getName())) {
                                        File zipFile = File.createTempFile("import", ".zip");
                                        os = new FileOutputStream(zipFile);
                                        byte[] buf = new byte[4096];
                                        int r;
                                        while ((r = zis.read(buf)) > 0) {
                                            os.write(buf, 0, r);
                                        }
                                        os.close();

                                        siteZIPFile = zipFile;
                                    }
                                }
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            } finally {
                                if (os != null) {
                                    try {
                                        os.close();
                                    } catch (IOException e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                                if (zis != null) {
                                    try {
                                        zis.close();
                                    } catch (IOException e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            }

                            NoCloseZipInputStream noCloseZis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(
                                    siteZIPFile)));
                            try {
                                while (true) {
                                    org.jahia.utils.zip.ZipEntry zipentry = noCloseZis.getNextEntry();
                                    if (zipentry == null)
                                        break;
                                    String name = zipentry.getName();
                                    if (name.equals("repository.xml")) {
                                        ValidationResults results = importExport.validateImportFile(session, noCloseZis, "application/xml",
                                                site.getInstalledModules());
                                        List<ValidationResult> valResults = results.getResults();
                                        assertTrue("No validation errors found although there should be some", valResults.size() > 0);
                                        
                                        for (ValidationResult result : valResults) {
                                            if (!result.isSuccessful()) {
                                                if (result instanceof MissingNodetypesValidationResult) {
                                                    assertEquals("There should be 4 missing nodetypes", 4, ((MissingNodetypesValidationResult)result).getMissingNodetypes().size());
                                                    assertEquals("There should be 1 missing mixin", 1, ((MissingNodetypesValidationResult)result).getMissingMixins().size());
                                                } 
                                            }
                                        }
                                    }
                                }
                            } finally {
                                noCloseZis.reallyClose();
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            if (siteZIPFile != null) {
                                siteZIPFile.delete();
                            }
                        }
                        return null;
                    }
                });
        sf.closeAllSessions();
    }
}