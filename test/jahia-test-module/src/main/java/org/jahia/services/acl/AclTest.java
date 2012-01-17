/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.acl;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AclTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ContentTest.class);

    private final static String TESTSITE_NAME = "aclTestSite";

    private static JahiaUser user1;
    private static JahiaUser user2;
    private static JahiaUser user3;
    private static JahiaUser user4;

    private static JahiaGroup group1;
    private static JahiaGroup group2;
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

    public AclTest() {
    }

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);

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

        user1 = userManager.createUser("user1", "password", new Properties());
        user2 = userManager.createUser("user2", "password", new Properties());
        user3 = userManager.createUser("user3", "password", new Properties());
        user4 = userManager.createUser("user4", "password", new Properties());

        JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);

        group1 = groupManager.createGroup(site.getID(), "group1", new Properties(), false);
        group2 = groupManager.createGroup(site.getID(), "group2", new Properties(), false);

        group1.addMember(user1);
        group1.addMember(user2);

        group2.addMember(user3);
        group2.addMember(user4);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists("/sites/"+TESTSITE_NAME)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }

            JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            groupManager.deleteGroup(group1);
            groupManager.deleteGroup(group2);

            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
            userManager.deleteUser(user1);
            userManager.deleteUser(user2);
            userManager.deleteUser(user3);
            userManager.deleteUser(user4);

        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        home = session.getNodeByIdentifier(homeIdentifier);
        content1 = session.getNodeByIdentifier(content1Identifier);
        content11 = session.getNodeByIdentifier(content11Identifier);
        content12 = session.getNodeByIdentifier(content12Identifier);
        content2 = session.getNodeByIdentifier(content2Identifier);
        content21 = session.getNodeByIdentifier(content21Identifier);
        content22 = session.getNodeByIdentifier(content22Identifier);
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
        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user2", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testGrantGroup() throws Exception {
        content11.grantRoles("g:group1", Collections.singleton("owner"));
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
        session.save();

        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testAclBreak() throws Exception {
        content1.setAclInheritanceBreak(true);

        content11.grantRoles("u:user1", Collections.singleton("owner"));
        session.save();
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(home.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content1.getPath(), "jcr:read"))));
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content11.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(content12.getPath(), "jcr:read"))));
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
