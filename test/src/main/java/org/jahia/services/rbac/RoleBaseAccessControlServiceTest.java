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
import java.util.Properties;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.jcr.RoleBasedAccessControlService;
import org.jahia.services.rbac.jcr.RoleService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRGroupManagerProvider;
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
public class RoleBaseAccessControlServiceTest {

    private static Logger logger = Logger.getLogger(RoleBaseAccessControlServiceTest.class);

    private static final String PERMISSION_PREFIX = "permission-test-";

    private static int permissionCounter;

    private static final String ROLE_PREFIX = "role-test-";

    private static int roleCounter;

    private static int siteId;

    private static final String TESTSITE_NAME = "rbacServiceTest";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull("Unable to create test site", site);
        roleCounter = 1;
        permissionCounter = 1;
        siteId = site.getID();
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
                    QueryResult result = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [jnt:permission] where localname() like '" + PERMISSION_PREFIX + "%'",
                            Query.JCR_SQL2).execute();
                    for (NodeIterator iterator = result.getNodes(); iterator.hasNext();) {
                        iterator.nextNode().remove();
                    }
                    QueryResult result2 = session.getWorkspace().getQueryManager().createQuery(
                            "select * from [jnt:role] where localname() like '" + ROLE_PREFIX + "%'", Query.JCR_SQL2)
                            .execute();
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

    private JahiaGroup group1;

    private JahiaGroup group2;

    private JahiaGroup group3;

    private Role role1;

    private Role role2;

    private Role role3;

    private JahiaUser user1;

    private String getNextPermissionName() {
        return PERMISSION_PREFIX + permissionCounter++;
    }

    private String getNextRoleName() {
        return ROLE_PREFIX + roleCounter++;
    }

    private RoleBasedAccessControlService getRBACService() {
        return (RoleBasedAccessControlService) SpringContextSingleton.getBean(RoleBasedAccessControlService.class
                .getName());
    }

    private RoleService getRoleService() {
        return (RoleService) SpringContextSingleton.getBean(RoleService.class.getName());
    }

    @Before
    public void setUp() throws RepositoryException {
        // create role1 with one permission
        role1 = new RoleIdentity(getNextRoleName());
        getRoleService().saveRole(role1);
        getRoleService().grantPermission(role1,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));

        // create role2 with two permissions
        role2 = new RoleIdentity(getNextRoleName());
        getRoleService().saveRole(role2);
        getRoleService().grantPermission(role2,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));
        getRoleService().grantPermission(role2,
                getRoleService().savePermission(new PermissionIdentity(getNextPermissionName())));

        // create role3
        role3 = new RoleIdentity(getNextRoleName());
        getRoleService().saveRole(role3);

        user1 = JCRUserManagerProvider.getInstance().createUser("role-tester-1", "password", new Properties());

        group1 = JCRGroupManagerProvider.getInstance().createGroup(siteId, "group-role-testers-1", null, false);

        group2 = JCRGroupManagerProvider.getInstance().createGroup(siteId, "group-role-testers-2", null, false);

        group3 = JCRGroupManagerProvider.getInstance().createGroup(siteId, "group-role-testers-3", null, false);
    }

    @After
    public void tearDown() {
        if (user1 != null) {
            ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(user1);
        }
        if (group1 != null) {
            ServicesRegistry.getInstance().getJahiaGroupManagerService().deleteGroup(group1);
        }
        if (group2 != null) {
            ServicesRegistry.getInstance().getJahiaGroupManagerService().deleteGroup(group2);
        }
        if (group3 != null) {
            ServicesRegistry.getInstance().getJahiaGroupManagerService().deleteGroup(group3);
        }
    }

    @Test
    public void testHasRoleDirect() throws Exception {

        getRBACService().grantRole(user1, role1);
        getRBACService().grantRole(group1, role1);
        getRBACService().grantRole(group1, role2);
        assertTrue("role1 is not granted to the user1", user1.hasRole(role1));
        assertTrue("role1 is not granted to the group1", group1.hasRole(role1));
        assertTrue("role2 is not granted to the group1", group1.hasRole(role2));

        // not assigned role
        assertFalse("role3 is granted to the user1", user1.hasRole(role3));
        assertFalse("role3 is granted to the group1", group1.hasRole(role3));

        // non-existing roles are evaluated to false by default
        assertFalse("unknownRole is granted to the user1", user1.hasRole(new RoleIdentity("unknownRole")));
        assertFalse("unknownRole is granted to the group1", group1.hasRole(new RoleIdentity("unknownRole")));
    }

    @Test
    public void testHasRoleInherited() throws Exception {
        getRBACService().grantRole(user1, role1);

        getRBACService().grantRole(group1, role1);
        getRBACService().grantRole(group1, role2);

        getRBACService().grantRole(group2, role2);

        getRBACService().grantRole(group3, role3);

        group2.addMember(user1);

        assertTrue("role1 is not granted to the user1", user1.hasRole(role1));
        assertTrue("role1 is not granted to the group1", group1.hasRole(role1));
        assertTrue("role2 is not granted to the group1", group1.hasRole(role2));
        assertTrue("role2 is not granted to the group2", group2.hasRole(role2));
        assertTrue("role3 is not granted to the group3", group3.hasRole(role3));

        assertTrue("role2 is not granted to the user1 (through membership)", user1.hasRole(role2));

        assertFalse("role3 should not be granted to the user1 (through membership)", user1.hasRole(role3));

        group3.addMember(user1);

        assertTrue("role3 is not granted to the user1 (through membership)", user1.hasRole(role3));
    }

    @Test
    public void testLookupService() throws Exception {
        assertNotNull("Unable to lookup RoleService instance", getRoleService());
        assertNotNull("Unable to lookup RoleBasedAccessControlService instance", getRBACService());
    }

    @Test
    public void testRoleGrant() throws Exception {
        getRBACService().grantRole(user1, role1);
        getRBACService().grantRole(group1, role1);
        getRBACService().grantRole(group1, role2);
        List<JahiaPrincipal> principals = getRBACService().getPrincipalsInRole(role1);
        List<JahiaPrincipal> principals2 = getRBACService().getPrincipalsInRole(role2);
        assertTrue("role1 is not granted to the user1", principals.contains(user1));
        assertTrue("role1 is not granted to the group1", principals.contains(group1));
        assertFalse("role2 is granted to the user1", principals2.contains(user1));
        assertEquals("role2 is granted to two more than one principal", principals2.size(), 1);
    }

    @Test
    public void testRoleGrantMultiple() throws Exception {

        List<Role> roles = new LinkedList<Role>();
        roles.add(role1);
        roles.add(role2);
        getRBACService().grantRoles(user1, roles);
        List<JahiaPrincipal> principals = getRBACService().getPrincipalsInRole(role1);
        assertEquals("role1 is granted to more than one principal", principals.size(), 1);
        assertTrue("role1 is not granted to the user1", principals.contains(user1));
        assertTrue("role2 is not granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));
        assertFalse("role1 is granted to the user2", principals.contains(group1));
    }

    @Test
    public void testRoleRevoke() throws Exception {
        List<Role> roles = new LinkedList<Role>();
        roles.add(role1);
        roles.add(role2);
        getRBACService().grantRoles(user1, roles);

        assertTrue("role1 is not granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
        assertTrue("role2 is not granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));

        // revoking role2
        getRBACService().revokeRole(user1, role2);

        assertFalse("role2 is still granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));
        assertTrue("role1 is not granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
    }

    @Test
    public void testRoleRevokeAll() throws Exception {
        List<Role> roles = new LinkedList<Role>();
        roles.add(role1);
        roles.add(role2);
        getRBACService().grantRoles(user1, roles);

        assertTrue("role1 is not granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
        assertTrue("role2 is not granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));

        // revoking all
        getRBACService().revokeAllRoles(user1);

        assertFalse("role1 is still granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
        assertFalse("role2 is still granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));
    }

    @Test
    public void testRoleRevokeMultiple() throws Exception {
        List<Role> roles = new LinkedList<Role>();
        roles.add(role1);
        roles.add(role2);
        roles.add(role3);
        getRBACService().grantRoles(user1, roles);

        assertTrue("role1 is not granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
        assertTrue("role2 is not granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));
        assertTrue("role3 is not granted to the user1", getRBACService().getPrincipalsInRole(role3).contains(user1));

        // revoking role1 and role2
        List<Role> revoked = new LinkedList<Role>();
        revoked.add(role1);
        revoked.add(role3);
        getRBACService().revokeRoles(user1, revoked);

        assertFalse("role1 is still granted to the user1", getRBACService().getPrincipalsInRole(role1).contains(user1));
        assertFalse("role3 is still granted to the user1", getRBACService().getPrincipalsInRole(role3).contains(user1));
        assertTrue("role2 is not granted to the user1", getRBACService().getPrincipalsInRole(role2).contains(user1));
    }

}