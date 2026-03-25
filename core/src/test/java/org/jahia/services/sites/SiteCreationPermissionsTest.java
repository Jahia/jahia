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
public class SiteCreationPermissionsTest extends AbstractJUnitTest {

    private static JahiaUserManagerService userManagerService;
    private static JahiaGroupManagerService groupManagerService;
    private static JCRSessionFactory sessionFactory;

    /**
     * User with the {@code editor} role on the site. Created once for all tests.
     */
    private static JahiaUser editorUser;
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
     * Editor user at the site level, re-created for each test. Used for self-edit preservation tests.
     */
    private JahiaUser editorSiteUser;
    /**
     * Regular group at the site level, re-created for each test. Acts as the target in group write-access tests.
     */
    private JahiaGroup siteGroup;

    private static final Faker faker = new Faker();

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManagerService = JahiaUserManagerService.getInstance();
        groupManagerService = JahiaGroupManagerService.getInstance();
        sessionFactory = JCRSessionFactory.getInstance();
        resetSystemSession();

        editorUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();
        siteAdministratorUser = userManagerService.createUser(faker.name().username(), faker.internet().password(), new Properties(),
                editSystemSession).getJahiaUser();
        editSystemSession.save();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        assertTrue(userManagerService.deleteUser(userManagerService.lookupUser(editorUser.getUsername()).getPath(), editSystemSession));
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

        Properties editorSiteUserProps = new Properties();
        editorSiteUserProps.put("j:lastName", faker.name().firstName());
        editorSiteUser = userManagerService.createUser(faker.name().username(), siteKey, faker.internet().password(), editorSiteUserProps,
                editSystemSession).getJahiaUser();

        editSystemSession.getNode(site.getJCRLocalPath()).grantRoles("u:" + editorUser.getUsername(), Collections.singleton("editor"));
        editSystemSession.getNode(site.getJCRLocalPath()).grantRoles("u:" + editorSiteUser.getUsername(), Collections.singleton("editor"));

        groupManagerService.getAdministratorGroup(siteKey, editSystemSession).addMember(siteAdministratorUser);

