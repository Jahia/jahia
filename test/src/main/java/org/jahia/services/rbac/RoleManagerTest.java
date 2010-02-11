/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.rbac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.rbac.jcr.JCRPermission;
import org.jahia.services.rbac.jcr.JCRRole;
import org.jahia.services.rbac.jcr.SystemRoleManager;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for the role and permission management features.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleManagerTest {

    private static Logger logger = Logger.getLogger(RoleManagerTest.class);

    private static final String PERMISSION_PREFIX = "permission-";

    private static int permissionCounter;

    private static final int PERMISSIONS_TO_CREATE = 100;

    private static final String ROLE_PREFIX = "role-";

    private static int roleCounter;

    private static final int ROLES_TO_CREATE = 100;

    private static final String TESTSITE_NAME = "roleManagerTest";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull("Unable to create test site", site);
        roleCounter = 1;
        permissionCounter = 1;
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    private String getNextPermissionName() {
        return PERMISSION_PREFIX + permissionCounter++;
    }

    private String getNextRoleName() {
        return ROLE_PREFIX + roleCounter++;
    }

    private SystemRoleManager getService() {
        return (SystemRoleManager) SpringContextSingleton.getBean("org.jahia.services.rbac.jcr.SystemRoleManager");
    }
    
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLookupService() throws Exception {
        assertNotNull("Unable to lookup RoleManager service instance", getService());
    }

    @Test
    public void testServerPermissionCreate() throws Exception {
        String name = getNextPermissionName();
        JCRPermission permission = new JCRPermission(name, "myGroup");
        getService().savePermission(permission);
        assertNotNull("The permission group is wrong", "myGroup".equals(permission.getGroup()));
        assertNotNull("The persisted permission path is null", permission.getPath());
        assertEquals("The JCR path of the persisted permission is wrong", "/permissions/myGroup/" + name, permission
                .getPath());
    }

    @Test
    public void testServerPermissionCreateDefGroup() throws Exception {
        String name = getNextPermissionName();
        JCRPermission permission = new JCRPermission(name);
        getService().savePermission(permission);
        assertNotNull("The persisted permission path is null", permission.getPath());
        assertNotNull("The persisted permission default group is wrong", "global".equals(permission.getGroup()));
        assertEquals("The JCR path of the persisted permission is wrong", "/permissions/global/" + name, permission
                .getPath());
    }

    @Test
    public void testServerPermissionRead() throws Exception {
        String name = getNextPermissionName();
        JCRPermission permission = new JCRPermission(name, "myGroup");
        getService().savePermission(permission);
        assertNotNull("The permission group is wrong", "myGroup".equals(permission.getGroup()));
        assertNotNull("The persisted permission path is null", permission.getPath());
        assertEquals("The JCR path of the persisted permission is wrong", "/permissions/myGroup/" + name, permission
                .getPath());
    }

    @Test
    public void testServerRoleCreateEmpty() throws Exception {
        String name = getNextRoleName();
        JCRRole role = new JCRRole(name);
        getService().saveRole(role);
        assertNotNull("The persisted role path is null", role.getPath());
        assertEquals("The JCR path of the persisted role is wrong", "/roles/" + name, role.getPath());
    }

    @Test
    public void testServerRoleCreateMany() throws Exception {
        for (int i = 0; i < ROLES_TO_CREATE; i++) {
            getService().saveRole(new JCRRole(getNextRoleName()));
        }
    }

    @Test
    public void testServerPermissionCreateMany() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE; i++) {
            getService().savePermission(new JCRPermission(getNextPermissionName()));
        }
    }

    @Test
    public void testServerPermissionCreateManyWithGroups() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE / 20; i++) {
            for (int j = 0; j < 20; j++) {
                getService().savePermission(new JCRPermission(getNextPermissionName(), "group-" + j));
            }
        }
    }

    @Test
    public void testServerRoleCreateWithPermissions() throws Exception {
        String name = getNextRoleName();
        JCRRole role = new JCRRole(name);
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));

        getService().saveRole(role);

        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), 5);
        assertNotNull("The permissions are not persisted in the role", role.getPermissions().iterator().next()
                .getPath());
    }

    @Test
    public void testServerRoleGrantPermissions() throws Exception {
        String name = getNextRoleName();
        JCRRole role = new JCRRole(name);
        getService().saveRole(role);

        // grant by updating the role
        JCRPermission read = new JCRPermission(getNextPermissionName());
        role.getPermissions().add(read);

        getService().saveRole(role);

        // read the role
        role = getService().getRole(name, (String) null);

        // check if the "read" permission is there
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), 1);
        assertTrue("The permission is not correctly persisted by updating the role", role.getPermissions().contains(
                read));

        // grant "write" permission
        JCRPermission write = new JCRPermission(getNextPermissionName());
        // persist the permission
        getService().savePermission(write);
        getService().grantPermission(role.getPath(), write.getPath());

        // read the role
        role = getService().getRole(name, (String) null);

        // check if the "write" permission is there
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), 2);
        assertTrue("The permission is not correctly persisted by updating the role", role.getPermissions().contains(
                write));

    }

    @Test
    public void testServerRoleRevokePermissions() throws Exception {
        String name = getNextRoleName();
        JCRRole role = new JCRRole(name);
        JCRPermission read = new JCRPermission(getNextPermissionName());
        JCRPermission write = new JCRPermission(getNextPermissionName());
        role.getPermissions().add(read);
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(write);
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));

        getService().saveRole(role);

        // read the role
        role = getService().getRole(name, (String) null);

        // check if permissions are there
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), 4);
        assertTrue("The permission is not correctly persisted by updating the role", role.getPermissions().contains(
                read));
        assertTrue("The permission is not correctly persisted by updating the role", role.getPermissions().contains(
                write));

        // revoke "write" permission
        getService().revokePermission(role.getPath(), write.getPath());

        // read the role
        role = getService().getRole(name, (String) null);

        // check if the "write" permission was removed
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), 3);
        assertFalse("The permission is not revoked", role.getPermissions().contains(write));
    }

    @Test
    public void testServerRoleRead() throws Exception {
        String name = getNextRoleName();
        JCRRole role = new JCRRole(name);
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role.getPermissions().add(new JCRPermission(getNextPermissionName()));

        getService().saveRole(role);

        JCRRole readRole = getService().getRole(name, (String) null);

        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), readRole
                .getPermissions().size());
        assertNotNull("The permissions are not persisted in the role", readRole.getPermissions().iterator().next()
                .getPath());
    }
    
    @Test
    public void testRolePrincipalAssignment() throws Exception {
        JCRRole role1 = new JCRRole(getNextRoleName());
        role1.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role1.getPermissions().add(new JCRPermission(getNextPermissionName()));

        getService().saveRole(role1);

        JCRRole role2 = new JCRRole(getNextRoleName());
        role2.getPermissions().add(new JCRPermission(getNextPermissionName()));
        role2.getPermissions().add(new JCRPermission(getNextPermissionName()));

        getService().saveRole(role2);
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();

        List<JahiaPrincipal> users = new ArrayList<JahiaPrincipal>();
        users.add(session.getUser());

        testRoleAssignmentForPrincipals(role1, role2, users, session);
        testRoleAssignmentForPrincipals(role1, role2, users, null);

        JahiaUser user = JCRUserManagerProvider.getInstance().createUser(
                "role-tester", "password", new Properties());
        try {
            users.clear();
            users.add(user);

            testRoleAssignmentForPrincipals(role1, role2, users, session);
            testRoleAssignmentForPrincipals(role1, role2, users, null);

            users.add(session.getUser());

            testRoleAssignmentForPrincipals(role1, role2, users, session);
            testRoleAssignmentForPrincipals(role1, role2, users, null);
        } finally {
            if (user != null) {
                JCRUserManagerProvider.getInstance().deleteUser(user);
            }
        }
    }

    private void testRoleAssignmentForPrincipals(JCRRole role1, JCRRole role2,
            List<JahiaPrincipal> users, JCRSessionWrapper currentSession)
            throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        List<String> allRoles = new ArrayList<String>();
        allRoles.add(role1.getPath());
        allRoles.add(role2.getPath());
        List<String> allRoleNames = new ArrayList<String>();
        allRoleNames.add(role1.getName());
        allRoleNames.add(role2.getName());

        List<JahiaPrincipal> principalsInRole = getService()
                .getPrincipalsInRole(role1.getPath(),
                        currentSession != null ? currentSession : session);

        assertTrue("No principals should be assigned to role1",
                principalsInRole.size() == 0);
        for (JahiaPrincipal user : users) {
            assertFalse("Current principal should not have role1 assigned",
                    user.hasRole(role1.getName()) && !isRootUser(user));
            assertFalse("Current principal should not have role2 assigned",
                    user.hasRole(role1.getName()) && !isRootUser(user));            
        }
        
        for (JahiaPrincipal user : users) {
            if (currentSession != null) {
                getService().grantRole(user, role1.getPath(), currentSession);
            } else {
                getService().grantRole(user, role1.getPath());
            }
        }
        principalsInRole = getService().getPrincipalsInRole(role1.getPath(),
                currentSession != null ? currentSession : session);

        assertTrue("Current user should be assigned to role1", principalsInRole
                .size() > 0
                && principalsInRole.containsAll(users));
        for (JahiaPrincipal user : users) {
            assertTrue("Current principal should have role1 assigned", user
                    .hasRole(role1.getName()));
            assertFalse("Current principal should not have role2 assigned", user
                    .hasRole(role2.getName()) && !isRootUser(user));            
        }

        for (JahiaPrincipal user : users) {
            if (currentSession != null) {
                getService().revokeRole(user, role1.getPath(), currentSession);
            } else {
                getService().revokeRole(user, role1.getPath());
            }
        }
        principalsInRole = getService().getPrincipalsInRole(role1.getPath(),
                currentSession != null ? currentSession : session);

        assertTrue("No principals should be assigned to role1",
                principalsInRole.size() == 0);
        for (JahiaPrincipal user : users) {
            assertFalse("Current principal should not have role1 assigned",
                    user.hasRole(role1.getName()) && !isRootUser(user));
            assertFalse("Current principal should not have role2 assigned",
                    user.hasRole(role2.getName()) && !isRootUser(user));            
        }

        for (JahiaPrincipal user : users) {
            if (currentSession != null) {
                getService().grantRoles(user, allRoles, currentSession);
            } else {
                getService().grantRoles(user, allRoles);
            }
        }
        principalsInRole = getService().getPrincipalsInRole(role2.getPath(),
                currentSession != null ? currentSession : session);

        assertTrue("Current users should be assigned to role2",
                principalsInRole.size() > 0
                        && principalsInRole.containsAll(users));
        for (JahiaPrincipal user : users) {
            assertTrue("Current principal should have role1 assigned", user
                    .hasRole(role1.getName()));
            assertTrue("Current principal should have role2 assigned", user
                    .hasRole(role2.getName()));
        }

        for (JahiaPrincipal user : users) {
            if (currentSession != null) {
                getService().revokeRoles(user, allRoles, currentSession);
            } else {
                getService().revokeRoles(user, allRoles);
            }
        }
        principalsInRole = getService().getPrincipalsInRole(role2.getPath(),
                currentSession != null ? currentSession : session);

        assertTrue("No principals should be assigned to role2",
                principalsInRole.size() == 0);
        for (JahiaPrincipal user : users) {
            assertFalse(
                    "Current principal should not have role1 assigned",
                    user.hasRole(role1.getName()) && !isRootUser(user));
            assertFalse(
                    "Current principal should not have role2 assigned",
                    user.hasRole(role2.getName()) && !isRootUser(user));
        }
    }
    
    private boolean isRootUser(JahiaPrincipal user) {
        boolean isRootUser = false;
        if (user instanceof JCRUser) {
            isRootUser = ((JCRUser) user).isRoot();
        }
        return isRootUser;
    }
}
