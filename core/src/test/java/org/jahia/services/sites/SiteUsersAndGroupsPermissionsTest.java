package org.jahia.services.sites;

import com.github.javafaker.Faker;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.jahia.utils.security.AccessManagerTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests that write access to site user and group nodes is correctly restricted:
 * <ul>
 *   <li>An {@code editor} user should be denied from modifying nodes under
 *       {@code /sites/<siteName>/users} and {@code /sites/<siteName>/groups}.</li>
 *   <li>A {@code site-administrator} user should be allowed to perform the same operations.</li>
 *   <li>Each user retains write access to their own user node (self-edit preservation).</li>
 * </ul>
 */
public class SiteUsersAndGroupsPermissionsTest extends AbstractJUnitTest {

    private static JahiaUserManagerService userManagerService;
    private static JahiaGroupManagerService groupManagerService;
    private static JCRSessionFactory sessionFactory;

    /**
     * Server (global) user created under {@code /users/...} with the {@code editor} role on the site.
     * Created once for all tests.
     */
    private static JahiaUser serverEditorUser;
    /**
     * User with the {@code site-administrator} role on the site. Created once for all tests.
     */
    private static JahiaUser siteAdministratorUser;

    /**
     * JCR session of the edit workspace.
     */
    private JCRSessionWrapper editSystemSession;

    /**
     * JCR session of the live workspace.
     */
    private JCRSessionWrapper liveSystemSession;
    private String siteKey;

    /**
     * Regular user at the site level, re-created for each test. Acts as the target in write-access tests.
     */
    private JahiaUser siteUser;
    /**
     * Site user created under {@code /sites/<siteKey>/users/...} with the {@code editor} role,
     * re-created for each test. Used for self-edit preservation tests.
     */
    private JahiaUser siteEditorUser;
    /**
     * Regular group at the site level, re-created for each test. Acts as the target in group write-access tests.
     */
    private JahiaGroup siteGroup;

    /**
     * A second site, re-created for each test. Used in cross-site permission scenarios.
     */
    private String differentSiteKey;
    /**
     * A user belonging to {@link #differentSiteKey}, re-created for each test. Acts as target in cross-site tests.
     */
    private JahiaUser differentSiteUser;
    /**
     * A plain server user (under {@code /users/...}) with no site roles, re-created for each test.
     * Acts as the target in server-user permission scenarios.
     */
    private JahiaUser serverUser;

    private static final Faker faker = new Faker();

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManagerService = JahiaUserManagerService.getInstance();
        groupManagerService = JahiaGroupManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        resetSystemSession();

        serverEditorUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();
        siteAdministratorUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();
        editSystemSession.save();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        assertTrue(
                userManagerService.deleteUser(userManagerService.lookupUser(serverEditorUser.getUsername()).getPath(), editSystemSession));
        assertTrue(userManagerService.deleteUser(userManagerService.lookupUser(siteAdministratorUser.getUsername()).getPath(),
                editSystemSession));
        editSystemSession.save();
    }

    @Before
    public void before() throws Exception {
        resetSystemSession();
        siteKey = faker.internet().domainName();
        JahiaSite site = TestHelper.createSite(siteKey);
        assertNotNull(site);

        Properties siteUserProps = new Properties();
        siteUserProps.put("j:lastName", faker.name().firstName());
        siteUser = userManagerService.createUser(faker.name().username(), siteKey, faker.internet().password(), siteUserProps,
                editSystemSession).getJahiaUser();

        Properties siteEditorUserProps = new Properties();
        siteEditorUserProps.put("j:lastName", faker.name().firstName());
        siteEditorUser = userManagerService.createUser(faker.name().username(), siteKey, faker.internet().password(), siteEditorUserProps,
                editSystemSession).getJahiaUser();

        editSystemSession.getNode(site.getJCRLocalPath())
                .grantRoles("u:" + serverEditorUser.getUsername(), Collections.singleton("editor"));
        editSystemSession.getNode(site.getJCRLocalPath()).grantRoles("u:" + siteEditorUser.getUsername(), Collections.singleton("editor"));

        groupManagerService.getAdministratorGroup(siteKey, editSystemSession).addMember(siteAdministratorUser);

        siteGroup = groupManagerService.createGroup(siteKey, faker.name().name(), new Properties(), false, editSystemSession)
                .getJahiaGroup();

        differentSiteKey = faker.internet().domainWord();
        JahiaSite differentSite = TestHelper.createSite(differentSiteKey);
        assertNotNull(differentSite);
        differentSiteUser = userManagerService.createUser(faker.name().username(), differentSiteKey, faker.internet().password(),
                new Properties(), editSystemSession).getJahiaUser();

        serverUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();

        editSystemSession.save();
        resetSystemSession();
    }

    @After
    public void after() throws Exception {
        TestHelper.deleteSite(siteKey);
        TestHelper.deleteSite(differentSiteKey);
        resetSystemSession();
        assertTrue(userManagerService.deleteUser(userManagerService.lookupUser(serverUser.getUsername()).getPath(), editSystemSession));
        editSystemSession.save();
    }

    // ==================== Read access ====================

    /**
     * e.g. {@code /users/serverEditor} can read {@code /users/serverEditor}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_own_user_node_THEN_should_be_allowed() throws RepositoryException {
        String serverEditorUserPath = userManagerService.lookupUser(serverEditorUser.getUsername(), editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(serverEditorUserPath)));

        assertTrue("Server editor user should be able to read their own user node", canRead);
    }

    /**
     * e.g. {@code /users/serverEditor} can read {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_same_site_user_node_THEN_should_be_allowed() throws RepositoryException {
        String siteUserPath = userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(siteUserPath)));

        assertTrue("Server editor user should be able to read a user in the same site", canRead);
    }

    /**
     * e.g. {@code /users/serverEditor} can read {@code /sites/siteB/users/siteUser}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_different_site_user_node_THEN_should_be_allowed() throws RepositoryException {
        String differentSiteUserPath = userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                .getPath();
        assertNotNull(differentSiteUserPath);

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(differentSiteUserPath)));

        assertTrue("Server editor user should be able to read a user from a different site", canRead);
    }

    /**
     * e.g. {@code /users/serverEditor} can read {@code /users/serverUser}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_server_user_node_THEN_should_be_allowed() throws RepositoryException {
        String serverUserPath = userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).getPath();
        assertNotNull(serverUserPath);

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(serverUserPath)));

        assertTrue("Server editor user should be able to read a server user node", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /sites/siteA/users/siteEditor}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_own_user_node_THEN_should_be_allowed() throws RepositoryException {
        String siteEditorUserPath = userManagerService.lookupUser(siteEditorUser.getUsername(), siteKey, editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(siteEditorUserPath)));

        assertTrue("Site editor user should be able to read their own user node", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_same_site_user_node_THEN_should_be_allowed() throws RepositoryException {
        String siteUserPath = userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(siteUserPath)));

        assertTrue("Site editor user should be able to read a user in the same site", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /sites/siteB/users/siteUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_different_site_user_node_THEN_should_be_allowed() throws RepositoryException {
        String differentSiteUserPath = userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                .getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(differentSiteUserPath)));

        assertTrue("Site editor user should be able to read a user from a different site", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /users/serverUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_server_user_node_THEN_should_be_allowed() throws RepositoryException {
        String serverUserPath = userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(serverUserPath)));

        assertTrue("Site editor user should be able to read a server user node", canRead);
    }

    // --- Group nodes ---

    /**
     * e.g. {@code /users/serverEditor} can read {@code /sites/siteA/groups/myGroup}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_same_site_group_node_THEN_should_be_allowed() throws RepositoryException {
        String siteGroupPath = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(siteGroupPath)));

        assertTrue("Server editor user should be able to read a group in the same site", canRead);
    }

    /**
     * e.g. {@code /users/serverEditor} can read {@code /sites/siteB/groups/site-administrators}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_different_site_group_node_THEN_should_be_allowed() throws RepositoryException {
        String differentSiteGroupPath = groupManagerService.getAdministratorGroup(differentSiteKey, editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(differentSiteGroupPath)));

        assertTrue("Server editor user should be able to read a group from a different site", canRead);
    }

    /**
     * e.g. {@code /users/serverEditor} can read {@code /groups/users}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_reading_global_group_node_THEN_should_be_allowed() throws RepositoryException {
        String globalGroupPath = groupManagerService.lookupGroup(null, "users", editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> session.nodeExists(globalGroupPath)));

        assertTrue("Server editor user should be able to read the global 'users' group node", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /sites/siteA/groups/myGroup}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_same_site_group_node_THEN_should_be_allowed() throws RepositoryException {
        String siteGroupPath = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(siteGroupPath)));

        assertTrue("Site editor user should be able to read a group in the same site", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /sites/siteB/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_different_site_group_node_THEN_should_be_allowed() throws RepositoryException {
        String differentSiteGroupPath = groupManagerService.getAdministratorGroup(differentSiteKey, editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(differentSiteGroupPath)));

        assertTrue("Site editor user should be able to read a group from a different site", canRead);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can read {@code /groups/users}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_reading_global_group_node_THEN_should_be_allowed() throws RepositoryException {
        String globalGroupPath = groupManagerService.lookupGroup(null, "users", editSystemSession).getPath();

        Boolean canRead = (Boolean) JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> session.nodeExists(globalGroupPath)));

        assertTrue("Site editor user should be able to read the global 'users' group node", canRead);
    }

    // ==================== Users ====================

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_creating_user_node_THEN_should_be_denied() throws RepositoryException {
        String siteUserNodePath = (String) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRUserNode newSiteUser = createSiteUser(session);
                    assertThrows("Editor should not be able to create a site user", AccessDeniedException.class, session::save);
                    return newSiteUser.getPath();
                }));

        assertFalse("Editor should not have been able to create a node under the users folder",
                editSystemSession.nodeExists(siteUserNodePath));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/users/}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_creating_user_node_THEN_should_be_allowed() throws RepositoryException {
        String siteUserNodePath = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    JCRUserNode newSiteUser = createSiteUser(session);
                    session.save();
                    return newSiteUser.getPath();
                }));

        assertTrue("Site administrator should have been able to create a node under the users folder",
                editSystemSession.nodeExists(siteUserNodePath));
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_another_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_another_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    setSiteUserFirstName(session, siteUser.getUsername());
                    assertThrows("Editor should not be able to set a new property of another user node", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        assertFalse("The property should remain unset",
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace)).hasProperty("j:firstName"));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_allowed()
            throws RepositoryException {
        String newFirstName = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    String firstName = setSiteUserFirstName(session, siteUser.getUsername());
                    session.save();
                    return firstName;
                }));

        JCRUserNode userNode = userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession);
        assertNotNull("The user node should exist", userNode);
        assertEquals("The property should be set to the new value", newFirstName, userNode.getProperty("j:firstName").getString());
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can write to {@code /sites/siteA/users/siteEditor} (edit workspace)
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_setting_a_new_property_of_own_user_node_in_edit_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_site_editor_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can write to {@code /sites/siteA/users/siteEditor} (live workspace)
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_setting_a_new_property_of_own_user_node_in_live_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_site_editor_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_site_editor_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(String workspace)
            throws RepositoryException {
        String newFirstName = (String) JCRTemplate.getInstance()
                .doExecute(siteEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    String firstName = setSiteUserFirstName(session, siteEditorUser.getUsername());
                    session.save();
                    return firstName;
                }));

        assertEquals("The property should have been set", newFirstName,
                userManagerService.lookupUser(siteEditorUser.getUsername(), siteKey, systemSession(workspace)).getProperty("j:firstName")
                        .getString());
    }

    /**
     * e.g. {@code /sites/siteA/users/siteUser} can write to {@code /sites/siteA/users/siteUser} (edit workspace)
     */
    @Test
    public void GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_in_edit_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteUser} can write to {@code /sites/siteA/users/siteUser} (live workspace)
     */
    @Test
    public void GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_in_live_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(String workspace)
            throws RepositoryException {
        String newFirstName = (String) JCRTemplate.getInstance()
                .doExecute(siteUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(siteUser, session -> {
                    String firstName = setSiteUserFirstName(session, siteUser.getUsername());
                    session.save();
                    return firstName;
                }));

        assertEquals("The property should have been set", newFirstName,
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace)).getProperty("j:firstName")
                        .getString());
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_updating_an_existing_property_of_another_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_updating_an_existing_property_of_another_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        String originalValue = userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace))
                .getProperty("j:lastName").getString();
        assertNotNull("The property should already exist", originalValue);

        JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    setOtherSiteUserLastName(session);
                    assertThrows("Editor should not be able to update an existing property of another user node",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        assertEquals("The property should remain unchanged", originalValue,
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace)).getProperty("j:lastName")
                        .getString());
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_allowed()
            throws RepositoryException {
        String originalValue = userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession).getProperty("j:lastName")
                .getString();
        assertNotNull("The property should already exist", originalValue);

        String newValue = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    String lastName = setOtherSiteUserLastName(session);
                    session.save();
                    return lastName;
                }));

        assertEquals("The property should be updated to its new value", newValue,
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession).getProperty("j:lastName").getString());
        assertNotEquals("The property should be different from the original value", originalValue, newValue);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_deleting_another_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/users/siteUser} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_deleting_another_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        String userPath = (String) JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRUserNode userNode = userManagerService.lookupUser(siteUser.getUsername(), siteKey, session);
                    assertNotNull("Editor should still be able to read the user node", userNode);
                    userNode.remove();
                    assertThrows("Editor should not be able to delete another user node", AccessDeniedException.class, session::save);
                    return userNode.getPath();
                }));

        assertTrue("Editor should not have been able to delete the user node", systemSession(workspace).nodeExists(userPath));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_deleting_another_user_node_THEN_should_be_allowed() throws RepositoryException {
        String userPath = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    JCRUserNode userNode = userManagerService.lookupUser(siteUser.getUsername(), siteKey, session);
                    assertNotNull("Site administrator should be able to read the user node", userNode);
                    userNode.remove();
                    session.save();
                    return userNode.getPath();
                }));

        assertFalse("Site administrator should have been able to delete the user node", editSystemSession.nodeExists(userPath));
    }

    /**
     * e.g. {@code /users/serverEditor} can write to {@code /users/serverEditor}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_changing_own_user_node_THEN_should_be_allowed() throws RepositoryException {
        String newFirstName = (String) JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRUserNode userNode = userManagerService.lookupUser(serverEditorUser.getUsername(), session);
                    assertNotNull("Server editor user node should be readable to themselves", userNode);
                    String firstName = faker.name().firstName();
                    userNode.setProperty("j:firstName", firstName);
                    session.save();
                    return firstName;
                }));

        JCRUserNode userNode = userManagerService.lookupUser(serverEditorUser.getUsername(), editSystemSession);
        assertNotNull("The server editor user node should exist", userNode);
        assertEquals("j:firstName should have been updated", newFirstName, userNode.getProperty("j:firstName").getString());
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteB/users/siteUser}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_changing_different_site_user_node_THEN_should_be_denied() throws RepositoryException {
        String differentSiteUserPath = userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                .getPath();

        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRUserNode userNode = (JCRUserNode) session.getNode(differentSiteUserPath);
                    userNode.setProperty("j:firstName", faker.name().firstName());
                    assertThrows("Server editor user should not be able to modify a user from a different site",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        assertFalse("j:firstName should not have been set on the different site user",
                userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                        .hasProperty("j:firstName"));
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /users/serverUser}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_changing_server_user_node_THEN_should_be_denied() throws RepositoryException {
        String serverUserPath = userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).getPath();

        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRUserNode userNode = (JCRUserNode) session.getNode(serverUserPath);
                    userNode.setProperty("j:firstName", faker.name().firstName());
                    assertThrows("Server editor user should not be able to modify an unrelated server user node",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        assertFalse("j:firstName should not have been set on the server user",
                userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).hasProperty("j:firstName"));
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteA/users/siteUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_changing_same_site_user_node_THEN_should_be_denied() throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    setSiteUserFirstName(session, siteUser.getUsername());
                    assertThrows("Site editor user should not be able to modify another user in the same site", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        assertFalse("j:firstName should not have been set on the same-site user",
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, editSystemSession).hasProperty("j:firstName"));
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteB/users/siteUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_changing_different_site_user_node_THEN_should_be_denied() throws RepositoryException {
        String differentSiteUserPath = userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                .getPath();

        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRUserNode userNode = (JCRUserNode) session.getNode(differentSiteUserPath);
                    userNode.setProperty("j:firstName", faker.name().firstName());
                    assertThrows("Site editor user should not be able to modify a user from a different site", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        assertFalse("j:firstName should not have been set on the different site user",
                userManagerService.lookupUser(differentSiteUser.getUsername(), differentSiteKey, editSystemSession)
                        .hasProperty("j:firstName"));
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /users/serverUser}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_changing_server_user_node_THEN_should_be_denied() throws RepositoryException {
        String serverUserPath = userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).getPath();

        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRUserNode userNode = (JCRUserNode) session.getNode(serverUserPath);
                    userNode.setProperty("j:firstName", faker.name().firstName());
                    assertThrows("Site editor user should not be able to modify an unrelated server user node", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        assertFalse("j:firstName should not have been set on the server user",
                userManagerService.lookupUser(serverUser.getUsername(), editSystemSession).hasProperty("j:firstName"));
    }

    // ==================== Groups ====================

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_creating_group_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_server_editor_user_WHEN_creating_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_creating_group_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_server_editor_user_WHEN_creating_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_creating_group_node_THEN_should_be_denied(String workspace) throws RepositoryException {
        String siteGroupPath = (String) JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode newSiteGroup = createSiteGroup(session);
                    assertThrows("Editor should not be able to create a site group", AccessDeniedException.class, session::save);
                    return newSiteGroup.getPath();
                }));

        assertFalse("Editor should not have been able to create a node under the groups folder",
                systemSession(workspace).nodeExists(siteGroupPath));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/groups/}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_creating_group_node_THEN_should_be_allowed() throws RepositoryException {
        String siteGroupPath = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    JCRGroupNode newSiteGroup = createSiteGroup(session);
                    session.save();
                    return newSiteGroup.getPath();
                }));

        assertTrue("Site administrator should have been able to create a node under the groups folder",
                editSystemSession.nodeExists(siteGroupPath));
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/site-administrators} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_themselves_to_site_administrators_group_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/site-administrators} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_themselves_to_site_administrators_group_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(serverEditorUser);
                    assertThrows("Editor should not be able to add themself to the site-administrators group", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, systemSession(workspace));
        boolean editorIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> serverEditorUser.getUsername().equals(m.getName()));
        assertFalse("Editor should not have been added to the site-administrators group", editorIsMember);
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_adding_member_to_site_administrators_group_THEN_should_be_allowed()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(siteUser);
                    session.save();
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean siteUserIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> siteUser.getUsername().equals(m.getName()));
        assertTrue("Site administrator should have been able to add a member to the site-administrators group", siteUserIsMember);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/myGroup} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_a_group_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/myGroup} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_a_group_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_server_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    setGroupDescription(session);
                    assertThrows("Editor should not be able to set a new property on a group node", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), systemSession(workspace));
        assertFalse("The property should remain unset", groupNode.hasProperty("j:description"));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/groups/myGroup}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_allowed() throws RepositoryException {
        String newDescription = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    String description = setGroupDescription(session);
                    session.save();
                    return description;
                }));

        JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), editSystemSession);
        assertNotNull("The group node should exist", groupNode);
        assertEquals("The property should be set to the new value", newDescription, groupNode.getProperty("j:description").getString());
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/myGroup} (edit workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_deleting_a_group_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_server_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/myGroup} (live workspace)
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_deleting_a_group_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_server_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_server_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(String workspace) throws RepositoryException {
        String groupPath = (String) JCRTemplate.getInstance()
                .doExecute(serverEditorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), session);
                    assertNotNull("Editor should still be able to read the group node", groupNode);
                    groupNode.remove();
                    assertThrows("Editor should not be able to delete a group node", AccessDeniedException.class, session::save);
                    return groupNode.getPath();
                }));

        assertTrue("Editor should not have been able to delete the group node", systemSession(workspace).nodeExists(groupPath));
    }

    /**
     * e.g. {@code /users/siteAdmin} (site-administrator on siteA) can write to {@code /sites/siteA/groups/myGroup}
     */
    @Test
    public void GIVEN_site_administrator_WHEN_deleting_a_group_node_THEN_should_be_allowed() throws RepositoryException {
        String groupPath = (String) JCRTemplate.getInstance().doExecute(siteAdministratorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteAdministratorUser, session -> {
                    JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), session);
                    assertNotNull("Site administrator should be able to read the group node", groupNode);
                    groupNode.remove();
                    session.save();
                    return groupNode.getPath();
                }));

        assertFalse("Site administrator should have been able to delete the group node", editSystemSession.nodeExists(groupPath));
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_same_site_user_to_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(siteUser);
                    assertThrows("Server editor user should not be able to add a same-site user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean siteUserIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> siteUser.getUsername().equals(m.getName()));
        assertFalse("Same-site user should not have been added to the site-administrators group", siteUserIsMember);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_different_site_user_to_same_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(differentSiteUser);
                    assertThrows("Server editor user should not be able to add a different-site user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean differentSiteUserIsMember = adminGroupNode.getMembers().stream()
                .anyMatch(m -> differentSiteUser.getUsername().equals(m.getName()));
        assertFalse("Different-site user should not have been added to the site-administrators group", differentSiteUserIsMember);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteB/groups/site-administrators}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_different_site_user_to_different_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode differentSiteAdminGroup = groupManagerService.getAdministratorGroup(differentSiteKey, session);
                    if (differentSiteAdminGroup != null) {
                        differentSiteAdminGroup.addMember(differentSiteUser);
                        assertThrows("Server editor user should not be able to add a user to a different site's admin group",
                                AccessDeniedException.class, session::save);
                    }
                    return null;
                }));

        JCRGroupNode differentSiteAdminGroup = groupManagerService.getAdministratorGroup(differentSiteKey, editSystemSession);
        boolean differentSiteUserIsMember = differentSiteAdminGroup.getMembers().stream()
                .anyMatch(m -> differentSiteUser.getUsername().equals(m.getName()));
        assertFalse("Different-site user should not have been added to the different site's admin group", differentSiteUserIsMember);
    }

    /**
     * e.g. {@code /users/serverEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_server_editor_user_WHEN_adding_server_user_to_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(serverEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(serverEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(serverUser);
                    assertThrows("Server editor user should not be able to add a server user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean serverUserIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> serverUser.getUsername().equals(m.getName()));
        assertFalse("Server user should not have been added to the site-administrators group", serverUserIsMember);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(siteEditorUser);
                    assertThrows("Site editor user should not be able to add themselves to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean siteEditorUserIsMember = adminGroupNode.getMembers().stream()
                .anyMatch(m -> siteEditorUser.getUsername().equals(m.getName()));
        assertFalse("Site editor user should not have been added to the site-administrators group", siteEditorUserIsMember);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_adding_same_site_user_to_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(siteUser);
                    assertThrows("Site editor user should not be able to add a same-site user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean siteUserIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> siteUser.getUsername().equals(m.getName()));
        assertFalse("Same-site user should not have been added to the site-administrators group", siteUserIsMember);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_adding_different_site_user_to_same_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(differentSiteUser);
                    assertThrows("Site editor user should not be able to add a different-site user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean differentSiteUserIsMember = adminGroupNode.getMembers().stream()
                .anyMatch(m -> differentSiteUser.getUsername().equals(m.getName()));
        assertFalse("Different-site user should not have been added to the site-administrators group", differentSiteUserIsMember);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteB/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_adding_different_site_user_to_different_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRGroupNode differentSiteAdminGroup = groupManagerService.getAdministratorGroup(differentSiteKey, session);
                    if (differentSiteAdminGroup != null) {
                        differentSiteAdminGroup.addMember(differentSiteUser);
                        assertThrows("Site editor user should not be able to add a user to a different site's admin group",
                                AccessDeniedException.class, session::save);
                    }
                    return null;
                }));

        JCRGroupNode differentSiteAdminGroup = groupManagerService.getAdministratorGroup(differentSiteKey, editSystemSession);
        boolean differentSiteUserIsMember = differentSiteAdminGroup.getMembers().stream()
                .anyMatch(m -> differentSiteUser.getUsername().equals(m.getName()));
        assertFalse("Different-site user should not have been added to the different site's admin group", differentSiteUserIsMember);
    }

    /**
     * e.g. {@code /sites/siteA/users/siteEditor} can NOT write to {@code /sites/siteA/groups/site-administrators}
     */
    @Test
    public void GIVEN_site_editor_user_WHEN_adding_server_user_to_site_administrators_group_THEN_should_be_denied()
            throws RepositoryException {
        JCRTemplate.getInstance().doExecute(siteEditorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(siteEditorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(serverUser);
                    assertThrows("Site editor user should not be able to add a server user to the site-administrators group",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, editSystemSession);
        boolean serverUserIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> serverUser.getUsername().equals(m.getName()));
        assertFalse("Server user should not have been added to the site-administrators group", serverUserIsMember);
    }

    // ==================== Helpers ====================

    private JCRUserNode createSiteUser(JCRSessionWrapper session) throws RepositoryException {
        return userManagerService.createUser(faker.name().username(), siteKey, faker.internet().password(), new Properties(), session);
    }

    private JCRGroupNode createSiteGroup(JCRSessionWrapper session) throws RepositoryException {
        return groupManagerService.createGroup(siteKey, faker.name().name(), new Properties(), false, session);
    }

    /**
     * Sets {@code j:firstName} on the given user's node. Not an update — users are created without a first name.
     */
    private String setSiteUserFirstName(JCRSessionWrapper session, String username) throws RepositoryException {
        JCRUserNode userNode = userManagerService.lookupUser(username, siteKey, session);
        assertNotNull("The user node should exist", userNode);
        String newFirstName = faker.name().firstName();
        userNode.setProperty("j:firstName", newFirstName);
        return newFirstName;
    }

    /**
     * Updates {@code j:lastName} on {@link #siteUser}'s node. Not a creation — the user is created with a last name pre-set.
     */
    private String setOtherSiteUserLastName(JCRSessionWrapper session) throws RepositoryException {
        JCRUserNode userNode = userManagerService.lookupUser(siteUser.getUsername(), siteKey, session);
        assertNotNull("The user node should exist", userNode);
        String newLastName = faker.name().lastName();
        userNode.setProperty("j:lastName", newLastName);
        return newLastName;
    }

    /**
     * Sets {@code j:description} on {@link #siteGroup}'s node. Not an update — the group is created without a description.
     */
    private String setGroupDescription(JCRSessionWrapper session) throws RepositoryException {
        JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), session);
        assertNotNull("The group node should exist", groupNode);
        String description = faker.lorem().sentence();
        groupNode.setProperty("j:description", description);
        return description;
    }

    private void resetSystemSession() throws RepositoryException {
        sessionFactory.closeAllSessions();
        editSystemSession = sessionFactory.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null);
        liveSystemSession = sessionFactory.getCurrentSystemSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH, null);
    }

    private JCRSessionWrapper systemSession(String workspace) throws RepositoryException {
        if (Constants.LIVE_WORKSPACE.equals(workspace)) {
            return liveSystemSession;
        }
        return editSystemSession;
    }
}
