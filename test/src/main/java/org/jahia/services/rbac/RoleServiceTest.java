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

import java.util.LinkedList;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.jcr.PermissionImpl;
import org.jahia.services.rbac.jcr.RoleImpl;
import org.jahia.services.rbac.jcr.RoleService;
import org.jahia.services.sites.JahiaSite;
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
public class RoleServiceTest {

    private static Logger logger = Logger.getLogger(RoleServiceTest.class);

    private static final String PERMISSION_PREFIX = "permission-test-";

    private static int permissionCounter;

    private static final int PERMISSIONS_TO_CREATE = 100;

    private static final String ROLE_PREFIX = "role-test-";

    private static int roleCounter;

    private static final int ROLES_TO_CREATE = 100;

    private static final String TESTSITE_NAME = "roleServiceTest";

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
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    QueryResult result = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:permission] where localname() like '" + PERMISSION_PREFIX + "%'", Query.JCR_SQL2).execute();
                    for (NodeIterator iterator = result.getNodes(); iterator.hasNext();) {
                        iterator.nextNode().remove();
                    }
                    QueryResult result2 = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:role] where localname() like '" + ROLE_PREFIX + "%'", Query.JCR_SQL2).execute();
                    for (NodeIterator iterator = result2.getNodes(); iterator.hasNext();) {
                        iterator.nextNode().remove();
                    }
                    session.save();
                    return true;
                }
            });
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

    private RoleService getRoleService() {
        return (RoleService) SpringContextSingleton.getBean(RoleService.class.getName());
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLookupService() throws Exception {
        assertNotNull("Unable to lookup RoleService instance", getRoleService());
    }

    @Test
    public void testServerPermissionCreate() throws Exception {
        String name = getNextPermissionName();
        Permission permission = new PermissionIdentity(name, "myGroup", null);
        getRoleService().savePermission(permission);

        PermissionImpl read = getRoleService().getPermission(permission);

        assertNotNull("The permission was not saved", read);
        assertEquals("The permission group is wrong", "myGroup", read.getGroup());
        assertEquals("The JCR path of the persisted permission is wrong", "/permissions/myGroup/"
                + permission.getName(), read.getPath());
    }

    @Test
    public void testServerPermissionCreateDefGroup() throws Exception {
        String name = getNextPermissionName();
        Permission permission = new PermissionIdentity(name);
        getRoleService().savePermission(permission);

        PermissionImpl read = getRoleService().getPermission(permission);

        assertNotNull("The permission was not saved", read);
        assertEquals("The permission group is wrong", "global", read.getGroup());
        assertEquals("The JCR path of the persisted permission is wrong",
                "/permissions/global/" + permission.getName(), read.getPath());
    }

    @Test
    public void testServerPermissionCreateMany() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE; i++) {
            getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        }
    }

    @Test
    public void testServerPermissionCreateManyWithGroups() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE / 20; i++) {
            for (int j = 0; j < 20; j++) {
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName(), "group-" + j, null));
            }
        }
    }

    @Test
    public void testServerRoleCreateEmpty() throws Exception {
        String name = getNextRoleName();
        Role role = new RoleIdentity(name);
        getRoleService().saveRole(role);
        RoleImpl read = getRoleService().getRole(role);
        assertNotNull("The role was not saved", read);
        assertEquals("The role name is wrong", role.getName(), read.getName());
        assertEquals("The JCR path of the persisted role is wrong", "/roles/" + role.getName(), read.getPath());
    }

    @Test
    public void testServerRoleCreateMany() throws Exception {
        for (int i = 0; i < ROLES_TO_CREATE; i++) {
            getRoleService().saveRole(new RoleIdentity(getNextRoleName()));
        }
    }

    @Test
    public void testServerRoleCreateWithPermissions() throws Exception {
        String name = getNextRoleName();
        RoleImpl role = new RoleImpl(name);
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));

        getRoleService().saveRole(role);

        RoleImpl read = getRoleService().getRole(role);

        assertNotNull("The role was not saved", read);
        assertEquals("The role name is wrong", role.getName(), read.getName());
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), read.getPermissions()
                .size());
        assertNotNull("The permissions are not persisted in the role", getRoleService().getPermission(
                role.getPermissions().iterator().next()));
    }

    @Test
    public void testServerRoleGrantPermissions() throws Exception {
        String name = getNextRoleName();
        Role role = new RoleIdentity(name);
        getRoleService().saveRole(role);

        RoleImpl savedRole = getRoleService().getRole(role);

        // grant "read" permission
        Permission readPermisison = new PermissionIdentity(getNextPermissionName());
        readPermisison = getRoleService().savePermission(readPermisison);
        // persist the permission
        getRoleService().grantPermission(role, readPermisison);

        savedRole = getRoleService().getRole(role);

        // check if the "read" permission is there
        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 1);
        assertTrue("The permission is not correctly granted", savedRole.getPermissions().contains(readPermisison));

        // grant "write" permission
        Permission writePermisison = new PermissionIdentity(getNextPermissionName());
        writePermisison = getRoleService().savePermission(writePermisison);
        // persist the permission
        getRoleService().grantPermission(role, writePermisison);

        // read the role
        savedRole = getRoleService().getRole(role);

        // check if both "read" and "write" permissions are there
        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 2);
        assertTrue("The permission is not correctly granted", savedRole.getPermissions().contains(readPermisison));
        assertTrue("The permission is not correctly granted", savedRole.getPermissions().contains(writePermisison));
    }

    @Test
    public void testServerRoleGrantPermissionsByUpdate() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        RoleImpl savedRole = getRoleService().getRole(originalRole);

        // grant by updating the role
        PermissionImpl readPermission = new PermissionImpl(getNextPermissionName());
        savedRole.getPermissions().add(readPermission);

        getRoleService().saveRole(savedRole);

        savedRole = getRoleService().getRole(originalRole);

        // check if the "read" permission is there
        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 1);
        assertTrue("The permission is not correctly persisted by updating the role", savedRole.getPermissions()
                .contains(readPermission));
    }

    @Test
    public void testServerRoleGrantPermissionsMultiple() throws Exception {
        String name = getNextRoleName();
        Role role = new RoleIdentity(name);
        getRoleService().saveRole(role);

        RoleImpl savedRole = getRoleService().getRole(role);

        List<Permission> perms = new LinkedList<Permission>();
        perms.add(new PermissionIdentity(getNextPermissionName()));
        perms.add(new PermissionIdentity(getNextPermissionName()));
        perms.add(new PermissionIdentity(getNextPermissionName()));

        // persist permissions
        for (Permission permission : perms) {
            getRoleService().savePermission(permission);
        }

        getRoleService().grantPermissions(role, perms);

        savedRole = getRoleService().getRole(role);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 3);
    }

    @Test
    public void testServerRoleGrantPermissionsNonExisting() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        // try granting to a non-existing role
        Exception ex = null;
        try {
            getRoleService().grantPermission(new RoleIdentity("non-existing"),
                    new PermissionIdentity(getNextPermissionName()));
        } catch (PathNotFoundException e) {
            ex = e;
        }
        assertNotNull("A permission cannot be granted to a non-existing role", ex);

        // try granting unsaved permission
        Permission unsavedPermission = new PermissionIdentity(getNextPermissionName());
        // persist the permission
        getRoleService().grantPermission(originalRole, unsavedPermission);
        assertFalse("Unsaved permission should not be grated", getRoleService().getRole(originalRole).getPermissions()
                .contains(unsavedPermission));
    }

    @Test
    public void testServerRoleRevokePermissions() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        Permission read = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission write = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission admin = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission unknown = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        List<Permission> perms = new LinkedList<Permission>();
        perms.add(read);
        perms.add(write);
        perms.add(admin);
        perms.add(unknown);

        getRoleService().grantPermissions(originalRole, perms);

        RoleImpl savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 4);

        // revoke one permission
        getRoleService().revokePermission(originalRole, write);

        savedRole = getRoleService().getRole(originalRole);
        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 3);
        assertFalse("The permission is still present", savedRole.getPermissions().contains(write));
    }

    @Test
    public void testServerRoleRevokePermissionsAll() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));
        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));
        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));

        RoleImpl savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 3);

        // revoke all permissions
        getRoleService().revokeAllPermissions(originalRole);

        savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 0);
    }

    @Test
    public void testServerRoleRevokePermissionsMultiple() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        Permission read = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission write = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission admin = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        Permission unknown = getRoleService().savePermission(new PermissionIdentity(getNextPermissionName()));
        List<Permission> perms = new LinkedList<Permission>();
        perms.add(read);
        perms.add(write);
        perms.add(admin);
        perms.add(unknown);

        getRoleService().grantPermissions(originalRole, perms);

        RoleImpl savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 4);

        // revoke multiple permissions
        List<Permission> toRevoke = new LinkedList<Permission>();
        toRevoke.add(write);
        toRevoke.add(unknown);

        getRoleService().revokePermissions(originalRole, toRevoke);

        savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 2);
        assertFalse("The permission is still present", savedRole.getPermissions().contains(write));
        assertFalse("The permission is still present", savedRole.getPermissions().contains(unknown));
    }

    @Test
    public void testServerRoleRevokePermissionsNonExisting() throws Exception {
        String name = getNextRoleName();
        Role originalRole = new RoleIdentity(name);
        getRoleService().saveRole(originalRole);

        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));
        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));
        getRoleService().grantPermission(originalRole,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));

        RoleImpl savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 3);

        // revoke non existing permission
        getRoleService().revokePermission(originalRole, new PermissionIdentity(getNextPermissionName()));

        savedRole = getRoleService().getRole(originalRole);

        assertEquals("The permission count in the role is wrong", savedRole.getPermissions().size(), 3);

        // try revoking from a non-existing role
        Exception ex = null;
        try {
            getRoleService().revokePermission(new RoleIdentity("non-existing"),
                    new PermissionIdentity(getNextPermissionName()));
        } catch (PathNotFoundException e) {
            ex = e;
        }
        assertNotNull("A permission cannot be revoked from a non-existing role", ex);

        // try revoking from a non-existing role
        Exception ex2 = null;
        try {
            getRoleService().revokeAllPermissions(new RoleIdentity("non-existing"));
        } catch (PathNotFoundException e) {
            ex2 = e;
        }
        assertNotNull("All permissions cannot be revoked from a non-existing role", ex2);
    }

    @Test
    public void testSitePermissionCreate() throws Exception {
        String name = getNextPermissionName();
        Permission permission = new PermissionIdentity(name, "myGroup", TESTSITE_NAME);
        getRoleService().savePermission(permission);

        PermissionImpl read = getRoleService().getPermission(permission);

        assertNotNull("The permission was not saved", read);
        assertEquals("The permission group is wrong", "myGroup", read.getGroup());
        assertEquals("The permission site is wrong", TESTSITE_NAME, read.getSite());
        assertEquals("The JCR path of the persisted permission is wrong", "/sites/" + TESTSITE_NAME
                + "/permissions/myGroup/" + permission.getName(), read.getPath());
    }

    @Test
    public void testSitePermissionCreateDefGroup() throws Exception {
        String name = getNextPermissionName();
        Permission permission = new PermissionIdentity(name, null, TESTSITE_NAME);
        getRoleService().savePermission(permission);

        PermissionImpl read = getRoleService().getPermission(permission);

        assertNotNull("The permission was not saved", read);
        assertEquals("The permission group is wrong", "global", read.getGroup());
        assertEquals("The permission site is wrong", TESTSITE_NAME, read.getSite());
        assertEquals("The JCR path of the persisted permission is wrong", "/sites/" + TESTSITE_NAME
                + "/permissions/global/" + permission.getName(), read.getPath());
    }

    @Test
    public void testSitePermissionCreateMany() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE; i++) {
            getRoleService().savePermission(new PermissionIdentity(getNextPermissionName(), null, TESTSITE_NAME));
        }
    }

    @Test
    public void testSitePermissionCreateManyWithGroups() throws Exception {
        for (int i = 0; i < PERMISSIONS_TO_CREATE / 20; i++) {
            for (int j = 0; j < 20; j++) {
                getRoleService().savePermission(
                        new PermissionIdentity(getNextPermissionName(), "group-" + j, TESTSITE_NAME));
            }
        }
    }

    @Test
    public void testSiteRoleCreateEmpty() throws Exception {
        String name = getNextRoleName();
        Role role = new RoleIdentity(name, TESTSITE_NAME);
        getRoleService().saveRole(role);
        RoleImpl read = getRoleService().getRole(role);
        assertNotNull("The role was not saved", read);
        assertEquals("The role name is wrong", role.getName(), read.getName());
        assertEquals("The role site is wrong", role.getSite(), read.getSite());
        assertEquals("The JCR path of the persisted role is wrong", "/sites/" + TESTSITE_NAME + "/roles/"
                + role.getName(), read.getPath());
    }

    @Test
    public void testSiteRoleCreateMany() throws Exception {
        for (int i = 0; i < ROLES_TO_CREATE; i++) {
            getRoleService().saveRole(new RoleIdentity(getNextRoleName(), TESTSITE_NAME));
        }
    }

    @Test
    public void testSiteRoleCreateWithPermissions() throws Exception {
        String name = getNextRoleName();
        RoleImpl role = new RoleImpl(name, TESTSITE_NAME);
        role.getPermissions().add(new PermissionImpl(getNextPermissionName(), "myGroup1"));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName(), "myGroup2"));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName()));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName(), "myGroup1", TESTSITE_NAME));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName(), "myGroup2", TESTSITE_NAME));
        role.getPermissions().add(new PermissionImpl(getNextPermissionName(), null, TESTSITE_NAME));

        getRoleService().saveRole(role);

        RoleImpl read = getRoleService().getRole(role);

        assertNotNull("The role was not saved", read);
        assertEquals("The role name is wrong", role.getName(), read.getName());
        assertEquals("The role site is wrong", role.getSite(), read.getSite());
        assertEquals("The permission count in the role is wrong", role.getPermissions().size(), read.getPermissions()
                .size());
        assertNotNull("The permissions are not persisted in the role", getRoleService().getPermission(
                role.getPermissions().iterator().next()));
    }
}
