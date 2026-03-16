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

import javax.jcr.RepositoryException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for verifying the deniedPathes ThreadLocal functionality in AccessManagerUtils.
 * The deniedPathes mechanism allows temporarily hiding certain paths from a session,
 * making them appear as access denied even if the user would normally have access.
 */
public class AccessManagerUtilsDeniedPathsTest extends AbstractJUnitTest {
    private static final String SITE_KEY = "deniedPathsTestSite";
    private static final String SITE_PATH = "/sites/" + SITE_KEY;
    private static final String HOME_PATH = SITE_PATH + "/home";
    private static final String TEST_USER_NAME = "deniedPathsTestUser";
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

        // Create test user
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

    // ==================== Tests for deniedPathes functionality ====================

    @Test
    public void shouldDenyAccessToPathInDeniedPaths() throws RepositoryException {
        getCleanSession();
        // Create a page that user normally has access to
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("testPage", "jnt:page");
        page.setProperty("jcr:title", "Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Grant editor role to test user
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Verify user can normally access the page
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertTrue("User should be able to access page before denial",
                    session.nodeExists(HOME_PATH + "/testPage"));
            return null;
        });

        // Set denied paths
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/testPage"));

        // Verify user cannot access the page when it's in denied paths
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertFalse("User should NOT be able to access page when in denied paths",
                    session.nodeExists(HOME_PATH + "/testPage"));
            return null;
        });
    }

    @Test
    public void shouldRestoreAccessWhenDeniedPathsCleared() throws RepositoryException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("testPage", "jnt:page");
        page.setProperty("jcr:title", "Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Grant editor role to test user
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Set denied paths
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/testPage"));

        // Verify access is denied
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertFalse("User should NOT be able to access page when in denied paths",
                    session.nodeExists(HOME_PATH + "/testPage"));
            return null;
        });

        // Clear denied paths
        AccessManagerUtils.setDeniedPaths(null);

        // Verify access is restored
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertTrue("User should be able to access page after denied paths cleared",
                    session.nodeExists(HOME_PATH + "/testPage"));
            return null;
        });
    }

    @Test
    public void shouldDenyAccessToMultiplePaths() throws RepositoryException {
        getCleanSession();
        // Create multiple pages
        JCRNodeWrapper page1 = systemEditSession.getNode(HOME_PATH).addNode("page1", "jnt:page");
        page1.setProperty("jcr:title", "Page 1");
        page1.setProperty("j:templateName", "simple");

        JCRNodeWrapper page2 = systemEditSession.getNode(HOME_PATH).addNode("page2", "jnt:page");
        page2.setProperty("jcr:title", "Page 2");
        page2.setProperty("j:templateName", "simple");

        JCRNodeWrapper page3 = systemEditSession.getNode(HOME_PATH).addNode("page3", "jnt:page");
        page3.setProperty("jcr:title", "Page 3");
        page3.setProperty("j:templateName", "simple");

        systemEditSession.save();

        // Grant editor role to test user
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Set multiple denied paths (page1 and page3, but not page2)
        AccessManagerUtils.setDeniedPaths(Arrays.asList(
                HOME_PATH + "/page1",
                HOME_PATH + "/page3"
        ));

        // Verify access
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertFalse("User should NOT be able to access page1", session.nodeExists(HOME_PATH + "/page1"));
            assertTrue("User should be able to access page2", session.nodeExists(HOME_PATH + "/page2"));
            assertFalse("User should NOT be able to access page3", session.nodeExists(HOME_PATH + "/page3"));
            return null;
        });
    }

    @Test
    public void shouldNotAffectOtherPathsWhenDeniedPathsSet() throws RepositoryException {
        getCleanSession();
        // Create pages
        JCRNodeWrapper deniedPage = systemEditSession.getNode(HOME_PATH).addNode("deniedPage", "jnt:page");
        deniedPage.setProperty("jcr:title", "Denied Page");
        deniedPage.setProperty("j:templateName", "simple");

        JCRNodeWrapper allowedPage = systemEditSession.getNode(HOME_PATH).addNode("allowedPage", "jnt:page");
        allowedPage.setProperty("jcr:title", "Allowed Page");
        allowedPage.setProperty("j:templateName", "simple");

        systemEditSession.save();

        // Grant editor role to test user
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        // Deny only one path
        AccessManagerUtils.setDeniedPaths(Collections.singletonList(HOME_PATH + "/deniedPage"));

        // Verify
        JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
            assertFalse("Denied page should not be accessible", session.nodeExists(HOME_PATH + "/deniedPage"));
            assertTrue("Allowed page should still be accessible", session.nodeExists(HOME_PATH + "/allowedPage"));
            assertTrue("Home should still be accessible", session.nodeExists(HOME_PATH));
            assertTrue("Site should still be accessible", session.nodeExists(SITE_PATH));
            return null;
        });
    }

    @Test
    public void deniedPathsShouldBeThreadLocal() throws RepositoryException, InterruptedException {
        getCleanSession();
        // Create a page
        JCRNodeWrapper page = systemEditSession.getNode(HOME_PATH).addNode("threadTestPage", "jnt:page");
        page.setProperty("jcr:title", "Thread Test Page");
        page.setProperty("j:templateName", "simple");
        systemEditSession.save();

        // Grant editor role to test user
        systemEditSession.getNode(SITE_PATH).grantRoles("u:" + TEST_USER_NAME, Collections.singleton("editor"));
        systemEditSession.save();

        final String pagePath = HOME_PATH + "/threadTestPage";
        final boolean[] thread1Result = new boolean[1];
        final boolean[] thread2Result = new boolean[1];

        // Thread 1: Set denied paths and check
        Thread thread1 = new Thread(() -> {
            try {
                AccessManagerUtils.setDeniedPaths(Collections.singletonList(pagePath));
                JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
                    thread1Result[0] = session.nodeExists(pagePath);
                    return null;
                });
            } catch (RepositoryException e) {
                throw new RuntimeException("Thread 1 failed", e);
            }
        });

        // Thread 2: No denied paths set, should have access
        Thread thread2 = new Thread(() -> {
            try {
                // Don't set any denied paths in this thread
                JCRTemplate.getInstance().doExecute(testUser, Constants.EDIT_WORKSPACE, null, session -> {
                    thread2Result[0] = session.nodeExists(pagePath);
                    return null;
                });
            } catch (RepositoryException e) {
                throw new RuntimeException("Thread 2 failed", e);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        assertFalse("Thread 1 should NOT have access (denied paths set)", thread1Result[0]);
        assertTrue("Thread 2 should have access (no denied paths in that thread)", thread2Result[0]);
    }
}


