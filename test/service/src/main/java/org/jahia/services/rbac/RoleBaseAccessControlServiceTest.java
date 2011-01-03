/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
import java.util.Set;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
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

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(RoleBaseAccessControlServiceTest.class);

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
    public void testLookupService() throws Exception {
        assertNotNull("Unable to lookup RoleService instance", getRoleService());
        assertNotNull("Unable to lookup RoleBasedAccessControlService instance", getRBACService());
    }


}