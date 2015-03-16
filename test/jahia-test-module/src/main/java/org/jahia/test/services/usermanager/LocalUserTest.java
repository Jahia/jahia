/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.test.services.usermanager;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Local user tests
 */
public class LocalUserTest {

    private static final String TESTSITE1_NAME = "localUserTest1";
    private static final String TESTSITE2_NAME = "localUserTest2";
    private static JahiaUserManagerService userManager;
    private static JahiaUser globalUser;
    private static JahiaUser localUser;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
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

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
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
}
