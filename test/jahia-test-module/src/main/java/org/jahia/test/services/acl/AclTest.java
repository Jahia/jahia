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
package org.jahia.test.services.acl;

import static org.assertj.core.api.Assertions.*;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.jahia.test.services.content.*;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AclTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ContentTest.class);

    private final static String TESTSITE_NAME = "aclTestSite";

    private static JCRUserNode user1;
    private static JCRUserNode user2;
    private static JCRUserNode user3;
    private static JCRUserNode user4;

    private static JCRGroupNode group1;
    private static JCRGroupNode group2;
    public static final String HOMEPATH = "/sites/"+TESTSITE_NAME+"/home";

    public static JCRPublicationService jcrService;

    private static JCRNodeWrapper home;
    private static JCRNodeWrapper content1;
    private static JCRNodeWrapper content11;
    private static JCRNodeWrapper content12;
    private static JCRNodeWrapper content2;
    private static JCRNodeWrapper content21;
    private static JCRNodeWrapper content22;
    private static String homeIdentifier;
    private JCRSessionWrapper session;
    static String content1Identifier;
    private static String content11Identifier;
    private static String content12Identifier;
    private static String content2Identifier;
    private static String content21Identifier;
    private static String content22Identifier;

    private static void assertRole(JCRNodeWrapper node, String principal, String grantType, String role) {
        Map<String, List<String[]>> aclEntries = node.getAclEntries();
        String path = node.getPath();
        assertThat(aclEntries)
                .as("ACL entries for node %s should contain %s for role for principal %s", path, grantType, role, principal)
                .containsKey(principal);
        assertThat(aclEntries.get(principal).get(0))
        .as("ACL entries for node %s should contain %s for role for principal %s", path, grantType, role, principal)
                .containsExactly(path, grantType, role);
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, TestHelper.DX_BASE_DEMO_TEMPLATES);

        jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

        Set<String> languages = null;

        home = session.getNode(HOMEPATH);
        homeIdentifier = home.getIdentifier();
        content1 = home.addNode("content1", "jnt:contentList");
        content1Identifier = content1.getIdentifier();
        content11 = content1.addNode("content1.1", "jnt:contentList");
        content11Identifier = content11.getIdentifier();
        content12 = content1.addNode("content1.2", "jnt:contentList");
        content12Identifier = content12.getIdentifier();
        content2 = home.addNode("content2", "jnt:contentList");
        content2Identifier = content2.getIdentifier();
        content21 = content2.addNode("content2.1", "jnt:contentList");
        content21Identifier = content21.getIdentifier();
        content22 = content2.addNode("content2.2", "jnt:contentList");
        content22Identifier = content22.getIdentifier();
        session.save();

        final JahiaUserManagerService userMgr = ServicesRegistry
                .getInstance().getJahiaUserManagerService();

        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        user1 = userManager.createUser("user1", "password", new Properties(), session);
        user2 = userManager.createUser("user2", "password", new Properties(), session);
        user3 = userManager.createUser("user3", "password", new Properties(), session);
        user4 = userManager.createUser("user4", "password", new Properties(), session);

        JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);

        group1 = groupManager.createGroup(site.getSiteKey(), "group1", new Properties(), false, session);
        group2 = groupManager.createGroup(site.getSiteKey(), "group2", new Properties(), false, session);

        group1.addMember(user1);
        group1.addMember(user2);

        group2.addMember(user3);
        group2.addMember(user4);
        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists("/sites/"+TESTSITE_NAME)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }

            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
            userManager.deleteUser(user1.getPath(), session);
            userManager.deleteUser(user2.getPath(), session);
            userManager.deleteUser(user3.getPath(), session);
            userManager.deleteUser(user4.getPath(), session);
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        home = session.getNodeByIdentifier(homeIdentifier);
        home.getAclEntries();
        content1 = session.getNodeByIdentifier(content1Identifier);
        content1.getAclEntries();
        content11 = session.getNodeByIdentifier(content11Identifier);
        content11.getAclEntries();
        content12 = session.getNodeByIdentifier(content12Identifier);
        content12.getAclEntries();
        content2 = session.getNodeByIdentifier(content2Identifier);
        content2.getAclEntries();
        content21 = session.getNodeByIdentifier(content21Identifier);
        content21.getAclEntries();
        content22 = session.getNodeByIdentifier(content22Identifier);
        content22.getAclEntries();
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        home.revokeAllRoles();
        content1.revokeAllRoles();
        content11.revokeAllRoles();
        content12.revokeAllRoles();
        content2.revokeAllRoles();
        content21.revokeAllRoles();
        content21.revokeAllRoles();
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDefaultReadRight() throws Exception {
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(HOMEPATH, "jcr:read"))));
    }

    @Test
    public void testGrantUser() throws Exception {
        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user2", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testGrantGroup() throws Exception {
        content11.grantRoles("g:group1", Collections.singleton("owner"));

        assertRole(content11, "g:group1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user2", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user3", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user4", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testDenyUser() throws Exception {
        content1.grantRoles("u:user1", Collections.singleton("owner"));
        content11.denyRoles("u:user1", Collections.singleton("owner"));
        assertRole(content1, "u:user1", "GRANT", "owner");
        assertRole(content11, "u:user1", "DENY", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testAclBreak() throws Exception {
        assertThat(content1.getAclEntries()).as("ACL entries for node %s should NOT be empty", content1.getPath()).isNotEmpty();

        content1.setAclInheritanceBreak(true);

        assertThat(content1.getAclEntries()).as("ACL entries for node %s should be empty", content1.getPath()).isEmpty();

        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");
        assertThat(content11.getAclEntries()).as("ACL entries for node %s should contains %s role for user %s", content11.getPath(),
                "owner", "user1").containsOnlyKeys("u:user1");

        session.save();
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(home.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:read"))));
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content12.getPath(), "jcr:read"))));
    }

    @Test
    public void testRevokeRoles() throws Exception {
        content11.grantRoles("u:user1", Collections.singleton("owner"));
        content11.grantRoles("u:user2", Collections.singleton("owner"));
        assertRole(content11, "u:user1", "GRANT", "owner");
        assertRole(content11, "u:user2", "GRANT", "owner");
        session.save();

        content11.revokeRolesForPrincipal("u:user2");
        assertRole(content11, "u:user1", "GRANT", "owner");
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user2")
                .doesNotContainKey("u:user2");

        session.save();

        content11.revokeAllRoles();
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user1")
                .doesNotContainKey("u:user1");
        assertThat(content11.getAclEntries())
                .as("ACL entries for node %s should NOT contain roles for principal %s", content11.getPath(), "u:user2")
                .doesNotContainKey("u:user2");

        session.save();
    }

    @Test
    public void testPrivilegedAccess() throws Exception {
        // Test case for the https://jira.jahia.org/browse/QA-9762

        assertFalse("user1 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user1"));
        assertFalse("user3 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user3"));

        assertFalse("user1 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user1"));
        assertFalse("user3 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user3"));

        // grant group1 an editor role on home page
        home.grantRoles("g:group1", Collections.singleton("editor"));
        session.save();

        assertTrue("user1 should have access to home page in edit mode", nodeExists(home.getPath(), "user1"));
        assertFalse("user3 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user3"));

        assertTrue("user1 should have access to site in edit mode", nodeExists(home.getParent().getPath(), "user1"));
        assertFalse("user3 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user3"));

        // revoke an editor role on home page from group1 and grant it to user1 directly
        home.revokeRolesForPrincipal("g:group1");
        home.grantRoles("u:user1", Collections.singleton("editor"));
        session.save();

        assertTrue("user1 should have access to home page in edit mode", nodeExists(home.getPath(), "user1"));
        assertFalse("user2 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user2"));
        assertFalse("user3 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user3"));

        assertTrue("user1 should have access to site in edit mode", nodeExists(home.getParent().getPath(), "user1"));
        assertFalse("user2 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user2"));
        assertFalse("user3 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user3"));

        // revoke an editor role on home page from user1 and grant her editor-in-chief role
        home.revokeRolesForPrincipal("u:user1");
        home.grantRoles("u:user1", Collections.singleton("editor-in-chief"));
        session.save();

        assertTrue("user1 should have access to home page in edit mode", nodeExists(home.getPath(), "user1"));
        assertTrue("user1 should have access to site in edit mode", nodeExists(home.getParent().getPath(), "user1"));

        // revoke all roles on home page from user1
        home.revokeRolesForPrincipal("u:user1");
        session.save();

        assertFalse("user1 should NOT have access to home page in edit mode", nodeExists(home.getPath(), "user1"));
        assertFalse("user1 should NOT have access to site in edit mode", nodeExists(home.getParent().getPath(), "user1"));
    }
    
    private boolean nodeExists(String path, String user) throws Exception {
        return doInJcrAsUser(user, session -> {
            try {
                session.getNode(path);
                return Boolean.TRUE;
            } catch (PathNotFoundException e) {
                return Boolean.FALSE;
            }
        });
    }

    private <T> T doInJcrAsUser(String user, JCRCallback<T> callback) throws Exception {
        return JCRTemplate.getInstance().doExecute(user, null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, callback);
    }

    class CheckPermission implements JCRCallback<Boolean> {
        private String path;
        private String permission;

        CheckPermission(String path, String permission) {
            this.path = path;
            this.permission = permission;
        }

        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
            try {
                return session.getNode(path).hasPermission(permission);
            } catch (PathNotFoundException e) {
                return false;
            }
        }
    }


}
