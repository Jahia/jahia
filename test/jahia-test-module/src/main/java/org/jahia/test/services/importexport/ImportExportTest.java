/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.importexport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.xml.transform.TransformerException;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
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
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.importexport.validation.MissingNodetypesValidationResult;
import org.jahia.services.importexport.validation.ValidationResult;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
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
            "j:installedModules", "jcr:lastModified", "jcr:lastModifiedBy", Constants.LASTPUBLISHED, "jcr:predecessors", "j:serverName",
            "j:siteId", "j:templateNode", "jcr:uuid", "jcr:versionHistory", "result", "j:fullpath", "j:allowsUnlistedLanguages");

    private final static Set<String> notExportedProperties = Sets.newHashSet("jcr:lockIsDeep", "j:lockTypes", "jcr:lockOwner",
            "j:locktoken");

    // j:fullpath is deprecated
    private final static Set<String> optionalProperties = Sets.newHashSet("jcr:mixinTypes", Constants.PUBLISHED, Constants.LASTPUBLISHED,
            Constants.LASTPUBLISHEDBY, "j:deletedChildren", "j:fullpath", "j:allowsUnlistedLanguages");

    private final static Set<String> optionalNodes = Sets.newHashSet("templates");

    private final static Set<String> optionalMixins = Sets.newHashSet("jmix:deletedChildren");

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
        initContent(session, site);

        JCRSessionFactory.getInstance().closeAllSessions();
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
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
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
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
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
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                }
        );
        sf.closeAllSessions();
    }

    @Test
    public void testExportImportWithComplexChanges() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
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
                        newPage.setProperty("j:templateName", "simple");

                        newPage = englishEditSiteHomeNode.addNode("added-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added child with subpage");
                        newPage.setProperty("j:templateName", "simple");
                        newPage = newPage.addNode("subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Subpage");
                        newPage.setProperty("j:templateName", "simple");

                        JCRNodeWrapper childPage = englishEditSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-child-to-existing-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added child to existing subpage");
                        newPage.setProperty("j:templateName", "simple");
                        session.save();

                        // updates
                        childPage = englishEditSiteHomeNode.getNode("child1");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        childPage.rename("renamed-child");
                        newPage = childPage.addNode("added-page-to-renamed-page", "jnt:page");
                        newPage.setProperty("jcr:title", "Added page to renamed page");
                        newPage.setProperty("j:templateName", "simple");

                        JCRNodeWrapper textNode = childPage.getNode("contentList0/contentList0_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(textNode.getPath());
                        }
                        textNode.setProperty("jcr:title", "updated title");
                        textNode = childPage.getNode("contentList2/contentList2_text2");
                        if (!textNode.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(textNode.getPath());
                        }
                        textNode.rename("renamed-text-node");
                        session.save();


                        // set ACL
                        childPage = englishEditSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
                        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

                        JCRUserNode user1 = userManager.createUser("impexpuser", "password", new Properties(), session);
                        user1 = userManager.createUser("user1", "password", new Properties(), session);
                        childPage.setAclInheritanceBreak(true);
                        childPage.grantRoles("u:" + user1.getName(), Sets.newHashSet("owner"));

                        // set tag
                        TaggingService tagService = (TaggingService) SpringContextSingleton
                                .getBean("org.jahia.services.tags.TaggingService");

                        childPage = englishEditSiteHomeNode.getNode("child2");
                        childPage.addMixin("jmix:tagged");
                        tagService.tag(childPage, "impexptag");
                        session.save();
                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
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
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();

        testExportImportWithUGCComplexChanges();
    }

    @Test
    public void testImportValidation() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper englishSiteRootNode = session.getNode("/" + SITECONTENT_ROOT_NODE);
                        JCRSiteNode site = englishSiteRootNode.getResolveSite();
                        ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
                        String prepackedZIPFile = SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites/acme.zip";
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
                                    ZipEntry zipentry = noCloseZis.getNextEntry();
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
                                                    assertEquals("There should be 4 missing nodetypes", 4, ((MissingNodetypesValidationResult) result).getMissingNodetypes().size());
                                                    assertEquals("There should be 1 missing mixin", 1, ((MissingNodetypesValidationResult) result).getMissingMixins().size());
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

    @Test
    public void testExportWithNonExportableContent() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        AtomicReference<String> userPath = new AtomicReference<>("");
        sf.closeAllSessions();
        try {
            JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), session -> {
                        // Delete user if any.
                        Session jcrSession = session.getProviderSession((session).getNode("/").getProvider());
                        deleteTestUser("external-user", session, jcrSession);
                        // Create user and content.
                        JCRUserNode user = JahiaUserManagerService.getInstance().createUser("external-user", "password", new Properties(), session);
                        JCRNodeWrapper text = session.getRootNode().getNode(SITECONTENT_ROOT_NODE).addNode("text", "jnt:text");
                        text.grantRoles("u:" + user.getName(), Collections.singleton("editor"));
                        JCRNodeWrapper userText = user.addNode("text", "jnt:text");
                        session.save();

                        // create a reference from the user node into the site
                        JCRNodeWrapper ref = session.getRootNode().getNode(SITECONTENT_ROOT_NODE).addNode("reference", "jnt:text");
                        ref.addMixin("jmix:internalLink");
                        ref.setProperty("j:linknode", userText);
                        // Set user node as external
                        Node jcrUserNode = jcrSession.getNode(user.getPath());
                        userPath.set(jcrUserNode.getPath());
                        jcrUserNode.addMixin("jmix:externalProviderExtension");
                        jcrUserNode.setProperty("j:isExternalProviderRoot", false);
                        jcrSession.save();
                        return null;
                    });
            sf.closeAllSessions();
            File createdZip = exportSite(TESTSITE_NAME);
            assertNotNull("Export failed - see console log for detailed exception", createdZip);

        } finally {
            // clean up data
            JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), session -> {
                        Session jcrSession = session.getProviderSession((session).getNode("/").getProvider());
                        deleteTestUser("external-user", session, jcrSession);
                        if (session.nodeExists("/" + SITECONTENT_ROOT_NODE + "/text")) {
                            session.getNode("/" + SITECONTENT_ROOT_NODE + "/text").remove();
                        }
                        if (session.nodeExists("/" + SITECONTENT_ROOT_NODE + "/reference")) {
                            session.getNode("/" + SITECONTENT_ROOT_NODE + "/reference").remove();
                        }
                        session.save();
                        return null;
                    });
            sf.closeAllSessions();
        }
    }

    private void deleteTestUser(String userName, JCRSessionWrapper session, Session jcrSession) throws RepositoryException {
        if (JahiaUserManagerService.getInstance().userExists(userName)) {
            JCRUserNode user = JahiaUserManagerService.getInstance().lookupUser(userName);
            Node jcrUserNode = jcrSession.getNode(user.getPath());
            if (jcrUserNode.isNodeType("jmix:externalProviderExtension")) {
                jcrUserNode.removeMixin("jmix:externalProviderExtension");
            }
            if (jcrUserNode.hasProperty("j:isExternalProviderRoot")) {
                jcrUserNode.getProperty("j:isExternalProviderRoot").remove();
            }
            jcrSession.save();
            JahiaUserManagerService.getInstance().deleteUser(user.getPath(), session);
            session.save();
        }
    }

    @Test
    public void testExportWithEncodedAttribute() throws Exception {
        try {
            JCRTemplate.getInstance().doExecute(JCRSessionFactory.getInstance().getCurrentUser(), Constants.EDIT_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            // Add usernode as it allows unstructured properties
                            Node userNode = session.getRootNode().getNode(SITECONTENT_ROOT_NODE).addNode("testExportWithEncodedAttribute-userNode", "jnt:user");
                            userNode.setProperty("jnt:3n", "test");
                            userNode.setProperty("3n", "test");
                            userNode.setProperty("_x0033_n", "test");
                            session.save();
                            exportImportAndCheck(session);
                            return null;
                        }
                    });
            // Add Content with properties starting with xml invalid characters but valid in JCR



        } finally {
            // Cleanup content
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists("/" + SITECONTENT_ROOT_NODE + "/testExportWithEncodedAttribute-userNode")) {
                session.getNode("/" + SITECONTENT_ROOT_NODE + "/testExportWithEncodedAttribute-userNode").remove();
                session.save();
            }
        }
    }


    public void testExportImportWithUGCComplexChanges() throws Exception {
        JCRSessionFactory sf = JCRSessionFactory.getInstance();
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.LIVE_WORKSPACE,
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
                        newPage.setProperty("j:templateName", "simple");

                        newPage = englishLiveSiteHomeNode.addNode("added-ugc-child-with-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added UGC child with subpage");
                        newPage.setProperty("j:templateName", "simple");
                        newPage = newPage.addNode("ugc-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "UGC subpage");
                        newPage.setProperty("j:templateName", "simple");

                        JCRNodeWrapper childPage = englishLiveSiteHomeNode.getNode("child2");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        newPage = childPage.addNode("added-ugc-child-to-existing-subpage", "jnt:page");
                        newPage.setProperty("jcr:title", "Added UGC child to existing subpage");
                        newPage.setProperty("j:templateName", "simple");
                        session.save();

                        // updates
                        childPage = englishLiveSiteHomeNode.getNode("renamed-child");
                        JCRNodeWrapper addedNode = childPage.addNode("added-ugc-page-to-renamed-page", "jnt:page");
                        addedNode.setProperty("jcr:title", "Added UGC pageto renamed page");
                        addedNode.setProperty("j:templateName", "simple");

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

                        // set ACL
                        childPage = englishLiveSiteHomeNode.getNode("added-ugc-child-with-subpage");
                        if (!childPage.isCheckedOut()) {
                            session.getWorkspace().getVersionManager().checkout(childPage.getPath());
                        }
                        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
                        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

                        JCRUserNode user2 = userManager.createUser("impexpuserugc", "password", new Properties(), session);
                        childPage.setAclInheritanceBreak(true);
                        childPage.grantRoles("u:" + user2.getName(), Sets.newHashSet("owner"));

                        // set tag
                        TaggingService tagService = (TaggingService) SpringContextSingleton
                                .getBean("org.jahia.services.tags.TaggingService");

                        childPage = englishLiveSiteHomeNode.getNode("child2");
                        childPage.addMixin("jmix:tagged");
                        tagService.tag(childPage, "impexptagugc");
                        session.save();
                        return null;
                    }
                });
        sf.closeAllSessions();
        JCRTemplate.getInstance().doExecute(sf.getCurrentUser(), Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE), new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        exportImportAndCheck(session);

                        return null;
                    }
                });
        sf.closeAllSessions();
    }

    private void exportImportAndCheck(JCRSessionWrapper editSession) throws RepositoryException {
        File createdZip = exportSite(TESTSITE_NAME);
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

    public static File exportSite(String siteName) throws RepositoryException {
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
                JahiaContextLoaderListener.getServletContext().getRealPath("/WEB-INF/etc/repository/export/cleanup.xsl"));
        ImportExportService importExportService = ServicesRegistry.getInstance().getImportExportService();
        File zipFile = null;

        try {
            zipFile = File.createTempFile("simpleimportexporttest", ".zip");
            try (OutputStream outputStream = new FileOutputStream(zipFile)) {
                List<JCRSiteNode> sites = Lists
                        .newArrayList((JCRSiteNode) ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(siteName));
                importExportService.exportSites(outputStream, params, sites);
            }
        } catch (FileNotFoundException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (IOException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (JahiaException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (SAXException e) {
            logger.error("Exception during ImportExportTest", e);
        } catch (TransformerException e) {
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
                } catch (Exception ex) {
                    logger.warn("Exception during site creation", ex);
                    fail("Exception during site creation");
                }
                return null;
            }
        });
    }

    private static void initContent(JCRSessionWrapper session, JahiaSite site) throws RepositoryException {
        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        String defaultLanguage = site.getDefaultLanguage();

        Locale englishLocale = Locale.ENGLISH;

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, englishLocale,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        JCRNodeWrapper englishEditSiteRootNode = englishEditSession.getNode("/" + SITECONTENT_ROOT_NODE);
        JCRNodeWrapper englishEditSiteHomeNode = (JCRNodeWrapper) englishEditSiteRootNode.getNode("home");

        TestHelper.createSubPages(englishEditSiteHomeNode, 1, 3, "Page title");
        englishEditSession.save();

        fillPagesWithLists(englishEditSiteHomeNode);
        englishEditSession.save();
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
                    if (!optionalNodes.contains(node.getName())) {
                        sourceChildNodes.put(node.getName(), node);
                    }
                }
                while (targetSiteIt.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) targetSiteIt.next();
                    if (!optionalNodes.contains(node.getName())) {
                        targetChildNodes.put(node.getName(), node);
                    }
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
                    Set<? extends Value> sourceValues = Sets.newHashSet(sourceProperty.getValues());
                    Set<? extends Value> targetValues = Sets.newHashSet(targetProperty.getValues());
                    if (sourceValues.size() != targetValues.size() && "jcr:mixinTypes".equals(sourceProperty.getDefinition().getName())) {
                        Iterator<? extends Value> it = sourceValues.iterator();
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
                    String sourceValue = sourceProperty.getValue().getString();
                    String targetValue = targetProperty.getValue().getString();
                    String sourceReferencePath = "";
                    String targetReferencePath = "";
                    if ("j:fullpath".equals(sourceEntry.getKey()) || "j:nodename".equals(sourceEntry.getKey())
                            || "j:title".equals(sourceEntry.getKey()) || "j:description".equals(sourceEntry.getKey())) {
                        targetValue = targetValue.replace(targetRootPath, sourceRootPath);
                        targetValue = targetValue.replace(TARGET_TESTSITE_NAME, TESTSITE_NAME);
                    } else if ("jcr:createdBy".equals(sourceEntry.getKey()) && sourceProperty.getPath().contains("/components/")) {
                        targetValue = targetValue.replace(targetRootPath, sourceRootPath);
                        if (sourceValue.equals("root") && targetValue.equals("system") || sourceValue.equals("system") && targetValue.equals("root")) {
                            sourceValue = targetValue;
                        }
                    } else if (isReference) {
                        try {
                            sourceReferencePath = sourceSiteNode.getSession().getNodeByUUID(sourceProperty.getValue().getString())
                                    .getPath();
                            sourceValue = sourceReferencePath;
                            sourceValue = sourceValue.replace(sourceRootPath, "");
                            sourceValue = sourceValue.replace(TESTSITE_NAME, "");
                        } catch (Exception e) {
                            logger.warn(sourceProperty.getPath() + "'s value leads to an exception: " + e.toString());
                            sourceReferencePath = ILLEGAL_STATE;
                            sourceValue = ILLEGAL_STATE;
                        }

                        try {
                            targetReferencePath = targetSiteNode.getSession().getNodeByUUID(targetProperty.getValue().getString())
                                    .getPath();
                            targetValue = targetReferencePath;
                            targetValue = targetValue.replace(targetRootPath, "");
                            targetValue = targetValue.replace(TARGET_TESTSITE_NAME, "");
                        } catch (Exception e) {
                            logger.warn(targetProperty.getPath() + "'s value leads to an exception: " + e.toString());
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

    private boolean compareArrayValues(Set<? extends Value> sourceValues, Set<? extends Value> targetValues) {
        boolean match = true;

        for (Iterator<? extends Value> it = sourceValues.iterator(); match && it.hasNext(); ) {
            match = targetValues.contains(it.next());
        }
        return match;
    }

    private boolean compareReferenceValues(Set<? extends Value> sourceValues, Set<? extends Value> targetValues, JCRSessionWrapper sourceSession,
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
}
