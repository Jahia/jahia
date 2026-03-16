package org.jahia.utils.security;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.*;

import javax.jcr.RepositoryException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for verifying admin and system principal bypass in AccessManagerUtils.
 * - System principals should always be granted access (unless deniedPathes is set)
 * - root and Admin users should always be granted access after initial checks
 */
public class AccessManagerUtilsAdminBypassTest extends AbstractJUnitTest {
    private static final String SITE_KEY = "adminBypassTestSite";
    private static final String SITE_PATH = "/sites/" + SITE_KEY;
    private static final String HOME_PATH = SITE_PATH + "/home";
    private static final String TEST_USER_NAME = "adminBypassTestUser";
    private static final String PASSWORD = "password";

    private static JahiaUserManagerService userManager;
    private static JahiaGroupManagerService groupManager;
    private static JCRSessionFactory sessionFactory;
    private static JahiaUser testUser;
    private static JCRSessionWrapper systemEditSession;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManager = JahiaUserManagerService.getInstance();
        groupManager = JahiaGroupManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        getCleanSession();

        // Create test user (non-admin)
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
        // Clear any denied paths
        AccessManagerUtils.setDeniedPaths(null);
    }

    @After
    public void after() throws Exception {
        AccessManagerUtils.setDeniedPaths(null);
        TestHelper.deleteSite(SITE_KEY);
        if (systemEditSession.nodeExists("/testSystemNode")) {
            systemEditSession.getNode("/testSystemNode").remove();
            systemEditSession.save();
        }
    }

    private void getCleanSession() throws RepositoryException {
        sessionFactory.closeAllSessions();
        systemEditSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null);
    }

    // ==================== Tests for System Principal Bypass ====================

    @Test
    public void systemSessionShouldAlwaysHaveReadAccess() throws RepositoryException {
        getCleanSession();
        // Create a page with no explicit permissions for anyone
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("systemTestPage", "jnt:page");
        page.setProperty("jcr:title", "System Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // System session should always be able to read
        assertTrue("System session should be able to read any node",
                systemEditSession.nodeExists(HOME_PATH + "/systemTestPage"));
    }

    @Test
    public void systemSessionShouldAlwaysHaveWriteAccess() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("systemWritePage", "jnt:page");
        page.setProperty("jcr:title", "System Write Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // System session should be able to modify
        getCleanSession();
        JCRNodeWrapper existingPage = systemEditSession.getNode(HOME_PATH + "/systemWritePage");
        existingPage.setProperty("jcr:title", "Modified Title");
        systemEditSession.save();

        // Verify modification
        getCleanSession();
        assertEquals("Modified Title",
                systemEditSession.getNode(HOME_PATH + "/systemWritePage").getProperty("jcr:title").getString());
    }

    @Test
    public void systemSessionShouldBeAffectedByDeniedPaths() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("deniedSystemPage", "jnt:page");
        page.setProperty("jcr:title", "Denied System Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Set denied paths - this should affect even system sessions
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/deniedSystemPage"));

        // System session should be affected by denied paths
        getCleanSession();
        assertFalse("System session should be affected by denied paths",
                systemEditSession.nodeExists(HOME_PATH + "/deniedSystemPage"));

        // Clear denied paths
        AccessManagerUtils.setDeniedPaths(null);

        // Now it should be accessible again
        getCleanSession();
        assertTrue("System session should access page after denied paths cleared",
                systemEditSession.nodeExists(HOME_PATH + "/deniedSystemPage"));
    }

    @Test
    public void systemSessionShouldBeAbleToAbleWriteSystemNode() throws RepositoryException {
        getCleanSession();
        // Create a test node at root level
        systemEditSession.getRootNode().addNode("testSystemNode", "jnt:contentFolder");
        systemEditSession.getNode("/testSystemNode").addMixin("jmix:systemNode");
        systemEditSession.save();

        assertTrue("Test node should exist", systemEditSession.nodeExists("/testSystemNode"));

        // System session should be able to delete it
        getCleanSession();
        systemEditSession.getNode("/testSystemNode").remove();
        systemEditSession.save();

        getCleanSession();
        assertFalse("Test node should have been deleted by system session", systemEditSession.nodeExists("/testSystemNode"));
    }

    // ==================== Tests for Root User Bypass ====================

    @Test
    public void rootUserShouldHaveAccessToAllNodes() throws RepositoryException {
        getCleanSession();
        // Create a page with no permissions granted
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("adminTestPage", "jnt:page");
        page.setProperty("jcr:title", "Admin Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Get the root user (admin)
        JahiaUser rootUser = userManager.lookupRootUser().getJahiaUser();

        // Admin should be able to access any node
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            assertTrue("Root should be able to read any node",
                    session.nodeExists(HOME_PATH + "/adminTestPage"));
            return null;
        }));
    }

    @Test
    public void rootUserShouldHaveWriteAccessToAllNodes() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("adminWritePage", "jnt:page");
        page.setProperty("jcr:title", "Root Write Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Get the root user (admin)
        JahiaUser rootUser = userManager.lookupRootUser().getJahiaUser();

        // Admin should be able to modify any node
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, Locale.ENGLISH, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            JCRNodeWrapper existingPage = session.getNode(HOME_PATH + "/adminWritePage");
            existingPage.setProperty("jcr:title", "Root Modified Title");
            session.save();
            return null;
        }));

        // Verify modification
        getCleanSession();
        assertEquals("Root Modified Title",
                systemEditSession.getNode(HOME_PATH + "/adminWritePage").getProperty("jcr:title").getString());
    }

    @Test
    public void rootUserShouldBeAbleToAccessACLNodes() throws RepositoryException {
        getCleanSession();
        // Site already has ACL nodes by default

        // Get the root user (admin)
        JahiaUser rootUser = userManager.lookupRootUser().getJahiaUser();

        // Admin should be able to access ACL nodes
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            assertTrue("Root should be able to read j:acl node",
                    session.nodeExists(SITE_PATH + "/j:acl"));
            return null;
        }));
    }

    @Test
    public void rootUserShouldBeAffectedByDeniedPaths() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("deniedSystemPage", "jnt:page");
        page.setProperty("jcr:title", "Denied System Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Set denied paths - this should affect even root user
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/deniedSystemPage"));

        // Get the root user (admin)
        JahiaUser rootUser = userManager.lookupRootUser().getJahiaUser();
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            assertFalse("Root user should be affected by denied paths", session.nodeExists(HOME_PATH + "/deniedSystemPage"));
            return null;
        }));

        // Clear denied paths
        AccessManagerUtils.setDeniedPaths(null);

        // Now it should be accessible again
        getCleanSession();
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            assertTrue("Root user should be affected by denied paths", session.nodeExists(HOME_PATH + "/deniedSystemPage"));
            return null;
        }));
    }

    @Test
    public void rootUserShouldNotBeAbleToWriteSystemNode() throws RepositoryException {
        getCleanSession();
        // Create a test node at root level using system session
        systemEditSession.getRootNode().addNode("testSystemNode", "jnt:contentFolder");
        systemEditSession.getNode("/testSystemNode").addMixin("jmix:systemNode");
        systemEditSession.save();

        assertTrue("Test node should exist", systemEditSession.nodeExists("/testSystemNode"));

        // Get the root user
        JahiaUser rootUser = userManager.lookupRootUser().getJahiaUser();

        // Root user should NOT be able to delete root-level nodes
        JCRTemplate.getInstance().doExecute(rootUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(rootUser, session -> {
            assertTrue("Root user should be able to read jmix:systemNode", session.nodeExists("/testSystemNode"));
            assertThrows("Root user should not be able to delete jmix:systemNode", javax.jcr.AccessDeniedException.class, () -> {
                session.getNode("/testSystemNode").remove();
                session.save();
            });
            return null;
        }));

        // Verify node still exists
        getCleanSession();
        assertTrue("Test node should still exist after root user delete attempt", systemEditSession.nodeExists("/testSystemNode"));
    }

    // ==================== Tests for administrators Bypass ====================

    @Test
    public void adminUserShouldHaveAccessToAllNodes() throws RepositoryException {
        getCleanSession();
        // Create a page with no permissions granted
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("adminTestPage", "jnt:page");
        page.setProperty("jcr:title", "Admin Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // put testUser in administrators group
        groupManager.lookupGroup(null, "administrators", systemEditSession).addMember(testUser);
        systemEditSession.save();

        // Admin should be able to access any node
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("Admin should be able to read any node", session.nodeExists(HOME_PATH + "/adminTestPage"));
            return null;
        }));
    }

    @Test
    public void adminUserShouldHaveWriteAccessToAllNodes() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("adminWritePage", "jnt:page");
        page.setProperty("jcr:title", "Admin Write Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // put testUser in administrators group
        groupManager.lookupGroup(null, "administrators", systemEditSession).addMember(testUser);
        systemEditSession.save();

        // Admin should be able to modify any node
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, Locale.ENGLISH, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            JCRNodeWrapper existingPage = session.getNode(HOME_PATH + "/adminWritePage");
            existingPage.setProperty("jcr:title", "Admin Modified Title");
            session.save();
            return null;
        }));

        // Verify modification
        getCleanSession();
        assertEquals("Admin Modified Title",
                systemEditSession.getNode(HOME_PATH + "/adminWritePage").getProperty("jcr:title").getString());
    }

    @Test
    public void adminUserShouldBeAbleToAccessACLNodes() throws RepositoryException {
        getCleanSession();
        // Site already has ACL nodes by default

        // put testUser in administrators group
        groupManager.lookupGroup(null, "administrators", systemEditSession).addMember(testUser);
        systemEditSession.save();

        // Admin should be able to access ACL nodes
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("Admin should be able to read j:acl node",
                    session.nodeExists(SITE_PATH + "/j:acl"));
            return null;
        }));
    }

    @Test
    public void adminUserShouldBeAffectedByDeniedPaths() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("deniedSystemPage", "jnt:page");
        page.setProperty("jcr:title", "Denied System Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Set denied paths - this should affect even root user
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/deniedSystemPage"));

        // Get the root user (admin)
        // put testUser in administrators group
        groupManager.lookupGroup(null, "administrators", systemEditSession).addMember(testUser);
        systemEditSession.save();
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertFalse("Root user should be affected by denied paths", session.nodeExists(HOME_PATH + "/deniedSystemPage"));
            return null;
        }));

        // Clear denied paths
        AccessManagerUtils.setDeniedPaths(null);

        // Now it should be accessible again
        getCleanSession();
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("Root user should be affected by denied paths", session.nodeExists(HOME_PATH + "/deniedSystemPage"));
            return null;
        }));
    }

    @Test
    public void adminUserShouldNotBeAbleToWriteSystemNode() throws RepositoryException {
        getCleanSession();
        // Create a test node at root level using system session
        systemEditSession.getRootNode().addNode("testSystemNode", "jnt:contentFolder");
        systemEditSession.getNode("/testSystemNode").addMixin("jmix:systemNode");
        systemEditSession.save();

        assertTrue("Test node should exist", systemEditSession.nodeExists("/testSystemNode"));

        // Put testUser in administrators group
        groupManager.lookupGroup(null, "administrators", systemEditSession).addMember(testUser);
        systemEditSession.save();

        // Root user should NOT be able to delete root-level nodes
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, AccessManagerTestUtils.setCurrentUserCallback(testUser, session -> {
            assertTrue("Root user should be able to read jmix:systemNode", session.nodeExists("/testSystemNode"));
            assertThrows("Root user should not be able to delete jmix:systemNode", javax.jcr.AccessDeniedException.class, () -> {
                session.getNode("/testSystemNode").remove();
                session.save();
            });
            return null;
        }));

        // Verify node still exists
        getCleanSession();
        assertTrue("Test node should still exist after root user delete attempt", systemEditSession.nodeExists("/testSystemNode"));
    }
}

