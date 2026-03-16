/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.security;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.*;

import javax.jcr.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test suite for verifying that jnt:acl, jnt:ace technical nodes cannot be mutated without required permissions.
 */
public class AccessManagerUtilsACLPermissionsTest extends AbstractJUnitTest {
    private static final String SITE_KEY = "aclDataProtectedSite";
    private static final String SITE_PATH = "/sites/" + SITE_KEY;
    private static final String HOME_PATH = SITE_PATH + "/home";
    private static final String CONTENTS_PATH = SITE_PATH + "/contents";
    private static final String TEST_USER_NAME = "aclDataProtectedUser";
    private static final String TEST_USER_NAME_PRINCIPAL = "u:" + TEST_USER_NAME;
    private static final String FOO_USER_NAME = "foo";
    private static final String FOO_USER_NAME_PRINCIPAL = "u:" + FOO_USER_NAME;
    private static final String PASSWORD = "password";
    private static final String ROLE_NO_ACL = "editor-no-acl-management";
    private static final String ROLE_NO_ACL_PATH = "/roles/" + ROLE_NO_ACL;
    private static final String ROLE_ACL = "editor-with-acl-management";
    private static final String ROLE_ACL_PATH = "/roles/" + ROLE_ACL;
    private static final String ROLE_DEFAULT_READER = "reader-default-only";
    private static final String ROLE_DEFAULT_READER_PATH = "/roles/" + ROLE_DEFAULT_READER;
    private static final String ACCESS_CONTROLLED_MIXIN = "jmix:accessControlled";

    private static JahiaUserManagerService userManager;
    private static JCRSessionFactory sessionFactory;

    private static JahiaUser testUser;
    private static JCRSessionWrapper systemEditSession;
    private static JCRSessionWrapper systemLiveSession;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManager = JahiaUserManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        getCleanSession();

        // Create test users
        testUser = userManager.createUser(TEST_USER_NAME, PASSWORD, new Properties(), systemEditSession).getJahiaUser();
        userManager.createUser(FOO_USER_NAME, PASSWORD, new Properties(), systemEditSession);
        systemEditSession.save();

        // Import test roles
        try (InputStream importStream = AccessManagerUtilsACLPermissionsTest.class.getClassLoader().getResourceAsStream("imports/aclDataProtectionTestRoles.xml")) {
            systemEditSession.importXML("/", importStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, null, null);
            systemEditSession.save();
        }
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        // Clean up test users
        userManager.deleteUser(userManager.lookupUser(TEST_USER_NAME).getPath(), systemEditSession);
        userManager.deleteUser(userManager.lookupUser(FOO_USER_NAME).getPath(), systemEditSession);

