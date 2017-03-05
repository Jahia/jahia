/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.acl;

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
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class AclIT extends AbstractJUnitTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(AclIT.class);

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

    public AclIT() {
    }

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, null);

        jcrService = ServicesRegistry.getInstance().getJCRPublicationService();

        JCRSessionWrapper session = jcrService.getSessionFactory().getCurrentUserSession();

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

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        
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
        assertFalse((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(HOMEPATH, "jcr:read"))));
    }

    @Test
    public void testGrantUser() throws Exception {
        content11.grantRoles("u:user1", Collections.singleton("owner"));
        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user2", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testGrantGroup() throws Exception {
        content11.grantRoles("g:group1", Collections.singleton("owner"));
        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertTrue((JCRTemplate.getInstance().doExecute("user2", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user3", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user4", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testDenyUser() throws Exception {
        content1.grantRoles("u:user1", Collections.singleton("owner"));
        content11.denyRoles("u:user1", Collections.singleton("owner"));
        session.save();

        assertTrue((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content1.getPath(), "jcr:write"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content11.getPath(), "jcr:write"))));
    }

    @Test
    public void testAclBreak() throws Exception {
        content1.setAclInheritanceBreak(true);

        content11.grantRoles("u:user1", Collections.singleton("owner"));
        session.save();
        assertFalse((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(home.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content1.getPath(), "jcr:read"))));
        assertTrue((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content11.getPath(), "jcr:read"))));
        assertFalse((JCRTemplate.getInstance().doExecute("user1", null, null, null, new CheckPermission(content12.getPath(), "jcr:read"))));
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
