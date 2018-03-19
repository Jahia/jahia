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
import static org.jahia.api.Constants.EDIT_WORKSPACE;
import static org.jahia.api.Constants.LIVE_WORKSPACE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
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
 * Test case for protecting access to JCR via REST API.
 *
 * @author Sergiy Shyrkov
 */
public class RestApiAccessTest extends JahiaTestCase {

    private static final String EDITOR_USER_NAME = "rest-api-access-test-editor";

    private static String editorFilesPath;

    private static final Logger logger = LoggerFactory.getLogger(RestApiAccessTest.class);

    private static JahiaSite site;

    private final static String TESTSITE_NAME = "restApiAccessTest";

    private static final String USER_PASSWORD = "password";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                TestHelper.INTRANET_TEMPLATES);
        assertNotNull(site);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // create editor user
                JCRUserNode editorUser = JahiaUserManagerService.getInstance().createUser(EDITOR_USER_NAME, null,
                        USER_PASSWORD, new Properties(), session);
                session.save();

                // grant her the editor role on the site
                JCRNodeWrapper siteNode = session.getNode(site.getJCRLocalPath());
                siteNode.grantRoles("u:" + EDITOR_USER_NAME, Collections.singleton("editor"));
                session.save();

                // create user files folder
                JCRNodeWrapper userFiles = session.getNode(editorUser.getPath()).addNode("files",
                        Constants.JAHIANT_FOLDER);
                session.save();
                editorFilesPath = userFiles.getPath();

                // publish files of a user
                JCRPublicationService.getInstance().publishByMainId(userFiles.getIdentifier(), Constants.EDIT_WORKSPACE,
                        Constants.LIVE_WORKSPACE, null, true, null);

                // publish site
                JCRPublicationService.getInstance().publishByMainId(siteNode.getIdentifier(),
                        Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                return null;
            }
        });
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
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

    @Test
    public void accessWithEditorUser() throws RepositoryException, IOException {
        login(EDITOR_USER_NAME, USER_PASSWORD);
        try {
            checkAccess(StringUtils.substringBeforeLast(editorFilesPath, "/"));
            checkAccess(editorFilesPath);

            checkAccess("/sites/" + TESTSITE_NAME + "/contents");
        } finally {
            logout();
        }
    }

    @Test
    public void accessWithGuestToFoldersAndPages() throws RepositoryException, IOException {
        checkAccess("/sites/" + TESTSITE_NAME + "/files");
        checkAccess("/sites/" + TESTSITE_NAME + "/home");
    }

    private void checkAccess(String url) throws IOException {
        checkAccess("/modules/api/jcr/v1/live/en/paths" + url, true);
    }

    private void checkAccess(String url, boolean shouldHaveAccess) throws IOException {
        String out = getAsText(url, shouldHaveAccess ? SC_OK : SC_NOT_FOUND);
        if (shouldHaveAccess) {
            assertFalse("Should have access to the URL: " + url,
                    StringUtils.contains(out, "\"exception\":\"javax.jcr.PathNotFoundException\""));
        } else {
            assertTrue("Should NOT have access to the URL: " + url,
                    StringUtils.contains(out, "\"exception\":\"javax.jcr.PathNotFoundException\""));
        }
    }

    private void checkNoAccess(String url) throws IOException {
        checkNoAccess(url, LIVE_WORKSPACE);
        checkNoAccess(url, EDIT_WORKSPACE);
    }

    private void checkNoAccess(String url, String workspace) throws IOException {
        checkAccess("/modules/api/jcr/v1/" + workspace + "/en/paths" + url, false);
    }

    @Test
    public void noAccessToConfiguredNodeTypesToSkip() throws RepositoryException, IOException {
        String pwdEntryPath = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
            @Override
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<JCRNodeWrapper> pwdHistoryEntries = JCRContentUtils.getChildrenOfType(
                        session.getNode("/users/root/passwordHistory"), "jnt:passwordHistoryEntry", 1);
                return pwdHistoryEntries.size() > 0 ? pwdHistoryEntries.iterator().next().getPath() : null;
            }
        });

        assertNotNull("Unable to find password history entry for root user", pwdEntryPath);

        // with guest
        checkNoAccess("/users/root/passwordHistory");
        checkNoAccess(pwdEntryPath);

        // even with root
        loginRoot();
        try {
            checkNoAccess("/users/root/passwordHistory");
            checkNoAccess(pwdEntryPath);
        } finally {
            logout();
        }
    }

    @Test
    public void noAccessWithGuestToOtherContent() throws RepositoryException, IOException {
        String[] paths = new String[] { "/groups", "/imports", "/j:acl", "/jcr:system", "/modules", "/passwordPolicy",
                "/referencesKeeper", "/settings", "/sites", "/users", "/users/root", "/users/root/files",
                editorFilesPath };
        for (String path : paths) {
            checkNoAccess(path);
        }
    }

}
