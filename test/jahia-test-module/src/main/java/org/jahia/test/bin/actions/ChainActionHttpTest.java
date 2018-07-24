/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.bin.actions;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.JahiaTestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * HTTP-based test for the ChainAction.
 * 
 * @author Sergiy Shyrkov
 */
public class ChainActionHttpTest extends JahiaTestCase {

    private final static String PASSWORD = "password";

    private final static String USERNAME = "chainActionHttpTestUser";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(USERNAME, PASSWORD, new Properties(),
                session);
        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JahiaUserManagerService userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        userManagerService.deleteUser(userManagerService.lookupUser(USERNAME).getPath(), session);
        session.save();
    }

    private boolean loggedIn;

    @After
    public void afterTestLogout() throws IOException {
        if (loggedIn) {
            logout();
        }
    }

    protected void doLogin() throws IOException {
        login(USERNAME, PASSWORD);
        loggedIn = true;
    }

    @Test
    public void testChainAction() throws Exception {
        doLogin();
        String content = getAsText(
                "/sites/systemsite/home.chain.do?chainOfAction=testPingPublicFirst,testPingPublicSecond");
        assertTrue("Chain of public actions should return two action names",
                content.contains("pong testPingPublicFirst") && content.contains("pong testPingPublicSecond"));
    }

    @Test
    public void testChainActionGuest() throws Exception {
        String content = getAsText(
                "/sites/systemsite/home.chain.do?chainOfAction=testPingPublicFirst,testPingPublicSecond");
        assertTrue("Chain of public actions should return two action names",
                content.contains("pong testPingPublicFirst") && content.contains("pong testPingPublicSecond"));
    }

    @Test
    public void testChainActionMix() throws Exception {
        doLogin();
        String content = getAsText(
                "/sites/systemsite/home.chain.do?chainOfAction=testPingPublicFirst,testPingProtected,testPingPublicSecond");
        assertTrue("Chain of mixed actions should return three action names",
                content.contains("pong testPingPublicFirst") && content.contains("pong testPingProtected")
                        && content.contains("pong testPingPublicSecond"));
    }

    @Test
    public void testChainActionMixGuest() throws Exception {
        assertTrue("Chain of mixed actions should return 401 and login page", getAsText(
                "/sites/systemsite/home.chain.do?chainOfAction=testPingPublicFirst,testPingProtected,testPingPublicSecond",
                401).contains("name=\"loginForm\""));
    }

    @Test
    public void testSingleAction() throws Exception {
        doLogin();
        assertTrue("Public action should return its name",
                getAsText("/sites/systemsite/home.testPingPublicFirst.do").contains("pong testPingPublicFirst"));
        assertTrue("Public action should return its name",
                getAsText("/sites/systemsite/home.testPingPublicSecond.do").contains("pong testPingPublicSecond"));

        assertTrue("Protected action should return its name",
                getAsText("/sites/systemsite/home.testPingProtected.do").contains("pong testPingProtected"));
    }

    @Test
    public void testSingleActionGuest() throws Exception {
        assertTrue("Public action should return its name",
                getAsText("/sites/systemsite/home.testPingPublicFirst.do").contains("pong testPingPublicFirst"));
        assertTrue("Public action should return its name",
                getAsText("/sites/systemsite/home.testPingPublicSecond.do").contains("pong testPingPublicSecond"));

        assertTrue("Protected action should return 401 and login page",
                getAsText("/sites/systemsite/home.testPingProtected.do", 401).contains("name=\"loginForm\""));
    }
}
