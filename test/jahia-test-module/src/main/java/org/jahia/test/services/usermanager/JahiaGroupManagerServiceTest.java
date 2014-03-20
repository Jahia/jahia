/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.test.services.usermanager;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.junit.*;
import org.slf4j.Logger;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Group manager unit test. This unit test is not yet complete, it was only implemented to test some marginal cases.
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
        JahiaGroup group = groupManager.lookupGroup(0, "test-group1");
        if (group != null) {
            groupManager.deleteGroup(group);
        }

        group = groupManager.lookupGroup(0, "test-group2");
        if (group != null) {
            groupManager.deleteGroup(group);
        }
    }

    @Test
    public void testGroupDelete() {
        JahiaGroup group1 = groupManager.createGroup(0, "test-group1", new Properties(), false);
        group1.addMember(user1);
        JahiaGroup group2 = groupManager.createGroup(0, "test-group2", new Properties(), false);
        group2.addMember(user2);
        group2.addMember(group1);

        groupManager.deleteGroup(group2);
        groupManager.deleteGroup(group1);

        group1 = groupManager.lookupGroup("test-group1:0");
        assertNull("Group 1 should have been deleted but is still available !", group1);
        group2 = groupManager.lookupGroup("test-group2:0");
        assertNull("Group 1 should have been deleted but is still available !", group2);
    }

    @Test
    public void testGroupMembership() {

        JahiaGroup group1 = groupManager.createGroup(0, "test-group1", new Properties(), false);
        group1.addMember(user1);
        JahiaGroup group2 = groupManager.createGroup(0, "test-group2", new Properties(), false);
        group2.addMember(user2);
        group2.addMember(group1);

        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2",
                user1.isMemberOfGroup(0, "test-group2"));
        List<String> user1GroupMembership = groupManager.getUserMembership(user1);
        assertTrue("User 1 should be a transitive member of group2, as group1 is a member of group 2",
                user1GroupMembership.contains("test-group2:0"));

        group1.removeMember(user1);
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.",
                user1.isMemberOfGroup(0, "test-group2"));
        user1GroupMembership = groupManager.getUserMembership(user1);
        assertFalse("User 1 should no longer be a transitive member of group2, as we have just removed it.",
                user1GroupMembership.contains("test-group2:0"));

        groupManager.deleteGroup(group2);
        groupManager.deleteGroup(group1);

    }

    @Test
    public void testSameNameUserAndGroup() {

        JahiaGroup group1 = groupManager.createGroup(0, "test-group1", new Properties(), false);
        group1.addMember(user1);
        JahiaGroup user1Group = groupManager.createGroup(0, "test-user1", new Properties(), false);
        group1.addMember(user1Group);

        group1 = groupManager.lookupGroup("test-group1:0");
        Collection<Principal> members = group1.getMembers();

        assertTrue("Test group 1 should contain user called 'test-user1'", members.contains(user1));
        assertTrue("Test group 1 should contain group called 'test-user1'", members.contains(user1Group));

        groupManager.deleteGroup(user1Group);
        groupManager.deleteGroup(group1);
    }
}
