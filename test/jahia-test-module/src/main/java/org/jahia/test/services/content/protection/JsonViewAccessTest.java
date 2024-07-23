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
package org.jahia.test.services.content.protection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test case for protecting access to JCR settings node.
 *
 * @author Sergiy Shyrkov
 */
public class JsonViewAccessTest extends JahiaTestCase {

    private static final String CATEGORY_NAME = "json-view-access-test-category";

    private static final String EDITOR_USER_NAME = "json-view-access-test-editor";

    private static String editorFilesPath;

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
                TestHelper.WEB_TEMPLATES);
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

        TestHelper.deleteSite(TESTSITE_NAME);

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    private void checkAccess(String url, boolean shouldHaveAccess, String textInResponse) throws IOException {
        String urlToTest = "/cms/render/live/en" + url;
        String out = getAsText(urlToTest, shouldHaveAccess ? SC_OK : SC_NOT_FOUND);
        if (textInResponse != null) {
            assertTrue("Response content of the URL " + urlToTest + " does not contain expected text: " + textInResponse,
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
            checkHasAccess(rootPath + path + ".json", "\"jcr:created\"");
            checkHasAccess(rootPath + path + ".tree.json", "\"path\":\"" + rootPath + path);
            checkHasAccess(rootPath + path + ".treeItem.json", "\"path\":\"" + rootPath + path);
            checkHasAccess(rootPath + path + ".treeRootItem.json",
                    "\"path\":\"" + rootPath + path);
        }

        // leaves
        relativePaths = new String[] { "/products/DX", "/services/Training" };

        for (String path : relativePaths) {
            checkHasAccess(rootPath + path + ".json", "\"jcr:created\"");
            checkHasAccess(rootPath + path + ".tree.json", null);
            checkHasAccess(rootPath + path + ".treeItem.json", "\"path\":\"" + rootPath + path);
            checkHasAccess(rootPath + path + ".treeRootItem.json",
                    "\"path\":\"" + rootPath + path);
        }
    }

    @Test
    public void guestHasAccessInLiveToSiteFolders() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();
        String[] relativePaths = new String[] { "/files", "/files/folder-1", "/files/folder-2" };

        for (String path : relativePaths) {
            checkNoAccess(sitePath + path + ".json");
            checkHasAccess(sitePath + path + ".tree.json",
                    "/files".equals(path) ? "\"path\":\"" + sitePath + path : "[]");
            checkHasAccess(sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkNoAccess(sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasAccessInLiveToSitePages() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();

        // access with treeRootItem view for site node itself
        checkHasAccess(sitePath + ".treeRootItem.json", "\"path\":\"" + sitePath + "\"");

        // access for pages with sub-pages
        String[] relativePaths = new String[] { "/home", "/home/page-a", "/home/label-c" };
        for (String path : relativePaths) {
            checkNoAccess(sitePath + path + ".json");
            checkHasAccess(sitePath + path + ".tree.json", "\"path\":\"" + sitePath + path);
            checkHasAccess(sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkNoAccess(sitePath + path + ".treeRootItem.json");
        }

        // access for pages without subpages
        relativePaths = new String[] { "/home/page-a/sub-page-1", "/home/label-c/label-sub-page-1" };

        for (String path : relativePaths) {
            checkNoAccess(sitePath + path + ".json");
            checkHasAccess(sitePath + path + ".tree.json", null);
            checkHasAccess(sitePath + path + ".treeItem.json", "\"path\":\"" + sitePath + path);
            checkNoAccess(sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToProtectedContent() throws RepositoryException, IOException {
        // root j:acl node
        getAsText("/cms/render/live/en/j:acl.json", SC_NOT_FOUND);
        getAsText("/cms/render/live/en/j:acl.tree.json", SC_NOT_FOUND);
        getAsText("/cms/render/live/en/j:acl.treeItem.json", SC_NOT_FOUND);
        getAsText("/cms/render/live/en/j:acl.treeRootItem.json", SC_NOT_FOUND);

        String[] relativePaths = new String[] { "/jcr:system", "/imports", "/groups", "/modules", "/referencesKeeper",
                "/settings", "/sites", "/users" };

        for (String path : relativePaths) {
            checkNoAccess(path + ".json");
            checkNoAccess(path + ".tree.json");
            checkNoAccess(path + ".treeItem.json");
            checkNoAccess(path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteFiles() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();
        String[] relativePaths = new String[] { "/files/folder-1/text-1.txt", "/files/folder-2/text-3.txt" };

        for (String path : relativePaths) {
            checkNoAccess(sitePath + path + ".json");
            checkNoAccess(sitePath + path + ".tree.json");
            checkNoAccess(sitePath + path + ".treeItem.json");
            checkNoAccess(sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteProtectedContent() throws RepositoryException, IOException {
        String sitePath = site.getJCRLocalPath();

        // site node itself
        checkNoAccess(sitePath + ".json");
        checkNoAccess(sitePath + ".tree.json");
        checkNoAccess(sitePath + ".treeItem.json");
        // the treeRootItem is still allowed on site node

        // j:acl node
        getAsText(sitePath + "/j:acl.json", SC_NOT_FOUND);
        getAsText(sitePath + "/j:acl.tree.json", SC_NOT_FOUND);
        getAsText(sitePath + "/j:acl.treeItem.json", SC_NOT_FOUND);
        getAsText(sitePath + "/j:acl.treeRootItem.json", SC_NOT_FOUND);

        String[] relativePaths = new String[] { "/contents", "/groups", "/users", "/home/listA", "/home/listA" };

        for (String path : relativePaths) {
            checkNoAccess(sitePath + path + ".json");
            checkNoAccess( sitePath + path + ".tree.json");
            checkNoAccess(sitePath + path + ".treeItem.json");
            checkNoAccess(sitePath + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToSiteUserFolders() throws RepositoryException, IOException {
        String root = editorFilesPath;
        String[] relativePaths = new String[] { "/files", "/files/folder-1", "/files/folder-2" };

        for (String path : relativePaths) {
            checkNoAccess(root + path + ".json");
            checkNoAccess(root + path + ".tree.json");
            checkNoAccess(root + path + ".treeItem.json");
            checkNoAccess(root + path + ".treeRootItem.json");
        }
    }

    @Test
    public void guestHasNoAccessInLiveToUserFilesAndFolder() throws RepositoryException, IOException {
        String root = editorFilesPath;
        String[] relativePaths = new String[] { "", "/folder-1", "/folder-2", "/folder-1/text-1.txt",
                "/folder-2/text-3.txt" };

        for (String path : relativePaths) {
            checkNoAccess(root + path + ".json");
            checkNoAccess(root + path + ".tree.json");
            checkNoAccess(root + path + ".treeItem.json");
            checkNoAccess(root + path + ".treeRootItem.json");
        }
    }

}
