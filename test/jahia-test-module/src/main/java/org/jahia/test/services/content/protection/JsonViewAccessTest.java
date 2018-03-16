/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.content.protection;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for protecting access to JCR settings node.
 *
 * @author Sergiy Shyrkov
 */
public class JsonViewAccessTest extends JahiaTestCase {

    private static final String CATEGORY_NAME = "json-view-access-test-category";

    private static final String EDITOR_USER_NAME = "json-view-access-test-editor";

    private static String editorFilesPath;

    private static final Logger logger = LoggerFactory.getLogger(JcrSettingsAccessTest.class);

    private static JahiaSite site;

    private final static String TESTSITE_NAME = "jsonViewAccessTest";

    private static final String USER_PASSWORD = "password";

    private static void createCategories() throws JahiaException {
        Category root = Category.createCategory(CATEGORY_NAME, null);

        Category products = Category.createCategory("products", root);
        Category.createCategory("DX", products);
        Category.createCategory("MF", products);
        Category.createCategory("FF", products);

        Category services = Category.createCategory("services", root);
        Category.createCategory("Training", services);
        Category.createCategory("Support", services);
    }

    private static void createContent() throws RepositoryException {
        JCRTemplate.getInstance().doExecute(EDITOR_USER_NAME, site.getSiteKey(), Constants.EDIT_WORKSPACE,
                Locale.ENGLISH, new JCRCallback<Boolean>() {
                    @Override
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(site.getJCRLocalPath());

                        JCRNodeWrapper pageNode = siteNode.getHome().addNode("page-a", Constants.JAHIANT_PAGE);
                        pageNode.setProperty("jcr:title", "Page A");
                        pageNode.setProperty("j:templateName", "simple");

                        JCRNodeWrapper subpageNode = pageNode.addNode("sub-page-1", Constants.JAHIANT_PAGE);
                        subpageNode.setProperty("jcr:title", "Subpage 1");
                        subpageNode.setProperty("j:templateName", "simple");

                        subpageNode = pageNode.addNode("sub-page-2", Constants.JAHIANT_PAGE);
                        subpageNode.setProperty("jcr:title", "Subpage 2");
                        subpageNode.setProperty("j:templateName", "simple");

                        pageNode = siteNode.getHome().addNode("page-b", Constants.JAHIANT_PAGE);
                        pageNode.setProperty("jcr:title", "Page B");
                        pageNode.setProperty("j:templateName", "simple");

                        pageNode = siteNode.getHome().addNode("label-c", "jnt:navMenuText");
                        pageNode.setProperty("jcr:title", "Label C");

                        subpageNode = pageNode.addNode("label-sub-page-1", Constants.JAHIANT_PAGE);
                        subpageNode.setProperty("jcr:title", "Subpage under label 1");
                        subpageNode.setProperty("j:templateName", "simple");

                        subpageNode = pageNode.addNode("label-sub-page-2", Constants.JAHIANT_PAGE);
                        subpageNode.setProperty("jcr:title", "Subpage under label 2");
                        subpageNode.setProperty("j:templateName", "simple");

                        // site files
                        JCRNodeWrapper folder = siteNode.getNode("files").addNode("folder-1", Constants.JAHIANT_FOLDER);
                        folder.uploadFile("text-1.txt", IOUtils.toInputStream("text-1"), "plain/text");
                        folder.uploadFile("text-2.txt", IOUtils.toInputStream("text-2"), "plain/text");
                        folder = siteNode.getNode("files").addNode("folder-2", Constants.JAHIANT_FOLDER);
                        folder.uploadFile("text-3.txt", IOUtils.toInputStream("text-3"), "plain/text");
                        folder.uploadFile("text-4.txt", IOUtils.toInputStream("text-4"), "plain/text");

                        session.save();

                        // publish site
                        JCRPublicationService.getInstance().publishByMainId(siteNode.getIdentifier(),
                                Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                        return null;
                    }
                });

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // editor user files
                JCRNodeWrapper userFiles = session.getNode(ServicesRegistry.getInstance().getJahiaUserManagerService()
                        .getUserSplittingRule().getPathForUsername(EDITOR_USER_NAME))
                        .addNode("files", Constants.JAHIANT_FOLDER);
                editorFilesPath = userFiles.getPath();
                JCRNodeWrapper folder = userFiles.addNode("folder-1", Constants.JAHIANT_FOLDER);
                folder.uploadFile("text-1.txt", IOUtils.toInputStream("text-1"), "plain/text");
                folder.uploadFile("text-2.txt", IOUtils.toInputStream("text-2"), "plain/text");
                folder = userFiles.addNode("folder-2", Constants.JAHIANT_FOLDER);
                folder.uploadFile("text-3.txt", IOUtils.toInputStream("text-3"), "plain/text");
                folder.uploadFile("text-4.txt", IOUtils.toInputStream("text-4"), "plain/text");

                session.save();

                // publish files of a user
                JCRPublicationService.getInstance().publishByMainId(userFiles.getIdentifier(), Constants.EDIT_WORKSPACE,
                        Constants.LIVE_WORKSPACE, null, true, null);
                return null;
            }
        });
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                TestHelper.INTRANET_TEMPLATES);
        assertNotNull(site);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // create editor user
                JahiaUserManagerService.getInstance().createUser(EDITOR_USER_NAME, null, USER_PASSWORD,
                        new Properties(), session);
                session.save();

                // grant her the editor role on the site
                JCRNodeWrapper siteNode = session.getNode(site.getJCRLocalPath());
                siteNode.grantRoles("u:" + EDITOR_USER_NAME, Collections.singleton("editor"));
                session.save();

                try {
                    createCategories();
                    session.save();
                } catch (JahiaException e) {
                    throw new RepositoryException(e);
                }

                return null;
            }
        });

        createContent();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (session.nodeExists("/sites/systemsite/categories/" + CATEGORY_NAME)) {
                    session.getNode("/sites/systemsite/categories/" + CATEGORY_NAME).remove();
                    session.save();
                }

                JahiaUserManagerService userManager = JahiaUserManagerService.getInstance();
                JCRUserNode editorUser = userManager.lookupUser(EDITOR_USER_NAME, session);
                if (editorUser != null) {
                    userManager.deleteUser(editorUser.getPath(), session);
                    session.save();
                }

                return null;
            }
        });

        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private void checkAccess(String url, boolean shouldHaveAccess, String textInResponse) throws IOException {
        String out = getAsText(url, shouldHaveAccess ? SC_OK : SC_NOT_FOUND);
        if (textInResponse != null) {
            assertTrue("Response content of the URL " + url + " does not contain expected text: " + textInResponse,
                    StringUtils.contains(out, textInResponse));
        }
    }

    private void checkHasAccess(String url, String textInResponse) throws IOException {
        checkAccess(url, true, textInResponse);
    }

    private void checkNoAccess(String url) throws IOException {
        checkAccess(url, false, "<title>404 - ");
    }

    @Test
    public void guestHasAccessInLiveToCategories() throws RepositoryException, IOException {
        String rootPath = JCRContentUtils.getSystemSitePath() + "/categories/" + CATEGORY_NAME;

        // categories with sub-categories
        String[] relativePaths = new String[] { "", "/products", "/services" };

        for (String path : relativePaths) {
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".json", "\"jcr:created\"");
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".tree.json", "\"path\":\"" + rootPath + path);
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".treeItem.json", "\"path\":\"" + rootPath + path);
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".treeRootItem.json",
                    "\"path\":\"" + rootPath + path);
        }

        // leaves
        relativePaths = new String[] { "/products/DX", "/services/Training" };

        for (String path : relativePaths) {
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".json", "\"jcr:created\"");
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".tree.json", null);
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".treeItem.json", "\"path\":\"" + rootPath + path);
            checkHasAccess("/cms/render/live/en" + rootPath + path + ".treeRootItem.json",
                    "\"path\":\"" + rootPath + path);
        }
    }

    @Test
    public void guestHasAccessInLiveToSiteFolders() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();
        String[] relativePaths = new String[] { "/files", "/files/folder-1", "/files/folder-2" };

        for (String path : relativePaths) {
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".json", "\"jcr:created\"");
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".tree.json",
                    "/files".equals(path) ? "\"path\":\"" + sitePath + path : "[]");
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeRootItem.json",
                    "\"path\":\"" + sitePath + path);
        }
    }

    @Test
    public void guestHasAccessInLiveToSitePages() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();

        // access with treeRootItem view for site node itself
        checkHasAccess("/cms/render/live/en" + sitePath + ".treeRootItem.json", "\"path\":\"" + sitePath + "\"");

        // access for pages with sub-pages
        String[] relativePaths = new String[] { "/home", "/home/page-a", "/home/label-c" };
        for (String path : relativePaths) {
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".json", "\"jcr:created\"");
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".tree.json", "\"path\":\"" + sitePath + path);
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeRootItem.json",
                    "\"path\":\"" + sitePath + path);
        }

        // access for pages without subpages
        relativePaths = new String[] { "/home/page-a/sub-page-1", "/home/label-c/label-sub-page-1" };

        for (String path : relativePaths) {
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".json", "\"jcr:created\"");
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".tree.json", null);
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkHasAccess("/cms/render/live/en" + sitePath + path + ".treeRootItem.json",
                    "\"path\":\"" + sitePath + path);
        }
    }

    @Test
    public void guestHasNoAccessInLiveToProtectedContent() throws RepositoryException, IOException {
        // root j:acl node
        getAsText("/cms/render/live/en/j:acl.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en/j:acl.tree.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en/j:acl.treeItem.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en/j:acl.treeRootItem.json", SC_UNAUTHORIZED);

        String[] relativePaths = new String[] { "/jcr:system", "/imports", "/groups", "/modules", "/referencesKeeper",
                "/settings", "/sites", "/users" };

        for (String path : relativePaths) {
            checkNoAccess("/cms/render/live/en" + path + ".json");
            checkNoAccess("/cms/render/live/en" + path + ".tree.json");
            checkNoAccess("/cms/render/live/en" + path + ".treeItem.json");
            checkNoAccess("/cms/render/live/en" + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteFiles() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();
        String[] relativePaths = new String[] { "/files/folder-1/text-1.txt", "/files/folder-2/text-3.txt" };

        for (String path : relativePaths) {
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".tree.json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".treeItem.json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteProtectedContent() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();

        // site node itself
        checkNoAccess("/cms/render/live/en" + sitePath + ".json");
        checkNoAccess("/cms/render/live/en" + sitePath + ".tree.json");
        checkNoAccess("/cms/render/live/en" + sitePath + ".treeItem.json");
        // the treeRootItem is still allowed on site node

        // j:acl node
        getAsText("/cms/render/live/en" + sitePath + "/j:acl.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en" + sitePath + "/j:acl.tree.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en" + sitePath + "/j:acl.treeItem.json", SC_UNAUTHORIZED);
        getAsText("/cms/render/live/en" + sitePath + "/j:acl.treeRootItem.json", SC_UNAUTHORIZED);

        String[] relativePaths = new String[] { "/contents", "/groups", "/users", "/home/listA", "/home/listA" };

        for (String path : relativePaths) {
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".tree.json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".treeItem.json");
            checkNoAccess("/cms/render/live/en" + sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteUserFolders() throws RepositoryException, IOException {
        String root = editorFilesPath;
        String[] relativePaths = new String[] { "/files", "/files/folder-1", "/files/folder-2" };

        for (String path : relativePaths) {
            checkNoAccess("/cms/render/live/en" + root + path + ".json");
            checkNoAccess("/cms/render/live/en" + root + path + ".tree.json");
            checkNoAccess("/cms/render/live/en" + root + path + ".treeItem.json");
            checkNoAccess("/cms/render/live/en" + root + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToUserFilesAndFolder() throws RepositoryException, IOException {
        String root = editorFilesPath;
        String[] relativePaths = new String[] { "", "/folder-1", "/folder-2", "/folder-1/text-1.txt",
                "/folder-2/text-3.txt" };

        for (String path : relativePaths) {
            checkNoAccess("/cms/render/live/en" + root + path + ".json");
            checkNoAccess("/cms/render/live/en" + root + path + ".tree.json");
            checkNoAccess("/cms/render/live/en" + root + path + ".treeItem.json");
            checkNoAccess("/cms/render/live/en" + root + path + ".treeRootItem.json");
        }
    }

}