        siteGroup = groupManagerService.createGroup(siteKey, faker.name().name(), new Properties(), false, editSystemSession)
                .getJahiaGroup();
        editSystemSession.save();
        resetSystemSession();
    }

    @After
    public void after() throws Exception {
        TestHelper.deleteSite(siteKey);
    }

    // ==================== Users ====================

    @Test
    public void GIVEN_editor_user_WHEN_creating_user_node_THEN_should_be_denied() throws RepositoryException {
        String siteUserNodePath = (String) JCRTemplate.getInstance().doExecute(editorUser, Constants.EDIT_WORKSPACE, null,
                AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    JCRUserNode newSiteUser = createSiteUser(session);
                    assertThrows("Editor should not be able to create a site user", AccessDeniedException.class, session::save);
                    return newSiteUser.getPath();
                }));

        assertFalse("Editor should not have been able to create a node under the users folder",
                editSystemSession.nodeExists(siteUserNodePath));
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_another_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_another_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_another_user_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    setSiteUserFirstName(session, siteUser.getUsername());
                    assertThrows("Editor should not be able to set a new property of another user node", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        assertFalse("The property should remain unset",
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace)).hasProperty("j:firstName"));
    }

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

    // Self-edit preservation: each user retains write access to their own user node.
    @Test
    public void GIVEN_editor_site_user_WHEN_setting_a_new_property_of_own_user_node_in_edit_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_editor_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_site_user_WHEN_setting_a_new_property_of_own_user_node_in_live_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_editor_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(String workspace)
            throws RepositoryException {
        String newFirstName = (String) JCRTemplate.getInstance()
                .doExecute(editorSiteUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorSiteUser, session -> {
                    String firstName = setSiteUserFirstName(session, editorSiteUser.getUsername());
                    session.save();
                    return firstName;
                }));

        assertEquals("The property should have been set", newFirstName,
                userManagerService.lookupUser(editorSiteUser.getUsername(), siteKey, systemSession(workspace)).getProperty("j:firstName")
                        .getString());
    }

    @Test
    public void GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_in_edit_workspace_THEN_should_be_allowed()
            throws RepositoryException {
        GIVEN_regular_site_user_WHEN_setting_a_new_property_of_own_user_node_THEN_should_be_allowed(Constants.EDIT_WORKSPACE);
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_updating_an_existing_property_of_another_user_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_updating_an_existing_property_of_another_user_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_updating_an_existing_property_of_another_user_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        String originalValue = userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace))
                .getProperty("j:lastName").getString();
        assertNotNull("The property should already exist", originalValue);

        JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    setOtherSiteUserLastName(session);
                    assertThrows("Editor should not be able to update an existing property of another user node",
                            AccessDeniedException.class, session::save);
                    return null;
                }));

        assertEquals("The property should remain unchanged", originalValue,
                userManagerService.lookupUser(siteUser.getUsername(), siteKey, systemSession(workspace)).getProperty("j:lastName")
                        .getString());
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_deleting_another_user_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_deleting_another_user_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_deleting_another_user_node_THEN_should_be_denied(String workspace) throws RepositoryException {
        String userPath = (String) JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    JCRUserNode userNode = userManagerService.lookupUser(siteUser.getUsername(), siteKey, session);
                    assertNotNull("Editor should still be able to read the user node", userNode);
                    userNode.remove();
                    assertThrows("Editor should not be able to delete another user node", AccessDeniedException.class, session::save);
                    return userNode.getPath();
                }));

        assertTrue("Editor should not have been able to delete the user node", systemSession(workspace).nodeExists(userPath));
    }

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

    // ==================== Groups ====================

    @Test
    public void GIVEN_editor_user_WHEN_creating_group_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_creating_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_creating_group_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_creating_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_creating_group_node_THEN_should_be_denied(String workspace) throws RepositoryException {
        String siteGroupPath = (String) JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    JCRGroupNode newSiteGroup = createSiteGroup(session);
                    assertThrows("Editor should not be able to create a site group", AccessDeniedException.class, session::save);
                    return newSiteGroup.getPath();
                }));

        assertFalse("Editor should not have been able to create a node under the groups folder",
                systemSession(workspace).nodeExists(siteGroupPath));
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_adding_themselves_to_site_administrators_group_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_adding_themselves_to_site_administrators_group_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_adding_themselves_to_site_administrators_group_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, session);
                    assertNotNull("The site-administrators group should exist", adminGroupNode);
                    adminGroupNode.addMember(editorUser);
                    assertThrows("Editor should not be able to add themself to the site-administrators group", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        JCRGroupNode adminGroupNode = groupManagerService.getAdministratorGroup(siteKey, systemSession(workspace));
        boolean editorIsMember = adminGroupNode.getMembers().stream().anyMatch(m -> editorUser.getUsername().equals(m.getName()));
        assertFalse("Editor should not have been added to the site-administrators group", editorIsMember);
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_a_group_node_in_edit_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_a_group_node_in_live_workspace_THEN_should_be_denied()
            throws RepositoryException {
        GIVEN_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_setting_a_new_property_of_a_group_node_THEN_should_be_denied(String workspace)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    setGroupDescription(session);
                    assertThrows("Editor should not be able to set a new property on a group node", AccessDeniedException.class,
                            session::save);
                    return null;
                }));

        JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), systemSession(workspace));
        assertFalse("The property should remain unset", groupNode.hasProperty("j:description"));
    }

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

    @Test
    public void GIVEN_editor_user_WHEN_deleting_a_group_node_in_edit_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(Constants.EDIT_WORKSPACE);
    }

    @Test
    public void GIVEN_editor_user_WHEN_deleting_a_group_node_in_live_workspace_THEN_should_be_denied() throws RepositoryException {
        GIVEN_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(Constants.LIVE_WORKSPACE);
    }

    public void GIVEN_editor_user_WHEN_deleting_a_group_node_THEN_should_be_denied(String workspace) throws RepositoryException {
        String groupPath = (String) JCRTemplate.getInstance()
                .doExecute(editorUser, workspace, null, AccessManagerTestUtils.setCurrentUserCallback(editorUser, session -> {
                    JCRGroupNode groupNode = groupManagerService.lookupGroup(siteKey, siteGroup.getName(), session);
                    assertNotNull("Editor should still be able to read the group node", groupNode);
                    groupNode.remove();
                    assertThrows("Editor should not be able to delete a group node", AccessDeniedException.class, session::save);
                    return groupNode.getPath();
                }));

        assertTrue("Editor should not have been able to delete the group node", systemSession(workspace).nodeExists(groupPath));
    }

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
