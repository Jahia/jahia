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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
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
import java.util.List;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.jahia.api.Constants.EDIT_WORKSPACE;
import static org.jahia.api.Constants.LIVE_WORKSPACE;
import static org.junit.Assert.*;

/**
 * Test case for protecting access to JCR via REST API.
 *
 * @author Sergiy Shyrkov
 */
public class RestApiAccessTest extends JahiaTestCase {

    private static final String EDITOR_USER_NAME = "rest-api-access-test-editor";

    private static final String EDITOR_USER_PASSWORD = "password";

    private static String editorNodePath;

    private static JahiaSite site;

    private final static String TESTSITE_NAME = "restApiAccessTest";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, "localhost" + System.currentTimeMillis(),
                TestHelper.WEB_TEMPLATES);
        assertNotNull(site);

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // create editor user
                JCRUserNode editorUser = JahiaUserManagerService.getInstance().createUser(EDITOR_USER_NAME, null,
                        EDITOR_USER_PASSWORD, new Properties(), session);
                editorNodePath = editorUser.getPath();
                session.save();

                // grant her the editor role on the site
                JCRNodeWrapper siteNode = session.getNode(site.getJCRLocalPath());
                siteNode.grantRoles("u:" + EDITOR_USER_NAME, Collections.singleton("editor"));
                session.save();

                // create user files folder
                JCRNodeWrapper userFiles = editorUser.addNode("files", Constants.JAHIANT_FOLDER);
                session.save();

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

        TestHelper.deleteSite(TESTSITE_NAME);

        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void accessWithEditorUser() throws RepositoryException, IOException {
        login(EDITOR_USER_NAME, EDITOR_USER_PASSWORD);
        try {
            checkLiveAccess(editorNodePath);
            checkLiveAccess(editorNodePath + "/files");
            checkLiveAccess("/sites/" + TESTSITE_NAME + "/contents");
        } finally {
            logout();
        }
    }

    @Test
    public void accessWithGuestToFoldersAndPages() throws RepositoryException, IOException {
        checkNoAccess("/sites/" + TESTSITE_NAME + "/files");
        checkNoAccess("/sites/" + TESTSITE_NAME + "/home");
    }

    private void checkLiveAccess(String path) throws IOException {
        checkAccess("/modules/api/jcr/v1/live/en/paths" + path, true);
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

    private void checkNoAccess(String path) throws IOException {
        checkNoAccess(path, LIVE_WORKSPACE);
        checkNoAccess(path, EDIT_WORKSPACE);
    }

    private void checkNoAccess(String path, String workspace) throws IOException {
        checkAccess("/modules/api/jcr/v1/" + workspace + "/en/paths" + path, false);
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
                editorNodePath + "/files" };
        for (String path : paths) {
            checkNoAccess(path);
        }
    }
}
