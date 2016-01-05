/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.usermanager;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Local user tests
 */
public class LocalUserIT extends AbstractJUnitTest {

    private static final String TESTSITE1_NAME = "localUserTest1";
    private static final String TESTSITE2_NAME = "localUserTest2";
    private static JahiaUserManagerService userManager;
    private static JahiaUser globalUser;
    private static JahiaUser localUser;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        userManager = JahiaUserManagerService.getInstance();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);
        JahiaSite site1 = TestHelper.createSite(TESTSITE1_NAME);
        assertNotNull(site1);
        JahiaSite site2 = TestHelper.createSite(TESTSITE2_NAME);
        assertNotNull(site2);
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRUserNode g = userManager.createUser("globalUser", "password", new Properties(), session);
                assertNotNull(g);
                JCRUserNode l = userManager.createUser("localUser", TESTSITE1_NAME, "password", new Properties(), session);
                assertNotNull(l);
                globalUser = g.getJahiaUser();
                localUser = l.getJahiaUser();
                JCRNodeWrapper site = session.getNode("/sites/" + TESTSITE1_NAME);
                site.grantRoles("u:globalUser", Collections.singleton("editor"));
                site.grantRoles("u:localUser", Collections.singleton("editor"));
                session.save();
                return null;
            }
        });
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (session.nodeExists(globalUser.getLocalPath())) {
                    userManager.deleteUser(globalUser.getLocalPath(), session);
                }
                if (session.nodeExists(localUser.getLocalPath())) {
                    userManager.deleteUser(localUser.getLocalPath(), session);
                }
                session.save();
                return null;
            }
        });
        TestHelper.deleteSite(TESTSITE1_NAME);
        TestHelper.deleteSite(TESTSITE2_NAME);
    }

    @Test
    public void testPermissions() throws Exception {
        JCRTemplate.getInstance().doExecute(globalUser, "default", Locale.ENGLISH, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                session.getNode("/sites/" + TESTSITE1_NAME).addNode("test1", "jnt:text");
                session.save();
                JCRNodeWrapper node = session.getNode("/sites/" + TESTSITE1_NAME + "/test1");
                assertNotNull(node);
                return null;
            }
        });

        JCRTemplate.getInstance().doExecute(localUser, "default", Locale.ENGLISH, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                session.getNode("/sites/" + TESTSITE1_NAME).addNode("test2", "jnt:text");
                session.save();
                JCRNodeWrapper node = session.getNode("/sites/" + TESTSITE1_NAME + "/test2");
                assertNotNull(node);
                return null;
            }
        });

        JCRTemplate.getInstance().doExecute(localUser, "default", Locale.ENGLISH, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                boolean creationFailed = false;
                try {
                    session.getNode("/sites/" + TESTSITE2_NAME).addNode("test3", "jnt:text");
                    session.save();
                } catch (Exception e) {
                    creationFailed = true;
                }
                assertTrue("Local user from a site should not have access to another site", creationFailed);
                return null;
            }
        });
    }
    
    @Test
    public void testLookup() {
        JahiaUserManagerService userService = JahiaUserManagerService.getInstance();
        
        assertEquals("Lookup for global user by name failed", globalUser.getUserKey(),
                userService.lookupUser(globalUser.getUsername()).getUserKey());
        assertEquals("Lookup for global user by path failed", globalUser.getUserKey(),
                userService.lookupUserByPath(globalUser.getUserKey()).getUserKey());
        
        assertEquals("Lookup for local user by path failed", localUser.getUserKey(),
                userService.lookupUserByPath(localUser.getUserKey()).getUserKey());
        assertEquals("Lookup for local user by name and site failed", localUser.getUserKey(),
                userService.lookupUser(localUser.getUsername(), localUser.getRealm()).getUserKey());
        
        // Detect the lookup type
        // by path
        assertEquals("Lookup for global user by path failed", globalUser.getUserKey(),
                userService.lookup(globalUser.getUserKey()).getUserKey());
        // by name
        assertEquals("Lookup for global user by name failed", globalUser.getUserKey(),
                userService.lookup(globalUser.getUsername()).getUserKey());
        // by legacy user key 
        assertEquals("Lookup for global user by name failed", globalUser.getUserKey(),
                userService.lookup("{jcr}" + globalUser.getUsername()).getUserKey());
    }
}