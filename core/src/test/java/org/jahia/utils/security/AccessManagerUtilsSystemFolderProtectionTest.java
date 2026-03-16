package org.jahia.utils.security;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.*;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test suite for verifying that write access is always denied on system folders (jmix:systemNode).
 */
public class AccessManagerUtilsSystemFolderProtectionTest extends AbstractJUnitTest {
    private static final String SITE_KEY = "systemFolderTestSite";
    private static final String SITE_PATH = "/sites/" + SITE_KEY;
    private static final String HOME_PATH = SITE_PATH + "/home";
    private static final String TEST_USER_NAME = "systemFolderTestUser";
    private static final String PASSWORD = "password";

    private static JahiaUserManagerService userManager;
    private static JCRSessionFactory sessionFactory;
    private static JahiaUser testUser;
    private static JCRSessionWrapper systemEditSession;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManager = JahiaUserManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        getCleanSession();
        testUser = userManager.createUser(TEST_USER_NAME, PASSWORD, new Properties(), systemEditSession).getJahiaUser();
        systemEditSession.save();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        getCleanSession();
        userManager.deleteUser(userManager.lookupUser(TEST_USER_NAME).getPath(), systemEditSession);
        systemEditSession.save();
    }

    @Before
    public void before() throws Exception {
        getCleanSession();
        TestHelper.createSite(SITE_KEY).getSiteKey();
        // Clear any denied paths from previous tests
        AccessManagerUtils.setDeniedPaths(null);
    }

    @After
    public void after() throws Exception {
        // Clear denied paths
        AccessManagerUtils.setDeniedPaths(null);
        TestHelper.deleteSite(SITE_KEY);
    }

    private void getCleanSession() throws RepositoryException {
        sessionFactory.closeAllSessions();
        systemEditSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null);
    }

    @Test
    public void shouldDenyWriteAccessOnSystemFolder() throws RepositoryException {
        getCleanSession();
        JCRNodeWrapper folder = systemEditSession.getNode("/").addNode("systemFolder", "jnt:contentFolder");
        folder.addMixin("jmix:systemNode");
        systemEditSession.save();

        systemEditSession.getNode("/systemFolder").grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("User should be able to read system folder", session.nodeExists("/systemFolder"));
            return null;
        }));

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to modify properties on system folder", AccessDeniedException.class, () -> {
                JCRNodeWrapper sysFolder = session.getNode("/systemFolder");
                sysFolder.setProperty("jcr:description", "test");
                session.save();
            });
            return null;
        }));
    }

    @Test
    public void shouldDenyRemoveNodeOnSystemFolder() throws RepositoryException {
        getCleanSession();
        JCRNodeWrapper folder = systemEditSession.getNode("/").addNode("systemFolder", "jnt:contentFolder");
        folder.addMixin("jmix:systemNode");
        systemEditSession.save();

        systemEditSession.getNode("/systemFolder").grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("User should be able to read system folder", session.nodeExists("/systemFolder"));
            return null;
        }));

        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to modify properties on system folder", AccessDeniedException.class, () -> {
                session.getNode("/systemFolder").remove();
                session.save();
            });
            return null;
        }));
    }

    // ==================== Tests for auto-created system nodes ====================

    @Test
    public void shouldDenyRemoveAutoCreatedSystemNode() throws RepositoryException {
        getCleanSession();

        // Verify the auto-created system node exists and has the mixin
        String siteUsersPath = SITE_PATH + "/groups/site-users";
        assertTrue("site-users node should exist", systemEditSession.nodeExists(siteUsersPath));
        assertTrue("site-users should have jmix:systemNode mixin", systemEditSession.getNode(siteUsersPath).isNodeType("jmix:systemNode"));

        // Grant editor role to test user on the site
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Test user should be able to read the node
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("User should be able to read site-users node", session.nodeExists(siteUsersPath));
            return null;
        }));

        // Test user should NOT be able to remove the auto-created system node
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to remove auto-created system node", AccessDeniedException.class, () -> {
                session.getNode(siteUsersPath).remove();
                session.save();
            });
            return null;
        }));

        // Verify node still exists
        getCleanSession();
        assertTrue("site-users node should still exist after removal attempt", systemEditSession.nodeExists(siteUsersPath));
    }

    @Test
    public void shouldDenyRemoveSystemNodeMixin() throws RepositoryException {
        getCleanSession();

        String siteUsersPath = SITE_PATH + "/groups/site-users";
        assertTrue("site-users should have jmix:systemNode mixin",
                systemEditSession.getNode(siteUsersPath).isNodeType("jmix:systemNode"));

        // Grant editor role to test user on the site
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Test user should NOT be able to remove the jmix:systemNode mixin
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to remove jmix:systemNode mixin", AccessDeniedException.class, () -> {
                session.getNode(siteUsersPath).removeMixin("jmix:systemNode");
                session.save();
            });
            return null;
        }));

        // Verify mixin still exists
        getCleanSession();
        assertTrue("jmix:systemNode mixin should still exist after removal attempt", systemEditSession.getNode(siteUsersPath).isNodeType("jmix:systemNode"));
    }

    @Test
    public void shouldDenyAddMixinOnSystemNode() throws RepositoryException {
        getCleanSession();

        String siteUsersPath = SITE_PATH + "/groups/site-users";
        assertTrue("site-users should have jmix:systemNode mixin", systemEditSession.getNode(siteUsersPath).isNodeType("jmix:systemNode"));

        // Grant editor role to test user on the site
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Test user should NOT be able to remove the jmix:systemNode mixin
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to add jmix:tagged mixin on a system node", AccessDeniedException.class, () -> {
                session.getNode(siteUsersPath).addMixin("jmix:tagged");
                session.save();
            });
            return null;
        }));

        // Verify mixin still exists
        getCleanSession();
        assertFalse("jmix:tagged mixin should not have been added", systemEditSession.getNode(siteUsersPath).isNodeType("jmix:tagged"));
    }

    @Test
    public void shouldDenyModifyPropertiesOnAutoCreatedSystemNode() throws RepositoryException {
        getCleanSession();

        String siteUsersPath = SITE_PATH + "/groups/site-users";

        // Grant editor role to test user on the site
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Test user should NOT be able to modify properties on the system node
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertThrows("Should not be able to modify properties on auto-created system node", AccessDeniedException.class, () -> {
                JCRNodeWrapper siteUsers = session.getNode(siteUsersPath);
                siteUsers.setProperty("j:hidden", true);
                session.save();
            });
            return null;
        }));
    }
}


