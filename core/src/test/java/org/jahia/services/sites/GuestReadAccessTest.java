package org.jahia.services.sites;

import com.github.javafaker.Faker;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.jahia.utils.security.AccessManagerTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests confirming that the {@code guest} user (anonymous visitor) is denied read access to
 * sensitive JCR nodes in the {@code live} workspace. For the {@code default} (edit) workspace,
 * the {@code guest} user is denied all read/write access.
 */
public class GuestReadAccessTest extends AbstractJUnitTest {

    private static JahiaUserManagerService userManagerService;
    private static JCRSessionFactory sessionFactory;

    /**
     * The guest (anonymous) user ({@code /users/guest}) — obtained once for the whole test class and shared across all tests.
     */
    private static JahiaUser guestUser;

    /**
     * System session on the {@code default} (edit) workspace. Re-created before each test.
     */
    private JCRSessionWrapper editSystemSession;

    /**
     * A fresh site key used to isolate each test.
     * Created in {@link #before()} and deleted in {@link #after()}.
     */
    private String siteKey;

    /**
     * Actual JCR path of th regular global user (e.g. {@code /users/hj/di/ac/<username>}).
     * Resolved once with the system session after user creation.
     */
    private String regularGlobalUserPath;

    private static final Faker faker = new Faker();

    // ==================== Lifecycle ====================

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManagerService = JahiaUserManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        resetSystemSession();

        guestUser = userManagerService.lookupUser(Constants.GUEST_USERNAME).getJahiaUser();
        assertNotNull("Guest user must exist in the repository", guestUser);
    }

    @Before
    public void before() throws Exception {
        resetSystemSession();
        siteKey = faker.internet().domainName();
        assertNotNull("Test site creation must succeed", TestHelper.createSite(siteKey));

        // Create a regular global user as an additional sensitive-node target (complements /users/root).
        // NB: it is automatically published to the live WS
        JahiaUser regularGlobalUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();
        editSystemSession.save();
        resetSystemSession();

        // Resolve the split path once the user is created
        regularGlobalUserPath = userManagerService.lookupUser(regularGlobalUser.getUsername(), editSystemSession).getPath();
        assertNotNull("Regular global user path must be resolvable", regularGlobalUserPath);
    }

    @After
    public void after() throws Exception {
        TestHelper.deleteSite(siteKey);
        resetSystemSession();
        userManagerService.deleteUser(userManagerService.lookupUserByPath(regularGlobalUserPath).getPath(), editSystemSession);
        editSystemSession.save();
    }

    // ==================== /users subtree — guest MUST be denied (both WS) ====================

    @Test
    public void GIVEN_guest_user_WHEN_reading_root_user_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/users/root", Constants.LIVE_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_root_user_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/users/root", Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_a_regular_global_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        assertGuestIsDenied(regularGlobalUserPath, Constants.LIVE_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_a_regular_global_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        assertGuestIsDenied(regularGlobalUserPath, Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_browsing_global_users_folder_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/users", Constants.LIVE_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_browsing_global_users_folder_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/users", Constants.EDIT_WORKSPACE);
    }

    // ==================== /settings — guest MUST be denied (both WS) ====================

    @Test
    public void GIVEN_guest_user_WHEN_reading_settings_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/settings", Constants.LIVE_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_settings_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/settings", Constants.EDIT_WORKSPACE);
    }

    // ==================== Per-site sensitive folders — guest MUST be denied (both WS) ====================

    @Test
    public void GIVEN_guest_user_WHEN_browsing_site_users_folder_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites/" + siteKey + "/users", Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_browsing_site_users_folder_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites/" + siteKey + "/users", Constants.LIVE_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_browsing_site_groups_folder_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites/" + siteKey + "/groups", Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_browsing_site_groups_folder_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites/" + siteKey + "/groups", Constants.LIVE_WORKSPACE);
    }

    // ==================== Public nodes — guest MUST retain read access on live WS but not on edit WS ====================

    @Test
    public void GIVEN_guest_user_WHEN_reading_sites_folder_in_live_workspace_THEN_should_be_allowed() throws RepositoryException {
        assertGuestIsAllowedInLiveWS("/sites");
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_sites_folder_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites", Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_a_site_node_in_live_workspace_THEN_should_be_allowed() throws RepositoryException {
        assertGuestIsAllowedInLiveWS("/sites/" + siteKey);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_a_site_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/sites/" + siteKey, Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_modules_folder_in_live_workspace_THEN_should_be_allowed() throws RepositoryException {
        assertGuestIsAllowedInLiveWS("/modules");
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_modules_folder_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/modules", Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_guest_user_node_in_live_workspace_THEN_should_be_allowed() throws RepositoryException {
        assertGuestIsAllowedInLiveWS("/users/guest");
    }

    @Test
    public void GIVEN_guest_user_WHEN_reading_guest_user_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        assertGuestIsDenied("/users/guest", Constants.EDIT_WORKSPACE);
    }

    // ==================== Helpers ====================

    /**
     * Asserts that the guest user cannot see the given node path in the given workspace.
     *
     * <p>In JCR, when read access is denied {@code session.nodeExists(path)} returns
     * {@code false} (the node is hidden rather than raising an exception). This is the correct
     * observable behaviour to assert for access-control enforcement.
     */
    private void assertGuestIsDenied(String nodePath, String workspace) throws RepositoryException {
        assertFalse("Guest should NOT be able to read [" + nodePath + "] in workspace [" + workspace + "]",
                readNodeAsGuest(nodePath, workspace));
    }

    /**
     * Asserts that the guest user can see the given node path in the live workspace (the guest user has no access in the edit workspace).
     */
    private void assertGuestIsAllowedInLiveWS(String nodePath) throws RepositoryException {
        assertTrue("Guest SHOULD be able to read [" + nodePath + "] in workspace [" + Constants.LIVE_WORKSPACE + "]",
                readNodeAsGuest(nodePath, Constants.LIVE_WORKSPACE));
    }

    /**
     * Opens a JCR user-session as {@link #guestUser} and returns whether the given path is
     * visible ({@code session.nodeExists(path)}).
     *
     * <p>{@link AccessManagerTestUtils#setCurrentUserCallback} is used to keep the
     * {@code JCRSessionFactory} current-user thread-local in sync with the open session,
     * which is required for the Jahia ACL evaluation layer to use the correct principal.
     */
    private boolean readNodeAsGuest(String nodePath, String workspace) throws RepositoryException {
        return (Boolean) JCRTemplate.getInstance().doExecute(guestUser, workspace, null,
                AccessManagerTestUtils.setCurrentUserCallback(guestUser, session -> session.nodeExists(nodePath)));
    }

    private void resetSystemSession() throws RepositoryException {
        sessionFactory.closeAllSessions();
        editSystemSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null);
    }
}

