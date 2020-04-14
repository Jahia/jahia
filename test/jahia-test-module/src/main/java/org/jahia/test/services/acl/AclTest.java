/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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

import com.google.common.collect.ImmutableMap;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AclTest {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ContentTest.class);

    private static final String TESTSITE_NAME = "aclTestSite";

    public static final String SITEPATH = "/sites/" + TESTSITE_NAME;
    public static final String HOMEPATH = SITEPATH + "/home";
    public static final String GROUP1 = "group1";
    public static final String GROUP2 = "group2";
    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    public static final String USER3 = "user3";
    public static final String USER4 = "user4";
    
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

        JCRPublicationService jcrService = ServicesRegistry.getInstance().getJCRPublicationService();
        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();

        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

        JCRNodeWrapper home = session.getNode(HOMEPATH);
        homeIdentifier = home.getIdentifier();
        JCRNodeWrapper content1 = home.addNode("content1", Constants.JAHIANT_CONTENTLIST);
        content1Identifier = content1.getIdentifier();
        JCRNodeWrapper content11 = content1.addNode("content1.1", Constants.JAHIANT_CONTENTLIST);
        content11Identifier = content11.getIdentifier();
        JCRNodeWrapper content12 = content1.addNode("content1.2", Constants.JAHIANT_CONTENTLIST);
        content12Identifier = content12.getIdentifier();
        JCRNodeWrapper content2 = home.addNode("content2", Constants.JAHIANT_CONTENTLIST);
        content2Identifier = content2.getIdentifier();
        JCRNodeWrapper content21 = content2.addNode("content2.1", Constants.JAHIANT_CONTENTLIST);
        content21Identifier = content21.getIdentifier();
        JCRNodeWrapper content22 = content2.addNode("content2.2", Constants.JAHIANT_CONTENTLIST);
        content22Identifier = content22.getIdentifier();
        session.save();

        JCRUserNode user1 = userService.createUser(USER1, "password", new Properties(), session);
        JCRUserNode user2 = userService.createUser(USER2, "password", new Properties(), session);
        JCRUserNode user3 = userService.createUser(USER3, "password", new Properties(), session);
        JCRUserNode user4 = userService.createUser(USER4, "password", new Properties(), session);

        JCRGroupNode group1 = groupService.createGroup(site.getSiteKey(), GROUP1, new Properties(), false, session);
        JCRGroupNode group2 = groupService.createGroup(site.getSiteKey(), GROUP2, new Properties(), false, session);

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
            userManager.deleteUser(userManager.getUserPath(USER1), session);
            userManager.deleteUser(userManager.getUserPath(USER2), session);
            userManager.deleteUser(userManager.getUserPath(USER3), session);
            userManager.deleteUser(userManager.getUserPath(USER4), session);
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper home = session.getNodeByIdentifier(homeIdentifier);
        home.getAclEntries();
        JCRNodeWrapper content1 = session.getNodeByIdentifier(content1Identifier);
        content1.getAclEntries();
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
        content11.getAclEntries();
        JCRNodeWrapper content12 = session.getNodeByIdentifier(content12Identifier);
        content12.getAclEntries();
        JCRNodeWrapper content2 = session.getNodeByIdentifier(content2Identifier);
        content2.getAclEntries();
        JCRNodeWrapper content21 = session.getNodeByIdentifier(content21Identifier);
        content21.getAclEntries();
        JCRNodeWrapper content22 = session.getNodeByIdentifier(content22Identifier);
        content22.getAclEntries();
        session.save();
    }

    @After
    public void tearDown() throws Exception {
        session.getNodeByIdentifier(homeIdentifier).revokeAllRoles();
        session.getNodeByIdentifier(content1Identifier).revokeAllRoles();
        session.getNodeByIdentifier(content11Identifier).revokeAllRoles();
        session.getNodeByIdentifier(content12Identifier).revokeAllRoles();
        session.getNodeByIdentifier(content2Identifier).revokeAllRoles();
        session.getNodeByIdentifier(content21Identifier).revokeAllRoles();
        session.getNodeByIdentifier(content22Identifier).revokeAllRoles();
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDefaultReadRight() throws Exception {
        assertFalse((JCRTemplate.getInstance().doExecute(USER1, null, null, null, new CheckPermission(HOMEPATH, "jcr:read"))));
    }

    @Test
    public void testGrantUser() throws Exception {
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER2, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testGrantGroup() throws Exception {
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
        content11.grantRoles("g:group1", Collections.singleton("owner"));

        assertRole(content11, "g:group1", "GRANT", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertTrue((JCRTemplate.getInstance().doExecute(USER2, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER3, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER4, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testDenyUser() throws Exception {
        JCRNodeWrapper content1 = session.getNodeByIdentifier(content1Identifier);
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
        content1.grantRoles("u:user1", Collections.singleton("owner"));
        content11.denyRoles("u:user1", Collections.singleton("owner"));
        assertRole(content1, "u:user1", "GRANT", "owner");
        assertRole(content11, "u:user1", "DENY", "owner");

        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content1.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testAclBreak() throws Exception {
        JCRNodeWrapper content1 = session.getNodeByIdentifier(content1Identifier);
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
        JCRNodeWrapper content12 = session.getNodeByIdentifier(content12Identifier);
        assertThat(content1.getAclEntries()).as("ACL entries for node %s should NOT be empty", content1.getPath()).isNotEmpty();

        content1.setAclInheritanceBreak(true);

        assertThat(content1.getAclEntries()).as("ACL entries for node %s should be empty", content1.getPath()).isEmpty();

        content11.grantRoles("u:user1", Collections.singleton("owner"));

        assertRole(content11, "u:user1", "GRANT", "owner");
        assertThat(content11.getAclEntries()).as("ACL entries for node %s should contains %s role for user %s", content11.getPath(),
                "owner", "user1").containsOnlyKeys("u:user1");

        session.save();
        assertFalse((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(HOMEPATH, "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content1.getPath(), "jcr:read"))));
        assertTrue((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content11.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecute(USER1, null,  null, null, new CheckPermission(content12.getPath(), "jcr:read"))));
    }

    @Test
    public void testRevokeRoles() throws Exception {
        JCRNodeWrapper content11 = session.getNodeByIdentifier(content11Identifier);
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
    // Test case for the https://jira.jahia.org/browse/QA-9762
    public void testPrivilegedAccess() throws Exception {
        assertAccess(ImmutableMap.of("user1", false, "user3", false));
        
        JCRNodeWrapper home = session.getNodeByIdentifier(homeIdentifier);

        // grant group1 an editor role on home page
        home.grantRoles("g:group1", Collections.singleton("editor"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user3", false));

        // revoke an editor role on home page from group1 and grant it to user1 directly
        home.revokeRolesForPrincipal("g:group1");
        home.grantRoles("u:user1", Collections.singleton("editor"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user2", false, "user3", false));

        // revoke an editor role on home page from user1 and grant her editor-in-chief role
        home.revokeRolesForPrincipal("u:user1");
        home.grantRoles("u:user1", Collections.singleton("editor-in-chief"));
        session.save();

        assertAccess(ImmutableMap.of("user1", true, "user2", false, "user3", false));

        // revoke all roles on home page from user1
        home.revokeRolesForPrincipal("u:user1");
        session.save();

        assertAccess(ImmutableMap.of("user1", false, "user2", false, "user3", false));

        home.grantRoles("g:group1", Collections.singleton("editor"));
        home.revokeRolesForPrincipal("g:group1");
        session.save();

        assertAccess(ImmutableMap.of("user1", false, "user2", false, "user3", false));
    }

    private static void assertAccess(ImmutableMap<String, Boolean> expectations) throws Exception {
        for (Map.Entry<String, Boolean> expectationEntry : expectations.entrySet()) {

            String principal = expectationEntry.getKey();
            Boolean shouldHaveAccess = expectationEntry.getValue();

            assertThat(isUserPrivileged(principal))
                    .as("%s should %sbe in privileged group", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);

            assertThat(nodeExists(HOMEPATH, principal))
                    .as("%s should %shave access to home page in edit mode", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);
            assertThat(nodeExists(SITEPATH, principal))
                    .as("%s should %shave access to site in edit mode", principal, shouldHaveAccess ? "" : "NOT ")
                    .isEqualTo(shouldHaveAccess);
        }
    }

    private static boolean isUserPrivileged(String user) throws Exception {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            return ServicesRegistry.getInstance().getJahiaGroupManagerService()
                    .lookupGroup(TESTSITE_NAME, JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, session)
                    .isMember(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user, session));
        });
    }

    private static boolean nodeExists(String path, String user) throws Exception {
        return doInJcrAsUser(user, session -> {
            try {
                session.getNode(path);
                return Boolean.TRUE;
            } catch (PathNotFoundException e) {
                return Boolean.FALSE;
            }
        });
    }

    private static <T> T doInJcrAsUser(String user, JCRCallback<T> callback) throws Exception {
        return JCRTemplate.getInstance().doExecute(user, null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, callback);
    }

    private static class CheckPermission implements JCRCallback<Boolean> {

        private String path;
        private String permission;

        CheckPermission(String path, String permission) {
            this.path = path;
            this.permission = permission;
        }

        @Override
        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
            try {
                return session.getNode(path).hasPermission(permission);
            } catch (PathNotFoundException e) {
                return false;
            }
        }
    }
}