        // Clean up test roles
        systemEditSession.removeItem(ROLE_ACL_PATH);
        systemEditSession.removeItem(ROLE_NO_ACL_PATH);
        systemEditSession.removeItem(ROLE_DEFAULT_READER_PATH);
        systemEditSession.save();
    }

    @Before
    public void before() throws Exception {
        getCleanSession();
        TestHelper.createSite(SITE_KEY).getSiteKey();
    }

    @After
    public void after() throws Exception {
        TestHelper.deleteSite(SITE_KEY);
    }

    // ==================== Helper Methods ====================

    private void getCleanSession() throws RepositoryException {
        sessionFactory.closeAllSessions();
        systemEditSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null);
        systemLiveSession = sessionFactory.getCurrentSystemSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, null);
    }

    private void createContentFolder(String parentPath, String name, boolean withAcl) throws RepositoryException {
        JCRNodeWrapper folder = systemEditSession.getNode(parentPath).addNode(name, "jnt:contentFolder");
        if (withAcl) {
            folder.addMixin(ACCESS_CONTROLLED_MIXIN);
        }
        systemEditSession.save();
    }

    private void createPage(String parentPath, String name, String title, boolean withAcl) throws RepositoryException {
        JCRNodeWrapper page = systemEditSession.getNode(parentPath).addNode(name, "jnt:page");
        page.setProperty("jcr:title", title);
        page.setProperty("j:templateName", "simple");
        if (withAcl) {
            page.addMixin(ACCESS_CONTROLLED_MIXIN);
        }
        systemEditSession.save();
    }

    // ==================== Root node test ====================

    @Test
    public void shouldBeAbleToReadRootNode() throws RepositoryException {
        getCleanSession();
        JahiaUser guest = userManager.lookupUser("guest").getJahiaUser();
        JCRTemplate.getInstance().doExecute(guest, Constants.EDIT_WORKSPACE, null,  AccessManagerTestUtils.setCurrentUserCallback(guest, session -> {
            assertTrue("Guest should be able to read / root node", session.nodeExists("/"));
            assertFalse("Guest should not be able to read /sites root node", session.nodeExists("/sites"));
            return null;
        }));

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null,  AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("Normal user should be able to read / root node", session.nodeExists("/"));
            assertFalse("Normal user should not be able to read /sites root node", session.nodeExists("/sites"));
            return null;
        }));
    }

    // ==================== Check creation of jnt:ace/jnt:acl ====================

    @Test
    public void shouldBeAbleToGrantRoleWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null,  AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH).grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("ACE should have been created", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldNotBeAbleToGrantRoleWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to grant role", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH).grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertFalse("ACE node should not have been created", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToCreateACENodeWithAclManagement() throws RepositoryException {
        String testAceNodeName = "test-ace";
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper ace = session.getNode(SITE_PATH + "/j:acl").addNode(testAceNodeName, "jnt:ace");
            ace.setProperty("j:principal", FOO_USER_NAME_PRINCIPAL);
            ace.setProperty("j:roles", new String[]{"editor"});
            ace.setProperty("j:aceType", "GRANT");
            ace.setProperty("j:protected", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("ACE node should have been created", systemEditSession.nodeExists(SITE_PATH + "/j:acl/" + testAceNodeName));
    }

    @Test
    public void shouldNotBeAbleToCreateACENodeWithoutAclManagement() throws RepositoryException {
        String testAceNodeName = "test-ace";
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to create jnt:ace nodes", AccessDeniedException.class, () -> {
                JCRNodeWrapper ace = session.getNode(SITE_PATH + "/j:acl").addNode(testAceNodeName, "jnt:ace");
                ace.setProperty("j:principal", FOO_USER_NAME_PRINCIPAL);
                ace.setProperty("j:roles", new String[]{"editor"});
                ace.setProperty("j:aceType", "GRANT");
                ace.setProperty("j:protected", false);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertFalse("ACE node should not have been created", systemEditSession.nodeExists(SITE_PATH + "/j:acl/" + testAceNodeName));
    }

    @Test
    public void shouldBeAbleToCreateACLNodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(HOME_PATH).addNode("j:acl", "jnt:acl");
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("ACL node should have been created", systemEditSession.nodeExists(HOME_PATH + "/j:acl"));
    }

    @Test
    public void shouldNotBeAbleToCreateACLNodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to create jnt:acl nodes", AccessDeniedException.class, () -> {
                session.getNode(HOME_PATH).addNode("j:acl", "jnt:acl");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertFalse("ACL node should not have been created", systemEditSession.nodeExists(HOME_PATH + "/j:acl"));
    }

    // ==================== Check deletion of jnt:ace/jnt:acl ====================

    @Test
    public void shouldBeAbleToRevokeRoleWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH).revokeRolesForPrincipal("g:site-administrators");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("ACE should have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_g_site-administrators"));
    }

    @Test
    public void shouldNotBeAbleToRevokeRoleWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to revoke role", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH).revokeRolesForPrincipal("g:site-administrators");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACE node should not have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_g_site-administrators"));
    }

    @Test
    public void shouldBeAbleToRemoveACENodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").remove();
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("ACE node should have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_g_site-administrators"));
    }

    @Test
    public void shouldNotBeAbleToRemoveACENodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to remove jnt:ace nodes", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").remove();
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACE node should not have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl/GRANT_g_site-administrators"));
    }

    @Test
    public void shouldBeAbleToRemoveACLNodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH + "/j:acl").remove();
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("ACL node should have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl"));
    }

    @Test
    public void shouldNotBeAbleToRemoveACLNodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to remove jnt:acl nodes", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH + "/j:acl").remove();
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACL node should not have been removed", systemEditSession.nodeExists(SITE_PATH + "/j:acl"));
    }

    // ==================== Check modify properties on jnt:ace/jnt:acl ====================

    @Test
    public void shouldBeAbleToModifyACEPropertiesWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").setProperty("j:principal", FOO_USER_NAME_PRINCIPAL);
            session.save();
            return null;
        }));

        getCleanSession();
        assertEquals(FOO_USER_NAME_PRINCIPAL, systemEditSession.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").getProperty("j:principal").getString());
    }

    @Test
    public void shouldNotBeAbleToModifyACEPropertiesWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to modify jnt:ace properties", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").setProperty("j:roles", new String[]{"editor", "contributor"});
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertEquals("g:site-administrators", systemEditSession.getNode(SITE_PATH + "/j:acl/GRANT_g_site-administrators").getProperty("j:principal").getString());
    }

    @Test
    public void shouldBeAbleToModifyACLPropertiesWithAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(SITE_PATH + "/j:acl").setProperty("j:inherit", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse(systemEditSession.getNode(SITE_PATH + "/j:acl").getProperty("j:inherit").getBoolean());
    }

    @Test
    public void shouldNotBeAbleToModifyACLPropertiesWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to modify jnt:acl properties", AccessDeniedException.class, () -> {
                session.getNode(SITE_PATH + "/j:acl").setProperty("j:inherit", false);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertFalse(systemEditSession.getNode(SITE_PATH + "/j:acl").hasProperty("j:inherit"));
    }

    // ==================== Check move/copy of jnt:ace/jnt:acl ====================

    @Test
    public void shouldBeAbleToMoveACENodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source and destination folders
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.move(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo", CONTENTS_PATH + "/dest-folder/j:acl/GRANT_u_foo");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("ACE should have been moved from source", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo"));
        assertTrue("ACE should exist in destination", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldNotBeAbleToMoveACENodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source and destination folders
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to move jnt:ace nodes", AccessDeniedException.class, () -> {
                session.move(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo", CONTENTS_PATH + "/dest-folder/j:acl/GRANT_u_foo");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACL should still exist in source", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl"));
    }

    @Test
    public void shouldNotBeAbleToMoveACENodeWhenLackingPermissionOnSource() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL and ACE, and pageB with ACL
        createPage(HOME_PATH, "pageA", "Page A", true);
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createPage(HOME_PATH, "pageB", "Page B", true);

        // Grant ACL management on destination (pageB) but NO ACL management on source (pageA)
        systemEditSession.getNode(HOME_PATH + "/pageB").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to move ACE when lacking permission on source (destructive move)", AccessDeniedException.class, () -> {
                session.move(HOME_PATH + "/pageA/j:acl/GRANT_u_foo", HOME_PATH + "/pageB/j:acl/GRANT_u_foo");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACE should still exist in source (move should have failed)", systemEditSession.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));
        assertFalse("ACE should not exist in destination", systemEditSession.nodeExists(HOME_PATH + "/pageB/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldNotBeAbleToMoveACLNodeWhenLackingPermissionOnSource() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL and pageB without ACL
        createPage(HOME_PATH, "pageA", "Page A", true);
        createPage(HOME_PATH, "pageBParent", "page B Parent", true);
        createPage(HOME_PATH + "/pageBParent", "pageB", "Page B", false);

        // Grant ACL management on destination (pageB) but NO ACL management on source (pageA)
        systemEditSession.getNode(HOME_PATH + "/pageBParent").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to move ACL when lacking permission on source (destructive move)", AccessDeniedException.class, () -> {
                session.move(HOME_PATH + "/pageA/j:acl", HOME_PATH + "/pageBParent/pageB/j:acl");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACL should still exist in source (move should have failed)", systemEditSession.nodeExists(HOME_PATH + "/pageA/j:acl"));
        assertFalse("ACL should not exist in destination", systemEditSession.nodeExists(HOME_PATH + "/pageBParent/pageB/j:acl"));
    }

    @Test
    public void shouldBeAbleToMoveACLNodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source folder with ACL and destination folder without ACL
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/dest-folder/j:acl").remove();
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.move(CONTENTS_PATH + "/source-folder/j:acl", CONTENTS_PATH + "/dest-folder/j:acl");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("ACL should have been moved from source", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl"));
        assertTrue("ACL should exist in destination", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl"));
        assertTrue("ACE should exist in copied ACL", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldNotBeAbleToMoveACLNodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source folder with ACL and destination folder without ACL
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/dest-folder/j:acl").remove();
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to move jnt:acl nodes without permission", AccessDeniedException.class, () -> {
                session.move(CONTENTS_PATH + "/source-folder/j:acl", CONTENTS_PATH + "/dest-folder/j:acl");
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("ACL should still exist in source", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl"));
    }

    @Test
    public void shouldBeAbleToCopyACENodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source and destination folders
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper sourceAce = session.getNode(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo");
            JCRNodeWrapper destAcl = session.getNode(CONTENTS_PATH + "/dest-folder/j:acl");
            sourceAce.copy(destAcl, "copied-ace", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Original ACE should still exist", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo"));
        assertTrue("Copied ACE should exist in destination", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl/copied-ace"));
    }

    @Test
    public void shouldNotBeAbleToCopyACENodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source and destination folders
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to copy jnt:ace nodes", AccessDeniedException.class, () -> {
                JCRNodeWrapper sourceAce = session.getNode(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo");
                JCRNodeWrapper destAcl = session.getNode(CONTENTS_PATH + "/dest-folder/j:acl");
                sourceAce.copy(destAcl, "copied-ace", false);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("Original ACE should still exist", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl/GRANT_u_foo"));
        assertFalse("Copied ACE should not exist", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl/copied-ace"));
    }

    @Test
    public void shouldBeAbleToCopyACLNodeWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source folder with ACL and destination folder without ACL
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/dest-folder/j:acl").remove();
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper sourceAcl = session.getNode(CONTENTS_PATH + "/source-folder/j:acl");
            JCRNodeWrapper destFolder = session.getNode(CONTENTS_PATH + "/dest-folder");
            sourceAcl.copy(destFolder, "j:acl", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Original ACL should still exist", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl"));
        assertTrue("Copied ACL should exist in destination", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl"));
        assertTrue("ACE should exist in copied ACL", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldNotBeAbleToCopyACLNodeWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create source folder with ACL and destination folder without ACL
        createContentFolder(CONTENTS_PATH, "source-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/source-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createContentFolder(CONTENTS_PATH, "dest-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/dest-folder/j:acl").remove();
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to copy jnt:acl nodes", AccessDeniedException.class, () -> {
                JCRNodeWrapper sourceAcl = session.getNode(CONTENTS_PATH + "/source-folder/j:acl");
                JCRNodeWrapper destFolder = session.getNode(CONTENTS_PATH + "/dest-folder");
                sourceAcl.copy(destFolder, "j:acl", false);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("Original ACL should still exist", systemEditSession.nodeExists(CONTENTS_PATH + "/source-folder/j:acl"));
        assertFalse("Copied ACL should not exist", systemEditSession.nodeExists(CONTENTS_PATH + "/dest-folder/j:acl"));
    }

    // ==================== Check move/copy of contents with ACE/ACL nodes ====================

    @Test
    public void shouldBeAbleToCopyPageWithACLWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL
        createPage(SITE_PATH, "pageA", "Page A", true);
        systemEditSession.getNode(SITE_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper pageA = session.getNode(SITE_PATH + "/pageA");
            JCRNodeWrapper site = session.getNode(SITE_PATH);
            pageA.copy(site, "pageB", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Copied page should exist", systemEditSession.nodeExists(SITE_PATH + "/pageB"));
        assertTrue("Copied page should have ACL with ACE", systemEditSession.nodeExists(SITE_PATH + "/pageB/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToMovePageWithACLWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL and dest-page
        createPage(HOME_PATH, "pageA", "Page A", true);
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createPage(HOME_PATH, "dest-page", "Dest Page", false);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.move(HOME_PATH + "/pageA", HOME_PATH + "/dest-page/pageA");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("Original page should not exist", systemEditSession.nodeExists(HOME_PATH + "/pageA"));
        assertTrue("Moved page should exist", systemEditSession.nodeExists(HOME_PATH + "/dest-page/pageA"));
        assertTrue("Moved page should have ACL with ACE", systemEditSession.nodeExists(HOME_PATH + "/dest-page/pageA/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToCopyDeeplyNestedContentWithMultipleACLWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create nested structure with multiple ACL/ACE nodes
        createPage(HOME_PATH, "parent-page", "Parent Page", true);
        systemEditSession.getNode(HOME_PATH + "/parent-page").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createPage(HOME_PATH + "/parent-page", "child-page", "Child Page", true);
        systemEditSession.getNode(HOME_PATH + "/parent-page/child-page").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor-in-chief"));
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper parentPage = session.getNode(HOME_PATH + "/parent-page");
            JCRNodeWrapper home = session.getNode(HOME_PATH);
            parentPage.copy(home, "copied-parent", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Copied parent should exist", systemEditSession.nodeExists(HOME_PATH + "/copied-parent"));
        assertTrue("Parent ACE should be copied", systemEditSession.nodeExists(HOME_PATH + "/copied-parent/j:acl/GRANT_u_foo"));
        assertTrue("Child page should be copied", systemEditSession.nodeExists(HOME_PATH + "/copied-parent/child-page"));
        assertTrue("Child ACE should be copied", systemEditSession.nodeExists(HOME_PATH + "/copied-parent/child-page/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToMoveDeeplyNestedContentWithMultipleACLWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create nested structure with multiple ACL/ACE nodes
        createPage(HOME_PATH, "parent-page", "Parent Page", true);
        systemEditSession.getNode(HOME_PATH + "/parent-page").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createPage(HOME_PATH + "/parent-page", "child-page", "Child Page", true);
        systemEditSession.getNode(HOME_PATH + "/parent-page/child-page").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor-in-chief"));
        createPage(HOME_PATH, "dest-page", "Dest Page", false);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.move(HOME_PATH + "/parent-page", HOME_PATH + "/dest-page/parent-page");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("Original parent should not exist", systemEditSession.nodeExists(HOME_PATH + "/parent-page"));
        assertTrue("Moved parent should exist", systemEditSession.nodeExists(HOME_PATH + "/dest-page/parent-page"));
        assertTrue("Parent ACE should be moved", systemEditSession.nodeExists(HOME_PATH + "/dest-page/parent-page/j:acl/GRANT_u_foo"));
        assertTrue("Child page should be moved", systemEditSession.nodeExists(HOME_PATH + "/dest-page/parent-page/child-page"));
        assertTrue("Child ACE should be moved", systemEditSession.nodeExists(HOME_PATH + "/dest-page/parent-page/child-page/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToCopyPageWithACLWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL and ACE
        createPage(SITE_PATH, "pageA", "Page A", true);
        systemEditSession.getNode(SITE_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper pageA = session.getNode(SITE_PATH + "/pageA");
            JCRNodeWrapper site = session.getNode(SITE_PATH);
            pageA.copy(site, "pageB", false);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Copied page should exist", systemEditSession.nodeExists(SITE_PATH + "/pageB"));
        assertTrue("Copied page should have ACL with ACE", systemEditSession.nodeExists(SITE_PATH + "/pageB/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldBeAbleToMovePageWithACLWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create pageA with ACL and dest-page
        createPage(HOME_PATH, "pageA", "Page A", true);
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        createPage(HOME_PATH, "dest-page", "Dest Page", false);
        systemEditSession.save();

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.move(HOME_PATH + "/pageA", HOME_PATH + "/dest-page/pageA");
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("Original page should not exist", systemEditSession.nodeExists(HOME_PATH + "/pageA"));
        assertTrue("Moved page should exist", systemEditSession.nodeExists(HOME_PATH + "/dest-page/pageA"));
        assertTrue("Moved page should have ACL with ACE", systemEditSession.nodeExists(HOME_PATH + "/dest-page/pageA/j:acl/GRANT_u_foo"));
    }

    @Test
    public void shouldCopyContentWithoutACEWhenUserCannotReadACLOnSource() throws RepositoryException {
        getCleanSession();
        // Setup:
        // 1. Grant editor-with-acl-management to test user at site level
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));

        // 2. Create /home/parent with break inheritance and reader-default-only role for test user
        createPage(HOME_PATH, "parent", "Parent Page", true);
        systemEditSession.getNode(HOME_PATH + "/parent").setAclInheritanceBreak(true);
        systemEditSession.getNode(HOME_PATH + "/parent").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton("reader-default-only"));

        // 3. Create /home/parent/pageA with editor role granted to foo user
        createPage(HOME_PATH + "/parent", "pageA", "Page A", true);
        systemEditSession.getNode(HOME_PATH + "/parent/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        systemEditSession.save();

        // Verify setup: ACL and ACE exist on pageA
        assertTrue("pageA should have j:acl", systemEditSession.nodeExists(HOME_PATH + "/parent/pageA/j:acl"));
        assertTrue("pageA should have ACE for foo", systemEditSession.nodeExists(HOME_PATH + "/parent/pageA/j:acl/GRANT_u_foo"));

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            // User should be able to read pageA (has reader-default-only)
            assertTrue("User should be able to read pageA", session.nodeExists(HOME_PATH + "/parent/pageA"));

            // User should NOT be able to read pageA's j:acl (no privileged access due to inheritance break)
            assertFalse("User should NOT be able to read pageA's j:acl",
                    session.nodeExists(HOME_PATH + "/parent/pageA/j:acl"));

            // Copy pageA to /home/pageB
            JCRNodeWrapper pageA = session.getNode(HOME_PATH + "/parent/pageA");
            JCRNodeWrapper home = session.getNode(HOME_PATH);
            pageA.copy(home, "pageB", false);
            session.save();

            return null;
        }));

        getCleanSession();
        // Verify: pageB should exist
        assertTrue("pageB should have been created", systemEditSession.nodeExists(HOME_PATH + "/pageB"));

        // Verify: pageB should NOT have the ACE since user couldn't read them from source
        assertFalse("pageB should NOT have ACE (user couldn't read source ACE)",
                systemEditSession.nodeExists(HOME_PATH + "/pageB/j:acl/GRANT_u_foo"));
    }

    // ==================== Check mixin operations on jmix:accessControlled ====================

    @Test
    public void shouldBeAbleToAddAccessControlledMixinWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder without ACL mixin
        createContentFolder(CONTENTS_PATH, "test-folder", false);

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(CONTENTS_PATH + "/test-folder").addMixin(ACCESS_CONTROLLED_MIXIN);
            session.save();
            return null;
        }));

        getCleanSession();
        assertTrue("Folder should have accessControlled mixin", systemEditSession.getNode(CONTENTS_PATH + "/test-folder").isNodeType(ACCESS_CONTROLLED_MIXIN));
        assertTrue("j:acl node should have been auto-created", systemEditSession.nodeExists(CONTENTS_PATH + "/test-folder/j:acl"));
    }

    @Test
    public void shouldNotBeAbleToAddAccessControlledMixinWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder without ACL mixin
        createContentFolder(CONTENTS_PATH, "test-folder", false);

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to add jmix:accessControlled mixin", AccessDeniedException.class, () -> {
                session.getNode(CONTENTS_PATH + "/test-folder").addMixin(ACCESS_CONTROLLED_MIXIN);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertFalse("Folder should not have accessControlled mixin", systemEditSession.getNode(CONTENTS_PATH + "/test-folder").isNodeType(ACCESS_CONTROLLED_MIXIN));
    }

    @Test
    public void shouldBeAbleToRemoveAccessControlledMixinWithAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder with ACL mixin
        createContentFolder(CONTENTS_PATH, "test-folder", true);

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            session.getNode(CONTENTS_PATH + "/test-folder").removeMixin(ACCESS_CONTROLLED_MIXIN);
            session.save();
            return null;
        }));

        getCleanSession();
        assertFalse("Folder should not have accessControlled mixin", systemEditSession.getNode(CONTENTS_PATH + "/test-folder").isNodeType(ACCESS_CONTROLLED_MIXIN));
    }

    @Test
    public void shouldNotBeAbleToRemoveAccessControlledMixinWithoutAclManagement() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder with ACL mixin
        createContentFolder(CONTENTS_PATH, "test-folder", true);

        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to remove jmix:accessControlled mixin", AccessDeniedException.class, () -> {
                session.getNode(CONTENTS_PATH + "/test-folder").removeMixin(ACCESS_CONTROLLED_MIXIN);
                session.save();
            });
            return null;
        }));

        getCleanSession();
        assertTrue("Folder should still have accessControlled mixin", systemEditSession.getNode(CONTENTS_PATH + "/test-folder").isNodeType(ACCESS_CONTROLLED_MIXIN));
    }

    // ==================== Check read access to jnt:ace/jnt:acl ====================

    @Test
    public void shouldNotBeAbleToReadACLNodeWithoutPrivilegedRole() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder with ACL and grant foo user editor role
        createContentFolder(CONTENTS_PATH, "test-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/test-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        systemEditSession.save();

        // Revoke privileged role for test user on this folder (grant a role without privileged)
        systemEditSession.getNode(CONTENTS_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_NO_ACL));
        systemEditSession.getNode(CONTENTS_PATH + "/test-folder").setAclInheritanceBreak(true);
        systemEditSession.getNode(CONTENTS_PATH + "/test-folder").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_DEFAULT_READER));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            // User should be able to read the folder itself
            assertTrue("User should be able to read the folder", session.nodeExists(CONTENTS_PATH + "/test-folder"));

            // User should NOT be able to read the j:acl node
            assertFalse("User without privileged role should NOT be able to read j:acl node",
                    session.nodeExists(CONTENTS_PATH + "/test-folder/j:acl"));
            assertFalse("User without privileged role should NOT be able to read j:acl node",
                    session.nodeExists(CONTENTS_PATH + "/test-folder/j:acl/GRANT_u_foo"));

            // Also verify getNode throws PathNotFoundException
            assertThrows("getNode on j:acl should throw PathNotFoundException", PathNotFoundException.class, () -> {
                session.getNode(CONTENTS_PATH + "/test-folder/j:acl");
            });
            assertThrows("getNode on j:acl should throw PathNotFoundException", PathNotFoundException.class, () -> {
                session.getNode(CONTENTS_PATH + "/test-folder/j:acl/GRANT_u_foo");
            });

            return null;
        }));
    }

    @Test
    public void shouldBeAbleToReadACLNodeWithPrivilegedRole() throws RepositoryException {
        getCleanSession();
        // Setup: Create content folder with ACL and grant foo user editor role
        createContentFolder(CONTENTS_PATH, "test-folder", true);
        systemEditSession.getNode(CONTENTS_PATH + "/test-folder").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        systemEditSession.save();

        // Grant a role that includes privileged permission to test user
        systemEditSession.getNode(CONTENTS_PATH + "/test-folder").grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(ROLE_ACL));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            // User should be able to read the folder
            assertTrue("User should be able to read the folder", session.nodeExists(CONTENTS_PATH + "/test-folder"));

            // User WITH privileged role should be able to read the j:acl node
            assertTrue("User with privileged role should be able to read j:acl node",
                    session.nodeExists(CONTENTS_PATH + "/test-folder/j:acl"));
            assertTrue("User with privileged role should be able to read ACE node",
                    session.nodeExists(CONTENTS_PATH + "/test-folder/j:acl/GRANT_u_foo"));

            // Verify getNode works
            assertNotNull("getNode on j:acl should succeed",
                    session.getNode(CONTENTS_PATH + "/test-folder/j:acl"));
            assertNotNull("getNode on ACE should succeed",
                    session.getNode(CONTENTS_PATH + "/test-folder/j:acl/GRANT_u_foo"));

            return null;
        }));
    }

    // ==================== Check publication of content with ACE/ACL nodes ====================

    @Test
    public void shouldNotBeAbleToPublishACLNodesWithoutReadAccessControl() throws RepositoryException {
        publicationACLTest("not-privileged-reader-default-and-publish", false);
    }

    @Test
    public void shouldPublishACLNodesWithPrivilegedReaderPublishRole() throws RepositoryException {
        publicationACLTest("privileged-reader-default-and-publish", true);
    }

    @Test
    public void shouldPublishACLNodesWithEditorAclManagementAndPublishRole() throws RepositoryException {
        publicationACLTest("editor-with-acl-management-and-publish", true);
    }

    @Test
    public void shouldPublishACLNodesWithEditorNoAclManagementAndPublishRole() throws RepositoryException {
        publicationACLTest("editor-no-acl-management-and-publish", true);
    }

    private void publicationACLTest(String testRole, boolean shouldPass) throws RepositoryException {
        getCleanSession();

        // Setup: Create pageA and publish it initially
        createPage(HOME_PATH, "pageA", "Page A", false);
        String pageAUuid = systemEditSession.getNode(HOME_PATH + "/pageA").getIdentifier();
        systemEditSession.save();

        // Initial publish of pageA
        JCRPublicationService.getInstance().publishByMainId(pageAUuid, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);

        // Grant role "editor" to foo on pageA (this creates ACL/ACE)
        getCleanSession();
        systemEditSession.getNode(HOME_PATH + "/pageA").grantRoles(FOO_USER_NAME_PRINCIPAL, Collections.singleton("editor"));
        String aclUuid = systemEditSession.getNode(HOME_PATH + "/pageA/j:acl").getIdentifier();
        systemEditSession.save();

        // Verify ACL/ACE exists in default workspace
        assertTrue("ACL should exist in default", systemEditSession.nodeExists(HOME_PATH + "/pageA/j:acl"));
        assertTrue("ACE should exist in default", systemEditSession.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));

        // Grant role to test user at site level
        systemEditSession.getNode(SITE_PATH).grantRoles(TEST_USER_NAME_PRINCIPAL, Collections.singleton(testRole));
        systemEditSession.save();

        // Verify test user CAN read ACL/ACE nodes (has privileged access via parent role) or not (no privileged access)
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("User should be able to read pageA", session.nodeExists(HOME_PATH + "/pageA"));
            if (shouldPass) {
                assertTrue("User WITH privileged should be able to read j:acl", session.nodeExists(HOME_PATH + "/pageA/j:acl"));
                assertTrue("User WITH privileged should be able to read ACE", session.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));
            } else {
                assertFalse("User should NOT be able to read j:acl", session.nodeExists(HOME_PATH + "/pageA/j:acl"));
                assertFalse("User should NOT be able to read ACE", session.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));
            }
            return null;
        }));

        // Publish pageA as test user
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            if (shouldPass) {
                JCRPublicationService.getInstance().publishByMainId(aclUuid, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
            } else {
                assertThrows("publish should throw ItemNotFoundException", ItemNotFoundException.class, () -> {
                    JCRPublicationService.getInstance().publishByMainId(aclUuid, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, null);
                });
            }
            return null;
        }));

        // Verify ACL/ACE exists in live workspace or not depending on shouldPass
        getCleanSession();
        assertTrue("pageA should exist in live", systemLiveSession.nodeExists(HOME_PATH + "/pageA"));
        if (shouldPass) {
            assertTrue("ACL should exist in live after publication", systemLiveSession.nodeExists(HOME_PATH + "/pageA/j:acl"));
            assertTrue("ACE should exist in live after publication", systemLiveSession.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));
        } else {
            assertFalse("ACL should NOT exist in live after publication", systemLiveSession.nodeExists(HOME_PATH + "/pageA/j:acl"));
            assertFalse("ACE should NOT exist in live after publication", systemLiveSession.nodeExists(HOME_PATH + "/pageA/j:acl/GRANT_u_foo"));
        }
    }
}
