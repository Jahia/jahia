package org.jahia.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.junit.*;
import org.slf4j.Logger;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Group manager unit test. This unit test is not yet complete, it was only implemented to test some marginal cases.
 *
 */
public class JahiaGroupManagerServiceTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaGroupManagerServiceTest.class);

    private static JahiaUserManagerService userManager;
    private static JahiaGroupManagerService groupManager;

    private static JahiaUser user1;
    private static JahiaUser user2;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);
        groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);

        // now let's create some users and groups.
        user1 = userManager.createUser("test-user1", "password", new Properties());
        user2 = userManager.createUser("test-user2", "password", new Properties());

    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        userManager.deleteUser(user1);
        userManager.deleteUser(user2);
    }


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGroupMembership() {

        JahiaGroup group1 = groupManager.createGroup(0, "test-group1", new Properties(), false);
        group1.addMember(user1);
        JahiaGroup group2 = groupManager.createGroup(0, "test-group2", new Properties(), false);
        group2.addMember(user2);
        group1.addMember(group1);

        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2", user1.isMemberOfGroup(0, "test-group2"));
        List<String> user1GroupMembership = groupManager.getUserMembership(user1);
        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2", user1GroupMembership.contains("test-group2:0"));

        group1.removeMember(user1);
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.", user1.isMemberOfGroup(0, "test-group2"));
        user1GroupMembership = groupManager.getUserMembership(user1);
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.", user1GroupMembership.contains("test-group2:0"));

        groupManager.deleteGroup(group2);
        groupManager.deleteGroup(group1);

    }
}
